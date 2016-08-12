package com.qx.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.Function;
import com.manu.dynasty.template.TiLi;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.award.AwardMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.timeworker.FunctionID;
import com.qx.vip.VipMgr;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

import qxmobile.protobuf.Activity.ActivityCardGetRewardReq;
import qxmobile.protobuf.Activity.ActivityGetRewardResp;
import qxmobile.protobuf.Activity.ActivityGetStrengthResp;
import qxmobile.protobuf.Activity.PeriodInfo;
import qxmobile.protobuf.Explore.Award;
import qxmobile.protobuf.Explore.ExploreResp;
import qxmobile.protobuf.NReportProtos.NReport;

public class StrengthGetMgr extends EventProc{
	public Logger logger = LoggerFactory.getLogger(StrengthGetMgr.class);
	public static StrengthGetMgr inst;
	public static String STRENGTH_REFRESH_TIME = "4:00";
	public static String STRENGTH_GET_TIME1_START = "12:00";
	public static String STRENGTH_GET_TIME1_END = "14:00";
	public static String STRENGTH_GET_TIME2_START = "18:00";
	public static String STRENGTH_GET_TIME2_END = "20:00";
	public static String STRENGTH_GET_TIME3_START = "21:00";
	public static String STRENGTH_GET_TIME3_END = "24:00";
	
	public static final int TYPE_1 = 1;
	public static final int TYPE_2 = 2;
	public static final int TYPE_3 = 3;
	
	public Map<Integer,TiLi> tiliMap = null; 

	public StrengthGetMgr() {
		inst = this;
		init();
	}

	public void init() {
		List<TiLi> lTiLis = TempletService.getInstance().listAll(TiLi.class.getSimpleName());
		tiliMap = new HashMap<Integer,TiLi>();
		for (TiLi tiLi : lTiLis) {
			switch (tiLi.ID) {
			case TYPE_1:
				STRENGTH_GET_TIME1_START = tiLi.start;
				STRENGTH_GET_TIME1_END = tiLi.end;
				break;
			case TYPE_2:
				STRENGTH_GET_TIME2_START = tiLi.start;
				STRENGTH_GET_TIME2_END = tiLi.end;
				break;
			case TYPE_3:
				STRENGTH_GET_TIME3_START = tiLi.start;
				STRENGTH_GET_TIME3_END = tiLi.end;
				break;
			default:
				break;
			}
			tiliMap.put(tiLi.ID,tiLi);
		}
	}

