package com.qx.fight;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.template.Action;
import com.manu.dynasty.template.LianMengKeJi;
import com.manu.dynasty.template.Purchase;
import com.manu.dynasty.template.Skill;
import com.manu.dynasty.template.YunBiaoSafe;
import com.manu.dynasty.template.YunbiaoTemp;
import com.manu.network.BigSwitch;
import com.manu.network.SessionAttKey;
import com.qx.alliance.building.JianZhuMgr;
import com.qx.buff.BuffMgr;
import com.qx.buff.SkillActionType;
import com.qx.buff.SkillDamageInfo;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.world.FightNPC;
import com.qx.world.FightScene;
import com.qx.world.Player;
import com.qx.world.Scene;
import com.qx.world.TowerNPC;
import com.qx.world.YaBiaoScene;
import com.qx.yabiao.YBBattleBean;
import com.qx.yabiao.YaBiaoHuoDongMgr;
import com.qx.yabiao.YaBiaoRobot;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

import qxmobile.protobuf.AllianceFightProtos.FightAttackReq;
import qxmobile.protobuf.AllianceFightProtos.FightAttackResp;
import qxmobile.protobuf.AllianceFightProtos.PlayerReviveNotify;
import qxmobile.protobuf.AllianceFightProtos.PlayerReviveRequest;
import qxmobile.protobuf.AllianceFightProtos.Result;

public class FightMgr {
	//用这个来控制是否检测距离。
	public static int extrSkillDist = 200;
	public static FightMgr inst;
		
	public static Logger logger = LoggerFactory.getLogger(FightMgr.class.getSimpleName());
	
	/** 玩家技能冷却map  <junzhuId, map<skillId, endTime>> */
	public Map<Long, Map<Integer, Long>> skillCDTimeMap = new HashMap<Long, Map<Integer,Long>>();

	/** 玩家马车第一次被攻击记录<君主id, 第一次被攻击时间> */
	public Map<Long, Set<Long>> cartInjuredFirstRecord = new HashMap<Long, Set<Long>>();
	
	public FightMgr() {
		inst = this;
	}
	
	public synchronized void activeFight(int id, IoSession session, Builder builder) {
		FightAttackReq.Builder request = (qxmobile.protobuf.AllianceFightProtos.FightAttackReq.Builder) builder;
		int targetUid = request.getTargetUid();			// 被攻击者的uid
		int skillId = request.getSkillId();				// 使用的技能id
//		skillId = 121;
		
		int attackUid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
		Player targetPlayer = scene.players.get(targetUid);
		Player attackPlayer = scene.players.get(attackUid);
		if(targetPlayer == null) {
			logger.error("攻击失败，被攻击的玩家uid:{}不在场景里", targetUid);
			return;
		}
		if(attackPlayer == null) {
			logger.error("攻击失败，发起攻击的玩家uid:{}不在场景里", attackUid);
			return;
		}
		if(attackPlayer.safeArea > 0&&skillId!=121) {//2015年12月30日安全区可以用血瓶
			sendAttackResponse(attackUid, -1, Result.SUCCESS, skillId, scene, new SkillDamageInfo(0, false), targetPlayer.currentLife, true);
			return;
		}
		//if(targetPlayer.currentLife <= 0 && skillId == 171){
			//targetPlayer.currentLife = 9999999;//DELETE THIS CODE
		//}
		if(targetPlayer.currentLife <= 0 || attackPlayer.currentLife <= 0) {
			logger.warn("参与者已死 src {}, dst {}",attackPlayer.currentLife,targetPlayer.currentLife);
			return;
		}
		
		JunZhu attacker = attackPlayer.jz;
		if(attacker == null){
			attacker = attackPlayer.jz = JunZhuMgr.inst.getJunZhu(session);
		}
		if(attacker == null) {
			logger.error("fight攻击失败，找不到君主");
			return;
		}
		
		if(targetPlayer.roleId == Scene.YBRobot_RoleId) {
			processAttackCart(attacker, session, scene, attackPlayer, targetPlayer, skillId);
		} else {
			processAttackPlayer(attacker, session, scene, attackPlayer, targetPlayer, skillId);
		}
	}
	

