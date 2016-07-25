package com.qx.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.event.ED;
import com.qx.event.EventMgr;

public class GuojiaDayRankResetJob implements Job {
	public Logger log = LoggerFactory.getLogger(GuojiaDayRankResetJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// 国家日榜重置
		EventMgr.addEvent(0,ED.GUOJIA_DAY_RANK_RESET, null);
		log.info("国家日榜开始重置");
	}
}
