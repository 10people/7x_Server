package com.qx.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.ShouChong;
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
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;
import com.qx.timeworker.FunctionID;
import com.qx.timeworker.FunctionID4Open;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import qxmobile.protobuf.Activity.ActivityGetRewardResp;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.Explore.Award;
import qxmobile.protobuf.Explore.ExploreResp;

/**
 * @author hejincheng
 * 
 */
public class ShouchongMgr extends EventProc{
	public Logger logger = LoggerFactory.getLogger(ShouchongMgr.class);
	public static ShouchongMgr instance;
	public static final short SUCCESS = 0;// 领取奖励成功
	public static final short FAILED_1 = 1;// 领取奖励失败,未充值
	public static final short FAILED_2 = 2;// 领取奖励失败，已经领取
	public static final short STATE_NULL = 0;// 没有首冲
	public static final short STATE_AWARD = 1;// 未领取奖励
	public static final short STATE_FINISHED = 2;// 完成首冲并领取奖励
	public static List<String> awardList = new ArrayList<String>();
	public ShouchongMgr() {
		instance = this;
		initData();
	}
	public void initData() {
		List<ShouChong> shouChongSetting = TempletService.getInstance().listAll(ShouChong.class.getSimpleName());
		if(shouChongSetting == null){
			logger.error("cmd:{},没有首冲配置");
		}
		ShouChong shouChong = shouChongSetting.get(0);
		awardList.add(shouChong.award1);
		awardList.add(shouChong.award2);
		awardList.add(shouChong.award3);
	}