	public void processAttackPlayer(JunZhu attacker, IoSession session, Scene scene,
			Player attackPlayer, Player targetPlayer, int skillId) {
		int targetUid = targetPlayer.userId;
		int attackUid = attackPlayer.userId;
		JunZhu defender = null;
		if(targetPlayer instanceof FightNPC){
			FightNPC fp = (FightNPC)targetPlayer;
			defender = fp.fakeJz;
		//}else if(targetPlayer.roleId == Scene.TOWER_RoleId){
		}else if(targetPlayer instanceof TowerNPC){
			//此时defender是null
			if(skillId != 101){
				return;
			}
		}else{
			defender = targetPlayer.jz;
			if(defender == null){
				defender = targetPlayer.jz = HibernateUtil.find(JunZhu.class, targetPlayer.jzId);
			}
			if(defender == null) {
				logger.error("攻击失败，找不到被攻击的君主，id:{}", targetPlayer.jzId);
				return;
			}
		}
		
		boolean teammate = isTeammate(targetPlayer, attackPlayer); 
		boolean targetIsSelf = isTargetIsSelf(targetUid, attackUid);
		Skill skill = BuffMgr.inst.getSkillById(skillId);
		float distance = scene.getPlayerDistance(attackPlayer.userId, targetPlayer.userId) * 100;
		Result result = verifySkill(skill, targetIsSelf, distance, teammate, attackPlayer.jzId);
		if(result != Result.SUCCESS) {
			logger.error("uid{}技能使用失败，attackJzId:{},targetJzId:{}，skillId:{},result:{}",
					attackUid,attacker.id, targetPlayer.jzId, skillId, result);
			sendAttackError(result, scene, attackUid);
			return;
		}
		//CD没问题才检查这个，否则扣了血瓶，CD又不足，就浪费血瓶了。 
		if(scene.checkSkill(attacker,attackPlayer,targetPlayer,skillId)==false){
			return;
		}
		
		SkillDamageInfo skillDamageInfo = null;
		//if(targetPlayer.roleId == Scene.TOWER_RoleId){
		if(targetPlayer instanceof TowerNPC){
			long damageValue = 0;
			TowerNPC t = (TowerNPC)targetPlayer;
			FightScene fs = (FightScene) scene;
			if(fs.preTowerDie(t) == false){
			}else{
				long ms = System.currentTimeMillis();
				if(t.preHurtMS + FightScene.damage_CD<=ms){
					damageValue = t.conf.zhanlingzhiAdd;
					damageValue = Math.max(damageValue, FightScene.fixTakeTowerSpeed);
					t.preHurtMS = ms;
				}
			}
			skillDamageInfo = new SkillDamageInfo(damageValue, false);
		}else{
			skillDamageInfo = BigSwitch.inst.buffMgr.calcSkillDamage(attacker, defender, attackPlayer, targetPlayer, skill, targetUid, scene);
			fixDamageValue(targetPlayer, skill, skillDamageInfo);
		}
//		damageValue+=8888;
		updateSkillCdTime(attackPlayer.jzId, skill);
		BigSwitch.inst.buffMgr.processSkillEffect(skillDamageInfo.damageValue, targetPlayer, skill);
	
		logger.info("uid{},jz:{}攻击了jz:{},使用技能:{},造成伤害值:{}",attackUid, attacker.id, targetPlayer.jzId, skillId, skillDamageInfo.damageValue);
		sendAttackResponse(attackUid, targetUid, Result.SUCCESS, skillId, scene, skillDamageInfo, targetPlayer.currentLife, true);
		if(targetPlayer.currentLife <= 0) {
			scene.playerDie(targetPlayer,  attackPlayer.userId);
			if(scene instanceof YaBiaoScene) {
				JunZhu attackJz = HibernateUtil.find(JunZhu.class, attackPlayer.jzId);
				YaBiaoHuoDongMgr.inst.isEnemy4Award(attackJz, defender);
			}
		}
	}

