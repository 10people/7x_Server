package com.qx.quartz.job;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.event.ED;
import com.qx.event.EventMgr;

public class GuojiaWeekRankResetJob implements Job {
	public Logger log = LoggerFactory.getLogger(GuojiaWeekRankResetJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// 国家周榜重置
		EventMgr.addEvent(1,ED.GUOJIA_WEEK_RANK_RESET, null);
		log.info("国家周榜开始重置");
	}
}
