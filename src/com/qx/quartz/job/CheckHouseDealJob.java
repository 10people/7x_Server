package com.qx.quartz.job;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.network.BigSwitch;
import com.qx.alliance.HouseApplyBean;
import com.qx.alliance.HouseMgr;
import com.qx.persistent.HibernateUtil;

public class CheckHouseDealJob implements Job {
	private Logger log = LoggerFactory.getLogger(CheckHouseDealJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("自动完成大房子交易开始");
		Date today =new Date();
		//自动完成大房子交易
		Map<Long,Boolean> isFishMap=new HashMap<Long,Boolean>();
		List<HouseApplyBean> list = HibernateUtil.list(HouseApplyBean.class, " order by dt ");
		for (HouseApplyBean applyBean: list){
			if(isFishMap.get(applyBean.keeperId)==null||(isFishMap.get(applyBean.keeperId)!=null&&!isFishMap.get(applyBean.keeperId))){
				//超过七天的房屋交易申请自动完成
				long cha=today.getTime()-applyBean.dt.getTime();
				if(cha* 1.0 / (1000 * 60 * 60)>168){
					log.info("自动完成{}购买{}的大房子交易",applyBean.buyerId,applyBean.keeperId);
					BigSwitch.inst.houseMgr.answerEx(applyBean.keeperId, applyBean.buyerId, 10);
					Boolean isComplete=HouseMgr.sentIsComplete.get();
					if(isComplete){
						isFishMap.put(applyBean.keeperId, isComplete);
					}
				}
			}
		}
		log.info("自动完成大房子交易结束");
	}
}
   