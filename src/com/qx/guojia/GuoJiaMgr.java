package com.qx.guojia;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.GuoJia.GuoJiaMainInfoResp;
import qxmobile.protobuf.GuoJia.GuojiaRankInfo;
import qxmobile.protobuf.GuoJia.JuanXianDayAwardResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.store.MemcachedCRUD;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.ChouHenJiSuan;
import com.manu.dynasty.template.LueduoUnionRank;
import com.manu.dynasty.template.Mail;
import com.manu.dynasty.template.ShangJiaoTemp;
import com.manu.dynasty.util.DateUtils;
import com.manu.dynasty.util.MathUtils;
import com.manu.network.PD;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AlliancePlayer;
import com.qx.award.AwardMgr;
import com.qx.email.EmailMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.ranking.RankingMgr;
import com.qx.world.Mission;

public class GuoJiaMgr  extends EventProc implements Runnable{
	public static Logger log = LoggerFactory.getLogger(GuoJiaMgr.class);
	public static int serverId = GameServer.serverId;
	private static Mission exit = new Mission(0, null, null);
	public LinkedBlockingQueue<Mission> missions = new LinkedBlockingQueue<Mission>();
	public static GuoJiaMgr inst;
	public static Object gongJinLock = new Object();
	public static Map<Integer, ShangJiaoTemp> shangJiaoMap = new HashMap<Integer, ShangJiaoTemp>();
	public Map<Integer,List<LueduoUnionRank>> dayAwardMap = new HashMap<Integer, List<LueduoUnionRank>>();
	public Map<Integer,Double> chouHenJiSuanMap = new HashMap<Integer, Double>();
	
	public static final Object shengWangLock = new Object();
	/*
	 * 国家id
		 * <NameId nameId="0" Name="无" />
			<NameId nameId="1" Name="齐" />
			<NameId nameId="2" Name="楚" />
			<NameId nameId="3" Name="燕" />
			<NameId nameId="4" Name="韩" />
			<NameId nameId="5" Name="赵" />
			<NameId nameId="6" Name="魏" />
			<NameId nameId="7" Name="秦" />
	 */
	public static final byte guoJia_1 = 1;
	public static final byte guoJia_2 = 2;
	public static final byte guoJia_3 = 3;
	public static final byte guoJia_4 = 4;
	public static final byte guoJia_5 = 5;
	public static final byte guoJia_6 = 6;
	public static final byte guoJia_7 = 7;
	public static final byte[] guoJiaIds = {guoJia_1, guoJia_2, guoJia_3, guoJia_4, guoJia_5, guoJia_6, guoJia_7};
	
	public GuoJiaMgr() {
		inst = this;
		initData();
		// 开启线程
		new Thread(this, "GuoJiaMgr").start();
	}

	@SuppressWarnings("unchecked")
	public void initData() {
		//上缴配置
		 Map<Integer, ShangJiaoTemp> shangJiaoMap = new HashMap<Integer, ShangJiaoTemp>();
		List<ShangJiaoTemp> list=TempletService.listAll(ShangJiaoTemp.class.getSimpleName());
		for (ShangJiaoTemp s : list) {
			shangJiaoMap.put(s.getTimes(), s);
		}
		GuoJiaMgr.shangJiaoMap=shangJiaoMap;
		//每日奖励配置
		Map<Integer,List<LueduoUnionRank>> dayAwardMap = new HashMap<Integer, List<LueduoUnionRank>>();
		List<LueduoUnionRank> awardlist=TempletService.listAll(LueduoUnionRank.class.getSimpleName());
		int tempCountryRank=0;
		List<LueduoUnionRank> tempList=null;
		for (LueduoUnionRank l : awardlist) { 
			if(tempCountryRank!=l.getCountryRank()){
				tempCountryRank=l.getCountryRank();
				tempList=new ArrayList<LueduoUnionRank>();
				tempList.add(l);
			}else{
				tempList.add(l);
			}
			dayAwardMap.put(l.getCountryRank(), tempList);
		}
		this.dayAwardMap=dayAwardMap;
		//仇恨结算权重配置
		Map<Integer,Double> chouHenJiSuanMap = new HashMap<Integer, Double>();
		List<ChouHenJiSuan> chlist=TempletService.listAll(ChouHenJiSuan.class.getSimpleName());
		for (ChouHenJiSuan ch : chlist) {
			chouHenJiSuanMap.put(ch.getTerm(), ch.getWeight());
		}
		this.chouHenJiSuanMap=chouHenJiSuanMap;

	}

	public static void setGuoJiaPlayerNumber(int guoJiaId){
		String key = getKeyByGuoJiaId(guoJiaId);
		// 设置
		if(MemcachedCRUD.getMemCachedClient().getCounter(key) == -1){
			MemcachedCRUD.getMemCachedClient().storeCounter(key, 1L);
		}else{
			MemcachedCRUD.getMemCachedClient().incr(key, 1L);
		}
	}
	public static long getGuoJiaPlayerNumber(int guoJiaId){
		String key = getKeyByGuoJiaId(guoJiaId);
		long count = MemcachedCRUD.getMemCachedClient().getCounter(key);
		if(count <= 0){
			count = 0;
		}
		return count;
	}
	public static String getKeyByGuoJiaId(int guoJiaId){
		String key = "";
		switch(guoJiaId){
		case 1: key = serverId + "guoJia" + guoJia_1; break;
		case 2: key = serverId + "guoJia" + guoJia_2; break;
		case 3: key = serverId + "guoJia" + guoJia_3; break;
		case 4: key = serverId + "guoJia" + guoJia_4; break;
		case 5: key = serverId + "guoJia" + guoJia_5; break;
		case 6: key = serverId + "guoJia" + guoJia_6; break;
		case 7: key = serverId + "guoJia" + guoJia_7; break;
		}
		return key;
	}

