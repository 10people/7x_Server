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

import qxmobile.protobuf.BattlePveResult.BattleResult;
import qxmobile.protobuf.Chat.ChatPct;
import qxmobile.protobuf.Chat.ChatPct.Channel;
import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Scene.ExitScene;
import qxmobile.protobuf.Scene.SpriteMove;
import qxmobile.protobuf.Yabiao.AnswerYaBiaoHelpReq;
import qxmobile.protobuf.Yabiao.AnswerYaBiaoHelpResp;
import qxmobile.protobuf.Yabiao.AskYaBiaoHelpResp;
import qxmobile.protobuf.Yabiao.BiaoCheState;
import qxmobile.protobuf.Yabiao.BuyCountsReq;
import qxmobile.protobuf.Yabiao.BuyCountsResp;
import qxmobile.protobuf.Yabiao.EnemiesInfo;
import qxmobile.protobuf.Yabiao.EnemiesResp;
import qxmobile.protobuf.Yabiao.EnterYaBiaoScene;
import qxmobile.protobuf.Yabiao.HorseProp;
import qxmobile.protobuf.Yabiao.HorsePropReq;
import qxmobile.protobuf.Yabiao.HorsePropResp;
import qxmobile.protobuf.Yabiao.HorseType;
import qxmobile.protobuf.Yabiao.JieBiaoResult;
import qxmobile.protobuf.Yabiao.RoomInfo;
import qxmobile.protobuf.Yabiao.SetHorseResult;
import qxmobile.protobuf.Yabiao.TiChuXieZhuResp;
import qxmobile.protobuf.Yabiao.TiChuYBHelpRsq;
import qxmobile.protobuf.Yabiao.XieZhuJunZhu;
import qxmobile.protobuf.Yabiao.XieZhuTimesResp;
import qxmobile.protobuf.Yabiao.YBHistory;
import qxmobile.protobuf.Yabiao.YBHistoryResp;
import qxmobile.protobuf.Yabiao.YBPveNpcInfo;
import qxmobile.protobuf.Yabiao.YaBiaoHelpResp;
import qxmobile.protobuf.Yabiao.YabiaoInfoResp;
import qxmobile.protobuf.Yabiao.YabiaoJunZhuInfo;
import qxmobile.protobuf.Yabiao.YabiaoJunZhuList;
import qxmobile.protobuf.Yabiao.YabiaoMainInfoResp;
import qxmobile.protobuf.Yabiao.YabiaoMenuResp;
import qxmobile.protobuf.Yabiao.YabiaoResult;
import qxmobile.protobuf.Yabiao.YabiaoRoomInfo;
import qxmobile.protobuf.Yabiao.isNew4RecordResp;
import qxmobile.protobuf.ZhanDou;
import qxmobile.protobuf.ZhanDou.Group;
import qxmobile.protobuf.ZhanDou.Node;
import qxmobile.protobuf.ZhanDou.PvpZhanDouInitReq;
import qxmobile.protobuf.ZhanDou.ZhanDouInitError;
import qxmobile.protobuf.ZhanDou.ZhanDouInitResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.chat.ChatMgr;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.CartTemp;
import com.manu.dynasty.template.DescId;
import com.manu.dynasty.template.JunzhuShengji;
import com.manu.dynasty.template.Mail;
import com.manu.dynasty.template.Purchase;
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
import com.qx.battle.PveMgr;
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
import com.qx.prompt.PromptMsgMgr;
import com.qx.prompt.SuBaoConstant;
import com.qx.purchase.PurchaseMgr;
import com.qx.pvp.PvpMgr;
import com.qx.robot.RobotSession;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.timeworker.FunctionID;
import com.qx.vip.VipData;
import com.qx.vip.VipMgr;
import com.qx.world.Mission;
import com.qx.world.Scene;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

public class YaBiaoHuoDongMgr extends EventProc implements Runnable {
	public static Logger log = LoggerFactory.getLogger(YaBiaoHuoDongMgr.class);
	public static YaBiaoHuoDongMgr inst;

