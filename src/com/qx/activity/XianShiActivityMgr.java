package com.qx.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.MiBao;
import com.manu.dynasty.template.MibaoSuiPian;
import com.manu.dynasty.template.PveTemp;
import com.manu.dynasty.template.QiriQiandaoControl;
import com.manu.dynasty.template.XianshiControl;
import com.manu.dynasty.template.XianshiHuodong;
import com.manu.dynasty.template.ZhuangBei;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.award.AwardMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.bag.EquipGrid;
import com.qx.bag.EquipMgr;
import com.qx.chonglou.ChongLouMgr;
import com.qx.equip.domain.UserEquip;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.explore.ExploreMgr;
import com.qx.explore.ExploreMine;
import com.qx.explore.TanBaoData;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.junzhu.PlayerTime;
import com.qx.mibao.MiBaoDB;
import com.qx.mibao.MibaoMgr;
import com.qx.mibao.v2.MiBaoV2Mgr;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.pve.PveMgr;
import com.qx.pve.PveRecord;
import com.qx.task.GameTaskMgr;
import com.qx.task.TaskData;
import com.qx.timeworker.FunctionID;
import com.qx.vip.VipMgr;
import com.qx.world.Mission;

import qxmobile.protobuf.Activity.ActivityAchievementResp;
import qxmobile.protobuf.Activity.ActivityGetRewardResp;
import qxmobile.protobuf.Activity.ActivityGrowthFundRewardResp;
import qxmobile.protobuf.Activity.GrowLevel;
import qxmobile.protobuf.Explore.Award;
import qxmobile.protobuf.Explore.ExploreResp;
import qxmobile.protobuf.XianShi.FuLiHuoDong;
import qxmobile.protobuf.XianShi.FuLiHuoDongAwardReq;
import qxmobile.protobuf.XianShi.FuLiHuoDongAwardResp;
import qxmobile.protobuf.XianShi.FuLiHuoDongResp;
import qxmobile.protobuf.XianShi.GainAward;
import qxmobile.protobuf.XianShi.HongBaoResp;
import qxmobile.protobuf.XianShi.HuoDongInfo;
import qxmobile.protobuf.XianShi.OpenXianShi;
import qxmobile.protobuf.XianShi.OpenXianShiResp;
import qxmobile.protobuf.XianShi.ReturnAward;
import qxmobile.protobuf.XianShi.XinShouXSActivity;
import qxmobile.protobuf.XianShi.XinShouXianShiInfo;
/**
 * @Description 限时活动/成就管理 (其他限时活动已经改名叫成就了)
 *
 */
public class XianShiActivityMgr  extends EventProc{
	public Logger log = LoggerFactory.getLogger(XianShiActivityMgr.class.getSimpleName());
	public static XianShiActivityMgr instance;
	public static Map<Integer, XianshiHuodong> activityMap=  new HashMap<Integer, XianshiHuodong>(); ;
	public static Map<Integer, XianshiControl> xsControlMap =new HashMap<Integer, XianshiControl>();
	public static Map<Integer, QiriQiandaoControl> xs7DaysControlMap= new HashMap<Integer, QiriQiandaoControl>();
	public static Map<Integer, List<XianshiHuodong>> bigActivityMap = new HashMap<Integer, List<XianshiHuodong>>();
	public LinkedBlockingQueue<Mission> missions = new LinkedBlockingQueue<Mission>();
	public static final Redis DB = Redis.getInstance();
	public static final String XIANSHIYILING_KEY = "xianshiyiling_" + GameServer.serverId;//存储已领完奖励的限时活动小条目Id
	public static final String XIANSHIFINISH_KEY = "xianshifinish_" + GameServer.serverId;//存储已领完奖励的限时活动bigId
	public static final String XIANSHIKELING_KEY = "xianshikeling_" + GameServer.serverId;//存储可以完成未领奖的限时活动 小条目I
	// 2016年3月28日 没有超时判断了
//	public static final String XIANSHICHAOSHI_KEY = "xianshichaoshi_" + GameServer.serverId;//存储可以完成未领奖的限时活动
	public static final String XIANSHI7DAY_KEY = "xianshi7Day_" + GameServer.serverId;//记录登录总天数
	public static List<Integer> xshdCloseList=new ArrayList<Integer>();
	public static boolean isShow=false;
	public static boolean isOpen4YueKa=false;//封测月卡福利活动是否开启标记
	public static boolean isOpen4FengceHongBao=false;//封测红包活动是否开启标记
	public XianShiActivityMgr() {
		instance = this;
		initData();
	}

	@SuppressWarnings("unchecked")
	public void initData() {
		//加载限时活动小条目所有配置
		Map<Integer, XianshiHuodong> activityMap = new HashMap<Integer, XianshiHuodong>();
		Map<Integer, List<XianshiHuodong>> bigActivityMap = new HashMap<Integer, List<XianshiHuodong>>();
		// 加载活动列表
		List<XianshiHuodong> xianshiActivityList =TempletService.listAll(XianshiHuodong.class.getSimpleName());
		int tmpId=0;
		List<XianshiHuodong> xsList=new ArrayList<XianshiHuodong>();
		for (XianshiHuodong xsHuoDong : xianshiActivityList) {
			if(tmpId==xsHuoDong.BigId){
				xsList.add(xsHuoDong);
			}else{
				xsList=new ArrayList<XianshiHuodong>();
				xsList.add(xsHuoDong);
				tmpId=xsHuoDong.BigId;
			}

			bigActivityMap.put(xsHuoDong.BigId, xsList);
			activityMap.put(xsHuoDong.id, xsHuoDong);
		}
		XianShiActivityMgr.bigActivityMap=bigActivityMap;
		XianShiActivityMgr.activityMap=activityMap;
		
		//加载限时活动大配置
		Map<Integer, XianshiControl> xsControlMap=new HashMap<Integer, XianshiControl>();
		List<XianshiControl> xsControlList = TempletService.listAll(XianshiControl.class.getSimpleName());
		for (XianshiControl xs : xsControlList) {
			xsControlMap.put(xs.id,xs);
		}
		XianShiActivityMgr.xsControlMap=xsControlMap;
		
		//加载首日 七日限时活动配置
		Map<Integer, QiriQiandaoControl>	xs7DaysControlMap= new HashMap<Integer, QiriQiandaoControl>();
		List<QiriQiandaoControl> xsDaysControlList = TempletService.listAll(QiriQiandaoControl.class.getSimpleName());
		for (QiriQiandaoControl xs : xsDaysControlList) {
			xs7DaysControlMap.put(xs.id,xs);
		}
		XianShiActivityMgr.xs7DaysControlMap=xs7DaysControlMap;
		checkGlobalActivityState() ;
	}

	
	/**
	 * @Description 	初始化服务器时间为准的活动数据
	 */
	public  void initGlobalActivityInfo() {
		//封测红包活动
		GlobalActivityBean hongbaoInfo = HibernateUtil.find(GlobalActivityBean.class,FuliConstant.fengcehongbao);
		if(hongbaoInfo==null){
			Date startTime =new Date();
//			log.info("封测红包活动数据初始化--{}",startTime);
			hongbaoInfo = new GlobalActivityBean();
			hongbaoInfo.id=FuliConstant.fengcehongbao;
			hongbaoInfo.startTime=startTime;
			HibernateUtil.save(hongbaoInfo);
		}
		//封测月卡活动 
		GlobalActivityBean yuekaInfo = HibernateUtil.find(GlobalActivityBean.class,FuliConstant.yuekafuli);
		if(yuekaInfo==null){
			Date startTime =new Date();
//			log.info("封测月卡活动数据初始化--{}",startTime);
		    yuekaInfo = new GlobalActivityBean();
			yuekaInfo.id=FuliConstant.yuekafuli;
			yuekaInfo.startTime=startTime;
			HibernateUtil.save(yuekaInfo);
		}
	}
	
	/**
	 * @Description 重置服务器时间为准的活动状态
	 */
	public void checkGlobalActivityState() {
		
		//封测红包活动 
		GlobalActivityBean hongbaoInfo = HibernateUtil.find(GlobalActivityBean.class,FuliConstant.fengcehongbao);
		if(hongbaoInfo==null){
			Date startTime =new Date();
//			log.info("重置服务器时间为准的活动状态时，封测红包活动数据初始化--{}",startTime);
			hongbaoInfo = new GlobalActivityBean();
			hongbaoInfo.id=FuliConstant.fengcehongbao;
			hongbaoInfo.startTime=startTime;
			HibernateUtil.save(hongbaoInfo);
		}
		Date now =new Date();
		int hourDistance1=DateUtils.timeDistanceByHour(now, hongbaoInfo.startTime);
		if((hourDistance1/24)-30<0){
			isOpen4FengceHongBao=true;
		}else{
			isOpen4FengceHongBao=false;
		}
//		log.info("以服务器时间为准的---<封测红包>活动状态--{}",isOpen4FengceHongBao);
		//封测月卡活动
		GlobalActivityBean yuekaInfo = HibernateUtil.find(GlobalActivityBean.class,FuliConstant.yuekafuli);
		if(yuekaInfo==null){
			Date startTime =new Date();
//			log.info("重置服务器时间为准的活动状态时，封测月卡活动数据初始化--{}",startTime);
			yuekaInfo = new GlobalActivityBean();
			yuekaInfo.id=FuliConstant.yuekafuli;
			yuekaInfo.startTime=startTime;
			HibernateUtil.save(yuekaInfo);
		}
		int hourDistance2=DateUtils.timeDistanceByHour(now, yuekaInfo.startTime);
		if((hourDistance2/24)-30<0){
			isOpen4YueKa=true;
		}else {
			isOpen4YueKa=false;
		}
//		log.info("以服务器时间为准的---<封测月卡>活动状态--{}",isOpen4YueKa);
	}
	/**
	 * @Description jsp控制限时活动开关
	 * @param typeId
	 * @param state
	 */
	public void changeXianShiHuoDongState(int typeId,int state) {
		Integer huodongTypeId=Integer.parseInt(typeId+"");
		QiriQiandaoControl tmp = xs7DaysControlMap.get(huodongTypeId);
		if (tmp != null) {
			switch (state) {
			case XianShiConstont.XIANSHIOPEN:
				XianShiActivityMgr.xshdCloseList.remove(huodongTypeId);
				broadcast(huodongTypeId,state);
				break;
			case XianShiConstont.XIANSHIClOSE:
				XianShiActivityMgr.xshdCloseList.add(huodongTypeId);
				broadcast(huodongTypeId,state);
				break;
			default:
				break;
			}
		}else{
			XianshiControl xs = xsControlMap.get(huodongTypeId);
			if (xs != null) {
				switch (state) {
				case XianShiConstont.XIANSHIOPEN:
					XianShiActivityMgr.xshdCloseList.remove(huodongTypeId);
					broadcast(huodongTypeId,state);
					break;
				case XianShiConstont.XIANSHIClOSE:
					XianShiActivityMgr.xshdCloseList.add(huodongTypeId);
					broadcast(huodongTypeId,state);
					break;
				default:
					break;
				}
			}
		}
	}
	
	/**
	 * @Description jsp控制限时活动开关后广播活动状态
	 */
	public void broadcast(int huodongTypeId,int state) {
		OpenXianShiResp.Builder resp=OpenXianShiResp.newBuilder();
		OpenXianShi.Builder xianshi=OpenXianShi.newBuilder();
		switch (huodongTypeId) {
		case XianShiConstont.ZAIXIANLIBAO_TYPE:
			xianshi.setTypeId(XianShiConstont.ZAIXIANLIBAO_TYPE);
			xianshi.setName("在线礼包");
			xianshi.setShunxu(-1);
			xianshi.setState(state);
			xianshi.setIsNewAward(false);
			resp.addXianshi(xianshi);
//			log.info("广播活动{}状态变为{}",huodongTypeId,state);
			broadcastXianShiHuoDong(resp);
			break;
		case XianShiConstont.QIRIQIANDAO_TYPE:
			xianshi.setTypeId(XianShiConstont.QIRIQIANDAO_TYPE);
			xianshi.setName("七日签到");
			xianshi.setShunxu(-1);
			xianshi.setState(state);
			xianshi.setIsNewAward(false);
			resp.addXianshi(xianshi);
			broadcastXianShiHuoDong(resp);
//			log.info("广播活动{}状态变为{}",huodongTypeId,state);
			break;
		default:
			XianshiControl xs=xsControlMap.get(huodongTypeId);
			if(xs!=null){
				xianshi.setTypeId(xs.id);
				xianshi.setName(xs.Name);
				xianshi.setShunxu(xs.Rank);
				xianshi.setState(state);
				xianshi.setIsNewAward(false);
				resp.addXianshi(xianshi);
//				log.info("广播活动{}状态变为{}",huodongTypeId,state);
				broadcastXianShiHuoDong(resp);
			}else{
				log.error("错误活动类型--{}，广播活动失败",huodongTypeId);
			}
			break;
		}
	}
	/**
	 * @Description jsp控制限时活动开关后广播活动状态
	 */
	public void broadcastXianShiHuoDong(OpenXianShiResp.Builder resp) {
		List<SessionUser> list = SessionManager.inst.getAllSessions();
		for (SessionUser su: list){
			IoSession session = su.session;
			if(session != null){
				session.write(resp.build());
			}
		}
	}
	/**
	 * @Description   获取可开启活动 （ 包括首日、7日和成就）
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getOpenXianShiHuoDong(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
//			log.error("请求限时活动出错：君主不存在");
			return;
		}
		long jzId=jz.id;
		OpenXianShiResp.Builder resp=OpenXianShiResp.newBuilder();
		/**************************************************************************************/
		//2016年3月28日 已经废弃判断 
//		List<XianShiBean> xianShiList=HibernateUtil.list(XianShiBean.class, "where junZhuId="+jzId);
		//判断当前君主哪些活动超过限时，自动关闭
//		if(xianShiList!=null){
//			for (XianShiBean xsBean : xianShiList) {
//				if((xsBean.finishDate==null)&&(xsBean.bigId!=XianShiConstont.ZAIXIANLIBAO_TYPE)
//						&&(xsBean.bigId!=XianShiConstont.QIRIQIANDAO_TYPE)){
//					checkisFinished(xsBean);
//				}
//			}
//		}
		
