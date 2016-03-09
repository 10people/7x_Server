package com.qx.fight;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.AllianceFightProtos.FightAttackReq;
import qxmobile.protobuf.AllianceFightProtos.FightAttackResp;
import qxmobile.protobuf.AllianceFightProtos.PlayerDeadNotify;
import qxmobile.protobuf.AllianceFightProtos.PlayerReviveNotify;
import qxmobile.protobuf.AllianceFightProtos.PlayerReviveRequest;
import qxmobile.protobuf.AllianceFightProtos.Result;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.template.Action;
import com.manu.dynasty.template.CartNPCTemp;
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
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.world.Player;
import com.qx.world.Scene;
import com.qx.yabiao.YBBattleBean;
import com.qx.yabiao.YaBiaoHuoDongMgr;
import com.qx.yabiao.YaBiaoRobot;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

public class FightMgr {
	public static FightMgr inst;
		
	protected static Logger logger = LoggerFactory.getLogger(FightMgr.class);
	
	/** 玩家技能冷却map  <junzhuId, map<skillId, endTime>> */
	public Map<Long, Map<Integer, Long>> skillCDTimeMap = new HashMap<Long, Map<Integer,Long>>();

	/** 玩家技能公共冷却结束时间 */
	public long skillGroupEndTime = System.currentTimeMillis();
	
	/** 玩家马车第一次被攻击记录<君主id, 第一次被攻击时间> */
	public Map<Long, Set<Long>> cartInjuredFirstRecord = new HashMap<Long, Set<Long>>();
	
	public FightMgr() {
		inst = this;
	}
	
	public void activeFight(int id, IoSession session, Builder builder) {
		FightAttackReq.Builder request = (qxmobile.protobuf.AllianceFightProtos.FightAttackReq.Builder) builder;
		int targetUid = request.getTargetUid();			// 被攻击者的uid
		int skillId = request.getSkillId();				// 使用的技能id
		
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
			sendAttackResponse(attackUid, -1, Result.SUCCESS, skillId, scene, 0, targetPlayer.currentLife, true);
			return;
		}
		
		if(targetPlayer.currentLife <= 0) {
			return;
		}
		
		JunZhu attacker = JunZhuMgr.inst.getJunZhu(session);
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
	

	private void processAttackPlayer(JunZhu attacker, IoSession session, Scene scene,
			Player attackPlayer, Player targetPlayer, int skillId) {
		int targetUid = targetPlayer.userId;
		int attackUid = attackPlayer.userId;
		JunZhu defender = HibernateUtil.find(JunZhu.class, targetPlayer.jzId);
		if(defender == null) {
			logger.error("攻击失败，找不到被攻击的君主，id:{}", targetPlayer.jzId);
			return;
		}
		YBBattleBean ybBattle = YaBiaoHuoDongMgr.inst.getYBZhanDouInfo(defender.id, defender.vipLevel);
		if(skillId != 121 && targetPlayer.safeArea >= 0) {
			logger.info("攻击失败，被攻击的目标在安全区，safeArea:{}，targetJZId:{}",targetPlayer.safeArea,targetPlayer.jzId);
			sendAttackError(Result.TARGET_IN_SAFE_AREA, scene, attackUid);
			return;
		} else if(skillId == 121){
			if(YaBiaoHuoDongMgr.inst.getXuePingRemainTimes(defender.id, defender.vipLevel) <= 0) {
				return;
			}
		}
		
		boolean teammate = isTeammate(targetPlayer, attackPlayer); 
		boolean targetIsSelf = isTargetIsSelf(targetUid, attackUid);
		Skill skill = BuffMgr.inst.getSkillById(skillId);
		float distance = scene.getPlayerDistance(attackPlayer.userId, targetPlayer.userId) * 100;
		Result result = verifySkill(skill, targetIsSelf, distance, teammate, attacker.id);
		if(result != Result.SUCCESS) {
			logger.error("攻击失败-技能使用失败，attackJzId:{},targetJzId:{}，skillId:{},result:{}",
					attacker.id, defender.id, skillId, result);
			sendAttackError(result, scene, attackUid);
			return;
		}
		
		int damageValue = BigSwitch.inst.buffMgr.calcSkillDamage(attacker, defender, skill, targetUid);
		damageValue = fixDamageValue(targetPlayer, skill, damageValue);
		updateSkillCdTime(attacker.id, skill);
		BigSwitch.inst.buffMgr.processSkillEffect(damageValue, targetPlayer, skill);
	
		if(skillId == 121) {
			attackPlayer.xuePingRemain -= 1;
			if(ybBattle != null) {
				ybBattle.xueping4uesd += 1;
				HibernateUtil.save(ybBattle);
			}
		}

		logger.info("发生打架事件：jz:{}攻击了jz:{},使用技能:{},造成伤害值:{}", attacker.id, defender.id, skillId, damageValue);
		sendAttackResponse(attackUid, targetUid, Result.SUCCESS, skillId, scene, damageValue, targetPlayer.currentLife, true);
		if(targetPlayer.currentLife <= 0) {
			if(scene.name.contains("YB")) {
				processYBPlayerDead(scene, defender, targetUid, attackPlayer.userId);
			}
		}
	}

