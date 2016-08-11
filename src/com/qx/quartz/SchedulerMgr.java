package com.qx.quartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.YunbiaoTemp;
import com.qx.activity.StrengthGetMgr;
import com.qx.alliancefight.BidMgr;
import com.qx.quartz.job.AllianceResouceOutputJob;
import com.qx.quartz.job.AllianceRewardStoreJob;
import com.qx.quartz.job.BaiZhanDailyAwardJob;
import com.qx.quartz.job.BaiZhanNoticeJob;
import com.qx.quartz.job.BaoXiangQueueJob;
import com.qx.quartz.job.BigHouseWorthReduceJob;
import com.qx.quartz.job.BroadcastJob;
import com.qx.quartz.job.CheckHouseDealJob;
import com.qx.quartz.job.CityBidBillingJob;
import com.qx.quartz.job.CityBidPriceRefreshJob;
import com.qx.quartz.job.CityWarBidClearData;
import com.qx.quartz.job.CleanLMSBJob;
import com.qx.quartz.job.ClearPromptJob;
import com.qx.quartz.job.DailyTaskJob;
import com.qx.quartz.job.DailyTiliJob;
import com.qx.quartz.job.GuojiaChouhenJieSuanJob;
import com.qx.quartz.job.GuojiaDayRankResetJob;
import com.qx.quartz.job.GuojiaSetDiDuiGuoJob;
import com.qx.quartz.job.GuojiaWeekRankResetJob;
import com.qx.quartz.job.LianMengBySWDayRankResetJob;
import com.qx.quartz.job.LianMengBySWWeekRankResetJob;
import com.qx.quartz.job.LogPerHourJob;
import com.qx.quartz.job.LogPerMinuteJob;
import com.qx.quartz.job.LveDuoJunQingJob;
import com.qx.quartz.job.RefreshGlobalActivity;
import com.qx.quartz.job.RefreshGongHeInfo;
import com.qx.quartz.job.RefreshLoginCountJob;
import com.qx.quartz.job.ResetGongJinJob;
import com.qx.quartz.job.SendGongJinAwardJob;
import com.qx.quartz.job.ShopRefreshJob;
import com.qx.quartz.job.YaBiaoJiaChengJob;
import com.qx.quartz.job.YaBiaoManageJob;

import xg.push.XGJob;


public class SchedulerMgr {
	public static Scheduler scheduler;
	public static SchedulerMgr inst;
	public static Logger log = LoggerFactory.getLogger(SchedulerMgr.class);

	public SchedulerMgr(){
		inst = this;
		init();
	}
	public void init(){
		SchedulerFactory  sfa = new StdSchedulerFactory();
		try {
			scheduler = sfa.getScheduler();
			scheduler.start();
			log.info("获取scheduler成功");
		}catch (SchedulerException e) {
			log.error("获取scheduler失败");
			e.printStackTrace();
		}
	}