		/**************************************************************************************/
		//“七日签到”奖励是否领取完毕标记
		boolean isFinish47Day=DB.lexist((XIANSHIFINISH_KEY + jzId), XianShiConstont.QIRIQIANDAO_TYPE + "");
		//处理首日和七日签到活动
		OpenXianShi.Builder xianshi=OpenXianShi.newBuilder();
		if(isFinish47Day){
			log.info("{}七日签到奖励已领完",jzId);
		}else{
			if(!xshdCloseList.contains(XianShiConstont.QIRIQIANDAO_TYPE)){
				xianshi.setTypeId(XianShiConstont.QIRIQIANDAO_TYPE);
				xianshi.setName("七日签到");
				xianshi.setShunxu(-1);
				xianshi.setState(10);
				boolean isNewAward=false;
				isNewAward=	get7DaysisNewAward(jzId);
				xianshi.setIsNewAward(isNewAward);
				resp.addXianshi(xianshi);
			}
		}
		//首日奖励是否领取完毕标记
		boolean isFinish4ShouRi=DB.lexist((XIANSHIFINISH_KEY + jzId), XianShiConstont.ZAIXIANLIBAO_TYPE + "");
		if(isFinish4ShouRi){
			log.info("{}在线礼包奖励已领完",jzId);
		}else{
			if(!xshdCloseList.contains(XianShiConstont.ZAIXIANLIBAO_TYPE)){
				xianshi.setTypeId(XianShiConstont.ZAIXIANLIBAO_TYPE);
				xianshi.setName("在线礼包");
				int shengyuTime=getShouRiAwardShengTime(jzId);
				boolean isNewAward=true;
				if(shengyuTime>0){
					isNewAward=false;
				}
				xianshi.setShunxu(shengyuTime);
				xianshi.setState(10);
				xianshi.setIsNewAward(isNewAward);
				resp.addXianshi(xianshi);
			}
		}
//		//处理其他限时活动
//		for (XianshiControl xs : xsControlMap.values()) {
//			// 是否完成标记
//			boolean isFinish = DB.lexist((XIANSHIFINISH_KEY + jzId), xs.id+ "");
//			if (isFinish) {
//				log.info("{}的限时活动{}-编码{}已完成",jzId,xs.getName(),xs.id);
//			} else {
//				// 活动未完成且活动开启
//				if (!xshdCloseList.contains(xs.id)) {
//					if(xs.doneType == 1){
//						continue; //冲级送礼不算成就
//					}
//					//2015年9月7日 改为Event事件触发刷新限时活动数据
//					OpenXianShi.Builder xshuodong = OpenXianShi.newBuilder();
//					xshuodong.setTypeId(xs.id);
//					xshuodong.setName(xs.getName());
//					xshuodong.setShunxu(xs.getRank());
//					xshuodong.setState(10);
//					boolean isNewAward = (isNewAward(xs.id, jzId) == 1) ? true: false;
//					xshuodong.setIsNewAward(isNewAward);
//					resp.addXianshi(xshuodong);
//				}
//			}
//		}
		session.write(resp.build());
	}
	
	
	/**
	 * @Description //获取福利信息
	 */
	public void getFuLiInfo(int id, Builder builder,IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
//			log.error("请求福利状态信息出错：君主不存在");
			return;
		}
		long jzId=jz.id;
		Date now =new Date();
		FuLiHuoDongResp.Builder resp=FuLiHuoDongResp.newBuilder();
		FuliInfo info=HibernateUtil.find(FuliInfo.class, jzId);
		if(info==null){
//			log.info("初始化君主--{}的福利info",jzId);
			info=new FuliInfo();
			info.jzId=jzId;
			Cache.fuliInfoCache.put(jzId, info);
			HibernateUtil.save(info);
		}
		if(isOpen4FengceHongBao){
			FuLiHuoDong.Builder fengce=FuLiHuoDong.newBuilder();
			fengce.setTypeId(FuliConstant.fengcehongbao);
		
			getFengCeHongBaoInfo(info, now,fengce);
			
			resp.addXianshi(fengce);
		}
		boolean isCanGetYueKa=	VipMgr.INSTANCE.hasYueKaAward(jzId);
		if(isCanGetYueKa&&isOpen4YueKa){
			FuLiHuoDong.Builder yueka=FuLiHuoDong.newBuilder();
			yueka.setTypeId(FuliConstant.yuekafuli);
			boolean isGet2=check4YuKaFuLi(info, now);
//			log.info("君主--{}月卡福利可领取状态--{}",jzId,isGet2);
			yueka.setIsCanGet(isGet2);
			if(isGet2){
				yueka.setContent(FuliConstant.yuekafuliAward);
			}else{
				int hour = DateUtils.getHourOfDay(now);
				int year=now.getYear();
				int month=now.getMonth();
				int hrs=4;
				int min=0;
				if(hour>=4){
					int date =now.getDate()+1;
					Date nextDay=new Date(year, month, date, hrs, min);
					int	timeDistance = DateUtils.timeDistanceBySecond(nextDay, now)/1000;
					yueka.setRemainTime(timeDistance);
					yueka.setContent("明天4:00");
				}else {
					int date =now.getDate();
					Date nextDay=new Date(year, month, date, hrs, min);
					int	timeDistance = DateUtils.timeDistanceBySecond(nextDay, now)/1000;
					yueka.setRemainTime(timeDistance);
					yueka.setContent("今天4:00");
				};
			}
			resp.addXianshi(yueka);
		}
		if(!xshdCloseList.contains(FuliConstant.tilifuli)){
			FuLiHuoDong.Builder tili=FuLiHuoDong.newBuilder();
			tili.setTypeId(FuliConstant.tilifuli);
			getTiliFuLiInfo(info, now,tili);
//			log.info("君主--{}体力福利可领取状态--{}",jzId,tili.getContent());
			resp.addXianshi(tili);
		}
		session.write(resp.build());
	}
	
	/**
	 * @Description 为封测红包拆出来的协议   获取封测（感恩）红包信息
	 */
	public void getGanEnHongBao(int id, Builder builder,IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
//			log.error("请求封测红包信息出错：君主不存在");
			return;
		}
		long jzId=jz.id;
		FuliInfo info=HibernateUtil.find(FuliInfo.class, jzId);
		if(info==null){
//			log.info("初始化君主--{}的福利info",jzId);
			info=new FuliInfo();
			info.jzId=jzId;
			Cache.fuliInfoCache.put(jzId, info);
			HibernateUtil.save(info);
		}
