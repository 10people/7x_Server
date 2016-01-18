package com.qx.quartz.job;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.template.YunbiaoTemp;
import com.manu.dynasty.util.DateUtils;
import com.qx.world.BroadcastMgr;
import com.qx.yabiao.YaBiaoHuoDongMgr;

public class YaBiaoJiaChengJob implements Job {
	private Logger log = LoggerFactory.getLogger(YaBiaoJiaChengJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("更新押镖活动--多收益参数开始");
		//每次执行变换一次多收益参数
		refreshMoreProfitState();
		log.info("更新押镖活动--多收益参数结束{}，多收益参数为{}",new Date(),YaBiaoHuoDongMgr.SHOUYI_PROFIT);
	}
	/**
	 * @Description 刷新押镖收益状态
	 */
	public void refreshMoreProfitState() {
		boolean buff2Profit1=	DateUtils.isInDeadline4Start(YunbiaoTemp.incomeAdd_startTime1, YunbiaoTemp.incomeAdd_endTime1);
		boolean buff2Profit2=  DateUtils.isInDeadline4Start(YunbiaoTemp.incomeAdd_startTime2, YunbiaoTemp.incomeAdd_endTime2);
		String template=YunbiaoTemp.yunbiao_start_broadcast;
		if(buff2Profit1||buff2Profit2){
			YaBiaoHuoDongMgr.SHOUYI_PROFIT=YunbiaoTemp.incomeAddPro;
			BroadcastMgr.inst.send(template);
		}else{
			template=YunbiaoTemp.yunbiao_end_broadcast;
			YaBiaoHuoDongMgr.SHOUYI_PROFIT=100;
			BroadcastMgr.inst.send(template);
		}
		YaBiaoHuoDongMgr.syncBroadExecutor.submit(new Runnable() {
			@Override
			public void run() {
				while(YaBiaoHuoDongMgr.SHOUYI_PROFIT>100) {
					try {
						String template=YunbiaoTemp.yunbiao_start_broadcast;
						BroadcastMgr.inst.send(template);
						Thread.sleep(YunbiaoTemp.yunbiao_start_broadcast_CD*1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		log.info("押镖收益比率为{}",YaBiaoHuoDongMgr.SHOUYI_PROFIT);
	}
}
   