	public void fixDamageValue(Player targetPlayer, Skill skill, SkillDamageInfo skillDamageInfo) {
		if(targetPlayer.session != null) {
			Object scene = targetPlayer.session.getAttribute(SessionAttKey.Scene);
			if(scene instanceof FightScene) {
				return;
			}
		}
		if(skill != null) {
			if(101 == skill.SkillId) {
				skillDamageInfo.damageValue = Math.min(skillDamageInfo.damageValue, (int)(targetPlayer.totalLife * YunbiaoTemp.damage_amend_X));
			} else if(111 == skill.SkillId) {
				skillDamageInfo.damageValue = Math.min(skillDamageInfo.damageValue, (int)(targetPlayer.totalLife * YunbiaoTemp.damage_amend_Y));
			}
		}
	}

	public void processAttackCart(JunZhu attacker, IoSession session, Scene scene, 
			Player attackPlayer, Player targetPlayer, int skillId) {
		int targetUid = targetPlayer.userId;
		int attackUid = attackPlayer.userId;
		
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(targetPlayer.jzId);
		if(ybr == null) {
			logger.error("攻击失败，找不到被攻击的目标（系统马车），uid:{}, jzId:{}", targetPlayer.userId, targetPlayer.jzId);
			return;
		}
		boolean isPlayerCart = false;
		JunZhu defender = ybr.cartAttr4Fight;
		if(targetPlayer.jzId > 0) {// 是系统马车
			isPlayerCart = true;
		} 
		
		if(defender == null) {
			logger.error("攻击失败，君主马车的属性有误，君主id:{}", targetPlayer.jzId);
			return;
		}
		
		YBBattleBean jbBattleBean = YaBiaoHuoDongMgr.inst.getYBZhanDouInfo(attacker.id, attacker.vipLevel);
		if(jbBattleBean.remainJB4Award<=0){
			sendAttackError(Result.DAY_NOT_GET_AWARD_TIMES, scene, attackUid);
			return;
		}
		
		if(ybr.protectTime > 0) {
			sendAttackError(Result.CART_IN_PROTECT_TIME, scene, attackUid);
			return;
		}
		
		boolean targetIsSelf = isTargetIsSelf(targetUid, attackUid);
		boolean cartIsTeammate = isCartOfTeammate(targetPlayer, attackPlayer);
		Skill skill = BuffMgr.inst.getSkillById(skillId);
		float distance = scene.getPlayerDistance(attackPlayer.userId, targetPlayer.userId) * 100;//配置文件里与前端的比例
		Result result = verifySkill(skill, targetIsSelf, distance, cartIsTeammate, attacker.id);
		if(result != Result.SUCCESS) {
			logger.error("攻击失败-技能使用失败，attackJzId:{},targetJzId:{}，skillId:{},result:{}",
					attacker.id, defender.id, skillId,result);
			sendAttackError(result, scene, attackUid);
			return;
		}
		
		SkillDamageInfo skillDamageInfo = BigSwitch.inst.buffMgr.calcSkillDamage(attacker, defender, attackPlayer, targetPlayer, skill, targetUid,scene);
		fixDamageValue(targetPlayer, skill, skillDamageInfo);
		updateSkillCdTime(attacker.id, skill);
		BigSwitch.inst.buffMgr.processSkillEffect(skillDamageInfo.damageValue, targetPlayer, skill);

		logger.info("发生打架事件：jz:{}攻击了jz:{},使用技能:{},造成伤害值:{}", attacker.id, defender.id, skillId, skillDamageInfo.damageValue);
		sendAttackResponse(attackUid, targetUid, Result.SUCCESS, skillId, scene, skillDamageInfo, targetPlayer.currentLife, true);
		// 第一次被攻击 ， 需要发送速报
		if(isPlayerCart) {
			Set<Long> firstInjuredSet = cartInjuredFirstRecord.get(targetPlayer.jzId);
			if(firstInjuredSet == null) {
				firstInjuredSet = new HashSet<Long>();
				cartInjuredFirstRecord.put(targetPlayer.jzId, firstInjuredSet);
			}
			if(!firstInjuredSet.contains(attackPlayer.jzId)) {
				EventMgr.addEvent(attacker.id,ED.BIAOCHE_BEIDA, new Object[] {attacker.id, defender.id, attackPlayer.userId, targetPlayer.userId});
				firstInjuredSet.add(attackPlayer.jzId);
			}
		}

		if(targetPlayer.currentLife <= 0) {
			scene.playerDie(targetPlayer,  attackPlayer.userId);
			cartInjuredFirstRecord.remove(defender.id);
			YaBiaoHuoDongMgr.inst.settleJieBiaoResult(targetPlayer.jzId, session);
		}
		//攻击的若是马车，计算马车的反击伤害 2016年1月20日 16:41:53 不要该功能
		//processCartBeatBack(attacker, scene, attackPlayer, targetPlayer, defender);
	}

