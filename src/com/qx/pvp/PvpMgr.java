package com.qx.pvp;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.BaiZhan;
import com.manu.dynasty.template.BaiZhanNpc;
import com.manu.dynasty.template.BaiZhanRank;
import com.manu.dynasty.template.BaizhanPipei;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.CartTemp;
import com.manu.dynasty.template.Duihuan;
import com.manu.dynasty.template.GongjiType;
import com.manu.dynasty.template.GuYongBing;
import com.manu.dynasty.template.Mail;
import com.manu.dynasty.template.Purchase;
import com.manu.dynasty.template.PvpNpc;
import com.manu.dynasty.template.VIP;
import com.manu.dynasty.template.VipFuncOpen;
import com.manu.dynasty.util.DateUtils;
import com.manu.dynasty.util.MathUtils;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.qx.account.FunctionOpenMgr;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
import com.qx.award.AwardMgr;
import com.qx.email.EmailMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.huangye.shop.ShopMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.mibao.MibaoMgr;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.pve.PveMgr;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.timeworker.FunctionID;
import com.qx.util.TableIDCreator;
import com.qx.vip.PlayerVipInfo;
import com.qx.vip.VipData;
import com.qx.vip.VipMgr;
import com.qx.world.GameObject;
import com.qx.world.Mission;
import com.qx.yabiao.YaBiaoBean;
import com.qx.yabiao.YaBiaoHuoDongMgr;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

import log.ActLog;
import qxmobile.protobuf.PvpProto.BaiZhanInfoResp;
import qxmobile.protobuf.PvpProto.BaiZhanResult;
import qxmobile.protobuf.PvpProto.BaiZhanResultResp;
import qxmobile.protobuf.PvpProto.BuyCiShuInfo;
import qxmobile.protobuf.PvpProto.ChallengeReq;
import qxmobile.protobuf.PvpProto.ChallengeResp;
import qxmobile.protobuf.PvpProto.CleanCDInfo;
import qxmobile.protobuf.PvpProto.ConfirmExecuteReq;
import qxmobile.protobuf.PvpProto.ConfirmExecuteResp;
import qxmobile.protobuf.PvpProto.GetAwardInfo;
import qxmobile.protobuf.PvpProto.OpponentInfo;
import qxmobile.protobuf.PvpProto.PayChangeInfo;
import qxmobile.protobuf.PvpProto.RefreshOtherInfo;
import qxmobile.protobuf.PvpProto.ZhandouItem;
import qxmobile.protobuf.PvpProto.ZhandouRecordResp;
import qxmobile.protobuf.ZhanDou;
import qxmobile.protobuf.ZhanDou.Group;
import qxmobile.protobuf.ZhanDou.Node;
import qxmobile.protobuf.ZhanDou.NodeProfession;
import qxmobile.protobuf.ZhanDou.NodeType;
import qxmobile.protobuf.ZhanDou.PvpZhanDouInitReq;
import qxmobile.protobuf.ZhanDou.ZhanDouInitResp;

public class PvpMgr extends EventProc implements Runnable {
	public static Logger log = LoggerFactory.getLogger(PvpMgr.class);
	public static PvpMgr inst;
	public Map<Integer, BaiZhanNpc> baiZhanNpcMap;
	public Map<Integer, BaiZhanNpc> npcs;
	public Map<Integer, BaiZhan> baiZhanMap;
	public Map<Integer, BaiZhanRank> baiZhanRankMap;
	public static Map<Integer, GuYongBing> guYongBingMap;
	public Map<Integer, Duihuan> duihuanMap;
	public Map<Integer, BaizhanPipei> piPeiMap = new HashMap<Integer, BaizhanPipei>();
	public static int duiHuanNum = 1;
	public static int yongBingZhiYeMax = 1;
	public static int yongBingZhiYeMin = 1000;
	public static final Redis DB = Redis.getInstance();
	public static final String KEY = "baiZhan_" + GameServer.serverId;
	public static Random r = new Random();
	public static int maxChoice = 10 + 1;
	public static int getAwardTimes = 2;
	public static Object pvpRankLock = new Object();
	public static Set<Integer> yinDaoTargetLock = new HashSet<Integer>();
	/** 雇佣兵的个数 **/
	public static final int ALL = 4;
	public static boolean isSaveJunXianList = false;
	/* 存放正在战场中的君主 或者 npc */
	static public ConcurrentHashMap<Long, BattleInfo4Pvp> goingMap;
	/* 战斗id */
//	public AtomicInteger zhandouIdMgr = new AtomicInteger(1);
	/* 战斗录像 */
	//public static Map<Integer, ZhanDouInitResp> replayCache = new HashMap<Integer, ZhanDouInitResp>();
	/*
	 * 存放玩家和对手的战斗数据
	 */
	public static Map<Long, ChallengeResp.Builder> armyCache = new HashMap<Long, ChallengeResp.Builder>();
	/*
	 * 存放双方的雇佣兵 int数组 是8个元素
	 */
	public static Map<Long, int[]> bingsCache = new HashMap<Long, int[]>();
	public static Map<Long, int[]> bingsCache4YB = new HashMap<Long, int[]>();
	public LinkedBlockingQueue<Mission> missions = new LinkedBlockingQueue<Mission>();
	public static Mission exit = new Mission(0, null, null);
	/**	类百战千军的战斗（有人挑战，别人不能挑战的），最大延迟时间，超过即为失败,单位-秒	**/
	public int PVP_BATTLE_DELAY_TIME = 60;

	public PvpMgr() {
		inst = this;
		goingMap = new ConcurrentHashMap<Long, BattleInfo4Pvp>();
		initData();
		// 开启线程
		new Thread(this, "PvpMgr").start();
	}

	@SuppressWarnings("unchecked")
	public void initData() {
		guYongBingMap = new HashMap<Integer, GuYongBing>();
		HashMap<Integer, BaiZhanNpc> baiZhanNpcMap = new HashMap<Integer, BaiZhanNpc>();
		HashMap<Integer, BaiZhanNpc> npcs = new HashMap<Integer, BaiZhanNpc>();
		HashMap<Integer, BaiZhan> baiZhanMap = new HashMap<Integer, BaiZhan>();
		HashMap<Integer, BaiZhanRank> baiZhanRankMap = new HashMap<Integer, BaiZhanRank>();		
		HashMap<Integer, Duihuan> duihuanMap = new HashMap<Integer, Duihuan>();
		HashMap<Integer, BaizhanPipei> piPeiMap = new HashMap<Integer, BaizhanPipei>();

		List<BaiZhanNpc> list = TempletService.listAll(BaiZhanNpc.class
				.getSimpleName());
		List<BaiZhan> baizhanList = TempletService.listAll(BaiZhan.class
				.getSimpleName());
		List<BaiZhanRank> baizhanRankList = TempletService
				.listAll(BaiZhanRank.class.getSimpleName());
		List<GuYongBing> guYongBingList = TempletService
				.listAll(GuYongBing.class.getSimpleName());
		List<Duihuan> duihuanList = TempletService.listAll(Duihuan.class
				.getSimpleName());
		List<BaizhanPipei> piPeiList = TempletService.listAll(BaizhanPipei.class
				.getSimpleName());
		for (Duihuan elem : duihuanList) {
			duihuanMap.put(elem.id, elem);
			if (elem.site > duiHuanNum)
				duiHuanNum = elem.site;
		}
		for (GuYongBing elem : guYongBingList) {
			guYongBingMap.put(elem.id, elem);
			if (elem.zhiye > yongBingZhiYeMax) {
				yongBingZhiYeMax = elem.zhiye;
			}
			if (elem.zhiye < yongBingZhiYeMin) {
				yongBingZhiYeMin = elem.zhiye;
			}
		}
		for (BaiZhanRank elem : baizhanRankList) {
			baiZhanRankMap.put(elem.rank, elem);
		}
		for (BaiZhan elem : baizhanList) {
			baiZhanMap.put(elem.id, elem);
		}
		for (BaiZhanNpc elem : list) {
			baiZhanNpcMap.put(elem.id, elem);
		}
		for (BaizhanPipei elem : piPeiList) {
			piPeiMap.put(elem.roomNum, elem);
		}
		
		this.baiZhanNpcMap = baiZhanNpcMap;
		this.baiZhanMap = baiZhanMap;
		this.baiZhanRankMap = baiZhanRankMap;
		this.duihuanMap = duihuanMap;
		this.piPeiMap = piPeiMap;
		// TODO 对于每次重新启动服务器，是否需要删除之前的数据，重新建立缓存
		// if (DB.exist_(KEY))
		// DB.del(KEY);
		// DB.del("baiZhan_1");
		// DB.del("baiZhan_2");
		// DB.del("baiZhan_3");
		if (!DB.exist_(KEY)) {
			long time = System.currentTimeMillis();
			for (int index = list.size() - 1; index > -1; index--) {
				BaiZhanNpc npc = list.get(index);
				initRedisList(npc);
			}
			log.info("redis的zset初始化5000名npc初始完成， 花费时间：{}",
					(System.currentTimeMillis() - time));
			time = System.currentTimeMillis();
			// 从大到小排名，获取member的排名， 从0开始的排序
			long rank = Redis.getInstance().zrank(KEY, "npc_5000");
			log.info("redis的zset中id是5000的元素的排名是: {}", rank);
			log.info("获取某一个member的排序值: 花费时间：{}",
					(System.currentTimeMillis() - time));
		}

		for (int index = 0; index < list.size(); index++) {
			BaiZhanNpc npc = list.get(index);
			initNPC(npcs, npc);
		}
		this.npcs = npcs;
		BaiZhanNpc cc = npcs.get(3600);
		log.info(" npcs的id是3600对应的配置行: " + cc.id);
		// 从大到小排名，获取member的排名， 从0开始的排序
		long rank = Redis.getInstance().zrank(KEY, "npc_5000");
		log.info("redis的zset中id是5000的元素的排名是: {}", rank);
	}
	/**初始化排行榜*/
	public void initRedisList(BaiZhanNpc npc) {
		int minRank = npc.minRank;
		int maxRank = npc.maxRank;
		for (int i = minRank; i <= maxRank; i++) {
			// i指的是排名，而"npc_" + i中的i指的是npc的id，i的值等于PvpBean中的rank值
			DB.zadd(KEY, i - 1, "npc_" + i);
		}
	}
/**初始化NPC数据*/
	public void initNPC(Map<Integer, BaiZhanNpc> npcs, BaiZhanNpc npc) {
		int minRank = npc.minRank;
		int maxRank = npc.maxRank;
		for (int i = minRank; i <= maxRank; i++) {
			// i指的是npc的id，为了将5000个npc的id和BaiZhanNpc.xlsx的行对应起来
			npcs.put(i, npc);
		}
	}

	/**响应客户端请求主界面信息*/
	public void getBaiZhanMainInfo(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求百战主页出错：君主不存在");
			return;
		}
		long jid = jz.id;
		PvpBean bean = HibernateUtil.find(PvpBean.class, jid);
		int junRank = getRankById(jid);
		if(junRank < 0 ){
			return;
		}
		if (bean == null) {
			bean = initJunZhuPVPInfo(jid, junRank);
			log.info("君主{}请求百战主界面信息，没有Pvp数据，创建PVPbean",jid);
			// 从有军衔开始就进行贡金的记录 去掉20160324
//			EventMgr.addEvent(ED.GET_JUNXIAN, new Object[]{jz.id,bean});
		} else {
			// 检查是否更新数据
			resetPvpBean(bean);
		}
		if (bean != null){
			//TODO 构建主页面信息返回协议
			BaiZhanInfoResp.Builder resp = BaiZhanInfoResp.newBuilder();
			resp.setRank(junRank);
			resp.setLeftTimes(bean.remain);
			resp.setTotalTimes(bean.usedTimes+bean.remain);
			resp.setTime(getCountDown(bean));
			resp.setHistoryHighRank(bean.highestRank);
			resp.setLasthistoryHighRank(bean.lastHighestRank);
			resp.setCdYuanBao(getYuanBao(bean.cdCount + 1, PurchaseConstants.BAIZHAN_CD));
			int pay = PurchaseMgr.inst.getNeedYuanBao(
					PurchaseConstants.BAIZHAN_REFRESH_ENEMYS,
					bean.todayRefEnemyTimes + 1);
			resp.setHuanYiPiYB(pay);
			//调用刷新玩家威望函数
			refreshJunZhuWeiWang(jz);
			//获取玩家威望数据
			int[] da = getMainWeiWang(bean, jid, jz.vipLevel);				
			resp.setWeiWangHour(da[0]);
			resp.setHasWeiWang(da[1]);
			
			int[] data = getBuyADDChanceData(jz.vipLevel, bean.buyCount,
					VipData.bugBaizhanTime, PurchaseConstants.BAIZHAN);
			if (data == null || data[0] == 0) {
				// 今日购买回数已经用完，无法再购买
				resp.setLeftCanBuyCount(0);
			}else{
				resp.setLeftCanBuyCount(data[3] - bean.buyCount);
				resp.setBuyNeedYB(data[2]);
				resp.setBuyNumber(data[1]);
			}
			
			resp.setRankAward(bean.rankAward);
			if(bean.lastAwardTime != null && DateUtils.isSameDay(bean.lastAwardTime)){
				resp.setNextTimeTo21(-1); // 今日奖励已经领取过了
			}else{
				int timeDistance = DateUtils.timeDistanceTodayOclock(21, 0);
				resp.setNextTimeTo21(timeDistance / 1000);
			}
			session.write(resp.build());
			log.info("君主{}请求百战主界面信息：剩余次数{}；总次数{}",jid,resp.getLeftTimes(),resp.getTotalTimes());
			return;
		}
/*================================本方法以下内容理论上不会调用======================================================*/
		// 判断玩家是否上次有异常退出百战的情况
		Object ob = isFinishBattle(jz, bean, junRank);
		if (ob != null && ob instanceof ChallengeResp.Builder) {
			ChallengeResp.Builder b = (ChallengeResp.Builder) ob;
			session.write(b.build());
			return;
		}
//		if (ob != null && ob instanceof PlayerStateResp.Builder) {
//			PlayerStateResp.Builder b = (PlayerStateResp.Builder) ob;
//			session.write(b.build());
//			armyCache.remove(jid);
//			return;
//		}
		// 接受请求数据
		BaiZhanInfoResp.Builder resp = BaiZhanInfoResp.newBuilder();
		//resp.setJunZhuId(jid);
		//resp.setJunZhuName(jz.name);
		AllianceBean guild = AllianceMgr.inst.getAllianceByJunZid(jid);
		//resp.setLianMengName(guild == null ? " " : guild.name);
		// 是否显示战斗记录
		//resp.setIsShow(!bean.isLook);
		/*
		 * 能够清除百战的CD最低vip等级
		 */
		VipFuncOpen vipData = VipMgr.vipFuncOpenTemp.get(VipData.clean_baiZhan_time);
		
		//resp.setCanCleanCDvipLev(vipData == null? Integer.MAX_VALUE: vipData.needlv);

		if(vipData != null && jz.vipLevel >= vipData.needlv){
			resp.setCdYuanBao(getYuanBao(bean.cdCount + 1, 
					PurchaseConstants.BAIZHAN_CD));
		}

		// 当前的vip等级，今日可挑战的最大次数（包括购买）
//		resp.setNowMaxBattleCount(getBattleCountForVip(jz.vipLevel));
//		if (jz.vipLevel == VipMgr.maxVip) {
//			resp.setNextMaxBattleCount(resp.getNowMaxBattleCount());
//		} else {
//			resp.setNextMaxBattleCount(getBattleCountForVip(jz.vipLevel + 1));
//		}

		/*
		 * 换一批的元宝数
		 */
		int pay = PurchaseMgr.inst.getNeedYuanBao(
				PurchaseConstants.BAIZHAN_REFRESH_ENEMYS,
				bean.todayRefEnemyTimes + 1);
		resp.setHuanYiPiYB(pay);

		/*
		 * pvp信息
		 */
//		resp.setPvpInfo(fillPVPInfo(jz, bean, junRank));//客户端不再需要此内容，协议相关字段去除 2016-04-21
//		resp.setIsShow(!bean.isLook);

