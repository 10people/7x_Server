package com.qx.pvp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import log.ActLog;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import qxmobile.protobuf.PvpProto.PVPInfo;
import qxmobile.protobuf.PvpProto.PayChangeInfo;
import qxmobile.protobuf.PvpProto.PlayerStateReq;
import qxmobile.protobuf.PvpProto.PlayerStateResp;
import qxmobile.protobuf.PvpProto.RefreshMyInfo;
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

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.store.Redis;
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
import com.manu.dynasty.template.VipFuncOpen;
import com.manu.dynasty.util.DateUtils;
import com.manu.dynasty.util.MathUtils;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.qx.account.FunctionOpenMgr;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
import com.qx.award.AwardMgr;
import com.qx.battle.PveMgr;
import com.qx.email.EmailMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.guojia.GuoJiaMgr;
import com.qx.huangye.shop.ShopMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.mibao.MibaoMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.timeworker.FunctionID;
import com.qx.vip.VipData;
import com.qx.vip.VipMgr;
import com.qx.world.GameObject;
import com.qx.world.Mission;
import com.qx.yabiao.YaBiaoInfo;
import com.qx.yabiao.YabiaoMgr;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

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
	/** 雇佣兵的个数 **/
	public static final int ALL = 4;
	/* 存放正在战场中的君主 或者 npc */
	static public ConcurrentHashMap<Long, Long> goingMap;
	/* 战斗id */
	private AtomicInteger zhandouIdMgr = new AtomicInteger(1);
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
	private static Mission exit = new Mission(0, null, null);

	public PvpMgr() {
		inst = this;
		goingMap = new ConcurrentHashMap<Long, Long>();
		initData();
		// 开启线程
		new Thread(this, "PvpMgr").start();
	}

	@SuppressWarnings("unchecked")
	public void initData() {
		HashMap<Integer, BaiZhanNpc> baiZhanNpcMap = new HashMap<Integer, BaiZhanNpc>();
		HashMap<Integer, BaiZhanNpc> npcs = new HashMap<Integer, BaiZhanNpc>();
		HashMap<Integer, BaiZhan> baiZhanMap = new HashMap<Integer, BaiZhan>();
		HashMap<Integer, BaiZhanRank> baiZhanRankMap = new HashMap<Integer, BaiZhanRank>();
		guYongBingMap = new HashMap<Integer, GuYongBing>();
		HashMap<Integer, Duihuan> duihuanMap = new HashMap<Integer, Duihuan>();
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
		this.baiZhanNpcMap = baiZhanNpcMap;
		this.baiZhanMap = baiZhanMap;
		this.baiZhanRankMap = baiZhanRankMap;
		this.duihuanMap = duihuanMap;

		List<BaizhanPipei> piPeiList = TempletService
				.listAll(BaizhanPipei.class.getSimpleName());
		for (BaizhanPipei elem : piPeiList) {
			piPeiMap.put(elem.id, elem);
		}
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

	private void initRedisList(BaiZhanNpc npc) {
		int minRank = npc.minRank;
		int maxRank = npc.maxRank;
		for (int i = minRank; i <= maxRank; i++) {
			// i指的是排名，而"npc_" + i中的i指的是npc的id，i的值等于PvpBean中的rank值
			DB.zadd(KEY, i - 1, "npc_" + i);
		}
	}

	private void initNPC(Map<Integer, BaiZhanNpc> npcs, BaiZhanNpc npc) {
		int minRank = npc.minRank;
		int maxRank = npc.maxRank;
		for (int i = minRank; i <= maxRank; i++) {
			// i指的是npc的id，为了将5000个npc的id和BaiZhanNpc.xlsx的行对应起来
			npcs.put(i, npc);
		}
	}

	public void getBaiZhanMainInfo(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求百战主页出错：君主不存在");
			return;
		}
		long jid = jz.id;
		PvpBean bean = HibernateUtil.find(PvpBean.class, jid);
		int junRank = getPvpRankById(jid);
		if (junRank <= 0) {
			try {
				addPvpRankToRedis(jid);
				// 获取jz的排名,排名是从0开始
				{// 测试代码
					junRank = DB.zscore_(KEY, "jun_" + jid);
					log.info("新建立的君主id{},添加到key：{}的redis中，排名是:{} ", jid,
							KEY, junRank);
				}
			} catch (Exception e) {
				log.error("君主：{}百战取数据读取redis出错,请求百战主页失败，直接返回", jid, e);
				return;
			}
		}
		if (bean == null) {
			bean = initJunZhuPVPInfo(jid, junRank, jz.level);
			log.info("从有军衔开始就进行贡金的记录");
			// 从有军衔开始就进行贡金的记录
			EventMgr.addEvent(ED.GET_JUNXIAN, new Object[]{jz.id,bean});
		} else {
			// 检查是否更新数据
			resetPvpBean(bean);
		}

		// 判断玩家是否上次有异常退出百战的情况
		Object ob = isFinishBattle(jz, bean, junRank);
		if (ob != null && ob instanceof ChallengeResp.Builder) {
			ChallengeResp.Builder b = (ChallengeResp.Builder) ob;
			session.write(b.build());
			return;
		}
		if (ob != null && ob instanceof PlayerStateResp.Builder) {
			PlayerStateResp.Builder b = (PlayerStateResp.Builder) ob;
			session.write(b.build());
			armyCache.remove(jid);
			return;
		}
		// 接受请求数据
		BaiZhanInfoResp.Builder resp = BaiZhanInfoResp.newBuilder();
		resp.setJunZhuId(jid);
		resp.setJunZhuName(jz.name);
		AllianceBean guild = AllianceMgr.inst.getAllianceByJunZid(jid);
		resp.setLianMengName(guild == null ? " " : guild.name);
		// 是否显示战斗记录
		resp.setIsShow(!bean.isLook);
		/*
		 * 能够清除百战的CD最低vip等级
		 */
		VipFuncOpen vipData = VipMgr.vipFuncOpenTemp.get(VipData.clean_baiZhan_time);
		
		resp.setCanCleanCDvipLev(vipData == null? Integer.MAX_VALUE: vipData.needlv);

		if(vipData != null && jz.vipLevel >= vipData.needlv){
			resp.setCdYuanBao(getYuanBao(bean.cdCount + 1, 
					PurchaseConstants.BAIZHAN_CD));
		}

		// 当前的vip等级，今日可挑战的最大次数（包括购买）
		resp.setNowMaxBattleCount(getBattleCountForVip(jz.vipLevel));
		if (jz.vipLevel == VipMgr.maxVip) {
			resp.setNextMaxBattleCount(resp.getNowMaxBattleCount());
		} else {
			resp.setNextMaxBattleCount(getBattleCountForVip(jz.vipLevel + 1));
		}

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
		resp.setPvpInfo(fillPVPInfo(jz, bean, junRank));
		resp.setIsShow(!bean.isLook);

		/*
		 * 对手列表信息
		 */
		List<Integer> tenRanks = getChallengeRanks(bean, junRank);
		// 根据策划需求，百战第一次，随机一名等级==2的npc
		if(bean == null || bean.allBattleTimes == 0){
			BaiZhanNpc npc = null;
			int size = baiZhanNpcMap.size();
			for(int i =0; i<size; i++){
				npc = baiZhanNpcMap.get(i);
				if(npc != null && npc.level == 2){
					break;
				}
			}
			if(npc != null){
				int forthId = MathUtils.getRandomInMax(npc.minRank, npc.maxRank);
				int forthRank = getPvpRankById(forthId);
				log.info("forthId is {}, forthRank is {}", forthId, forthRank);
				// 前端第四个是引导对象
				tenRanks.set(3, forthRank);
			}
		}
		List<OpponentInfo.Builder> enemyBuilderList = fillEnemyListInfo(tenRanks);
		for (OpponentInfo.Builder b : enemyBuilderList) {
			resp.addOppoList(b);
		}
		/*
		 * 威望信息
		 */
		//return new int[]{weiHour, hasWei, getwei};
		int[] da = getMainWeiWang(bean, jid, jz.vipLevel);
		resp.setWeiWangHour(da[0]);
		resp.setHasWeiWang(da[1]);
		resp.setCanGetweiWang(da[2]);
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
		 * 发送
		 */
		session.write(resp.build());
	}

	public int[] getMainWeiWang(PvpBean pb, long jzId, int vipLevel) {
		int weiHour = 0;
		int hasWei = 0;
		int getwei = 0;
		
		int myRank = DB.zscore_(KEY, "jun_" + jzId);
		if (myRank > PVPConstant.TOTAL_NPC_COUNT) {
			myRank = PVPConstant.TOTAL_NPC_COUNT;
		}
		BaiZhanRank bai = baiZhanRankMap.get(myRank);
		if (bai == null) {
			log.error("BaiZhanRank表中没有找到军衔等级是：{}的奖励配置信息", myRank);
			weiHour = 0;
		} else {
			weiHour = bai.weiwang;
		}
		hasWei = ShopMgr.inst.getMoney(ShopMgr.Money.weiWang, jzId, null);
		int[] addA = getAccAward(new Date(), pb.lastCalculateAward, myRank,
				vipLevel, pb.lastGetAward);
		getwei = addA[0] + pb.leiJiWeiWang;
		return new int[]{weiHour, hasWei, getwei};
	}
	public int getBattleCountForVip(int vipLev) {
		int value = VipMgr.INSTANCE.getValueByVipLevel(vipLev,
				VipData.bugBaizhanTime);
		int times = PVPConstant.ZHAN_TOTAL_TIMES;
		times += PurchaseMgr.inst.getAllUseNumbers(PurchaseConstants.BAIZHAN,
				value);
		return times;
	}

	public PVPInfo.Builder fillPVPInfo(JunZhu jz, PvpBean bean, int junRank) {
		PVPInfo.Builder pInfo = PVPInfo.newBuilder();
		pInfo.setRank(junRank);
		pInfo.setLeftTimes(bean.remain);
		pInfo.setTotalTimes(bean.usedTimes + bean.remain);
		// 挑战CD时间
		pInfo.setTime(bean.remain == 0 ? 0 : getCountDown(bean));
		pInfo.setJunXianLevel(bean.junXianLevel);
		pInfo.setZhanLi(JunZhuMgr.inst.getJunZhuZhanliFinally(jz));
		// 添加组合id
		pInfo.setZuheId(bean.zuheId);
		return pInfo;
	}

	public List<OpponentInfo.Builder> fillEnemyListInfo(List<Integer> tenRanks) {
		List<OpponentInfo.Builder> enemyBuilderList = new ArrayList<OpponentInfo.Builder>();
		for (Integer rankTemp : tenRanks) {
			if (rankTemp < 1) {
				continue;
			}
			Set<String> elem = DB.zrangebyscore_(KEY, rankTemp - 1,
					rankTemp - 1);
			if (elem == null) {
				continue;
			}
			String s = null;
			for (String str : elem) {
				if (str != null) {
					s = str;
					break;
				}
			}
			if (s == null) {
				log.error("zrangebyscore_有问题:{}", rankTemp - 1);
				continue;
			}
			String[] sss = s.split("_");
			long playerId = Long.parseLong(sss[1]);
			if ("npc".equals(sss[0])) {
				// NPC
				BaiZhanNpc npc = npcs.get((int) playerId);
				OpponentInfo.Builder you = OpponentInfo.newBuilder();
				// npc发送负数的id
				you.setJunZhuId(-playerId);
				you.setJunZhuName(npc.name);
				you.setRank(rankTemp);
				you.setJunXianLevel(getBaiZhanItemByRank(rankTemp).jibie);
				you.setLianMengName("");
				you.setGuojia(npc.getGuoJiaId((int) playerId));
				you.setRoleId(npc.getRoleId((int) playerId));
				int zhanli = npc.power;
				log.info("npc:{} 战力是:{}", playerId, zhanli);
				you.setZhanLi(zhanli);
				you.setActivateMiBaoCount(npc.mibaoNum);
				you.setZuheId(npc.mibaoZuhe);
				you.setLevel(npc.level);
				enemyBuilderList.add(you);
			} else {
				// junzhu
				PvpBean pb = HibernateUtil.find(PvpBean.class, playerId);
				if (pb == null) {
					continue;
				}
				JunZhu junz = HibernateUtil.find(JunZhu.class, playerId);
				if(junz == null){
					continue;
				}
				OpponentInfo.Builder you = OpponentInfo.newBuilder();
				you.setJunZhuId(playerId);
				you.setJunZhuName(junz.name);
				you.setRank(rankTemp);
				you.setJunXianLevel(pb.junXianLevel);
				AllianceBean guild = AllianceMgr.inst
						.getAllianceByJunZid(playerId);
				you.setLianMengName(guild == null ? "" : guild.name);
				int zhanli = JunZhuMgr.inst.getJunZhuZhanliFinally(junz);
				you.setZhanLi(zhanli);
				you.setGuojia(junz.guoJiaId);
				you.setRoleId(junz.roleId);
				you.setActivateMiBaoCount(MibaoMgr.inst
						.getActivateCountByZuheId(playerId, pb.zuheId));
				you.setZuheId(pb.zuheId);
				you.setLevel(junz.level);
				enemyBuilderList.add(you);
			}
		}
		return enemyBuilderList;
	}

	public Object isFinishBattle(JunZhu jz, PvpBean mybean, int myRank) {
		long jid = jz.id;
		ChallengeResp.Builder bu = armyCache.get(jid);
		if (bu == null) {
			return null;
		}
		// change 20150901
		if(DateUtils.isTimeToReset(new Date(bu.getOpenTime()), CanShu.REFRESHTIME_PURCHASE)){
			return null;
		}
		// 君主名次发生变化
		if (bu.getMyRank() != myRank) {
			PlayerStateResp.Builder resp = PlayerStateResp.newBuilder();
			resp.setType(8);
			armyCache.remove(jid);
			return resp;
		}
		// 对手名次发生变化
		long oppoId = bu.getOppoId();
		int yourank = getPvpRankById(oppoId);
		if (yourank != bu.getOppoRank()) {
			PlayerStateResp.Builder resp = PlayerStateResp.newBuilder();
			resp.setType(7);
			armyCache.remove(jid);
			return resp;
		}
		int oppoLe = 0;
		int opzhanli = 0;
		int oppoRoleId = 1;
		int oppoZuheId = -1;
		int oppoActiveMiBaoCount = 0;
		if (oppoId < 0) {
			BaiZhanNpc npc = npcs.get((int) -oppoId);
			if (npc != null) {
				oppoLe = npc.level;
				opzhanli = npc.power;
				oppoZuheId = npc.mibaoZuhe;
				oppoActiveMiBaoCount = npc.mibaoNum;
				oppoRoleId = npc.getRoleId((int) -oppoId);
			}
		} else {
			JunZhu oppo = HibernateUtil.find(JunZhu.class, oppoId);
			if (oppo != null) {
				oppoLe = oppo.level;
				opzhanli = getZhanli(oppo);
				PvpBean you = HibernateUtil.find(PvpBean.class, oppoId);
				oppoActiveMiBaoCount = MibaoMgr.inst.getActivateCountByZuheId(
						oppoId, you.zuheId);
				oppoZuheId = you.zuheId;
				oppoRoleId = oppo.roleId;
			}
		}
		// 判断等级是否相等
		if (bu.getMyLevel() == jz.level && bu.getOppoLevel() == oppoLe) {
			bu.setMyZhanli(getZhanli(jz));
			bu.setOppoZhanli(opzhanli);
			bu.setMyZuheId(mybean.gongJiZuHeId);
			bu.setOppZuheId(oppoZuheId);
			bu.setOppActivateMiBaoCount(oppoActiveMiBaoCount);
			return bu;
		}
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
		resp.setOppActivateMiBaoCount(oppoActiveMiBaoCount);
		resp.setOppZuheId(oppoZuheId);
		// 添加到战斗队形缓存
		armyCache.put(jid, resp);
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

	public void setChallengeRanks(PvpBean bean, int myRank) {
		List<Integer> ranks = refreshChallengeRanks(myRank);
		setChallengeRanks(bean, ranks);
	}

	public List<Integer> getChallengeRanks(PvpBean bean, int junRank) {
		List<Integer> ranks = new ArrayList<Integer>();
		if (bean.rank1 == 0) {
			List<Integer> list = refreshChallengeRanks(junRank);
			setChallengeRanks(bean, list);
			HibernateUtil.save(bean);
			return list;
		}
		ranks.add(bean.rank1);
		ranks.add(bean.rank2);
		ranks.add(bean.rank3);
		ranks.add(bean.rank4);
		ranks.add(bean.rank5);
		ranks.add(bean.rank6);
		ranks.add(bean.rank7);
		ranks.add(bean.rank8);
		ranks.add(bean.rank9);
		ranks.add(bean.rank10);
		return ranks;
	}

	/*
	 * 
	 */
	public List<Integer> refreshChallengeRanks(int junZhuRank) {
		// 最低排名
		int maxRankValue = getMaxScore();
		List<Integer> ranks = new ArrayList<Integer>(10);
		if (maxRankValue == 0) {
			return ranks;
		}
		int[] A = new int[maxChoice];
		int[] B = new int[maxChoice];
		BaizhanPipei piPei = null;
		int k = 0;
		for (int i = 1; i < maxChoice; i++) {
			piPei = piPeiMap.get(i);
			A[i] = MathUtils.getMin(
					maxRankValue - piPei.paramY - piPei.paramQ,
					MathUtils.getMax(B[i - 1] + 1,
							(int) Math.floor(piPei.param1 * junZhuRank)));
			B[i] = MathUtils.getMin(
					maxRankValue - piPei.paramZ - piPei.paramQ,
					MathUtils.getMax(A[i] + piPei.paramX,
							(int) Math.floor(piPei.param2 * junZhuRank)));
			int number = MathUtils.getRandomInMax(A[i], B[i]);
			while (number == junZhuRank && k++ < 100) {
				number = MathUtils.getRandomInMax(A[i], B[i]);
			}
			if (number != 0 && number != junZhuRank) {
				ranks.add(number);
			}
		}
		return ranks;
	}

	// 获取军衔等级
	public BaiZhan getBaiZhanItemByRank(int rank) {
		for (BaiZhan bai : baiZhanMap.values()) {
			int min = bai.minRank;
			int max = bai.maxRank;
			if (rank >= min && rank <= max) {
				return bai;
			}
		}
		return baiZhanMap.get(1);
	}

	private int[] getSoldiers(long jId) {
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

	// 重新获取雇佣兵id
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

	/*
	// 兑换 按钮(每9点 或者 21点系统主动更新一次)
	public void getExchangeItemInfo(int id, Builder builder, IoSession session) {
		Date now = new Date();
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("玩家百战请求兑换出错：君主不存在");
			return;
		}
		PvpDuiHuanBean bean = HibernateUtil.find(PvpDuiHuanBean.class, jz.id);
		if (bean == null) {
			bean = initDuiHuanInfo(jz.id);
		} else {
			// 检查是否更新
			resetPvpDuiHuanBean(bean);
		}
		ExchageAwardReq.Builder req = (ExchageAwardReq.Builder) builder;
		ExchageAwardResp.Builder resp = ExchageAwardResp.newBuilder();
		int weiWa = bean.weiWang;
		int type = req.getType();
		int[] ids = null;
		/*
		 * type == 1: 花费元宝刷新商品兑换
		if (type == 1) {
			int needYB = getRefreshNeedWeiWang(bean);
			if (needYB > weiWa) {
				// 威望不足
				resp.setMsg(0);
				log.info("玩家id{},姓名 {}, 用威望刷新兑换列表失败：威望不足", jz.id, jz.name);
				session.write(resp.build());
				return;
			}
			ids = restDuihuanList(bean);
			saveChangeWeiWang(bean, -needYB);
			log.info("玩家id{},姓名 {}, 用威望刷新兑换列表成功花费威望", jz.id, jz.name, needYB);
			fillDuiHuanInfo(bean, resp, ids);
			session.write(resp.build());
			return;
		}
		/*
		 * type == 0: 请求商品兑换页面
		ids = stringToIntArr(bean.duiHuanId);
		if (ids == null || now.getTime() >= bean.nextDuiHuanTime.getTime()) {
			ids = restDuihuanList(bean);
			bean.nextDuiHuanTime = getNextNineTime(new Date());
			HibernateUtil.save(bean);
		}
		fillDuiHuanInfo(bean, resp, ids);
		session.write(resp.build());
	}
*/
	/*
	public void fillDuiHuanInfo(PvpDuiHuanBean bean,
			ExchageAwardResp.Builder resp, int[] ids) {
		resp.setWeiWang(bean.weiWang);
		resp.setNeedYB(getRefreshNeedWeiWang(bean));
		resp.setLeftTime(getLeftDuihuanTime(bean.nextDuiHuanTime, new Date()));
		resp.setMsg(1);
		DuiHuanInfo.Builder duihuan = null;
		int[] yes = stringToIntArr(bean.isBuy);
		if (yes == null || yes.length == 0) {
			yes = setIsBuy();
		}
		for (int i = 0; i < ids.length; i++) {
			duihuan = DuiHuanInfo.newBuilder();
			duihuan.setId(ids[i]);
			duihuan.setSite(duihuanMap.get(ids[i]).site);
			duihuan.setIsChange(yes[i] == 1 ? true : false);
			resp.addDuiHuanList(duihuan);
		}
	}
*/
	/*
	public void resetPvpDuiHuanBean(PvpDuiHuanBean bean) {
		//change 20150901
		if(DateUtils.isTimeToReset(bean.lastShowDuiHuanTime, CanShu.REFRESHTIME_PURCHASE)){
			bean.buyNumber = 0;
			bean.lastShowDuiHuanTime = new Date();
			HibernateUtil.save(bean);
		}
	}
*/
	/*
	private int getRefreshNeedWeiWang(PvpDuiHuanBean bean) {
		// 获取刷新兑换列表需要的威望
		int number = bean.buyNumber;
		number += 1;
		int wei = getYuanBao(number, PVPConstant.DUI_HUAN_REFRESH);
		return wei;
	}
*/
	/*
	private int[] restDuihuanList(PvpDuiHuanBean bean) {
		int[] ids = getDuiHuanId();
		bean.duiHuanId = intArrToString(ids);
		bean.isBuy = intArrToString(setIsBuy());
		return ids;
	}
*/
	/*
	private int[] getDuiHuanId() {
		int[] ids = new int[duiHuanNum];
		for (int i = 0; i < duiHuanNum; i++) {
			List<Duihuan> list = new ArrayList<Duihuan>();
			for (Duihuan e : duihuanMap.values()) {
				if (e.site == i + 1) {
					list.add(e);
				}
			}
			if (list.size() == 0) {
				log.error("百战兑换奖励位置是 {}的商品获取失败", i + 1);
				continue;
			}
			int seed = r.nextInt(10000);
			int sum = 0;
			int id = 0;
			for (Duihuan conf : list) {
				sum += conf.weight;
				if (seed < sum) {
					id = conf.id;
					break;
				}
			}
			ids[i] = id;
		}
		return ids;
	}
*/
	/*public void addChallengeChance(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("玩家购买百战次数出错：玩家不存在");
			return;
		}
		PvpBean bean = HibernateUtil.find(PvpBean.class, jz.id);
		if (bean == null) {
			log.error("玩家{}购买百战次数出错：百战没有开启", jz.id);
			return;
		} else {
			resetPvpBean(bean);
		}
		int[] data = getBuyADDChanceData(jz.vipLevel, bean.buyCount,
				VipData.bugBaizhanTime, PurchaseConstants.BAIZHAN);
		if (data == null) {
			log.error("玩家{}购买百战次数出错", jz.id);
			return;
		}
		AddChanceResp.Builder resp = AddChanceResp.newBuilder();
		int can = data[0];
		if (can == 0) {
			// 今日购买回数已经用完，无法再购买
			resp.setCan(false);
			session.write(resp.build());
			log.error("玩家：{}购买百战次数失败：当日可购买次数已用完", jz.id);
			return;
		}
		int count = data[1];
		int yuanbao = data[2];
		int allHuishu = data[3];
		int remain = allHuishu - bean.buyCount;
		resp.setLeft(remain);
		resp.setCount(count);
		resp.setCan(true);
		resp.setYuanbao(yuanbao);
		session.write(resp.build());
	}
*/
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
		int count = (int) pc.getNumber();
		int yuanbao = pc.getYuanbao();
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
		HibernateUtil.save(jz);
		JunZhuMgr.inst.sendMainInfo(session);
		return true;
	}

//	public void reveiveProduceAward(int id, Builder builder, IoSession session) {
//		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
//		if (jz == null) {
//			log.error("君主不存在");
//			return;
//		}
//		ReceiveAwardResp.Builder resp = ReceiveAwardResp.newBuilder();
////		PvpDuiHuanBean bean = HibernateUtil.find(PvpDuiHuanBean.class, jz.id);
////		if (bean == null) {
////			bean = initDuiHuanInfo(jz.id);
////			return;
////		}
//		// }else{
//		// resetPvpDuiHuanBean(bean);
//		// }
//		PvpBean pb = HibernateUtil.find(PvpBean.class, jz.id);
//		int myRank = DB.zscore_(KEY, "jun_" + jz.id);
//		resp.setJunXianLevel(pb.junXianLevel);
//		resp.setRank(myRank);
//		if (myRank > PVPConstant.TOTAL_NPC_COUNT) {
//			myRank = PVPConstant.TOTAL_NPC_COUNT;
//		}
//		BaiZhanRank bai = baiZhanRankMap.get(myRank);
//		if (bai == null) {
//			log.error("BaiZhanRank表中没有找到军衔等级是：{}的奖励配置信息", myRank);
//			return;
//		} else {
//			resp.setWeiWangHour(bai.weiwang);
//			// resp.setJinQianHour(bai.money);
//		}
//		int[] addA = getAccAward(new Date(), pb.lastCalculateAward, myRank,
//				jz.vipLevel, pb.lastGetAward);
//		resp.setWeiWang(addA[0] + pb.leiJiWeiWang);
//		// resp.setJinQian(addA[1] + bean.leiJiTongBi);
//		// 最多可以累计7天领奖
//		resp.setDays(7);
//		session.write(resp.build());
//	}

	private int[] getAccAward(Date now, Date lastCalculate, int rank,
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
		BaiZhanRank bai = baiZhanRankMap.get(rank);
		if (bai == null) {
			log.error("百战：BaiZhanRank中rank是{}的数据不存在", rank);
			return new int[2];
		} else
			return new int[] {
					(int) Math.floor(minutes * param * bai.weiwang / 60d), 0 };
		// (int)Math.floor(minutes *param * bai.money/60d)};
	}

	/**
	 * 
	 * @Title: getHour
	 * @Description:
	 * @param now
	 * @param last
	 * @return
	 */
	private int getMinutes(Date now, Date lastCalculate, Date lastget) {
		if (lastCalculate == null || lastget == null)
			return 0;
		long nowL = now.getTime();
		long lastCal = lastCalculate.getTime();
		long lastGe = lastget.getTime();

		long getJianGe = (nowL - lastGe) / 60000;
		long jiange = (nowL - lastCal) / 60000;
		long jiange2 = (lastCal - lastGe) / 60000;
		if (jiange2 < 0) {
			jiange2 = 0;
		}
		if (getJianGe >= 7 * 24 * 60) {
			int ho = (int) (7 * 24 * 60 - jiange2);
			return ho < 0 ? 0 : ho;
		}
		if (jiange < 60) {
			return (int) jiange;
		} else if (jiange >= 7 * 24 * 60) {
			return 7 * 24 * 60;
		} else {
			return (int) jiange;
		}
	}

	/**
	 * 获取君主的挑战对手信息
	 * 
	 * @Title: getChanllengeArmy
	 * @Description:
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getChanllengeArmy(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("百战千军挑战请求出错：君主不存在");
			return;
		}
		PvpBean bean = HibernateUtil.find(PvpBean.class, jz.id);
		if (bean == null) {
			log.error("百战千军挑战请求出错：百战数据不存在");
			return;
		}
		/*
		 * 是否重设数据
		 */
		resetPvpBean(bean);
		ChallengeReq.Builder req = (ChallengeReq.Builder) builder;

		// 需要改动 客户端发送的是某一玩家的排名
		long oppoId = req.getOppoJunZhuId();

		int youRank = getPvpRankById(oppoId);
		int myRank = getPvpRankById(jz.id);
		int oppoLe = 0;
		int opzhanli = 0;
		int oppoRoleId = 1;
		int oppoZuheId = -1;
		int oppoActiveMiBaoCount = 0;
		if (oppoId < 0) {
			BaiZhanNpc npc = npcs.get((int) -oppoId);
			if (npc != null) {
				oppoLe = npc.level;
				opzhanli = npc.power;
				oppoZuheId = npc.mibaoZuhe;
				oppoActiveMiBaoCount = npc.mibaoNum;
				oppoRoleId = npc.getRoleId((int) -oppoId);
			}
		} else {
			JunZhu oppo = HibernateUtil.find(JunZhu.class, oppoId);
			if (oppo != null) {
				oppoLe = oppo.level;
				opzhanli = getZhanli(oppo);
				PvpBean you = HibernateUtil.find(PvpBean.class, oppoId);
				oppoActiveMiBaoCount = MibaoMgr.inst.getActivateCountByZuheId(
						oppoId, you.zuheId);
				oppoZuheId = you.zuheId;
				oppoRoleId = oppo.roleId;
			}
		}
		ChallengeResp.Builder resp = ChallengeResp.newBuilder();
		fillChallengeResp(resp, jz.level, oppoLe, jz.id);
		resp.setOpenTime(System.currentTimeMillis());
		resp.setMyZhanli(getZhanli(jz));
		resp.setMyZuheId(bean.gongJiZuHeId);
		resp.setMyRank(myRank);
		resp.setOppoZhanli(opzhanli);
		resp.setOppoId(oppoId);
		resp.setOppoRank(youRank);
		resp.setOppoRoleId(oppoRoleId);
		resp.setOppActivateMiBaoCount(oppoActiveMiBaoCount);
		resp.setOppZuheId(oppoZuheId);
		// 添加到战斗队形缓存
		armyCache.put(jz.id, resp);
		session.write(resp.build());
	}

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
		PvpBean bean = HibernateUtil.find(PvpBean.class, jzId);
		if (bean == null) {
			log.error("玩家{}的百战没有开启:", jzId);
			return;
		}
		bean.zuheId = zuheId;
		HibernateUtil.save(bean);
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
			buyChallengeCount(jz, session, bean);
			break;
		case PVPConstant.CLEAN_CHALLENGE_CD:
			cleanChallengeTime(jz, session);
			break;
		case PVPConstant.GET_PRODUCE_AWARD:
			getProduceAward(jz, session, bean, myRank);
			break;
		case PVPConstant.REFRESH_RANKS:
			refreshEnemysByYuanbao(jz, bean, session, myRank);
			break;
		// 君主名次变了，刷新整个对手名次列表
		case PVPConstant.REFRESH_RANKS_FREE:
			refreshEnemysFree(bean, session, myRank);
			break;
		case PVPConstant.reget_ranks:
			regetEnemys(bean, session, myRank);
			break;
		}
	}

	public void refreshEnemysByYuanbao(JunZhu jz, PvpBean bean,
			IoSession session, int rank) {
		ConfirmExecuteResp.Builder resp = ConfirmExecuteResp.newBuilder();
		int pay = PurchaseMgr.inst.getNeedYuanBao(
				PurchaseConstants.BAIZHAN_REFRESH_ENEMYS,
				bean.todayRefEnemyTimes + 1);
		PayChangeInfo.Builder info = PayChangeInfo.newBuilder();
		boolean ok = isBuySuccess(jz, pay, session, YBType.YB_BUY_PVP_REFRESH, "PVP购买换一批");
		if (ok) {
			info.setSuccess(1);
			log.info("玩家id{},姓名 {}, 购买刷新对手列表们成功，花费元宝{}个", jz.id, jz.name, pay);
			bean.todayRefEnemyTimes += 1;
			List<Integer> tenRanks = refreshChallengeRanks(rank);
			setChallengeRanks(bean, tenRanks);
			HibernateUtil.save(bean);
			List<OpponentInfo.Builder> enemyBuilderList = fillEnemyListInfo(tenRanks);
			for (OpponentInfo.Builder b : enemyBuilderList) {
				info.addOppoList(b);
			}
			pay = PurchaseMgr.inst.getNeedYuanBao(
					PurchaseConstants.BAIZHAN_REFRESH_ENEMYS,
					bean.todayRefEnemyTimes + 1);
			info.setNextHuanYiPiYB(pay);
		} else {
			// 0是元宝不足
			info.setSuccess(0);
			log.info("玩家id{},姓名 {}, 购买刷新对手列表失败，元宝不足", jz.id, jz.name);
		}
		resp.setPayChangeInfo(info);
		session.write(resp.build());
	}

	public void refreshEnemysFree(PvpBean bean, IoSession session, int rank) {
		ConfirmExecuteResp.Builder resp = ConfirmExecuteResp.newBuilder();
		RefreshMyInfo.Builder info = RefreshMyInfo.newBuilder();
		List<Integer> tenRanks = refreshChallengeRanks(rank);
		setChallengeRanks(bean, tenRanks);
		HibernateUtil.save(bean);
		List<OpponentInfo.Builder> enemyBuilderList = fillEnemyListInfo(tenRanks);
		for (OpponentInfo.Builder b : enemyBuilderList) {
			info.addOppoList(b);
		}
		info.setJunZhuRank(rank);
		resp.setRefreshMyInfo(info);
		session.write(resp.build());
	}

	public void regetEnemys(PvpBean bean, IoSession session, int rank) {
		ConfirmExecuteResp.Builder resp = ConfirmExecuteResp.newBuilder();
		RefreshOtherInfo.Builder info = RefreshOtherInfo.newBuilder();
		List<Integer> tenRanks = getChallengeRanks(bean, rank);
		List<OpponentInfo.Builder> enemyBuilderList = fillEnemyListInfo(tenRanks);
		for (OpponentInfo.Builder b : enemyBuilderList) {
			info.addOppoList(b);
		}
		resp.setRefreshOtherInfo(info);
		session.write(resp.build());
	}

	private void buyChallengeCount(JunZhu jz, IoSession session, PvpBean bean) {
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
	private void cleanChallengeTime(JunZhu jz, IoSession session) {
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

	public void getProduceAward(JunZhu jz, IoSession session, PvpBean bean,
			int rank) {
		ConfirmExecuteResp.Builder resp = ConfirmExecuteResp.newBuilder();
		int[] addA = getAccAward(new Date(), bean.lastCalculateAward, rank,
				jz.vipLevel, bean.lastGetAward);
		int allWei = bean.leiJiWeiWang + addA[0];
		bean.lastCalculateAward = bean.lastGetAward = new Date();
		bean.leiJiWeiWang = 0;
		if(allWei > 0){ 
			bean.getProduceWeiWangTimes += 1;
		}
		HibernateUtil.save(bean);
		ShopMgr.inst.addMoney(ShopMgr.Money.weiWang, 
				ShopMgr.baizhan_shop_type, jz.id, allWei);
		GetAwardInfo.Builder info = GetAwardInfo.newBuilder();
		info.setSuccess(1);
		resp.setGetAwardInfo(info);
		session.write(resp.build());
		log.info("玩家id{},姓名 {}, 百战获取生产奖励, 威望:{}", jz.id, jz.name, allWei);
		// 主线任务: 领取一次威望 20190916
		if(allWei > 0){
			EventMgr.addEvent(ED.get_produce_weiWang , new Object[] { jz.id});
		}
	}

	// 战斗结束进行战斗数据的更新
	public void dealBaiZhanResult(int id, Builder builder, IoSession session) {
		long time = System.currentTimeMillis();
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
		armyCache.remove(jId);
		bingsCache.remove(jId);
		goingMap.remove(otherId);
		log.info("挑战者:{}和被挑战者：{}战斗结束，移出战斗map", jId, otherId);
		PvpBean bean = HibernateUtil.find(PvpBean.class, jId);
		if (bean == null) {
			log.error("百战战斗结束相关处理出错：百战无记录", jId);
			return;
		}
		int rank = getPvpRankById(jId);
		ActLog.log.Challenge(jz.id, jz.name, ActLog.vopenid, "OpposName", otherId, winId==jz.id?1:2, rank, rank);
		/*
		 * 输的直接返回
		 */
		if (winId == otherId) {
			BaiZhanResultResp.Builder resp = BaiZhanResultResp.newBuilder();
			resp.setOldRank(rank);
			resp.setNewRank(rank);
			resp.setHighest(bean.highestRank);
			resp.setOldJunXianLevel(bean.junXianLevel);
			resp.setNewJunXianLevel(bean.junXianLevel);
			session.write(resp.build());
			return;
		}
		// 是否重新设置数据
		resetPvpBean(bean);

		int oldJunXianLevel = bean.junXianLevel;
		/*
		 * 修改双方名次
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
				HibernateUtil.save(otherbean);
			}
		}

		// 君主信息
		int oldHighestRank = bean.highestRank;
		if (ranks[2] < bean.highestRank) {
			bean.highestRank = ranks[2];
			log.info("玩家：{}，挑战对手战斗胜利之后，最高名次变为:{}", jId, bean.highestRank);
			// 发送历史最高名次变化情况邮件
			sendEmailOfHighestRank(jz, bean.highestRank, oldHighestRank);
		}
		if(otherJun!=null){//可能击败了NPC
			npcName = otherJun.name;
		}
		EventMgr.addEvent(ED.BAI_ZHAN_RANK_UP, new Object[]{jz.name, npcName, ranks[2]});
		bean.allWin += 1;
		bean.winToday += 1;
		HibernateUtil.save(bean);

		// 记录一条战斗数据
		ZhanDouRecord zhanR = HibernateUtil
				.find(ZhanDouRecord.class, zhandouId);
		if (zhanR == null) {
			zhanR = new ZhanDouRecord(zhandouId, jId, otherId, new Date(),
					PVPConstant.GONG_JI_WIN, junRankChangeV, -junRankChangeV,
					bean.lastWeiWang, lostBuild);
		} else {
			zhanR.result1 = PVPConstant.GONG_JI_WIN;
			zhanR.junRankChangeV = junRankChangeV;
			zhanR.enemyRankChangeV = -junRankChangeV;
			zhanR.getWeiWang = bean.lastWeiWang;
			zhanR.lostBuild = lostBuild;
		}
		HibernateUtil.save(zhanR);
		log.error("数据库保存一场战斗：{}", zhandouId);

		/*
		 * 返回战斗之后等级变化情况
		 */
		BaiZhanResultResp.Builder resp = BaiZhanResultResp.newBuilder();
		resp.setOldRank(ranks[0]);
		resp.setNewRank(ranks[2]);
		resp.setHighest(bean.highestRank);
		resp.setOldJunXianLevel(oldJunXianLevel);
		resp.setNewJunXianLevel(bean.junXianLevel);
		session.write(resp.build());
		// // 主线任务： 完成N次百战（无论输赢）暂时没有开启20150519
		// EventMgr.addEvent(ED.FINISH_BAIZHAN_N, new Object[]{jId,
		// bean.allWin});
		// 主线任务： 百战达到名次N
		// if(ranks[2] == TaskData.bai_n_1 ||
		// ranks[2] == TaskData.bai_n_2 ||
		// ranks[2] == TaskData.bai_n_3){
		EventMgr.addEvent(ED.baizhan_rank_n, new Object[] { jId, ranks[2] });
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
		log.info("方法的执行时间是: " + (System.currentTimeMillis() - time));
		// 君主名次确定发生变化
		if(isJunChange){
			// 2015-7-22 刷新百战榜
			EventMgr.addEvent(ED.BAIZHAN_RANK_REFRESH,jz);
			// 2015-7-22 刷新君主榜
			EventMgr.addEvent(ED.JUN_RANK_REFRESH,jz);
		}
		// 对手名次确定发生变化
		if(isEnemyChange && otherJun != null){
			// 2015-8-26 刷新对手百战榜
			EventMgr.addEvent(ED.BAIZHAN_RANK_REFRESH,otherJun);
			// 2015-8-26 刷新对手君主榜
			EventMgr.addEvent(ED.JUN_RANK_REFRESH,otherJun);
		}
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
		jzPvp.allBattleTimes += 1;
		// 主动攻击不进行战斗记录提示
	//	jzPvp.isLook = false;
		HibernateUtil.save(jzPvp);
		// 每日任务中记录百战胜利一次
		// 策划需求修改为： 扣次数即为完成任务 20150831
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
				jz.id, DailyTaskConstants.baizhan_win_id, 1));
		goingMap.put(enemyId, System.currentTimeMillis());

		/*
		 * A挑战B，只有当B输且A和B是不同的联盟的时候，B才会扣掉联盟建设值，A不会增加。其他任何情况，和建设值都没有关系
		 */
		/*
		 * 当A挑战B， A输掉了，A什么也不得到什么也不损失，B也是 默认君主输
		 */
		long jId = jz.id;
		ZhanDouRecord zhanR = new ZhanDouRecord(zhandouId, jId, enemyId,
				new Date(), PVPConstant.GONG_JI_LOSE, 0, 0, 0, 0);
		HibernateUtil.save(zhanR);
		log.error("数据库保存一场战斗：{}", zhandouId);
		log.info("玩家：{},挑战对手：{}，记录进入战斗(默认记录是输场)", jId, enemyId);
		bingsCache.remove(jId);
		armyCache.remove(jId);
		if (enemyId > 0) {
			PvpBean eb = HibernateUtil.find(PvpBean.class, enemyId);
			if (eb != null) {
				eb.isLook = false;
				eb.allBattleTimes += 1;
				/*
				 * 默认对方赢
				 */
				eb.allWin += 1;
				eb.winToday += 1;
				HibernateUtil.save(eb);
			}
		}
		// 主线任务： 完成N次百战（无论输赢）
		EventMgr.addEvent(ED.FINISH_BAIZHAN_N, new Object[] { jId,
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
		ActLog.log.ChallengeAward(jz.id, jz.name, ActLog.vopenid, 2, values[0]);
	}

	//
	public void sendEmailOfHighestRank(JunZhu jz, int highestRank,
			int oldHighestRank) {
		if (highestRank > 5000) {
			log.error("玩家:{}最高名次：{}小于5000，所以没有奖励", jz.id, highestRank);
			return;
		}
		int oldYuanB = 0;
		// 发送历史最高名次变化情况邮件
		BaiZhanRank br = baiZhanRankMap.get(highestRank);
		if (oldHighestRank <= 5000) {
			BaiZhanRank old = baiZhanRankMap.get(oldHighestRank);
			if (old != null) {
				oldYuanB = old.yuanbao;
			}
		}
		if (br == null) {
			log.error("玩家:{}百战名次升到历史最高:{}，但是baizhanrank中没有对应名次配置奖励数据", jz.id,
					highestRank);
		} else {
			int awardYuanBao = br.yuanbao - oldYuanB;
			if (awardYuanBao <= 0) {
				log.error("玩家:{}百战名次升到历史最高:{}，奖励元宝小于等于0，所以不发放奖励", jz.id,
						highestRank);
				return;
			}
			String fujian = "0:900002:" + awardYuanBao;
			Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(40001);
			if (mailConfig == null) {
				log.error("mail.xml配置文件找不到type=40001的数据");
				return;
			}
			String content = mailConfig.content;
			String senderName = mailConfig.sender;
			boolean suc = EmailMgr.INSTANCE.sendMail(jz.name, content, fujian,
					senderName, mailConfig, "");
			if (suc) {
				log.info("玩家:{}百战获胜，名次升到历史最高:{}，发送邮件奖励(元宝)成功个数:{}", jz.id,
						highestRank, awardYuanBao);
			} else {
				log.error("玩家:{}百战历史名次最高奖励邮件发送失败", jz.id);
			}
		}
	}

	public boolean resetBeanAfterWin(long jId, PvpBean bean, int weiWang,
			int rank) {
		int beforLevel = bean.junXianLevel;
		bean.junXianLevel = getBaiZhanItemByRank(rank).jibie;
		if(beforLevel != bean.junXianLevel){
			// 君主的军衔值发生改变，贡金进行一次结算
			GuoJiaMgr.inst.getGongJin(jId, beforLevel);
			if(GuoJiaMgr.inst.isCanShangjiao(jId)){
				GuoJiaMgr.inst.pushCanShangjiao(jId);
			}
		}
		setChallengeRanks(bean, rank);
		return false;
	}
//
//	public boolean ChangeWeiWang(long jId, int weiWang) {
//		PvpDuiHuanBean duihuan = HibernateUtil.find(PvpDuiHuanBean.class, jId);
//		if (duihuan == null) {
//			duihuan = initDuiHuanInfo(jId);
//		} else {
//			resetPvpDuiHuanBean(duihuan);
//		}
//		boolean problem = false;
//		if (weiWang > 0) {
//			int allWei = duihuan.todayGetWeiWang;
//			int maxW = CanShu.BAIZHAN_WEIWANG_ADDLIMIT;
//			int value = allWei + weiWang - maxW;
//			if (value > 0) {
//				weiWang = maxW - allWei;
//				log.info("君主:{}的威望超过：{}, 实际上增加的威望值是：{}", jId, maxW, weiWang);
//				problem = true;
//				JunZhu junzhu = HibernateUtil.find(JunZhu.class, jId);
//				ProblemPlayer pp = new ProblemPlayer(jId, junzhu.name,
//						new Date(), "", "百战，当日获取到的威望总值大于系统每日获取限定值");
//				HibernateUtil.save(pp);
//			}
//			if (weiWang <= 0) {
//				return false;
//			}
//			duihuan.todayGetWeiWang += weiWang;
//		}
//		saveChangeWeiWang(duihuan, weiWang);
//		return problem;
//	}

//	public void saveChangeWeiWang(PvpDuiHuanBean duihuan, int changeWeiWang) {
//		// synchronized (weiWangLock) {
//		duihuan.weiWang += changeWeiWang;
//		if (duihuan.weiWang < 0) {
//			duihuan.weiWang = 0;
//		}
//		HibernateUtil.save(duihuan);
//		// }
//	}

	public void getPlayerState(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("百战千军出错：君主不存在");
			return;
		}
		PlayerStateReq.Builder req = (PlayerStateReq.Builder) builder;
		long enemyId = req.getEnemyId();
		int rank = req.getRank();
		int junRank = req.getJunRank();
		log.info("getPlayerState 敌人是: {} , 排名是 : {}",enemyId,rank);
		PlayerStateResp.Builder resq = PlayerStateResp.newBuilder();
		/*
		 * 判断挑战者的状态
		 */
		PvpBean bean = HibernateUtil.find(PvpBean.class, jz.id);
		if (bean == null) {
			log.error("百战千军挑战请求出错：百战数据不存在");
			resq.setType(6);
			session.write(resq.build());
			return;
		} else {
			resetPvpBean(bean);
		}
		// 1判断今日次数是不是够
		if (bean.remain <= 0) {
			log.error("百战千军挑战请求出错：今日百战挑战次数已经用完");
			resq.setType(4);
			session.write(resq.build());
			return;
		}
		// 2 判断cd时间到不到
		int timeCD = getCountDown(bean);
		if (timeCD > 0) {
			log.error("百战千军挑战请求出错：CD时间没有结束");
			resq.setType(5);
			session.write(resq.build());
			return;
		}
		int myRank = getPvpRankById(jz.id);
		if (junRank != myRank) {
			log.error("百战千军挑战请求出错:君主名次变化");
			log.info("myRank is :{} ", myRank);
			log.info("junRank is :{} ", junRank);
			resq.setType(8);
			session.write(resq.build());
			armyCache.remove(jz.id);
			return;
		}
		int dbRank2 = getPvpRankById(enemyId);
		if (dbRank2 != -1 && dbRank2 != rank) {
			log.error("百战千军挑战请求出错:对手名次变化");
			log.info("dbRank2 is :{} ", dbRank2);
			log.info("rank is :{} ", rank);
			resq.setType(7);
			session.write(resq.build());
			armyCache.remove(jz.id);
			return;
		}
		// type = 2 挑战中, 1 可以挑战
		Long yes = goingMap.get(enemyId);
		long now = System.currentTimeMillis() / 1000;
		if (yes != null) {
			long time = yes / 1000 + CanShu.MAXTIME_BAIZHAN + 60;
			if (now >= time) {
				goingMap.remove(enemyId);
				log.info("对手:{}百战中的状态是异常终止状态,现在可以参加战斗", enemyId);
				yes = null;
			}
		}
		if (yes == null) {
			resq.setType(1);
			log.info("被挑战者：{}百战中的状态是无战斗状态，可以进入挑战", enemyId);
		} else {
			resq.setType(2);
			log.info("被挑战者：{}百战是战斗状态，不可以进入挑战", enemyId);
		}
		session.write(resq.build());
	}

	private int getYuanBao(int buyCount, int type) {
		return PurchaseMgr.inst.getNeedYuanBao(type, buyCount);
	}

	@Override
	public void proc(Event event) {
		switch (event.id) {
		case ED.ACC_LOGIN:
			JunZhu junzhu = null;
			if (event.param != null && event.param instanceof JunZhu) {
				junzhu = (JunZhu) event.param;
				if (junzhu == null) {
					log.error("junzhu not exist");
					return;
				}
			}
			if (event.param != null && event.param instanceof Long) {
				long jzid = (Long) event.param;
				addDailyAward(jzid, PVPConstant.LOGIN_SEND_EMAIL);
			}
			break;
		case ED.REFRESH_TIME_WORK:
			// 对方被挑战发送红点通知
			IoSession session=(IoSession) event.param;
			if(session==null){
				break;
			}
			JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
			if(jz == null){
				break;
			}
			boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.baizhan, jz.id, jz.level);
			if(!isOpen){
				log.info("君主：{}--百战千军：{}的功能---未开启,被挑战不推送",jz.id,FunctionID.baizhan);
				break;
			}
			PvpBean bean = HibernateUtil.find(PvpBean.class, jz.id);
			if(bean != null && !bean.isLook){
				FunctionID.pushCanShangjiao(jz.id, session, FunctionID.baizhan);
			}
			break;
		}
	}

	/**
	 * // 玩家上线 检查玩家前一个21点的百战奖励是否发出 PvpMgr.inst.addDailyAward(sessoinId,
	 * PVPConstant.LOGIN_SEND_EMAIL);;
	 */
	@Override
	protected void doReg() {
		EventMgr.regist(ED.ACC_LOGIN, this);
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
	}

	private PvpBean initJunZhuPVPInfo(long jzId, int rank, int jzLevel) {
		// 获取数据库中是否有此记录，有的话什么也不做
		PvpBean bean = new PvpBean();
		bean.junZhuId = jzId;
		bean.usedTimes = 0;
		bean.remain = PVPConstant.ZHAN_TOTAL_TIMES;
		bean.junXianLevel = PVPConstant.XIAO_ZU_JI_BIE;
		bean.zuheId = -1;
		bean.gongJiZuHeId = -1;
		bean.highestRank = rank;
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
		bean.lastCalculateAward = bean.lastGetAward = new Date();
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
	private int getCountDown(PvpBean pvb) {
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

	public void addDailyAward(long id, byte sendType) {

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
		if (sendType == PVPConstant.LOGIN_SEND_EMAIL) {
			Date last = bean.lastAwardTime;
			if (last != null) {
				int remainTime = DateUtils.getTimeToNextNeedHour(last, 21);
				int time = (int) ((System.currentTimeMillis() - last.getTime()) / 1000);
				if (time < remainTime) {
					log.info("君主:{}上一个21点的百战每日奖励已经发出", id);
					return;
				}
			} else {
				int ho1 = DateUtils.getHourOfDay(bean.initPvpTime);
				Date now = new Date();
				int ho2 = DateUtils.getHourOfDay(now);
				boolean sameDay = DateUtils.isSameDay(bean.initPvpTime, now);
				if (sameDay && (ho2 < 21 || ho1 >= 21)) {
					log.error("玩家:{}是今天21点之前,开启，登陆，无奖励或者今天21点后开的百战，登陆，无奖励", junZhu.name);
					return;
				}
				//如果是相邻天
				if(ho1 >= 21 && ho2 < 21 && (now.getTime() - bean.initPvpTime.getTime()) < 12 * 3600 *1000 ){
					log.error("玩家:{}是昨天21点后开的百战，近日21点之前登陆游戏无昨天21点奖励", junZhu.name);
					return;
				}
			}
			// sendEmailTime = DateUtils.getLast21Time();
		}
		int rrr = getPvpRankById(junZhu.id);
		BaiZhan bai = getBaiZhanItemByRank(rrr);
		if (bai == null) {
			log.error("BaiZhan表中没有找到军衔等级是：{}的奖励配置信息", bean.junXianLevel);
			return;
		}
		StringBuilder dayAward = AwardMgr.inst.reStructAwardStr(bai.dayAward);
		// 百战奖励去掉铜币 20150625
		// int tong = bai.xishu;
		// JunzhuShengji ss =
		// JunZhuMgr.inst.getJunzhuShengjiByLevel(junZhu.level);
		// if(ss == null){
		// log.error("JunzhuShengji表中没有找到君主等级是：{}的配置信息", junZhu.level);
		// }else {
		// tong = tong * ss.xishu;
		// }
		// 附件 组成形式 type,itemId,count#type,itemId,count(类型，id,数量#类型，id,数量)
		// dayAward.append("#0:900001:" + tong);
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
			log.info("玩家:{}百战每日奖励邮件发出成功，奖励是:{}", junZhu.name, dayAward);
		} else {
			log.error("玩家:{}百战每日奖励邮件发送失败", junZhu.name);
		}
		bean.lastAwardTime = new Date();
		HibernateUtil.save(bean);
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
			fillNPCDataInfo(resp, session, enemys, npc, enemyFlagIndex,
					enemyTroop, npcId);
			oppolevel = npc.level;
			enemyTroop.setMaxLevel(999);
		} else {
			// 君主
			JunZhu enemy = HibernateUtil.find(JunZhu.class, enemyId);
			PvpBean ebean = HibernateUtil.find(PvpBean.class, enemyId);
			int zuheId = ebean == null ? -1 : ebean.zuheId;
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

		int zhandouId = zhandouIdMgr.incrementAndGet(); // 战斗id 后台使用
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
		selfTroop.setMaxLevel(BigSwitch.pveGuanQiaMgr
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
	 * 请求 劫镖 战斗数据
	 * 
	 * @param id
	 * @param session
	 * @param builder
	 */
//	public void YBDateInfoRequest(int id, IoSession session, Builder builder,
//			boolean isPVp) {
//		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
//		if (junZhu == null) {
//			log.error("君主不存在");
//			return;
//		}
//		PvpZhanDouInitReq.Builder req = (qxmobile.protobuf.ZhanDou.PvpZhanDouInitReq.Builder) builder;
//		long enemyId = req.getUserId();
//		log.info("recordChallenger : 敌人是: {}", enemyId);
//		ZhanDouInitError.Builder error = ZhanDouInitError.newBuilder();
//		YaBiaoInfo enemyBean = HibernateUtil.find(YaBiaoInfo.class, enemyId);
//		if (enemyBean == null) {
//			log.error("玩家挑战对手出错，对手:{}押镖没有开启", enemyId);
//			error.setResult("对手押镖功能没有开启");
//			session.write(error.build());
//			return;
//		}
//		AllianceBean selfAlliance = AllianceMgr.inst
//				.getAllianceByJunZid(junZhu.id);
//		AllianceBean enemyAlliance = AllianceMgr.inst
//				.getAllianceByJunZid(enemyId);
//		if (selfAlliance != null && enemyAlliance != null
//				&& selfAlliance.id == enemyAlliance.id) {
//			log.error("玩家挑战对手出错，请不要打劫自己的盟友{}:{}-{}:{}", junZhu.id,
//					selfAlliance.id, enemyId, enemyAlliance.id);
//			error.setResult("请不要打劫自己的盟友");
//			session.write(error.build());
//			return;
//		}
//		YBRobot ybrobot = (YBRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap
//				.get(enemyId);
//
//		synchronized (ybrobot) {
//			if (ybrobot.isBattle) {
//				log.error("玩家挑战对手出错，对手:{}已经在被打劫", enemyId);
//				error.setResult("对手已经在被打劫");
//				session.write(error.build());
//				return;
//			}
//			ybrobot.isBattle = true;
//			ybrobot.battleStart= System.currentTimeMillis();
//		}
//		int protectTime = ybrobot.protectCD
//				- ((int) (System.currentTimeMillis() - ybrobot.endBattleTime) / 1000);
//		if (protectTime > 0) {
//			log.error("玩家挑战对手出错，对手:{}还在押镖保护期间{}", enemyId, protectTime);
//			error.setResult("对手还在押镖保护期间");
//			session.write(error.build());
//			return;
//		}
//		// 更新劫镖次数
//		YaBiaoInfo selfBean = HibernateUtil.find(YaBiaoInfo.class, junZhu.id);
//		if (selfBean == null) {
//			log.error("玩家挑战对手出错，玩家:{}押镖没有开启", junZhu.id);
//			error.setResult("玩家押镖功能没有开启");
//			session.write(error.build());
//			return;
//		} else {
//			YabiaoMgr.inst.resetYBBean(selfBean, junZhu.vipLevel);
//		}
//		if (selfBean.remainJB == 0) {
//			log.error("玩家挑战对手出错，玩家剩余劫镖次数为:0-{}", junZhu.id);
//			error.setResult("玩家剩余劫镖次数为:0");
//			session.write(error.build());
//			return;
//		}
//		int distanceTime = BigSwitch.inst.ybMgr
//				.timeDistanceBySeconds(selfBean.lastJBDate);
//		if ((distanceTime > 0) && (CanShu.JIEBIAO_CD - distanceTime > 0)) {
//			log.error("玩家挑战对手出错，玩家还处于打劫冷却期内-{}：{}", junZhu.id,
//					CanShu.JIEBIAO_CD - distanceTime);
//			error.setResult("玩家还处于打劫冷却期内");
//			session.write(error.build());
//			return;
//		}
//		selfBean.usedJB += 1;
//		selfBean.remainJB -= 1;
//		selfBean.lastJBDate = new Date();
//		selfBean.historyJB += 1;// 劫镖历史次数+1
//		HibernateUtil.save(selfBean);
//		// 获取马车配置
////		CartTemp cart = YabiaoMgr.inst.cartMap.get(enemyBean.horseType);
//		log.info("君主:{} 向:{}发起劫镖，今日挑战次数加1,变为:{}，剩余次数减1，变为:{}，挑战时间是:{}",
//				junZhu.id, enemyId, selfBean.usedJB, selfBean.remainJB,
//				selfBean.lastJBDate);
//		// ybrobot.protectCD=cart.protectTime;//重置保护CD
//		log.info("劫镖者:{}和运镖者：{}进入战斗状态", junZhu.id, enemyId);
//		Scene sc = (Scene) session.getAttribute(SessionAttKey.Scene);
//		YabiaoMgr.inst.broadBattleEvent(sc, ybrobot, 20);
//		boolean isNpc = false;
//		ZhanDouInitResp.Builder resp = ZhanDouInitResp.newBuilder();
//		Group.Builder enemyTroop = Group.newBuilder();
//		List<Node> enemys = new ArrayList<ZhanDou.Node>();
//		int oppolevel = 0;
//		int enemyFlagIndex = 101;
//		JunZhu enemy = HibernateUtil.find(JunZhu.class, enemyId);
//		if (isNpc) {
//			// npc
//		} else {
//			// 押镖君主
//			int zuheId = enemyBean == null ? -1 : enemyBean.zuheId;
//			int hp = enemyBean.hp;
//			// 护盾
//			int hudun = enemyBean.hudun;
//			int hudunMax = enemyBean.hudunMax;
//			PveMgr.inst.fillYaBiaoJunZhuDataInfo4YB(resp, session, enemys,
//					enemy, enemyFlagIndex, zuheId, hp, hudun, hudunMax,
//					enemyTroop);
//			oppolevel = enemy.level;
//			enemyTroop.setMaxLevel(BigSwitch.pveGuanQiaMgr
//					.getGuanQiaMaxId(enemy.id));
//		}
//		long jId = enemy.id;
//		int jlevel = enemy.level;
//		// 加载押镖者雇佣兵
//		enemyFlagIndex += 1;
//		HashMap<Integer, Integer> ybNpc = (HashMap<Integer, Integer>) YabiaoMgr.inst.ybNpcMap
//				.get(enemyId);
//		setBingData4YaBiao(enemys, ybNpc, enemyFlagIndex, jId, jlevel,
//				oppolevel, (byte) ALL);
//		enemyTroop.addAllNodes(enemys);
//		resp.setEnemyTroop(enemyTroop);
//
////		int zhandouId = zhandouIdMgr.incrementAndGet(); // 战斗id 后台使用
//		int mapId = 0;
//		Group.Builder selfTroop = Group.newBuilder();
//		List<Node> selfs = new ArrayList<ZhanDou.Node>();
//		int zuheId = selfBean == null ? -1 : selfBean.gongJiZuHeId;
//		int selfFlagIndex = 1;
//		PveMgr.inst.fillJunZhuDataInfo(resp, session, selfs, junZhu,
//				selfFlagIndex, zuheId, selfTroop);
//		// 加载劫镖者雇佣兵
//		selfFlagIndex += 1;
//		int jiebiaoLevel = (jlevel + junZhu.level) / 2;
//		// 根据双方平均等级产生劫镖者的佣兵jId参数无用
//		setBingData(selfs, selfFlagIndex, jId, jiebiaoLevel, oppolevel,
//				(byte) 0);
//		selfTroop.addAllNodes(selfs);
//		selfTroop.setMaxLevel(BigSwitch.pveGuanQiaMgr
//				.getGuanQiaMaxId(junZhu.id));
//		resp.setSelfTroop(selfTroop);
//		resp.setZhandouId(1);//用不到默认 传1
//		resp.setMapId(mapId);
//		resp.setLimitTime(CanShu.MAXTIME_JIEBIAO);// 根据配置设置战斗时间
//		session.write(resp.build());
//		// 每日任务：完成一次劫镖活动
//		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
//				junZhu.id, DailyTaskConstants.jieBiao, 1));
//		// 主线任务完成:劫镖
//		EventMgr.addEvent(ED.finish_jiebiao_x, new Object[] { junZhu.id });
//
//	}

	private void fillNPCDataInfo(ZhanDouInitResp.Builder resp,
			IoSession session, List<Node> selfs, BaiZhanNpc npc, int flagIndex,
			Group.Builder troop, int npcId) {
		Node.Builder npcNode = Node.newBuilder();
		npcNode.addFlagIds(flagIndex);

		// 君主类型
		npcNode.setNodeType(NodeType.PLAYER);
		npcNode.setNodeProfession(NodeProfession.NULL);
		npcNode.setModleId(npc.getRoleId(npcId));
		PveMgr.inst.fillDataByGongjiType(npcNode, null);
		// type
		npcNode.setNodeType(NodeType.valueOf(npc.type));
		npcNode.setNodeProfession(NodeProfession.valueOf(npc.profession));
		npcNode.setNodeName(getNPCName(npc.name));
		PveMgr.inst.fillGongFangInfo(npcNode, npc);
		// 添加装备
		List<Integer> weaps = Arrays.asList(npc.weapon1, npc.weapon2,
				npc.weapon3);
		PveMgr.inst.fillZhuangbei(npcNode, weaps);
		// 添加秘宝信息
		List<Integer> skillIdList = MibaoMgr.inst.getBattleSkillIds(npc.mibaoZuhe, npc.mibaoNum);
		for(Integer skillId : skillIdList) {
			PveMgr.inst.addNodeSkill(npcNode, skillId);
		}
		npcNode.setHp(npcNode.getHpMax());
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
			YaBiaoInfo ybBean, int flagIndex, long jId, int mylevel,
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
			wjNode.setHp(bing.shengming);
			wjNode.setNodeName(getNPCName(bing.name));
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
			YaBiaoInfo ybBean, int flagIndex,
			List<GuYongBing> bingList) {
		HashMap<Integer, Integer> ybNpc = (HashMap<Integer, Integer>)  YabiaoMgr.inst.ybNpcMap.get(ybBean.junZhuId);
		CartTemp cart = YabiaoMgr.cartMap.get(ybBean.horseType);
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
				wjNode.setHp(shengming);
				wjNode.setNodeName(getNPCName(bing.name));
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

	public void refreshEnemyList(int id, IoSession session, Builder builder) {
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
		List<ZhanDouRecord> recordList = HibernateUtil.list(
				ZhanDouRecord.class, where);
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
				HibernateUtil.delete(reco);
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

//	public int getTodayBeChallengedTimes(long id) {
//		String where = "where enemyId =" + id + "  and  TO_DAYS(time)= "
//				+ "TO_DAYS(NOW()) and result1 = " + PVPConstant.GONG_JI_WIN;
//		int count = 0;
//		List<ZhanDouRecord> list = HibernateUtil.list(ZhanDouRecord.class,
//				where);
//		if (list != null) {
//			count = list.size();
//		}
//		return count;
//	}
/*
	public int receiveWeiWang(long enemyId) {
		int changeWeiWang = 0;
		int beFightTimes = getTodayBeChallengedTimes(enemyId);
		beFightTimes += 1;
		if (beFightTimes > getAwardTimes) {
			return 0;
		}
		if (enemyId < 0) {
			if (beFightTimes == 1) {
				changeWeiWang = (int) (CanShu.BAIZHAN_NPC_WEIWANG * CanShu.BAIZHAN_LVEDUO_K);
			} else {
				changeWeiWang = (int) ((beFightTimes - 1)
						* (1 - CanShu.BAIZHAN_LVEDUO_K)
						* CanShu.BAIZHAN_NPC_WEIWANG * CanShu.BAIZHAN_LVEDUO_K);
			}
		} else {
			PvpDuiHuanBean duihuan = HibernateUtil.find(PvpDuiHuanBean.class,
					enemyId);
			changeWeiWang = duihuan == null ? 0
					: (int) (duihuan.weiWang * CanShu.BAIZHAN_LVEDUO_K);
		}
		return changeWeiWang;
	}
*/
//	public int receiveJianShe(long enemyId) {
//		int beFightTimes = getTodayBeChallengedTimes(enemyId);
//		beFightTimes += 1;
//		if (beFightTimes > getAwardTimes || enemyId < 0) {
//			return 0;
//		}
//		return CanShu.BAIZHAN_LVEDUO_JIANSHEZHI;
//	}

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

	// public int getTodayAllGetWeiWang(long jId){
	// String where = "where junzhuId = " + jId + "  and  TO_DAYS(time)= "
	// + "TO_DAYS(NOW())";
	// List<ZhanDouRecord> list = HibernateUtil.list(ZhanDouRecord.class,
	// where);
	// for(ZhanDouRecord record: list){
	// log.info("这场战斗的时间是：{}，得到的威望是:{}", record.time, record.time);
	// }
	// return 0;
	// }
	public long removePvpRedisData(long jId) {
		long id = DB.zrem(KEY, "jun_" + jId);
		log.info("removePvpRedisData : 君主id:{}, 得到结果是:{}", jId, id);
		return id;
	}

	public String getNPCName(String key) {
		return HeroService.getNameById(key);
	}

	public int getPvpRankById(long junIdOrNpcId) {
		synchronized (pvpRankLock) {
			if (junIdOrNpcId <= 0) {
				return DB.zscore_(KEY, "npc_" + (-junIdOrNpcId));
			} else {
				return DB.zscore_(KEY, "jun_" + junIdOrNpcId);
			}
		}
	}

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
				if (score != 0) {
					DB.zadd(KEY, score + 1, "jun_" + addId);
				}
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

	/**
	 * @Title: getJunxian
	 * @Description: 获取军衔
	 * @param junZhu
	 * @return
	 * @return String
	 * @throws
	 */
	public String getJunxian(JunZhu junZhu) {
		PvpBean bean = HibernateUtil.find(PvpBean.class, junZhu.id);
		if (bean != null) {
			BaiZhan bz = baiZhanMap.get(bean.junXianLevel);
			String jxStr = bz == null ? "小卒" : HeroService.getNameById(bz.name);
			return jxStr;
		}
		return "小卒";
	}

	public static int getJunxianLevel(long jid) {
		PvpBean bean = HibernateUtil.find(PvpBean.class, jid);
		if (bean != null) {
			return bean.junXianLevel;
		}
		return -1;
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
//
//		case PD.ADD_CHANCE_REQ:
//			addChallengeChance(id, builder, session);
//			break;
		case PD.CHALLENGE_REQ:
			getChanllengeArmy(id, builder, session);
			break;
		case PD.CONFIRM_EXECUTE_REQ:
			doConfirmExecute(id, builder, session);
			break;
		case PD.BAIZHAN_RESULT:
			dealBaiZhanResult(id, builder, session);
			break;
		case PD.PLAYER_STATE_REQ:
			getPlayerState(id, builder, session);
			break;
		// 请求刷新挑战对手列表
		case PD.REFRESH_ENEMY_LIST_REQ:
			refreshEnemyList(id, session, builder);
			break;
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
}
