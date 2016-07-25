package com.qx.world;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.map.LRUMap;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.Scene.ExitFightScene;
import qxmobile.protobuf.Scene.ExitScene;
import qxmobile.protobuf.ErrorMessageProtos.DataList;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.JCZCity;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.alliancefight.AllianceFightMatch;
import com.qx.alliancefight.AllianceFightMgr;
import com.qx.alliancefight.CityBean;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.explore.treasure.BaoXiangScene;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.junzhu.PlayerTime;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;
import com.qx.yabiao.YaBiaoHuoDongMgr;
import com.qx.yabiao.YaBiaoRobot;

public class SceneMgr extends EventProc{
	public static int sizePerSc = 20;
	public Logger logger = LoggerFactory.getLogger(SceneMgr.class);
	//TODO 联盟改变时修改缓存
	public Map<Long, Integer> jzId2lmId = Collections.synchronizedMap(new LRUMap(5000));
	public Map<Long, Long> jzId2houseId = Collections.synchronizedMap(new LRUMap(50));
	public ConcurrentHashMap<Integer, Scene> lmCities;
	public ConcurrentHashMap<Long, Scene> houseScenes;
	public ConcurrentHashMap<Integer, FightScene> fightScenes;
	
	public SceneMgr(){
		lmCities = new ConcurrentHashMap<Integer, Scene>();
		houseScenes = new ConcurrentHashMap<Long, Scene>();
		fightScenes = new ConcurrentHashMap<Integer, FightScene>();
	}
	
	public void route(int code, IoSession session, Builder builder){
		Scene sc1 = (Scene) session.getAttribute(SessionAttKey.Scene);
		if(sc1 != null && sc1 instanceof BaoXiangScene && code != PD.Enter_Scene){
			//在十连副本里，则交个十连副本处理。但是退出副本返回主城，不交给副本处理。
			sc1.exec(code, session, builder);
			return;
		}

		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			return;
		}
		
		Integer lmId = (Integer) session.getAttribute(SessionAttKey.Chosed_Scene);//session中尝试获取玩家指定场景ID
		if(lmId == null ){
			//玩家指定场景ID为空，调用自动分配ID方法，分配ID之后写入session（需求不明，暂时注销）；
			lmId = locateFakeLmId(junZhuId);
//			session.setAttribute(SessionAttKey.Chosed_Scene, lmId);
		}
		
//		Integer lmId = jzId2lmId.get(junZhuId);
//		if(lmId == null){//之前没存过
//			AlliancePlayer ap = HibernateUtil.find(AlliancePlayer.class, junZhuId);
//			if(ap == null || ap.lianMengId < 0) {	//没有联盟数据
//				lmId = locateFakeLmId(junZhuId);	//分配一个场景
//			}else{
//				lmId = ap.lianMengId;
//			}
//			jzId2lmId.put(junZhuId,lmId);			//加入缓存
//		}
		
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
//			case PD.Enter_TBBXScene:
//				enterTBBXScene(code, session, builder);
//				break;
			case PD.Exit_TBBXScene:
				exitTBBXScene(code, session, builder, junZhuId);
				break;
				
			default:
				Scene sc = (Scene) session.getAttribute(SessionAttKey.Scene);
				if(sc == null){
					logger.info("未找到{}所在的场景, pd:{}",junZhuId,code);
				}else{
					sc.exec(code, session, builder);
				}
				break;
		}
	}
	
	public void exitTBBXScene(int code, IoSession session, Builder builder, Long junZhuId) {
		
	}