		/**
		 * 对手列表信息
		 */
		
		/*//需求变更，主页面不再提供对手列表信息
		List<Integer> tenRanks = getChallengeRanks(bean, junRank);
		// 根据策划需求，百战第一次，随机一名等级==2的npc
		if(bean == null || bean.allBattleTimes == 0){
			BaiZhanNpc npc = null;
//			int size = baiZhanNpcMap.size();
//			for(int i =1; i<=size; i++){
//				npc = baiZhanNpcMap.get(i);
//				if(npc != null && npc.level == 2){
//					break;
//				}
//			}
			// 策划新需求  20160106
			npc = baiZhanNpcMap.get(1);
			BaiZhanNpc npc2 = null;
			int forthId = -1;
			if(npc != null){
				int minRank = npc.minRank - 1;
				int maxRank = npc.maxRank - 1;
				int realrank = -1;

				do{
					// 名次在[minrank-1, maxRank-1] 之间的玩家
					Set<String> elem = DB.zrangebyscore_(KEY, minRank, maxRank);
					if (elem != null && elem.size() != 0) {
						for (String str : elem) {
							if (str == null) {
								continue;
							}
							String[] sss = str.split("_");
							if (!"npc".equals(sss[0])) {
								continue;
							}
							forthId = Integer.parseInt(sss[1]);
							npc2 = npcs.get(forthId);
							if(npc2 != null && npc2.level == npc.level){
								realrank = getPvpRankById(-forthId);
								break;
							}
						}
					}else{
						//elem 没数据，break while
						break;
					}
					minRank = ++maxRank;
					maxRank = minRank+200; //200个200个的取出
				}while(realrank == -1);
				if(realrank != -1){
					tenRanks.set(0, realrank);// 前端第1个是引导对象
					bean.rank1 = realrank; // 数据库中的修改
					HibernateUtil.save(bean);
					log.info("引导首次百战：forthId is {}, realrank is {} ", forthId, realrank);
				}else{
					log.error("君主：{}引导首次百战出错，没有找到匹配等级低的npc对手", jz.id);
				}
			}else{
				log.error("找不到baiZhanNpcMap中id ==1的配置条目");
			}
		}
		List<OpponentInfo.Builder> enemyBuilderList = fillEnemyListInfo(tenRanks);
		for (OpponentInfo.Builder b : enemyBuilderList) {
			resp.addOppoList(b);
		}
 */
		/*
		 * 威望信息
		 */
		//return new int[]{weiHour, hasWei, getwei};
		int[] da = getMainWeiWang(bean, jid, jz.vipLevel);
		resp.setWeiWangHour(da[0]);
		resp.setHasWeiWang(da[1]);
//		resp.setCanGetweiWang(da[2]);//修改为自动获取，协议相关字段去除 2016-04-21
		/*
		 * 购买百战战斗机会信息
		 */
		int[] data = getBuyADDChanceData(jz.vipLevel, bean.buyCount,
				VipData.bugBaizhanTime, PurchaseConstants.BAIZHAN);
		if (data == null || data[0] == 0) {
			// 今日购买回数已经用完，无法再购买
			resp.setLeftCanBuyCount(0);
		}else{
			resp.setLeftCanBuyCount(data[3] - bean.buyCount);
			resp.setBuyNeedYB(data[2]);
			resp.setBuyNumber(data[1]);
		}
		
		/*
		 *  玩家的排名奖励
		 */
		resp.setRankAward(bean.rankAward);
//		resp.setLastHighestRank(bean.lastHighestRank);//客户端不再需要此内容，协议相关字段去除 2016-04-21
		/*玩家每日奖励信息
		 * */
		if(bean.lastAwardTime != null && DateUtils.isSameDay(bean.lastAwardTime)){
			resp.setNextTimeTo21(-1); // 今日奖励已经领取过了
		}else{
			int timeDistance = DateUtils.timeDistanceTodayOclock(21, 0);
			resp.setNextTimeTo21(timeDistance / 1000);
		}

