package com.qx.world;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.map.LRUMap;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.Scene.ExitFightScene;
import qxmobile.protobuf.Scene.ExitScene;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.alliancefight.AllianceFightMatch;
import com.qx.alliancefight.AllianceFightMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.junzhu.PlayerTime;
import com.qx.persistent.HibernateUtil;
import com.qx.yabiao.YaBiaoHuoDongMgr;

public class SceneMgr extends EventProc{
	public static int sizePerSc = 20;
	public Logger logger = LoggerFactory.getLogger(SceneMgr.class);
	//TODO 联盟改变时修改缓存
	public Map<Long, Integer> jzId2lmId = Collections.synchronizedMap(new LRUMap(5000));
	public Map<Long, Long> jzId2houseId = Collections.synchronizedMap(new LRUMap(50));
	public ConcurrentHashMap<Integer, Scene> lmCities;
	public ConcurrentHashMap<Long, Scene> houseScenes;
	public ConcurrentHashMap<Integer, Scene> fightScenes;
	
	public SceneMgr(){
		lmCities = new ConcurrentHashMap<Integer, Scene>();
		houseScenes = new ConcurrentHashMap<Long, Scene>();
		fightScenes = new ConcurrentHashMap<Integer, Scene>();
	}
	
	public void route(int code, IoSession session, Builder builder){
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			return;
		}
		Integer lmId = jzId2lmId.get(junZhuId);
		if(lmId == null){//之前没存过
			AlliancePlayer ap = HibernateUtil.find(AlliancePlayer.class, junZhuId);
			if(ap == null || ap.lianMengId < 0) {	//没有联盟数据
				lmId = locateFakeLmId(junZhuId);	//分配一个场景
			}else{
				lmId = ap.lianMengId;
			}
			jzId2lmId.put(junZhuId,lmId);			//加入缓存
		}
		
