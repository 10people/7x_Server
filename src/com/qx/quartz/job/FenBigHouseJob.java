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

public class FenBigHouseJob implements Job {
	public Logger log = LoggerFactory.getLogger(FenBigHouseJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		//分配大房子
		log.info("分配大房子开始");
		List<AllianceBean> list = HibernateUtil.list(AllianceBean.class, "");
		for (AllianceBean lm: list){  
			BigSwitch.inst.houseMgr.fenPeiBigHouse(lm.id);
		}
		log.info("分配大房子结束");
	}
}
   