		/*
		 * 发送
		 */
		session.write(resp.build());
		/*==========================================================================================*/
	}
	
	/**传入君主和请求的军衔，返回该君主在该军衔存储的目标排名数组
	 *   如果存储数据为空，则新创建一个
	 */
	@SuppressWarnings("null")
	public List<Integer> getJunXianEnemylist(long jzid , int junxianId){
		
		List<Integer> res = new ArrayList<Integer>(); 
		if( isSaveJunXianList){
			//尝试数据库获取玩家在该军衔的敌人记录
			String hql = "where jzId = " + jzid + " and junxianId = "+ junxianId;
			JunXianEnemys junxianenemys = HibernateUtil.find(JunXianEnemys.class, hql );
			//如果记录存在，直接返回记录
			if(junxianenemys != null){
				res.add(0, junxianenemys.rank1);
				res.add(1, junxianenemys.rank2);
				res.add(2, junxianenemys.rank3);
				res.add(3, junxianenemys.rank4);
				return res ;
			}else{
				log.info("玩家{}在军衔{}没有存储敌人列表信息，开始创建新数据",jzid,junxianId);
				for(int i = 0 ; i < 4 ; i++){				
					int rank = getRandomRank(junxianId, i+1 );
					res.add(i , rank);
				}
				//新手引导判断，仅在请求小卒军衔的敌人列表时进行新手引导判断
				if(junxianId == PVPConstant.XIAO_ZU_JI_BIE){
					PvpBean bean = HibernateUtil.find(PvpBean.class, jzid);
					if(bean == null ){
						log.error("无法获取玩家{}的百战千军信息",jzid);
						return null; 
					}
					if(bean.allBattleTimes == 0){
						yinDaoChuLi(res);
					}
				}
				//创建敌人列表数据并存储
				junxianenemys = new JunXianEnemys();
				junxianenemys.jzId = jzid;
				junxianenemys.junxianId = junxianId;
				junxianenemys.rank1 = res.get(0);
				junxianenemys.rank2 = res.get(1);
				junxianenemys.rank3 = res.get(2);
				junxianenemys.rank4 = res.get(3);
				
				HibernateUtil.save(junxianenemys);
				log.info("玩家{}在军衔{}创建敌人列表信息成功，存入数据库",jzid,junxianId);
			}
		}else{
			for(int i = 0 ; i < 4 ; i++){				
				int rank = getRandomRank(junxianId, i+1 );
				res.add(i , rank);
			}
			//新手引导判断，仅在请求小卒军衔的敌人列表时进行新手引导判断
			if(junxianId == PVPConstant.XIAO_ZU_JI_BIE){
				PvpBean bean = HibernateUtil.find(PvpBean.class, jzid);
				if(bean == null ){
					log.error("无法获取玩家{}的百战千军信息",jzid);
					return null; 
				}
				//FIXME 如果无君主的PVP信息，需特殊处理
				if(bean.allBattleTimes == 0){
					yinDaoChuLi(res);
				}
			}
		}

		return res;
	}
	
	/**处理新手引导相关问题，在请求敌人列表时调用
	 * 策划需求第一次百战请求列表，列表第一位需要是很弱的NPC
	 * 2016-04-22
	 * */
	public void yinDaoChuLi( List<Integer> list){
		BaiZhanNpc npc = null;
		npc = baiZhanNpcMap.get(1);
		BaiZhanNpc npc2 = null;
		int forthId = -1;
		if(npc != null){
			int minRank = npc.minRank - 1;
			int maxRank = npc.maxRank - 1;
			int realrank = -1;

			do{
				// 名次在[minrank-1, maxRank-1] 之间的玩家
				Set<String> elem = DB.zrangebyscore_(KEY, minRank, maxRank);
				if (elem != null && elem.size() != 0) {
					for (String str : elem) {
						if (str == null) {
							continue;
						}
						String[] sss = str.split("_");
						if (!"npc".equals(sss[0])) {
							continue;
						}
						forthId = Integer.parseInt(sss[1]);
						npc2 = npcs.get(forthId);
						if(npc2 != null && npc2.level == npc.level){
							realrank = getPvpRankById(-forthId);
							if(yinDaoTargetLock.add(realrank)){
								break;
							}
						}
					}
				}else{
					//elem 没数据，break while
					break;
				}
				minRank = ++maxRank;
				maxRank = minRank+200; //200个200个的取出
			}while(realrank == -1);
			if(realrank != -1){
				list.set(0, realrank);// 前端第1个是引导对象								
				//根据引导对象分配后续3个对手
				int rankEnd = (int)DB.zcard_(KEY);
				//由引导对象的排位和最大排位来确定分区间隔
				int fenqujiange = (rankEnd - realrank)/3 ;
				
				//由引导对象开始，在后续分区依次获取排名，加入列表
				list.set(1, MathUtils.getRandomExMax(realrank+1, realrank+fenqujiange));
				realrank += fenqujiange;
				list.set(2, MathUtils.getRandomExMax(realrank+1, realrank+fenqujiange));
				realrank += fenqujiange;
				list.set(3, MathUtils.getRandomExMax(realrank+1, realrank+fenqujiange));
				
				log.info("引导首次百战：forthId is {}, realrank is {} ", forthId, realrank);
			}else{
				log.error("君主引导首次百战出错，没有找到匹配等级低的npc对手");
			}
		}else{
			log.error("找不到baiZhanNpcMap中id ==1的配置条目");
		}
	}
	
	/**根据传入军衔和分区返回军衔对应分区内的一个随机排名*/
	public int getRandomRank(int junxian , int fenqu){
		int res = -1 ;
		int minRandom , maxRandom ;
		BaizhanPipei piPei = piPeiMap.get(junxian);
		BaiZhan baizhan = baiZhanMap.get(junxian);
		if(piPei == null || baizhan ==null){
			log.error("无法生成{}军衔的敌人，百战匹配配置不存在对应军衔",junxian);
			return -1;
		}
		
		int minRank = baizhan.minRank;
		//军衔是小卒时需要特殊处理，获取服务器排行榜最大人数，而不是配置中的最大排名
		int maxRank = junxian == PVPConstant.XIAO_ZU_JI_BIE ? (int)DB.zcard_(KEY) : baizhan.maxRank ;
		int totalRanks = maxRank - minRank + 1 ;
		switch (fenqu) {
		case 1:			
			minRandom = (int) (minRank + (totalRanks)*piPei.param1);
			maxRandom = (int) (minRank + (totalRanks)*piPei.param2);
			res = MathUtils.getRandomExMax(minRandom, maxRandom);
			break;
		case 2:
			minRandom = (int) (minRank + (totalRanks)*piPei.param2);
			maxRandom = (int) (minRank + (totalRanks)*piPei.param3);
			res = MathUtils.getRandomExMax(minRandom, maxRandom);
			break;
		case 3:
			minRandom = (int) (minRank + (totalRanks)*piPei.param3);
			maxRandom = (int) (minRank + (totalRanks)*piPei.param4);
			res = MathUtils.getRandomExMax(minRandom, maxRandom);
			break;
		case 4:
			minRandom = (int) (minRank + (totalRanks)*piPei.param4);
			maxRandom = (int) (minRank + (totalRanks)*piPei.param5);
			res = MathUtils.getRandomExMax(minRandom, maxRandom);
			break;

		default:
			break;
		}
		return res;
	}
	
	/**刷新君主的威望，即自动领取，在每次需要给客户端发送威望信息时调用*/
	public void refreshJunZhuWeiWang(JunZhu jz){
		if(jz == null){
			return ;
		}
		PvpBean bean = HibernateUtil.find(PvpBean.class, jz.id);
		if(bean == null){
			return;
		}
		int rank = getRankById(jz.id);
		if(rank < 0 ){
			return;
		}
		//根据当前时间、上次计算奖励时间、君主排名、君主VIP等级、君主上次领取时间获取威望数据
		int[] addA = getAccAward(new Date(), bean.lastCalculateAward, rank,
				jz.vipLevel, bean.lastGetAward);
		//计算可获取威望
		int allWei = bean.leiJiWeiWang + addA[0];
		
		//更新计算奖励时间，领取奖励时间等信息
		bean.lastCalculateAward = bean.lastGetAward = new Date();
		bean.leiJiWeiWang = 0;
		
		// 新需求：不得超过单次可领取威望最大值
		BaiZhanRank bai = baiZhanRankMap.get(rank);
		allWei = MathUtils.getMin(allWei, bai==null?Integer.MAX_VALUE: bai.weiwangLimit);
		// end
		//判断是否领取
		if(allWei > 0){ 
			bean.getProduceWeiWangTimes += 1;
		}
		//存储威望变更
		int showWeiWang = ShopMgr.inst.addMoney(ShopMgr.Money.weiWang, 
						ShopMgr.baizhan_shop_type, jz.id, allWei);
				
		//存储领取记录
		bean.showWeiWang = showWeiWang ;
		HibernateUtil.save(bean);
		
	}
	
	
	/**响应客户端点击军衔房屋请求对手列表*/
	public void getTiaozhanList(int id, Builder builder, IoSession session){
		//TODO 点击房屋发送对手列表
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null ){
			log.error("百战千军请求对手列表失败：君主信息不存在");
			return;
		}
		ConfirmExecuteReq.Builder req = (ConfirmExecuteReq.Builder)builder ;
		if(req.getType() != 8){
			//type为8时表示请求对手列表
			return;
		}
		int junxianid = req.getJunxianid() ;
		if( baiZhanMap.get(junxianid) == null ){
			log.error("君主{}请求百战对手列表失败，发送军衔信息{}无配置", jz.id, junxianid);
			return ;
		}
		
		List<Integer> fourRanks = getJunXianEnemylist(jz.id , junxianid); 
		if(fourRanks == null || fourRanks.size() == 0){		
			log.error("获取玩家{}在军衔{}敌人排名列表失败",jz.id , junxianid);
			return;
		}
		List<OpponentInfo.Builder> enemyBuilderList = fillEnemyListInfo(fourRanks);
		ConfirmExecuteResp.Builder resp = ConfirmExecuteResp.newBuilder();
		RefreshOtherInfo.Builder res = RefreshOtherInfo.newBuilder();
		res.setJunxianid(junxianid);
		for (OpponentInfo.Builder b : enemyBuilderList) {
			res.addOppoList(b);
		}
		resp.setRefreshOtherInfo(res);
		session.write(resp.build());
	}

	public int[] getMainWeiWang(PvpBean pb, long jzId, int vipLevel) {
		int weiHour = 0;
		int hasWei = 0;
		int getwei = 0;
		
		int myRank = DB.zscore_(KEY, "jun_" + jzId);
		int myJunXian = getJunXianByRank(myRank);
		BaiZhan bai = baiZhanMap.get(myJunXian);
		VIP vTemp = null ;
		PlayerVipInfo vInfo = VipMgr.INSTANCE.getPlayerVipInfo(jzId);
		if( vInfo != null ){
			vTemp = VipMgr.INSTANCE.getVIPByVipLevel(vInfo.level);
		}
		if (bai == null) {
			log.error("BaiZhanRank表中没有找到军衔等级是：{}的奖励配置信息", myRank);
			weiHour = 0;
		} else {
			weiHour = bai.produceSpeed;
			if(vTemp != null ){
				weiHour = (int)Math.floor(weiHour * vTemp.baizhanPara);
			}
		}
		hasWei = ShopMgr.inst.getMoney(ShopMgr.Money.weiWang, jzId, null);
		
		return new int[]{weiHour, hasWei };
	}
	public int getBattleCountForVip(int vipLev) {
		int value = VipMgr.INSTANCE.getValueByVipLevel(vipLev,
				VipData.bugBaizhanTime);
		int times = PVPConstant.ZHAN_TOTAL_TIMES;
		times += PurchaseMgr.inst.getAllUseNumbers(PurchaseConstants.BAIZHAN,
				value);
		return times;
	}
	
	/**根据传入列表，逐一构建对应位置的百战玩家协议
	 * 返回协议列表
	 * */
	public List<OpponentInfo.Builder> fillEnemyListInfo(List<Integer> tenRanks) {
		
		List<OpponentInfo.Builder> enemyBuilderList = new ArrayList<OpponentInfo.Builder>();
		List<Long> jzIdList = new LinkedList<Long>();
		List<Long> tenScores = new  LinkedList<Long>();
		tenRanks.stream().forEach(rank -> tenScores.add(rank -1L));
		List<Object> retList =  DB.zrangeAllbyscore(KEY, tenScores);
		retList.stream().forEach(ret ->{
			String s = null;
			for(String str : (Set<String>)ret){
				if (str != null) {
					s = str;
					break;
				}
			}
			if (s == null) {
				log.error("无法找到敌人排名列表对应的敌人{}",tenRanks);
				return;
			}
			String[] sss = s.split("_");
			long playerId = Long.parseLong(sss[1]);
			if ("npc".equals(sss[0])) {
				jzIdList.add(-playerId);
			} else {
				jzIdList.add(playerId);
			}
		});
		if(jzIdList== null || jzIdList.size() == 0){
			log.error("玩家请求的目标排名没有玩家占据{}",tenRanks);
			return enemyBuilderList;
		}
		String str = jzIdList.stream().map( jz-> String.valueOf(jz)).collect(Collectors.joining(","));
		String hql =  "select JunZhu.id , JunZhu.guoJiaId , JunZhu.name , JunZhu.roleId , JunZhu.level ,pvp_bean.gongJiZuHeId"
				+ " from JunZhu  join pvp_bean  on JunZhu.id = pvp_bean.junZhuId "
				+" where JunZhu.id in ("+str+")";
		List<Object[]> junZhuList = (List<Object[]>) HibernateUtil.querySql(hql);
		Map<Long, Object[]> junZhuMap = junZhuList.stream()
				.collect(Collectors.toMap(arr->((BigInteger)arr[0]).longValue(), 
						arr->{ long jzid = ((BigInteger)arr[0]).longValue();
							arr[0] = jzid;
							return arr ;
						}));
		int cnt = 0 ;
		for(int i = 0 ; i <jzIdList.size() ; i++ ){
			long jzId = jzIdList.get(i);
			int rankTemp = tenRanks.get(i);
			if (jzId < 0 ) {
				int npcId = (int)(-jzId);
				BaiZhanNpc npc = npcs.get(npcId);
				OpponentInfo.Builder you = OpponentInfo.newBuilder();
				// npc发送负数的id
				you.setJunZhuId(jzId);
				you.setJunZhuName(npc.name);
				you.setRank(rankTemp);
				you.setJunXianLevel(getBaiZhanTempByRank(rankTemp).jibie);
				you.setLianMengName("");
				you.setGuojia(npc.getGuoJiaId((int) npcId));
				you.setRoleId(npc.getRoleId((int) npcId));
				int zhanli = npc.power;
				you.setZhanLi(zhanli);
				you.setZuheId(npc.mibaoZuhe);
				you.setLevel(npc.level);
				enemyBuilderList.add(you);
			} else {
				Object[] jzInfo = junZhuMap.get(jzId);
				OpponentInfo.Builder you = OpponentInfo.newBuilder();
				you.setJunZhuId(jzId);
				you.setJunZhuName((String)jzInfo[2]);
				you.setRank(rankTemp);
				you.setJunXianLevel(getBaiZhanTempByRank(rankTemp).jibie);
				IoSession session = SessionManager.inst.getIoSession(jzId);
				if(session != null){
					String LmName = (String)session.getAttribute(SessionAttKey.LM_NAME);
					you.setLianMengName(LmName == null ? "" : LmName);
				}else{
					AllianceBean alliance = AllianceMgr.inst.getAllianceByJunZid(jzId);
					you.setLianMengName(alliance == null ? "" : alliance.name);
				}
				//TODO 获取战力
				String keyStr = SessionAttKey.jzKey+"#"+jzId;
				String zhanLiStr = Redis.instance.hget(keyStr, "zhanLi");
				int zhanli = 0;
				if(zhanLiStr == null ){
					JunZhu junZhu = HibernateUtil.find(JunZhu.class, jzId);
					zhanli = JunZhuMgr.inst.getJunZhuZhanliFinally(junZhu);
					Redis.instance.hset(keyStr, "zhanLi", String.valueOf(zhanli));
				}else{
					zhanli = Integer.parseInt(zhanLiStr);
				}
				
				you.setZhanLi(zhanli);
				you.setGuojia((int)jzInfo[1]);
				you.setRoleId((int)jzInfo[3]);
				you.setZuheId((int)jzInfo[5]);
				you.setLevel((int)jzInfo[4]);
				enemyBuilderList.add(you);
			}
			cnt++;
			if(cnt > 4){
				break;
			}
		}
		log.info("enemyBuilderList .size is：{}",  enemyBuilderList.size());
		return enemyBuilderList;
	}

	/**
	 * 判定玩家上次是否由百战准备界面退出百战
	 * @param jz
	 * @param mybean
	 * @param myRank
	 * @return
	 */
	public Object isFinishBattle(JunZhu jz, PvpBean mybean, int myRank) {
		long jid = jz.id;
		ChallengeResp.Builder bu = armyCache.get(jid);
		if (bu == null) {
			return null;
		}
		// change 20150901
		if(DateUtils.isTimeToReset(new Date(bu.getOpenTime()), CanShu.REFRESHTIME_PURCHASE)){
			// remove战斗准备记录
			armyCache.remove(jid);
			return null;
		}
		// 君主名次发生变化
		if (bu.getMyRank() != myRank) {
			bu.setType(8);
			armyCache.remove(jid);
			return bu;
		}
		// 对手名次发生变化
		long oppoId = bu.getOppoId();
		int yourank = getPvpRankById(oppoId);
		if (yourank != bu.getOppoRank()) {	
			bu.setType(7);
			armyCache.remove(jid);
			return bu;
		}
		int oppoLe = 0;
		int opzhanli = 0;
		int oppoRoleId = 1;
		int oppoZuheId = -1;
//		int oppomibaozuheLevel = 0;//需求变更，不再需要秘宝技能等级 2016-4-21
		if (oppoId < 0) {
			BaiZhanNpc npc = npcs.get((int) -oppoId);
			if (npc != null) {
				oppoLe = npc.level;
				opzhanli = npc.power;
				oppoZuheId = npc.mibaoZuhe;
//				oppomibaozuheLevel = npc.mibaoZuheLv;//需求变更，不再需要秘宝技能等级 2016-4-21
				oppoRoleId = npc.getRoleId((int) -oppoId);
			}
		} else {
			JunZhu oppo = HibernateUtil.find(JunZhu.class, oppoId);
			if (oppo != null) {
				oppoLe = oppo.level;
				opzhanli = getZhanli(oppo);
				PvpBean you = HibernateUtil.find(PvpBean.class, oppoId);
//				oppomibaozuheLevel = MibaoMgr.inst.getActiveZuHeLevel(oppoId, 
//						you==null?-1:you.gongJiZuHeId);//不再需要秘宝技能等级 2016-4-21
				oppoZuheId = you==null?-1:you.gongJiZuHeId;
				oppoRoleId = oppo.roleId;
			}
		}
		// 判断等级是否相等
		if (bu.getMyLevel() == jz.level && bu.getOppoLevel() == oppoLe) {
			bu.setMyZhanli(getZhanli(jz));
			bu.setOppoZhanli(opzhanli);
			bu.setMyZuheId(mybean.gongJiZuHeId);
			bu.setOppZuheId(oppoZuheId);
//			bu.setOppZuHeLevel(oppomibaozuheLevel);//不再需要秘宝技能等级 2016-4-21
			return bu;
		}
		// 等级，战斗等数据改变，重新获取数据进入准备战斗界面
		ChallengeResp.Builder resp = ChallengeResp.newBuilder();
		fillChallengeResp(resp, jz.level, oppoLe, jid);
		resp.setOpenTime(System.currentTimeMillis());
		resp.setMyZhanli(getZhanli(jz));
		resp.setMyZuheId(mybean.gongJiZuHeId);
		resp.setMyRank(myRank);
		resp.setOppoZhanli(opzhanli);
		resp.setOppoId(oppoId);
		resp.setOppoRank(yourank);
		resp.setOppoRoleId(oppoRoleId);
//		resp.setOppZuHeLevel(MibaoMgr.inst.getActiveZuHeLevel(oppoId, oppoZuheId));//不再需要秘宝技能等级 2016-4-21
		resp.setOppZuheId(oppoZuheId);
		// 添加到战斗队形缓存
//		armyCache.put(jid, resp);//需求变更，无需记录玩家队形2016-04-21
		return resp;
	}

	public long getTimeOfLeft(long jId) {
		ChallengeResp.Builder bu = armyCache.get(jId);
		if (bu == null)
			return -1;
		return bu.getOpenTime();
	}

	public void setChallengeRanks(PvpBean bean, List<Integer> ranks) {
		bean.rank1 = ranks.get(0);
		bean.rank2 = ranks.get(1);
		bean.rank3 = ranks.get(2);
		bean.rank4 = ranks.get(3);
		bean.rank5 = ranks.get(4);
		bean.rank6 = ranks.get(5);
		bean.rank7 = ranks.get(6);
		bean.rank8 = ranks.get(7);
		bean.rank9 = ranks.get(8);
		bean.rank10 = ranks.get(9);
	}
	
	public BaiZhan getBaiZhanTempByRank(int rank) {
		for (BaiZhan bai : baiZhanMap.values()) {
			int min = bai.minRank;
			int max = bai.maxRank;
			if (rank >= min && rank <= max) {
				return bai;
			}
		}
		return baiZhanMap.get(PVPConstant.XIAO_ZU_JI_BIE);
	}

	/**
	 * 君主A挑战玩家B：获取准备界面已经随机确定的雇佣兵列表
	 * @param jId
	 * @return
	 */
	public int[] getSoldiers(long jId) {
		long time = getTimeOfLeft(jId);
		if (time == -1) {
			return null;
		}
		int[] armys = bingsCache.get(jId);
		if (armys == null) {
			return null;
		}
		// change 20150901
		if(DateUtils.isTimeToReset(new Date(time), CanShu.REFRESHTIME_PURCHASE)){
			return null;
		}else {
			return armys;
		}
	}

	/**
	 * 根据敌我双方君主等级随机雇佣兵
	 * 先随机职业，再根据职业随机GuYongBing
	 * @param mylevel
	 * @param opplevel
	 * @return
	 */
	public int[] refreshSoldiers(int mylevel, int opplevel) {
		int level = (mylevel + opplevel) / 2;
		int index = ALL;
		int[] zhiYes = new int[index];
		int zhiYe;
		int[] bings = new int[index];
		for (int i = 0; i < index; i++) {
			zhiYe = MathUtils
					.getRandomInMax(yongBingZhiYeMin, yongBingZhiYeMax);
			zhiYes[i] = zhiYe;
		}
		for (GuYongBing bing : guYongBingMap.values()) {
			for (int i = 0; i < index; i++) {
				if (bing.zhiye == zhiYes[i] && bing.needLv == level) {
					bings[i] = bing.id;
				}
			}
		}
		log.info("随机出来得到的雇佣兵是：" + Arrays.toString(bings));
		return bings;
	}

	
	public int[] getBuyADDChanceData(int vipLev, int buyHuiShu,
			int vipSelfType, int purchaseType) {
		int can = 1;
		int allHuiShu = VipMgr.INSTANCE.getValueByVipLevel(vipLev, vipSelfType);
		if (allHuiShu <= buyHuiShu) {
			// 无法购买
			can = 0;
			return new int[]{can, 0, 0, allHuiShu};
		}
		Purchase pc = getPurchase(buyHuiShu + 1, purchaseType);
		if (pc == null) {
			log.error("没有获取到类型是：{}的数据", purchaseType);
			return null;
		}
		// 购买的次数
		int count = (int) pc.number;
		int yuanbao = pc.yuanbao;
		return new int[] { can, count, yuanbao, allHuiShu };
	}

	public Purchase getPurchase(int huishu, int type) {
		Purchase pc = PurchaseMgr.inst.getPurchaseCfg(type, huishu);
		return pc;
	}

	public boolean isBuySuccess(JunZhu jz, int yuanbao, 
			IoSession session, int type, String describe) {
		if (jz.yuanBao < yuanbao)
			return false;
		YuanBaoMgr.inst.diff(jz, -yuanbao, 0, yuanbao, type, describe);
		HibernateUtil.update(jz);
		JunZhuMgr.inst.sendMainInfo(session,jz,false);
		return true;
	}
	

	/**
	 * 计算君主累计生产的生产奖励（威望）
	 * @param now
	 * @param lastCalculate
	 * @param rank
	 * @param vipLevel
	 * @param lastGet
	 * @return
	 */
	public int[] getAccAward(Date now, Date lastCalculate, int rank,
			int vipLevel, Date lastGet) {
		if (lastGet == null)
			return new int[2];
		if (lastCalculate == null) {
			lastCalculate = lastGet;
		}
		double param = VipMgr.INSTANCE.getDoubleValueByVipLevel(vipLevel,
				VipData.baizhanPara);
		param = param == 0 ? 1 : param;
		int minutes = getMinutes(now, lastCalculate, lastGet);
		if (minutes == 0) {
			return new int[2];
		}
		// 名次高于TOTAL_NPC_COUNT的生产奖励，按照TOTAL_NPC_COUNT来
		if (rank > PVPConstant.TOTAL_NPC_COUNT) {
			rank = PVPConstant.TOTAL_NPC_COUNT;
		}
		int junxian = getJunXianByRank(rank);
		//2016-04-27需求变更，每小时获取威望由军衔配置决定
		BaiZhan bai = baiZhanMap.get(junxian);
		if (bai == null) {
			log.error("百战：BaiZhanRank中rank是{}的数据不存在", rank);
			return new int[2];
		} else
			return new int[] {
					(int) Math.floor(minutes * param * bai.produceSpeed / 60d), 0 };
		// (int)Math.floor(minutes *param * bai.money/60d)};
	}

	/**
	 * 参数需满足: now > lastCalculate >= lastget
	 * @param now
	 * @param lastCalculate
	 * @param lastget
	 * @return
	 */
	public int getMinutes(Date now, Date lastCalculate, Date lastget) {
		if(now.getTime() > lastCalculate.getTime()
				&& lastCalculate.getTime() >= lastget.getTime()){
			if (lastCalculate == null || lastget == null)
				return 0;
			long nowL = now.getTime();
			long lastCal = lastCalculate.getTime();
			long lastGe = lastget.getTime();
	
			long getJianGe = (nowL - lastGe) / 60000;
			long jiange = (nowL - lastCal) / 60000;
			long jiange2 = (lastCal - lastGe) / 60000;
			// 判断now-get是否大于7天，是以七天为期限，计算，否则直接(nowL - lastCal)的累计分钟数
			if (getJianGe >= 7 * 24 * 60) {
				int ho = (int) (7 * 24 * 60 - jiange2);
				return ho < 0 ? 0 : ho;
			}
			return (int)jiange;
		}
		return 0;
	}
	
	/**根据传入的君主和敌人，在传入的协议中添加对战界面所需协议内容*/
	public void makechanllenginfo(JunZhu jz , long enemyid  , PvpBean bean , ChallengeResp.Builder resp){
		int youRank = getPvpRankById(enemyid);
		int myRank = getPvpRankById(jz.id);
		int oppoLe = 0;
		int opzhanli = 0;
		int oppoRoleId = 1;
		int oppoZuheId = -1;
//		int oppoZuheLevel = 0;//需求变更，客户端不再请求该数据2016-04-22
		//oppoId<0: 对手是npc
		if (enemyid < 0) {
			BaiZhanNpc npc = npcs.get((int) -enemyid);
			if (npc != null) {
				oppoLe = npc.level;
				opzhanli = npc.power;
				oppoZuheId = npc.mibaoZuhe;
//				oppoZuheLevel = npc.mibaoZuheLv;//需求变更，客户端不再请求该数据2016-04-22
				oppoRoleId = npc.getRoleId((int) -enemyid);
			}
		} else {
			JunZhu oppo = HibernateUtil.find(JunZhu.class, enemyid);
			if (oppo != null) {
				oppoLe = oppo.level;
				opzhanli = getZhanli(oppo);
				PvpBean you = HibernateUtil.find(PvpBean.class, enemyid);
//				oppoZuheLevel = MibaoMgr.inst.getActiveZuHeLevel(enemyid, you.gongJiZuHeId);//需求变更，客户端不再请求该数据2016-04-22
				oppoZuheId = you.gongJiZuHeId;
				oppoRoleId = oppo.roleId;
			}
		}
		fillChallengeResp(resp, jz.level, oppoLe, jz.id);
		resp.setOpenTime(System.currentTimeMillis());
		resp.setMyZhanli(getZhanli(jz));
		resp.setMyZuheId(bean.gongJiZuHeId);
		resp.setMyRank(myRank);
		resp.setOppoZhanli(opzhanli);
		resp.setOppoId(enemyid);
		resp.setOppoRank(youRank);
		resp.setOppoRoleId(oppoRoleId);
//		resp.setOppZuHeLevel(oppoZuheLevel);//客户端不再需要此内容，协议相关字段去除 2016-04-21
		resp.setOppZuheId(oppoZuheId);
		// 添加到战斗队形缓存
		armyCache.put(jz.id, resp); //需求变更：无需记录战斗队形2016-04-21
	}
	
	/**根据敌我君主等级在传入的协议中增加雇佣兵队列*/	
	public void fillChallengeResp(ChallengeResp.Builder resp, int jlev,
			int ylev, long jId) {
		qxmobile.protobuf.PvpProto.GuYongBing.Builder gu = null;
		int[] youBingIds = refreshSoldiers(jlev, ylev);
		int[] myBingIds = refreshSoldiers(jlev, ylev);
		for (int i = 0; i < youBingIds.length; i++) {
			gu = qxmobile.protobuf.PvpProto.GuYongBing.newBuilder();
			gu.setId(youBingIds[i]);
			resp.addOppoSoldiers(gu);
		}

		for (int i = 0; i < myBingIds.length; i++) {
			gu = qxmobile.protobuf.PvpProto.GuYongBing.newBuilder();
			gu.setId(myBingIds[i]);
			resp.addMySoldiers(gu);
		}
		resp.setMyLevel(jlev);
		resp.setOppoLevel(ylev);
		// 佣兵加到map中
		int[] bings = new int[ALL + ALL];
		System.arraycopy(myBingIds, 0, bings, 0, ALL);
		System.arraycopy(youBingIds, 0, bings, ALL, ALL);
		log.info("记录到佣兵缓存中的数据是： {}", Arrays.toString(bings));
		bingsCache.put(jId, bings);
	}

	public int getZhanli(JunZhu jz) {
		if (jz != null) {
			return JunZhuMgr.inst.getJunZhuZhanliFinally(jz);
		}
		return 0;
	}

	/**
	 * 攻击秘宝技能和防守秘宝技能都调用bean.gongJiZuHeId
	 * @param jzId
	 * @param mibaoIds
	 * @param zuheId
	 */
	public void saveGongJiMiBao(long jzId, List<Long> mibaoIds, int zuheId) {
		PvpBean bean = HibernateUtil.find(PvpBean.class, jzId);
		if (bean == null) {
			log.error("玩家{}的百战没有开启:", jzId);
			return;
		}
		bean.gongJiZuHeId = zuheId;
		HibernateUtil.save(bean);
		log.info("玩家:{}百战更换攻击组合id{}成功", jzId, zuheId);
	}

	public void saveFangShouMiBao(long jzId, List<Long> mibaoIds, int zuheId) {
//		PvpBean bean = HibernateUtil.find(PvpBean.class, jzId);
//		if (bean == null) {
//			log.error("玩家{}的百战没有开启:", jzId);
//			return;
//		}
////		bean.zuheId = zuheId;
//		HibernateUtil.save(bean);
	}

	/**
	 * 对一些花费元宝或者威望来做某事的请求作出相应处理，并返回给客户端是否做成功的结果
	 * 
	 * @Title: doConfirmExecute
	 * @Description:
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void doConfirmExecute(int id, Builder builder, IoSession session) {
		ConfirmExecuteReq.Builder req = (ConfirmExecuteReq.Builder) builder;
		int type = req.getType();
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("百战千军出错：君主不存在");
			return;
		}
		PvpBean bean = HibernateUtil.find(PvpBean.class, jz.id);
		if (bean == null) {
			log.error("玩家{}购买百战次数出错：百战没有开启", jz.id);
			return;
		} else {
			resetPvpBean(bean);
		}
		int myRank = DB.zscore_(KEY, "jun_" + jz.id);
		switch (type) {
		case PVPConstant.BUY_CHALLENGE_COUNT:
			// 确定 购买挑战次数
			buyChallengeCount(jz, session, bean);
			break;
		case PVPConstant.CLEAN_CHALLENGE_CD:
			// 购买 清除挑战CD
			cleanChallengeTime(jz, session);
			break;
		case PVPConstant.GET_PRODUCE_AWARD:
			// 领取生产奖励（威望）
			getProduceAward(jz, session, bean, myRank);
			break;
		case PVPConstant.REFRESH_RANKS:
			// 花元宝 【换一批】
			freeRefreshEnemy(id, builder, session);
			break;
/*=====================需求变更，客户端不会再次发送以下两个参数==========================*/
//		case PVPConstant.REFRESH_RANKS_FREE:
//			refreshEnemysFree(bean, session, myRank);// 君主名次变了，免费刷新整个对手名次列表
//			break;
//		case PVPConstant.reget_ranks:
//			regetEnemys(bean, session, myRank);// 要挑战的对手的名次发生改变，重新获取该名次的玩家
//			break;
/*=========================================================================================*/
		case PVPConstant.get_rank_award: // 领取玩家的排名奖励
			getRankAward(jz, bean, session);
			break;
		case PVPConstant.get_daily_award: //领取玩家百战每日奖励
			getDailyAward(jz, bean, session);
		case PVPConstant.get_junxian_enemys://请求某一个军衔对应的敌人列表
			getTiaozhanList(id, builder, session);
			break;
		}
	}

	public void getDailyAward(JunZhu jz, PvpBean bean, IoSession session){
		int timeDistance = DateUtils.timeDistanceTodayOclock(21, 0);
		if(timeDistance > 0){
			log.error("当日的奖励还不能领取");
			return;
		} //否则就在 21:00 ~ 24:00之间
		// 
		if(bean.lastAwardTime != null && DateUtils.isSameDay(bean.lastAwardTime)){
			log.error("今天的奖励已经领取了");
			return;
		}
		// 领奖
		int rrr = getPvpRankById(jz.id);
		BaiZhan bai = getBaiZhanTempByRank(rrr);
		if (bai == null) {
			log.error("BaiZhan表中没有找到军衔等级是：{}的奖励配置信息", bean.junXianLevel);
			return;
		}
		ConfirmExecuteResp.Builder resp = ConfirmExecuteResp.newBuilder();
		GetAwardInfo.Builder info = GetAwardInfo.newBuilder();
//		StringBuilder dayAward = AwardMgr.inst.reStructAwardStr(bai.dayAward);
		boolean ok = AwardMgr.inst.giveReward(session, bai.dayAward, jz);
		if(ok){
			log.info("ok");
			bean.lastAwardTime = new Date();
			HibernateUtil.save(bean);
			log.error("君主:{}手动领取百战每日奖励：{}成功", jz.id, bai.dayAward);
			info.setSuccess(1);
		}else{
			log.error("君主:{}手动领取百战每日奖励失败", jz.id);
			info.setSuccess(0);
		}
		resp.setGetAwardInfo(info);
		session.write(resp.build());
		
	}
	public void getRankAward(JunZhu jz, PvpBean bean, IoSession session){
		int num = bean.rankAward;
		AwardTemp award = new AwardTemp();
		award.awardId = 111;
		award.itemId = AwardMgr.item_yuan_bao;
		award.itemNum = num;
		award.itemType = 0;
		
		ConfirmExecuteResp.Builder resp = ConfirmExecuteResp.newBuilder();
		GetAwardInfo.Builder info = GetAwardInfo.newBuilder();

		boolean ok = AwardMgr.inst.giveReward(session, award, jz, true);
		if(ok){
			bean.rankAward = 0;
			HibernateUtil.save(bean);
			log.info("玩家:{}领取百战名次奖励元宝{}个,成功", jz.id,num);
			info.setSuccess(1);
		}else{
			log.info("玩家:{}领取百战名次奖励(元宝)失败", jz.id);
			info.setSuccess(0);
		}
		resp.setGetAwardInfo(info);
		session.write(resp.build());
	}
	public void payForRefreshEnemy(int id, Builder builder, IoSession session){
		ConfirmExecuteReq.Builder req = (ConfirmExecuteReq.Builder)builder ;
		int junXianId = req.getJunxianid();
		if( piPeiMap.get(junXianId) == null ){
			log.error("百战请求换一批错误，协议中无法获取请求军衔id{}" , junXianId );
			return ;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			log.error("百战请求换一批，君主信息错误");
			return ;
		}
		int rank = getRankById(jz.id);
		PvpBean bean = HibernateUtil.find(PvpBean.class, jz.id);
		if(bean == null){
			log.error("君主：{}获取百战数据失败，重新创建pvpbean" ,jz.id);
			bean = initJunZhuPVPInfo(jz.id, rank);
		}
		int pay = PurchaseMgr.inst.getNeedYuanBao(
				PurchaseConstants.BAIZHAN_REFRESH_ENEMYS,
				bean.todayRefEnemyTimes + 1);
		boolean ok = false;
		if(pay < 0){
			log.error("尝试获取百战换一批消耗元宝错误：需求为负值");
			return;
		}else if( pay >0 ){
			ok = isBuySuccess(jz, pay, session, YBType.YB_BUY_PVP_REFRESH, "PVP购买换一批");
		}else if(pay == 0){
			ok = true;
		}
		ConfirmExecuteResp.Builder resp = ConfirmExecuteResp.newBuilder();
		PayChangeInfo.Builder info = PayChangeInfo.newBuilder();	
		
		if(ok){				
			info.setSuccess(1);
			log.info("玩家id{},姓名 {}, 购买刷新对手列表们成功，花费元宝{}个", jz.id, jz.name, pay);
			bean.todayRefEnemyTimes += 1;
			HibernateUtil.save(bean);
			//FIXME 换一批操作：删除数据库原有数据，然后调用请求列表函数重新生成
			//可能出现数据没有成功删除时，就进入下一步操作，导致扣除玩家元宝，但是没有更换对手列表
			String hql = "where jzId = " + jz.id + " and junxianId = "+ junXianId;			
			JunXianEnemys junxianenemys = HibernateUtil.find(JunXianEnemys.class, hql );
			if(junxianenemys != null){
				HibernateUtil.delete(junxianenemys);
			}
			List<Integer> fourRanks = getJunXianEnemylist(jz.id , junXianId); 			
			if(fourRanks == null || fourRanks.size() == 0){
				log.error("生成玩家{}在军衔{}敌人排名列表失败",jz.id , junXianId);
			}
			List<OpponentInfo.Builder> enemyBuilderList = fillEnemyListInfo(fourRanks);
			for (OpponentInfo.Builder b : enemyBuilderList) {
				info.addOppoList(b);
			}
			pay = PurchaseMgr.inst.getNeedYuanBao(
					PurchaseConstants.BAIZHAN_REFRESH_ENEMYS,
					bean.todayRefEnemyTimes + 1);
			info.setNextHuanYiPiYB(pay);
		}else {
			// 0是元宝不足
			info.setSuccess(0);
			log.info("玩家id{},姓名 {}, 购买刷新对手列表失败，元宝不足", jz.id, jz.name);
		}
		resp.setPayChangeInfo(info);
		session.write(resp.build());
	}
	
	public void freeRefreshEnemy(int id, Builder builder, IoSession session){
		ConfirmExecuteReq.Builder req = (ConfirmExecuteReq.Builder)builder ;
		int junXianId = req.getJunxianid();
		if( piPeiMap.get(junXianId) == null ){
			log.error("百战请求换一批错误，协议中无法获取请求军衔id{}" , junXianId );
			return ;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if(jz == null){
			log.error("百战请求换一批，君主信息错误");
			return ;
		}
		ConfirmExecuteResp.Builder resp = ConfirmExecuteResp.newBuilder();
		PayChangeInfo.Builder info = PayChangeInfo.newBuilder();	
		
		List<Integer> fourRanks = getJunXianEnemylist(jz.id , junXianId); 			
		if(fourRanks == null || fourRanks.size() == 0){
			log.error("生成玩家{}在军衔{}敌人排名列表失败",jz.id , junXianId);
		}
		List<OpponentInfo.Builder> enemyBuilderList = fillEnemyListInfo(fourRanks);
		for (OpponentInfo.Builder b : enemyBuilderList) {
			info.addOppoList(b);
		}
		info.setSuccess(1);
		info.setNextHuanYiPiYB(0);
		resp.setPayChangeInfo(info);
		session.write(resp.build());
	}
	
	public void buyChallengeCount(JunZhu jz, IoSession session, PvpBean bean) {
		ConfirmExecuteResp.Builder resp = ConfirmExecuteResp.newBuilder();
		/* int[]{can,count, yuanbao,allHuishu}; */
		int[] info = getBuyADDChanceData(jz.vipLevel, bean.buyCount, 
				VipData.bugBaizhanTime, PurchaseConstants.BAIZHAN);
		BuyCiShuInfo.Builder respInfo = BuyCiShuInfo.newBuilder();
		if (info != null) {
			if (info[0] == 0) {
				// 今日次数已经用完
				respInfo.setSuccess(3);
				resp.setBuyCiShuInfo(respInfo);
				session.write(resp.build());
				return;
			}
			int yuanbao = info[2];
			int count = info[1];
			boolean ok = isBuySuccess(jz, yuanbao, session, YBType.YB_BUY_PVP, "PVP购买挑战次数");
			if (ok) {
				bean.remain += count;
				bean.buyCount += 1;
				HibernateUtil.save(bean);
				respInfo.setSuccess(1);
				respInfo.setLeftTimes(bean.remain);
				respInfo.setTotalTimes(bean.usedTimes + bean.remain);
				respInfo.setCdTime(getCountDown(bean));
				log.info("玩家id{},姓名 {}, 购买挑战百战的次数成功花费元宝{}个", jz.id, jz.name,
						yuanbao);
			} else {
				// 0是元宝不足
				respInfo.setSuccess(0);
			}
		} else {
			// 数据不对
			respInfo.setSuccess(2);
		}
		resp.setBuyCiShuInfo(respInfo);
		session.write(resp.build());
	}

	/**
	 * 清除冷却时间
	 * 
	 * @Title: cleanChallengeTime
	 * @Description:
	 * @param jz
	 * @param session
	 */
	public void cleanChallengeTime(JunZhu jz, IoSession session) {
		PvpBean bean = HibernateUtil.find(PvpBean.class, jz.id);
		if (bean == null) {
			log.error("玩家{}清除挑战冷却时间：百战没有开启", jz.id);
			return;
		}
		ConfirmExecuteResp.Builder resp = ConfirmExecuteResp.newBuilder();
		CleanCDInfo.Builder info = CleanCDInfo.newBuilder();
		boolean isPermit = VipMgr.INSTANCE.isVipPermit(
				VipData.clean_baiZhan_time, jz.vipLevel);
		if (!isPermit) {
			// vip等级不足
			info.setSuccess(3);
			log.info("玩家id{},姓名 {}, 购买清除挑战冷却时间失败：vip等级{}不足，没有权利消除", jz.id,
					jz.name, jz.vipLevel);
			resp.setCleanCDInfo(info);
			session.write(resp.build());
			return;
		}
		if(bean.remain <=0){
			return;
		}
		int cdYuan = getYuanBao(bean.cdCount + 1, PurchaseConstants.BAIZHAN_CD);
		boolean yes = isBuySuccess(jz, cdYuan, session, YBType.YB_BUY_PVP_CLEAR_CD, "PVP购买清除CD");
		if (yes) {
			bean.cdCount += 1;
			bean.lastDate = new Date(System.currentTimeMillis()
					- PVPConstant.INTERVAL_ZHAN_SECOND * 1000);
			HibernateUtil.save(bean);
			log.info("玩家id{},姓名 {}, 购买清除挑战冷却时间成功花费元宝{}个", jz.id, jz.name,
					cdYuan);
			info.setLeftTimes(bean.remain);
			info.setTotalTimes(bean.usedTimes + bean.remain);
			info.setSuccess(1);
			info.setNextCDYB(getYuanBao(bean.cdCount + 1,  PurchaseConstants.BAIZHAN_CD));
		} else {
			/* 0是元宝不足 */
			info.setSuccess(0);
			log.info("玩家id{},姓名 {}, 购买清除挑战冷却时间失败：元宝不足", jz.id, jz.name);
		}
		resp.setCleanCDInfo(info);
		session.write(resp.build());
	}

	/**获取君主已生产威望*/
	public void getProduceAward(JunZhu jz, IoSession session, PvpBean bean,
			int rank) {
		ConfirmExecuteResp.Builder resp = ConfirmExecuteResp.newBuilder();
		int[] addA = getAccAward(new Date(), bean.lastCalculateAward, rank,
				jz.vipLevel, bean.lastGetAward);
		int allWei = bean.leiJiWeiWang + addA[0];
		bean.lastCalculateAward = bean.lastGetAward = new Date();
		bean.leiJiWeiWang = 0;
		// 新需求：不得超过最大值
		BaiZhanRank bai = baiZhanRankMap.get(rank);
		allWei = MathUtils.getMin(allWei, bai==null?Integer.MAX_VALUE: bai.weiwangLimit);
		// end
		if(allWei > 0){ 
			bean.getProduceWeiWangTimes += 1;
		}
		HibernateUtil.save(bean);
		ShopMgr.inst.addMoney(ShopMgr.Money.weiWang, 
				ShopMgr.baizhan_shop_type, jz.id, allWei);
		ShopMgr.inst.sendMainIfo(session, jz.id, ShopMgr.Money.weiWang);
		GetAwardInfo.Builder info = GetAwardInfo.newBuilder();
		info.setSuccess(1);
		resp.setGetAwardInfo(info);
		session.write(resp.build());
		log.info("玩家id{},姓名 {}, 百战获取生产奖励, 威望:{}", jz.id, jz.name, allWei);
		// 主线任务: 领取一次威望 20190916
		if(allWei > 0){
			EventMgr.addEvent(jz.id,ED.get_produce_weiWang , new Object[] { jz.id});
		}
	}

	// 战斗结束进行战斗数据的更新
	public void dealBaiZhanResult(int id, Builder builder, IoSession session) {
//		log.error("时间是:" + time);
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("百战千军出错：君主不存在");
			return;
		}
		BaiZhanResult.Builder req = (BaiZhanResult.Builder) builder;
		long otherId = req.getEnemyId();
		int zhandouId = req.getZhandouId();
		long jId = jz.id;
		long winId = req.getWinId();
		// 战斗队形缓存remove该玩家数据
		armyCache.remove(jId);//需求变更，无需记录玩家队形2016-04-21
		bingsCache.remove(jId);
		BattleInfo4Pvp battleInfo4Pvp = goingMap.remove(otherId);
		// 要是超过最大百战允许时间，说明挑战失败了
		if(battleInfo4Pvp == null || 
				(System.currentTimeMillis() - battleInfo4Pvp.startTime) / 1000 > CanShu.MAXTIME_BAIZHAN + PVP_BATTLE_DELAY_TIME) {
			winId = otherId;
		}
		log.info("挑战者:{}和被挑战者：{}战斗结束，移出战斗map", jId, otherId);
		PvpBean bean = HibernateUtil.find(PvpBean.class, jId);
		if (bean == null) {
			log.error("百战战斗结束相关处理出错：百战无记录", jId);
			return;
		}
		int rank = getPvpRankById(jId);
		int jibie = getBaiZhanTempByRank(rank).jibie;
		ActLog.log.Challenge(jz.id, jz.name, "OpposName", otherId, winId==jz.id?1:2, rank, rank);
		
		/*
		 * 1.1版本要求首次百战必须胜利： 数据记录已经在进入战斗时处理
		 */
		if(bean.allBattleTimes == 1 && otherId < 0){
			ZhanDouRecord zhanR = ZhanDouRecordDao.find(zhandouId);
			BaiZhanResultResp.Builder resp = BaiZhanResultResp.newBuilder();
			yinDaoTargetLock.remove(rank);
			resp.setOldRank(zhanR.junRankChangeV + rank);
			resp.setNewRank(rank);
			resp.setHighest(bean.lastHighestRank);
			resp.setOldJunXianLevel(getBaiZhanTempByRank(zhanR.junRankChangeV + rank).jibie);
			resp.setNewJunXianLevel(jibie);
			resp.setYuanbao(bean.rankAward);
			session.write(resp.build());
			int npcId = (int) -otherId;
			BaiZhanNpc baiZhanNpc = npcs.get(npcId);
			String npcName = baiZhanNpc.name;
			npcName = HeroService.getNameById(npcName);
			EventMgr.addEvent(jz.id, ED.BAI_ZHAN_RANK_UP, new Object[]{jz.name, npcName,rank});
			EventMgr.addEvent(jz.id, ED.baizhan_rank_n, new Object[] { jz.id,rank });
			// 问候
			EventMgr.addEvent(jz.id, ED.first_baiZhan_success, new Object[] { jz.id, bean.allWin });
			return;
		}
		/*
		 * 挑战失败，没有损失也没有获取
		 */
		if (winId == otherId) {
			BaiZhanResultResp.Builder resp = BaiZhanResultResp.newBuilder();
			resp.setOldRank(rank);
			resp.setNewRank(rank);
			resp.setHighest(bean.highestRank);
			resp.setOldJunXianLevel(jibie);
			resp.setNewJunXianLevel(jibie);
			resp.setYuanbao(0);
			session.write(resp.build());
			return;
		}
		// 是否重新设置数据
		resetPvpBean(bean);

		int oldJunXianLevel = jibie;
		/*
		 * 修改双方名次
		 * 
		 * 君主胜利，并不代表君主的名次会改变！！，因为规则要求君主可以挑战比自己名次低的人，赢了，名次不变化
		 * 所以下面代码中 if{} 才表示胜利并且名次升高。
		 */
		int[] ranks = dealBattleEndRank(jId, otherId);
		int lostBuild = 0;
		int junRankChangeV = ranks[0] - ranks[2];
		
		JunZhu otherJun = null;
		String npcName = "?";
		boolean isJunChange = false;
		boolean isEnemyChange = false;
		if (otherId < 0) {
			int npcId = (int) -otherId;
			BaiZhanNpc baiZhanNpc = npcs.get(npcId);
			npcName = baiZhanNpc.name;
			npcName = HeroService.getNameById(npcName);
			/*
			 * if 代码块：名次发生变化 处理
			 *  君主 的名次升高(比如 90 ——> 50)，对手(npc)的名次下降(比如 50  ——> 90)
			 */
			if (ranks[0] != ranks[2]) {
				// 更换名次之前计算累计的生产奖励
				calculateProduceJiangLi(jz, ranks[0], jz.vipLevel, bean);
				resetBeanAfterWin(jId, bean, bean.lastWeiWang,
						ranks[2]);
				isJunChange = true;
			}
		} else {
			otherJun = HibernateUtil.find(JunZhu.class, otherId);
			if(otherJun!=null){
				npcName = otherJun.name;
			}
			PvpBean otherbean = HibernateUtil.find(PvpBean.class, otherId);
			/*
			 * if 代码块：名次发生变化 处理
			 *  君主 的名次升高(比如 90 ——> 50)，对手(玩家)的名次下降(比如 50  ——> 90)
			 */
			if (ranks[0] != ranks[2] && otherbean != null) {
				// 更换名次之前计算累计的生产奖励
				calculateProduceJiangLi(jz, ranks[0], jz.vipLevel, bean);
				// 更换名次之前计算累计的生产奖励
				calculateProduceJiangLi(otherJun, ranks[1], otherJun == null ? 0
						: otherJun.vipLevel, otherbean);
				// reset敌人信息
				resetBeanAfterWin(otherId, otherbean, -bean.lastWeiWang,
						ranks[3]);
				resetBeanAfterWin(jId, bean, bean.lastWeiWang,
						ranks[2]);
				isJunChange = true;
				isEnemyChange = true;
				// 对手扣除建设值  策划新需求：不扣建设值： 20150725
//				lostBuild = receiveJianShe(otherId);
//				reduceJianShe(jId, otherId, lostBuild);
			}
			if (otherbean != null) {
				// 进入战场之前的处理回退
				otherbean.winToday -= 1;
				otherbean.allWin -= 1;
				otherbean.isLook = false;
				HibernateUtil.save(otherbean);
				IoSession enemySession = SessionManager.inst.getIoSession(otherJun.id);
				if(enemySession != null) {
					FunctionID.pushCanShowRed(otherJun.id, enemySession, FunctionID.baiZhanRecord);
				}
			}
		}

		// 君主信息
		int oldHighestRank = bean.highestRank;
		if (ranks[2] < oldHighestRank) {
			bean.lastHighestRank = oldHighestRank;
			bean.highestRank = ranks[2];
			log.info("玩家：{}，挑战对手战斗胜利之后，最高名次变为:{}", jId, bean.highestRank);
			// 发送历史最高名次变化情况邮件
			// 20160114  策划改为不从邮件发送，而是手动领取奖励,所以只做记录
			sendEmailOfHighestRank(jz, bean, oldHighestRank);
		}
		bean.allWin += 1;
		bean.winToday += 1;
		HibernateUtil.save(bean);
	
		// 记录一条战斗数据
		ZhanDouRecord zhanR = ZhanDouRecordDao.find(zhandouId);
		if (zhanR == null) {
			zhanR = new ZhanDouRecord(zhandouId, jId, otherId, new Date(),
					PVPConstant.GONG_JI_WIN, junRankChangeV, -junRankChangeV,
					bean.lastWeiWang, lostBuild);
			ZhanDouRecordDao.insert(zhanR);
		} else {
			zhanR.result1 = PVPConstant.GONG_JI_WIN;
			zhanR.junRankChangeV = junRankChangeV;
			zhanR.enemyRankChangeV = -junRankChangeV;
			zhanR.getWeiWang = bean.lastWeiWang;
			zhanR.lostBuild = lostBuild;
			HibernateUtil.save(zhanR);
		}
		log.info("数据库保存一场战斗：{}", zhandouId);
		
		/*
		 * 返回战斗之后等级变化情况
		 */
		BaiZhanResultResp.Builder resp = BaiZhanResultResp.newBuilder();
		resp.setOldRank(ranks[0]);
		resp.setNewRank(ranks[2]);
		resp.setHighest(oldHighestRank); //注意返回的是 oldHighestRank！！！
		resp.setOldJunXianLevel(oldJunXianLevel);
		BaiZhan baiZhanNew = baiZhanMap.get(bean.junXianLevel);
		int newJiBie = baiZhanNew == null? 1 : baiZhanNew.jibie;
		resp.setNewJunXianLevel(newJiBie);
		resp.setYuanbao(bean.rankAward);
		session.write(resp.build());
		
		/*
		 *  君主 名次变化  (且君主名次只有一种变化：升高)
		 */
		EventMgr.addEvent(jId, ED.SUCCESS_BAIZHAN_N, new Object[] { jId, bean.allWin });
		if(isJunChange){
			// 2015-7-22 刷新百战榜
			EventMgr.addEvent(jId, ED.BAIZHAN_RANK_REFRESH, new Object[]{jz, jz.guoJiaId});
			EventMgr.addEvent(jId, ED.BAI_ZHAN_RANK_UP, new Object[]{jz.name, npcName, ranks[2]});
			EventMgr.addEvent(jId, ED.baizhan_rank_n, new Object[] { jId, ranks[2] });
			
		}
		/*
		 *  对手名次变化(且对手名次只有一种变化：降低)
		 */
		if(isEnemyChange && otherJun != null){
			// 2015-8-26 刷新对手百战榜
			EventMgr.addEvent(otherJun.id,ED.BAIZHAN_RANK_REFRESH, new Object[]{otherJun, otherJun.guoJiaId});
			EventMgr.addEvent(otherJun.id,ED.BAI_ZHAN_A_WIN_B,new Object[]{jz,otherJun});
		}
	
		// // 主线任务： 完成N次百战（无论输赢）暂时没有开启20150519
		// EventMgr.addEvent(ED.FINISH_BAIZHAN_N, new Object[]{jId,
		// bean.allWin});
		// 主线任务： 百战达到名次N
		// if(ranks[2] == TaskData.bai_n_1 ||
		// ranks[2] == TaskData.bai_n_2 ||
		// ranks[2] == TaskData.bai_n_3){
//		EventMgr.addEvent(ED.baizhan_rank_n, new Object[] { jId, ranks[2] });
		// }
		/*
		 *  去掉国家限定：20140727
		 */
//		DailyTaskBean task = DailyTaskMgr.INSTANCE.getTaskByTaskId(jId,
//				DailyTaskConstants.baizhan_win_id);
//		if (enemyGuoJiaId == task.duiShouGuoJia1
//				|| enemyGuoJiaId == task.duiShouGuoJia2) {
		// 每日任务中记录百战胜利一次
		// 策划需求修改为： 扣次数即为完成任务 20150831
//		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
//				jId, DailyTaskConstants.baizhan_win_id, 1));
//		}
//		log.info("方法的执行时间是: " + (System.currentTimeMillis() - time));
	}

	public void dealDataAfterEnterBattle(int zhandouId, JunZhu jz,
			PvpBean jzPvp, long enemyId) {
		/*
		 * 是否重设数据
		 */
		resetPvpBean(jzPvp);
		/*
		 * 扣除玩家的战斗次数等
		 */
		jzPvp.usedTimes += 1;
		jzPvp.remain -= 1;
		jzPvp.lastDate = new Date();
		// allBattleTimes只是主动竞技的挑战次数，被打不增加
		jzPvp.allBattleTimes += 1;
		/*
		 * 走到引导打第二百战， 为顺利通过，不设置CD
		 */
		if(jzPvp.allBattleTimes == 1){
			jzPvp.lastDate = null;
		}
		/*
		 * 特殊处理： 第一次战斗默认是胜利
		 */
		int junRankChangeV = 0;
		if(jzPvp.allBattleTimes == 1 && enemyId <= 0){
			int[] ranks = dealBattleEndRank(jz.id, enemyId);
			junRankChangeV = ranks[0] - ranks[2];
			// 更换名次之前计算累计的生产奖励
			if(junRankChangeV > 0){
				calculateProduceJiangLi(jz, ranks[0], jz.vipLevel, jzPvp);
				resetBeanAfterWin(jz.id, jzPvp, jzPvp.lastWeiWang,
					ranks[2]);
				EventMgr.addEvent(jz.id,ED.BAIZHAN_RANK_REFRESH, new Object[]{jz, jz.guoJiaId});
//				int npcId = (int) -enemyId;
//				BaiZhanNpc baiZhanNpc = npcs.get(npcId);
//				String npcName = baiZhanNpc.name;
//				npcName = HeroService.getNameById(npcName);
//				EventMgr.addEvent(ED.BAI_ZHAN_RANK_UP, new Object[]{jz.name, npcName, ranks[2]});
//				EventMgr.addEvent(ED.baizhan_rank_n, new Object[] { jz.id, ranks[2] });
			}
			int oldHighestRank = jzPvp.highestRank;
			if (ranks[2] < oldHighestRank) {
				jzPvp.lastHighestRank = oldHighestRank;
				jzPvp.highestRank = ranks[2];
				sendEmailOfHighestRank(jz, jzPvp, oldHighestRank);
			}
			jzPvp.allWin += 1;
			jzPvp.winToday += 1;
			EventMgr.addEvent(jz.id,ED.SUCCESS_BAIZHAN_N, new Object[] { jz.id, jzPvp.allWin });
		}
		// 主动攻击不进行战斗记录提示
	//	jzPvp.isLook = false;
		HibernateUtil.save(jzPvp);
		// 每日任务中记录百战胜利一次
		// 策划需求修改为： 扣次数即为完成任务 20150831
		EventMgr.addEvent(jz.id,ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
				jz.id, DailyTaskConstants.baizhan_win_id, 1));
		BattleInfo4Pvp battleInfo4Pvp = new BattleInfo4Pvp(jz.id, System.currentTimeMillis());
		goingMap.put(enemyId, battleInfo4Pvp);

		/*
		 * A挑战B，只有当B输且A和B是不同的联盟的时候，B才会扣掉联盟建设值，A不会增加。其他任何情况，和建设值都没有关系
		 */
		/*
		 * 当A挑战B， A输掉了，A什么也不得到什么也不损失，B也是 默认君主输
		 */
		long jId = jz.id;
		ZhanDouRecord zhanR = new ZhanDouRecord(zhandouId, jId, enemyId,
				new Date(), PVPConstant.GONG_JI_LOSE, 0, 0, 0, 0);
		// 首次战斗默认胜利
		if(jzPvp.allBattleTimes == 1 && enemyId <= 0){
			zhanR.result1 = PVPConstant.GONG_JI_WIN;
			zhanR.junRankChangeV = junRankChangeV;
			zhanR.enemyRankChangeV = -junRankChangeV;
			zhanR.getWeiWang = jzPvp.lastWeiWang;
			zhanR.lostBuild = 0;
		}
		ZhanDouRecordDao.insert(zhanR);
		log.info("数据库保存一场战斗：{}", zhandouId);
		log.info("玩家：{},挑战对手：{}，记录进入战斗(默认记录是输场)", jId, enemyId);
		bingsCache.remove(jId);
		armyCache.remove(jId);//需求变更，无需记录玩家队形2016-04-21
		if (enemyId > 0) {
			PvpBean eb = HibernateUtil.find(PvpBean.class, enemyId);
			if (eb != null) {
				eb.isLook = false;
				/*
				 * 君主非首次，挑战对手默认对方赢
				 */
				if(jzPvp.allBattleTimes == 1 && enemyId <= 0){
					
				}else{
					eb.allWin += 1;
					eb.winToday += 1;
				}
				HibernateUtil.save(eb);
			}
		}
		// 主线任务： 完成N次百战（无论输赢）
		EventMgr.addEvent( jId,ED.FINISH_BAIZHAN_N, new Object[] { jId,
				jzPvp.allBattleTimes });
		
	}

	public void reduceJianShe(long winnerId, long loserId, int lost) {
		AllianceBean a1 = AllianceMgr.inst.getAllianceByJunZid(winnerId);
		AllianceBean a2 = AllianceMgr.inst.getAllianceByJunZid(loserId);
		if (a2 == null) {
			return;
		}
		// 相同联盟不扣除建设值
		if (a1 != null && a2.id == a1.id) {
			return;
		}
		AllianceMgr.inst.changeAlianceBuild(a2, -lost);
	}

	public void calculateProduceJiangLi(JunZhu jz, int rank, int vipLevel, PvpBean bean) {
		int[] values = getAccAward(new Date(), bean.lastCalculateAward, rank,
				vipLevel, bean.lastGetAward);
		bean.leiJiWeiWang += values[0];
		bean.lastCalculateAward = new Date();
		HibernateUtil.save(bean);
		ActLog.log.ChallengeAward(jz.id, jz.name,2, values[0]);
	}

	/*
	 * 20160114  策划改为不从邮件发送，而是手动领取奖励
	 */
	public void sendEmailOfHighestRank(JunZhu jz, PvpBean bean,
			int oldHighestRank) {
		int highestRank = bean.highestRank;
		// 非首次战斗5000名以外没有奖励
		if (highestRank > 5000 && bean.allBattleTimes != 1) {
			log.info("玩家:{}最高名次：{}小于5000，所以没有奖励", jz.id, highestRank);
			return;
		}

		int oldYuanB = 0;
		int newYuanb = 0;
		BaiZhanRank br = baiZhanRankMap.get(highestRank);
		if (br != null) newYuanb = br.yuanbao;
		BaiZhanRank old = baiZhanRankMap.get(oldHighestRank);
		if (old != null) oldYuanB = old.yuanbao;
		int awardYuanBao = newYuanb - oldYuanB;
		awardYuanBao = awardYuanBao<0?0:awardYuanBao;
		/*
		 * 所以希望对第一次百战胜利后排名奖励的元宝数做特殊处理，
		 * 在原有计算值的基础上加上一个固定值，固定值读Canshu表的BAIZHAN_FIRSTWIN_YUANBAO
		 */
		if(bean.allBattleTimes == 1){
			awardYuanBao += CanShu.BAIZHAN_FIRSTWIN_YUANBAO;
			log.info("玩家:{}首次百战胜利，额外添加奖励，总奖励元宝：{}", jz.id, awardYuanBao);
		}
		bean.rankAward += awardYuanBao;
		log.info("玩家:{}百战获胜，名次升到历史最高:{}，本次奖励元宝:{}个", jz.id,
				highestRank, awardYuanBao);

//			String fujian = "0:900002:" + awardYuanBao;
//			Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(40001);
//			if (mailConfig == null) {
//				log.error("mail.xml配置文件找不到type=40001的数据");
//				return;
//			}
//			String content = mailConfig.content;
//			String senderName = mailConfig.sender;
//			boolean suc = EmailMgr.INSTANCE.sendMail(jz.name, content, fujian,
//					senderName, mailConfig, "");
//		if (suc) {
//		log.info("玩家:{}百战获胜，名次升到历史最高:{}，本次奖励元宝:{}个", jz.id,
//				highestRank, awardYuanBao);
//		} else {
//			log.error("玩家:{}百战历史名次最高奖励邮件发送失败", jz.id);
//		}
	}

	public boolean resetBeanAfterWin(long jId, PvpBean bean, int weiWang,
			int rank) {
		bean.junXianLevel = getBaiZhanTempByRank(rank).id;
		IoSession su = SessionManager.inst.findByJunZhuId(jId);
		if(su != null ){
			su.setAttribute(SessionAttKey.JUN_XIAN_ID, bean.junXianLevel);
		}
		return false;
	}
	

	/**
	 * 进入战斗时，检验双方数据是否发生变化
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getPlayerState(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("百战千军出错：君主不存在");
			return;
		}
		ChallengeReq.Builder req = (ChallengeReq.Builder) builder;
		long enemyId = req.getOppoJunZhuId();
		int enemyrankreq = req.getOppoJunzhuRank();
		log.info("getPlayerState 敌人是: {} , 排名是 : {}",enemyId,enemyrankreq);
		ChallengeResp.Builder resp = ChallengeResp.newBuilder();
		/*
		 * 判断挑战者的状态
		 */
		if(enemyId == jz.id){
			log.error("玩家{}在百战千军中试图挑战自己",enemyId);
			//TODO  如果策划需要玩家挑战自己的提示，这里加
			return ;
		}
		PvpBean bean = HibernateUtil.find(PvpBean.class, jz.id);
		if (bean == null) {
			log.error("百战千军挑战请求出错：百战数据不存在");
			resp.setType(6);
			session.write(resp.build());
			return;
		} else {
			resetPvpBean(bean);
		}
		// 1判断今日次数是不是够
		if (bean.remain <= 0) {
			log.error("百战千军挑战请求出错：今日百战挑战次数已经用完");
			resp.setType(4);
			session.write(resp.build());
			return;
		}
		// 2 判断cd时间到不到
		int timeCD = getCountDown(bean);
		if (timeCD > 0) {
			log.error("百战千军挑战请求出错：CD时间没有结束");
			resp.setType(5);
			session.write(resp.build());
			return;
		}
		int myRank = getPvpRankById(jz.id);
		int enemyRank = getPvpRankById(enemyId);
		int myjunxian = getJunXianByRank(myRank);	
		int enemyJunxian = getJunXianByRank(enemyRank);
		log.info("君主{}进行百战千军挑战，我的排名：{}，目标排名：{}",jz.id,myRank,enemyRank);
		//获取玩家在请求目标军衔的挑战目标列表
		int junXianId = getJunXianByRank(enemyrankreq) ;
