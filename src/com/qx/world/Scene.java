package com.qx.world;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.buffer.IoBuffer;
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

import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.util.ProtobufUtils;
import com.manu.network.PD;
import com.manu.network.ProtoBuffEncoder;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.alliance.AllianceMgr;
import com.qx.explore.treasure.ExploreTreasureMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.robot.RobotSession;



/**
 * java -jar -Djava.ext.dirs=libs -jar st.jar k2 >/dev/null
 *场景坐标移动、进入场景、离开场景等逻辑处理。
 * @author Hudali
 *
 */
//public class Scene {
public class Scene implements Runnable{

	public static AtomicInteger atomicInteger = new AtomicInteger(1);
	
	//public static final Scene instance = new Scene();
	public static Logger log = LoggerFactory.getLogger(Scene.class.getSimpleName());
	public volatile ConcurrentHashMap<Integer, Player> players = null;
	public BlockingQueue<Mission> missions = new LinkedBlockingQueue<Mission>();
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

	public Player spriteMove(IoSession session, SpriteMove.Builder move) {
		Integer uid = (Integer)session.getAttribute(SessionAttKey.playerId_Scene);
		if(uid == null){
			log.error("找不到scene里的uid");
			return null;
		}
	
		Player player = players.get(uid);
		if (player == null) {
			Object jzid = session.getAttribute(SessionAttKey.junZhuId);
			log.warn("player who want to move is null {}, jzId {},场景名称{},{}",uid,jzid,this.name,session);
			return null;
		}
		
		if((this.name.contains("YB"))&& player.roleId != YBRobot_RoleId && (player.pState!=State.State_YABIAO)){
			return null;
		}
		player.setPosX(move.getPosX());
		player.setPosY(move.getPosY());
		player.setPosZ(move.getPosZ());
		move.setUid(player.userId);
		move.setDir(move.getDir());
		player.fullCache=null;
//		log.info("{} move to {},{}",player.userId,move.getPosX(),player.posZ);
		
		SpriteMove lite = move.build();
		short id = PD.Spirite_Move;
		IoBuffer buf = pack(lite, id);
		this.broadCastEvent(player.userId,buf);
		return player;
	}

	public IoBuffer pack(MessageLite s, short id) {
//		byte[] body = s.toByteArray();
//		int len = body.length;
//		IoBuffer buf = IoBuffer.allocate(body.length + 4 + 2);
//		buf.putInt(body.length + 2);//数据（协议号和逻辑数据）长度
//		buf.putShort(id);
//		buf.put(body);
//		buf.flip();
//		return 
		IoBuffer buf2 = IoBuffer.wrap(ProtoBuffEncoder.toByteArray(s, id));
//		if(buf2.remaining() != buf.remaining()){
//			len = 0;
//		}
		return buf2;
	}
	
	

