package com.qx.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.yabiao.YaBiaoRobotProduceMgr;

public class YBrobotRefreshJob implements Job {
	private Logger log = LoggerFactory.getLogger(YBrobotRefreshJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("刷新押镖系统机器人马车列表开始");
		YaBiaoRobotProduceMgr.inst.refreshSysCartLevelList();
		log.info("刷新押镖系统机器人马车列表结束");
	}
}
   