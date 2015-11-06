package com.qx.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.qx.world.BroadcastMgr;

public class BroadcastJob  implements Job{

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		BroadcastMgr.inst.check();
	}

}
