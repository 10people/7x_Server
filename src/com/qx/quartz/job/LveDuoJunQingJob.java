 package com.qx.quartz.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.template.LianmengEvent;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceBeanDao;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;
import com.qx.prompt.LveDuoMI;

public class LveDuoJunQingJob implements Job {
	public Logger log = LoggerFactory.getLogger(LveDuoJunQingJob.class);
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("开始刷新掠夺军情");
		String where = " where willLostBuildTime <= now()" ;
		List<LveDuoMI> list = HibernateUtil.list(LveDuoMI.class, where);
		Map<Integer, AllianceBean> allianceMap = new HashMap<>();
		Map<Integer, Integer> lostMap = new HashMap<>();
		
		LianmengEvent e = AllianceMgr.inst.lianmengEventMap.get(24);
		String eventStr = e == null? "": e.str;
		for(LveDuoMI mi: list){
			// TODO hiber先删除应该是可以的吧
			deleteLveDuoMI(mi);
			AllianceBean enemyAlli = null;
			if(mi.lmId > 0) {
				enemyAlli = allianceMap.get(mi.lmId);
				if(enemyAlli == null){
					enemyAlli = AllianceBeanDao.inst.getAllianceBean(mi.lmId);
				}
			}
			if(enemyAlli == null){
				continue;
			}
			int thisLost = mi.willLostBuild;
			if(thisLost <=0 ){
				continue;
			}
			/*暂时不用处理*/
//			AlliancePlayer p = HibernateUtil.find(AlliancePlayer.class, mi.beanLveDuoJunId);
//			if(p == null || p.lianMengId != mi.lmId){
//				continue;//换联盟不能扣除
//			}
			allianceMap.put(mi.lmId, enemyAlli);

			Integer value = lostMap.get(mi.lmId);
			value = value == null? 0: value;
			lostMap.put(mi.lmId, value + thisLost);

			JunZhu enemy = HibernateUtil.find(JunZhu.class, mi.lveDuoJunId);

			//<LianmengEvent ID="14" str="%d被%d掠夺成功，联盟损失%d建设值！" />
			eventStr = eventStr.replaceFirst("%d", enemy.name)
					.replaceFirst("%d", thisLost+"");
			AllianceMgr.inst.addAllianceEvent(enemyAlli.id, eventStr);
			
		}
		for(Map.Entry<Integer, AllianceBean> entry: allianceMap.entrySet()){
			AllianceBean a = entry.getValue();
			Integer va = lostMap.get(entry.getKey());
			if(va == null || va == 0) continue;
			AllianceMgr.inst.changeAlianceBuild(a, -va);
		}
	}
	public void deleteLveDuoMI(LveDuoMI mi){
		HibernateUtil.delete(mi);
	}
}
