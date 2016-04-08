package com.qx.quartz.job;

import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.util.DateUtils;
import com.qx.persistent.HibernateUtil;
import com.qx.prompt.PromptMSG;
import com.qx.prompt.SuBaoConstant;

public class CleanLMSBJob implements Job {
	private Logger log = LoggerFactory.getLogger(CleanLMSBJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("清理联盟速报开始");
		//查出8小时前的所有速报删掉
		String clearShortDistance=DateUtils.getNHourAgo(SuBaoConstant.clearShortDistance);
		log.info("清理{}小时之前联盟速报",SuBaoConstant.clearShortDistance);
		String condition4Short="where  addTime<'"+clearShortDistance+"' and award='' and  cartWorth =''";
		clearMsgByTime(clearShortDistance,condition4Short);
		//查出48小时前的所有速报删掉
		log.info("清理{}小时之前联盟速报",SuBaoConstant.clearLongDistance);
		String clearLongDistance=DateUtils.getNHourAgo(SuBaoConstant.clearLongDistance);
		String condition4Long="where  addTime<'"+clearLongDistance+"'";
		clearMsgByTime(clearLongDistance,condition4Long);
		log.info("清理联盟速报结束");
	}
	
	public void clearMsgByTime(String clearDistance,String condition) {
		try {
			List<PromptMSG> promptMsgList = HibernateUtil.list(PromptMSG.class,condition );
			for (PromptMSG msg: promptMsgList) {
				if (msg!=null) {
					log.info("删除君主{}的联盟速报，速报Id--{}",msg.jzId,msg.id);
					HibernateUtil.delete(msg);
				}
			}
			
		} catch (Exception e) {
			log.error("清理联盟速报异常",e);
		}
	}
	
}