package com.qx.pvp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.chat.ChatMgr;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.DescId;
import com.manu.dynasty.template.GongjiType;
import com.manu.dynasty.template.GuYongBing;
import com.manu.dynasty.template.LianmengEvent;
import com.manu.dynasty.template.LueduoLianmengRank;
import com.manu.dynasty.template.LueduoPersonRank;
import com.manu.dynasty.template.Mail;
import com.manu.dynasty.template.VipFuncOpen;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.qx.account.FunctionOpenMgr;
import com.qx.activity.ActivityMgr;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.award.AwardMgr;
import com.qx.bag.EquipMgr;
import com.qx.email.EmailMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.guojia.GuoJiaBean;
import com.qx.guojia.GuoJiaMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.mibao.MibaoMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.pve.PveMgr;
import com.qx.ranking.RankingGongJinMgr;
import com.qx.ranking.RankingMgr;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.timeworker.FunctionID;
import com.qx.util.TableIDCreator;
import com.qx.vip.VipData;
import com.qx.vip.VipMgr;
import com.qx.world.Mission;
import com.qx.yuanbao.YBType;

import qxmobile.protobuf.Chat.ChatPct;
import qxmobile.protobuf.Chat.ChatPct.Channel;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.LveDuo.Bing;
import qxmobile.protobuf.LveDuo.GuoInfo;
import qxmobile.protobuf.LveDuo.LveBattleEndReq;
import qxmobile.protobuf.LveDuo.LveBattleEndResp;
import qxmobile.protobuf.LveDuo.LveBattleItem;
import qxmobile.protobuf.LveDuo.LveBattleRecordResp;
import qxmobile.protobuf.LveDuo.LveConfirmReq;
import qxmobile.protobuf.LveDuo.LveConfirmResp;
import qxmobile.protobuf.LveDuo.LveDuoInfoResp;
import qxmobile.protobuf.LveDuo.LveGoLveDuoReq;
import qxmobile.protobuf.LveDuo.LveGoLveDuoResp;
import qxmobile.protobuf.LveDuo.LveHelpReq;
import qxmobile.protobuf.LveDuo.LveNextItemReq;
import qxmobile.protobuf.LveDuo.LveNextItemResp;
import qxmobile.protobuf.Ranking.JunZhuInfo;
import qxmobile.protobuf.Ranking.LianMengInfo;
import qxmobile.protobuf.ZhanDou;
import qxmobile.protobuf.ZhanDou.Group;
import qxmobile.protobuf.ZhanDou.Node;
import qxmobile.protobuf.ZhanDou.NodeProfession;
import qxmobile.protobuf.ZhanDou.NodeType;
import qxmobile.protobuf.ZhanDou.PvpZhanDouInitReq;
import qxmobile.protobuf.ZhanDou.ZhanDouInitError;
import qxmobile.protobuf.ZhanDou.ZhanDouInitResp;

/**
 * 
 * This class is used for 
 * 掠夺管理类
 * @author wangZhuan
 * @version   
 *       9.0, 2015年7月15日 下午1:59:21
 */
public class LveDuoMgr extends EventProc implements Runnable{
	public static LveDuoMgr inst;
	public static Logger log = LoggerFactory.getLogger(LveDuoMgr.class);
	public static Mission exit = new Mission(0, null, null);
	public LinkedBlockingQueue<Mission> missions = new LinkedBlockingQueue<Mission>();
	
	public static Map<Long, BattleInfo4Pvp> fightingLock = new HashMap<Long, BattleInfo4Pvp>();
	public static Map<Long, Long[]> prepareLock = new HashMap<Long, Long[]>();
	public static Map<Integer, GuYongBing> bingMap = new HashMap<Integer, GuYongBing>();
	public static int lveduoOpenId = 211;
	/*
	 * enemyId, 站位， guyongbingId, hp
	 */
	public static  ConcurrentHashMap<Long, Map<Integer, int[]>> ldnpcMapHP = 
			new  ConcurrentHashMap<Long, Map<Integer, int[]>>();
	/*
	 * 回血时间
	 */
	public static ConcurrentHashMap<Long, Long> rollbackHpTime = 
			new ConcurrentHashMap<Long, Long>();
			
	/* 战斗id */
//	public AtomicInteger zhandouIdMgr = new AtomicInteger(1);
	public static Map<Long, int[]> gongJiBingMap = new HashMap<Long,int[]>();
	public static Map<Long, Integer> willLostGongJin = new HashMap<Long, Integer>(); 
	public static String helpContent="";
	
	public LveDuoMgr(){
		inst = this;
		List<GuYongBing> guYongBingList = TempletService.listAll(GuYongBing.class.getSimpleName());
		for (GuYongBing elem : guYongBingList) {
			bingMap.put(elem.id, elem);
		}
		//2015年8月10日 1500436 ==》730001
		DescId desc = ActivityMgr.descMap.get(730001);
		if(desc != null){
			helpContent = desc.getDescription();
		}
		// 开启线程
		new Thread(this, "LveDuoMgr").start();
	}
	
	public void getMainLveDuoInfo(int id, IoSession session, Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("掠夺主页出错：君主不存在");
			return;
		}
		AlliancePlayer player = HibernateUtil.find(AlliancePlayer.class, jz.id);
		if(player == null || player.lianMengId <= 0){
			log.error("玩家：{}无联盟，不能掠夺别人", jz.id);
			sendError(session, id, "未加入联盟，不能掠夺");
			return;
		}
		long jzid = jz.id;
		int jzGuo = jz.guoJiaId;
		GuoJiaBean guo = HibernateUtil.find(GuoJiaBean.class, jzGuo);
		if (guo == null){
			log.error("国家id是：{}没有数据库数据", jzGuo);
			guo = new GuoJiaBean();
			guo.guoJiaId = jzGuo;
		}
		LveDuoBean lve = HibernateUtil.find(LveDuoBean.class, jzid);
		if(lve == null){
			lve = new LveDuoBean(jzid);
		}else{
			resetLveDuoBean(lve);
		}
		LveDuoInfoResp.Builder resp = LveDuoInfoResp.newBuilder();
		resp.setUsed(lve.usedTimes);
		resp.setAll(lve.todayTimes);
		resp.setHasRecord(lve.hasRecord);
		resp.setCdTime(getLeftCD(lve));
		
		/*
		 * vip是否能购买消除CD的VIP等级
		 */
		VipFuncOpen vipData = VipMgr.vipFuncOpenTemp.get(VipData.clear_lveDuo_CD);
		resp.setCanClearCdVIP(vipData == null? Integer.MAX_VALUE: vipData.needlv);
		if(vipData != null && jz.vipLevel >= vipData.needlv){
			resp.setClearCdYB(getYuanBao(lve.buyClearCdCount + 1, 
					PurchaseConstants.LVE_DUO_CD));
		}
	
		/*
		 * 购买战斗次数
		 */
		int[] data = getBuyADDChanceData(jz.vipLevel, 
				lve.buyBattleHuiShu, VipData.buyLveDuoTimes, PurchaseConstants.LVE_DUO_BATTLE);
		if(data != null){
			// 全部
			int all = getAllBattleCountToday(jz.vipLevel, data[3]) ;
			// 当前vip等级，玩家可以掠夺的最大次数
			resp.setNowMaxBattleCount(all);
			if(data[0] == 1){
				resp.setBuyNextBattleCount(data[1]);
				resp.setBuyNextBattleYB(data[2]);
				resp.setRemainBuyHuiShi(data[3] - lve.buyBattleHuiShu);
			}
		}else{
			resp.setNowMaxBattleCount(0);
		}
		/*
		 * 贡金（后改为积分）
		 */
		Double gj = RankingGongJinMgr.DB.zScoreGongJin(RankingGongJinMgr.gongJinPersonalRank, 
				jzid+"");
		int myGongj = 0;
		if(gj == null){
			// 开启掠夺初始化贡金
			myGongj = RankingGongJinMgr.inst.iniGongJin(jzid, player.lianMengId);
		}else{
			myGongj = (int)gj.doubleValue();
		}
		resp.setGongJin(myGongj);
		resp.setMyFangShouId(lve.gongJiZuHeId);
		int maxHpGuojiaId = fillGuoInfo(resp, guo);
		resp.setShowGuoId(maxHpGuojiaId);
		
