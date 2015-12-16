package com.qx.yabiao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import log.ActLog;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.Prompt.PromptActionResp;
import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Scene.SpriteMove;
import qxmobile.protobuf.Yabiao.AnswerYaBiaoHelpReq;
import qxmobile.protobuf.Yabiao.AnswerYaBiaoHelpResp;
import qxmobile.protobuf.Yabiao.AskYaBiaoHelpResp;
import qxmobile.protobuf.Yabiao.BiaoCheState;
import qxmobile.protobuf.Yabiao.BuyCountsReq;
import qxmobile.protobuf.Yabiao.BuyCountsResp;
import qxmobile.protobuf.Yabiao.EnemiesInfo;
import qxmobile.protobuf.Yabiao.EnemiesResp;
import qxmobile.protobuf.Yabiao.HorseProp;
import qxmobile.protobuf.Yabiao.HorsePropReq;
import qxmobile.protobuf.Yabiao.HorsePropResp;
import qxmobile.protobuf.Yabiao.HorseType;
import qxmobile.protobuf.Yabiao.JiaSuReq;
import qxmobile.protobuf.Yabiao.JiaSuResp;
import qxmobile.protobuf.Yabiao.Move2BiaoCheReq;
import qxmobile.protobuf.Yabiao.Move2BiaoCheResp;
import qxmobile.protobuf.Yabiao.RoomInfo;
import qxmobile.protobuf.Yabiao.SetHorseResult;
import qxmobile.protobuf.Yabiao.TiChuXieZhuResp;
import qxmobile.protobuf.Yabiao.TiChuYBHelpRsq;
import qxmobile.protobuf.Yabiao.XieZhuJunZhu;
import qxmobile.protobuf.Yabiao.XieZhuJunZhuResp;
import qxmobile.protobuf.Yabiao.YBHistory;
import qxmobile.protobuf.Yabiao.YBHistoryResp;
import qxmobile.protobuf.Yabiao.YaBiaoHelpResp;
import qxmobile.protobuf.Yabiao.YabiaoJunZhuInfo;
import qxmobile.protobuf.Yabiao.YabiaoJunZhuList;
import qxmobile.protobuf.Yabiao.YabiaoMainInfoResp;
import qxmobile.protobuf.Yabiao.YabiaoMenuResp;
import qxmobile.protobuf.Yabiao.YabiaoResult;
import qxmobile.protobuf.Yabiao.isNew4RecordResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.CartNPCTemp;
import com.manu.dynasty.template.CartTemp;
import com.manu.dynasty.template.DescId;
import com.manu.dynasty.template.JunzhuShengji;
import com.manu.dynasty.template.MaJu;
import com.manu.dynasty.template.Mail;
import com.manu.dynasty.template.Purchase;
import com.manu.dynasty.template.YunBiaoSafe;
import com.manu.dynasty.template.YunbiaoTemp;
import com.manu.dynasty.util.DateUtils;
import com.manu.dynasty.util.MathUtils;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.qx.account.FunctionOpenMgr;
import com.qx.activity.ActivityMgr;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
import com.qx.email.EmailMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.guojia.GuoJiaBean;
import com.qx.guojia.GuoJiaMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.prompt.PromptMSG;
import com.qx.prompt.PromptMsgMgr;
import com.qx.prompt.SuBaoConstant;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.ranking.RankingMgr;
import com.qx.robot.RobotSession;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.timeworker.FunctionID;
import com.qx.vip.VipData;
import com.qx.vip.VipMgr;
import com.qx.world.BroadcastMgr;
import com.qx.world.Mission;
import com.qx.world.Player;
import com.qx.world.Scene;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

public class YaBiaoHuoDongMgr extends EventProc implements Runnable {
	public static Logger log = LoggerFactory.getLogger(YaBiaoHuoDongMgr.class);
	public static YaBiaoHuoDongMgr inst;

	public ConcurrentHashMap<Long, Integer> ybJzId2ScIdMap;// 押镖君主和场景对应存储，方便君主找到Scid
	public ConcurrentHashMap<Integer, Set<Long>> ybJzList2ScIdMap; // 存储每个场景中押镖的君主列表
	//2015年12月12日1.1 版本 玩家不会从YaBiaoHuoDongMgr进入押镖场景 在这里 存储劫镖人已经无效
//	public ConcurrentHashMap<Integer, Set<Long>> jbJzList2ScIdMap; // 劫镖君主和场景对应存储，方便君主找到Scid
//	public ConcurrentHashMap<Long, Integer> jbJz2ScIdMap;// 存储每个场景中劫镖的君主列表
	public ConcurrentHashMap<Integer, Scene> yabiaoScenes;
	//2015年11月25日玩法变更废弃
//	public ConcurrentHashMap<Long, Map<Integer, Integer>> ybNpcMap;
	public static Map<Integer, CartTemp> cartMap ;
	public static Map<Integer, CartNPCTemp> biaoCheNpcMap;
	public static Map<Integer, YunBiaoSafe> safeAreaMap;
	public static Map<Integer, MaJu> majuMap;
	public static boolean openFlag = false;// 开启标记
	public static int tongbiCODE = 900001;
	public static int gongxianCODE = 900015;
	public static final Redis DB = Redis.getInstance();
	public static final String ENEMY_KEY = "enemy_" + GameServer.serverId;
	public static final String HISTORY_KEY = "history_" + GameServer.serverId;
	public static final String SOS_HISTORY_KEY = "sos_history_" + GameServer.serverId;
	public static int historySize = 50;
	public static int sosHistorySize = 50;
	public static int enemySize = 50;
	public static int[][] cartArray;
	public static int totalProbability = 0;
	public static ConcurrentHashMap<Long, HashSet<Long>> xieZhuCache4YBJZ;// 保存君主A的所有协助者
	public static ConcurrentHashSet<Long> xzJZSatrtYB;// 保存已开始协助运镖的君主
	public static ConcurrentHashMap<Long, List<Long>> answerHelpCache4YB;// 保存答复过协助请求的所有君主
	public static int XIEZHU_YABIAO_SIZE = 3;// 运镖协助人数上限
	public static int YABIAO_ASKHELP_TIMES = 3;// 运镖协助人数上限
	public static int ANSWER_YBHELP_COLD_TIME = 24 * 60 * 60 * 1000;// 同意时间
	public static String xiezhuContent;
	public static  ConcurrentHashMap<Integer, Integer> xtmcs4Scene=new ConcurrentHashMap<Integer, Integer>();
	//场景id为x的场景中要产生的马车数目y


	/** 收益倍率 默认100 表示100% **/
	public static  double SHOUYI_PROFIT = 100;
	/** 最大押镖人数 **/
	public static  int MAX_YB_NUM = 10000;
	public LinkedBlockingQueue<Mission> missions = new LinkedBlockingQueue<Mission>();
	private static Mission exit = new Mission(0, null, null);

	public YaBiaoHuoDongMgr() {
		inst = this;
		initData();
		// 开启线程
		new Thread(this, "YabiaoMgr").start();
	}

	@SuppressWarnings("unchecked")
	public void initData() {
		yabiaoScenes = new ConcurrentHashMap<Integer, Scene>();
		ybJzId2ScIdMap = new ConcurrentHashMap<Long, Integer>();
		ybJzList2ScIdMap = new ConcurrentHashMap<Integer, Set<Long>>();
//		jbJzList2ScIdMap = new ConcurrentHashMap<Integer, Set<Long>>();
//		jbJz2ScIdMap = new ConcurrentHashMap<Long, Integer>();
		xieZhuCache4YBJZ = new ConcurrentHashMap<Long, HashSet<Long>>();
		xzJZSatrtYB = new ConcurrentHashSet<Long>();
		answerHelpCache4YB = new ConcurrentHashMap<Long, List<Long>>();
		cartMap = new HashMap<Integer, CartTemp>();
		biaoCheNpcMap = new HashMap<Integer, CartNPCTemp>();
		safeAreaMap = new HashMap<Integer, YunBiaoSafe>();
		majuMap=new HashMap<Integer, MaJu>();
		
		XIEZHU_YABIAO_SIZE = CanShu.YUNBIAOASSISTANCE_MAXNUM;
		List<CartTemp> list = TempletService.listAll(CartTemp.class.getSimpleName());
		Map<Integer, CartTemp> cartMap = new HashMap<Integer, CartTemp>();
		for (CartTemp c : list) {
			cartMap.put(c.quality, c);
		}
		YaBiaoHuoDongMgr.cartMap=cartMap;
		List<CartNPCTemp> npcList = TempletService.listAll(CartNPCTemp.class.getSimpleName());
		Map<Integer, CartNPCTemp> biaoCheNpcMap = new HashMap<Integer, CartNPCTemp>();
		for (CartNPCTemp cNpc : npcList) {
			biaoCheNpcMap.put(cNpc.id, cNpc);
		}
		YaBiaoHuoDongMgr.biaoCheNpcMap=biaoCheNpcMap;
		List<YunBiaoSafe> safeAreaList = TempletService.listAll(YunBiaoSafe.class.getSimpleName());
		Map<Integer, YunBiaoSafe> safeAreaMap = new HashMap<Integer, YunBiaoSafe>();
		for (YunBiaoSafe sa : safeAreaList) {
			safeAreaMap.put(sa.areaID, sa);
		}
		YaBiaoHuoDongMgr.safeAreaMap=safeAreaMap;
		List<MaJu> majuList = TempletService.listAll(MaJu.class.getSimpleName());
		Map<Integer, MaJu> majuMap=new HashMap<Integer, MaJu>();
		for (MaJu mj : majuList) {
			majuMap.put(mj.id,mj);
		}
		YaBiaoHuoDongMgr.majuMap=majuMap;
		// 添加首冲奖励描述
		DescId desc = ActivityMgr.descMap.get(4001);
		xiezhuContent = desc.getDescription();
		initRandomCartData();
		fixOpenFlag();
	}

	public void fixOpenFlag() {
		try {
			Calendar calendar = Calendar.getInstance();
			Date date = calendar.getTime();

			String[] startTimeArr = CanShu.OPENTIME_YUNBIAO.split(":");
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTimeArr[0]));
			calendar.set(Calendar.MINUTE, Integer.parseInt(startTimeArr[1]));
			Date startTime = calendar.getTime();