	public ConcurrentHashMap<Long, Integer> ybJzId2ScIdMap;// 押镖君主和场景对应存储，方便君主找到Scid
	public ConcurrentHashMap<Integer, Set<Long>> ybJzList2ScIdMap; // 存储每个场景中押镖的君主列表
	public ConcurrentHashMap<Integer, Set<Long>> jbJzList2ScIdMap; // 劫镖君主和场景对应存储，方便君主找到Scid
	public ConcurrentHashMap<Long, Integer> jbJz2ScIdMap;// 存储每个场景中劫镖的君主列表
	public ConcurrentHashMap<Integer, Scene> yabiaoScenes;
	//2015年11月25日玩法变更废弃
//	public ConcurrentHashMap<Long, Map<Integer, Integer>> ybNpcMap;
	public static Map<Integer, CartTemp> cartMap = new HashMap<Integer, CartTemp>();
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
	/** 购买押镖类型 **/
	public static final int BUY_YABIAO_COUNT = 18;
	/** 购买劫镖类型 **/
	public static final int BUY_JIEBIAO_COUNT = 19;
	/** 收益加成 **/
	public static  double BUFF_PROFIT = 1;
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
		jbJzList2ScIdMap = new ConcurrentHashMap<Integer, Set<Long>>();
		jbJz2ScIdMap = new ConcurrentHashMap<Long, Integer>();
//		ybNpcMap = new ConcurrentHashMap<Long, Map<Integer, Integer>>();
		xieZhuCache4YBJZ = new ConcurrentHashMap<Long, HashSet<Long>>();
		xzJZSatrtYB = new ConcurrentHashSet<Long>();
		answerHelpCache4YB = new ConcurrentHashMap<Long, List<Long>>();
		XIEZHU_YABIAO_SIZE = CanShu.YUNBIAOASSISTANCE_MAXNUM;
		List<CartTemp> list = TempletService.listAll(CartTemp.class.getSimpleName());
		for (CartTemp c : list) {
			cartMap.put(c.quality, c);
		}
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
	//刷新押镖收益比率
	public void refreshBuff2Profit() {
		boolean buff2Profit1=	DateUtils.isInDeadline(CanShu.OPENTIME_LUEDUO, CanShu.CLOSETIME_LUEDUO);
		boolean buff2Profit2=  DateUtils.isInDeadline(CanShu.OPENTIME_LUEDUO, CanShu.CLOSETIME_LUEDUO);
		//需要配置 TODO
		if(buff2Profit1||buff2Profit2){
			BUFF_PROFIT=2.0;
		}else{
			BUFF_PROFIT=1.0;
		}
		log.info("押镖收益比率为{}",BUFF_PROFIT);
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
			case PD.C_JIEBIAO_INFO_REQ:
				getJieBiaoInfo(id, builder, session);
				break;
			case PD.C_ENTER_YABIAOSCENE:
				enterJiebiaoScene(id, builder, session);
				break;
			case PD.C_BIAOCHE_INFO:
				pushBiaoCheList(id, builder, session);
				break;
//			case PD.C_ENDYABIAO_REQ:// 测试用
//				settleYaBiaoResult(id, builder, session);
//				break;
			case PD.C_YABIAO_RESULT:
//				settleJieBiaoResult(id, builder, session);
				break;
			case PD.C_YABIAO_BUY_RSQ:
				buyCounts4YaBiao(id, builder, session);
				break;
			case PD.C_YABIAO_HELP_RSQ:
				askHelp4YB(id, builder, session);
				break;
			case PD.C_ANSWER_YBHELP_RSQ:
				answerHelp2YB(id, builder, session);
				break;
			case PD.C_TICHU_YBHELP_RSQ:
				tichuHelper2YB(id, builder, session);
				break;
			case PD.C_YABIAO_XIEZHU_TIMES_RSQ:
				getXieZhuTimes(id, builder, session);
				break;
			case PD.C_ZHANDOU_INIT_YB_REQ:// 押镖请求战斗初始化数据
//				YBDateInfoRequest(id, session, builder, true);
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
				pushBiaoCheList(id, builder, session);
				break;
			case PD.C_YABIAO_RESULT:
//				settleJieBiaoResult(id, builder, session);
				break;
			case PD.C_YABIAO_ENEMY_RSQ:
				getYabiaoEnemyInfo(id, builder, session);
				break;
			case PD.C_YABIAO_HISTORY_RSQ:
				getYabiaoHistoryInfo(id, builder, session);
				break;
			default:
				getYabiaoMainInfo(id, builder, session);
				log.error("未处理的消息{}", id);
				break;
			}
		}
	}

	/**
	 * @Description: //请求协助押镖次数
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getXieZhuTimes(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求协助押镖次数：君主不存在");
			return;
		}else{
			log.info("请求协助押镖次数：君主-{}",jz.id);
		}
		YaBiaoBean ybbean = HibernateUtil.find(YaBiaoBean.class, jz.id);
		XieZhuTimesResp.Builder resp = XieZhuTimesResp.newBuilder();
		int remainXZ=0;
		int usedXZ=getXiezhuCountForVip( jz.vipLevel);;
		if (ybbean == null) {
			//2015年8月6日 取消注释没开启押镖也能协助
			ybbean = initJunZhuYBInfo(jz.id, jz.vipLevel);
//			resp.setRemainXZ(remainXZ);
//			resp.setUsedXZ(usedXZ);
		} else {
			ybbean = resetYBBean(ybbean, jz.vipLevel);
		}
		remainXZ=ybbean.remainXZ;
		usedXZ=ybbean.usedXZ;
		resp.setRemainXZ(remainXZ);
		resp.setUsedXZ(usedXZ);
		session.write(resp.build());
	}

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
	 * @Description: 推送镖车信息
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void pushBiaoCheList(int id, Builder builder, IoSession session) {
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
		int roomId = req.getRoomId();
		YabiaoJunZhuList.Builder resp = YabiaoJunZhuList.newBuilder();
		Set<Long> ybSet = ybJzList2ScIdMap.get(roomId);
		for (Long junzhuId : ybSet) {
			YabiaoJunZhuInfo.Builder biaoChe = YabiaoJunZhuInfo.newBuilder();
			if(junzhuId<0){
				YaBiaoRobot4Sys ybr = (YaBiaoRobot4Sys) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(junzhuId);
				if(ybr==null){
					log.error("推送镖车信息，运镖君主--{}的镖车机器人不存在",junzhuId);
					continue;
				}
				biaoChe.setJunZhuId(junzhuId);
				biaoChe.setJunZhuName(ybr.name);
				biaoChe.setLevel(ybr.jzLevel);
				biaoChe.setLianMengName("");
				int protectTime = ybr.protectCD - ((int) (System.currentTimeMillis() - ybr.endBattleTime) / 1000);
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
				boolean isExit = DB.lexist((ENEMY_KEY + jzId), junzhuId + "");
				biaoChe.setIsEnemy(isExit);
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
				int protectTime = ybr.protectCD - ((int) (System.currentTimeMillis() - ybr.endBattleTime) / 1000);
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
				boolean isExit = DB.lexist((ENEMY_KEY + jzId), junzhuId + "");
				biaoChe.setIsEnemy(isExit);
				biaoChe.setJunzhuGuojia(ybJunZhu.guoJiaId);
				//2015年8月31日返回盟友增加的护盾
				biaoChe.setHuDun(ybr.hudun*100/ybJunZhu.shengMingMax);
			}
			resp.addYabiaoJunZhuList(biaoChe.build());
		}
		// 计算劫镖冷却时间
		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jz.id);
		int lengqueCD = 0;
		int remainJB = 0;
		int gongjiZuheId = -1;
		int buyCounts = 0;
		if (ybBean != null) {
			if (ybBean.lastJBDate != null) {
				int distanceTime = timeDistanceBySeconds(ybBean.lastJBDate);
				if (distanceTime > 0) {
					lengqueCD = 
					(CanShu.JIEBIAO_CD - distanceTime > 0) ? (CanShu.JIEBIAO_CD - distanceTime): 0;
				}
			}
			remainJB = ybBean.remainJB;
			gongjiZuheId = ybBean.gongJiZuHeId;
			buyCounts = ybBean.todayBuyJBTimes;
		}
		resp.setGongjiZuHeId(gongjiZuheId);
		resp.setLengqueCD(lengqueCD);
		resp.setJieBiaoCiShu(remainJB);
		resp.setBuyCiShu(buyCounts);
		session.write(resp.build());
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
		YaBiaoBean ybbean = HibernateUtil.find(YaBiaoBean.class, jz.id);
		int remainYB = getYaBiaoCountForVip(jz.vipLevel);
		int remainJB = getJieBiaoCountForVip(jz.vipLevel);
		boolean isNew4Enemy = false;
		boolean isNew4History = false;
		int todayBuyYBTimes=0;
		if (ybbean == null) {
//			ybbean = initJunZhuYBInfo(jz.id, jz.vipLevel);
		} else {
			ybbean = resetYBBean(ybbean, jz.vipLevel);
			remainYB=ybbean.remainYB;
			remainJB=ybbean.remainJB;
			isNew4Enemy=ybbean.isNew4Enemy;
			isNew4History=ybbean.isNew4History;
			todayBuyYBTimes=ybbean.todayBuyYBTimes;
		}
		
		resp.setYaBiaoCiShu(remainYB);
		resp.setJieBiaoCiShu(remainJB);
		resp.setIsNew4Enemy(isNew4Enemy);
		resp.setIsNew4History(isNew4History);
		Integer roomid = ybJzId2ScIdMap.get(jz.id);
		if (roomid != null) {// 押镖时返回房间号
			resp.setRoomId(roomid);
			resp.setState(10);
		} else {
			resp.setRoomId(-1);
			resp.setState(20);
		}
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
	 * @param id
	 * @param builder
	 * @param session
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

	// 请求押镖界面面
	public void getYabiaoMenu(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求押镖界面出错：君主不存在");
			return;
		}
		Long jzId = jz.id;
		YabiaoMenuResp.Builder resp = YabiaoMenuResp.newBuilder();
		YaBiaoBean ybbean = HibernateUtil.find(YaBiaoBean.class, jzId);
		int horseType=0;
		if (ybbean == null) {
			ybbean = initJunZhuYBInfo(jzId, jz.vipLevel);
//			resp.setRemainAskXZ(ybbean.remainAskXZ);
//			resp.setHorse(ybbean.horseType);
		} else {
			if(ybbean.horseType==-1){
				ybbean.horseType=0;
			}
			horseType=ybbean.horseType;
			ybbean = resetYBBean(ybbean, jz.vipLevel);
		}
		//是否播放随机马匹效果
		boolean isNewHorse=true;
		if(horseType==ybbean.horseType){
			isNewHorse=false;
		}
		resp.setRemainAskXZ(ybbean.remainAskXZ);
		resp.setHorse(ybbean.horseType);
		resp.setIsNewHorse(isNewHorse);
		// 加载协助君主信息
//		getXieZhuJZInfo(jzId,jz.shengMingMax, resp);
		//TODO 加载马车道具
		HorseProp.Builder hprop=HorseProp.newBuilder();
		hprop.setBaodi(ybbean.baodi);
		hprop.setBaohu(ybbean.baohu);
		hprop.setJiasu(ybbean.jiasu);
		resp.setHorseprop(hprop.build());
		session.write(resp.build());
	}

	public void getXieZhuJZInfo(Long ybJzId,int shengMingMax, YabiaoMenuResp.Builder resp) {
		HashSet<Long> xiezhuSet = xieZhuCache4YBJZ.get(ybJzId);
		if (xiezhuSet == null || xiezhuSet.size() == 0) {
			log.info("{}无协助君主，时间-{}", ybJzId, new Date());
			return;
		}
		for (Long jzId : xiezhuSet) {
			JunZhu xzJz = HibernateUtil.find(JunZhu.class, jzId);
			if (xzJz != null) {
				XieZhuJunZhu.Builder xiezhujz = XieZhuJunZhu.newBuilder();
				xiezhujz.setJzId(xzJz.id);
				xiezhujz.setName(xzJz.name);
				xiezhujz.setRoleId(xzJz.roleId);
				//2015年8月31日返回盟友增加的护盾
				int hudun=(int) (xzJz.shengMingMax * CanShu.YUNBIAOASSISTANCE_HPBONUS);
				xiezhujz.setAddHuDun(hudun*100/shengMingMax);
				resp.addJz(xiezhujz);
			}
		}
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
		YaBiaoBean ybbean = HibernateUtil.find(YaBiaoBean.class, jz.id);
		if (ybbean == null) {
			log.error("请求设置马匹出错：君主押镖信息不存在{}", jz.id);
			resp.setResult(30);
			session.write(resp.build());
			return;
		} 
		if (ybbean.horseType == 5) {
			log.error("请求设置马匹出错：马匹已经达到满级{}-{}", jz.id, ybbean.horseType);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		int cost=0;
		for (int i = ybbean.horseType; i < targetType; i++) {
			// 获取马车配置
			CartTemp cart = cartMap.get(i);
			// 此处引用了世界聊天元宝的配置，要根据需求改
			cost += cart.ShengjiCost;
			log.info("请求设置马匹,马匹从{}升级到{},需要花费--{}",i,i+1,cart.ShengjiCost);
		}
		if(cost<=0){
			log.info("请求设置马匹出错：{}需要花费的元宝---{}计算错误", jz.id,cost);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		log.info("请求设置马匹,马匹从{}升级到{},需要花费--{}", ybbean.horseType,targetType,cost);
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
		ybbean.horseType += 1;
		HibernateUtil.save(ybbean);
		log.info("junzhu:{}升级{}马匹为{}，花费元宝{}", jz.id, ybbean.horseType - 1,ybbean.horseType, cost);
		resp.setResult(10);
		session.write(resp.build());
	}
	/**
	 * @Description: 买马车buff
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void buyHorseBuff(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求设置马匹出错：君主不存在");
			return;
		}
		HorsePropReq.Builder req=(HorsePropReq.Builder) builder;
		int propType=req.getPropType();
		HorsePropResp.Builder resp = HorsePropResp.newBuilder();
		YaBiaoBean ybbean = HibernateUtil.find(YaBiaoBean.class, jz.id);
		if (ybbean == null) {
			log.error("请求设置马匹出错：君主押镖信息不存在{}", jz.id);
			resp.setRes(30);
			session.write(resp.build());
			return;
		} 
		switch (propType) {
		case 1:
			buyBaoDi(session, resp, jz, ybbean);
			break;
		case 2:
			buyJiaSu(session, resp, jz, ybbean);
			break;
		case 3:
			buyBaoHu(session, resp, jz, ybbean);
			break;
		default:
			log.error("{}请求购买马车道具失败：道具类型--{}错误", jz.id, propType);
			resp.setRes(30);
			session.write(resp.build());
			break;
		}
	}
	protected void buyBaoDi(IoSession session, HorsePropResp.Builder resp,JunZhu jz,YaBiaoBean ybbean) {
		if (ybbean.baodi >0) {
			log.error("{}请求购买马车道具失败：保底收益道具已购买", jz.id);
			resp.setRes(20);
			session.write(resp.build());
			return;
		}
		int cost=0;
		//TODO 读取参数
		log.info("{}请求购买马车保底收益道具,需要花费--{}", jz.id,cost);
		if (jz.yuanBao < cost) {
			log.info("{}请求购买马车保底收益道具,元宝---{}不够花费--{},", jz.id,jz.yuanBao,cost);
			resp.setRes(40);
			session.write(resp.build());
			return;
		}

		YuanBaoMgr.inst.diff(jz, -cost, 0, cost,YBType.YB_SHENGJI_YABIAO_MAPI, "购买马车保底收益道具");
		HibernateUtil.save(jz);
		JunZhuMgr.inst.sendMainInfo(session);// 推送元宝信息
		ybbean.baodi = 1;
		HibernateUtil.save(ybbean);
		log.info("{}请求购买马车保底收益道具--{},花费--{},剩余元宝{}", jz.id,ybbean.baodi,cost,jz.yuanBao);
		
		resp.setRes(10);
		session.write(resp.build());
	}
	protected void buyJiaSu(IoSession session, HorsePropResp.Builder resp,JunZhu jz,YaBiaoBean ybbean) {
		if (ybbean.jiasu >0) {
			log.error("{}请求购买马车道具失败：加速道具已购买", jz.id);
			resp.setRes(20);
			session.write(resp.build());
			return;
		}
		int cost=0;
		//TODO 读取参数
		log.info("{}请求购买马车加速道具,需要花费--{}", jz.id,cost);
		if (jz.yuanBao < cost) {
			log.info("{}请求购买马车加速道具,元宝---{}不够花费--{},", jz.id,jz.yuanBao,cost);
			resp.setRes(40);
			session.write(resp.build());
			return;
		}

		YuanBaoMgr.inst.diff(jz, -cost, 0, cost,YBType.YB_SHENGJI_YABIAO_MAPI, "购买马车加速道具");
		HibernateUtil.save(jz);
		JunZhuMgr.inst.sendMainInfo(session);// 推送元宝信息
		ybbean.jiasu = 1;
		HibernateUtil.save(ybbean);
		log.info("{}请求购买马车加速道具--{},花费--{},剩余元宝{}", jz.id,ybbean.jiasu,cost,jz.yuanBao);
		resp.setRes(10);
		session.write(resp.build());
	}
	protected void buyBaoHu(IoSession session, HorsePropResp.Builder resp,JunZhu jz,YaBiaoBean ybbean) {
		if (ybbean.baohu >0) {
			log.error("{}请求购买马车道具失败：保护道具已购买", jz.id);
			resp.setRes(20);
			session.write(resp.build());
			return;
		}
		int cost=0;
		//TODO 读取参数
		log.info("{}请求购买马车保护道具,需要花费--{}", jz.id,cost);
		if (jz.yuanBao < cost) {
			log.info("{}请求购买马车保护道具,元宝---{}不够花费--{},", jz.id,jz.yuanBao,cost);
			resp.setRes(40);
			session.write(resp.build());
			return;
		}

		YuanBaoMgr.inst.diff(jz, -cost, 0, cost,YBType.YB_SHENGJI_YABIAO_MAPI, "购买马车保护道具");
		HibernateUtil.save(jz);
		JunZhuMgr.inst.sendMainInfo(session);// 推送元宝信息
		ybbean.baohu = 1;
		HibernateUtil.save(ybbean);
		log.info("{}请求购买马车保护道具--{},花费--{},剩余元宝{}", jz.id,ybbean.baohu,cost,jz.yuanBao);
		resp.setRes(10);
		session.write(resp.build());
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
			resp.setRoomId(-1);
			session.write(resp.build());
			log.error("请求进入押镖场景出错：君主不存在");
			return;
		}

		// 判断协助者状态
		List<Long> returnSet = CheckXieZhuState(jz.id);
		if (returnSet.size() > 0) {
			resp.setResult(40);
			resp.setRoomId(-1);
			for (Long jzId : returnSet) {
				resp.addJzId(jzId);
			}
			session.write(resp.build());
		}
		YaBiaoRobot temp = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(jz.id);
		if (temp != null) {
			resp.setResult(30);
			// resp.setPathId(-1);废弃字段
			resp.setRoomId(-1);
			session.write(resp.build());
			log.error("请求进入押镖场景出错：君主已参加押镖");
			return;
		}
		YaBiaoBean ybbean = HibernateUtil.find(YaBiaoBean.class, jz.id);
		if(ybbean.remainYB<1){
			resp.setResult(30);
			// resp.setPathId(-1);废弃字段
			resp.setRoomId(-1);
			session.write(resp.build());
			log.error("请求进入押镖场景出错：君主押镖次数已用完");
			return;
		}
		// 进入押镖场景进行押镖
		int scId = locateFakeSceneId();
		Scene sc = yabiaoScenes.get(scId);
		if (sc == null) {// 没有场景
			synchronized (yabiaoScenes) {// 防止多次创建
				sc = yabiaoScenes.get(scId);
				if (sc == null) {
					sc = new Scene("YB#" + scId);
					sc.startMissionThread();
					yabiaoScenes.put(scId, sc);
				}
			}
		}

		// 获取马车配置
		CartTemp cart = cartMap.get(ybbean.horseType);
		// 创建押镖机器人
		YaBiaoRobot ybr = new YaBiaoRobot();
		initYaBiaoRobot(jz, ybr, sc, cart.cartTime, ybbean.horseType);
//		ybrobot.jzId = jz.id;
//		ybrobot.name = jz.name;
//		int pathId = new Random().nextInt(3) + 1;// 随机路线
//		pathId = pathId > 3 ? 3 : pathId;// pathId大于3重置为3
//		
//		ybrobot.pathId = pathId;
//		ybrobot.totalTime = cart.cartTime * 1000;// 毫秒计算
//		ybrobot.usedTime = 0;
//		ybrobot.startTime = System.currentTimeMillis();
//		ybrobot.startTime4short = System.currentTimeMillis();
//		ybrobot.endBattleTime = System.currentTimeMillis();
//		ybrobot.horseType = ybbean.horseType;
//		ybrobot.session = new RobotSession();
//		AllianceBean a = AllianceMgr.inst.getAllianceByJunZid(jz.id);
//		ybrobot.lmName = a == null ? "" : a.name;
//
//		EnterScene.Builder enter = EnterScene.newBuilder();
//		enter.setUid(0);
//		enter.setSenderName(ybrobot.name);
//		enter.setPosX(0);
//		enter.setPosY(0);
//		enter.setPosZ(0);
//		sc.exec(PD.Enter_YBScene, ybrobot.session, enter);
		ybbean.usedYB += 1;
		ybbean.remainYB -= 1;
		HashSet<Long> xiezhuSet = xieZhuCache4YBJZ.get(jz.id);
		if (xiezhuSet != null) {
			log.info("扣除{}请求协助次数", jz.id);
			ybbean.usedAskXZ += 1;
			ybbean.remainAskXZ -= 1;
		}

		
		JunzhuShengji ss = JunZhuMgr.inst.getJunzhuShengjiByLevel(jz.level);
		ybr.worth = (int) (ss.xishu * cart.profitPara);
//		ybbean.lastYBDate = new Date();
//		ybbean.historyYB += 1;// 押镖历史次数+1
		HibernateUtil.save(ybbean);
		// 存入押镖Map
		pushYbJz2Map(jz.id, scId);
		// 将镖车机器人加入镖车机器人管理线程
		BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.put(jz.id, ybr);
		// 扣除盟友协助次数
		settleXieZhuCount(jz.id);
		// 生成护盾
		initHuDun4YBJZ(jz.id,ybr);
		// 返回成功进入押镖活动
		resp.setResult(10);
		// resp.setPathId(pathId);废弃字段
		resp.setRoomId(scId);
		session.write(resp.build());

		// 广播押镖机器人进入场景
//		broadBiaoCheINScene(ybrobot, jz, sc);
		// 每日任务：完成1次押镖活动
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(jz.id,
				DailyTaskConstants.yunBiao, 1));
		// 主线任务完成
		EventMgr.addEvent(ED.finish_yunbiao_x, new Object[] { jz.id });
	}
	
	/**
	 * @Description 初始化君主押镖机器人
	 * @param jz
	 * @param ybrobot
	 * @param sc
	 * @param cartTime
	 * @param horseType
	 * @return
	 */
	public YaBiaoRobot initYaBiaoRobot(JunZhu jz,YaBiaoRobot ybrobot,Scene sc,long  cartTime,int horseType) {
		ybrobot.session = new RobotSession();
		ybrobot.move = SpriteMove.newBuilder();
		ybrobot.jzId = jz.id;
		ybrobot.name = jz.name;
		int pathId = new Random().nextInt(3) + 1;// 随机路线
		pathId = pathId > 3 ? 3 : pathId;// pathId大于3重置为3
		ybrobot.pathId = pathId;
		ybrobot.nodeId=1;
		ybrobot.totalTime = cartTime * 1000;// 毫秒计算
		ybrobot.usedTime = 0;
		ybrobot.startTime = System.currentTimeMillis();
		ybrobot.startTime2upSpeed = System.currentTimeMillis();
		//TODO 根据配置读取加速时间 和速度
		ybrobot.upSpeedTime = 10*1000;
		ybrobot.speed=1.5;
		ybrobot.startTime4short = System.currentTimeMillis();
		ybrobot.endBattleTime = System.currentTimeMillis();
		ybrobot.horseType =horseType;
		HashMap<Integer, RoadNode> roadPath=YBRobotMgr.road.get(pathId);
		RoadNode rd=roadPath.get(1); 
		ybrobot.totaltime4short=rd.totalTime;
		ybrobot.startPosX=rd.posX;
		ybrobot.startPosZ=rd.posZ;
		RoadNode rd4next=roadPath.get(2); 
		ybrobot.nextPosX=rd4next.posX;
		ybrobot.nextPosZ=rd4next.posZ;
		ybrobot.move.setPosX(ybrobot.posX);
		ybrobot.move.setPosY(0);//不用管Y坐标
		ybrobot.move.setPosZ(ybrobot.posZ);
		AllianceBean a = AllianceMgr.inst.getAllianceByJunZid(jz.id);
		ybrobot.lmName = a == null ? "" : a.name;
		ybrobot.hp = jz.shengMingMax;
		ybrobot.maxHp = jz.shengMingMax;
		log.info("君主{}机器人进入押镖场景,血量为{}", jz.id, ybrobot.hp);
		EnterScene.Builder enter = EnterScene.newBuilder();
		enter.setUid(0);
		enter.setSenderName(ybrobot.name);
		enter.setPosX(ybrobot.posX);
		enter.setPosY(0);
		enter.setPosZ(ybrobot.posZ);
		sc.exec(PD.Enter_YBScene, ybrobot.session, enter);
		return ybrobot;
	}

	/**
	 * @Description 产生系统机器人
	 * @param jz
	 * @param ybrobot
	 * @param sc
	 * @param cartTime
	 * @param horseType
	 * @return
	 */
	public void initSysYBRobots(Scene sc,int pathId,int scId) {
		long  cartTime=60;
		int hType = getRandomCart();// 随机一匹马 1 2 3 4 5 （0表示没有马）
		if (hType > 5) {
			hType = 5;
		}
		String robotName=getRandomString(5);
		YaBiaoRobot4Sys ybrobot=new YaBiaoRobot4Sys();
		ybrobot.session = new RobotSession();
		ybrobot.move = SpriteMove.newBuilder();
		long sysJzId=getRobotJzId() ;
		ybrobot.jzId = sysJzId;
		ybrobot.name = robotName;
		//TODO 读取配置得到血量
		ybrobot.hp = 10000;
		ybrobot.maxHp = 10000;
		ybrobot.pathId = pathId;
		ybrobot.nodeId=1;
		ybrobot.totalTime = cartTime * 1000;// 毫秒计算
		ybrobot.usedTime = 0;
		ybrobot.startTime = System.currentTimeMillis();
		ybrobot.startTime4short = System.currentTimeMillis();
		ybrobot.endBattleTime = System.currentTimeMillis();
		ybrobot.horseType =hType;
		HashMap<Integer, RoadNode> roadPath=YBRobotMgr.road.get(pathId);
		RoadNode rd=roadPath.get(1); 
		ybrobot.totaltime4short=rd.totalTime;
		ybrobot.startPosX=rd.posX;
		ybrobot.startPosZ=rd.posZ;
		RoadNode rd4next=roadPath.get(2); 
		ybrobot.nextPosX=rd4next.posX;
		ybrobot.nextPosZ=rd4next.posZ;
		ybrobot.move.setPosX(ybrobot.posX);
		ybrobot.move.setPosY(0);//不用管Y坐标
		ybrobot.move.setPosZ(ybrobot.posZ);
		ybrobot.lmName = "";
		EnterScene.Builder enter = EnterScene.newBuilder();
		enter.setUid(0);
		enter.setSenderName(ybrobot.name);
		enter.setPosX(ybrobot.posX);
		enter.setPosY(0);
		enter.setPosZ(ybrobot.posZ);
		sc.exec(PD.Enter_YBScene, ybrobot.session, enter);
		// 存入押镖Map
		pushYbJz2Map(sysJzId, scId);
		// 将镖车机器人加入镖车机器人管理线程
		BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.put(sysJzId, ybrobot);
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
	/**
	 * @Description: 检查协助者是否可以协助
	 * @param ybJzId
	 * @return
	 */
	private List<Long> CheckXieZhuState(Long ybJzId) {
		List<Long> wuxiaoJZList = new ArrayList<Long>();
		HashSet<Long> xiezhuSet = xieZhuCache4YBJZ.get(ybJzId);
		if (xiezhuSet == null || xiezhuSet.size() == 0) {
			return wuxiaoJZList;
		}
		for (Long jzId : xiezhuSet) {
			if (xzJZSatrtYB.contains(jzId)) {
				wuxiaoJZList.add(jzId);
			}
		}
		return wuxiaoJZList;
	}

	/**
	 * @Description: //获取场景id
	 * @return
	 */
	public int locateFakeSceneId() {
		int sizePerSc = 1000;// 默认最大押镖人数
		int scId = 0;// 默认0号场景
		do {
			Scene sc = yabiaoScenes.get(scId);
			Set<Long> ybSet = ybJzList2ScIdMap.get(scId);
			if (sc == null) {
				break;// 已有的场景都满了
			} else if (ybSet != null && ybSet.size() < sizePerSc) {
				// 这个场景还没满
				break;
			}
			scId++;// 押镖场景ID递增
		} while (true);
		return scId;
	}

	/**
	 * @Description: //请求劫镖主页面
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getJieBiaoInfo(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求劫镖主页出错：君主不存在");
			return;
		}
		long jzId = jz.id;
		log.info("{}请求劫镖主页",jzId);
		YabiaoInfoResp.Builder resp = YabiaoInfoResp.newBuilder();
		YaBiaoBean yb = HibernateUtil.find(YaBiaoBean.class, jzId);
		if (yb == null) {
			yb = initJunZhuYBInfo(jz.id, jz.vipLevel);
		} else {
			yb = resetYBBean(yb, jz.vipLevel);
		}
		resp.setFangyuZuHeId(yb.zuheId);
		Iterator<Integer> iter = yabiaoScenes.keySet().iterator();
		while (iter.hasNext()) {
			int roomId = (int) iter.next();
			Set<Long> ybSet = ybJzList2ScIdMap.get(roomId);
			if(ybSet!=null){
				YabiaoRoomInfo.Builder ybroom = YabiaoRoomInfo.newBuilder();
				ybroom.setRoomId(roomId);
				for (Long junzhuId : ybSet) {
					YabiaoJunZhuInfo.Builder ybjz = YabiaoJunZhuInfo.newBuilder();
					if(junzhuId<0){
						//TODO 处理系统镖车机器人
						YaBiaoRobot4Sys sysRobot=(YaBiaoRobot4Sys) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(junzhuId);
						ybjz.setJunZhuId(junzhuId);
						ybjz.setJunZhuName(sysRobot.name);
						ybjz.setLevel(sysRobot.jzLevel);
//						YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class,ybJunZhu.id);
//						YaBiaoRobot ybrobot = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJunZhu.id);
//						if(ybrobot==null){
//							log.error("运镖君主--{}的镖车机器人不存在",ybJunZhu.id);
//							continue;
//						}
						ybjz.setHp(sysRobot.hp);
						ybjz.setMaxHp(sysRobot.maxHp);
						ybjz.setWorth(sysRobot.worth);
						ybjz.setMaxWorth(sysRobot.worth);
						ybjz.setPathId(sysRobot.pathId);
						ybjz.setUsedTime(sysRobot.usedTime);
						ybjz.setTotalTime(sysRobot.totalTime);
						ybjz.setJunzhuGuojia(sysRobot.guojiaId);
						int reduceTime = ((int) (System.currentTimeMillis() - sysRobot.endBattleTime) / 1000);
						
						int protectTime = sysRobot.protectCD - reduceTime;
						log.info("{}的保护时间为（{}）/（{})", junzhuId, protectTime,sysRobot.protectCD);
						ybjz.setBaohuCD(protectTime > 0 ? protectTime : 0);
						ybjz.setState(sysRobot.isBattle ? 20 : (protectTime > 0 ? 30: 10));
						ybjz.setLianMengName( "" );
						ybjz.setHorseType(sysRobot.horseType);
						boolean isExit = DB.lexist((ENEMY_KEY + jzId), junzhuId + "");
						ybjz.setIsEnemy(isExit);
//						int zhanli = JunZhuMgr.inst.getJunZhuZhanliFinally(ybJunZhu);
						ybjz.setZhanLi(sysRobot.zhanli);
						//2015年8月31日返回盟友增加的护盾
						ybjz.setHuDun(0);

					}else{
						//处理玩家镖车机器人
						JunZhu ybJunZhu = HibernateUtil.find(JunZhu.class, junzhuId);
						ybjz.setJunZhuId(ybJunZhu.id);
						ybjz.setJunZhuName(ybJunZhu.name);
						ybjz.setLevel(ybJunZhu.level);
//						YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class,ybJunZhu.id);
						YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJunZhu.id);
						if(ybr==null){
							log.error("运镖君主--{}的镖车机器人不存在",ybJunZhu.id);
							continue;
						}
						ybjz.setHp(ybr.hp);
						ybjz.setMaxHp(ybJunZhu.shengMingMax);
						ybjz.setWorth(ybr.worth);
						ybjz.setMaxWorth(ybr.worth);
						ybjz.setPathId(ybr.pathId);
						ybjz.setUsedTime(ybr.usedTime);
						ybjz.setTotalTime(ybr.totalTime);
						ybjz.setJunzhuGuojia(ybJunZhu.guoJiaId);
						int reduceTime = ((int) (System.currentTimeMillis() - ybr.endBattleTime) / 1000);
						
						int protectTime = ybr.protectCD - reduceTime;
						log.info("{}的保护时间为（{}）/（{})", junzhuId, protectTime,ybr.protectCD);
						ybjz.setBaohuCD(protectTime > 0 ? protectTime : 0);
						ybjz.setState(ybr.isBattle ? 20 : (protectTime > 0 ? 30: 10));
						AllianceBean ybabean = AllianceMgr.inst.getAllianceByJunZid(ybJunZhu.id);
						ybjz.setLianMengName(ybabean == null ? "" : ybabean.name);
						ybjz.setHorseType(ybr.horseType);
						boolean isExit = DB.lexist((ENEMY_KEY + jzId), junzhuId + "");
						ybjz.setIsEnemy(isExit);
						int zhanli = JunZhuMgr.inst.getJunZhuZhanliFinally(ybJunZhu);
						ybjz.setZhanLi(zhanli);
						//2015年8月31日返回盟友增加的护盾
						ybjz.setHuDun(ybr.hudun*100/ybJunZhu.shengMingMax);
					}
					ybroom.addYbjzList(ybjz.build());
				}
				resp.addRoomList(ybroom);
			}
		}
		session.write(resp.build());
	}

	/**
	 * @Description: 保存押镖攻击密保
	 * @param jzId
	 * @param mibaoIds
	 * @param zuheId
	 */
	public void saveGongJiMiBao(long jzId, List<Long> mibaoIds, int zuheId) {
		YaBiaoBean bean = HibernateUtil.find(YaBiaoBean.class, jzId);
		if (bean == null) {
			log.error("玩家{}的押镖没有开启:", jzId);
			return;
		}
		bean.gongJiZuHeId = zuheId;
		HibernateUtil.save(bean);
		log.info("玩家:{}押镖更换秘宝成功", jzId);
	}

	/**
	 * @Description: //保存押镖防御密保
	 * @param jzId
	 * @param mibaoIds
	 * @param zuheId
	 */
	public void saveFangShouMiBao(long jzId, List<Long> mibaoIds, int zuheId) {
		YaBiaoBean bean = HibernateUtil.find(YaBiaoBean.class, jzId);
		if (bean == null) {
			log.error("玩家{}的押镖没有开启:", jzId);
			return;
		}
		bean.zuheId = zuheId;
		HibernateUtil.save(bean);
		log.info("玩家:{}押镖更换秘宝成功", jzId);
	}

	/**
	 * @Description: //劫镖人请求进入场景  玩法变更此方法废弃
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void enterJiebiaoScene(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求进入劫镖场景出错：君主不存在");
			return;
		}
		EnterYaBiaoScene.Builder req = (EnterYaBiaoScene.Builder) builder;
		int reqSceneId = 0;
		if (req != null) {
			reqSceneId = req.getRoomId();
		} else {
			log.error("请求进入劫镖场景出错：请求信息不存在");
			return;
		}
		// 离开原来的场景
		Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
		if (scene != null) {
			if (scene.name.contains("FW")) {
				int uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
				ExitScene.Builder exit = ExitScene.newBuilder();
				exit.setUid(uid);
				scene.exec(PD.Exit_HouseScene, session, exit);
			} else {
				int uid = (Integer) session.getAttribute(SessionAttKey.playerId_Scene);
				ExitScene.Builder exit = ExitScene.newBuilder();
				exit.setUid(uid);
				scene.exec(PD.Exit_Scene, session, exit);
			}
		}
		// 进入劫镖场景
		Scene sc = yabiaoScenes.get(reqSceneId);
		if (sc != null) {// 该联盟没有场景
			synchronized (yabiaoScenes) {// 防止多次创建
				sc = yabiaoScenes.get(reqSceneId);
				if (sc != null) {
					// 存入劫镖Map
					pushJbJz2Map(jz.id, reqSceneId);
					EnterScene.Builder enter = EnterScene.newBuilder();
					enter.setUid(req.getUid());
					enter.setSenderName(req.getSenderName());
					enter.setPosX(req.getPosX());
					enter.setPosY(req.getPosY());
					enter.setPosZ(req.getPosZ());
					sc.exec(PD.Enter_YBScene, session, enter);
				} else {
					log.error("请求进入劫镖场景出错1：请求场景不存在SceneId:{}", reqSceneId);
					return;
				}
			}
		} else {
			log.error("请求进入劫镖场景出错2：请求场景不存在SceneId:{}", reqSceneId);
			return;
		}
	}

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
		long jzid = jz.id;
		YaBiaoHelpResp.Builder resp = YaBiaoHelpResp.newBuilder();
		Long cdTime = (Long) session.getAttribute(SessionAttKey.LAST_CHAT_KEY);
		long currentMillis = System.currentTimeMillis();
		if (cdTime != null && cdTime > currentMillis) {
			log.warn("发送速度过快{}", jz.id);
			resp.setCode(30);
			session.write(resp.build());
			return;
		}
		AllianceBean aBean = AllianceMgr.inst.getAllianceByJunZid(jzid);
		if (aBean == null) {
			log.info("{}没有联盟，请求押镖协助失败", jzid);
			resp.setCode(20);
			session.write(resp.build());
			return;
		}
		YaBiaoBean ybbean = HibernateUtil.find(YaBiaoBean.class, jzid);
		CartTemp cart = cartMap.get(ybbean.horseType);
		broadaskHelpEvent(aBean, jz, cart.name, session);
		log.info("{}请求押镖协助成功", jzid);
		resp.setCode(10);
		session.write(resp.build());
	}

	/**
	 * @Description: //扣除协助者次数
	 * @param ybJzId
	 */
	private void settleXieZhuCount(Long ybJzId) {
		log.info("结算{}的盟友的协助次数", ybJzId);
		HashSet<Long> xiezhuSet = xieZhuCache4YBJZ.get(ybJzId);
		if (xiezhuSet == null) {
			return;
		}
		for (Long jzId : xiezhuSet) {
			if (!jzId.equals(0L)) {
				YaBiaoBean bean = HibernateUtil.find(YaBiaoBean.class, jzId);
				if (bean == null) {
					log.error("玩家{}的押镖没有开启:", jzId);
					return;
				} else {
					bean.usedXZ += 1;
					bean.remainXZ -= 1;
					HibernateUtil.save(bean);
					log.info("扣除{}的押镖协助次数，剩余次数{}", jzId, bean.remainXZ);
					xzJZSatrtYB.add(jzId);
				}
			}
		}
	}
	
//	public SOSHistory saveSOSHistory(long jzId,AllianceBean lmBean,
//			long otherJzId,String jzName,
//			int horseType,Date startTime,int msgType) {
//		SOSHistory sosHistory = new SOSHistory();
//		sosHistory.otherJzId = otherJzId;
//		sosHistory.otherJzName=jzName;
//		sosHistory.horseType=horseType;
//		sosHistory.startTime=startTime;
//		sosHistory.msgType=msgType;
//		Long sizeAfterAdd = DB.rpush4JieBiao((SOS_HISTORY_KEY + jzId).getBytes(),
//				SerializeUtil.serialize(sosHistory));
//		if (sizeAfterAdd > sosHistorySize) {
//			Redis.getInstance().lpop(SOS_HISTORY_KEY + jzId);
//		}
//		//TODO 拆分开 保存完了再调下面的方法
//		if(lmBean!=null){
//			pushYaBiaoSOS2AllMengYou(lmBean, sosHistory);
//		}
//		return sosHistory;
//	}
//	public void pushYaBiaoSOS2AllMengYou(AllianceBean lmBean,SOSHistory sosHistory) {
//		log.info("向联盟---{}的成员推送协助请求开始",lmBean.id);
//		// 联盟成员的君主id
//		SOSYBHistory.Builder sosResp = SOSYBHistory.newBuilder();
//		sosResp.setOtherJzId(sosHistory.otherJzId);
//		sosResp.setOtherJzName(sosHistory.otherJzName);
//		sosResp.setHorseType(sosHistory.horseType);
//		sosResp.setMsgType(sosHistory.msgType);
//		Set<String> jzIds = Redis.getInstance().sget(AllianceMgr.inst.CACHE_MEMBERS_OF_ALLIANCE + lmBean.id);
//		Iterator<SessionUser> it = ChatMgr.inst.allUser.values().iterator();
//		while (it.hasNext()) {
//			SessionUser u = it.next();
//			Long junzhuId = (Long) u.session.getAttribute(SessionAttKey.junZhuId);
//			if (junzhuId == null) {
//				continue;
//			}
//			String v = junzhuId.toString();
//			if (!jzIds.contains(v)) {
//				continue;
//			}
//			u.session.write(sosResp.build());
//		}
//		log.info("向联盟---{}的成员推送协助请求结束",lmBean.id);
//	}
//	/**
//	 * @Description: 推送押镖求助列表
//	 * @param id
//	 * @param builder
//	 * @param session
//	 */
//	public void pushYaBiaoSOS2MengYou(long jzId) {
////		YaBiaoBean beidaBean = HibernateUtil.find(YaBiaoBean.class, jzId);
////		CartTemp cart = cartMap.get(beidaBean.horseType);
//		SOSYBHistoryResp.Builder resp = SOSYBHistoryResp.newBuilder();
//		// 历史记录
//		List<byte[]> sosHistoryList = Redis.getInstance().lrange(SOS_HISTORY_KEY + jzId, 0, -1);
//		for (byte[] bs : sosHistoryList) {
//			SOSHistory sosBean = (SOSHistory) SerializeUtil.unserialize(bs);
//			SOSYBHistory.Builder sos = SOSYBHistory.newBuilder();
//			sos.setOtherJzId(sosBean.otherJzId);
//			sos.setOtherJzName(sosBean.otherJzName);
//			sos.setHorseType(sosBean.horseType);
//			sos.setMsgType(sosBean.msgType);
//			resp.addSosList(sos.build());
//		}
////		//重置新历史标记
////		YaBiaoBean ybBean=HibernateUtil.find(YaBiaoBean.class, jz.id);
////		if(ybBean!=null){
////			ybBean.isNew4History=false;
////			HibernateUtil.save(ybBean);
////		}
//	}
//	/**
//	 * @Description: 获取押镖求助列表
//	 * @param id
//	 * @param builder
//	 * @param session
//	 */
//	public void getYaBiaoSOSList(int id, Builder builder, IoSession session) {
//		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
//		if (jz == null) {
//			log.error("答复协助君主不存在");
//			return;
//		}
//		SOSYBHistoryResp.Builder resp = SOSYBHistoryResp.newBuilder();
//		Long jzId = jz.id;
//		// 历史记录
//		List<byte[]> sosHistoryList = Redis.getInstance().lrange(SOS_HISTORY_KEY + jzId, 0, -1);
//		Date now =new Date();
//		for (byte[] bs : sosHistoryList) {
//			SOSHistory sosBean = (SOSHistory) SerializeUtil.unserialize(bs);
//			if(sosBean!=null&&sosBean.startTime!=null&&DateUtils.is(sosBean.startTime, now, x) sosBean.startTime)
//			SOSYBHistory.Builder sos = SOSYBHistory.newBuilder();
//			sos.setOtherJzId(sosBean.otherJzId);
//			sos.setOtherJzName(sosBean.otherJzName);
//			sos.setHorseType(sosBean.horseType);
//			sos.setMsgType(sosBean.msgType);
//			resp.addSosList(sos.build());
//		}
//		AnswerYaBiaoHelpReq.Builder req = (AnswerYaBiaoHelpReq.Builder) builder;
//		long ybJzId = req.getJzId();
//		log.info("君主--{}答复君主--{}的协助邀请", jzid,ybJzId);
//		int code = req.getCode();
//		if (ybJzId == 0) {
//			log.error("{}答复协助目标不存在", jzid);
//			return;
//		}
//		if (code <= 0) {
//			log.error("{}答复协助编码--{}错误", jzid, code);
//			return;
//		}
//		JunZhu ybJZ = HibernateUtil.find(JunZhu.class, ybJzId);
//		AnswerYaBiaoHelpResp.Builder resp = AnswerYaBiaoHelpResp.newBuilder();
//		if (jzid.equals(ybJzId)) {
//			log.info("{}不能协助自己运镖{}", jzid, ybJzId);
//			resp.setCode(70);
//			resp.setName(ybJZ.name);
//			session.write(resp.build());
//			return;
//		}
//		AllianceBean aBean = AllianceMgr.inst.getAllianceByJunZid(jzid);
//		if (aBean == null) {
//			log.info("{}不在联盟，答复押镖协助失败", jzid);
//			resp.setCode(30);
//			resp.setName(ybJZ.name);
//			session.write(resp.build());
//			return;
//		}
//		
//		AllianceBean askBean = AllianceMgr.inst.getAllianceByJunZid(ybJzId);
//		if (askBean == null) {
//			log.info("{}协助的目标{}没有联盟，答复押镖协助失败", jzid, ybJzId);
//			resp.setCode(40);
//			resp.setName(ybJZ.name);
//			session.write(resp.build());
//			return;
//		}
//		if(askBean.id!=aBean.id){
//			log.info("{}协助的目标{}的联盟不是同一个，答复押镖协助失败", jzid, ybJzId);
//			resp.setCode(40);
//			resp.setName(ybJZ.name);
//			session.write(resp.build());
//			return;
//		}
//		SessionUser su = SessionManager.inst.findByJunZhuId(ybJzId);
//		if(su==null){
//			log.info("{}协助的目标{}不在线，答复押镖协助失败", jzid, ybJzId);
//			resp.setCode(40);
//			resp.setName(ybJZ.name);
//			session.write(resp.build());
//			return;
//		}
//		
//		YaBiaoRobot temp = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJzId);
//		if (temp != null) {
//			log.info("{}协助的目标{}已经运镖，答复押镖协助失败", jzid, ybJzId);
//			resp.setCode(40);
//			resp.setName(ybJZ.name);
//			session.write(resp.build());
//			return;
//		}
//		YaBiaoBean ybbean = HibernateUtil.find(YaBiaoBean.class, jzid);
//		if (ybbean.remainXZ <= 0) {
//			log.info("{}协助押镖次数已用完，答复押镖协助失败", jzid, ybJzId);
//			resp.setCode(60);
//			resp.setName(ybJZ.name);
//			session.write(resp.build());
//			return;
//		}
//		// 判断是否答复过
//		List<Long> helperList = (List<Long>) answerHelpCache4YB.get(ybJzId);
//		if (helperList != null && helperList.contains(jzid)) {
//			log.info("{}已答复{}", jzid, ybJzId);
//			resp.setCode(50);
//			resp.setName(ybJZ.name);
//			session.write(resp.build());
//			return;
//		} else {
//			if (helperList == null) {
//				helperList = new ArrayList<Long>();
//			}
//			// 存储答复队列
//			helperList.add(jzid);
//			answerHelpCache4YB.put(ybJzId, helperList);
//		}
//		
//		if (code == 10) {
//			// 保存协助者
//			if (!saveXieZhuSet(ybJzId, jzid)) {
//				resp.setCode(80);
//				resp.setName(ybJZ.name);
//				session.write(resp.build());
//				return;
//			}
//		}
//		// 答复协助的返回
//		resp.setCode(10);
//		resp.setName(jz.name);
//		session.write(resp.build());
//		
//		AskYaBiaoHelpResp.Builder resp2Asker = AskYaBiaoHelpResp.newBuilder();
//		resp2Asker.setCode(code);
//		XieZhuJunZhu.Builder xzJz = XieZhuJunZhu.newBuilder();
//		xzJz.setJzId(jzid);
//		xzJz.setName(jz.name);
//		xzJz.setRoleId(jz.roleId);
//		
//		//2015年8月31日返回盟友增加的护盾
//		int hudun=(int) (jz.shengMingMax * CanShu.YUNBIAOASSISTANCE_HPBONUS);
//		int mubiaoShengMingMax= JunZhuMgr.inst.getJunZhu(su.session).shengMingMax;
//		int hudunzenyi=hudun*100/mubiaoShengMingMax;
//		xzJz.setAddHuDun(hudun*100/mubiaoShengMingMax);
//		resp2Asker.setJz(xzJz);
//		log.info("通知{}协助这变化成功,君主--{}协助君主--{}押镖，护盾增益{}%", ybJzId,jzid,ybJzId,hudunzenyi);
//		su.session.write(resp2Asker.build());
//	}
	/**
	 * @Description: //答复协助
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void answerHelp2YB(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("答复协助君主不存在");
			return;
		}
		Long jzid = jz.id;
		AnswerYaBiaoHelpReq.Builder req = (AnswerYaBiaoHelpReq.Builder) builder;
		long ybJzId = req.getJzId();
		log.info("君主--{}答复君主--{}的协助邀请", jzid,ybJzId);
		int code = req.getCode();
		if (ybJzId == 0) {
			log.error("{}答复协助目标不存在", jzid);
			return;
		}
		if (code <= 0) {
			log.error("{}答复协助编码--{}错误", jzid, code);
			return;
		}
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
			log.info("{}不在联盟，答复押镖协助失败", jzid);
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
		SessionUser su = SessionManager.inst.findByJunZhuId(ybJzId);
		if(su==null){
			log.info("{}协助的目标{}不在线，答复押镖协助失败", jzid, ybJzId);
			resp.setCode(40);
			resp.setName(ybJZ.name);
			session.write(resp.build());
			return;
		}
		
		YaBiaoRobot temp = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJzId);
		if (temp != null) {
			log.info("{}协助的目标{}已经运镖，答复押镖协助失败", jzid, ybJzId);
			resp.setCode(40);
			resp.setName(ybJZ.name);
			session.write(resp.build());
			return;
		}
		YaBiaoBean ybbean = HibernateUtil.find(YaBiaoBean.class, jzid);
		if (ybbean.remainXZ <= 0) {
			log.info("{}协助押镖次数已用完，答复押镖协助失败", jzid, ybJzId);
			resp.setCode(60);
			resp.setName(ybJZ.name);
			session.write(resp.build());
			return;
		}
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
		
		//2015年8月31日返回盟友增加的护盾
		int hudun=(int) (jz.shengMingMax * CanShu.YUNBIAOASSISTANCE_HPBONUS);
		int mubiaoShengMingMax= JunZhuMgr.inst.getJunZhu(su.session).shengMingMax;
		int hudunzenyi=hudun*100/mubiaoShengMingMax;
		xzJz.setAddHuDun(hudun*100/mubiaoShengMingMax);
		resp2Asker.setJz(xzJz);
		log.info("通知{}协助这变化成功,君主--{}协助君主--{}押镖，护盾增益{}%", ybJzId,jzid,ybJzId,hudunzenyi);
		su.session.write(resp2Asker.build());
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
	 * @param ybJzId
	 * @param xzJzId
	 */
	private void removeXieZhu2Set(Long ybJzId, String name, Long xzJzId,
			IoSession session) {
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
	 * @Description: 发送协助请求
	 * @param abean
	 * @param jz
	 * @param session
	 */
	public void broadaskHelpEvent(AllianceBean abean, JunZhu jz,
			String horseName, IoSession session) {
		String chatContent = xiezhuContent.replace("**", horseName);
		ChatPct.Builder b = ChatPct.newBuilder();
		Channel value = Channel.valueOf(1);// 联盟频道1
		b.setChannel(value);
		b.setContent(chatContent);
		b.setIsYBHelp(true);
		b.setSenderId(jz.id);
		b.setSenderName(jz.name);
		ChatMgr.inst.addMission(PD.C_Send_Chat, session, b);
	}

	/**
	 * @Description: 广播状态
	 * @param sc
	 * @param ybrobot
	 * @param code
	 */
	public void broadBattleEvent(Scene sc, YaBiaoRobot ybrobot, int code) {
		// 10押送中 20 战斗中 30 保护CD 40到达终点 50镖车摧毁
		BiaoCheState.Builder resp = BiaoCheState.newBuilder();
		resp.setState(code);
		resp.setJunZhuId(ybrobot.jzId);
		resp.setUsedTime(ybrobot.usedTime);
		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, ybrobot.jzId);
		if (ybBean == null) {
			log.error("广播战斗状态失败，押镖人未开启押镖功能{}", ybrobot.jzId);
			return;
		}
		resp.setHp(ybrobot.hp);
		resp.setWorth(ybrobot.worth);
		int protectTime = ybrobot.protectCD
				- ((int) (System.currentTimeMillis() - ybrobot.endBattleTime) / 1000);
		resp.setBaohuCD(protectTime > 0 ? protectTime : 0);
		Integer scId = ybJzId2ScIdMap.get(ybrobot.jzId);
		if (scId == null) {
			log.error("镖车所在场景未找到{}", ybrobot.jzId);
			return;
		}
		Set<Long> jbSet = BigSwitch.inst.ybMgr.jbJzList2ScIdMap.get(scId);
		if (jbSet == null) {
			log.info("场景{}中没有劫镖者，不广播", sc.name);
			return;
		}
		for (Long jId : jbSet) {
			SessionUser su = SessionManager.inst.findByJunZhuId(jId);
			if (su != null) {
				log.info("广播{}的镖车状态{}给{}", ybrobot.jzId, code, jId);
				su.session.write(resp.build());
			} else {
				log.error("广播{}的镖车状态{}给{}失败，未找到session,劫镖者已下线", ybrobot.jzId,code, jId);
			}
		}
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
	 * @Description:押镖人移出押镖场景
	 * @param jzId
	 */
	public void removeYbJz2Map(Long jzId) {
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
			//TODO 处理系统镖车机器人
			removeSysRobotInfo2Sc(scId, jzId);
			log.info("从场景-{}移除系统镖车-{}成功", scId, jzId);
		}else{
			removeJzRobotInfo2Sc(scId, jzId);
			log.info("从场景-{}移除君主镖车-{}成功", scId, jzId);
		}
	}
	
	/**
	 * @Description 从场景移除君主镖车信息
	 * @param scId
	 * @param jzId
	 */
	protected void removeJzRobotInfo2Sc(Integer scId, Long jzId) {
		//玩家镖车机器人
		YaBiaoBean ybbean = HibernateUtil.find(YaBiaoBean.class, jzId);
		// 重置马车等数据 
		ybbean.horseType = -1;
		//重置  护盾 =》属性移到ybrobot中
//		ybr.hudun=0;
//		ybr.hudunMax=0;
		HibernateUtil.save(ybbean);
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
			sc.exitForYaBiaoRobot(jzId);
		}
	}

	/**
	 * @Description 从场景中移除系统镖车信息
	 * @param scId 
	 * @param jzId
	 */
	protected void removeSysRobotInfo2Sc(Integer scId, Long jzId) {
		log.info("从场景-{}移除系统镖车-{}", scId, jzId);
		ybJzId2ScIdMap.remove(jzId);
		Scene sc = yabiaoScenes.get(scId);
		// 移出押镖机器人
		if (sc != null) {
			sc.exitForYaBiaoRobot(jzId);
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

	// 发送邮件给协助者 2015年11月27日改为发盟友快报
	public void sendKuaiBao2XieZhu(long  ybJzId, String ybJzName, long jzId,int gongxian,
			boolean isSuccess) {
		String award ="";
		if(gongxian>0){
			award = "0:" + gongxianCODE + ":" + gongxian;
		}
		if (isSuccess) {
			String content = "";
			int	msgType=SuBaoConstant.mengyouwancheng;
			long startTime=System.currentTimeMillis();
			PromptMsgMgr.inst.saveLianMengKuaiBao(jzId, ybJzId, ybJzName, msgType, startTime, award, content);
			log.info("发送协助{}押镖成功快报给{}", jzId, ybJzId);
		} else {
			String content = "";
			int	msgType=SuBaoConstant.mengyoubeisha;
			long startTime=System.currentTimeMillis();
			PromptMsgMgr.inst.saveLianMengKuaiBao(jzId, ybJzId, ybJzName, msgType, startTime, award, content);
			log.info("发送协助{}押镖失败快报给{}", jzId, ybJzId);
		}
	}
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
	public void pushJbJz2Map(Long jzId, int scId) {
		// 存入劫镖Map
		jbJz2ScIdMap.put(jzId, scId);
		Set<Long> jbSet = new HashSet<Long>();
		if (jbJzList2ScIdMap.get(scId) != null) {
			jbSet = jbJzList2ScIdMap.get(scId);
			log.info("场景已存储劫镖人数-{}", jbSet.size());
		}
		jbSet.add(jzId);
		jbJzList2ScIdMap.put(scId, jbSet);
	}

	/**
	 * @Description: //劫镖人移出劫镖map
	 * @param jzId
	 */
	public void removeJbJz2Map(Long jzId) {
		// 从ybJzList2ScIdMap移除君主
		Integer scId = jbJz2ScIdMap.get(jzId);
		if (scId == null)
			return;
		Set<Long> jbSet = jbJzList2ScIdMap.get(scId);
		if (jbSet != null) {
			boolean res = jbSet.remove(jzId);
			jbJzList2ScIdMap.put(scId, jbSet);
			log.info("从场景-{}移除君主-{}，更新押镖君主列表成功?{}", scId, jzId, res);
		} else {
			log.error("从场景-{}移除君主-{}失败,未找到押镖君主List", scId, jzId);
		}
		jbJz2ScIdMap.remove(jzId);
		log.info("从场景-{}移除君主-{}成功", scId, jzId);
	}

	private YaBiaoBean initJunZhuYBInfo(Long jzId, int vipLevel) {
		// 获取数据库中是否有此记录，有的话什么也不做
		log.info("初始化{}的押镖数据，vip等级-{}", jzId, vipLevel);
		YaBiaoBean bean = new YaBiaoBean();
		bean.junZhuId = jzId;
		bean.zuheId=-1;
		bean.usedYB = 0;
		bean.remainYB = getYaBiaoCountForVip(vipLevel);
		bean.usedJB = 0;
		bean.remainJB = getJieBiaoCountForVip(vipLevel);
		bean.usedXZ = 0;
		bean.remainXZ = getXiezhuCountForVip(vipLevel);
		bean.usedAskXZ = 0;
		bean.remainAskXZ = getAskXiezhuCountForVip(vipLevel);
//		bean.lastYBDate = null;
		bean.lastJBDate = null;
		bean.lastShowTime = null;
		//2015年9月1日拆分字段到新表
//		bean.successYB = 0;
//		bean.successJB = 0;
//		bean.historyJB = 0;
//		bean.historyYB = 0;
//		bean.isYaBiao = false;
//		bean.initYBTime = new Date();
		bean.horseType = -1;// 随机一匹马 1 2 3 4 5 （-1 0表示没有马）
		bean.isNew4History=false;
		bean.isNew4Enemy=false;
		bean.todayBuyJBTimes = 0;
		bean.todayBuyYBTimes = 0;
		MC.add(bean, jzId);
		HibernateUtil.insert(bean);
		log.info("玩家id是 ：{}的 押镖数据库记录YaBiaoInfo生成成功", jzId);
		return bean;
	}

	public YaBiaoBean resetYBBean(YaBiaoBean bean, int vipLevel) {
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
			bean.usedJB = 0;
			bean.remainJB = getJieBiaoCountForVip(vipLevel);
			bean.usedXZ = 0;
			bean.remainXZ = getXiezhuCountForVip(vipLevel);
			bean.usedAskXZ = 0;
			bean.remainAskXZ = getAskXiezhuCountForVip(vipLevel);
			bean.todayBuyJBTimes = 0;
			bean.todayBuyYBTimes = 0;
			// bean.horseType=0;//新一天是否保存之前没用的马车
			bean.lastShowTime = new Date();
			HibernateUtil.save(bean);
		}else{
			// do nothing
			log.info("还是今天，不重置用户押镖数据--君主ID--{}", bean.junZhuId);
		}
		return bean;
	}

	/**
	 * @Description: 战斗结束进行战斗数据的更新
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void settleJieBiaoResult(long ybJzId,  IoSession session) {
		long time = System.currentTimeMillis();
		JunZhu dajieJz = JunZhuMgr.inst.getJunZhu(session);
		if (dajieJz == null) {
			log.error("劫镖出错：劫镖君主不存在");
			return;
		}
		long djJzId = dajieJz.id;
		log.info("{}的劫镖结束时间是:{}" ,djJzId, time);
		JunZhu yabiaoJz = HibernateUtil.find(JunZhu.class, ybJzId);
		if (yabiaoJz == null) {
			log.error("劫镖出错：目标君主不存在");
			return;
		}
		YaBiaoBean dajieBean = HibernateUtil.find(YaBiaoBean.class, djJzId);
		if (dajieBean == null) {
			log.error("劫镖战斗结束相关处理出错：劫镖者{}无押镖活动记录", djJzId);
			return;
		} else {
			// 是否重新设置数据
			resetYBBean(dajieBean, dajieJz.vipLevel);
		}
		Scene sc = (Scene) session.getAttribute(SessionAttKey.Scene);

		YaBiaoBean ybbean = HibernateUtil.find(YaBiaoBean.class, ybJzId);
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJzId);
		if(ybr==null){
			log.error("劫镖出错：目标{}镖车不存在",ybJzId);
			return;
		}
		if (ybbean != null) {
			CartTemp cart = cartMap.get(ybbean.horseType);
			//TODO 根据时段收益增益
			int dajieshouyi = (int) ((ybr.worth * cart.robProfit)*BUFF_PROFIT);
			//2015年9月1日拆分字段到新表
			YunBiaoHistory ybHis = HibernateUtil.find(YunBiaoHistory.class, dajieJz.id);
			ybHis.successJB += 1;
			HibernateUtil.save(ybHis);

			HibernateUtil.save(dajieBean);
			dajieJz.tongBi += dajieshouyi;
			HibernateUtil.save(dajieJz);
			JunZhuMgr.inst.sendMainInfo(session);// 推送铜币信息
			BattleResult.Builder resp = BattleResult.newBuilder();
			resp.setExp(0);
			resp.setMoney(dajieshouyi);
			session.write(resp.build());
			log.info("劫镖者{}劫镖成功，收益{}", dajieJz.id, dajieshouyi);
			ActLog.log.LootDart(dajieJz.id, dajieJz.name, ActLog.vopenid, ActLog.vopenid, ybJzId, "", dajieshouyi, ybHis.successJB);

			// ===================================劫镖成功，押镖失败退出押镖===================================
			//TODO 押镖结算
			int shouru = (int) (ybr.worth * cart.failProfit*BUFF_PROFIT);
//			2015年10月8日押镖君主收益应该在邮件领取
//			otherJz.tongBi += shouru;
			HibernateUtil.save(yabiaoJz);
			ybbean.horseType = -1;
			//TODO 重置道具
			ybbean.baohu = 0;
			ybbean.baodi = 0;
			ybbean.jiasu = 0;
			ybbean.isNew4History=true;
			ybbean.isNew4Enemy=true;
			HibernateUtil.save(ybbean);

			// 劫镖成功，加入被劫镖者仇人列表 加入劫镖人的记录
			log.info("劫镖者{}加入{}的仇人列表", djJzId, ybJzId);
			saveJieBiaoEnemy(ybJzId,djJzId);

			// 劫镖成功，加入劫镖人的记录
			saveYaBiaoHistory(dajieJz, yabiaoJz, 3, dajieshouyi, ybr.horseType);
			// 劫镖成功，加入被劫镖者的记录
			saveYaBiaoHistory(yabiaoJz, dajieJz, 4, dajieshouyi, ybr.horseType);
			//结算仇恨 FIXME 2015年11月25日玩法变更废弃
//			updateCountryHate(dajieJz.guoJiaId,yabiaoJz.guoJiaId);
			// 结算劫镖者收益，包括物品和劫镖成功次数

			log.info("劫镖者{}劫镖{}结果OK？{}记录", djJzId, ybJzId, true);
			SessionUser ybjzSu = SessionManager.inst.findByJunZhuId(ybJzId);
			if (ybjzSu != null) {
				JunZhuMgr.inst.sendMainInfo(ybjzSu.session);// 推送铜币信息
			} else {
				log.info("推送押镖失败消息给{}失败，未找到session，已下线", djJzId);
			}

			if (ybr != null) {
				// 广播镖车摧毁
				broadBattleEvent(sc, ybr, 50);
			}
			
			// 移除押镖者
			removeYbJz2Map(ybJzId);
			removeXieZhu4EndYB(yabiaoJz.name, ybJzId,CanShu.YUNBIAOASSISTANCE_GAIN_FAIL, false);
			// 发送押镖失败邮件
			//TODO 增加劫镖人信息
			sendFailMail2YaBiaoRen(yabiaoJz.name,dajieJz.name, shouru);
			//推送新历史记录给劫镖者 2015年9月14日1.0自身主动攻击的记录红点不允许推到一级界面按钮上
//			pushYBRecord(jId, true, false);
			//推送新历史记录给押镖者
			pushYBRecord(ybJzId, true, true);
		} else {
			BattleResult.Builder resp = BattleResult.newBuilder();
			resp.setExp(0);
			resp.setMoney(0);
			session.write(resp.build());
			log.info("未找到被劫镖者{}，结算失败", ybJzId);
		}
	}
	/**
	 * @Description: 战斗结束进行战斗数据的更新
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void settleJieBiaoResult4废弃(int id, Builder builder, IoSession session) {
		long time = System.currentTimeMillis();
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("劫镖出错：君主不存在");
			return;
		}
		log.info("{}的劫镖结束时间是:{}" ,jz.id, time);
		JieBiaoResult.Builder req = (JieBiaoResult.Builder) builder;
		long otherId = req.getEnemyId();
		long winId = req.getWinId();
		long jId = jz.id;
		JunZhu otherJz = HibernateUtil.find(JunZhu.class, otherId);
		if (otherJz == null) {
			log.error("劫镖出错：目标君主不存在");
			return;
		}
		YaBiaoBean bean = HibernateUtil.find(YaBiaoBean.class, jId);
		if (bean == null) {
			log.error("劫镖战斗结束相关处理出错：劫镖者{}无押镖活动记录", jId);
			return;
		} else {
			// 是否重新设置数据
			resetYBBean(bean, jz.vipLevel);
		}
		Scene sc = (Scene) session.getAttribute(SessionAttKey.Scene);
		
		YaBiaoBean ybbean = HibernateUtil.find(YaBiaoBean.class, otherId);
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(otherId);
		if(ybr==null){
			log.error("劫镖出错：目标{}镖车不存在",otherId);
			return;
		}
		if (ybbean != null) {
			CartTemp cart = cartMap.get(ybbean.horseType);
			List<YBPveNpcInfo> npcInfoList = req.getNpcInfosList();
			if (npcInfoList == null || npcInfoList.size() <= 0) {
				log.error("劫镖战斗结束相关处理出错：结算NPC列表为空{}", jId);
				return;
			}
			int remainHP = 0;
			int remainHudun = 0;
			Map<Integer, Integer> ybNpc = new HashMap<Integer, Integer>();
			for (YBPveNpcInfo npcInfo : npcInfoList) {
				if (npcInfo.getNpcId() < 100)
					continue;
				if (npcInfo.getNpcId() == 101) {
					remainHP = (int) (npcInfo.getRemainHP()  + cart.recoveryRate* otherJz.shengMingMax);
					remainHP = remainHP > otherJz.shengMingMax ? otherJz.shengMingMax: remainHP;
					if (npcInfo.hasRemainHudun()) {
						remainHudun = npcInfo.getRemainHudun();
					}
					log.info("君主-{},剩余血量-{}", npcInfo.getNpcId(), remainHP);
				} else {
					int npcHp = (int) (npcInfo.getRemainHP());// * (1 + cart.recoveryRate));
					ybNpc.put(npcInfo.getNpcId(), npcHp);
				}
			}
			
			// 劫镖失败，保存NPC
//			ybNpcMap.put(otherId, ybNpc);
			if (winId == otherId) {
//				ybbean.hp = remainHP;// 保存血量
//				ybbean.hudun = remainHudun;// 保存护盾
				ybbean.isNew4History=true;
				HibernateUtil.save(ybbean);
				ybr.startTime = System.currentTimeMillis();
				ybr.protectCD = cart.protectTime;// 重置保护CD
				ybr.endBattleTime = System.currentTimeMillis();
				ybr.isBattle=false;
				// 劫镖失败，加入劫镖者的记录
				saveYaBiaoHistory(jz,otherJz, 1, 0, ybr.horseType);
				
				// 劫镖失败，加入押镖者的记录
				saveYaBiaoHistory(otherJz, jz, 2, 0, ybr.horseType);
				// 劫镖失败结算 更加新记录标记
//				bean.isNew4History=true;
				HibernateUtil.save(bean);
				BattleResult.Builder resp = BattleResult.newBuilder();
				resp.setExp(0);
				resp.setMoney(0);
				session.write(resp.build());
				if (ybr != null) {
					// 广播镖车保护中
					broadBattleEvent(sc, ybr, 30);
				}
				//推送新历史记录给劫镖者 2015年9月14日1.0自身主动攻击的记录红点不允许推到一级界面按钮上
//				pushYBRecord(jId, true, false);
				//推送新历史记录给押镖者
				pushYBRecord(otherId, true, false);
				return;
				// ===================================劫镖失败处理结束===================================
			}
			//TODO 根据时段收益增益
			int dajieshouyi = (int) ((ybr.worth * cart.robProfit)*BUFF_PROFIT);
			//2015年9月1日拆分字段到新表
			YunBiaoHistory ybHis = HibernateUtil.find(YunBiaoHistory.class, jz.id);
			ybHis.successJB += 1;
			HibernateUtil.save(ybHis);
//			bean.isNew4History=true;
			
			HibernateUtil.save(bean);
			jz.tongBi += dajieshouyi;
			HibernateUtil.save(jz);
			JunZhuMgr.inst.sendMainInfo(session);// 推送铜币信息
			BattleResult.Builder resp = BattleResult.newBuilder();
			resp.setExp(0);
			resp.setMoney(dajieshouyi);
			session.write(resp.build());
			log.info("劫镖者{}劫镖成功，收益{}", jz.id, dajieshouyi);
			ActLog.log.LootDart(jz.id, jz.name, ActLog.vopenid, ActLog.vopenid, otherId, "", dajieshouyi, ybHis.successJB);
			
			// ===================================劫镖成功，押镖失败退出押镖===================================
			//TODO 押镖结算
			int shouru = (int) (ybr.worth * cart.failProfit*BUFF_PROFIT);
//			2015年10月8日押镖君主收益应该在邮件领取
			HibernateUtil.save(otherJz);
//			ybbean.worth = 0;
			ybbean.horseType = -1;
			ybbean.isNew4History=true;
			ybbean.isNew4Enemy=true;
			HibernateUtil.save(ybbean);
			
			// 劫镖成功，加入被劫镖者仇人列表 加入劫镖人的记录
			log.info("劫镖者{}加入{}的仇人列表", jId, otherId);
			saveJieBiaoEnemy(otherId,jId);
			
			// 劫镖成功，加入劫镖人的记录
			saveYaBiaoHistory(jz, otherJz, 3, dajieshouyi, ybr.horseType);
			// 劫镖成功，加入被劫镖者的记录
			saveYaBiaoHistory(otherJz, jz, 4, dajieshouyi, ybr.horseType);
			//结算仇恨
			updateCountryHate(jz.guoJiaId,otherJz.guoJiaId);
			// 结算劫镖者收益，包括物品和劫镖成功次数
			
			log.info("劫镖者{}劫镖{}结果OK？{}记录", jId, otherId, true);
			SessionUser otherSu = SessionManager.inst.findByJunZhuId(otherId);
			if (otherSu != null) {
				JunZhuMgr.inst.sendMainInfo(otherSu.session);// 推送铜币信息
			} else {
				log.info("推送押镖失败消息给{}失败，未找到session，已下线", jId);
			}
			
			if (ybr != null) {
				// 广播镖车摧毁
				broadBattleEvent(sc, ybr, 50);
			}
			
			// 移除押镖者
			removeYbJz2Map(otherId);
			removeXieZhu4EndYB(otherJz.name, otherId,CanShu.YUNBIAOASSISTANCE_GAIN_FAIL, false);
			// 发送押镖失败邮件
			//TODO 增加劫镖人信息
			sendFailMail2YaBiaoRen(otherJz.name,jz.name, shouru);
			//推送新历史记录给劫镖者 2015年9月14日1.0自身主动攻击的记录红点不允许推到一级界面按钮上
//			pushYBRecord(jId, true, false);
			//推送新历史记录给押镖者
			pushYBRecord(otherId, true, true);
		} else {
			BattleResult.Builder resp = BattleResult.newBuilder();
			resp.setExp(0);
			resp.setMoney(0);
			session.write(resp.build());
			log.info("未找到被劫镖者{}，结算失败", otherId);
		}
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

	private void initHuDun4YBJZ(Long jzid,YaBiaoRobot ybr) {
		HashSet<Long> xiezhuSet = xieZhuCache4YBJZ.get(jzid);
		int hudun = 0;
		if (xiezhuSet != null) {
			for (Long xzJzId : xiezhuSet) {
				JunZhu jz = HibernateUtil.find(JunZhu.class, xzJzId);
				if (jz == null)
					continue;
				hudun += jz.shengMingMax * CanShu.YUNBIAOASSISTANCE_HPBONUS;
			}
		}
//		YaBiaoBean ybbean = HibernateUtil.find(YaBiaoBean.class, jzid);
		log.info("生成君主{}的护盾---血量{}",jzid,hudun);
		ybr.hudun = hudun;
		ybr.hudunMax = hudun;
//		HibernateUtil.save(ybbean);
	}

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
		boolean isExit = DB.lexist((ENEMY_KEY + jzId), enemyId + "");
		if (!isExit) {
			Long sizeAfterAdd = DB.rpush4YaBiao((ENEMY_KEY + jzId), enemyId
					+ "");
			if (sizeAfterAdd > enemySize) {
				Redis.getInstance().lpop(ENEMY_KEY + jzId);
			}
		}
	}

//	// 押镖结束进行押镖的更新 测试用的方法
//	public void settleYaBiaoResult(int id, Builder builder, IoSession session) {
//		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
//		if (jz == null) {
//			log.error("押镖结算出错：君主不存在");
//			return;
//		}
//		log.info("junzhu:{}押镖成功，进入结算", jz.id);
//		EndYabiao.Builder req = (EndYabiao.Builder) builder;
//		int roomId = req.getRoomId();
//		int result = req.getResult();
//		Scene sc = yabiaoScenes.get(roomId);
//		// //移出押镖
//		// if(sc!=null){
//		// log.info("押镖完毕,移出押镖者{}",jz.id);
//		// sc.exitForTrasn(jz.id);
//		// }
//		YaBiaoInfo ybbean = HibernateUtil.find(YaBiaoInfo.class, jz.id);
//		if (result == 10) {
//			ybbean.successYB += 1;
//			log.info("押镖成功,{}现在的成功押镖次数为{}", jz.id, ybbean.successYB);
//		}
//		HibernateUtil.save(ybbean);
//		YBRobot ybrobot = (YBRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap
//				.get(jz.id);
//		if (ybrobot != null) {
//			// 广播押镖结束
//			broadBattleEvent(sc, ybrobot, 40);
//		}
//		// 移除押镖的机器人
//		removeYbJz2Map(jz.id);
//		removeXieZhu4EndYB(jz.name, jz.id, CanShu.YUNBIAOASSISTANCE_GAIN_FAIL,
//				false);
//		session.write(PD.S_ENDYABIAO_RESP);
//	}

	/**
	 * @Description: 押镖成功结算
	 * @param jzId
	 * @param roomId
	 */
	public void settleYaBiaoSuccess(Long jzId, int roomId) {
		if(jzId<0){
			//TODO 处理系统镖车机器人
			log.info("系统机器人---{}到达终点开始", jzId);
			YaBiaoRobot4Sys ybrobot = (YaBiaoRobot4Sys) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(jzId);
			if (ybrobot != null) {
				log.info("系统机器人---押镖成功,开始时间为{}，结束时间为{}", ybrobot.startTime,System.currentTimeMillis());
				Scene sc4Sys = yabiaoScenes.get(roomId);
				// 广播押镖结束
				broadBattleEvent(sc4Sys, ybrobot, 40);
				// 移除押镖人的相关信息
				removeYbJz2Map(jzId);
			}
			log.info("系统机器人---{}到达终点结束", jzId);
			return;
		}
		JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
		if (jz == null) {
			log.error("押镖结算出错：押镖君主不存在{}", jzId);
			return;
		}
		SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
		Scene sc = yabiaoScenes.get(roomId);
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(jz.id);
		if (ybr != null) {
			log.info("押镖成功,开始时间为{}，结束时间为{}", ybr.startTime,System.currentTimeMillis());
			// 广播押镖结束
			broadBattleEvent(sc, ybr, 40);
		}else{
			log.error("押镖结算出错：押镖君主--{}镖车机器人未找到", jzId);
			return;
		}
		YaBiaoBean ybbean = HibernateUtil.find(YaBiaoBean.class, jzId);

		// 结算收益
		//		CartTemp cart = cartMap.get(ybbean.horseType);
		int shouru = ybr.worth ;
//		2015年10月8日押镖君主收益应该在邮件领取
//		jz.tongBi += shouru;
		HibernateUtil.save(jz);
		final int horseType = ybbean.horseType;
		// 重置数据
//		ybbean.worth = 0;
		ybbean.horseType = -1;
		//TODO 重置道具
		ybbean.baohu = 0;
		ybbean.baodi = 0;
		ybbean.jiasu = 0;
		//2015年9月1日拆分字段到新表
		YunBiaoHistory ybHis =getYunBiaoHistory( jz.id);
		ybHis.successYB += 1;
		HibernateUtil.save(ybHis);
		log.info("{}押镖成功,现在的成功押镖次数为{}", jz.id, ybHis.successYB);
		HibernateUtil.save(ybbean);
		if (su != null) {
			JunZhuMgr.inst.sendMainInfo(su.session);// 推送铜币信息
		} else {
			log.info("推送押镖成功消息给{}失败，未找到session，已下线", jz.id);
		}
		EventMgr.addEvent(ED.YA_BIAO_SUCCESS, new Object[]{jz,horseType});
		// 移除押镖人的相关信息
		removeYbJz2Map(jz.id);
		// 移出协助者
		removeXieZhu4EndYB(jz.name, jz.id, CanShu.YUNBIAOASSISTANCE_GAIN_SUCCEED, true);
		// 移出押镖场景
		// if(sc!=null){
		// log.info("押镖完毕,移出押镖者{}",jz.id);
		// sc.exitForTrasn(jz.id);
		// }
		//TODO 根据策划配置求出奖励
		String award="";
		// 发送快报 给押镖人2015年11月27日改成发快报 不发邮件
		sendKuaiBao2YBJZ(jzId, jz.name, award);

	}

	/**
	 * @Description: //广播镖车进入场景
	 * @param ybr
	 * @param jz
	 * @param sc
	 */
	public void broadBiaoCheINScene(YaBiaoRobot ybr, JunZhu jz, Scene sc) {
		YabiaoJunZhuInfo.Builder resp = YabiaoJunZhuInfo.newBuilder();
		resp.setJunZhuId(jz.id);
		resp.setJunZhuName(jz.name);
		resp.setLevel(jz.level);
		AllianceBean ybabean = AllianceMgr.inst.getAllianceByJunZid(jz.id);
		resp.setLianMengName(ybabean == null ? "" : ybabean.name);
		int protectTime = ybr.protectCD
				- ((int) (System.currentTimeMillis() - ybr.endBattleTime) / 1000);
		resp.setBaohuCD(protectTime > 0 ? protectTime : 0);
		resp.setTotalTime(ybr.totalTime);
		resp.setUsedTime(ybr.usedTime);
//		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, ybr.jzId);
		resp.setHp(ybr.hp);
		resp.setMaxHp(jz.shengMingMax);
		resp.setWorth(ybr.worth);
		resp.setMaxWorth(ybr.worth);
		resp.setState(ybr.isBattle ? 20 : (protectTime > 0 ? 30 : 10));
		// 10押送中 20 战斗中 30保护CD
		int zhanli = JunZhuMgr.inst.getJunZhuZhanliFinally(jz);
		resp.setZhanLi(zhanli);
		resp.setState(ybr.isBattle ? 20 : (protectTime > 0 ? 30 : 10));
		// 10押送中20战斗中 30保护CD
		Integer scId = BigSwitch.inst.ybMgr.ybJzId2ScIdMap.get(ybr.jzId);
		if (scId == null) {
			log.error("镖车所在场景未找到{},不广播", ybr.jzId);
			return;
		}
		resp.setPathId(ybr.pathId);

		Set<Long> jbSet = BigSwitch.inst.ybMgr.jbJzList2ScIdMap.get(scId);
		if (jbSet == null) {
			return;
		}
		resp.setHorseType(ybr.horseType);
		resp.setJunzhuGuojia(jz.guoJiaId);
		
		//2015年8月31日返回盟友增加的护盾
		resp.setHuDun(ybr.hudun*100/jz.shengMingMax);
		for (Long jId : jbSet) {
			SessionUser su = SessionManager.inst.findByJunZhuId(jId);
			if (su == null)
				continue;
			boolean isExit = DB.lexist((ENEMY_KEY + jId), jz.id + "");
			resp.setIsEnemy(isExit);
			su.session.write(resp.build());
		}

	}

	/**
	 * @Description: 发送押镖结果快报给押镖人  2015年11月27日改成发快报 不发邮件
	 * @param jzName
	 */
	public void sendKuaiBao2YBJZ(long ybJzId, String jzName, String award) {
	//TODO 配置
		String content = "";
		int	msgType=0;
		long startTime=System.currentTimeMillis();
		long otherJzId=-1L;
		String otherJzName="";
		PromptMsgMgr.inst.saveLianMengKuaiBao(ybJzId, otherJzId, otherJzName, msgType, startTime, award, content);
		log.info("发送押镖成功快报给--{}",  ybJzId);
	}
	/**
	 * @Description: 发送押镖成功邮件给押镖人
	 * @param jzName
	 */
	public void sendSuccessMail2YaBiaoRen(String jzName, int shouru) {
		
		Mail cfg = EmailMgr.INSTANCE.getMailConfig(50001);
		String content = cfg.content;
		String fuJian = "0:" + tongbiCODE + ":" + shouru;
		boolean ok = EmailMgr.INSTANCE.sendMail(jzName, content, fuJian,cfg.sender, cfg, "");
		log.info("发送押镖成功邮件给{},OK--{}", jzName, ok);
	}

	/**
	 * @Description: 发送押镖失败邮件给押镖人
	 * @param ybjzName 押镖君主,jbjzName 劫镖君主
	 */
	public void sendFailMail2YaBiaoRen(String ybjzName, String jbjzName,int shouru) {
		Mail cfg = EmailMgr.INSTANCE.getMailConfig(50002);
		//增加劫镖人信息
		String content = cfg.content.replace("***", jbjzName);
		String fuJian = "0:" + tongbiCODE + ":" + shouru;
		boolean ok = EmailMgr.INSTANCE.sendMail(ybjzName, content, fuJian,cfg.sender, cfg, "");
		log.info("发送押镖失败邮件给{},OK--{}", ybjzName, ok);
	}

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

	/**
	 * @Description: 根据vip等级获取协助次数
	 * @param vipLev
	 * @return
	 */
	public int getXiezhuCountForVip(int vipLev) {
		// int value = VipMgr.INSTANCE.getValueByVipLevel(vipLev,
		// VipData.bugYaBiaoTime);
		// int times = PVPConstant.YABIAO_TOTAL_TIMES;
		// times +=PurchaseMgr.inst.getAllUseNumbers(PurchaseConstants.YABIAO,
		// value);
		return CanShu.YUNBIAOASSISTANCE_INVITEDMAXNUM;
	}

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
		YaBiaoBean bean = HibernateUtil.find(YaBiaoBean.class, jz.id);
		if (bean == null) {
			log.error("玩家{}购买押镖次数出错：押镖没有开启", jz.id);
			return;
		} else {
			resetYBBean(bean, jz.vipLevel);
		}
		switch (type) {
		case 10:
			buyYaBiaoCount(jz, session, bean);
			break;
		case 20:
			buyJieBiaoCount(jz, session, bean);
			break;
		default:
			log.error("{}请求购买参数错误{}", jz.id, type);
			break;
		}
	}

	public void buyYaBiaoCount(JunZhu jz, IoSession session, YaBiaoBean bean) {
		BuyCountsResp.Builder resp = BuyCountsResp.newBuilder();
		int vipLev = jz.vipLevel;
		int buyYaBiaoCounts = VipMgr.INSTANCE.getValueByVipLevel(vipLev,
				VipData.yabiaoTimes);
		if (bean.todayBuyYBTimes >= buyYaBiaoCounts) {
			log.info("{}够买押镖次数已用完", jz.id);
			resp.setResult(30);
			resp.setLeftJBTimes(bean.remainJB);
			resp.setLeftYBTimes(bean.remainYB);
			return;
		}
		// 获取够买配置
		Purchase pc = getPurchase(bean.todayBuyYBTimes + 1, BUY_YABIAO_COUNT);
		if (pc == null) {
			log.error("没有获取到购买押镖的数据");
			resp.setLeftJBTimes(bean.remainJB);
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
			resp.setLeftJBTimes(bean.remainJB);
			resp.setLeftYBTimes(bean.remainYB);
			return;
		}
		YuanBaoMgr.inst.diff(jz, -yuanbao, 0,PurchaseMgr.inst.getPrice(BUY_YABIAO_COUNT),YBType.YB_BUY_YABIAO_CISHU, "押镖次数购买");
		HibernateUtil.save(jz);
		JunZhuMgr.inst.sendMainInfo(session);

		// 保存够买次数
		bean.remainYB += count;
		bean.todayBuyYBTimes += 1;
		HibernateUtil.save(bean);

		resp.setResult(10);
		resp.setLeftJBTimes(bean.remainJB);
		resp.setLeftYBTimes(bean.remainYB);
		resp.setUsedJBVip(bean.todayBuyJBTimes);
		resp.setUsedYBVip(bean.todayBuyYBTimes);
		session.write(resp.build());
	}

	public void buyJieBiaoCount(JunZhu jz, IoSession session, YaBiaoBean bean) {
		BuyCountsResp.Builder resp = BuyCountsResp.newBuilder();
		int vipLev = jz.vipLevel;
		int buyYaBiaoCounts = VipMgr.INSTANCE.getValueByVipLevel(vipLev,VipData.jiebiaoTimes);
		if (bean.todayBuyJBTimes >= buyYaBiaoCounts) {
			log.info("{}够买劫镖次数已用完", jz.id);
			resp.setResult(30);
			resp.setLeftJBTimes(bean.remainJB);
			resp.setLeftYBTimes(bean.remainYB);
			return;
		}

		// 获取够买配置
		Purchase pc = getPurchase(bean.todayBuyYBTimes + 1, BUY_JIEBIAO_COUNT);
		if (pc == null) {
			log.error("没有获取到购买劫镖次数的数据");
			resp.setLeftJBTimes(bean.remainJB);
			resp.setLeftYBTimes(bean.remainYB);
			resp.setResult(40);
			return;
		}
		// 购买的次数
		int count = (int) pc.getNumber();
		int yuanbao = pc.getYuanbao();
		if (jz.yuanBao < yuanbao) {
			log.info("{}够买劫镖次数失败，元宝不足", jz.id);
			resp.setResult(20);
			resp.setLeftJBTimes(bean.remainJB);
			resp.setLeftYBTimes(bean.remainYB);
			return;
		}
		YuanBaoMgr.inst.diff(jz, -yuanbao, 0,PurchaseMgr.inst.getPrice(BUY_JIEBIAO_COUNT),YBType.YB_BUY_JIEBIAO_CISHU, "劫镖次数购买");
		HibernateUtil.save(jz);
		JunZhuMgr.inst.sendMainInfo(session);

		// 保存够买次数
		bean.remainJB += count;
		bean.todayBuyJBTimes += 1;
		HibernateUtil.save(bean);

		resp.setResult(10);
		resp.setLeftJBTimes(bean.remainJB);
		resp.setLeftYBTimes(bean.remainYB);
		resp.setUsedJBVip(bean.todayBuyJBTimes);
		resp.setUsedYBVip(bean.todayBuyYBTimes);
		session.write(resp.build());
	}

	private Purchase getPurchase(int huishu, int type) {
		List<Purchase> list = PurchaseMgr.inst.purchaseMap.get(type);// (PVPConstant.BUY_BAI_ZHAN_COUNT);
		Purchase pc = null;
		for (Purchase p : list) {
			if (p.getTime() == huishu) {
				pc = p;
				break;
			}
		}
		if (pc == null) {
			log.error("没有获取到购买押镖/劫镖次数的数据");
			return null;
		}
		return pc;
	}

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

	/**
	 * 请求 劫镖 战斗数据
	 * 
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void YBDateInfoRequest4废弃(int id, IoSession session, Builder builder,
			boolean isPVp) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.error("君主不存在");
			return;
		}
		PvpZhanDouInitReq.Builder req = (qxmobile.protobuf.ZhanDou.PvpZhanDouInitReq.Builder) builder;
		long enemyId = req.getUserId();
		log.info("{}请求 劫镖 战斗数据，敌人是: {}",junZhu.id, enemyId);
		ZhanDouInitError.Builder error = ZhanDouInitError.newBuilder();
		YaBiaoBean enemyBean = HibernateUtil.find(YaBiaoBean.class, enemyId);
		if (enemyBean == null) {
			log.error("玩家挑战对手出错，对手:{}押镖没有开启", enemyId);
			error.setResult("对手押镖功能没有开启");
			session.write(error.build());
			return;
		}
		AllianceBean selfAlliance = AllianceMgr.inst.getAllianceByJunZid(junZhu.id);
		AllianceBean enemyAlliance = AllianceMgr.inst.getAllianceByJunZid(enemyId);
		if (selfAlliance != null && enemyAlliance != null&& selfAlliance.id == enemyAlliance.id) {
			log.error("玩家挑战对手出错，请不要打劫自己的盟友{}:{}-{}:{}", junZhu.id,selfAlliance.id, enemyId, enemyAlliance.id);
			error.setResult("请不要打劫自己的盟友");
			session.write(error.build());
			return;
		}
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(enemyId);
		if(ybr==null){
			log.error("玩家挑战对手出错，对手:{}镖车被摧毁或已到达终点", enemyId);
			error.setResult("镖车已被摧毁或已到达终点");
			session.write(error.build());
			return;
		}
		log.info("玩家{}请求打劫{}的镖车",junZhu.id, enemyId);
		synchronized (ybr) {
			if (ybr.isBattle) {
				log.error("玩家挑战对手出错，对手:{}已经在被打劫", enemyId);
				error.setResult("对手已经在被打劫");
				session.write(error.build());
				return;
			}
			ybr.isBattle = true;
//			ybrobot.battleStart= System.currentTimeMillis();
		}
		int protectTime = ybr.protectCD- ((int) (System.currentTimeMillis() - ybr.endBattleTime) / 1000);
		if (protectTime > 0) {
			log.error("玩家挑战对手出错，对手:{}还在押镖保护期间{}", enemyId, protectTime);
			error.setResult("对手还在押镖保护期间");
			session.write(error.build());
			return;
		}
		// 更新劫镖次数
		YaBiaoBean selfBean = HibernateUtil.find(YaBiaoBean.class, junZhu.id);
		if (selfBean == null) {
			log.error("玩家挑战对手出错，玩家:{}押镖没有开启", junZhu.id);
			error.setResult("玩家押镖功能没有开启");
			session.write(error.build());
			return;
		} else {
			YaBiaoHuoDongMgr.inst.resetYBBean(selfBean, junZhu.vipLevel);
		}
		if (selfBean.remainJB == 0) {
			log.error("玩家挑战对手出错，玩家剩余劫镖次数为:0-{}", junZhu.id);
			error.setResult("玩家剩余劫镖次数为:0");
			session.write(error.build());
			return;
		}
		int distanceTime = BigSwitch.inst.ybMgr.timeDistanceBySeconds(selfBean.lastJBDate);
		if ((distanceTime > 0) && (CanShu.JIEBIAO_CD - distanceTime > 0)) {
			log.error("玩家挑战对手出错，玩家还处于打劫冷却期内-{}：{}", junZhu.id,CanShu.JIEBIAO_CD - distanceTime);
			error.setResult("玩家还处于打劫冷却期内");
			session.write(error.build());
			return;
		}
		selfBean.usedJB += 1;
		selfBean.remainJB -= 1;
		selfBean.lastJBDate = new Date();
		//2015年9月1日拆分字段到新表
		YunBiaoHistory ybHis =getYunBiaoHistory( junZhu.id);
		ybHis.historyJB += 1;// 劫镖历史次数+1
		HibernateUtil.save(ybHis);
		HibernateUtil.save(selfBean);
		// 获取马车配置
		//		CartTemp cart = YabiaoMgr.inst.cartMap.get(enemyBean.horseType);
		log.info("君主:{} 向:{}发起劫镖，今日挑战次数加1,变为:{}，剩余次数减1，变为:{}，挑战时间是:{}",
				junZhu.id, enemyId, selfBean.usedJB, selfBean.remainJB,
				selfBean.lastJBDate);
		// ybrobot.protectCD=cart.protectTime;//重置保护CD
		log.info("劫镖者:{}和运镖者：{}进入战斗状态", junZhu.id, enemyId);
		Scene sc = (Scene) session.getAttribute(SessionAttKey.Scene);
		YaBiaoHuoDongMgr.inst.broadBattleEvent(sc, ybr, 20);
		boolean isNpc = false;
		ZhanDouInitResp.Builder resp = ZhanDouInitResp.newBuilder();
		Group.Builder enemyTroop = Group.newBuilder();
		List<Node> enemys = new ArrayList<ZhanDou.Node>();
		int oppolevel = 0;
		int enemyFlagIndex = 101;
		JunZhu enemy = HibernateUtil.find(JunZhu.class, enemyId);
		if (isNpc) {
			// npc
		} else {
			// 押镖君主
			int zuheId = enemyBean == null ? -1 : enemyBean.zuheId;
			int hp = ybr.hp;
			// 护盾
			int hudun = ybr.hudun;
			int hudunMax = ybr.hudunMax;
			log.info("押镖君主{}的护盾---血量--{}--Max--{}",enemyId,hudun,hudunMax);
			PveMgr.inst.fillYaBiaoJunZhuDataInfo4YB(resp, session, enemys,
					enemy, enemyFlagIndex, zuheId, hp, hudun, hudunMax,
					enemyTroop);
			oppolevel = enemy.level;
			enemyTroop.setMaxLevel(BigSwitch.pveGuanQiaMgr.getGuanQiaMaxId(enemy.id));
		}
		long jId = enemy.id;
		int jlevel = enemy.level;
		// 加载押镖者雇佣兵
		enemyFlagIndex += 1;
//		HashMap<Integer, Integer> ybNpc = (HashMap<Integer, Integer>) ybNpcMap.get(enemyId);
		int ALL=PvpMgr.ALL;
		PvpMgr.inst.setBingData4YaBiao(enemys, enemyBean, enemyFlagIndex, jId, jlevel,
				oppolevel, (byte) ALL);
		enemyTroop.addAllNodes(enemys);
		resp.setEnemyTroop(enemyTroop);

//		int zhandouId = zhandouIdMgr.incrementAndGet(); // 战斗id 后台使用
		int mapId = 0;
		Group.Builder selfTroop = Group.newBuilder();
		List<Node> selfs = new ArrayList<ZhanDou.Node>();
		int zuheId = selfBean == null ? -1 : selfBean.gongJiZuHeId;
		int selfFlagIndex = 1;
		PveMgr.inst.fillJunZhuDataInfo(resp, session, selfs, junZhu,selfFlagIndex, zuheId, selfTroop);
		// 加载劫镖者雇佣兵
		selfFlagIndex += 1;
		int jiebiaoLevel = junZhu.level;// (jlevel + junZhu.level) / 2;
		// 根据双方平均等级产生劫镖者的佣兵jId参数无用
		PvpMgr.inst.setBingData(selfs, selfFlagIndex, jId, jiebiaoLevel, oppolevel,(byte) 0);
		selfTroop.addAllNodes(selfs);
		selfTroop.setMaxLevel(BigSwitch.pveGuanQiaMgr.getGuanQiaMaxId(junZhu.id));
		resp.setSelfTroop(selfTroop);
		resp.setZhandouId(1);//用不到传1
		resp.setMapId(mapId);
		resp.setLimitTime(CanShu.MAXTIME_JIEBIAO);// 根据配置设置战斗时间
		session.write(resp.build());
		// 每日任务：完成一次劫镖活动
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(junZhu.id, DailyTaskConstants.jieBiao, 1));
		// 主线任务完成:劫镖
		EventMgr.addEvent(ED.finish_jiebiao_x, new Object[] { junZhu.id });

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
	/**
	 * @Description 是否在安全区
	 * @param x 
	 * @param z
	 * @param r 安全区半径
	 * @return 
	 */
	public boolean isInSafeArea(float x,float z,double r){
		float x4safe1=0;
		float z4safe1=0;
		float distance1=(x-x4safe1)*(x-x4safe1) +(z-z4safe1)*(z-z4safe1);
		float x4safe2=2;
		float z4safe2=2;
		float distance2=(x-x4safe2)*(x-x4safe2) +(z-z4safe2)*(z-z4safe2);
		float x4safe3=3;
		float z4safe3=3;
		float distance3=(x-x4safe3)*(x-x4safe3) +(z-z4safe3)*(z-z4safe3);
		float x4safe4=4;
		float z4safe4=4;
		float distance4=(x-x4safe4)*(x-x4safe4) +(z-z4safe4)*(z-z4safe4);
		return distance1<r||distance2<r||distance3<r||distance4<r;
	}
	
	public int timeDistanceBySeconds(Date smallDate) {
		Date bigDate = new Date();
		int ldDis = 0;
		if (smallDate == null) {
			return ldDis;
		}
		long differTime = bigDate.getTime() - smallDate.getTime();
		ldDis = (int) (differTime / 1000);
		return ldDis;
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
				YaBiaoBean ybbean = HibernateUtil.find(YaBiaoBean.class, jz.id);
				if(ybbean != null){
					if(ybbean.isNew4Enemy){
						log.info("-----发送押镖有新仇人红点通知");
						FunctionID.pushCanShangjiao(jz.id, session, FunctionID.yabiao_enemy);
					}
					if(ybbean.isNew4History){
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