package com.qx.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.alliancefight.BidMgr;

public class CityBidPriceRefreshJob implements Job {
	private Logger logger = LoggerFactory.getLogger(CityBidPriceRefreshJob.class);
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("开始,更新竞拍记录");
		BidMgr.inst.refreshBidPrice();
		logger.info("结束,更新竞拍记录");
	}

}