			String[] endTimeArr = CanShu.CLOSETIME_YUNBIAO.split(":");
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTimeArr[0]));
			calendar.set(Calendar.MINUTE, Integer.parseInt(endTimeArr[1]));
			Date endTime = calendar.getTime();
			if((date.after(startTime) && date.before(endTime))||date.equals(startTime)) {
				openFlag = true;
			}else{
				openFlag = false;
			}
			log.info("运镖开启状态为{}",openFlag);
		} catch (Exception e) {
			log.error("运镖开始结束时间配置有误");
		}
	}
	
	/**
	 * @Description 设置押镖收益倍率
	 */
	public void setMoreProfitState(int canshu) {
		String template=YunbiaoTemp.yunbiao_start_broadcast;
		if(canshu>100){
			SHOUYI_PROFIT=canshu;
			BroadcastMgr.inst.send(template+"收益率为"+canshu+"%");
		}else{
			template=YunbiaoTemp.yunbiao_end_broadcast;
			SHOUYI_PROFIT=canshu;
			BroadcastMgr.inst.send(template);
		}
		log.info("押镖收益比率为{}",SHOUYI_PROFIT);
	}

	@Override
	public void run() {
		while (GameServer.shutdown == false) {
			Mission m = null;
			try {
				m = missions.take();
			} catch (InterruptedException e) {
				log.error("interrupt", e);
				continue;
			}
			if (m == exit) {
				break;
			}
			try {
				handle(m);
			} catch (Throwable e) {
				log.info("异常协议{}", m.code);
				log.error("处理出现异常", e);
			}
		}
		log.info("退出YabiaoMgr");
	}


	public void handle(Mission m) {
		int id = m.code;
		IoSession session = m.session;
		Builder builder = m.builer;
		if (openFlag) {
			switch (m.code) {
			case PD.C_YABIAO_INFO_REQ:
				getYabiaoMainInfo(id, builder, session);
				break;
			case PD.C_YABIAO_ENEMY_RSQ:
				getYabiaoEnemyInfo(id, builder, session);
				break;
			case PD.C_YABIAO_HISTORY_RSQ:
				getYabiaoHistoryInfo(id, builder, session);
				break;
			case PD.C_YABIAO_MENU_REQ:
				getYabiaoMenu(id, builder, session);
				break;
			case PD.C_SETHORSE_REQ:
				setHorseType(id, builder, session);
				break;
			case PD.C_YABIAO_REQ:
				startYabiaoReq(id, builder, session);
				break;
//			case PD.C_JIEBIAO_INFO_REQ:
//				log.info("请求劫镖主页逻辑已经废弃");
//				getJieBiaoInfo(id, builder, session);
//				break;
			case PD.C_BIAOCHE_INFO:
				getBiaoCheList(id, builder, session);
				break;
			case PD.C_YABIAO_BUY_RSQ:
				buyCounts4YaBiao(id, builder, session);
				break;
			case PD.C_YABIAO_HELP_RSQ:
				askHelp4YB(id, builder, session);
				break;
			case PD.C_ANSWER_YBHELP_RSQ:
				jionHelp2YB(id, builder, session);
				break;
			case PD.C_TICHU_YBHELP_RSQ:
				tichuHelper2YB(id, builder, session);
				break;
//			case PD.C_YABIAO_XIEZHU_TIMES_RSQ:
				//2015年12月12日 1.1版本协助次数完全没限制了
//				getXieZhuTimes(id, builder, session);
//				break;
//			case PD.C_MOVE2BIAOCHE_REQ:
//				move2BiaoChe4KB(id, builder, session);
//				break;
			case PD.C_CARTJIASU_REQ:
				jiasuBiaoChe(id, builder, session);
				break;
			case PD.C_BUYHORSEBUFF_REQ:
				buyHorseTool(id, builder, session);
				break;
			case PD.C_YABIAO_XIEZHUS_REQ:
				getXieZhuJZInfo(id, builder, session);
				break;
			default:
				log.error("YabiaoMgr-未处理的消息{}", id);
				break;
			}
		} else {// 活动未开启只处理以下消息
			switch (m.code) {
			case PD.C_YABIAO_INFO_REQ:
				getYabiaoMainInfo(id, builder, session);
				break;
			case PD.C_BIAOCHE_INFO:
				getBiaoCheList(id, builder, session);
				break;
			case PD.C_YABIAO_ENEMY_RSQ:
				getYabiaoEnemyInfo(id, builder, session);
				break;
			case PD.C_YABIAO_HISTORY_RSQ:
				getYabiaoHistoryInfo(id, builder, session);
				break;
			default:
				log.error("未处理的消息{}", id);
				break;
			}
		}
	}
	
	/**
	 * @Description 点联盟快报前往 请求前往镖车坐标，返回镖车坐标
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void move2BiaoChe4KB(long subaoId, JunZhu jz , IoSession session) {
		log.info("{}请求前往镖车",jz.id);
		PromptMSG msg = HibernateUtil.find(PromptMSG.class,subaoId);
		PromptActionResp.Builder resp=PromptActionResp.newBuilder();
		if(msg==null){
			log.error("{}请求前往镖车失败，未找到快报--{}信息",jz.id,subaoId);
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		long ybJzId=msg.otherJzId;
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJzId);
		if(ybr==null){
			log.error("{}请求前往镖车失败，未找到--{}的镖车信息",jz.id,ybJzId);
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		resp.setSubaoId(subaoId);
		resp.setResult(10);
		resp.setPosX(ybr.posX);
		resp.setPosZ(ybr.posZ);
		session.write(resp.build());
		log.info("{}请求前往{}镖车成功，镖车坐标x--{},z---{}",jz.id,ybJzId,ybr.posX,ybr.posZ);
		HibernateUtil.delete(msg);
	}
	/**
	 * @Description 请求前往镖车坐标，返回镖车坐标
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void move2BiaoChe(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求前往镖车失败，未找到请求君主");
			return;
		}
		log.info("{}请求前往镖车",jz.id);
		//修改req 和resp消息体
		Move2BiaoCheReq.Builder req=(Move2BiaoCheReq.Builder)builder;
		long ybJzId=req.getTargetId();
		Move2BiaoCheResp.Builder resp=Move2BiaoCheResp.newBuilder();
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJzId);
		if(ybr==null){
			log.error("{}请求前往镖车失败，未找到--{}的镖车信息",jz.id,ybJzId);
			resp.setTargetId(ybJzId);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		resp.setTargetId(ybJzId);
		resp.setResult(10);
		resp.setPosX(ybr.posX);
		resp.setPosZ(ybr.posZ);
		session.write(resp.build());
		log.info("{}请求前往{}镖车成功，镖车坐标x--{},z---{}",jz.id,ybJzId,ybr.posX,ybr.posZ);
	}
	//2015年12月12日 1.1版本协助次数完全没限制了
//	/**
//	 * @Description: //请求协助押镖次数
//	 * @param id
//	 * @param builder
//	 * @param session
//	 */
//	public void getXieZhuTimes(int id, Builder builder, IoSession session) {
//		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
//		if (jz == null) {
//			log.error("请求协助押镖次数：君主不存在");
//			return;
//		}else{
//			log.info("请求协助押镖次数：君主-{}",jz.id);
//		}
//		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jz.id);
//		XieZhuTimesResp.Builder resp = XieZhuTimesResp.newBuilder();
//		int remainXZ=0;
//		if (ybBean == null) {
//			//2015年8月6日 取消注释没开启押镖也能协助
//			ybBean = initYaBiaoBeanInfo(jz.id, jz.vipLevel);
////			resp.setRemainXZ(remainXZ);
////			resp.setUsedXZ(usedXZ);
//		} else {
//			ybBean = resetybBean(ybBean, jz.vipLevel);
//		}
//		remainXZ=ybBean.remainXZ;
//		usedXZ=ybBean.usedXZ;
//		resp.setRemainXZ(remainXZ);
//		resp.setUsedXZ(usedXZ);
//		session.write(resp.build());
//	}

	/**
	 * @Description: 初始化产生马车的数据
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public int[][] initRandomCartData() {
		Iterator<?> cartit = cartMap.entrySet().iterator();
		int size = cartMap.size();
		cartArray = new int[size][2];
		int i = 0;
		while (cartit.hasNext()) {
			Map.Entry obj = (Map.Entry) cartit.next();
			Integer key = (Integer) obj.getKey();
			CartTemp cart = (CartTemp) obj.getValue();
			cartArray[i][0] = key;
			cartArray[i][1] = cart.CartProbability;
			totalProbability += cart.CartProbability;
			i++;
		}
		return cartArray;
	}

	/**
	 * @Description: 产生马车
	 * @return
	 */
	public int getRandomCart() {
		int result = MathUtils.getRandom(cartArray, totalProbability);
		return result;
	}
	/**
	 * @Description: 产生系统马车配置Id
	 * @return CartNPCTemp
	 */
	@SuppressWarnings("rawtypes")
	public CartNPCTemp getRandomCartNPC() {
		int hType = getRandomCart();// 随机一匹马 1 2 3 4 5 （0表示没有马）
		if (hType > 5) {
			hType = 5;
		}
		int fuwuqilevel=20;
		fuwuqilevel=(int) RankingMgr.inst.getTopJunzhuAvgLevel(50);
		int suijixiaxian=fuwuqilevel-10;
		suijixiaxian=suijixiaxian>20?suijixiaxian:20;
		int suijishangxian=fuwuqilevel>20?fuwuqilevel:20;
		int bcLevel = MathUtils.getRandomInMax(suijixiaxian, suijishangxian);
		CartNPCTemp cartNpc=null;
		Iterator iter = biaoCheNpcMap.entrySet().iterator();
		while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				CartNPCTemp cn= (CartNPCTemp) entry.getValue();
				if(cn!=null&&cn.level==bcLevel&&cn.quality==hType){
					cartNpc=cn;
					break;
				}
		}
		if(cartNpc==null){
			log.error("产生系统马车配置失败,返回第一条配置");
			return	biaoCheNpcMap.get(902001);
		}
		return cartNpc;
		
	}

	/**
	 * @Description: 推送镖车信息
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getBiaoCheList(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("推送镖车信息出错：君主不存在");
			return;
		}
		RoomInfo.Builder req = (RoomInfo.Builder) builder;
		if (req == null) {
			log.error("推送镖车信息出错：请求不存在");
			return;
		}
		long jzId = jz.id;
		int roomId = 0;//req.getRoomId(); FIXME 2015年11月30日 策划改成服务器只有一个押镖场景
		YabiaoJunZhuList.Builder resp = YabiaoJunZhuList.newBuilder();
		Set<Long> ybSet = ybJzList2ScIdMap.get(roomId);
		for (Long junzhuId : ybSet) {
			YabiaoJunZhuInfo.Builder biaoChe = YabiaoJunZhuInfo.newBuilder();
			if(junzhuId<0){
				YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(junzhuId);
				if(ybr==null){
					log.error("推送镖车信息，运镖君主--{}的镖车机器人不存在",junzhuId);
					continue;
				}
				biaoChe.setJunZhuId(junzhuId);
				biaoChe.setJunZhuName(ybr.name);
				biaoChe.setLevel(ybr.jzLevel);
				biaoChe.setLianMengName("");
				int protectTime = ybr.protectTime - ((int) (System.currentTimeMillis() - ybr.endBattleTime) / 1000);
				biaoChe.setBaohuCD(protectTime > 0 ? protectTime : 0);
				biaoChe.setTotalTime(ybr.totalTime);
				biaoChe.setUsedTime(ybr.usedTime);
				biaoChe.setHp(ybr.hp);
				biaoChe.setMaxHp(ybr.maxHp);
				biaoChe.setWorth(ybr.worth);
				biaoChe.setMaxWorth(ybr.worth);
				biaoChe.setState(ybr.isBattle ? 20 : (protectTime > 0 ? 30 : 10));
				// 10押送中 20 战斗中 30 保护CD
				biaoChe.setZhanLi(ybr.zhanli);
				biaoChe.setPathId(ybr.pathId);
				biaoChe.setHorseType(ybr.horseType);
				boolean IsEnemy = isEmeny(jzId, junzhuId);
				biaoChe.setIsEnemy(IsEnemy);
				biaoChe.setJunzhuGuojia(ybr.guojiaId);
				//2015年8月31日返回盟友增加的护盾
				biaoChe.setHuDun(0);
			}else {
				YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(junzhuId);
				if(ybr==null){
					log.error("推送镖车信息，运镖君主--{}的镖车机器人不存在",junzhuId);
					continue;
				}
				JunZhu ybJunZhu = HibernateUtil.find(JunZhu.class, junzhuId);
				biaoChe.setJunZhuId(ybJunZhu.id);
				biaoChe.setJunZhuName(ybJunZhu.name);
				biaoChe.setLevel(ybJunZhu.level);
				AllianceBean ybabean = AllianceMgr.inst.getAllianceByJunZid(ybJunZhu.id);
				biaoChe.setLianMengName(ybabean == null ? "" : ybabean.name);
				int protectTime = ybr.protectTime - ((int) (System.currentTimeMillis() - ybr.endBattleTime) / 1000);
				biaoChe.setBaohuCD(protectTime > 0 ? protectTime : 0);
				biaoChe.setTotalTime(ybr.totalTime);
				biaoChe.setUsedTime(ybr.usedTime);
//				YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, ybr.jzId);
				biaoChe.setHp(ybr.hp);
				biaoChe.setMaxHp(ybJunZhu.shengMingMax);
				biaoChe.setWorth(ybr.worth);
				biaoChe.setMaxWorth(ybr.worth);
				biaoChe.setState(ybr.isBattle ? 20 : (protectTime > 0 ? 30 : 10));
				// 10押送中 20 战斗中 30 保护CD
				int zhanli = JunZhuMgr.inst.getJunZhuZhanliFinally(ybJunZhu);
				biaoChe.setZhanLi(zhanli);
				biaoChe.setPathId(ybr.pathId);
				biaoChe.setHorseType(ybr.horseType);
				boolean IsEnemy =isEmeny(jzId, junzhuId);
				biaoChe.setIsEnemy(IsEnemy);
				biaoChe.setJunzhuGuojia(ybJunZhu.guoJiaId);
				//2015年8月31日返回盟友增加的护盾
				biaoChe.setHuDun(ybr.hudun*100/ybJunZhu.shengMingMax);
			}
			resp.addYabiaoJunZhuList(biaoChe.build());
		}
		// 计算劫镖冷却时间
		YBBattleBean zdBean = HibernateUtil.find(YBBattleBean.class, jz.id);
		int lengqueCD = 0;
		int remainJB = 0;
		int gongjiZuheId = -1;
		int buyCounts = 0;
		if (zdBean != null) {
			if (zdBean.lastJBDate != null) {
				int distanceTime = DateUtils.timeDistanceBySeconds(zdBean.lastJBDate);
				if (distanceTime > 0) {
					lengqueCD = 
					(CanShu.JIEBIAO_CD - distanceTime > 0) ? (CanShu.JIEBIAO_CD - distanceTime): 0;
				}
			}
			remainJB = zdBean.remainJB4Award;
			gongjiZuheId = zdBean.gongJiZuHeId;
//			buyCounts = zdBean.todayBuyJBTimes;
		}
		resp.setGongjiZuHeId(gongjiZuheId);
		resp.setLengqueCD(lengqueCD);
		resp.setJieBiaoCiShu(remainJB);
		resp.setBuyCiShu(buyCounts);
		session.write(resp.build());
	}
	
	/**
	 * @Description 扣除满血复活次数
	 * @param jz
	 * @return
	 */
	public boolean kouchuFuhuoTimes(JunZhu jz) {
		long jzId=jz.id;
		int vipLevel=jz.vipLevel;
		YBBattleBean zdbean = HibernateUtil.find(YBBattleBean.class, jzId);
		if (zdbean == null) {
			zdbean=initYBBattleInfo(jzId, vipLevel);
		} else {
			zdbean = resetYBBattleInfo(zdbean, vipLevel);
		}
		if(YunbiaoTemp.resurgenceTimes+zdbean.fuhuoTimes4Vip<=zdbean.fuhuo4uesd){
			log.error("扣除君主{}满血复活次数失败,已用次数{},免费满血次数为{}，购买的满血次数为{}，满血次数用完了",jzId,zdbean.fuhuo4uesd,
					YunbiaoTemp.resurgenceTimes,zdbean.fuhuoTimes4Vip);
			return false;
		}
		zdbean.fuhuo4uesd+=1;
		log.info("扣除君主{}满血复活次数成功,已用次数{},免费满血次数为{}，购买的满血次数为{}",jzId,zdbean.fuhuo4uesd,
				YunbiaoTemp.resurgenceTimes,zdbean.fuhuoTimes4Vip);
		HibernateUtil.save(zdbean);
		return true;
	}
	/**
	 * @Description 返回剩余免费满血复活次数
	 * @param jz
	 * @return
	 */
	public int getFuhuoTimes(JunZhu jz) {
		long jzId=jz.id;
		int vipLevel=jz.vipLevel;
		YBBattleBean zdbean = HibernateUtil.find(YBBattleBean.class, jzId);
		if (zdbean == null) {
			zdbean=initYBBattleInfo(jzId, vipLevel);
		} else {
			zdbean = resetYBBattleInfo(zdbean, vipLevel);
		}
		int	remainTimes	=YunbiaoTemp.resurgenceTimes+zdbean.fuhuoTimes4Vip-zdbean.fuhuo4uesd;
		log.info("返回君主{}剩余满血复活次数{},已用次数{},免费满血次数为{}，购买的满血次数为{}",jzId,remainTimes,zdbean.fuhuo4uesd,
				YunbiaoTemp.resurgenceTimes,zdbean.fuhuoTimes4Vip);
		return remainTimes;
	}
	
	/**
	 * @Description 增加君主的vip购买的满血复活总次数
	 * @param jz
	 * @param addTimes
	 */
	public void buyfuhuoTimes4Vip(JunZhu jz,int addTimes) {
		long jzId=jz.id;
		int vipLevel=jz.vipLevel;
		YBBattleBean zdbean = HibernateUtil.find(YBBattleBean.class, jzId);
		if (zdbean == null) {
			zdbean=initYBBattleInfo(jzId, vipLevel);
		} else {
			zdbean = resetYBBattleInfo(zdbean, vipLevel);
		}
		log.info("增加君主{} 的vip购买的满血复活次数{}===》{},已用次数{},免费满血次数为{}",jzId,zdbean.fuhuoTimes4Vip,zdbean.fuhuoTimes4Vip+addTimes,
				zdbean.fuhuo4uesd,YunbiaoTemp.resurgenceTimes);
		zdbean.fuhuoTimes4Vip+=addTimes;
		zdbean.buyfuhuo4Vip+=1;
		HibernateUtil.save(zdbean);
	}
	/**
	 * @Description 返回Vip今日进行"购买满血复活次数"的 次数
	 * @param jz
	 * @return
	 */
	public int getBuyFuhuoTimes4Vip(JunZhu jz) {
		long jzId=jz.id;
		int vipLevel=jz.vipLevel;
		YBBattleBean zdbean = HibernateUtil.find(YBBattleBean.class, jzId);
		if (zdbean == null) {
			zdbean=initYBBattleInfo(jzId, vipLevel);
		} else {
			zdbean = resetYBBattleInfo(zdbean, vipLevel);
		}
		return zdbean.buyfuhuo4Vip;
	}

	/**
	 * @Description: 请求押镖活动主页面
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getYabiaoMainInfo(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求押镖主页出错：君主不存在");
			return;
		}
		YabiaoMainInfoResp.Builder resp = YabiaoMainInfoResp.newBuilder();
		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jz.id);
		int remainYB = getYaBiaoCountForVip(jz.vipLevel);
		boolean isNew4Enemy = false;
		boolean isNew4History = false;
		int todayBuyYBTimes=0;
		if (ybBean == null) {
			ybBean =initYaBiaoBeanInfo(jz.id, jz.vipLevel);
		} else {
			ybBean = resetybBean(ybBean, jz.vipLevel);
			remainYB=ybBean.remainYB;
			isNew4Enemy=ybBean.isNew4Enemy;
			isNew4History=ybBean.isNew4History;
			todayBuyYBTimes=ybBean.todayBuyYBTimes;
		}
		resp.setYaBiaoCiShu(remainYB);
		resp.setIsNew4Enemy(isNew4Enemy);
		resp.setIsNew4History(isNew4History);
		resp.setBuyCiShu(todayBuyYBTimes);
		resp.setIsOpen(openFlag);
		session.write(resp.build());
	}

	/**
	 * @Description: 请求历史列表
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getYabiaoHistoryInfo(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求押镖历史出错：君主不存在");
			return;
		}
		YBHistoryResp.Builder resp = YBHistoryResp.newBuilder();
		// 历史记录
		List<byte[]> historyList = Redis.getInstance().lrange(HISTORY_KEY + jz.id, 0, -1);
		for (byte[] bs : historyList) {
			YaBiaoHistory hisBean = (YaBiaoHistory) SerializeUtil.unserialize(bs);
			YBHistory.Builder history = YBHistory.newBuilder();
			history.setEnemyId(hisBean.enemyId);
			history.setEnemyName(hisBean.enemyName);
			history.setEnemyLevel(hisBean.enemyLevel);
			history.setEnemylianMengName(hisBean.enemyLianMengName);
			history.setEnemyzhanLi(hisBean.enemyZhanLi);
			history.setSelfzhanLi(hisBean.junzhuZhanLi);
			history.setSelfLevel(hisBean.enemyLevel);
			history.setShouyi(hisBean.shouyi);
			history.setTime(hisBean.battleTime.getTime());
			history.setResult(hisBean.result);
			JunZhu enemyJz=HibernateUtil.find(JunZhu.class, hisBean.enemyId);
			int guojia =enemyJz==null?1:enemyJz.guoJiaId;
			history.setEnemyGuojia(guojia);
			int type = -1;
			if (hisBean.result == 1 || hisBean.result == 3) {
				type = hisBean.horseType;
			} else if (hisBean.result == 2 || hisBean.result == 4) {
				type = hisBean.enemyRoleId;
			}
			history.setType(type);
			resp.addHistoryList(history.build());
		}
		//重置新历史标记
		YaBiaoBean ybBean=HibernateUtil.find(YaBiaoBean.class, jz.id);
		if(ybBean!=null){
			ybBean.isNew4History=false;
			HibernateUtil.save(ybBean);
		}
		session.write(resp.build());
	}
	
	/**
	 * @Description: 推送我在押镖给所有被我打劫的人 此方法不会重置数据库中运镖仇人标记
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void sendYabiaoState2Shouhaizhe(long jzId) {
		// 历史记录
		List<byte[]> historyList = Redis.getInstance().lrange(HISTORY_KEY + jzId, 0, -1);
		for (byte[] bs : historyList) {
			YaBiaoHistory hisBean = (YaBiaoHistory) SerializeUtil.unserialize(bs);
			//1：表示打劫别人未成功 2：被打劫成功击退敌人 3表示：表示成功打劫别人 4：表示被成功打劫
			if (hisBean.result == 3) {
				long shouhaizhejzId=hisBean.enemyId;
				YaBiaoBean shbean = HibernateUtil.find(YaBiaoBean.class, shouhaizhejzId);
				shbean.isNew4Enemy=true;
				HibernateUtil.save(shbean);
				log.info("推送==={}在押镖给他被打劫过的君主===={}",jzId,shouhaizhejzId);
				//给被打劫者推送仇人在运镖
				pushYBRecord(shouhaizhejzId, false, true);
			}
		}
	}
	/**
	 * @Description: 更新仇人运镖结束
	 */
	public void refreshYabiaoState2Shouhaizhe(long jzId) {
		// 历史记录
		List<byte[]> historyList = Redis.getInstance().lrange(HISTORY_KEY + jzId, 0, -1);
		for (byte[] bs : historyList) {
			YaBiaoHistory hisBean = (YaBiaoHistory) SerializeUtil.unserialize(bs);
			//1：表示打劫别人未成功 2：被打劫成功击退敌人 3表示：表示成功打劫别人 4：表示被成功打劫
			if (hisBean.result == 3) {
				long shouhaizhejzId=hisBean.enemyId;
				YaBiaoBean shbean = HibernateUtil.find(YaBiaoBean.class, shouhaizhejzId);
				if(shbean!=null){
					shbean.isNew4Enemy=false;
					HibernateUtil.save(shbean);
				}
			}
		}
	}
	/**
	 * @Description: 请求仇人列表
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getYabiaoEnemyInfo(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求押镖仇人出错：君主不存在");
			return;
		}
		EnemiesResp.Builder resp = EnemiesResp.newBuilder();
		log.info("君主{}请求押镖仇人",jz.id);
		List<String> list = DB.lgetList(ENEMY_KEY + jz.id);
		for (String str : list) {
			Long enemyId = Long.valueOf(str);
			EnemiesInfo.Builder enemy = EnemiesInfo.newBuilder();
			enemy.setJunZhuId(enemyId);
			JunZhu enJz = HibernateUtil.find(JunZhu.class, enemyId);
			int zhanli = JunZhuMgr.inst.getJunZhuZhanliFinally(enJz);
			enemy.setZhanLi(zhanli);
			enemy.setJzLevel(enJz.level);
			enemy.setJunZhuName(enJz.name);
			enemy.setGuojia(enJz.guoJiaId);
			YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(enemyId);
			// 10：押镖，20：未参加押镖活动
			int state = 20;
			long usedTime = 0;
			long totalTime = 0;
			int hrseType = 0;
			int hp = -1;
			int hudun=0;
			String lmName = "";
			if (ybr != null) {
				state = 10;
				usedTime = ybr.usedTime;
				totalTime = ybr.totalTime;
				hrseType = ybr.horseType;
				YaBiaoBean yb = HibernateUtil.find(YaBiaoBean.class, enemyId);
				if (yb != null) {
					hp = ybr.hp;
					hudun=ybr.hudun;
				}
			}
			AllianceBean askBean = AllianceMgr.inst.getAllianceByJunZid(enemyId);
			lmName = (askBean == null) ? lmName : askBean.name;
			enemy.setHp(hp);
			enemy.setMaxHp(enJz.shengMingMax);
			// int state=ybJzId2ScIdMap.get(enJz.id) != null ? 10 :
			// (jbJz2ScIdMap
			// .get(enJz.id) != null ? 20 : 30);
			enemy.setState(state);
			enemy.setUsedTime(usedTime);
			enemy.setTotalTime(totalTime);
			enemy.setHorseType(hrseType);
			enemy.setLianMengName(lmName);
			enemy.setRoleId(enJz.roleId);
			//2015年8月31日返回仇人的护盾
			enemy.setHudun(hudun*100/enJz.shengMingMax);
			Integer scId = ybJzId2ScIdMap.get(enemyId);
			// 不在押镖的时候，ERoomId值为-1
			// 只有值大于等于0的时候，才能直接进入其押镖犯贱
			if (scId != null) {
				enemy.setERoomId(scId);
			} else {
				enemy.setERoomId(-1);
			}
			resp.addEnemyList(enemy.build());
		}
		//重置新仇人标记
		YaBiaoBean ybBean=HibernateUtil.find(YaBiaoBean.class, jz.id);
		if(ybBean!=null){
			ybBean.isNew4Enemy=false;
			HibernateUtil.save(ybBean);
		}
		session.write(resp.build());
	}

	
	/**
	 * @Description 请求押镖界面面
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getYabiaoMenu(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求押镖界面出错：君主不存在");
			return;
		}
		Long jzId = jz.id;
		YabiaoMenuResp.Builder resp = YabiaoMenuResp.newBuilder();
		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jzId);
		int horseType=0;
		if (ybBean == null) {
			ybBean = initYaBiaoBeanInfo(jzId, jz.vipLevel);
		} else {
			if(ybBean.horseType==-1){
				ybBean.horseType=0;
			}
			horseType=ybBean.horseType;
			ybBean = resetybBean(ybBean, jz.vipLevel);
		}
		//是否播放随机马匹效果
		boolean isNewHorse=true;
		if(horseType==ybBean.horseType){
			isNewHorse=false;
		}
//		resp.setRemainAskXZ(ybBean.remainAskXZ);
		resp.setHorse(ybBean.horseType);
		resp.setIsNewHorse(isNewHorse);
		// 加载协助君主信息
//		getXieZhuJZInfo(jzId,jz.shengMingMax, resp);
		//加载马车道具
		YBBattleBean zdbean =getYBZhanDouInfo(jz.id, jz.vipLevel);
		HorseProp.Builder prop=HorseProp.newBuilder();
		makeHorseToolResp(prop, zdbean);
		resp.setHorseprop(prop.build());
		session.write(resp.build());
	}

	
	/**
	 * @Description 请求协助君主列表
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getXieZhuJZInfo(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求押镖界面出错：君主不存在");
			return;
		}
		long  ybJzId=jz.id;
		log.info("{}请求协助君主列表，开始", ybJzId);
		HashSet<Long> xiezhuSet = xieZhuCache4YBJZ.get(ybJzId);
		if (xiezhuSet == null || xiezhuSet.size() == 0) {
			log.info("{}无协助君主", ybJzId);
			return;
		}
		XieZhuJunZhuResp.Builder resp=XieZhuJunZhuResp.newBuilder();
		for (Long jzId : xiezhuSet) {
			JunZhu xzJz = HibernateUtil.find(JunZhu.class, jzId);
			if (xzJz != null) {
				XieZhuJunZhu.Builder xiezhujz = XieZhuJunZhu.newBuilder();
				xiezhujz.setJzId(xzJz.id);
				xiezhujz.setName(xzJz.name);
				xiezhujz.setRoleId(xzJz.roleId);
				resp.addXiezhuJz(xiezhujz);
			}
		}
		session.write(resp.build());
		log.info("{}请求协助君主列表，结束", ybJzId);
	}

	/**
	 * @Description: 升级马匹（满级时直接返回）
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void setHorseType(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求设置马匹出错：君主不存在");
			return;
		}
		HorseType.Builder req=(HorseType.Builder) builder;
		int targetType=req.getHorseType();
		SetHorseResult.Builder resp = SetHorseResult.newBuilder();
		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jz.id);
		if (ybBean == null) {
			log.error("请求设置马匹出错：君主押镖信息不存在{}", jz.id);
			resp.setResult(30);
			session.write(resp.build());
			return;
		} 
		if (ybBean.horseType == 5) {
			log.error("请求设置马匹出错：马匹已经达到满级{}-{}", jz.id, ybBean.horseType);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		int cost=0;
		int nowhorseType=ybBean.horseType;
		//2015年12月11日ShengjiCost 意义从“升到下一级花费元宝”改成“当前品质马车的价值”  处理逻辑进行修正
		CartTemp nowCart = cartMap.get(nowhorseType);
		if(nowCart==null){
			log.info("请求设置马匹出错：{}当前马匹品质{}未找到CartTemp配置", jz.id,nowhorseType);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		CartTemp targetCart = cartMap.get(targetType);
		if(targetCart==null){
			log.info("请求设置马匹出错：{}升级的目标马匹品质{}未找到CartTemp配置", jz.id,targetType);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		cost=targetCart.ShengjiCost-nowCart.ShengjiCost;
//		for (int i = nowhorseType; i < targetType; i++) {
//			// 获取马车配置
//			CartTemp cart = cartMap.get(i);
//			// 此处引用了世界聊天元宝的配置，要根据需求改
//			cost += cart.ShengjiCost;
//			log.info("请求设置马匹,计算马匹从{}升级到{},需要花费--{}",i,i+1,cart.ShengjiCost);
//		}
		if(cost<=0){
			log.info("请求设置马匹出错：{}需要花费的元宝---{}计算错误", jz.id,cost);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		log.info("请求设置马匹,马匹从{}升级到{},需要花费--{}", nowhorseType,targetType,cost);
		// PurchaseMgr.inst.getNeedYuanBao(UPDATE_HORSE_COST_TYPE, 1);
		if (jz.yuanBao < cost) {
			log.info("请求设置马匹出错：君主{}元宝{}不足消耗--{}", jz.id,jz.yuanBao,cost);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}

		YuanBaoMgr.inst.diff(jz, -cost, 0, cost,YBType.YB_SHENGJI_YABIAO_MAPI, "升级押镖马匹");
		HibernateUtil.save(jz);
		JunZhuMgr.inst.sendMainInfo(session);// 推送元宝信息
		ybBean.horseType = targetType;
		HibernateUtil.save(ybBean);
		log.info("junzhu:{}升级{}马匹为{}，花费元宝{}", jz.id, ybBean.horseType - 1,ybBean.horseType, cost);
		resp.setResult(10);
		session.write(resp.build());
	}
	/**
	 * @Description: 买马车道具
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void buyHorseTool(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求设置马匹出错：君主不存在");
			return;
		}
		HorsePropReq.Builder req=(HorsePropReq.Builder) builder;
		int propType=req.getPropType();
		HorsePropResp.Builder resp = HorsePropResp.newBuilder();
		YBBattleBean zdbean =getYBZhanDouInfo(jz.id, jz.vipLevel);
		switch (propType) {
		case MaJuConstant.baodi:
			buyBaoDi(session, resp, jz, zdbean);
			break;
		case MaJuConstant.gaojimabian:
			buyJiaSu(session, resp, jz, zdbean);
			break;
		case MaJuConstant.baohu:
			buyBaoHu(session, resp, jz, zdbean);
			break;
		default:
			log.error("{}请求购买马车道具失败：道具类型--{}错误", jz.id, propType);
			resp.setRes(20);
			HorseProp.Builder prop=HorseProp.newBuilder();
			makeHorseToolResp(prop, zdbean);
			resp.setProp(prop.build());
			session.write(resp.build());
			break;
		}
	}
	
	/**
	 * @Description 得到君主的押镖战斗YBBattleBean
	 * @param jzId
	 * @param vipLevel
	 * @return
	 */
	public YBBattleBean getYBZhanDouInfo(long jzId,int vipLevel) {
		YBBattleBean zdbean = HibernateUtil.find(YBBattleBean.class, jzId);
		if (zdbean == null) {
			zdbean=initYBBattleInfo(jzId, vipLevel);
		}else{
			zdbean=resetYBBattleInfo(zdbean, vipLevel);
		}
		return zdbean;
	}
	/**
	 * @Description 得到君主的押镖YaBiaoBean
	 * @param jzId
	 * @param vipLevel
	 * @return
	 */
	public YaBiaoBean getYaBiaoBean(long jzId,int vipLevel) {
		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jzId);
		if (ybBean == null) {
			ybBean =initYaBiaoBeanInfo(jzId, vipLevel);
		} else {
			ybBean =resetybBean(ybBean, vipLevel);
		}
		return ybBean;
	}

	protected void buyBaoDi(IoSession session, HorsePropResp.Builder resp,JunZhu jz,YBBattleBean zdbean) {
		if (zdbean.baodi >0) {
			log.error("{}请求购买马车道具失败：保底收益道具已购买", jz.id);
			resp.setRes(20);
			HorseProp.Builder prop=HorseProp.newBuilder();
			makeHorseToolResp(prop, zdbean);
			resp.setProp(prop.build());
			session.write(resp.build());
			return;
		}
		int cost=0;
		MaJu mj=majuMap.get(MaJuConstant.baodi);
		if(mj!=null){
			cost=mj.price;
		}else{
			log.info("{}请求购买马车保底收益失败,没找到配置id为{}的MaJu配置,", jz.id,MaJuConstant.baodi);
			resp.setRes(20);
			HorseProp.Builder prop=HorseProp.newBuilder();
			makeHorseToolResp(prop, zdbean);
			resp.setProp(prop.build());
			session.write(resp.build());
			return;
		}
		log.info("{}请求购买马车保底收益道具,需要花费--{}", jz.id,cost);
		if (jz.yuanBao < cost) {
			log.info("{}请求购买马车保底收益道具失败,元宝---{}不够花费--{},", jz.id,jz.yuanBao,cost);
			resp.setRes(30);
			HorseProp.Builder prop=HorseProp.newBuilder();
			makeHorseToolResp(prop, zdbean);
			resp.setProp(prop.build());
			session.write(resp.build());
			return;
		}

		YuanBaoMgr.inst.diff(jz, -cost, 0, cost,YBType.YB_SHENGJI_YABIAO_MAPI, "购买马车保底收益道具");
		HibernateUtil.save(jz);
		JunZhuMgr.inst.sendMainInfo(session);// 推送元宝信息
		zdbean.baodi = MaJuConstant.baodi;
		HibernateUtil.save(zdbean);
		log.info("{}请求购买马车保底收益道具--{},花费--{},剩余元宝{}", jz.id,zdbean.baodi,cost,jz.yuanBao);
		
		resp.setRes(10);
		HorseProp.Builder prop=HorseProp.newBuilder();
		makeHorseToolResp(prop, zdbean);
		resp.setProp(prop.build());
		session.write(resp.build());
	}
	protected void buyJiaSu(IoSession session, HorsePropResp.Builder resp,JunZhu jz,YBBattleBean zdbean) {
		if (zdbean.jiasu >0) {
			log.error("{}请求购买马车道具失败：加速道具已购买", jz.id);
			resp.setRes(20);
			HorseProp.Builder prop=HorseProp.newBuilder();
			makeHorseToolResp(prop, zdbean);
			resp.setProp(prop.build());
			session.write(resp.build());
			return;
		}
		int cost=0;
		MaJu mj=majuMap.get(MaJuConstant.gaojimabian);
		if(mj!=null){
			cost=mj.price;
		}else{
			log.info("{}请求购买马车加速道具失败,没找到配置id为{}的MaJu配置,", jz.id,MaJuConstant.gaojimabian);
			resp.setRes(20);
			HorseProp.Builder prop=HorseProp.newBuilder();
			makeHorseToolResp(prop, zdbean);
			resp.setProp(prop.build());
			session.write(resp.build());
			return;
		}
		log.info("{}请求购买马车加速道具,需要花费--{}", jz.id,cost);
		if (jz.yuanBao < cost) {
			log.info("{}请求购买马车加速道具失败,元宝---{}不够花费--{},", jz.id,jz.yuanBao,cost);
			resp.setRes(30);
			HorseProp.Builder prop=HorseProp.newBuilder();
			makeHorseToolResp(prop, zdbean);
			resp.setProp(prop.build());
			session.write(resp.build());
			return;
		}

		YuanBaoMgr.inst.diff(jz, -cost, 0, cost,YBType.YB_SHENGJI_YABIAO_MAPI, "购买马车加速道具");
		HibernateUtil.save(jz);
		JunZhuMgr.inst.sendMainInfo(session);// 推送元宝信息
		zdbean.jiasu = MaJuConstant.gaojimabian;
		HibernateUtil.save(zdbean);
		log.info("{}请求购买马车加速道具--{},花费--{},剩余元宝{}", jz.id,zdbean.jiasu,cost,jz.yuanBao);
		resp.setRes(10);
		HorseProp.Builder prop=HorseProp.newBuilder();
		makeHorseToolResp(prop, zdbean);
		resp.setProp(prop.build());
		session.write(resp.build());
	}
	protected void buyBaoHu(IoSession session, HorsePropResp.Builder resp,JunZhu jz,YBBattleBean zdbean) {
		if (zdbean.baohu >0) {
			log.error("{}请求购买马车道具失败：保护道具已购买", jz.id);
			resp.setRes(20);
			HorseProp.Builder prop=HorseProp.newBuilder();
			makeHorseToolResp(prop, zdbean);
			resp.setProp(prop.build());
			session.write(resp.build());
			return;
		}
		int cost=0;
		MaJu mj=majuMap.get(MaJuConstant.baohu);
		if(mj!=null){
			cost=mj.price;
		}else{
			log.info("{}请求购买马车保护道具失败,没找到配置id为{}的MaJu配置,", jz.id,MaJuConstant.baohu);
			resp.setRes(20);
			HorseProp.Builder prop=HorseProp.newBuilder();
			makeHorseToolResp(prop, zdbean);
			resp.setProp(prop.build());
			session.write(resp.build());
			return;
		}
		log.info("{}请求购买马车保护道具,需要花费--{}", jz.id,cost);
		if (jz.yuanBao < cost) {
			log.info("{}请求购买马车保护道具失败,元宝---{}不够花费--{},", jz.id,jz.yuanBao,cost);
			resp.setRes(30);
			HorseProp.Builder prop=HorseProp.newBuilder();
			makeHorseToolResp(prop, zdbean);
			resp.setProp(prop.build());
			session.write(resp.build());
			return;
		}

		YuanBaoMgr.inst.diff(jz, -cost, 0, cost,YBType.YB_SHENGJI_YABIAO_MAPI, "购买马车保护道具");
		HibernateUtil.save(jz);
		JunZhuMgr.inst.sendMainInfo(session);// 推送元宝信息
		zdbean.baohu = MaJuConstant.baohu;
		HibernateUtil.save(zdbean);
		log.info("{}请求购买马车保护道具--{},花费--{},剩余元宝{}", jz.id,zdbean.baohu,cost,jz.yuanBao);
		resp.setRes(10);
		HorseProp.Builder prop=HorseProp.newBuilder();
		makeHorseToolResp(prop, zdbean);
		resp.setProp(prop.build());
		session.write(resp.build());
	}

	protected HorseProp.Builder makeHorseToolResp(HorseProp.Builder prop,YBBattleBean zdbean) {
		if(zdbean.baodi>0){
			prop.addToolId(MaJuConstant.baodi);
		}
		if(zdbean.baohu>0){
			prop.addToolId(MaJuConstant.baohu);
		}
		if(zdbean.jiasu>0){
			prop.addToolId(MaJuConstant.gaojimabian);
		}
		return prop;
	}


	/**
	 * @Description: //押镖机器人进入押镖
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void startYabiaoReq(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		YabiaoResult.Builder resp = YabiaoResult.newBuilder();

		if (jz == null) {
			resp.setResult(20);
			// resp.setPathId(-1);废弃字段
//			resp.setRoomId(-1);
			session.write(resp.build());
			log.error("请求开始押镖出错：君主不存在");
			return;
		}
		long jzId=jz.id;
		// 判断协助者状态
//		List<Long> returnSet = CheckXieZhuState(jz.id);
//		if (returnSet.size() > 0) {
//			resp.setResult(40);
//			resp.setRoomId(-1);
//			for (Long jzId : returnSet) {
//				resp.addJzId(jzId);
//			}
//			session.write(resp.build());
//		}
		//判断是否已经在运镖
		YaBiaoRobot temp = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(jz.id);
		if (temp != null) {
			resp.setResult(30);
			// resp.setPathId(-1);废弃字段
//			resp.setRoomId(-1);
			session.write(resp.build());
			log.error("{}请求开始押镖出错：君主已参加押镖",jzId);
			return;
		}
		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jz.id);
		if(ybBean.remainYB<1){
			resp.setResult(40);
			// resp.setPathId(-1);废弃字段
//			resp.setRoomId(-1);
			session.write(resp.build());
			log.error("{}请求开始押镖出错：君主押镖次数已用完",jzId);
			return;
		}
		
//		// 进入押镖场景进行押镖 2015年12月11日1.1版本改成马车进入君主所在场景
//		int scId = locateFakeSceneId();
//		Scene sc = yabiaoScenes.get(scId);
		Scene sc = (Scene) session.getAttribute(SessionAttKey.Scene);
		Integer scId =(Integer)session.getAttribute(SessionAttKey.SceneID);
		if(sc == null||scId==null||!sc.name.contains("YB")){
			resp.setResult(20);
			// resp.setPathId(-1);废弃字段
//			resp.setRoomId(-1);
			session.write(resp.build());
			log.error("{}请求开始押镖出错：君主不在押镖场景",jzId);
			return;
		}
		if(scId!=0){
			log.error("{}请求开始押镖出错：君主不在0号押镖场景",jzId);
		}
		Integer uid = (Integer)session.getAttribute(SessionAttKey.playerId_Scene);
		if(uid == null){
			resp.setResult(20);
			session.write(resp.build());
			log.error("{}请求开始押镖出错：找不到scene里的uid",jzId);
			return ;
		}
		// 创建押镖机器人
		int safeId=	getsafeId2Jz(sc, uid);
		YaBiaoRobot ybr=initYaBiaoRobot(session,jz,ybBean, sc,scId,safeId);
		if(ybr==null){
			resp.setResult(20);
			// resp.setPathId(-1);废弃字段
//			resp.setRoomId(-1);
			session.write(resp.build());
			log.error("{}请求开始押镖出错：创建押镖机器人失败",jzId);
			return;
		}
		
//		HashSet<Long> xiezhuSet = xieZhuCache4YBJZ.get(jzId);
//		if (xiezhuSet != null) {
//			log.info("扣除{}请求协助次数", jzId);
//			ybBean.usedAskXZ += 1;
//			ybBean.remainAskXZ -= 1;
//		}

		
	
//		ybBean.lastYBDate = new Date();
//		ybBean.historyYB += 1;// 押镖历史次数+1

	
		// 返回成功进入押镖活动
		resp.setResult(10);
		// resp.setPathId(pathId);废弃字段
//		resp.setRoomId(scId);
		session.write(resp.build());

		// 广播押镖机器人进入场景
		broadBiaoCheInfo(sc, ybr);
		// 扣除盟友协助次数 2015年12月11日1.1版本出发不扣除盟友协助次数 无盟友加成护盾
//		settleXieZhuCount(jzId);
		// 生成护盾
//		initHuDun4YBJZ(jz.id,ybr);
//		HibernateUtil.save(ybBean);
		// 每日任务：完成1次押镖活动
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(jz.id,
				DailyTaskConstants.yunBiao, 1));
		//押镖开始事件
		EventMgr.addEvent(ED.BIAOCHE_CHUFA, new Object[] { jz,ybBean.horseType, System.currentTimeMillis()});
		// 主线任务完成
		EventMgr.addEvent(ED.finish_yunbiao_x, new Object[] { jz.id });
	}
	
	/**
	 * @Description 根据君主uid 得出镖车出发的安全区编号
	 * @param sc
	 * @param uid
	 * @return
	 */
	public int getsafeId2Jz(Scene sc,Integer uid) {
		Player player =sc.players.get(uid);
		if(player==null){
			log.error("求君主UID--{}所在安全区失败,未找到Player",uid);
			return 1;
		}
		player.safeArea=getSafeArea(player.getPosX(), player.getPosZ());
		if(player.safeArea<0){
			player.safeArea=1;
		}
		return player.safeArea;
	}
	/**
	 * @Description 初始化君主押镖机器人
	 * @param jz
	 * @param ybr
	 * @param sc
	 * @param cartTime
	 * @param horseType
	 * @return
	 */
	public YaBiaoRobot initYaBiaoRobot(IoSession session,JunZhu jz,YaBiaoBean ybBean,Scene sc,int scId,int safeId) {
		JunzhuShengji ss = JunZhuMgr.inst.getJunzhuShengjiByLevel(jz.level);
		if(ss==null){
			log.error("初始化君主--{}押镖机器人失败,未找到JunzhuShengji配置",jz.id);
			return null;
		}
		int horseType=ybBean.horseType;
		// 获取马车配置
		CartTemp cart = cartMap.get(horseType);
		if(cart==null){
			log.error("初始化君主--{}押镖机器人失败,未找到CartTemp配置",jz.id);
			return null;
		}
		YunBiaoSafe safeArea=	safeAreaMap.get(safeId);
		if(safeArea==null){
			log.error("初始化君主--{}押镖机器人失败,未找到安全区配置",jz.id);
			return null;
		}
		YaBiaoRobot ybr = new YaBiaoRobot();
		ybr.session = new RobotSession();
		ybr.move = SpriteMove.newBuilder();
		ybr.jzId = jz.id;
		ybr.jzLevel=jz.level;
		ybr.name = jz.name;
		//马车出现的路线和安全区编号一致
		int pathId =safeArea.areaID;
		ybr.pathId = pathId;
		ybr.nodeId=1;
		ybr.totalTime = cart.cartTime * 1000;// 毫秒计算
		ybr.usedTime = 0;
		ybr.startTime = System.currentTimeMillis();
		ybr.startTime2upSpeed = System.currentTimeMillis();
		int upSpeedTime=0;
		int protectTime=0;
		YBBattleBean zdbean =getYBZhanDouInfo(jz.id, jz.vipLevel);
		if(zdbean.baohu>0){
			MaJu wudi=majuMap.get(zdbean.baohu);
			protectTime=wudi.value1;//保护罩时间
		}
		ybr.protectTime=protectTime*1000;
		ybr.upSpeedTime = upSpeedTime*1000;
		log.info("产生系统机器人--{},初始无敌时间为{},初始加速时间为{}",jz.id,protectTime,ybr.upSpeedTime);
		ybr.speed=1;
		ybr.startTime4short = System.currentTimeMillis();
		ybr.endBattleTime = System.currentTimeMillis();
		ybr.horseType =horseType;
		int zhanli = JunZhuMgr.inst.getJunZhuZhanliFinally(jz);
		ybr.zhanli=zhanli;
		HashMap<Integer, RoadNode> roadPath=YBRobotMgr.road.get(pathId);
		if(roadPath==null){
			log.error("产生系统机器人--{}出错,未找到pathId=={}的路线配置",jz.id,pathId);
			return null;
		}
		RoadNode rd=roadPath.get(1); 
		if(rd==null){
			log.error("产生系统机器人--{}出错,未找到pathId=={}的路线配置的第1个节点",jz.id,pathId);
			return null;
		}
		ybr.totaltime4short=rd.totalTime;
		ybr.posX=ybr.startPosX=rd.posX;
		ybr.posZ=ybr.startPosZ=rd.posZ;
		RoadNode rd4next=roadPath.get(2); 
		if(rd4next==null){
			log.error("产生系统机器人--{}出错,未找到pathId=={}的路线配置的第2个节点",jz.id,pathId);
			return null;
		}
		ybr.nextPosX=rd4next.posX;
		ybr.nextPosZ=rd4next.posZ;
		ybr.move.setPosX(ybr.posX);
		ybr.move.setPosY(0);//不用管Y坐标
		ybr.move.setPosZ(ybr.posZ);
		ybr.hp = jz.shengMingMax;
		ybr.maxHp = jz.shengMingMax;
		log.info("君主{}机器人进入押镖场景,血量为{}", jz.id, ybr.hp);
		EnterScene.Builder enter = EnterScene.newBuilder();
		enter.setUid(0);
		enter.setSenderName(ybr.name);
		enter.setPosX(ybr.posX);
		enter.setPosY(0);
		enter.setPosZ(ybr.posZ);
		//镖车标记
		ybr.session.setAttribute(SessionAttKey.RobotType, Scene.YBRobot_RoleId);
		//镖车君主id
		ybr.session.setAttribute(SessionAttKey.RobotJZID, ybr.jzId);
		//君主职位
		Object zhiWu=session.getAttribute(SessionAttKey.LM_ZHIWU, 0);
		ybr.session.setAttribute(SessionAttKey.LM_ZHIWU,zhiWu);
		//君主称号
		Object chenghao=session.getAttribute(SessionAttKey.CHENG_HAO_ID, "-1");
		ybr.session.setAttribute(SessionAttKey.CHENG_HAO_ID, chenghao);
		//联盟名字
		Object lmName=session.getAttribute(SessionAttKey.LM_NAME, "***");
		ybr.session.setAttribute(SessionAttKey.LM_NAME, lmName);
		
		ybr.worth = (int) (ss.xishu * cart.profitPara);
		// 存入押镖Map
		pushYbJz2Map(jz.id, scId);
		// 将镖车机器人加入镖车机器人管理线程
		BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.put(jz.id, ybr);
		sc.exec(PD.Enter_YBScene, ybr.session, enter);
		return ybr;
	}
	/**
	 * @Description 得到君主出生安全区坐标配置
	 * @param ybsc
	 */
	public YunBiaoSafe getBirthPlace4YBJZ(Scene ybsc) {
		Map<Integer, Integer> safeMap=YaBiaoHuoDongMgr.inst.getSafeAreaCount(ybsc);
		int safeId=1;
		YunBiaoSafe safeArea=null;
		for(int i=1;i<5;i++) {
			Integer count=safeMap.get(i);
			count=count==null?0:count;
			if(count<YunbiaoTemp.saveArea_people_max){
				safeId=i;
				//安全区内人数不满开始产生马车
				log.info("安全区--{}人数--{}不满{}",i,count,YunbiaoTemp.saveArea_people_max);
				break;
			}
		}
		safeArea=safeAreaMap.get(safeId);
		if(safeArea==null){
			safeArea=safeAreaMap.get(1);
		}
		return safeArea;
	}
	/**
	 * @Description 得到镖车出生安全区坐标配置
	 * @param ybsc
	 */
	public YunBiaoSafe getBirthPlace4BiaoChe(Scene ybsc) {
		Map<Integer, Integer> safeMap=YaBiaoHuoDongMgr.inst.getSafeAreaCount(ybsc);
		int safeId=1;
		YunBiaoSafe safeArea=null;
		for(int i=1;i<5;i++) {
			Integer count=safeMap.get(i);
			if(count<YunbiaoTemp.saveArea_people_max){
				safeId=i;
				//安全区内人数不满开始产生马车
				log.info("安全区--{}人数--{}不满{}",i,count,YunbiaoTemp.saveArea_people_max);
			}
		}
		safeArea=safeAreaMap.get(safeId);
		if(safeArea==null){
			safeArea=safeAreaMap.get(1);
		}
		return safeArea;
	}
	/**
	 * @Description 算出指定安全区人数人数
	 * @param ybsc 
	 * @param safeId 安全区编号
	 * @return
	 */
	public Integer getSafeAreaCountById(Scene ybsc,int safeId) {
		Map<Integer, Integer> safeMap= getSafeAreaCount(ybsc);
		return safeMap.get(safeId);
	}
	
	/** 
	 * @Description 算出所有安全区人数
	 * @param ybsc
	 * @return
	 */
	public Map<Integer, Integer> getSafeAreaCount( Scene ybsc) {
		Map<Integer, Integer> safeMap= new HashMap<Integer, Integer>();
		safeMap.put(1, 0);//默认第一安全全0个人
		for(Map.Entry<Integer, Player> entry : ybsc.players.entrySet()) {
			Player p = entry.getValue();
			if(p.roleId!=Scene.YBRobot_RoleId&& p.safeArea>0){
			//不是镖车 且在安全区才计算
				Integer count=safeMap.get(p.safeArea);
				count=(count==null)?1:count+1;
				safeMap.put(p.safeArea,count);	
			}
		}
		return safeMap;
	}
	
	/**
	 * @Description  产生系统机器人
	 * @param sc
	 * @param pathId
	 * @param scId
	 * @return
	 */
	public boolean initSysYBRobots(Scene sc,int pathId,int scId) {
		
		//得到随机的马车配置
		CartNPCTemp biaoCheNPC=getRandomCartNPC();
		int bcNPCId =biaoCheNPC.id;
		log.info(" 产生系统机器人,配置ID--{}",bcNPCId);
		String robotName=getRandomString(5);
		YaBiaoRobot ybr=new YaBiaoRobot();
		ybr.session = new RobotSession();
		ybr.move = SpriteMove.newBuilder();
		long sysJzId=getRobotJzId() ;
		ybr.jzId = sysJzId;
		ybr.name = robotName;
		ybr.jzLevel=biaoCheNPC.level;
		JunzhuShengji ss = JunZhuMgr.inst.getJunzhuShengjiByLevel(ybr.jzLevel);
		if(ss==null){
			log.error("产生系统机器人--{}出错,未找到JunzhuShengji配置",	ybr.jzId );
			return false;
		}
		//读取配置得到血量
		ybr.hp = biaoCheNPC.shengming;
		ybr.maxHp = biaoCheNPC.shengming;
		ybr.bcNPCId=bcNPCId;
		ybr.zhanli=biaoCheNPC.power;
		ybr.pathId = pathId;
		ybr.nodeId=1;
		ybr.usedTime = 0;
		ybr.startTime = System.currentTimeMillis();
		ybr.startTime4short = System.currentTimeMillis();
		ybr.endBattleTime = System.currentTimeMillis();
		ybr.startTime2upSpeed = System.currentTimeMillis();
		ybr.horseType =biaoCheNPC.quality;
		CartTemp cart = cartMap.get(ybr.horseType );
		if(cart==null){
			log.error("产生系统机器人--{}出错,未找到CartTemp配置",sysJzId);
			return false;
		}
		ybr.totalTime = cart.cartTime * 1000;// 毫秒计算
		//加入配置系数
		ybr.worth = (int) (ss.xishu * cart.profitPara);
		HashMap<Integer, RoadNode> roadPath=YBRobotMgr.road.get(pathId);
		if(roadPath==null){
			log.error("产生系统机器人--{}出错,未找到pathId=={}的路线配置",sysJzId,pathId);
			return false;
		}
		RoadNode rd=roadPath.get(1); 
		if(rd==null){
			log.error("产生系统机器人--{}出错,未找到pathId=={}的路线配置的第1个节点",sysJzId,pathId);
			return false;
		}
		ybr.totaltime4short=rd.totalTime;
		ybr.posX=ybr.startPosX=rd.posX;
		ybr.posZ=ybr.startPosZ=rd.posZ;
		RoadNode rd4next=roadPath.get(2); 
		if(rd4next==null){
			log.error("产生系统机器人--{}出错,未找到pathId=={}的路线配置的第2个节点",sysJzId,pathId);
			return false;
		}
		ybr.nextPosX=rd4next.posX;
		ybr.nextPosZ=rd4next.posZ;
		ybr.move.setPosX(ybr.posX);
		ybr.move.setPosY(0);//不用管Y坐标
		ybr.move.setPosZ(ybr.posZ);
		EnterScene.Builder enter = EnterScene.newBuilder();
		enter.setUid(0);
		enter.setSenderName(ybr.name);
		enter.setPosX(ybr.posX);
		enter.setPosY(0);
		enter.setPosZ(ybr.posZ);
		ybr.session.setAttribute(SessionAttKey.RobotType, Scene.YBRobot_RoleId);
		ybr.session.setAttribute(SessionAttKey.RobotJZID, ybr.jzId);
		// 存入押镖Map
		pushYbJz2Map(sysJzId, scId);
		// 将镖车机器人加入镖车机器人管理线程
		BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.put(sysJzId, ybr);
		sc.exec(PD.Enter_YBScene, ybr.session, enter);
		Integer macheshu=YaBiaoHuoDongMgr.xtmcs4Scene.get(scId);
		if(macheshu!=null&&macheshu>0){
			macheshu--;
			YaBiaoHuoDongMgr.xtmcs4Scene.put(scId, macheshu);
		}
		return true;
	}
	//随机生产名字
	public static String getRandomString(int length) { 
		String base = "abcdefghijklmnopqrstuvwxyz0123456789俞伯牙席潮海丁克曾管正学管虎管谟业管仲陈伟霆王世充李渊杨坚郭树清李鸿忠王穗明刘铁男李登辉彭长健邓鸿王中军景百孚赵永亮陆兆禧严介和郁亮茅于轼王小波冯唐";   
		Random random = new Random();   
		StringBuffer sb = new StringBuffer();   
		for (int i = 0; i < length; i++) {   
			int number = random.nextInt(base.length());   
			sb.append(base.charAt(number));   
		}   
		return sb.toString();   
	}  

	
	/**
	 * @Description	随机生成系统机器人id,为负数表示系统机器人
	 * @return sysJzId
	 */
	public static long getRobotJzId() { 
		YaBiaoRobot tem=new YaBiaoRobot();
		long sysJzId=0;
		while (tem!=null) {
			sysJzId=-MathUtils.getRandom(Long.MAX_VALUE);
			tem=(YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(sysJzId);
			if(tem!=null){
				System.out.println("111");
			}
		}
		log.info("	随机生成系统机器人id,为负数表示系统机器人==={}",sysJzId);
		return sysJzId ;   
	}  
//	/**
//	 * @Description: 检查协助者是否可以协助
//	 * @param ybJzId
//	 * @return
//	 */
//	private List<Long> CheckXieZhuState(Long ybJzId) {
//		List<Long> wuxiaoJZList = new ArrayList<Long>();
//		HashSet<Long> xiezhuSet = xieZhuCache4YBJZ.get(ybJzId);
//		if (xiezhuSet == null || xiezhuSet.size() == 0) {
//			return wuxiaoJZList;
//		}
//		for (Long jzId : xiezhuSet) {
//			if (xzJZSatrtYB.contains(jzId)) {
//				wuxiaoJZList.add(jzId);
//			}
//		}
//		return wuxiaoJZList;
//	}

	/**
	 * @Description: //获取场景id
	 * @return
	 */
	public int locateFakeSceneId() {
		int size4YBSC = MAX_YB_NUM;// 默认最大押镖场景容纳人数
		int scId = 0;// 默认0号场景
		do {
			Scene sc = yabiaoScenes.get(scId);
			if (sc == null) {
				log.info("押镖场景--{}不存在,可以使用",scId);
				break;// 已有的场景都满了
			} else if (sc.players.size() < size4YBSC) {
				// 这个场景还没满
				log.info("押镖场景--{}人数为{},场景最大人数{},可以使用",scId,sc.players.size(),size4YBSC);
				break;
			}
			scId++;// 押镖场景ID递增
		} while (true);
		return scId;
	}
	
	/**
	 * @Description:鞭打马车加速
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void jiasuBiaoChe(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求马车加速失败，未找到请求君主");
			return;
		}
		log.info("{}请求马车加速开始",jz.id);
		long jzId=jz.id;
		JiaSuReq.Builder req=(JiaSuReq.Builder)builder;
		long ybjzId=req.getYbjzId();
		JiaSuResp.Builder resp=JiaSuResp.newBuilder();
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybjzId);
		if(ybr==null){
			log.error("请求马车加速失败，未找到{}的镖车",ybjzId);
			resp.setResCode(20);
			session.write(resp.build());
			return;
		}
		YBBattleBean zdBean = getYBZhanDouInfo(jzId, jz.vipLevel);
//		if (ybBean == null) {
//			log.error("请求马车加速失败，未找到请求君主--{}的YaBiaoBean数据",jzId);
//			resp.setResCode(20);
//			session.write(resp.build());
//			return;
//		}
		MaJu mabian=majuMap.get(zdBean.jiasu);
		boolean isGaojiMabian=true;
		//没有高级马具 或者  有高级马具但不是对自己的马车使用
		if((mabian==null)&&(ybjzId!=jzId)){
			mabian=majuMap.get(MaJuConstant.mabian);
			if(mabian==null){
				log.error("请求马车加速失败，未找到普通马具--{}配置",jzId,MaJuConstant.mabian);
				resp.setResCode(20);
				session.write(resp.build());
				return;
			}
			isGaojiMabian=false;
			log.info("{}请求{}马车加速,使用的是普通马鞭",jz.id,ybjzId);
		}
		int jiasuTime=mabian.value1*1000;
		ybr.upSpeedTime+=jiasuTime;
		int speed4jiasu=100+mabian.value2;
		ybr.speed=speed4jiasu/100;
		resp.setResCode(10);
		session.write(resp.build());
		log.info("{}请求{}马车加速完成,使用的是---《{}》马鞭",jz.id,ybjzId,isGaojiMabian?"普通":"高级");
		//广播马车加速时间？？看策划需求
		Scene sc = (Scene) session.getAttribute(SessionAttKey.Scene);
		log.info("广播{}的马车加速时间",ybjzId);
		broadBiaoCheInfo(sc, ybr);
	}

	/**
	 * @Description: //请求劫镖主页面
	 * @param id
	 * @param builder
	 * @param session
	 */
//	public void getJieBiaoInfo(int id, Builder builder, IoSession session) {
//		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
//		if (jz == null) {
//			log.error("请求劫镖主页出错：君主不存在");
//			return;
//		}
//		long jzId = jz.id;
//		log.info("{}请求劫镖主页",jzId);
//		YabiaoInfoResp.Builder resp = YabiaoInfoResp.newBuilder();
//		YaBiaoBean yb = HibernateUtil.find(YaBiaoBean.class, jzId);
//		if (yb == null) {
//			yb = initYaBiaoBeanInfo(jz.id, jz.vipLevel);
//		} else {
//			yb = resetybBean(yb, jz.vipLevel);
//		}
//		resp.setFangyuZuHeId(yb.zuheId);
//		Iterator<Integer> iter = yabiaoScenes.keySet().iterator();
//		while (iter.hasNext()) {
//			int roomId = (int) iter.next();
//			Set<Long> ybSet = ybJzList2ScIdMap.get(roomId);
//			if(ybSet!=null){
//				YabiaoRoomInfo.Builder ybroom = YabiaoRoomInfo.newBuilder();
//				ybroom.setRoomId(roomId);
//				for (Long junzhuId : ybSet) {
//					YabiaoJunZhuInfo.Builder ybjz = YabiaoJunZhuInfo.newBuilder();
//					if(junzhuId<0){
//						//处理系统镖车机器人
//						YaBiaoRobot sysRobot=(YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(junzhuId);
//						ybjz.setJunZhuId(junzhuId);
//						ybjz.setJunZhuName(sysRobot.name);
//						ybjz.setLevel(sysRobot.jzLevel);
////						YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class,ybJunZhu.id);
////						YaBiaoRobot ybrobot = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJunZhu.id);
////						if(ybrobot==null){
////							log.error("运镖君主--{}的镖车机器人不存在",ybJunZhu.id);
////							continue;
////						}
//						ybjz.setHp(sysRobot.hp);
//						ybjz.setMaxHp(sysRobot.maxHp);
//						ybjz.setWorth(sysRobot.worth);
//						ybjz.setMaxWorth(sysRobot.worth);
//						ybjz.setPathId(sysRobot.pathId);
//						ybjz.setUsedTime(sysRobot.usedTime);
//						ybjz.setTotalTime(sysRobot.totalTime);
//						ybjz.setJunzhuGuojia(sysRobot.guojiaId);
//						int reduceTime = ((int) (System.currentTimeMillis() - sysRobot.endBattleTime) / 1000);
//						
//						int protectTime = sysRobot.protectTime - reduceTime;
//						log.info("{}的保护时间为（{}）/（{})", junzhuId, protectTime,sysRobot.protectTime);
//						ybjz.setBaohuCD(protectTime > 0 ? protectTime : 0);
//						ybjz.setState(sysRobot.isBattle ? 20 : (protectTime > 0 ? 30: 10));
//						ybjz.setLianMengName( "" );
//						ybjz.setHorseType(sysRobot.horseType);
//						boolean IsEnemy =isYourEmeny(jzId, junzhuId) ;
//						ybjz.setIsEnemy(IsEnemy);
////						int zhanli = JunZhuMgr.inst.getJunZhuZhanliFinally(ybJunZhu);
//						ybjz.setZhanLi(sysRobot.zhanli);
//						//2015年8月31日返回盟友增加的护盾
//						ybjz.setHuDun(0);
//
//					}else{
//						//处理玩家镖车机器人
//						JunZhu ybJunZhu = HibernateUtil.find(JunZhu.class, junzhuId);
//						ybjz.setJunZhuId(ybJunZhu.id);
//						ybjz.setJunZhuName(ybJunZhu.name);
//						ybjz.setLevel(ybJunZhu.level);
////						YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class,ybJunZhu.id);
//						YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJunZhu.id);
//						if(ybr==null){
//							log.error("运镖君主--{}的镖车机器人不存在",ybJunZhu.id);
//							continue;
//						}
//						ybjz.setHp(ybr.hp);
//						ybjz.setMaxHp(ybJunZhu.shengMingMax);
//						ybjz.setWorth(ybr.worth);
//						ybjz.setMaxWorth(ybr.worth);
//						ybjz.setPathId(ybr.pathId);
//						ybjz.setUsedTime(ybr.usedTime);
//						ybjz.setTotalTime(ybr.totalTime);
//						ybjz.setJunzhuGuojia(ybJunZhu.guoJiaId);
//						int reduceTime = ((int) (System.currentTimeMillis() - ybr.endBattleTime) / 1000);
//						
//						int protectTime = ybr.protectTime - reduceTime;
//						log.info("{}的保护时间为（{}）/（{})", junzhuId, protectTime,ybr.protectTime);
//						ybjz.setBaohuCD(protectTime > 0 ? protectTime : 0);
//						ybjz.setState(ybr.isBattle ? 20 : (protectTime > 0 ? 30: 10));
//						AllianceBean ybabean = AllianceMgr.inst.getAllianceByJunZid(ybJunZhu.id);
//						ybjz.setLianMengName(ybabean == null ? "" : ybabean.name);
//						ybjz.setHorseType(ybr.horseType);
//						boolean IsEnemy =isYourEmeny(jzId, junzhuId);
//						ybjz.setIsEnemy(IsEnemy);
//						int zhanli = JunZhuMgr.inst.getJunZhuZhanliFinally(ybJunZhu);
//						ybjz.setZhanLi(zhanli);
//						//2015年8月31日返回盟友增加的护盾
//						ybjz.setHuDun(ybr.hudun*100/ybJunZhu.shengMingMax);
//					}
//					ybroom.addYbjzList(ybjz.build());
//				}
//				resp.addRoomList(ybroom);
//			}
//		}
//		session.write(resp.build());
//	}

	/**
	 * @Description: 保存押镖攻击密保
	 * @param jz
	 * @param mibaoIds
	 * @param zuheId
	 */
	public void saveGongJiMiBao(JunZhu jz, List<Long> mibaoIds, int zuheId) {
		YBBattleBean zdBean = getYBZhanDouInfo(jz.id, jz.vipLevel);
//		if (ybBean == null) {
//			log.error("玩家{}的押镖没有开启:", jz.id);
//			return;
//		}
		zdBean.gongJiZuHeId = zuheId;
		HibernateUtil.save(zdBean);
		log.info("玩家:{}押镖更换秘宝成功", jz.id);
	}

	/**
	 * @Description: //保存押镖防御密保
	 * @param jz
	 * @param mibaoIds
	 * @param zuheId
	 */
	public void saveFangShouMiBao(JunZhu jz, List<Long> mibaoIds, int zuheId) {
		YBBattleBean zdBean = getYBZhanDouInfo(jz.id, jz.vipLevel);
//		if (zdBean == null) {
//			log.error("玩家{}的押镖没有开启:", jz.id);
//			return;
//		}
		zdBean.zuheId = zuheId;
		HibernateUtil.save(zdBean);
		log.info("玩家:{}押镖更换秘宝成功", jz.id);
	}

	/**
	 * @Description: //劫镖人请求进入场景  玩法变更此方法废弃
	 * @param id
	 * @param builder
	 * @param session
	 */
//	public void enterJiebiaoScene(int id, Builder builder, IoSession session) {
//		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
//		if (jz == null) {
//			log.error("请求进入劫镖场景出错：君主不存在");
//			return;
//		}
//		EnterYaBiaoScene.Builder req = (EnterYaBiaoScene.Builder) builder;
//		int reqSceneId = 0;
//		if (req != null) {
//			reqSceneId = req.getRoomId();
//		} else {
//			log.error("请求进入劫镖场景出错：请求信息不存在");
//			return;
//		}
//		// 离开原来的场景
//		Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
//		if (scene != null) {
//			if (scene.name.contains("FW")) {
//				int uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
//				ExitScene.Builder exit = ExitScene.newBuilder();
//				exit.setUid(uid);
//				scene.exec(PD.Exit_HouseScene, session, exit);
//			} else {
//				int uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
//				ExitScene.Builder exit = ExitScene.newBuilder();
//				exit.setUid(uid);
//				scene.exec(PD.Exit_Scene, session, exit);
//			}
//		}
//		// 进入劫镖场景
//		Scene sc = yabiaoScenes.get(reqSceneId);
//		if (sc != null) {// 该联盟没有场景
//			synchronized (yabiaoScenes) {// 防止多次创建
//				sc = yabiaoScenes.get(reqSceneId);
//				if (sc != null) {
//					// 存入劫镖Map
//					pushJbJz2Map(jz.id, reqSceneId);
//					EnterScene.Builder enter = EnterScene.newBuilder();
//					enter.setUid(req.getUid());
//					enter.setSenderName(req.getSenderName());
//					enter.setPosX(req.getPosX());
//					enter.setPosY(req.getPosY());
//					enter.setPosZ(req.getPosZ());
//					sc.exec(PD.Enter_YBScene, session, enter);
//				} else {
//					log.error("请求进入劫镖场景出错1：请求场景不存在SceneId:{}", reqSceneId);
//					return;
//				}
//			}
//		} else {
//			log.error("请求进入劫镖场景出错2：请求场景不存在SceneId:{}", reqSceneId);
//			return;
//		}
//	}

	/**
	 * @Description:请求协助
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void askHelp4YB(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("君主不存在");
			return;
		}
		long ybJzId = jz.id;
		YaBiaoHelpResp.Builder resp = YaBiaoHelpResp.newBuilder();
		Long cdTime = (Long) session.getAttribute(SessionAttKey.LAST_CHAT_KEY);
		long currentMillis = System.currentTimeMillis();
		if (cdTime != null && cdTime > currentMillis) {
			log.warn("发送速度过快{}", jz.id);
			resp.setCode(30);
			session.write(resp.build());
			return;
		}
		AllianceBean aBean = AllianceMgr.inst.getAllianceByJunZid(ybJzId);
		if (aBean == null) {
			log.info("{}没有联盟，请求押镖协助失败", ybJzId);
			resp.setCode(20);
			session.write(resp.build());
			return;
		}
		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, ybJzId);
		if ( ybBean == null) {
			log.info("{}没有找到YaBiaoBean信息，请求押镖协助失败", ybJzId);
			resp.setCode(20);
			session.write(resp.build());
			return;
		}
//		CartTemp cart = cartMap.get(ybBean.horseType);
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJzId);
		if ( ybr == null) {
			log.info("{}没有找到镖车YaBiaoRobot信息，请求押镖协助失败", ybJzId);
			resp.setCode(20);
			session.write(resp.build());
			return;
		}
		int eventId=SuBaoConstant.zdqz;
//		boolean ret1=false;
//		boolean ret2=false;
		boolean ret3=false;
		int horseType=ybBean.horseType;
		long startTime=ybr.startTime;
		//2015年12月14日 改完配置  明确只向未协助盟友求助   没有向协助盟友、所有盟友求助操作求助 
//		ret1= PromptMsgMgr.inst.pushKB2ALLMengYou(jz, aBean.id, horseType, eventId, startTime);
//		if(!ret1){
//			ret2= PromptMsgMgr.inst.pushKB2XieZhuMengYou(jz, aBean.id, horseType, eventId, startTime);
//		}
//		if(!ret2){
			ret3=PromptMsgMgr.inst.pushKB2WeiXieZhuMengYou(jz, aBean.id, horseType, eventId, startTime);
//		}
		log.info("镖车出发事件处理结果ret3=={}",ret3);
		log.info("{}请求押镖协助成功", ybJzId);
		resp.setCode(10);
		session.write(resp.build());
	}

//	/**
//	 * @Description: //扣除协助者次数
//	 * @param ybJzId
//	 */
//	private void settleXieZhuCount(Long ybJzId) {
//		log.info("结算{}的盟友的协助次数", ybJzId);
//		HashSet<Long> xiezhuSet = xieZhuCache4YBJZ.get(ybJzId);
//		if (xiezhuSet == null) {
//			return;
//		}
//		for (Long jzId : xiezhuSet) {
//			if (!jzId.equals(0L)) {
//				YaBiaoBean bean = HibernateUtil.find(YaBiaoBean.class, jzId);
//				if (bean == null) {
//					log.error("玩家{}的押镖没有开启:", jzId);
//					return;
//				} else {
//					bean.usedXZ += 1;
//					bean.remainXZ -= 1;
//					HibernateUtil.save(bean);
//					log.info("扣除{}的押镖协助次数，剩余次数{}", jzId, bean.remainXZ);
//					xzJZSatrtYB.add(jzId);
//				}
//			}
//		}
//	}
	
	/**
	 * @Description:加入协助列表
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void jionHelp2YB(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("加入协助君主不存在");
			return;
		}
		Long jzid = jz.id;
		AnswerYaBiaoHelpReq.Builder req = (AnswerYaBiaoHelpReq.Builder) builder;
		long ybJzId = req.getJzId();
		log.info("君主--{}加入协助君主--{}", jzid,ybJzId);
		int code =10;//req.getCode();
		if (ybJzId <= 0) {
			log.error("{}加入协助目标不存在", jzid);
			return;
		}
//		if (code <= 0) {
//			log.error("{}加入协助编码--{}错误", jzid, code);
//			return;
//		}
		JunZhu ybJZ = HibernateUtil.find(JunZhu.class, ybJzId);
		AnswerYaBiaoHelpResp.Builder resp = AnswerYaBiaoHelpResp.newBuilder();
		if (jzid.equals(ybJzId)) {
			log.info("{}不能协助自己运镖{}", jzid, ybJzId);
			resp.setCode(70);
			resp.setName(ybJZ.name);
			session.write(resp.build());
			return;
		}
		AllianceBean aBean = AllianceMgr.inst.getAllianceByJunZid(jzid);
		if (aBean == null) {
			log.info("协助目标{}不在联盟，加入押镖协助失败", jzid);
			resp.setCode(30);
			resp.setName(ybJZ.name);
			session.write(resp.build());
			return;
		}

		AllianceBean askBean = AllianceMgr.inst.getAllianceByJunZid(ybJzId);
		if (askBean == null) {
			log.info("{}协助的目标{}没有联盟，答复押镖协助失败", jzid, ybJzId);
			resp.setCode(40);
			resp.setName(ybJZ.name);
			session.write(resp.build());
			return;
		}
		if(askBean.id!=aBean.id){
			log.info("{}协助的目标{}的联盟不是同一个，答复押镖协助失败", jzid, ybJzId);
			resp.setCode(40);
			resp.setName(ybJZ.name);
			session.write(resp.build());
			return;
		}
//		SessionUser su = SessionManager.inst.findByJunZhuId(ybJzId);
//		if(su==null){
//			log.info("{}协助的目标{}不在线，答复押镖协助失败", jzid, ybJzId);
//			resp.setCode(40);
//			resp.setName(ybJZ.name);
//			session.write(resp.build());
//			return;
//		}
		
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJzId);
		if (ybr == null) {
			log.info("{}协助的目标{}已经运镖，答复押镖协助失败", jzid, ybJzId);
			resp.setCode(40);
			resp.setName(ybJZ.name);
			session.write(resp.build());
			return;
		}
		//2015年12月12日 1.1版本协助次数完全没限制了
//		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jzid);
//		if (ybBean.remainXZ <= 0) {
//			log.info("{}加入协助押镖次数已用完，加入押镖协助失败", jzid, ybJzId);
//			resp.setCode(60);
//			resp.setName(ybJZ.name);
//			session.write(resp.build());
//			return;
//		}
		// 判断是否答复过
		List<Long> helperList = (List<Long>) answerHelpCache4YB.get(ybJzId);
		if (helperList != null && helperList.contains(jzid)) {
			log.info("{}已答复{}", jzid, ybJzId);
			resp.setCode(50);
			resp.setName(ybJZ.name);
			session.write(resp.build());
			return;
		} else {
			if (helperList == null) {
				helperList = new ArrayList<Long>();
			}
			// 存储答复队列
			helperList.add(jzid);
			answerHelpCache4YB.put(ybJzId, helperList);
		}

		if (code == 10) {
			// 保存协助者
			if (!saveXieZhuSet(ybJzId, jzid)) {
				resp.setCode(80);
				resp.setName(ybJZ.name);
				session.write(resp.build());
				return;
			}
		}
		// 答复协助的返回
		resp.setCode(10);
		resp.setName(jz.name);
		session.write(resp.build());

		AskYaBiaoHelpResp.Builder resp2Asker = AskYaBiaoHelpResp.newBuilder();
		resp2Asker.setCode(code);
		XieZhuJunZhu.Builder xzJz = XieZhuJunZhu.newBuilder();
		xzJz.setJzId(jzid);
		xzJz.setName(jz.name);
		xzJz.setRoleId(jz.roleId);
		
		//2015年8月31日返回盟友增加的护盾 2015年12月3日 1.1版本去掉护盾
//		int hudun=(int) (jz.shengMingMax * CanShu.YUNBIAOASSISTANCE_HPBONUS);
//		int mubiaoShengMingMax= JunZhuMgr.inst.getJunZhu(su.session).shengMingMax;
//		int hudunzenyi=hudun*100/mubiaoShengMingMax;
		xzJz.setAddHuDun(0);
		resp2Asker.setJz(xzJz);
//		log.info("通知{}协助这变化成功,君主--{}协助君主--{}押镖，护盾增益{}%", ybJzId,jzid,ybJzId,hudunzenyi);
		SessionUser su = SessionManager.inst.findByJunZhuId(ybJzId);
		if(su!=null){
			su.session.write(resp2Asker.build());
		}
	}

	/**
	 * @Description: 存储协助列表
	 * @param ybJzId
	 * @param xzJzId
	 */
	private boolean saveXieZhuSet(Long ybJzId, Long xzJzId) {
		HashSet<Long> xiezhuSet = xieZhuCache4YBJZ.get(ybJzId);
		if (xiezhuSet == null) {
			xiezhuSet = new HashSet<Long>();
		}
		if (xiezhuSet.size() >= XIEZHU_YABIAO_SIZE) {
			log.info("{}的协助者队列已满", ybJzId);
			return false;
		}
		log.info("{}进入{}的协助者队列", xzJzId, ybJzId);
		xiezhuSet.add(xzJzId);
		xieZhuCache4YBJZ.put(ybJzId, xiezhuSet);
		return true;
	}

	/**
	 * @Description: //踢除协助人
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void tichuHelper2YB(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("押镖君主不存在");
			return;
		}
		Long jzid = jz.id;
		TiChuYBHelpRsq.Builder req = (TiChuYBHelpRsq.Builder) builder;
		long tichuJzId = req.getJzId();
		if (tichuJzId <= 0) {
			log.error("踢除押镖协助君主失败，目标--{}不存在", tichuJzId);
		}
		removeXieZhu2Set(jzid, jz.name, tichuJzId, session);
	}

	/**
	 * @Description: 移除某个协助者
	 */
	private void removeXieZhu2Set(Long ybJzId, String name, Long xzJzId,IoSession session) {
		HashSet<Long> xiezhuSet = xieZhuCache4YBJZ.get(ybJzId);
		if (xiezhuSet == null) {
			log.info("{}的协助者队列为空，移除{}失败", ybJzId, xzJzId);
			return;
		}
		xiezhuSet.remove(xzJzId);
		xieZhuCache4YBJZ.put(ybJzId, xiezhuSet);
		session.write(PD.S_TICHU_YBHELP_RESP);
		// 踢出ybJzId的答复队列
		List<Long> helperList = (List<Long>) answerHelpCache4YB.get(ybJzId);
		if (helperList != null) {
			boolean a = helperList.remove(xzJzId);
			if (a) {
				log.info("从{}的答复队列踢出{}", ybJzId, xzJzId);
			}
			answerHelpCache4YB.put(ybJzId, helperList);
		}
		SessionUser su = SessionManager.inst.findByJunZhuId(xzJzId);
		if (su != null) {
			TiChuXieZhuResp.Builder resp = TiChuXieZhuResp.newBuilder();
			resp.setName(name);
			log.info("通知{}被踢出{}的协助队列", xzJzId, ybJzId);
			su.session.write(resp.build());
		} else {
			log.error("通知{}协助者变化失败，已下线", ybJzId);
		}
	}


	/**
	 * @Description: 广播镖车附加信息
	 * @param sc
	 * @param ybr
	 */
	public void broadBiaoCheInfo(Scene sc, YaBiaoRobot ybr) {
		Integer uid=(Integer)ybr.session.getAttribute(SessionAttKey.playerId_Scene);
		if(uid == null){
			log.error("广播马车信息出错：找不到君主{}马车的uid",ybr.jzId);
			return ;
		}
		int code=10;
		// 10押送中 20 战斗中 30 保护CD 40到达终点 50镖车摧毁
		BiaoCheState.Builder resp = BiaoCheState.newBuilder();
		resp.setState(code);
		resp.setUid(uid);
		resp.setJindu((int) (ybr.usedTime/ybr.totalTime));//进度（是一个百分比）
//		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, ybrobot.jzId);
//		if (ybBean == null) {
//			log.error("广播战斗状态失败，押镖人未开启押镖功能{}", ybrobot.jzId);
//			return;
//		}
//		resp.setHp(ybrobot.hp);
//		resp.setWorth(ybrobot.worth);
		int protectTime = ybr.protectTime
				- ((int) (System.currentTimeMillis() - ybr.endBattleTime) / 1000);
		resp.setBaohuCD(protectTime > 0 ? protectTime : 0);
		int jiaSuTime=ybr.upSpeedTime;
		resp.setJiasuTime(jiaSuTime);
		Integer scId = ybJzId2ScIdMap.get(ybr.jzId);
		if (scId == null) {
			log.error("镖车所在场景未找到{}", ybr.jzId);
			return;
		}
		for(Player player : sc.players.values()){
			//机器人马车不广播
			if(player.roleId==Scene.YBRobot_RoleId){
				continue;
			}
			player.session.write(resp.build());
		}
//		Set<Long> jbSet = BigSwitch.inst.ybMgr.jbJzList2ScIdMap.get(scId);
//		if (jbSet == null) {
//			log.info("场景{}中没有劫镖者，不广播", sc.name);
//			return;
//		}
//		for (Long jId : jbSet) {
//			SessionUser su = SessionManager.inst.findByJunZhuId(jId);
//			if (su != null) {
//				log.info("广播{}的镖车状态{}给{}", ybr.jzId, code, jId);
//				su.session.write(resp.build());
//			} else {
//				log.error("广播{}的镖车状态{}给{}失败，未找到session,劫镖者已下线", ybr.jzId,code, jId);
//			}
//		}
	}

	/**
	 * @Description: 押镖人加入押镖map
	 * @param jzId
	 * @param scId
	 */
	public void pushYbJz2Map(Long jzId, int scId) {
		// 存入押镖Map
		ybJzId2ScIdMap.put(jzId, scId);
		Set<Long> ybSet = new HashSet<Long>();
		if (ybJzList2ScIdMap.get(scId) != null) {
			ybSet = ybJzList2ScIdMap.get(scId);
		}
		ybSet.add(jzId);
		ybJzList2ScIdMap.put(scId, ybSet);
	}

	/**
	 * @Description:押镖人镖车移出押镖场景
	 */
	public void removeYaBiaoJzInfo(Long jzId,boolean isKill) {
		// 从jzList2Sc移除君主
		Integer scId = ybJzId2ScIdMap.get(jzId);
		if (scId == null)
			return;
		Set<Long> ybSet = ybJzList2ScIdMap.get(scId);
		if (ybSet != null) {
			boolean res = ybSet.remove(jzId);
			log.info("从场景ybJzList2ScIdMap List-{}移除君主-{}，更新押镖君主列表成功---OK?{}", scId, jzId, res);
			ybJzList2ScIdMap.put(scId, ybSet);
		} else {
			log.error("从场景-{}移除君主-{}失败,未找到押镖君主List", scId, jzId);
		}
		if(jzId<0){
			//处理系统镖车机器人
			removeSysRobotInfo2Sc(scId, jzId,isKill);
			log.info("从场景-{}移除系统镖车-{}成功", scId, jzId);
		}else{
			removeJzRobotInfo2Sc(scId, jzId,isKill);
			log.info("从场景-{}移除君主镖车-{}成功", scId, jzId);
		}
		
	}
	
	/**
	 * @Description 从场景移除君主镖车信息
	 * @param scId
	 * @param jzId
	 */
	protected void removeJzRobotInfo2Sc(Integer scId, Long jzId,boolean isKill) {
		//玩家镖车机器人
		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jzId);
		// 重置马车等数据 
		ybBean.horseType = -1;
		//重置  护盾 =》属性移到ybrobot中
//		ybr.hudun=0;
//		ybr.hudunMax=0;
		HibernateUtil.save(ybBean);
		log.info("从场景List-{}移除系统镖车-{}", scId, jzId);
		ybJzId2ScIdMap.remove(jzId);
		// 移除雇佣兵数据 策划更改玩法废除佣兵
//		PvpMgr.bingsCache4YB.remove(jzId);
//		ybNpcMap.remove(jzId);
		// 移除本次押镖答复的队列
		answerHelpCache4YB.remove(jzId);
		//更新受害人的仇人状态
		refreshYabiaoState2Shouhaizhe(jzId);
		Scene sc = yabiaoScenes.get(scId);
		// 移出君主镖车
		if (sc != null) {
			sc.exitForYaBiaoRobot(jzId,isKill);
		}
	}

	/**
	 * @Description 从场景中移除系统镖车信息
	 */
	protected void removeSysRobotInfo2Sc(Integer scId, Long jzId,boolean isKill) {
		log.info("从场景-{}移除系统镖车-{}", scId, jzId);
		ybJzId2ScIdMap.remove(jzId);
		Scene sc = yabiaoScenes.get(scId);
		// 移出押镖机器人
		if (sc != null) {
			sc.exitForYaBiaoRobot(jzId,isKill);
		}
	}

	/**
	 * @Description: 移除参加押镖的协助者
	 * @param ybjzId
	 */
	private void removeXieZhu4EndYB(String ybName, Long ybjzId, int gongxian,
			boolean isSuccess) {
		log.info("结束押镖，清除{}的押镖协助者", ybjzId);
		// 移除参加押镖的协助者
		HashSet<Long> xzSet = xieZhuCache4YBJZ.get(ybjzId);
		if (xzSet != null) {
			for (Long xzJzId : xzSet) {
				JunZhu jz = HibernateUtil.find(JunZhu.class, xzJzId);
				if (jz != null) {
				// 2015年11月27日改为发盟友快报
					sendKuaiBao2XieZhu(ybjzId,ybName,xzJzId,gongxian, isSuccess);
//					sendMail2XieZhu(ybName, jz.name, gongxian, isSuccess);
				}
				xzJZSatrtYB.remove(xzJzId);
			}
		}
		// 移除押镖君主的协助者队列
		xieZhuCache4YBJZ.remove(ybjzId);
	}
	
	/**
	 * @Description 	// 发送邮件给协助者 2015年11月27日改为发盟友快报
	 * @param ybJzId
	 * @param ybJzName
	 * @param jzId
	 * @param gongxian
	 * @param isSuccess
	 */
	public void sendKuaiBao2XieZhu(long  ybJzId, String ybJzName, long jzId,int gongxian,
			boolean isSuccess) {
		if (isSuccess) {
			//协助成功
			int eventId=SuBaoConstant.xzcg;
			PromptMsgMgr.inst.saveLMKBByCondition(jzId, ybJzId, new String[]{"", ybJzName}, eventId, 0);
			log.info("发送协助{}押镖成功的快报给君主--{}", ybJzId, jzId);
		} else {
			//协助失败
			int	eventId=SuBaoConstant.xzsb;
			PromptMsgMgr.inst.saveLMKBByCondition(jzId, ybJzId, new String[]{ybJzName, ""}, eventId, 0);
			log.info("发送协助{}押镖失败的快报给君主--{}", ybJzId, jzId);
		}
	}
	
	
	/**
	 * @Description //此方法是发送邮箱给协助者 2015年11月27日改为发盟友快报
	 * @param ybName
	 * @param jzName
	 * @param gongxian
	 * @param isSuccess
	 */
	protected void sendMail2XieZhu(String ybName, String jzName, int gongxian,
			boolean isSuccess) {
		String fuJian ="";
		if(gongxian>0){
			fuJian = "0:" + gongxianCODE + ":" + gongxian;
		}
		if (isSuccess) {
			Mail cfg = EmailMgr.INSTANCE.getMailConfig(50003);
			String content = cfg.content.replace("***", ybName);
			boolean ok = EmailMgr.INSTANCE.sendMail(jzName, content, fuJian,cfg.sender, cfg, "");
			log.info("发送协助押镖成功邮件给{},贡献--{}，OK?--{}", jzName, gongxian, ok);
		} else {
			Mail cfg = EmailMgr.INSTANCE.getMailConfig(50004);
			String content = cfg.content.replace("***", ybName);
			boolean ok = EmailMgr.INSTANCE.sendMail(jzName, content, fuJian,cfg.sender, cfg, "");
			log.info("发送协助押镖失败邮件给{},贡献--{}，OK?--{}", jzName, gongxian, ok);
		}
	}

	/**
	 * @Description: //劫镖人加入押镖map
	 * @param jzId
	 * @param scId
	 */