	private int fixDamageValue(Player targetPlayer, Skill skill, int damageValue) {
		if(skill != null) {
			if(101 == skill.SkillId) {
				damageValue = Math.min(damageValue, (int)(targetPlayer.totalLife * YunbiaoTemp.damage_amend_X));
			} else if(111 == skill.SkillId) {
				damageValue = Math.min(damageValue, (int)(targetPlayer.totalLife * YunbiaoTemp.damage_amend_Y));
			}
		}
		return damageValue;
	}

	private void processAttackCart(JunZhu attacker, IoSession session, Scene scene, 
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
		
		int damageValue = BigSwitch.inst.buffMgr.calcSkillDamage(attacker, defender, skill, targetUid);
		damageValue = fixDamageValue(targetPlayer, skill, damageValue);
		updateSkillCdTime(attacker.id, skill);
		BigSwitch.inst.buffMgr.processSkillEffect(damageValue, targetPlayer, skill);

		logger.info("发生打架事件：jz:{}攻击了jz:{},使用技能:{},造成伤害值:{}", attacker.id, defender.id, skillId, damageValue);
		sendAttackResponse(attackUid, targetUid, Result.SUCCESS, skillId, scene, damageValue, targetPlayer.currentLife, true);
		// 第一次被攻击 ， 需要发送速报
		if(isPlayerCart) {
			Set<Long> firstInjuredSet = cartInjuredFirstRecord.get(targetPlayer.jzId);
			if(firstInjuredSet == null) {
				firstInjuredSet = new HashSet<Long>();
				cartInjuredFirstRecord.put(targetPlayer.jzId, firstInjuredSet);
			}
			if(!firstInjuredSet.contains(attackPlayer.jzId)) {
				EventMgr.addEvent(ED.BIAOCHE_BEIDA, new Object[] {attacker.id, defender.id, attackPlayer.userId, targetPlayer.userId});
				firstInjuredSet.add(attackPlayer.jzId);
			}
		}

		if(targetPlayer.currentLife <= 0) {
			processYBPlayerDead(scene, defender, targetUid, attackPlayer.userId);
			cartInjuredFirstRecord.remove(defender.id);
			YaBiaoHuoDongMgr.inst.settleJieBiaoResult(targetPlayer.jzId, session);
		}
		//攻击的若是马车，计算马车的反击伤害 2016年1月20日 16:41:53 不要该功能
		//processCartBeatBack(attacker, scene, attackPlayer, targetPlayer, defender);
	}

	private boolean isCartOfTeammate(Player targetPlayer, Player attackPlayer) {
		int attackerLmId = attackPlayer.allianceId;
		int defenderLmId = targetPlayer.allianceId;
		if((attackerLmId > 0 && defenderLmId > 0) && attackerLmId == defenderLmId) {
			return true;
		} else if((attackerLmId <=0 || defenderLmId <= 0) && targetPlayer.jzId == attackPlayer.jzId) {
			return true;
		}
		return false;
	}

	protected void processCartBeatBack(JunZhu attacker, Scene scene,
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
		
		int beatBackDamage = BigSwitch.inst.buffMgr.calcSkillDamage(defenderClone, attacker, beatBackSkill, attackUid);
		BigSwitch.inst.buffMgr.processSkillEffect(beatBackDamage, attackPlayer, beatBackSkill);
		sendAttackResponse(targetUid,attackUid, Result.SUCCESS, 101, scene, beatBackDamage, attackPlayer.currentLife, true);
		if(attackPlayer.currentLife <= 0) {
			processYBPlayerDead(scene, attacker, attackUid, targetUid);
		}
	}

