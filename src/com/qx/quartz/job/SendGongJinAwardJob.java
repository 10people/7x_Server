package com.qx.quartz.job;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.pvp.LveDuoMgr;


public class SendGongJinAwardJob implements Job{
	private Logger log = LoggerFactory.getLogger(SendGongJinAwardJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
//		log.info("每日22点, 总结贡金排行榜，发送贡金个人奖励");
//		LveDuoMgr.inst.sendGongJinDayAward();
//		log.info("每日22点，发送贡金个人排行奖励完");
		
		log.info("每日22点, 总结贡金联盟排行榜，发送贡金联盟奖励");
		LveDuoMgr.inst.sendGongJinAllianceAward();
		log.info("每日22点，发送贡金联盟排行奖励完成");
	}
}