	public void broadCastEvent(int uid0, Object build) {
		try {
			if (players == null) {
				log.error("players is null");
				return;
			}
			Player cur = players.get(uid0);
			for(Player player : players.values()){
				if (player.userId == uid0) {
					continue;
				}
				//FIXME 在小屋、联盟城、主城、押镖场景、战斗场景才给广播
				if(player.pState != State.State_LEAGUEOFCITY
						&&player.pState != State.State_HOUSE
						&& player.pState != State.State_FIGHT_SCENE
						&& player.pState != State.State_YABIAO){
					continue;
				}
				//镖车机器人 跳过
				if(player.roleId==YBRobot_RoleId){
					continue;
				}
				boolean isVisible = checkVisibility(cur, player);
				if(isVisible==false){
					continue;
				}
				if(build instanceof IoBuffer){
					build = ((IoBuffer)build).asReadOnlyBuffer();
//					((IoBuffer) build).flip();
					((IoBuffer) build).position(0);
				}
				player.session.write(build);
			}
		} catch (Exception e) {
			log.error("广播出错AA", e);
		}
	}
	public boolean checkVisibility(Player p1, Player p2){
		//总是可见
		return true;
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
		postEnter(player);
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
	public void postEnter(Player player) {
		// TODO Auto-generated method stub
		
	}

//	public ProtobufMsg makeHeadPct(final Player player) {
//		ErrorMessage.Builder head = ErrorMessage.newBuilder();
//		head.setCmd(0);
//		head.setErrorCode(player.userId);
//		head.setErrorDesc("chengHao:"+player.chengHaoId
//				+"#$#LM:"+player.lmName
//				+"#$#VIP:"+(player.vip)
//				+"#$#ZhiWu:"+(player.zhiWu)
//				);
//		ProtobufMsg msg = new ProtobufMsg();
//		msg.id = PD.S_HEAD_STRING;
//		msg.builder = head;
//		return msg;
//	}
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
//		syncSceneExecutor.submit(new Runnable() {
//			@Override
//			public void run() {
//				ProtobufMsg pm = new ProtobufMsg();
//				pm.id = PD.Enter_HouseScene;
//				pm.builder = enterScene;
//				broadCastEvent(pm, enterScene.getUid());
//			}
//		});
	}
	/**
	 * @Description
	 * @param session 	告诉进入某个场景的人当前场景中都有谁
	 * @param skip 进入的人的 Player对象
	 */
	public void informComerOtherPlayers(IoSession session, Player skip) {
		log.warn("告知刚登陆玩家当前在线玩家个数： " + players.size());
		for(Player player : players.values()){
			if(player.equals(skip)){
				continue;
			}
			EnterScene.Builder playerInfo = buildEnterInfo(player);
			
			session.write(playerInfo.build());
//			//更新脑门上的称号
//			ProtobufMsg msg = makeHeadPct(player);
//			session.write(msg);
		}
	}

	

	
	
	
	public EnterScene.Builder buildEnterInfo(Player player) {
		EnterScene.Builder playerInfo = EnterScene.newBuilder();
		
		playerInfo.setSenderName(player.getName());
		playerInfo.setUid(player.userId);
		playerInfo.setPosX(player.getPosX());
		playerInfo.setPosY(player.getPosY());
		playerInfo.setPosZ(player.getPosZ());
		playerInfo.setRoleId(player.roleId);
		playerInfo.setJzId(player.jzId);
		int chenghaId=Integer.valueOf(player.chengHaoId==null?"0":player.chengHaoId);
		playerInfo.setChengHao(chenghaId);
		playerInfo.setAllianceName(player.lmName);
		playerInfo.setVipLevel(player.vip);
		playerInfo.setZhiWu(player.zhiWu);
		return playerInfo;
	}

	/**
	 * @Description 向场景中的人广播某人进来了
	 * @param build
	 * @param skip
	 */
	public void broadCastEvent(MessageLite build, int skip) {
		Integer protoId = ProtobufUtils.protoClassToIdMap.get(build.getClass());
		IoBuffer io = pack(build, protoId.shortValue());
		for(Player player : players.values()){
			if(player.userId == skip)continue;
			//镖车机器人 跳过
			if(player.roleId==YBRobot_RoleId){
				continue;
			}
			IoBuffer dup = io.asReadOnlyBuffer();
			dup.position(0);
			if(player.session!=null)
			player.session.write(dup);
		}
	}
	

	//增加房屋人员广播 重复方法废弃无用
	