	public static int getLeastCountGuoJiaId(){
		long[] data = new long[]{
				getGuoJiaPlayerNumber(guoJia_1),
				getGuoJiaPlayerNumber(guoJia_2),
				getGuoJiaPlayerNumber(guoJia_3),
				getGuoJiaPlayerNumber(guoJia_4),
				getGuoJiaPlayerNumber(guoJia_5),
				getGuoJiaPlayerNumber(guoJia_6),
				getGuoJiaPlayerNumber(guoJia_7)
				};
		int flag = 0;
		for(int i = 0; i<data.length; i++){
			if(data[i] < data[flag]){
				flag = i;
			}
		}
		// 返回的是国家id(==index+1)
		return flag+1;
	}
	
	
	/**
	 * @Description //获取国家主页
	 * @param id
	 * @param builder
	 * @param session
	 */
	public  void getGuoJiaMainInfoResp(int id, Builder builder,IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求获取国家主页出错：君主不存在");
			return;
		}
		long jzId=jz.id;
		log.info("君主{}获取国家主页信息",jzId);
		GuoJiaMainInfoResp.Builder resp=GuoJiaMainInfoResp.newBuilder();
		ResourceGongJin gongjinBean =HibernateUtil.find(ResourceGongJin.class, jzId);
		if(gongjinBean==null){
			gongjinBean = new ResourceGongJin();
			gongjinBean.junzhuId = jzId;
		}
		GuoJiaBean gjBean = HibernateUtil.find(GuoJiaBean.class, jz.guoJiaId);
		if (gjBean == null) {
			gjBean=GuoJiaMgr.inst.initGuoJiaBeanInfo(jz.guoJiaId);
		}
		getGuoJiaMainInfo(jzId, gjBean, gongjinBean, resp);
		session.write(resp.build());
	}

	/**
	 * @Description 领取奖励
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getGongjinAwardResp(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求领取国家每日奖励失败：君主不存在");
			return;
		}
		long jzId=jz.id;
		log.info("君主{}请求领取国家每日奖励",jzId);
		ResourceGongJin gongjinBean =HibernateUtil.find(ResourceGongJin.class, jzId);
		JuanXianDayAwardResp.Builder resp=JuanXianDayAwardResp.newBuilder();
		if(gongjinBean == null){
//			log.error("请求领取贡金奖励出错：{}的贡金奖励功能未开启", jzId);
//			resp.setResult(20);
//			resp.setAward("");
//			resp.setGuojiRank(-1);
//			resp.setLianMengRank(-1);
//			session.write(resp.build());
//			return;
			gongjinBean = new ResourceGongJin();
			gongjinBean.junzhuId = jzId;
		}else{
//			resetResourceGongJin(gongjinBean);
//			if(gongjinBean.lastJX<CanShu.LUEDUO_HAND_DAYMINNUM){
//				log.error("请求领取贡金奖励出错：{}的上缴的贡金未到达领奖要求", jzId);
//				resp.setResult(30);
//				resp.setAward("");
//				resp.setGuojiRank(-1);
//				resp.setLianMengRank(-1);
//				session.write(resp.build());
//				return;
//			}
		}
		if(gongjinBean.getDayAwardTime!=null){
			boolean isSameDay = DateUtils.isSameDay(gongjinBean.getDayAwardTime);	
			if(isSameDay){
				log.error("请求领取声望排行奖励出错：{}已领取本日奖励", jzId);
				resp.setResult(60);
				resp.setAward("");
				resp.setGuojiRank(-1);
				resp.setLianMengRank(-1);
				session.write(resp.build());
				return;
			}
		}
		AlliancePlayer apBean = HibernateUtil.find(AlliancePlayer.class, jzId);
		if (apBean == null) {
			log.error("请求领取国家声望日奖励出错：君主{}无联盟",jzId);
			resp.setResult(50);
			resp.setAward("");
			resp.setGuojiRank(-1);
			resp.setLianMengRank(-1);
			session.write(resp.build());
			return;
		}
		AllianceBean aBean = HibernateUtil.find(AllianceBean.class, apBean.lianMengId);
		if (aBean == null) {
			log.error("君主:{}请求领取国家声望日奖励出错,联盟：{}不存在", jzId,apBean.lianMengId);
			resp.setResult(50);
			resp.setAward("");
			resp.setGuojiRank(-1);
			resp.setLianMengRank(-1);
			session.write(resp.build());
			return;
		}
		//上一日国家排行
		Integer guojiRank=(int) RankingMgr.inst.getRankById(RankingMgr.GUOJIA_DAY_LAST_RANK, aBean.country);
		//上一日联盟在国家中的声望排行
		Integer lianMengRank=(int)RankingMgr.inst.getRankByGjIdAndId(RankingMgr.LIANMENG_SW_LAST_DAY_RANK, aBean.country, aBean.id);
		if(guojiRank<1){
			guojiRank=7;
		}
		if(lianMengRank<1){
			lianMengRank=10000;
		}
		log.info("{}请求领取每日国家声望排行奖励,上一日国家排名--{}，上一日联盟在国家中的声望排行--{}", jzId,guojiRank,lianMengRank);
		List<LueduoUnionRank> awardList=dayAwardMap.get(guojiRank);
		String award=null;
		for (LueduoUnionRank dayAward : awardList) {
			if(lianMengRank >= dayAward.getUnionRankMin() && 
					lianMengRank <= dayAward.getUnionRankMax()){
				award=dayAward.getAward();
			}
		}
		
		if(award==null){
			log.error("{}请求领取每日国家声望排行出错,未找到奖励配置", jzId);
			resp.setResult(40);
			resp.setAward("");
			resp.setGuojiRank(-1);
			resp.setLianMengRank(-1);
			session.write(resp.build());
			return;
		}
		giveAward(award, session, jz);
		gongjinBean.getDayAwardTime = new Date();
		HibernateUtil.save(gongjinBean);
		log.info("{}请求领取每日国家声望排行奖励成功", jzId);
		resp.setAward(award);
		resp.setResult(10);
		resp.setGuojiRank(guojiRank);
		resp.setLianMengRank(lianMengRank);
		session.write(resp.build());
	
	}
	//判断是否有每日奖励
	public boolean isDayAward(	ResourceGongJin gongjinBean){
		if(gongjinBean != null && gongjinBean.getDayAwardTime != null){
			boolean isSameDay = DateUtils.isSameDay(gongjinBean.getDayAwardTime);	
			if(isSameDay){
				log.info("君主国家声望每日排行奖励今日已领取过");
				return false;
			}
		}
		AlliancePlayer alBean = HibernateUtil.find(AlliancePlayer.class, gongjinBean.junzhuId);
		if (alBean == null || alBean.lianMengId <=0 ) {
			log.info("君主{}没有国家声望每日排行奖励可领，无联盟", gongjinBean.junzhuId);
			return false;
		}
		AllianceBean aBean = HibernateUtil.find(AllianceBean.class, alBean.lianMengId);
		if (aBean == null) {
			log.info("君主{}没有国家声望每日排行奖励可领，联盟{}不存在", gongjinBean.junzhuId,alBean.lianMengId);
			return false;
		}
		Integer guojiRank=(int) RankingMgr.inst.getRankById(RankingMgr.GUOJIA_DAY_LAST_RANK, aBean.country);
		Integer lianMengRank=(int)RankingMgr.inst.getRankByGjIdAndId(RankingMgr.LIANMENG_SW_WEEK_RANK, aBean.country, aBean.id);
		if(guojiRank<1){
			guojiRank=7;
		}
		if(lianMengRank<1){
			lianMengRank=10000;
		}
		log.info("君主{} 国家声望每日排行奖励判断国家排名---{}，联盟排名---{}", gongjinBean.junzhuId,guojiRank,lianMengRank);
		List<LueduoUnionRank> awardList=dayAwardMap.get(guojiRank);
		String award=null;
		for (LueduoUnionRank dayAward : awardList) {
			if(lianMengRank>=dayAward.getUnionRankMin()&&lianMengRank<=dayAward.getUnionRankMax()){
				award=dayAward.getAward();
			}
		}
		if(award==null){
			log.error("君主{}国家声望每日排行奖励无每日奖励，未找到奖励配置", gongjinBean.junzhuId);
			return false;
		}
		log.info("君主{}国家声望每日排行奖励有每日奖励", gongjinBean.junzhuId);
		return true;
		
	}
	//发放奖励
	public void giveAward(String goods,IoSession session,JunZhu jz){
		String[] goodsArray = goods.split("#");
		//增加物品
		for (String g : goodsArray) {
			String[] ginfo = g.split(":");
			AwardTemp award = new AwardTemp();
			award.setItemType(Integer.parseInt(ginfo[0]));
			award.setItemId(Integer.parseInt(ginfo[1]));
			award.setItemNum(Integer.parseInt(ginfo[2]));
			AwardMgr.inst.giveReward(session, award, jz);
		}

	}
//	/**
//	 * @Description 捐献贡金
//	 * @param id
//	 * @param builder
//	 * @param session
//	 */
//	public void getJuanXianGongjiniResp(int id, Builder builder, IoSession session) {
//			JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
//			if (jz == null) {
//				log.error("请求捐献贡金出错：君主不存在");
//				return;
//			}
//			long jzId=jz.id;
//			log.info("君主{}请求捐献贡金",jzId);
//			JuanXianGongJinResp.Builder resp=JuanXianGongJinResp.newBuilder();
//			if(!DateUtils.isInDeadline(CanShu.OPENTIME_LUEDUO, CanShu.CLOSETIME_LUEDUO)){
//				log.error("君主{}请求捐献贡金出错：现在不是上缴贡金的时间",jzId);
//				
//				resp.setResult(70);
//				resp.setGongxian(0);
//				resp.setLmshengwang(0);
//				resp.setGjshengwang(0);
//				resp.setNextNeedGongjin(1000);
//				session.write(resp.build());
//				return;
//			}
//			AlliancePlayer alBean = HibernateUtil.find(AlliancePlayer.class, jzId);
//			if (alBean == null) {
//				log.error("请求捐献贡金出错：君主{}无联盟",jzId);
//				resp.setResult(20);
//				resp.setGongxian(0);
//				resp.setLmshengwang(0);
//				resp.setGjshengwang(0);
//				resp.setNextNeedGongjin(1000);
//				session.write(resp.build());
//				return;
//			}
//			AllianceBean aBean = HibernateUtil.find(AllianceBean.class, alBean.lianMengId);
//			if (aBean == null) {
//				log.error("请求捐献贡金出错：{}的联盟{}不存在", jzId,alBean.lianMengId);
//				resp.setResult(30);
//				resp.setGongxian(0);
//				resp.setLmshengwang(0);
//				resp.setGjshengwang(0);
//				resp.setNextNeedGongjin(1000);
//				session.write(resp.build());
//				return;
//			}
//			GuoJiaBean gjBean = HibernateUtil.find(GuoJiaBean.class, jz.guoJiaId);
//			if (gjBean == null) {
//				gjBean=GuoJiaMgr.inst.initGuoJiaBeanInfo(jz.guoJiaId);
//			}
//			
//			ResourceGongJin gongjinBean =HibernateUtil.find(ResourceGongJin.class, jzId);
//			if(gongjinBean==null){
//				log.error("请求捐献贡金出错：{}的捐献贡金功能未开启", jzId);
//				resp.setResult(60);
//				resp.setGongxian(0);
//				resp.setLmshengwang(0);
//				resp.setGjshengwang(0);
//				resp.setNextNeedGongjin(1000);
//				session.write(resp.build());
//				return;
//			}
//			resetResourceGongJin(gongjinBean);
//
//			int gongjin =getGongJin(gongjinBean, PvpMgr.getJunxianLevel(jzId)); 
////			PvpBean p = HibernateUtil.find(PvpBean.class, jzId);
////			if(p != null){
////			gongjin = getGongJin(gongjinBean, p.junXianLevel);
////			}
//			int needGongjin=0;
//			int juanXianTimes=gongjinBean.todayJXTimes+1;
//			ShangJiaoTemp sj=shangJiaoMap.get(juanXianTimes);
//			if (sj!=null) {
//				log.info("君主{}本次需要上缴贡金次数为---第{}次，需要上缴值为{}",jzId,juanXianTimes,needGongjin);
//				needGongjin=sj.getNeedNum(); 
//			}else{
//				log.info("请求捐献贡金失败：君主{}上缴贡金所需配置未找到，上缴次数用完",jzId);
//				resp.setResult(50);
//				resp.setGongxian(0);
//				resp.setLmshengwang(0);
//				resp.setGjshengwang(0);
//				resp.setNextNeedGongjin(0);
//				session.write(resp.build());
//				return;
//			}
//			if(gongjin<needGongjin){
//				log.info("请求捐献贡金失败：君主{}贡金{}未达到捐献要求{}",jzId,gongjin,needGongjin);
//				resp.setResult(40);
//				resp.setGongxian(0);
//				resp.setLmshengwang(0);
//				resp.setGjshengwang(0);
//				resp.setNextNeedGongjin(1000);
//				session.write(resp.build());
//				return;
//			}
//			Date now =new Date();
//			gongjinBean.todayJX+=needGongjin;
//			gongjinBean.juanXianTime=now;
//			boolean isSameWeek = DateUtils.isSameWeek_CN(now,gongjinBean.juanXianTime);
//			if(isSameWeek){
//				gongjinBean.thisWeekJX+=needGongjin;
//				log.info("同一周，君主{}本周捐献值增加{},变为{}",jzId,needGongjin,gongjinBean.thisWeekJX);
//			}
//			gongjinBean.todayJXTimes+=1;
//			log.info("君主{}达成上缴贡金条件，扣除个人贡金{}",jzId,needGongjin);
//			//扣除个人贡金
//			changeGongJin(gongjinBean, -needGongjin);
//			
//			//发放个人奖励
//			synchronized (alBean) {
//				log.info("君主{}上缴贡金{},获得个人奖励{}",jzId,needGongjin,sj.getPersonAward());
//				alBean.gongXian+=sj.getPersonAward();
//				HibernateUtil.save(alBean);
//			}
//			AllianceMgr.inst.changeGongXianRecord(alBean.junzhuId, sj.getPersonAward());
//			//发放联盟声望增值
//
//			AllianceMgr.inst.changeAlianceReputation(aBean, sj.getUnionAward());
//			//发放国家声望
//			changeGuoJiaShengWang(gjBean, sj.getCountryAward());
//			
//			//上缴贡金事件
//			EventMgr.addEvent(ED.SHANGJIAO_XIAYI, new Object[] { jzId });
//			int nextJuanXianTimes=gongjinBean.todayJXTimes+1;
//			ShangJiaoTemp sj2Next=shangJiaoMap.get(nextJuanXianTimes);
//			int nextNeedGongjin=0;
//			if(sj2Next!=null){
//				nextNeedGongjin=sj2Next.getNeedNum(); 
//			}else{
//				//1.0版本加入
//				Integer max=shangJiaoMap.size();
//				sj2Next=shangJiaoMap.get(max);
//				if(sj2Next!=null){
//					needGongjin=sj2Next.getNeedNum();
//					log.info("君主{}下次---第{}次--上缴次数已满，默认下次上缴为最大次数需要上缴贡金值{}",jzId,nextJuanXianTimes,needGongjin);
//				}else{
//					log.info("君主{}下次---第{}次--上缴次数已满，需要上缴贡金未找到配置，置为10000",jzId,nextJuanXianTimes);
//					needGongjin=10000;
//				}
//			}
//			log.info("君主{}下次需要上缴贡金次数为---第{}次，需要上缴值为{}",jzId,nextJuanXianTimes,nextNeedGongjin);
//			int gongXian=0;
//			int lmShengwang=0;
//			int gjShengwang=0;
//			if(sj!=null){
//				gongXian=sj.getPersonAward();
//				lmShengwang=sj.getUnionAward();
//				gjShengwang=sj.getCountryAward();
//			}
//			log.info("君主{}本次上缴活动个人贡献--{}，联盟声望增加--{},国家声望增加--{}",jzId,gongXian,lmShengwang,gjShengwang);
//			resp.setResult(10);
//			resp.setGongxian(gongXian);
//			resp.setLmshengwang(lmShengwang);
//			resp.setGjshengwang(gjShengwang);
//			resp.setNextNeedGongjin(nextNeedGongjin);
//			//返回更新后主页面
//			GuoJiaMainInfoResp.Builder mainResp=GuoJiaMainInfoResp.newBuilder();
//			getGuoJiaMainInfo(jzId, gjBean, gongjinBean, mainResp);
//			resp.setGjmainInfo(mainResp.build());
//			session.write(resp.build());
//			
//			String eventStr = AllianceMgr.inst.lianmengEventMap.get(15).str
//					.replaceFirst("%d", jz.name)
//					.replaceFirst("%d", String.valueOf(needGongjin))
//					.replaceFirst("%d", String.valueOf(sj.getUnionAward()));
//			AllianceMgr.inst.addAllianceEvent(aBean.id, eventStr);
//			/*
//			 * 捐献贡金一次
//			 */
//			EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
//					jzId, DailyTaskConstants.give_gongJin, 1));
//			EventMgr.addEvent(ED.JUAN_XIAN_GONG_JIN, new Object[]{jz, session, gongjinBean.todayJXTimes,aBean});
//			// 主线任务: 成功上缴1次贡金算完成任务  20190916
//			EventMgr.addEvent(ED.give_gongjin , new Object[] { jz.id});
//	}
//	
	/**
	 * @Description 获取主页面信息
	 * @param guo
	 * @param which
	 */
	public void  getGuoJiaMainInfo(long  jzId,GuoJiaBean gjBean,ResourceGongJin gongjinBean,GuoJiaMainInfoResp.Builder mainResp){
		log.info("君主{}获取国家主页信息  --getGuoJiaMainInfo",jzId);
		//当前国家排行
//		List<GuoJiaBean> gjNowRankList=RankingMgr.inst.getGuojiaWeekRank();
		List<GuoJiaBean> gjNowRankList=RankingMgr.inst.getGuojiaRank(RankingMgr.GUOJIA_WEEK_RANK);
//		List<GuoJiaBean> gjNowRankList=HibernateUtil.list(GuoJiaBean.class,"where 1=1");
		int rankSize=gjNowRankList.size();
		for (int i = 0; i < rankSize; i++) {
			GuoJiaBean guoJiaBean=gjNowRankList.get(i);
			GuojiaRankInfo.Builder nowResp=GuojiaRankInfo.newBuilder();
			nowResp.setGuojiaId(guoJiaBean.guoJiaId);
			nowResp.setRank(i);
			nowResp.setShengwang(guoJiaBean.shengWang);
			mainResp.addNowRank(nowResp.build());
		}
		//上期国家排行
//		List<GuoJiaBean> gjLastRankList=RankingMgr.inst.getGuojiaLastWeekRank();
		List<GuoJiaBean> gjLastRankList=RankingMgr.inst.getGuojiaRank(RankingMgr.GUOJIA_WEEK_LAST_RANK);
		int lastRankSize=gjLastRankList.size();
		for (int i = 0; i < lastRankSize; i++) {
			GuoJiaBean guoJiaBean=gjLastRankList.get(i);
			GuojiaRankInfo.Builder lastResp=GuojiaRankInfo.newBuilder();
			lastResp.setGuojiaId(guoJiaBean.guoJiaId);
			lastResp.setRank(i);
			lastResp.setShengwang(guoJiaBean.shengWang);
			mainResp.addLastRank(lastResp.build());
		}
		mainResp.setGuojiaId(gjBean.guoJiaId);
		//国王定为 无
		mainResp.setKingId(-1);
		mainResp.setKingName("无");
		boolean isCanGive=DateUtils.isInDeadline(CanShu.OPENTIME_LUEDUO, CanShu.CLOSETIME_LUEDUO);
		mainResp.setIsCanGive(isCanGive);
		mainResp.setTodayGive(0);//(gongjinBean.todayJX);
		mainResp.setThisWeekGive(0);//(gongjinBean.thisWeekJX);
		mainResp.setHateGuoId1(gjBean.diDuiGuo_1);
		mainResp.setHateGuoId2(gjBean.diDuiGuo_2);
		mainResp.setMyGongJin(0);//(gongjinBean.gongJin);
//		int juanXianTimes=gongjinBean.todayJXTimes+1;
//		ShangJiaoTemp sj=shangJiaoMap.get(juanXianTimes);
		//int needGongjin=0;
//		if(sj!=null){
//			needGongjin=sj.getNeedNum(); 
//		}else{
//			//1.0版本加入
//			Integer max=shangJiaoMap.size();
//			sj=shangJiaoMap.get(max);
//			if(sj!=null){
//				needGongjin=sj.getNeedNum();
//				log.info("君主{}下次---第{}次--上缴次数已满，默认下次上缴为最大次数需要上缴贡金值{}",jzId,juanXianTimes,needGongjin);
//			}else{
//				log.info("君主{}下次---第{}次--上缴次数已满，需要上缴贡金未找到配置，置为10000",jzId,juanXianTimes);
//				needGongjin=10000;
//			}
//		}
//		log.info("君主{}下次需要上缴贡金次数为---第{}次，需要上缴值为{}",jzId,juanXianTimes,needGongjin);
		mainResp.setShouldGive(0);//(needGongjin);
		//有每日奖励为1 没有每日奖励为0
		String  dayAward=isDayAward(gongjinBean)?"1":"0";
		mainResp.setGuojiaAward(dayAward);
	}
	
	public static int getHate(GuoJiaBean guo, int which){
		switch(which){
		case guoJia_1:
			return guo.hate_1;
		case guoJia_2:
			return guo.hate_2;
		case guoJia_3:
			return guo.hate_3;
		case guoJia_4:
			return guo.hate_4;
		case guoJia_5:
			return guo.hate_5;
		case guoJia_6:
			return guo.hate_6;
		case guoJia_7:
			return guo.hate_7;
		}
		return 0;
	}

	public  int changeGuoJiaShengWang(GuoJiaBean gjBean, int changeValue){
		if(gjBean == null){
			return 0;
		}
		if(changeValue == 0){
			return gjBean.shengWang;
		}
		boolean change = false;
		synchronized (gjBean) {
			log.info("国家{}声望变化{}",gjBean.guoJiaId,changeValue);
			gjBean.shengWang += changeValue;
			if (gjBean.shengWang <= 0) {
				gjBean.shengWang = 0;
			}
			HibernateUtil.save(gjBean);
			change = true;
		}
		if(change){
			// 添加国家声望榜刷新事件 @何金成
			EventMgr.addEvent(ED.GUOJIA_DAY_RANK_REFRESH, new Object[]{gjBean,changeValue});
			EventMgr.addEvent(ED.GUOJIA_WEEK_RANK_REFRESH, new Object[]{gjBean,changeValue});
		}
		return gjBean.shengWang;
	}

