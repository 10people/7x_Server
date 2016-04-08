package com.qx.world;
import java.util.HashSet;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.AllianceFightProtos.SafeAreaBloodReturn;
import qxmobile.protobuf.PlayerData;
import qxmobile.protobuf.PlayerData.State;
import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Scene.EnterSceneConfirm;
import qxmobile.protobuf.Scene.ExitScene;
import qxmobile.protobuf.Scene.SpriteMove;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.template.LianMengKeJi;
import com.manu.dynasty.template.YunBiaoSafe;
import com.manu.dynasty.template.YunbiaoTemp;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.building.JianZhuMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.yabiao.LastExitYBInfo;
import com.qx.yabiao.YaBiaoHuoDongMgr;
import com.qx.yabiao.YaBiaoRobot;


/** 
 * 押镖场景Scene
 *
 */
public class YaBiaoScene  extends Scene{
	public static Logger log = LoggerFactory.getLogger(YaBiaoScene.class.getSimpleName());
	public static float visibleDist = 20;

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

	public Player spriteMove(IoSession session, SpriteMove.Builder move) {
		Player player=	super.spriteMove(session, move);
		//刷新所在的安全区位置
		if(player!=null){
			int safearea=YaBiaoHuoDongMgr.inst.getSafeArea(player.getPosX(), player.getPosZ());
			player.safeArea=safearea;
		}
		return player;
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
	public void clientStateChange(
			qxmobile.protobuf.PlayerData.PlayerState.Builder psd, IoSession session) {
		State state = psd.getSState();
		Integer uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
		if(uid == null){
			log.error("报告玩家状态发生错误，押镖场景:{},状态:{},pid is null {}", name, state, session);
			return;
		}
		Player enterPlayer = players.get(uid);
		if(enterPlayer == null){
			log.error("报告玩家状态发生错误，押镖场景:{},状态:{}，player not find with uid {}",name,state, uid);
			return;
		}
		enterPlayer.pState = state;
		log.info("player {} change state to {}", enterPlayer.getName(), enterPlayer.pState);
		//发送其他玩家信息给当前玩家。
		switch(enterPlayer.pState) {
		case State_YABIAO:
			//告诉他场景里有谁
			informComerOtherPlayers(session, PD.Enter_YBScene, enterPlayer);
			//告诉场景里的别人，他进来了
			broadCastEvent(PD.Enter_YBScene, enterPlayer);
			break;
		default:
//			log.warn("YaBiaoScene场景处理不了的状态变化， code: {}" , enterPlayer.pState);
			super.clientStateChange(psd, session); 
			break;
		}
		
	}
	/**
	 * @Description
	 * @param session 	告诉进入某个场景的人当前场景中都有谁
	 * @param msgId 进入场景的协议ID
	 * @param enterPlayer 进入的人的 Player对象
	 */
	public void informComerOtherPlayers(IoSession session,int msgId, Player enterPlayer) {
		log.info("告诉进入某个场景的--{}当前场景--{}中都有谁，人数--{}", enterPlayer.name,this.name, players.size());
		checkVisibleSet(enterPlayer);
		for(Player p : players.values()){
			if(p == enterPlayer){
				continue;//跳过自己。
			}
			if(enterPlayer.roleId !=YBRobot_RoleId && p.roleId !=YBRobot_RoleId){
				if(inRange(enterPlayer, p)==false){
					continue;
				}
				//对方的visbileUids的改变在下一步【告知在线玩家谁进来了】里面。
				enterPlayer.visbileUids.add(p.userId);
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
			ProtobufMsg pm = new ProtobufMsg();
			pm.id = msgId;
			pm.builder = enterSc;
			session.write(pm);
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
	}
	
	/**
	 * @Description 广播某人进入某个场景给场景里的其他人
	 * @param protobufMsgId ProtobufMsg的协议ID
	 * @param p 进入场景的Player对象
	 */
	public void broadCastEvent(int msgId ,Player p){
		log.info(" 广播name=={} 进入押镖场景--{}",p.name,this.name);
		
		EnterScene.Builder enterSc = buildEnterInfo(p);
//		if(this.name.contains("YB")){
			//2016年3月10日10:44:09 由于需要发送押镖人自己的信息，所以不跳过自己
			broadCastEvent4YB(enterSc, 0/*p.userId*/);
//		}else{
//			log.error(" 广播name=={} 进入押镖场景--{}异常，这不是押镖 场景",p.name,this.name);
//		}
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
	 * @Description 广播某人 进/出场景
	 * @param pmsg
	 * @param skip 某人userId
	 */
	public void broadCastEvent4YB(EnterScene.Builder enterYBSc, int skip) {
		IoBuffer io = pack(enterYBSc.build(), PD.Enter_YBScene);
		log.info(" 广播userId=={} 进场景--{}",skip,this.name);
		Player enterPlayer = players.get(enterYBSc.getUid());
		for(Player player : players.values()){
			if(player.userId == skip)continue;
			//镖车机器人 跳过
			if(player.roleId==YBRobot_RoleId){
				continue;
			}
			
			if(enterPlayer.roleId !=YBRobot_RoleId && player.roleId !=YBRobot_RoleId
					&& player != enterPlayer){
				if(inRange(enterPlayer, player)==false){
					continue;
				}
				checkVisibleSet(player);
				//对方的visbileUids的改变在上一步一步【告知当前玩家场景里有谁】里面。
				player.visbileUids.add(enterPlayer.userId);
			}
			
			//TODO 等梁霄做成客户端请求仇人列表
			IoBuffer dup = io.asReadOnlyBuffer();
			dup.position(0);
			player.session.write(dup);
		}
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
	public boolean inRange(Player cur, Player p2){
		if(cur==null ||p2==null){
			return false;
		}
		if(cur==p2){
			return true;
		}
		if(cur.roleId ==YBRobot_RoleId || p2.roleId ==YBRobot_RoleId){
			return true;
		}
//		visibleDist=20;
		float dx = Math.abs(cur.posX - p2.posX);
		float dz = Math.abs(cur.posZ - p2.posZ);
		boolean visible = false;
		if(dx>visibleDist || dz>visibleDist){
			
		}else{
			visible = true;
		}
		return visible;
	}
	public boolean checkVisibility(Player cur, Player p2){
		boolean v = inRange(cur, p2);
		if(cur.roleId ==YBRobot_RoleId || p2.roleId ==YBRobot_RoleId){
			//有马车参与，不做show、hidden的处理
			return v;
		}
		if(v){
			checkNeedShow(cur,p2);
		}else{
			//不在视野
			checkNeedHidden(cur,p2);
		}
		return v;
	}
	
	public void checkNeedShow(Player cur, Player p2) {
		if(cur == p2){
			return;
		}
		// 原先不可见，现在可见了，则需要给客户端发送进入场景
		checkVisibleSet(cur);
		checkVisibleSet(p2);
		if(cur.visbileUids.contains(p2.userId)){
			//之前就可见
			return;
		}
		//添加为互相可见。
		boolean a1 = cur.visbileUids.add(p2.userId);
		boolean a2 = p2.visbileUids.add(cur.userId);
		if(a1==false || a2 == false){//false就是之前添加过
			//log.warn("why?");
		}
		show(cur,p2);
		show(p2,cur);
		//cur的移动要回发给p2，p2的坐标要同步给cur
		/*发进入场景了，就不要这个了。
		SpriteMove.Builder move = SpriteMove.newBuilder();
		move.setDir(p2.userId%360);
		move.setUid(p2.userId);
		move.setPosX(p2.posX);
		move.setPosY(p2.posY);
		move.setPosZ(p2.posZ);
		SpriteMove build = move.build();
		cur.session.write(build);
		cur.session.write(build);
		*/
	}

	/**
	 * 给对方发送进入场景
	 * @param p2
	 * @param p1
	 */
	public void show(Player p2, Player p1) {
		qxmobile.protobuf.Scene.EnterScene.Builder msg = buildEnterInfo(p2);
		p1.session.write(new ProtobufMsg(PD.Enter_YBScene, msg));
	}

	public void checkVisibleSet(Player p1) {
		if(p1.visbileUids == null){
			p1.visbileUids = new HashSet<>();//打算在单线程里处理
		}
	}

	public void checkNeedHidden(Player p1, Player p2) {
		// 原先可见，现在不可见了，则需要给客户端发送离开场景。
		if(p1.visbileUids == null || p2.visbileUids == null){
			//以前不可见
			return;
		}
		if(p1.visbileUids.contains(p2.userId) == false){
			//以前不可见
			return;
		}
		hidden(p1,p2);
		hidden(p2,p1);
	}

	/**
	 * 给对方发送离开场景消息
	 * @param p2
	 * @param p1
	 */
	public void hidden(Player p2, Player p1) {
		p1.visbileUids.remove(p2.userId);
		ExitScene.Builder exitYBSc = ExitScene.newBuilder();
		exitYBSc.setUid(p2.userId);
		ProtobufMsg pm = new ProtobufMsg();
		pm.id=PD.Exit_YBScene;
		pm.builder = exitYBSc;
		p1.session.write(pm);
	}

	public boolean isBiaoChe(IoSession session) {
		Integer uid = (Integer) session.getAttribute(SessionAttKey.RobotType);
		if(uid!=null&&uid.intValue() == Scene.YBRobot_RoleId){
			return true;
		}
		return false;
	}
	
	
}
