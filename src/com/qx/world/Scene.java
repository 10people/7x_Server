package com.qx.world;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.AllianceFightProtos.SafeAreaBloodReturn;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.PlayerData;
import qxmobile.protobuf.PlayerData.State;
import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Scene.EnterSceneConfirm;
import qxmobile.protobuf.Scene.ExitFightScene;
import qxmobile.protobuf.Scene.ExitScene;
import qxmobile.protobuf.Scene.SpriteMove;
import qxmobile.protobuf.SoundData.PlayerSound;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.LianMengKeJi;
import com.manu.dynasty.template.YunBiaoSafe;
import com.manu.dynasty.template.YunbiaoTemp;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.building.JianZhuMgr;
import com.qx.explore.treasure.BaoXiangBean;
import com.qx.explore.treasure.ExploreTreasureMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.robot.RobotSession;
import com.qx.yabiao.LastExitYBInfo;
import com.qx.yabiao.YaBiaoHuoDongMgr;
import com.qx.yabiao.YaBiaoRobot;


/**
 *场景坐标移动、进入场景、离开场景等逻辑处理。
 * @author Hudali
 *
 */
//public class Scene {
public class Scene implements Runnable{

	public static AtomicInteger atomicInteger = new AtomicInteger(1);
	
	//public static final Scene instance = new Scene();
	public static Logger log = LoggerFactory.getLogger(Scene.class);
	public volatile ConcurrentHashMap<Integer, Player> players = null;
	public BlockingQueue<Mission> missions = new LinkedBlockingQueue<Mission>();
	public ExecutorService syncSceneExecutor = Executors.newSingleThreadExecutor();
	public static Mission exit = new Mission(0,null,null);
	public String name;
	public static int YBRobot_RoleId= 50000;//镖车机器人的roleId 很大的数 区分Player是玩家还是玩家的镖车
	
	public Scene(String key){
		players  = new ConcurrentHashMap<Integer, Player>();
		name = key;
	}
	