//	public void pushJbJz2Map(Long jzId, int scId) {
//		// 存入劫镖Map
//		jbJz2ScIdMap.put(jzId, scId);
//		Set<Long> jbSet = new HashSet<Long>();
//		if (jbJzList2ScIdMap.get(scId) != null) {
//			jbSet = jbJzList2ScIdMap.get(scId);
//			log.info("场景已存储劫镖人数-{}", jbSet.size());
//		}
//		jbSet.add(jzId);
//		jbJzList2ScIdMap.put(scId, jbSet);
//	}

	/**
	 * @Description: //劫镖人移出劫镖map
	 * @param jzId
	 */
//	public void removeJbJz2Map(Long jzId) {
//		// 从ybJzList2ScIdMap移除君主
//		Integer scId = jbJz2ScIdMap.get(jzId);
//		if (scId == null)
//			return;
//		Set<Long> jbSet = jbJzList2ScIdMap.get(scId);
//		if (jbSet != null) {
//			boolean res = jbSet.remove(jzId);
//			jbJzList2ScIdMap.put(scId, jbSet);
//			log.info("从场景-{}移除君主-{}，更新押镖君主列表成功?{}", scId, jzId, res);
//		} else {
//			log.error("从场景-{}移除君主-{}失败,未找到押镖君主List", scId, jzId);
//		}
//		jbJz2ScIdMap.remove(jzId);
//		log.info("从场景-{}移除君主-{}成功", scId, jzId);
//	}
	
	/**
	 * @Description 初始化君主押镖数据
	 * @param jzId
	 * @param vipLevel
	 * @return
	 */
	private YaBiaoBean initYaBiaoBeanInfo(Long jzId, int vipLevel) {
		// 获取数据库中是否有此记录，有的话什么也不做
		log.info("初始化{}的押镖数据，vip等级-{}", jzId, vipLevel);
		YaBiaoBean bean = new YaBiaoBean();
		bean.junZhuId = jzId;
		bean.usedYB = 0;
		bean.remainYB = getYaBiaoCountForVip(vipLevel);
		bean.usedAskXZ = 0;
		bean.remainAskXZ = getAskXiezhuCountForVip(vipLevel);
		bean.lastShowTime = null;
		bean.horseType = -1;// 随机一匹马 1 2 3 4 5 （-1 0表示没有马）
		bean.isNew4History=false;
		bean.isNew4Enemy=false;
		bean.todayBuyYBTimes = 0;
		MC.add(bean, jzId);
		HibernateUtil.insert(bean);
		log.info("玩家id是 ：{}的 押镖数据库记录YaBiaoInfo生成成功", jzId);
		return bean;
	}

	public YaBiaoBean resetybBean(YaBiaoBean bean, int vipLevel) {
		// 如果没有马随机一匹马
		if (bean.horseType == 0) {
			int hType = getRandomCart();// 随机一匹马 1 2 3 4 5 （0表示没有马）
			if (hType > 5) {
				hType = 5;
			}
			bean.horseType = hType;
			HibernateUtil.save(bean);
		} else {
			if (bean.horseType>0&&bean.horseType > 5) {
				bean.horseType = 5;
				HibernateUtil.save(bean);
			}
		}
		if (DateUtils.isTimeToReset(bean.lastShowTime, CanShu.REFRESHTIME_PURCHASE)) {
			log.info("新的一天，重置用户押镖数据--君主ID--{}", bean.junZhuId);
			bean.usedYB = 0;
			bean.remainYB = getYaBiaoCountForVip(vipLevel);
			bean.usedAskXZ = 0;
			bean.remainAskXZ = getAskXiezhuCountForVip(vipLevel);
			bean.todayBuyYBTimes = 0;
			bean.lastShowTime = new Date();
			HibernateUtil.save(bean);
		}else{
			// do nothing
			log.info("还是今天，不重置用户押镖数据--君主ID--{}", bean.junZhuId);
		}
		return bean;
	}
	//初始化押镖战斗数据
	/**
	 * @Description 初始化君主押镖数据
	 * @param jzId
	 * @param vipLevel
	 * @return
	 */
	private YBBattleBean initYBBattleInfo(Long jzId, int vipLevel) {
		// 获取数据库中是否有此记录，有的话什么也不做
		log.info("初始化{}的押镖数据，vip等级-{}", jzId, vipLevel);
		YBBattleBean bean = new YBBattleBean();
		bean.jzId = jzId;
		bean.zuheId=-1;
		bean.remainJB4Award = getJieBiaoCountForVip(vipLevel);
	
		bean.lastJBDate = null;
		MC.add(bean, jzId);
		HibernateUtil.insert(bean);
		log.info("玩家id是 ：{}的 押镖数据库记录YaBiaoInfo生成成功", jzId);
		return bean;
	}
	
	/**
	 * @Description 重置押镖相关的战斗数据
	 * @param zdbean
	 * @param vipLevel
	 * @return
	 */
	public YBBattleBean resetYBBattleInfo(YBBattleBean zdbean, int vipLevel) {
		if (DateUtils.isTimeToReset(zdbean.lastResetTime, CanShu.REFRESHTIME_PURCHASE)) {
			log.info("新的一天，重置用户押镖杀戮数据--君主ID--{}", zdbean.jzId);
			zdbean.count4kill = 0;
			zdbean.usedJB = 0;
			zdbean.remainJB4Award = getJieBiaoCountForVip(vipLevel);
			//2015年12月4日加入满血复活次数
			zdbean.fuhuo4uesd = 0;
			zdbean.buyfuhuo4Vip = 0;
			zdbean.fuhuoTimes4Vip = 0;
			zdbean.xueping4uesd=0;
			
			HibernateUtil.save(zdbean);
		}else{
			// do nothing
			log.info("还是今天，不重置用户押镖战斗数据--君主ID--{}", zdbean.jzId);
		}
		return zdbean;
	}
	/**
	 * @Description 判断杀死的是不是仇人并领取奖励 
	 * @param jz
	 * @param deadJz
	 * @return
	 */
	public boolean isEnemy4Award(JunZhu jz,JunZhu deadJz) {
		boolean isEnemy=	isEmeny(jz.id, deadJz.id);
		if(!isEnemy){
			return false;
		}
		getAward4killEnemy(jz, deadJz);
		return true;
	}
	/**
	 * @Description 砍死仇人领奖
	 * @param jzId 杀人的君主ID
	 * @param enemyJzId 被杀的君主ID
	 */
	public void getAward4killEnemy(JunZhu jz,JunZhu otherJz) {
		long jzId=jz.id;
		long enemyJzId=otherJz.id;
		String enemyName=otherJz.name;
		YBBattleBean zdBean = getYBZhanDouInfo(jzId, jz.vipLevel);
		if(zdBean.count4kill>=YunbiaoTemp.rewarding_killFoe_max){
			log.info("{}结算击杀仇人{}奖励完成，（有奖励）杀死仇人 达到上限 ",jzId,  enemyJzId);
			return;
		}
		zdBean.count4kill+=1;
		HibernateUtil.save(zdBean);
		int horseType=0;
		int eventId=SuBaoConstant.jscr;
		PromptMSG msg=	PromptMsgMgr.inst.saveLMKBByCondition(jzId, enemyJzId, new String[]{jz.name, enemyName}, 
			 eventId, horseType);
		String	award=msg!=null?msg.award:"";
		log.info("{}杀死了仇人{},获得奖励{}",jzId,enemyJzId,award);
	}
	/**
	 * @Description  战斗结束进行战斗数据的更新
	 * @param ybJzId 押镖君主
	 * @param session
	 */
	public void settleJieBiaoResult(long ybJzId,  IoSession session) {
		JunZhu dajieJz = JunZhuMgr.inst.getJunZhu(session);
		if (dajieJz == null) {
			log.error("劫镖出错：劫镖君主不存在");
			return;
		}
		long djJzId = dajieJz.id;
		
		if(ybJzId<0){
			log.info("{}劫镖目标君主{}为空，尝试打劫系统机器人结算",djJzId,ybJzId);
			gainAward4XitongBiaoChe(ybJzId, dajieJz,session);
			return;
		}
		JunZhu yabiaoJz = HibernateUtil.find(JunZhu.class, ybJzId);
		if (yabiaoJz == null) {
			log.error("劫镖出错：目标君主不存在");
			return;
		}
		//结算时劫镖人的数据从YBBattleBean获取
//		YaBiaoBean dajieBean = HibernateUtil.find(YaBiaoBean.class, djJzId);
//		if (dajieBean == null) {
//			log.error("劫镖战斗结束相关处理出错：劫镖者{}无押镖活动记录", djJzId);
//			return;
//		} else {
//			// 是否重新设置数据
//			resetybBean(dajieBean, dajieJz.vipLevel);
//		}
		//打劫君主的押镖战斗数据
		YBBattleBean jbBattleBean = getYBZhanDouInfo(dajieJz.id, dajieJz.vipLevel);
		//押镖君主的押镖战斗数据
		YBBattleBean ybBattleBean = getYBZhanDouInfo(dajieJz.id, dajieJz.vipLevel);
		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, ybJzId);
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJzId);
		if(ybr==null){
			log.error("劫镖出错：目标{}镖车不存在",ybJzId);
			return;
		}
		if (ybBean != null) {
			// ===================================以下劫镖成功，劫镖人结算===================================
			CartTemp cart = cartMap.get(ybBean.horseType);
			if(cart==null){
				log.error("劫镖出错：目标{}镖车马匹类型{}为找到CartTemp配置",ybJzId,ybBean.horseType);
				return;
			}
			//根据时段收益增益
			int dajieshouyi = (int) ((ybr.worth * cart.robProfit)*SHOUYI_PROFIT/100);
			//2015年9月1日拆分字段到新表
			YunBiaoHistory ybHis = HibernateUtil.find(YunBiaoHistory.class, dajieJz.id);
			ybHis.successJB += 1;
			HibernateUtil.save(ybHis);
			jbBattleBean.usedJB+=1;
			if(jbBattleBean.remainJB4Award>0){
				jbBattleBean.remainJB4Award-=1;
			}
			HibernateUtil.save(jbBattleBean);
//			HibernateUtil.save(dajieBean);
//			dajieJz.tongBi += dajieshouyi;
//			HibernateUtil.save(dajieJz);
//			JunZhuMgr.inst.sendMainInfo(session);// 推送铜币信息
//			BattleResult.Builder resp = BattleResult.newBuilder();
//			resp.setExp(0);
//			resp.setMoney(dajieshouyi);
//			session.write(resp.build());
			log.info("劫镖者{}劫镖成功，收益{},劫镖次数，剩余次数{}", dajieJz.id, dajieshouyi,jbBattleBean.usedJB,jbBattleBean.remainJB4Award);
			ActLog.log.LootDart(dajieJz.id, dajieJz.name, ActLog.vopenid, ActLog.vopenid, ybJzId, "", dajieshouyi, ybHis.successJB);
			// 劫镖成功，加入劫镖人的记录
			saveYaBiaoHistory(dajieJz, yabiaoJz, 3, dajieshouyi, ybr.horseType);
			log.info("劫镖者{}劫镖{}结果OK？{}记录", djJzId, ybJzId, true);
			// ===================================以下劫镖成功，运镖失败，押镖人结算===================================
			//押镖结算
			int shouru=0;
			if(ybBattleBean.baodi>0){
				MaJu baodi=majuMap.get(ybBattleBean.baodi);
				if(baodi==null){
					shouru = (int) (ybr.worth *1*SHOUYI_PROFIT);
					log.error("君主{}有保底收益道具,但是未找到保底道具配置收益,保底收益率默认成--{}",ybJzId,1);
				}else{
					double baodilv=baodi.value1/100;//Integer.valueOf(baodi.function)/100 ;//保底收益率
					shouru = (int) (ybr.worth * baodilv*SHOUYI_PROFIT);
					log.info("君主{}有保底收益道具，保底收益率--{}",ybJzId,baodilv);
				}
			}else{
				shouru = (int) (ybr.worth * cart.failProfit*SHOUYI_PROFIT);
			}
//			2015年10月8日押镖君主收益应该在邮件领取
//			otherJz.tongBi += shouru;
			HibernateUtil.save(yabiaoJz);
			ybBean.horseType = -1;
			//重置道具
			ybBean.usedYB += 1;
			ybBean.remainYB -= 1;
			ybBean.isNew4History=true;
			ybBean.isNew4Enemy=true;
			HibernateUtil.save(ybBean);
			ybBattleBean.baohu = 0;
			ybBattleBean.baodi = 0;
			ybBattleBean.jiasu = 0;
			HibernateUtil.save(ybBattleBean);
			// 劫镖成功，加入被劫镖者仇人列表 加入劫镖人的记录
			log.info("劫镖者{}加入{}的仇人列表", djJzId, ybJzId);
			saveJieBiaoEnemy(ybJzId,djJzId);
			
			
			// 劫镖成功，加入被劫镖者的记录
			saveYaBiaoHistory(yabiaoJz, dajieJz, 4, dajieshouyi, ybr.horseType);
			//结算 国家仇恨 2015年11月25日玩法变更 可能再改回来，暂时废弃废弃废弃废弃废弃废弃 FIXME
//			updateCountryHate(dajieJz.guoJiaId,yabiaoJz.guoJiaId);
			//以下奖励通过快报发送--废弃废弃
//			SessionUser ybjzSu = SessionManager.inst.findByJunZhuId(ybJzId);
//			if (ybjzSu != null) {
//				JunZhuMgr.inst.sendMainInfo(ybjzSu.session);// 推送铜币信息
//			} else {
//				log.info("推送押镖失败消息给{}失败，未找到session，已下线", djJzId);
//			}
//			//以上奖励通过快报发送--废弃废弃
//			if (ybr != null) {
				// 广播镖车摧毁
//				broadBattleEvent(sc, ybr, 50);
//			}
			
			// 移除押镖者
			removeYaBiaoJzInfo(ybJzId,true);
			
			//发送押镖失败快报给押镖人  2015年11月27日改成发快报 不发邮件
			sendLoseKuaiBao2YBJZ(ybJzId, djJzId, dajieJz.name,shouru);
			//移除协助者并发送
			removeXieZhu4EndYB(yabiaoJz.name, ybJzId,CanShu.YUNBIAOASSISTANCE_GAIN_FAIL, false);
			//推送新历史记录给劫镖者 2015年9月14日1.0自身主动攻击的记录红点不允许推到一级界面按钮上
//			pushYBRecord(jId, true, false);
			//推送新历史记录给押镖者
			pushYBRecord(ybJzId, true, true);
			//押镖镖车从场景中移除事件
			EventMgr.addEvent(ED.BIAOCHE_END, new Object[] { ybJzId,ybBean.horseType, ybr.startTime});
			EventMgr.addEvent(ED.BIAOCHE_CUIHUI, new Object[] { yabiaoJz,ybBean.horseType, ybr.startTime});
		} else {
			log.error("未找到被劫镖者{}，结算失败", ybJzId);
		}
	}
	
	/**
	 * @Description 杀死系统镖车得到奖励
	 * @param ybJzId
	 * @param dajieJz
	 * @param session
	 */
	public void gainAward4XitongBiaoChe(long ybJzId,JunZhu dajieJz,IoSession session) {
		long djJzId=dajieJz.id;
		//处理系统镖车机器人
		log.info("{}打劫系统机器人--{}领奖--开始",djJzId, ybJzId);
		//打劫君主的押镖战斗数据
		YBBattleBean zdBean4DJ = getYBZhanDouInfo(dajieJz.id, dajieJz.vipLevel);
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJzId);
		if (ybr == null) {
			log.error("劫镖战斗结束相关处理出错：为找到系统jzid--{}的马车", djJzId);
			return;
		}
		CartTemp cart = cartMap.get(ybr.horseType);
		if(cart==null){
			log.error("劫镖出错：目标{}系统镖车马匹类型{}为找到CartTemp配置",ybJzId,ybr.horseType);
			return;
		}
		//根据时段收益增益
		int dajieshouyi = (int) ((ybr.worth * cart.robProfit)*SHOUYI_PROFIT/100);
		//2015年9月1日拆分字段到新表
		YunBiaoHistory ybHis = HibernateUtil.find(YunBiaoHistory.class, dajieJz.id);
		ybHis.successJB += 1;
		HibernateUtil.save(ybHis);
		zdBean4DJ.usedJB+=1;
		zdBean4DJ.remainJB4Award-=1;
		HibernateUtil.save(zdBean4DJ);
		log.info("劫镖者{}劫镖成功，收益{},劫镖次数{}，剩余次数{}", dajieJz.id, dajieshouyi,zdBean4DJ.usedJB,zdBean4DJ.remainJB4Award);
		ActLog.log.LootDart(dajieJz.id, dajieJz.name, ActLog.vopenid, ActLog.vopenid, ybJzId, "", dajieshouyi, ybHis.successJB);
		//劫镖成功，加入劫镖人的记录
		saveYaBiaoHistory4SysCart(dajieJz, ybr, 3, dajieshouyi, ybr.horseType);
		log.info("劫镖者{}劫镖{}结果OK？{}记录", djJzId, ybJzId, true);

		// 移除押镖者
		removeYaBiaoJzInfo(ybJzId,true);

		log.info("劫镖者{}劫镖成功，收益{}", dajieJz.id, dajieshouyi);
		ActLog.log.LootDart(dajieJz.id, dajieJz.name, ActLog.vopenid, ActLog.vopenid, ybJzId, "", dajieshouyi, ybHis.successJB);

	}

	/**
	 * @Description 结算国家仇恨
	 * @param jz
	 * @param yabiaoJz
	 */
	public void updateCountryHate(int jiebiaoGuoJiaId, int yabiaoGuoJiaId) {
		if(jiebiaoGuoJiaId==yabiaoGuoJiaId){
			return;
		}
		GuoJiaBean gjBean = HibernateUtil.find(GuoJiaBean.class, yabiaoGuoJiaId);
		if (gjBean == null) {
			gjBean=GuoJiaMgr.inst.initGuoJiaBeanInfo(yabiaoGuoJiaId);
		}
		synchronized (gjBean) {
			switch (jiebiaoGuoJiaId) {
			case 1:
				gjBean.hate_1+=1;
				break;
			case 2:
				gjBean.hate_2+=1;
				break;
			case 3:
				gjBean.hate_3+=1;
				break;
			case 4:
				gjBean.hate_4+=1;
				break;
			case 5:
				gjBean.hate_5+=1;
				break;
			case 6:
				gjBean.hate_6+=1;
				break;
			case 7:
				gjBean.hate_7+=1;
				break;
			default:
				log.error("敌方国家{}编码错误，仇恨增加失败",jiebiaoGuoJiaId); 
				break;
			}
			HibernateUtil.save(gjBean);
		}
	}

	/**
	 * @Description: 存储押镖记录
	 * @param jzId 君主ID
	 * @param enemyId  敌人ID
	 * @param result 1：表示打劫别人未成功 2：被打劫成功击退敌人 3表示：表示成功打劫别人 4：表示被成功打劫
	 * @param shouyi 收益
	 */
	private void saveYaBiaoHistory(JunZhu jz, JunZhu enemy, int result,
			int shouyi, int horseType) {
		YaBiaoHistory yaBiaoH = new YaBiaoHistory();
		yaBiaoH.junzhuId = jz.id;
		int jzzhanli = JunZhuMgr.inst.getJunZhuZhanliFinally(jz);
		yaBiaoH.junzhuZhanLi = jzzhanli;
		yaBiaoH.junzhuLevel = jz.level;
		yaBiaoH.enemyId = enemy.id;
		yaBiaoH.enemyName = enemy.name;
		AllianceBean enall = AllianceMgr.inst.getAllianceByJunZid(enemy.id);
		yaBiaoH.enemyLianMengName = enall == null ? "" : enall.name;
		int enemyzhanli = JunZhuMgr.inst.getJunZhuZhanliFinally(enemy);
		yaBiaoH.enemyZhanLi = enemyzhanli;
		yaBiaoH.enemyLevel = enemy.level;
		yaBiaoH.battleTime = new Date();
		yaBiaoH.shouyi = shouyi;
		yaBiaoH.result = result;
		yaBiaoH.enemyRoleId = enemy.roleId;
		yaBiaoH.horseType = horseType;
		Long sizeAfterAdd = DB.rpush4JieBiao((HISTORY_KEY + jz.id).getBytes(),
				SerializeUtil.serialize(yaBiaoH));
		if (sizeAfterAdd > historySize) {
			Redis.getInstance().lpop(HISTORY_KEY + jz.id);
		}
	}
	
	/**
	 * @Description 保存打劫系统马车的历史记录
	 * @param jz
	 * @param ybr
	 * @param result
	 * @param shouyi
	 * @param horseType
	 */
	private void saveYaBiaoHistory4SysCart(JunZhu jz, YaBiaoRobot ybr, int result,
			int shouyi, int horseType) {
		YaBiaoHistory yaBiaoH = new YaBiaoHistory();
		yaBiaoH.junzhuId = jz.id;
		int jzzhanli = JunZhuMgr.inst.getJunZhuZhanliFinally(jz);
		yaBiaoH.junzhuZhanLi = jzzhanli;
		yaBiaoH.junzhuLevel = jz.level;
		yaBiaoH.enemyId = ybr.jzId;
		yaBiaoH.enemyName = ybr.name;
		yaBiaoH.enemyLianMengName =  "***";
		yaBiaoH.enemyZhanLi = ybr.zhanli;
		yaBiaoH.enemyLevel = ybr.jzLevel;
		yaBiaoH.battleTime = new Date();
		yaBiaoH.shouyi = shouyi;
		yaBiaoH.result = result;
		CartNPCTemp cnt=biaoCheNpcMap.get(ybr.bcNPCId);
		//TODO roleId
		int roleId=cnt==null?1:2;//cnt.roleId;
		yaBiaoH.enemyRoleId = roleId;
		yaBiaoH.horseType = horseType;
		Long sizeAfterAdd = DB.rpush4JieBiao((HISTORY_KEY + jz.id).getBytes(),
				SerializeUtil.serialize(yaBiaoH));
		if (sizeAfterAdd > historySize) {
			Redis.getInstance().lpop(HISTORY_KEY + jz.id);
		}
	}