	public boolean isCartOfTeammate(Player targetPlayer, Player attackPlayer) {
		int attackerLmId = attackPlayer.allianceId;
		int defenderLmId = targetPlayer.allianceId;
		if((attackerLmId > 0 && defenderLmId > 0) && attackerLmId == defenderLmId) {
			return true;
		} else if((attackerLmId <=0 || defenderLmId <= 0) && targetPlayer.jzId == attackPlayer.jzId) {
			return true;
		}
		return false;
	}

	public void processCartBeatBack(JunZhu attacker, Scene scene,
			Player attackPlayer, Player targetPlayer, JunZhu defender) {
		int targetUid = targetPlayer.userId;
		int attackUid = attackPlayer.userId;
		Skill beatBackSkill = BuffMgr.inst.getSkillById(101);
		// 根据联盟科技计算马车攻击力加成，这里的君主对象defenderClone 必须是克隆出来的，以免修改原君主的属性
		JunZhu defenderClone = defender.clone();
		double keJiRate = 0;
		LianMengKeJi lmKeJiConf = JianZhuMgr.inst.getKeJiConfForYaBiao(targetPlayer.allianceId, 202);//202马车攻击力加成
		if(lmKeJiConf != null) {
			keJiRate = lmKeJiConf.value1;
		}
		defenderClone.gongJi = (int) (defenderClone.gongJi + defender.gongJi * (keJiRate / 100));
		 
		SkillDamageInfo skillDamageInfo = BigSwitch.inst.buffMgr.calcSkillDamage(defenderClone, attacker, attackPlayer, targetPlayer,beatBackSkill, attackUid,scene);
		BigSwitch.inst.buffMgr.processSkillEffect(skillDamageInfo.damageValue, attackPlayer, beatBackSkill);
		sendAttackResponse(targetUid,attackUid, Result.SUCCESS, 101, scene, skillDamageInfo, attackPlayer.currentLife, true);
		if(attackPlayer.currentLife <= 0) {
			scene.playerDie(attackPlayer,  targetUid);
		}
	}

	public boolean isTargetIsSelf(int targetUid, int attackUid) {
		return attackUid== targetUid;
	}

	public boolean isTeammate(Player targetPlayer, Player attackPlayer) {
		int attackerLmId = attackPlayer.allianceId;
		int defenderLmId = targetPlayer.allianceId;
		if((attackerLmId > 0 && defenderLmId > 0) && attackerLmId == defenderLmId) {
			return true;
		} 
		return false;
	}
	