	public void startMissionThread(){
		//线程的命名规则待定
		new Thread(this, "Scene "+name).start();
		log.warn("启动场景{}",name);
	}
	public void shutdown(){
		missions.add(exit);
	}
	public void run() {
		while (true) {
				Mission mission = null;
				try {
					mission = missions.take();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				if(mission==exit){
					break;
				}
				try {
					completeMission(mission);
				} catch (Throwable e) {
					log.error("线程休眠异常", e);
				}
		}
		syncSceneExecutor.shutdown();
		log.info("退出 {}",Thread.currentThread().getName());
	}
	
	public void exec(int code, IoSession session, Builder builder) {
		Mission mission = new Mission(code,session,builder);
		Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
		if(this != scene) {
			session.setAttribute(SessionAttKey.Scene, this);
		}
		missions.add(mission);
	}

	
	//******************************************内部方法************************************************************

	public void spriteMove(IoSession session, SpriteMove.Builder move) {
		Integer uid = (Integer)session.getAttribute(SessionAttKey.playerId_Scene);
		if(uid == null){
			log.error("找不到scene里的uid");
			return;
		}
	
		Player player = players.get(uid);
		if (player == null) {
			Object jzid = session.getAttribute(SessionAttKey.junZhuId);
			log.warn("player who want to move is null {}, jzId {},场景名称{},{}",uid,jzid,this.name,session);
			return;
		}
		
		if((this.name.contains("YB"))&& player.roleId != YBRobot_RoleId && (player.pState!=State.State_YABIAO)){
			return;
		}
		player.setPosX(move.getPosX());
		player.setPosY(move.getPosY());
		player.setPosZ(move.getPosZ());
		move.setUid(player.userId);
		move.setDir(move.getDir());
		
		this.broadCastEvent(player.userId,move.build());
		//刷新所在的安全区位置
		if(this.name.contains("YB")){
			int safearea=YaBiaoHuoDongMgr.inst.getSafeArea(player.getPosX(), player.getPosZ());
			player.safeArea=safearea;
		}
	}

	public void broadCastEvent(int uid, SpriteMove build) {
		try {
			if (players == null) {
				log.error("players is null");
				return;
			}
			for(Player player : players.values()){
//				if (player.userId == uid) {
//					continue;
//				}
				//FIXME 在小屋、联盟城、主城、押镖场景、战斗场景才给广播
				if(player.pState != State.State_LEAGUEOFCITY
						&&player.pState != State.State_HOUSE
						&& player.pState != State.State_FIGHT_SCENE
						&& player.pState != State.State_YABIAO){
					continue;
				}
				player.session.write(build);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void enterScene(IoSession session, final EnterScene.Builder enterScene) {
		JunZhu jz = null;
		if(session instanceof RobotSession){
			
		}else{
			jz = JunZhuMgr.inst.getJunZhu(session);
			if(jz == null) {
				return;
			}
			ExploreTreasureMgr.inst.playerEnter(session);
		}

		Object uidObject = session.getAttribute(SessionAttKey.playerId_Scene);
		
		//bind this scene with this session
		session.setAttribute(SessionAttKey.Scene, this);
		
		final int userId = uidObject == null ? getUserId() : (Integer)uidObject;;
		session.setAttribute(SessionAttKey.playerId, jz==null?0:jz.id);
		
		final Player player = new Player();
		player.userId = userId;
		session.setAttribute(SessionAttKey.playerId_Scene, userId);
		player.session = session;
		player.setName(jz == null ? enterScene.getSenderName() : jz.name);
		log.info("进入场景 {}:{}", this.name,player.getName());
		player.setPosX(enterScene.getPosX());
		player.setPosY(enterScene.getPosY());
		player.setPosZ(enterScene.getPosZ());
		player.jzId = (jz == null ? 0 : jz.id);
		player.allianceId = AllianceMgr.inst.getAllianceId(player.jzId);
		player.roleId = (jz == null ? 1: jz.roleId);
		Integer ForceRoleId = (Integer) session.getAttribute("ForceRoleId");
		if(ForceRoleId != null){
			player.roleId = ForceRoleId;
		}
		player.chengHaoId = (String)session.getAttribute(SessionAttKey.CHENG_HAO_ID, "-1");
		player.lmName = (String)session.getAttribute(SessionAttKey.LM_NAME, "***");
		player.vip = jz == null ? 0:jz.vipLevel;
		player.zhiWu = (Integer)session.getAttribute(SessionAttKey.LM_ZHIWU, -1);
		players.put(userId, player);
		
		//告诉当前玩家它的信息，确认进入
		EnterSceneConfirm.Builder ret = EnterSceneConfirm.newBuilder();
		ret.setUid(userId);
		session.write(ret.build());
/*2016年3月12日21:01:19 ， 改变状态时再同步 
		//告诉其他玩家，谁进来了。
		enterScene.setUid(userId);
		enterScene.setSenderName(player.getName());
		enterScene.setRoleId(jz == null ? 1: jz.roleId);
		enterScene.setJzId(player.jzId);
		syncSceneExecutor.submit(new Runnable() {
			@Override
			public void run() {
				broadCastEvent(enterScene.build(), enterScene.getUid());
				ProtobufMsg msg = makeHeadPct(player);
				broadCastEvent(msg, enterScene.getUid());
			}
		});
		*/
	}
	public ProtobufMsg makeHeadPct(final Player player) {
		ErrorMessage.Builder head = ErrorMessage.newBuilder();
		head.setCmd(0);
		head.setErrorCode(player.userId);
		head.setErrorDesc("chengHao:"+player.chengHaoId
				+"#$#LM:"+player.lmName
				+"#$#VIP:"+(player.vip)
				+"#$#ZhiWu:"+(player.zhiWu)
				);
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.S_HEAD_STRING;
		msg.builder = head;
		return msg;
	}
	//进入房子 2015年11月27日策划废弃联盟城 没房子废弃
	public void enterHouseScene(IoSession session, final EnterScene.Builder enterScene) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null) {
			return;
		}
		
		Object uidObject = session.getAttribute(SessionAttKey.playerId_Scene);
		
		//bind this scene with this session
		session.setAttribute(SessionAttKey.Scene, this);
		
		final int userId = uidObject == null ? getUserId() : (Integer)uidObject;;
		session.setAttribute(SessionAttKey.playerId, jz.id);
		
		final Player player = new Player();
		player.userId = userId;
		session.setAttribute(SessionAttKey.playerId_Scene, userId);
		player.session = session;
		player.setName(jz == null ? enterScene.getSenderName() : jz.name);
		log.info("进入场景 {}:{}", this.name,player.getName());
		player.setPosX(enterScene.getPosX());
		player.setPosY(enterScene.getPosY());
		player.setPosZ(enterScene.getPosZ());
		player.jzId = (jz == null ? 0 : jz.id);
		player.allianceId = AllianceMgr.inst.getAllianceId(player.jzId);
		player.roleId = (jz == null ? 1: jz.roleId);
		players.put(userId, player);
		
		//告诉当前玩家它的信息，确认进入
		EnterSceneConfirm.Builder ret = EnterSceneConfirm.newBuilder();
		ret.setUid(userId);
		session.write(ret.build());
		
		//告诉其他玩家，谁进来了。
		enterScene.setUid(userId);
		enterScene.setJzId(player.jzId);
		enterScene.setSenderName(player.getName());
		enterScene.setRoleId(jz == null ? 1 : jz.roleId);
		syncSceneExecutor.submit(new Runnable() {
			@Override
			public void run() {
				ProtobufMsg pm = new ProtobufMsg();
				pm.id = PD.Enter_HouseScene;
				pm.builder = enterScene;
				broadCastEvent(pm, enterScene.getUid());
			}
		});
	}
	/**
	 * @Description
	 * @param session 	告诉进入某个场景的人当前场景中都有谁
	 * @param skip 进入的人的 Player对象
	 */
	public void informComerOtherPlayers(IoSession session, Player skip) {
		// TODO Auto-generated method stub
		log.warn("告知刚登陆玩家当前在线玩家个数： " + players.size());
		for(Player player : players.values()){
			if(player.equals(skip)){
				continue;
			}
			EnterScene.Builder playerInfo = EnterScene.newBuilder();
			
			playerInfo.setSenderName(player.getName());
			playerInfo.setUid(player.userId);
			playerInfo.setPosX(player.getPosX());
			playerInfo.setPosY(player.getPosY());
			playerInfo.setPosZ(player.getPosZ());
			playerInfo.setRoleId(player.roleId);
			playerInfo.setJzId(player.jzId);
			session.write(playerInfo.build());
			//更新脑门上的称号
			ProtobufMsg msg = makeHeadPct(player);
			session.write(msg);
		}
	}

	
	/**
	 * @Description
	 * @param session 	告诉进入某个场景的人当前场景中都有谁
	 * @param msgId 进入场景的协议ID
	 * @param enterPlayer 进入的人的 Player对象
	 */
	public void informComerOtherPlayers(IoSession session,int msgId, Player enterPlayer) {
		long jzId=enterPlayer.jzId;
		log.warn("告诉进入某个场景的--{}当前场景--{}中都有谁，人数--{}", enterPlayer.name,this.name, players.size());
		for(Player p : players.values()){
			if(p == enterPlayer){
				continue;//跳过自己。
			}
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
			enterSc.setXuePingRemain(p.xuePingRemain);
			enterSc.setJzId(p.jzId);
			if(p.jzId>0&&p.roleId!=YBRobot_RoleId){//2015年12月31日 只有君主才赋值血量){
				boolean IsEnemy =YaBiaoHuoDongMgr.inst.isEmeny(jzId, p.jzId);
				enterSc.setIsEnemy(IsEnemy);
			}
			ProtobufMsg pm = new ProtobufMsg();
			pm.id = msgId;
			pm.builder = enterSc;
			session.write(pm);
		}
	}
	
	
	
	/**
	 * @Description 向场景中的人广播某人进来了
	 * @param build
	 * @param skip
	 */
	public void broadCastEvent(Object build, int skip) {
		for(Player player : players.values()){
			if(player.userId == skip)continue;
			player.session.write(build);
		}
	}
	
	
	/**
	 * @Description 广播某人进入某个场景给场景里的其他人
	 * @param protobufMsgId ProtobufMsg的协议ID
	 * @param p 进入场景的Player对象
	 */
	public void broadCastEvent(int msgId ,Player p){
		log.info(" 广播name=={} 进入场景--{}",p.name,this.name);
		
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
		if(this.name.contains("YB")){
			//2016年3月10日10:44:09 由于需要发送押镖人自己的信息，所以不跳过自己
			enterSc.setXuePingRemain(p.xuePingRemain);
			broadCastEvent4YB(enterSc, 0/*p.userId*/);
		}else{
			ProtobufMsg pm = new ProtobufMsg();
			pm.id = msgId;
			pm.builder = enterSc;
			broadCastEvent(pm,p.userId);
		}
	}
	//增加房屋人员广播 重复方法废弃无用
	
	/**
	 * @Description 广播某人 进/出场景
	 * @param pmsg
	 * @param skip 某人userId
	 */
	public void broadCastEvent(ProtobufMsg pmsg, int skip) {
		log.info(" 广播userId=={} 进/出场景--{}",skip,this.name);
		for(Player player : players.values()){
//			if(player.userId == skip)continue;
			player.session.write(pmsg);
		}
	}
	/**
	 * @Description 广播某人 进/出场景
	 * @param pmsg
	 * @param skip 某人userId
	 */
	public void broadCastEvent4YB(EnterScene.Builder enterYBSc, int skip) {
		ProtobufMsg pm = new ProtobufMsg();
		pm.id = PD.Enter_YBScene;
		long ybjzId=enterYBSc.getJzId();
		log.info(" 广播userId=={} 进场景--{}",skip,this.name);
		for(Player player : players.values()){
			if(player.userId == skip)continue;
			if(ybjzId>0){
				boolean IsEnemy =YaBiaoHuoDongMgr.inst.isEmeny(player.jzId, ybjzId);
				enterYBSc.setIsEnemy(IsEnemy);
			}
			pm.builder = enterYBSc;
			player.session.write(pm);
		}
	}
	public void broadCastEvent2All4YB(EnterScene.Builder enterYBSc) {
		ProtobufMsg pm = new ProtobufMsg();
		pm.id = PD.Enter_YBScene;
		long ybjzId=enterYBSc.getJzId();
		log.info(" 广播{}:{} 进/出场景--{}",enterYBSc.getUid(),enterYBSc.getSenderName(),this.name);
		for(Player player : players.values()){
			if(player.jzId<0)continue;
			if(ybjzId>0){
				boolean IsEnemy =YaBiaoHuoDongMgr.inst.isEmeny(player.jzId, ybjzId);
				enterYBSc.setIsEnemy(IsEnemy);
			}
			pm.builder = enterYBSc;
			player.session.write(pm);
		}
	}

	public int getUserId() {
		return atomicInteger.getAndIncrement();
	}

	public void completeMission(Mission mission) {
		if (mission == null) {
			log.error("mission is null...");
			return;
		}
		
		int code = mission.code;
		final Builder builder = mission.builer;
		final IoSession session = mission.session;
		switch (code) {
			case PD.Enter_Scene:
				EnterScene.Builder enterScene = (EnterScene.Builder)builder;
				enterScene(session,enterScene);
				break;
			case PD.Enter_HouseScene://2015年11月27日 策划删除联盟城 房屋废弃
				EnterScene.Builder enterHouseScene = (EnterScene.Builder)builder;
				enterHouseScene(session,enterHouseScene);
				break;
			case PD.C_SHOW_WU_QI:
				setShowWuQi(code,session,builder);
				break;
			case PD.Spirite_Move:
				SpriteMove.Builder move = (SpriteMove.Builder)builder;
				spriteMove(session,move);
				break;
			case PD.EXIT_FIGHT_SCENE:
				ExitFightScene.Builder exitFight = (ExitFightScene.Builder) builder;
				exitFightScene(exitFight, session);
				break;
			case PD.Exit_HouseScene:
				ExitScene.Builder exitHouse = (ExitScene.Builder) builder;
				ExitHouseScene(exitHouse,session);
				break;
			case PD.Exit_Scene:
				ExitScene.Builder exit = (ExitScene.Builder) builder;
				ExitScene(exit,session);
				break;
			case PD.PLAYER_STATE_REPORT:
				PlayerData.PlayerState.Builder psd = (qxmobile.protobuf.PlayerData.PlayerState.Builder) builder;
				clientStateChange(psd, session);
				break;
			case PD.PLAYER_SOUND_REPORT:
				PlayerSound.Builder psdb = (qxmobile.protobuf.SoundData.PlayerSound.Builder) builder;
				sendSound(psdb);
				break;
			case PD.C_APP_SLEEP:
				break;
			case PD.C_APP_WAKEUP:
				if(session.getAttribute(SessionAttKey.junZhuId) != null)
					JunZhuMgr.inst.sendMainInfo(session);
				break;
			case PD.ENTER_FIGHT_SCENE:
				EnterScene.Builder enterFightScene = (EnterScene.Builder)builder;
				enterFightScene(session, enterFightScene);
				break;
			case PD.Enter_YBScene:
				EnterScene.Builder enterYBScene = (EnterScene.Builder)builder;
				enterYBScene(session,enterYBScene);
				break;
			case PD.Exit_YBScene:
				ExitScene.Builder exitYBSc = (ExitScene.Builder) builder;
				exitYBScene( session,exitYBSc);
				break;
			case PD.SAFE_AREA_BOOLD_RETURN:
				bloodReturnInSafeArea();
				break;
			default:
				log.warn("Scene场景处理不了的协议unkown code: {}" , code);
				break;
		}
	}

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

	public void setShowWuQi(int code, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			return;
		}
		int useWq = 1;
		PosInfo p = HibernateUtil.find(PosInfo.class, junZhuId);
		if(p == null){
			p = new PosInfo();
			p.x = p.y = p.z = 0;
			p.jzId = junZhuId;
			p.showWuQi = useWq;
			HibernateUtil.insert(p);
		}else{
			p.showWuQi = useWq;
			HibernateUtil.update(p);
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
		saveExitYBInfo(player);
		log.info("君主:{}退出押镖场景:{},剩余玩家个数：{},退出时坐标x--{},z---{}"
				 ,session.getAttribute(SessionAttKey.junZhuId),this.name, players.size(), player.posX, player.posZ);
	}

	public void saveExitYBInfo(long junzhuId) {
		Player player = getPlayerByJunZhuId(junzhuId);
		if(player != null) {
			saveExitYBInfo(player);
		}
	}
	
	protected void saveExitYBInfo(Player player) {
		if(player.jzId>0 && player.roleId != Scene.YBRobot_RoleId){//马车不保存
			LastExitYBInfo lastExitInfo = HibernateUtil.find(LastExitYBInfo.class, player.jzId);
			if(lastExitInfo == null) {
				lastExitInfo = new LastExitYBInfo(player.jzId, player.safeArea, player.currentLife, 
						player.posX, player.posY, player.posZ);
			} else {
				lastExitInfo.updateInfo(player.safeArea, player.currentLife, player.posX, player.posY, player.posZ);
			}
			log.info("君主:{}离开押镖场景，坐标x,z:{},{},剩余血量:{},处于安全区id:{}",player.name,player.posX, player.posZ,player.currentLife,player.safeArea);
			HibernateUtil.save(lastExitInfo);
		}
	}

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
					log.info("君主:{}进入押镖场景:{},原来血量:{},增加血量:{},总血量:{}",player.name, this.name, player.currentLife, addLife, addLife + lastExitInfo.remainLife);
					player.currentLife = addLife + lastExitInfo.remainLife;
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
			broadCastEvent(PD.Enter_YBScene, player);
		}else{
			//告诉当前玩家它的信息，确认进入
			EnterSceneConfirm.Builder ret = EnterSceneConfirm.newBuilder();
			ret.setUid(userId);
			ret.setPosX(player.getPosX());
			ret.setPosY(player.getPosY());
			ret.setPosZ(player.getPosZ());
			session.write(ret.build());
		}
		/* 2016年3月7日17:25:41 发送状态时会进行玩家可见性同步。
		//告诉其他玩家，谁进来了。
		enterYBSc.setUid(userId);
		enterYBSc.setSenderName(player.getName());
		enterYBSc.setRoleId(player.roleId);
		enterYBSc.setPosX(player.getPosX());
		enterYBSc.setPosY(player.getPosY());
		enterYBSc.setPosZ(player.getPosZ());
		int chenghaId=Integer.valueOf(player.chengHaoId==null?"0":player.chengHaoId);
		enterYBSc.setChengHao(chenghaId);
		enterYBSc.setCurrentLife(player.currentLife);
		enterYBSc.setTotalLife(player.totalLife);    
		enterYBSc.setAllianceName(player.lmName);
		enterYBSc.setVipLevel(player.vip);
		enterYBSc.setZhiWu(player.zhiWu);     
		//2015年12月9日 梁霄说策划加上国家，等级，战力
		enterYBSc.setLevel(player.jzlevel);
		enterYBSc.setZhanli(player.zhanli);
		enterYBSc.setGuojia(player.guojia);
		//2015年12月12日 加入马车价值 马车类型
		enterYBSc.setWorth(player.worth);
		enterYBSc.setHorseType(player.horseType);
		enterYBSc.setXuePingRemain(xuePingRemain);
		enterYBSc.setJzId(player.jzId);
		syncSceneExecutor.submit(new Runnable() {
			@Override
			public void run() {
				broadCastEvent2All4YB(enterYBSc);
			}
		});
		*/
	}