//	private void initHuDun4YBJZ(Long jzid,YaBiaoRobot ybr) {
//		HashSet<Long> xiezhuSet = xieZhuCache4YBJZ.get(jzid);
//		int hudun = 0;
//		if (xiezhuSet != null) {
//			for (Long xzJzId : xiezhuSet) {
//				JunZhu jz = HibernateUtil.find(JunZhu.class, xzJzId);
//				if (jz == null)
//					continue;
//				hudun += jz.shengMingMax * CanShu.YUNBIAOASSISTANCE_HPBONUS;
//			}
//		}
////		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jzid);
//		log.info("生成君主{}的护盾---血量{}",jzid,hudun);
//		ybr.hudun = hudun;
//		ybr.hudunMax = hudun;
////		HibernateUtil.save(ybBean);
//	}

	/**
	 * @Description: 存储仇人
	 * @param jzId 押镖君主ID
	 * @param enemyId 劫镖者ID
	 */
	private void saveJieBiaoEnemy(long jzId, long enemyId) {
		// Enemy enemy=new Enemy();
		// enemy.junzhuId=jzId;
		// enemy.enemyId=enemyId;
		log.info("{}成为{} 的仇人-------------------------",enemyId,jzId);
		boolean IsEnemy = isEmeny(jzId, enemyId);
		if (!IsEnemy) {
			Long sizeAfterAdd = DB.rpush4YaBiao((ENEMY_KEY + jzId), enemyId
					+ "");
			if (sizeAfterAdd > enemySize) {
				Redis.getInstance().lpop(ENEMY_KEY + jzId);
			}
		}
	}


	/**
	 * @Description: 押镖成功结算
	 * @param jzId
	 * @param roomId
	 */
	public void settleYaBiaoSuccess(Long jzId, int roomId) {
		if(jzId<0){
			//处理系统镖车机器人
			log.info("系统机器人---《{}》到达终点处理--开始", jzId);
			YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(jzId);
			if (ybr != null) {
				log.info("系统机器人---押镖成功,开始时间为{}，结束时间为{}", ybr.startTime,System.currentTimeMillis());
//				Scene sc4Sys = yabiaoScenes.get(roomId);
				// 广播押镖结束
//				broadBattleEvent(sc4Sys, ybr, 40);
				// 移除押镖人的相关信息
				removeYaBiaoJzInfo(jzId,false);
			}
			log.info("系统机器人---{}到达终点处理--结束", jzId);
			return;
		}
		JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
		if (jz == null) {
			log.error("押镖结算出错：押镖君主不存在{}", jzId);
			return;
		}
		SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
//		Scene sc = yabiaoScenes.get(roomId);
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(jz.id);
		if (ybr != null) {
			log.info("君主{}押镖成功",jz.id);
			// 广播押镖结束
//			broadBattleEvent(sc, ybr, 40);
		}else{
			log.error("押镖结算出错：押镖君主--{}镖车机器人未找到", jzId);
			return;
		}
		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jzId);

		// 结算收益
		//		CartTemp cart = cartMap.get(ybBean.horseType);
