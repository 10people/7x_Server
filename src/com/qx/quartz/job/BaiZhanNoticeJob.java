package com.qx.quartz.job;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.AnnounceTemp;
import com.qx.world.BroadcastMgr;

public class BaiZhanNoticeJob  implements Job{
	public static Logger log = LoggerFactory.getLogger(BaiZhanNoticeJob.class.getSimpleName());
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		Map<Integer, AnnounceTemp> annnounceMap = new HashMap<Integer, AnnounceTemp>();
		for (AnnounceTemp a : confList) {
			if(29 == a.type){
				StringBuffer time1=new StringBuffer();
				String[] str = a.condition.split(":");
				int hour = Integer.parseInt(str[0]);
				int min = Integer.parseInt(str[1]);
				Calendar c = Calendar.getInstance();
				int curHour = c.get(Calendar.HOUR_OF_DAY);
				int curMin = c.get(Calendar.MINUTE);
				if(hour == curHour && min == curMin){
					log.info("触发广播 {}",a.id);
					BroadcastMgr.inst.send(a.announcement);
				}
			}
		}		
	}

}
