package com.qx.quartz.job;

import java.util.Date;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.Mail;
import com.manu.dynasty.util.DateUtils;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AlliancePlayer;
import com.qx.email.EmailMgr;
import com.qx.huangye.HYResource;
import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;

public class AllianceResouceOutputJob implements Job {
	private Logger logger = LoggerFactory.getLogger(AllianceResouceOutputJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		List<AllianceBean> alncList = HibernateUtil.list(AllianceBean.class, "");
		Date nowDate = new Date();
		logger.info("定时发放联盟资源点产出奖励开始");
		for(AllianceBean alliance : alncList) {
			List<HYResource> resourceList = HibernateUtil.list(HYResource.class,
					" where curHoldId=" + alliance.id);
			for(HYResource resource : resourceList) {
				Date lastAllotTime = resource.lastAllotTime;
				if(lastAllotTime == null) {
					lastAllotTime = resource.holdStartTime;
				}
				int hours = DateUtils.timeDistanceByHour(nowDate, lastAllotTime);
				int getTongbi = (int) (hours * CanShu.HUANGYEPVP_PRODUCE_P);
				List<AlliancePlayer> memberAll = HibernateUtil.list(AlliancePlayer.class, " where lianMengId="+alliance.id);
				Mail mailConfig = null;
				String fujian = "";
				boolean sendOK = false;
				if(getTongbi > 0) {
					mailConfig = EmailMgr.INSTANCE.getMailConfig(21002);
					fujian = 0 + ":" + 900001 + ":" + getTongbi;
				} else {
					mailConfig = EmailMgr.INSTANCE.getMailConfig(21003);
				}
				for(AlliancePlayer ap : memberAll) {
					JunZhu getJunzhu = HibernateUtil.find(JunZhu.class, ap.junzhuId);
					sendOK = EmailMgr.INSTANCE.sendMail(getJunzhu.name, mailConfig.content, fujian, mailConfig.sender, mailConfig,"");
					logger.info("定时发送联盟:{}资源点:{}奖励，以邮件发送奖励, 结果:{}", alliance.id, resource.id, sendOK);
				}
			}
		}
		logger.info("定时发放联盟资源点产出奖励结束");
	}
	
}
