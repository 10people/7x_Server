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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import log.ActLog;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.PlayerData.State;
import qxmobile.protobuf.Prompt.PromptActionResp;
import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Scene.SpriteMove;
import qxmobile.protobuf.Yabiao.AnswerYaBiaoHelpReq;
import qxmobile.protobuf.Yabiao.AskYaBiaoHelpResp;
import qxmobile.protobuf.Yabiao.BiaoCheState;
import qxmobile.protobuf.Yabiao.BuyAllLifeReviveTimesReq;
import qxmobile.protobuf.Yabiao.BuyAllLifeReviveTimesResp;
import qxmobile.protobuf.Yabiao.BuyCountsReq;
import qxmobile.protobuf.Yabiao.BuyCountsResp;
import qxmobile.protobuf.Yabiao.BuyXuePingReq;
import qxmobile.protobuf.Yabiao.BuyXuePingResp;
import qxmobile.protobuf.Yabiao.CheckYabiaoHelpResp;
import qxmobile.protobuf.Yabiao.EnemiesInfo;
import qxmobile.protobuf.Yabiao.EnemiesResp;
import qxmobile.protobuf.Yabiao.GainFuLiResp;
import qxmobile.protobuf.Yabiao.HorseProp;
import qxmobile.protobuf.Yabiao.HorsePropReq;
import qxmobile.protobuf.Yabiao.HorsePropResp;
import qxmobile.protobuf.Yabiao.HorseType;
import qxmobile.protobuf.Yabiao.JiaSuReq;
import qxmobile.protobuf.Yabiao.JiaSuResp;
import qxmobile.protobuf.Yabiao.MaBianTypeResp;
import qxmobile.protobuf.Yabiao.Move2BiaoCheReq;
import qxmobile.protobuf.Yabiao.RoomInfo;
import qxmobile.protobuf.Yabiao.SetHorseResult;
import qxmobile.protobuf.Yabiao.XieZhuJunZhu;
import qxmobile.protobuf.Yabiao.XieZhuJunZhuResp;
import qxmobile.protobuf.Yabiao.YBHistory;
import qxmobile.protobuf.Yabiao.YBHistoryResp;
import qxmobile.protobuf.Yabiao.YaBiaoHelpResp;
import qxmobile.protobuf.Yabiao.YaBiaoMoreInfoReq;
import qxmobile.protobuf.Yabiao.YaBiaoMoreInfoResp;
import qxmobile.protobuf.Yabiao.YabiaoJunZhuInfo;
import qxmobile.protobuf.Yabiao.YabiaoJunZhuList;
import qxmobile.protobuf.Yabiao.YabiaoMainInfoResp;
import qxmobile.protobuf.Yabiao.YabiaoMenuResp;
import qxmobile.protobuf.Yabiao.YabiaoResult;
import qxmobile.protobuf.Yabiao.isNew4RecordResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.chat.ChatMgr;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.CartNPCName;
import com.manu.dynasty.template.CartNPCTemp;
import com.manu.dynasty.template.CartTemp;
import com.manu.dynasty.template.DescId;
import com.manu.dynasty.template.JunzhuShengji;
import com.manu.dynasty.template.LianMengKeJi;
import com.manu.dynasty.template.MaJu;
import com.manu.dynasty.template.Purchase;
import com.manu.dynasty.template.RobCartXishu;
import com.manu.dynasty.template.VipFuncOpen;
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
import com.qx.alliance.AlliancePlayer;
import com.qx.alliance.building.JianZhuMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.fight.FightMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.prompt.PromptMSG;
import com.qx.prompt.PromptMsgMgr;
import com.qx.prompt.SuBaoConstant;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.robot.RobotSession;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.timeworker.FunctionID;
import com.qx.timeworker.FunctionID4Open;
import com.qx.vip.VipData;
import com.qx.vip.VipMgr;
import com.qx.world.BroadcastMgr;
import com.qx.world.Mission;
import com.qx.world.Player;
import com.qx.world.Scene;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

public class YaBiaoHuoDongMgr extends EventProc implements Runnable {
	public static Logger log = LoggerFactory.getLogger(YaBiaoHuoDongMgr.class.getSimpleName());
	public static YaBiaoHuoDongMgr inst;
	/**押镖君主和场景对应存储，方便君主找到Scid*/ 
//	public ConcurrentHashMap<Long, Integer> ybJzId2ScIdMap= new ConcurrentHashMap<Long, Integer>();
	/**存储每个场景中押镖的君主列表*/
//	public ConcurrentHashMap<Integer, Set<Long>> ybJzList2ScIdMap= new ConcurrentHashMap<Integer, Set<Long>>(); 
	public ConcurrentHashMap<Integer, Scene> yabiaoScenes= new ConcurrentHashMap<Integer, Scene>();
	public static Map<Integer, CartTemp> cartMap = new HashMap<Integer, CartTemp>();
	public static Map<Integer, CartNPCTemp> biaoCheNpcMap= new HashMap<Integer, CartNPCTemp>();
	public static Map<Integer, CartNPCName> cartNpcNameMap;
	public static Map<Integer, YunBiaoSafe> safeAreaMap= new HashMap<Integer, YunBiaoSafe>();
	public static Map<Integer, MaJu> majuMap=new HashMap<Integer, MaJu>();
	public static Map<Integer, RobCartXishu> robCartXishuMap=new HashMap<Integer, RobCartXishu>();
	public static boolean openFlag = false;// 开启标记
	public static final Redis DB = Redis.getInstance();
	public static final String ENEMY_KEY = "enemy_" + GameServer.serverId;
	public static final String HISTORY_KEY = "history_" + GameServer.serverId;
	public static final String SOS_HISTORY_KEY = "sos_history_" + GameServer.serverId;
	public static int historySize = 50;
	public static int sosHistorySize = 50;
	public static int enemySize = 50;
	public static int[][] cartArray;
	public static int totalProbability = 0;
	public static ConcurrentHashMap<Long, HashSet<Long>> xieZhuCache4YBJZ= new ConcurrentHashMap<Long, HashSet<Long>>();// 保存君主A的所有协助者
	public static ConcurrentHashMap<Long, List<Long>> answerHelpCache4YB= new ConcurrentHashMap<Long, List<Long>>();// 保存答复过协助请求的所有君主
	public static int XIEZHU_YABIAO_SIZE =  CanShu.YUNBIAOASSISTANCE_MAXNUM;;// 运镖协助人数上限
	public static int YABIAO_ASKHELP_TIMES = 3;// 运镖协助人数上限
	public static int ANSWER_YBHELP_COLD_TIME = 24 * 60 * 60 * 1000;// 同意时间
	public static String xiezhuContent;
	
	public static ExecutorService syncBroadExecutor = Executors.newSingleThreadExecutor();
	/** 收益倍率 默认100 表示100% **/
	public static  double SHOUYI_PROFIT = 100;
	/**2016年1月25日 需求变更去掉多倍收益时段 改成福利时间*/
	public static  boolean FULITIME_FLAG = false;
	/** 最大押镖场景人数 **/
	//2015年12月29日 
	public static  int MAX_YB_NUM = YunbiaoTemp.yunbiaoScene_modelNum_max;
	public LinkedBlockingQueue<Mission> missions = new LinkedBlockingQueue<Mission>();
	private static Mission exit = new Mission(0, null, null);

	public YaBiaoHuoDongMgr() {
		inst = this;
		initData();
		// 开启线程
		new Thread(this, "YabiaoMgr").start();
		new Thread(new BloodReturn(), "SafeAreaBloodReturn").start();
	}
	