	protected void exitFightScene(ExitFightScene.Builder exitFight, IoSession session) {
		Integer uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		Player player = players.get(uid);
		if(player == null) {
			return;
		}
		ProtobufMsg pm = new ProtobufMsg();
		pm.id=PD.EXIT_FIGHT_SCENE;
		pm.builder = exitFight;
		exitFight.setUid(uid);
		broadCastEvent(pm, player.userId);
		players.remove(uid);
		log.info("退出联盟战场景成功，君主:{}退出联盟战场景:{},剩余玩家个数：{}" ,session.getAttribute(SessionAttKey.junZhuId),
				this.name, players.size());
	}

	public void sendSound(qxmobile.protobuf.SoundData.PlayerSound.Builder psdb) {
		broadCastEvent(psdb.build(), -1);
		log.debug("发布声音完毕。长度{}",psdb.getSSoundDataCount());
	}

	public void clientStateChange(
			qxmobile.protobuf.PlayerData.PlayerState.Builder psd, IoSession session) {
		State state = psd.getSState();
		Integer uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		if(uid == null){
			log.error("报告玩家状态发生错误，场景:{},状态:{},pid is null {}", name, state, session);
			return;
		}
		Player enterPlayer = players.get(uid);
		if(enterPlayer == null){
			log.error("报告玩家状态发生错误，场景:{},状态:{}，player not find with uid {}",name,state, uid);
			return;
		}
		enterPlayer.pState = state;
		log.info("player {} change state to {}", enterPlayer.getName(), enterPlayer.pState);
		//发送其他玩家信息给当前玩家。
		switch(enterPlayer.pState) {
			case State_FIGHT_SCENE:
				processStateOnFight(session, enterPlayer);
				break;
			case State_HOUSE: //2015年11月27日 策划删除联盟城 房屋废弃
				informComerOtherPlayers(session, enterPlayer);
				log.info("同步小屋玩家信息给 {}",enterPlayer.getName());
				EnterScene.Builder enterHouseInfo = EnterScene.newBuilder();
				enterHouseInfo.setSenderName(enterPlayer.getName());
				enterHouseInfo.setUid(enterPlayer.userId);
				enterHouseInfo.setPosX(enterPlayer.getPosX());
				enterHouseInfo.setPosY(enterPlayer.getPosY());
				enterHouseInfo.setPosZ(enterPlayer.getPosZ());
				enterHouseInfo.setRoleId(enterPlayer.roleId);
				enterHouseInfo.setJzId(enterPlayer.jzId);
				ProtobufMsg pm = new ProtobufMsg();
				pm.id = PD.Enter_HouseScene;
				pm.builder = enterHouseInfo;
				broadCastEvent(pm, enterPlayer.userId);
				break;
			case State_LEAGUEOFCITY: //22015年11月27日 策划删除联盟城 废弃
				informComerOtherPlayers(session, enterPlayer);
				log.info("同步在线玩家信息给 {}",enterPlayer.getName());
				EnterScene.Builder enterCity = EnterScene.newBuilder();
				enterCity.setSenderName(enterPlayer.getName());
				enterCity.setUid(enterPlayer.userId);
				enterCity.setPosX(enterPlayer.getPosX());
				enterCity.setPosY(enterPlayer.getPosY());
				enterCity.setPosZ(enterPlayer.getPosZ());
				enterCity.setRoleId(enterPlayer.roleId);
				enterCity.setJzId(enterPlayer.jzId);
				broadCastEvent(enterCity.build(), enterPlayer.userId);
				break;
			case State_LOADINGSCENE:
				break;
			case State_PVEOFBATTLE:
				ExitScene.Builder eb = ExitScene.newBuilder();
				eb.setUid(enterPlayer.userId);
				broadCastEvent(eb.build(), eb.getUid());
				break;
			case State_YABIAO:
				//告诉他场景里有谁
				informComerOtherPlayers(session, PD.Enter_YBScene, enterPlayer);
				//告诉场景里的别人，他进来了
				broadCastEvent(PD.Enter_YBScene, enterPlayer);
				break;
			default:
				break;
		}
		
	}
	//2015年11月27日 策划删除联盟城 废弃
	public void ExitScene(ExitScene.Builder exit, IoSession session) {
		if (exit == null) {
			return;
		}
		log.warn("退出 {}" ,session.getAttribute(SessionAttKey.junZhuId));
		Integer uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		Player ep = players.remove(uid);
		log.warn("{}场景剩余玩家个数：{}" ,this.name, players.size());
		broadCastEvent(exit.build(), exit.getUid());
		if(ep != null){
			PosInfo pi = new PosInfo();
			pi.jzId = ep.jzId;
			pi.x = ep.posX;
			pi.y = ep.posY;
			pi.z = ep.posZ;
			HibernateUtil.save(pi);
		}
	}
	public void ExitHouseScene(ExitScene.Builder exit, IoSession session) {
		if (exit == null) {
			return;
		}
		log.warn("退出房屋 {}" ,session.getAttribute(SessionAttKey.junZhuId));
		Integer uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		ProtobufMsg pm = new ProtobufMsg();
		pm.id=PD.Exit_HouseScene;
		pm.builder=exit;
		exit.setUid(uid);
		broadCastEvent(pm, uid);
		Player ep = players.remove(uid);
		log.warn("{}房屋场景剩余玩家个数：{}" ,this.name, players.size());
	}