//		List<Integer> fourRanks = getJunXianEnemylist(jz.id , junXianId); 
//		if(fourRanks == null || fourRanks.size() == 0){
//			log.error("获取玩家{}在军衔{}敌人排名列表失败",jz.id , junXianId);
//			return;
//		}		
		if (enemyRank != -1 && enemyRank != enemyrankreq) {
			log.error("百战千军挑战请求出错:对手名次变化");
			log.info("dbRank2 is :{} ", enemyRank);
			log.info("rank is :{} ", enemyrankreq);
			resp.setType(7);
			// 对手名次发生变化，发送目标军衔对应挑战目标列表2016-04-21
			List<Integer> fourRanks = getJunXianEnemylist(jz.id , junXianId); 
			List<OpponentInfo.Builder> enemyBuilderList = fillEnemyListInfo(fourRanks);
			for(OpponentInfo.Builder b : enemyBuilderList ){
				resp.addOppoList(b);
			}
			session.write(resp.build());
			armyCache.remove(jz.id);//需求变更，无需记录玩家队形2016-04-21
			return;
		}
		if (!checkJunXianCanChallenge(myjunxian , enemyJunxian)) {
			log.error("百战千军挑战请求出错:君主名次变化导致军衔不足以挑战目标");
			log.info("myRank is :{} ", myRank);
			resp.setType(8);
			session.write(resp.build());
			armyCache.remove(jz.id);//需求变更，无需记录玩家队形2016-04-21
			return;
		}