		int showMengId = 0;
		List<LianMengInfo.Builder> menglist = 
				RankingMgr.inst.getLianMengRank(1, maxHpGuojiaId, null, 201);
		if(menglist == null){
			menglist = new ArrayList<LianMengInfo.Builder>();
			log.error("君主id:{}, 国家id：{}， 没有排行记录", jz.id, maxHpGuojiaId);
		}else{
			for(LianMengInfo.Builder b: menglist){
				if(b.getRank() == 1){
					showMengId = b.getMengId();
				}
				resp.addMengInfos(b);
			}
			List<JunZhuInfo.Builder> jl = fillJunZhuInfo(guo.diDuiGuo_1, guo.diDuiGuo_2, showMengId);
			for(JunZhuInfo.Builder b: jl){
				resp.addJunInfos(b);
			}
		}
		// 领奖倒计时
		if(lve.getGongJinTime != null && DateUtils.isSameDay(lve.getGongJinTime)){
			resp.setTimeToNight(-1); // 今日奖励已经领取过了
		}else{
			int timeDistance = DateUtils.timeDistanceTodayOclock(22, 0);
			resp.setTimeToNight(timeDistance / 1000);
		}
		long rank = RankingGongJinMgr.DB.zrevrank(RankingGongJinMgr.gongJinPersonalRank, jzid+"");
		if(rank == -1){
			log.error("玩家:{}没有贡金排名", jz.id);
		}
		resp.setGongJinRank((int)rank);
		AlliancePlayer p = HibernateUtil.find(AlliancePlayer.class, jzid);
		if(p != null && p.lianMengId > 0){
			long mengRank = RankingGongJinMgr.DB.zrevrank(RankingGongJinMgr.gongJinAllianceRank,
					p.lianMengId+"");
			if(mengRank == -1){
				log.error("联盟:{}没有贡金排名", jz.id);
			}
			resp.setGongJinMengRank((int)mengRank);
		}
		session.write(resp.build());
	}
	public List<JunZhuInfo.Builder> fillJunZhuInfo(int didui1, int didui2,  int showMengId){
		List<AlliancePlayer> aplayers = AllianceMgr.inst.getAllianceMembers(showMengId);
		List<JunZhuInfo.Builder> list = new ArrayList<JunZhuInfo.Builder>();
		for(AlliancePlayer ap: aplayers){
			if(ap == null || ap.lianMengId <= 0){
				log.error("联盟不存在，不能被掠夺");
				continue;
			}
			JunZhuInfo.Builder b = JunZhuInfo.newBuilder();
			JunZhu zhu = HibernateUtil.find(JunZhu.class, ap.junzhuId);
			if(zhu == null){
				continue;
			}
			// 对手是否开启掠夺
			boolean isEnemyCanLve = FunctionOpenMgr.inst.isFunctionOpen(FunctionID.lveDuo, zhu.id, zhu.level);
			if(!isEnemyCanLve){
				// 对手没有开启掠夺
				continue;
			}
			b.setJunZhuId(zhu.id);
			b.setName(zhu.name);
			b.setLevel(zhu.level);
			b.setRoleId(zhu.roleId);
			b.setZhanli(getZhanli(zhu));
			b.setRank(-1);
			
			/*
			 * a)掠夺获胜时被挑战方为非敌对国时
			 * i.挑战方获得被挑战方当前拥有的N%+L的贡金，防守方损失N%的贡金（个数向上取整）
			 * b)掠夺获胜时被挑战方为敌对国时
			 * i.挑战方获得被挑战方当前拥有的M%+L的贡金，防守方损失M%的贡金（个数向上取整）
			 */
			int lostGongJin = RankingGongJinMgr.inst.getJunZhuGongJin(zhu.id);
			// 敌对国
			if(zhu.guoJiaId == didui1 || zhu.guoJiaId == didui2){
				lostGongJin = (int)Math.floor(lostGongJin * CanShu.LUEDUO_CANSHU_M) 
						+ (int)CanShu.LUEDUO_CANSHU_L;
			}else{ // 非敌对国
				lostGongJin = (int)Math.floor(lostGongJin * CanShu.LUEDUO_CANSHU_N)
						+ (int)CanShu.LUEDUO_CANSHU_L;
			}

			b.setGongjin(lostGongJin);

			// 显示剩余血量，回血只是一个显示，在进入战斗界面进行回血并且记录
			b.setRemainHp(getBackHp(zhu.id, zhu.shengMingMax));
//			Map<Integer, int[]> m = ldnpcMapHP.get(zhu.id);
//			if(m != null){
//				int[] tem = m.get(101);
//				b.setRemainHp(tem == null? zhu.shengMingMax: tem[1]);
//			}else{
//				b.setRemainHp(zhu.shengMingMax);
//			}
			b.setShengMingMax(zhu.shengMingMax);
			LveDuoBean zhulve = HibernateUtil.find(LveDuoBean.class, zhu.id);
			if(zhulve == null || zhulve.lastBattleEndTime == null){
				b.setLeftProtectTime(0);
			}else{
				int compare = (int)(zhulve.lastBattleEndTime.getTime()/1000);
				int time = (int)(LveStaticData.lveDuo_protect_time  + compare - 
						new Date().getTime()/1000);
				b.setLeftProtectTime(time<=0?0: time);
			}
			list.add(b);
		}
		return list;
	}
	
	public int getBackHp(long junId, int allShengMing){
		long now = System.currentTimeMillis();
		boolean isroll = isRollBackAll(junId);
		if(isroll){
			return allShengMing;
		}
		Map<Integer, int[]> m = ldnpcMapHP.get(junId);
		if(m == null){
			return allShengMing;
		}
		int[] bing = m.get(101);
		if(bing == null){
			return allShengMing;
		}
		
		Long backHpTime = rollbackHpTime.get(junId);
		long time = (now - backHpTime) /1000;
		int recoveTime = CanShu.LUEDUO_RECOVER_INTERVAL_TIME;
		int hp = bing[1];
		if(time > recoveTime){
			hp += (int)Math.floor(time / recoveTime *
					CanShu.LUEDUO_RECOVER_PERCENT * 0.01 * allShengMing);
			if(hp > allShengMing){
				hp = allShengMing;
			}
		}
		log.info("被掠夺的君主：{}的剩余血量目前是：{}", junId, hp);
		return hp;
	}
	
	public int fillGuoInfo(LveDuoInfoResp.Builder resp, GuoJiaBean guo){
		int myGuojia = guo.guoJiaId;
		int di1 = guo.diDuiGuo_1;
		int di2 = guo.diDuiGuo_2;
		// 添加敌对国1
		GuoInfo.Builder guoinfo = GuoInfo.newBuilder();
		guoinfo.setGuojiaId(di1);
		guoinfo.setHate(GuoJiaMgr.getHate(guo, di1));
		resp.addGuoLianInfos(guoinfo);
		
		// 添加敌对国2
		guoinfo = GuoInfo.newBuilder();
		guoinfo.setGuojiaId(di2);
		guoinfo.setHate(GuoJiaMgr.getHate(guo, di2));
		resp.addGuoLianInfos(guoinfo);

		// 国家排序
		Map<Integer, Integer> map = new TreeMap<Integer, Integer>();
		map.put((int)GuoJiaMgr.guoJia_1, GuoJiaMgr.getHate(guo, GuoJiaMgr.guoJia_1));
		map.put((int)GuoJiaMgr.guoJia_2, GuoJiaMgr.getHate(guo, GuoJiaMgr.guoJia_2));
		map.put((int)GuoJiaMgr.guoJia_3, GuoJiaMgr.getHate(guo, GuoJiaMgr.guoJia_3));
		map.put((int)GuoJiaMgr.guoJia_4, GuoJiaMgr.getHate(guo, GuoJiaMgr.guoJia_4));
		map.put((int)GuoJiaMgr.guoJia_5, GuoJiaMgr.getHate(guo, GuoJiaMgr.guoJia_5));
		map.put((int)GuoJiaMgr.guoJia_6, GuoJiaMgr.getHate(guo, GuoJiaMgr.guoJia_6));
		map.put((int)GuoJiaMgr.guoJia_7, GuoJiaMgr.getHate(guo, GuoJiaMgr.guoJia_7));

		// 去掉自己国家和敌对国
		map.remove(myGuojia);
		map.remove(di1);
		map.remove(di2);

		// map按照value进行排序
		List<Map.Entry<Integer,Integer>> arrayList = 
				new ArrayList<Map.Entry<Integer,Integer>>(map.entrySet());
		// 学习使用lambda
		Collections.sort(arrayList,(o1, o2) ->(o2.getValue().compareTo(o1.getValue())));
//		Collections.sort(arrayList, new Comparator<Map.Entry<Integer,Integer>>(){
//			public int compare(Map.Entry<Integer,Integer> o1, Map.Entry<Integer,Integer> o2) {
//				return o2.getValue().compareTo(o1.getValue());
//				}
//			});
		arrayList.forEach((intMap) -> {
			Integer key =  intMap.getKey();
			Integer value = intMap.getValue();
			GuoInfo.Builder  guoinfo2 = GuoInfo.newBuilder();
			guoinfo2.setGuojiaId(key);
			guoinfo2.setHate(value);
			resp.addGuoLianInfos(guoinfo2);
//			log.info("key is:{}, value is:{}", key, value);
		});
//		for(Map.Entry<Integer,Integer> intMap: arrayList){
//			Integer key =  intMap.getKey();
//			Integer value = intMap.getValue();
//			guoinfo = GuoInfo.newBuilder();
//			guoinfo.setGuojiaId(key);
//			guoinfo.setHate(value);
//			resp.addGuoLianInfos(guoinfo);
//			log.info("key is:{}, value is:{}", key, value);
//		}
	
		// 自己的国家排在最后
		guoinfo = GuoInfo.newBuilder();
		guoinfo.setGuojiaId(myGuojia);
		guoinfo.setHate(0);
		resp.addGuoLianInfos(guoinfo);
		return di1;
	}
	
	public void resetLveDuoBean(LveDuoBean bean){
		if(bean == null){
			return;
		}
		//change 20150901
		if(DateUtils.isTimeToReset(bean.lastRestTime, CanShu.REFRESHTIME_PURCHASE)){
			bean.usedTimes = 0;
			bean.todayTimes = LveStaticData.free_all_battle_times;
			bean.todayWin = 0;
			bean.lastRestTime = new Date();
			bean.buyBattleHuiShu = 0;
			bean.buyClearCdCount = 0;
			HibernateUtil.save(bean);
			log.info("君主：{}过第二天零点重置了掠夺bean：", bean.junzhuId);
		}
	}
	
	public int[] resetFangShouGuongYongBing( int level){
		int[] bs = PvpMgr.inst.refreshSoldiers(level, level);
		return bs;
	}
	public int getLeftCD(LveDuoBean bean) {
		if (bean.lastBattleTime == null) {
			return 0;
		}
		// 今日免费挑战次数已经用完
		if (bean.usedTimes == bean.todayTimes) {
			// 当前时间距离第二天凌晨四点的时间
			int leftSeconds = DateUtils.getTimeToNextNeedHour(new Date(),
					LveStaticData.clear_point);
			return leftSeconds;
		}
		Date now = new Date();
		int leftTime = (int) (bean.lastBattleTime.getTime() / 1000 - now.getTime()
				/ 1000 + LveStaticData.allCD);
		log.info("玩家:{}掠夺剩余CD时长是：{}", bean.junzhuId, leftTime);
		return leftTime <= 0 ? 0 : leftTime;
	}

	public int getYuanBao(int buyCount, int type){
		int yuan =  PurchaseMgr.inst.getNeedYuanBao(type, buyCount);
		return yuan;
	}
	public int[] getBuyADDChanceData(int vipLev, int buyHuiShu, 
			int vipSelfType, int purchaseType){
		return PvpMgr.inst.getBuyADDChanceData(vipLev, buyHuiShu, vipSelfType
				,purchaseType);
	}

	/**
	 * 当前vip等级，当日包括购买可拥有的最大战斗次数
	 * @Title: getAllBattleCount 
	 * @Description:
	 * @param vipLev
	 * @param buyHuiShu
	 * @return
	 */
	public int getAllBattleCountToday(int vipLev, int buyHuiShu) {
		int times = PurchaseMgr.inst.getAllUseNumbers(PurchaseConstants.LVE_DUO_BATTLE,
				buyHuiShu) + LveStaticData.free_all_battle_times;
		return times;
	}
	
	public void goLveDuo(int id, IoSession session, Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("掠夺请求出错：君主不存在");
			return;
		}
		LveGoLveDuoReq.Builder req = (LveGoLveDuoReq.Builder)builder;
		long enemyId = req.getEnemyId();
		
		LveGoLveDuoResp.Builder resp = LveGoLveDuoResp.newBuilder();
		// 掠夺活动是否开始
		boolean yes = DateUtils.isInDeadline(CanShu.OPENTIME_LUEDUO, CanShu.CLOSETIME_LUEDUO);
		if(!yes){
			log.error("君主：{}掠夺失败，掠夺活动没有开启", jz.id);
			resp.setIsCanLveDuo(0);
			session.write(resp);
			return;
		}
		
		// 无联盟不可掠夺和被掠夺
		AlliancePlayer playerA = HibernateUtil.find(AlliancePlayer.class, jz.id);
		if(playerA == null || playerA.lianMengId <= 0){
			log.error("玩家：{}无联盟，不能掠夺别人", jz.id);
			resp.setIsCanLveDuo(9);
			session.write(resp);
			return;
		}
		
		
		// -1 不能掠夺自己
		if(enemyId == jz.id){
			log.error("君主：{}掠夺失败，玩家不能掠夺自己", jz.id);
			resp.setIsCanLveDuo(-1);
			session.write(resp);
			return;
		}
		
		
		LveDuoBean myBean = HibernateUtil.find(LveDuoBean.class, jz.id);
		if (myBean == null) {
			myBean = new LveDuoBean(jz.id);
		}else{
			resetLveDuoBean(myBean);
		}
		
		
		// 次数不够
		if(myBean.todayTimes == myBean.usedTimes){
			log.error("玩家：{}掠夺次数：{}已用完，请求掠夺失败", jz.id, myBean.usedTimes);
			resp.setIsCanLveDuo(3);
			session.write(resp);
			return;
		}
		
		
		// CD未到
		if(getLeftCD(myBean) > 0){
			log.error("玩家：{}CD时间没到，请求掠夺失败", jz.id);
			resp.setIsCanLveDuo(2);
			session.write(resp);
			return;
		}
	
		// 对手情况
		JunZhu oppo = HibernateUtil.find(JunZhu.class, enemyId); 
		if(oppo == null){
			resp.setIsCanLveDuo(6);
			session.write(resp);
			return;
		}
		
		//对手未开启联盟，不能掠夺
		boolean isEnemyLmOpen = FunctionOpenMgr.inst.isFunctionOpen(FunctionID.LianMeng, enemyId, oppo.level);
		if(!isEnemyLmOpen){
			log.error("玩家：{}未开启联盟功能，不能被人掠夺", enemyId);
			resp.setIsCanLveDuo(11);
			session.write(resp);
			return;
		}
		
		// 对手未加入联盟，不能掠夺
		AlliancePlayer playerB = HibernateUtil.find(AlliancePlayer.class, enemyId);
		if(playerB == null || playerB.lianMengId <= 0){
			log.error("玩家：{}无联盟，不能被人掠夺", enemyId);
			resp.setIsCanLveDuo(10);
			session.write(resp);
			return;
		}
		
		// 同联盟不能掠夺
		if (playerA.lianMengId == playerB.lianMengId) {
			log.error("玩家：{}和被掠夺者：{}，同联盟，无法掠夺", jz.id, enemyId);
			resp.setIsCanLveDuo(1);
			session.write(resp);
			return;
		}
		
		
		//对手加入联盟，但是等级不够开启掠夺，不能掠夺
		boolean isEnemyCanLve = FunctionOpenMgr.inst.isFunctionOpen(FunctionID.lveDuo, enemyId, oppo.level);
		if(!isEnemyCanLve){
			resp.setIsCanLveDuo(8);
			session.write(resp);
			return;
		}
		
		LveDuoBean enemyBean = HibernateUtil.find(LveDuoBean.class, enemyId);
		if(enemyBean == null){
			enemyBean = new LveDuoBean(enemyId);
		}
		// 对手正在被略
		BattleInfo4Pvp battleInfo4Pvp = fightingLock.get(enemyId);
		if(battleInfo4Pvp != null){
			if(System.currentTimeMillis() > ((CanShu.MAXTIME_LUEDUO+PvpMgr.inst.PVP_BATTLE_DELAY_TIME) *1000 + battleInfo4Pvp.startTime) ){
				fightingLock.remove(enemyId);
				log.info("掠夺防守玩家：{}战斗非一场结束，时间过长，从fightingLock中remove掉", enemyId);
			}else{
				log.error("玩家：{}请求掠夺失败，对手：{}正在被掠夺", jz.id, enemyId);
				resp.setIsCanLveDuo(5);
				session.write(resp);
				return;
			}
		}
		
		// 对手正在准备阶段（玩家不存在）
		Long[] info = prepareLock.get(enemyId);
		if(info != null){
			if (new Date().getTime() <= Long.valueOf(info[1]) / 1000 + 30) {
				log.error("玩家：{}请求掠夺失败，对手：{}正在和别人战斗的准备阶段", jz.id, enemyId);
				resp.setIsCanLveDuo(12);
				session.write(resp);
				return;
			}else{
				// 超时
				prepareLock.remove(enemyId);
			}
		}
		
		
		// 对手保护期间
		yes = isPassProtectPoint(enemyBean.lastBattleEndTime);
		if(!yes){
			log.error("玩家：{}请求掠夺失败，对手:{}处于保护期", jz.id, enemyId);
			resp.setIsCanLveDuo(4);
			session.write(resp);
			return;
		}
		
		int myGongJin = RankingGongJinMgr.inst.getJunZhuGongJin(jz.id);
		int lostGongJin = RankingGongJinMgr.inst.getJunZhuGongJin(enemyId);
		GuoJiaBean guojia = HibernateUtil.find(GuoJiaBean.class, jz.guoJiaId);
		if(guojia == null){
			guojia = new GuoJiaBean();
			log.error("GuojiaBean 报错：{}, 不应该，请服务器程序处理", jz.guoJiaId);
		}
		int enemyGuoId = oppo.guoJiaId;
		// 敌对国
		if(enemyGuoId == guojia.diDuiGuo_1 || enemyGuoId == guojia.diDuiGuo_2){
			lostGongJin = (int)Math.floor(lostGongJin * CanShu.LUEDUO_CANSHU_M) 
					+ (int)CanShu.LUEDUO_CANSHU_L;
		}else{ // 非敌对国
			lostGongJin = (int)Math.floor(lostGongJin * CanShu.LUEDUO_CANSHU_N)
					+ (int)CanShu.LUEDUO_CANSHU_L;
		}

		/*
		 * 是否零点回血更新
		 */
		long now = System.currentTimeMillis();
		long time = 0;
		boolean isroll = isRollBackAll(enemyId);
		if(isroll){
			ldnpcMapHP.remove(enemyId);
			rollbackHpTime.remove(enemyId);
			log.info("防守方：{}血量记录从缓存map中remove掉", enemyId);
		}else{
			Long backHpTime = rollbackHpTime.get(enemyId);
			time = (now - backHpTime) /1000;
		}

		Map<Integer, int[]> m = ldnpcMapHP.get(enemyId);
		if(m == null){
			m = intGYBMap(oppo.level, enemyId, oppo.shengMingMax);
			log.info("掠夺防守君主：{}完全更新血条记录", enemyId);
		}
		Bing.Builder gu = null;
		int recoveTime = CanShu.LUEDUO_RECOVER_INTERVAL_TIME;
		for(Entry<Integer, int[]> e: m.entrySet()){
			gu = Bing.newBuilder();
			int zhanwei = e.getKey();
			int[] bing = e.getValue(); 
			if(zhanwei == 101){
				// 101 站位君主回血, 最多至满血
				if(time > recoveTime){
					int hp = bing[1];
					hp += (int)Math.floor(time / recoveTime *
							CanShu.LUEDUO_RECOVER_PERCENT * 0.01 * oppo.shengMingMax);
					if(hp > oppo.shengMingMax){
						hp = oppo.shengMingMax;
					}
					log.info("防守方：{}回血回到：{}", enemyId, hp);
					e.setValue(new int[]{-1, hp});
				}
				continue;
			}
			if(bing == null || bing[0] <= 0){
				continue;
			}
			int bingId = bing[0];
			int hp = bing[1];
			if(time > recoveTime){
				GuYongBing gyb = bingMap.get(bingId);
				if(gyb == null){
					continue;
				}
				// 回血(最多到满血)
				hp += (int)Math.floor(time / recoveTime *
						CanShu.LUEDUO_RECOVER_PERCENT * 0.01 * gyb.shengming);
				if(hp > gyb.shengming){
					hp = gyb.shengming;
				}
				log.info("雇佣兵：{}回血回到：{}", bingId, hp);
				// 重新set
				e.setValue(new int[]{bingId, hp});
			}
			gu.setId(bingId);
			gu.setHp(hp);
			resp.addOppoSoldiers(gu);
		}
		if(time / recoveTime >= 1){
			// 记录回血时间
			rollbackHpTime.put(enemyId, now - time%recoveTime);
			log.info("防守方：{}及其雇佣兵回血结束", enemyId);
		}
		/*
		 * 攻击方的雇佣兵 是随机来的
		 */
		int[] myBingIds =  PvpMgr.inst.refreshSoldiers(jz.level, jz.level);
		for (int i = 0; i < myBingIds.length; i++) {
			gu = Bing.newBuilder();
			gu.setId(myBingIds[i]);
			gu.setHp(jz.shengMingMax);
			resp.addMySoldiers(gu);
			gongJiBingMap.put(jz.id, myBingIds);
		}
		// 可以挑战
		resp.setIsCanLveDuo(7);
		resp.setMyZhanli(getZhanli(jz));
		resp.setMyGongjiZuheId(myBean.gongJiZuHeId);
		resp.setOppoZhanli(getZhanli(oppo));
		resp.setOppoId(enemyId);
		resp.setOppoRoleId(oppo.roleId);
		resp.setOppFangShouZuheId(enemyBean.gongJiZuHeId);
