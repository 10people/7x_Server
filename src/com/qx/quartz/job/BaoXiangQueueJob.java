package com.qx.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.qx.explore.treasure.ExploreTreasureMgr;

public class BaoXiangQueueJob implements Job{

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		ExploreTreasureMgr.inst.checkQueue();
	}

}
