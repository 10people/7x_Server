package com.qx.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.Activity.ActivityFunctionResp;
import qxmobile.protobuf.Activity.ActivityInfo;
import qxmobile.protobuf.Activity.GetActivityListResp;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.DescId;
import com.manu.dynasty.template.FunctionOpen;
import com.manu.dynasty.template.HuoDong;
import com.manu.dynasty.template.QianDaoDesc;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.timeworker.FunctionID;
import com.qx.timeworker.FunctionID4Open;
import com.qx.vip.VipRechargeRecord;
import com.qx.yuanbao.BillHist;

public class ActivityMgr extends EventProc{
	public Logger logger = LoggerFactory.getLogger(ActivityMgr.class);
	public static ActivityMgr instance;
	public static Map<Integer, HuoDong> activityMap = new HashMap<Integer, HuoDong>();
	public static Map<Integer, DescId> descMap = new HashMap<Integer, DescId>();
	public static final int ACTIVITY_NOT_FINISH = 0;// 活动未完成
	public static final int ACTIVITY_FINISH = 1;// 活动已完成
	public static final int ACT_QIANDAO = 1;// 签到活动
	public static final int ACT_SHOUCHONG = 2;// 首冲活动
	public static final int ACT_XIANSHI = 3;// 限时活动
	public static final int ACT_OTHER = 4;// 敬请期待

	public ActivityMgr() {
		instance = this;
		initData();
	}

	public void initData() {
		// 加载活动列表
		List<HuoDong> activityList = TempletService.listAll(HuoDong.class
				.getSimpleName());
		for (HuoDong huoDong : activityList) {
			activityMap.put(huoDong.id, huoDong);
		}
		// 加载活动描述
		List<DescId> descList = TempletService.listAll(DescId.class
				.getSimpleName());
		for (DescId desc : descList) {
			descMap.put(desc.descId, desc);
		}
	}

	/**
	 * 获取活动列表
	 * 
	 * @param cmd
	 * @param session
	 * @param builder
	 */
	public void getActivityList(int cmd, IoSession session) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			sendError(session, cmd, "未发现君主");
			logger.error("cmd:{},未发现君主", cmd);
			return;
		}
		List<FunctionOpen> list = TempletService.getInstance().listAll(FunctionOpen.class.getSimpleName());
		ActivityFunctionResp.Builder resp = ActivityFunctionResp.newBuilder();
		for (FunctionOpen functionOpen : list) {
			if(functionOpen.MenuID != 14) continue; 
			//校验活动是否开启
			if(isActivityShow(junZhu,functionOpen.id)){
				resp.addFunctionList(functionOpen.id);
			}
		}
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.ACTIVITY_FUNCTIONLIST_INFO_RESP;
		msg.builder = resp;
		session.write(msg);
