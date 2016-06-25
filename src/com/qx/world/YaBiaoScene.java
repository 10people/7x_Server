package com.qx.world;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.AllianceFightProtos.PlayerDeadNotify;
import qxmobile.protobuf.AllianceFightProtos.Result;
import qxmobile.protobuf.AllianceFightProtos.SafeAreaBloodReturn;
import qxmobile.protobuf.PlayerData;
import qxmobile.protobuf.PlayerData.State;
import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Scene.EnterSceneCache;
import qxmobile.protobuf.Scene.EnterSceneConfirm;
import qxmobile.protobuf.Scene.ExitScene;
import qxmobile.protobuf.Scene.SpriteMove;

import com.google.protobuf.ByteString;
import com.google.protobuf.DirectBytes;
import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.template.LianMengKeJi;
import com.manu.dynasty.template.Purchase;
import com.manu.dynasty.template.YunBiaoSafe;
import com.manu.dynasty.template.YunbiaoTemp;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.building.JianZhuMgr;
import com.qx.buff.BuffMgr;
import com.qx.fight.FightMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.yabiao.LastExitYBInfo;
import com.qx.yabiao.YBBattleBean;
import com.qx.yabiao.YaBiaoHuoDongMgr;
import com.qx.yabiao.YaBiaoRobot;


/** 
 * @Description 押镖场景Scene
 *
 */
public class YaBiaoScene  extends VisionScene{
	public static Logger log = LoggerFactory.getLogger(YaBiaoScene.class.getSimpleName());
	public YaBiaoScene(String key){
		super(key);
	}
	
	@Override
	public void completeMission(Mission mission) {
		if (mission == null) {
			log.error("mission is null...");
			return;
		}
		
		int code = mission.code;
		final Builder builder = mission.builer;
		final IoSession session = mission.session;
		switch (code) {
			case PD.Enter_YBScene:
				EnterScene.Builder enterYBScene = (EnterScene.Builder)builder;
				enterYBScene(session,enterYBScene);
				break;
			case PD.Exit_YBScene:
				ExitScene.Builder exitYBSc = (ExitScene.Builder) builder;
				exitYBScene( session,exitYBSc);
				break;
			case PD.PLAYER_STATE_REPORT:
				PlayerData.PlayerState.Builder psd = (qxmobile.protobuf.PlayerData.PlayerState.Builder) builder;
				clientStateChange(psd, session);
				break;
			case PD.SAFE_AREA_BOOLD_RETURN:
				bloodReturnInSafeArea();
				break;
			case PD.Spirite_Move:
				SpriteMove.Builder move = (SpriteMove.Builder)builder;
				spriteMove(session,move);
				break;
			default:
//				log.warn("YaBiaoScene场景处理不了的协议unkown code: {}" , code);
				super.completeMission(mission);
				break;
		}
	}
	/**
	 * @Description 移动
	 */
	public Player spriteMove(IoSession session, SpriteMove.Builder move) {
		Player player=	super.spriteMove(session, move);
		//刷新所在的安全区位置
		if(player!=null){
			int safearea=YaBiaoHuoDongMgr.inst.getSafeArea(player.getPosX(), player.getPosZ());
			player.safeArea=safearea;
		}
		return player;
	}
	
	/**
	 * @Description 安全区回血
	 */
	private void bloodReturnInSafeArea() {
		for(Player player : players.values()) {
			if(player.jzId <= 0 || player.roleId == Scene.YBRobot_RoleId) {
				continue;
			}
			if(player.safeArea > 0 && player.currentLife < player.totalLife) {
				double keJiRate = 0;
				if(player.allianceId > 0) {
					LianMengKeJi lmKeJiConf = JianZhuMgr.inst.getKeJiConfForYaBiao(player.allianceId, 204);//204君主在安全区回血速率
					if(lmKeJiConf != null) {
						keJiRate = lmKeJiConf.value1;
					}
				}
				int returnValue = (int) Math.floor(player.totalLife * (YunbiaoTemp.saveArea_recoveryPro/100.0 * (1 + keJiRate/100)));
				player.currentLife = Math.min(player.totalLife, player.currentLife + returnValue);
				SafeAreaBloodReturn.Builder response = SafeAreaBloodReturn.newBuilder();
				response.setUid(player.userId);
				response.setReturnValue(returnValue);
				response.setRemainLife(player.currentLife);
				for(Player p : players.values()) {
					p.session.write(response.build());
				}
			}
		}
	}
	
