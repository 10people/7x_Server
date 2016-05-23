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
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.award.AwardMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;

import qxmobile.protobuf.Activity.ActivityCardGetRewardReq;
import qxmobile.protobuf.Activity.ActivityGetRewardResp;
import qxmobile.protobuf.Activity.ActivityGetStrengthResp;
import qxmobile.protobuf.Activity.PeriodInfo;
import qxmobile.protobuf.Explore.Award;
import qxmobile.protobuf.Explore.ExploreResp;

public class StrengthGetMgr {
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

	public StrengthGetMgr() {
		inst = this;
		init();
	}

	public void init() {
		
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
			if(rewardStatus1){//第一阶段未领取
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
		//阶段1
		pBuilder.setId(TYPE_1);
		pBuilder.setTime(STRENGTH_GET_TIME1_START + "~" + STRENGTH_GET_TIME1_END);
		pBuilder.setNumber(CanShu.MEIRI_DINGSHIZENGSONG_TILI);
		pBuilder.setStatus(status1);//1-可以领
		resp.addPeriodList(pBuilder);
		//阶段2
		pBuilder.setId(TYPE_2);
		pBuilder.setTime(STRENGTH_GET_TIME2_START + "~" + STRENGTH_GET_TIME2_END);
		pBuilder.setNumber(CanShu.MEIRI_DINGSHIZENGSONG_TILI);
		pBuilder.setStatus(status2);//1-可以领
		resp.addPeriodList(pBuilder);
		//阶段3
		pBuilder.setId(TYPE_3);
		pBuilder.setTime(STRENGTH_GET_TIME3_START + "~" + STRENGTH_GET_TIME3_END);
		pBuilder.setNumber(CanShu.MEIRI_DINGSHIZENGSONG_TILI);
		pBuilder.setStatus(status3);//1-可以领
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
		if(type == TYPE_1){
			if(!DateUtils.isInDeadline4Start(STRENGTH_GET_TIME1_START,STRENGTH_GET_TIME1_END)){
				resp.setResult(1);
				msgSend(PD.ACTIVITY_STRENGTH_GET_RESP,session,resp);
				return;
			}
		}else if(type == TYPE_2){
			if(!DateUtils.isInDeadline4Start(STRENGTH_GET_TIME2_START,STRENGTH_GET_TIME2_END)){
				resp.setResult(1);
				msgSend(PD.ACTIVITY_STRENGTH_GET_RESP,session,resp);
				return;
			}
		}else if(type == TYPE_3){
			if(!DateUtils.isInDeadline4Start(STRENGTH_GET_TIME3_START,STRENGTH_GET_TIME3_END)){
				resp.setResult(1);
				msgSend(PD.ACTIVITY_STRENGTH_GET_RESP,session,resp);
				return;
			}
		}else{
			logger.error("未找到体力奖励类型");
			return;
		}
		//校验是否领取
		StrengthGetBean sGetBean = HibernateUtil.find(StrengthGetBean.class, "where jzId=" + jz.id + " and type=" + type + " and getTime>'" + dayStart() + "' and getTime<'" + dayEnd() + "'");
		if(isGetReward(sGetBean)){
			resp.setResult(2);
			msgSend(PD.ACTIVITY_STRENGTH_GET_RESP,session,resp);
			return;
		}else{
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
		jz.tiLi += CanShu.MEIRI_DINGSHIZENGSONG_TILI;
		HibernateUtil.update(jz);
		JunZhuMgr.inst.sendMainInfo(session,jz);
		//奖励弹窗消息
		ExploreResp.Builder awardresp = ExploreResp.newBuilder();
		awardresp.setSuccess(0);
		Award.Builder awardInfo = Award.newBuilder();
 		awardInfo.setItemType(0);
		awardInfo.setItemId(AwardMgr.ITEM_TILI_ID);
		awardInfo.setItemNumber(CanShu.MEIRI_DINGSHIZENGSONG_TILI);
		awardresp.addAwardsList(awardInfo);
		msgSend(PD.S_USE_ITEM,session,awardresp);
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
	 * @Description 校验月卡是否领取
	 * @return true-已领取
	 */
	public boolean isGetReward(StrengthGetBean info) {
		boolean result=false;
		if(info != null&&info.getTime != null){
			result = !DateUtils.isTimeToReset(info.getTime,CanShu.REFRESHTIME_PURCHASE);
		}
		return result;
	}
}