	@SuppressWarnings("unchecked")
	public void initData() {
		
		XIEZHU_YABIAO_SIZE = CanShu.YUNBIAOASSISTANCE_MAXNUM;
		//加载CartTemp马车品质相关
		List<CartTemp> list = TempletService.listAll(CartTemp.class.getSimpleName());
		Map<Integer, CartTemp> cartMap = new HashMap<Integer, CartTemp>();
		for (CartTemp c : list) {
			cartMap.put(c.quality, c);
		}
		YaBiaoHuoDongMgr.cartMap=cartMap;
		//加载CartNPCTemp 属性相关
		List<CartNPCTemp> npcList = TempletService.listAll(CartNPCTemp.class.getSimpleName());
		Map<Integer, CartNPCTemp> biaoCheNpcMap = new HashMap<Integer, CartNPCTemp>();
		for (CartNPCTemp cNpc : npcList) {
			biaoCheNpcMap.put(cNpc.id, cNpc);
		}
		YaBiaoHuoDongMgr.biaoCheNpcMap=biaoCheNpcMap;
		//加载安全区
		List<YunBiaoSafe> safeAreaList = TempletService.listAll(YunBiaoSafe.class.getSimpleName());
		Map<Integer, YunBiaoSafe> safeAreaMap = new HashMap<Integer, YunBiaoSafe>();
		for (YunBiaoSafe sa : safeAreaList) {
			safeAreaMap.put(sa.areaID, sa);
		}
		YaBiaoHuoDongMgr.safeAreaMap=safeAreaMap;
		//加载马具
		List<MaJu> majuList = TempletService.listAll(MaJu.class.getSimpleName());
		Map<Integer, MaJu> majuMap=new HashMap<Integer, MaJu>();
		for (MaJu mj : majuList) {
			majuMap.put(mj.id,mj);
		}
		YaBiaoHuoDongMgr.majuMap=majuMap;
		//加载robCartXishu
		List<RobCartXishu> robCartXishuList = TempletService.listAll(RobCartXishu.class.getSimpleName());
		 Map<Integer, RobCartXishu> robCartXishuMap=new HashMap<Integer, RobCartXishu>();
		for (RobCartXishu xs : robCartXishuList) {
			robCartXishuMap.put(xs.scale,xs);
		}
		YaBiaoHuoDongMgr.robCartXishuMap=robCartXishuMap;
		
		cartNpcNameMap=new HashMap<Integer, CartNPCName>();
		List<CartNPCName> namelist = TempletService.listAll(CartNPCName.class.getSimpleName());
		Map<Integer, CartNPCName> cartNpcNameMap=new HashMap<Integer, CartNPCName>();
		for (CartNPCName c : namelist) {
			cartNpcNameMap.put(c.id, c);
		}
		YaBiaoHuoDongMgr.cartNpcNameMap=cartNpcNameMap;
		// 添加首冲奖励描述
		DescId desc = ActivityMgr.descMap.get(4001);
		xiezhuContent = desc.getDescription();
		initRandomCartData();
		fixOpenFlag();
		refreshMoreProfitState4Start();
	}
	public void refreshMoreProfitState4Start() {
		boolean buff2Profit2=  DateUtils.isInDeadline4Start(YunbiaoTemp.incomeAdd_startTime2, YunbiaoTemp.incomeAdd_endTime2);
		if(buff2Profit2){
			YaBiaoHuoDongMgr.FULITIME_FLAG=true;
		}else{
			YaBiaoHuoDongMgr.FULITIME_FLAG=false;
		}
	}
	/**
	 * @Description 刷新押镖收益状态
	 */
	public void refreshMoreProfitState() {
		int fuliTimeCode=getNowFuliTime();
		String template=YunbiaoTemp.yunbiao_start_broadcast;
		if(fuliTimeCode>0){
			YaBiaoHuoDongMgr.FULITIME_FLAG=true;
			BroadcastMgr.inst.send(template);
		}else{
			YaBiaoHuoDongMgr.FULITIME_FLAG=false;
			BroadcastMgr.inst.send(template);
		}
		syncBroadExecutor.submit(new Runnable() {
			@Override
			public void run() {
				/**2016年1月25日 需求变更去掉多倍收益时段 改成福利时间*/
				while(YaBiaoHuoDongMgr.FULITIME_FLAG) {
					try {
						String template=YunbiaoTemp.yunbiao_start_broadcast;
						BroadcastMgr.inst.send(template);
						YaBiaoHuoDongMgr.inst.pushFuLiTimeState();
						Thread.sleep(YunbiaoTemp.yunbiao_start_broadcast_CD*1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		log.info("更新押镖活动--福利时段--状态{}",YaBiaoHuoDongMgr.FULITIME_FLAG);
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
			BroadcastMgr.inst.send(template);
			syncBroadExecutor.submit(new Runnable() {
				@Override
				public void run() {
					while(YaBiaoHuoDongMgr.SHOUYI_PROFIT>100) {
						try {
							String template=YunbiaoTemp.yunbiao_start_broadcast;
							BroadcastMgr.inst.send(template);
							log.info("连续广播现在是多倍收益时间");
							Thread.sleep(YunbiaoTemp.yunbiao_start_broadcast_CD*1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}else{
			template=YunbiaoTemp.yunbiao_end_broadcast;
			SHOUYI_PROFIT=canshu;
			BroadcastMgr.inst.send(template);
		}
		log.info("押镖收益比率为{}",SHOUYI_PROFIT);
	}
	public void setFuLiState(boolean canshu) {
		String template=YunbiaoTemp.yunbiao_start_broadcast;
		if(canshu){
			FULITIME_FLAG=canshu;
			BroadcastMgr.inst.send(template);
			syncBroadExecutor.submit(new Runnable() {
				@Override
				public void run() {
					while(YaBiaoHuoDongMgr.FULITIME_FLAG) {
						try {
							String template=YunbiaoTemp.yunbiao_start_broadcast;
							BroadcastMgr.inst.send(template);
							log.info("连续广播现在是福利时间");
							YaBiaoHuoDongMgr.inst.pushFuLiTimeState();
							Thread.sleep(YunbiaoTemp.yunbiao_start_broadcast_CD*1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}else{
			template=YunbiaoTemp.yunbiao_end_broadcast;
			FULITIME_FLAG=false;
			BroadcastMgr.inst.send(template);
			YaBiaoHuoDongMgr.inst.pushFuLiTimeState();
			log.info("福利时间结束");
		}
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
		openFlag=true;
//		if (openFlag) { //2016年2月27日 应朱老师建议 只在请求主页面信息是验证是否是开启押镖活动的时间
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
				buyHorseType(id, builder, session);
				break;
			case PD.C_YABIAO_REQ:
				startYabiaoReq(id, builder, session);
				break;
//			case PD.C_JIEBIAO_INFO_REQ:
//				log.info("请求劫镖主页逻辑已经废弃");
//				getJieBiaoInfo(id, builder, session);
//				break;
			case PD.C_BUYXUEPING_REQ:
				buyXuePing(id, builder, session);
				break;
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
			case PD.C_CHECK_YABIAOHELP_RSQ:
				getJionHelp2YBList(id, builder, session);
				break;
			case PD.C_TICHU_YBHELP_RSQ:
				log.error("YabiaoMgr-未处理的消息{},没有踢出协助者功能", id);
//				tichuHelper2YB(id, builder, session);
				break;
//			case PD.C_YABIAO_XIEZHU_TIMES_RSQ:
				//2015年12月12日 1.1版本协助次数完全没限制了
//				getXieZhuTimes(id, builder, session);
//				break;
			case PD.C_MOVE2BIAOCHE_REQ: //2016年3月4日 重启启用
				move2BiaoChe(id, builder, session);
				break;
			case PD.C_YABIAO_MOREINFO_RSQ:
				getYaBiaoMoreInfo(id, builder, session);
				break;
			case PD.C_GETMABIANTYPE_REQ:
				getMabianType(id, builder, session);
				break;
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
	}
	
	/**
	 * @Description 
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getYaBiaoMoreInfo(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("getYaBiaoMoreInfo请求押镖某些相关数据出错：君主不存在");
			return;
		}
		YaBiaoMoreInfoReq.Builder req=(YaBiaoMoreInfoReq.Builder)builder;
		int type=req.getType();
		log.info("{}请求押镖某些相关数据 type--{}  ",jz.id,type);
		YaBiaoMoreInfoResp.Builder resp=YaBiaoMoreInfoResp.newBuilder();
		resp.setType(type);
		switch (type) {
		case 1://请求剩余押镖次数
			getYabiaoRemainTimes(jz, resp, session);
			break;
		case 2://请求剩余劫镖次数
			pushJieBiaoRemainTimes(jz, session);
			break;
		case 3://请求已领取福利次数
			getFuliYilingTimes(jz, session);
			break;
		default:
			break;
		}
	}
	
	public void getFuliYilingTimes(JunZhu jz, IoSession session) {
		YaBiaoMoreInfoResp.Builder resp=YaBiaoMoreInfoResp.newBuilder();
		YaBiaoBean bean = getYaBiaoBean(jz.id, jz.vipLevel);
		int fuliTimes=bean.todayFuliTimes1+bean.todayFuliTimes2+bean.todayFuliTimes3;
		int remainFuli=fuliTimes>bean.usedYB?fuliTimes-bean.usedYB:0;
		resp.setType(3);
		resp.setCount(remainFuli);//剩余福利次数
		session.write(resp.build());
		log.info("{}请求领取福利次数信息,今日领取福利总次数----{}", jz.id,fuliTimes);
	}

	/**
	 * @Description 请求剩余押镖次数
	 * @param jz
	 * @param resp
	 * @param session
	 */
	public void getYabiaoRemainTimes(JunZhu jz ,YaBiaoMoreInfoResp.Builder resp,IoSession session){
		log.info("{}请求剩余押镖次数",jz.id);
		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jz.id);
		int remainYB = getYaBiaoCountForVip(jz.vipLevel);
		if (ybBean == null) {
			ybBean =initYaBiaoBeanInfo(jz.id, jz.vipLevel);
		} else {
			ybBean = resetybBean(ybBean, jz.vipLevel);
			remainYB=ybBean.remainYB;
		}
		log.info("{}请求剩余押镖次数--{}",jz.id,remainYB);
		resp.setCount(remainYB);
		session.write(resp.build());
	}
	/**
	 * @Description 请求剩余劫镖（有奖）次数 2016年1月19日 可能继续变需求当前是这意思
	 * @param jz
	 * @param resp
	 * @param session
	 */
	public void pushJieBiaoRemainTimes(JunZhu jz ,IoSession session){
		YaBiaoMoreInfoResp.Builder resp=YaBiaoMoreInfoResp.newBuilder();
		//type 2表示劫镖次数
		resp.setType(2);
		log.info("{}推送剩余劫镖次数",jz.id);
		YBBattleBean zdbean =getYBZhanDouInfo(jz.id, jz.vipLevel);
		int remainJB = zdbean.remainJB4Award;
		log.info("{}推送剩余劫镖次数--{}",jz.id,remainJB);
		resp.setCount(remainJB);
		session.write(resp.build());
	}
	/**
	 * @Description 请求马鞭类型
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getMabianType(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求马鞭类型失败，未找到请求君主");
			return;
		}
		long jzId=jz.id;
		log.info("{}请求马鞭类型开始",jzId);
		MaBianTypeResp.Builder resp=MaBianTypeResp.newBuilder();
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(jzId);
		if(ybr==null){
			log.info("{}请求马鞭类型为普通，不在运镖",jzId);
			resp.setType(1);
			session.write(resp.build());
			return;
		}
		YBBattleBean zdBean = getYBZhanDouInfo(jzId, jz.vipLevel);
		MaJu mabian=majuMap.get(zdBean.jiasu);
		//没有高级马具 或者  有高级马具但不是对自己的马车使用
		if(mabian==null){
			resp.setType(1);
			log.info("{}请求马鞭类型为普通，没有高级马鞭YBBattleBean-jiasu--{}",jzId,zdBean.jiasu);
		}else{
			resp.setType(2);
		}
		session.write(resp.build());
		String type=resp.getType()==2?"高级":"普通";
		log.info("{}请求马鞭类型，结果为--{}",jz.id,type);
	}

	/**
	 * @Description 买血瓶相关
	 * @param jz
	 * @param resp
	 * @param session
	 */
	public void buyXuePing(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求购买血瓶出错：君主不存在");
			return;
		}
		BuyXuePingReq.Builder req=(BuyXuePingReq.Builder)builder;
		int buyCode=req.getCode();
		BuyXuePingResp.Builder resp=BuyXuePingResp.newBuilder();
		switch (buyCode) {
		case 1:
			buyBlood(jz, resp, session);
			break;
		case 2:
			getBuyBloodInfo(jz, resp, session);
			break;
		default:
			break;
		}
	}
	
	/**
	 * @Description 请求买血瓶数据
	 * @param jz
	 * @param resp
	 * @param session
	 */
	protected void getBuyBloodInfo(JunZhu jz, BuyXuePingResp.Builder resp, IoSession session) {
		long jzId=jz.id;
		int vipLevel=jz.vipLevel;
		YBBattleBean zdbean =getYBZhanDouInfo(jzId, vipLevel);
		int buyBloodTimes=zdbean.buyblood4Vip;
		int remainXuePing=YunbiaoTemp.bloodVial_freeTimes+zdbean.bloodTimes4Vip - zdbean.xueping4uesd;
		VipFuncOpen vipFuncOpen = VipMgr.INSTANCE.getVipFuncOpen(VipData.buy_revive_all_life);
		if(vipFuncOpen == null) {
			log.error("找不到购买满血复活次数vip配置，key:{}", VipData.buy_revive_all_life);
			return;
		}
		int needVipLevel = vipFuncOpen.needlv;
		if(vipLevel < needVipLevel) {
			log.info("玩家{}请求买血瓶数据失败,玩家vip--{}等级不够，无法买血瓶", jzId,jz.vipLevel);
			sendBuyBloodResp(session, resp,60,0,0,remainXuePing,0);
			return;
		}
		int maxBuyTimes= VipMgr.INSTANCE.getValueByVipLevel(jz.vipLevel, VipData.buy_ybblood_times);
		if(maxBuyTimes<=0){
			log.info("玩家{}请求买血瓶数据失败,玩家vip--{}等级不够，无法买血瓶,最大次数用完", jzId,jz.vipLevel);
			sendBuyBloodResp(session, resp,60,0,0,remainXuePing,0);
			return;
		}
		int remainBuyTimes= maxBuyTimes-zdbean.buyblood4Vip;
		Purchase buyConf = PurchaseMgr.inst.getPurchaseCfg(PurchaseConstants.BUY_XUEPING_COUNT, buyBloodTimes+1);
		if(buyConf==null){
			log.error("玩家{}请求买血瓶数据失败,未找到Purchase配置", jzId);
			sendBuyBloodResp(session, resp,40,0,0,remainXuePing,0);
			return;
		}
		int buyCost=buyConf.getYuanbao();
		int nextGet=buyConf.getNumber();
		log.info("君主--{}，下次购买血瓶花费--{}，得到{}个血瓶,还可购买--{}次，剩余血瓶数目--{},已用次数--{}",jzId,buyCost,nextGet,remainBuyTimes,remainXuePing,zdbean.xueping4uesd);
		sendBuyBloodResp(session, resp,40,buyCost,remainBuyTimes,remainXuePing,nextGet);
		return;
	}
	
	/**
	 * @Description 买血瓶
	 * @param jz
	 * @param resp
	 * @param session
	 */
	protected void buyBlood(JunZhu jz, BuyXuePingResp.Builder resp, IoSession session) {
		long jzId=jz.id;
		int vipLevel=jz.vipLevel;
		YBBattleBean zdbean =getYBZhanDouInfo(jzId, vipLevel);
		int buyBloodTimes=zdbean.buyblood4Vip;
		int remainXuePing=YunbiaoTemp.bloodVial_freeTimes+zdbean.bloodTimes4Vip - zdbean.xueping4uesd;
		VipFuncOpen vipFuncOpen = VipMgr.INSTANCE.getVipFuncOpen(VipData.buy_revive_all_life);
		if(vipFuncOpen == null) {
			log.error("找不到购买满血复活次数vip配置，key:{}", VipData.buy_revive_all_life);
			return;
		}
		int needVipLevel = vipFuncOpen.needlv;
		if(vipLevel < needVipLevel) {
			log.error("玩家{}请求买血瓶数据失败,玩家vip--{}等级不够，无法买血瓶", jzId,jz.vipLevel);
			sendBuyBloodResp(session, resp,60,0,0,remainXuePing,0);
			return;
		}
		int maxBuyTimes= VipMgr.INSTANCE.getValueByVipLevel(jz.vipLevel, VipData.buy_ybblood_times);
		int remainBuyTimes= maxBuyTimes-zdbean.buyblood4Vip;
		Purchase buyConf = PurchaseMgr.inst.getPurchaseCfg(PurchaseConstants.BUY_XUEPING_COUNT, buyBloodTimes+1);
		if(buyConf==null){
			log.error("玩家{}请求买血瓶数据失败,未找到Purchase配置", jzId);
			sendBuyBloodResp(session, resp,50,0,0,remainXuePing,0);
			return;
		}
		int buyCost=buyConf.getYuanbao();
		int canGet=buyConf.getNumber();
		log.info("君主--{}，本次购买血瓶花费--{}，可得到{}个血瓶,还可购买--{}次，剩余血瓶数目--{},已用次数--{}",jzId,buyCost,
				canGet,remainBuyTimes,remainXuePing,zdbean.xueping4uesd);
		if(maxBuyTimes<=0){
			log.error("玩家{}购买血瓶失败,vip等级不够，最大次数--{}，已用购买次数--{}", jzId,maxBuyTimes,remainBuyTimes);
			sendBuyBloodResp(session, resp,60,buyCost,remainBuyTimes,remainXuePing,canGet);
			return;
		}
	
		if(remainBuyTimes<=0){
			log.error("玩家{}购买血瓶失败,购买次数用完，最大次数--{}，已用购买次数--{}", jzId,maxBuyTimes,remainBuyTimes);
			sendBuyBloodResp(session, resp,20,buyCost,remainBuyTimes,remainXuePing,canGet);
			return;
		}
		if (buyCost> jz.yuanBao) {
			log.error("玩家{}购买血瓶失败,元宝不够", jzId);
			sendBuyBloodResp(session, resp,30,buyCost,remainBuyTimes,remainXuePing,canGet);
			return;
		}
	
		YuanBaoMgr.inst.diff(jz, -buyCost, 0,buyCost, YBType.YB_SHENGJI_YABIAO_MAPI, "押镖购买血瓶");
		JunZhuMgr.inst.sendMainInfo(session);
		int addTimes=buyConf.getNumber();
		log.info("增加君主{} 的vip购买的血瓶次数{},花费元宝--{},已用次数{}",jzId,addTimes,buyCost,zdbean.xueping4uesd);
		zdbean.bloodTimes4Vip+=addTimes;
		zdbean.buyblood4Vip+=1;
		HibernateUtil.save(zdbean);
		int remainBuyTimes2Next= maxBuyTimes-zdbean.buyblood4Vip;
		int remainXuePing2Next=YunbiaoTemp.bloodVial_freeTimes+zdbean.bloodTimes4Vip - zdbean.xueping4uesd;
		Purchase buyConf2Next = PurchaseMgr.inst.getPurchaseCfg(PurchaseConstants.BUY_XUEPING_COUNT, buyBloodTimes+1);
		if(buyConf2Next==null){
			log.error("玩家{}请求买血瓶数据失败,未找到Purchase配置", jzId);
			sendBuyBloodResp(session, resp,50,0,remainBuyTimes2Next,remainXuePing2Next,0);
			return;
		}
		int buyCost2Next=buyConf2Next.getYuanbao();
		int nextGet2Next=buyConf2Next.getNumber();
		//购买成功返回数据
		log.info("君主--{}，下次购买血瓶花费--{}，可得到{}个血瓶,还可购买--{}次，剩余血瓶数目--{},已用次数--{}",jzId,buyCost2Next,
				nextGet2Next,remainBuyTimes2Next,remainXuePing2Next,zdbean.xueping4uesd);
		sendBuyBloodResp(session, resp,10,buyCost2Next,remainBuyTimes2Next,remainXuePing2Next,nextGet2Next); 
	}
	
	/**
	 * @Description 返回买血瓶数据  10成功 20 购买次数用完  30钱不够 40：返回次数数据 50没找到配置
	 */
	protected void sendBuyBloodResp(IoSession session,BuyXuePingResp.Builder resp,int code,int cost,int remainBuyTimes,int remainXuePing,int nextGet) {
		resp.setResCode(code);
		resp.setNextCost(cost);
		resp.setNextGet(nextGet);
		resp.setRemainTimes(remainBuyTimes);
		resp.setRemainXuePing(remainXuePing);
		session.write(resp.build());
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
		resp.setSubaoType(msg.eventId);
		long ybjzId=msg.otherJzId;
		/******************************请求加入协助队伍***********************************************/
		if(msg.eventId==SuBaoConstant.mccf_toOther||msg.eventId==SuBaoConstant.zdqz){
			log.info("{}请求加入{}协助队伍",jz.id,ybjzId);
			jionHelp2YB(jz, msg, session);
			HibernateUtil.delete(msg);
			return;
		}
		
		
		/******************************请求前往目标镖车***********************************************/
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybjzId);
		if(ybr==null){
			log.error("{}请求前往镖车失败，未找到--{}的镖车信息",jz.id,ybjzId);
			resp.setSubaoId(subaoId);
			resp.setResult(20);
		
			session.write(resp.build());
			return;
		}
		IoSession ybrSession =ybr.session;
		if(ybrSession==null){
			log.error("{}请求前往镖车失败，未找到押镖君主--{}的镖车所在场景",jz.id,ybjzId);
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			session.write(resp.build());
		}
		Integer	uid=(Integer)ybrSession.getAttribute(SessionAttKey.playerId_Scene);
		if(uid==null){
			log.error("{}请求前往镖车失败，未找到押镖君主--{}镖车的Uid",jz.id,ybjzId);
			resp.setSubaoId(subaoId);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		resp.setSubaoId(subaoId);
		resp.setResult(10);
		resp.setYbuid(uid);
		resp.setPosX(ybr.posX);
		resp.setPosZ(ybr.posZ);
		session.write(resp.build());
		log.info("{}请求前往{}镖车成功，镖车坐标x--{},z---{}",jz.id,ybjzId,ybr.posX,ybr.posZ);
		HibernateUtil.delete(msg);
	}
	
	/**
	 * @Description:加入协助列表
	 */
	public void jionHelp2YB(JunZhu jz,PromptMSG msg, IoSession session) {
		Long jzId = jz.id;
		long ybjzId=msg.otherJzId;
		PromptActionResp.Builder resp=PromptActionResp.newBuilder();
		resp.setSubaoId(msg.id);
		resp.setSubaoType(msg.eventId);
		log.info("君主--{}加入君主--{}协助队伍", jzId,ybjzId);
		if (ybjzId <= 0) {
			log.error("{}加入协助目标不存在", jzId);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		if (jzId.equals(ybjzId)) {
			log.info("{}不能协助自己运镖{}", jzId, ybjzId);
			resp.setResult(70);
			session.write(resp.build());
			return;
		}
		AllianceBean aBean = AllianceMgr.inst.getAllianceByJunZid(jzId);
		if (aBean == null) {
			log.info("协助目标{}不在联盟，加入押镖协助失败", jzId);
			resp.setResult(30);
			session.write(resp.build());
			return;
		}
		AllianceBean askBean = AllianceMgr.inst.getAllianceByJunZid(ybjzId);
		if (askBean == null) {
			log.info("{}协助的目标{}没有联盟，答复押镖协助失败", jzId, ybjzId);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		if(askBean.id!=aBean.id){
			log.info("{}协助的目标{}的联盟不是同一个，答复押镖协助失败", jzId, ybjzId);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybjzId);
		if (ybr == null) {
			log.info("{}协助的目标{}未运镖，答复押镖协助失败", jzId, ybjzId);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		//马车所在场景对象
		Scene sc = (Scene) ybr.session.getAttribute(SessionAttKey.Scene);
		if (sc == null) {
			log.info("{}协助的目标{}场景未找到，答复押镖协助失败", jzId, ybjzId);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		//2015年12月12日 1.1版本协助次数完全没限制了
		// 判断是否答复过
		List<Long> helperList = (List<Long>) answerHelpCache4YB.get(ybjzId);
		if (helperList != null && helperList.contains(jzId)) {
			log.info("{}已答复{}", jzId, ybjzId);
			resp.setResult(50);
			session.write(resp.build());
			return;
		} else {
			if (helperList == null) {
				helperList = new ArrayList<Long>();
			}
			// 存储答复队列
			helperList.add(jzId);
			answerHelpCache4YB.put(ybjzId, helperList);
		}
		// 保存协助者
		if (!saveXieZhuSet(ybjzId, jzId)) {
			resp.setResult(80);
			resp.setSubaoId(msg.id);
			session.write(resp.build());
			return;
		}
		// 答复协助的返回

		resp.setResult(10);
		resp.setPosX(ybr.posX);
		resp.setPosZ(ybr.posZ);
		session.write(resp.build());
		
		//保存加入协助成功快报 
		//给参加协助的人的快报
		PromptMSG msg2jion=	sendJionSuccessKB2XieZhu(ybjzId, ybr.name, jzId);
		PromptMsgMgr.inst.pushSubao(session, msg2jion);
		//推送君主协助目标的列表
		pushHelpList4YB(jz, session);
		/*******************************通知运镖君主谁加入协助*****************************************************/
		//通知运镖君主谁加入协助
		SessionUser su = SessionManager.inst.findByJunZhuId(ybjzId);
		if(su==null){
			log.info("运镖君主--{}已下线，取消君主--{}加入协助的通知",ybjzId,jzId);
			return;
		}
		int curHp=jz.shengMing;
		LastExitYBInfo lastExitInfo = HibernateUtil.find(LastExitYBInfo.class, jzId);
		if(lastExitInfo != null) {
			lastExitInfo.remainLife = lastExitInfo.remainLife;
			curHp = lastExitInfo.remainLife <= 0 ? 1: lastExitInfo.remainLife;
			curHp=curHp>jz.shengMingMax?jz.shengMingMax:curHp;
		}
		AskYaBiaoHelpResp.Builder resp2Asker = AskYaBiaoHelpResp.newBuilder();
		XieZhuJunZhu.Builder xzJz = XieZhuJunZhu.newBuilder();
		xzJz.setJzId(jzId);
		xzJz.setName(jz.name);
		xzJz.setRoleId(jz.roleId);
		xzJz.setCurHp(curHp);
		xzJz.setMaxHp(jz.shengMingMax);
		resp2Asker.setCode(10);
		resp2Asker.setJz(xzJz);
		if(su!=null){
			log.info("通知运镖君主--{}，君主--{}加入协助,",ybjzId,jzId);
			su.session.write(resp2Asker.build());
		}
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
		PromptActionResp.Builder resp=PromptActionResp.newBuilder();
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJzId);
		if(ybr==null){
			log.error("{}请求前往镖车失败，未找到--{}的镖车信息",jz.id,ybJzId);
			resp.setSubaoId(0);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		IoSession ybrSession =ybr.session;
		Integer	uid=(Integer)ybrSession.getAttribute(SessionAttKey.playerId_Scene);
		if(uid==null){
			log.error("{}请求前往镖车失败，未找到押镖君主--{}镖车的Uid",jz.id,ybJzId);
			resp.setSubaoId(0);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		
		resp.setSubaoId(0);
		resp.setResult(10);
		resp.setYbuid(uid);
		resp.setPosX(ybr.posX);
		resp.setPosZ(ybr.posZ);
		session.write(resp.build());
		log.info("{}请求前往{}镖车成功，镖车坐标x--{},z---{}",jz.id,ybJzId,ybr.posX,ybr.posZ);
	}
	//2015年12月12日 1.1版本协助次数完全没限制了

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
	public CartNPCTemp getRandomCartNPC(int bcLevel) {
		int hType = getRandomCart();// 随机一匹马 1 2 3 4 5 （0表示没有马）
		if (hType > 5) {
			hType = 5;
		}
		//2016年1月19日 YunbiaoTemp.cartAILvlMin从20-》14 策划变更算法暂时废弃
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
	 * @Description: 推送镖车信息 2016年1月20日此方法截止目前策划暂无需求使用
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
		Scene sc = (Scene) session.getAttribute(SessionAttKey.Scene);
		if(sc==null||sc.players==null){
			log.error("{}请求推送镖车信息出错：未找到押镖场景",jzId);
			return;
		}
//		Set<Long> ybSet = ybJzList2ScIdMap.get(roomId);
		for(Player p : sc.players.values()){
			long junzhuId=p.jzId;
			//不是机器人 跳过
			if(p.roleId != Scene.YBRobot_RoleId){
				continue;
			}
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
		YBBattleBean zdbean = getYBZhanDouInfo(jzId, vipLevel);
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
		if(jzId<0)return 0;
		int vipLevel=jz.vipLevel;
		YBBattleBean zdbean =getYBZhanDouInfo(jzId, vipLevel);
		int	remainTimes	=YunbiaoTemp.resurgenceTimes+zdbean.fuhuoTimes4Vip-zdbean.fuhuo4uesd;
		log.info("返回君主{}剩余满血复活次数{},已用次数{},免费满血次数为{}，购买的满血次数为{}",jzId,remainTimes,zdbean.fuhuo4uesd,
				YunbiaoTemp.resurgenceTimes,zdbean.fuhuoTimes4Vip);
		return remainTimes;
	}
	
	/**
	 * 获取今日原地复活次数
	 * @param jz
	 * @return
	 */
	public int getReviveOnDeadPosTimes(JunZhu jz) {
		long jzId=jz.id;
		if(jzId<0)return 0;
		int vipLevel=jz.vipLevel;
		YBBattleBean zdbean =getYBZhanDouInfo(jzId, vipLevel);
		Date date = new Date();
		if(zdbean.lastReviveOnDeadPosTime == null) {
			return 0;
		} else {
			//是否更新次数
			if(date.getDay() != zdbean.lastReviveOnDeadPosTime.getDay()){
				zdbean.reviveOnDeadPos = 0;
			}
		}
		return zdbean.reviveOnDeadPos;
	}
	
	
	/**
	 * @Description 返回Vip今日进行"购买满血复活次数"的 次数
	 * @param jz
	 * @return
	 */
	public int getBuyFuhuoTimes4Vip(JunZhu jz) {
		long jzId=jz.id;
		int vipLevel=jz.vipLevel;
		YBBattleBean zdbean = getYBZhanDouInfo(jzId, vipLevel);
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
		log.info("请求押镖主要信息：君主--{}",jz.id);
		YabiaoMainInfoResp.Builder resp = YabiaoMainInfoResp.newBuilder();
		YunBiaoHistory histo = getYunBiaoHistory(jz.id);
		boolean isOpen=openFlag?openFlag:histo.historyYB==0?true:false;
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
		if(histo.historyYB==0&&ybBean.horseType==5){
			log.info("君主---{}第一次押镖马车随机出最高品质，为了过引导强制降低品质",jz.id);
			ybBean.horseType=4;
			HibernateUtil.save(ybBean);
		}
		resp.setYaBiaoCiShu(remainYB);
		resp.setIsNew4Enemy(isNew4Enemy);
		resp.setIsNew4History(isNew4History);
		resp.setBuyCiShu(todayBuyYBTimes);
		resp.setIsOpen(isOpen);
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
	 * @Description: 告诉当前运镖君主的仇人 他不在运镖了
	 */
	public void refreshYabiaoState2Enemy(long jzId) {
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
			enemy.setState(state);
			enemy.setUsedTime(usedTime);
			enemy.setTotalTime(totalTime);
			enemy.setHorseType(hrseType);
			enemy.setLianMengName(lmName);
			enemy.setRoleId(enJz.roleId);
			//2015年8月31日返回仇人的护盾
			enemy.setHudun(hudun*100/enJz.shengMingMax);
			SessionUser su = SessionManager.inst.findByJunZhuId(enemyId);
			Integer scId=null;
			if(su!=null){
				scId =(Integer)su.session.getAttribute(SessionAttKey.SceneID);
			}
//			Integer scId = ybJzId2ScIdMap.get(enemyId);
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
		log.info("请求押镖界面，随机马车 ，君主--{}",jz.id);
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
		resp.setHorse(ybBean.horseType);
		resp.setIsNewHorse(isNewHorse);
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
		XieZhuJunZhuResp.Builder resp=XieZhuJunZhuResp.newBuilder();
		if (xiezhuSet == null || xiezhuSet.size() == 0) {
			log.info("{}无协助君主", ybJzId);
			session.write(resp.build());
			return;
		}
		Scene sc = (Scene) session.getAttribute(SessionAttKey.Scene);
		if(sc==null){
			log.error("请求协助运镖失败，未找到君主--{}所在场景",ybJzId);
			session.write(resp.build());
			return;
		}
		for (Long jzId : xiezhuSet) {
			JunZhu xzJz = HibernateUtil.find(JunZhu.class, jzId);
			if (xzJz != null) {
				XieZhuJunZhu.Builder xiezhujz = XieZhuJunZhu.newBuilder();
				xiezhujz.setJzId(xzJz.id);
				xiezhujz.setName(xzJz.name);
				xiezhujz.setRoleId(xzJz.roleId);
				int curHp=xzJz.shengMing;
				SessionUser su = SessionManager.inst.findByJunZhuId(xzJz.id);
				Integer uid=null;
				Player p=null;
				LastExitYBInfo lastExitInfo = HibernateUtil.find(LastExitYBInfo.class, jzId);
				boolean isget=false;
				if(su!=null){
					uid=(Integer)su.session.getAttribute(SessionAttKey.playerId_Scene);
					if(uid != null){
						p= sc.players.get(uid);
						if(p!=null){
							curHp=p.currentLife;
							isget=true;
						}
					}
				}
				if(!isget){
					if(lastExitInfo != null) {
						log.info("君主--{}的协助君主--{}，不在运镖场景，血量读取离开时血量");
						curHp = lastExitInfo.remainLife <= 0 ? 1: lastExitInfo.remainLife;
						curHp=curHp>xzJz.shengMingMax?xzJz.shengMingMax:curHp;
					}
				}
				xiezhujz.setCurHp(curHp);
				xiezhujz.setMaxHp(xzJz.shengMingMax);
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
	public void buyHorseType(int id, Builder builder, IoSession session) {
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
		if(cost<=0){
			log.info("请求设置马匹出错：{}需要花费的元宝---{}计算错误", jz.id,cost);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		log.info("请求设置马匹,马匹从{}升级到{},需要花费--{}", nowhorseType,targetType,cost);
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
		if(jzId<0){
			return null;
		}
		YBBattleBean zdbean = HibernateUtil.find(YBBattleBean.class, jzId);
		if (zdbean == null) {
			zdbean=initYBBattleInfo(jzId, vipLevel);
		}else{
			zdbean=resetYBBattleInfo(zdbean, vipLevel);
		}
		return zdbean;
	}
	
	public int getXuePingRemainTimes(long jzId,int vipLevel) {
		YBBattleBean zdbean = getYBZhanDouInfo(jzId, vipLevel);
		int remainTimes = YunbiaoTemp.bloodVial_freeTimes+zdbean.bloodTimes4Vip - zdbean.xueping4uesd;
		return Math.max(remainTimes, 0);
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
			session.write(resp.build());
			log.error("请求开始押镖出错：君主不存在");
			return;
		}
		long jzId=jz.id;
		// 移除押镖君主的协助者队列
		xieZhuCache4YBJZ.remove(jzId);
		answerHelpCache4YB.remove(jzId);
		//判断是否已经在运镖
		YaBiaoRobot temp = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(jz.id);
		if (temp != null) {
			resp.setResult(30);
			session.write(resp.build());
			log.error("{}请求开始押镖出错：君主已参加押镖",jzId);
			return;
		}
		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jz.id);
		final int horseType=ybBean.horseType;
		if(ybBean.remainYB<1){
			resp.setResult(40);
			session.write(resp.build());
			log.error("{}请求开始押镖出错：君主押镖次数已用完",jzId);
			return;
		}
		
//		// 进入押镖场景进行押镖 2015年12月11日1.1版本改成马车进入君主所在场景
		Scene sc = (Scene) session.getAttribute(SessionAttKey.Scene);
		Integer scId =(Integer)session.getAttribute(SessionAttKey.SceneID);
		if(sc == null||scId==null||!sc.name.contains("YB")){
			resp.setResult(20);
			session.write(resp.build());
			log.error("{}请求开始押镖出错：君主不在押镖场景",jzId);
			return;
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
			session.write(resp.build());
			log.error("{}请求开始押镖出错：创建押镖机器人失败",jzId);
			return;
		}
		

		// 返回成功进入押镖活动
		resp.setResult(10);
		session.write(resp.build());
		
		//2016年1月18日更新数据
		ybBean.horseType = -1;
		ybBean.usedYB += 1;
		ybBean.remainYB -= 1;
		HibernateUtil.save(ybBean);
		// 运镖历史
		YunBiaoHistory histo = getYunBiaoHistory(jz.id);
		histo.historyYB += 1;
		HibernateUtil.save(histo);
		
		// 广播押镖机器人进入场景
		broadBiaoCheInfo(sc, ybr);
		// 扣除盟友协助次数 2015年12月11日1.1版本出发不扣除盟友协助次数 无盟友加成护盾
		// 每日任务：完成1次押镖活动
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(jz.id,DailyTaskConstants.yunBiao, 1));
		//押镖开始事件
		EventMgr.addEvent(ED.BIAOCHE_CHUFA, new Object[] { jz,horseType, System.currentTimeMillis()});
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
		int safeArea=getNearSafeArea(player.getPosX(), player.getPosZ());
		log.info("{}最近的安全区为{}",player.name,safeArea);
		return safeArea;
	}
	
	/**
	 * @Description 得到最近的安全区
	 * @param x 
	 * @param z
	 * @return 
	 */
	public int getNearSafeArea(float x,float z){
		int safeArea=1;
		double distance=getDistance4SafeArea(x, z, 1);
		double temp=getDistance4SafeArea(x, z, 2);
		if(distance>temp){
			safeArea=2;
			distance=temp;
		}
		temp=getDistance4SafeArea(x, z, 3);
		if(distance>temp){
			safeArea=3;
			distance=temp;
		}
		temp=getDistance4SafeArea(x, z, 4);
		if(distance>temp){
			safeArea=4;
			distance=temp;
		}
		return safeArea;
	}
	
	public double getDistance4SafeArea(float x,float z,int safeId){
		YunBiaoSafe	safeArea=safeAreaMap.get(safeId);
		float x4safe=safeArea.saveAreaX;
		float z4safe=safeArea.saveAreaZ;
		double distance=Math.sqrt((x-x4safe)*(x-x4safe) +(z-z4safe)*(z-z4safe)) ;
		return distance;
	}
	/**
	 * @Description 初始化君主押镖机器人
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
		ybr.sc = sc;
		ybr.session = new RobotSession();
		ybr.move = SpriteMove.newBuilder();
		ybr.jzId = jz.id;
		ybr.jzLevel=jz.level;
		ybr.name = jz.name;
		//马车出现的路线和安全区编号一致
		int pathId =safeArea.areaID;
		ybr.pathId = pathId;
		ybr.nodeId=1;
		ybr.totalTime = YunbiaoTemp.cartTime * 1000;// 毫秒计算
		ybr.usedTime = 0;
		ybr.startTime = System.currentTimeMillis();
		ybr.startTime2upSpeed = System.currentTimeMillis();
		int upSpeedTime=0;
		int protectTime=0;
		YBBattleBean zdbean =getYBZhanDouInfo(jz.id, jz.vipLevel);
		//2016年1月25日 加入马具收益加成
		double majujiacheng=0;
		if(zdbean.baohu>0){
			MaJu maju=majuMap.get(zdbean.baohu);
			protectTime=maju.value1;//保护罩时间
			majujiacheng+=maju.profitPara;
		}
		if(zdbean.jiasu>0){
			MaJu maju=majuMap.get(zdbean.jiasu);
			majujiacheng+=maju.profitPara;
		}
		ybr.protectTime=protectTime*1000;
		ybr.upSpeedTime = upSpeedTime*1000;
		float attrRate = YunbiaoTemp.cart_attribute_pro / 100;
		// 根据联盟科技计算马车生命值加成
		double keJiRate = 0;
		int allianceId = AllianceMgr.inst.getAllianceId(jz.id);
		LianMengKeJi lmKeJiConf = JianZhuMgr.inst.getKeJiConfForYaBiao(allianceId, 205);//205马车血量加成
		if(lmKeJiConf != null) {
			keJiRate = lmKeJiConf.value1;
		}
		int cartLife =  (int) ((jz.shengMingMax * attrRate) * YunbiaoTemp.cartLifebarNum);
		int cartTotalLife = (int) (cartLife + cartLife * (keJiRate / 100));
		ybr.hp = cartTotalLife;
		ybr.maxHp = cartTotalLife;
		log.info("产生君主押镖机器人--{},初始无敌时间为{},初始加速时间为{},血量--{}",jz.id,protectTime,ybr.upSpeedTime,ybr.hp);
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
		
		ybr.worth = (int) (ss.xishu * (cart.profitPara+majujiacheng));
		int temp=(int) (ss.xishu * (cart.profitPara));
		log.info("计算马车价值，新版价值---{}，旧版价值---{},马具加成--{}",ybr.worth ,temp,majujiacheng);
		
		YBCartAttr4Fight cartAttr4Fight = new YBCartAttr4Fight();
		int shengMing = (int) (jz.shengMingMax * attrRate);
		cartAttr4Fight.id = jz.id;
		cartAttr4Fight.shengMing = shengMing;
		cartAttr4Fight.shengMingMax = shengMing;
		cartAttr4Fight.fangYu = (int) (jz.fangYu * attrRate);
		cartAttr4Fight.gongJi = (int) (jz.gongJi * attrRate);
		cartAttr4Fight.wqSH = (int) (jz.wqSH * attrRate);
		cartAttr4Fight.wqJM = (int) (jz.wqJM * attrRate);
		cartAttr4Fight.wqBJ = (int) (jz.wqBJ * attrRate);
		cartAttr4Fight.wqRX = (int) (jz.wqRX * attrRate);
		cartAttr4Fight.jnSH = (int) (jz.jnSH * attrRate);
		cartAttr4Fight.jnJM = (int) (jz.jnJM * attrRate);
		cartAttr4Fight.jnBJ = (int) (jz.jnBJ * attrRate);
		cartAttr4Fight.jnRX = (int) (jz.jnRX * attrRate);
		ybr.cartAttr4Fight = cartAttr4Fight;
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
	 */
	public boolean initSysYBRobots(Scene sc,int pathId,int scId,int cartNo) {
		int	bcLevel=YaBiaoRobotProduceMgr.produceCartList.get(cartNo);
		//得到随机的马车配置
		CartNPCTemp biaoCheNPC=getRandomCartNPC(bcLevel);
		int bcNPCId =biaoCheNPC.id;
		CartNPCName cartNPCName=cartNpcNameMap.get(cartNo+1);
		String robotName=cartNPCName==null?getRandomString(5):cartNPCName.name;
		log.info(" 产生系统机器人,编号---{},等级---{},配置ID--{},名字----{},",cartNo,bcLevel,bcNPCId,robotName);
		YaBiaoRobot ybr=new YaBiaoRobot();
		ybr.session = new RobotSession();
		ybr.move = SpriteMove.newBuilder();
		long sysJzId=getRobotJzId() ;
		ybr.jzId = sysJzId;
		ybr.name = robotName;
		ybr.jzLevel=biaoCheNPC.level;
		ybr.bcNPCNo=cartNo;
		JunzhuShengji ss = JunZhuMgr.inst.getJunzhuShengjiByLevel(ybr.jzLevel);
		if(ss==null){
			log.error("产生系统机器人--{}出错,未找到JunzhuShengji配置",	ybr.jzId );
			return false;
		}
		//读取配置得到血量
		ybr.hp = (int) (biaoCheNPC.shengming * biaoCheNPC.lifebarNum);
		ybr.maxHp = (int) (biaoCheNPC.shengming * biaoCheNPC.lifebarNum);
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
		ybr.sc = sc;
		CartTemp cart = cartMap.get(ybr.horseType );
		if(cart==null){
			log.error("产生系统机器人--{}出错,未找到CartTemp配置",sysJzId);
			return false;
		}
		ybr.totalTime = YunbiaoTemp.cartTime * 1000;// 毫秒计算
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
		YBCartAttr4Fight cartAttr4Fight = new YBCartAttr4Fight();
		cartAttr4Fight.id = ybr.jzId;
		cartAttr4Fight.shengMing = biaoCheNPC.shengming;
		cartAttr4Fight.shengMingMax = biaoCheNPC.shengming;
		cartAttr4Fight.fangYu = biaoCheNPC.fangyu;
		cartAttr4Fight.gongJi = biaoCheNPC.gongji;
		cartAttr4Fight.wqSH = biaoCheNPC.wqSH;
		cartAttr4Fight.wqJM = biaoCheNPC.wqJM;
		cartAttr4Fight.wqBJ = biaoCheNPC.wqBJ;
		cartAttr4Fight.wqRX = biaoCheNPC.wqRX;
		cartAttr4Fight.jnSH = biaoCheNPC.jnSH;
		cartAttr4Fight.jnJM = biaoCheNPC.jnJM;
		cartAttr4Fight.jnBJ = biaoCheNPC.jnBJ;
		cartAttr4Fight.jnRX = biaoCheNPC.jnRX;
		ybr.cartAttr4Fight = cartAttr4Fight;
		// 将镖车机器人加入镖车机器人管理线程
		BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.put(sysJzId, ybr);
		sc.exec(PD.Enter_YBScene, ybr.session, enter);
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
		return -Scene.atomicInteger.incrementAndGet();
	}  
	//2015年12月12日 1.1版本 检查协助者是否可以协助废弃
	/**
	 * @Description: //获取场景id
	 * @return
	 */
	public synchronized int locateFakeSceneId() {
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
		int ybUid=req.getYbUid();
		JiaSuResp.Builder resp=JiaSuResp.newBuilder();
		Scene sc = (Scene) session.getAttribute(SessionAttKey.Scene);
		if(sc==null){
			log.error("请求马车加速失败，未找到君主--{}所在场景",jzId);
			resp.setResCode(20);
			session.write(resp.build());
			return;
		}
		Player ybPlay= sc.players.get(ybUid);
		if(ybPlay==null){
			log.error("{}请求马车加速失败，未找到目标君主uid-{}的Player",jzId,ybUid);
			resp.setResCode(20);
			session.write(resp.build());
			return;
		}
		long ybjzId=ybPlay.jzId;
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybjzId);
		if(ybr==null){
			log.error("请求马车加速失败，未找到{}的镖车",ybjzId);
			resp.setResCode(20);
			session.write(resp.build());
			return;
		}
		if(ybr.upSpeedTime>0){
			log.error("请求马车加速失败，马车已经在加速了，加速剩余时长--{}",ybjzId,ybr.upSpeedTime);
			resp.setResCode(30);
			session.write(resp.build());
			return;
		}
		YBBattleBean zdBean = getYBZhanDouInfo(jzId, jz.vipLevel);
		MaJu mabian=majuMap.get(zdBean.jiasu);
		boolean isGaojiMabian=true;
		//没有高级马具 或者  有高级马具但不是对自己的马车使用
		if((mabian==null)||(ybjzId!=jzId)){
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
		double speed4jiasu=100+mabian.value2;
		ybr.upSpeedTime+=jiasuTime;
		ybr.speed=(double)speed4jiasu/100;
		ybr.startTime2upSpeed=System.currentTimeMillis();
		resp.setResCode(10);
		session.write(resp.build());
		log.info("{}请求{}马车加速完成,使用的是---《{}》马鞭,加速效果==《{}》",jz.id,ybjzId,isGaojiMabian?"高级":"普通",ybr.speed);
		//广播马车加速时间？？看策划需求
		broadBiaoCheInfo(sc, ybr);
	}

	//1.1版本变成MMO 无请求劫镖主页面

	/**
	 * @Description: 保存押镖攻击密保
	 * @param jz
	 * @param mibaoIds
	 * @param zuheId
	 */
	public void saveGongJiMiBao(JunZhu jz, List<Long> mibaoIds, int zuheId) {
		YBBattleBean zdBean = getYBZhanDouInfo(jz.id, jz.vipLevel);
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
		zdBean.zuheId = zuheId;
		HibernateUtil.save(zdBean);
		log.info("玩家:{}押镖更换秘宝成功", jz.id);
	}
	//1.1版本变成MMO  劫镖人请求进入场景 玩法变更此方法废弃

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
		Long cdTime = (Long) session.getAttribute(SessionAttKey.LAST_ASKYBHLEP_KEY);
		long currentMillis = System.currentTimeMillis();
		if (cdTime != null && cdTime > currentMillis) {
			log.warn("发送速度过快{}", jz.id);
			resp.setCode(30);
			session.write(resp.build());
			return;
		}
		session.setAttribute(SessionAttKey.LAST_ASKYBHLEP_KEY, currentMillis+ ChatMgr.CHAT_COOL_TIME);
		AllianceBean aBean = AllianceMgr.inst.getAllianceByJunZid(ybJzId);
		if (aBean == null) {
			log.info("{}没有联盟，请求押镖协助失败", ybJzId);
			resp.setCode(40);
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
			resp.setCode(50);
			session.write(resp.build());
			return;
		}
		int eventId=SuBaoConstant.zdqz;
		boolean ret3=false;
		int horseType=ybr.horseType;
		//2015年12月14日 改完配置  明确只向未协助盟友求助   没有向协助盟友、所有盟友求助操作求助 
		ret3=PromptMsgMgr.inst.pushKB2WeiXieZhuMengYou(jz, aBean.id, horseType, eventId, "");
		log.info("镖车出发事件处理结果ret3=={} ，{}请求押镖协助成功",ret3, ybJzId);
		resp.setCode(10);
		session.write(resp.build());
	}
	//1.1版本  玩法变更  扣除协助者次数方法废弃
	/**
	 * @Description 得到君主协助目标的列表
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getJionHelp2YBList(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("判断是否可以协助君主不存在");
			return;
		}
		Long jzId = jz.id;
		CheckYabiaoHelpResp.Builder resp=CheckYabiaoHelpResp.newBuilder();
		for(Map.Entry<Long,HashSet<Long>> entry : xieZhuCache4YBJZ.entrySet()) {
			HashSet<Long> helperSet=(HashSet<Long>) entry.getValue();
			if (helperSet != null && helperSet.contains(jzId)) {
				Long	ybjzId=(Long) entry.getKey();
				resp.addYbjzId(ybjzId);
			}
		}
		session.write(resp.build());
	}
	//推送君主协助目标的列表
	public void pushHelpList4YB(JunZhu jz, IoSession session) {
		if (jz == null) {
			log.error("判断是否可以协助君主不存在");
			return;
		}
		Long jzId = jz.id;
		CheckYabiaoHelpResp.Builder resp=CheckYabiaoHelpResp.newBuilder();
		for (Map.Entry<Long,HashSet<Long>> entry : xieZhuCache4YBJZ.entrySet()) {
			HashSet<Long> helperList=(HashSet<Long>) entry.getValue();
			if (helperList != null && helperList.contains(jzId)) {
				Long	ybjzId=(Long) entry.getKey();
				resp.addYbjzId(ybjzId);
			}
		}
		session.write(resp.build());
	}
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
		Long jzId = jz.id;
		AnswerYaBiaoHelpReq.Builder req = (AnswerYaBiaoHelpReq.Builder) builder;
		int ybUid=req.getYbUid();
		PromptActionResp.Builder resp=PromptActionResp.newBuilder();
		resp.setSubaoId(0);
		//2016年1月22日 需求变动 点加入协助不传送了
		resp.setSubaoType(SuBaoConstant.zdqz);
		resp.setSubaoType(0);
		Scene sc = (Scene) session.getAttribute(SessionAttKey.Scene);
		if(sc==null){
			log.error("请求协助运镖失败，未找到君主--{}所在场景",jzId);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		Player ybPlay= sc.players.get(ybUid);
		if(ybPlay==null){
			log.error("{}请求协助运镖失败，未找到协助目标君主uid-{}的Player",jzId,ybUid);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		long ybjzId=ybPlay.jzId;
		log.info("君主--{}加入协助君主--{}", jzId,ybjzId);
		int code =10;//req.getCode();
		if (ybjzId <= 0) {
			log.error("{}加入协助目标不存在", jzId);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		if (jzId.equals(ybjzId)) {
			log.info("{}不能协助自己运镖{}", jzId, ybjzId);
			resp.setResult(70);
			session.write(resp.build());
			return;
		}
		AllianceBean aBean = AllianceMgr.inst.getAllianceByJunZid(jzId);
		if (aBean == null) {
			log.info("协助目标{}不在联盟，加入押镖协助失败", jzId);
			resp.setResult(30);
			session.write(resp.build());
			return;
		}

		AllianceBean askBean = AllianceMgr.inst.getAllianceByJunZid(ybjzId);
		if (askBean == null) {
			log.info("{}协助的目标{}没有联盟，答复押镖协助失败", jzId, ybjzId);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		if(askBean.id!=aBean.id){
			log.info("{}协助的目标{}的联盟不是同一个，答复押镖协助失败", jzId, ybjzId);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybjzId);
		if (ybr == null) {
			log.info("{}协助的目标{}已经运镖，答复押镖协助失败", jzId, ybjzId);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		//2015年12月12日 1.1版本协助次数完全没限制了
		// 判断是否答复过
		List<Long> helperList = (List<Long>) answerHelpCache4YB.get(ybjzId);
		if (helperList != null && helperList.contains(jzId)) {
			log.info("{}已答复{}", jzId, ybjzId);
			resp.setResult(50);
			session.write(resp.build());
			return;
		} else {
			if (helperList == null) {
				helperList = new ArrayList<Long>();
			}
			// 存储答复队列
			helperList.add(jzId);
			answerHelpCache4YB.put(ybjzId, helperList);
		}

		if (code == 10) {
			// 保存协助者
			if (!saveXieZhuSet(ybjzId, jzId)) {
				resp.setResult(80);
				session.write(resp.build());
				return;
			}
		}
		// 答复协助的返回
		resp.setResult(10);
		resp.setPosX(ybr.posX);
		resp.setPosZ(ybr.posZ);
		session.write(resp.build());
		//推送君主协助目标的列表
		pushHelpList4YB(jz, session);
		//保存加入协助成功快报
		PromptMSG msg=sendJionSuccessKB2XieZhu(ybjzId, ybr.name, jzId);
		PromptMsgMgr.inst.pushSubao(session, msg);
		AskYaBiaoHelpResp.Builder resp2Asker = AskYaBiaoHelpResp.newBuilder();
		resp2Asker.setCode(code);
		XieZhuJunZhu.Builder xzJz = XieZhuJunZhu.newBuilder();
		xzJz.setJzId(jzId);
		xzJz.setName(jz.name);
		xzJz.setRoleId(jz.roleId);
		
		//2015年8月31日返回盟友增加的护盾 2015年12月3日 1.1版本去掉护盾
		int curHp=jz.shengMing;
		Integer uid=(Integer)session.getAttribute(SessionAttKey.playerId_Scene);
		if(uid != null){
			Player p= sc.players.get(uid);
			if(p!=null){
				curHp=p.currentLife;
			}
		}
		xzJz.setCurHp(curHp);
		xzJz.setMaxHp(jz.shengMingMax);
		resp2Asker.setJz(xzJz);
		SessionUser su = SessionManager.inst.findByJunZhuId(ybjzId);
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
	//2015年12月12日 1.1版本 劫镖人请求进入场景方法废弃
	//2016年2月26日 踢除协助人方法清理
	//移除某个协助者  2016年1月12日 1.1版本无此功能了


	/**
	 * @Description: 广播镖车附加信息
	 * @param sc
	 * @param ybr
	 */
	public void broadBiaoCheInfo(Scene sc, YaBiaoRobot ybr) {
		Integer uid=(Integer)ybr.session.getAttribute(SessionAttKey.playerId_Scene);
		if(uid == null){
//			log.error("广播马车信息出错：找不到君主{}马车的uid",ybr.jzId);
			return ;
		}
		int code=10;
		// 10押送中 20 战斗中 30 保护CD 40到达终点 50镖车摧毁
		BiaoCheState.Builder resp = BiaoCheState.newBuilder();
		resp.setState(code);
		resp.setUid(uid);
		int jindu=	(int)((ybr.usedTime/(double)ybr.totalTime)*100);
		jindu=jindu>100?100:jindu;
		resp.setJindu(jindu);//进度（是一个百分比）
		int protectTime = ybr.protectTime
				- ((int) (System.currentTimeMillis() - ybr.endBattleTime) / 1000);
		resp.setBaohuCD(protectTime > 0 ? protectTime : 0);
		int jiaSuTime=ybr.upSpeedTime;
		resp.setJiasuTime(jiaSuTime);

		for(Player player : sc.players.values()){
			//机器人马车不广播 2016年1月29日 状态不在押镖不广播
			if(player.roleId==Scene.YBRobot_RoleId||player.pState!=State.State_YABIAO){
				continue;
			}
			player.session.write(resp.build());
		}
	}


	
	/**
	 * @Description:移除镖车
	 */
	public void removeYBJzInfo(YaBiaoRobot ybr) {
		Scene sc = ybr.sc;
		sc.exit4YaBiaoRobot(ybr);
		log.info("从场景-{}移除君主镖车信息-{}成功", ybr.sc.name, ybr.jzId);
	}
	


	/**
	 * @Description 移除参加押镖的协助者
	 * @param ybName
	 * @param ybjzId
	 * @param ybWorth 运镖君主马车的100%价值 根据策划要求
	 * @param isSuccess
	 */
	private void removeXieZhu4EndYB(String ybName, Long ybjzId, String ybWorth,
			boolean isSuccess) {
		log.info("结束押镖，清除{}的押镖协助者", ybjzId);
		// 移除参加押镖的协助者
		HashSet<Long> xzSet = xieZhuCache4YBJZ.get(ybjzId);
		// 移除押镖君主的协助者队列
		xieZhuCache4YBJZ.remove(ybjzId);
		answerHelpCache4YB.remove(ybjzId);
		if (xzSet != null) {
			for (Long xzJzId : xzSet) {
				JunZhu jz = HibernateUtil.find(JunZhu.class, xzJzId);
				if (jz != null) {
				// 2015年11月27日改为发盟友快报
					sendKuaiBao2XieZhu(ybjzId,ybName,xzJzId,ybWorth, isSuccess);
				}
//				xzJZSatrtYB.remove(xzJzId);
				//通知协助君主更新协助列表
				noticeXieZhu4Remove(xzJzId);
			}
		}
	}
	//通知协助君主更新协助列表
	public void noticeXieZhu4Remove(long xzJzId) {
		log.info("通知协助君主--{}更新协助列表",xzJzId);
		JunZhu jz =HibernateUtil.find(JunZhu.class, xzJzId);
		if(jz==null){
			log.error("通知协助君主--{}更新协助列表失败，未找到JunZhu",xzJzId);
			return;
		}
	
		SessionUser su = SessionManager.inst.findByJunZhuId(xzJzId);
		if (su != null) {
			//推送君主协助目标的列表
			pushHelpList4YB(jz, su.session);
		}
	}

	/**
	 * @Description 	// 发送成功协助快报给协助者
	 */
	public PromptMSG sendJionSuccessKB2XieZhu(long  ybJzId, String ybJzName, long jzId) {
		//协助成功
		int eventId=SuBaoConstant.jionxz_toSelf;
		PromptMSG msg=PromptMsgMgr.inst.saveLMKBByCondition(jzId, ybJzId, new String[]{ybJzName,""}, eventId, -1);
		log.info("发送协助{}押镖成功的快报给君主--{}", ybJzId, jzId);
		return msg;
	}
	/**
	 * @Description 	// 发送邮件给协助者 2015年11月27日改为发盟友快报
	 * @param ybJzId
	 * @param ybJzName
	 * @param jzId
	 * @param shouru
	 * @param isSuccess
	 */
	public void sendKuaiBao2XieZhu(long  ybJzId, String ybJzName, long jzId,String shouru,
			boolean isSuccess) {
		PromptMSG msg=null;
		int eventId=0;
		if (isSuccess) {
			//协助成功
			eventId=SuBaoConstant.xzcg;
			msg=PromptMsgMgr.inst.saveLMKBByCondition(jzId, ybJzId, new String[]{ybJzName,""}, eventId, -1);
			log.info("发送协助{}押镖成功的快报给君主--{}", ybJzId, jzId);
		} else {
			//协助失败
			eventId=SuBaoConstant.xzsb;
			msg=PromptMsgMgr.inst.saveLMKBByCondition(jzId, ybJzId, new String[]{ybJzName, ""}, eventId, -1);
			log.info("发送协助{}押镖失败的快报给君主--{}", ybJzId, jzId);
		}
		SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
		if (msg!=null&&su != null){
			PromptMsgMgr.inst.pushSubao(su.session, msg);
		}
	}
	//2015年12月12日 1.1版本 发送邮箱给协助者废弃
	

	
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
			bean.todayFuliTimes1=0;
			bean.todayFuliTimes2=0;
			bean.lastShowTime = new Date();
			HibernateUtil.save(bean);
		}else{
			// do nothing
			log.info("还是今天，不重置用户押镖数据--君主ID--{}", bean.junZhuId);
		}
		return bean;
	}
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
			zdbean.buyblood4Vip=0;
			zdbean.bloodTimes4Vip=0;
			zdbean.lastResetTime=new Date();
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
		//杀死仇人计算公式JunzhuShengji -- moneyXishu
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
		PromptMSG msg=	PromptMsgMgr.inst.saveLMKBByCondition(jzId, enemyJzId, new String[]{jz.name, enemyName,null,null,null,otherJz.level+""},  eventId, horseType);
		SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
		if (msg!=null&&su != null){
			PromptMsgMgr.inst.pushSubao(su.session, msg);
		}
		String	award=msg!=null?msg.award:"";
		log.info("{}杀死了仇人{},获得奖励{}",jzId,enemyJzId,award);
	}
	/**
	 * @Description  劫镖成功结算
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
		//打劫君主的押镖战斗数据
		YBBattleBean jbBattleBean = getYBZhanDouInfo(dajieJz.id, dajieJz.vipLevel);
		if(jbBattleBean.remainJB4Award<=0){
			log.info("{}剩余有奖打劫次数为0，本次无奖励",djJzId);
			return;
		}
		if(ybJzId<0){
			log.info("{}劫镖目标君主{}，尝试打劫系统镖车结算",djJzId,ybJzId);
			settleJieBiao4Sys(ybJzId, dajieJz,jbBattleBean,session);
		}else{
			log.info("{}劫镖目标君主{}，尝试打劫君主镖车结算",djJzId,ybJzId);
			settleJieBiao4Jz(ybJzId,dajieJz,jbBattleBean,session);
		}
		// 历史劫镖
		YunBiaoHistory histo = getYunBiaoHistory(djJzId);
		histo.historyJB += 1;
		HibernateUtil.save(histo);
				
		//移除押镖机器人线程中的YaBiaoRobot对象
		BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.remove(ybJzId);
		// 每日任务：完成一次劫镖活动
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
				djJzId, DailyTaskConstants.jieBiao, 1));
		// 主线任务完成:劫镖
		EventMgr.addEvent(ED.finish_jiebiao_x, new Object[] { djJzId });
	}
	/**
	 * @Description 打劫君主镖车结算
	 * @param ybJzId
	 * @param session
	 */
	public void settleJieBiao4Jz(long ybJzId,JunZhu dajieJz,YBBattleBean jbBattleBean, IoSession session) {
		long djJzId = dajieJz.id;
		JunZhu yabiaoJz = HibernateUtil.find(JunZhu.class, ybJzId);
		if (yabiaoJz == null) {
			log.error("{}劫镖出错：目标君主不存在",djJzId);
			return;
		}
		Integer scId =(Integer)session.getAttribute(SessionAttKey.SceneID);
		if(scId==null){
			log.error("{}劫镖出错：君主不在押镖场景",djJzId);
			return;
		}
		//押镖君主的押镖战斗数据
		YBBattleBean ybBattleBean = getYBZhanDouInfo(dajieJz.id, dajieJz.vipLevel);
		YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, ybJzId);
		if (ybBean == null) {
			log.error("未找到被劫镖者{}，结算失败", ybJzId);
			return;
		}
		YaBiaoRobot ybr = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(ybJzId);
		if(ybr==null){
			log.error("劫镖出错：目标{}镖车不存在",ybJzId);
			return;
		}
		// ===================================以下劫镖人结算===================================
		CartTemp cart = cartMap.get(ybr.horseType);
		if(cart==null){
			log.error("劫镖出错：目标{}镖车马匹类型{}为找到CartTemp配置",ybJzId,ybr.horseType);
			return;
		}
		double enemyJiaCheng=100;
		boolean isEmeny= isEmeny(djJzId, ybJzId);
		//仇人收益
		if(isEmeny){
			enemyJiaCheng+=YunbiaoTemp.foeCart_incomeAdd_pro;
		}
		//根据时段收益增益
		//2016年1月20日 增加“劫镖收益减益”
		int dajieshouyi0 = (int) ((ybr.worth * cart.robProfit)*(enemyJiaCheng/100)*SHOUYI_PROFIT/100); 
		int dajieshouyi=getJieBiaoShouYi(dajieJz,yabiaoJz,ybr,cart,enemyJiaCheng);
		log.info("原算法收益---------------------"+dajieshouyi0+"现在算法收益---------------------"+dajieshouyi);
		//2015年9月1日拆分字段到新表
		YunBiaoHistory ybHis =getYunBiaoHistory(dajieJz.id);
		ybHis.successJB += 1;
		HibernateUtil.save(ybHis);
		jbBattleBean.usedJB+=1;
		jbBattleBean.remainJB4Award-=1;//到这里还能减成负数，前面是怎么进行的
		HibernateUtil.save(jbBattleBean);
		log.info("劫镖者{}劫镖成功，仇人加成--{}，收益{},劫镖次数{}，剩余次数{}", dajieJz.id,enemyJiaCheng/100,dajieshouyi,jbBattleBean.usedJB,jbBattleBean.remainJB4Award);
		ActLog.log.LootDart(dajieJz.id, dajieJz.name, ActLog.vopenid, ActLog.vopenid, ybJzId, "", dajieshouyi, ybHis.successJB);
		// 劫镖成功，加入劫镖人的记录
		saveYaBiaoHistory(dajieJz, yabiaoJz, 3, dajieshouyi, ybr.horseType);
	
		//发放劫镖成功联盟相关奖励
		int rob2gongxian=gainAllianceAward4JieBiao(djJzId, cart);
		//劫镖成功快报
		sendSuccessKB2JBJZ(djJzId,ybJzId, ybr.name,dajieshouyi,isEmeny,rob2gongxian);
		// ===================================以下押镖人结算===================================
		//押镖结算
		int shouru=0;
		if(ybBattleBean.baodi>0){
			MaJu baodi=majuMap.get(ybBattleBean.baodi);
			if(baodi==null){
				shouru = (int) (ybr.worth *1*SHOUYI_PROFIT/100);
				log.error("君主{}有保底收益道具,但是未找到保底道具配置收益,保底收益率默认成--{}",ybJzId,1);
			}else{
				double baodilv=((double)baodi.value1)/100;//Integer.valueOf(baodi.function)/100 ;//保底收益率
				shouru = (int) (ybr.worth * baodilv*SHOUYI_PROFIT/100);
				log.info("君主{}有保底收益道具，保底收益率--{}",ybJzId,baodilv);
			}
		}else{
			shouru = (int) (ybr.worth * cart.failProfit*SHOUYI_PROFIT/100);
		}
		final int horseType=ybr.horseType;
		//			2015年10月8日押镖君主收益应该在邮件领取
		//更新运镖数据 2016年1月18日挪到运镖开始就扣除次数
		ybBean.isNew4History=true;
		ybBean.isNew4Enemy=true;
		HibernateUtil.save(ybBean);
		//重置道具
		ybBattleBean.baohu = 0;
		ybBattleBean.baodi = 0;
		ybBattleBean.jiasu = 0;
		HibernateUtil.save(ybBattleBean);
		//加入被劫镖者仇人列表 
		saveJieBiaoEnemy(ybJzId,djJzId);
		// 劫镖成功，加入被劫镖者的记录
		saveYaBiaoHistory(yabiaoJz, dajieJz, 4, dajieshouyi, ybr.horseType);
		// 玩家镖车被杀死 ，移除押镖者
		removeYBJzInfo(ybr);
		// 移除本次押镖答复的队列
		//发放运镖成功镖联盟相关奖励
		int gongxian=gainAllianceAward4YaBiaoFail(ybJzId, cart);
		//发送押镖失败快报给押镖人  2015年11月27日改成发快报 不发邮件
		sendLoseKB2YBJZ(ybJzId, djJzId, dajieJz.name,shouru,gongxian);
		//推送新历史记录给押镖者
		pushYBRecord(ybJzId, true, true);
		//移除协助者并发送快报
		removeXieZhu4EndYB(yabiaoJz.name, ybJzId,ybr.worth+"", false);
		//告诉运镖君主的仇人 他不在运镖
		refreshYabiaoState2Enemy(ybJzId);
		//2016年3月7日 策划加入运镖联盟相关数值
	
		//押镖镖车从场景中移除事件
		EventMgr.addEvent(ED.BIAOCHE_END, new Object[] { ybJzId,horseType, ybr.startTime});
		EventMgr.addEvent(ED.BIAOCHE_CUIHUI, new Object[] { yabiaoJz,dajieJz,horseType,ybr.worth+""});
		
	}
	
	/**
	 * @Description 发放劫镖成功联盟相关奖励  会返回个人得到的联盟贡献
	 * @param jbJz
	 * @param cart
	 */
	public int gainAllianceAward4JieBiao(long jzId,CartTemp cart) {
		log.info("发放  劫镖成功的君主---{} ,联盟相关奖励",jzId);
		AlliancePlayer  player = HibernateUtil.find(AlliancePlayer.class, jzId);
		if (player == null || player.lianMengId <= 0) {
			log.info("发放  劫镖成功的君主---{} ,联盟相关奖励失败，玩家没有联盟", jzId);
			return 0;
		}
		int lmId=player.lianMengId;
		AllianceBean lmBean = HibernateUtil.find(AllianceBean.class, lmId);
		if (lmBean == null) {
			log.error("发放  劫镖成功的君主---{} ,联盟相关奖励失败，联盟{}不存在", jzId, lmId);
			return 0;
		}
		//2016年3月12日 策划要求把贡献发到速报里面
		int gongxian=cart.rob_Jianshe;
//		player.gongXian += gongxian;
//		log.info("玩家---{}劫镖成功，获得联盟贡献---{}", jzId,gongxian);
//		HibernateUtil.save(player);
		
		int jianshe=cart.rob_Jianshe;
		log.info("玩家---{}劫镖成功，给联盟--{}，增加联盟贡献---{}", jzId,lmId,jianshe);
		AllianceMgr.inst.changeAlianceBuild(lmBean, jianshe);
		return gongxian;
	}
	
	/**
	 * @Description 发放运镖成功镖联盟相关奖励  会返回个人得到的联盟贡献
	 */
	public int  gainAllianceAward4YaBiaoSuccess(long jzId,CartTemp cart) {
		log.info("发放运镖成功的君主---{} ,联盟相关奖励",jzId);
		AlliancePlayer  player = HibernateUtil.find(AlliancePlayer.class, jzId);
		if (player == null || player.lianMengId <= 0) {
			log.info("获取玩家联盟失败，玩家:{}没有联盟", jzId);
			return 0;
		}
		int lmId=player.lianMengId;
		AllianceBean lmBean = HibernateUtil.find(AllianceBean.class, lmId);
		if (lmBean == null) {
			log.error("获取玩家联盟失败，联盟{}不存在", lmId);
			return 0;
		}
		//2016年3月12日 策划要求把贡献发到速报里面
		int gongxian=cart.success_Jianshe;
//		player.gongXian += gongxian;
//		log.info("玩家---{}运镖成功，获得联盟贡献---{}", jzId,gongxian);
//		HibernateUtil.save(player);
		int jianshe=cart.success_Jianshe;
		log.info("玩家---{}运镖成功，给联盟--{}，增加联盟建设值---{}", jzId,lmId,jianshe);
		AllianceMgr.inst.changeAlianceBuild(lmBean, jianshe);
		return gongxian;
	}
	/**
	 * @Description 发放运镖失败镖联盟相关奖励  会返回个人得到的联盟贡献
	 */
	public int gainAllianceAward4YaBiaoFail(long jzId,CartTemp cart) {
		log.info("发放运镖失败的君主---{} ,联盟相关奖励",jzId);
		AlliancePlayer  player = HibernateUtil.find(AlliancePlayer.class, jzId);
		if (player == null || player.lianMengId <= 0) {
			log.info("获取玩家联盟失败，玩家:{}没有联盟", jzId);
			return 0;
		}
		int lmId=player.lianMengId;
		AllianceBean lmBean = HibernateUtil.find(AllianceBean.class, lmId);
		if (lmBean == null) {
			log.error("获取玩家联盟失败，联盟{}不存在", lmId);
			return 0;
		}
		//2016年3月12日 策划要求把贡献发到速报里面
		int gongxian=cart.fail_Jianshe;
//		player.gongXian += gongxian;
//		log.info("玩家---{}运镖失败，获得联盟贡献---{}", jzId,gongxian);
//		HibernateUtil.save(player);
		int jianshe=cart.fail_Jianshe;
		log.info("玩家---{}运镖失败，给联盟--{}，增加联盟建设值---{}", jzId,lmId,jianshe);
		AllianceMgr.inst.changeAlianceBuild(lmBean, jianshe);
		return gongxian;
	} 
	/**
	 * @Description 算出劫镖收益
	 * @return
	 */
	public int getJieBiaoShouYi(JunZhu djJz, JunZhu ybJz, YaBiaoRobot ybr, CartTemp cart, double enemyJiaCheng) {
		int djJzLevel=djJz.level;
		int ybjzLevel=ybJz.level;
		int levelDistance=YunbiaoTemp.robincome_LvMax;
		//当被劫镖//马等级-劫镖人等级≥5时，基础劫镖收益按照劫镖人等级+5计算（等级差可配置)
		int shouyiLevel	=(ybjzLevel-djJzLevel>levelDistance)?djJzLevel+5:ybjzLevel;
		int djZhanli = JunZhuMgr.inst.getJunZhuZhanliFinally(djJz);
		double ybZhanli =ybr.zhanli;
		double k4temp=ybZhanli>djZhanli?1.0:ybZhanli/djZhanli;
		int k=(int) (k4temp*100);
		RobCartXishu robCartXishu=robCartXishuMap.get(k);
		if(robCartXishu==null){
			log.error("算出劫镖收益--{}出错,未找到RobCartXishu配置,强制取第一个配置",djJz.id );
			robCartXishu=robCartXishuMap.get(0);
		}
		double w=robCartXishu.xishu;
		JunzhuShengji ss = JunZhuMgr.inst.getJunzhuShengjiByLevel(shouyiLevel);
		if(ss==null){
			log.error("产生系统机器人--{}出错,未找到JunzhuShengji配置",shouyiLevel );
		}
		//2016年1月25日 加入马具收益加成
		double majujiacheng=0;
		if(ybJz.id>0){
			YBBattleBean zdbean =getYBZhanDouInfo(ybJz.id, ybJz.vipLevel);
			if(zdbean.baohu>0){
				MaJu maju=majuMap.get(zdbean.baohu);
				majujiacheng+=maju.profitPara;
			}
			if(zdbean.jiasu>0){
				MaJu maju=majuMap.get(zdbean.jiasu);
				majujiacheng+=maju.profitPara;
			}
		}
		int baseDaJieShouyi = (int) (ss.xishu *( cart.profitPara+majujiacheng)* cart.robProfit) ;
		log.info("君主--{}劫镖结算JunzhuShengji系数--{},profitPara==={},robProfit===={},仇人加成--{},系统多倍收益率---{}%，马车价值--{}",
				djJz.id,ss.xishu,cart.profitPara,cart.robProfit,enemyJiaCheng,SHOUYI_PROFIT,ybr.worth);
		int dajieshouyi = (int) ((baseDaJieShouyi*w)*(enemyJiaCheng/100)*SHOUYI_PROFIT/100);
		return dajieshouyi;
	}

	/**
	 * @Description 杀死系统镖车得到奖励
	 * @param ybJzId
	 * @param dajieJz
	 * @param session
	 */
	public void settleJieBiao4Sys(long ybJzId,JunZhu dajieJz,YBBattleBean jbBattleBean,IoSession session) {
		long djJzId=dajieJz.id;
		//处理系统镖车机器人
		log.info("{}打劫系统机器人--{}领奖--开始",djJzId, ybJzId);
		Integer scId =(Integer)session.getAttribute(SessionAttKey.SceneID);
		if(scId==null){
			log.error("{}劫镖出错：君主不在押镖场景",djJzId);
			return;
		}
		//打劫君主的押镖战斗数据
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
		CartNPCTemp cartNpcTemp = biaoCheNpcMap.get(ybr.bcNPCId);
		JunZhu ybjz = cartNpcTemp.getSimuJunZhu(ybJzId); 
		//根据时段收益增益
		int dajieshouyi0 = (int) ((ybr.worth * cart.robProfit)*SHOUYI_PROFIT/100);
		//2016年1月20日 增加“劫镖收益减益”
		int dajieshouyi=getJieBiaoShouYi(dajieJz,ybjz,ybr,cart,100);
		//TODO 待删除  啥时候验收了再删这个输出吧 ，策划自己都算不对到底给多少奖励
		log.info("原算法收益---------------------"+dajieshouyi0+"现在算法收益---------------------"+dajieshouyi);
		//2015年9月1日拆分字段到新表
		YunBiaoHistory ybHis = getYunBiaoHistory(dajieJz.id);
		ybHis.successJB += 1;
		HibernateUtil.save(ybHis);
		jbBattleBean.usedJB+=1;
		jbBattleBean.remainJB4Award-=1;
		HibernateUtil.save(jbBattleBean);
		log.info("劫镖者{}劫镖系统机器人--{}镖车编号--{}等级--{}成功，收益{},劫镖次数{}，剩余次数{}", dajieJz.id,ybr.jzId,ybr.bcNPCNo,ybr.jzLevel, dajieshouyi,jbBattleBean.usedJB,jbBattleBean.remainJB4Award);
		//劫镖成功快报 打劫系统镖车没有建设值 贡献值
		int rob2gongxian=0;
		sendSuccessKB2JBJZ(djJzId,ybJzId,ybr.name, dajieshouyi,false,rob2gongxian);
		//劫镖成功，加入劫镖人的记录
		saveYaBiaoHistory2SysCart(dajieJz, ybr, 3, dajieshouyi, ybr.horseType);
		
		//系统镖车死亡， 移除运镖君主信息
		removeYBJzInfo(ybr);
		ActLog.log.LootDart(dajieJz.id, dajieJz.name, ActLog.vopenid, ActLog.vopenid, ybJzId, "", dajieshouyi, ybHis.successJB);

	}
	//2015年12月12日 1.1版本  结算国家仇恨废弃

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
	private void saveYaBiaoHistory2SysCart(JunZhu jz, YaBiaoRobot ybr, int result,
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
		int roleId=cnt==null?1:cnt.roleId;
		yaBiaoH.enemyRoleId = roleId;
		yaBiaoH.horseType = horseType;
		Long sizeAfterAdd = DB.rpush4JieBiao((HISTORY_KEY + jz.id).getBytes(),
				SerializeUtil.serialize(yaBiaoH));
		if (sizeAfterAdd > historySize) {
			Redis.getInstance().lpop(HISTORY_KEY + jz.id);
		}
	}


	/**
	 * @Description: 存储仇人
	 * @param jzId 押镖君主ID
	 * @param enemyId 劫镖者ID
	 */
	private void saveJieBiaoEnemy(long jzId, long enemyId) {
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
	 * @Description: 君主押镖成功结算
	 * @param jzId
	 * @param roomId
	 */
	public void settleYaBiaoSuccess4Jz(YaBiaoRobot ybr) {
		Long jzId = ybr.jzId;
		JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
		if (jz == null) {
			log.error("押镖结算出错：押镖君主不存在{}", jzId);
			return;
		}
		SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
		
		CartTemp cart = cartMap.get(ybr.horseType);
		if(cart==null){
			log.error("押镖结算出错,目标{}镖车马匹类型{}为找到CartTemp配置",jzId,ybr.horseType);
			return;
		}
		// 结算收益
		int shouru = ybr.worth ;
		final int horseType = ybr.horseType;
		//更新数据 2016年1月18日挪到运镖开始就扣除次数
		//2015年9月1日拆分字段到新表
		YunBiaoHistory ybHis =getYunBiaoHistory( jz.id);
		ybHis.successYB += 1;
		HibernateUtil.save(ybHis);
		log.info("{}押镖成功,现在的成功押镖次数为{}", jz.id, ybHis.successYB);
		//重置道具
		YBBattleBean zdBean = getYBZhanDouInfo(jzId, jz.vipLevel);
		zdBean.baohu = 0;
		zdBean.baodi = 0;
		zdBean.jiasu = 0;
		HibernateUtil.save(zdBean);
		if (su != null) {
			JunZhuMgr.inst.sendMainInfo(su.session);// 推送铜币信息
		} 
	
		//2016年3月7日 策划加入运镖联盟相关数值
		//发放运镖成功镖联盟相关奖励
		int gongxian=gainAllianceAward4YaBiaoSuccess(jzId, cart);
		//发送运镖成功快报
		sendSuccessKB2YBJZ(jzId,shouru,gongxian);
		//2016年1月7日 移除第一次被攻击记录
		FightMgr.inst.cartInjuredFirstRecord.remove(jzId);
		// 移出协助者
		removeXieZhu4EndYB(jz.name, jz.id, ybr.worth+"", true);
		//添加运镖成功广播事件
		EventMgr.addEvent(ED.YA_BIAO_SUCCESS, new Object[]{jz,horseType});
		//押镖镖车从场景中移除事件 
		EventMgr.addEvent(ED.BIAOCHE_END, new Object[] { jzId,horseType, ybr.startTime});
		//保存联盟押镖成功事件
		AlliancePlayer ap = HibernateUtil.find(AlliancePlayer.class, jzId);
		if (ap == null) {
			log.error("联盟成员信息没有找到：{},不保存《联盟押镖成功》事件", jzId);
			return;
		}
		int lmId = ap.lianMengId;
		if (lmId <= 0) {
			log.warn("{}已不在联盟中,,不保存《联盟押镖成功》事件", jzId);
			return;
		}
		int jianshe=cart.success_Jianshe;
		//保存联盟押镖成功事件
		saveAllianceEvent(jz.name, jianshe, lmId,27);

	}
	
	/**
	 * @Description 保存联盟押镖事件
	 * @param jzName
	 * @param jianshe
	 * @param lianMengId
	 * @param code 27 运镖成功 其他种类策划暂时没加  谁知道什么时候会用
	 */
	public void saveAllianceEvent(String jzName,int jianshe,int lianMengId,int code) {
		String eventStr = AllianceMgr.inst.lianmengEventMap.get(code).str.replaceFirst("%d", jzName)
				.replaceFirst("%d",jianshe+"");
		AllianceMgr.inst.addAllianceEvent(lianMengId, eventStr);
	}

	/**
	 * @Description: 押镖成功结算
	 * @param jzId
	 * @param roomId
	 */
	public  void  settleYaBiaoSuccess(YaBiaoRobot ybr) {
		ybr.sc.exitYBSc(ybr.session);
		if(ybr.jzId<0){
			log.info("系统镖车---{}走到终点",ybr.jzId);
		}else {
			settleYaBiaoSuccess4Jz(ybr);
		}
	}


	/**
	 * @Description: 发送押镖成功快报给押镖人  2015年11月27日改成发快报 不发邮件
	 * @param jzName
	 */
	public void sendSuccessKB2YBJZ(long ybJzId,int shouru,int gongxian) {
		//押镖成功
		int eventId=SuBaoConstant.ybcg;
		PromptMSG msg=	PromptMsgMgr.inst.saveLMKBByCondition(ybJzId, ybJzId, new String[]{"", "",shouru+"#"+gongxian},  eventId,  0);
		SessionUser su = SessionManager.inst.findByJunZhuId(ybJzId);
		if (msg!=null&su != null){
			PromptMsgMgr.inst.pushSubao(su.session, msg);
		}
		log.info("发送押镖成功快报给--{}",  ybJzId);
	}
	/**
	 * @Description: 发送打劫成功快报  2015年11月27日改成发快报 不发邮件
	 */
	public void sendSuccessKB2JBJZ(long jbJzId,long ybJzId,String ybName,int shouru,boolean isEmeny,int gongxian) {
		int eventId=0;
		PromptMSG msg=null;
		if(isEmeny){
			//打劫仇人成功给自己
			eventId=SuBaoConstant.djcr_toSelf;
			msg=PromptMsgMgr.inst.saveLMKBByCondition(jbJzId, ybJzId, new String[]{ybName,"",shouru+"#"+gongxian},  eventId,  -1);
		}else{
			//打劫非仇人成功给自己
			eventId=SuBaoConstant.djfcr_toSelf;
			msg=PromptMsgMgr.inst.saveLMKBByCondition(jbJzId, ybJzId, new String[]{ybName,"",shouru+"#"+gongxian},  eventId,  -1);
		}
		SessionUser su = SessionManager.inst.findByJunZhuId(jbJzId);
		if (msg!=null&&su != null){
			JunZhu jz = JunZhuMgr.inst.getJunZhu(su.session);
			if(jz!=null){
				pushJieBiaoRemainTimes(jz, su.session);
			}else{
				log.error("向{}推送劫镖次数失败，君主为空",  jbJzId);
			}
			PromptMsgMgr.inst.pushSubao(su.session, msg);
		}
		log.info("发送打劫成功快报给--{}",  jbJzId);
	}
	/**
	 * @Description: 发送押镖失败快报给押镖人  2015年11月27日改成发快报 不发邮件
	 * @param jzName
	 */
	public void sendLoseKB2YBJZ(long ybJzId,long jbJzId,String jbJzName,int ybShouyi,int gongxian) {
		int eventId=SuBaoConstant.ybsb ;//运镖失败
		PromptMSG msg =	PromptMsgMgr.inst.saveLMKBByCondition(ybJzId, jbJzId, new String[]{jbJzName,"",ybShouyi+"#"+gongxian}, eventId,  -1);
		SessionUser su = SessionManager.inst.findByJunZhuId(ybJzId);
		if (msg!=null&&su != null){
			PromptMsgMgr.inst.pushSubao(su.session, msg);
		}
		log.info("发送押镖失败快报给--{}",  ybJzId);
	}
	/** 
	 * @Description: 获取押镖次数   随机可能变成根据vip等级获得
	 * @param vipLev
	 * @return
	 */
	public int getYaBiaoCountForVip(int vipLev) {
		return CanShu.YUNBIAO_MAXNUM;
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
		switch (type) {
		case 10:
			buyYaBiaoCount(jz, session);
			break;
//		case 20:
//			log.error("{}请求购买参数错误{}，1.1版本不能购买劫镖次数", jz.id, type);
//			buyJieBiaoCount(jz, session);
//			break;
		case 30:
			log.info("{}请求领取押镖福利次数", jz.id, type);
			gainYaBiaoFuliTimes(jz, session);
			break;
		default:
			log.error("{}请求购买参数错误{}", jz.id, type);
			break;
		}
	}
	//领取福利次数
	public void gainYaBiaoFuliTimes(JunZhu jz, IoSession session) {
		long jzId=jz.id;
		GainFuLiResp.Builder resp = GainFuLiResp.newBuilder();
		YaBiaoBean bean = getYaBiaoBean(jzId, jz.vipLevel);
		int fuliTimeCode=getNowFuliTime();
		boolean isReturn=false;
		int fuliTimes=bean.todayFuliTimes1+bean.todayFuliTimes2+bean.todayFuliTimes3;
		switch (fuliTimeCode) {
		case 1:
			if(bean.todayFuliTimes1>0){
				log.error("{}请求领取福利次数1失败，当前时段福利已经领取", jzId);
				resp.setResult(20);
				resp.setFuliTimes(fuliTimes);
				resp.setLeftYBTimes(bean.remainYB);
				isReturn=true;
			}
			break;
		case 2:
			if(bean.todayFuliTimes2>0){
				log.error("{}请求领取福利次数2失败，当前时段福利已经领取", jzId);
				resp.setResult(20);
				resp.setFuliTimes(fuliTimes);
				resp.setLeftYBTimes(bean.remainYB);
				isReturn=true;
			}
			break;
		case 3:
			if(bean.todayFuliTimes3>0){
				log.error("{}请求领取福利次数3失败，当前时段福利已经领取", jzId);
				resp.setResult(20);
				resp.setFuliTimes(fuliTimes);
				resp.setLeftYBTimes(bean.remainYB);
				isReturn=true;
			}
			break;
		default:
			log.error("{}请求领取福利次数失败，当前时段福利不是福利时段", jzId);
			resp.setResult(20);
			resp.setFuliTimes(fuliTimes);
			resp.setLeftYBTimes(bean.remainYB);
			isReturn=true;
			break;
		}
		if(isReturn){
			session.write(resp.build());
			return;
		}
		sendFuliTimesKB2JZ(jzId);
		resp.setResult(10);
		resp.setFuliTimes(fuliTimes);
		resp.setLeftYBTimes(bean.remainYB);
		session.write(resp.build());
		log.info("{}请求领取福利次数成功,发送快报完成", jzId);
	}
	//获取当前是第几个福利时段
	public int  getNowFuliTime() {
		boolean isNow1=	DateUtils.isInDeadline4Start(YunbiaoTemp.incomeAdd_startTime1, YunbiaoTemp.incomeAdd_endTime1);
		if(isNow1){
			isNow1=YunbiaoTemp.time1_switch==1?true:false;
		}
		if(isNow1){
			log.info("现在为第一个福利时段，{}---{}", YunbiaoTemp.incomeAdd_startTime1, YunbiaoTemp.incomeAdd_endTime1);
			return 1;
		}
		boolean isNow2=	DateUtils.isInDeadline4Start(YunbiaoTemp.incomeAdd_startTime2, YunbiaoTemp.incomeAdd_endTime2);
		if(isNow2){
			isNow2=YunbiaoTemp.time2_switch==1?true:false;
		}
		if(isNow2){
			log.info("现在为第二个福利时段，{}---{}", YunbiaoTemp.incomeAdd_startTime2, YunbiaoTemp.incomeAdd_endTime2);
			return 2;
		}
		boolean isNow3=	DateUtils.isInDeadline4Start(YunbiaoTemp.incomeAdd_startTime3, YunbiaoTemp.incomeAdd_endTime3);
		if(isNow3){
			isNow3=YunbiaoTemp.time3_switch==1?true:false;
		}
		if(isNow3){
			log.info("现在为第三个福利时段，{}---{}", YunbiaoTemp.incomeAdd_startTime3, YunbiaoTemp.incomeAdd_endTime3);
			return 3;
		}
		log.info("现在不是福利时段");
		return 0;
	}
//领取福利次数保存操作
	public void saveFuliTimes(JunZhu jz ,int fuliTimes) {
		if (jz == null) {
			return;
		}
		if (fuliTimes <= 0) {
			return;
		}
		long jzId=jz.id;
		GainFuLiResp.Builder resp = GainFuLiResp.newBuilder();
		YaBiaoBean ybBean = getYaBiaoBean(jzId, jz.vipLevel);
		SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
		//TODO 不知道策划会不会增加福利时段 如果加了判断要改  
		//2016年2月1日策划已经  增加福利时段
		int fuliTimeCode=getNowFuliTime();
		boolean isReturn=false;
		int fuliTimes4Ret=ybBean.todayFuliTimes1+ybBean.todayFuliTimes2+ybBean.todayFuliTimes3;
		switch (fuliTimeCode) {
		case 1:
			if(ybBean.todayFuliTimes1>0){
				log.error("{}请求领取福利次数1失败，当前时段福利已经领取", jzId);
				resp.setResult(20);
				resp.setFuliTimes(fuliTimes4Ret);
				resp.setLeftYBTimes(ybBean.remainYB);
				isReturn=true;
			}
			break;
		case 2:
			if(ybBean.todayFuliTimes2>0){
				log.error("{}请求领取福利次数2失败，当前时段福利已经领取", jzId);
				resp.setResult(20);
				resp.setFuliTimes(fuliTimes4Ret);
				resp.setLeftYBTimes(ybBean.remainYB);
				isReturn=true;
			}
			break;
		case 3:
			if(ybBean.todayFuliTimes3>0){
				log.error("{}请求领取福利次数3失败，当前时段福利已经领取", jzId);
				resp.setResult(20);
				resp.setFuliTimes(fuliTimes4Ret);
				resp.setLeftYBTimes(ybBean.remainYB);
				isReturn=true;
			}
			break;
		default:
			log.error("{}请求领取福利次数失败，当前时段福利不是福利时段", jzId);
			resp.setResult(20);
			resp.setFuliTimes(fuliTimes4Ret);
			resp.setLeftYBTimes(ybBean.remainYB);
			isReturn=true;
			break;
		}
		if(isReturn){
			if (su != null){
				su.session.write(resp.build());
			}
			return;
		}
		ybBean.remainYB +=fuliTimes;
		switch (fuliTimeCode) {
		case 1:
			ybBean.todayFuliTimes1 =fuliTimes;
			break;
		case 2:
			ybBean.todayFuliTimes2 =fuliTimes;
			break;
		case 3:
			ybBean.todayFuliTimes3 =fuliTimes;
			break;
		default:
			break;
		}
		HibernateUtil.save(ybBean);
		if (su != null){
			refreshFuLiTimeState(jz, ybBean,fuliTimeCode, su.session);
			//推送剩余福利次数
			getFuliYilingTimes(jz, su.session);
		}
		fuliTimes4Ret=ybBean.todayFuliTimes1+ybBean.todayFuliTimes2+ybBean.todayFuliTimes3;
		log.info("{}请求领取福利次数---{}次成功,今日已领次数--{}", jzId,fuliTimes,fuliTimes4Ret);
	}

	/**
	 * @Description: 发放福利次数速报
	 */
	public void sendFuliTimesKB2JZ(long ybJzId) {
		//发放福利次数速报
		int eventId=SuBaoConstant.fuli_toSelf;
		PromptMSG msg=	PromptMsgMgr.inst.saveLMKBByCondition(ybJzId, ybJzId, null,  eventId,  0);
		SessionUser su = SessionManager.inst.findByJunZhuId(ybJzId);
		if (msg!=null&su != null){
			PromptMsgMgr.inst.pushSubao(su.session, msg);
		}
		log.info("发送发放福利次数速报给--{}",  ybJzId);
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
		resp.setLeftYBTimes(bean.remainYB);
		session.write(resp.build());
	}

	
	/**
	 * @Description:获取劫镖次数  随机可能变成根据vip等级
	 * @param vipLev
	 * @return
	 */
	public int getJieBiaoCountForVip(int vipLev) {
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
				log.info("刷新君主---{}福利时段开启状态",jz.id);
				YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jz.id);
				//2016年1月29日 策划要求没有开启也推送福利时段
				int fuliTimeCode=getNowFuliTime();
				refreshFuLiTimeState(jz,ybBean,fuliTimeCode, session);
				//判断功能是否开启
				boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.yabiao, jz.id, jz.level);
				if(!isOpen){
					log.info("刷新君主--{}押镖相关红点取消，押镖功能没开启",jz.id);
					return;
				}
				
				try {
					log.info("刷新君主--{}押镖相关红点",jz.id);
					refreshYaBiaoInfo(jz,ybBean, session);
				} catch (Exception e2) {
					log.error("刷新君主--{}押镖相关红点出错--{}",jz.id,e2);
				}
			
				break;
			}
	}
	//刷新君主福利时段开启状态
	public void refreshFuLiTimeState(JunZhu jz,YaBiaoBean ybBean ,int fuliTimeCode,IoSession session) {
		boolean isPush=false;
		//2016年1月29日 押镖没开启 或者要把开启了并且福利次数没领的人推送
		if(ybBean==null){
			isPush=true;
		}else{
			isPush=isNeedPushFuliTime(fuliTimeCode, ybBean);
		}
		if(isPush&&FULITIME_FLAG){
			//推送福利时间可以出现
			FunctionID4Open.pushOpenFunction(jz.id, session, FunctionID4Open.fuli);
		}else{
			//推送福利时间关闭
			FunctionID4Open.pushOpenFunction(jz.id, session, -FunctionID4Open.fuli);
		}
	}
	//是否需要推送福利按钮出现
	public boolean isNeedPushFuliTime(int fuliTimeCode,YaBiaoBean ybBean ) {
		boolean ret=false;
		switch (fuliTimeCode) {
		case 1:
			if(ybBean.todayFuliTimes1==0){
				ret=true;
			}
			break;
		case 2:
			if(ybBean.todayFuliTimes2==0){
				ret=true;
			}
			break;
		case 3:
			if(ybBean.todayFuliTimes3==0){
				ret=true;
			}
			break;
		default:
			break;
		}
		return ret;
	}
	public void pushFuLiTimeState() {
		log.info("向所有玩家推送福利活动开启状态---{}",FULITIME_FLAG);
		List<SessionUser> list = SessionManager.inst.getAllSessions();
		if(list == null){
			return;
		}
		int fuliTimeCode=getNowFuliTime();
		for (SessionUser su: list){
			if(su==null){
				continue;
			}
			IoSession session = su.session;
			JunZhu jz =  JunZhuMgr.inst.getJunZhu(session);
			if(jz==null){
				continue;
			}
			YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jz.id);
			if(ybBean==null){
				continue;
			}
			refreshFuLiTimeState(jz, ybBean, fuliTimeCode,session);
		}
		log.info("向所有玩家推送福利活动开启状态---{}结束",FULITIME_FLAG);
	}
	//刷新君主押镖相关红点
	public void refreshYaBiaoInfo(JunZhu jz,YaBiaoBean ybBean ,IoSession session) {
		if(ybBean != null){
			if(ybBean.isNew4Enemy){
				log.info("-----发送押镖有新仇人红点通知");
				/*
				 * 前台 页面等级发生改变，所以不用显示具体的，下同
				 */
				/*
				 * 又改为具体的了 ~ ~ 下同
				 */
				FunctionID.pushCanShowRed(jz.id, session, FunctionID.yabiao_enemy);
			}
			if(ybBean.isNew4History){
				log.info("-----发送押镖有新战斗记录红点通知");
				FunctionID.pushCanShowRed(jz.id, session, FunctionID.yabiao_history);
			}
		}
		int remain = -1;
		if(ybBean == null){
			remain = YaBiaoHuoDongMgr.inst.getYaBiaoCountForVip(0);
		}else{
			remain = ybBean.remainYB;
		}
		if(remain > 0){
			FunctionID.pushCanShowRed(jz.id, session, FunctionID.yabiao_ciShu);
		}
	}
	@Override
	protected void doReg() {
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
	}
	
	
	private class BloodReturn implements Runnable {
		public boolean bloodReturn = true;
		@Override
		public void run() {
			while(bloodReturn) {
				try {
					for(Scene scene : yabiaoScenes.values()) {
						Mission mission = new Mission(PD.SAFE_AREA_BOOLD_RETURN, null, null);
						scene.missions.add(mission);
					}
					Thread.sleep(YunbiaoTemp.saveArea_recovery_interval * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void buyReviveAllLifeTimes(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求满血复活次数出错：君主不存在");
			return;
		}
		BuyAllLifeReviveTimesReq.Builder req=(BuyAllLifeReviveTimesReq.Builder)builder;
		int buyCode = req.getCode();
		switch (buyCode) {
			case 1:
				buyReviveTimes(jz, session);
				break;
			case 0:
				getReviveTimesInfo(jz, session);
				break;
			default:
				sendBuyReviveAllLifeTimesReponse(session,1, 0, 0, 0,1);
				break;
		}
	}

	private void getReviveTimesInfo(JunZhu jz, IoSession session) {
		YBBattleBean zdbean = getYBZhanDouInfo(jz.id, jz.vipLevel);
		VipFuncOpen vipFuncOpen = VipMgr.INSTANCE.getVipFuncOpen(VipData.buy_revive_all_life);
		if(vipFuncOpen == null) {
			log.error("找不到购买满血复活次数vip配置，key:{}", VipData.buy_revive_all_life);
			return;
		}
		int needVipLevel = vipFuncOpen.needlv;
		if(jz.vipLevel < needVipLevel) {
			sendBuyReviveAllLifeTimesReponse(session, 3, 0, 0, 0, needVipLevel);
			return;
		}
		int dayTimes = zdbean.buyfuhuo4Vip;
		Purchase purchase = PurchaseMgr.inst.getPurchaseCfg(PurchaseConstants.BUY_FUHUO_COUNT, dayTimes+1);
		if(purchase == null) {
			log.error("找不到类型为:{}的purchase配置", PurchaseConstants.BUY_FUHUO_COUNT);
			return;
		}
		int	remainTimes	=YunbiaoTemp.resurgenceTimes + zdbean.fuhuoTimes4Vip - zdbean.fuhuo4uesd;
		sendBuyReviveAllLifeTimesReponse(session,0, remainTimes, purchase.getNumber(), purchase.getYuanbao(),needVipLevel);
	}

	private void buyReviveTimes(JunZhu jz, IoSession session) {
		YBBattleBean zdbean = getYBZhanDouInfo(jz.id, jz.vipLevel);
		int dayTimes = zdbean.buyfuhuo4Vip;
		Purchase purchase = PurchaseMgr.inst.getPurchaseCfg(PurchaseConstants.BUY_FUHUO_COUNT, dayTimes+1);
		if(purchase == null) {
			log.error("找不到类型为:{}的purchase配置", PurchaseConstants.BUY_FUHUO_COUNT);
			return;
		}
		VipFuncOpen vipFuncOpen = VipMgr.INSTANCE.getVipFuncOpen(VipData.buy_revive_all_life);
		if(vipFuncOpen == null) {
			log.error("找不到购买满血复活次数vip配置，key:{}", VipData.buy_revive_all_life);
			return;
		}
		int needVipLevel = vipFuncOpen.needlv;
		if(jz.vipLevel < needVipLevel) {
			sendBuyReviveAllLifeTimesReponse(session, 3, 0, 0, 0, needVipLevel);
			log.info("购买满血复活次数失败，vip等级不足:{}", needVipLevel);
			return;
		}
		int maxBuyTimes= VipMgr.INSTANCE.getValueByVipLevel(jz.vipLevel, VipData.buy_revive_times);
		if(zdbean.fuhuo4uesd >= maxBuyTimes){
			log.info("购买满血复活次数失败，今日购买次数用完，已使用:{}次", zdbean.fuhuo4uesd);
			sendBuyReviveAllLifeTimesReponse(session,4, 0, 0, 0,needVipLevel);
		}
		
		if(jz.yuanBao < purchase.getYuanbao()) {
			log.info("购买满血复活次数失败，元宝不足:{}", purchase.getYuanbao());
			sendBuyReviveAllLifeTimesReponse(session,2, 0, 0, 0,needVipLevel);
			return;
		}
		
		zdbean.buyfuhuo4Vip += 1;
		zdbean.fuhuoTimes4Vip += purchase.getNumber();
		HibernateUtil.save(zdbean);
		
		YuanBaoMgr.inst.diff(jz, -purchase.getYuanbao(), purchase.getYuanbao(), 0, YBType.YB_All_LIFE_REVIVE, "购买押镖满血复活次数");
		HibernateUtil.save(jz);
		JunZhuMgr.inst.sendMainInfo(session);
		log.info("君主:{}购买满血复活次数第{}次，花费元宝:{}得到次数:{}",jz.id, dayTimes+1, purchase.getYuanbao(), purchase.getNumber());
		int	remainTimes	=YunbiaoTemp.resurgenceTimes+zdbean.fuhuoTimes4Vip-zdbean.fuhuo4uesd;
		sendBuyReviveAllLifeTimesReponse(session, 100, remainTimes, purchase.getNumber(), purchase.getYuanbao(),needVipLevel);
	}
	
	private void sendBuyReviveAllLifeTimesReponse(IoSession session, int result, int remainTimes, 
			int getTimes, int cost, int needVipLevel) {
		BuyAllLifeReviveTimesResp.Builder response = BuyAllLifeReviveTimesResp.newBuilder();
		response.setResult(result);
		response.setRemainTimes(remainTimes);
		response.setGetTimes(getTimes);
		response.setCost(cost);
		response.setVipLevel(needVipLevel);
		session.write(response.build());
	}



}