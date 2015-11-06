package com.qx.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.ShouChong.AwardInfo;
import qxmobile.protobuf.ShouChong.GetShouchong;
import qxmobile.protobuf.ShouChong.ShouChongAward;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.DescId;
import com.manu.dynasty.template.HuoDong;
import com.manu.dynasty.template.Jiangli;
import com.manu.dynasty.template.ShouChong;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.award.AwardMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.purchase.PurchaseMgr;

/**
 * @author hejincheng
 * 
 */
public class ShouchongMgr {
	public Logger logger = LoggerFactory.getLogger(ShouchongMgr.class);
	public static ShouchongMgr instance;
	public static final short SUCCESS = 0;// 领取奖励成功
	public static final short FAILED = 1;// 领取奖励失败
	public static final short STATE_NULL = 0;// 没有首冲
	public static final short STATE_AWARD = 1;// 未领取奖励
	public static final short STATE_FINISHED = 2;// 完成首冲并领取奖励
	public static List<ShouChong> awardList = new ArrayList<ShouChong>();
	public static Date date = null;

	public ShouchongMgr() {
		instance = this;
		initData();
	}

	public void initData() {
		// 加载首冲奖励配置文件
		Jiangli jiangli = PurchaseMgr.inst.jiangliMap.get(20);
		String[] items = jiangli.getItem().split("#");
		for (String item : items) {
			String[] award = item.split(":");
			ShouChong shouChong = new ShouChong();
			shouChong.setAwardId(Integer.parseInt(award[1]));
			shouChong.setAwardNum(Integer.parseInt(award[2]));
			shouChong.setAwardType(Integer.parseInt(award[0]));
			awardList.add(shouChong);
		}
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
		// 添加首冲奖励描述
		DescId desc = ActivityMgr.descMap
				.get(Integer.parseInt(PurchaseMgr.inst.jiangliMap.get(20)
						.getDescription()));
		GetShouchong.Builder response = GetShouchong.newBuilder();
		response.setDesc(null == desc ? "" : desc.getDescription());// 不存在描述传空串
		// 添加首冲奖励列表
		for (ShouChong shouChong : awardList) {
			AwardInfo.Builder award = AwardInfo.newBuilder();
			award.setAwardNum(shouChong.getAwardNum());
			award.setAwardType(shouChong.getAwardType());
			award.setAwardId(shouChong.getAwardId());
			response.addAward(award);
		}
		writeByProtoMsg(session, PD.S_GET_SHOUCHONG_RESP, response);
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
		ShouchongInfo info = HibernateUtil.find(ShouchongInfo.class,
				"where junzhuId=" + junZhu.id + "");
		if (getShouChongState(info) == 2) {
			logger.info("君主{}已经领取奖励，不要重复领取",junZhu.id);
			return;
		}
		for (ShouChong award : awardList) {
			// 添加奖励到账户
			AwardTemp tmp = new AwardTemp();
			tmp.setItemType(award.getAwardType());
			tmp.setItemId(award.getAwardId());
			tmp.setItemNum(award.getAwardNum());
			AwardMgr.inst.giveReward(session, tmp, junZhu);// 添加到账户
			logger.info("{}领取到奖励 type {}, itemId {}, itemNum {}", junZhu.id,
					award.getAwardType(), award.getAwardId(),
					award.getAwardNum());
		}
		// 更改领取首冲奖励状态
//		ShouchongInfo info = HibernateUtil.find(ShouchongInfo.class,
//				"where junzhuId=" + junZhu.id + "");
		info.setHasAward(1);
		HibernateUtil.save(info);
		// 返回客户端
		ShouChongAward.Builder response = ShouChongAward.newBuilder();
		response.setResult(SUCCESS);
		writeByProtoMsg(session, PD.S_SHOUCHONG_AWARD_RESP, response);
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
		} else if (info.getHasAward() == 0) {// 没有领取奖励
			return STATE_AWARD;
		} else if (info.getHasAward() == 1) { // 完成首冲并已领取奖励
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
		info.setDate(new Date());
		info.setHasAward(0);
		info.setJunzhuId(junZhuId);
		HibernateUtil.insert(info);
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
}
