package com.qx.quartz.job;

import java.util.Date;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.network.SessionManager;
import com.qx.activity.XianShiActivityMgr;
import com.qx.junzhu.PlayerTime;
import com.qx.persistent.HibernateUtil;

public class RefreshLoginCountJob   implements Job {
	public Logger log = LoggerFactory.getLogger(RefreshLoginCountJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("开始刷新登录天数");
		refreshLoginCount();
		log.info("结束刷新登录天数");
	}
	public void refreshLoginCount() {
		List<Long> list = SessionManager.inst.getAllOnlineJunZhuId();
		if(list != null){
			for (Long jzId: list){
				PlayerTime playerTime = HibernateUtil.find(PlayerTime.class, jzId);
				if(playerTime==null){
					log.error("君主{}---playerTime为空",jzId);
					continue;
				}else{
					XianShiActivityMgr.instance.updateLoginDate(jzId);
					playerTime.loginTime = new Date();
					HibernateUtil.update(playerTime);
				}
			}
		}
	}
}
