package com.qx.quartz.job;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.template.YunbiaoTemp;
import com.manu.network.BigSwitch;
import com.qx.world.Scene;
import com.qx.yabiao.YaBiaoHuoDongMgr;

public class YBrobotManageJob implements Job {
	private Logger log = LoggerFactory.getLogger(YBrobotManageJob.class);
	@SuppressWarnings("rawtypes")
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("押镖系统机器人马车生成开始");
		if(BigSwitch.inst.ybMgr.yabiaoScenes.size()==0){
			log.info("押镖场景为空~~~~~~~~~~~~~~~~~~~~");
			return;
		}
		Iterator<?> it4Scene= BigSwitch.inst.ybMgr.yabiaoScenes.entrySet().iterator();
	
		while (it4Scene.hasNext()) {
			Map.Entry entry4Sc = (Map.Entry) it4Scene.next();
			Integer ybScId =(Integer) entry4Sc.getKey();
			Scene ybsc =(Scene) entry4Sc.getValue();
			Set<Long> ybSet=BigSwitch.inst.ybMgr.ybJzList2ScIdMap.get(ybScId);
			int macheSize=ybSet.size();
			if(macheSize>=YunbiaoTemp.cartAImax){
				log.info("场景中有马车{}辆，不需要系统马车机器人");
				break;
			}
			Map<Integer, Integer> safeMap=YaBiaoHuoDongMgr.inst.getSafeAreaCount(ybsc);
			for(Map.Entry<Integer, Integer> entry :safeMap.entrySet()) {
				Integer safeId=entry.getValue();
				if(safeId<YunbiaoTemp.saveArea_people_max){
					int macheshu=YunbiaoTemp.cartAImax-macheSize;
					//安全区内人数不满开始产生马车
					log.info("场景中有马车{}辆，需要系统马车机器人--{}辆");
					produceXiTongMaChe(ybsc, ybScId, safeId, macheshu);
				}
			}
			
			
		}
		log.info("押镖系统机器人马车生成结束");
	}
	/**
	 * @Description 产生系统马车
	 * @param ybsc 场景
	 * @param ybScId 场景Id
	 * @param pathId 安全区编号/路线编号
	 * @param count  产生马车数目
	 */
	protected void produceXiTongMaChe(Scene ybsc,int ybScId,int pathId,int count) {
		for(int i = 0; i < count; i++) {
			log.info("产生系统马车--{}辆",i+1);
			try {
				Thread.sleep(10000);//10秒放一辆镖车 
				YaBiaoHuoDongMgr.inst.initSysYBRobots(ybsc, pathId,ybScId);
			} catch (InterruptedException e) {
			log.error("产生系统马车错误:{}",e);	
			}
		}
	}
	
}
   