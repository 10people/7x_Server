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
			Integer yaochanshengmacheshu=YaBiaoHuoDongMgr.xtmcs4Scene.get(ybScId);
			if(yaochanshengmacheshu!=null&&yaochanshengmacheshu>0){
				log.info("场景{}中有{}辆系统马车要投放",ybScId,yaochanshengmacheshu);
				continue;
			}
			int macheSize=0;
			Set<Long> ybSet=BigSwitch.inst.ybMgr.ybJzList2ScIdMap.get(ybScId);
			if(ybSet!=null){
				macheSize=ybSet.size();//场景中的押镖马车数目
			}
			if(macheSize>=YunbiaoTemp.cartAImax){
				log.info("场景中有马车{}辆，不需要系统马车机器人",macheSize);
				break;
			}
			Map<Integer, Integer> safeMap=YaBiaoHuoDongMgr.inst.getSafeAreaCount(ybsc);
			for(Map.Entry<Integer, Integer> entry :safeMap.entrySet()) {
				Integer renshu=entry.getValue();//人数
				Integer pathId=entry.getKey();//路线
				if(renshu<YunbiaoTemp.saveArea_people_max){
					int macheshu=YunbiaoTemp.cartAImax-macheSize;
					YaBiaoHuoDongMgr.xtmcs4Scene.put(ybScId, macheshu);
					//安全区内人数不满开始产生马车
					log.info("场景中有马车{}辆，需要系统马车机器人--{}辆",macheSize,macheshu);
					produceXiTongMaChe(ybsc, ybScId, pathId, macheshu);
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
				boolean res=YaBiaoHuoDongMgr.inst.initSysYBRobots(ybsc, pathId,ybScId);
				log.info("产生系统马车结果isOK?==={}",res);
			} catch (InterruptedException e) {
			log.error("产生系统马车错误:{}",e);	
			}
		}
	}
	
}
   