	public Result verifySkill(Skill skill, boolean targetIsSelf, float distance, boolean teammate, Long attackerId) {
		if(skill == null) {		
			return Result.SKILL_NOT_EXIST;
		}
		Action action = BigSwitch.inst.buffMgr.getActionById(skill.Action1);
		if(action == null) {
			logger.error("攻击失败，找不到action配置，skillId:{}", skill.SkillId);
			return Result.SKILL_NOT_EXIST;
		}
		SkillActionType skillActionType = SkillActionType.getSkillActionType(action.TypeKey);
		if(skillActionType == null) {
			logger.error("攻击失败，无效的ActionTypeKey配置，skillId:{}", skill.SkillId);
			return Result.SKILL_NOT_EXIST;
		}
		if(skill.SkillTarget == 0 && !targetIsSelf) {
			return Result.SKILL_TARGET_NOT_OTHER;
		} else if(skill.SkillTarget == 1 && targetIsSelf){
			return Result.SKILL_TARGET_NOT_SELF;
		}
		// 判断距离 2016年2月1日 18:20:25 最大距离要加100
		if(distance > skill.Range_Max + skill.Range_Max/2 + extrSkillDist) {
			return Result.SKILL_DISTANCE_ERROR;
		}
		
		long currentTime = System.currentTimeMillis();
		// 判断cd时间
		Map<Integer, Long> skillCDMap = skillCDTimeMap.get(attackerId);
		if(skillCDMap != null) {
			// 判断是否受公共cd影响 
			Long gcd = skillCDMap.get(0);
			if(gcd != null && currentTime < gcd) {
				return Result.SKILL_COOL_TIME;
			}
			//
			Long endTime = skillCDMap.get(skill.SkillId);
			if(endTime != null && endTime > currentTime) {
//				logger.info("end {} BBB {}",skill.SkillId, endTime);
				return Result.SKILL_COOL_TIME;
			}
		}
		
		if(!targetIsSelf) {
			// 判断敌友方
			if(skill.CRRejectU == 1 && !teammate) { //1.不可对敌对单位使用
				return Result.SKILL_TARGET_NOT_ENEMY;
			} else if(skill.CRRejectU == 2 && teammate) {//2.不可对友方单位使用
				return Result.SKILL_TARGET_NOT_TEAMMATE ;
			}
		}
		return Result.SUCCESS;
	}
	
	public void sendAttackError(Result result, Scene scene, int attackUid) {
		sendAttackResponse(attackUid, 0, result, 0, scene, new SkillDamageInfo(0, false), 0, false);
	}

	public void sendAttackResponse(int attackUid, int targetUid, Result result,
			int skillId, Scene scene, SkillDamageInfo skillDamageInfo, int remainLife, boolean succeed) {
		FightAttackResp.Builder response = FightAttackResp.newBuilder();
		response.setResult(result);
		response.setAttackUid(attackUid);
		response.setTargetUid(targetUid);
		response.setSkillId(skillId);
		response.setDamage(skillDamageInfo.damageValue);		
		response.setRemainLife(remainLife);
		response.setIsBaoJi(skillDamageInfo.isBaoji);
		if(succeed) {
			scene.broadCastEvent(response.build(), 0);
		} else {
			Player p = scene.players.get(attackUid);
			p.session.write(response.build());
		}
	}
	
	/**
	 * 更新君主技能cd时间
	 * 
	 * @param junzhuId
	 * @param skillId
	 */
	public void updateSkillCdTime(long junzhuId, Skill skill) {
		Map<Integer, Long> skillCDMap = skillCDTimeMap.get(junzhuId);
		if(skillCDMap == null) {
			synchronized (skillCDTimeMap) {
				if(skillCDMap == null) {
					skillCDMap = new HashMap<Integer, Long>();
					skillCDTimeMap.put(junzhuId, skillCDMap);
				}
			}
		}
		skillCDMap.put(skill.SkillId, System.currentTimeMillis() + skill.BaseCD);
		if(skill.IsInGCD == 1) {
			skillCDMap.put(0, System.currentTimeMillis() + skill.CDGroup);
		}
	}
	
	public void reviveRequest(int id, IoSession session, Builder builder) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if(junzhu == null) {
			logger.error("押镖场景复活失败，找不到君主");
			return;
		}
		
