package com.qx.quartz.job;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.network.BigSwitch;
import com.qx.alliance.AllianceBean;
import com.qx.persistent.HibernateUtil;

public class BigHouseWorthReduceJob implements Job {
	private Logger log = LoggerFactory.getLogger(BigHouseWorthReduceJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("衰减大房子价值开始");
		//衰减大房子价值
		List<AllianceBean> list = HibernateUtil.list(AllianceBean.class, "");
		for (AllianceBean lm: list){
			BigSwitch.inst.houseMgr.reduceBigHouseWorth(lm.id);
		}
		log.info("衰减大房子价值结束");
	}
}
   