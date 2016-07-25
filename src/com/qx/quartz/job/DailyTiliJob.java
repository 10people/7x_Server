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
import com.qx.activity.StrengthGetMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;

public class DailyTiliJob implements Job {

	public Logger log = LoggerFactory.getLogger(DailyTiliJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("开始刷新体力");
		List<SessionUser> list = SessionManager.inst.getAllSessions();
		for (SessionUser user: list){
			IoSession session = user.session;
			if(session != null){
				JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
				StrengthGetMgr.inst.isShowRed(session,jz);
				StrengthGetMgr.inst.strengthGetInfo(0, session,null);
			}
		}
		log.info("结束刷新每日体力");
	}

}