//	/**
//	 * 君主的贡金是一个随时在改变的数值，不能直接去LveDuoBean获取，
//	 * 只能获取时时时计算，且改变时，也时时计算
//	 * @Title: getAndSetGongJin 
//	 * @Description:
//	 * @param jId
//	 * @param junXianLevel
//	 * @param changeValue
//	 * @return
//	 */
//	public  int getGongJin(long jId, int junXianLevel){
//		ResourceGongJin gong = HibernateUtil.find(ResourceGongJin.class, jId);
//		return getGongJin(gong, junXianLevel);
//	}
	
//	public  int getGongJin(ResourceGongJin gong, int junXianLevel){
//		if(gong == null){
//			return 0;
//		}
//		// 无联盟不生产贡金
//		AlliancePlayer player = HibernateUtil.find(AlliancePlayer.class, gong.junzhuId);
//		if(player == null || player.lianMengId <=0){
//			return gong.gongJin;
//		}
//		BaiZhan bz = PvpMgr.inst.baiZhanMap.get(junXianLevel);
//		Date lastDate = gong.lastGetGongJinTime;
//		if(lastDate != null && bz != null){
//			int produceTime =0;
//			Date nowDate = new Date();
//			long now = nowDate.getTime();
//			long last = lastDate.getTime();
//			boolean isSameDay = DateUtils.isSameDay(lastDate);
//			boolean nowFlag = DateUtils.isInDeadline(CanShu.OPENTIME_LUEDUO, CanShu.CLOSETIME_LUEDUO);
//			if (isSameDay) {
//				if(nowFlag){
//					produceTime = (int)(now -last)/1000;
//				}
//			} else {
//				int jiange=(int)(now -last)/1000;
//				int shengyuTime=jiange%86400;
//				boolean lastFlag =  DateUtils.isInDeadline(CanShu.OPENTIME_LUEDUO,
//						CanShu.CLOSETIME_LUEDUO, lastDate );
//				produceTime = (jiange-shengyuTime)*16/24;
//				int nowTemp =  nowDate.getHours() * 3600 +
//						nowDate.getMinutes() * 60 + nowDate.getSeconds();
//				int lastTemp = lastDate.getHours() * 3600 +
//						lastDate.getMinutes() * 60 + lastDate.getSeconds();
//				if(!nowFlag && !lastFlag){
//					produceTime += 16 * 3600;
//				}else if(nowFlag && !lastFlag){
//					produceTime += nowTemp - (8 * 3600);
//				}else if(!nowFlag && lastFlag){
//					produceTime += 24 *3600 - lastTemp;
//				}else if(nowFlag && lastFlag){
//					if(nowTemp < lastTemp){
//						produceTime += shengyuTime - 8 * 3600;
//					}else {
//						produceTime += shengyuTime;
//					}
//				}
//			
//			}
//			int changValue =( produceTime *  bz.produceSpeed)/60;
//			log.info("君主{}贡金生产，产出贡金{}",gong.junzhuId,changValue);
//			return changeGongJin(gong, changValue);
//		}
//		return gong.gongJin;
//	}
//
//	public int changeGongJin(ResourceGongJin gonj, int changeValues){
//		if(gonj==null){
//			return 0;
//		}
//		if(changeValues==0){
//			return gonj.gongJin;
//		}
//		synchronized (gongJinLock) {
//			log.info("君主{}贡金{}变化{}",gonj.junzhuId,gonj.gongJin,changeValues);
//			gonj.gongJin += changeValues;
//			gonj.lastGetGongJinTime = new Date();
//			if (gonj.gongJin <= 0) {
//				gonj.gongJin = 0;
//			}
//			HibernateUtil.save(gonj);
//			return gonj.gongJin;
//		}
//	}
//	//推送可以捐献
//	public void pushCanShangjiao(long jzId){
//		SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
//		if (su != null)
//		{
//			log.info("向君主{}推送贡金可以上缴",jzId);
//			FunctionID.pushCanShangjiao(jzId, su.session, FunctionID.ShangJiao4Gongjin);
//		}
//	}
//	//推送取消捐献红点
//	public void pushCancleShangjiao(long jzId){
//		SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
//		if (su != null)
//		{
//			log.info("向君主{}推送取消贡金上缴红点",jzId);
//			FunctionID.pushCanShangjiao(jzId, su.session, -FunctionID.ShangJiao4Gongjin);
//		}
//	}
//	//请求判断贡金是否可以上缴
//	public  void isCanShangjiao(JunZhu jz,IoSession session) {
//		if (jz == null) {
//			log.error("向君主推送贡金可以上缴失败：君主不存在");
//			return;
//		}
//		long jzId=jz.id;
//		int level=jz.level;
//		boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.ShangJiao4Gongjin, jzId, level);
//		if(!isOpen){
//			log.info("君主--{}的功能---{}未开启,不推送",jzId,FunctionID.ShangJiao4Gongjin);
//			return;
//		}
//		log.info("君主{}请求判断贡金是否可以上缴",jzId);
//		if(isCanShangjiao(jzId)){
//			FunctionID.pushCanShangjiao(jzId, session, FunctionID.ShangJiao4Gongjin);
//		}else{
//			log.info("君主{}贡金未达到上缴条件",jzId);
//		}
//	}
//	//判断是否可以上缴
//	public boolean  isCanShangjiao(	long jzId){
//		JunZhu jz=HibernateUtil.find(JunZhu.class, jzId);
//		if(!DateUtils.isInDeadline(CanShu.OPENTIME_LUEDUO, CanShu.CLOSETIME_LUEDUO)){
//			return false;
//		}
//		AlliancePlayer alBean = HibernateUtil.find(AlliancePlayer.class, jzId);
//		if (alBean == null) {
//			return false;
//		}
//		AllianceBean aBean = HibernateUtil.find(AllianceBean.class, alBean.lianMengId);
//		if (aBean == null) {
//			return false;
//		}
//		GuoJiaBean gjBean = HibernateUtil.find(GuoJiaBean.class, jz.guoJiaId);
//		if (gjBean == null) {
//			gjBean=GuoJiaMgr.inst.initGuoJiaBeanInfo(jz.guoJiaId);
//		}
//		
//		ResourceGongJin gongjinBean =HibernateUtil.find(ResourceGongJin.class, jzId);
//		if(gongjinBean==null){
//			return false;
//		}
//		
//		int needGongjin=0;
//		int juanXianTimes=gongjinBean.todayJXTimes+1;
//		ShangJiaoTemp sj=shangJiaoMap.get(juanXianTimes);
//		if (sj!=null) {
//			needGongjin=sj.getNeedNum(); 
//		}else{
//			log.info("君主--{}，本日上缴次数已满",jzId);
//			return false;
//		}
//		int gongjin =gongjinBean.gongJin;
//		if(gongjin<needGongjin){
//			return false;
//		}
//		return true;
//	}
//	public ResourceGongJin initjzGongJinInfo(Long jzId, Date startTime,ResourceGongJin bean) {
//		// 获取数据库中是否有此记录，有的话什么也不做
//		log.info("初始化{}的贡金数据，时间-{}", jzId,startTime);
//		bean=new ResourceGongJin();
//		bean.gongJin=0;
//		bean.junzhuId=jzId;
//		bean.lastGetGongJinTime=startTime;
//		bean.lastJX=0;
//		bean.lastRestTime=startTime;
//		bean.lastWeekJX=0;
//		bean.thisWeekJX=0;
//		bean.todayJX=0;
//		bean.todayJXTimes=0;
//		bean.getWeekAwardTime = new Date();
//		HibernateUtil.insert(bean);
//		log.info("玩家id是 ：{}的 贡金数据库记录ResourceGongJin生成成功", jzId);
//		return bean;
//	}
	public GuoJiaBean initGuoJiaBeanInfo(int guojiaId) {
		// 获取数据库中是否有此记录，有的话什么也不做
		log.info("初始化国家{}数据，时间-{}", guojiaId,new Date());
		GuoJiaBean bean = new GuoJiaBean();
		bean.guoJiaId=guojiaId;
		HibernateUtil.insert(bean);
		return bean;
	}
	public void initGuoJiaBeanInfo() {
		// 获取数据库中是否有此记录，有的话什么也不做
		log.info("初始化国家数据，时间-{}",new Date());
		for (int i = 1; i < 8; i++) {
			GuoJiaBean bean = HibernateUtil.find(GuoJiaBean.class, i);
			if(bean==null){
				GuoJiaBean gjbean = new GuoJiaBean();
				gjbean.guoJiaId=i;
				gjbean.diDuiGuo_1 = getRandomDiDui(i, -1);
				gjbean.diDuiGuo_2 = getRandomDiDui(i, gjbean.diDuiGuo_1);
				HibernateUtil.insert(gjbean);
			}
		}
		log.info("国家-GuoJiaBean生成成功");
	}
	public int getRandomDiDui(int excep1, int excep2){
		int k = 0;
		do{
			k = MathUtils.getRandomInMax(guoJia_1, guoJia_7);
		}while(k == excep1 || k == excep2);
		return k;
	}