//		int shouru = ybr.worth ;
//		2015年10月8日押镖君主收益应该在邮件领取
//		jz.tongBi += shouru;
//		HibernateUtil.save(jz);
		final int horseType = ybBean.horseType;
		// 重置数据
//		ybBean.worth = 0;
		ybBean.horseType = -1;
		//重置道具
	
		ybBean.usedYB += 1;
		ybBean.remainYB -= 1;
		//2015年9月1日拆分字段到新表
		YunBiaoHistory ybHis =getYunBiaoHistory( jz.id);
		ybHis.successYB += 1;
		HibernateUtil.save(ybHis);
		log.info("{}押镖成功,现在的成功押镖次数为{}", jz.id, ybHis.successYB);
		HibernateUtil.save(ybBean);
		YBBattleBean zdBean = getYBZhanDouInfo(jzId, jz.vipLevel);
		zdBean.baohu = 0;
		zdBean.baodi = 0;
		zdBean.jiasu = 0;
		HibernateUtil.save(zdBean);
		if (su != null) {
			JunZhuMgr.inst.sendMainInfo(su.session);// 推送铜币信息
		} else {
			log.info("推送押镖成功消息给{}失败，未找到session，已下线", jz.id);
		}
		EventMgr.addEvent(ED.YA_BIAO_SUCCESS, new Object[]{jz,horseType});
		// 移除押镖人的相关信息、
		removeYaBiaoJzInfo(jz.id,false);
		//押镖镖车从场景中移除事件 
		EventMgr.addEvent(ED.BIAOCHE_END, new Object[] { jzId,ybr.horseType, ybr.startTime});
		// 移出协助者
		removeXieZhu4EndYB(jz.name, jz.id, CanShu.YUNBIAOASSISTANCE_GAIN_SUCCEED, true);
		sendSuccessKuaiBao2YBJZ(jzId);
	}

	/**
	 * @Description: //广播镖车进入场景
	 * @param ybr
	 * @param jz
	 * @param sc
	 */