	/**
	 * 获取首冲信息
	 * 
	 * @param session
	 * @param builder
	 * @param cmd
	 */
	public void getShouchong(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			sendError(session, cmd, "未发现君主");
			logger.error("cmd:{},未发现君主", cmd);
			return;
		}
		//充值成功，判断首冲
		ShouchongInfo info = HibernateUtil.find(ShouchongInfo.class,junZhu.id);
		ExploreResp.Builder resp = ExploreResp.newBuilder();
		if (ShouchongMgr.instance.getShouChongState(info) == 0) {// 未完成首冲
			resp.setSuccess(STATE_NULL);
		}else if(ShouchongMgr.instance.getShouChongState(info) == 1){ //为领奖
			resp.setSuccess(STATE_AWARD);
		}else if(ShouchongMgr.instance.getShouChongState(info) == 2){ //完成领奖
			resp.setSuccess(STATE_FINISHED);
		}
		for(String awardStr : awardList){
			Award.Builder awardInfo = Award.newBuilder();
			String[] awardArr = awardStr.split(":");
			awardInfo.setItemType(Integer.parseInt(awardArr[0]));
			awardInfo.setItemId(Integer.parseInt(awardArr[1]));
			awardInfo.setItemNumber(Integer.parseInt(awardArr[2]));
			resp.addAwardsList(awardInfo);
		} 
		writeByProtoMsg(session, PD.ACTIVITY_FIRST_CHARGE_REWARD_RESP,resp);
	}

	/**
	 * 领取奖励
	 * 
	 * @param session
	 * @param builder
	 * @param cmd
	 */
	public void shouchongAward(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			sendError(session, cmd, "未发现君主");
			logger.error("cmd:{},未发现君主", cmd);
			return;
		}
		ShouchongInfo info = HibernateUtil.find(ShouchongInfo.class,junZhu.id);
		ActivityGetRewardResp.Builder resp = ActivityGetRewardResp.newBuilder();
		if (getShouChongState(info) == 0) {
			logger.info("君主{}没有首冲",junZhu.id);
			resp.setResult(FAILED_1); 
			writeByProtoMsg(session, PD.ACTIVITY_FIRST_CHARGE_GETREWARD_RESP,resp);
			return;
		}
		if (getShouChongState(info) == 2){
			logger.info("君主{}已经领取奖励，不要重复领取",junZhu.id);
			resp.setResult(FAILED_2); 
			writeByProtoMsg(session, PD.ACTIVITY_FIRST_CHARGE_GETREWARD_RESP,resp);
			return;
		}
		resp.setResult(SUCCESS);
		ExploreResp.Builder awardresp = ExploreResp.newBuilder();
		awardresp.setSuccess(0);
		for(String awardStr : awardList){
			Award.Builder awardInfo = Award.newBuilder();
			String[] awardArr = awardStr.split(":");
			awardInfo.setItemType(Integer.parseInt(awardArr[0]));
			awardInfo.setItemId(Integer.parseInt(awardArr[1]));
			awardInfo.setItemNumber(Integer.parseInt(awardArr[2]));
			awardresp.addAwardsList(awardInfo);
			AwardMgr.inst.giveReward(session,awardStr,junZhu);
		} 
		FunctionID4Open.pushOpenFunction(junZhu.id, session, -FunctionID.Shouchong);
		writeByProtoMsg(session, PD.S_USE_ITEM,awardresp);
		// 更改领取首冲奖励状态
		info.hasAward = 1;
		HibernateUtil.save(info);
		writeByProtoMsg(session, PD.ACTIVITY_FIRST_CHARGE_GETREWARD_RESP,resp);
	}

	/**
	 * 首冲活动是否完成
	 * 
	 * @param junzhuId
	 * @return 0-没有首冲，1-首冲完没有领取奖励，2-完成首冲并已领取奖励
	 */
	public int getShouChongState(ShouchongInfo info) {
//		ShouchongInfo info = HibernateUtil.find(ShouchongInfo.class,
//				"where junzhuId=" + junzhuId + "");
		if (null == info) {// 没有首冲记录
			return STATE_NULL;
		} else if (info.hasAward == 0) {// 没有领取奖励
			return STATE_AWARD;
		} else if (info.hasAward == 1) { // 完成首冲并已领取奖励
			return STATE_FINISHED;
		}
		return 0;
	}

	/**
	 * 完成首充，添加记录到DB
	 * 
	 * @param junZhuId
	 */
	public void finishShouchong(long junZhuId) {
		ShouchongInfo info = new ShouchongInfo();
		info.date = new Date();
		info.hasAward = 0;
		info.junzhuId = junZhuId;
		HibernateUtil.insert(info);
		Cache.caCheMap.get(ShouchongInfo.class).put(junZhuId,info);
		logger.info("玩家:{}完成首冲记录", junZhuId);
	}

	/**
	 * 发送指定协议号的消息
	 * 
	 * @param session
	 * @param prototype
	 * @param response
	 * @return
	 */
	public void writeByProtoMsg(IoSession session, int prototype,
			Builder response) {
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = prototype;
		msg.builder = response;
		logger.info("发送协议号为：{}", prototype);
		session.write(msg);
	}

	/**
	 * 发送错误消息
	 * 
	 * @param session
	 * @param cmd
	 * @param msg
	 */
	public void sendError(IoSession session, int cmd, String msg) {
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(cmd);
		test.setErrorDesc(msg);
		session.write(test.build());
	}
	
	public boolean isShow(JunZhu jz){
		boolean result = true;
		ShouchongInfo info = HibernateUtil.find(ShouchongInfo.class,jz.id);
		if(getShouChongState(info)== 2){ //领取后消失
			result = false;
		}
		return result;
	}
	@Override
	public void proc(Event event) {
		switch (event.id) {
		case ED.JUNZHU_LOGIN:
			JunZhu jz = (JunZhu) event.param;
			IoSession session = AccountManager.sessionMap.get(jz.id);
			if(session == null){
				return;
			}
			isShowRed(session, jz.id);
			break;
		case ED.activity_shouchong:
			IoSession session2 = (IoSession) event.param;
			if(session2 == null){
				return;
			}
			JunZhu jZhu = JunZhuMgr.inst.getJunZhu(session2);
			if(jZhu == null){
				return;
			}
			isShowRed(session2, jZhu.id);
			break;
		default:
				break;
		}
			
	}
	
	
	@Override
	public void doReg() {
		EventMgr.regist(ED.activity_shouchong, this);
		EventMgr.regist(ED.JUNZHU_LOGIN, this);
	}
	
	public void isShowRed(IoSession session,long jzId){
		//充值成功，判断首冲
		ShouchongInfo info = HibernateUtil.find(ShouchongInfo.class,jzId);
		if(session == null){
			return;
		}
		if(ShouchongMgr.instance.getShouChongState(info) == 1){ //为领奖
			FunctionID.pushCanShowRed(jzId, session,FunctionID.activity_shouchong);
		}
	}
}