//		if(!fourRanks.contains(enemyrankreq)){
//			log.error("百战千军君主{}试图挑战不再对手列表中的对手，军衔ID：{}",jz.id,junXianId);
//			return;
//		}
		// type = 2 挑战中, 1 可以挑战
		BattleInfo4Pvp battleInfo4Pvp = goingMap.get(enemyId);
		long now = System.currentTimeMillis() / 1000;
		if (battleInfo4Pvp != null) {
			long time = battleInfo4Pvp.startTime / 1000 + CanShu.MAXTIME_BAIZHAN + PVP_BATTLE_DELAY_TIME;
			if (now >= time) {
				goingMap.remove(enemyId);
				log.info("对手:{}百战中的状态是异常终止状态,现在可以参加战斗", enemyId);
			}
		}  
		if (battleInfo4Pvp == null) {
			resp.setType(1);
			//校验全部通过之后，根据请求类型判断是否发送对战界面信息
			if(req.getType() == 1){
				makechanllenginfo(jz, enemyId, bean, resp);
			}
			log.info("被挑战者：{}百战中的状态是无战斗状态，可以进入挑战", enemyId);
		} else {
			resp.setType(2);
			log.info("被挑战者：{}百战是战斗状态，不可以进入挑战", enemyId);
		}
		session.write(resp.build());
	}

	public int getYuanBao(int buyCount, int type) {
		return PurchaseMgr.inst.getNeedYuanBao(type, buyCount);
	}

	@Override
	public void proc(Event event) {
		switch (event.id) {
		case ED.JUNZHU_LOGIN:
			if (event.param != null && event.param instanceof Long) {
				long jzid = (Long) event.param;
				//君主是否在线
				IoSession session = SessionManager.getInst().getIoSession(jzid);
				JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
				if(jz == null){
					break;
				}
				//君主是否开启百战
				boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.baizhan, jz.id, jz.level);
				if(!isOpen){
					break;
				}
				
				//登录的时候领奖邮件判断
				addDailyAward(jz, PVPConstant.LOGIN_SEND_EMAIL);
				
				//登录的时候各种百战相关的红点推送
				// 百战历史记录（被人打了）
				PvpBean bean = HibernateUtil.find(PvpBean.class, jz.id);
				if(bean != null && !bean.isLook){
					FunctionID.pushCanShowRed(jz.id, session, FunctionID.baiZhanRecord);
				}
				// 百战次数（有没用的次数）
				if (bean == null ||
						(bean.remain > 0 && getCountDown(bean) <= 0)){
					FunctionID.pushCanShowRed(jz.id, session, FunctionID.baizhanCount);
				}
				//未领取排名奖励
				if(bean != null && bean.rankAward > 0 ){
					//TODO 添加排名奖励红点推送，等待策划配参数
					FunctionID.pushCanShowRed(jz.id, session, FunctionID.baizhan);
				}
				//未领取每日奖励（21点到24点之间）
				if(bean != null && 
				(bean.lastAwardTime == null || !DateUtils.isSameDay(bean.lastAwardTime))){
					int timeDistance = DateUtils.timeDistanceTodayOclock(21, 0);
					if(timeDistance <= 0 ){
						//TODO 添加每日奖励红点推送，等待策划配置参数
						FunctionID.pushCanShowRed(jz.id, session, FunctionID.baiLingJiang);
					}
				} 
			}
			break;
		case ED.REFRESH_TIME_WORK:
			//定时刷新时百战相关红点推送
			{
				IoSession session=(IoSession) event.param;
				JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
				if(jz == null){
					break;
				}
				boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.baizhan, jz.id, jz.level);
				if(!isOpen){
					log.info("君主：{}--百战千军：{}的功能---未开启,被挑战不推送",jz.id,FunctionID.baizhan);
					break;
				}
				// 百战历史记录（被人打了）
				PvpBean bean = HibernateUtil.find(PvpBean.class, jz.id);
				if(bean != null && !bean.isLook){
					FunctionID.pushCanShowRed(jz.id, session, FunctionID.baiZhanRecord);
				}
				// 百战次数（有没打的次数）
				if (bean == null ||
						(bean.remain > 0 && getCountDown(bean) <= 0)){
					FunctionID.pushCanShowRed(jz.id, session, FunctionID.baizhanCount);
				}
				//未领取排名奖励
				if(bean != null && bean.rankAward > 0 ){
					//TODO 添加排名奖励红点推送，等待策划配参数
					FunctionID.pushCanShowRed(jz.id, session, FunctionID.baizhan);
				}
				//未领取每日奖励（21点到24点之间）
				if(bean != null && 
				(bean.lastAwardTime == null || !DateUtils.isSameDay(bean.lastAwardTime))){
					int timeDistance = DateUtils.timeDistanceTodayOclock(21, 0);
					if(timeDistance <= 0 ){
						//TODO 添加每日奖励红点推送，等待策划配置参数
						FunctionID.pushCanShowRed(jz.id, session, FunctionID.baiLingJiang);
					}
				} 
				break;
			}
		}
	}

	@Override
	public void doReg() {
		EventMgr.regist(ED.JUNZHU_LOGIN, this);
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
	}

	public PvpBean initJunZhuPVPInfo(long jzId, int rank) {
		// 根据参数创建一个一个新的PVP数据，会覆盖原有数据
		PvpBean bean = new PvpBean();
		bean.junZhuId = jzId;
		bean.usedTimes = 0;
		bean.remain = PVPConstant.ZHAN_TOTAL_TIMES;
		bean.junXianLevel = PVPConstant.XIAO_ZU_JI_BIE;
//		bean.zuheId = -1;
		bean.gongJiZuHeId = -1;
		bean.highestRank = rank;
		bean.lastHighestRank = rank;
		bean.isLook = true;
		bean.winToday = 0;
		bean.allWin = 0;
		bean.cdCount = 0;
		bean.buyCount = 0;
		bean.lastDate = null;
		bean.lastShowTime = null;
		bean.lastAwardTime = null;
		bean.todayRefEnemyTimes = 0;
		bean.showWeiWang = 0;
		bean.lastWeiWang = 0;
		bean.allBattleTimes = 0;
		bean.initPvpTime = new Date();
		bean.leiJiWeiWang = CanShu.WEIWANG_INIT; // 玩家未领取的威望初始累计值，add 20150917
		bean.lastCalculateAward = bean.lastGetAward = new Date(); //初始设定lastCalculateAward==lastGetAward
		Cache.pvpBeanCaChe.put(bean.junZhuId, bean);
		HibernateUtil.insert(bean);
		log.info("玩家id是 ：{}的百战数据库记录PvpBean生成成功，等级是{}", jzId, rank);
		return bean;
	}

	/**
	 * 获取距离下次挑战剩余时间（倒计时时间）
	 * 
	 * @Title: getCountDown
	 * @Description:
	 * @param pvb
	 * @return
	 */
	public int getCountDown(PvpBean pvb) {
		if (pvb.lastDate == null) {
			return 0;
		}
		// 今日免费挑战次数已经用完
		if (pvb.remain == 0) {
			// 当前时间距离第二天凌晨四点的时间
			int leftSeconds = DateUtils.getTimeToNextNeedHour(new Date(), 4);
			return leftSeconds;
		}
		Date now = new Date();
		int leftTime = (int) (pvb.lastDate.getTime() / 1000 - now.getTime()
				/ 1000 + PVPConstant.INTERVAL_ZHAN_SECOND);
		return leftTime <= 0 ? 0 : leftTime;
	}

	public void addDailyAward(JunZhu jz, byte sendType) {

		long id = jz.id;
		JunZhu junZhu = HibernateUtil.find(JunZhu.class, id);
		if (junZhu == null) {
			log.error("在线发送百战每日奖励邮件，玩家不存在");
			return;
		}
		PvpBean bean = HibernateUtil.find(PvpBean.class, id);
		if (bean == null) {
			log.error("在线发送百战每日奖励邮件，玩家:{}百战没有开启", id);
			return;
		}
		// 玩家登陆
		if (sendType == PVPConstant.LOGIN_SEND_EMAIL) {
			Date last = bean.lastAwardTime;
			// 今天的奖励已经发出去了, 今日奖励已经给出表明昨天的也已经给出。
			if(last != null){
				if(DateUtils.isSameDay(last)){
					return;
				}else if(DateUtils.isBeforeDay(System.currentTimeMillis(), last.getTime())){
					log.info("君主:{}上一个21点的百战每日奖励已经发出", id);
					return;
				}
			}else {
				last = bean.initPvpTime;
				int ho1 = DateUtils.getHourOfDay(bean.initPvpTime);
				Date now = new Date();
				int ho2 = DateUtils.getHourOfDay(now);
				boolean sameDay = DateUtils.isSameDay(bean.initPvpTime, now);
				if (sameDay && (ho2 < 21 || ho1 >= 21)) {
					log.error("玩家:{}是今天21点之前,开启，登陆，无奖励或者是昨天21点后开的百战，登陆，无奖励", junZhu.name);
					return;
				}
				//如果是相邻天
				if(ho1 >= 21 && ho2 < 21 && (DateUtils.isBeforeDay(now.getTime(), bean.initPvpTime.getTime())) ){
					log.error("玩家:{}是昨天21点后开的百战，今日21点之前登陆游戏无昨天21点奖励", junZhu.name);
					return;
				}
			}
			// 玩家离线天数计算，超过1天可以获取离线奖励
			int offLineDays = (int) (System.currentTimeMillis()/ (24*3600*1000) - ( last.getTime() / (24*3600*1000) ) );
			if( offLineDays >1 ){
				log.info("玩家{}离线{}天，发送百战千军离线奖励", id,offLineDays);
				offLineDays = MathUtils.getMin(offLineDays, 15) ;
				sendoffLineAward( offLineDays ,junZhu,bean );
//				return;
			}
		}else if(sendType == PVPConstant.ONLINE_SEND_EMAIL){
			// 玩家在线发送奖励
			Date last = bean.lastAwardTime;
			// 当日奖励已经领取了
			if(last != null && DateUtils.isSameDay(last)){
				return;
			}
		}
		int rrr = getPvpRankById(junZhu.id);
		BaiZhan bai = getBaiZhanTempByRank(rrr);
		if (bai == null) {
			log.error("BaiZhan表中没有找到军衔等级是：{}的奖励配置信息", bean.junXianLevel);
			return;
		}
		StringBuilder dayAward = AwardMgr.inst.reStructAwardStr(bai.dayAward);
		
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(40002);
		if (mailConfig == null) {
			log.error("mail.xml配置文件找不到type=40002的数据, cmd:{}", id);
			return;
		}
		String content = mailConfig.content;
		String senderName = mailConfig.sender;
		boolean suc = EmailMgr.INSTANCE.sendMail(junZhu.name, content,
				dayAward.toString(), senderName, mailConfig, "");
		if (suc) {
			if (sendType == PVPConstant.LOGIN_SEND_EMAIL) {
				/*
				 * 因为发送的是昨日奖励，因此 设定上次的奖励时间是昨天22点，而不是现在
				 */
				bean.lastAwardTime = DateUtils.getLast10();
				log.info("玩家:{}再次登录游戏发送前一天的百战每日奖励邮件，成功，奖励是:{}，bean.lastAwardTime 记做：{} ",
						 junZhu.name, dayAward, bean.lastAwardTime);
			}else if(sendType == PVPConstant.ONLINE_SEND_EMAIL){
				bean.lastAwardTime = DateUtils.getLast10();
				log.info("玩家:{}在线发送当天的百战每日奖励邮件，成功，奖励是:{}，bean.lastAwardTime 记做：{} ",
						 junZhu.name, dayAward, bean.lastAwardTime);
			}
			HibernateUtil.save(bean);
		} else {
			log.error("玩家:{}百战每日奖励邮件发送失败， sendMail出错导致", junZhu.name);
		}
	}
	
	public void sendoffLineAward( int offLineDays , JunZhu junZhu , PvpBean bean){
		//TODO 发送离线天数奖励逻辑
		int rank =getRankById(junZhu.id); 
		BaiZhan baiZhanTemp = getBaiZhanTempByRank(rank);
		if (baiZhanTemp == null) {
			log.error("BaiZhan表中没有找到rank是：{}的奖励配置信息", rank);
			return;
		}
		StringBuilder awardBuilder = new StringBuilder();
		String[] s = baiZhanTemp.dayAward.split("#");
		if (s != null) {
			int len = s.length;
			for (int i = 0; i < len; i++) {
				String[] award = s[i].split(":");
				if (award != null) {
					int type = Integer.parseInt(award[0]);
					int tempId = Integer.parseInt(award[1]);
					int number = Integer.parseInt(award[2]);
					if (type == 10) {
						AwardTemp a = AwardMgr.inst.calcAwardTemp(tempId);
						if(a != null) {
							type = a.itemType;
							tempId = a.itemId;
							number = a.itemNum;
						}
					}
					number *= offLineDays ;
					awardBuilder.append("#").append(type).append(":").append(tempId)
							.append(":").append(number);
				}
			}
			awardBuilder = new StringBuilder(awardBuilder.substring(1));
		}
		//TODO 目前策划没有提出具体的邮件配置，暂时套用旧的
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(40002);
		if (mailConfig == null) {
			log.error("mail.xml配置文件找不到type=40002的数据, cmd:{}", junZhu.id);
			return;
		}
		String content = mailConfig.content;
		String senderName = mailConfig.sender;
		boolean succes = EmailMgr.INSTANCE.sendMail(junZhu.name, content,
				awardBuilder.toString(), senderName, mailConfig, "");
		if(succes){
			bean.lastAwardTime = DateUtils.getLast10();
			HibernateUtil.save(bean);
			log.info("玩家{}离线{}天之后登录，发送百战千军离线奖励邮件:{}",junZhu.id,offLineDays,awardBuilder.toString());
		}
	}
	
	
	/**通过玩家的排名获取玩家军衔*/
	public int getJunXianByRank(int rank){
		@SuppressWarnings("unchecked")
		List<BaiZhan> baizhanList = TempletService.listAll(BaiZhan.class
				.getSimpleName());
		for(BaiZhan b:baizhanList){
//		for(int id : baiZhanMap.keySet()){
//			BaiZhan b = baiZhanMap.get(id);
			if(rank >= b.minRank && rank <=b.maxRank){
				return b.id;
			}
		}
		return PVPConstant.XIAO_ZU_JI_BIE;
	}
	
	/**检查君主军衔是否足以挑战目标军衔*/	
	public boolean checkJunXianCanChallenge( int myJunxian , int targetJunxian){
		if(myJunxian <= 0 || targetJunxian <=0){
			log.error("目标挑战判断出错：军衔id小于0，我的军衔{}，目标军衔{}",myJunxian,targetJunxian);
			return false ;
		}
		BaiZhan myPeiZhi = baiZhanMap.get(myJunxian);
		BaiZhan targetPeiZhi = baiZhanMap.get(targetJunxian);
		if(myPeiZhi == null ||targetPeiZhi == null ){
			log.error("目标挑战判断出错：无法获取军衔配置，我的军衔{}，目标军衔{}",myJunxian,targetJunxian);
			return false;
		}
		return (targetPeiZhi.jibie - myPeiZhi.jibie) <= 1 ;
	}
	

	/**
	 * 请求 PVP 战斗数据
	 * 
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void PVPDateInfoRequest(int id, IoSession session, Builder builder,
			boolean isPVp) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			log.error("君主不存在");
			return;
		}
		long jId = junZhu.id;
		int jlevel = junZhu.level;
		PvpZhanDouInitReq.Builder req = (qxmobile.protobuf.ZhanDou.PvpZhanDouInitReq.Builder) builder;
		long enemyId = req.getUserId();
		boolean isNpc = false;
		int npcId = 1;
		if (enemyId < 0) {
			isNpc = true;
			npcId = (int) -enemyId;
		}
		ZhanDouInitResp.Builder resp = ZhanDouInitResp.newBuilder();
		Group.Builder enemyTroop = Group.newBuilder();
		List<Node> enemys = new ArrayList<ZhanDou.Node>();
		int oppolevel = 0;
		int enemyFlagIndex = 101;
		if (isNpc) {
			// npc
			BaiZhanNpc npc = npcs.get(npcId);
			fillNPCDataInfo(enemys, npc, enemyFlagIndex, npcId, true);
			oppolevel = npc.level;
			enemyTroop.setMaxLevel(999);
		} else {
			// 君主
			JunZhu enemy = HibernateUtil.find(JunZhu.class, enemyId);
			PvpBean ebean = HibernateUtil.find(PvpBean.class, enemyId);
//			int zuheId = ebean == null ? -1 : ebean.zuheId;
			int zuheId = ebean == null ? -1 : ebean.gongJiZuHeId;
			PveMgr.inst.fillJunZhuDataInfo(resp, session, enemys, enemy,
					enemyFlagIndex, zuheId, enemyTroop);
			oppolevel = enemy.level;
			enemyTroop.setMaxLevel(BigSwitch.pveGuanQiaMgr
					.getGuanQiaMaxId(enemy.id));
		}
		enemyFlagIndex += 1;
		setBingData(enemys, enemyFlagIndex, jId, jlevel, oppolevel, (byte) ALL);
		enemyTroop.addAllNodes(enemys);
		resp.setEnemyTroop(enemyTroop);

		int zhandouId =  (int)TableIDCreator.getTableID(ZhanDouRecord.class, 1L);// 战斗id 后台使用
		int mapId = 0;
		Group.Builder selfTroop = Group.newBuilder();
		List<Node> selfs = new ArrayList<ZhanDou.Node>();
		PvpBean bean = HibernateUtil.find(PvpBean.class, junZhu.id);
		int zuheId = bean == null ? -1 : bean.gongJiZuHeId;
		int selfFlagIndex = 1;
		PveMgr.inst.fillJunZhuDataInfo(resp, session, selfs, junZhu,
				selfFlagIndex, zuheId, selfTroop);
		selfFlagIndex += 1;
		setBingData(selfs, selfFlagIndex, jId, jlevel, oppolevel, (byte) 0);
		selfTroop.addAllNodes(selfs);
		selfTroop.setMaxLevel((int) BigSwitch.pveGuanQiaMgr
				.getGuanQiaMaxId(junZhu.id));
		resp.setSelfTroop(selfTroop);
		resp.setZhandouId(zhandouId);
		resp.setMapId(mapId);
		resp.setLimitTime(CanShu.MAXTIME_BAIZHAN);
		session.write(resp.build());
		// 战斗开始
		if (isPVp) {
			dealDataAfterEnterBattle(zhandouId, junZhu, bean, enemyId);
		}
	}
	
	/**
	 * 
	 * @param selfs
	 * @param npc
	 * @param flagIndex
	 * @param modelNum			modelId，pve时读NpcTemp表的modelId需要根据modelCompute来判断是否需要计算
	 * @param modelCompute		modelId是否需要计算（pvp里需要计算）
	 */
	public void fillNPCDataInfo(List<Node> selfs, PvpNpc npc, int flagIndex,
			int modelNum, boolean modelCompute) {
		Node.Builder npcNode = Node.newBuilder();
		npcNode.addFlagIds(flagIndex);
		// 君主类型
		npcNode.setNodeType(NodeType.PLAYER);
		npcNode.setNodeProfession(NodeProfession.NULL);
		if(modelCompute) {
			npcNode.setModleId(npc.getRoleId(modelNum));
		} else {
			npcNode.setModleId(modelNum);
		}
		PveMgr.inst.fillDataByGongjiType(npcNode, null);
		// type
		npcNode.setNodeType(NodeType.valueOf(npc.type));
		npcNode.setNodeProfession(NodeProfession.valueOf(npc.profession));
		npcNode.setNodeName(getNPCName(npc.name));
		npcNode.setHpNum(npc.lifebarNum);
		npcNode.setAppearanceId(npc.modelApID);
		npcNode.setNuQiZhi(0);
		npcNode.setMibaoCount(0);
		npcNode.setMibaoPower(0);
		npcNode.setArmor(npc.armor);
		npcNode.setArmorMax(npc.armorMax);
		npcNode.setArmorRatio(npc.armorRatio);
		PveMgr.inst.fillGongFangInfo(npcNode, npc);
		// 添加装备
		List<Integer> weaps = Arrays.asList(npc.weapon1, npc.weapon2,
				npc.weapon3);
		PveMgr.inst.fillZhuangbei4Npc(npcNode, weaps, npc);
		// 添加秘宝信息
		List<Integer> skillIdList = MibaoMgr.inst.getSkillIdsFromConfig(npc.mibaoZuhe, npc.mibaoZuheLv);
		for(Integer skillId : skillIdList) {
			PveMgr.inst.addNodeSkill(npcNode, skillId);
		}
		npcNode.setHp(npcNode.getHpMax() * npc.lifebarNum);
		selfs.add(npcNode.build());
	}

	public void setBingData(List<Node> selfs, int flagIndex, long jId,
			int mylevel, int oppolevel, byte begin) {
		int[] bings = getSoldiers(jId);
		if (bings == null) {
			bings = refreshSoldiers(mylevel, oppolevel);
			begin = 0;
		}
		ArrayList<GuYongBing> bingList = new ArrayList<GuYongBing>();
		for (int i = begin; i < begin + ALL; i++) {
			GuYongBing bing = guYongBingMap.get(bings[i]);
			int renshu = bing.renshu;
			for (int k = 0; k < renshu; k++) {
				bingList.add(bing);
			}
		}
		fillGuYongBingDataInfo(selfs, flagIndex, bingList);
	}

	public void setBingData4YaBiao(List<Node> selfs,
			YaBiaoBean ybBean, int flagIndex, long jId, int mylevel,
			int oppolevel, byte begin) {
		int[] bings = bingsCache4YB.get(jId);
		// 没有雇佣兵则重新分配
		if (bings == null) {
			bings = refreshSoldiers(mylevel, oppolevel);
			begin = 0;
			bingsCache4YB.put(jId, bings);
			log.info("押镖君主本次押镖初始佣兵数据为{}", Arrays.toString(bings));
		} else {
			log.info("押镖君主上次的佣兵数据为{}", Arrays.toString(bings));
		}
		ArrayList<GuYongBing> bingList = new ArrayList<GuYongBing>();
		for (int i = 0; i < ALL; i++) {
			GuYongBing bing = guYongBingMap.get(bings[i]);
			int renshu = bing.renshu;
			for (int k = 0; k < renshu; k++) {
				bingList.add(bing);
			}
		}
		fillGuYongBingDataInfo4YaBiao(selfs, ybBean, flagIndex, bingList);
	}

	public ArrayList<GuYongBing> getGuYongBingList(int[] bingIds) {
		ArrayList<GuYongBing> bingList = new ArrayList<GuYongBing>();
		for (int i = 0; i < bingIds.length; i++) {
			GuYongBing bing = guYongBingMap.get(bingIds[i]);
			if (bing == null) {
				log.error("未发现雇佣兵，id:{}", bingIds[i]);
				continue;
			}
			int renshu = bing.renshu;
			for (int k = 0; k < renshu; k++) {
				bingList.add(bing);
			}
		}
		return bingList;
	}

	public void fillGuYongBingDataInfo(List<Node> selfs, int flagIndex,
			List<GuYongBing> bingList) {
		Node.Builder wjNode = null;
		for (GuYongBing bing : bingList) {
			wjNode = Node.newBuilder();
			GongjiType gongjiType = PveMgr.inst.id2GongjiType
					.get(bing.gongjiType);
			PveMgr.inst.fillDataByGongjiType(wjNode, gongjiType);
			if(flagIndex <= 0){
				 wjNode.addFlagIds(bing.zhanweiLve);
			}else {
				wjNode.addFlagIds(flagIndex++);
			}
			wjNode.setModleId(bing.modelId);
			wjNode.setNodeType(NodeType.valueOf(bing.type));
			wjNode.setNodeProfession(NodeProfession.valueOf(bing.profession));
			wjNode.setHp(bing.shengming * bing.lifebarNum);
			wjNode.setNodeName(getNPCName(bing.name));
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
			selfs.add(wjNode.build());
		}
	}

	public void fillGuYongBingDataInfo4YaBiao(List<Node> selfs,
			YaBiaoBean ybBean, int flagIndex,
			List<GuYongBing> bingList) {
		HashMap<Integer, Integer> ybNpc = (HashMap<Integer, Integer>)  null;//YaBiaoHuoDongMgr.inst.ybNpcMap.get(ybBean.junZhuId);
		CartTemp cart = YaBiaoHuoDongMgr.cartMap.get(ybBean.horseType);
		Node.Builder wjNode = null;
		for (GuYongBing bing : bingList) {
			wjNode = Node.newBuilder();
			GongjiType gongjiType = PveMgr.inst.id2GongjiType
					.get(bing.gongjiType);
			PveMgr.inst.fillDataByGongjiType(wjNode, gongjiType);
			Integer falgId = flagIndex++;
			int shengming =
					( ybNpc == null) ? bing.shengming
					: (ybNpc.get(falgId) == null ? 0 : ybNpc.get(falgId));
			shengming+=bing.shengming*cart.recoveryRate;//雇佣兵回血
			if (shengming > 0) {// 没血的雇佣兵不加入List
				wjNode.addFlagIds(falgId);
				wjNode.setModleId(bing.modelId);
				wjNode.setNodeType(NodeType.valueOf(bing.type));
				wjNode.setNodeProfession(NodeProfession
						.valueOf(bing.profession));
				wjNode.setHp(shengming * bing.lifebarNum);
				wjNode.setNodeName(getNPCName(bing.name));
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
				selfs.add(wjNode.build());
			}
		}
	}

	public void huanYiPi(PvpBean bean, BaiZhanInfoResp.Builder resp) {
		// 第一次刷新，花费是0元宝，配置中已配置，不用特殊处理
		int count = bean.todayRefEnemyTimes;
		int pay = PurchaseMgr.inst.getNeedYuanBao(
				PurchaseConstants.BAIZHAN_REFRESH_ENEMYS, count + 1);
		resp.setHuanYiPiYB(pay);
	}

	// 获取战斗记录列表并且返回
	public void getZhanDouRecord(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("百战千军出错：君主不存在");
			return;
		}
		long jId = jz.id;
		String where = "where junzhuId = " + jId + " or enemyId = " + jId;
		long now = System.currentTimeMillis() / 1000;
		List<ZhanDouRecord> recordList = ZhanDouRecordDao.list(jId);
		ZhandouRecordResp.Builder resp = ZhandouRecordResp.newBuilder();
		ZhandouItem.Builder info = null;
		int passTime = 0;
		if (recordList == null || recordList.size() == 0) {
			session.write(resp.build());
			return;
		}
		for (ZhanDouRecord reco : recordList) {
			passTime = (int) (now - reco.time.getTime() / 1000);
			if (passTime > PVPConstant.REPLAY_SAVE_TIME) {
				// 数据库删除时间过了的战斗记录
				ZhanDouRecordDao.delete(reco);
//				// 时间过了的战斗在 录像回放map中同样删除掉
				// 无战斗录像 20151105
//				replayCache.remove(reco.zhandouId);
				continue;
			}
			info = ZhandouItem.newBuilder();
			info.setZhandouId(reco.zhandouId);
			long eId = 0;
			GameObject enemy = null;
			if (jId == reco.junzhuId) {
				eId = reco.enemyId;
				info.setJunRankChangeV(reco.junRankChangeV);
				info.setWin(reco.result1);
			} else if(jId == reco.enemyId) {
				eId = reco.junzhuId;
				info.setJunRankChangeV(reco.enemyRankChangeV);
				if (reco.result1 == PVPConstant.GONG_JI_WIN) {
					info.setWin(PVPConstant.FANG_SHOU_LOSE);
				} else {
					info.setWin(PVPConstant.FANG_SHOU_WIN);
				}
			}
			if (eId < 0) {
				enemy = npcs.get((int) -eId);
			}else{
				enemy = HibernateUtil.find(JunZhu.class, eId);
			}
			if(enemy == null){
				continue;
			}
			info.setEnemyId(enemy.getId());
			info.setEnemyName(enemy.getName());
			info.setLevel(enemy.getLevel());
			info.setEnemyRoleId(enemy.getRoleId((int) -eId));
			info.setEnemyGuoJiaId(enemy.getGuoJiaId((int) -eId));
		
			info.setTime(passTime);
			resp.addInfo(info);
		}
		session.write(resp.build());
		PvpBean be = HibernateUtil.find(PvpBean.class, jId);
		if (be != null) {
			be.isLook = true;
			HibernateUtil.save(be);
		}
	}
	
	public void resetPvpBean(PvpBean bean) {
		// change 20150901
		if(DateUtils.isTimeToReset(bean.lastShowTime, CanShu.REFRESHTIME_PURCHASE)){
			bean.winToday = 0;
			bean.buyCount = 0;
			bean.cdCount = 0;
			bean.todayRefEnemyTimes = 0;

			bean.usedTimes = 0;
			bean.remain = PVPConstant.ZHAN_TOTAL_TIMES;
			bean.lastShowTime = new Date();
			HibernateUtil.save(bean);
		}
	}
	
	public String getNPCName(String key) {
		return HeroService.getNameById(key);
	}

	/*
	 * score 就是名次>=1开始的名次
	 * 如果不存在 返回-1
	 */
	public int getPvpRankById(long junIdOrNpcId) {
		synchronized (pvpRankLock) {
			if (junIdOrNpcId <= 0) {
				return DB.zscore_(KEY, "npc_" + (-junIdOrNpcId));
			} else {
				return DB.zscore_(KEY, "jun_" + junIdOrNpcId);
			}
		}
	}

	/*
	 * 从大到小排序，第一个score就是maxScore
	 */
	public int getMaxScore() {
		int score = 0;
		Map<String, Double> map = DB.zrevrangeWithScores(KEY, 0, 0);
		if (map == null) {
			return score;
		}
		for (Double d : map.values()) {
			if (d != null) {
				score = (int) d.doubleValue();
				return score;
			}
		}
		return score;
	}

	public int addPvpRankToRedis(long addId) {
		synchronized (pvpRankLock) {
			if (addId > 0) {
				int score = getMaxScore();
				DB.zadd(KEY, score + 1, "jun_" + addId);
				return score;
			}
		}
		return 0;
	}

	public void changeRankOfRedis(long changeId, long beChangeId,
			int changeRank, int beChangeRank) {
		if (changeId > 0) {
			DB.zadd(KEY, beChangeRank - 1, "jun_" + changeId);
		} else {
			DB.zadd(KEY, beChangeRank - 1, "npc_" + (-changeId));
		}
		if (beChangeId > 0) {
			DB.zadd(KEY, changeRank - 1, "jun_" + beChangeId);
		} else {
			DB.zadd(KEY, changeRank - 1, "npc_" + (-beChangeId));
		}
	}
	
	/**返回数组：[我的旧排名，敌人旧排名，我的新排名，敌人新排名]*/
	public int[] dealBattleEndRank(long jId, long enemyId) {
		int oldRankJunZhu = 0;
		int oldRankEnemy = 0;
		int newRankJunZhu = 0;
		int newRankEnemy = 0;
		synchronized (pvpRankLock) {
			oldRankJunZhu = DB.zscore_(KEY, "jun_" + jId);
			if (enemyId <= 0) {
				oldRankEnemy = DB.zscore_(KEY, "npc_" + (-enemyId));
			} else {
				oldRankEnemy = DB.zscore_(KEY, "jun_" + enemyId);
			}
			newRankJunZhu = oldRankJunZhu;
			newRankEnemy = oldRankEnemy;
			if (oldRankJunZhu > oldRankEnemy) {
				newRankJunZhu = oldRankEnemy;
				newRankEnemy = oldRankJunZhu;
				// 改变双方名次
				changeRankOfRedis(jId, enemyId, oldRankJunZhu, oldRankEnemy);
				log.info("玩家：{}，名次：{}挑战对手：{}，名次：{}战斗胜利，名次互换", jId,
						oldRankJunZhu, enemyId, oldRankEnemy);
			} else {
				log.info("玩家：{}，名次：{}挑战对手：{}，名次：{}战斗胜利，名次不变化", jId,
						oldRankJunZhu, enemyId, oldRankEnemy);
			}
		}
		return new int[] { oldRankJunZhu, oldRankEnemy, newRankJunZhu,
				newRankEnemy };
	}
	
	/**根据传入的君主id获取君主的军衔*/
	public int getJunXianById( long junZhuId ){
		IoSession session = SessionManager.inst.getIoSession(junZhuId);
		if(session != null ){
			Integer junXianLevel = (Integer)session.getAttribute(SessionAttKey.JUN_XIAN_ID);
			if( junXianLevel!= null ){
				return junXianLevel ;
			}
		}
		int ret = PVPConstant.XIAO_ZU_JI_BIE;
		int rank = getRankById(junZhuId);
		if(rank > 0){
			ret = getJunXianByRank(rank);
		}
		if( session != null ){
			session.setAttribute(SessionAttKey.JUN_XIAN_ID, ret);
		}
		return ret ;
	}
	
