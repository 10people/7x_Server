package com.qx.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.alliancefight.BidMgr;

public class CityWarBidClearData implements Job {

	Logger logger = LoggerFactory.getLogger(CityWarBidClearData.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("开始,清理竞拍相关记录");
		BidMgr.inst.regularClearData();
		logger.info("结束,清理竞拍相关记录");
	}
}
