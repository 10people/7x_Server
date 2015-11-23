package com.qx.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.XianShi.GainAward;
import qxmobile.protobuf.XianShi.HuoDongInfo;
import qxmobile.protobuf.XianShi.OpenXianShi;
import qxmobile.protobuf.XianShi.OpenXianShiResp;
import qxmobile.protobuf.XianShi.ReturnAward;
import qxmobile.protobuf.XianShi.XinShouXSActivity;
import qxmobile.protobuf.XianShi.XinShouXianShiInfo;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.PveTemp;
import com.manu.dynasty.template.QiriQiandaoControl;
import com.manu.dynasty.template.XianshiControl;
import com.manu.dynasty.template.XianshiHuodong;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.manu.network.msg.ProtobufMsg;
import com.qx.award.AwardMgr;
import com.qx.bag.Bag;
import com.qx.bag.EquipGrid;
import com.qx.bag.EquipMgr;
import com.qx.battle.PveMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.junzhu.PlayerTime;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.task.GameTaskMgr;
import com.qx.task.TaskData;
import com.qx.world.Mission;

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
	public static final String XIANSHIKELING_KEY = "xianshikeling_" + GameServer.serverId;//存储可以完成未领奖的限时活动
	public static final String XIANSHICHAOSHI_KEY = "xianshichaoshi_" + GameServer.serverId;//存储可以完成未领奖的限时活动
	public static final String XIANSHI7DAY_KEY = "xianshi7Day_" + GameServer.serverId;//记录登录总天数
	public static List<Integer> xshdCloseList=new ArrayList<Integer>();
	public static boolean isShow=false;
	public XianShiActivityMgr() {
		instance = this;
		initData();
	}

	@SuppressWarnings("unchecked")
	public void initData() {
		Map<Integer, XianshiHuodong> activityMap = new HashMap<Integer, XianshiHuodong>();
		Map<Integer, List<XianshiHuodong>>		bigActivityMap = new HashMap<Integer, List<XianshiHuodong>>();
		Map<Integer, XianshiControl> xsControlMap=new HashMap<Integer, XianshiControl>();
		Map<Integer, QiriQiandaoControl>	xs7DaysControlMap= new HashMap<Integer, QiriQiandaoControl>();
		// 加载活动列表
		List<XianshiHuodong> xianshiActivityList =TempletService.listAll(XianshiHuodong.class.getSimpleName());
		int tmpId=0;
		List<XianshiHuodong> xsList=new ArrayList<XianshiHuodong>();
		for (XianshiHuodong xsHuoDong : xianshiActivityList) {
			if(tmpId==xsHuoDong.getBigId()){
				xsList.add(xsHuoDong);
			}else{
				xsList=new ArrayList<XianshiHuodong>();
				xsList.add(xsHuoDong);
				tmpId=xsHuoDong.getBigId();
			}

			bigActivityMap.put(xsHuoDong.getBigId(), xsList);
			activityMap.put(xsHuoDong.getId(), xsHuoDong);
		}
		XianShiActivityMgr.bigActivityMap=bigActivityMap;
		XianShiActivityMgr.activityMap=activityMap;
		List<XianshiControl> xsControlList = TempletService.listAll(XianshiControl.class.getSimpleName());
		for (XianshiControl xs : xsControlList) {
			xsControlMap.put(xs.getId(),xs);
		}
		XianShiActivityMgr.xsControlMap=xsControlMap;
		List<QiriQiandaoControl> xsDaysControlList = TempletService.listAll(QiriQiandaoControl.class.getSimpleName());
		for (QiriQiandaoControl xs : xsDaysControlList) {
			xs7DaysControlMap.put(xs.getId(),xs);
		}
		XianShiActivityMgr.xs7DaysControlMap=xs7DaysControlMap;
	}
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
			log.info("广播活动{}状态变为{}",huodongTypeId,state);
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
			log.info("广播活动{}状态变为{}",huodongTypeId,state);
			break;
		default:
			XianshiControl xs=xsControlMap.get(huodongTypeId);
			if(xs!=null){
				xianshi.setTypeId(xs.getId());
				xianshi.setName(xs.getName());
				xianshi.setShunxu(xs.getRank());
				xianshi.setState(state);
				xianshi.setIsNewAward(false);
				resp.addXianshi(xianshi);
				log.info("广播活动{}状态变为{}",huodongTypeId,state);
				broadcastXianShiHuoDong(resp);
			}else{
				log.error("错误活动类型--{}，广播活动失败",huodongTypeId);
			}
			break;
		}
	}
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
	 * @Description: 获取可开启活动
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getOpenXianShiHuoDong(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求限时活动出错：君主不存在");
			return;
		}
		long jzId=jz.id;
		OpenXianShiResp.Builder resp=OpenXianShiResp.newBuilder();
		List<XianShiBean> xianShiList=HibernateUtil.list(XianShiBean.class, "where junZhuId="+jzId);
		//判断当前君主哪些活动超过限时，自动关闭
		if(xianShiList!=null){
			for (XianShiBean xsBean : xianShiList) {
				if((xsBean.finishDate==null)&&(xsBean.bigId!=XianShiConstont.ZAIXIANLIBAO_TYPE)
						&&(xsBean.bigId!=XianShiConstont.QIRIQIANDAO_TYPE)){
					Date canjiaTime=xsBean.startDate;
					if(isChaoShi(xsBean.bigId, canjiaTime)){
						log.info("{}的活动{}超时，自动完成",jzId,xsBean.bigId);
						xsBean.finishDate=new Date();
						HibernateUtil.save(xsBean);
						DB.rpush4YaBiao((XIANSHIFINISH_KEY + jzId), xsBean.bigId+ "");
					}
				}
			}
		}
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
		//处理其他限时活动
		for (XianshiControl xs : xsControlMap.values()) {
			// 是否完成标记
			boolean isFinish = DB.lexist((XIANSHIFINISH_KEY + jzId), xs.getId()+ "");
			if (isFinish) {
				log.info("{}的限时活动{}-编码{}已完成",jzId,xs.getName(),xs.getId());
			} else {
				// 活动未完成且活动开启
				if (!xshdCloseList.contains(xs.getId())) {
					//2015年9月7日 改为Event事件触发刷新限时活动数据
//					if (true) {//每次请求显示活动的时候对所有活动 刷新活动状态 
//						XianShiBean xsBean = HibernateUtil.find(XianShiBean.class, xs.getId() + jzId * 100);
//						if (xsBean == null) {
//							xsBean = initXianShiInfo(jzId, xs.getId());
//						}
//						// 刷新活动状态
//						getOtherXianShiInfo(jzId, jz.level, xs, xsBean, null);
//					}
					OpenXianShi.Builder xshuodong = OpenXianShi.newBuilder();
					xshuodong.setTypeId(xs.getId());
					xshuodong.setName(xs.getName());
					xshuodong.setShunxu(xs.getRank());
					xshuodong.setState(10);
					boolean isNewAward = (isNewAward(xs.getId(), jzId) == 1) ? true: false;
					xshuodong.setIsNewAward(isNewAward);
					resp.addXianshi(xshuodong);
				}
			}
		}
		session.write(resp.build());
	}

	
	/**
	 * @Description: 根据大活动id判断大活动是否关闭
	 * @param bigId
	 * @return
	 */
	public boolean isClosedByBigId(int bigId){
		XianshiHuodong xs=activityMap.get(bigId);
		if(xs==null) return true;
		return xshdCloseList.contains(xs.getBigId());
	}

	/**
	 * @Description: 根据小活动id判断大活动是否关闭
	 * @param huodongId
	 * @return
	 */
	public boolean isClosedById(int huodongId){
		XianshiHuodong xs=activityMap.get(huodongId);
		if(xs==null) return true;
		return xshdCloseList.contains(xs.getBigId());
	}

	/**
	 * @Description: 请求新手首日/七日签到活动信息
	 * @param id
	 * @param builder
	 * @param session
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
	 * @Description: 获取首日在线活动数据
	 * @param jz
	 * @param builder
	 * @param session
	 */
	public void getShouRiInfo(JunZhu jz, Builder builder,IoSession session) {
		long jzId=jz.id;
		XianShiBean xsBean=HibernateUtil.find(XianShiBean.class, XianShiConstont.ZAIXIANLIBAO_TYPE+jzId*100);
		XinShouXianShiInfo.Builder resp=XinShouXianShiInfo.newBuilder();
		if(xsBean==null){
			xsBean=initXianShiInfo(jzId,XianShiConstont.ZAIXIANLIBAO_TYPE);
		}
		int bigId=xsBean.bigId;
		log.info("获取{}首日限时活动-{}数据", jzId,bigId);
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
			int huoDongId=xshd.getId();
			if(code==10){
				//剩下首日奖励不进行判断处理
				if(isShow){
					log.info("当前君主{}首日限时活动-{}数据-可领取状态:领取（未达到条件）不可领取 code-{}", jzId, huoDongId,code);
				}
				hd.setShengTime(0);
				hd.setState(40);
				hd.setJiangli(xshd.getAward());
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
				hd.setShengTime(shengyu);
				hd.setState(code);
				hd.setJiangli(xshd.getAward());
				hd.setHuodongId(huoDongId);
				resp.addHuodong(hd);
				continue;
			}
			int doneCondition=Integer.parseInt(xshd.getDoneCondition());
			boolean isKeling = DB.lexist((XIANSHIKELING_KEY + jzId), huoDongId + "");
			if(isKeling){
				//达成领奖条件，进入下一条循环
				if(isShow){
					log.info("当前君主{}首日限时活动-{}数据-可领取状态：可领取isKeling-{}", jzId, huoDongId,isKeling);
				}
				code = 10;
				hd.setShengTime(shengyu);
				hd.setState(code);
				hd.setJiangli(xshd.getAward());
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
			hd.setShengTime(shengyu);
			hd.setState(code);
			hd.setJiangli(xshd.getAward());
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
	 * @param jzId
	 * @param bigId
	 * @param xsList
	 * @return
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
		for (int i = 0; i < hdSize; i++) {
			XianshiHuodong xshd=xsList.get(i);
			//无限时 返回剩余时间-1
			int huoDongId=xshd.getId();
			boolean isYiling = DB.lexist((XIANSHIYILING_KEY + jzId), huoDongId+ "");
			if(isYiling){
				//达成领奖条件，进入下一条循环
				continue;
			}
			int doneCondition=Integer.parseInt(xshd.getDoneCondition());
			boolean isKeling = DB.lexist((XIANSHIKELING_KEY + jzId), huoDongId + "");
			if(isKeling){
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
	 * @param jzId
	 * @param bigId
	 * @param xsList
	 * @return
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
		//每一小条活动信息
		for (int i = 0; i < hdSize; i++) {
			XianshiHuodong xshd=xsList.get(i);
			int huoDongId=xshd.getId();
			boolean isYiling = DB.lexist((XIANSHIYILING_KEY + jzId), huoDongId+ "");
			if(isYiling){
				//达成领奖条件，进入下一条循环
				continue;
			}
			boolean isKeling = DB.lexist((XIANSHIKELING_KEY + jzId), huoDongId + "");
			if(isKeling){
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
	 * @Description: 初始化活动数据
	 * @param jzId
	 * @param bigId
	 * @return
	 */
	protected XianShiBean initXianShiInfo(Long jzId,int bigId) {
		// 获取数据库中是否有此记录，有的话什么也不做
		log.info("初始化{}限时活动（类型-{}）数据", jzId,bigId);
		PlayerTime playerTime = HibernateUtil.find(PlayerTime.class, jzId);
		Date startDate=null;
		if(playerTime!=null&&playerTime.getZhunchengTime()!=null){
			startDate=playerTime.getZhunchengTime();
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
	 * @Description: 获取其他限时活动数据
	 * @param jzId
	 * @param jzLevel
	 * @param xsControl
	 * @param xianshiBean
	 * @param resp
	 */
	public void getOtherXianShiInfo(long jzId,int jzLevel,	XianshiControl	xsControl,XianShiBean xianshiBean,XinShouXianShiInfo.Builder resp) {
		int bigId=xsControl.getId();
		log.info("获取{}限时活动-{}数据", jzId,bigId);
		List<XianshiHuodong> xsList=bigActivityMap.get(bigId);
		if(xsList==null){
			log.error("玩家{}获取活动{}数据出错，XianshiHuodong-List为空",jzId,bigId);
			return;
		}
		int remainTime=-1;
		Date canjiaTime =xianshiBean.startDate;
		int useDtime= (int) ((System.currentTimeMillis()-canjiaTime.getTime())/1000);
		if(useDtime>(xsControl.getCloseTime()*3600)){
			remainTime=(xsControl.getDelayTime()+xsControl.getCloseTime())*3600-useDtime;
			if(remainTime<0){
				log.info("玩家{}活动-{}剩余延迟领奖时间为-{},活动超时自动完成",jzId,bigId,remainTime);
				xianshiBean.finishDate=new Date();
				HibernateUtil.save(xianshiBean);
			}
		}
		//返回整个活动剩余领奖时间
		if(resp!=null){
			log.info("玩家{}活动-{}剩余延迟领奖时间为-{}",jzId,bigId,remainTime);
			resp.setRemainTime(remainTime);
		}
		int hdSize=xsList.size();
		//当前进度
		int condition=0;
		int beizhu=0;
		int doneType=xsControl.getDoneType();
		beizhu=condition=getOtherXianShiCondition(jzId,jzLevel, doneType,bigId);
		//每一小条活动信息
		//HuoDongInfo -State 奖励状态10：可领取 20：已领取 30：（超时未完成）不能领取 40:（未达到条件）不可领取
		for (int i = 0; i < hdSize; i++) {
			HuoDongInfo.Builder hd=HuoDongInfo.newBuilder();
			XianshiHuodong xshd=xsList.get(i);
			//判断小活动是否超时 
			int limitTime=xshd.getLimitTime();
			int huoDongId=xshd.getId();
			int shengyu=limitTime>useDtime?(limitTime-useDtime):0;
			hd.setShengTime(shengyu);

			//达到领奖条件判断
			int code=getOtherXianShiState(jzId, huoDongId, shengyu, condition, xshd);
			hd.setState(code);
			hd.setJiangli(xshd.getAward());
			hd.setHuodongId(huoDongId);
			if(resp!=null){
				resp.addHuodong(hd);
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
	 * @Description: 刷新其他限时活动数据
	 * @param jzId
	 * @param jzLevel
	 * @param xsControl
	 * @param xianshiBean
	 * @param resp
	 */
	public void refreshOtherXianShiInfo(long jzId,int jzLevel,	XianshiControl	xsControl,XianShiBean xianshiBean) {
		int bigId=xsControl.getId();
		log.info("刷新{}限时活动-{}数据", jzId,bigId);
		List<XianshiHuodong> xsList=bigActivityMap.get(bigId);
		if(xsList==null){
			log.error("玩家{}刷新活动{}数据出错，XianshiHuodong-List为空",jzId,bigId);
			return;
		}
		int remainTime=-1;
		Date canjiaTime =xianshiBean.startDate;
		int useDtime= (int) ((System.currentTimeMillis()-canjiaTime.getTime())/1000);
		if(useDtime>(xsControl.getCloseTime()*3600)){
			remainTime=(xsControl.getDelayTime()+xsControl.getCloseTime())*3600-useDtime;
			if(remainTime<0){
				log.info("玩家{}活动-{}剩余延迟领奖时间为-{},活动超时自动完成",jzId,bigId,remainTime);
				xianshiBean.finishDate=new Date();
				HibernateUtil.save(xianshiBean);
			}
		}
		int hdSize=xsList.size();
		//当前进度
		int condition=0;
		int doneType=xsControl.getDoneType();
		condition=getOtherXianShiCondition(jzId,jzLevel, doneType,bigId);
		//每一小条活动信息
		//HuoDongInfo -State 奖励状态10：可领取 20：已领取 30：（超时未完成）不能领取 40:（未达到条件）不可领取
		for (int i = 0; i < hdSize; i++) {
			XianshiHuodong xshd=xsList.get(i);
			//判断小活动是否超时 
			int limitTime=xshd.getLimitTime();
			int huoDongId=xshd.getId();
			int shengyu=limitTime>useDtime?(limitTime-useDtime):0;
			//达到领奖条件判断
			getOtherXianShiState(jzId, huoDongId, shengyu, condition, xshd);
		}
		boolean isNewAward = (isNewAward(xsControl.getId(), jzId) == 1) ? true: false;
		if(isNewAward){
			log.info("君主{}限时活动-{} 有新奖励，推送数据", jzId,xsControl.getId());
			broadIsNew(jzId, xsControl);
		}
	}
	//推送限时活动有新奖励
	public void broadIsNew(long jzId,	XianshiControl	xs) {
		OpenXianShiResp.Builder resp=OpenXianShiResp.newBuilder();
		OpenXianShi.Builder xshuodong = OpenXianShi.newBuilder();
		xshuodong.setTypeId(xs.getId());
		xshuodong.setName(xs.getName());
		xshuodong.setShunxu(xs.getRank());
		xshuodong.setState(10);
		boolean isNewAward = (isNewAward(xs.getId(), jzId) == 1) ? true: false;
		xshuodong.setIsNewAward(isNewAward);
		resp.addXianshi(xshuodong);
		SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
		if (su != null)
		{
			su.session.write(resp.build());
		}else{
			log.info("君主{}不在线，取消推送限时活动-{}新奖励数据", jzId,xs.getId());
		}
	}
	//获得限时活动状态
	public int getOtherXianShiState(long jzId,int huoDongId,int shengyu,int condition,XianshiHuodong xshd) {
		//达到完成条件 code 10：可领取 20：已领取  30超时 40 未达到条件
		boolean isKeling = DB.lexist((XIANSHIKELING_KEY + jzId), huoDongId + "");
		boolean isYiling = DB.lexist((XIANSHIYILING_KEY + jzId), huoDongId+ "");
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
		//超时判断
		boolean isChaoShi = DB.lexist((XIANSHICHAOSHI_KEY + jzId), huoDongId + "");
		if(isChaoShi){
			if(isShow){
				log.info("当前君主{}限时活动-{}数据-可领取状态--超时不可领", jzId,huoDongId);
			}
			return 30;
		}
		if(shengyu<=0){
			DB.rpush4YaBiao((XIANSHICHAOSHI_KEY+ jzId), huoDongId+ "");
			if(isShow){
				log.info("当前君主{}限时活动-{}数据-可领取状态--超时不可领", jzId,huoDongId);
			}
			return 30;
		}
		//未超时
		if(isCompleted(jzId,condition, xshd)){
			if(isShow){
				log.info("当前君主{}限时活动-{}-{}数据-可领取状态--可领", jzId,xshd.getBigId(),huoDongId);
			}
			return  10;
		}else{
			if(isShow){
				log.info("当前君主{}限时活动-{}-{}数据-可领取状态--未达到条件", jzId,xshd.getBigId(),huoDongId);
			}
			return  40;
		}
	}
	

	/**
	 * @Description: 判断首日 七日活动领奖条件是否达成
	 * @param jzId
	 * @param bean
	 * @param xshd
	 * @return
	 */
	protected int getShengyuTime4ShouRi(long jzId,int huoDongId,int doneCondition,int useDTime) {
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
	 * @Description: 判断首日 七日活动领奖条件是否达成
	 * @param jzId
	 * @param bean
	 * @param xshd
	 * @return
	 */
	protected boolean isCompleted47Days(long jzId,XianshiHuodong xshd) {
		boolean result=false;
		Integer doneCondition4QIRI=Integer.parseInt(xshd.getDoneCondition());
		String loginCount= DB.get(XIANSHI7DAY_KEY + jzId);
		loginCount=loginCount==null?"1":loginCount;
		Integer loginDay=Integer.parseInt(loginCount);
		log.info("君主七日奖励登录天数loginCount为{}",loginCount);
		result=(loginDay>=doneCondition4QIRI);
		//达成领奖条件，记录到未可领
		if(result){
			int huoDongId=xshd.getId();
			log.info("君主-{}的限时活动-{}状态变为可领取",jzId,huoDongId);
			DB.rpush4YaBiao((XIANSHIKELING_KEY + jzId), huoDongId+ "");
		}
		return result;
	}
	/**
	 * @Description: 判断活动领奖条件是否达成
	 * @param jzId
	 * @param bean
	 * @param xshd
	 * @return
	 */
	protected boolean isCompleted(long jzId,int condition,XianshiHuodong xshd) {
		boolean result=false;
		switch (xshd.getDoneType()) {
		/**内测开发以下4个限时活动*/
		case TaskData.junzhu_level_up://1
			Integer doneCondition4CHONGJI=Integer.parseInt(xshd.getDoneCondition());
			result= (condition>=doneCondition4CHONGJI);
			break;
		case TaskData.PVE_GUANQIA://2
			Integer doneCondition4Guoguan=Integer.parseInt(xshd.getDoneCondition());
			result= (condition>=doneCondition4Guoguan);
			break;
		case TaskData.jingyingjixing://44
			Integer doneCondition4JingYing=Integer.parseInt(xshd.getDoneCondition());
			result=(condition>=doneCondition4JingYing);
			break;
		case TaskData.N_quality_ok://40
			result =(condition>=xshd.getId());
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
			// 对应处理
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
			log.error(" {}判断活动条件是否达成出错，DoneType错误-{}",jzId,xshd.getDoneType());
			break;
		}
		//达成领奖条件，记录到未可领
		if(result){
			int huoDongId=xshd.getId();
			log.info("君主-{}的限时活动-{}状态变为可领取",jzId,huoDongId);
			DB.rpush4YaBiao((XIANSHIKELING_KEY + jzId), huoDongId+ "");
		}
		return result;
	}



	/**
	 * @Description: 进阶达人是否可领
	 * @param xingshu
	 * @param xshd
	 * @return
	 */
	protected boolean getJinJieDaRenCanGet(Bag<EquipGrid> equips,XianshiHuodong xshd) {
		String condition=xshd.getDoneCondition();
		boolean result=	isPinZhiOk(condition, equips);
		log.info("进阶达人任务---{}达成条件为{},判断结果为—--{}",xshd.getId(),condition,result);
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
	/**
	 * @Description: 判断活动奖励是否可领取
	 * @param jzId
	 * @param huoDongId
	 * @param useDTime
	 * @return
	 */
	protected int getAwardState(long jzId,int huoDongId) {
		boolean isUseD = DB.lexist((XIANSHIYILING_KEY + jzId), huoDongId+ "");
		//10未领取 已领取20 
		int result=isUseD?20:10;
		return result;
	}


	/**
	 * @Description:请求领取新手首日/七日签到活动奖励
	 * @param id
	 * @param builder
	 * @param session
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
	 * @param jz
	 * @param req
	 * @param session
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
		boolean isLingQu = DB.lexist((XIANSHIYILING_KEY + jzId), huodongId+ "");
		//判断是否领取过奖励
		if(isLingQu){
			log.info("{}新手首日之{}奖励已领取完毕",jzId,huodongId);
			resp.setHuodongId(huodongId);
			resp.setResult(20);
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
		String goods=xshd.getAward();
		//发放奖励
		giveAward( goods, session, jz);
		//存储已领取奖励
		DB.rpush4YaBiao((XIANSHIYILING_KEY + jzId),huodongId+ "");
		XianShiBean xsBean=HibernateUtil.find(XianShiBean.class, xshd.getBigId()+jzId*100);
		xsBean.startDate=new Date();
		boolean isFinish= isFinish(xshd.getBigId(),jzId);
		if(isFinish){///1.0版本 2015年9月15日1 改为 首日 七日一起开启    （当前活动领完奖励，进入七日活动--废弃）
			log.info("{}首日活动奖励领取完毕",jzId);
			//保存完成时间
			xsBean.finishDate=new Date();
			//存储到完成Redis
			DB.rpush4YaBiao((XIANSHIFINISH_KEY + jzId), xsBean.bigId+ "");
			//初始化七日活动废弃1.0版本 2015年9月15日1 改为 首日 七日一起开启 
//			initXianShiInfo(jz.id,XianShiConstont.QIRIQIANDAO_TYPE);

			//1.0版本 2015年9月15日1 改为关闭首日  不通知开启七日活动,
			OpenXianShiResp.Builder openResp=OpenXianShiResp.newBuilder();
			OpenXianShi.Builder xianshi4ShouRi=OpenXianShi.newBuilder();
			xianshi4ShouRi.setTypeId(XianShiConstont.ZAIXIANLIBAO_TYPE);
			xianshi4ShouRi.setName("在线礼包");
			xianshi4ShouRi.setShunxu(-1);
			xianshi4ShouRi.setState(20);
			xianshi4ShouRi.setIsNewAward(false);
			openResp.addXianshi(xianshi4ShouRi);
//			OpenXianShi.Builder xianshi4QiRi=OpenXianShi.newBuilder();
//			xianshi4QiRi.setTypeId(XianShiConstont.QIRIQIANDAO_TYPE);
//			xianshi4QiRi.setName("七日签到");
//			xianshi4QiRi.setShunxu(-1);
//			xianshi4QiRi.setState(10);
//			xianshi4QiRi.setIsNewAward(true);
//			openResp.addXianshi(xianshi4QiRi);
			session.write(openResp.build());
		}
		HibernateUtil.save(xsBean);
		resp.setHuodongId(huodongId);
		resp.setResult(10);
		session.write(resp.build());
	}
	
	
	/**
	 * @Description 判断君主的限时活动是否可完成
	 * @param bigId
	 * @param jzId
	 * @return
	 */
	public boolean isFinish(int bigId,long jzId){
		return (isNewAward(bigId,jzId)==2)?true:false;
	}
	
	/**
	 * @Description 完成并关闭君主的限时活动
	 * @param bigId
	 * @param jzId
	 * @param xsBean
	 * @return
	 */
	public XianShiBean finishXianShiActivity(int bigId,long jzId,XianShiBean xsBean,IoSession session){
		boolean isFinish=isFinish(bigId, jzId);
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
	//发放奖励
	protected void giveAward(String goods,IoSession session,JunZhu jz){
		String[] goodsArray = goods.split("#");
		//增加物品
		for (String g : goodsArray) {
			String[] ginfo = g.split(":");
			AwardTemp award = new AwardTemp();
			award.setItemType(Integer.parseInt(ginfo[0]));
			award.setItemId(Integer.parseInt(ginfo[1]));
			award.setItemNum(Integer.parseInt(ginfo[2]));
			AwardMgr.inst.giveReward(session, award, jz);
		}

	}

	/**
	 * @Description 判断是否有可领奖励 
	 *  0未找到配置 1有新奖励但没有领取过 2领完所有奖励 3未达到领奖条件
	 * @param bigId
	 * @param jzId
	 * @return
	 */
	protected int isNewAward(int bigId,long jzId){
		List<XianshiHuodong> huodongList=bigActivityMap.get(bigId);
		if(huodongList==null){
			log.error("isCanOpen--玩家{}获取活动{}数据出错，XianshiHuodong-List为空",jzId,bigId);
			return 0;
		}
		int hdSize=huodongList.size();
		for (int i = 0; (i < hdSize); i++) {
			XianshiHuodong xshd=huodongList.get(i);
			int huoDongId=xshd.getId();
			boolean isChaoShi = DB.lexist((XIANSHICHAOSHI_KEY + jzId), huoDongId + "");
			if(isChaoShi){
				continue;
			}
			//当前有奖励未领退出循环
			boolean isYiLing=DB.lexist((XIANSHIYILING_KEY + jzId), huoDongId+ "");
			boolean isKeLing = DB.lexist((XIANSHIKELING_KEY + jzId), huoDongId + "");
			if(!isYiLing&&isKeLing){//有新奖励但没有领取过
				return 1;
			}
			if((i==hdSize-1)){//领完所有奖励
				if(isYiLing||isChaoShi){
					return 2;
				}
			}
		}
		return 3;
	}

	/**
	 * @Description//活动是否超过完成时间，数据finishDate是否可更新
	 * @param bigId
	 * @param canjiaTime
	 * @return
	 */
	protected boolean isChaoShi(int bigId,Date canjiaTime){ 
		XianshiControl	huoDong=xsControlMap.get(bigId);
		if(huoDong==null){
			log.error("活动{}是否超过完成时间判断出错，未找到配置",bigId);
		}else{
			long closeTime= huoDong.getCloseTime()+huoDong.getDelayTime();
			long usedTime=(System.currentTimeMillis()-canjiaTime.getTime())/3600000;
			return ((usedTime-closeTime)>0L);
		}
		return false;
	}
	/**
	 * @Description: 获取七日在线活动数据
	 * @param jz
	 * @param builder
	 * @param session
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
		log.info("获取{}七日限时活动-{}数据", jzId,bigId);
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
			int huoDongId=xshd.getId();
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
			hd.setJiangli(xshd.getAward());
			hd.setHuodongId(huoDongId);
			resp.addHuodong(hd);
		}
		//首日七日活动没有备注返回-1
		resp.setBeizhu(-1);
		//返回整个活动剩余时间 首日和七日无限时返回-1
		resp.setRemainTime(-1);
		resp.setTypeId(bigId);
		session.write(resp.build());
	}

	/**
	 * @Description: 领取七日活动奖励
	 * @param jz
	 * @param req
	 * @param session
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
		boolean isLingQu = DB.lexist((XIANSHIYILING_KEY + jzId), huodongId+ "");
		//判断是否领取过奖励
		if(isLingQu){
			log.info("{}七日活动之{}奖励已领取完毕",jzId,huodongId);
			resp.setHuodongId(huodongId);
			resp.setResult(20);
			session.write(resp.build());
			return;
		}
		XianShiBean xsBean=HibernateUtil.find(XianShiBean.class, xshd.getBigId()+jzId*100);
		if(xsBean==null){
			//活动未开启
			resp.setHuodongId(huodongId);
			resp.setResult(40);
			session.write(resp.build());
			return;
		}
		boolean isKeLing = DB.lexist((XIANSHIKELING_KEY + jzId), huodongId + "");
		if(isKeLing){
			String goods=xshd.getAward();
			giveAward(goods, session, jz);
			//存储已领取奖励
			DB.rpush4YaBiao((XIANSHIYILING_KEY + jzId),huodongId+ "");
			log.info("{}-七日签到领取小活动--{}奖励",jzId,huodongId);
			//判断七日签到是否领完全部奖励 
			finishXianShiActivity(XianShiConstont.QIRIQIANDAO_TYPE, jzId, xsBean,session);
			resp.setHuodongId(huodongId);
			resp.setResult(10);
			session.write(resp.build());
			return;
		}else{
			resp.setHuodongId(huodongId);
			resp.setResult(50);
			session.write(resp.build());
		}
	}

	/**
	 * @Description: 获取其他限时活动信息
	 * @param id
	 * @param builder
	 * @param session
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
		getOtherXianShiInfo(jzId,jz.level,xControl,xsBean, resp);
		ProtobufMsg pm = new ProtobufMsg();
		pm.id=PD.S_XIANSHI_INFO_RESP;
		pm.builder=resp;
		session.write(pm);
	}

	public void getOtherXianShiArard(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
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
		boolean isChaoShi = DB.lexist((XIANSHICHAOSHI_KEY + jzId), huodongId + "");
		if(isChaoShi){
			log.info("君主{}的活动之{}奖励已超过完成时间",jzId,huodongId);
			resp.setHuodongId(huodongId);
			resp.setResult(40);
			GainOtherAwardResp(session, resp);
			return;
		}
		boolean isLingQu = DB.lexist((XIANSHIYILING_KEY + jzId), huodongId+ "");
		//判断是否领取过奖励
		if(isLingQu){
			log.info("君主{}的活动之{}奖励已领取完毕",jzId,huodongId);
			resp.setHuodongId(huodongId);
			resp.setResult(20);
			GainOtherAwardResp(session, resp);
			return;
		}
		int condition=0;
		condition=getOtherXianShiCondition(jzId,jz.level, xshd.getDoneType(),bigId);
		log.info("当前君主{}领取限时活动-{}奖励，达成条件--{}",jzId,huodongId,condition);
		XianShiBean xsBean=HibernateUtil.find(XianShiBean.class,xshd.getBigId()+jzId*100);
		if(xsBean==null||(xsBean!=null&&xsBean.finishDate!=null)){
			//君主此活动未开启 或活动已过期
			resp.setHuodongId(huodongId);
			resp.setResult(40);
			GainOtherAwardResp(session, resp);
			return;
		}
		boolean isCan= DB.lexist((XIANSHIKELING_KEY + jzId), xshd.getId() + "");
		if(isCan){
			//达到领奖条件，判断限时
			//			if(limitTime>0&&limitTime<useDtime){
			//				//超过限时
			//				log.info("君主{}的活动之{},超过完成时间",jzId,huodongId);
			//				resp.setHuodongId(huodongId);
			//				resp.setResult(40);
			//				DB.rpush4YaBiao((XIANSHICHAOSHI_KEY+ jzId), huodongId+ "");
			//				GainOtherAwardResp(session, resp);
			//			}else{
			String goods=xshd.getAward();
			giveAward(goods, session, jz);
			//存储已领取奖励
			DB.rpush4YaBiao((XIANSHIYILING_KEY + jzId),huodongId+ "");
			resp.setHuodongId(huodongId);
			resp.setResult(10);
			GainOtherAwardResp(session, resp);
			log.info("君主{}的其他限时活动之{}奖励， 领取成功",jzId,huodongId);
			//判断限时活动是否可以完成关闭
			finishXianShiActivity(bigId, jzId, xsBean,session);
			//			}
		}else{ 
			//条件未达成
			resp.setHuodongId(huodongId);
			resp.setResult(50);
			GainOtherAwardResp(session, resp);
		}

	}

	/**
	 * @Description 根据doneType类型获取jzId的君主限时活动达到的进度
	 * @param jzId
	 * @param doneType
	 * @return
	 */
	protected int getOtherXianShiCondition(long jzId,int jzlevel,int doneType,int bigId){
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
			condition=BigSwitch.pveGuanQiaMgr.getGuanQiaSumStart(jzId);
			log.info("计算星级{}的星级--结果为{}",jzId,condition);
			break;
		case  TaskData.N_quality_ok:
			List<XianshiHuodong> xsList=bigActivityMap.get(bigId);
			Bag<EquipGrid> equips=EquipMgr.inst.loadEquips(jzId);
			int xsSize=xsList.size();
			for (int i = 0; i < xsSize; i++) {
				XianshiHuodong xshd=xsList.get(i);
				int huoDongId=xshd.getId();
				boolean isKeling = DB.lexist((XIANSHIKELING_KEY + jzId), huoDongId + "");
				if(isKeling){
					condition=huoDongId;
					continue;
				}
				boolean isChaoShi = DB.lexist((XIANSHICHAOSHI_KEY + jzId), huoDongId + "");
				if(isChaoShi){
					continue;
				}
				//如果当前任务达成继续判断下一个任务
				if(getJinJieDaRenCanGet(equips,xshd)){
						condition=huoDongId;
						DB.rpush4YaBiao((XIANSHIKELING_KEY + jzId), huoDongId+ "");
						log.info("君主-{}的进阶达人限时活动-{}状态变为可领取",jzId,huoDongId);
						continue;
				}else{
					//跳出循环
					i=xsSize+1;
				}
			}
			//计算进阶总数
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
			//对应操作
			break;
		case TaskData.mibao_shengji_x://20
			//对应操作
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
		default:
			break;
		}
		return condition;
	}
	/**
	 * @Description:请求其他限时活动的推送消息操作
	 * @param session
	 * @param resp
	 */
	public void  GainOtherAwardResp( IoSession session,ReturnAward.Builder resp) {
		ProtobufMsg pm = new ProtobufMsg();
		pm.id=PD.S_XIANSHI_AWARD_RESP;
		pm.builder=resp;
		session.write(pm);
	}

	@Override
	public void proc(Event event) {

		if (event.param == null){
			return;
		}
		XianshiControl	xs=null;
		long jzId=0L;
		XianShiBean xsBean=null;
		Object[] obs =null;
		switch (event.id) {
		//君主升级 冲级送礼
		case ED.junzhu_level_up:
			// 活动未完成且活动开启
			obs = (Object[])event.param;
			jzId= (Long)obs[0];
			xs=xsControlMap.get(1501000);
			Integer jzlevel = (Integer)obs[1];
			log.info("君主-{}--冲级送礼限时活动数据刷新",jzId);
			xsBean = HibernateUtil.find(XianShiBean.class, xs.getId() + jzId * 100);
			if (xsBean == null) {
				xsBean = initXianShiInfo(jzId, xs.getId());
			}
			//限时活动不是首日和七日活动是进行完成超时判断
			if((xsBean.finishDate==null)&&(xsBean.bigId!=XianShiConstont.ZAIXIANLIBAO_TYPE)
					&&(xsBean.bigId!=XianShiConstont.QIRIQIANDAO_TYPE)){
				Date canjiaTime=xsBean.startDate;
				if(isChaoShi(xsBean.bigId, canjiaTime)){
					log.info("{}的活动{}超时，自动完成",jzId,xsBean.bigId);
					xsBean.finishDate=new Date();
					HibernateUtil.save(xsBean);
					DB.rpush4YaBiao((XIANSHIFINISH_KEY + jzId), xsBean.bigId+ "");
				}
			}
			if (!xshdCloseList.contains(xs.getId())) {
				if(xsBean.finishDate==null){
					//限时活动未完成 刷新活动状态
					refreshOtherXianShiInfo(jzId, jzlevel, xs, xsBean);
				}
			}
			break;
			//进阶达人
		case ED.JINJIE_ONE_GONG:
			// 活动未完成且活动开启
			xs=xsControlMap.get(1540000);
			obs = (Object[])event.param;
			jzId= (Long) obs[0];
			log.info("君主-{}--进阶达人限时活动数据刷新",jzId);
			xsBean = HibernateUtil.find(XianShiBean.class, xs.getId() + jzId * 100);
			if (xsBean == null) {
				xsBean = initXianShiInfo(jzId, xs.getId());
			}
			//限时活动不是首日和七日活动是进行完成超时判断
			if((xsBean.finishDate==null)&&(xsBean.bigId!=XianShiConstont.ZAIXIANLIBAO_TYPE)
					&&(xsBean.bigId!=XianShiConstont.QIRIQIANDAO_TYPE)){
				Date canjiaTime=xsBean.startDate;
				if(isChaoShi(xsBean.bigId, canjiaTime)){
					log.info("{}的活动{}超时，自动完成",jzId,xsBean.bigId);
					xsBean.finishDate=new Date();
					HibernateUtil.save(xsBean);
					DB.rpush4YaBiao((XIANSHIFINISH_KEY + jzId), xsBean.bigId+ "");
				}
			}
			if (!xshdCloseList.contains(xs.getId())) {
				if(xsBean.finishDate==null){
					// 刷新活动状态
					refreshOtherXianShiInfo(jzId, 0, xs, xsBean);
				}
			}
			break;
			//过关斩将
		case ED.PVE_GUANQIA:
			// 活动未完成且活动开启
			obs = (Object[])event.param;
			xs=xsControlMap.get(1502000);
			jzId = (Long) obs[0];
			log.info("君主-{}--过关斩将限时活动数据刷新",jzId);
			xsBean = HibernateUtil.find(XianShiBean.class, xs.getId() + jzId * 100);
			if (xsBean == null) {
				xsBean = initXianShiInfo(jzId, xs.getId());
			}
			//限时活动不是首日和七日活动是进行完成超时判断
			if((xsBean.finishDate==null)&&(xsBean.bigId!=XianShiConstont.ZAIXIANLIBAO_TYPE)
					&&(xsBean.bigId!=XianShiConstont.QIRIQIANDAO_TYPE)){
				Date canjiaTime=xsBean.startDate;
				if(isChaoShi(xsBean.bigId, canjiaTime)){
					log.info("{}的活动{}超时，自动完成",jzId,xsBean.bigId);
					xsBean.finishDate=new Date();
					HibernateUtil.save(xsBean);
					DB.rpush4YaBiao((XIANSHIFINISH_KEY + jzId), xsBean.bigId+ "");
				}
			}
			if (!xshdCloseList.contains(xs.getId())) {
				if(xsBean.finishDate==null){
					// 刷新活动状态
					refreshOtherXianShiInfo(jzId, 0, xs, xsBean);
				}
			}
			break;	
			//精英集星
		case ED.JINGYINGJIXING:
			// 活动未完成且活动开启
			xs=xsControlMap.get(1544000);
			log.info("君主-{}精英集星限时活动数据刷新",jzId);
			xsBean = HibernateUtil.find(XianShiBean.class, xs.getId() + jzId * 100);
			if (xsBean == null) {
				xsBean = initXianShiInfo(jzId, xs.getId());
			}
			jzId = (Long) event.param;
			//限时活动不是首日和七日活动是进行完成超时判断
			if((xsBean.finishDate==null)&&(xsBean.bigId!=XianShiConstont.ZAIXIANLIBAO_TYPE)
					&&(xsBean.bigId!=XianShiConstont.QIRIQIANDAO_TYPE)){
				Date canjiaTime=xsBean.startDate;
				if(isChaoShi(xsBean.bigId, canjiaTime)){
					log.info("{}的活动{}超时，自动完成",jzId,xsBean.bigId);
					xsBean.finishDate=new Date();
					HibernateUtil.save(xsBean);
					DB.rpush4YaBiao((XIANSHIFINISH_KEY + jzId), xsBean.bigId+ "");
				}
			}
			if (!xshdCloseList.contains(xs.getId())) {
				if(xsBean.finishDate==null){
					// 刷新活动状态
					log.info("刷新活动状态---{}的活动{} ",jzId,xsBean.bigId);
					refreshOtherXianShiInfo(jzId, 0, xs, xsBean);
				}
			}
			break;	
		}
	}


	/**
	 * @Description: 保存登录天数
	 * @param jzId
	 */
	public void updateLoginDate(long jzId) {
		String loginCount= DB.get(XIANSHI7DAY_KEY + jzId);
		log.info("君主{}登录天数更新，开始为 第{}天",jzId);
		if(loginCount==null){
			log.info("君主{} 第一次登录，登录天数第一天",jzId);
			DB.set((XIANSHI7DAY_KEY + jzId),""+1);
			return;
		}
		PlayerTime playerTime = HibernateUtil.find(PlayerTime.class, jzId);
		if(playerTime==null){
			log.error("君主{}---playerTime为空",jzId);
			return;
		}
		Date lastLogInTime = playerTime.getLoginTime();
		log.info("君主{}上次登录时间—--{}",jzId,lastLogInTime);
		// change 20150901
		if((lastLogInTime!=null)&&(DateUtils.isTimeToReset(lastLogInTime, CanShu.REFRESHTIME_PURCHASE))){
			Integer dayCount=Integer.parseInt(loginCount);
			dayCount++;
			DB.set((XIANSHI7DAY_KEY + jzId),""+dayCount);
			log.info("君主{} 登录--第{}天",jzId,dayCount);
		}
		log.info("君主{} 登录第{}天 ,更新完成",jzId,DB.get(XIANSHI7DAY_KEY + jzId));
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.ACC_LOGIN, this);
		EventMgr.regist(ED.junzhu_level_up, this);
		EventMgr.regist(ED.JINJIE_ONE_GONG, this);
		EventMgr.regist(ED.PVE_GUANQIA, this);
		EventMgr.regist(ED.JINGYINGJIXING, this);
	}

}
