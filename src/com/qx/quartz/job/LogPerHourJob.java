package com.qx.quartz.job;

import log.Log2DB;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogPerHourJob  implements Job{
	private Logger logger = LoggerFactory.getLogger(LogPerHourJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("开始,导出日志到数据库");
		Log2DB ld = new Log2DB();
		ld.saveAll2db();
		logger.info("结束,导出日志到数据库");
	}

}
