package com.qx.quartz.job;

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
//		log.info("更新押镖活动--多收益参数开始");
		log.info("更新押镖活动--福利时段开始");
		//每次执行变换一次多收益参数
		YaBiaoHuoDongMgr.inst.refreshMoreProfitState();
		YaBiaoHuoDongMgr.inst.pushFuLiTimeState();
		log.info("更新押镖活动--福利时段结束--状态{}",YaBiaoHuoDongMgr.FULITIME_FLAG);
//		log.info("更新押镖活动--多收益参数结束{}，多收益参数为{}",new Date(),YaBiaoHuoDongMgr.SHOUYI_PROFIT);
	}

}
   