	public void exitYBScene(IoSession session,ExitScene.Builder exitYBSc) {
		if (exitYBSc == null) {
			return;
		}
		Integer uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		Player player = players.get(uid);
		if(player == null) {
			return;
		}
		ProtobufMsg pm = new ProtobufMsg();
		pm.id=PD.Exit_YBScene;
		pm.builder = exitYBSc;
		broadCastEvent(pm, player.userId);
		players.remove(uid);
		removeVisibleIds(player);
		saveExitYBInfo(player);
		log.info("君主:{}退出押镖场景:{},剩余玩家个数：{},退出时坐标x:{},z:{}"
				 ,session.getAttribute(SessionAttKey.junZhuId),this.name, players.size(), player.posX, player.posZ);
	}

	
	/**
	 * @Description 退出押镖场景保存玩家坐标
	 */
	public void saveExitYBInfo(long junzhuId) {
		Player player = getPlayerByJunZhuId(junzhuId);
		if(player != null) {
			saveExitYBInfo(player);
		}
	}
	
	protected void saveExitYBInfo(Player player) {
		if(player.jzId>0 && player.roleId != Scene.YBRobot_RoleId){//马车不保存
			int currentLife = player.currentLife;
			if(currentLife <= 0) {
				currentLife = 1;
			}
			LastExitYBInfo lastExitInfo = HibernateUtil.find(LastExitYBInfo.class, player.jzId);
			if(lastExitInfo == null) {
				lastExitInfo = new LastExitYBInfo(player.jzId, player.safeArea, player.currentLife, 
						player.posX, player.posY, player.posZ);
				HibernateUtil.insert(lastExitInfo);
			} else {
				lastExitInfo.updateInfo(player.safeArea, currentLife, player.posX, player.posY, player.posZ);
				HibernateUtil.update(lastExitInfo);
			}
			log.info("君主:{}离开押镖场景，坐标x,z:{},{},剩余血量:{},处于安全区id:{}",player.name,player.posX, player.posZ,player.currentLife,player.safeArea);
		}
	}
	
	/**
	 * @Description 进入押镖场景
	 */
	public void enterYBScene(IoSession session,final EnterScene.Builder enterYBSc) {
		//镖车机器人没有JunZhu对象
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		boolean isBiaoChe=isBiaoChe(session);
		if(jz == null){
			if(!isBiaoChe){
				log.error("进入押镖场景错误，JunZhu为空，且不是镖车,~~~~~~~~~~~");
				return;
			}
		}
		YunBiaoSafe birthPlace=null;
		int zhanli = 0;
		int xuePingRemain = 0;
		if(jz!=null){
			zhanli=JunZhuMgr.inst.getJunZhuZhanliFinally(jz);
			birthPlace=YaBiaoHuoDongMgr.inst.getBirthPlace4YBJZ(this);
			if(birthPlace==null){
				log.error("进入押镖场景错误，未找到出生点配置");
				return;
			}
			xuePingRemain = YaBiaoHuoDongMgr.inst.getXuePingRemainTimes(jz.id, jz.vipLevel);
		}
		Object uidObject = session.getAttribute(SessionAttKey.playerId_Scene);
		session.setAttribute(SessionAttKey.Scene, this);
		final int userId = uidObject == null ? getUserId() : (Integer)uidObject;
		
		final Player player = new Player();
		player.userId = userId;
		session.setAttribute(SessionAttKey.playerId_Scene, userId);
		player.session = session;
		player.setName(jz == null ? enterYBSc.getSenderName() : jz.name);
		float posX=birthPlace==null?enterYBSc.getPosX():birthPlace.saveAreaX;
		float posZ=birthPlace==null?enterYBSc.getPosZ():birthPlace.saveAreaZ;
		player.setPosX(posX);
		player.setPosY(0);
		player.setPosZ(posZ);
		player.safeArea=birthPlace==null?1:birthPlace.areaID;
		Long jzId4bc=0L;//君主Id
		YaBiaoRobot ybr =null;
		if(jz == null&&isBiaoChe){
			//TDDO 存君主镖车HP
			jzId4bc = (Long) session.getAttribute(SessionAttKey.RobotJZID);
			if(jzId4bc!=null){
				ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(jzId4bc);
				zhanli=ybr.zhanli;
			}
		}
		if(isBiaoChe){
			player.jzId = jzId4bc;
			//roleId  镖车机器人的roleId 很大的数 区分Player是玩家还是玩家的镖车
			player.roleId =YBRobot_RoleId;
			player.totalLife = ybr.hp; 
			player.currentLife =ybr.hp;
			//2015年12月9日 梁霄说策划加上国家，等级，战力
			player.jzlevel=ybr.jzLevel;
			player.guojia=ybr.guojiaId;
			player.worth=ybr.worth;
			player.horseType=ybr.horseType;
			JunZhu jz4Vip=HibernateUtil.find(JunZhu.class, player.jzId);
			player.vip = jz4Vip==null?0:jz4Vip.vipLevel;
		}else {
			player.jzId =  jz.id;
			player.roleId =jz.roleId;
			player.totalLife =  jz.shengMingMax; 
			player.currentLife =jz.shengMingMax;
			//2015年12月9日 梁霄说策划加上国家，等级，战力
			player.jzlevel=jz.level;
			player.guojia=jz.guoJiaId;
			player.vip = jz==null?0:jz.vipLevel;
		}
		if(player.jzId > 0){
			if(player.roleId != YBRobot_RoleId) {//2015年12月31日 只有君主才赋值血量
				LastExitYBInfo lastExitInfo = HibernateUtil.find(LastExitYBInfo.class, player.jzId);
				if(lastExitInfo != null && !lastExitInfo.isReset()) {
					player.posX = lastExitInfo.posX;
					player.posZ = lastExitInfo.posZ;
					int addLife = lastExitInfo.getAddLife(player.totalLife);
					log.info("君主:{}进入押镖场景:{},并在上次退出时位置，原来血量:{},增加血量:{},总血量:{}",player.name, this.name, player.currentLife, addLife, addLife + lastExitInfo.remainLife);
					player.currentLife = Math.max(1, addLife + lastExitInfo.remainLife);
					player.currentLife = Math.min(player.totalLife, player.currentLife);
				}
			}
			player.allianceId = AllianceMgr.inst.getAllianceId(player.jzId);
		}
		
		if(enterYBSc.getPosX()!=0||enterYBSc.getPosZ()!=0){
			player.posX=enterYBSc.getPosX();
			player.posZ=enterYBSc.getPosZ();
			log.info("客户端指定押镖场景出生坐标---x---{}---z---{}",enterYBSc.getPosX(),enterYBSc.getPosZ());
		}
		player.lmName =(String)session.getAttribute(SessionAttKey.LM_NAME, "***");
		player.chengHaoId = (String)session.getAttribute(SessionAttKey.CHENG_HAO_ID, "-1");
		player.zhanli=zhanli;
		player.xuePingRemain = xuePingRemain;
		Object zhiWu=session.getAttribute(SessionAttKey.LM_ZHIWU, -1);
		zhiWu=(zhiWu==null)?-1:zhiWu;
		player.zhiWu = (Integer)zhiWu;
		players.put(userId, player);
		
		log.info("{}进入押镖场景 {},这货是<{}>，血量--<{}/{}>， 坐标:x-{},z-{}", player.getName(),this.name,isBiaoChe?"镖车机器人":"玩家",player.currentLife,player.totalLife,
				player.posX,player.posZ);
		if(isBiaoChe){
			bdEnterInfo(player);
		}else{
			//告诉当前玩家它的信息，确认进入
			EnterSceneConfirm.Builder ret = EnterSceneConfirm.newBuilder();
			ret.setUid(userId);
			ret.setPosX(player.getPosX());
			ret.setPosY(player.getPosY());
			ret.setPosZ(player.getPosZ());
			session.write(ret.build());
		}
	}
	
