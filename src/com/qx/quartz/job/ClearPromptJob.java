package com.qx.quartz.job;

import java.io.File;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.util.DateUtils;
import com.qx.persistent.HibernateUtil;
import com.qx.prompt.PromptMsgMgr;
import com.qx.prompt.SuBaoConstant;

public class ClearPromptJob implements Job {
	public Logger log = LoggerFactory.getLogger(ClearPromptJob.class.getSimpleName());
	public 	 StringBuffer sql=  new StringBuffer();
	public static boolean doIt = true;
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		//windows服务器不执行这里的逻辑。doIt会变为false
		doIt = File.separatorChar == '/';
		if(doIt == false)return;
		log.info("开始清理3分钟失效的通知");
 		String timeStr=DateUtils.getNSecondsAgo(SuBaoConstant.clearSecondsDistance);
		String sql=PromptMsgMgr.sql2ThreeMinClear+timeStr+"'";
		HibernateUtil.executeSql(sql);
		log.info("结束清理3分钟失效的通知");
		//doIt = false;//debug code
	}
}