//	public void broadBiaoCheINScene(YaBiaoRobot ybr, JunZhu jz, Scene sc) {
//		YabiaoJunZhuInfo.Builder resp = YabiaoJunZhuInfo.newBuilder();
//		resp.setJunZhuId(jz.id);
//		resp.setJunZhuName(jz.name);
//		resp.setLevel(jz.level);
//		AllianceBean ybabean = AllianceMgr.inst.getAllianceByJunZid(jz.id);
//		resp.setLianMengName(ybabean == null ? "" : ybabean.name);
//		int protectTime = ybr.protectTime
//				- ((int) (System.currentTimeMillis() - ybr.endBattleTime) / 1000);
//		resp.setBaohuCD(protectTime > 0 ? protectTime : 0);
//		resp.setTotalTime(ybr.totalTime);
//		resp.setUsedTime(ybr.usedTime);
////		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, ybr.jzId);
//		resp.setHp(ybr.hp);
//		resp.setMaxHp(jz.shengMingMax);
//		resp.setWorth(ybr.worth);
//		resp.setMaxWorth(ybr.worth);
//		resp.setState(ybr.isBattle ? 20 : (protectTime > 0 ? 30 : 10));
//		// 10押送中 20 战斗中 30保护CD
//		int zhanli = JunZhuMgr.inst.getJunZhuZhanliFinally(jz);
//		resp.setZhanLi(zhanli);
//		resp.setState(ybr.isBattle ? 20 : (protectTime > 0 ? 30 : 10));
//		// 10押送中20战斗中 30保护CD
//		Integer scId =ybJzId2ScIdMap.get(ybr.jzId);
//		if (scId == null) {
//			log.error("镖车所在场景未找到{},不广播", ybr.jzId);
//			return;
//		}
//		resp.setPathId(ybr.pathId);
//
//		Set<Long> jbSet = BigSwitch.inst.ybMgr.jbJzList2ScIdMap.get(scId);
//		if (jbSet == null) {
//			return;
//		}
//		resp.setHorseType(ybr.horseType);
//		resp.setJunzhuGuojia(jz.guoJiaId);
//		
//		//2015年8月31日返回盟友增加的护盾
//		resp.setHuDun(ybr.hudun*100/jz.shengMingMax);
//		for (Long jId : jbSet) {
//			SessionUser su = SessionManager.inst.findByJunZhuId(jId);
//			if (su == null)
//				continue;
//			boolean IsEnemy = DB.lexist((ENEMY_KEY + jId), jz.id + "");
//			resp.setIsEnemy(IsEnemy);
//			su.session.write(resp.build());
//		}
//
//	}

	/**
	 * @Description: 发送押镖成功快报给押镖人  2015年11月27日改成发快报 不发邮件
	 * @param jzName
	 */
	public void sendSuccessKuaiBao2YBJZ(long ybJzId) {
		//eventId 8=押镖成功
		int eventId=SuBaoConstant.ybcg;
		PromptMsgMgr.inst.saveLMKBByCondition(ybJzId, ybJzId, null,  eventId,  0);
		log.info("发送押镖成功快报给--{}",  ybJzId);
	}
	/**
	 * @Description: 发送押镖失败快报给押镖人  2015年11月27日改成发快报 不发邮件
	 * @param jzName
	 */
	public void sendLoseKuaiBao2YBJZ(long ybJzId,long jbJzId,String jbJzName,int ybShouyi) {
		 int eventId=SuBaoConstant.ybsb ;//运镖失败
		PromptMsgMgr.inst.saveLMKBByCondition(ybJzId, jbJzId, new String[]{"",jbJzName}, eventId,  0);
		log.info("发送押镖成功快报给--{}",  ybJzId);
	}