	/**
	 * @Description 广播某人 进/出场景
	 * @param pmsg
	 * @param skip 某人userId
	 */
	public void broadCastEvent(ProtobufMsg pmsg, int skip) {
		log.info("广播 skip userId {}, msg {} 场景 {}",skip,pmsg.builder.getClass().getSimpleName(),this.name);
		IoBuffer io = pack(pmsg.builder.build(), (short)pmsg.id);
		for(Player player : players.values()){
//			if(player.userId == skip)continue;
			//镖车机器人 跳过
			if(player.roleId==YBRobot_RoleId){
				continue;
			}
			IoBuffer dup = io.asReadOnlyBuffer();
			dup.position(0);
			if(player.session!=null)
			player.session.write(dup);
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
				ExploreTreasureMgr.inst.sendPickedInfo(session);
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
				//not used
//				ExitFightScene.Builder exitFight = (ExitFightScene.Builder) builder;
//				exitFightScene(exitFight, session);
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
				log.warn("Scene场景处理不了的协议unkown code: {}" , code);
				break;
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




//	protected void exitFightScene(ExitFightScene.Builder exitFight, IoSession session) {
//		Integer uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
//		Player player = players.get(uid);
//		if(player == null) {
//			return;
//		}
//		ProtobufMsg pm = new ProtobufMsg();
//		pm.id=PD.EXIT_FIGHT_SCENE;
//		pm.builder = exitFight;
//		exitFight.setUid(uid);
//		broadCastEvent(pm, player.userId);
//		players.remove(uid);
//		log.info("退出联盟战场景成功，君主:{}退出联盟战场景:{},剩余玩家个数：{}" ,session.getAttribute(SessionAttKey.junZhuId),
//				this.name, players.size());
//	}

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
			case State_YABIAO:
			case State_LEAGUEOFCITY: //22015年11月27日 策划删除联盟城 废弃
				informComerOtherPlayers(session, enterPlayer);
				log.info("同步在线玩家信息给 {}",enterPlayer.getName());
				bdEnterInfo(enterPlayer);
				break;
			case State_LOADINGSCENE:
				break;
			case State_PVEOFBATTLE:
				ExitScene.Builder eb = ExitScene.newBuilder();
				eb.setUid(enterPlayer.userId);
				broadCastEvent(eb.build(), eb.getUid());
				break;
			default:
				break;
		}
		
	}

	public void bdEnterInfo(Player enterPlayer) {
		EnterScene.Builder enterCity = buildEnterInfo(enterPlayer);
		broadCastEvent(enterCity.build(), enterPlayer.userId);
	}
	
	public Player ExitScene(ExitScene.Builder exit, IoSession session) {
		if (exit == null) {
			return null;
		}
		log.warn("退出 {}" ,session.getAttribute(SessionAttKey.junZhuId));
		Integer uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		Player ep = players.remove(uid);
		log.warn("{}场景剩余玩家个数：{}" ,this.name, players.size());
		broadCastEvent(exit.build(), exit.getUid());
		savePosInfo(ep);
		return ep;
	}

	public void savePosInfo(Player ep) {
		//宝箱场景不保存坐标，子类会覆盖此方法
		if(ep != null){
			PosInfo pi = new PosInfo();
			pi.jzId = ep.jzId;
			pi.x = ep.posX;
			pi.y = ep.posY;
			pi.z = ep.posZ;
			HibernateUtil.save(pi);
			if(ep.posX == 0 && ep.posY == -2.5f && ep.posZ == 0){
				System.out.println();
			}
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

//	public void exitForTrasn(Long jzId) {
//		IoSession ss = AccountManager.sessionMap.get(jzId);
//		if(ss == null)
//			return;
//		Integer uid = (Integer) ss.getAttribute(SessionAttKey.playerId_Scene);
//		if(uid == null)
//			return;
//		players.remove(uid);
//		ExitScene.Builder b = ExitScene.newBuilder();
//		b.setUid(uid);
//		broadCastEvent(b.build(), b.getUid());
//	}

	
	public Player getPlayerByJunZhuId(long junzhuId) {
		do{
			IoSession ss = AccountManager.sessionMap.get(junzhuId);
			if(ss == null){
				break;
			}
			{//test code
//				Scene sc = (Scene) ss.getAttribute(SessionAttKey.Scene);
//				log.info("sc is "+sc.name);
			}
			Integer uidObject = (Integer) ss.getAttribute(SessionAttKey.playerId_Scene);
			if(uidObject==null){
				break;
			}
			Player p = players.get(uidObject);
			if(p==null){
				break;
			}
			
			return p;
			
		}while(false);
		
		return null;
	}
	
	public float getPlayerDistance(int uidOne, int uidOther) {
		Player playerOne = players.get(uidOne);
		Player playerTwo = players.get(uidOther);
		if(playerOne == null || playerTwo == null) {
			return Integer.MAX_VALUE;
		}
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
	
	public void playerDie(JunZhu defender, int uid, int killerUid){
		// sub class implement
	}
	
	public boolean checkSkill(JunZhu attacker, Player attackPlayer, Player targetPlayer, int skillId){
		return true;
	}
}