//		log.info("君主--{}请求获取红包信息",jzId);
		HongBaoResp.Builder resp=HongBaoResp.newBuilder();
		if(isOpen4FengceHongBao){
			getHongBaoInfo(info, new Date(),resp);
		}else{
			log.error("{}请求封测红包信息出错：，活动未开启",jzId);
			resp.setAwardTime("0");
			resp.setDay(-1);
			resp.setRemainTime(Integer.MAX_VALUE);
			resp.setYuanbao(-1);
		}
		session.write(resp.build());
	}
	/**
	 * @Description 获取封测红包福利信息 
	 * 部分参数写死 比如17:30分之类的 如果要变更需求 该配置需一起修改
	 */
	@SuppressWarnings("deprecation")
	public void getHongBaoInfo(FuliInfo info, Date now,HongBaoResp.Builder hongbao) {
		long jzId=info.jzId;
		int fengceHongBaoCode=getNowHongBaoFuLiCode();
		int isGetCode=check4FengCeHongBao(info, now,fengceHongBaoCode);
//		log.info("君主--{}封测红包福利可领取状态--{}",jzId,isGetCode);
//		isGetCode1 ：
//		 * 0：将要领明天9：00后 奖励;
//		 * 10：将要领今天9:00 奖励;
//		 * 1： 可领9:00 奖励 ; 
//		 * 20：将要领今天17:30 奖励;
//		 * 2：可领17:30 奖励;  
		Date nextDay=null;
		int timeDistance=0;
		switch (isGetCode) {
		case 1:
			hongbao.setRemainTime(-1);
			hongbao.setYuanbao(Integer.valueOf(FuliConstant.fengcehongbaoAward1) );
			break;
		case 2:
			hongbao.setRemainTime(-1);
			hongbao.setYuanbao(Integer.valueOf(FuliConstant.fengcehongbaoAward2));
			break;
		case 10:
			int year1=now.getYear();
			int month1=now.getMonth();
			int date1 =now.getDate();
			int hrs1=9;
			int min1=0;
			nextDay=new Date(year1, month1, date1, hrs1, min1);
			timeDistance = DateUtils.timeDistanceBySecond(nextDay, now)/1000;
			hongbao.setRemainTime(timeDistance);
			hongbao.setYuanbao(Integer.valueOf(FuliConstant.fengcehongbaoAward1));
			break;
		case 20:
			int year=now.getYear();
			int month=now.getMonth();
			int date =now.getDate();
			int hrs=17;
			int min=30;
			nextDay=new Date(year, month, date, hrs, min);
			timeDistance = DateUtils.timeDistanceBySecond(nextDay, now)/1000;
			hongbao.setRemainTime(timeDistance);
			hongbao.setYuanbao(Integer.valueOf(FuliConstant.fengcehongbaoAward2));
			break;
		case 0:
			//算出明天9点到现在的秒差
			timeDistance=DateUtils.timeDistanceBySecond()/1000;
			//加5小时
			timeDistance+=5*3600;
			hongbao.setRemainTime(timeDistance);
			hongbao.setYuanbao(Integer.valueOf(FuliConstant.fengcehongbaoAward1));
			break;
		default:
			break;
		}
		//TODO 下面俩参数写死了 根据策划坑的程度修改
		hongbao.setAwardTime("每天9:00、17:30");
		hongbao.setDay(30);
	}
	
	/**
	 * @Description //领取封测福利奖励
	 */
	public void gainFuLiAward(int id, Builder builder,IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
//			log.error("请求领取福利出错：君主不存在");
			return;
		}
		long jzId=jz.id;
		FuLiHuoDongAwardReq.Builder req=(FuLiHuoDongAwardReq.Builder)builder;
		int type=req.getFuLiType();
		FuliInfo info=HibernateUtil.find(FuliInfo.class, jzId);
		if(info==null){
//			log.info("初始化君主--{}的福利info,此处未保存",jzId);
			info=new FuliInfo();
			info.jzId=jzId;
			Cache.fuliInfoCache.put(jzId, info);
		}
		FuLiHuoDongAwardResp.Builder resp=FuLiHuoDongAwardResp.newBuilder();
		resp.setFuLiType(type);
		String result=null;
		switch (type) {
		case FuliConstant.fengcehongbao:
			result=gainFengCeHongBao(jz, info, session);
			resp.setResult(result);
			break;
		case FuliConstant.yuekafuli:
			result=gainYueKaFuLi(jz, info, session);
			resp.setResult(result);
			break;
		case FuliConstant.tilifuli:
			result=gainTiLiFuLi(jz, info, session);
			resp.setResult(result);
			break;
		default:
			log.error("{}--请求领取福利出错：type--{}不存在",jzId,type);
			break;
		}
		session.write(resp.build());
		//给前端重新发福利信息
		getFuLiInfo(id, builder, session);
	}
	
	
	/**
	 * @Description //领取封测红包福利
	 */
	public String gainFengCeHongBao(JunZhu jz,FuliInfo info,IoSession session) {
		long jzId=jz.id;
		if(!isOpen4FengceHongBao){
//			log.info("君主--{}领取封测红包福利失败,活动关闭",jzId);
			return "0";
		}
		Date now =new Date();
		log.info("君主--{}领取封测红包福利",jzId);
		int fengceHongBaoCode=getNowHongBaoFuLiCode();
		int isGetCode=check4FengCeHongBao(info, now,fengceHongBaoCode);
		String award="0";
//		boolean isGet1=check4FengCeHongBao(info, now);
		switch (isGetCode) {
		case 1:
			award="0:900002:"+FuliConstant.fengcehongbaoAward1;
			log.info("君主--{}领取封测红包福利1--{}",jzId,award);
			if(award!=null&&!"".equals(award)&&award.contains(":")){
				AwardMgr.inst.giveReward(session, award, jz);
			}
			info.getFengCeHongBaoTime1=now;
			HibernateUtil.save(info);
			break;
		case 2:
			award="0:900002:"+FuliConstant.fengcehongbaoAward2;
			log.info("君主--{}领取封测红包福利2--{}",jzId,award);
			if(award!=null&&!"".equals(award)&&award.contains(":")){
				AwardMgr.inst.giveReward(session, award, jz);
			}
			info.getFengCeHongBaoTime2=now;
			HibernateUtil.save(info);
			break;
		default:
			log.error("君主--{}不可以领取封测红包福利",jzId);
			break;
		}
		return award;
	}

	/**
	 * @Description 	//领取月卡福利
	 */
	public String gainYueKaFuLi(JunZhu jz,FuliInfo info,IoSession session) {
		long jzId=jz.id;
		if(!isOpen4YueKa){
//			log.info("君主--{}领取月卡福利失败,活动关闭",jzId);
			return "0";
		}
		Date now =new Date();
		log.info("君主--{}领取月卡福利",jzId);
		boolean isCanGetYueKa=	VipMgr.INSTANCE.hasYueKaAward(jzId);
		if(!isCanGetYueKa){
			log.info("君主--{}领取月卡福利失败,isCanGetYueKa为false",jzId);
			return "0";
		}
		boolean isGet1=check4YuKaFuLi(info, now);
		if(isGet1){
			//TODO 奖励
			String award="0:900002:120";
			log.info("君主--{}领取月卡福利--{}",jzId,award);
			if(award!=null&&!"".equals(award)&&award.contains(":")){
				AwardMgr.inst.giveReward(session, award, jz);
			}
			info.getYuKaFuLiTime=now;
			HibernateUtil.save(info);
			return award;
		}else{
			log.info("君主--{}不可以领取月卡福利",jzId);
			return "0";
		}
	}
	
	
	/**
	 * @Description //领取体力福利
	 */
	public String gainTiLiFuLi(JunZhu jz,FuliInfo info,IoSession session) {
		long jzId=jz.id;
		log.info("君主--{}领取体力福利",jzId);
		if(xshdCloseList.contains(FuliConstant.tilifuli)){
			log.info("封测体力活动强制关闭,{}--领取失败",jzId);
			return "0";
		}
		Date now =new Date();
		int tiliCode=getNowTiliCode();
		String award=null;
		switch (tiliCode) {
		case 1:
			if(getDistance4Tili1(info)){
				//可领奖励1
				award="0:900003:50";
				info.getTiLiTime1=now;
			}
			break;
		case 2:
			if(getDistance4Tili2(info)){
				//可领奖励2
				award="0:900003:50";
				info.getTiLiTime2=now;
			}
			break;
		case 3:
			if(getDistance4Tili3(info)){
				//可领奖励3
				award="0:900003:50";
				info.getTiLiTime3=now;
			}
			break;
		case 4:
			//不可领奖励3

			break;
		}
		//TODO 奖励
		if(award!=null&&!"".equals(award)&&award.contains(":")){
			log.info("君主--{}领取体力福利--{}",jzId,award);
			AwardMgr.inst.giveReward(session, award, jz);
			HibernateUtil.save(info);
			return award;
		}else{
			log.info("君主--{}领取体力福利失败",jzId);
			return "0";
		}
	}

	
	/**
	 * @Description 	// 现在应该领哪个体力奖励
	 */
	public int getNowTiliCode() {
		int hour = DateUtils.getHourOfDay(new Date());
		//算出现在是哪个时段
		int tiliCode=(hour >= 0 && hour < FuliConstant.show_tili_clock_12)?0:
					(hour >= FuliConstant.show_tili_clock_12 && hour < FuliConstant.show_tili_clock_14)?1:
					(hour >= FuliConstant.show_tili_clock_14 && hour < FuliConstant.show_tili_clock_18)?11:
					(hour >= FuliConstant.show_tili_clock_18 && hour < FuliConstant.show_tili_clock_20)?2:
					(hour >= FuliConstant.show_tili_clock_20 && hour < FuliConstant.show_tili_clock_21)?21:
					(hour >= FuliConstant.show_tili_clock_21 && hour < FuliConstant.show_tili_clock_24)?3:4;
		return tiliCode;
	}

	@SuppressWarnings("deprecation")
	public void getTiliFuLiInfo(FuliInfo info, Date now,FuLiHuoDong.Builder tili) {
		int tiliCode=getNowTiliCode();
		int year=now.getYear();
		int month=now.getMonth();
		int date =now.getDate();
		int hrs=12;
		int min=0;
		Date nextDay;
		int	timeDistance;
		boolean isCan=false;
		switch (tiliCode) {
		case 0:
			//不可领奖励1
			tili.setContent("今天12:00");
			break;
		case 1:
			isCan=getDistance4Tili1(info);
			if(isCan){
				//可领奖励1
				tili.setContent(FuliConstant.tilifuliAward1);
			}else{
				hrs=FuliConstant.show_tili_clock_18;
				//不可领奖励1
				tili.setContent("今天"+FuliConstant.show_tili_clock_18+":00");
			}
			break;
		case 11:
			hrs=FuliConstant.show_tili_clock_18;
				//不可领奖励1
			tili.setContent("今天"+FuliConstant.show_tili_clock_18+":00");
			break;
		case 2:
			isCan=getDistance4Tili2(info);
			if(isCan){
				//可领奖励1
				tili.setContent(FuliConstant.tilifuliAward2);
			}else{
				hrs=FuliConstant.show_tili_clock_21;
				//不可领奖励1
				tili.setContent("今天"+FuliConstant.show_tili_clock_21+":00");
			}
			break;
		case 21:
			hrs=FuliConstant.show_tili_clock_21;
			//不可领奖励1
			tili.setContent("今天"+FuliConstant.show_tili_clock_21+":00");
			break;
		case 3:
			isCan=getDistance4Tili3(info);
			if(isCan){
				//可领奖励1
				tili.setContent(FuliConstant.tilifuliAward3);
			}else{
				date+=1;
				hrs=FuliConstant.show_tili_clock_12;
				//不可领奖励1
				tili.setContent("明天"+FuliConstant.show_tili_clock_12+":00");
			}
			break;
		case 4:
			date+=1;
			hrs=FuliConstant.show_tili_clock_12;
			//不可领奖励3
			tili.setContent("明天"+FuliConstant.show_tili_clock_12+":00");
			break;
		}
		nextDay=new Date(year, month, date, hrs, min);
		timeDistance = DateUtils.timeDistanceBySecond(nextDay, now)/1000;
		tili.setRemainTime(timeDistance);
		tili.setIsCanGet(isCan);
	}
	
	/**
	 * @Description 获取封测红包福利信息 
	 * 部分参数写死 比如17:30分之类的 如果要变更需求 该配置需一起修改
	 */
	@SuppressWarnings("deprecation")
	public void getFengCeHongBaoInfo(FuliInfo info, Date now,FuLiHuoDong.Builder fengce) {
		long jzId=info.jzId;
		int fengceHongBaoCode=getNowHongBaoFuLiCode();
		int isGetCode=check4FengCeHongBao(info, now,fengceHongBaoCode);
//		log.info("君主--{}封测红包福利可领取状态--{}",jzId,isGetCode);
//		isGetCode1 ：
//		 * 0：将要领明天9：00后 奖励;
//		 * 10：将要领今天9:00 奖励;
//		 * 1： 可领9:00 奖励 ; 
//		 * 20：将要领今天17:30 奖励;
//		 * 2：可领17:30 奖励;  
		Date nextDay=null;
		int timeDistance=0;
		switch (isGetCode) {
		case 1:
			fengce.setContent(FuliConstant.fengcehongbaoAward1);
			break;
		case 2:
			fengce.setContent(FuliConstant.fengcehongbaoAward2);
			break;
		case 10:
			int year1=now.getYear();
			int month1=now.getMonth();
			int date1 =now.getDate()+1;
			int hrs1=9;
			int min1=0;
			nextDay=new Date(year1, month1, date1, hrs1, min1);
			timeDistance = DateUtils.timeDistanceBySecond(nextDay, now)/1000;
			fengce.setRemainTime(timeDistance);
			fengce.setContent("今天9:00");
			break;
		case 20:
			int year=now.getYear();
			int month=now.getMonth();
			int date =now.getDate()+1;
			int hrs=17;
			int min=30;
			nextDay=new Date(year, month, date, hrs, min);
			timeDistance = DateUtils.timeDistanceBySecond(nextDay, now)/1000;
			fengce.setRemainTime(timeDistance);
			fengce.setContent("今天17:30");
			break;
		case 0:
			//算出明天9点到现在的秒差
			timeDistance=DateUtils.timeDistanceBySecond()/1000;
			//加5小时
			timeDistance+=5*3600;
			fengce.setRemainTime(timeDistance);
			fengce.setContent("明天9:00");
			break;
		default:
			break;
		}
		fengce.setIsCanGet(isGetCode==1||isGetCode==2);
	}
	
	/** 
	 * @Description 现在应该领哪个红包奖励
	 * @return 1表示 可领取9：00之前的奖励
	 *         2表示  可领取17：30之后的奖励  
	 *         3表示 可领取9：00-17：30之间的奖励 
	 */
	public int getNowHongBaoFuLiCode() {
		int distance1=DateUtils.timeDistanceTodayOclock(9, 0);
		if(distance1>0){
			return 1;
		}
		int distance2=DateUtils.timeDistanceTodayOclock(17, 30);
		if(distance2>0){
			return 3;
		}
		return 2;
	}
	
	/**
	 * @Description 获取封测红包领奖状态
	 * @param info
	 * @param now
	 * @param  fengceHongBaoCode
	 *   	   1表示 可领取9：00之前的奖励
	 *         2表示  可领取17：30之后的奖励  
	 *         3表示 可领取9：00-17：30之后的奖励 
	 * @return 
	 * 0：将要领明天9：00后 奖励;
	 * 10：将要领今天9:00 奖励;
	 * 1： 可领9:00 奖励 ; 
	 * 20：将要领今天17:30 奖励;
	 * 2：可领17:30 奖励;  
	 */
	public int check4FengCeHongBao(FuliInfo info,Date now,int fengceHongBaoCode) {
		int result=0;
		boolean distance2_9=false;
		switch (fengceHongBaoCode) {
		case 3:
			if(info.getFengCeHongBaoTime1==null)
			{
				result=  1;
				break;
			}
			boolean hasGet=DateUtils.isTimeToReset(info.getFengCeHongBaoTime1, 9);
			//false 说明9:00-17:30已经领奖  ； true  可领今天9:00-17:30奖励
			if(!hasGet){
				result= 20;
			}else{
				result= 1;
			}
			break;
		case 1:
			if(info.getFengCeHongBaoTime2==null)
			{
				result= 2;
				break;
			}
			distance2_9=DateUtils.isTimeToReset(info.getFengCeHongBaoTime2, 9);
			if(distance2_9){
				result=  2;
			}else{
				result=  10;
			}
			break;
	
		case 2:
			if(info.getFengCeHongBaoTime2==null)
			{
				result= 2;
				break;
			}
			int distance2_17=DateUtils.timeDistanceTodayByclock(info.getFengCeHongBaoTime2, 17, 30);
			//0 说明已经领过
			if(distance2_17==0){
				result=  0;
			}else{
				result=  2;
			}
			break;
		default:
			log.error("君主--{}，check4FengCeHongBao,ERROR_CODE--{}",info.jzId,fengceHongBaoCode);
			break;
		}
		return result;
	}
	
	
	/** 
	 * @Description  今天领了体力福利1没有
	 */
	public boolean getDistance4Tili1(FuliInfo info) {
		boolean result=true;
		if(info!=null&&info.getTiLiTime1!=null){
			result=DateUtils.isTimeToReset(info.getTiLiTime1, CanShu.REFRESHTIME_PURCHASE);
		}
		return result;
	}

	
	/**
	 * @Description  今天领了体力福利2没有
	 */
	public boolean getDistance4Tili2(FuliInfo info) {
		boolean result=true;
		if(info!=null&&info.getTiLiTime2!=null){
			result=DateUtils.isTimeToReset(info.getTiLiTime2, CanShu.REFRESHTIME_PURCHASE);
		}
		return result;
	}
	
	/**
	 * @Description 今天领了体力福利3没有
	 */
	public boolean getDistance4Tili3(FuliInfo info) {
		boolean result=true;
		if(info!=null&&info.getTiLiTime3!=null){
			result=DateUtils.isTimeToReset(info.getTiLiTime3, CanShu.REFRESHTIME_PURCHASE);
		}
		return result;
	}
	
	
	/**
	 * @Description  今天领了月卡福利没有
	 */
	public boolean check4YuKaFuLi(FuliInfo info,Date now) {
		boolean result=true;
		
		if(info!=null&&info.getYuKaFuLiTime!=null){
			result=DateUtils.isTimeToReset(info.getYuKaFuLiTime, CanShu.REFRESHTIME_PURCHASE);
		}
		return result;
	}
	/**
	 * @Description   根据小活动id判断大活动是否关闭
	 * @param huodongId
	 * @return
	 */
	public boolean isClosedById(int huodongId){
		XianshiHuodong xs=activityMap.get(huodongId);
		if(xs==null) return true;
		return xshdCloseList.contains(xs.BigId);
	}

	/**
	 * @Description   请求新手首日/七日签到活动信息
	 */
	public void getXinShouXianShiInfo(int id, Builder builder,IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求新手首日/七日签到活动出错：君主不存在");
			return;
		}
		XinShouXSActivity.Builder req=(XinShouXSActivity.Builder)builder;
		if(req==null){
			log.error("{}请求领取新手首日/七日活动出错：请求参数错误",jz.id);
			return;
		}
		int typeId=req.getTypeId();
		switch (typeId) {
		case XianShiConstont.ZAIXIANLIBAO_TYPE:
			getShouRiInfo(jz, builder, session);
			break;
		case XianShiConstont.QIRIQIANDAO_TYPE:
			get7DaysInfo(jz, builder, session);
			break;
		default:
			log.error("请求新手首日/七日签到活动出错：活动类型编码{}错误",typeId);;
			break;
		}
	}

	/**
	 * @Description   获取首日在线活动数据
	 */
	public void getShouRiInfo(JunZhu jz, Builder builder,IoSession session) {
		long jzId=jz.id;
		XianShiBean xsBean=HibernateUtil.find(XianShiBean.class, XianShiConstont.ZAIXIANLIBAO_TYPE+jzId*100);
		XinShouXianShiInfo.Builder resp=XinShouXianShiInfo.newBuilder();
		if(xsBean==null){
			xsBean=initXianShiInfo(jzId,XianShiConstont.ZAIXIANLIBAO_TYPE);
		}
		int bigId=xsBean.bigId;
//		log.info("获取{}首日限时活动-{}数据", jzId,bigId);
		List<XianshiHuodong> xsList=bigActivityMap.get(bigId);
		if(xsList==null){
			log.error("玩家{}获取首日活动{}数据出错，XianshiHuodong-List为空",jzId,bigId);
			return;
		}
		int hdSize=xsList.size();
		Date startDate=xsBean.startDate;
		int useDTime=(int) ((System.currentTimeMillis()-startDate.getTime())/1000);
		//每一小条活动信息
		//HuoDongInfo -State 奖励状态10：可领取 20：已领取 30：（超时未完成）不能领取 40:（未达到条件）不可领取
		int shengyu=-1;
		int code=0;
		for (int i = 0; i < hdSize; i++) {
			HuoDongInfo.Builder hd=HuoDongInfo.newBuilder();
			XianshiHuodong xshd=xsList.get(i);
			//无限时 返回剩余时间-1
			int huoDongId=xshd.id;
			if(code==10){
				//剩下首日奖励不进行判断处理
				if(isShow){
					log.info("当前君主{}首日限时活动-{}数据-可领取状态:领取（未达到条件）不可领取 code-{}", jzId, huoDongId,code);
				}
				hd.setShengTime(Integer.valueOf(xshd.doneCondition));
				hd.setState(40);
				hd.setJiangli(xshd.Award);
				hd.setHuodongId(huoDongId);
				resp.addHuodong(hd);
				continue;
			}
			boolean isYiling = DB.lexist((XIANSHIYILING_KEY + jzId), huoDongId+ "");
			if(isYiling){
				//达成领奖条件，进入下一条循环
				if(isShow){
					log.info("当前君主{}首日限时活动-{}数据-可领取状态已领取 isYiling-{}", jzId, huoDongId,isYiling);
				}
				code = 20;
				hd.setShengTime(Integer.valueOf(xshd.doneCondition));
				hd.setState(code);
				hd.setJiangli(xshd.Award);
				hd.setHuodongId(huoDongId);
				resp.addHuodong(hd);
				continue;
			}
			int doneCondition=Integer.parseInt(xshd.doneCondition);
			boolean isKeling = DB.lexist((XIANSHIKELING_KEY + jzId), huoDongId + "");
			if(isKeling){
				//达成领奖条件，进入下一条循环
				if(isShow){
					log.info("当前君主{}首日限时活动-{}数据-可领取状态：可领取isKeling-{}", jzId, huoDongId,isKeling);
				}
				code = 10;
				hd.setShengTime(Integer.valueOf(xshd.doneCondition));
				hd.setState(code);
				hd.setJiangli(xshd.Award);
				hd.setHuodongId(huoDongId);
				resp.addHuodong(hd);
				continue;
			}

			int condition=getShengyuTime4ShouRi(jzId,huoDongId,doneCondition,useDTime);
			// 达到完成条件
			if (condition<=0) {
				//达成领奖条件，记录到未可领
				if(isShow){
					log.info("当前君主{}首日限时活动-{}数据-可领取状态：可领取√", jzId, huoDongId);
				}
				code = 10;
			}else{
				if(isShow){
					log.info("当前君主{}首日限时活动-{}数据-可领取状态:（未达到条件）不可领取√", jzId, huoDongId);
				}
				if(shengyu<0){
					shengyu=condition;
				}
				code = 40;
			}
			hd.setShengTime(Integer.valueOf(xshd.doneCondition));
			hd.setState(code);
			hd.setJiangli(xshd.Award);
			hd.setHuodongId(huoDongId);
			resp.addHuodong(hd);
		}

		log.info("当前君主{}首日限时活动，下一条剩余领奖时间为{}", jzId,shengyu);
		//首日七日活动没有备注返回-1
		resp.setBeizhu(-1);
		//返回整个活动剩余时间 首日和七日无限时返回-1
		resp.setRemainTime(shengyu);
		resp.setTypeId(bigId);
		session.write(resp.build());
	}
	
	
	/**
	 * @Description //遍历得到首日在线活动的倒计时起点
	 */
	public int getShouRiAwardShengTime(long jzId) {
		List<XianshiHuodong> xsList=bigActivityMap.get(XianShiConstont.ZAIXIANLIBAO_TYPE);
		if(xsList==null){
			return Integer.MAX_VALUE;
		}
		XianShiBean xsBean=HibernateUtil.find(XianShiBean.class, XianShiConstont.ZAIXIANLIBAO_TYPE+jzId*100);
		if(xsBean==null){
			xsBean=initXianShiInfo(jzId,XianShiConstont.ZAIXIANLIBAO_TYPE);
		}
		Date startDate=xsBean.startDate;
		int hdSize=xsList.size();
		int useDTime=(int) ((System.currentTimeMillis()-startDate.getTime())/1000);
		//每一小条活动信息
		//HuoDongInfo -State 奖励状态10：可领取 20：已领取 30：（超时未完成）不能领取 40:（未达到条件）不可领取
		int shengyu=-1;
		List<String> isYiling = DB.lgetList(XIANSHIYILING_KEY + jzId);
		List<String>  isKeling = DB.lgetList(XIANSHIKELING_KEY + jzId);
		for (int i = 0; i < hdSize; i++) {
			XianshiHuodong xshd=xsList.get(i);
			//无限时 返回剩余时间-1
			int huoDongId=xshd.id;
			if(isYiling.contains(huoDongId + "")){
				//达成领奖条件，进入下一条循环
				continue;
			}
			int doneCondition=Integer.parseInt(xshd.doneCondition);
			
			if(isKeling.contains(huoDongId + "")){
				//达成领奖条件，进入下一条循环
				return -1;
			}
			int condition=getShengyuTime4ShouRi(jzId,huoDongId,doneCondition,useDTime);
			// 达到完成条件
			if (condition<=0) {
				//达成领奖条件，记录到未可领
				return -1;
			}else{
				shengyu=condition;
				return shengyu;
			}
		}
		return shengyu;
	}
	/**
	 * @Description 7日活动是否有新奖励
	 */
	public boolean get7DaysisNewAward(long jzId) {
		List<XianshiHuodong> xsList=bigActivityMap.get(XianShiConstont.QIRIQIANDAO_TYPE);
		if(xsList==null){
			return false;
		}
		XianShiBean xsBean=HibernateUtil.find(XianShiBean.class, XianShiConstont.QIRIQIANDAO_TYPE+jzId*100);
		if(xsBean==null){
			xsBean=initXianShiInfo(jzId,XianShiConstont.QIRIQIANDAO_TYPE);
		}
		int hdSize=xsList.size();
		List<String> isYiling = DB.lgetList(XIANSHIYILING_KEY + jzId);
		List<String> isKeling = DB.lgetList(XIANSHIKELING_KEY + jzId);
		//每一小条活动信息
		for (int i = 0; i < hdSize; i++) {
			XianshiHuodong xshd=xsList.get(i);
			int huoDongId=xshd.id;
			if(isYiling.contains(huoDongId+ "")){
				//达成领奖条件，进入下一条循环
				continue;
			}
			if(isKeling.contains(huoDongId+ "")){
				//达成领奖条件，进入下一条循环
				return true;
			}
			// 达到完成条件
			if (isCompleted47Days(jzId, xshd)) {
				//达成领奖条件
				return true;
			}
		}
		return false;
	}
	/**
	 * @Description   初始化活动数据
	 * @param jzId
	 * @param bigId
	 * @return
	 */
	public XianShiBean initXianShiInfo(Long jzId,int bigId) {
		// 获取数据库中是否有此记录，有的话什么也不做
		log.info("初始化{}限时活动（类型-{}）数据", jzId,bigId);
		PlayerTime playerTime = HibernateUtil.find(PlayerTime.class, jzId);
		Date startDate=null;
		if(playerTime!=null&&playerTime.zhunchengTime!=null){
			startDate=playerTime.zhunchengTime;
		}else{
			log.info("初始化{}限时活动-{}数据出错，playerTime|ZhunchengTime为空",jzId,bigId);
			startDate=new Date();
		}
		XianShiBean bean = new XianShiBean();
		bean.bigId=bigId;
		bean.huoDongId=bigId+1;
		bean.junZhuId = jzId;
		bean.startDate=startDate;
		bean.id=jzId*100+bigId;
		MC.add(bean, jzId);
		HibernateUtil.insert(bean);
		log.info("玩家id是 ：{}的 限时活动（类型-{}）生成成功", jzId,bigId);
		return bean;
	}
	
	/**
	 * @Description   初始化活动数据
	 * @param jzId
	 * @param bigId
	 * @return
	 */
	public XianShiBean initXianShiInfoNew(Long jzId,int bigId,PlayerTime playerTime) {
		// 获取数据库中是否有此记录，有的话什么也不做
		log.info("初始化{}限时活动（类型-{}）数据", jzId,bigId);
		if(playerTime == null){
			playerTime = HibernateUtil.find(PlayerTime.class, jzId);
		}
		Date startDate=null;
		if(playerTime!=null&&playerTime.zhunchengTime!=null){
			startDate=playerTime.zhunchengTime;
		}else{
			log.info("初始化{}限时活动-{}数据出错，playerTime|ZhunchengTime为空",jzId,bigId);
			startDate=new Date();
		}
		XianShiBean bean = new XianShiBean();
		bean.bigId=bigId;
		bean.huoDongId=bigId+1;
		bean.junZhuId = jzId;
		bean.startDate=startDate;
		bean.id=jzId*100+bigId;
		MC.add(bean, jzId);
		HibernateUtil.insert(bean);
		log.info("玩家id是 ：{}的 限时活动（类型-{}）生成成功", jzId,bigId);
		return bean;
	}
	
	/**
	 * @Description   获取其他限时活动数据
	 */
	public void getOtherXianShiInfo(long jzId,int jzLevel,	XianshiControl	xsControl,XianShiBean xianshiBean,XinShouXianShiInfo.Builder resp,List<String>isYilingList,List<String>isKelingList) {
		int bigId=xsControl.id;
//		log.info("获取{}限时活动-{}数据", jzId,bigId);
		List<XianshiHuodong> xsList=bigActivityMap.get(bigId);
		if(xsList==null){
			log.error("玩家{}获取活动{}数据出错，XianshiHuodong-List为空",jzId,bigId);
			return;
		}
		int remainTime=-1;
		//2016年3月26日修正逻辑 这个时候已经没有超时设定了
//		Date canjiaTime =xianshiBean.startDate;
//		int useDtime= (int) ((System.currentTimeMillis()-canjiaTime.getTime())/1000);
//		if(useDtime>(xsControl.getCloseTime()*3600)){
//			remainTime=(xsControl.getDelayTime()+xsControl.getCloseTime())*3600-useDtime;
//			if(remainTime<0){
//				log.info("玩家{}活动-{}剩余延迟领奖时间为-{},活动超时自动完成",jzId,bigId,remainTime);
//				xianshiBean.finishDate=new Date();
//				HibernateUtil.save(xianshiBean);
//			}
//		}
		//返回整个活动剩余领奖时间
		if(resp!=null){
//			log.info("玩家{}活动-{}剩余延迟领奖时间为-{}",jzId,bigId,remainTime);
			resp.setRemainTime(remainTime);
		}
		int hdSize=xsList.size();
		//当前进度
		int condition=0;
		int beizhu=0;
		int doneType=xsControl.doneType;
		//每一小条活动信息
		//HuoDongInfo -State 奖励状态10：可领取 20：已领取 30：（超时未完成）不能领取 40:（未达到条件）不可领取
		for (int i = 0; i < hdSize; i++) {
			HuoDongInfo.Builder hd=HuoDongInfo.newBuilder();
			XianshiHuodong xshd=xsList.get(i);
			if(xshd.doneType == 1) continue;
			int huoDongId=xshd.id;
			int shengyu=0;////2016年3月26日修正逻辑 这个时候已经没有超时设定了 limitTime>useDtime?(limitTime-useDtime):0;
			hd.setShengTime(shengyu);
			//达到领奖条件判断
			int code=getOtherXianShiState(jzId, huoDongId, shengyu, condition, xshd,isYilingList,isKelingList);
			if(code != 10 && code != 40){ //优化点击成就显示慢,领取过的奖励不查询进度，提高提高效率
				continue;
			}
			beizhu=condition=getOtherXianShiCondition(jzId,jzLevel,doneType,bigId,huoDongId);
			hd.setState(code);
			hd.setJiangli(xshd.Award);
			hd.setHuodongId(huoDongId);
			if(resp!=null){
				resp.addHuodong(hd);
			}
			if(code == 10 || code == 40){ //优化点击成就显示慢，显示一条即可(可领取或未达成)
				break;
			}
		}
		if(resp!=null){
			if(doneType==TaskData.PVE_GUANQIA){
				PveTemp conf = PveMgr.inst.id2Pve.get(condition);
				//if()如果这一关是本章最后一关 进度=bigId+1否则进度=bigId
				beizhu=conf.bigId;
			}
			resp.setBeizhu(beizhu);
			resp.setTypeId(xianshiBean.bigId);
		}
	}
	
	/**
	 * @Description 推送限时活动有新奖励
	 */
	public void broadIsNew(long jzId,	XianshiControl	xs) {
		OpenXianShiResp.Builder resp=OpenXianShiResp.newBuilder();
		OpenXianShi.Builder xshuodong = OpenXianShi.newBuilder();
		xshuodong.setTypeId(xs.id);
		xshuodong.setName(xs.Name);
		xshuodong.setShunxu(xs.Rank);
		xshuodong.setState(10);
		xshuodong.setIsNewAward(true);
		resp.addXianshi(xshuodong);
		IoSession su = AccountManager.sessionMap.get(jzId);
		if (su != null)
		{
			su.write(resp.build());
			FunctionID.pushCanShowRed(jzId, su,FunctionID.activity_chengjiu);
		}else{
			log.info("君主{}不在线，取消推送限时活动-{}新奖励数据", jzId,xs.id);
		}
	}

	
	/**
	 * @Description 	获得限时活动状态
	 */
	public int getOtherXianShiState(long jzId,int huoDongId,int shengyu,int condition,XianshiHuodong xshd,List<String>isYilingList,List<String>isKelingList) {
		//达到完成条件 code 10：可领取 20：已领取  30超时 40 未达到条件
		if(isYilingList == null){
			isYilingList = DB.lgetList(XIANSHIYILING_KEY + jzId);
		}
		if(isKelingList == null){
			isKelingList = DB.lgetList(XIANSHIKELING_KEY + jzId);
		}
		boolean isKeling = isKelingList.contains(huoDongId + "");
		boolean isYiling = isYilingList.contains(huoDongId+ "");
		if(isYiling){
			if(isShow){
				log.info("当前君主{}限时活动-{}数据-可领取状态--已领", jzId,huoDongId);
			}
			return 20;
		}
		if(isKeling){
			//可领或者已领
			if(isShow){
				log.info("当前君主{}限时活动-{}数据-可领取状态--可领", jzId,huoDongId);
			}
			return 10;
		}
		//超时判断 没有超时判断了 2016年3月28日
//		boolean isChaoShi = DB.lexist((XIANSHICHAOSHI_KEY + jzId), huoDongId + "");
//		if(isChaoShi){
//			if(isShow){
//				log.info("当前君主{}限时活动-{}数据-可领取状态--超时不可领", jzId,huoDongId);
//			}
//			return 30;
//		}
//		if(xshd.getLimitTime()>0&&shengyu<=0){
//			DB.rpush4YaBiao((XIANSHICHAOSHI_KEY+ jzId), huoDongId+ "");
//			if(isShow){
//				log.info("当前君主{}限时活动-{}数据-可领取状态--超时不可领", jzId,huoDongId);
//			}
//			return 30;
//		}
		//未超时
		//2016年1月5日 自动触发任务是否完成判断 这里不用进行完成判断
		if(isShow){
			log.info("当前君主{}限时活动-{}-{}数据-可领取状态--未达到条件", jzId,xshd.BigId,huoDongId);
		}
		return  40;
//		if(isCompleted(jzId,condition, xshd)){
//			if(isShow){
//				log.info("当前君主{}限时活动-{}-{}数据-可领取状态--可领", jzId,xshd.BigId,huoDongId);
//			}
//			return  10;
//		}else{
//			if(isShow){
//				log.info("当前君主{}限时活动-{}-{}数据-可领取状态--未达到条件", jzId,xshd.BigId,huoDongId);
//			}
//			return  40;
//		}
		
	}
	

	/**
	 * @Description   判断首日 七日活动领奖条件是否达成
	 */
	public int getShengyuTime4ShouRi(long jzId,int huoDongId,int doneCondition,int useDTime) {
		int result=0;
		log.info("君主首日奖励useDTime为{}",useDTime);
		result= (doneCondition-useDTime);
		//达成领奖条件，记录到未可领
		if(result<=0){
			log.info("君主-{}的限时活动-{}状态变为可领取",jzId,huoDongId);
			DB.rpush4YaBiao((XIANSHIKELING_KEY + jzId), huoDongId+ "");
		}
		return result;
	}
	/**
	 * @Description   判断首日 七日活动领奖条件是否达成
	 */
	public boolean isCompleted47Days(long jzId,XianshiHuodong xshd) {
		boolean result=false;
		Integer doneCondition4QIRI=Integer.parseInt(xshd.doneCondition);
		String loginCount= DB.get(XIANSHI7DAY_KEY + jzId);
		loginCount=loginCount==null?"1":loginCount;
		Integer loginDay=Integer.parseInt(loginCount);
//		log.info("君主七日奖励登录天数loginCount为{}",loginCount);
		result=(loginDay>=doneCondition4QIRI);
		//达成领奖条件，记录到未可领
		if(result){
			int huoDongId=xshd.id;
//			log.info("君主-{}的限时活动-{}状态变为可领取",jzId,huoDongId);
			DB.rpush4YaBiao((XIANSHIKELING_KEY + jzId), huoDongId+ "");
		}
		return result;
	}
	/**
	 * @Description   判断活动领奖条件是否达成
	 */
	public boolean isCompleted(long jzId,int condition,XianshiHuodong xshd) {
		boolean result=false;
		switch (xshd.doneType) {
		/**内测开发以下4个限时活动*/
		case TaskData.junzhu_level_up://1 等级
			Integer doneCondition4CHONGJI=Integer.parseInt(xshd.doneCondition);
			result= (condition>=doneCondition4CHONGJI);
			break;
		case TaskData.PVE_GUANQIA://2 过关数
			Integer doneCondition4Guoguan=Integer.parseInt(xshd.doneCondition);
			result= (condition>=doneCondition4Guoguan);
			break;
		case TaskData.jingyingjixing://44 集星 星级数
			Integer doneCondition4JingYing=Integer.parseInt(xshd.doneCondition);
			result=(condition>=doneCondition4JingYing);
			break;
		case TaskData.N_quality_ok://40  装装备进阶
			result =(condition>=xshd.id);
			break;
			/**内测开发以上4个限时活动*/
		case TaskData.ONE_QIANG_HAU://9
			// 对应处理
			break;
		case TaskData.FINISH_CHUANQI://13
			// 对应处理
			break;
		case TaskData.XILIAN_ONE_GONG://14
			// 对应处理
			break;
		case TaskData.get_x_mibao://19
			// 对应处理
			break;
		case TaskData.mibao_shengji_x://20
			result=checkLevel4Mibao(jzId, xshd);
			break;
		case TaskData.mibao_shengStar_x://21
			// 对应处理
			break;
		case TaskData.one_quality_ok://41
			// 对应处理
			break;
		case TaskData.tanbaocishu://45
			// 对应处理
			break;
		default:
			// 对应处理
			log.error(" {}判断活动条件是否达成出错，DoneType错误-{}",jzId,xshd.doneType);
			break;
		}
		//达成领奖条件，记录到未可领
		if(result){
			int huoDongId=xshd.id;
			log.info("君主-{}的限时活动-{}状态变为可领取",jzId,huoDongId);
			DB.rpush4YaBiao((XIANSHIKELING_KEY + jzId), huoDongId+ "");
		}
		return result;
	}


	
	/**
	 * @Description   《进阶达人》是否可领
	 */
	public boolean isJinJieCanGet(Bag<EquipGrid> equips,XianshiHuodong xshd) {
		String condition=xshd.doneCondition;
		boolean result=	isPinZhiOk(condition, equips);
		log.info("《进阶达人》限时任务---{}达成条件为{},判断结果为—--{}",xshd.id,condition,result);
		return result;
	}

	public boolean isPinZhiOk( String condition, Bag<EquipGrid> equips){
		String[] condis = condition.split("#");
		if(condis == null ){
			return false;
		}else if (condis.length >1) {
			for (int i = 0; i < condis.length; i++) {
				String string = condis[i];
				String[] conditions=string.split(":");
				if(!GameTaskMgr.inst.isFinishTask41(equips, Integer.parseInt(conditions[0]), 
						Integer.parseInt(conditions[1])))
				{
					return false;
				}
			}
		}else if(condis.length==1){
			String[] conditions=condition.split(":");
			return	GameTaskMgr.inst.isFinishTask40(equips, Integer.parseInt(conditions[0]),
					Integer.parseInt(conditions[1]));
		}else{
			return false;
		}
		return true;
	}
	
	public int jinJieProcess(String condition, Bag<EquipGrid> equips){
		String[] conditionsArr = condition.split(":");
		int minPinzhi = Integer.parseInt(conditionsArr[1]);
		List<EquipGrid> list = equips.grids;
		int count = 0;
		for(EquipGrid eg: list){
			if(eg == null){
				continue;
			}
			ZhuangBei zb = (ZhuangBei)TempletService.itemMap.get(eg.itemId);
			if(zb == null){
				continue;
			}
			// x件装备x品质以上
			if(zb.color >= minPinzhi){
				count ++;
			}
		}
		return count;
	}
	
	/**
	 * @Description   判断活动奖励是否可领取
	 */
	public int getAwardState(long jzId,int huoDongId) {
		boolean isUseD = DB.lexist((XIANSHIYILING_KEY + jzId), huoDongId+ "");
		//10未领取 已领取20 
		int result=isUseD?20:10;
		return result;
	}


	/**
	 * @Description  请求领取新手首日/七日签到活动奖励
	 */
	public void gainXinShouXianShiAward(int id, Builder builder,IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求领取新手首日活动奖励出错：君主不存在");
			return;
		}
		GainAward.Builder req = (GainAward.Builder) builder;
		if(req==null){
			log.error("{}请求领取新手首日/七日活动奖励出错：请求参数错误",jz.id);
			return;
		}
		int typeId=req.getTypeId();
		switch (typeId) {
		case XianShiConstont.ZAIXIANLIBAO_TYPE:
			getShouRiAward(jz, req, session); 
			break;
		case XianShiConstont.QIRIQIANDAO_TYPE:
			get7DaysAward(jz, req, session);
			break;
		default:
			log.error("请求新手首日/七日签到活动奖励出错：活动类型编码{}错误",typeId);;
			break;
		}

	}


	/**
	 * @Description 获取首日奖励
	 */
	public void getShouRiAward(JunZhu jz ,GainAward.Builder req,IoSession session) {
		long jzId=jz.id;
		int huodongId=req.getHuodongId(); 
		ReturnAward.Builder resp=ReturnAward.newBuilder();
		//判断活动是否关闭
		if(isClosedById(huodongId)){
			log.info("{}新手首日之暂时关闭",jzId);
			resp.setHuodongId(huodongId);
			resp.setResult(30);
			session.write(resp.build());
			return;
		}
		boolean isKeLing = DB.lexist((XIANSHIKELING_KEY + jzId), huodongId + "");
		if(!isKeLing){
			log.info("{}新手首日之{}奖励未达到领取条件",jzId,huodongId);
			resp.setHuodongId(huodongId);
			resp.setResult(50);
			session.write(resp.build());
			return;
		}
		XianshiHuodong xshd=activityMap.get(huodongId);
		if(xshd==null){
			log.error("{}首日活动奖励{}领取失败，未找到奖励配置",jzId,huodongId);
			return;
		}
		String goods=xshd.Award;
		//发放奖励
		giveAward( goods, session, jz,false);
		//存储已领取奖励
		DB.rpush4YaBiao((XIANSHIYILING_KEY + jzId),huodongId+ "");
		//移除可领
		DB.lrem(XIANSHIKELING_KEY + jzId,0,huodongId+ "");
		XianShiBean xsBean=HibernateUtil.find(XianShiBean.class, xshd.BigId+jzId*100);
		xsBean.startDate=new Date();
		boolean isFinish= isFinish(xshd.BigId,jzId);
		if(isFinish){///1.0版本 2015年9月15日1 改为 首日 七日一起开启    （当前活动领完奖励，进入七日活动--废弃）
			log.info("{}首日活动奖励领取完毕",jzId);
			//保存完成时间
			xsBean.finishDate=new Date();
			//存储到完成Redis
			DB.rpush4YaBiao((XIANSHIFINISH_KEY + jzId), xsBean.bigId+ "");
			//初始化七日活动废弃1.0版本 2015年9月15日1 改为 首日 七日一起开启 

			//1.0版本 2015年9月15日1 改为关闭首日  不通知开启七日活动,
			OpenXianShiResp.Builder openResp=OpenXianShiResp.newBuilder();
			OpenXianShi.Builder xianshi4ShouRi=OpenXianShi.newBuilder();
			xianshi4ShouRi.setTypeId(XianShiConstont.ZAIXIANLIBAO_TYPE);
			xianshi4ShouRi.setName("在线礼包");
			xianshi4ShouRi.setShunxu(-1);
			xianshi4ShouRi.setState(20);
			xianshi4ShouRi.setIsNewAward(false);
			openResp.addXianshi(xianshi4ShouRi);
			session.write(openResp.build());
			//Bag<BagGrid> bag = BagMgr.inst.loadBag(jz.id);
			//BagMgr.inst.sendBagInfo(session, bag);
		}
		HibernateUtil.save(xsBean);
		resp.setHuodongId(huodongId);
		resp.setResult(10);
		session.write(resp.build());
	}
	
	
	/**
	 * @Description 判断君主的限时活动是否可完成
	 */
	public boolean isFinish(int bigId,long jzId){
		return (isNewAward(bigId,jzId)==2)?true:false;
	}
	
	/**
	 * @Description 判断君主的限时活动是否可完成
	 */
	public boolean isFinishNew(int bigId,long jzId,int huodongId){
		List<XianshiHuodong> huodongList=bigActivityMap.get(bigId);
		if(huodongList==null){
			log.error("isCanOpen--玩家{}获取活动{}数据出错，XianshiHuodong-List为空",jzId,bigId);
			return false;
		}
		int hdSize=huodongList.size();
		XianshiHuodong xshd=huodongList.get(hdSize - 1);
		if(huodongId == xshd.id){
			return true;
		}
		return false;
	}
	
	/**
	 * @Description 完成并关闭君主的限时活动
	 */
	public XianShiBean finishXianShiActivity(int bigId,long jzId,XianShiBean xsBean,IoSession session,int huodongId){
		boolean isFinish=isFinishNew(bigId, jzId,huodongId);
		if(isFinish){
			xsBean.finishDate=new Date();
			HibernateUtil.save(xsBean);
			DB.rpush4YaBiao((XIANSHIFINISH_KEY + jzId),bigId+ "");
			log.info("君主{}限时活动---{}完成",jzId,bigId);
			OpenXianShiResp.Builder openResp=OpenXianShiResp.newBuilder();
			OpenXianShi.Builder xianshi=OpenXianShi.newBuilder();
			xianshi.setTypeId(bigId);
			xianshi.setName("");
			xianshi.setShunxu(-1);
			xianshi.setState(20);
			xianshi.setIsNewAward(false);
			openResp.addXianshi(xianshi);
			session.write(openResp.build());
		}
		return xsBean;
	}

	
	/**
	 * @Description 	发放奖励
	 * @isUseItem 是否需要弹窗 true 需要
	 */
	public void giveAward(String goods,IoSession session,JunZhu jz,boolean isUseItem){
		String[] goodsArray = goods.split("#");
		//增加物品
		ExploreResp.Builder awardresp = ExploreResp.newBuilder(); //奖励弹窗
		awardresp.setSuccess(0);
		for (String g : goodsArray) {
			String[] ginfo = g.split(":");
			AwardTemp award = new AwardTemp();
			award.itemType = Integer.parseInt(ginfo[0]);
			award.itemId = Integer.parseInt(ginfo[1]);
			award.itemNum = Integer.parseInt(ginfo[2]);
			AwardMgr.inst.giveReward(session, award, jz,false,false);
			//奖励弹窗
			Award.Builder awardInfo = Award.newBuilder();
			awardInfo.setItemType(Integer.parseInt(ginfo[0]));
			awardInfo.setItemId(Integer.parseInt(ginfo[1]));
			awardInfo.setItemNumber(Integer.parseInt(ginfo[2]));
			awardresp.addAwardsList(awardInfo);
		}
		if(isUseItem){
			ProtobufMsg pm = new ProtobufMsg();
			pm.id=PD.S_USE_ITEM;
			pm.builder=awardresp;
			session.write(pm);
		}
	}
	
	

	/**
	 * @Description 判断是否有可领奖励 
	 *  0未找到配置 1有新奖励但没有领取过 2领完所有奖励 3未达到领奖条件
	 * @param bigId
	 * @param jzId
	 * @return
	 */
	public int isNewAward(int bigId,long jzId){
		List<XianshiHuodong> huodongList=bigActivityMap.get(bigId);
		if(huodongList==null){
			log.error("isCanOpen--玩家{}获取活动{}数据出错，XianshiHuodong-List为空",jzId,bigId);
			return 0;
		}
		int hdSize=huodongList.size();
		for (int i = 0; (i < hdSize); i++) {
			XianshiHuodong xshd=huodongList.get(i);
			int huoDongId=xshd.id;
			int state=checkItemState(jzId, huoDongId);
			if(state==20){//有新奖励但没有领取过
				return 1;
			}
			if((i==hdSize-1)){//领完所有奖励
				if(state>0){
					return 2;
				}
			}
		}
		return 3;
	}

	/**
	 * @Description   获取七日在线活动数据
	 */
	public void get7DaysInfo(JunZhu jz, Builder builder, IoSession session) {
		long jzId=jz.id;
		XianShiBean xsBean=HibernateUtil.find(XianShiBean.class, XianShiConstont.QIRIQIANDAO_TYPE+jzId*100);
		XinShouXianShiInfo.Builder resp=XinShouXianShiInfo.newBuilder();
		if(xsBean==null){
			xsBean=initXianShiInfo(jzId,XianShiConstont.QIRIQIANDAO_TYPE);
		}
		log.info("君主{} 登录第{}天 ",jzId,DB.get(XIANSHI7DAY_KEY + jzId));
		int bigId =xsBean.bigId;
//		log.info("获取{}七日限时活动-{}数据", jzId,bigId);
		List<XianshiHuodong> xsList=bigActivityMap.get(bigId);
		if(xsList==null){
			log.error("玩家{}获取七日活动{}数据出错，XianshiHuodong-List为空",jzId,bigId);
			return;
		}
		int hdSize=xsList.size();
		//每一小条活动信息
		//HuoDongInfo -State 奖励状态10：可领取 20：已领取 30：（超时未完成）不能领取 40:（未达到条件）不可领取
		for (int i = 0; i < hdSize; i++) {
			HuoDongInfo.Builder hd=HuoDongInfo.newBuilder();
			XianshiHuodong xshd=xsList.get(i);
			//无限时 返回剩余时间-1
			hd.setShengTime(-1);
			int huoDongId=xshd.id;
			if(isCompleted47Days(jzId,xshd)){//达到完成条件
				int code=getAwardState(jzId, huoDongId);
				if(isShow){
					log.info("当前君主{}七日限时活动-{}数据-可领取状态{}", jzId,huoDongId,code);
				}
				hd.setState(code);
			}else{
				if(isShow){
					log.info("当前君主{}七日限时活动-{}数据-可领取状态{}", jzId,huoDongId,40);
				}
				hd.setState(40);
			}
			hd.setJiangli(xshd.Award);
			hd.setHuodongId(huoDongId);
			resp.addHuodong(hd);
		}
		Calendar calendar = Calendar.getInstance();
		Date nowDate = calendar.getTime();
		calendar.set(Calendar.HOUR_OF_DAY,4); //四点重置
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		if(nowDate.getTime() > calendar.getTimeInMillis()){
			calendar.add(Calendar.DATE,1);
		}
		int timeDistance=DateUtils.timeDistanceBySecond(calendar.getTime(),nowDate);
		//七日活动 2015年12月3日 返回现在时间离明天4点的时间间隔
		resp.setBeizhu(timeDistance);
		//返回整个活动剩余时间 首日和七日无限时返回-1
		resp.setRemainTime(-1);
		resp.setTypeId(bigId);
		session.write(resp.build());
	}

	/**
	 * @Description   领取七日活动奖励
	 */
	public void get7DaysAward(JunZhu jz, GainAward.Builder req, IoSession session) {
		long jzId=jz.id;
		int huodongId=req.getHuodongId(); 
		ReturnAward.Builder resp=ReturnAward.newBuilder();
		//判断活动是否关闭
		if(isClosedById(huodongId)){
			log.info("{}七日活动暂时关闭",jzId);
			resp.setHuodongId(huodongId);
			resp.setResult(30);
			session.write(resp.build());
			return;
		}
		XianshiHuodong xshd=activityMap.get(huodongId);
		if(xshd==null){
			log.error("{}7日活动奖励{}领取失败，未找到奖励配置",jzId,huodongId);
			return;
		}
		XianShiBean xsBean=HibernateUtil.find(XianShiBean.class, xshd.BigId+jzId*100);
		if(xsBean==null){
			//活动未开启
			resp.setHuodongId(huodongId);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		boolean isKeLing = DB.lexist((XIANSHIKELING_KEY + jzId), huodongId + "");
		if(isKeLing){
			String goods=xshd.Award;
			setReturnAward(jz,resp,goods);
			giveAward(goods, session, jz,false);
			//存储已领取奖励
			DB.rpush4YaBiao((XIANSHIYILING_KEY + jzId),huodongId+ "");
			//移除可领
			DB.lrem(XIANSHIKELING_KEY + jzId,0,huodongId+ "");
			log.info("{}-七日签到领取小活动--{}奖励",jzId,huodongId);
			//判断七日签到是否领完全部奖励 
			finishXianShiActivity(XianShiConstont.QIRIQIANDAO_TYPE, jzId, xsBean,session,huodongId);
			resp.setHuodongId(huodongId);
			resp.setResult(10);
			session.write(resp.build());
			//Bag<BagGrid> bag = BagMgr.inst.loadBag(jz.id);
			//BagMgr.inst.sendBagInfo(session, bag);
			return;
		}else{
			resp.setHuodongId(huodongId);
			resp.setResult(50);
			session.write(resp.build());
		}
	}

	/**
	 * @Description   获取成就（其他限时活动）信息 
	 */
	public void getOtherXianShiInfo(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求限时活动出错：君主不存在");
			return;
		}
		XinShouXSActivity.Builder req=(XinShouXSActivity.Builder)builder;
		if(req==null){
			log.error("{}请求限时活动出错：请求参数错误",jz.id);
			return;
		}
		int typeId=req.getTypeId();
		if(xsControlMap.get(typeId)==null){
			log.error("{}请求限时活动出错：请求活动类型参数错误-{}",jz.id,typeId);
			return;
		}
		long jzId=jz.id;
		XianShiBean xsBean=HibernateUtil.find(XianShiBean.class, typeId+jzId*100);
		if(xsBean==null){
			xsBean=initXianShiInfo(jzId,typeId);
		}
		XinShouXianShiInfo.Builder resp=XinShouXianShiInfo.newBuilder();
		XianshiControl	xControl=xsControlMap.get(typeId);
		List<String> isYilingList = DB.lgetList(XIANSHIYILING_KEY + jz.id);
		List<String> isKelingList = DB.lgetList(XIANSHIKELING_KEY + jz.id);
		getOtherXianShiInfo(jzId,jz.level,xControl,xsBean, resp,isYilingList,isKelingList);
		ProtobufMsg pm = new ProtobufMsg();
		pm.id=PD.S_XIANSHI_INFO_RESP;
		pm.builder=resp;
		session.write(pm);
	}
	
	/**
	 * @Description 领取成就奖励（七日 首日之外的显示活动的奖励 getOtherXianShiArard拼写错误）
	 */
	public void getOtherXianShiAward(int id, Builder builder, IoSession session) {
		JunZhu jz =  JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求限时活动奖励出错：君主不存在");
			return;
		}
		GainAward.Builder req = (GainAward.Builder) builder;
		if(req==null){
			log.error("{}请求限时活动奖励出错：请求参数错误",jz.id);
			return;
		}
		int bigId=req.getTypeId();
		if(xsControlMap.get(bigId)==null){
			log.error("{}请求限时活动奖励出错：请求活动类型参数错误-{}",jz.id,bigId);
			return;
		}
		long jzId=jz.id;
		int huodongId=req.getHuodongId(); 
		ReturnAward.Builder resp=ReturnAward.newBuilder();
		//判断活动是否关闭
		if(isClosedById(huodongId)){
			log.info("{}---{}活动暂时关闭",jzId,huodongId);
			resp.setHuodongId(huodongId);
			resp.setResult(30);
			GainOtherAwardResp(session, resp);
			return;
		}

		XianshiHuodong xshd=activityMap.get(huodongId);
		if(xshd==null){
			log.error("{}请求限时活动{}奖励领取失败，未找到奖励配置",jzId,huodongId);
			return;
		}
		XianShiBean xsBean=HibernateUtil.find(XianShiBean.class,xshd.BigId+jzId*100);
		if(xsBean==null){
			log.error("{}请求限时活动{}奖励领取失败，未找到奖励配置",jzId,huodongId);
			//君主此活动未开启 或活动已过期
			resp.setHuodongId(huodongId);
			resp.setResult(40);
			GainOtherAwardResp(session, resp);
			return;
		}
		//没有超时判断了 2016年3月28日