//		resp.setOppoActivateMiBaoCount(MibaoMgr.inst.getActiveSkillFromDB(jId, zuheId));
		resp.setOppoLevel(oppo.level);
		resp.setGongJin(myGongJin);
		resp.setLostGongJin(lostGongJin);
		resp.setUsed(myBean.usedTimes);
		resp.setAll(myBean.todayTimes);
		session.write(resp.build());
		// 敌人准备阶段的锁定
		prepareLock.put(enemyId, new Long[]{jz.id, new Date().getTime()});
		// 记录临时贡金
		if(lostGongJin > 0){
			willLostGongJin.put(jz.id, lostGongJin);
		}
	}
	public void sendError(IoSession session, int cmd, String msg) {
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(cmd);
		test.setErrorDesc(msg);
		session.write(test.build());
	}
	
	public boolean isRollBackAll(long enemyId){
		Long time = rollbackHpTime.get(enemyId);
		if(time == null){
			return true;
		}
		// change 20150901
		if(DateUtils.isTimeToReset(new Date(time), CanShu.REFRESHTIME_PURCHASE)){
			return true;
		}
		return false;
	}
	public int getZhanli(JunZhu jz) {
		if (jz != null) {
			return JunZhuMgr.inst.getJunZhuZhanliFinally(jz);
		}
		return 0;
	}
	public boolean isPassProtectPoint(Date d){
		if(d == null){
			return true;
		}
		int now = (int)(new Date().getTime()/1000);
		int compare = (int)(d.getTime()/1000);
		if(now < compare + LveStaticData.lveDuo_protect_time && now > compare){
			return false;
		}
		return true;

	}
	
	public void lveDuoInitData(int d, IoSession session, Builder builder){
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.error("君主不存在");
			return;
		}
		
		PvpZhanDouInitReq.Builder req = (PvpZhanDouInitReq.Builder) builder;
		long enemyId = req.getUserId();
		AlliancePlayer p = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if(p == null || p.lianMengId <= 0){
			ZhanDouInitError.Builder errresp = ZhanDouInitError.newBuilder();
			errresp.setResult("您已退出联盟，无法掠夺。");
			session.write(errresp.build());
			log.error("君主：{}掠夺敌人：{}失败，君主退出联盟", junZhu.id, enemyId);
			return;
		}
		AlliancePlayer p2 = HibernateUtil.find(AlliancePlayer.class, enemyId);
		if(p2 == null || p2.lianMengId <= 0){
			ZhanDouInitError.Builder errresp = ZhanDouInitError.newBuilder();
			errresp.setResult("对手已退出联盟，无法掠夺。");
			session.write(errresp.build());
			log.error("君主：{}掠夺敌人：{}失败，敌人退出联盟", junZhu.id, enemyId);
			return;
		}
		Long[] yes = prepareLock.get(enemyId);
		if(yes == null){
			ZhanDouInitError.Builder errresp = ZhanDouInitError.newBuilder();
			errresp.setResult("所选玩家正在被其他玩家掠夺，请选择其他玩家。");
			session.write(errresp.build());
			log.error("君主：{}掠夺敌人：{}失败，两者都没有在备战map中", junZhu.id, enemyId);
			return;
		}else{
			if(yes[0] != junZhu.id){
				ZhanDouInitError.Builder errresp = ZhanDouInitError.newBuilder();
				errresp.setResult("所选玩家正在被其他玩家掠夺，请选择其他玩家。");
				session.write(errresp.build());
				log.error("君主：{}掠夺敌人：{}失败，另有攻击方已经在掠夺敌人", junZhu.id, enemyId);
				return;
			}
		}
		/*
		 * 30秒准备界面锁定
		 */
		if (new Date().getTime() > (Long.valueOf(yes[1]) + 30 * 1000)) {
			ZhanDouInitError.Builder errresp = ZhanDouInitError.newBuilder();
			errresp.setResult("所选玩家正在被其他玩家掠夺，请选择其他玩家。");
			session.write(errresp.build());
			prepareLock.remove(enemyId);
			log.error("君主：{}掠夺敌人：{}失败, 两人在备战界面时间过长", junZhu.id, enemyId);
			return;
		}
		prepareLock.remove(enemyId);
		
		Integer zhandouId =  (int)TableIDCreator.getTableID(LveZhanDouRecord.class, 1L);// 战斗id 后台使用
		
		ZhanDouInitResp.Builder resp = ZhanDouInitResp.newBuilder();
		Group.Builder enemyTroop = Group.newBuilder();
		List<Node> enemys = new ArrayList<ZhanDou.Node>();
		// 对手
		JunZhu enemy = HibernateUtil.find(JunZhu.class, enemyId);
		LveDuoBean ebean = HibernateUtil.find(LveDuoBean.class, enemyId);
		resetLveDuoBean(ebean);
		int fangshouId = ebean == null ? -1 : ebean.gongJiZuHeId;
	
		enemyTroop.setMaxLevel(BigSwitch.pveGuanQiaMgr.getGuanQiaMaxId(enemyId));
		
		// 对手雇佣兵
		ArrayList<GuYongBing> bingList = new ArrayList<GuYongBing>();
		Map<Integer, int[]> m = ldnpcMapHP.get(enemyId);
		if(m == null){
			m =  intGYBMap(enemy.level, enemyId, enemy.shengMingMax);
		}
		int enemyRemainHp = enemy.shengMingMax;
		Node.Builder wjNode = null;
		for(Entry<Integer, int[]> e: m.entrySet()){
			int zhanwei = e.getKey();
			int[] gybs = e.getValue();
			if(zhanwei == 101){
				enemyRemainHp = gybs[1];
				continue;
			}
			if(gybs == null){
				continue;
			}
			//id或者血量小于等于0，不加载
			if(gybs[0] <= 0  || gybs[1] <= 0){
				continue;
			}
			GuYongBing bing = bingMap.get(gybs[0]);
			if(bing == null){
				continue;
			}
//			bing.shengming = gybs[1];
//			bing.zhanweiLve = zhanwei;
//			bingList.add(bing);
		
			wjNode = Node.newBuilder();
			GongjiType gongjiType = PveMgr.inst.id2GongjiType.get(bing.gongjiType);
			PveMgr.inst.fillDataByGongjiType(wjNode, gongjiType);
			wjNode.addFlagIds(zhanwei);
			wjNode.setModleId(bing.modelId);
			wjNode.setNodeType(NodeType.valueOf(bing.type));
			wjNode.setNodeProfession(NodeProfession.valueOf(bing.profession));
			wjNode.setHp(gybs[1]);
			wjNode.setNodeName(PvpMgr.inst.getNPCName(bing.name));
			wjNode.setHpNum(bing.lifebarNum);
			wjNode.setAppearanceId(bing.modelApID);
			wjNode.setNuQiZhi(0);
			wjNode.setMibaoCount(0);
			wjNode.setMibaoPower(0);
			wjNode.setArmor(bing.armor);
			wjNode.setArmorMax(bing.armorMax);
			wjNode.setArmorRatio(bing.armorRatio);
			PveMgr.inst.fillGongFangInfo(wjNode, bing);
			String skills = bing.skills;
			if (skills != null && !skills.equals("")) {
				String[] skillList = skills.split(",");
				for (String s : skillList) {
					int skillId = Integer.parseInt(s);
					PveMgr.inst.addNodeSkill(wjNode, skillId);
				}
			}
			enemys.add(wjNode.build());
		}
