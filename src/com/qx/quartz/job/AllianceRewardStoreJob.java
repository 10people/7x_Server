package com.qx.quartz.job;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.HuangyeAward;
import com.manu.dynasty.template.Mail;
import com.qx.alliance.AllianceBean;
import com.qx.email.EmailMgr;
import com.qx.huangye.HYMgr;
import com.qx.huangye.HYRewardStore;
import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;

public class AllianceRewardStoreJob implements Job {
	public Logger logger = LoggerFactory.getLogger(AllianceRewardStoreJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("AllianceRewardStoreJob 开始");
		List<AllianceBean> alncList = HibernateUtil.list(AllianceBean.class, "");
		Date nowDate = new Date();
		List<HYRewardStore> rewardStoreListAll = HibernateUtil.list(HYRewardStore.class,
				" where amount > 0");
		for(AllianceBean alliance : alncList) {
			Iterator<HYRewardStore> rewardStoreIt = rewardStoreListAll.stream().filter(b->b.lianmengId==alliance.id).iterator();
			while(rewardStoreIt.hasNext()) {
				HYRewardStore rewardStore = rewardStoreIt.next();
				int site = rewardStore.site;
				String siteCacheKey = HYMgr.CACHE_HYSTORE_APPLY + alliance.id + "_"  + site;
				List<String> jzIdList = Redis.getInstance().lrange4String(siteCacheKey, 0, -1);
				for(String id : jzIdList) {
					if(rewardStore.amount > 0) {
						long junzhuId = Long.parseLong(id);
						Redis.getInstance().lrem(siteCacheKey, 0,"" + junzhuId);
						String jzCacheKey = HYMgr.CACHE_HYSTORE_APPLY + alliance.id + "_" + junzhuId;
						Redis.getInstance().del(jzCacheKey);
						
						HuangyeAward awardInfo = HYMgr.inst.huangyeAwardMap.get(site);
						JunZhu junzhu = HibernateUtil.find(JunZhu.class, junzhuId); 
						Mail cfg = EmailMgr.INSTANCE.getMailConfig(21004);
						String fuJian = awardInfo.itemType + ":" + awardInfo.itemId + ":" + 1;
						boolean ok = EmailMgr.INSTANCE.sendMail(junzhu.name, cfg.content,
								fuJian, cfg.sender, cfg, "");
						logger.info("联盟:{}荒野奖励库自动发放奖励给{}成功? {}", alliance.id, junzhu.name,ok);
						rewardStore.lastAllotTime = nowDate;
						rewardStore.amount -= 1;
					}
				}
				HibernateUtil.save(rewardStore);
			}
		}
		logger.info("AllianceRewardStoreJob 结束");
	}
	
}