//		boolean isChaoShi = DB.lexist((XIANSHICHAOSHI_KEY + jzId), huodongId + "");
//		if(isChaoShi){
//			log.info("君主{}的活动之{}奖励已超过完成时间",jzId,huodongId);
//			resp.setHuodongId(huodongId);
//			resp.setResult(40);
//			GainOtherAwardResp(session, resp);
//			return;
//		}
		boolean isCan= DB.lexist((XIANSHIKELING_KEY + jzId), xshd.id + "");
		if(isCan){
			String goods=xshd.Award;
			giveAward(goods, session, jz,true);
			//存储已领取奖励
			DB.rpush4YaBiao((XIANSHIYILING_KEY + jzId),huodongId+ "");
			//移除可领
			DB.lrem(XIANSHIKELING_KEY + jzId,0,huodongId+ "");
			resp.setHuodongId(huodongId);
			resp.setResult(10);
			GainOtherAwardResp(session, resp);
			log.info("君主{}的其他限时活动之{}奖励， 领取成功",jzId,huodongId);
			// 领取【探宝】
			if(bigId == XianShiConstont.TANBAO_ONETIMES){
				EventMgr.addEvent(jzId,ED.get_achieve , new Object[] {jzId});
			}
			//判断限时活动是否可以完成关闭
			finishXianShiActivity(bigId, jzId, xsBean,session,huodongId);
			//Bag<BagGrid> bag = BagMgr.inst.loadBag(jz.id);
			//BagMgr.inst.sendBagInfo(session, bag);
		}else{ 
			//条件未达成
			resp.setHuodongId(huodongId);
			resp.setResult(50);
			GainOtherAwardResp(session, resp);
		}

	}


	/**
	 * @Description 根据doneType类型获取jzId的君主限时活动达到的进度
	 */
	public int getOtherXianShiCondition(long jzId,int jzlevel,int doneType,int bigId,int hdId){
		int condition=0;
		switch (doneType) {
		case  TaskData.junzhu_level_up:
			condition=jzlevel;
			break;
		case  TaskData.PVE_GUANQIA:
			//计算最大关卡
			condition=BigSwitch.pveGuanQiaMgr.getGuanQiaMaxId(jzId);
			break;
		case  TaskData.jingyingjixing:
			//计算星级
			condition=BigSwitch.pveGuanQiaMgr.getAllGuanQiaStartSum(jzId);
			log.info("计算星级{}的星级--结果为{}",jzId,condition);
			break;
		case  TaskData.N_quality_ok://40
			//装备进阶
			List<XianshiHuodong> xsList=bigActivityMap.get(bigId);
			Bag<EquipGrid> equips=EquipMgr.inst.loadEquips(jzId);
			int xsSize=xsList.size();
			for (int i = 0; i < xsSize; i++) {
				XianshiHuodong xshd=xsList.get(i);
				int huoDongId=xshd.id;
				if(huoDongId != hdId) continue;
				boolean isKeling = DB.lexist((XIANSHIKELING_KEY + jzId), huoDongId + "");
				if(isKeling){
//					condition=huoDongId;
					condition = jinJieProcess(xshd.doneCondition,equips);
					continue;
				}
				//没有超时判断了 2016年3月28日
//				boolean isChaoShi = DB.lexist((XIANSHICHAOSHI_KEY + jzId), huoDongId + "");
//				if(isChaoShi){
//					continue;
//				}
				
				if(isJinJieCanGet(equips,xshd)){
//					condition=huoDongId;
				    condition = jinJieProcess(xshd.doneCondition,equips);
					DB.rpush4YaBiao((XIANSHIKELING_KEY + jzId), huoDongId+ "");
					log.info("君主-{}的《进阶达人》限时活动-{}状态变为可领取",jzId,huoDongId);
					continue;
				}else{
					//跳出循环
					i=xsSize+1;
				}
			}
			break;
		case TaskData.zhuangBei_x_qiangHua_N://63
			//装备强化等级
			condition=getQianghuMaxLevel(jzId);
			log.info("君主--{}最大装备强化等级--{}",jzId,condition);
			break;
		case TaskData.ONE_QIANG_HAU://9
			//对应操作
			break;
		case TaskData.FINISH_CHUANQI://13
			//对应操作
			break;
		case TaskData.XILIAN_ONE_GONG://14
			//对应操作
			break;
		case TaskData.get_x_mibao://19
			condition = MibaoMgr.inst.getActivateMiBaoCount(jzId);
			break;
		case TaskData.mibao_shengji_x://20
			condition=MibaoMgr.inst.getMaxMibaoLevel(jzId);
			log.info("君主--{}最大秘宝等级--{}",jzId,condition);
			break;
		case TaskData.mibao_shengStar_x://21
			//对应操作
			break;

		case TaskData.one_quality_ok://41
			//对应操作
			break;
			//		case TaskData.zaixianlibao://42
			//			//对应操作
			//			break;
			//		case TaskData.qiriqiandao://43
			//			//对应操作
			//			break;

		case TaskData.tanbaocishu://45
			//对应操作
			break;

		case TaskData.miabao_x_star_n://68
			String conditionStr = activityMap.get(hdId).doneCondition;
			String[] conditionStrArr = conditionStr.split(":");
			condition = (int)MibaoMgr.inst.getMibaoStarNum(jzId, Integer.parseInt(conditionStrArr[0]),Integer.parseInt(conditionStrArr[1]));
			break;
		case TaskData.tanbao_oneTime://74
			ExploreMine mine = ExploreMgr.inst.getMineByType(jzId, TanBaoData.yuanBao_type);
			if(mine != null){
				condition = mine.danChouClickNumber;
			}
			break;
		case TaskData.tanbao_tenTime://75
			ExploreMine mine2 = ExploreMgr.inst.getMineByType(jzId, TanBaoData.yuanBao_type);
			if(mine2 != null){
				condition = mine2.tenChouClickNumber;
			}
			break;
		case TaskData.get_miShu_pinZhi_y://105
			condition = MiBaoV2Mgr.inst.getMaxMiShu(jzId);
			break;
		case TaskData.chonglou_tiaozhan://111
			IoSession session = AccountManager.inst.sessionMap.get(jzId);
			JunZhu jZhu = JunZhuMgr.inst.getJunZhu(session);
			condition = ChongLouMgr.inst.getChongLouHighestLayer(jZhu.id);
			break;
		default:
			break;
		}
		return condition;
	}
	/**
	 * @Description 求最大强化等级
	 */
	public int getQianghuMaxLevel(long jzId) {
		String where = "where userId = " + jzId;
		List<UserEquip> equipList = HibernateUtil.list(UserEquip.class, where);
		int maxLevel=0;
		for (UserEquip ue : equipList) {
			if(ue.level>maxLevel){
				maxLevel=ue.level;
			}
		}
		return maxLevel;
	}

	/**
	 * @Description  请求其他限时活动的推送消息操作
	 */
	public void  GainOtherAwardResp( IoSession session,ReturnAward.Builder resp) {
		ProtobufMsg pm = new ProtobufMsg();
		pm.id=PD.S_XIANSHI_AWARD_RESP;
		pm.builder=resp;
		session.write(pm);
	}
	
	/**
	 * @Description 刷新等级限时活动
	 */
	public void refreshLevelActivity(Event event) {
		// 活动未完成且活动开启
		Object[]	obs = (Object[])event.param;
		long jzId = (Long)obs[0];
//		log.info("君主-{}--冲级送礼限时活动数据刷新",jzId);
		XianshiControl xs=xsControlMap.get(XianShiConstont.JUNZHU_LEVEUP);
		if(xs==null){
//			log.info("君主-{}--限时活动--{}数据刷新,活动配置没了，不刷新",jzId,XianShiConstont.JUNZHU_LEVEUP);
			return;
		}
		XianShiBean 	xsBean = HibernateUtil.find(XianShiBean.class, xs.id + jzId * 100);
		if (xsBean == null) {
			xsBean = initXianShiInfo(jzId, xs.id);
		}
		//限时活动 进行完成超时判断  （不是首日和七日活动时）
		if(checkisFinished(xsBean)){
//			log.info("君主-{}--冲级送礼限时活动数据刷新,活动完成，不刷新",jzId);
			return;
		}
		if (!xshdCloseList.contains(xs.id)) {
			if(xsBean.finishDate==null){
				//限时活动未完成 刷新活动状态
				refreshNormalXianShiInfo(jzId, xs, xsBean,null,null);
			}
		}
	}

	
	/**
	 * @Description 《进阶达人》限时活动数据刷新
	 */
	public void refreshEquipJinJieActivity(Event event) {
		// 活动未完成且活动开启
		Object[]	obs = (Object[])event.param;
		long 	jzId= (Long) obs[0];
//		log.info("君主-{}--《进阶达人》限时活动数据刷新",jzId);
		XianshiControl	xs=xsControlMap.get(XianShiConstont.EQUIP_JINJIE);
		if(xs==null){
			log.error("君主-{}--限时活动--{}数据刷新,活动配置没了，不刷新",jzId,XianShiConstont.EQUIP_JINJIE);
			return;
		}
		XianShiBean		xsBean = HibernateUtil.find(XianShiBean.class, xs.id + jzId * 100);
		if (xsBean == null) {
			xsBean = initXianShiInfo(jzId, xs.id);
		}
		//限时活动 进行完成超时判断  （不是首日和七日活动时）
		if(checkisFinished(xsBean)){
//			log.info("君主-{}--《进阶达人》限时活动数据刷新,活动完成，不刷新",jzId);
			return;
		}
		if (!xshdCloseList.contains(xs.id)) {
			if(xsBean.finishDate==null){
				// 刷新活动状态
				refreshNormalXianShiInfo(jzId,  xs, xsBean,null,null);
			}
		}
	}
	
	/**
	 * @Description 装备强化 限时活动数据刷新
	 */
	public void refreshEquipQiangHuaActivity(Event event) {
		Object[]	obs = (Object[])event.param;
		long 	jzId= (Long) obs[0];
//		log.info("君主-{}--《装备强化》限时活动数据刷新",jzId);
		XianshiControl	xs=xsControlMap.get(XianShiConstont.EQUIP_QIANGHUA);
		if(xs==null){
//			log.error("君主-{}--限时活动--{}数据刷新,活动配置没了，不刷新",jzId,XianShiConstont.EQUIP_QIANGHUA);
			return;
		}
		XianShiBean		xsBean = HibernateUtil.find(XianShiBean.class, xs.id + jzId * 100);
		if (xsBean == null) {
			xsBean = initXianShiInfo(jzId, xs.id);
		}
		//限时活动 进行完成超时判断  （不是首日和七日活动时）
		if(checkisFinished(xsBean)){
//			log.info("君主-{}--《装备强化》限时活动数据刷新,活动完成，不刷新",jzId);
			return;
		}
		if (!xshdCloseList.contains(xs.id)) {
			if(xsBean.finishDate==null){
				// 刷新活动状态
				refreshNormalXianShiInfo(jzId,  xs, xsBean,null,null);
			}
		}
	}
	
	/**
	 * @Description 《过关斩将》限时活动数据刷新
	 */
	public void refreshGuanQiaActivity(Event event) {
		// 活动未完成且活动开启
		Object[] obs = (Object[])event.param;
		long	jzId = (Long) obs[0];
//		log.info("君主-{}--《过关斩将》限时活动数据刷新",jzId);
		XianshiControl	xs=xsControlMap.get(1502000);
		if(xs==null){
//			log.error("君主-{}--限时活动--{}数据刷新,活动配置没了，不刷新",jzId,1502000);
			return;
		}
		XianShiBean	xsBean = HibernateUtil.find(XianShiBean.class, xs.id + jzId * 100);
		if (xsBean == null) {
			xsBean = initXianShiInfo(jzId, xs.id);
		}
		//限时活动 进行完成超时判断  （不是首日和七日活动时）
		if(checkisFinished(xsBean)){
//			log.info("君主-{}--《过关斩将》限时活动数据刷新,活动完成，不刷新",jzId);
			return;
		}
		if (!xshdCloseList.contains(xs.id)) {
			if(xsBean.finishDate==null){
				// 刷新活动状态
				refreshNormalXianShiInfo(jzId, xs, xsBean,null,null);
			}
		}
	}
	
	/**
	 * @Description 《精英集星》限时活动数据刷新
	 */
	public void refreshGuanQiaJiXingActivity(Event event) {
		Long jzId = (Long) event.param;
//		log.info("君主-{}《精英集星》限时活动数据刷新",jzId);
		XianshiControl	xs=xsControlMap.get(XianShiConstont.GUANQIA_STAR);
		if(xs==null){
//			log.error("君主-{}--限时活动--{}数据刷新,活动配置没了，不刷新",jzId,XianShiConstont.GUANQIA_STAR);
			return;
		}
		XianShiBean	xsBean = HibernateUtil.find(XianShiBean.class, xs.id + jzId * 100);
		if (xsBean == null) {
			xsBean = initXianShiInfo(jzId, xs.id);
		}
		//限时活动 进行完成超时判断  （不是首日和七日活动时）
		if(checkisFinished(xsBean)){
//			log.info("君主-{}--《精英集星》限时活动数据刷新,活动完成，不刷新",jzId);
			return;
		}
		if (!xshdCloseList.contains(xs.id)) {
			if(xsBean.finishDate==null){
				// 刷新活动状态
				refreshNormalXianShiInfo(jzId,  xs, xsBean,null,null);
			}
		}
	}

	
	/**
	 * @Description 《升星秘宝》限时活动数据刷新
	 */
	public void refreshMiBaoStarActivity(Event event) {
		Object[] obs = (Object[])event.param;
		long	jzId = (Long) obs[0];
//		log.info("君主-{} 《升星秘宝》限时活动数据刷新",jzId);
		XianshiControl	xsControl=xsControlMap.get(XianShiConstont.MIBAO_STAR);
		if(xsControl==null){
//			log.error("君主-{}--限时活动--{}数据刷新,活动配置没了，不刷新",jzId,XianShiConstont.MIBAO_STAR);
			return;
		}
		XianShiBean	xsBean = HibernateUtil.find(XianShiBean.class, xsControl.id + jzId * 100);
		if (xsBean == null) {
			xsBean = initXianShiInfo(jzId, xsControl.id);
		}
		//限时活动 进行完成超时判断  （不是首日和七日活动时）
		if(checkisFinished(xsBean)){
//			log.info("君主-{}--《升星秘宝》限时活动数据刷新,活动完成，不刷新",jzId);
			return;
		}
		if (!xshdCloseList.contains(xsControl.id)) {
			if(xsBean.finishDate==null){
				// 刷新活动状态
				refreshNormalXianShiInfo(jzId, xsControl, xsBean,null,null);
				
			}
		}
	}
	
	/**
	 * @Description 《秘宝获得》限时活动数据刷新
	 */
	public void refreshMiBaoGetActivity(Event event) {
		Object[] obs = (Object[])event.param;
		long	jzId = (Long) obs[0];
//		log.info("君主-{} 《秘宝获得》限时活动数据刷新",jzId);
		XianshiControl	xsControl=xsControlMap.get(XianShiConstont.MIBAO_GET);
		if(xsControl==null){
//			log.error("君主-{}--限时活动--{}数据刷新,活动配置没了，不刷新",jzId,XianShiConstont.MIBAO_GET);
			return;
		}
		XianShiBean	xsBean = HibernateUtil.find(XianShiBean.class, xsControl.id + jzId * 100);
		if (xsBean == null) {
			xsBean = initXianShiInfo(jzId, xsControl.id);
		}
		//限时活动 进行完成超时判断  （不是首日和七日活动时）
		if(checkisFinished(xsBean)){
//			log.info("君主-{}--《秘宝获得》限时活动数据刷新,活动完成，不刷新",jzId);
			return;
		}
		if (!xshdCloseList.contains(xsControl.id)) {
			if(xsBean.finishDate==null){
				// 刷新活动状态
				refreshNormalXianShiInfo(jzId, xsControl, xsBean,null,null);
				
			}
		}
	}

	/**
	 * @Description 《秘宝升级》限时活动数据刷新
	 */
	public void refreshMiBaoLevelUpActivity(Event event) {
		Object[] op=(Object[] )event.param;
		Long jzId = (Long) op[0];
//		log.info("君主-{}《秘宝升级》限时活动数据刷新",jzId);
		XianshiControl	xsControl=xsControlMap.get(XianShiConstont.MIBAO_LEVELUP);
		if(xsControl==null){
			log.error("君主-{}--限时活动--{}数据刷新,活动配置没了，不刷新",jzId,XianShiConstont.MIBAO_LEVELUP);
			return;
		}
		XianShiBean	xsBean = HibernateUtil.find(XianShiBean.class, xsControl.id + jzId * 100);
		if (xsBean == null) {
			xsBean = initXianShiInfo(jzId, xsControl.id);
		}
		//限时活动 进行完成超时判断  （不是首日和七日活动时）
		if(checkisFinished(xsBean)){
//			log.info("君主-{}--《秘宝升级》限时活动数据刷新,活动完成，不刷新",jzId);
			return;
		}
		if (!xshdCloseList.contains(xsControl.id)) {
			if(xsBean.finishDate==null){
				// 刷新活动状态
				refreshNormalXianShiInfo(jzId, xsControl, xsBean,null,null);
			}
		}
	}
	
	
	/**
	 * @Description 检查活动是否完成
	 */
	public boolean checkisFinished(XianShiBean xsBean) {
		//2016年3月26日修正逻辑 这个时候已经没有超时设定了
		long jzId=xsBean.junZhuId;
		boolean isFinish = DB.lexist((XIANSHIFINISH_KEY + jzId), xsBean.bigId+ "");
//		log.info("君主--{}限时活动大Id-{},完成状态--{}", jzId,xsBean.bigId,isFinish);
		return isFinish;
	}
	
	/**
	 * @Description 检查小活动条目 状态 是否需要刷新 0没达成，需要刷新 10已领 20可领 30超时
	 * @param jzId
	 * @param huoDongId
	 * @return
	 */
	public int checkItemState(long jzId,int huoDongId) {
		boolean isYiling = DB.lexist((XIANSHIYILING_KEY + jzId), huoDongId+ "");
		if(isYiling){
			return 10;
		}
		boolean isKeling = DB.lexist((XIANSHIKELING_KEY + jzId), huoDongId + "");
		if(isKeling){
			return 20;
		}
		//没有超时判断了 2016年3月28日
//		boolean isChaoShi = DB.lexist((XIANSHICHAOSHI_KEY + jzId), huoDongId + "");
//		if(isChaoShi){
//			return 30;
//		}
		return 0;
	}
	/**
	 * @Description   刷新除首日 七日外的限时活动数据
	 */
	public boolean refreshNormalXianShiInfo(long jzId,	XianshiControl	xsControl,XianShiBean xsBean,List<String>isYilingList,List<String> isKelingList) {
		int bigId=xsControl.id;
//		log.info("刷新{}限时活动-{}数据", jzId,bigId);
		List<XianshiHuodong> xsList=bigActivityMap.get(bigId);
		if(xsList==null){
//			log.error("玩家{}刷新限时活动{}数据出错，XianshiHuodong-List为空",jzId,bigId);
			return false;
		}
		int useDtime=0; //	//2016年3月26日修正逻辑 这个时候已经没有超时设定了(int) ((System.currentTimeMillis()-xsBean.startDate.getTime())/1000);
		boolean isNewAward =refreshSmallItem(jzId,xsList,useDtime,isYilingList,isKelingList);
		if(isNewAward){
//			log.info("君主{}限时活动-{} 有新奖励，推送数据", jzId,xsControl.id);
			broadIsNew(jzId, xsControl);
		}
		return isNewAward;
	}

	/**
	 * @Description 刷新限时活动小条目状态
	 */
	public boolean refreshSmallItem(long jzId, List<XianshiHuodong> xsList, int useDtime,List<String>isYilingList,List<String> isKelingList) {
		boolean isNewAward=false;
		int xsSize=xsList.size();
		if(isYilingList == null){
			isYilingList = DB.lgetList(XIANSHIYILING_KEY + jzId);
		}
		if(isKelingList == null){
			isKelingList = DB.lgetList(XIANSHIKELING_KEY + jzId);
		}
		for (int i = 0; i < xsSize; i++) {
			XianshiHuodong xshd=xsList.get(i);
			int huoDongId=xshd.id;
			int state=0;
			if(isYilingList.contains(huoDongId + "")){
				state = 10;
			}
			if(isKelingList.contains(huoDongId + "") && state != 10){
				state = 20;
			}
			if(state>0){
				if(state==20){
					isNewAward=true;	
				}
				continue;
			}
			//判断小活动是否超时 
			int limitTime=xshd.LimitTime;
			if(limitTime>0){
				int shengyu=limitTime>useDtime?(limitTime-useDtime):0;
				if(shengyu<=0){
					//没有超时判断了
//					DB.rpush4YaBiao((XIANSHICHAOSHI_KEY+ jzId), huoDongId+ "");
					continue;
				}
			}
//			FIXME 认为第一个未完成的后面所有任务都不能完成 策划这么决定的 认不认不知道
			//此处判定条件随机待变更
			if(isWanCheng(jzId,xshd)){
				isNewAward=true;
				continue;
			}else{
				//跳出循环
				i=xsSize+1;
			}
		}
		return isNewAward;
	}

	/**
	 * @Description 判断限时活动小条目是否完成
	 */
	public boolean isWanCheng(long jzId,XianshiHuodong xshd){
		boolean result=false;
		int doneType=xshd.doneType;
		switch (doneType) {
		case  TaskData.junzhu_level_up://升级
			result=checkJZLevel(jzId, xshd);
			break;
		case  TaskData.PVE_GUANQIA://章节
			result=checkGuanQia(jzId, xshd);
			break;
		case  TaskData.jingyingjixing://关卡星级
			result=checkStar4GuanQia(jzId, xshd);
			break;
		case  TaskData.N_quality_ok://装备进阶 40
			result=checkEquipJinJie(jzId, xshd);
			break;
		case TaskData.zhuangBei_x_qiangHua_N://63
			//装备强化等级
			result=checkEquipQianHua(jzId, xshd);
			break;
		case TaskData.mibao_shengji_x://20
			// 秘宝升级
			result=checkLevel4Mibao(jzId, xshd);
			break;
		case TaskData.miabao_x_star_n://68
			//秘宝星级
			result= checkStar4Mibao(jzId, xshd);
			break;
		case TaskData.get_x_mibao://19
			//秘宝获得或者合成
			result= checkCount4Mibao(jzId, xshd);
			//对应操作
			break;
		case TaskData.tanbao_oneTime://74
			//探宝1抽
			result= check4TanbaoOneTimes(jzId, xshd);
			//对应操作
			break;
		case TaskData.tanbao_tenTime://	75	
			//探宝10连抽
			result= check4TanbaoTenTimes(jzId, xshd);
			//对应操作
			break;
		//TODO 探宝10次 1次处理
		case TaskData.get_miShu_pinZhi_y://激活秘术
			result= checkMishuPinzhi(jzId, xshd);
			break;
		case TaskData.chonglou_tiaozhan://挑战指定层千重楼
			result= checkChonglouLayer(jzId, xshd);
			break;
		default:
			break;
		}
		//达成领奖条件，记录到未可领
		if(result){
			int huoDongId=xshd.id;
//			info("君主-{}的限时活动-{}状态变为可领取",jzId,huoDongId);
			DB.rpush4YaBiao((XIANSHIKELING_KEY + jzId), huoDongId+ "");
		}
		return result;
	}
	
	/**
	 * @Description 检查探宝10连抽是否完成
	 */
	public boolean check4TanbaoTenTimes(long jzId, XianshiHuodong xshd) {
		boolean	result=false;
		ExploreMine tanbaodata=	ExploreMgr.inst.getMineByType(jzId, TanBaoData.yuanBao_type);
		if(tanbaodata!=null){
			Integer count4TanbaoTenTime=Integer.valueOf(xshd.doneCondition);
			result=tanbaodata.tenChouClickNumber>=count4TanbaoTenTime;
//			log.info("君主-{}的探宝10连抽限时活动-{}状态---{}---君主完成次数--{}，所需条件---{}",
//					jzId,xshd.id,result,tanbaodata.tenChouClickNumber,count4TanbaoTenTime);
		}
		return result;
	}

	
	/**
	 * @Description 	检查探宝单抽是否完成
	 */
	public boolean check4TanbaoOneTimes(long jzId, XianshiHuodong xshd) {
		boolean	result=false;
		ExploreMine tanbaodata=	ExploreMgr.inst.getMineByType(jzId, TanBaoData.yuanBao_type);
		if(tanbaodata!=null){
			Integer count4TanbaoOneTime=Integer.valueOf(xshd.doneCondition);
			result=tanbaodata.danChouClickNumber>=count4TanbaoOneTime;
//			log.info("君主-{}的探宝10连抽限时活动-{}状态---{}---君主完成次数--{}，所需条件---{}",
//					jzId,xshd.id,result,tanbaodata.danChouClickNumber,count4TanbaoOneTime);
		}
		return result;
	}
	/**
	 * @Description 	检查冲级限时活动小活动是否完成
	 */
	public boolean checkJZLevel(long jzId,XianshiHuodong xshd) {
		boolean	result=false;
		JunZhu jz=HibernateUtil.find(JunZhu.class, jzId);
		if(jz!=null){
			Integer level=Integer.valueOf(xshd.doneCondition);
			result= jz.level>=level;
//			log.info("君主-{}的冲级限时活动-{}状态---{}---君主等级--{}，所需条件---{}",jzId,xshd.id,result,jz.level,level);
		}
		return  result;
	}
	/**
	 * @Description 	检查冲关卡小活动是否完成
	 */
	public boolean checkGuanQia(long jzId,XianshiHuodong xshd) {
		boolean	result=false;
		int maxguanqia=(BigSwitch.pveGuanQiaMgr.getGuanQiaMaxId(jzId));
		if(maxguanqia>0){
			Integer guanqia=Integer.valueOf(xshd.doneCondition);
			result=maxguanqia>=guanqia;
		}
		return  result;
	}
	/**
	 * @Description 	检查冲关卡集星小活动是否完成
	 */
	public boolean checkStar4GuanQia(long jzId,XianshiHuodong xshd) {
		boolean	result=false;
		int	countStar=BigSwitch.pveGuanQiaMgr.getAllGuanQiaStartSum(jzId);
		Integer star4Guanqia=Integer.valueOf(xshd.doneCondition);
		result= countStar>=star4Guanqia;
		return  result;
	}
	/**
	 * @Description 	检查装备进阶小活动是否完成
	 */
	public boolean checkEquipJinJie(long jzId,XianshiHuodong xshd) {
		boolean	result=false;
		Bag<EquipGrid> equips4JinJie=EquipMgr.inst.loadEquips(jzId);
		result=isJinJieCanGet(equips4JinJie,xshd);
		return  result;
	}
	/**
	 * @Description 	检查装备强化小活动是否完成
	 */
	public boolean checkEquipQianHua(long jzId,XianshiHuodong xshd) {
		boolean	result=false;
		String[] condis = xshd.doneCondition.split(":");
		int xCount=Integer.parseInt(condis[0]);
		int nLevel=Integer.parseInt(condis[1]);
		result=GameTaskMgr.inst.isQiangHua_X_N(jzId,xCount,nLevel) ;
		return  result;
	}
	/**
	 * @Description 	检查秘宝升级小活动是否完成
	 */
	public boolean checkLevel4Mibao(long jzId,XianshiHuodong xshd) {
		boolean	result=false;
		String[] doneCondition =xshd.doneCondition.split(":");
		Integer count1=Integer.valueOf(doneCondition[0]);
		Integer mibaoLevel=Integer.valueOf(doneCondition[1]);
		result=MibaoMgr.inst.isMibaoLevelOk(jzId, count1, mibaoLevel);
		return  result;
	}
	/**
	 * @Description 	检查秘宝星级小活动是否完成
	 */
	public boolean checkStar4Mibao(long jzId,XianshiHuodong xshd) {
		boolean	result=false;
		String[] doneCondition4miBaoStar =xshd.doneCondition.split(":");
		Integer count=Integer.valueOf(doneCondition4miBaoStar[0]);
		Integer star4Mibao=Integer.valueOf(doneCondition4miBaoStar[1]);
		result= MibaoMgr.inst.isMibaoStarOk(jzId, count, star4Mibao);
		return  result;
	}
	/**
	 * @Description 	检查秘宝数目小活动是否完成
	 */
	public boolean checkCount4Mibao(long jzId,XianshiHuodong xshd) {
		boolean	result=false;
		String doneCondition4miBaoGet =xshd.doneCondition;
		Integer count=Integer.valueOf(doneCondition4miBaoGet);
		result= MibaoMgr.inst.isMibaoCountOk(jzId, count);
		return  result;
	}
	
	/**
	 * 检查秘术品质
	 * @param jzId
	 * @param xshd
	 * @return
	 */
	public boolean checkMishuPinzhi(long jzId,XianshiHuodong xshd){
		String doneCondition=xshd.doneCondition;
		int pinzhi = MiBaoV2Mgr.inst.getMaxMiShu(jzId);
		if(pinzhi >= Integer.parseInt(doneCondition)){
			return true;
		}
		return false;
	}
	
	/**
	 * 检查重楼层数
	 * @param jzId
	 * @param xshd
	 * @return
	 */
	public boolean checkChonglouLayer(long jzId,XianshiHuodong xshd){
		String doneCondition=xshd.doneCondition;
		IoSession session = AccountManager.sessionMap.get(jzId);
		JunZhu jZhu = JunZhuMgr.inst.getJunZhu(session);
		int maxLayer = ChongLouMgr.inst.getChongLouHighestLayer(jZhu.id);
		if(maxLayer >= Integer.parseInt(doneCondition)){
			return true;
		}
		return false;
	}
	
	@Override
	public void proc(Event event) {
		if (event.param == null){
			return;
		}
		switch (event.id) {
		//君主升级 冲级送礼
		case ED.junzhu_level_up:
//			refreshLevelActivity(event);
			break;
			//装备进阶 《进阶达人》 
		case ED.JINJIE_ONE_GONG:
			refreshEquipJinJieActivity(event);
			break;
			//装备 --强化
		case ED.QIANG_HUA_FINISH:
			refreshEquipQiangHuaActivity(event);
			break;
			//关卡- 《过关斩将》
		case ED.PVE_GUANQIA:
			refreshGuanQiaActivity(event);
			break;	
			//关卡- 《精英集星》
		case ED.JINGYINGJIXING:
			// 活动未完成且活动开启
			refreshGuanQiaJiXingActivity(event);
			break;	
			//秘宝-获得、合成和升星
		case ED.MIBAO_HECHENG://合成 、获得 
			//由于秘宝有俩交错的活动 《秘宝获得》 《 秘宝星级活动》此的case 不会break;
			refreshMiBaoGetActivity(event);
		case ED.MIBAO_SEHNGXING://升星 
			refreshMiBaoStarActivity(event);
			break;
			//秘宝-升级
		case ED.mibao_shengji_x:
			// 活动未完成且活动开启 
			refreshMiBaoLevelUpActivity(event);
			break;	
		case ED.tanbao_oneTimes:
			//探宝1次
			refreshTanbao4oneTimes(event);
			break;	
		case ED.tanbao_tenTimes:
			//探宝10次
			refreshTanbao4tenTimes(event);
			break;	
		case ED.REFRESH_TIME_WORK:
			//封测福利红点
			refreshFuLiRedNode(event);
			break;	
		case ED.get_miShu_pinZhi_y:
			refreshMiShuJihuo(event);
			break;
		case ED.done_qianChongLou:
			refreshQianChongLou(event);
			break;
		case ED.JUNZHU_LOGIN:
			achievementRed(event);
			break;
		}
	}
	
	/**
	 * @Description 刷新君主福利奖励红点
	 */
	public void refreshFuLiRedNode(Event e) {
		IoSession session = (IoSession) e.param;
		if(session == null){
			return;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
//			log.error("刷新君主福利奖励红点出错，君主不存在");
			return;
		}
		long jzId=jz.id;
//		log.info("刷新君主---{}福利奖励红点",jz.id);
		Date now =new Date();
		FuliInfo info=HibernateUtil.find(FuliInfo.class, jzId);
		if(info==null){
//			log.info("定时刷新中初始化君主--{}的福利info",jzId);
			info=new FuliInfo();
			info.jzId=jzId;
			Cache.fuliInfoCache.put(jzId, info);
			HibernateUtil.save(info);
		}
		int fengceHongBaoCode=getNowHongBaoFuLiCode();
		int isGetCode=check4FengCeHongBao(info, now,fengceHongBaoCode);
//		log.info("君主--{}封测红包福利可领取状态--{}",jzId,isGetCode);
		if(isGetCode==1||isGetCode==2){
			FunctionID.pushCanShowRed(jz.id, session, FunctionID.fengcehongbao);
		}
		boolean isCanGetYueKa=	VipMgr.INSTANCE.hasYueKaAward(jzId);
		if(isCanGetYueKa){
			boolean isGet2=check4YuKaFuLi(info, now);
//			log.info("君主--{}月卡福利可领取状态--{}",jzId,isGet2);
			if(isGet2){
				FunctionID.pushCanShowRed(jz.id, session, FunctionID.yuekafuli);
			}
		}else{
//			log.info("君主--{}月卡福利未开启，无红点推送判断",jzId);
		}
		FuLiHuoDong.Builder tili=FuLiHuoDong.newBuilder();
		tili.setTypeId(FuliConstant.tilifuli);
		getTiliFuLiInfo(info, now,tili);
//		log.info("君主--{}体力福利可领取状态--{}",jzId,tili.getContent());
		boolean isGet3="体力奖励".equals(tili.getContent());
		if(isGet3){
			FunctionID.pushCanShowRed(jz.id, session, FunctionID.tilifuli);
		}
	}

	
	
	/**
	 * @Description //刷新探宝10次活动数据
	 */
	public void refreshTanbao4tenTimes(Event event) {
		// 活动未完成且活动开启
		Object[]	obs = (Object[])event.param;
		long jzId = (Long)obs[0];
//		log.info("君主-{}--探宝10次限时活动数据刷新",jzId);
		XianshiControl xs=xsControlMap.get(XianShiConstont.TANBAO_TENTIMES);
		if(xs==null){
//			log.info("君主-{}--限时活动--{}数据刷新,活动配置没了，不刷新",jzId,XianShiConstont.TANBAO_TENTIMES);
			return;
		}
		XianShiBean 	xsBean = HibernateUtil.find(XianShiBean.class, xs.id + jzId * 100);
		if (xsBean == null) {
			xsBean = initXianShiInfo(jzId, xs.id);
		}
		//限时活动 进行完成超时判断  （不是首日和七日活动时）
		if(checkisFinished(xsBean)){
//			log.info("君主-{}--探宝10次限时活动数据刷新,活动完成，不刷新",jzId);
			return;
		}
		if (!xshdCloseList.contains(xs.id)) {
			if(xsBean.finishDate==null){
				//限时活动未完成 刷新活动状态
				refreshNormalXianShiInfo(jzId, xs, xsBean,null,null);
			}
		}
	}

	
	/**
	 * @Description 	//刷新探宝1次活动数据
	 */
	public void refreshTanbao4oneTimes(Event event) {
		// 活动未完成且活动开启
		Object[]	obs = (Object[])event.param;
		long jzId = (Long)obs[0];
//		log.info("君主-{}--冲级送礼限时活动数据刷新",jzId);
		XianshiControl xs=xsControlMap.get(XianShiConstont.TANBAO_ONETIMES);
		if(xs==null){
//			log.info("君主-{}--限时活动--{}数据刷新,活动配置没了，不刷新",jzId,XianShiConstont.TANBAO_ONETIMES);
			return;
		}
		XianShiBean 	xsBean = HibernateUtil.find(XianShiBean.class, xs.id + jzId * 100);
		if (xsBean == null) {
			xsBean = initXianShiInfo(jzId, xs.id);
		}
		//限时活动 进行完成超时判断  （不是首日和七日活动时）
		if(checkisFinished(xsBean)){
//			log.info("君主-{}--冲级送礼限时活动数据刷新,活动完成，不刷新",jzId);
			return;
		}
		if (!xshdCloseList.contains(xs.id)) {
			if(xsBean.finishDate==null){
				//限时活动未完成 刷新活动状态
				refreshNormalXianShiInfo(jzId, xs, xsBean,null,null);
			}
		}
	}
	
	/**
	 * @Description 	//激活秘术
	 */
	public void refreshMiShuJihuo(Event event) {
		// 活动未完成且活动开启
		Object[] objs = (Object[]) event.param;
		long jzId = (Long)objs[0];
		XianshiControl xs=xsControlMap.get(XianShiConstont.MISHU_JIHUO);
		if(xs==null){
			return;
		}
		XianShiBean xsBean = HibernateUtil.find(XianShiBean.class, xs.id + jzId * 100);
		if (xsBean == null) {
			xsBean = initXianShiInfo(jzId, xs.id);
		}
		//限时活动 进行完成超时判断  （不是首日和七日活动时）
		if(checkisFinished(xsBean)){
			return;
		}
		if (!xshdCloseList.contains(xs.id)) {
			if(xsBean.finishDate==null){
				refreshNormalXianShiInfo(jzId, xs, xsBean,null,null);
			}
		}
	}
	
	/**
	 * @Description 	//千重楼
	 */
	public void refreshQianChongLou(Event event) {
		// 活动未完成且活动开启
		Object[] objs = (Object[]) event.param;
		long jzId = (Long)objs[0];
		XianshiControl xs=xsControlMap.get(XianShiConstont.CHONGLOU_TIAOZHAN);
		if(xs==null){
			return;
		}
		XianShiBean xsBean = HibernateUtil.find(XianShiBean.class, xs.id + jzId * 100);
		if (xsBean == null) {
			xsBean = initXianShiInfo(jzId, xs.id);
		}
		//限时活动 进行完成超时判断  （不是首日和七日活动时）
		if(checkisFinished(xsBean)){
			return;
		}
		if (!xshdCloseList.contains(xs.id)) {
			if(xsBean.finishDate==null){
				refreshNormalXianShiInfo(jzId, xs, xsBean,null,null);
			}
		}
	}

	@Override
	public void doReg() {
		EventMgr.regist(ED.JUNZHU_LOGIN, this);
		EventMgr.regist(ED.junzhu_level_up, this);
		EventMgr.regist(ED.JINJIE_ONE_GONG, this);
		EventMgr.regist(ED.PVE_GUANQIA, this);
		EventMgr.regist(ED.JINGYINGJIXING, this);
		EventMgr.regist(ED.MIBAO_HECHENG, this);// 主线任务：完成一次秘宝合成或者获得密保
		EventMgr.regist(ED.MIBAO_SEHNGXING, this);// 主线任务：完成一次秘宝生星级
		EventMgr.regist(ED.mibao_shengji_x, this);// 主线任务：完成一次秘宝升级
		EventMgr.regist(ED.QIANG_HUA_FINISH, this);//完成一次强化
		EventMgr.regist(ED.tanbao_oneTimes, this);//探宝单抽
		EventMgr.regist(ED.tanbao_tenTimes, this);//探宝10连抽
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);//红点刷新
		EventMgr.regist(ED.get_miShu_pinZhi_y, this);//秘术激活
		EventMgr.regist(ED.done_qianChongLou, this);//千重楼
	}

	/**
	 * @Description   保存登录天数 调用这个方法后必须刷新PlayerTime 的loginTime
	 * @param jzId
	 */
	public void updateLoginDate(long jzId) {
		String loginCount= DB.get(XIANSHI7DAY_KEY + jzId);
//		log.info("君主{}登录天数更新，开始为 第{}天",jzId,loginCount);
		if(loginCount==null){
//			log.info("君主{} 第一次登录，登录天数第一天",jzId);
			DB.set((XIANSHI7DAY_KEY + jzId),""+1);
			return;
		}
		PlayerTime playerTime = HibernateUtil.find(PlayerTime.class, jzId);
		if(playerTime==null){
//			log.error("君主{}---playerTime为空",jzId);
			return;
		}
		Date lastLogInTime = playerTime.loginTime;
//		log.info("君主{}上次登录时间—--{}",jzId,lastLogInTime);
		// change 20150901
		if((lastLogInTime!=null)&&(DateUtils.isTimeToReset(lastLogInTime, CanShu.REFRESHTIME_PURCHASE))){
			Integer dayCount=Integer.parseInt(loginCount);
			dayCount++;
			DB.set((XIANSHI7DAY_KEY + jzId),""+dayCount);
//			log.info("君主{} 登录--第{}天",jzId,dayCount);
		}
//		log.info("君主{} 登录第{}天 ,更新完成",jzId,DB.get(XIANSHI7DAY_KEY + jzId));
	}
	
	public void getAchievementList(int id,IoSession session,Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
//			log.error("请求限时活动出错：君主不存在");
			return;
		}
		// 加载活动列表
		List<XianshiControl> XianshiControlList =TempletService.listAll(XianshiControl.class.getSimpleName());
		ActivityAchievementResp.Builder respNew = ActivityAchievementResp.newBuilder();
