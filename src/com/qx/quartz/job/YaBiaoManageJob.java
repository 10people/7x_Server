package com.qx.quartz.job;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.yabiao.YaBiaoHuoDongMgr;

public class YaBiaoManageJob implements Job {
	public Logger log = LoggerFactory.getLogger(YaBiaoManageJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("更新押镖活动标记开始");
		//每次执行变换一次开启标记
		YaBiaoHuoDongMgr.inst.fixOpenFlag();
		log.info("更新押镖活动标记结束{}，重置活动开启标记为{}",new Date(),YaBiaoHuoDongMgr.openFlag);
	}
}
   