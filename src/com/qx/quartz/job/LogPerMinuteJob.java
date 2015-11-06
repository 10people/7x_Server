package com.qx.quartz.job;

import java.util.Calendar;

import log.OurLog;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 每分钟一条日志
 * @author 康建虎
 *
 */
public class LogPerMinuteJob  implements Job{
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		int minuteCnt = Calendar.getInstance().get(Calendar.MINUTE);
		minuteCnt %= 5;//每5分钟生成一次在线数量
		if(minuteCnt == 0){
			OurLog.log.PlayerOnline();
		}
		OurLog.log.GameSvrState();
	}

}