/**由君主ID获取军衔级别*/
	public int getJunxianLevel(long jid) {
		int junxian = getJunXianById(jid);
		BaiZhan bai = baiZhanMap.get(junxian);
		if(bai != null ){
			return bai.jibie;
		}
		return -1;
	}
	
	/**
	 * 根据排行军衔获取军衔级别
	 * @param junXian
	 * @return
	 */
	public static int getJunxianLevel(int junXian) {
		BaiZhan baiZhan = PvpMgr.inst.baiZhanMap.get(junXian);
		if(baiZhan !=  null){
			return baiZhan.jibie;
		}
		return -1;
	}
	
	/**
	 * @Title: getJunxian
	 * @Description: 获取军衔
	 * @param junZhu
	 * @return
	 * @return String
	 * @throws
	 */
	public String getJunXianName(long jzId) {
		int junxian = getJunXianById(jzId);
		return getJunXianName(junxian);
	}
	
	public String getJunXianName(int junXianId){
		BaiZhan bz = baiZhanMap.get(junXianId);
		String jxStr = bz == null ? "小卒" : bz.jibieName;
		return jxStr;
	}

	public int getRankById(long jzid){
		//获取实时排名
				int rank = getPvpRankById(jzid);
				if (rank <= 0) {
					try {
						//如果无法获取君主排名，则将君主加入排行榜
						addPvpRankToRedis(jzid);
						{//加入之后，重新获取排名，打印日志
							rank = getPvpRankById(jzid);
							log.info("新建立的君主id{},添加到key：{}的redis中，排名是:{} ", jzid,
									KEY, rank);
						}
					} catch (Exception e) {
						log.error("君主：{}百战取数据读取redis出错,请求百战主页失败，直接返回", jzid, e);
						return -1 ;
					}
				}
				return rank ;
	}
	
	public List<Integer> getAllRank(Collection<Long> ids){
		List<Integer> res = new LinkedList<Integer>();
		List<String> idStrList = new LinkedList<String>();
		for(Long id:ids){
			if(id<0){
				idStrList.add("npc_" + (-id));
			}else if (id>0) {
				idStrList.add("jun_" + id);
			}else{
				log.error("批量获取百战排名出错，请求的君主id是0");
			}
		}
		List<Object> ranks = DB.zscoreAll(KEY, idStrList);
		for(Object rank :ranks){
			if(rank != null){
				res.add( (int)((Double)rank).doubleValue()+1);
			}else{
				res.add(null);
			}
		}
		return res;
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
		log.info("退出PvpMgr");
	}

	public void handle(Mission m) {
		int id = m.code;
		IoSession session = m.session;
		Builder builder = m.builer;
		switch (m.code) {
		case PD.BAIZHAN_INFO_REQ:
			getBaiZhanMainInfo(id, builder, session);
			break;
		case PD.CHALLENGE_REQ:
			getPlayerState(id, builder, session);
			break;
		case PD.CONFIRM_EXECUTE_REQ:
			doConfirmExecute(id, builder, session);
			break;
		case PD.BAIZHAN_RESULT:
			dealBaiZhanResult(id, builder, session);
			break;
//		case PD.PLAYER_STATE_REQ:
//		协议变更2016-04-21	
//			break;
		// 请求此刻之前的战斗记录列表
		case PD.ZHAN_DOU_RECORD_REQ:
			getZhanDouRecord(id, session, builder);
			break;
		case PD.ZHANDOU_INIT_PVP_REQ:
			// PVP 战斗 add 20141225
			PVPDateInfoRequest(id, session, builder, true);
			break;
//		case PD.C_ZHANDOU_INIT_YB_REQ:// 押镖请求战斗初始化数据
//			YBDateInfoRequest(id, session, builder, true);
//			break;
		default:
			log.error("未处理的消息{}", id);
			break;
		}
	}

	public void addMission(int id, IoSession session, Builder builder) {
		Mission m = new Mission(id, session, builder);
		missions.add(m);
	}

	public void shutdown() {
		missions.add(exit);
	}
	
	/**redis被清了的时候用于重新生成排行榜，别乱用*/
	public void makeRank(List<BaiZhanNpc> list ){
		long time = System.currentTimeMillis();
		for (int index = list.size() - 1; index > -1; index--) {
			BaiZhanNpc npc = list.get(index);
			int minRank = npc.minRank;
			int maxRank = npc.maxRank;
			for (int i = minRank; i <= maxRank; i++) {
				// i指的是排名，而"npc_" + i中的i指的是npc的id，i的值等于PvpBean中的rank值
				Set<String> elem = DB.zrangebyscore_(KEY, i-1, i-1);
				if(elem != null && elem.size() > 0){
					continue;
				}
				DB.zadd(KEY, i - 1, "npc_" + i);
			}
		}
		log.info("redis的zset初始化5000名npc初始完成， 花费时间：{}",
				(System.currentTimeMillis() - time));
		time = System.currentTimeMillis();
		// 从大到小排名，获取member的排名， 从0开始的排序
		long rank = Redis.getInstance().zrank(KEY, "npc_5000");
		log.info("redis的zset中id是5000的元素的排名是: {}", rank);
		log.info("获取某一个member的排序值: 花费时间：{}",
				(System.currentTimeMillis() - time));
	
	}
	/**GM工具从排行榜上删人用*/
	public long removePvpRedisData(long jId) {
		long id = DB.zrem(KEY, "jun_" + jId);
		if( id==0 ){
			id = DB.zrem(KEY, "npc_" + jId);
		}
		log.info("removePvpRedisData : 君主id:{}, 得到结果是:{}", jId, id);
		return id;
	}
}