//		PvpMgr.inst.fillGuYongBingDataInfo(enemys, -1, bingList);
			
		/*
		 * 敌人
		 */
//		PveMgr.inst.fillJunZhuDataInfo(resp, session, enemys, enemy,
//				101, fangshouId, enemyTroop);
		
		JunZhuMgr.inst.calcJunZhuTotalAtt(enemy);
		Node.Builder enemyNode = Node.newBuilder();
		// 添加装备
		List<Integer> zbIdList = EquipMgr.inst.getEquipCfgIdList(enemy);
		PveMgr.inst.fillZhuangbei4Player(enemyNode, zbIdList, enemy);
		// 添加flag,添加君主基本信息（暴击、类型、读表类型、视野）
		enemyNode.addFlagIds(101);
		enemyNode.setNodeType(NodeType.PLAYER);
		enemyNode.setNodeProfession(NodeProfession.NULL);
		enemyNode.setModleId(enemy.roleId);
		enemyNode.setNodeName(enemy.name);
		PveMgr.inst.fillDataByGongjiType(enemyNode, null);
		PveMgr.inst.fillGongFangInfo(enemyNode, enemy);
		// 添加秘宝信息
		PveMgr.inst.fillJZMiBaoDataInfo(enemyNode, fangshouId, enemy.id);
		// 敌人的剩余血量
		enemyNode.setHp(enemyRemainHp);
		enemyNode.setHpNum(1);
		enemyNode.setAppearanceId(1);
		enemyNode.setNuQiZhi(MibaoMgr.inst.getChuShiNuQi(enemy.id));
		enemyNode.setMibaoCount(MibaoMgr.inst.getActivateMiBaoCount(enemy.id));
		enemyNode.setMibaoPower(JunZhuMgr.inst.getAllMibaoProvideZhanli(enemy));
		enemyNode.setArmor(0);
		enemyNode.setArmorMax(0);
		enemyNode.setArmorRatio(0);
		enemys.add(enemyNode.build());
		
		
		enemyTroop.addAllNodes(enemys);
		resp.setEnemyTroop(enemyTroop);

		/*
		 *  君主自己
		 */
		
		long jId = junZhu.id;
		int jlevel = junZhu.level;
		int mapId = 0;
		Group.Builder selfTroop = Group.newBuilder();
		List<Node> selfs = new ArrayList<ZhanDou.Node>();
		LveDuoBean bean = HibernateUtil.find(LveDuoBean.class, jId);
		resetLveDuoBean(bean);
		int gongjiId = bean == null ? -1 : bean.gongJiZuHeId;
		int selfFlagIndex = 1;
		PveMgr.inst.fillJunZhuDataInfo(resp, session, selfs, junZhu,
				selfFlagIndex, gongjiId, selfTroop);
		selfFlagIndex += 1;
		bingList.clear();
		int[] bings = gongJiBingMap.get(jId);
		if(bings == null){
			bings = PvpMgr.inst.refreshSoldiers(jlevel, jlevel);
		}
		for (int i = 0; i<bings.length; i++) {
			GuYongBing bing = bingMap.get(bings[i]);
			if(bing == null){
				continue;
			}
			int renshu = bing.renshu;
			for (int k = 0; k < renshu; k++) {
				bingList.add(bing);
			}
		}
		PvpMgr.inst.fillGuYongBingDataInfo(selfs, selfFlagIndex, bingList);
		selfTroop.addAllNodes(selfs);
		selfTroop.setMaxLevel(BigSwitch.pveGuanQiaMgr.getGuanQiaMaxId(jId));
		resp.setSelfTroop(selfTroop);
		resp.setZhandouId(zhandouId);
		resp.setMapId(mapId);
		resp.setLimitTime(CanShu.MAXTIME_LUEDUO);
		session.write(resp.build());
		log.info("君主：{}掠夺敌人：{}， 符合条件，进入战斗界面完成", junZhu.id, enemyId);
		// 进行战前一些数据的处理
		if(bean == null){
			bean = new LveDuoBean(jId);
		}
		if(ebean == null){
			ebean = new LveDuoBean(enemyId);
		}
		dealBattleRecord(bean, junZhu, zhandouId, enemyId, ebean);
	}

	public Map<Integer, int[]> intGYBMap(int level, long enemyId, int enemyShengming){
		Map<Integer, int[]> m = new HashMap<Integer, int[]>();
		int c = 102;
		int[] bl = resetFangShouGuongYongBing(level);
		for(int i= 0; i < bl.length; i++){
			GuYongBing bing = bingMap.get(bl[i]);
			if(bing == null){
				continue;
			}
			int renshu = bing.renshu;
			int shengming = bing.shengming * bing.lifebarNum;
			for (int k = 0; k < renshu; k++) {
				m.put(c, new int[]{bl[i], shengming});
				c++;
			}
		}
		m.put(101, new int[]{-1, enemyShengming});
		ldnpcMapHP.put(enemyId, m);
		return m;
	}
	
	public void dealBattleRecord(LveDuoBean bean, JunZhu jz, Integer zhandouId,
			long enemyId, LveDuoBean eb){
		/*
		 * 扣除玩家的战斗次数等
		 */
		bean.usedTimes += 1;
		bean.hisAllBattle += 1;
		bean.lastBattleTime = new Date();
		// 主动攻击别人，不做记录
//		bean.hasRecord = true;
		HibernateUtil.save(bean);
		log.info("君主：{}主动参加一次掠夺， 掠夺相关数据变为：已用掠夺次数：{}， 历史战斗次数：{}， 战斗时间：{}",
				bean.usedTimes, bean.hisAllBattle, bean.lastBattleTime);
		 
		BattleInfo4Pvp battleInfo4Pvp = new BattleInfo4Pvp(jz.id, System.currentTimeMillis());
		fightingLock.put(enemyId, battleInfo4Pvp);
		log.info("防守方：{}，战斗锁定", enemyId);

		/*（A B 不同联盟）
		 * A挑战B，只有当B输，B扣贡金子，A增加。
		 */
		/*
		 * 当A挑战B， A输掉了，A， B什么也不得到什么也不损失
		 */
		long jId = jz.id;
		LveZhanDouRecord zhanR = new LveZhanDouRecord(zhandouId, jId, enemyId,
				new Date(), PVPConstant.GONG_JI_LOSE);
		HibernateUtil.save(zhanR);
		log.info("玩家：{}, 掠夺对手：{}，记录进入战斗(默认记录是输场)", jId, enemyId);
		if (eb != null) {
			eb.hasRecord = true;
			eb.hisAllBattle += 1;
			/*
			 * 默认对方赢
			 */
			eb.hisWin += 1;
			eb.todayWin += 1;
			// 默认防守方赢  不进入保护期
			eb.lastBattleEndTime = null;
			HibernateUtil.save(eb);
		}
		// 主线任务: 输赢不计，掠夺1次 20190916
		EventMgr.addEvent(ED.lve_duo , new Object[] { jz.id});
	}
	public void doConfirm(int id, IoSession session, Builder builder){
		LveConfirmReq.Builder req = (LveConfirmReq.Builder)builder;
		int doType = req.getDoType();
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		LveConfirmResp.Builder resp =LveConfirmResp.newBuilder();
		if (junZhu == null) {
			log.error("君主不存在");
			return;
		}
		LveDuoBean lve = HibernateUtil.find(LveDuoBean.class, junZhu.id);
		if(lve == null){
			lve = new LveDuoBean(junZhu.id);
		}else{
			// reset 数据
			resetLveDuoBean(lve);
		}
		switch(doType){
		case 1:
			clearCD(junZhu, session, resp, lve);
			break;
		case 2: 
			buyLveDuoChance(junZhu, session, resp, lve);
			break;
		case 3:
			sendGongJinDayAward(junZhu, session, resp, lve);
			break;
		}
	}
	public void clearCD(JunZhu jz, IoSession session,
			LveConfirmResp.Builder resp, LveDuoBean lve){
		VipFuncOpen vipData = VipMgr.vipFuncOpenTemp.get(VipData.clear_lveDuo_CD);
		if (vipData == null || jz.vipLevel < vipData.needlv) {
			resp.setIsOk(2);
			session.write(resp.build());
			log.error("君主：{}clear掠夺CD失败， vip等级不足", jz.id);
			return;
		}
		// 查看玩家CD时间是否已经是小于等于0了
		int time = getLeftCD(lve);
		if(time <= 0){
			sendError(session, PD.LVE_CONFIRM_REQ, "CD时间已经结束，可以直接掠夺");
			log.error("君主：{}clear掠夺CD失败， 没有CD时间，不用购买清除", jz.id);
			return;
		}
		int cdYuan = getYuanBao(lve.buyClearCdCount + 1, PurchaseConstants.LVE_DUO_CD);
		boolean yes = PvpMgr.inst.isBuySuccess(jz, cdYuan, session, 
				YBType.YB_BUY_LVE_DUO_CLEAR_CD, "掠夺购买清除CD");

		if (yes) {
			lve.buyClearCdCount += 1;
			lve.lastBattleTime = new Date(System.currentTimeMillis()
					- LveStaticData.lveDuo_CD * 1000);
			HibernateUtil.save(lve);
			log.info("玩家id{},姓名 {}, 购买清除掠夺战斗CD成功花费元宝{}个, 已经购买的clear次数是：{}", 
					jz.id, jz.name, cdYuan, lve.buyClearCdCount);
			resp.setIsOk(1);
			resp.setLeftCD(0);
			resp.setUsed(lve.usedTimes);
			resp.setAll(lve.todayTimes);
			resp.setGongJin(RankingGongJinMgr.inst.getJunZhuGongJin(jz.id));
			resp.setNextCDYuanBao(getYuanBao(lve.buyClearCdCount + 1,  PurchaseConstants.LVE_DUO_CD));
			resp.setCanClearCdVIP(vipData.needlv);
			int[] data = getBuyADDChanceData(jz.vipLevel, 
					lve.buyBattleHuiShu, VipData.buyLveDuoTimes, PurchaseConstants.LVE_DUO_BATTLE);
			if(data != null){
				// 全部
				int all = getAllBattleCountToday(jz.vipLevel, data[3]) ;
				// 当前vip等级，玩家可以掠夺的最大次数
				resp.setNowMaxBattleCount(all);
				if(data[0] == 1){
					resp.setBuyNextBattleCount(data[1]);
					resp.setBuyNextBattleYB(data[2]);
					resp.setRemainBuyHuiShi(data[3] - lve.buyBattleHuiShu);
				}
			}else{
				resp.setNowMaxBattleCount(0);
			}
		
		} else {
			resp.setIsOk(0);
			log.info("玩家id{},姓名 {},购买清除掠夺战斗CD失败：元宝不足, 需要的元宝数是：{}", jz.id, jz.name, cdYuan);
		}
		session.write(resp.build());
		
	}
	public void buyLveDuoChance(JunZhu jz, IoSession session,
			LveConfirmResp.Builder resp, LveDuoBean lve){
		int[] data = getBuyADDChanceData(jz.vipLevel, 
				lve.buyBattleHuiShu, VipData.buyLveDuoTimes, PurchaseConstants.LVE_DUO_BATTLE);
		if(data != null){
			int can = data[0];
			if (can == 0) {
				// 今日购买回数已经用完，无法再购买
				resp.setIsOk(6);
				log.error("玩家：{}购买掠夺'回数'失败：当日可购买'回数'已用完：{}", jz.id, data[3]);
			}else{
				int count = data[1];
				int yuanbao = data[2];
				boolean ok = PvpMgr.inst.isBuySuccess(jz, yuanbao, session, 
						YBType.YB_BUY_LVE_DUO_BATTLE, "掠夺购买挑战次数");
				if(!ok){
					// 元宝不足
					resp.setIsOk(0);
					log.error("玩家：{}购买掠夺'回数'失败：元宝不足，需要元宝{}", jz.id, yuanbao);
				}else{
					resp.setIsOk(1);
					lve.todayTimes += count;
					lve.buyBattleHuiShu += 1;
					HibernateUtil.save(lve);
					log.error("玩家：{}购买掠夺'回数'成功，今日总掠夺次数:{}, 已经购买掠夺回数：{}",
							jz.id, lve.todayTimes, lve.buyBattleHuiShu);
					
					resp.setGongJin(RankingGongJinMgr.inst.getJunZhuGongJin(jz.id));
					resp.setLeftCD(getLeftCD(lve));
					resp.setUsed(lve.usedTimes);
					resp.setAll(lve.todayTimes);
					/*
					 * vip是否能购买消除CD的VIP等级
					 */
					VipFuncOpen vipData = VipMgr.vipFuncOpenTemp.get(VipData.clear_lveDuo_CD);
					resp.setCanClearCdVIP(vipData == null? Integer.MAX_VALUE: vipData.needlv);
					if(vipData != null && jz.vipLevel >= vipData.needlv){
						resp.setNextCDYuanBao(getYuanBao(lve.buyClearCdCount + 1,  PurchaseConstants.LVE_DUO_CD));
					}else{
						resp.setNextCDYuanBao(Integer.MAX_VALUE);
					}
					data = getBuyADDChanceData(jz.vipLevel, 
							lve.buyBattleHuiShu, VipData.buyLveDuoTimes, PurchaseConstants.LVE_DUO_BATTLE);
					if(data != null){
						// 全部
						int all = getAllBattleCountToday(jz.vipLevel, data[3]) ;
						// 当前vip等级，玩家可以掠夺的最大次数
						resp.setNowMaxBattleCount(all);
						if(data[0] == 1){
							resp.setBuyNextBattleCount(data[1]);
							resp.setBuyNextBattleYB(data[2]);
							resp.setRemainBuyHuiShi(data[3] - lve.buyBattleHuiShu);
						}
					}else{
						resp.setNowMaxBattleCount(0);
					}
				
				}
			}
		}else{
			// 数据出错，购买失败
			resp.setIsOk(3);
			log.error("君主：{}购买掠夺'回数'失败，purchase配置文件无响应条目", jz.id);
		}
		session.write(resp.build());
	}

	public void dealBattleResult(int id, IoSession session, Builder builder){
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.error("君主不存在");
			return;
		}
		long jId = junZhu.id;
		LveBattleEndReq.Builder req = (LveBattleEndReq.Builder)builder;
		long enemyId = req.getEnemyId();
		JunZhu enemy = HibernateUtil.find(JunZhu.class, enemyId);
		long winId = req.getWinId();
		int zhandouId = req.getZhandouId();
		List<Bing.Builder> bings = req.getBingsBuilderList();
		
		BattleInfo4Pvp battleInfo4Pvp = fightingLock.remove(enemyId);
		if(battleInfo4Pvp == null || 
				(System.currentTimeMillis() - battleInfo4Pvp.startTime)/1000 > CanShu.MAXTIME_LUEDUO + PvpMgr.inst.PVP_BATTLE_DELAY_TIME) {
			winId = enemyId;
		}
		
		
		int num =0;
