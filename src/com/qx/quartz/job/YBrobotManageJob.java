package com.qx.quartz.job;

import java.util.Iterator;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.network.BigSwitch;
import com.qx.world.Scene;
import com.qx.yabiao.YaBiaoHuoDongMgr;

public class YBrobotManageJob implements Job {
	private Logger log = LoggerFactory.getLogger(YBrobotManageJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("押镖系统机器人镖车生成开始");
		if(BigSwitch.inst.ybMgr.yabiaoScenes.size()==0){
			log.info("押镖场景为空~~~~~~~~~~~~~~~~~~~~");
			return;
		}
		Iterator<?> it= BigSwitch.inst.ybMgr.yabiaoScenes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Integer scId =(Integer) entry.getKey();
			Scene ybsc =(Scene) entry.getValue();
			//每次执行变换一次开启标记
			for(int i = 1; i < 5; i++) {
				YaBiaoHuoDongMgr.inst.initSysYBRobots(ybsc, i,scId);
			}
		}
		log.info("押镖系统机器人镖车生成开始");
	}
}
   