	/**
	 * 添加任务
	 * @Title: start 
	 * @Description:
	 */
	public void doSchedule(){
	//	addScheduler(TestJob.class, "0/10 * * * * ?");
		// 每天晚上23:58 发送在线且没有领奖的百战日奖励邮件, 掠夺也会在23:58发送每日奖励邮件，因此都加在这个任务中
		addScheduler(BaiZhanDailyAwardJob.class, "0 58 3am * * ?");
		//每周一到周六晚上22点衰减高级房屋价值
		StringBuffer shuaijianTime=new StringBuffer();
		shuaijianTime.append("0 0 ").append(String.valueOf(CanShu.REFRESHTIME_GAOJIFANGWU)).append(" ? * 2-7");
		addScheduler(BigHouseWorthReduceJob.class, shuaijianTime.toString());//"0 0 22 ? * 2-7"
		//每周日22点分配大房子 1.0无大房子
//		StringBuffer fenpeiTime=new StringBuffer();
//		fenpeiTime.append("0 0 ").append(String.valueOf(CanShu.REFRESHTIME_GAOJIFANGWU)).append(" ? *  1");
//		addScheduler(FenBigHouseJob.class, fenpeiTime.toString());//"0 0 22 ? *  1"
		//每天0点检查未完成的房屋交易
		addScheduler(CheckHouseDealJob.class, "0 0 0 * * ?");
		//每天0点检查/更新联盟状态   TODO 0.99不上
//		addScheduler(AllianceVoteJob.class, "0 0 0 * * ?");
		// 每天0点刷新国家日榜
		addScheduler(GuojiaDayRankResetJob.class, "0 0 0 * * ?");
		// 每天0点刷新联盟昨日声望榜
		addScheduler(LianMengBySWDayRankResetJob.class, "0 0 0 * * ?");
		// 每周一0点刷新国家周榜
		addScheduler(GuojiaWeekRankResetJob.class, "0 0 0 ? * 2");
		// 每周一0点刷新联盟声望周榜
		addScheduler(LianMengBySWWeekRankResetJob.class, "0 0 0 ? * 2");
		/*
		 * 每日任务 固定时间更新每日任务列表
		 */
		addScheduler(DailyTaskJob.class, "0 0 12,14,18,20,21,0,4 * * ?");
		addScheduler(XGJob.class, "0 54 11,17 * * ?");
		addScheduler(XGJob.class, "0 40 20 * * ?");
		
		//开启押镖活动
		StringBuffer openYBTime=new StringBuffer();
		String[] openYB = CanShu.OPENTIME_YUNBIAO.split(":");
		int openH = Integer.parseInt(openYB[0]);
		int openM = Integer.parseInt(openYB[1]);
		openYBTime.append("0 ").append(openM).append(" ").append(openH).append(" * * ?");
		addScheduler(YaBiaoManageJob.class, openYBTime.toString());//"0 0 8 * * ?"
		//关闭押镖活动
		StringBuffer closeYBTime=new StringBuffer();
		String[] closeYB = CanShu.CLOSETIME_YUNBIAO.split(":");
		int closeH = Integer.parseInt(closeYB[0]);
		int closeM = Integer.parseInt(closeYB[1]);
		closeYBTime.append("0 ").append(closeM).append(" ").append(closeH).append(" * * ?");
		addScheduler(YaBiaoManageJob.class, closeYBTime.toString());//0 0 11 * * ?
		
		
		//开启押镖活动福利1 2016年1月27日去掉 2016年2月1日加回来
		StringBuffer openYBMORETime1=new StringBuffer();
		String[] openYBMORE1 = YunbiaoTemp.incomeAdd_startTime1.split(":");
		int openHMORE1 = Integer.parseInt(openYBMORE1[0]);
		int openMMORE1 = Integer.parseInt(openYBMORE1[1]);
		openYBMORETime1.append("0 ").append(openMMORE1).append(" ").append(openHMORE1).append(" * * ?");
		addScheduler(YaBiaoJiaChengJob.class, openYBMORETime1.toString());//"0 0 8 * * ?"
		//开启押镖活动福利2
		StringBuffer openYBMORETime2=new StringBuffer();
		String[] openYBMORE2 =YunbiaoTemp.incomeAdd_startTime2.split(":");
		int openHMORE2 = Integer.parseInt(openYBMORE2[0]);
		int openMMORE2 = Integer.parseInt(openYBMORE2[1]);
		openYBMORETime2.append("0 ").append(openMMORE2).append(" ").append(openHMORE2).append(" * * ?");
		addScheduler(YaBiaoJiaChengJob.class, openYBMORETime2.toString());//"0 0 8 * * ?"
		//开启押镖活动福利3
		StringBuffer openYBMORETime3=new StringBuffer();
		String[] openYBMORE3 =YunbiaoTemp.incomeAdd_startTime3.split(":");
		int openHMORE3 = Integer.parseInt(openYBMORE3[0]);
		int openMMORE3 = Integer.parseInt(openYBMORE3[1]);
		openYBMORETime3.append("0 ").append(openMMORE3).append(" ").append(openHMORE3).append(" * * ?");
		addScheduler(YaBiaoJiaChengJob.class, openYBMORETime3.toString());//"0 0 8 * * ?"
//		//关闭押镖活动福利1 2016年1月27日 去掉
		StringBuffer closeYBMORETime1=new StringBuffer();
		String[] closeYBMORE1 = YunbiaoTemp.incomeAdd_endTime1.split(":");
		int closeHMORE1 = Integer.parseInt(closeYBMORE1[0]);
		int closeMMORE1 = Integer.parseInt(closeYBMORE1[1]);
		closeYBMORETime1.append("0 ").append(closeMMORE1).append(" ").append(closeHMORE1).append(" * * ?");
		addScheduler(YaBiaoJiaChengJob.class, closeYBMORETime1.toString());//0 0 11 * * ?
		//关闭押镖活动福利2
		StringBuffer closeYBMORETime2=new StringBuffer();
		String[] closeYBMORE2 = YunbiaoTemp.incomeAdd_endTime2.split(":");
		int closeHMORE2 = Integer.parseInt(closeYBMORE2[0]);
		int closeMMORE2 = Integer.parseInt(closeYBMORE2[1]);
		closeYBMORETime2.append("0 ").append(closeMMORE2).append(" ").append(closeHMORE2).append(" * * ?");
		addScheduler(YaBiaoJiaChengJob.class, closeYBMORETime2.toString());//0 0 11 * * ?
		//关闭押镖活动福利3
		StringBuffer closeYBMORETime3=new StringBuffer();
		String[] closeYBMORE3 = YunbiaoTemp.incomeAdd_endTime3.split(":");
		int closeHMORE3 = Integer.parseInt(closeYBMORE3[0]);
		int closeMMORE3 = Integer.parseInt(closeYBMORE3[1]);
		closeYBMORETime3.append("0 ").append(closeMMORE3).append(" ").append(closeHMORE3).append(" * * ?");
		addScheduler(YaBiaoJiaChengJob.class, closeYBMORETime3.toString());//0 0 11 * * ?
		
		String time = CanShu.HUANGYEPVP_AWARDTIME;
		String[] timeArray = time.split(":");
		StringBuilder resOutputJobTime = new StringBuilder();
		resOutputJobTime.append("0 ").append(timeArray[1]).append(" ").append(timeArray[0]).append(" ").append(" * * ?");
		//addScheduler(AllianceResouceOutputJob.class, resOutputJobTime.toString());//没有荒野资源点了
		//addScheduler(AllianceRewardStoreJob.class, "0 0 * * * ?");//没有荒野奖励库了
		//
		addScheduler(LogPerMinuteJob.class,"1 * * * * ?");
		addScheduler(LogPerHourJob.class,"0 30 * * * ?");
		addScheduler(BaoXiangQueueJob.class,"* * * * * ?");//1秒检查一次
		addScheduler(BroadcastJob.class,"1 * * * * ?");//定时广播，没分钟检查
		addScheduler(BaiZhanNoticeJob.class,"1 * * * * ?");//定时广播，没分钟检查
		// 每周一0点，向前窜着记录上一期仇恨值
		addScheduler(GuojiaChouhenJieSuanJob.class,"0 0 0 ? * 2");
		// 每天00:05，刷新国家敌对国
		addScheduler(GuojiaSetDiDuiGuoJob.class, "0 5 0 * * ?");
		// 每天8:00 ，刷新君主贡金排行和 联盟贡金排行
		addScheduler(ResetGongJinJob.class, "0 0 8 * * ?");
		// 每天4:00  清理过期的盟友速报
		addScheduler(CleanLMSBJob.class, "0 0 4 * * ?");
		// 每天22:00 贡金个人排行和联盟排行发放奖励 
		addScheduler(SendGongJinAwardJob.class, "0 0 22 * * ?");
		// 每天4:00  重置服务器时间为准的活动状态
		addScheduler(RefreshGlobalActivity.class, "0 0 4 * * ?");
		// 每天4:00 所有在线人数的登录天数+1
		addScheduler(RefreshLoginCountJob.class, "0 0 4 * * ?");
		addScheduler(LveDuoJunQingJob.class, "0 */1 * * * ?");
		addScheduler(RefreshGongHeInfo.class, "*/20 * * * * ?");
		//addScheduler(ShopRefreshJob.class, "0 0 9,21 * * ?");
		addScheduler(ClearPromptJob.class, "*/20 * * * * ?");
		//宣战竞拍缓存更新
		addScheduler(CityBidPriceRefreshJob.class ,"0 0 * * * ?");
		//每天 4:00 定时清理过期竞拍记录
		addScheduler(CityWarBidClearData.class, "0 0 4 * * ?");
		//每天 18:00 定时竞拍结算
		String[] bidTimeArr = BidMgr.city_war_preparation_startTime.split(":");
		StringBuilder bidJobTime = new StringBuilder();
		bidJobTime.append("0 ").append(bidTimeArr[1]).append(" ").append(bidTimeArr[0]).append(" * * ?");
		addScheduler(CityBidBillingJob.class,bidJobTime.toString());
		/*
		 * 每日体力 固定时间更新体力红点
		 */
		String[] s1 = StrengthGetMgr.STRENGTH_GET_TIME1_START.split(":");
		String[] s2 = StrengthGetMgr.STRENGTH_GET_TIME2_START.split(":");
		String[] s3 = StrengthGetMgr.STRENGTH_GET_TIME3_START.split(":");
		addScheduler(DailyTiliJob.class, "0 0 " + s1[0] +"," + s2[0] + "," + s3[0] + " * * ?");
		String[] e1 = StrengthGetMgr.STRENGTH_GET_TIME1_END.split(":");
		String[] e2 = StrengthGetMgr.STRENGTH_GET_TIME2_END.split(":");
		String[] e3 = StrengthGetMgr.STRENGTH_GET_TIME3_END.split(":");
		if("24".equals(e3[0])) e3[0] = "0";
		addScheduler(DailyTiliJob.class, "0 0 " + e1[0] +"," + e2[0] + "," + e3[0] + " * * ?");
	}
	/**
	 * 任务列表
	 * @Title: addScheduler 
	 * @Description:
	 * @param jobClass
	 * @param time 时间通配符
	 */
	public void addScheduler(Class<? extends Job> jobClass, String time){
		JobDetail job = JobBuilder.newJob(jobClass).build();
		CronTrigger trigger =
				TriggerBuilder.newTrigger()
				.withSchedule(CronScheduleBuilder.cronSchedule(time)) 
				.build(); 
		try {
			scheduler.scheduleJob(job, trigger);
//			log.info("添加job：{}到定时任务列表中成功", jobClass);
		} catch (SchedulerException e) {
			e.printStackTrace();
			log.error("添加job：{}到定时任务列表中失败", jobClass);
		}
	}

	/**
	 * 定时任务关闭，暂时没有被调用
	 * @Title: stop 
	 * @Description:
	 */
	public void stop(){
		try {
			scheduler.shutdown(true);
		} catch (SchedulerException e){
			e.printStackTrace();
		}
	}
}