	/**
	 * 获取赠送体力详情
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void strengthGetInfo(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			logger.error("未找到君主信息");
			return;
		}
		ActivityGetStrengthResp.Builder resp = ActivityGetStrengthResp.newBuilder();
		PeriodInfo.Builder pBuilder = PeriodInfo.newBuilder();
		int[] stateArr = getState(session, jz);
		if(stateArr == null) return;
		//阶段1
		pBuilder.setId(TYPE_1);
		pBuilder.setTime(STRENGTH_GET_TIME1_START + "~" + STRENGTH_GET_TIME1_END);
		pBuilder.setNumber(tiliMap.get(TYPE_1).tili);
		pBuilder.setStatus(stateArr[0]);//1-可以领
		pBuilder.setCost(tiliMap.get(TYPE_1).cost);
		resp.addPeriodList(pBuilder);
		//阶段2
		pBuilder.setId(TYPE_2);
		pBuilder.setTime(STRENGTH_GET_TIME2_START + "~" + STRENGTH_GET_TIME2_END);
		pBuilder.setNumber(tiliMap.get(TYPE_2).tili);
		pBuilder.setStatus(stateArr[1]);//1-可以领
		pBuilder.setCost(tiliMap.get(TYPE_2).cost);
		resp.addPeriodList(pBuilder);
		//阶段3
		pBuilder.setId(TYPE_3);
		pBuilder.setTime(STRENGTH_GET_TIME3_START + "~" + STRENGTH_GET_TIME3_END);
		pBuilder.setNumber(tiliMap.get(TYPE_3).tili);
		pBuilder.setStatus(stateArr[2]);//1-可以领
		pBuilder.setCost(tiliMap.get(TYPE_3).cost);
		resp.addPeriodList(pBuilder);
		msgSend(PD.ACTIVITY_STRENGTH_INFO_RESP,session,resp);
	}

	/**
	 * 领取体力
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void strengthGetReward(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			logger.error("未找到君主信息");
			return;
		}
		ActivityCardGetRewardReq.Builder req = (ActivityCardGetRewardReq.Builder)builder;
		int type = req.getType();
		ActivityGetRewardResp.Builder resp = ActivityGetRewardResp.newBuilder();
		//resp result 0-领奖成功，1-失败，时间未到，2-失败，已经领取
		//校验时间段
		Calendar calendar = Calendar.getInstance();
		long now = calendar.getTimeInMillis();
		String timeStartStr = "";
		String timeEndStr = "";
		if(type == TYPE_1){
			timeStartStr = STRENGTH_GET_TIME1_START;
			timeEndStr = STRENGTH_GET_TIME1_END;
		}else if(type == TYPE_2){
			timeStartStr = STRENGTH_GET_TIME2_START;
			timeEndStr = STRENGTH_GET_TIME2_END;
		}else if(type == TYPE_3){
			timeStartStr = STRENGTH_GET_TIME3_START;
			timeEndStr = STRENGTH_GET_TIME3_END;
		}else{
			logger.error("未找到体力奖励类型");
			return;
		}
		//时间是否
		String[] startTimeArr = timeStartStr.split(":");
		calendar.set(Calendar.HOUR_OF_DAY,Integer.parseInt(startTimeArr[0]));
		calendar.set(Calendar.MINUTE,Integer.parseInt(startTimeArr[1]));
		if(now < calendar.getTimeInMillis()){
			resp.setResult(1);
			msgSend(PD.ACTIVITY_STRENGTH_GET_RESP,session,resp);
			return;
		}
		//校验是否领取
		StrengthGetBean sGetBean = HibernateUtil.find(StrengthGetBean.class, "where jzId=" + jz.id + " and type=" + type + " and getTime>'" + dayStart() + "' and getTime<'" + dayEnd() + "'");
		if(isGetReward(sGetBean)){
			resp.setResult(2);
			msgSend(PD.ACTIVITY_STRENGTH_GET_RESP,session,resp);
			return;
		}else{
			//校验是否是补领
			String[] endTimeArr = timeEndStr.split(":");
			calendar.set(Calendar.HOUR_OF_DAY,Integer.parseInt(endTimeArr[0]));
			calendar.set(Calendar.MINUTE,Integer.parseInt(endTimeArr[1]));
			if(now > calendar.getTimeInMillis()){ //补领 需要校验VIP等级，元宝
				boolean canBuling = VipMgr.INSTANCE.isVipPermit(VipMgr.tili_buling,jz.vipLevel);
				if(!canBuling){
					resp.setResult(3); //vip等级不足
					msgSend(PD.ACTIVITY_STRENGTH_GET_RESP,session,resp);
					return;
				}
				int costYuanbao = tiliMap.get(type).cost;
				if (jz.yuanBao < costYuanbao) {
					logger.error("体力补领失败，君主:{}背包内的元宝有:{}不足:{}个", jz.id, jz.yuanBao,costYuanbao);
					resp.setResult(4);//元宝不足
					msgSend(PD.ACTIVITY_STRENGTH_GET_RESP,session,resp);
					return;
				}
				//扣元宝
				YuanBaoMgr.inst.diff(jz,-costYuanbao,0,0,YBType.ACTIVITY_TILIBULING,"补领体力");
				JunZhuMgr.inst.sendMainInfo(session,jz); //通知前端
			}
			resp.setResult(0);
			msgSend(PD.ACTIVITY_STRENGTH_GET_RESP,session,resp);
			//更新数据库
			if(sGetBean == null){
				sGetBean = new StrengthGetBean();
				sGetBean.jzId =jz.id;
				sGetBean.type = type;
				sGetBean.getTime = new Date();
				HibernateUtil.insert(sGetBean);
			}else{
				sGetBean.getTime = new Date();
				HibernateUtil.update(sGetBean);
			}
		}
		//君主加体力
//		jz.tiLi += CanShu.MEIRI_DINGSHIZENGSONG_TILI;
		//需求变更，领取赠送体力不再可以超过999上限 2016-06-08
		JunZhuMgr.inst.updateTiLi(jz,tiliMap.get(type).tili, "每日赠送体力");
		HibernateUtil.update(jz);
		JunZhuMgr.inst.sendMainInfo(session,jz);
		//奖励弹窗消息
		ExploreResp.Builder awardresp = ExploreResp.newBuilder();
		awardresp.setSuccess(0);
		Award.Builder awardInfo = Award.newBuilder();
 		awardInfo.setItemType(0);
		awardInfo.setItemId(AwardMgr.ITEM_TILI_ID);
		awardInfo.setItemNumber(tiliMap.get(type).tili);
		awardresp.addAwardsList(awardInfo);
		msgSend(PD.S_USE_ITEM,session,awardresp);
		isShowRed(session, jz); //红点
	}
	
	/**
	 * day起始时间
	 * @return Date
	 */ 
	public Date dayStart(){
		return DateUtils.getDayStart(new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
	}
	
	/**
	 * day 结束时间
	 * @return Date
	 */
	public Date dayEnd(){
		return DateUtils.getDayEnd(new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
	}
	
	/**
	 * 消息发送
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void msgSend(int id,IoSession session,Builder builder){
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = id;
		msg.builder = builder;
		session.write(msg);
	}
	
	/**
	 * @Description 校验体力是否领取
	 * @return true-已领取
	 */
	public boolean isGetReward(StrengthGetBean info) {
		boolean result=false;
		if(info != null&&info.getTime != null){
			result = !DateUtils.isTimeToReset(info.getTime,CanShu.REFRESHTIME_PURCHASE);
		}
		return result;
	}

	@Override
	public void proc(Event event) {
		switch (event.id) {
		case ED.JUNZHU_LOGIN:{
			JunZhu jz = (JunZhu) event.param;
			IoSession session = AccountManager.sessionMap.get(jz.id);
			if(session == null){
				return;
			}
			isShowRed(session,jz);
		}
		break;
//		case ED.REFRESH_TIME_WORK:{
//			IoSession session = (IoSession) event.param;
//			if(session == null){
//				return;
//			}
//			JunZhu jZhu = JunZhuMgr.inst.getJunZhu(session);
//			if(jZhu == null){
//				return;
//			}
//			isShowRed(session, jZhu);
//		}
//		break;
		default:
				break;
		}
	}

	@Override
	public void doReg() {
		EventMgr.regist(ED.JUNZHU_LOGIN,this);
//		EventMgr.regist(ED.REFRESH_TIME_WORK,this);
	}
	
	public void isShowRed(IoSession session,JunZhu jz){
		if(jz == null) return;
		int[] stateArr = getState(session, jz);
		if(stateArr == null) return; 
		if(DateUtils.isInDeadline4Start(STRENGTH_GET_TIME1_START,STRENGTH_GET_TIME1_END) && stateArr[0] == 1) { //1阶段
			FunctionID.pushCanShowRed(jz.id, session, FunctionID.activity_tili);
		}else if (DateUtils.isInDeadline4Start(STRENGTH_GET_TIME2_START, STRENGTH_GET_TIME2_END) && stateArr[1] == 1) { //2阶段
			FunctionID.pushCanShowRed(jz.id, session, FunctionID.activity_tili);
		}else if(DateUtils.isInDeadline4Start(STRENGTH_GET_TIME3_START,STRENGTH_GET_TIME3_END) && stateArr[2] == 1) { //3阶段
			FunctionID.pushCanShowRed(jz.id, session, FunctionID.activity_tili);
		}else{
			FunctionID.pushCanShowRed(jz.id, session, -FunctionID.activity_tili);
		}
	}
	
	public int[] getState(IoSession session,JunZhu jz){
		if (jz == null) {
			logger.error("未找到君主信息");
			return null;
		}
		List<StrengthGetBean> getlist = HibernateUtil.list(StrengthGetBean.class, "where jzId=" + jz.id + " and getTime>'" + dayStart() + "' and getTime<'" + dayEnd() + "'");
		Map<Integer,StrengthGetBean> getMap = new HashMap<Integer,StrengthGetBean>();
		for (StrengthGetBean strengthGetBean : getlist) {
			getMap.put(strengthGetBean.type,strengthGetBean);
		}
		int status1 = 1; //1-可领取
		int status2= 1; //1-可领取
		int status3 = 1; //1-可领取
		boolean rewardStatus1 = isGetReward(getMap.get(TYPE_1));
		boolean rewardStatus2 = isGetReward(getMap.get(TYPE_2));
		boolean rewardStatus3 = isGetReward(getMap.get(TYPE_3));
		if(DateUtils.isInDeadline4Start(STRENGTH_REFRESH_TIME, STRENGTH_GET_TIME1_START)) { //刷新时间~1阶段
		}else if(DateUtils.isInDeadline4Start(STRENGTH_GET_TIME1_START,STRENGTH_GET_TIME1_END)) { //1阶段
			//第一阶段
			if(rewardStatus1){//第一阶段已经领取
				status1 = 2;
			}
		}else if(DateUtils.isInDeadline4Start(STRENGTH_GET_TIME1_END,STRENGTH_GET_TIME2_START)){ //1阶段-2阶段
			//第一阶段
			if(rewardStatus1){//第一阶段已经领取
				status1 = 2;
			}else{
				status1 = 3;
			}
		}else if (DateUtils.isInDeadline4Start(STRENGTH_GET_TIME2_START, STRENGTH_GET_TIME2_END)) { //2阶段
			 //第一阶段
			if(rewardStatus1){ //第一阶段已经领取
				status1 = 2;
			}else{
				status1 = 3; //3-时间过，未领取
			}
			//第二阶段
			if(rewardStatus2){ //第二阶段已经领取
				status2 = 2;
			}
			
		}else if(DateUtils.isInDeadline4Start(STRENGTH_GET_TIME2_END,STRENGTH_GET_TIME3_START)){ //2阶段-3阶段
			 //第一阶段
			if(rewardStatus1){ //第一阶段已经领取
				status1 = 2;
			}else{
				status1 = 3; //3-时间过，未领取
			}
			//第二阶段
			if(rewardStatus2){ //第二阶段未领取
				status2 = 2;
			}else{
				status2 = 3;
			}
		}else if(DateUtils.isInDeadline4Start(STRENGTH_GET_TIME3_START,STRENGTH_GET_TIME3_END)) { //3阶段
			 //第一阶段
			if(rewardStatus1){ //第一阶段已经领取
				status1 = 2;
			}else{
				status1 = 3; //3-时间过，未领取
			}
			//第二阶段
			if(rewardStatus2){ //第二阶段已经领取
				status2 = 2;
			}else{
				status2 = 3; //3-时间过，未领取
			}
			//第三阶段
			if(rewardStatus3){ //第三阶段已经领取
				status3 = 2;
			}
		}else{
			 //第一阶段
			if(rewardStatus1){ //第一阶段已经领取
				status1 = 2;
			}else{
				status1 = 3; //3-时间过，未领取
			}
			//第二阶段
			if(rewardStatus2){ //第二阶段已经领取
				status2 = 2;
			}else{
				status2 = 3; //3-时间过，未领取
			}
			//第三阶段
			if(rewardStatus3){ //第三阶段已经领取
				status3 = 2;
			}else{
				status3 = 3; //3-时间过，未领取
			}
		}
		return new int[]{status1,status2,status3};
	}
}
