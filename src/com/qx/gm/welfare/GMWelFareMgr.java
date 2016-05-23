package com.qx.gm.welfare;

import java.io.PrintWriter;
import java.util.Date;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.VIP.RechargeReq;

import com.manu.dynasty.core.servlet.GMServlet;
import com.manu.network.PD;
import com.manu.network.SessionManager;
import com.qx.activity.ShouchongInfo;
import com.qx.activity.ShouchongMgr;
import com.qx.gm.message.BaseResp;
import com.qx.gm.message.DoSendTestCodeReq;
import com.qx.gm.message.DoSendWelfareReq;
import com.qx.gm.util.CodeUtil;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.vip.VipMgr;
import com.qx.vip.VipRechargeRecord;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

public class GMWelFareMgr {
	public static GMWelFareMgr inst;
	private Logger logger = LoggerFactory.getLogger(GMWelFareMgr.class);

	public GMWelFareMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		
	} 

	public void doSendTestCode(DoSendTestCodeReq request, PrintWriter writer) {
		BaseResp response = new BaseResp();

		if (!request.checkMd5()) {
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		int type = request.getType();
		int firm = request.getFirm();
		int zone = request.getZone();
		String roleid = request.getRoleid();
		String rolename = request.getRolename();

		/*
		 * 说明：
		 * 角色ID和角色名称需要对应，在审核通过后，发送的时候服务器需要对角色ID和角色名称做验证，如有不匹配情况，则发送不成功，并返回失败消息；
		 * 使用者：即账号使用者； 发起人：申请福利的人（一般为公司内部人员）
		 * 产品人员在输入相关账号信息后，必须点击验证信息，（验证：服务器、角色ID和角色名称是否能够对应
		 * ），只有所有的账号信息全部验证通过，才能点击申请，如有验证失败的情况，不能进行申请；
		 */
		if (roleid == null || roleid.length() == 0 || rolename == null
				|| rolename.length() == 0) {
			response.setCode(CodeUtil.PARAM_ERROR);
			GMServlet.write(response, writer);
			return;
		}
		JunZhu junZhu = HibernateUtil.find(JunZhu.class, Long.valueOf(roleid));
		if (junZhu == null) {
			response.setCode(CodeUtil.NONE_JUNZHU);
			GMServlet.write(response, writer);
			return;
		} else {
			if (junZhu.name.equals(rolename)) {
				response.setCode(CodeUtil.SUCCESS);
			} else {
				response.setCode(CodeUtil.NONE_JUNZHU);
			}
			GMServlet.write(response, writer);
			return;
		}
	}

	public void doSendWelfare(DoSendWelfareReq request, PrintWriter writer) {
		BaseResp response = new BaseResp();

		if (!request.checkMd5()) {
			response.setCode(CodeUtil.MD5_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		int type = request.getType();
		int firm = request.getFirm();
		int zone = request.getZone();
		String roleid = request.getRoleid();
		String rolename = request.getRolename();
		int gamegole = request.getGamegole();

		if (roleid == null || roleid.length() == 0 || rolename == null
				|| rolename.length() == 0) {
			response.setCode(CodeUtil.PARAM_ERROR);
			GMServlet.write(response, writer);
			return;
		}

		long junZhuId = Long.parseLong(roleid);
		JunZhu jz = HibernateUtil.find(JunZhu.class, junZhuId);

		int addYB = gamegole;
		int nowYB = jz.yuanBao + addYB;
		logger.info("玩家：{}，充值之前的元宝：{}， 充值之后的元宝：{}", junZhuId, jz.yuanBao, nowYB);
		YuanBaoMgr.inst.diff(jz, addYB, 0, 0, YBType.YB_VIP_CHONGZHI, "vip充值");

		// TODO 玩家充值vip经验和vip等级
		// int vipExp = vipInfo.vipExp + data.addVipExp;
		// log.info("玩家：{}，充值之前的vipExp：{}， 充值之后的vipExp：{}", jid, vipInfo.vipExp,
		// vipExp);
		// int vip = getVipLevel(vipInfo.level, vipExp); 
		// log.info("玩家：{}，充值之前的等级：{}， 充值之后的等级：{}", jid, jz.vipLevel, vip);
		// jz.vipLevel = vip;
		HibernateUtil.save(jz);
		// // 刷新首页玩家信息
		// JunZhuMgr.inst.sendMainInfo(session);
		// int sumRmb = vipInfo.sumAmount + amount;
		// logger.info("玩家：{}充值RMB，before：{}， after：{}", vipInfo.sumAmount,
		// sumRmb);
		// vipInfo.sumAmount = sumRmb;
		// vipInfo.level = vip;
		// vipInfo.vipExp = vipExp;
		// HibernateUtil.save(vipInfo);
		// VipRechargeRecord r = new VipRechargeRecord(jid, amount, new Date(),
		// sumRmb, vip, chongZhiId, addYB, yueKaValid);
		// HibernateUtil.save(r);
		// logger.info("玩家:{},充值人民币:{},成功，一次性增加了元宝:{}，现在玩家的元宝数:{}", junZhuId,
		// amount, addYB, nowYB);
		
		// 充值成功，判断首冲
		ShouchongInfo info = HibernateUtil.find(ShouchongInfo.class,
				"where junzhuId=" + junZhuId + "");
		if (ShouchongMgr.instance.getShouChongState(info) == 0) {// 未完成首冲
			ShouchongMgr.instance.finishShouchong(junZhuId);
		}
		
		response.setCode(CodeUtil.SUCCESS);
		GMServlet.write(response, writer);
	}
}
