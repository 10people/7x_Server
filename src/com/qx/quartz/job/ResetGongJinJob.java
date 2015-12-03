package com.qx.quartz.job;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.ranking.RankingGongJinMgr;


public class ResetGongJinJob implements Job{
	private Logger log = LoggerFactory.getLogger(ResetGongJinJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("每日8点, 更新贡金排行");
		RankingGongJinMgr.inst.resetGongJinAt8_clock();
		log.info("更新贡金排行结束");
	}
}