	public void exitForTrasn(Long jzId) {
		IoSession ss = AccountManager.sessionMap.get(jzId);
		if(ss == null)
			return;
		Integer uid = (Integer) ss.getAttribute(SessionAttKey.playerId_Scene);
		if(uid == null)
			return;
		players.remove(uid);
		ExitScene.Builder b = ExitScene.newBuilder();
		b.setUid(uid);
		broadCastEvent(b.build(), b.getUid());
	}
	/**
	 * @Description 移除君主马车，被杀掉的直接移除，不广播
	 * @param jzId
	 * @param isKill 是否被杀
	 */
	public synchronized void exit4YaBiaoRobot(YaBiaoRobot ybrobot) {
		IoSession session=ybrobot.session;
			Integer uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
			Player player = players.remove(uid);
		log.info("从场景中移除君主-{}押镖机器人成功", ybrobot.jzId);
	}

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
	
	public Player getPlayerByJunZhuId(long junzhuId) {
		for(Map.Entry<Integer, Player> entry : players.entrySet()) {
			Player player = entry.getValue();
			if(player.jzId == junzhuId && player.roleId != Scene.YBRobot_RoleId) {
				return player;
			}
		}
		return null;
	}
	
	public boolean isBiaoChe(IoSession session) {
		Integer uid = (Integer) session.getAttribute(SessionAttKey.RobotType);
		if(uid!=null&&uid.equals(Scene.YBRobot_RoleId)){
			return true;
		}
		return false;
	}
	
	public float getPlayerDistance(int uidOne, int uidOther) {
		Player playerOne = players.get(uidOne);
		Player playerTwo = players.get(uidOther);
		float distance = (float) Math.sqrt(
				Math.pow(playerOne.posX - playerTwo.posX, 2)+
				Math.pow(playerOne.posZ - playerTwo.posZ, 2));
		return distance;
	}
	
	protected void enterFightScene(IoSession session, final EnterScene.Builder enterFightScene) {
		// sub class implement
	}

	protected void informOtherFightScene(IoSession session, Player skip) {
		// sub class implement
	}
	
	protected void processStateOnFight(IoSession session, Player p) {
		// sub class implement
	}
}
