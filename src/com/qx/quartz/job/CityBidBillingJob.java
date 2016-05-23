package com.qx.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.alliancefight.BidMgr;

public class CityBidBillingJob implements Job{
	Logger logger =  LoggerFactory.getLogger(CityBidBillingJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("开始，竞拍结算");
		BidMgr.inst.bidJieSuan();
		logger.info("结束，竞拍结算");
	}

}
