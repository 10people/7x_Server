package com.qx.quartz.job;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.yabiao.YaBiaoHuoDongMgr;

public class YaBiaoJiaChengJob implements Job {
	private Logger log = LoggerFactory.getLogger(YaBiaoJiaChengJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("更新押镖活动收益比率开始");
		//每次执行变换一次开启标记
		YaBiaoHuoDongMgr.inst.fixOpenFlag();
		log.info("更新押镖活动收益比率结束{}，押镖活动收益比率为---{}",new Date(),YaBiaoHuoDongMgr.BUFF_PROFIT);
	}
}
   