package com.qx.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.event.ED;
import com.qx.event.EventMgr;

public class LianMengBySWDayRankResetJob implements Job {
	public Logger log = LoggerFactory.getLogger(LianMengBySWDayRankResetJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// 联盟声望榜日榜重置
		EventMgr.addEvent(2,ED.LIANMENG_DAY_RANK_RESET, null);
		log.info("联盟声望日榜开始重置");
	}
}