//	
//	public void resetResourceGongJin(ResourceGongJin bean){
//		if(bean == null){
//			return;
//		}
//		boolean isSameDay = DateUtils.isSameDay(bean.lastRestTime);
//		if (isSameDay) {
//			// do nothing
//		} else {
//			log.info("不是同一天，君主{}贡金数据更新",bean.junzhuId);
//			
//			Calendar calendar = Calendar.getInstance();
//			Date now=new Date();
//			calendar.setTime(now);
//			calendar.add(Calendar.DAY_OF_YEAR,-1);
//			Date yesterday = calendar.getTime();
//			boolean isSameDay2yesterday = DateUtils.isSameDay(yesterday,bean.juanXianTime);
//			if(isSameDay2yesterday){
//				log.info("君主{}上次捐献时间是昨天，更新上日捐献贡金值{}=》{}",bean.junzhuId,bean.lastJX,bean.todayJX);
//				bean.lastJX=bean.todayJX;
//			}else{
//				log.info("君主{}上次捐献时间不是昨天，重置上日捐献贡金值-{}",bean.junzhuId,bean.lastJX);
//				bean.lastJX=0;
//			}
//			boolean isSameWeek = DateUtils.isSameWeek_CN(now,bean.lastRestTime);
//			if(!isSameWeek){
//				log.info("和本周不是同一周，君主{}本周捐献贡金值重置",bean.junzhuId);
//				calendar.setTime(now);
//				calendar.add(Calendar.DAY_OF_YEAR,-7);
//				Date weekAgo = calendar.getTime();
//				//是否是上一周
//				boolean isSameWeek2weekAgo = DateUtils.isSameWeek_CN(weekAgo,bean.lastRestTime);
//				if(isSameWeek2weekAgo){
//					log.info("和前一周是同一周，君主{}上周捐献贡金值更新从{}变为{}",bean.junzhuId,bean.lastWeekJX,bean.thisWeekJX);
//					bean.lastWeekJX=bean.thisWeekJX;
//				}else {
//					log.info("和前一周不是是同一周，君主{}上周捐献贡金值重置",bean.junzhuId);
//					bean.lastWeekJX=0;
//				}
//				bean.thisWeekJX=0;
//			}
////			bean.todayJX=0;
////			bean.todayJXTimes=0;
//			bean.lastRestTime=now;
//			HibernateUtil.save(bean);
//		}
//	}

	/**
	 * 
	 * @Title: updateCountryHate 
	 * @Description:
	 * @param guojiaId
	 * @param changeGuoJiaId
	 * @param addHate
	 */
	public static void updateCountryHate(int guojiaId, int changeGuoJiaId, int addHate) {
		if(guojiaId == changeGuoJiaId){
			return;
		}
		GuoJiaBean gjBean = HibernateUtil.find(GuoJiaBean.class, guojiaId);
		if (gjBean == null) {
			gjBean=GuoJiaMgr.inst.initGuoJiaBeanInfo(guojiaId);
		}
		synchronized (gjBean) {
			switch (changeGuoJiaId) {
			case 1:
				gjBean.hate_1+=addHate;
				break;
			case 2:
				gjBean.hate_2+=addHate;
				break;
			case 3:
				gjBean.hate_3+=addHate;
				break;
			case 4:
				gjBean.hate_4+=addHate;
				break;
			case 5:
				gjBean.hate_5+=addHate;
				break;
			case 6:
				gjBean.hate_6+=addHate;
				break;
			case 7:
				gjBean.hate_7+=addHate;
				break;
			default:
				log.error("敌方国家{}编码错误，仇恨增加失败",changeGuoJiaId); 
				break;
			}
			log.info("国家：{}对国家：{}的仇恨值增加了：{}", guojiaId, changeGuoJiaId, addHate);
			HibernateUtil.save(gjBean);
		}
	}

	public void sendWeekRankWard(JunZhu jz){
		if(jz == null){
			log.error("发送国家声望周排行奖励失败，玩家不存在");
			return;
		}
		long jId = jz.id;
		ResourceGongJin gongjinBean = HibernateUtil.find(ResourceGongJin.class, jId);
//		if(gongjinBean == null){
//			log.error("玩家：{}还没有捐献贡金的记录", gongjinBean);
//			return;
//		}
		if(gongjinBean != null && gongjinBean.getWeekAwardTime != null){
			if(DateUtils.isSameWeek_CN(gongjinBean.getWeekAwardTime, new Date())){
				log.info("玩家：{}国家声望周排行奖励方法return，本周已经领取过", jId );
				return;
			}
		}
		
		/*
		 * reset
		 */
//		resetResourceGongJin(gongjinBean);
//		if(gongjinBean.lastWeekJX < CanShu.LUEDUO_HAND_WEEKMINNUM){
//			log.error("玩家：{}上周缴纳的贡金值太少，所以没有周排行奖励");
//			return; 
//		}
		AlliancePlayer pl = HibernateUtil.find(AlliancePlayer.class, jId);
		if(pl == null){
			log.info("玩家:{}国家声望周排行奖励方法return，不是联盟玩家", jId);
			return;
		}
//		Integer guojiRank = (int) RankingMgr.inst.getRankAtGuojiaWeek(jz.guoJiaId);
//		Integer lianMengRank = (int) RankingMgr.inst.getRankAtLianmengBySW(pl.lianMengId, jz.guoJiaId);
		// 因为代码执行是在重置排行榜之后，所以用last排行榜的数据进行奖励分配。
		Integer guojiRank = (int) RankingMgr.inst.getRankById(RankingMgr.GUOJIA_WEEK_LAST_RANK, jz.guoJiaId);
		Integer lianMengRank = (int) RankingMgr.inst.getRankByGjIdAndId(
				RankingMgr.LIANMENG_SW_LAST_WEEK_RANK, jz.guoJiaId, pl.lianMengId);
		if(guojiRank < 1 || lianMengRank < 1){
			log.error("玩家：{}发送国家声望周排行奖励失败，所在国家或者联盟声望排行出错", jId);
			return;
		}
		
		List<LueduoUnionRank> awardList = dayAwardMap.get(guojiRank);
		String award = null;
		for (LueduoUnionRank dayAward : awardList) {
			if(lianMengRank >= dayAward.getUnionRankMin() &&
					lianMengRank <= dayAward.getUnionRankMax()){
				// 周奖励
				award = dayAward.bigAward;
				break;
			}
		}
		if(award == null){
			log.error("{}发送国家声望周排行奖励出错,未找到奖励配置", jId);
			return;
		}
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(100001);
		if (mailConfig == null) {
			log.error("mail.xml配置文件找不到type=100001的数据");
			return;
		}
		String content = mailConfig.content.
				replace("***", guojiRank+"").replace("###", lianMengRank+"");
		String senderName = mailConfig.sender;
		boolean suc = EmailMgr.INSTANCE.sendMail(jz.name, content,
				award.toString(), senderName, mailConfig, "");
		if (suc) {
			log.info("玩家：{}发送国家声望周排行奖励邮件，发送成功,award 是：{}, 发送时间是：{}", 
					jId, award, new Date());
		} else {
			log.error("玩家:{}发送国家声望周排行奖励邮件失败", jz.name);
		}
		if(gongjinBean == null){
			gongjinBean = new ResourceGongJin();
			gongjinBean.junzhuId = jz.id;
		}
		gongjinBean.getWeekAwardTime = new Date();
		HibernateUtil.save(gongjinBean);
		log.info("玩家：{}国家声望周排行奖励邮件发送，gongjinbean保存成功，其中"
				+ "guojiRank：{}， lianMengRank：{}，发送邮件结果success：{} ",
				jz.id, guojiRank, lianMengRank, suc);
	}
	/**
	 * @Description 计算敌对国
	 * @param nowBean
	 * @return
	 */
	public Integer[] getDiDuiGuo(int[] jiesuanhate,int guojiaId) {
		TreeMap<Integer,Integer> hateMap=new TreeMap<Integer,Integer>();
		hateMap.put(1, jiesuanhate[0]);
		hateMap.put(2, jiesuanhate[1]);
		hateMap.put(3, jiesuanhate[2]);
		hateMap.put(4, jiesuanhate[3]);
		hateMap.put(5, jiesuanhate[4]);
		hateMap.put(6, jiesuanhate[5]);
		hateMap.put(7, jiesuanhate[6]);
		hateMap.remove(guojiaId);
		int maxhate1 =MathUtils.getMax4Map(hateMap) ;
		hateMap.remove(maxhate1);
		int maxhate2= MathUtils.getMax4Map(hateMap); 
		log.info("计算国家{}敌对国为{}和{}",guojiaId,maxhate1,maxhate2);
		return new Integer[]{maxhate1,maxhate2};
	}

