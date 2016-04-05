package com.qx.quartz.job;

import java.util.Date;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xg.push.XG;

import com.manu.network.PD;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.qx.task.DailyTaskConstants;
import com.qx.task.DailyTaskMgr;

public class DailyTaskJob implements Job {
	private Logger log = LoggerFactory.getLogger(DailyTaskJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("开始刷新每日任务");
		List<SessionUser> list = SessionManager.inst.getAllSessions();
		for (SessionUser user: list){
			IoSession session = user.session;
			if(session != null){
				DailyTaskMgr.INSTANCE.taskListRequest(PD.C_DAILY_TASK_LIST_REQ, user.session);
			}
		}
		log.info("结束刷新每日任务");
	}
}
