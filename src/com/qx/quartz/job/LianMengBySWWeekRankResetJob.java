package com.qx.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.persistent.HibernateUtil;

public class LianMengBySWWeekRankResetJob implements Job {
	private Logger log = LoggerFactory.getLogger(LianMengBySWWeekRankResetJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// 联盟声望都变为0，然后重置redis
		HibernateUtil.setAllAllianceReputation();
		// 联盟声望榜日榜重置
		EventMgr.addEvent(ED.LIANMENG_WEEK_RANK_RESET, null);
		log.info("联盟声望周榜开始重置");
	}
}