//		List<XianShiBean> xsBeanList = HibernateUtil.list(XianShiBean.class,"");
//		Map<Long,XianShiBean> xsBeanMap = new HashMap<Long,XianShiBean>();
//		for (XianShiBean xianShiBean : xsBeanList) {
//			xsBeanMap.put(xianShiBean.id, xianShiBean);
//		}
		List<String> isYilingList = DB.lgetList(XIANSHIYILING_KEY + jz.id);
		List<String> isKelingList = DB.lgetList(XIANSHIKELING_KEY + jz.id);
		String ids = XianshiControlList.stream()
				.map(m->String.valueOf(m.id + jz.id*100))
				.collect(Collectors.joining(","));
		String where = " where id in("+ ids +")";
		List<XianShiBean> list = HibernateUtil.list(XianShiBean.class, where);
		Map<Long, XianShiBean> xianshiMap = new HashMap<Long,XianShiBean>();
		for (XianShiBean xianShiBean : list) {
			xianshiMap.put(xianShiBean.id, xianShiBean);
		}
		PlayerTime playerTime = HibernateUtil.find(PlayerTime.class,jz.id);
		for (XianshiControl xianshiControl : XianshiControlList) {
			//刷新数据
			if(xianshiControl.doneType == 1) continue; //不算冲级
//			refreshNormalXianShiInfo(jz.id, xianshiControl,xsBeanMap.get(xianshiControl.id + jz.id * 100),isYilingList,isKelingList); 
			int typeId=xianshiControl.id;
			long jzId=jz.id;
//			XianShiBean xsBean=HibernateUtil.find(XianShiBean.class, typeId+jzId*100);
			XianShiBean xsBean=xianshiMap.get(typeId+jzId*100);
			if(xsBean==null){
				xsBean=initXianShiInfoNew(jzId,typeId,playerTime);
			}
			XinShouXianShiInfo.Builder resp=XinShouXianShiInfo.newBuilder();
			XianshiControl	xControl=xsControlMap.get(typeId);
			getOtherXianShiInfo(jzId,jz.level,xControl,xsBean,resp,isYilingList,isKelingList);
			GrowLevel.Builder oneach = GrowLevel.newBuilder();
			List<HuoDongInfo> huoDongInfoList = resp.getHuodongList();
			for (HuoDongInfo huoDongInfo : huoDongInfoList) {
				if(huoDongInfo.getState() == 10 || huoDongInfo.getState() == 40){
					oneach.setId(huoDongInfo.getHuodongId());
					oneach.setDes(activityMap.get(huoDongInfo.getHuodongId()).desc);
					oneach.setFunctionid(activityMap.get(huoDongInfo.getHuodongId()).doneType);
					String[] awardsArr = huoDongInfo.getJiangli().split("#");
					List<String> awardList = Arrays.asList(awardsArr);
					for (String awardStr : awardList) {
						//0:900018:2000
						String[] awardarr = awardStr.split(":");
						Award.Builder award = Award.newBuilder();
						award.setItemType(Integer.parseInt(awardarr[0]));
						award.setItemId(Integer.parseInt(awardarr[1]));
						award.setItemNumber(Integer.parseInt(awardarr[2]));
						oneach.addAwardList(award);
					}
					int maxPro = getDoneCondition(activityMap.get(huoDongInfo.getHuodongId()).doneType,huoDongInfo.getHuodongId());
					oneach.setProcess(Math.min(resp.getBeizhu(),maxPro));
					oneach.setMaxProcess(maxPro);
					respNew.addLeveList(oneach);
					break;
				}
			}
		}
		ProtobufMsg pm = new ProtobufMsg();
		pm.id=PD.S_ACTIVITY_ACHIEVEMENT_INFO_RESP;
		pm.builder=respNew;
		session.write(pm);
	}
	
	/**
	 * @Description 领取成就奖励
	 */
	public void getAchievementAward(int id, IoSession session, Builder builder) {
		JunZhu jz =  JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
//			log.error("请求限时活动奖励出错：君主不存在");
			return;
		}
		ActivityGrowthFundRewardResp.Builder req = (ActivityGrowthFundRewardResp.Builder) builder;
		if(req==null){
			log.error("{}请求限时活动奖励出错：请求参数错误",jz.id);
			return;
		}
		long jzId=jz.id;
		int huodongId=req.getLevel();
		int bigId = activityMap.get(huodongId).BigId;
		ActivityGetRewardResp.Builder resp = ActivityGetRewardResp.newBuilder();
		//判断活动是否关闭
		if(isClosedById(huodongId)){
//			log.info("{}---{}活动暂时关闭",jzId,huodongId);
//			resp.setHuodongId(huodongId);
//			resp.setResult(30);
//			GainOtherAwardResp(session, resp);
			return;
		}
		XianshiHuodong xshd=activityMap.get(huodongId);
		if(xshd==null){
			log.error("{}请求限时活动{}奖励领取失败，未找到奖励配置",jzId,huodongId);
			return;
		}
		XianShiBean xsBean=HibernateUtil.find(XianShiBean.class,xshd.BigId+jzId*100);
