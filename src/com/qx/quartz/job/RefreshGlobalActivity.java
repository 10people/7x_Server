package com.qx.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.activity.XianShiActivityMgr;

public class RefreshGlobalActivity implements Job {
	private Logger log = LoggerFactory.getLogger(RefreshGlobalActivity.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("重置服务器时间为准的活动状态开始");
		XianShiActivityMgr.instance.checkGlobalActivityState();
		log.info("重置服务器时间为准的活动状态结束");
	}
	
}