	public IoBuffer buildEnterInfoCache(Player p) {
		ByteString bs = p.infoCache;
		if(bs == null){
			qxmobile.protobuf.Scene.EnterScene.Builder es = buildEnterInfo(p);
			es.clearPosX();
			es.clearPosY();
			es.clearPosZ();
			byte[] me = es.build().toByteArray();
			bs = new DirectBytes(me);
			p.infoCache = bs;
		}
		if(p.fullCache == null){
			EnterSceneCache.Builder b = EnterSceneCache.newBuilder();
			b.setBody(bs);
			b.setPosX(p.posX);
			b.setPosY(p.posY);
			b.setPosZ(p.posZ);
	//		return b;
			IoBuffer io = pack(b.build(), PD.Enter_YBScene);
			p.fullCache = io;
		}else{
			//log.info("hit cache "+hitCnt++);
		}
		IoBuffer ret = p.fullCache.asReadOnlyBuffer();
		ret.position(0);
		return ret;
	}
	public EnterScene.Builder buildEnterInfo(Player p) {
		EnterScene.Builder enterSc = EnterScene.newBuilder();
		enterSc.setSenderName(p.getName());
		enterSc.setUid(p.userId);
		enterSc.setPosX(p.getPosX());
		enterSc.setPosY(p.getPosY());
		enterSc.setPosZ(p.getPosZ());
		enterSc.setRoleId(p.roleId);
		int chenghaId=Integer.valueOf(p.chengHaoId==null?"0":p.chengHaoId);
		enterSc.setChengHao(chenghaId);
		enterSc.setCurrentLife(p.currentLife);
		enterSc.setTotalLife(p.totalLife);    
		enterSc.setAllianceName(p.lmName);
		enterSc.setVipLevel(p.vip);
		enterSc.setZhiWu(p.zhiWu);     
		//2015年12月9日 梁霄说策划加上国家，等级，战力
		enterSc.setLevel(p.jzlevel);
		enterSc.setZhanli(p.zhanli);
		enterSc.setGuojia(p.guojia);
		//2015年12月12日 加入马车价值 马车类型
		enterSc.setWorth(p.worth);
		enterSc.setHorseType(p.horseType);
		enterSc.setJzId(p.jzId);
		enterSc.setXuePingRemain(p.xuePingRemain);
		return enterSc;
	}
	
	
	/**
	 * @Description 移除君主马车，被杀掉的直接移除，不广播
	 */
	public synchronized void exit4YaBiaoRobot(YaBiaoRobot ybrobot) {
		IoSession session=ybrobot.session;
		Integer uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		Player player = players.remove(uid);
		log.info("从场景中移除君主-{}押镖机器人成功", ybrobot.jzId);
	}
	
	
	/**
	 * @Description 移除君主马车
	 */
	public void exitYBSc(IoSession session) {
		Integer uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		if(uid == null){
			log.error("移除君主马车出错：找不到君主{}马车的uid",session);
			return;
		}
		ExitScene.Builder exitYBSc = ExitScene.newBuilder();
		exitYBSc.setUid(uid);
		exitYBScene( session,exitYBSc);
	}
	public boolean isBiaoChe(IoSession session) {
		Integer uid = (Integer) session.getAttribute(SessionAttKey.RobotType);
		if(uid!=null&&uid.intValue() == Scene.YBRobot_RoleId){
			return true;
		}
		return false;
	}
	