//	/**
//	 * @Description: 发送押镖成功邮件给押镖人
//	 * @param jzName
//	 */
//	public void sendSuccessMail2YaBiaoRen(String jzName, int shouru) {
//		Mail cfg = EmailMgr.INSTANCE.getMailConfig(50001);
//		String content = cfg.content;
//		String fuJian = "0:" + tongbiCODE + ":" + shouru;
//		boolean ok = EmailMgr.INSTANCE.sendMail(jzName, content, fuJian,cfg.sender, cfg, "");
//		log.info("发送押镖成功邮件给{},OK--{}", jzName, ok);
//	}

//	/**
//	 * @Description: 发送押镖失败邮件给押镖人
//	 * @param ybjzName 押镖君主,jbjzName 劫镖君主
//	 */
//	public void sendFailMail2YaBiaoRen(String ybjzName, String jbjzName,int shouru) {
//		Mail cfg = EmailMgr.INSTANCE.getMailConfig(50002);
//		//增加劫镖人信息
//		String content = cfg.content.replace("***", jbjzName);
//		String fuJian = "0:" + tongbiCODE + ":" + shouru;
//		boolean ok = EmailMgr.INSTANCE.sendMail(ybjzName, content, fuJian,cfg.sender, cfg, "");
//		log.info("发送押镖失败邮件给{},OK--{}", ybjzName, ok);
//	}

	/**
	 * @Description: 根据vip等级获取押镖次数
	 * @param vipLev
	 * @return
	 */
	public int getYaBiaoCountForVip(int vipLev) {
		// int value = VipMgr.INSTANCE.getValueByVipLevel(vipLev,
		// VipData.bugYaBiaoTime);
		// int times = PVPConstant.YABIAO_TOTAL_TIMES;
		// times +=PurchaseMgr.inst.getAllUseNumbers(PurchaseConstants.YABIAO,
		// value);
		return CanShu.YUNBIAO_MAXNUM;
	}