//		boolean isR = isRollBackAll(enemyId);
		// 赢了重置
		if(winId == jId){
			ldnpcMapHP.remove(enemyId);
			rollbackHpTime.remove(enemyId);
			log.info("防守方：{}血量记录从缓存map中remove掉", enemyId);
		}else{
			Map<Integer, int[]> zhanWeiInfo = ldnpcMapHP.get(enemyId);
			if(zhanWeiInfo != null){
				for(Bing.Builder bing: bings){
					int zhanwei = bing.getId();
					int hp = bing.getHp();
					int[] idhp = zhanWeiInfo.get(zhanwei);
					if(idhp != null){
						zhanWeiInfo.put(zhanwei, new int[]{idhp[0], hp});
						num ++;
					}
				}
				if(req.getEnemyHp() <= 0){
					if(num != 0){
						if(enemy != null){
							zhanWeiInfo.put(101, new int[]{-1, (int)Math.floor(enemy.shengMingMax * 0.01)});
							// 保险起见
							ldnpcMapHP.put(enemyId, zhanWeiInfo);
							rollbackHpTime.put(enemyId, new Date().getTime());
							log.info("防守方：{}战斗结束，重新set血量结束", enemyId);
						}else{
							// 玩家不存在
							ldnpcMapHP.remove(enemyId);
							rollbackHpTime.remove(enemyId);
							log.info("防守方：{}血量记录从缓存map中remove掉", enemyId);
						}
					}else{
						// 都挂掉了，remove掉
						ldnpcMapHP.remove(enemyId);
						rollbackHpTime.remove(enemyId);
						log.info("防守方：{}血量记录从缓存map中remove掉", enemyId);
					}
				}else{
					zhanWeiInfo.put(101, new int[]{-1, req.getEnemyHp()});
					// 保险起见
					ldnpcMapHP.put(enemyId, zhanWeiInfo);
					rollbackHpTime.put(enemyId, new Date().getTime());
					log.info("防守方：{}战斗结束，重新set血量结束", enemyId);
				}
			}
		}

		int L = (int)CanShu.LUEDUO_CANSHU_L;
		
		LveDuoBean fangshouLve = HibernateUtil.find(LveDuoBean.class, enemyId);
		if(fangshouLve == null){
			fangshouLve = new LveDuoBean(enemyId);
		}
		
		LveZhanDouRecord zhanr = HibernateUtil.find(LveZhanDouRecord.class, 
				zhandouId);
		if(zhanr == null){
			zhanr = new LveZhanDouRecord(zhandouId, jId,
					enemyId, new Date(), 0);
		}
		int fangshouFangSuishi = 0;
		if(winId == jId){
			/*
			 * 1-- 贡金，在显示的时候，是否是敌对国，数据不同，已经确定
			 */
			Integer vI =  willLostGongJin.get(jId);
			int v = vI == null? 0: vI;
			if(v != 0){
				RankingGongJinMgr.inst.addGongJin(jId, v);
				log.info("君主：{}参加掠夺挑战敌人获得胜利，得到贡金：{}", jId, v);

				// 防守方损失N%的贡金 或者 防守方损失M%的贡金
				fangshouFangSuishi = v -L;
				if(fangshouFangSuishi > 0){
					RankingGongJinMgr.inst.addGongJin(enemyId, -fangshouFangSuishi);
					log.info("君主：{}在掠夺中被挑战，且失败，失去贡金：{}", enemyId, v);
				}
			}

			/*
			 *2--  A 挑战B，A国家对B国家的仇恨值增加 
			 */
			if(enemy!= null && junZhu.guoJiaId != enemy.guoJiaId){
				GuoJiaMgr.updateCountryHate(junZhu.guoJiaId, enemy.guoJiaId, v);
				log.info("君主：{}，国家：{}，主动掠夺{}国的敌人{},胜利，仇恨值增加：{}",
						jId, junZhu.guoJiaId, enemy.guoJiaId, enemyId, v );
			}
	
			int alliaceJianShe = 0;
			int addGuoJiaSW = 0;
		
			// 是否是敌对国
			GuoJiaBean g = HibernateUtil.find(GuoJiaBean.class, junZhu.guoJiaId);
			// 敌对国
			if(g != null && (g.diDuiGuo_1 == enemy.guoJiaId || g.diDuiGuo_2 == enemy.guoJiaId)){
				zhanr.isHateGuoJia = true;
				// ii.挑战方所在联盟获得Y建设值，被挑战方所在联盟损失Y建设值
				alliaceJianShe = (int)CanShu.LUEDUO_CANSHU_Y;
				// iii.挑战方所在国家获得B声望值
				addGuoJiaSW = (int)CanShu.LUEDUO_CANSHU_B;
			}else{
				 // 非敌对国
				zhanr.isHateGuoJia = false;
				// ii.挑战方所在联盟获得X建设值，被挑战方所在联盟损失X建设值
				alliaceJianShe = (int)CanShu.LUEDUO_CANSHU_X;
				// iii.挑战方所在国家获得A声望值
				addGuoJiaSW = (int)CanShu.LUEDUO_CANSHU_A;
			}

			/*
			 *3-- 联盟建设值发生变化
			 * 胜利方获取，失败方损失
			 */
			AllianceBean gongjiAlli = AllianceMgr.inst.getAllianceByJunZid(jId);
			AllianceBean enemyAlli = AllianceMgr.inst.getAllianceByJunZid(enemyId);

			if(gongjiAlli != null){
				// 得到建设值
				AllianceMgr.inst.changeAlianceBuild(gongjiAlli, alliaceJianShe);
				log.info("君主：{}主动掠夺：{}，成功，联盟：{}获得建设值：{}",
						jId, enemyId, gongjiAlli.id, alliaceJianShe);
				//TODO 王专
				// <LianmengEvent ID="17" str="%d掠夺d%成功，联盟获得%d建设值！" />
				LianmengEvent e = AllianceMgr.inst.lianmengEventMap.get(17);
				String eventStr = e == null? "": e.str;
				eventStr = eventStr
						.replaceFirst("%d", junZhu.name)
						.replaceFirst("%d", enemy.name)
						.replaceFirst("%d", alliaceJianShe+"");
				AllianceMgr.inst.addAllianceEvent(gongjiAlli.id, eventStr);
			}
	
			if(enemyAlli != null){
				// 掠夺成功enemy 是被掠夺的人
				int lostBuild = alliaceJianShe;
				EventMgr.addEvent(ED.been_lve_duo , new Object[] {junZhu.id, 
						enemy.id, lostBuild, zhandouId, enemyAlli.id});
//				// 损失建设值
//				//<LianmengEvent ID="14" str="%d被%d掠夺成功，联盟损失%d建设值！" />
//				AllianceMgr.inst.changeAlianceBuild(enemyAlli, -alliaceJianShe);
//				log.info("君主：{}主动掠夺：{}，成功，联盟：{}损失建设值：{}",
//						jId, enemyId, enemyAlli.id, alliaceJianShe);
//				//TODO 王专
//				LianmengEvent e = AllianceMgr.inst.lianmengEventMap.get(14);
//				String eventStr = e == null? "": e.str;
//				eventStr = AllianceMgr.inst.lianmengEventMap.get(14).str
//						.replaceFirst("%d", enemy.name)
//						.replaceFirst("%d", junZhu.name)
//						.replaceFirst("%d", alliaceJianShe+"");
//				AllianceMgr.inst.addAllianceEvent(enemyAlli.id, eventStr);
			}
	
			/*
			 *4-- 声望值发生变化,胜利方获得，失败方不损失
			 */
			// 胜利方获取国家声望
			GuoJiaMgr.inst.changeGuoJiaShengWang(g, addGuoJiaSW);
			log.info("君主：{}主动掠夺：{}，成功，国家：{}获得声望值：{}",
					jId, enemyId, g.guoJiaId, addGuoJiaSW);

			/*
			 * 1.1 去掉联盟声望的操作
			 * --新--
			 * 2016年3月20日15:11:32，bug http://192.168.0.250:81/index.php/bug/41012
			 */
//				// 得到联盟声望
//				AllianceBean gongjiAlli = AllianceMgr.inst.getAllianceByJunZid(jId);
				if(gongjiAlli != null){
					int addMengSW = addGuoJiaSW;//(int)Math.floor(v * 0.1);
					AllianceMgr.inst.changeAlianceReputation(gongjiAlli, addMengSW );
//					zhanr.addMengShengWang = addMengSW;
					log.info("君主：{}主动掠夺：{}，成功，联盟：{}获得声望值：{}",
							jId, enemyId, gongjiAlli.id, addMengSW);
				}
				
			
			/*
			 *5--战斗记录数据
			 */
			zhanr.result1 = PVPConstant.GONG_JI_WIN;

			zhanr.gongJiGetGongjin = v;	
			zhanr.gongjiGetGuoSW = addGuoJiaSW;
			zhanr.gongJiGetMengJianShe = alliaceJianShe;
		
			/*
			 *  6--防守方数据： 记录上次结束时间
			 */
			fangshouLve.lastBattleEndTime = new Date();
		}else{
			/*
			 *  攻击失败 
			 *  i.挑战方获得L的贡金做为保底奖励，被挑战方无损失
				ii.挑战方所在联盟获得Z建设值
				iii.挑战方所在国家获得C声望值
			 */
			 //以下部分参数，策划配置数据，是int型，CanShu就不改了
			
			RankingGongJinMgr.inst.addGongJin(jId, L);
			log.info("君主：{}参加掠夺挑战敌人，失败，得到保底贡金：{}", jId, L);
			
			int Z= (int)CanShu.LUEDUO_CANSHU_Z;
			AllianceBean gongjiAlli = AllianceMgr.inst.getAllianceByJunZid(jId);
			if(gongjiAlli != null){
				// 得到建设值
				AllianceMgr.inst.changeAlianceBuild(gongjiAlli, Z);
				log.info("君主：{}主动掠夺：{}，失败，联盟：{}获得保底建设值：{}",
						jId, enemyId, gongjiAlli.id, Z);
			}
			
			int C = (int)CanShu.LUEDUO_CANSHU_C;
			GuoJiaBean g = HibernateUtil.find(GuoJiaBean.class, junZhu.guoJiaId);
			GuoJiaMgr.inst.changeGuoJiaShengWang(g, C);
			log.info("君主：{}主动掠夺：{}，失败，国家：{}获得保底声望值：{}",
					jId, enemyId, g.guoJiaId, C);
	
			zhanr.gongJiGetGongjin = L;	
			zhanr.gongjiGetGuoSW = Z;
			zhanr.gongJiGetMengJianShe = C;

			zhanr.result1 = PVPConstant.GONG_JI_LOSE;
			
			
			// 防守方数据回退
			fangshouLve.todayWin -= 1;
			fangshouLve.hisWin -= 1;
			// 防守方成功，所以不计算保护期
			fangshouLve.lastBattleEndTime =null;
			
			// 掠夺别人失败
//			EventMgr.addEvent(ED.Lve_duo_fail , new Object[] {jId, junZhu.name, enemy.name});
		}
		
		HibernateUtil.save(fangshouLve);
		HibernateUtil.save(zhanr);

		willLostGongJin.remove(jId);
		LveBattleEndResp.Builder resp = LveBattleEndResp.newBuilder();
		resp.setJifen(zhanr.gongJiGetGongjin);
		resp.setShengwang(zhanr.gongjiGetGuoSW);
		resp.setBuild(zhanr.gongJiGetMengJianShe);
		session.write(resp.build());
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(junZhu.id , DailyTaskConstants.lueDuo, 1));
//		int anweiJiang = (int) Math.round(CanShu.LUEDUO_COMFORTED_AWARD_K *
//				fangshouFangSuishi + CanShu.LUEDUO_COMFORTED_AWARD_B);
		
