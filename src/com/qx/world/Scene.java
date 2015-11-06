package com.qx.world;

import java.util.HashMap;
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
import qxmobile.protobuf.Scene.EnterFightScene;
import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Scene.EnterSceneConfirm;
import qxmobile.protobuf.Scene.ExitFightScene;
import qxmobile.protobuf.Scene.ExitScene;
import qxmobile.protobuf.Scene.SpriteMove;
import qxmobile.protobuf.SoundData.PlayerSound;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.template.CanShu;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.alliance.AllianceMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.yabiao.YBRobot;


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
			//log.warn("player who want to move is null {}, jzId {}",uid,jzid);
			return;
		}
		
		player.setPosX(move.getPosX());
		player.setPosY(move.getPosY());
		player.setPosZ(move.getPosZ());
		
		move.setUid(uid);
		this.broadCastEvent(uid,move.build());
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

		Object uidObject = session.getAttribute(SessionAttKey.playerId_Scene);
		
		//bind this scene with this session
		session.setAttribute(SessionAttKey.Scene, this);
		
		final int userId = uidObject == null ? getUserId() : (Integer)uidObject;;
		session.setAttribute(SessionAttKey.playerId, userId);
		
		final Player player = new Player();
		player.userId = userId;
		session.setAttribute(SessionAttKey.playerId_Scene, userId);
		player.session = session;
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
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
	//进入房子
	public void enterHouseScene(IoSession session, final EnterScene.Builder enterScene) {
		
		Object uidObject = session.getAttribute(SessionAttKey.playerId_Scene);
		
		//bind this scene with this session
		session.setAttribute(SessionAttKey.Scene, this);
		
		final int userId = uidObject == null ? getUserId() : (Integer)uidObject;;
		session.setAttribute(SessionAttKey.playerId, userId);
		
		final Player player = new Player();
		player.userId = userId;
		session.setAttribute(SessionAttKey.playerId_Scene, userId);
		player.session = session;
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
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
				broadCastEventForHouse(pm, enterScene.getUid());
			}
		});
	}

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
			//
			ProtobufMsg msg = makeHeadPct(playerInfo, player);
			session.write(msg);
		}
	}
	public void informComerOtherPlayersForHouse(IoSession session, Player skip) {
		// TODO Auto-generated method stub
		log.warn("告知刚登陆玩家当前房屋玩家个数： " + players.size());
		for(Player player : players.values()){
			if(player .equals(skip) ){
				continue;
			}
			EnterScene.Builder playerInfo = EnterScene.newBuilder();
			
			playerInfo.setSenderName(player.getName());
			playerInfo.setUid(player.userId);
			playerInfo.setPosX(player.getPosX());
			playerInfo.setPosY(player.getPosY());
			playerInfo.setPosZ(player.getPosZ());
			playerInfo.setRoleId(player.roleId);
			ProtobufMsg pm = new ProtobufMsg();
			pm.id = PD.Enter_HouseScene;
			pm.builder = playerInfo;
			session.write(pm);
		}
	}
	

	public void broadCastEvent(Object build, int skip) {
		for(Player player : players.values()){
			if(player.userId == skip)continue;
			player.session.write(build);
		}
	}
	//增加房屋人员广播
	public void broadCastEventForHouse(ProtobufMsg pmsg, int skip) {
		for(Player player : players.values()){
			if(player.userId == skip)continue;
			player.session.write(pmsg);
			log.info("通知场景里的玩家{}，有人进来了，消息id{},内容{}", player.jzId, pmsg.id, pmsg.builder);
		}
	}
	
	public void broadCastEvent(ProtobufMsg pmsg, int skip) {
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
			case PD.Enter_HouseScene:
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
			default:
				log.warn("unkown code: {}" , code);
				break;
		}
	}

	protected void exitFightScene(ExitFightScene.Builder exitFight, IoSession session) {
		int uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		Player player = players.get(uid);
		if(player == null) {
			return;
		}
		long junzhuId = player.jzId;
		ProtobufMsg pm = new ProtobufMsg();
		pm.id=PD.EXIT_FIGHT_SCENE;
		pm.builder = exitFight;
		exitFight.setJunzhuId(junzhuId);
		broadCastEvent(pm, uid);
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
		Integer pid = (Integer) session.getAttribute(SessionAttKey.playerId);
		if(pid == null){
			log.error("报告玩家状态发生错误，场景:{},状态:{},pid is null {}", name, state, session);
			return;
		}
		Player p = players.get(pid);
		if(p == null){
			log.error("报告玩家状态发生错误，场景:{},状态:{}，player not find with id {}",name,state, pid);
			return;
		}
		p.pState = state;
		log.info("player {} change state to {}", p.getName(), p.pState);
		//发送其他玩家信息给当前玩家。
		switch(p.pState) {
			case State_FIGHT_SCENE:
				processStateOnFight(session, p);
				break;
			case State_HOUSE:
				informComerOtherPlayersForHouse(session, p);
				log.info("同步小屋玩家信息给 {}",p.getName());
				EnterScene.Builder enterHouseInfo = EnterScene.newBuilder();
				enterHouseInfo.setSenderName(p.getName());
				enterHouseInfo.setUid(p.userId);
				enterHouseInfo.setPosX(p.getPosX());
				enterHouseInfo.setPosY(p.getPosY());
				enterHouseInfo.setPosZ(p.getPosZ());
				enterHouseInfo.setRoleId(p.roleId);
				ProtobufMsg pm = new ProtobufMsg();
				pm.id = PD.Enter_HouseScene;
				pm.builder = enterHouseInfo;
				broadCastEventForHouse(pm, p.userId);
				break;
			case State_LEAGUEOFCITY:
				informComerOtherPlayers(session, p);
				log.info("同步在线玩家信息给 {}",p.getName());
				EnterScene.Builder enterCity = EnterScene.newBuilder();
				enterCity.setSenderName(p.getName());
				enterCity.setUid(p.userId);
				enterCity.setPosX(p.getPosX());
				enterCity.setPosY(p.getPosY());
				enterCity.setPosZ(p.getPosZ());
				enterCity.setRoleId(p.roleId);
				broadCastEvent(enterCity.build(), p.userId);
				break;
			case State_LOADINGSCENE:
				break;
			case State_PVEOFBATTLE:
				ExitScene.Builder eb = ExitScene.newBuilder();
				eb.setUid(p.userId);
				broadCastEvent(eb.build(), eb.getUid());
				break;
			case State_YABIAO:
				break;
			default:
				break;
		}
		
	}

	public void ExitScene(ExitScene.Builder exit, IoSession session) {
		if (exit == null) {
			return;
		}
		log.warn("退出 {}" ,session.getAttribute(SessionAttKey.junZhuId));
		int uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
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
		int uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		ProtobufMsg pm = new ProtobufMsg();
		pm.id=PD.Exit_HouseScene;
		pm.builder=exit;
		exit.setUid(uid);
		broadCastEventForHouse(pm, uid);
		Player ep = players.remove(uid);
		log.warn("{}房屋场景剩余玩家个数：{}" ,this.name, players.size());
		if(ep != null){
			PosInfo pi = new PosInfo();
			pi.jzId = ep.jzId;
			pi.x = ep.posX;
			pi.y = ep.posY;
			pi.z = ep.posZ;
			HibernateUtil.save(pi);
		}
	}

	public void exitForTrasn(Long jzId) {
		IoSession ss = AccountManager.sessionMap.get(jzId);
		if(ss == null)
			return;
		Object uidObject = ss.getAttribute(SessionAttKey.playerId_Scene);
		if(uidObject == null)
			return;
		Integer uid = (Integer)uidObject;
		players.remove(uid);
		ExitScene.Builder b = ExitScene.newBuilder();
		b.setUid(uid);
		broadCastEvent(b.build(), b.getUid());
	}
	public void exitForYaBiao(Long jzId) {
		YBRobot ybrobot=(YBRobot)BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(jzId);
		IoSession session=ybrobot.session;
		if(session == null)
			return;
		Object uidObject = session.getAttribute(SessionAttKey.playerId_Scene);
		if(uidObject == null)
			return;
		Integer uid = (Integer)uidObject;
		players.remove(uid);
		log.info("从场景中移除君主-{}押镖机器人成功", jzId);
		//移除押镖机器人
		BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.remove(jzId);
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
