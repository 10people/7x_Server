package com.qx.quartz.job;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.persistent.HibernateUtil;
import com.qx.prompt.PromptMSG;

public class CleanLMSBJob implements Job {
	private Logger log = LoggerFactory.getLogger(CleanLMSBJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("清理联盟速报开始");
		//查出8小时前的所有速报删掉
		List<PromptMSG> promptMsgList = HibernateUtil.list(PromptMSG.class, "where addTime<date_add(now(), interval -8 hour)");
		for (PromptMSG msg: promptMsgList) {
			if (msg!=null) {
				log.info("删除君主{}的联盟速报，速报Id--{}",msg.jzId,msg.id);
				HibernateUtil.delete(msg);
			}
		}
		log.info("清理联盟速报结束");
	}
}