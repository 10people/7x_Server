package com.qx.quartz.job;

import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.qx.huangye.shop.ShopMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.timeworker.FunctionID;

public class ShopRefreshJob implements Job {
	private Logger log = LoggerFactory.getLogger(ShopRefreshJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("商铺刷新， 发送红点");
		List<SessionUser> list = SessionManager.inst.getAllSessions();
		for (SessionUser user: list){
			IoSession session = user.session;
			if(session == null) continue;
			JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
			if (jz == null) continue;
			ShopMgr.inst.sendShopRed(jz, session, FunctionID.mysterious_shop);
			ShopMgr.inst.sendShopRed(jz, session, FunctionID.baizhan_shop);
			ShopMgr.inst.sendShopRed(jz, session, FunctionID.lianMeng_shop);
			ShopMgr.inst.sendShopRed(jz, session, FunctionID.huangYe_shop);
		}
	}
}