//		EventMgr.addEvent(ED.been_lve_duo , new Object[] {junZhu.id, junZhu.name,
//				enemy.id, enemy.name, anweiJiang});
	}
	public void getNextItem(int id, IoSession session, Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("君主不存在");
			return;
		}
		LveNextItemReq.Builder  req = (LveNextItemReq.Builder)builder;
		int rankType = req.getRankType();
		int pageNo = req.getPageNo();
		int guojiaId = req.getGuojiaId();
		LveNextItemResp.Builder resp = LveNextItemResp.newBuilder();
		int showMengId = 0;
		if(rankType == 1){
			List<LianMengInfo.Builder> menglist = 
					RankingMgr.inst.getLianMengRank(pageNo, guojiaId, null, 201);
			if(menglist == null){
				log.error("君主id:{}, 国家id：{}， 没有排行记录", jz.id, guojiaId);
				session.write(resp.build());
				return;
			}
			for(LianMengInfo.Builder lian: menglist){
				if(lian.getMengId() <= 0){
					continue;
				}
				resp.addMengList(lian);
				// 第一个正常的联盟作为显示的联盟id
				if(showMengId <= 0){
					showMengId = lian.getMengId();
				}
			}
		}else if(rankType == 2){
			showMengId = req.getMengId();
		}
		GuoJiaBean guo = HibernateUtil.find(GuoJiaBean.class, jz.guoJiaId);
		if (guo == null){
			log.error("国家id是：{}没有数据库数据", jz.guoJiaId);
			guo = new GuoJiaBean();
			guo.guoJiaId = jz.guoJiaId;
		}
		List<JunZhuInfo.Builder> jl = fillJunZhuInfo(guo.diDuiGuo_1, guo.diDuiGuo_2, showMengId);
		for(JunZhuInfo.Builder b: jl){
			resp.addJunList(b);
		}
		session.write(resp.build());
	}

	public void getZhanDouRecord(int id, IoSession session, Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求掠夺战斗记录出错，君主不存在");
			return;
		}
		long jId = jz.id;
		String where = "where gongJiJunId = " + jId + " or fangShouJunId = " + jId;
		long now = System.currentTimeMillis() / 1000;
		List<LveZhanDouRecord> recordList = HibernateUtil.list(
				LveZhanDouRecord.class, where);
		LveBattleRecordResp.Builder resp = LveBattleRecordResp.newBuilder();
		LveBattleItem.Builder info = null;
		int passTime = 0;
		if (recordList == null || recordList.size() == 0) {
			session.write(resp.build());
			return;
		}
		for (LveZhanDouRecord reco : recordList) {
			passTime = (int) (now - reco.time.getTime() / 1000);
			if (passTime > LveStaticData.save_zhanDou_record_time) {
				// 数据库删除时间过了的战斗记录
				HibernateUtil.delete(reco);
				log.info("因为超出7天限制，删除一条掠夺的战斗记录, zhandouId：{}", reco.zhandouId);
				continue;
			}
			info = LveBattleItem.newBuilder();
			info.setZhandouId(reco.zhandouId);
			info.setGongJiId(reco.gongJiJunId);
			info.setGongJiwin(reco.result1);
			info.setTime(reco.time.getTime());
			int gongJinChange = 0;
			JunZhu other = null;
			if (jId == reco.gongJiJunId) {
				other = HibernateUtil.find(JunZhu.class, reco.fangShouJunId);
				gongJinChange = reco.gongJiGetGongjin;
			} else if(jId == reco.fangShouJunId){
				other = HibernateUtil.find(JunZhu.class, reco.gongJiJunId);
				if(reco.result1 == PVPConstant.GONG_JI_WIN){
					gongJinChange = reco.gongJiGetGongjin - (int)CanShu.LUEDUO_CANSHU_L;
				}else gongJinChange = 0;
			}
			if(other == null){
				continue;
			}
			info.setAnotherId(other.id);
			info.setAnotherLevel(other.level);
			info.setAnotherRoleId(other.roleId);
			info.setAnotherGuoJiaId(other.guoJiaId);
			info.setAnotherName(other.name);
			info.setAnotherZhanli(getZhanli(other));
			AllianceBean b = AllianceMgr.inst.getAllianceByJunZid(other.id);
			if(b == null){
				info.setAnotherMengName("");
			}else{
				info.setAnotherMengName(b.name);
			}
			info.setLostXiaYi(gongJinChange);
			resp.addInfo(info);
		}
		session.write(resp.build());
		LveDuoBean be = HibernateUtil.find(LveDuoBean.class, jId);
		if (be != null) {
			be.hasRecord = false;
			HibernateUtil.save(be);
		}
	}
	public void sendHelp(int id, IoSession session, Builder builder){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("掠夺求助出错，君主不存在");
			return;
		}
		long jId = jz.id;
		AlliancePlayer p = HibernateUtil.find(AlliancePlayer.class, jId);
		// 玩家可能在战斗过程中退出了联盟
		if(p == null || p.lianMengId <= 0){
			log.error("掠夺求助出错， 君主已经退出联盟");
			return;
		}
		LveHelpReq.Builder req = (LveHelpReq.Builder)builder;
		long enemyId = req.getEnemyId();
		long remainHp = req.getRemainHp();
		JunZhu enemy =   HibernateUtil.find(JunZhu.class, enemyId);
		if(enemy == null){
			log.error("君主：{}联盟求助失败,敌人不存在：{}", jId, enemyId);
			return;
		}
		int remianAll = (int)Math.floor(remainHp / enemy.shengMingMax);
		String lastHp = remianAll <= 0? "1%": (remianAll * 100)+"%";
		String  chatContent = helpContent.replace("XX", jz.name).
				replace("YY", enemy.name).
				replace("ZZ", lastHp).replace("AA", enemy.name).concat("#" +enemyId);
		ChatPct.Builder b = ChatPct.newBuilder();
		Channel value = Channel.valueOf(1);// 联盟频道1
		b.setChannel(value);
		b.setContent(chatContent);
		b.setIsLveDuoHelp(true);
		b.setSenderId(jz.id);
		b.setSenderName(jz.name);
		ChatMgr.inst.addMission(PD.C_Send_Chat, session, b);
		
		// 告诉前端 发送完成
		session.write(PD.LVE_HELP_RESP);
	}

	
	public void addGongJinDailyAward(JunZhu junZhu, byte sendType){
		long jzid = junZhu.id;
		int min = 1;
		int max = 1;
		long rank = RankingGongJinMgr.DB.zrevrank(RankingGongJinMgr.gongJinPersonalRank, jzid+"");
		if(rank == -1){
			log.info("玩家:{}没有贡金数据", jzid);
			return;
		}
		Double g = RankingGongJinMgr.DB.zScoreGongJin(RankingGongJinMgr.gongJinPersonalRank, 
				jzid+"");
		if(g == null){
			log.info("玩家:{}没有贡金数据", jzid);
			return;
		}
		if(g.doubleValue() == -1){
			log.info("玩家:{}已经退出联盟，贡金值为-1", jzid);
			return;
		}
		LveDuoBean bean = HibernateUtil.find(LveDuoBean.class, jzid);
		if(bean == null) bean = new LveDuoBean(jzid);
		long now = System.currentTimeMillis();
		int i = 1;
		// 玩家登陆
		if(bean.getGongJinTime != null){
			Date last = bean.getGongJinTime;
			if (sendType == PVPConstant.LOGIN_SEND_EMAIL) {
				// 今天的奖励已经发出去了, 今日奖励已经给出表明昨天的也已经给出。
				if(DateUtils.isSameDay(last)){
					return;
				}else if(DateUtils.isBeforeDay(now, last.getTime())){
					return;
				}
				// 计算发送多少邮件
				int all =  (int)(now - last.getTime() ) /(24 * 60 * 60 * 1000);
				i = all<=0 ? 1: all; 
				// 玩家在线发送奖励
			}else if(sendType == PVPConstant.ONLINE_SEND_EMAIL){
				// 当日奖励已经领取了
				if(DateUtils.isSameDay(last)){
					return;
				}
			}
		}
		
		List<LueduoPersonRank> list = RankingGongJinMgr.persRankList;
		if(list == null){
			log.error("LueduoPersonRank.xml 无整个条目配置");
			return;
		}
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(100002);
		if (mailConfig == null) {
			log.error("mail.xml配置文件找不到type=100002的数据 ");
			return;
		}
		LueduoPersonRank sendr = null;
		for(LueduoPersonRank r: list){
			min = r.min;
			max = r.max;
			if(rank >= min && rank <= max){
				sendr = r;
				break;
			}
		}
		if(sendr == null){
			return;
		}
		String content = mailConfig.content;
		content = content.replace("***", rank +"");
		String senderName = mailConfig.sender;
		boolean suc = false;
		for(int k=1; k<= i; k++){
			suc = EmailMgr.INSTANCE.sendMail(junZhu.name, content,
					sendr.award, senderName, mailConfig, "");
		}
//		boolean suc = EmailMgr.INSTANCE.sendMail(junZhu.name, content,
//				sendr.award, senderName, mailConfig, "");
		if (suc) {
			if (sendType == PVPConstant.LOGIN_SEND_EMAIL) {
				bean.getGongJinTime = DateUtils.getLast10();
				log.info("玩家:{}再次登录游戏发送前一天的每日贡金排行奖励，成功，奖励是:{}，bean.getGongJinTime 记做：{} ",
						 junZhu.name, sendr.award, bean.getGongJinTime);
			}else if(sendType == PVPConstant.ONLINE_SEND_EMAIL){
				bean.getGongJinTime = new Date();
				log.info("玩家:{}在线发送当天每日贡金排行奖励，成功，奖励是:{}，bean.getGongJinTime 记做：{} ",
						 junZhu.name, sendr.award, bean.getGongJinTime);
			}
			HibernateUtil.save(bean);
		} else {
			log.error("玩家:{}发送每日个人贡金排行奖励失败， sendMail出错导致", junZhu.name);
		}
	}

	/**
	 * 每天晚上10点向所有贡金排行的玩家发送排行奖励
	 * @param jz
	 */
	public void sendGongJinDayAward(JunZhu jz, IoSession session,
			LveConfirmResp.Builder resp, LveDuoBean lve){
//		int id = 100002;
//		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(id);
//		if (mailConfig == null) {
//			log.error("mail.xml配置文件找不到type={}的数据", id);
//			return;
//		}
//		String senderName = mailConfig.sender;
//		String content = mailConfig.content;
//	
		List<LueduoPersonRank> list = RankingGongJinMgr.persRankList;
		if(list == null){
			log.error("LueduoPersonRank.xml 无整个条目配置");
			return;
		}
		if(lve.getGongJinTime != null && DateUtils.isSameDay(lve.getGongJinTime)){
			log.error("今天的奖励已经领取了");
			resp.setIsOk(7);
			session.write(resp.build());
			return;
		}
		int min = 1;
		int max = 1;
		long rank = RankingGongJinMgr.DB.zrevrank(RankingGongJinMgr.gongJinPersonalRank, jz.id+"");
		if(rank == -1){
			log.error("玩家:{}没有贡金排名", jz.id);
			return;
		}
		Double g = RankingGongJinMgr.DB.zScoreGongJin(RankingGongJinMgr.gongJinPersonalRank, 
				jz.id+"");
		if(g == null){
			log.error("玩家:{}没有贡金数据", jz.id);
			return;
		}
		if(g.doubleValue() == -1){
			log.error("玩家:{}已经退出联盟，贡金值为-1", jz.id);
			return;
		}
		for(LueduoPersonRank r: list){
			min = r.min;
			max = r.max;
			if(rank >= min && rank <= max){
				boolean suc = AwardMgr.inst.giveReward(session, r.award, jz);
				if (suc) {
					log.info("玩家：{}领取每日贡金排行奖励成功,award 是：{}, 发送时间是：{}", jz.id, r.award, new Date());
					lve.getGongJinTime = new Date();
					HibernateUtil.save(lve);
					resp.setIsOk(8);
					session.write(resp.build());
				} else {
					log.error("玩家:{}领取每日贡金排行奖励失败", jz.id);
				}
				break;
			}
		}
	}

	/**
	 * 每天晚上10点向所有贡金联盟排行的联盟发送邮件奖励
	 */
	public void sendGongJinAllianceAward(){
		int id = 100003;
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(id);
		if (mailConfig == null) {
			log.error("mail.xml配置文件找不到type={}的数据", id);
			return;
		}
		String senderName = mailConfig.sender;
		String content = mailConfig.content;
		List<LueduoLianmengRank> list = RankingGongJinMgr.lianMengRankList;
		if(list == null){
			log.error("LueduoLianmengRank.xml 无整个条目配置");
			return;
		}
		int min = 1;
		int max = 1;
		for(LueduoLianmengRank r: list){
			min = r.min;
			max = r.max;
			Map<String, Double> map = 
					RankingGongJinMgr.inst.getPaiHangOfType(min-1, max-1, RankingGongJinMgr.gongJinAllianceRank);
			if(map == null || map.size() == 0){
				continue;
			}
			for(Map.Entry<String, Double> entry: map.entrySet()){
				double score = entry.getValue();
				// 小于等于0的不发送贡金奖励
				if(score <= 0){
					continue;
				}
				String lmIdStr = entry.getKey();
				if(lmIdStr == null ){
					continue;
				}
				int lmId = Integer.parseInt(lmIdStr);
				AllianceBean alli = HibernateUtil.find(AllianceBean.class, lmId);
				if(alli == null){
					continue;
				}
				int rank = min++;
				// 增加
				AllianceMgr.inst.changeAlianceBuild(alli, r.award);
				//AllianceMgr.inst.addAllianceExp(r.LMExp, alli);
				
				//事件
				LianmengEvent e = AllianceMgr.inst.lianmengEventMap.get(26);
				String eventStr = e == null? "": e.str;
				eventStr = eventStr.replaceFirst("%d", rank+"")
						.replaceFirst("%d", r.award+"");
				AllianceMgr.inst.addAllianceEvent(alli.id, eventStr);
				
				content = mailConfig.content;
				content = content.replace("***a", rank +"")
							     .replace("***b", r.award+"");
				
				long mengzhuid = alli.creatorId;
				List<AlliancePlayer> memberList = AllianceMgr.inst.getAllianceMembers(lmId);
				for(AlliancePlayer member : memberList) {
					JunZhu mengZhu = HibernateUtil.find(JunZhu.class, member.junzhuId);
					if(mengZhu == null ){
						continue;
					}
					boolean suc = EmailMgr.INSTANCE.sendMail(mengZhu.name, content, 
							"", senderName, mailConfig, "");
					if (suc) {
						log.info("掠夺积分联盟排行奖励发送了，发送通知：{}邮件成功，发送时间是：{}", 
								mengzhuid, r.award, new Date());
					} else {
						log.error("掠夺积分排行奖励发送了，发送通知：{}邮件失败，", mengZhu.name);
					}
				}
			}
		}
	}
	
	public void addMission(int id, IoSession session, Builder builder) {
		Mission m = new Mission(id, session, builder);
		missions.add(m);
	}

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
	public void shutdown(){
		missions.add(exit);
	}
	public void handle(Mission m) {
		int id = m.code;
		IoSession session = m.session;
		Builder builder = m.builer;
		switch (m.code) {
		case PD.LVE_DUO_INFO_REQ:
			getMainLveDuoInfo(id, session, builder);
			break;
		case PD.LVE_GO_LVE_DUO_REQ:
			goLveDuo(id, session, builder);
			break;
		case PD.ZHANDOU_INIT_LVE_DUO_REQ:
			lveDuoInitData(id, session, builder);
			break;
		case PD.LVE_BATTLE_RECORD_REQ:
			getZhanDouRecord(id, session, builder);
			break;
		case PD.LVE_CONFIRM_REQ:
			doConfirm(id, session, builder);
			break;
		case PD.LVE_BATTLE_END_REQ:
			dealBattleResult(id, session, builder);
			break;
		case PD.LVE_NEXT_ITEM_REQ:
			getNextItem(id, session, builder);
			break;
		case PD.LVE_HELP_REQ:
			sendHelp(id, session, builder);
			break;
		default:
			log.error("未处理的消息{}", id);
			break;
		}
		
	}
	
	public void saveLveDuoFSZuheId(int fangShouzuhe, long jId){
		LveDuoBean bean = HibernateUtil.find(LveDuoBean.class, jId);
		if(bean == null){
			bean = new LveDuoBean(jId);
		}
		bean.fangShouZuHeId = fangShouzuhe;
		HibernateUtil.save(bean);
	}
	public void saveLveDuoGJZuheId(int gongJizuhe, long jId){
		LveDuoBean bean = HibernateUtil.find(LveDuoBean.class, jId);
		if(bean == null){
			bean = new LveDuoBean(jId);
		}
		bean.gongJiZuHeId = gongJizuhe;
		HibernateUtil.save(bean);
	}

	public void showLveDuoRed(JunZhu junZhu){
		AlliancePlayer p = HibernateUtil.find(AlliancePlayer.class, junZhu.id);
		if(p != null && p.lianMengId > 0){
			String where = " select count(*) from LveDuoMI where lmId = " + p.lianMengId + " "
					+ " and willLostBuildTime != null and willLostBuildTime <= now()";
			int c = HibernateUtil.getCount(where);
			if(c > 0){
				SessionUser su = SessionManager.inst.findByJunZhuId(junZhu.id);
				if(su != null)
				FunctionID.pushCanShowRed(junZhu.id, su.session, FunctionID.lianmengJunQingLveDuo);
			}
		}
	}
	@Override
	public void proc(Event e) {
		switch (e.id) {
			case ED.ACC_LOGIN:
				// 掠夺登陆奖励领取
				if (e.param != null && e.param instanceof Long) {
					long jzid = (Long) e.param;
					JunZhu junZhu = HibernateUtil.find(JunZhu.class, jzid);
					if (junZhu == null) {
						break;
					}
					// 看 掠夺功能是否开启
					boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.lveDuo, jzid, junZhu.level);
					if(!isOpen){
						break;
					}
					try{
						addGongJinDailyAward(junZhu, PVPConstant.LOGIN_SEND_EMAIL);
					}catch (Exception e2) {
						e2.printStackTrace();
					}
					// 显示掠夺军情的红点
					try{
						showLveDuoRed(junZhu);
					}catch(Exception e3){
						e3.printStackTrace();
					}
				}
				break;
			case ED.REFRESH_TIME_WORK:
				IoSession session = (IoSession) e.param;
				if(session == null){
					break;
				}
				JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
				if(jz == null){
					break;
				}
				// 看 掠夺功能是否开启
				boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.lveDuo, jz.id, jz.level);
				if(!isOpen){
					break;
				}
				boolean yes = DateUtils.isInDeadline(CanShu.OPENTIME_LUEDUO, CanShu.CLOSETIME_LUEDUO);
				if(!yes){
					return;
				}
				LveDuoBean bean = HibernateUtil.find(LveDuoBean.class, jz.id);
				if(bean != null && bean.hasRecord){
					/*
					 * 前台 页面等级发生改变，所以不用显示具体行为，下同
					 */
					/*
					 * 又改为具体的了  ~ ~ 欲哭无泪啊 
					 * */
					FunctionID.pushCanShowRed(jz.id, session, FunctionID.LueDuoJiLu);
				}
				int all = LveStaticData.free_all_battle_times;
				int used = 0;
				if(bean != null){
					resetLveDuoBean(bean);
					all = bean.todayTimes;
					used = bean.usedTimes;
				}
				if(all > used){
					FunctionID.pushCanShowRed(jz.id, session, FunctionID.LueDuoCiShu);
				}
				break;
			}
	}
	
	@Override
	protected void doReg() {
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
		EventMgr.regist(ED.ACC_LOGIN, this);
	}
	public int getGongJin(){
		return 0;
	}
}