//		if(xsBean==null){
//			log.error("{}请求限时活动{}奖励领取失败，未找到奖励配置",jzId,huodongId);
//			//君主此活动未开启 或活动已过期
//			resp.setHuodongId(huodongId);
//			resp.setResult(40);
//			GainOtherAwardResp(session, resp);
//			return;
//		}
		boolean isCan= DB.lexist((XIANSHIKELING_KEY + jzId), xshd.id + "");
		ProtobufMsg pm = new ProtobufMsg();
		if(isCan){
			String goods=xshd.Award;
			giveAward(goods, session, jz,true);
			//存储已领取奖励
			DB.rpush4YaBiao((XIANSHIYILING_KEY + jzId),huodongId+ "");
			//移除可领
			DB.lrem(XIANSHIKELING_KEY + jzId,0,huodongId+ "");
			resp.setResult(0);
			log.info("君主{}的其他限时活动之{}奖励， 领取成功",jzId,huodongId);
			// 领取【探宝】
			if(bigId == XianShiConstont.TANBAO_ONETIMES){
				EventMgr.addEvent(jzId,ED.get_achieve , new Object[] {jzId});
			}
			//判断限时活动是否可以完成关闭
			finishXianShiActivity(bigId, jzId, xsBean,session,huodongId);
			//Bag<BagGrid> bag = BagMgr.inst.loadBag(jz.id);
			//BagMgr.inst.sendBagInfo(session, bag);
		}else{ 
			//条件未达成
			resp.setResult(1);
		}
		pm.id=PD.S_ACTIVITY_ACHIEVEMENT_GET_RESP;
		pm.builder=resp;
		resp.build();
		session.write(pm);
		JunZhuMgr.inst.sendMainInfo(session,jz,false);
	}
	
	public int getDoneCondition(int doneType,int achId){
		String conditionStr = activityMap.get(achId).doneCondition;
		String[] conditionStrArr = conditionStr.split(":");
		int condition = 0;
		switch (doneType) {
		case TaskData.get_x_mibao:
			condition = Integer.parseInt(conditionStr);
			break;
		case TaskData.mibao_shengji_x:
			condition = Integer.parseInt(conditionStrArr[1]);
			break;
		case TaskData.N_quality_ok:
			condition = Integer.parseInt(conditionStrArr[0]);
			break;
		case TaskData.jingyingjixing:
			condition = Integer.parseInt(conditionStr);
			break;
		case TaskData.zhuangBei_x_qiangHua_N:
			condition = Integer.parseInt(conditionStrArr[1]);
			break;
		case TaskData.miabao_x_star_n:
			condition = Integer.parseInt(conditionStrArr[0]);
			break;
		case TaskData.tanbao_oneTimes:
			condition = Integer.parseInt(conditionStr);
			break;
		case TaskData.tanbao_tenTimes:
			condition = Integer.parseInt(conditionStr);
			break;
		case TaskData.get_miShu_pinZhi_y:
			condition = Integer.parseInt(conditionStr);
			break;
		case TaskData.chonglou_tiaozhan:
			condition = Integer.parseInt(conditionStr);
			break;
		default:
			break;
		}
		return condition;
	}
	
	/**
	 * 是否在活动页显示
	 * @param jz
	 * @return
	 */
	public boolean isShow(JunZhu jz){
		boolean isShow = false;
		List<XianshiControl> XianshiControlList =TempletService.listAll(XianshiControl.class.getSimpleName());
		List<String> isYilingList = DB.lgetList(XIANSHIYILING_KEY + jz.id);
		List<String> isKelingList = DB.lgetList(XIANSHIKELING_KEY + jz.id);
		for (XianshiControl xianshiControl : XianshiControlList) {
			if(xianshiControl.doneType == 1) continue; //冲级送礼
			int typeId=xianshiControl.id;
			long jzId=jz.id;
			XianShiBean xsBean=HibernateUtil.find(XianShiBean.class, typeId+jzId*100);
			if(xsBean==null){
				xsBean=initXianShiInfo(jzId,typeId);
			}
			XinShouXianShiInfo.Builder resp=XinShouXianShiInfo.newBuilder();
			XianshiControl	xControl=xsControlMap.get(typeId);
			getOtherXianShiInfo(jzId,jz.level,xControl,xsBean,resp,isYilingList,isKelingList);
			List<HuoDongInfo> huoDongInfoList = resp.getHuodongList();
			for (HuoDongInfo huoDongInfo : huoDongInfoList) {
				if(huoDongInfo.getState() == 10 || huoDongInfo.getState() == 40){
					isShow = true;
					break;
				}
			}
		}
		return isShow;
	}
	
	public void setReturnAward(JunZhu jz,ReturnAward.Builder resp,String goods){
		List<MiBaoDB> mibaoDBList = MibaoMgr.inst.getActiveMibaosFromDB(jz.id);
		Map<Integer, MiBaoDB> mibaoMap = new HashMap<Integer,MiBaoDB>();
		for (MiBaoDB miBaoDB : mibaoDBList) {
			mibaoMap.put(miBaoDB.tempId,miBaoDB);
		}
		String[] goodsArray = goods.split("#");
		for (MiBaoDB miBaoDB : mibaoDBList) {
			mibaoMap.put(miBaoDB.tempId,miBaoDB);
		}
		for (String g : goodsArray) {
			String[] ginfo = g.split(":");
			//奖励弹窗
			Award.Builder awardInfo = Award.newBuilder();
			awardInfo.setItemType(Integer.parseInt(ginfo[0]));
			awardInfo.setItemId(Integer.parseInt(ginfo[1]));
			awardInfo.setItemNumber(Integer.parseInt(ginfo[2]));
			//判断将魂
			if(Integer.parseInt(ginfo[0]) == 4){
				MiBao mibao = MibaoMgr.mibaoMap.get(awardInfo.getItemId());
				if(mibaoMap.containsKey(mibao.tempId)){ //存在秘宝转为碎片
					MibaoSuiPian suipian = MibaoMgr.inst.mibaoSuipianMap_2.get(mibao.suipianId);
					awardInfo.setPieceNumber(suipian.fenjieNum);
				}
			}
			resp.addAwardList(awardInfo);
		}
	}
	
	public void achievementRed(Event event){
		JunZhu jz = (JunZhu) event.param;
		IoSession session = AccountManager.sessionMap.get(jz.id);
		if(session == null){
			return;
		}
		long jzId = jz.id;
		// 加载活动列表
		List<XianshiControl> XianshiControlList =TempletService.listAll(XianshiControl.class.getSimpleName());
//		List<XianShiBean> xsBeanList = HibernateUtil.list(XianShiBean.class,"");
//		Map<Long,XianShiBean> xsBeanMap = new HashMap<Long,XianShiBean>();
//		for (XianShiBean xianShiBean : xsBeanList) {
//			xsBeanMap.put(xianShiBean.id, xianShiBean);
//		} 
		List<String> isYilingList = DB.lgetList(XIANSHIYILING_KEY + jzId);
		List<String> isKelingList = DB.lgetList(XIANSHIKELING_KEY + jzId);
		for (XianshiControl xianshiControl : XianshiControlList) {
			//刷新数据
			if(xianshiControl.doneType == 1) continue; //不算冲级
			boolean isNew = refreshNormalXianShiInfo(jzId, xianshiControl,null,isYilingList,isKelingList);
			if(isNew) break;
		}
	}
	
}