	protected boolean isTargetIsSelf(int targetUid, int attackUid) {
		return attackUid== targetUid;
	}

	protected boolean isTeammate(Player targetPlayer, Player attackPlayer) {
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
		if(distance > skill.Range_Max + 100 || distance < skill.Range_Min) {
			return Result.SKILL_DISTANCE_ERROR;
		}
		
		long currentTime = System.currentTimeMillis();
		// 判断是否受公共cd影响 
		if(skill.IsInGCD == 1 && currentTime < skillGroupEndTime) {
			return Result.SKILL_COOL_TIME;
		}
		// 判断cd时间
		Map<Integer, Long> skillCDMap = skillCDTimeMap.get(attackerId);
		if(skillCDMap != null) {
			Long endTime = skillCDMap.get(skill.SkillId);
			if(endTime != null && endTime >= currentTime) {
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
	
	protected void sendAttackError(Result result, Scene scene, int attackUid) {
		sendAttackResponse(attackUid, 0, result, 0, scene, 0, 0, false);
	}

	protected void sendAttackResponse(int attackUid, int targetUid, Result result,
			int skillId, Scene scene, int damageValue, int remainLife, boolean succeed) {
		FightAttackResp.Builder response = FightAttackResp.newBuilder();
		response.setResult(result);
		response.setAttackUid(attackUid);
		response.setTargetUid(targetUid);
		response.setSkillId(skillId);
		response.setDamage(damageValue);		
		response.setRemainLife(remainLife);
		
		if(succeed) {
			for(Map.Entry<Integer, Player> entry : scene.players.entrySet()) {
				Player p = entry.getValue();
				p.session.write(response.build());
			}
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
			skillGroupEndTime = System.currentTimeMillis() + skill.CDGroup;
		}
	}

	public void processYBPlayerDead(Scene scene, JunZhu defender, int uid, int killerUid) {
		BuffMgr.inst.removeBuff(defender.id);
		int onSiteReviveCost = 20;//默认20，为了假如找不到配置能够继续执行
		int remainReviveTimes = 0;
		if(defender.id > 0) {// 表示是真实玩家
			int reviveOnDeadPosTimes = YaBiaoHuoDongMgr.inst.getReviveOnDeadPosTimes(defender);
			Purchase purchase = PurchaseMgr.inst.getPurchaseCfg(PurchaseConstants.YB_REVIVE_DEAD_POS, reviveOnDeadPosTimes+1);
			if(purchase == null) {
				logger.error("找不到类型为:{}的purchase配置", PurchaseConstants.YB_REVIVE_DEAD_POS);
			} else {
				onSiteReviveCost = purchase.getYuanbao();
			}
			remainReviveTimes = YaBiaoHuoDongMgr.inst.getFuhuoTimes(defender);
		}
		
		PlayerDeadNotify.Builder deadNotify = PlayerDeadNotify.newBuilder();
		deadNotify.setUid(uid);
		deadNotify.setKillerUid(killerUid);
		deadNotify.setAutoReviveRemainTime(YunbiaoTemp.autoResurgenceTime);
		deadNotify.setRemainAllLifeTimes(remainReviveTimes);
		deadNotify.setOnSiteReviveCost(onSiteReviveCost);
		for(Map.Entry<Integer, Player> entry : scene.players.entrySet()) {
			Player p = entry.getValue();
			p.session.write(deadNotify.build());
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
				costYuanBao = purchase.getYuanbao();
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
		logger.info("junzhu:{}复活坐标x:{},y:{}",junzhu.name, reviveNotify.getPosX(), reviveNotify.getPosZ());
		for(Map.Entry<Integer, Player> entry : scene.players.entrySet()) {
			Player p = entry.getValue();
			p.session.write(reviveNotify.build());
		}
		
		YaBiaoHuoDongMgr.inst.kouchuFuhuoTimes(junzhu);
		JunZhuMgr.inst.sendMainInfo(session);
	}

	private void sendPlayerReviveNotifyError(IoSession session, int result, Player player) {
		PlayerReviveNotify.Builder reviveNotify = PlayerReviveNotify.newBuilder();
		reviveNotify.setUid(player.userId);
		reviveNotify.setPosX(player.posX);
		reviveNotify.setPosZ(player.posZ);
		reviveNotify.setResult(1);
		reviveNotify.setLife(0);
		session.write(reviveNotify.build());
	}

}
