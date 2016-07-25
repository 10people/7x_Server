package com.qx.huangye;

import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.HuangyeAward;
import com.manu.dynasty.template.Mail;
import com.qx.email.EmailMgr;
import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;


public class RewardStoreTask implements Runnable {
	public static RewardStoreTask INSTANCE = null;
	public Logger logger = LoggerFactory.getLogger(RewardStoreTask.class);
	public ConcurrentLinkedQueue<HYRewardStore> rewardStoreQueue = new ConcurrentLinkedQueue<HYRewardStore>(); 
	
	public RewardStoreTask() {
		INSTANCE = this;
	}
	
	@Override
	public void run() {
		while(rewardStoreQueue.size() > 0) {
			HYRewardStore rewardStore = rewardStoreQueue.peek();
			Date curDate = new Date();
			int differMinutes = (int) ((curDate.getTime() - rewardStore.lastAllotTime.getTime()) / 1000 /60);
			if(differMinutes >= 30) {
				int lianMengId = rewardStore.lianmengId;
				int site = rewardStore.site;
				String siteCacheKey = HYMgr.CACHE_HYSTORE_APPLY + lianMengId + "_" + site;
				long junzhuId = Long.parseLong(Redis.getInstance().lpop(siteCacheKey));
				Redis.getInstance().lrem(
						HYMgr.CACHE_HYSTORE_APPLY + lianMengId + "_"  + site, 0,
						"" + junzhuId);
				String jzCacheKey = HYMgr.CACHE_HYSTORE_APPLY + lianMengId + "_" + junzhuId;
				Redis.getInstance().del(jzCacheKey);
				
				HuangyeAward awardInfo = HYMgr.inst.huangyeAwardMap.get(site);
				JunZhu junzhu = HibernateUtil.find(JunZhu.class, junzhuId); 
				Mail cfg = EmailMgr.INSTANCE.getMailConfig(20001);
				String fuJian = awardInfo.itemType + ":" + awardInfo.itemId + ":" + 1;
				boolean ok = EmailMgr.INSTANCE.sendMail(junzhu.name, cfg.content,
						fuJian, cfg.sender, cfg, "");
				logger.info("盟主分配荒野奖励库奖励给{}成功? {}", junzhu.name,ok);
				rewardStore.lastAllotTime = curDate;
				rewardStore.amount -= 1;
				HibernateUtil.save(rewardStore);
				rewardStoreQueue.poll();
			}
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addTask(HYRewardStore task) {
		if(task != null) {
			rewardStoreQueue.offer(task);
		}
	}
	
	public boolean containsTask(HYRewardStore task) {
		if(task != null) {
			return rewardStoreQueue.contains(task);
		}
		return false;
	}

}