		switch(code) {
			case PD.Enter_Scene:
				enterScene(code, session, builder, junZhuId, lmId);
				break;
			case PD.Enter_HouseScene:
				enterHouse(code, session, builder, junZhuId, lmId);
				break;
			case PD.Exit_HouseScene:
				exitHouse(code, session, builder, junZhuId);
				break;
			case PD.ENTER_FIGHT_SCENE:
				enterFight(code, session, builder);
				break;
			case PD.EXIT_FIGHT_SCENE:
				exitFight(code, session, builder, junZhuId);
				break;
			case PD.Enter_YBScene:
				enterYBScene(code, session, builder);
				break;
			case PD.Exit_YBScene:
				exitYBScene(code, session, builder, junZhuId);
				break;
				
			default:
				Scene sc = (Scene) session.getAttribute(SessionAttKey.Scene);
				if(sc == null){
					logger.info("未找到{}所在的场景",junZhuId);
				}else{
					sc.exec(code, session, builder);
				}
				break;
		}
	}
	
	public void exitYBScene(int code, IoSession session, Builder builder,
			Long junZhuId) {
		Scene ybSc = (Scene) session.getAttribute(SessionAttKey.Scene);
		if (ybSc != null) {
			ybSc.exec(code, session, builder);
		}else{
			logger.info("用户{}不在押镖场景中，退出押镖场景失败",junZhuId);
		}
	}

	public void enterYBScene(int code, IoSession session, Builder builder) {
		//离开原来的场景
		playerExitScene(session);
		// 进入押镖场景进行押镖
		int scId = YaBiaoHuoDongMgr.inst.locateFakeSceneId();
		Scene sc = YaBiaoHuoDongMgr.inst.yabiaoScenes.get(scId);
		if (sc == null) {// 没有场景
			synchronized (YaBiaoHuoDongMgr.inst.yabiaoScenes) {// 防止多次创建
				sc = YaBiaoHuoDongMgr.inst.yabiaoScenes.get(scId);
				if (sc == null) {
					sc = new Scene("YB#" + scId);
					sc.startMissionThread();
					YaBiaoHuoDongMgr.inst.yabiaoScenes.put(scId, sc);
				}
			}
		}
		sc.exec(code, session, builder);
	}

	private void exitFight(int code, IoSession session, Builder builder,
			Long junZhuId) {
		Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
		if (scene != null) {
			scene.exec(code, session, builder);
		}else{
			logger.info("退出联盟战失败，君主:{}场景为null", junZhuId);
		}
	}
	
	protected void enterFight(int code, IoSession session, Builder builder) {
		//离开原来的场景
		playerExitScene(session);
		
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		AllianceBean alliance = AllianceMgr.inst.getAllianceByJunZid(junZhu.id);
		if(alliance == null) {
			return;
		}
		AllianceFightMatch fightMatch = HibernateUtil.find(AllianceFightMatch.class, 
				" where allianceId1="+alliance.id + " or allianceId2="+alliance.id);
		if(fightMatch == null) {
			return;
		}
		
		Scene fightScene = fightScenes.get(fightMatch.id);
		if(fightScene == null) {
			synchronized (fightScenes) {
				fightScene = fightScenes.get(fightMatch.id);
				if(fightScene == null){
					long fightEndTime = AllianceFightMgr.getFightEndTime();
					fightScene = new FightScene("Fight#"+fightMatch.id, fightEndTime, fightMatch.id);
					BigSwitch.inst.cdTimeMgr.addFightScene((FightScene) fightScene);
					fightScene.startMissionThread();
					fightScenes.put(fightMatch.id, fightScene);
				}
			}
		}
		fightScene.exec(code, session, builder);
	}
	
	protected void enterScene(int code, IoSession session, Builder builder,
			Long junZhuId, Integer lmId) {
		PlayerTime playerTime = HibernateUtil.find(PlayerTime.class, junZhuId);
		if (playerTime != null) {
			if(playerTime.getZhunchengTime() == null){
				playerTime.setZhunchengTime(new Date());
				HibernateUtil.save(playerTime);
				logger.info(" 记录玩家{}进入主城时间",junZhuId);
			}
		}
		
		//离开原来的场景
		playerExitScene(session);
		
		//进入联盟或者主城场景
		Scene sc = lmCities.get(lmId);
		if(sc == null){//该联盟没有场景
			synchronized (lmCities) {//防止多次创建
				sc = lmCities.get(lmId);
				if(sc == null){
					sc = new Scene("LM#"+lmId);
					sc.startMissionThread();
					lmCities.put(lmId, sc);
				}
			}
		}
		sc.exec(code, session, builder);
	}
	
	protected void exitHouse(int code, IoSession session, Builder builder,
			Long junZhuId) {
		Scene houseScene = (Scene) session.getAttribute(SessionAttKey.Scene);
		if (houseScene != null) {
			houseScene.exec(code, session, builder);
		}else{
			logger.info("用户{}不在房屋场景中，退出小屋场景失败",junZhuId);
		}
	}
	
	protected void enterHouse(int code, IoSession session, Builder builder,
			Long junZhuId, Integer lmId) {
		if(lmId <= 0){
			logger.info("用户:{}无联盟，无法进入小屋", junZhuId);
			return;
		}
		//离开原来的场景
		playerExitScene(session);

		Long houseJzId=BigSwitch.getInst().houseMgr.inWhichHouse.get(junZhuId);
		Long nowHouseSceneId = jzId2houseId.get(houseJzId);
		if(nowHouseSceneId == null){//之前没存过
			//加入缓存
			nowHouseSceneId=locateFakeHouseId(junZhuId);
			jzId2houseId.put(houseJzId,nowHouseSceneId); 
		}

		Scene houseScene= houseScenes.get(nowHouseSceneId);
		if(houseScene == null){//该房屋没有场景
			synchronized (houseScenes) {//防止多次创建
				houseScene = houseScenes.get(nowHouseSceneId);
				if(houseScene == null){
					houseScene = new Scene("FW#"+nowHouseSceneId);
					houseScene.startMissionThread();
					//robot
					houseScenes.put(nowHouseSceneId, houseScene);
				}
			}
		}
		houseScene.exec(code, session, builder);
	}

	public Integer locateFakeLmId(Long junZhuId) {
		int scId = 0;//默认0号场景
		do{
			Scene sc = lmCities.get(scId);
			if(sc == null){
				break;//已有的场景都满了
			}else if(sc.players.size()<sizePerSc){
				//这个场景还没满
				break;
			}
			scId --;//无联盟的城池id用负数递减。
		}while(true);
		return scId;
	}
	
	public Long locateFakeHouseId(Long houseJzId) {
		long scId = -houseJzId;//默认0号场景
		Scene sc = houseScenes.get(scId);
		if(sc == null){
			logger.info("未创建{}的房屋场景",houseJzId); 
		}else if(sc.players.size()<sizePerSc){
			logger.info("已创建{}的房屋场景",houseJzId); 
		}
		return scId;
	}
	
	public void shutdown() {
		Iterator<Scene> it = lmCities.values().iterator();
		while(it.hasNext()){
			Scene scene = it.next();
			scene.shutdown();		
		}
		
		it = houseScenes.values().iterator();
		while(it.hasNext()){
			Scene scene = it.next();
			scene.shutdown();		
		}
		
		it = fightScenes.values().iterator();
		while(it.hasNext()){
			Scene scene = it.next();
			scene.shutdown();		
		}
	}
	
	@Override
	public void proc(Event param) {
		switch(param.id){
			case ED.Join_LM:{
				Object[] oa = (Object[]) param.param;
				Long jzId = (Long) oa[0];
				Integer lmId = (Integer) oa[1];
				Integer pre = jzId2lmId.put(jzId, lmId);
				removeFromPreSc(jzId, pre);
			}
			break;
			case ED.Leave_LM:{
				Object[] oa = (Object[]) param.param;
				Long jzId = (Long) oa[0];
				Integer preLmId  = (Integer) oa[1];
				Integer	nowSceneId = locateFakeLmId(jzId);
				jzId2lmId.put(jzId, nowSceneId);
				removeFromPreSc(jzId, preLmId);
			}
			break;
		}
	}
	
	public void removeFromPreSc(Long jzId, Integer pre) {
		if(pre == null){
			return;
		}
		Scene sc = lmCities.get(pre);
		if(sc == null){
			return;
		}
		sc.exitForTrasn(jzId);
	}
	
	public void playerExitScene(IoSession session) {
		Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
		if (scene != null) {
			Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
			int uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
			
			if(scene.name.contains("Fight")) {
				ExitFightScene.Builder exitFight = ExitFightScene.newBuilder();
				exitFight.setUid(uid);
				logger.info("君主:{},Uid:{})从战斗场景:{}中退出", junZhuId, uid, scene.name);
				scene.exec(PD.EXIT_FIGHT_SCENE, session, exitFight);
				return;
			}
			
			ExitScene.Builder exit = ExitScene.newBuilder();
			exit.setUid(uid);
			if(scene.name.contains("FW")){
				logger.info("君主:{},Uid:{})从房屋:{}退出", junZhuId, uid, scene.name);
				scene.exec(PD.Exit_HouseScene, session, exit);
			} else if(scene.name.contains("YB")){
				logger.info("君主:{},Uid:{})从押镖场景:{}退出", junZhuId, uid, scene.name);
				YaBiaoHuoDongMgr.inst.removeJbJz2Map(junZhuId);
				scene.exec(PD.Exit_YBScene, session, exit);
			} else{
				logger.info("君主:{},Uid:{}从场景:{}退出", junZhuId, uid, scene.name);
			}	
			scene.exec(PD.Exit_Scene, session, exit);
		}
	}
	
	@Override
	protected void doReg() {
		EventMgr.regist(ED.Join_LM, this);
		EventMgr.regist(ED.Leave_LM, this);
	}
}