		PlayerReviveRequest.Builder request = (qxmobile.protobuf.AllianceFightProtos.PlayerReviveRequest.Builder) builder;
		int type = request.getType();
		int allLifeReviveTimes = YaBiaoHuoDongMgr.inst.getFuhuoTimes(junzhu);
		Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
		Player player = scene.getPlayerByJunZhuId(junzhu.id);
		PlayerReviveNotify.Builder reviveNotify = PlayerReviveNotify.newBuilder();
		reviveNotify.setUid(player.userId);
		if(type == 0) {//安全区复活
			int safeAreaId = YaBiaoHuoDongMgr.inst.getNearSafeArea(player.posX, player.posZ);
			YunBiaoSafe safeArea = YaBiaoHuoDongMgr.safeAreaMap.get(safeAreaId);
			if(safeArea == null) {
				reviveNotify.setPosX(player.posX);
				reviveNotify.setPosZ(player.posZ);
			} else {
				reviveNotify.setPosX(safeArea.saveAreaX);
				reviveNotify.setPosZ(safeArea.saveAreaZ);
				player.posX = safeArea.saveAreaX;
				player.posZ = safeArea.saveAreaZ;
			}
		} else if(type == 1) {//原地安全复活
			int costYuanBao = 20;
			int reviveOnDeadPosTimes = YaBiaoHuoDongMgr.inst.getReviveOnDeadPosTimes(junzhu);
			Purchase purchase = PurchaseMgr.inst.getPurchaseCfg(PurchaseConstants.YB_REVIVE_DEAD_POS, reviveOnDeadPosTimes+1);
			if(purchase == null) {
				logger.error("找不到类型为:{}的purchase配置", PurchaseConstants.YB_REVIVE_DEAD_POS);
			} else {
				costYuanBao = purchase.yuanbao;
			}
			if(junzhu.yuanBao < costYuanBao) {
				logger.error("押镖场景复活失败，原地复活失败，元宝不足");
				sendPlayerReviveNotifyError(session, 1, player);
				return;
			}
			reviveNotify.setPosX(player.posX);
			reviveNotify.setPosZ(player.posZ);
			YuanBaoMgr.inst.diff(junzhu, -costYuanBao, 0, costYuanBao, YBType.YB_DEAD_POS_REVIVE, "押镖场景原地安全复活");
			HibernateUtil.save(junzhu);
			YBBattleBean zdbean = YaBiaoHuoDongMgr.inst.getYBZhanDouInfo(junzhu.id, junzhu.vipLevel);
			zdbean.reviveOnDeadPos += 1;
			zdbean.lastReviveOnDeadPosTime = new Date();
			HibernateUtil.save(zdbean);
		}else{		
			logger.error("押镖场景复活失败，没有该复活类型type:{}, jzId:{}", type, junzhu.id);
			return;
		}
		if(allLifeReviveTimes > 0) {
			reviveNotify.setLife(player.totalLife);
			player.currentLife = player.totalLife;
		} else {
			reviveNotify.setLife(1);
			player.currentLife = 1;
		}
		reviveNotify.setResult(0);
		logger.info("junzhu:{}复活坐标x:{},y:{},复活后的血量:{}",junzhu.name, reviveNotify.getPosX(), reviveNotify.getPosZ(),player.currentLife);
		scene.broadCastEvent(reviveNotify.build(), 0);
		
		YaBiaoHuoDongMgr.inst.kouchuFuhuoTimes(junzhu);
		JunZhuMgr.inst.sendMainInfo(session,junzhu,false);
	}

	public void sendPlayerReviveNotifyError(IoSession session, int result, Player player) {
		PlayerReviveNotify.Builder reviveNotify = PlayerReviveNotify.newBuilder();
		reviveNotify.setUid(player.userId);
		reviveNotify.setPosX(player.posX);
		reviveNotify.setPosZ(player.posZ);
		reviveNotify.setResult(1);
		reviveNotify.setLife(0);
		session.write(reviveNotify.build());
	}

}