//		GetActivityListResp.Builder response = GetActivityListResp.newBuilder();
//		for (int key : activityMap.keySet()) {
//			HuoDong huoDong = activityMap.get(key);
//			if (huoDong.getHuoDongStatus() == 1) {// 活动处于开启状态
//				ActivityInfo.Builder ac = ActivityInfo.newBuilder();
//				ac.setId(huoDong.getId());
//				switch (huoDong.getId()) {
//				case ACT_QIANDAO:// 每日登录奖励
//					QiandaoInfo qiandaoInfo = HibernateUtil.find(QiandaoInfo.class,	 junZhu.id );
//					if (QiandaoMgr.instance.hasAlreadyQiandao(qiandaoInfo)) {
//						if (QiandaoMgr.instance.canBuQian(junZhu.id,qiandaoInfo)) {// 今天是否可以补签
//							ac.setState(ACTIVITY_NOT_FINISH);
//						} else {
//							ac.setState(ACTIVITY_FINISH);
//						}
//					} else {
//						ac.setState(ACTIVITY_NOT_FINISH);
//					}
//					Calendar calendar = Calendar.getInstance();
//					Date tmpDate = calendar.getTime();
//					if(QiandaoMgr.DATE_DEBUG){
//						tmpDate = new Date(QiandaoMgr.debugDate.getTime());
//					}
//					if(tmpDate.getHours()<QiandaoMgr.RESET_TIME){
//						tmpDate.setDate(tmpDate.getDate()-1);
//					}
//					int month = tmpDate.getMonth() + 1;
//					QianDaoDesc qdDesc = QiandaoMgr.instance.qianDaoDescMap.get(month);
//					if(qdDesc == null) {
//						ac.setAwardDesc(huoDong.getAwardDesc());
//						ac.setDesc(huoDong.getDesc());
//						logger.error("未找到月份为:{}的签到活动描述配置", month);
//					} else {
//						ac.setAwardDesc(qdDesc.getAwardDesc());
//						ac.setDesc(qdDesc.getDesc());
//					}
//					response.addActivityList(ac);
//					
//					break;
//				case ACT_SHOUCHONG:// 首冲
//					ac.setDesc(huoDong.getDesc());
//					ac.setAwardDesc(huoDong.getAwardDesc());
//					ShouchongInfo info = HibernateUtil.find(ShouchongInfo.class,"where junzhuId=" + junZhu.id + "");
//					if (ShouchongMgr.instance.getShouChongState(info) == ShouchongMgr.STATE_AWARD) {
//						ac.setState(ACTIVITY_FINISH); // 前台显示领取
//						response.addActivityList(ac);
//					} else if (ShouchongMgr.instance.getShouChongState(info) == ShouchongMgr.STATE_NULL) {
//						ac.setState(ACTIVITY_NOT_FINISH);// 前台显示查看
//						response.addActivityList(ac);
//					}
//					break;
//				case ACT_XIANSHI:// 敬请期待
//					ac.setDesc(huoDong.getDesc());
//					ac.setAwardDesc(huoDong.getAwardDesc());
//					ac.setState(ACTIVITY_NOT_FINISH);
//					response.addActivityList(ac);
//					break;
//				case ACT_OTHER:// 敬请期待
//					break;
//				default:
//					response.addActivityList(ac);
//					break;
//				}
//			}
//		}
//		// 当现在开放的活动数量为1个或2个时，敬请期待面板出现；多于等于3个时，面板隐藏
//		if (response.getActivityListCount() < 3) {
//			HuoDong huoDong = activityMap.get(ACT_OTHER);
//			ActivityInfo.Builder ac = ActivityInfo.newBuilder();
//			ac.setId(huoDong.getId());
//			ac.setDesc(huoDong.getDesc());
//			ac.setAwardDesc(huoDong.getAwardDesc());
//			ac.setState(ACTIVITY_NOT_FINISH);
//			response.addActivityList(ac);
//		}
//		writeByProtoMsg(session, PD.S_GET_ACTIVITYLIST_RESP, response);
	}

	/** 
	 * @Title: pushQiandaoAvailable 
	 * @Description: 签到推送
	 * @param junZhuId
	 * @param session
	 * @return void
	 * @throws 
	 */
	public void pushQiandaoAvailable(long junZhuId,IoSession session){
		QiandaoInfo qiandaoInfo = HibernateUtil.find(QiandaoInfo.class,	junZhuId);
		if (QiandaoMgr.instance.hasAlreadyQiandao(qiandaoInfo)) {
			if (QiandaoMgr.instance.canBuQian(junZhuId,qiandaoInfo)) {// 今天是否可以补签
				FunctionID.pushCanShowRed(junZhuId, session, FunctionID.Qiandao);
			}
		} else {
			FunctionID.pushCanShowRed(junZhuId, session, FunctionID.Qiandao);
		}
	}
	
	/** 
	 * @Title: pushShouchongAvailable 
	 * @Description: 首冲推送
	 * @param junZhuId
	 * @param session
	 * @return void
	 * @throws 
	 */
	public void pushShouchongAvailable(long junZhuId,IoSession session){
		int count = HibernateUtil.getColumnValueMaxOnWhere(BillHist.class, "save_amt", "where jzId="+junZhuId);
		int count2 = HibernateUtil.getColumnValueMaxOnWhere(VipRechargeRecord.class, "sumAmount", "where accId="+junZhuId);
		if (count <= 0 && count2<=0) {
			FunctionID4Open.pushOpenFunction(junZhuId, session, FunctionID.Shouchong);
		} else {
			FunctionID4Open.pushOpenFunction(junZhuId, session, -FunctionID.Shouchong);
		}
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
	
	@Override
	public void proc(Event event) {
		switch (event.id) {
		case ED.REFRESH_TIME_WORK:
//			logger.info("定时刷新活动列表");
			IoSession session=(IoSession) event.param;
			if(session==null){
				logger.error("定时刷新活动列表错误，session为null");
				break;
			}
			JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
			if(jz==null){
				logger.error("定时刷新活动列表错误，JunZhu为null");
				break;
			}
			pushQiandaoAvailable(jz.id,session);
			pushShouchongAvailable(jz.id,session);
			break;
		default:
			break;
		}
	}

	@Override
	public void doReg() {
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
	}
	
	/**
	 * 活动是否显示，部分活动领取完关闭
	 * @param id 活动ID（functionOpen.xml id）
	 * @return
	 */
	public boolean isActivityShow(JunZhu jz,int id){
		boolean isShow = true;
		switch (id) {
		case 1422://首冲大礼
			isShow = ShouchongMgr.instance.isShow(jz);
			break;
		case 1394: //成长基金
			isShow = GrowthFundMgr.inst.isShow(jz);
			break;
		case 600200: //等级奖励
			isShow = LevelUpGiftMgr.inst.isShow(jz);
			break;
		case 144: //成就
			isShow = XianShiActivityMgr.instance.isShow(jz);
		default:
			break;
		}
		return isShow;
	}

}