//	public void enterTBBXScene(int code, IoSession session, Builder builder) {
//		//离开原来的场景
//		playerExitScene(session);
//		// 进入押镖场景进行押镖
//		int scId = 1;
//		synchronized (this) {
//			Scene sc = new Scene("TBBX#" + scId);
//			sc.startMissionThread();
//			YaBiaoHuoDongMgr.inst.yabiaoScenes.put(scId, sc);
//			sc.exec(code, session, builder);
//		}
//		session.setAttribute(SessionAttKey.SceneID, scId);
//	}

	public void exitYBScene(int code, IoSession session, Builder builder,
			Long junZhuId) {
		Scene ybSc = (Scene) session.getAttribute(SessionAttKey.Scene);
		if(ybSc != null && ybSc instanceof YaBiaoScene){
			ExitScene.Builder exitYBSc = ExitScene.newBuilder();
			Integer uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
			if(uid == null){
				logger.error("离开押镖场景处理出错，未找到君主---{}的uid",junZhuId);
				return;
			}
			exitYBSc.setUid(uid);
			ybSc.exec(code, session, exitYBSc);
		}else{
			logger.info("用户{}不在押镖场景中，退出押镖场景失败,Scene --{}",junZhuId,ybSc);
		}
	}

	public void enterYBScene(int code, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			logger.error("请求进入押镖场景出错：君主不存在");
			return;
		}
		//离开原来的场景
		playerExitScene(session);
		//判断是否已经在运镖
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(jz.id);
		if (ybr != null) {
			logger.info("请求进入押镖场景,已经有马车在运镖，进入马车场景");
			YaBiaoScene  sc=ybr.sc;
			sc.exec(code, session, builder);
			return;
		}
		// 进入押镖场景进行押镖
		int scId = YaBiaoHuoDongMgr.inst.locateFakeSceneId();
		synchronized (YaBiaoHuoDongMgr.inst.yabiaoScenes) {
			YaBiaoScene  sc = YaBiaoHuoDongMgr.inst.yabiaoScenes.get(scId);
			if (sc == null) {// 没有场景
				synchronized (YaBiaoHuoDongMgr.inst.yabiaoScenes) {// 防止多次创建
					sc = YaBiaoHuoDongMgr.inst.yabiaoScenes.get(scId);
					if (sc == null) {
						sc = new YaBiaoScene("YB#" + scId);
						sc.startMissionThread();
						YaBiaoHuoDongMgr.inst.yabiaoScenes.put(scId, sc);
					}
				}
			}
			sc.exec(code, session, builder);
		}
	}

	public void exitFight(int code, IoSession session, Builder builder,
			Long junZhuId) {
		Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
		if (scene != null) {
			scene.exec(code, session, builder);
		}else{
			logger.info("退出联盟战失败，君主:{}场景为null", junZhuId);
		}
	}
	
	public synchronized void enterFight(int code, IoSession session, Builder builder) {
		//离开原来的场景
		playerExitScene(session);
		
		/*  临时关闭 2016年4月22日15:11:04
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
		int scId = fightMatch.id;
		 */
		ErrorMessage.Builder req = (ErrorMessage.Builder)builder;
		int cityId = req.getErrorCode();
		List<JCZCity> cityList = TempletService.listAll(JCZCity.class.getSimpleName());
		Optional<JCZCity> op = cityList.stream().filter(c->c.id==cityId).findAny();
		if(op.isPresent()==false){
			logger.error("没有找到城池配置 {}",cityId);
			return;
		}
		FightScene fightScene = null;
		JCZCity city = op.get();
		int scId;
		Long jzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (jzId == null) {
			return;
		}
		AlliancePlayer player = AllianceMgr.inst.getAlliancePlayer(jzId);
		if(player == null || player.lianMengId<=0){
			return;
		}
		int redLmId=-100;//守方
		int blueLmId=0;
		if(city.type == 2){//野城
			blueLmId = player.lianMengId;
			scId = -Scene.atomicInteger.incrementAndGet();
			for(FightScene fs : fightScenes.values()){
				if(fs.cityId == cityId && fs.blueLmId == player.lianMengId){
					fightScene = fs;
					break;
				}
			}
		}else{
			CityBean cityBean = HibernateUtil.find(CityBean.class,cityId);
			if(cityBean == null){
				logger.warn("no city bean for {}", cityId);
				return;
			}
			if(cityBean.lmId>0){
				redLmId = cityBean.lmId;
			}
			blueLmId = cityBean.atckLmId;
			scId = cityId;
			fightScene = fightScenes.get(cityId);		
		}
//		fightScenes.clear();
//		scId = 510102;
		if(fightScene == null) {
					long fightEndTime = AllianceFightMgr.getFightEndTime();
					if(redLmId==-100){
						redLmId = FightScene.TEAM_RED;
						fightScene = new FightNPCScene("Fight#"+scId, fightEndTime, scId);
					}else{
						fightScene = new FightScene("Fight#"+scId, fightEndTime, scId);
					}
					fightScene.redLmId = redLmId;
					fightScene.blueLmId = blueLmId;
					BigSwitch.inst.cdTimeMgr.addFightScene((FightScene) fightScene);
					fightScene.startMissionThread();
					fightScene.cityId = cityId;
					fightScene.bornAllNPC();
					fightScenes.put(scId, fightScene);
		}else if(fightScene.step == 500){//已结束
			return;
		}else{
			fightScene.blueLmId = blueLmId;
		}
		fightScene.exec(code, session, builder);
	}
	
	public void enterScene(int code, IoSession session, Builder builder,
			Long junZhuId, Integer lmId) {
		PlayerTime playerTime = HibernateUtil.find(PlayerTime.class, junZhuId);
		if (playerTime != null) {
			if(playerTime.zhunchengTime == null){
				playerTime.zhunchengTime = new Date();
				Cache.playerTimeCache.put(junZhuId, playerTime);
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
	
	public void exitHouse(int code, IoSession session, Builder builder,
			Long junZhuId) {
		Scene houseScene = (Scene) session.getAttribute(SessionAttKey.Scene);
		if (houseScene != null) {
			houseScene.exec(code, session, builder);
		}else{
			logger.info("用户{}不在房屋场景中，退出小屋场景失败",junZhuId);
		}
	}
	
	public void enterHouse(int code, IoSession session, Builder builder,
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
		Iterator<? extends Scene> it = lmCities.values().iterator();
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
//				Object[] oa = (Object[]) param.param;
//				Long jzId = (Long) oa[0];
//				Integer lmId = (Integer) oa[1];
//				Integer pre = jzId2lmId.put(jzId, lmId);
//				removeFromPreSc(jzId, pre);
			}
			break;
			case ED.Leave_LM:{
//				Object[] oa = (Object[]) param.param;
//				Long jzId = (Long) oa[0];
//				Integer preLmId  = (Integer) oa[1];
//				Integer	nowSceneId = locateFakeLmId(jzId);
//				jzId2lmId.put(jzId, nowSceneId);
//				removeFromPreSc(jzId, preLmId);
			}
			break;
		}
	}
	
//	public void removeFromPreSc(Long jzId, Integer pre) {
//		if(pre == null){
//			return;
//		}
//		Scene sc = lmCities.get(pre);
//		if(sc == null){
//			return;
//		}
//		sc.exitForTrasn(jzId);
//	}
	
	public void playerExitScene(IoSession session) {
		Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
		if (scene != null) {
			Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
			Integer uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
			if(uid == null)return;
			
			if(scene.name.contains("Fight")) {
//				ExitFightScene.Builder exitFight = ExitFightScene.newBuilder();
//				exitFight.setUid(uid);
//				logger.info("君主:{},Uid:{})从战斗场景:{}中退出", junZhuId, uid, scene.name);
//				scene.exec(PD.EXIT_FIGHT_SCENE, session, exitFight);
//				return;
			}
			
			ExitScene.Builder exit = ExitScene.newBuilder();
			exit.setUid(uid);
			if(scene.name.contains("FW")){
				logger.info("君主:{},Uid:{})从房屋:{}退出", junZhuId, uid, scene.name);
				scene.exec(PD.Exit_HouseScene, session, exit);
			} else if(scene.name.contains("YB")){
				logger.info("君主:{},Uid:{})从押镖场景:{}退出", junZhuId, uid, scene.name);
				scene.exec(PD.Exit_YBScene, session, exit);
			} else if(scene.name.contains("TBBX")){
				logger.info("君主:{},Uid:{})从十连探宝宝箱场景:{}退出", junZhuId, uid, scene.name);
				scene.exec(PD.Exit_TBBXScene, session, exit);
			} else{
				logger.info("君主:{},Uid:{}从场景:{}退出", junZhuId, uid, scene.name);
				scene.exec(PD.Exit_Scene, session, exit);
			}	
		}
	}
	
	@Override
	public void doReg() {
		EventMgr.regist(ED.Join_LM, this);
		EventMgr.regist(ED.Leave_LM, this);
	}
	
	/**玩家指定进入主城副本ID方法，调用之后获取玩家指定id存入session中*/
	public void choseScene(int id, IoSession session, Builder builder){
		ErrorMessage.Builder get = (ErrorMessage.Builder)builder ;//收到的协议强制转换为方法对应协议，方法协议使用ErrorMessage的协议格式
		ErrorMessage.Builder ret = ErrorMessage.newBuilder() ;// 创建返回协议
		//因为Errormessage协议格式多处复用，所以不能与协议号双向绑定，不同功能之间区分，所以使用msg传递协议号
		ProtobufMsg msg = new ProtobufMsg();
		Integer lmid = (Integer)get.getErrorCode();//获取协议中的场景ID
		if(lmid != null ){
			if(lmid > 0){
				//场景ID大于0，非法，返回错误消息，结束方法				
				ret.setErrorCode(1);
				ret.setErrorDesc("指定进入场景失败:场景ID大于0");			
				msg.builder = ret ;
				msg.id =PD.S_CHOOSE_SCENE ;
				session.write(msg);
				return;
			}
			//判断指定场景人满，如果满，返回错误消息，结束
			if(lmCities.get(lmid).players.size() >= sizePerSc){
				ret.setErrorCode(1);
				ret.setErrorDesc("指定进入场景失败:场景人数已满");			
				msg.builder = ret ;
				msg.id =PD.S_CHOOSE_SCENE ;
				session.write(msg);
				return;
			}
			//验证全部通过，写入session “setlmid”—>指定场景ID，键值对
			session.setAttribute(SessionAttKey.Chosed_Scene, lmid);
			//返回消息，顺便打印日志
			ret.setErrorCode(lmid) ;
			msg.builder = ret ;
			msg.id =PD.S_CHOOSE_SCENE ;
			session.write(msg);
			logger.info("玩家:{}选择跳转指定场景副本id:{}",session.getAttribute(SessionAttKey.junZhuId),lmid);
		}else {
			//协议中无法获取场景ID，可能客户端篡改协议，日志记录，结束方法
			logger.error("选择登录场景：收到错误的协议内容,id:{}，无法获取指定场景ID",id);
			return;
		}
	}
	
	/**获取服务器所有已开启场景id及人数返回客户端*/
	public void getAllScene(int id, IoSession session, Builder builder){
		Set<Integer> set = null;
		if(!lmCities.isEmpty()){
			set =  lmCities.keySet(); //获取场景map的所有key值的set
		}
		if(set != null && set.size() > 0){
			DataList.Builder ret = DataList.newBuilder();//创建返回消息
			ErrorMessage.Builder value = ErrorMessage.newBuilder();//创建返回消息
			for(Integer i :set){//遍历场景id，获取ID和人数写入返回消息中
				value.setErrorCode(i);
				value.setCmd(lmCities.get(i).players.size());
				ret.addData(value);
			}
			ProtobufMsg msg = new ProtobufMsg();//完成返回消息，发送
			msg.builder =ret ;
			msg.id = PD.S_SCENE_GETALL;
			session.write(msg);
		}
	}
}