	@Override
	public void playerDie(Player defender,  int killerUid) {
		BuffMgr.inst.removeBuff(defender.jzId);
		int onSiteReviveCost = 20;//默认20，为了假如找不到配置能够继续执行
		int remainReviveTimes = 0;
		if(defender.jzId > 0 && defender.roleId != YBRobot_RoleId) {// 表示是真实玩家
			int reviveOnDeadPosTimes = YaBiaoHuoDongMgr.inst.getReviveOnDeadPosTimes(defender.jz);
			Purchase purchase = PurchaseMgr.inst.getPurchaseCfg(PurchaseConstants.YB_REVIVE_DEAD_POS, reviveOnDeadPosTimes+1);
			if(purchase == null) {
				log.error("找不到类型为:{}的purchase配置", PurchaseConstants.YB_REVIVE_DEAD_POS);
			} else {
				onSiteReviveCost = purchase.getYuanbao();
			}
			remainReviveTimes = YaBiaoHuoDongMgr.inst.getFuhuoTimes(defender.jz);
		}
		
		PlayerDeadNotify.Builder deadNotify = PlayerDeadNotify.newBuilder();
		deadNotify.setUid(defender.userId);
		deadNotify.setKillerUid(killerUid);
		deadNotify.setAutoReviveRemainTime(YunbiaoTemp.autoResurgenceTime);
		deadNotify.setRemainAllLifeTimes(remainReviveTimes);
		deadNotify.setOnSiteReviveCost(onSiteReviveCost);
		broadCastEvent(deadNotify.build(), 0);
	}
	@Override
	public boolean checkSkill(JunZhu attacker, Player attackPlayer, Player targetPlayer, int skillId) {
		int attackUid = attackPlayer.userId;
		YBBattleBean ybBattle = YaBiaoHuoDongMgr.inst.getYBZhanDouInfo(attacker.id, attacker.vipLevel);
		if(skillId != 121 && targetPlayer.safeArea >= 0) {
			log.info("攻击失败，被攻击的目标在安全区，safeArea:{}，targetJZId:{}",targetPlayer.safeArea,targetPlayer.jzId);
			FightMgr.inst.sendAttackError(Result.TARGET_IN_SAFE_AREA, this, attackUid);
			return false;
		} else if(skillId == 121){
			if(YaBiaoHuoDongMgr.inst.getXuePingRemainTimes(attacker.id, attacker.vipLevel) <= 0) {
				return false;
			}
		}
		
		if(skillId == 121) {
			attackPlayer.xuePingRemain -= 1;
			if(ybBattle != null) {
				ybBattle.xueping4uesd += 1;
				HibernateUtil.save(ybBattle);
			}
		}
		return true;
	}
	
	public void hidden(Player p2, Player p1) {
		p1.visbileUids.remove(p2.userId);
		ExitScene.Builder exitYBSc = ExitScene.newBuilder();
		exitYBSc.setUid(p2.userId);
		ProtobufMsg pm = new ProtobufMsg();
		pm.id=PD.Exit_YBScene;
		pm.builder = exitYBSc;
		p1.session.write(pm);
	}
	
	public void show(Player p2, Player p1) {
		Object msg = buildEnterInfoCache(p2);
		p1.session.write(msg);
	}
}