//	/**
//	 * 注意：在退出联盟之前调用（也就是alliancePlayer.lianmengId = 0 之前调用）
//	 * @param jId
//	 */
//	public void calculateGongJinBeforeQuitAlliance(long jId){
//		getGongJin(jId, PvpMgr.getJunxianLevel(jId));
//	}
//	/**
//	 * 加入联盟的时候调用
//	 * @param jId
//	 */
//	public void calculateGongJinJoinAlliance(long jId){
//		ResourceGongJin gong = HibernateUtil.find(ResourceGongJin.class, jId);
//		if(gong != null){
//			gong.lastGetGongJinTime = new Date();
//			HibernateUtil.save(gong);
//		}
//	}

	@Override
	public void run() {
		while (GameServer.shutdown == false) {
			Mission m = null;
			try {
				m = missions.take();
			} catch (InterruptedException e) {
				log.error("interrupt", e);
				continue;
			}
			if (m == exit) {
				break;
			}
			try {
				handle(m);
			} catch (Throwable e) {
				log.info("异常协议{}", m.code);
				log.error("处理出现异常", e);
			}
		}
		log.info("退出掠夺管理类");
	}
	
	public void addMission(int id, IoSession session, Builder builder) {
		Mission m = new Mission(id, session, builder);
		missions.add(m);
	}

	public void shutdown() {
		missions.add(exit);
	}
	public void handle(Mission m) {
		int id = m.code;
		IoSession session = m.session;
		Builder builder = m.builer;
		switch (m.code) {
		case PD.C_GET_JUANXIAN_GONGJIN_REQ://请求捐献贡金
//			getJuanXianGongjiniResp(id, builder, session);
			break;
		case PD.C_GET_JUANXIAN_DAYAWARD_REQ://请求捐献贡金 日奖励
			getGongjinAwardResp(id, builder, session);
			break;
		case PD.GUO_JIA_MAIN_INFO_REQ://请求国家主页
			getGuoJiaMainInfoResp(id, builder, session);
			break;
//		case PD.C_ISCAN_JUANXIAN_REQ://请求判断贡金是否可以上缴
//			isCanShangjiao(id, builder, session);
//			break;
		default:
			log.error("未处理的消息{}", id);
			break;
		}
		
	}
	
	
	@Override
	public void proc(Event e) {
		if(e == null){
			return;
		}
		if(e.param != null){
			switch(e.id){
//			case ED.GET_JUNXIAN:
//				if(e.param == null || e.param instanceof JunZhu){
//					//2015年9月18日 基本无用的判断
//					log.error("从有军衔开始就进行贡金的记录,GET_JUNXIAN事件触发失败Exception e.param == null && e.param instanceof JunZhu");
//					break;
//				}
//				log.info("从有军衔开始就进行贡金的记录,GET_JUNXIAN事件触发");
//				Object[] param = (Object[]) e.param;
//				long jzId =(Long) param[0];
//				PvpBean pvpBean= (PvpBean)param[1];
//				Date startTime=null;
//				if(pvpBean==null){
//					startTime=new Date();
//					log.error("君主{}的PvpBean为空--------------",jzId);
//				}else{
//					startTime=pvpBean.initPvpTime;
//				}
//
//				ResourceGongJin gongjinBean =HibernateUtil.find(ResourceGongJin.class,jzId);
//				if(gongjinBean==null){
//					gongjinBean=initjzGongJinInfo(jzId, startTime,gongjinBean);
//				}else{
//					gongjinBean.lastGetGongJinTime=startTime;
//					HibernateUtil.save(gongjinBean);
//				}
//				break;
//			case ED.REFRESH_TIME_WORK:
//				log.info("定时刷新贡金");
//				IoSession session=(IoSession) e.param;
//				if(session==null){
//					log.error("定时刷新贡金错误，session为null");
//					break;
//				}
//				JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
//				if(jz==null){
//					log.error("定时刷新贡金错误，JunZhu为null");
//					break;
//				}
//				isCanShangjiao(jz, session);
//				log.info("定时刷新完成");
//				break;
			default:
				log.error("错误事件参数",e.id);
				break;
			}
		}

	}

	public boolean verifyGuoJiaExist(byte guoJiaId) {
		for(byte id : guoJiaIds) {
			if(id == guoJiaId) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void doReg() {
//		EventMgr.regist(ED.GET_JUNXIAN, this);
//		//定时刷新 2015年9月17日
//		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
	}

}