//	/**
//	 * @Description: 根据vip等级获取协助次数
//	 * @param vipLev
//	 * @return
//	 */
//	public int getXiezhuCountForVip(int vipLev) {
//		// int value = VipMgr.INSTANCE.getValueByVipLevel(vipLev,
//		// VipData.bugYaBiaoTime);
//		// int times = PVPConstant.YABIAO_TOTAL_TIMES;
//		// times +=PurchaseMgr.inst.getAllUseNumbers(PurchaseConstants.YABIAO,
//		// value);
//		return CanShu.YUNBIAOASSISTANCE_INVITEDMAXNUM;
//	}
//	/**
//	 * @Description: 根据vip等级获取可买满血复活次数
//	 * @param vipLev
//	 * @return
//	 */
//	public int getFuhuoTimesForVip(int vipLev) {
//		// int value = VipMgr.INSTANCE.getValueByVipLevel(vipLev,
//		// VipData.bugYaBiaoTime);
//		// int times = PVPConstant.YABIAO_TOTAL_TIMES;
//		// times +=PurchaseMgr.inst.getAllUseNumbers(PurchaseConstants.YABIAO,
//		// value);
//		return  VipMgr.INSTANCE.getValueByVipLevel(vipLev, VipData.buy_ybmanxue_times);
//	}

	/**
	 * @Description: 根据vip等级获取请求协助次数
	 * @param vipLev
	 * @return
	 */
	public int getAskXiezhuCountForVip(int vipLev) {
		return VipMgr.INSTANCE.getValueByVipLevel(vipLev, VipData.askHelpTimes);
	}

	public void buyCounts4YaBiao(int id, Builder builder, IoSession session) {
		BuyCountsReq.Builder req = (BuyCountsReq.Builder) builder;
		int type = req.getType();
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("够买押镖次数千军出错：君主不存在");
			return;
		}
		switch (type) {
		case 10:
			buyYaBiaoCount(jz, session);
			break;
		case 20:
			log.error("{}请求购买参数错误{}，1.1版本不能购买劫镖次数", jz.id, type);
//			buyJieBiaoCount(jz, session);
			break;
		default:
			log.error("{}请求购买参数错误{}", jz.id, type);
			break;
		}
	}

	public void buyYaBiaoCount(JunZhu jz, IoSession session) {
		YaBiaoBean bean = getYaBiaoBean(jz.id, jz.vipLevel);
		BuyCountsResp.Builder resp = BuyCountsResp.newBuilder();
		int vipLev = jz.vipLevel;
		int buyYaBiaoCounts = VipMgr.INSTANCE.getValueByVipLevel(vipLev,
				VipData.yabiaoTimes);
		if (bean.todayBuyYBTimes >= buyYaBiaoCounts) {
			log.info("{}够买押镖次数已用完", jz.id);
			resp.setResult(30);
			resp.setLeftYBTimes(bean.remainYB);
			return;
		}
		// 获取够买配置
		Purchase pc = PurchaseMgr.inst.getPurchaseCfg(PurchaseConstants.BUY_YABIAO_COUNT, bean.todayBuyYBTimes + 1);
		if (pc == null) {
			log.error("没有获取到购买押镖的数据");
			resp.setLeftYBTimes(bean.remainYB);
			resp.setResult(40);
			return;
		}
		// 购买的次数
		int count = (int) pc.getNumber();
		int yuanbao = pc.getYuanbao();
		if (jz.yuanBao < yuanbao) {
			log.info("{}够买押镖次数失败，元宝不足", jz.id);
			resp.setResult(20);
			resp.setLeftYBTimes(bean.remainYB);
			return;
		}
		YuanBaoMgr.inst.diff(jz, -yuanbao, 0, yuanbao,YBType.YB_BUY_YABIAO_CISHU, "押镖次数购买");
		HibernateUtil.save(jz);
		JunZhuMgr.inst.sendMainInfo(session);

		// 保存够买次数
		bean.remainYB += count;
		bean.todayBuyYBTimes += 1;
		HibernateUtil.save(bean);

		resp.setResult(10);
		resp.setUsedYBVip(bean.todayBuyYBTimes);
		session.write(resp.build());
	}
//2015年12月12日 1.1版本劫镖次数不需要买
//	public void buyJieBiaoCount(JunZhu jz, IoSession session) {
//		 YBBattleBean bean = getYBZhanDouInfo(jz.id, jz.vipLevel);
//		BuyCountsResp.Builder resp = BuyCountsResp.newBuilder();
//		int vipLev = jz.vipLevel;
//		int buyYaBiaoCounts = VipMgr.INSTANCE.getValueByVipLevel(vipLev,VipData.jiebiaoTimes);
//		if (bean.todayBuyJBTimes >= buyYaBiaoCounts) {
//			log.info("{}够买劫镖次数已用完", jz.id);
//			resp.setResult(30);
//			resp.setLeftJBTimes(bean.remainJB);
//			resp.setLeftYBTimes(-1);
//			return;
//		}
//
//		// 获取够买配置
//		Purchase pc = PurchaseMgr.inst.getPurchaseCfg(PurchaseConstants.BUY_JIEBIAO_COUNT,bean.todayBuyJBTimes + 1);
//		if (pc == null) {
//			log.error("没有获取到购买劫镖次数的数据");
//			resp.setLeftJBTimes(bean.remainJB);
//			resp.setLeftYBTimes(-1);
//			resp.setResult(40);
//			return;
//		}
//		// 购买的次数
//		int count = (int) pc.getNumber();
//		int yuanbao = pc.getYuanbao();
//		if (jz.yuanBao < yuanbao) {
//			log.info("{}够买劫镖次数失败，元宝不足", jz.id);
//			resp.setResult(20);
//			resp.setLeftJBTimes(bean.remainJB);
//			resp.setLeftYBTimes(-1);
//			return;
//		}
//		YuanBaoMgr.inst.diff(jz, -yuanbao, 0,PurchaseMgr.inst.getPrice(PurchaseConstants.BUY_JIEBIAO_COUNT),YBType.YB_BUY_JIEBIAO_CISHU, "劫镖次数购买");
//		HibernateUtil.save(jz);
//		JunZhuMgr.inst.sendMainInfo(session);
//
//		// 保存够买次数
//		bean.remainJB += count;
//		bean.todayBuyJBTimes += 1;
//		HibernateUtil.save(bean);
//
//		resp.setResult(10);
//		resp.setLeftJBTimes(bean.remainJB);
//		resp.setLeftYBTimes(-1);
//		resp.setUsedJBVip(bean.todayBuyJBTimes);
//		resp.setUsedYBVip(-1);
//		session.write(resp.build());
//	}

	
	/**
	 * @Description: 根据vip等级获取劫镖次数
	 * @param vipLev
	 * @return
	 */
	public int getJieBiaoCountForVip(int vipLev) {
		// int value = VipMgr.INSTANCE.getValueByVipLevel(vipLev,
		// VipData.bugJieBiaoTime);
		// int times = PVPConstant.JIEBIAO_TOTAL_TIMES;
		// times +=PurchaseMgr.inst.getAllUseNumbers(PurchaseConstants.JIEBIAO,
		// value);
		return CanShu.JIEBIAO_MAXNUM;
	}
	
	
	/**
	 * @Description //推送押镖战斗记录
	 * @param jId
	 * @param isNew4History
	 * @param isNew4Enemy
	 */
	public  void pushYBRecord(long jId,boolean isNew4History,boolean isNew4Enemy) {
		SessionUser su = SessionManager.inst.findByJunZhuId(jId);
		if (su != null)
		{
			isNew4RecordResp.Builder resp=isNew4RecordResp.newBuilder();
			resp.setIsNew4History(isNew4History);
			resp.setIsNew4Enemy(isNew4Enemy);
			su.session.write(resp.build());
		}
	}

	//初始化运镖历史记录
	public YunBiaoHistory getYunBiaoHistory(long jzId){
		YunBiaoHistory ybHis = HibernateUtil.find(YunBiaoHistory.class, jzId);
		if(ybHis==null){
			ybHis=new YunBiaoHistory();
			ybHis.successYB = 0;
			ybHis.successJB = 0;
			ybHis.historyJB = 0;
			ybHis.historyYB = 0;
			ybHis.junZhuId=jzId;
			HibernateUtil.insert(ybHis);
		}
		return ybHis;
	}
	public boolean isEmeny(long jzId,long otherJzId) {
		return DB.lexist((ENEMY_KEY + jzId), otherJzId + "");
	}
	/**
	 * @Description 是否在安全区
	 * @param x 
	 * @param z
	 * @return 
	 */
	public int getSafeArea(float x,float z){
		if(isInSafeArea(x, z,1)){
			return 1;
		}
		if(isInSafeArea(x, z,2)){
			return 2;
		}
		if(isInSafeArea(x, z,3)){
			return 3;
		}
		if(isInSafeArea(x, z,4)){
			return 4;
		}
		return -1;
	}
	/**
	 * @Description 根据当前坐标和安全区编号-safeId 判断是否在安全区safeId内
	 * @param x
	 * @param z
	 * @param safeId
	 * @return
	 */
	public boolean isInSafeArea(float x,float z,int safeId){
		YunBiaoSafe	safeArea=safeAreaMap.get(safeId);
		float x4safe=safeArea.saveAreaX;
		float z4safe=safeArea.saveAreaZ;
		double r=safeArea.saveArear;
		double distance=Math.sqrt((x-x4safe)*(x-x4safe) +(z-z4safe)*(z-z4safe)) ;
		return distance<r;
	}


	public void addMission(int id, IoSession session, Builder builder) {
		Mission m = new Mission(id, session, builder);
		missions.add(m);
	}

	public void shutdown() {
		missions.add(exit);
		Iterator<Scene> it = yabiaoScenes.values().iterator();
		while(it.hasNext()){
			it.next().shutdown();
		}
	}
	
	/**
	 * 获取离死亡地点最近的安全区对象
	 * @param deadPosX
	 * @param deadPosZ
	 * @return
	 */
	public YunBiaoSafe getNearestSafeArea(int deadPosX, int deadPosZ) {
		YunBiaoSafe safeArea = null;
		int distance = Integer.MAX_VALUE;
		for(Map.Entry<Integer, YunBiaoSafe> entry : safeAreaMap.entrySet()) {
			YunBiaoSafe ybSafe = entry.getValue();
			float dis = (float) Math.sqrt(
					Math.pow(deadPosX - ybSafe.saveAreaX, 2)+
					Math.pow(deadPosZ - ybSafe.saveAreaZ, 2));
			if(dis <= distance) {
				safeArea = ybSafe;
			}
		}
		return safeArea;
	}

	@Override
	public void proc(Event e) {
		switch (e.id) {
			case ED.REFRESH_TIME_WORK:
				IoSession session = (IoSession) e.param;
				if(session == null){
					break;
				}
				JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
				if(jz == null){
					break;
				}
				boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.yabiao, jz.id, jz.level);
				if(!isOpen){
					break;
				}
				YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jz.id);
				if(ybBean != null){
					if(ybBean.isNew4Enemy){
						log.info("-----发送押镖有新仇人红点通知");
						FunctionID.pushCanShangjiao(jz.id, session, FunctionID.yabiao_enemy);
					}
					if(ybBean.isNew4History){
						log.info("-----发送押镖有新战斗记录红点通知");
						FunctionID.pushCanShangjiao(jz.id, session, FunctionID.yabiao_history);
					}
				}
				break;
			}
	}
	@Override
	protected void doReg() {
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
	}



}