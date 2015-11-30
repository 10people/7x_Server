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
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
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
	public static Mission exitYBSc = new Mission(0,null,null);
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
		missions.add(exitYBSc);
	}
	public void run() {
		while (true) {
				Mission mission = null;
				try {
					mission = missions.take();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				if(mission==exitYBSc){
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
			log.warn("player who want to move is null {}, jzId {}",uid,jzid);
			return;
		}
		
		player.setPosX(move.getPosX());
		player.setPosY(move.getPosY());
		player.setPosZ(move.getPosZ());
		
		move.setUid(player.userId);
		this.broadCastEvent(player.userId,move.build());
	}

	public void broadCastEvent(int uid, SpriteMove build) {
		try {
			if (players == null) {
				log.error("players is null");
				return;
			}
			for(Player player : players.values()){
				if (player.userId == uid) {
					continue;
				}
				if(player.pState != State.State_LEAGUEOFCITY&&player.pState != State.State_HOUSE
						&& player.pState != State.State_FIGHT_SCENE){
					continue;
				}
				player.session.write(build);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void enterScene(IoSession session, final EnterScene.Builder enterScene) {
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
		player.roleId = (jz == null ? 1: jz.roleId);
		player.chengHaoId = (String)session.getAttribute(SessionAttKey.CHENG_HAO_ID, "-1");
		player.lmName = (String)session.getAttribute(SessionAttKey.LM_NAME, "***");
		player.vip = CanShu.VIPLV_ININT;
		player.zhiWu = (Integer)session.getAttribute(SessionAttKey.LM_ZHIWU, 0);
		players.put(userId, player);
		
		//告诉当前玩家它的信息，确认进入
		EnterSceneConfirm.Builder ret = EnterSceneConfirm.newBuilder();
		ret.setUid(userId);
		session.write(ret.build());

		//告诉其他玩家，谁进来了。
		enterScene.setUid(userId);
		enterScene.setSenderName(player.getName());
		enterScene.setRoleId(jz == null ? 1: jz.roleId);
		syncSceneExecutor.submit(new Runnable() {
			
			@Override
			public void run() {
				broadCastEvent(enterScene.build(), enterScene.getUid());
				
				ProtobufMsg msg = makeHeadPct(enterScene, player);
				broadCastEvent(msg, enterScene.getUid());
			}

		});
	}
	public ProtobufMsg makeHeadPct(final EnterScene.Builder enterScene,
			final Player player) {
		ErrorMessage.Builder head = ErrorMessage.newBuilder();
		head.setCmd(0);head.setErrorCode(enterScene.getUid());
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
		player.roleId = (jz == null ? 1: jz.roleId);
		players.put(userId, player);
		
		//告诉当前玩家它的信息，确认进入
		EnterSceneConfirm.Builder ret = EnterSceneConfirm.newBuilder();
		ret.setUid(userId);
		session.write(ret.build());
		
		//告诉其他玩家，谁进来了。
		enterScene.setUid(userId);
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
			
			session.write(playerInfo.build());
			//更新脑门上的称号
			ProtobufMsg msg = makeHeadPct(playerInfo, player);
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
		log.warn("告诉进入某个场景的--{}当前场景--{}中都有谁，人数--{}", enterPlayer.name,this.name, players.size());
		for(Player player : players.values()){
			//TODO 此处没有重写equals 可能出问题
			if(player.equals(enterPlayer) ){
				continue;
			}
			EnterScene.Builder otherPlayer = EnterScene.newBuilder();
			otherPlayer.setSenderName(player.getName());
			otherPlayer.setUid(player.userId);
			otherPlayer.setPosX(player.getPosX());
			otherPlayer.setPosY(player.getPosY());
			otherPlayer.setPosZ(player.getPosZ());
			otherPlayer.setRoleId(player.roleId);
			ProtobufMsg pm = new ProtobufMsg();
			pm.id = msgId;//PD.Enter_HouseScene;
			pm.builder = otherPlayer;
			session.write(pm);
			
			//更新脑门上的称号
			ProtobufMsg msg = makeHeadPct(otherPlayer, player);
			session.write(msg);
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
		
		ProtobufMsg pm = new ProtobufMsg();
		pm.id = msgId;
		pm.builder = enterSc;
		
		broadCastEvent(pm,p.userId);
	}
	//增加房屋人员广播 重复方法废弃无用
//	public void broadCastEventForHouse(ProtobufMsg pmsg, int skip) {
//		for(Player player : players.values()){
//			if(player.userId == skip)continue;
//			player.session.write(pmsg);
//			log.info("通知场景里的玩家{}，有人进来了，消息id{},内容{}", player.jzId, pmsg.id, pmsg.builder);
//		}
//	}
	
	/**
	 * @Description 广播某人 进/出场景
	 * @param pmsg
	 * @param skip 某人userId
	 */
	public void broadCastEvent(ProtobufMsg pmsg, int skip) {
		log.info(" 广播userId=={} 进/出场景--{}",skip,this.name);
		for(Player player : players.values()){
			if(player.userId == skip)continue;
			player.session.write(pmsg);
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
			default:
				log.warn("Scene场景处理不了的协议unkown code: {}" , code);
				break;
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
		log.info("君主:{}退出联盟战场景:{},剩余玩家个数：{}" ,session.getAttribute(SessionAttKey.junZhuId),this.name, players.size());
	}

	public void enterYBScene(IoSession session,final EnterScene.Builder enterYBSc) {
		//镖车机器人没有JunZhu对象
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		
		Object uidObject = session.getAttribute(SessionAttKey.playerId_Scene);
		session.setAttribute(SessionAttKey.Scene, this);
		final int userId = uidObject == null ? getUserId() : (Integer)uidObject;
//		session.setAttribute(SessionAttKey.playerId, jz.id); SessionAttKey.playerId 无用,
		
		final Player player = new Player();
		player.userId = userId;
		session.setAttribute(SessionAttKey.playerId_Scene, userId);
		player.session = session;
		player.setName(jz == null ? enterYBSc.getSenderName() : jz.name);
		
		player.setPosX(enterYBSc.getPosX());
		player.setPosY(enterYBSc.getPosY());
		player.setPosZ(enterYBSc.getPosZ());
		player.jzId = (jz == null ? 0 : jz.id);
		//roleId  镖车机器人的roleId 很大的数 区分Player是玩家还是玩家的镖车
		player.roleId = (jz == null ? YBRobot_RoleId: jz.roleId);
		players.put(userId, player);
		log.info("{}进入场景 {},这货是<{}>", player.getName(),this.name,(player.roleId==50000)?"镖车机器人":"玩家");
		//告诉当前玩家它的信息，确认进入
		EnterSceneConfirm.Builder ret = EnterSceneConfirm.newBuilder();
		ret.setUid(userId);
		session.write(ret.build());
		
		//告诉其他玩家，谁进来了。
		enterYBSc.setUid(userId);
		enterYBSc.setSenderName(player.getName());
		enterYBSc.setRoleId(player.roleId);
		syncSceneExecutor.submit(new Runnable() {
			@Override
			public void run() {
				ProtobufMsg pm = new ProtobufMsg();
				pm.id = PD.Enter_YBScene;
				pm.builder = enterYBSc;
				broadCastEvent(pm, enterYBSc.getUid());
			}
		});
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
		exitFight.setJunzhuId(player.jzId);
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
	
	public void exitForYaBiaoRobot(Long jzId) {
		YaBiaoRobot ybrobot=(YaBiaoRobot)BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(jzId);
		IoSession session=ybrobot.session;
		if(session == null)
			return;
		Integer uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		if(uid == null)
			return;
		players.remove(uid);
		log.info("从场景中移除君主-{}押镖机器人成功", jzId);
		//移除押镖机器人
		BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.remove(jzId);
	}
	
	public Player getPlayerByJunZhuId(long junzhuId) {
		Player player = null;
		for(Map.Entry<Integer, Player> entry : players.entrySet()) {
			Player p = entry.getValue();
			if(p.jzId == junzhuId) {
				player = p;
				break;
			}
		}
		return player;
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
