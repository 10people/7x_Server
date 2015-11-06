package com.qx.huangye;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.BattlePveResult.BattleResult;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.HuangYeProtos.ActiveTreasureReq;
import qxmobile.protobuf.HuangYeProtos.ActiveTreasureResp;
import qxmobile.protobuf.HuangYeProtos.DamageInfo;
import qxmobile.protobuf.HuangYeProtos.HYTreasureBattle;
import qxmobile.protobuf.HuangYeProtos.HYTreasureBattleResp;
import qxmobile.protobuf.HuangYeProtos.HuangYeTreasure;
import qxmobile.protobuf.HuangYeProtos.HyBuyBattleTimesResp;
import qxmobile.protobuf.HuangYeProtos.MaxDamageRankReq;
import qxmobile.protobuf.HuangYeProtos.MaxDamageRankResp;
import qxmobile.protobuf.HuangYeProtos.OpenHuangYeResp;
import qxmobile.protobuf.HuangYeProtos.OpenHuangYeTreasure;
import qxmobile.protobuf.HuangYeProtos.OpenHuangYeTreasureResp;
import qxmobile.protobuf.HuangYeProtos.TreasureNpcInfo;
import qxmobile.protobuf.ZhanDou;
import qxmobile.protobuf.ZhanDou.DroppenItem;
import qxmobile.protobuf.ZhanDou.Group;
import qxmobile.protobuf.ZhanDou.HYPveNpcInfo;
import qxmobile.protobuf.ZhanDou.HuangYePveOver;
import qxmobile.protobuf.ZhanDou.HuangYePveReq;
import qxmobile.protobuf.ZhanDou.Node;
import qxmobile.protobuf.ZhanDou.NodeProfession;
import qxmobile.protobuf.ZhanDou.NodeType;
import qxmobile.protobuf.ZhanDou.ZhanDouInitResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.EnemyTemp;
import com.manu.dynasty.template.FunctionOpen;
import com.manu.dynasty.template.GongjiType;
import com.manu.dynasty.template.HuangYe;
import com.manu.dynasty.template.HuangYeGuYongBing;
import com.manu.dynasty.template.HuangyeAward;
import com.manu.dynasty.template.HuangyeNpc;
import com.manu.dynasty.template.HuangyePve;
import com.manu.dynasty.template.HuangyePvpNpc;
import com.manu.dynasty.template.HuangyeRank;
import com.manu.dynasty.template.Mail;
import com.manu.dynasty.template.VipFuncOpen;
import com.manu.dynasty.template.YouxiaNpcTemp;
import com.manu.dynasty.util.DateUtils;
import com.manu.dynasty.util.MathUtils;
import com.manu.network.BigSwitch;
import com.qx.account.FunctionOpenMgr;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.award.AwardMgr;
import com.qx.battle.PveMgr;
import com.qx.email.EmailMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.huangye.shop.ShopMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.pve.PveRecord;
import com.qx.pvp.PvpMgr;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.vip.VipData;
import com.qx.vip.VipMgr;
import com.qx.yuanbao.YBType;

/**
 * 荒野求生
 * 
 * @author lizhaowen
 * x~0.99版本荒野玩法： 迷雾功能+资源点+藏宝点;
 * 1.0之后只有藏宝点相关玩法
 * 所以class中被注释掉的代码是1.0版本去掉的功能相关代码  change 20150907 by wangZhuan
 */
public class HYMgr {
	public static Logger logger = LoggerFactory.getLogger(HYMgr.class);
	public static HYMgr inst;
	public Map<Integer, List<HuangYe>> huangYeMapByFogId;
	public Map<Integer, HuangyeNpc> huangYeNpcMap;
//  去掉HuangYe.xml的加载 
//	public Map<Integer, HuangYe> huangYeMap;
//	public Map<Integer, HuangyeFog> huangyeFogMap;
	public static Map<Integer, HuangyePve> huangyePveMap;
	public static List<HuangyePve>  huangyePveList;
//	public Map<Integer, HuangyePvp> huangyePvpMap;
//	public Map<Integer, HuangyePvpNpc> huangyePvpNpcMap;
	public Map<Integer, HuangyeAward> huangyeAwardMap;
	public Map<Integer, HuangYeGuYongBing> huangyeGuYongBingMap;
	private Object treasureBattleLock = new Object();
//	private Object resourceBattleLock = new Object();
	private Object openTreasureLock = new Object(); // 藏宝点激活或者打开锁

	public static final int STATUS_NPC = 0;
	public static final int OPEN = 1;
	public static final int CLOSE = 0;
	public static String CACHE_HYSTORE_APPLY = "hystoreApply:";
	public static int TYPE_TREASURE = 0;//藏宝点
//	public static int TYPE_RESOURCE = 1;//资源点
	public static int TREASURE_DAY_TIMES = 2;//藏宝点每日可挑战的次数
//	public static int RESOURCE_DAY_TIMES = 3;//资源点每日可挑战的次数
	/*
	 * 联盟贡献 改为 荒野币 奖励 20150923 
	 * 900026： 荒野币id
	 */
	public static int LM_GONGXIAN_ITEMID = 900026;
	public static int FOG_OPEN_DEFAULT_ID = 0;
	/** 被其他联盟占领后，更换资源点等待时间，单位-天 */
	public static int CHANGE_RESURCE_INTERVAL = 3;
	public static int open_HY_lianMeng_level = 2;
	public static int huangYePve_first_pointId = 100001;  // hangyepve.xml第一个藏宝点id

	public static Map<Integer, HuangyeRank> huangyeRankMap = new HashMap<Integer, HuangyeRank>();
	public Map<Long, Map<Integer, List<AwardTemp>>> dropAwardMapBefore = new HashMap<Long, Map<Integer,List<AwardTemp>>>();

	public HYMgr() {
		inst = this;
		initData();
	}

	public void initData() {
//		List<HuangYe> listHY = TempletService.listAll(HuangYe.class.getSimpleName());
//		Map<Integer, List<HuangYe>> huangYeMapByFogId = new HashMap<Integer, List<HuangYe>>();
//		Map<Integer, HuangYe> huangYeMap = new HashMap<Integer, HuangYe>();
//		for (HuangYe hy : listHY) {
//			List<HuangYe> tempList = huangYeMapByFogId.get(hy.fogId);
//			if (tempList == null) {
//				tempList = new ArrayList<HuangYe>();
//				huangYeMapByFogId.put(hy.fogId, tempList);
//			}
//			tempList.add(hy);
//			huangYeMap.put(hy.id, hy);
//		}
//		this.huangYeMapByFogId = huangYeMapByFogId;
//		this.huangYeMap = huangYeMap;
		
		List<HuangyeNpc> listHYNpc = TempletService.listAll(HuangyeNpc.class.getSimpleName());
		Map<Integer, HuangyeNpc> huangYeNpcMap = new HashMap<Integer, HuangyeNpc>();
		for(HuangyeNpc hyNpc : listHYNpc) {
			huangYeNpcMap.put(hyNpc.id, hyNpc);
		}
		this.huangYeNpcMap = huangYeNpcMap;
		
/*		List<HuangyeFog> listHYFog = TempletService.listAll(HuangyeFog.class.getSimpleName());
		Map<Integer, HuangyeFog> huangyeFogMap = new HashMap<Integer, HuangyeFog>();
		for (HuangyeFog hyFog : listHYFog) {
			huangyeFogMap.put(hyFog.fogId, hyFog);
		}
		this.huangyeFogMap = huangyeFogMap;*/

		List<HuangyePve> listHYPve = TempletService.listAll(HuangyePve.class.getSimpleName());
		Map<Integer, HuangyePve> huangyePveMap = new HashMap<Integer, HuangyePve>();
		for (HuangyePve HYPve : listHYPve) {
			huangyePveMap.put(HYPve.id, HYPve);
		}
		huangyePveList = listHYPve;
		this.huangyePveMap = huangyePveMap;

//		List<HuangyePvp> listHYPvp = TempletService.listAll(HuangyePvp.class.getSimpleName());
//		Map<Integer, HuangyePvp> huangyePvpMap = new HashMap<Integer, HuangyePvp>();
//		for (HuangyePvp hyPvp : listHYPvp) {
//			huangyePvpMap.put(hyPvp.id, hyPvp);
//		}
//		this.huangyePvpMap = huangyePvpMap;

//		List<HuangyePvpNpc> listHYPvpNpc = TempletService.listAll(HuangyePvpNpc.class.getSimpleName());
//		Map<Integer, HuangyePvpNpc> huangyePvpNpcMap = new HashMap<Integer, HuangyePvpNpc>();
//		for (HuangyePvpNpc hyPvpNpc : listHYPvpNpc) {
//			huangyePvpNpcMap.put(hyPvpNpc.id, hyPvpNpc);
//		}
//		this.huangyePvpNpcMap = huangyePvpNpcMap;
//
//		List<HuangyeAward> listHYAward = TempletService.listAll(HuangyeAward.class.getSimpleName());
//		Map<Integer, HuangyeAward> huangyeAwardMap = new HashMap<Integer, HuangyeAward>();
//		for (HuangyeAward award : listHYAward) {
//			huangyeAwardMap.put(award.site, award);
//		}
//		this.huangyeAwardMap = huangyeAwardMap;
		
		List<HuangYeGuYongBing> listHYGuYongBing = TempletService.listAll(HuangYeGuYongBing.class.getSimpleName());
		Map<Integer, HuangYeGuYongBing> huangyeGuYongBingMap = new HashMap<Integer, HuangYeGuYongBing>();	
		for(HuangYeGuYongBing bing : listHYGuYongBing) {
			huangyeGuYongBingMap.put(bing.id, bing);
		}
		this.huangyeGuYongBingMap = huangyeGuYongBingMap;
		
		List<HuangyeRank> huangyeRankList =  TempletService.listAll(HuangyeRank.class.getSimpleName());
		for(HuangyeRank r: huangyeRankList){
			for(int rank = r.minRank; rank <= r.maxRank; rank++){
				huangyeRankMap.put(rank, r);
			}
		}
	}
	

	private void sendError(IoSession session, int cmd, String msg) {
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(cmd);
		test.setErrorDesc(msg);
		session.write(test.build());
	}

	public void openHuangYe(int cmd, IoSession session, Builder builder) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			logger.error("未找到君主，cmd:{}", cmd);
			return;
		}

		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
		// 注意 lianMengId <= 0表示没有加入或者退出联盟
		if (member == null || member.lianMengId <= 0) {
			sendError(session, cmd, "你还没有加入联盟");
			logger.error("君主:{} 未加入联盟，不能进行荒野求生", junzhu.name);
			return;
		}
		int lianMengId = member.lianMengId;
		AllianceBean alliance = HibernateUtil.find(AllianceBean.class, lianMengId);
		if (alliance == null) {
			sendError(session, cmd, "请求信息有误");
			logger.error("找不到联盟信息，不能进行荒野求生，联盟Id:{}", lianMengId);
			return;
		}
		if (alliance.level < open_HY_lianMeng_level) {
			sendError(session, cmd, "荒野求生未开放，需联盟等级达到2级");
			logger.error("联盟等级未达到2级，不能进行荒野求生，联盟Id:{}", lianMengId);
			return;
		}
		FunctionOpen o = FunctionOpenMgr.openMap.get(FunctionOpenMgr.HUANG_YE_QIU_SHENG);
		if(junzhu.level < o.lv) {
			sendError(session, cmd, "不能进入荒野求生，需等级达到" + o.lv+"级");
			logger.error("不能进入荒野求生，需等级达到"+ o.lv +"级");
			return;
		}
	
		List<HYTreasure> hyTreaList = HibernateUtil.list(HYTreasure.class,
				" where lianMengId=" + lianMengId);
		Map<Integer, HYTreasure> map = new HashMap<Integer, HYTreasure>();
		for(HYTreasure t: hyTreaList){
			map.put(t.idOfFile, t);
		}
	
		OpenHuangYeResp.Builder response = OpenHuangYeResp.newBuilder();
		HuangYeTreasure.Builder treaResp = null;
		for(HuangyePve hy: huangyePveList){
			treaResp = HuangYeTreasure.newBuilder();
			int idOfFile = hy.id;
			treaResp.setFileId(idOfFile);
			HYTreasure trDB = map.get(idOfFile);
			if(trDB != null){
				//1-可以被激活（没有激活状态）; 0-不可以被激活（没有激活状态），2-已经激活过了
				treaResp.setIsActive(2);
				treaResp.setIsOpen(trDB.isOpen() ? OPEN : CLOSE);
				treaResp.setJindu(trDB.progress);
				treaResp.setId(trDB.id);
			}else{
				if(idOfFile == huangYePve_first_pointId){
					treaResp.setIsActive(1);
				}else{
					HYTreasure trDBlast = map.get(idOfFile -1);
					// 上一关没有被激活或者没有通关过，下一关不可以被激活
					if(trDBlast == null || trDBlast.passTimes <= 0){
						treaResp.setIsActive(0);
					}else{
						// 可以被激活
						treaResp.setIsActive(1);
					}
				}
			}
			response.addTreasure(treaResp);
		}
		response.setHYMoney(ShopMgr.inst.getMoney(ShopMgr.Money.huangYeBi, junzhu.id, null));
		response.setAllianceBuild(alliance.build);

		HYTreasureTimes times = HibernateUtil.find(HYTreasureTimes.class, junzhu.id);
		if(times == null){
			times = new HYTreasureTimes();
			times.times = TREASURE_DAY_TIMES;
		}else{
			// 重置
			resetHYTreasureTimes(times, lianMengId);
		}
		response.setRemianTimes(times.times - times.used);
//		response.setAllTimes(times.times);
		response.setAllTimes(TREASURE_DAY_TIMES);

		VipFuncOpen vipData = VipMgr.vipFuncOpenTemp.get(VipData.can_buy_huagnye_times);
		int vipL = vipData == null? Integer.MAX_VALUE: vipData.needlv;
		if(junzhu.vipLevel < vipL){
			// vip等级不足，1-可买，发送下面信息，2-vip等级不够，不发，3-购买次数已经用尽，不发
			response.setBuyCiShuInfo(2);
		}else{
			// data{当日是否还有购买回数0-无，1-有, 购买可得次数, 购买需元宝，今日总可购买回数 }
			int[] data = getBuyADDChanceData(junzhu.vipLevel, 
					times.buyBattleHuiShu, VipData.buy_huangye_times, PurchaseConstants.BUY_HUANGYE_BATTLE);
			if(data == null || data[0] == 0){
				// 购买次数已经用尽
				response.setBuyCiShuInfo(3);
			}else{
				// 可以购买
				response.setBuyCiShuInfo(1);
				response.setBuyNextCiShu(data[1]);
				response.setBuyNextMoney(data[2]);
				response.setLeftBuyCiShu(data[3] - times.buyBattleHuiShu);
			}
		}
		session.write(response.build());

		/*
		 * 新荒野功能暂时不会用这些代码（这个方法） 20150902
		 */
//		HYFog huangyeFog = HibernateUtil.find(HYFog.class, lianMengId);
//		// 为null 表示还没初始化
//		if (huangyeFog == null) {
//			initHuangye(session, lianMengId, junzhu.id);
//		} else {
//			List<HYResourceAlliance> hyResIds = HibernateUtil.list(
//					HYResourceAlliance.class, " where lianMengId = " + lianMengId);
//			List<HYResource> hyResList = new ArrayList<HYResource>();
//			for (HYResourceAlliance ra : hyResIds) {
//				HYResource hyRes = HibernateUtil.find(HYResource.class, ra.resourceId);
//				if (hyRes != null) {
//					hyResList.add(hyRes);
//				}
//			}
//			List<HYTreasure> hyTreaList = HibernateUtil.list(HYTreasure.class,
//					" where lianMengId=" + lianMengId);
//			sendHuangYeInfo(session, huangyeFog, hyResList, hyTreaList, lianMengId);
//		}
	}

/*	private void initHuangye(IoSession session, int lianMengId, long junzhuId) {
		List<HYResource> initHyRes = new ArrayList<HYResource>();
		List<HYTreasure> initHyTrea = new ArrayList<HYTreasure>();
		HYTreasureRecord record = HibernateUtil.find(HYTreasureRecord.class, lianMengId);
		if(record == null) {
			record = new HYTreasureRecord();
			record.setLianMengId(lianMengId);
			record.setMaxLevel(0);
			HibernateUtil.insert(record);
		}
		int level = record.getMaxLevel();
		// 初始化荒野 云雾
		HYFog dbFog = new HYFog();
		dbFog.lianMengId = lianMengId;
		dbFog.addOpenFogId(FOG_OPEN_DEFAULT_ID);
		for(Map.Entry<Integer, HuangyeFog> entry : huangyeFogMap.entrySet()) {
			HuangyeFog fogCfg = entry.getValue();
			if(fogCfg.cangbaoLv <= level) {
				dbFog.addHaveFogId(fogCfg.fogId);
			}
		}
		HibernateUtil.insert(dbFog);

		for (Map.Entry<Integer, HuangYe> entry : huangYeMap.entrySet()) {
			HuangYe hyCfg = entry.getValue();
			if(hyCfg.fogId != FOG_OPEN_DEFAULT_ID) {	// fogId为0表示是默认开启的荒野点
				continue;
			}
			if (hyCfg.type == TYPE_TREASURE) {//藏宝点
				HYTreasure hyTreasure = insertHYTreasure(hyCfg.id, lianMengId);
				if(hyTreasure != null) {
					initHyTrea.add(hyTreasure);
				}
			} else if (hyCfg.type == TYPE_RESOURCE) {//资源点
				HYResource hyResource = insertHYResource(hyCfg.id, lianMengId);;
				if(hyResource != null) {
					initHyRes.add(hyResource);
				}
			} else {
				logger.error("荒野点类型不匹配，type:{}", hyCfg.type);
			}
		}
//		sendHuangYeInfo(session, dbFog, initHyRes, initHyTrea, lianMengId);
		sendHuangYeInfo(session, initHyTrea, lianMengId);
	}*/
	
/*	private HYTreasure insertHYTreasure(int fileId, int lianMengId) {
		HuangyePve hyPveCfg = huangyePveMap.get(fileId);
		if (hyPveCfg == null) {
			logger.error("huangyePve未找到匹配类型：id:{}", fileId);
			return null;
		}
		HYTreasure hyTreasure = new HYTreasure(lianMengId, fileId, hyPveCfg.maxTime);
		HibernateUtil.insert(hyTreasure);
		return hyTreasure;
		return null;
	}*/
	
/*	private HYResource insertHYResource(int fileId, int lianMengId) {
		HuangyePvp huangyePvp = huangyePvpMap.get(fileId);
		if (huangyePvp == null) {
			logger.error("huangyePvp未找到匹配类型：id:{}", fileId);
			return null;
		}
		HYResource hyResource = null;
		// 需要匹配联盟资源点
		if (huangyePvp.match == 1) {
			hyResource = matchHYResource(lianMengId, huangyePvp.npc, -1);
			if (hyResource != null) {
				hyResource.multiNums += 1;
				HibernateUtil.save(hyResource);
			}
		}
		// 匹配不成功生成新的资源点
		if (hyResource == null) {
			hyResource = new HYResource(huangyePvp.npc, 1);
			HibernateUtil.insert(hyResource);
		}

		HYResourceAlliance resAlnc = new HYResourceAlliance(hyResource.id, lianMengId, fileId);
		HibernateUtil.insert(resAlnc);
		return hyResource;
	}*/
	
/*	*//**
	 * 匹配资源点
	 * @param lianMengId
	 * @param levelMin		低等级，小于0则表示不进行该条件查询
	 * @param levelMax		高等级，小于0则表示不进行该条件查询
	 * @return
	 *//*
	private HYResource matchHYResource(int lianMengId, int levelMin, int levelMax) {
		List<HYResourceAlliance> hyResIds = HibernateUtil.list(
				HYResourceAlliance.class, " where lianMengId = " + lianMengId);
		int size = hyResIds.size();
		if(size == 0) {
			return null;
		}
		Object[] ids = new Object[size];
		for(int i = 0; i < size; i++) {
			ids[i] = hyResIds.get(i).resourceId; 
		}
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session s = sessionFactory.getCurrentSession();
		Transaction tr = s.beginTransaction();
		List<HYResource> list = new ArrayList<HYResource>();
		try {
			Criteria c = s.createCriteria(HYResource.class)
					.add(Restrictions.gt("curHoldId", 0))
					.add(Restrictions.eq("multiNums", 1))
					.add(Restrictions.not(Restrictions.in("id", ids)));
			if(levelMin >= 0) {
				c.add(Restrictions.ge("npcLevel", levelMin));
			}
			if(levelMax >= 0 && (levelMax > levelMin)) {
				c.add(Restrictions.le("npcLevel", levelMax));
			}
			if(levelMin >=0 || levelMax >= 0) {
				c.addOrder(Order.asc("npcLevel"));
			}
			c.setFirstResult(0);
			c.setMaxResults(1);
			list = c.list();
			tr.commit();
		} catch (Exception e) {
			tr.rollback();
			e.printStackTrace();
		}
		if (list.size() != 0) {
			return list.get(0);
		}
		return null;
	}
*/
/*	private void sendHuangYeInfo(IoSession session, //HYFog hyFog, List<HYResource> hyResList, 
			List<HYTreasure> hyTreaList,
			int lianMengId) {
		OpenHuangYeResp.Builder response = OpenHuangYeResp.newBuilder();
		for (Integer id : hyFog.getHaveFogIdSet()) {
			FogInfo.Builder fogInfo = FogInfo.newBuilder();
			fogInfo.setFileId(id);
			fogInfo.setStatus(hyFog.isFogOpen(id) ? 1 : 0);
			response.addFogInfo(fogInfo);
		}
		for (HYResource hyRes : hyResList) {
			response.addResource(getHYResourceBuilder(hyRes, hyFog.lianMengId));
		}

		for (HYTreasure hyTrea : hyTreaList) {
			response.addTreasure(getHYTreasureBuilder(hyTrea));
		}
		
		session.write(response.build());
	}*/
/*
	public void openFog(int cmd, IoSession session, Builder builder) {
		OpenFog.Builder request = (qxmobile.protobuf.HuangYeProtos.OpenFog.Builder) builder;
		int fogFileId = request.getFileId();

		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			logger.error("未找到君主，cmd:{}", cmd);
			return;
		}

		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
		if(member == null) {
			logger.error("未加入联盟，不能进行荒野求生1");
			return;
		}
		int lianmengId = member.lianMengId;
		AllianceBean alliance = HibernateUtil.find(AllianceBean.class, lianmengId);
		if (alliance == null) {
			sendError(session, cmd, "请求信息有误");
			logger.error("找不到联盟信息，不能进行荒野求生2，联盟Id:{}", lianmengId);
			return;
		}
		OpenFogResp.Builder response = OpenFogResp.newBuilder();
		int title = member.title;
		if(title != AllianceMgr.TITLE_LEADER && title != AllianceMgr.TITLE_DEPUTY_LEADER){
			response.setResult(2);
			session.write(response.build());
			logger.error("没有权限，不能打开云雾遮挡");
			return;
		}

		List<HuangYe> templateList = huangYeMapByFogId.get(fogFileId);
		if (templateList == null || templateList.size() == 0) {
			sendError(session, cmd, "操作失败，请稍候重试");
			logger.error("找不到对应的迷雾信息，fogId:{}", fogFileId);
			return;
		}

		HYFog hyFog = HibernateUtil.find(HYFog.class, lianmengId);
		if (hyFog == null || !hyFog.getHaveFogIdSet().contains(fogFileId)
				|| hyFog.getOpenFogIdSet().contains(fogFileId)) {
			response.setResult(3);
			session.write(response.build());
			logger.error("已经打开该迷雾。lianmengId:{},fogId:{}", lianmengId,
					fogFileId);
			return;
		}
		int openCost = huangyeFogMap.get(fogFileId).openCost;
		if (alliance.build < openCost) {
			response.setResult(1);
			session.write(response.build());
			logger.error("联盟建设值不足，不能开启迷雾,fogId:{}", fogFileId);
			return;
		}

		List<HYTreasure> openTreaList = new ArrayList<HYTreasure>();
		List<HYResource> openResList = new ArrayList<HYResource>();

		for (HuangYe hyCfg : templateList) {
			if (hyCfg.type == TYPE_TREASURE) {
				HYTreasure hyTreasure = insertHYTreasure(hyCfg.id, lianmengId);
				if(hyTreasure != null) {
					openTreaList.add(hyTreasure);
				}
			}
			// 资源点
			if (hyCfg.type == TYPE_RESOURCE) {
				HYResource hyResource = insertHYResource(hyCfg.id, lianmengId);
				if(hyResource != null) {
					openResList.add(hyResource);
				}
			}
		}
		hyFog.addOpenFogId(fogFileId);
		HibernateUtil.save(hyFog);

		response.setResult(0);
		response.setFileId(fogFileId);
		for (HYResource hyRes : openResList) {
			response.addResource(getHYResourceBuilder(hyRes, alliance.id));
		}

		for (HYTreasure hyTrea : openTreaList) {
			response.addTreasure(getHYTreasureBuilder(hyTrea));
		}
		alliance.build -= openCost;
		HibernateUtil.save(alliance);
		logger.info("君主:{}打开了联盟:{}迷雾:{},消耗建设值:{}", junzhu.name, alliance.name,
				fogFileId, openCost);
		session.write(response.build());
	}*/
/*
	private HuangYeResource.Builder getHYResourceBuilder(HYResource hyRes, int lianmengId) {
		HuangYeResource.Builder resResp = HuangYeResource.newBuilder();
		resResp.setId(hyRes.id);
		HYResourceAlliance resAlnc = HibernateUtil.find(HYResourceAlliance.class, 
				" where resourceId=" + hyRes.id + " and lianMengId =" + lianmengId);
		resResp.setFileId(resAlnc.idOfFile);
		if(hyRes.curHoldId == 0) {
			resResp.setStatus(0);
		} else if(hyRes.curHoldId == lianmengId) {
			resResp.setStatus(1);
		} else {
			resResp.setStatus(2);
		}
		String name = "";
		if(hyRes.curHoldId != 0) {
			AllianceBean alnc = HibernateUtil.find(AllianceBean.class, hyRes.curHoldId);
			name = alnc != null ? alnc.name : "";
		}
		resResp.setName(name);
		resResp.setLevel(hyRes.npcLevel);
		return resResp;
	}*/

/*	private HuangYeTreasure.Builder getHYTreasureBuilder(HYTreasure hyTrea) {
		HuangYeTreasure.Builder treaResp = HuangYeTreasure.newBuilder();
		treaResp.setId(hyTrea.id);
		treaResp.setFileId(hyTrea.idOfFile);
		treaResp.setIsOpen(hyTrea.isOpen() ? OPEN : CLOSE);
		treaResp.setJindu(hyTrea.progress);
		return treaResp;
	}*/
	
/*	
	public void reqRewardStore(int cmd, IoSession session, Builder builder) {
		ReqRewardStore.Builder request = (qxmobile.protobuf.HuangYeProtos.ReqRewardStore.Builder) builder;
		long lianmengId = request.getLianmengId();

		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			logger.error("未找到君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
		if (member == null) {
			sendError(session, cmd, "你还没有加入联盟");
			logger.error("君主:{} 未加入联盟，不能打开仓库1", junzhu.name);
			return;
		}
		AllianceBean alliance = HibernateUtil.find(AllianceBean.class, lianmengId);
		if (alliance == null || member.lianMengId != lianmengId) {
			sendError(session, cmd, "请求信息有误");
			logger.error("找不到联盟信息，不能打开仓库2，联盟Id:{}", member.lianMengId);
			return;
		}

		ReqRewardStoreResp.Builder response = ReqRewardStoreResp.newBuilder();

		List<HYRewardStore> storeList = HibernateUtil.list(HYRewardStore.class," where lianmengId =" + lianmengId);
		List<HuangyeAward> templeHYAwardList = TempletService.listAll(HuangyeAward.class.getSimpleName());
		for (HuangyeAward tepHYAward : templeHYAwardList) {
			HYRewardStore dbhyStore = matchHYRewardStore(storeList, tepHYAward.site);
			RewardItemInfo.Builder itemInfo = RewardItemInfo.newBuilder();
			itemInfo.setSite(tepHYAward.site);
			if (dbhyStore == null) {
				itemInfo.setNums(0);
			} else {
				itemInfo.setNums(dbhyStore.amount);
			}
			List<String> applyIdList = Redis.getInstance().lgetList(CACHE_HYSTORE_APPLY + lianmengId + "_" + tepHYAward.site);
			for (String id : applyIdList) {
				Long junzhuId = Long.parseLong(id);
				JunZhu jz = HibernateUtil.find(JunZhu.class, junzhuId.longValue());
				AlliancePlayer tempMember = HibernateUtil.find(AlliancePlayer.class, jz.id);
				ApplyerInfo.Builder applyInfo = ApplyerInfo.newBuilder();
				applyInfo.setJunzhuId(jz.id);
				applyInfo.setName(jz.name);
				applyInfo.setIconId(jz.roleId);
				applyInfo.setGongXian(tempMember.gongXian);
				itemInfo.addApplyerInfo(applyInfo.build());
			}
			response.addItemInfo(itemInfo.build());
		}
		session.write(response.build());
	}
*/
/*	private HYRewardStore matchHYRewardStore(List<HYRewardStore> storeList,
			int site) {
		for (HYRewardStore hyrs : storeList) {
			if (hyrs.site == site) {
				return hyrs;
			}
		}
		return null;
	}
*/
/*	public void applyReward(int cmd, IoSession session, Builder builder) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			logger.error("未找到君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class,
				junzhu.id);
		if (member == null) {
			sendError(session, cmd, "你还没有加入联盟");
			logger.error("君主:{} 未加入联盟，不能申请仓库奖励1", junzhu.name);
			return;
		}
		AllianceBean alliance = HibernateUtil.find(AllianceBean.class,
				member.lianMengId);
		if (alliance == null) {
			sendError(session, cmd, "请求信息有误");
			logger.error("找不到联盟信息，不能申请仓库奖励2，联盟Id:{}", member.lianMengId);
			return;
		}

		ApplyReward.Builder request = (qxmobile.protobuf.HuangYeProtos.ApplyReward.Builder) builder;
		int site = request.getSite();
		HYRewardStore rewardStore = HibernateUtil.find(HYRewardStore.class,
			 " where lianmengId =" + member.lianMengId + " and site=" + site);
		if (rewardStore == null) {
			rewardStore = new HYRewardStore(member.lianMengId, site, 0, new Date());
			HibernateUtil.save(rewardStore);
		}
		
		String siteCacheKey = CACHE_HYSTORE_APPLY + member.lianMengId + "_" + site;
		String jzCacheKey = CACHE_HYSTORE_APPLY + member.lianMengId + "_" + junzhu.id;
		Redis.getInstance().rpush_(siteCacheKey, ""+junzhu.id);
		logger.info("君主:{}申请联盟:{}奖励库site:{}的物品", junzhu.name, alliance.name, site);
		ApplyRewardResp.Builder response = ApplyRewardResp.newBuilder();
		ApplyerInfo.Builder applyerInfo = ApplyerInfo.newBuilder();
		applyerInfo.setGongXian(member.gongXian);
		applyerInfo.setIconId(junzhu.roleId);
		applyerInfo.setJunzhuId(junzhu.id);
		applyerInfo.setName(junzhu.name);
		response.setResult(0);
		response.setApplyerInfo(applyerInfo.build());
		response.setCurSite(site);
		// 移除之前申请的物品
		if(Redis.getInstance().exist_(jzCacheKey)) {
			int preSite = Integer.parseInt(Redis.getInstance().get(jzCacheKey));
			Redis.getInstance().lrem(CACHE_HYSTORE_APPLY + member.lianMengId + "_" + preSite, 0, junzhu.id+"");
			response.setPreSite(preSite);
		}
		session.write(response.build());
		Redis.getInstance().set(jzCacheKey, "" + site);
	}
*/
/*	public void cancelApplyReward(int cmd, IoSession session, Builder builder) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			logger.error("未找到君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class,
				junzhu.id);
		if (member == null) {
			sendError(session, cmd, "你还没有加入联盟");
			logger.error("君主:{} 未加入联盟，不能申请仓库奖励1", junzhu.name);
			return;
		}
		AllianceBean alliance = HibernateUtil.find(AllianceBean.class,
				member.lianMengId);
		if (alliance == null) {
			sendError(session, cmd, "请求信息有误");
			logger.error("找不到联盟信息，不能申请仓库奖励2，联盟Id:{}", member.lianMengId);
			return;
		}

		CancelApplyReward.Builder request = (qxmobile.protobuf.HuangYeProtos.CancelApplyReward.Builder) builder;
		int site = request.getSite();
//		HYRewardStore rewardStore = HibernateUtil
//				.find(HYRewardStore.class, " where lianmengId ="
//						+ member.lianMengId + " and site=" + site);
//		if (rewardStore == null) {
//			sendError(session, cmd, "奖励库中没有该物品");
//			return;
//		}
		Redis.getInstance().lrem(
				CACHE_HYSTORE_APPLY + member.lianMengId + "_" + site, 0,
				"" + junzhu.id);
		String jzCacheKey = CACHE_HYSTORE_APPLY + member.lianMengId + "_" + junzhu.id;
		Redis.getInstance().del(jzCacheKey);
		logger.info("君主:{}取消申请联盟:{}奖励库site:{}的物品", junzhu.name, alliance.name,
				site);
		CancelApplyRewardResp.Builder response = CancelApplyRewardResp.newBuilder();
		response.setResult(0);
		response.setJunzhuId(junzhu.id);
		response.setSite(site);
		session.write(response.build());
	}
	
	public void giveReward(int cmd, IoSession session, Builder builder) {
		GiveReward.Builder request = (qxmobile.protobuf.HuangYeProtos.GiveReward.Builder) builder;
		long reqJzId = request.getJunzhuId();
		int site = request.getSite();
		JunZhu reqJunzhu = HibernateUtil.find(JunZhu.class, reqJzId);
		if (reqJunzhu == null) {
			logger.error("未找到君主，cmd:{}", cmd);
			return;
		}
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			logger.error("未找到君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class,
				junzhu.id);
		if (member == null) {
			sendError(session, cmd, "你还没有加入联盟");
			logger.error("君主:{} 未加入联盟，不能执行奖励分配1", junzhu.name);
			return;
		}
		AllianceBean alliance = HibernateUtil.find(AllianceBean.class,
				member.lianMengId);
		if (alliance == null) {
			sendError(session, cmd, "请求信息有误");
			logger.error("找不到联盟信息，不能执行奖励分配2，联盟Id:{}", member.lianMengId);
			return;
		}

		GiveRewardResp.Builder response = GiveRewardResp.newBuilder();
		if (member.title != AllianceMgr.TITLE_LEADER) {
			response.setResult(1);
			session.write(response.build());
			logger.error("不是盟主，不能进行奖励分配操作，jinzhu:{},联盟:{}", junzhu.name,
					alliance.name);
			return;
		}

//		// 判断是否申请该物品		
//		String siteCacheKey = CACHE_HYSTORE_APPLY + member.lianMengId + "_" + site;
//		if(!Redis.getInstance().lexist(siteCacheKey, String.valueOf(reqJzId))) {
//			response.setResult(1);
//			session.write(response.build());
//			logger.error("君主:{}没有申请奖励库中的site:{}的物品", reqJunzhu.name, site);
//			return;
//		}

		HYRewardStore rewardStore = HibernateUtil.find(HYRewardStore.class, " where lianmengId ="
						+ member.lianMengId + " and site=" + site);
		if (rewardStore == null) {
			return;
		}
		if (rewardStore.amount < 1) {
			return;
		}
		HuangyeAward hyAward = huangyeAwardMap.get(site);
		if (hyAward == null) {
			logger.error("huangyeAward配置错误。找不到site:{}", site);
			return;
		}
		rewardStore.amount -= 1;
		HibernateUtil.save(rewardStore);
		
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(20001);
		boolean sendOK = false;
		if(mailConfig != null) {
			String fujian = hyAward.itemType + ":" + hyAward.itemId + ":" + 1;
			sendOK = EmailMgr.INSTANCE.sendMail(reqJunzhu.name, mailConfig.content, fujian, mailConfig.sender, mailConfig,"");
		}
		logger.info("奖励分配成功，并以邮件发送奖励, 结果:{}", sendOK);
		
		Redis.getInstance().lrem(
				CACHE_HYSTORE_APPLY + member.lianMengId + "_" + site, 0,
				"" + junzhu.id);
		response.setResult(0);
		response.setJunzhuId(reqJzId);
		response.setSite(site);
		response.setNums(rewardStore.amount);
		session.write(response.build());

	}
	*/
	
/*		 			资源点操作			
	
	*//**
	 * 资源点战前信息请求
	 * @param id
	 * @param session
	 * @param builder
	 *//*
	public void battleResouceReq(int id, IoSession session, Builder builder) {
		BattleResouceReq.Builder request = (qxmobile.protobuf.HuangYeProtos.BattleResouceReq.Builder) builder;
		long resourceId = request.getId();
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if(junzhu == null) {
			logger.error("找不到君主");
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
		if(member == null) {
			logger.error("不是联盟成员");
			return;
		}
		HYResource hyResource= HibernateUtil.find(HYResource.class, resourceId);
		if(hyResource == null) {
			logger.error("请求的资源点不存在hyResourceId:{}", resourceId);
			return;
		}
		
		HYResourceTimes resTimes = getResourceTimes(junzhu.id);
		BattleResouceResp.Builder response = BattleResouceResp.newBuilder();
		response.setResourceId(hyResource.id);
		response.setTimesOfDay(resTimes.times);
		response.setTotalTimes(RESOURCE_DAY_TIMES);
		int produce = (int) (CanShu.HUANGYEPVP_PRODUCE_P);
		response.setProduce(produce);
		
		int isChange = 0;//是否可以更换：0-不可以，1-可以;
		int changeRemainTime = 0;
		if(hyResource.curHoldId != 0 && hyResource.curHoldId != member.lianMengId) {
			Date date = new Date();
			if(hyResource.holdStartTime != null) {
				Date changeDate =
						org.apache.commons.lang.time.DateUtils.addDays(hyResource.holdStartTime, 3);
				if(!changeDate.after(date)) {
					isChange = 1;
				} else {
					changeRemainTime = (int) ((changeDate.getTime() - date.getTime()) / 1000);
				}
			}
		}
		response.setIsChange(isChange);
		response.setRemainTime(changeRemainTime);
		//我方秘宝id，MiBao配置文件中的id
		BuZhenHYPvp mibaoList = HibernateUtil.find(BuZhenHYPvp.class, junzhu.id);
		int zuheId = mibaoList == null ? -1 : mibaoList.zuheId;
		response.setZuheId(zuheId);
		
		//我方雇佣兵列表
		HYResourceJZYongbing jzYongbing = HibernateUtil.find(HYResourceJZYongbing.class, 
				" where junzhuId="+junzhu.id + " and resourceId=" + hyResource.id);
		if(jzYongbing == null) {
			int[] bingArray = getSoldiers(hyResource.npcLevel);
			if(bingArray.length != 4) {
				logger.error("获取的雇佣兵数量不为4");
				return;
			}
			jzYongbing = new HYResourceJZYongbing(member.lianMengId, resourceId, junzhu.id, 
					bingArray[0], bingArray[1], bingArray[2], bingArray[3]);
			HibernateUtil.insert(jzYongbing);
		}
		response.addYongBingList(jzYongbing.bing1);
		response.addYongBingList(jzYongbing.bing2);
		response.addYongBingList(jzYongbing.bing3);
		response.addYongBingList(jzYongbing.bing4);
		//npc信息
		List<HYResourceNpc> resourceNpcList = HibernateUtil.list(HYResourceNpc.class, " where resourceId=" + hyResource.id);
		if(resourceNpcList.size() == 0) {
			List<HuangyePvpNpc> npcList = getPvpNpcList(hyResource.npcLevel);
			for(HuangyePvpNpc npcCfg : npcList) {
				int[] bingArray = getSoldiers(npcCfg.level);
				HYResourceNpc resourceNpc = new HYResourceNpc(member.lianMengId, resourceId, npcCfg.id,
						0, bingArray[0], bingArray[1], bingArray[2], bingArray[3]);
				resourceNpcList.add(resourceNpc);
				HibernateUtil.save(resourceNpc);
			}
		}
		for(HYResourceNpc npc : resourceNpcList) {
			ResourceNpcInfo.Builder npcResp = ResourceNpcInfo.newBuilder();
			npcResp.setBossId(npc.bossId);
			npcResp.setBattleSuccess(npc.battleSuccess);
			npcResp.addYongBingId(npc.bing1);
			npcResp.addYongBingId(npc.bing2);
			npcResp.addYongBingId(npc.bing3);
			npcResp.addYongBingId(npc.bing4);
			String battleName = "";
			if(npc.battleJZId > 0) {
				JunZhu battleJZ = HibernateUtil.find(JunZhu.class, npc.battleJZId);
				if(AccountManager.getIoSession(npc.battleJZId) == null) {
					npc.battleJZId = 0;
					HibernateUtil.save(npc);
				} else {
					battleName = battleJZ.name;
				}
			} 
			npcResp.setBattleName(battleName);
			response.addResNpcInfo(npcResp.build());
		}
		session.write(response.build());
	}
	*/
/*	private List<HuangyePvpNpc> getPvpNpcList(int level) {
		List<HuangyePvpNpc> npcList = new ArrayList<HuangyePvpNpc>();
		for(Map.Entry<Integer, HuangyePvpNpc> entry : huangyePvpNpcMap.entrySet()) {
			HuangyePvpNpc npcCfg = entry.getValue();
			if(npcCfg.level == level) {
				npcList.add(npcCfg);
			}
		}
		return npcList;
	}*/
/*	
	public void pvpDataInfoReq(int id, IoSession session, Builder builder) {
		HuangYePvpReq.Builder request = (qxmobile.protobuf.ZhanDou.HuangYePvpReq.Builder) builder;
		long resourceId = request.getId();
		int bossId = request.getBossId();
		
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if(junzhu == null) {
			logger.error("找不到君主");
			PveMgr.inst.sendZhanDouInitError(session, "找不到君主");
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
		if(member == null || member.lianMengId <= 0) {
			logger.error("junzhu:{}还没有加入联盟", junzhu.name);
			PveMgr.inst.sendZhanDouInitError(session, "您还未加入联盟");
			return;
		}
		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, member.lianMengId);
		if(alncBean == null) {
			logger.error("DB中找不到id为:{}的联盟", member.lianMengId);
			PveMgr.inst.sendZhanDouInitError(session, "联盟信息有误");
			return;
		}
		int lianmengId = alncBean.id;
		HYResource hyResource = HibernateUtil.find(HYResource.class, resourceId);
		if(hyResource == null) {
			logger.error("请求的资源点不存在，hyResourceID:{}", resourceId);
			PveMgr.inst.sendZhanDouInitError(session, "请求的资源点信息有误1");
			return;
		}
		HYResourceTimes resTimes = getResourceTimes(junzhu.id);
		if(resTimes.times <= 0) { 
			logger.warn("今日资源点挑战次数已用完，resourceId:{}", hyResource.id);
			PveMgr.inst.sendZhanDouInitError(session, "今日资源点挑战次数已用完");
			return;
		}
		
		HYResourceAlliance resAlnc = HibernateUtil.find(HYResourceAlliance.class, 
				" where resourceId=" + resourceId + " and lianMengId =" + lianmengId);
		HuangyePvp hyPvpCfg = huangyePvpMap.get(resAlnc.idOfFile);
		if(hyPvpCfg == null) {
			logger.error("huangyePvp配置信息有误， huangyePvPiD:{}", resAlnc.idOfFile);
			PveMgr.inst.sendZhanDouInitError(session, "数据配置有误");
			return;
		}
		
		HYResourceNpc resourceNpc = null;
		synchronized(resourceBattleLock) {
			resourceNpc = HibernateUtil.find(HYResourceNpc.class, " where resourceId="+resourceId +" and bossId="+bossId);
			if(resourceNpc == null) {
				logger.error("找不到资源点npc信息。资源点id:{}", resourceId);
				PveMgr.inst.sendZhanDouInitError(session, "请求的资源点信息有误2");
				return;
			}
			if(resourceNpc.battleJZId > 0) {
				logger.info("资源点有人在挑战");
				PveMgr.inst.sendZhanDouInitError(session, "资源点有人在挑战，请稍后");
				return;
			} else {
				resourceNpc.battleJZId = junzhu.id;
				HibernateUtil.save(resourceNpc);
			}
		}
		if(resourceNpc.battleSuccess == 1) {
			logger.info("该boss已经被挑战成功");
			PveMgr.inst.sendZhanDouInitError(session, "该boss已经被挑战成功");
			return;
		}
		
		int zhandouId = PveMgr.battleIdMgr.incrementAndGet();    //战斗id 后台使用
		ZhanDouInitResp.Builder response = ZhanDouInitResp.newBuilder();
		response.setZhandouId(zhandouId);
		response.setMapId(hyPvpCfg.sceneId);
		response.setLimitTime(CanShu.MAXTIME_HUANGYE_PVP);
		//敌方数据
		List<HuangYeGuYongBing> npcBingList = getGuYongBingList(new int[]{resourceNpc.bing1, resourceNpc.bing2,
				resourceNpc.bing3, resourceNpc.bing4});
		int enemyFlag = 101;
		Group.Builder enemyTroop = Group.newBuilder();
		List<ZhanDou.Node> enemys = new ArrayList<ZhanDou.Node>();
		HuangyePvpNpc pvpNpc = huangyePvpNpcMap.get(resourceNpc.bossId);
		fillNpcJZDataInfo(response, enemys, enemyFlag, pvpNpc, enemyTroop);
		fillGuYongBingDataInfo(enemys, enemyFlag+1, npcBingList);
		enemyTroop.addAllNodes(enemys);
		enemyTroop.setMaxLevel(999);
		response.setEnemyTroop(enemyTroop);
		//我方数据
		HYResourceJZYongbing jzYongBing = HibernateUtil.find(HYResourceJZYongbing.class, " where resourceId="+resourceId +" and junzhuId="+junzhu.id);
		if(jzYongBing == null) {
			logger.error("找不到君主的荒野雇佣兵信息，resourceId:{},jzId:{}", resourceId, junzhu.id);
			return;
		}
		List<HuangYeGuYongBing> jzBingList = getGuYongBingList(new int[]{jzYongBing.bing1, jzYongBing.bing2,
				jzYongBing.bing3, jzYongBing.bing4});
		Group.Builder selfTroop = Group.newBuilder();
		List<Node> selfs = new ArrayList<Node>();
		BuZhenHYPvp buZhenHYPvp = HibernateUtil.find(BuZhenHYPvp.class, junzhu.id);
		int zuheId = buZhenHYPvp == null ? -1 : buZhenHYPvp.zuheId;
		PveMgr.inst.fillJunZhuDataInfo(response, session, selfs, junzhu, 1, zuheId, selfTroop);
		fillGuYongBingDataInfo(selfs, 2, jzBingList);
		selfTroop.setMaxLevel(BigSwitch.pveGuanQiaMgr.getGuanQiaMaxId(junzhu.id));
		selfTroop.addAllNodes(selfs);
		response.setSelfTroop(selfTroop);
		session.write(response.build());
	}
	
	*/
	public void fillGuYongBingDataInfo(List<Node> selfs, int flagIndex, List<HuangYeGuYongBing> bingList){ 
		Node.Builder wjNode = null;
		for(HuangYeGuYongBing bing : bingList){
			wjNode = Node.newBuilder();
			GongjiType gongjiType = PveMgr.inst.id2GongjiType.get(bing.gongjiType);
			PveMgr.inst.fillDataByGongjiType(wjNode, gongjiType);
			wjNode.addFlagIds(flagIndex++);
			wjNode.setModleId(bing.modelId);
			wjNode.setNodeType(NodeType.valueOf(bing.type));
			wjNode.setNodeProfession(NodeProfession.valueOf(bing.profession));
			wjNode.setHp(bing.shengming);
			wjNode.setNodeName(PvpMgr.inst.getNPCName(bing.name));
			PveMgr.inst.fillGongFangInfo(wjNode, bing);
			String skills = bing.skills;
			if(skills != null && !skills.equals("")){
				String[] skillList = skills.split(",");
				for (String s : skillList) {
					int skillId = Integer.parseInt(s);
					PveMgr.inst.addNodeSkill(wjNode, skillId);
				}
			}
			selfs.add(wjNode.build());
		}
	}
	
	public ArrayList<HuangYeGuYongBing> getGuYongBingList(int[] bingIds) {
		ArrayList<HuangYeGuYongBing> bingList = new ArrayList<HuangYeGuYongBing>(); 
		for (int i = 0; i < bingIds.length; i++){
			HuangYeGuYongBing bing = huangyeGuYongBingMap.get(bingIds[i]);
			if(bing == null) {
				logger.error("未发现雇佣兵，id:{}", bingIds[i]);
				continue;
			}
			int renshu = bing.renshu;
			for (int k = 0; k < renshu; k++){
				bingList.add(bing);
			}
		}
		return bingList;
	}
	
	// 重新获取雇佣兵id
	public int[] getSoldiers(int npcLevel){
		int index = PvpMgr.ALL;
		int[] zhiYes = new int[index];
		int zhiYe;
		int[] bings = new int[index];
		for(int i = 0 ; i < index;i++){
			zhiYe = MathUtils.getRandomInMax(PvpMgr.yongBingZhiYeMin, PvpMgr.yongBingZhiYeMax);
			zhiYes[i] = zhiYe;
		}
		for (HuangYeGuYongBing bing: huangyeGuYongBingMap.values()){
			for(int i =0; i < index; i++){
				if(bing.zhiye == zhiYes[i] && bing.needLv == npcLevel){
					bings[i] = bing.id;
				}
			}
		}
		logger.info("随机出来得到的雇佣兵是：" + Arrays.toString(bings));
		return bings;
	}
	
	public void fillNpcJZDataInfo(ZhanDouInitResp.Builder resp, 
			List<Node> selfs, int flagIndex, HuangyePvpNpc pvpNpc,
			Group.Builder enemyTroop){
		// 1 添加装备
		List<Integer> zbIdList = Arrays.asList(pvpNpc.weapon1, pvpNpc.weapon2, pvpNpc.weapon3);
		Node.Builder junzhuNode = Node.newBuilder();
		PveMgr.inst.fillZhuangbei(junzhuNode, zbIdList);
		// 添加flag
		junzhuNode.addFlagIds(flagIndex);
		// 添加君主基本信息（暴击、类型、读表类型、视野）
		junzhuNode.setNodeType(NodeType.PLAYER);
		junzhuNode.setNodeProfession(NodeProfession.NULL);
		junzhuNode.setModleId(pvpNpc.model);// 君主ModelId
		junzhuNode.setNodeName(pvpNpc.name+"");
		PveMgr.inst.fillDataByGongjiType(junzhuNode, null);
		PveMgr.inst.fillGongFangInfo(junzhuNode, pvpNpc);
		// 添加秘宝信息
		List<Integer> mibaoCfgIdList = Arrays.asList(pvpNpc.mibao1, pvpNpc.mibao2, pvpNpc.mibao3);
		PveMgr.inst.fillNpcMibaoDataInfo(mibaoCfgIdList, junzhuNode, enemyTroop);
		junzhuNode.setHp(junzhuNode.getHpMax());
		selfs.add(junzhuNode.build());
	}
	
/*	*//**
	 * 资源点战斗结束处理
	 * @param id
	 * @param session
	 * @param builder
	 *//*
	public void pvpOverProcess(int id, IoSession session, Builder builder) {
		HuangYePvpOver.Builder request = (qxmobile.protobuf.ZhanDou.HuangYePvpOver.Builder) builder;
		long resourceId = request.getId();
		int bossId = request.getBossId();
		int isPass = request.getIsPass();
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if(junzhu == null) {
			logger.error("找不到君主");
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
		if(member == null) {
			logger.error("未加入联盟，不能进行荒野求生1");
			return;
		}
		AllianceBean alliance = HibernateUtil.find(AllianceBean.class, member.lianMengId);
		if(alliance == null) {
			logger.error("找不到联盟:{}", member.lianMengId);
			return;
		}
	
		HYResource hyResource = HibernateUtil.find(HYResource.class, resourceId);
		if(hyResource == null) {
			logger.error("请求的资源点不存在，hyResourceID:{}", resourceId);
			return;
		}
		

		HYResourceAlliance resAlnc = HibernateUtil.find(HYResourceAlliance.class, 
				" where resourceId=" + resourceId + " and lianMengId =" + member.lianMengId);
		HuangyePvp hyPvpCfg = huangyePvpMap.get(resAlnc.idOfFile);
		if(hyPvpCfg == null) {
			logger.error("huangyePvp配置信息有误， huangyePvPId:{}", resAlnc.idOfFile);
			return;
		}
		
		//是否全部挑战成功
		boolean isAllSuccess = true;
		int totalGongxian = 0;
		int npcLevelFinal = hyResource.npcLevel;
		int levelChange = 0;
		List<HYResourceNpc> npcList = HibernateUtil.list(HYResourceNpc.class, " where resourceId="+resourceId);
		for(HYResourceNpc npc : npcList) {
			if(npc.bossId == bossId ) {
				if(isPass == 1) {//胜利
					npc.battleSuccess = 1;
					int getGongxian = (int) (CanShu.HUANGYEPVP_KILL_K);
					member.gongXian += getGongxian;
					totalGongxian += getGongxian;
					HibernateUtil.save(member);
					AllianceMgr.inst.changeGongXianRecord(junzhu.id, getGongxian);
				}
				npc.battleJZId = 0;
				HibernateUtil.save(npc);
			}
			if(npc.battleSuccess == 0) {
				isAllSuccess = false;
			}
		}
		Date date = new Date();
		if(isAllSuccess) {
			//  需要判断是否是挑战其他联盟占领的资源点  & 资源点产出计时
			if(hyResource.curHoldId > 0 && hyResource.curHoldId != member.lianMengId) {
				String name = "";
				HuangYe hyCfg = huangYeMap.get(hyPvpCfg.id);
				if(hyCfg == null) {
					logger.error("在huangye文件中找不到id:{}的配置", hyPvpCfg.id);
				}
				if(hyCfg != null) {
					name = HeroService.getNameById(String.valueOf(hyCfg.nameId));
				}
				Date lastAllotTime = hyResource.lastAllotTime;
				if(lastAllotTime == null) {
					lastAllotTime = hyResource.holdStartTime;
				}
				int hours = com.manu.dynasty.util.DateUtils.timeDistanceByHour(date, lastAllotTime);
				int getTongbi = (int) (hours * CanShu.HUANGYEPVP_PRODUCE_P);
				List<AlliancePlayer> memberAll = HibernateUtil.list(AlliancePlayer.class, " where lianMengId="+member.lianMengId);
				Mail mailConfig = null;
				String fujian = "";
				String content = "";
				boolean sendOK = false;
				if(getTongbi > 0) {
					mailConfig = EmailMgr.INSTANCE.getMailConfig(21002);
					fujian = 0 + ":" + 900001 + ":" + getTongbi;
					content = mailConfig.content.replace("XX", name);
				} else {
					mailConfig = EmailMgr.INSTANCE.getMailConfig(21003);
					content = mailConfig.content.replace("XX", name);
				}
				for(AlliancePlayer ap : memberAll) {
					JunZhu getJunzhu = HibernateUtil.find(JunZhu.class, ap.junzhuId);
					sendOK = EmailMgr.INSTANCE.sendMail(getJunzhu.name, content, fujian, mailConfig.sender, mailConfig,"");
					logger.info("资源点奖励，以邮件发送奖励, 结果:{}", sendOK);
				}
				
				String eventStr = AllianceMgr.inst.lianmengEventMap.get(13).str
						.replaceFirst("%d", name)
						.replaceFirst("%d", alliance.name);
				AllianceMgr.inst.addAllianceEvent(member.lianMengId, eventStr);
			}
			
			hyResource.holdStartTime = date;
			hyResource.curHoldId = member.lianMengId;
			hyResource.npcLevel += 1;
			npcLevelFinal += 1;
			levelChange = 1;
			List<HuangyePvpNpc> npcCfgList = getPvpNpcList(hyResource.npcLevel);
			int size = npcCfgList.size();
			for(int i = 0; i < size; i++) {
				HYResourceNpc resNpc = npcList.get(i);
				HuangyePvpNpc npcCfg = npcCfgList.get(i);
				int[] bingArray = getSoldiers(npcCfg.level);
				resNpc.battleSuccess = 0;
				resNpc.bossId = npcCfg.id;
				resNpc.bing1 = bingArray[0];
				resNpc.bing2 = bingArray[1];
				resNpc.bing3 = bingArray[2];
				resNpc.bing4 = bingArray[3];
				HibernateUtil.save(resNpc);
			}
		}
		
		HYResourceTimes resTimes = getResourceTimes(junzhu.id);
		resTimes.times -= 1;
		resTimes.lastChallengeTime = date;
		HibernateUtil.save(resTimes);
		HibernateUtil.save(hyResource);
		
		BattleResultHYPvp.Builder response = BattleResultHYPvp.newBuilder();
		response.setDkp(totalGongxian);
		response.setHeroLevel(npcLevelFinal);
		response.setSoldierLevel(npcLevelFinal);
		response.setIsLevelup(levelChange);
		session.write(response.build());
	}
	
	*/
	
	//************************ 藏宝点操作 **********************//
	/*
	 * 藏宝点被 盟主或者副盟主 打开（只有激活过以后 且 关闭状态下才能打开 ） 20150910
	 */
	public void openTreasurePoint(int cmd, IoSession session, Builder builder) {
		OpenHuangYeTreasure.Builder request = (qxmobile.protobuf.HuangYeProtos.OpenHuangYeTreasure.Builder) builder;
		// HYTreasure 表 id
		long id = request.getId();
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			logger.error("未找到君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
		if (member == null || member.lianMengId <= 0) {
			sendError(session, cmd, "你还没有加入联盟");
			logger.error("君主:{} 未加入联盟，不能开启藏宝点1", junzhu.name);
			return;
		}
		AllianceBean alliance = HibernateUtil.find(AllianceBean.class, member.lianMengId);
		if (alliance == null) {
			sendError(session, cmd, "请求信息有误");
			logger.error("找不到联盟信息，不能开启藏宝点2，联盟Id:{}", member.lianMengId);
			return;
		}
		if (member.title != AllianceMgr.TITLE_LEADER && member.title != AllianceMgr.TITLE_DEPUTY_LEADER) {
			sendError(session, cmd, "你的权限不够");
			logger.error("权限不够，不能开启藏宝点3，junzhuID:{}, 本人联盟职位是：{}", junzhu.id, member.title);
			return;
		}

		OpenHuangYeTreasureResp.Builder response = OpenHuangYeTreasureResp.newBuilder();
		HYTreasure hyTrea = null;
		synchronized (openTreasureLock) {
			hyTrea = HibernateUtil.find(HYTreasure.class, id);
			if (hyTrea == null) {
				// 0-成功，1-建设值不足, 2-还没有被激活过，3-已经开启了 
				response.setResult(2);
				session.write(response.build());
				logger.error("找不到藏宝点，Id:{}", id);
				return;
			}
			if(hyTrea.isOpen()) {
				response.setResult(3);
				session.write(response.build());
				logger.error("开启状态下的藏宝点，不能再被开启，Id:{}", id);
				return;
			}
			HuangyePve hyPveCfg = huangyePveMap.get(hyTrea.idOfFile);
			if(hyPveCfg == null){
				response.setResult(1);
				session.write(response.build());
				logger.error("找不到huangyepve的数值配置,idOfFile:{}", hyTrea.idOfFile);
				return;
			}
			int openCost = hyPveCfg.openCost;
			if (alliance.build < openCost) {
				response.setResult(1);
				session.write(response.build());
				logger.error("联盟：{}，建设值：{} 不足，不能打开藏宝点, idOfFile:{}",
						alliance.id, alliance.build, hyTrea.idOfFile);
				return;
			}
//			if(treasureNpcList == null || treasureNpcList.size() == 0) {
//				treasureNpcList = initTreasureNpc(hyTrea, hyPveCfg);
//			} else {
//				refreshHYTreasureNpcData(treasureNpcList);
//			}
			// 应该不会死锁
			AllianceMgr.inst.changeAlianceBuild(alliance, -openCost);
		
			//设置hyTrea
			hyTrea.progress = 100;
			hyTrea.openTime = new Date();
			HibernateUtil.save(hyTrea);
			
			//打开藏宝点初始化npc
			List<HYTreasureNpc> treasureNpcList = HibernateUtil.list(HYTreasureNpc.class, " where treasureId=" + hyTrea.id);
			if(treasureNpcList != null && treasureNpcList.size() != 0 ){
				refreshHYTreasureNpcData(treasureNpcList);
				logger.info("重新开启藏宝点：{}，重新设置npsList数据成功", hyTrea.id);
			}
			// 清除该关卡所有玩家的输出伤害数据
			List<HYTreasureDamage> damageList = HibernateUtil.list(
					HYTreasureDamage.class, " where treasureId = " + hyTrea.id);
			for(HYTreasureDamage damage : damageList) {
				logger.info("重新开启藏宝点：{}，则删除该藏宝点玩家：{}的伤害数据", hyTrea.id, damage.junzhuId);
				HibernateUtil.delete(damage);
			}
			
			logger.info("君主:{}开启了藏宝点:{},消耗建设值:{}，剩余联盟建设值", junzhu.name, id, openCost, alliance.build);
			response.setResult(0);
			response.setId(hyTrea.id);
			response.setBuildValue(alliance.build);
			session.write(response.build());
			// 向所有联盟成员发送邮件
			List<AlliancePlayer>  men = AllianceMgr.inst.getAllianceMembers(member.lianMengId);
			Mail f = EmailMgr.INSTANCE.getMailConfig(31001);
			String guanqiaN = HeroService.getNameById(hyPveCfg.nameId+"");
			String zhiWei = member.title == AllianceMgr.TITLE_LEADER?"盟主": "副盟主";
			String content = f.content.replace("AAA", guanqiaN).replace("N", zhiWei)
					.replace("XXX", junzhu.name);
			if(f != null){
				for(AlliancePlayer pla: men){
					if(pla.junzhuId == junzhu.id){
						continue;
					}
					JunZhu getj = HibernateUtil.find(JunZhu.class, pla.junzhuId);
					boolean sendOK = EmailMgr.INSTANCE.sendMail(getj.name, content,
								"", junzhu.name, f ,"");
					logger.info("藏宝点:{}被玩家：{}重置，向盟员：{}发送邮件通知, 结果:{}",hyTrea.id,
							junzhu.name, getj.name, sendOK);
				}
			}
			HuangyePve hyCfg = huangyePveMap.get(hyPveCfg.id);
			String treaName = HeroService.getNameById(hyCfg == null ? "" : hyCfg.nameId+"");
			String eventStr = AllianceMgr.inst.lianmengEventMap.get(11).str
					.replaceFirst("%d", junzhu.name)
					.replaceFirst("%d", treaName);
			AllianceMgr.inst.addAllianceEvent(alliance.id, eventStr);
		}
	}

	/**
	 * 盟主或者副盟主 激活藏宝点
	 * @param cmd
	 * @param session
	 * @param builder
	 * 20150902
	 */
	public void activeTreasurePoint(int cmd, IoSession session, Builder builder){
		ActiveTreasureReq.Builder request = (ActiveTreasureReq.Builder) builder;
		int idOfFile = request.getIdOfFile();
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			logger.error("未找到君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
		if (member == null || member.lianMengId <= 0) {
			sendError(session, cmd, "你还没有加入联盟");
			logger.error("君主:{} 未加入联盟，不能激活藏宝点1", junzhu.name);
			return;
		}
		AllianceBean alliance = HibernateUtil.find(AllianceBean.class, member.lianMengId);
		if (alliance == null) {
			sendError(session, cmd, "请求信息有误");
			logger.error("找不到联盟信息，不能激活藏宝点2，联盟Id:{}", member.lianMengId);
			return;
		}
		if (member.title != AllianceMgr.TITLE_LEADER && member.title != AllianceMgr.TITLE_DEPUTY_LEADER) {
			sendError(session, cmd, "你的权限不够");
			logger.error("权限不够，不能激活藏宝点3，junzhuID:{}, 君主的职位是：{}", junzhu.id, member.title);
			return;
		}

		ActiveTreasureResp.Builder response = ActiveTreasureResp.newBuilder();
		HYTreasure hyTrea = null;
		synchronized (openTreasureLock) {
			/*
			 * 判断上一关卡是否通关，否则不能激活 
			 */
			if(idOfFile > huangYePve_first_pointId){
				String where = "where lianMengId = " + member.lianMengId + 
						" and idOfFile = " + (idOfFile -1);
				HYTreasure lastTr = HibernateUtil.find(HYTreasure.class, where, false);
				if (lastTr == null || lastTr.passTimes <= 0) {
					//0-成功，1-建设值不足, 2-上一关没有通关，3-已经处于激活状态
					response.setResult(2);
					session.write(response.build());
					logger.error("上一关：{}从未通关过，所以不能激活本关卡，junzhuID{}， 联盟id：{}", 
							idOfFile -1, junzhu.id, member.lianMengId);
					return;
				}
			}
			String where = "where lianMengId = " + member.lianMengId + 
					" and idOfFile = " + idOfFile;
			hyTrea = HibernateUtil.find(HYTreasure.class, where, false);
			if (hyTrea != null) {
				response.setResult(3);
				session.write(response.build());
				logger.error("藏宝点已经处于激活状态，无需再次激活，junzhuid:{},idOfFile:{}, 联盟id：{}", 
						junzhu.id, idOfFile, member.lianMengId);
				return;
			}
			HuangyePve hyPveCfg = huangyePveMap.get(idOfFile);
			if(hyPveCfg == null){
				response.setResult(1);
				session.write(response.build());
				logger.error("找不到huangyepve的数值配置, 请程序员速速查看，idOfFile:{}", idOfFile);
				return;
			}
			if (alliance.build < hyPveCfg.openCost) {
				response.setResult(1);
				session.write(response.build());
				logger.error("联盟建设值不足，不能激活藏宝点,junzhuid:{},idOfFile:{}, 联盟id：{}", 
						junzhu.id, idOfFile, member.lianMengId);
				return;
			}
			int openCost = hyPveCfg.openCost;
			hyTrea = new HYTreasure(member.lianMengId, idOfFile);
			HibernateUtil.insert(hyTrea);
			/*
			 *  不用初始化藏宝点npc
			 */
//			//打开藏宝点初始化npc
//			List<HYTreasureNpc> treasureNpcList = HibernateUtil.list(HYTreasureNpc.class, " where treasureId=" + hyTrea.id);
//			if(treasureNpcList == null || treasureNpcList.size() == 0) {
//				treasureNpcList = initTreasureNpc(hyTrea, hyPveCfg);
//			} else {
//				refreshHYTreasureNpcData(treasureNpcList);
//			}
			// 应该不会死锁
			AllianceMgr.inst.changeAlianceBuild(alliance, -openCost);
	
			logger.info("君主:{}激活了藏宝点:{},消耗建设值:{}", junzhu.name, hyTrea.id, openCost);
			response.setResult(0);
			response.setId(hyTrea.id);
			response.setBuildValue(alliance.build);
			session.write(response.build());

			HuangyePve hyCfg = huangyePveMap.get(hyPveCfg.id);
			String treaName = HeroService.getNameById(hyCfg == null ? "" : hyCfg.nameId+"");
			String eventStr = AllianceMgr.inst.lianmengEventMap.get(11).str
					.replaceFirst("%d", junzhu.name)
					.replaceFirst("%d", treaName);
			AllianceMgr.inst.addAllianceEvent(alliance.id, eventStr);
		}
		
	}

	public void pveDataInfoReq(int id, IoSession session, Builder builder) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if(junzhu == null) {
			logger.error("找不到君主");
			PveMgr.inst.sendZhanDouInitError(session, "找不到君主");
			return;
		}
		HuangYePveReq.Builder request = (qxmobile.protobuf.ZhanDou.HuangYePveReq.Builder) builder;
		long treasureId = request.getId();

		HYTreasure hyTreasure = null;
		HuangyePve huangyePveCfg = null;
		HYTreasureTimes hyTimes = null;
		synchronized(treasureBattleLock) {
			hyTreasure = HibernateUtil.find(HYTreasure.class, treasureId);
			if(hyTreasure == null) {
				logger.error("请求的藏宝点没有被激活hyTreasureID:{}", treasureId);
				PveMgr.inst.sendZhanDouInitError(session, "请求的藏宝点信息有误");
				return;
			}
			if(!hyTreasure.isOpen()) {
				logger.info("藏宝点已关闭,hyTreasureID：{}", treasureId);
				PveMgr.inst.sendZhanDouInitError(session, "藏宝点已关闭");
				return;
			}
			if(hyTreasure.progress == 0) {
				logger.info("藏宝点已经通关,hyTreasureID：{}", treasureId);
				PveMgr.inst.sendZhanDouInitError(session, "藏宝点已通关");
				return;
			}
			// 是否有人正在挑战
			boolean has = hasSomeOneInBattle(hyTreasure);
			if(has) {
				logger.info("藏宝点有人在挑战");
				PveMgr.inst.sendZhanDouInitError(session, "藏宝点有人在挑战，请稍候");
				return;
			}
			// 配置是否有问题
			huangyePveCfg = huangyePveMap.get(hyTreasure.idOfFile);
			if(huangyePveCfg == null) {
				logger.error("huangyePve配置信息有误");
				PveMgr.inst.sendZhanDouInitError(session, "数据配置有误1");
				return;
			}
			// 是否是否挑战条件没问题
			int guanqiaId = huangyePveCfg.pveId;
			PveRecord r = HibernateUtil.find(PveRecord.class,
					"where guanQiaId=" + guanqiaId + " and uid=" + junzhu.id);
			if(r == null){
				logger.warn("玩家：{}荒野挑战藏宝点treasureId:{}, pve关卡id：{}没有通关，无法挑战",
						junzhu.id, hyTreasure.id, guanqiaId);
				PveMgr.inst.sendZhanDouInitError(session, "挑战条件不够：必须通关："+ guanqiaId);
				return;
			}
			// 挑战次数是否足够
			hyTimes = HibernateUtil.find(HYTreasureTimes.class, junzhu.id);
			if(hyTimes == null){
				hyTimes = new HYTreasureTimes(junzhu.id, TREASURE_DAY_TIMES, hyTreasure.lianMengId);
			}else{
				resetHYTreasureTimes(hyTimes, hyTreasure.lianMengId);
			}
			if(hyTimes.used >= hyTimes.times) { 
				logger.warn("今日藏宝点挑战次数已用完，treasureId:{}", hyTreasure.id);
				PveMgr.inst.sendZhanDouInitError(session, "今日藏宝点挑战次数已用完");
				return;
			}
			// 可以挑战，则 记录正在挑战君主id
			hyTreasure.battleJunzhuId = junzhu.id;
			hyTreasure.battleBeginTime = new Date();
			HibernateUtil.save(hyTreasure);
		}
		
		Map<Integer, List<AwardTemp>> npcDropAwardByPos = new HashMap<Integer, List<AwardTemp>>();
		int index = 0;//奖励列表index
		int zhandouId = PveMgr.battleIdMgr.incrementAndGet();    //战斗id 后台使用
		ZhanDouInitResp.Builder response = ZhanDouInitResp.newBuilder();
		response.setZhandouId(zhandouId);
		response.setMapId(huangyePveCfg.sceneId);
		response.setLimitTime(CanShu.MAXTIME_HUANGYE_PVE);
		// 填充敌方数据
		Group.Builder enemyTroop = Group.newBuilder();
		List<HYTreasureNpc> treasureNpcList = HibernateUtil.list(HYTreasureNpc.class, " where treasureId=" + hyTreasure.id);
		for (HYTreasureNpc npc : treasureNpcList) {
			if(npc.remainHp <= 0) {		//该npc已经死亡，不要再发给客户端
				continue;
			}
			HuangyeNpc hyNpcCfg = huangYeNpcMap.get(npc.npcId);
			if(hyNpcCfg == null) {
				logger.error("HuangyeNpc未发现npcid:{}的配置", npc.npcId);
				continue;
			}
			EnemyTemp enemyTemp = PveMgr.inst.id2Enemy.get(hyNpcCfg.enemyId);
			if(enemyTemp == null){
				logger.error("enemy未发现id:{}的配置", hyNpcCfg.enemyId);
				continue;
			}
			NodeType nodeType = NodeType.valueOf(hyNpcCfg.type);
			if(nodeType == null){
				logger.error("nodeType与enemyTemp的zhiye信息不一致，enemyTempId:{}", enemyTemp.getId());
				PveMgr.inst.sendZhanDouInitError(session, "数据配置有误2");
				continue;
			}

			Node.Builder node = Node.newBuilder();
			node.addFlagIds(hyNpcCfg.getPosition());
			node.setNodeType(nodeType);
			node.setNodeProfession(NodeProfession.valueOf(hyNpcCfg.profession));
			node.setModleId(hyNpcCfg.modelId);
			node.setHp(npc.remainHp);
			node.setNodeName(hyNpcCfg.name+"");
			GongjiType gongjiType = PveMgr.inst.id2GongjiType.get(hyNpcCfg.gongjiType);
			PveMgr.inst.fillDataByGongjiType(node, gongjiType);
			PveMgr.inst.fillGongFangInfo(node, enemyTemp);
			
			String skills = hyNpcCfg.skills;
			if(skills != null && !skills.equals("")){
				String[] skillList = skills.split(",");
				for (String s : skillList) {
					int skillId = Integer.parseInt(s);
					PveMgr.inst.addNodeSkill(node, skillId);
				}
			}
			List<AwardTemp> awardList = AwardMgr.inst.getHitAwardList(hyNpcCfg.award, ",", "=");
			npcDropAwardByPos.put(hyNpcCfg.position, awardList);
			int size = awardList.size();
			for (int i = 0; i < size; i++) {
				AwardTemp awardTemp = awardList.get(i);
				DroppenItem.Builder dropItem = DroppenItem.newBuilder();
				dropItem.setId(index);
				dropItem.setCommonItemId(awardTemp.getItemId());
				dropItem.setNum(awardTemp.getItemNum());
				node.addDroppenItems(dropItem);
				index++;
			}
			
			enemyTroop.addNodes(node);
		}
		dropAwardMapBefore.put(junzhu.id, npcDropAwardByPos);
		enemyTroop.setMaxLevel(999);
		response.setEnemyTroop(enemyTroop);
		
		// 填充己方数据（战斗数据和秘宝信息数据）
		Group.Builder selfTroop = Group.newBuilder();
		List<Node> selfs = new ArrayList<ZhanDou.Node>();
		BuZhenHYPve buZhenHYPve = HibernateUtil.find(BuZhenHYPve.class, junzhu.id);
		int zuheId = buZhenHYPve == null ? -1 : buZhenHYPve.zuheId;
		PveMgr.inst.fillJunZhuDataInfo(response, session, selfs, junzhu, 1, zuheId, selfTroop);
		selfTroop.setMaxLevel(BigSwitch.pveGuanQiaMgr.getGuanQiaMaxId(junzhu.id));
		selfTroop.addAllNodes(selfs);
		response.setSelfTroop(selfTroop);
		response.setHYK(huangyePveCfg.paraK);
		session.write(response.build());
		// 发送战斗数据结束，记录战斗数据
		dealPveBattleBefore(hyTimes, hyTreasure.id, junzhu.name );
	}
	public void dealPveBattleBefore(HYTreasureTimes hyTimes, long hyTreasureId, String junzhuname){
		// 已经用挑战次数+1
		hyTimes.used += 1;
		hyTimes.allBattleTimes += 1;
		HibernateUtil.save(hyTimes);
		logger.info("君主：{}打荒野， 发送战斗数据完成,今日已挑战次数是：{}", hyTimes.junzhuId, hyTimes.used);
		// 防止异常中断战斗，记录战斗伤害，默认0。
		String where = " where treasureId = " + hyTreasureId + 
				" and junzhuId = " + hyTimes.junzhuId;
		HYTreasureDamage treasureDamage = HibernateUtil.find(HYTreasureDamage.class, where);
		if(treasureDamage == null){
			treasureDamage = new HYTreasureDamage(hyTreasureId, hyTimes.junzhuId,0);
			HibernateUtil.save(treasureDamage);
			logger.info("君主：{}打荒野，新new HYTreasureDamage , hyTreasureId:{}", hyTimes.junzhuId, hyTreasureId);
		}
		// 主线任务: 输赢不计，攻打了任何一次荒野 20190916
		EventMgr.addEvent(ED.battle_huang_ye , new Object[] {hyTimes.junzhuId});
	}

	/**
	 * 点击藏宝点
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void battleTreasureReq(int id, IoSession session, Builder builder) {
		HYTreasureBattle.Builder request = (qxmobile.protobuf.HuangYeProtos.HYTreasureBattle.Builder) builder;
		long treasureId = request.getId();
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if(junzhu == null) {
			logger.error("点击藏宝点失败，找不到君主");
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
		if(member == null || member.lianMengId <= 0) {
			logger.error("点击藏宝点失败，不是联盟成员, junzhuid:{}", junzhu.id);
			return;
		}
		HYTreasure hyTreasure = HibernateUtil.find(HYTreasure.class, treasureId);
		if(hyTreasure == null) {
			logger.error("请求的藏宝点不存在hyTreasureID:{}", treasureId);
			return;
		}
		HuangyePve hyPveCfg = huangyePveMap.get(hyTreasure.idOfFile);
		if (hyPveCfg == null) {
			logger.error("huangyePve未找到匹配类型：id:{}", hyTreasure.idOfFile);
			return;
		}
		if(hyTreasure.progress <= 0){
			logger.error("藏宝点已经通关hyTreasureID:{}", treasureId);
			return;
		}
		HYTreasureBattleResp.Builder response = HYTreasureBattleResp.newBuilder();
		List<HYTreasureNpc> treasureNpcList = HibernateUtil.list(HYTreasureNpc.class, " where treasureId=" + hyTreasure.id);
		if(treasureNpcList == null || treasureNpcList.size() == 0){
			treasureNpcList = initTreasureNpc(hyTreasure, hyPveCfg);
		}
		fillTreasureNpcResp(response, treasureNpcList);
	
		response.setJindu(hyTreasure.progress);
		response.setRemainTime(getKuaiSuPassRemainTime(hyTreasure));
		// 挑战条件
		int guanqiaId = hyPveCfg.pveId;
		PveRecord r = HibernateUtil.find(PveRecord.class,
				"where guanQiaId=" + guanqiaId + " and uid=" + junzhu.id);
		if(r == null){
			response.setConditionIsOk(false);
		}else{
			response.setConditionIsOk(true);
		}
		// 是否有人正在挑战
		boolean has = hasSomeOneInBattle(hyTreasure);
		if(has) {
			JunZhu battleJZ = HibernateUtil.find(JunZhu.class, hyTreasure.battleJunzhuId);
			response.setBattleName(battleJZ.name);
			response.setStatus(1);
		} else {
			response.setStatus(0);
		}
		//我方秘宝id，MiBao配置文件中的id
		BuZhenHYPve mibaoList = HibernateUtil.find(BuZhenHYPve.class, junzhu.id);
		int zuheId = mibaoList == null ? -1 : mibaoList.zuheId;
		response.setZuheId(zuheId);

		HYTreasureTimes hyTimes = HibernateUtil.find(HYTreasureTimes.class, junzhu.id);
		if(hyTimes == null){
			hyTimes = new HYTreasureTimes();
			hyTimes.times = TREASURE_DAY_TIMES;
		}else{
			resetHYTreasureTimes(hyTimes, member.lianMengId);
		}
		response.setTimesOfDay(hyTimes.times - hyTimes.used);
//		response.setTotalTimes(hyTimes.times);
		response.setTotalTimes(TREASURE_DAY_TIMES);
		VipFuncOpen vipData = VipMgr.vipFuncOpenTemp.get(VipData.can_buy_huagnye_times);
		int vipL = vipData == null? Integer.MAX_VALUE: vipData.needlv;
		if(junzhu.vipLevel < vipL){
			// vip等级不足
			response.setBuyCiShuInfo(2);
		}else{
			int[] data = getBuyADDChanceData(junzhu.vipLevel, 
					hyTimes.buyBattleHuiShu, VipData.buy_huangye_times, PurchaseConstants.BUY_HUANGYE_BATTLE);
			if(data == null || data[0] == 0){
				// 购买次数已经用尽
				response.setBuyCiShuInfo(3);
			}else{
				// 可以购买
				response.setBuyCiShuInfo(1);
				response.setBuyNextCiShu(data[1]);
				response.setBuyNextMoney(data[2]);
				response.setLeftBuyCiShu(data[3] - hyTimes.buyBattleHuiShu);
			}
		}
		session.write(response.build());
	}

	/**
	 * 是否有人正在藏宝点正在挑战
	 * @param hyTreasure
	 * @return false: 没有， ture： 有人
	 */
	public boolean hasSomeOneInBattle(HYTreasure hyTreasure){
		if(hyTreasure.battleJunzhuId <= 0){
			return false;
		}
		Date d = hyTreasure.battleBeginTime;
		long now = System.currentTimeMillis();
		if(d == null || now > (d.getTime() + CanShu.MAXTIME_HUANGYE_PVE * 1000)){
			hyTreasure.battleJunzhuId = 0;
			HibernateUtil.save(hyTreasure);
			return false;
		}
		return true;
	}
	/*
	 * 返回单位是秒
	 */
	public int getKuaiSuPassRemainTime(HYTreasure hyTreasure){
		int time = CanShu.HUANGYEPVE_FASTCLEAR_TIME;
		if(hyTreasure == null || hyTreasure.openTime == null){
			return -1;
		}
		long passTime = System.currentTimeMillis() - hyTreasure.openTime.getTime();
		passTime = time * 1000 - passTime;
		if(passTime <= 0){
			return 0;
		}
		return (int)passTime/1000;
	}

	/**
	 * 荒野发送显示藏宝点怪物信息：只发送显示当前波次怪物
	 * @param response
	 * @param treasureNpcList
	 */
	public void fillTreasureNpcResp(HYTreasureBattleResp.Builder response,
			List<HYTreasureNpc> treasureNpcList) {
		int minBoci = 10; //假设10波次怪物
		for(HYTreasureNpc npc : treasureNpcList) {
			if(npc.remainHp > 0){
				minBoci = Math.min(npc.boCi, minBoci);
			}
		}
		// 只发送当前波次
		logger.info("当前显示波次是：{}", minBoci);
		for(HYTreasureNpc npc : treasureNpcList){
			HuangyeNpc hyNpcCfg = huangYeNpcMap.get(npc.npcId);
			if(hyNpcCfg == null) {
				logger.error("HuangyeNpc表未发现配置，HuangyeNpcId:{}", npc.npcId);
				continue;
			}
			if(hyNpcCfg.boCi != minBoci){
				continue;
			}
			EnemyTemp enemyTemp = PveMgr.inst.id2Enemy.get(hyNpcCfg.enemyId);
			if(enemyTemp == null) {
				logger.error("enemy表未发现配置，enemyId:{}", hyNpcCfg.enemyId);
				continue;
			}
			TreasureNpcInfo.Builder npcInfoResp = TreasureNpcInfo.newBuilder();
			npcInfoResp.setNpcId(npc.npcId);
			npcInfoResp.setRemainHP(npc.remainHp);
			npcInfoResp.setTotalHP(enemyTemp.getShengming());
			response.addNpcInfos(npcInfoResp);
		}
	}

	public void resetHYTreasureTimes(HYTreasureTimes hyTimes, int nowLianmengId) {
		if(hyTimes == null) {
			return ;
		}
		// 判断君主是否换过联盟，更新数据
		if(hyTimes.lianmengId != nowLianmengId){
			logger.info("君主：{}，换联盟，旧联盟id：{}，新联盟id：{}，因此HYTreasureTimes字段联盟id更新",
					hyTimes.junzhuId, hyTimes.lianmengId, nowLianmengId);
			HibernateUtil.delete(hyTimes);
			hyTimes =
					new HYTreasureTimes(hyTimes.junzhuId, TREASURE_DAY_TIMES, nowLianmengId);
			HibernateUtil.save(hyTimes);
			logger.info("君主：{}更换联盟，因此荒野挑战次数信息数据delete旧数据，并重新new数据", hyTimes.junzhuId);
		}
		// change 20150901
		if(DateUtils.isTimeToReset(hyTimes.lastResetTime, CanShu.REFRESHTIME_PURCHASE)){
			hyTimes.times = TREASURE_DAY_TIMES;
			hyTimes.lastResetTime = new Date();
			hyTimes.used = 0;
			hyTimes.buyBattleHuiShu = 0;
			HibernateUtil.save(hyTimes);
			logger.info("玩家：{}reset自己HYTreasureTimes对象成功，时间是：{}", hyTimes.junzhuId, new Date());
		}
	}
	
/*	private HYResourceTimes getResourceTimes(long junzhuId) {
		HYResourceTimes resTimes = HibernateUtil.find(HYResourceTimes.class, junzhuId);
		Date curDate = new Date();
		if(resTimes == null) {
			resTimes = new HYResourceTimes(junzhuId, RESOURCE_DAY_TIMES, curDate);
			HibernateUtil.insert(resTimes);
		} else {
			// change 20150901
			if(DateUtils.isTimeToReset(resTimes.lastChallengeTime, CanShu.REFRESHTIME_PURCHASE)){
				resTimes.times = RESOURCE_DAY_TIMES;
				HibernateUtil.save(resTimes);
			}
		}
		return resTimes;
	}*/

	public List<HYTreasureNpc> initTreasureNpc(HYTreasure hyTreasure,
			HuangyePve hyPveCfg) {
		List<HYTreasureNpc> treasureNpcList = new ArrayList<HYTreasureNpc>();
		//初始化npc信息 
		List<HuangyeNpc> npcCfgList = findHuangyeNpcFromConfig(hyPveCfg.npcId);
		for(HuangyeNpc npcCfg : npcCfgList) {
			EnemyTemp enemyCfg = PveMgr.inst.id2Enemy.get(npcCfg.enemyId);
			if(enemyCfg == null) {
				logger.error("未发现enemyId:{}的配置信息", npcCfg.enemyId);
				continue;
			}
			HYTreasureNpc treasureNpc = new HYTreasureNpc(npcCfg.position, 
					hyTreasure.id, npcCfg.id, enemyCfg.getShengming(), npcCfg.boCi); 
			HibernateUtil.insert(treasureNpc);
			treasureNpcList.add(treasureNpc);
		}
		return treasureNpcList;
	}
	
	private List<HuangyeNpc> findHuangyeNpcFromConfig(int npcId) {
		List<HuangyeNpc> npcCfgList = new ArrayList<HuangyeNpc>();
		for(Map.Entry<Integer, HuangyeNpc> entry : huangYeNpcMap.entrySet()) {
			HuangyeNpc npcCfg = entry.getValue();
			if(npcCfg.npcId == npcId) {
				npcCfgList.add(npcCfg);
			}
		}
		return npcCfgList;
	}

	public void pveOverProcess(int cmd, IoSession session, Builder builder) {
		HuangYePveOver.Builder request = (qxmobile.protobuf.ZhanDou.HuangYePveOver.Builder) builder;
		long treasureId = request.getId();
		int isPass = request.getIsPass();                  //是否通关(也是指是否击杀最后一个npc) :0-失败，1-成功
		int damageValue = request.getDamageValue();             //造成的伤害值
		List<HYPveNpcInfo> npcInfoList = request.getNpcInfosList();
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if(junzhu == null) {
			logger.error("找不到君主");
			return;
		}
		HYTreasure hyTreasure = HibernateUtil.find(HYTreasure.class, treasureId);
		if(hyTreasure == null) {
			logger.error("请求的藏宝点不存在hyTreasureID:{}", treasureId);
			return;
		}
		boolean hasLianmeng = true;
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
		if(member == null || member.lianMengId <= 0){
			// 玩家已不再联盟中
			hasLianmeng = false;
			logger.info("玩家：{}荒野战, treasureId:{}战斗结束，发现自己已不是联盟:{}成员，物是人非。", junzhu.id, treasureId, hyTreasure.lianMengId);
		}
		boolean lianmengOk = true;
		AllianceBean alli = HibernateUtil.find(AllianceBean.class, hyTreasure.lianMengId);
		if(alli == null){
			// 联盟被解散了
			lianmengOk = false;
			logger.info("玩家：{}荒野战, treasureId:{}战斗结束，发现联盟:{}已经解锁。", junzhu.id, treasureId, hyTreasure.lianMengId);
		}
		//1.更新玩家本次挑战全程总伤害值
		HYTreasureDamage treasureDamage = HibernateUtil.find(HYTreasureDamage.class, 
				" where treasureId="+hyTreasure.id+" and junzhuId="+junzhu.id);
		if(treasureDamage == null) {
			logger.error("服务器有错，进入战斗前已经new 并且记录了伤害记录, junzhuId:{}", junzhu.id );
		} else {
			if(hasLianmeng && lianmengOk){
				treasureDamage.damage += damageValue;
				int hi = treasureDamage.historyMaxDamage;
				if(damageValue > hi){
					treasureDamage.historyMaxDamage = damageValue;
					HibernateUtil.save(treasureDamage);
					logger.info("君主：{}荒野战,treasureId:{}的最大伤害是：{}", junzhu.id, 
							treasureId, treasureDamage.historyMaxDamage);
				}
			}else{
				HibernateUtil.delete(treasureDamage);
				logger.info("玩家：{}已经脱离联盟：{}，删除藏宝点:{}的伤害数据：{}", junzhu.id, hyTreasure.lianMengId,
						treasureId, treasureDamage.id);
			}
		}
		List<AwardTemp> getAwardList = new ArrayList<AwardTemp>();
		BattleResult.Builder response = BattleResult.newBuilder();
		//2. 更新npc血量 ， 计算获得铜币，掉落奖励。发现npc剩余血量为0，表示这个怪被打死，则要计算是否掉落物品
		int getTongbi = 0;
		Map<Integer, HYPveNpcInfo>  npcRemainHpMap = new HashMap<Integer, HYPveNpcInfo>();
		for(HYPveNpcInfo remainHpInfo: npcInfoList){
			npcRemainHpMap.put(remainHpInfo.getNpcId(), remainHpInfo);
		}
		List<HYTreasureNpc> treasureNpcList = HibernateUtil.list(HYTreasureNpc.class, " where treasureId=" + treasureId);
		for(HYTreasureNpc hyNpc : treasureNpcList) {
			HYPveNpcInfo npcInfo = npcRemainHpMap.get(hyNpc.position);
			if(npcInfo == null) {
				continue;
			}
			int curRemainHp = npcInfo.getRemainHP() <= 0 ? 0 : npcInfo.getRemainHP();
			int hurtValue = hyNpc.remainHp - curRemainHp;
			logger.info("荒野点：{}被玩家：{}攻打，npc：{}在被打之前血量是：{}，攻打之后血量是：{}",
					treasureId, junzhu.name, hyNpc.npcId, hyNpc.remainHp, curRemainHp);
			hurtValue = hurtValue < 0 ? 0 : hurtValue;
			getTongbi += hurtValue * CanShu.HUANGYEPVE_AWARD_X;
			hyNpc.remainHp = curRemainHp;
			HibernateUtil.save(hyNpc);
			if(curRemainHp > 0) {
				continue;
			}
			//退出联盟无其他奖励
			if(!hasLianmeng || !lianmengOk){
				continue;
			}
			// 击杀奖励
			HuangyeNpc hyNpcCfg = huangYeNpcMap.get(hyNpc.npcId);
			Map<Integer, List<AwardTemp>> npcDropAwardMap = dropAwardMapBefore.get(junzhu.id);
			List<AwardTemp> posNpcDropAward = npcDropAwardMap.get(hyNpcCfg.position);
			if(posNpcDropAward == null) {
				continue;
			}
			getAwardList.addAll(posNpcDropAward);
		}
		
		//判断是否挑战成功（必须联盟存在）
		if(isPass == 1 && hasLianmeng && lianmengOk) {//成功
			pveBattleSuccessProcess(session, junzhu, response, member, hyTreasure);
		} else {
			if(lianmengOk){
				hyTreasure.progress = getTreasureProgress(treasureNpcList);
				logger.info("藏宝点id:{}挑战未成功，剩余血量比:{}", hyTreasure.id, hyTreasure.progress);
			}
		}
		// 3 藏宝点战斗情况数据恢复
		hyTreasure.battleJunzhuId = 0;
		hyTreasure.battleBeginTime = null;
		HibernateUtil.save(hyTreasure);
		junzhu.tongBi += getTongbi;
		logger.info("玩家:{} , 打完荒野求生获取铜币：{}", junzhu.id,  getTongbi);
		HibernateUtil.save(junzhu);
		
		for(AwardTemp award : getAwardList) {
			if(award.getItemId() == AwardMgr.ITEM_TONGBI_ID) {
				getTongbi += award.getItemNum();
			}
			AwardMgr.inst.fillBattleAwardInfo(response, award);
		}
		response.setMoney(getTongbi);
		response.setExp(0);
		session.write(response.build());
		// 荒野战添加到每日任务中
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, 
				new DailyTaskCondition(junzhu.id, DailyTaskConstants.huangye_2_id, 1));
		for(AwardTemp award : getAwardList) {
			if(award.getItemId() == AwardMgr.ITEM_TONGBI_ID) {
				continue;
			}
			AwardMgr.inst.giveReward(session, award, junzhu, false);
		}
		JunZhuMgr.inst.sendMainInfo(session);
		dropAwardMapBefore.remove(junzhu.id);
	}

	private void pveBattleSuccessProcess(IoSession session, JunZhu junzhu, 
			BattleResult.Builder response,
			AlliancePlayer member,
			HYTreasure hyTreasure) {
		HuangyePve hyPveCfg = huangyePveMap.get(hyTreasure.idOfFile);
		//最后击杀奖励
		Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(20004);
		String fujian = "";
		boolean sendOK = false;
		if(mailConfig != null) {
			fujian = 0 + ":" + LM_GONGXIAN_ITEMID + ":" + hyPveCfg.killAward;
			sendOK = EmailMgr.INSTANCE.sendMail(junzhu.name, mailConfig.content, fujian, mailConfig.sender, mailConfig,"");
			logger.info("藏宝点最后击杀奖励，以邮件发送奖励, 结果:{}", sendOK);
		}
		// 快速通关
		boolean isFastPass = false;
		if(hyTreasure.openTime != null){
			int time = CanShu.HUANGYEPVE_FASTCLEAR_TIME;
			long passTime = System.currentTimeMillis() - hyTreasure.openTime.getTime();
			if(passTime <= time * 1000){
				isFastPass = true;
			}
		}
		//通关奖，有伤害记录均获得奖励
		String where = " where treasureId=" + hyTreasure.id;
		List<HYTreasureDamage> list = HibernateUtil.list(HYTreasureDamage.class, where);
		Collections.sort(list);
		int rank = 1;
		for(HYTreasureDamage d: list){
			JunZhu getJunzhu = HibernateUtil.find(JunZhu.class, d.junzhuId);
			mailConfig = EmailMgr.INSTANCE.getMailConfig(20002);
			if(mailConfig != null) {
				int awardId = getPassGuanQiaAward(rank++, hyTreasure.idOfFile);
				if(awardId != -1){
					fujian = 0 + ":" + LM_GONGXIAN_ITEMID + ":" + awardId;
					sendOK = EmailMgr.INSTANCE.sendMail(getJunzhu.name, mailConfig.content, fujian, mailConfig.sender, mailConfig,"");
					logger.info("藏宝点通关奖励，以邮件发送奖励, 结果:{}", sendOK);
				}
			}
			// 快速通关奖励
			if(isFastPass) { 
				mailConfig = EmailMgr.INSTANCE.getMailConfig(20003);
				if(mailConfig != null) {
					fujian = 0 + ":" + LM_GONGXIAN_ITEMID + ":" + hyPveCfg.fastAward;
					sendOK = EmailMgr.INSTANCE.sendMail(getJunzhu.name, mailConfig.content, fujian, mailConfig.sender, mailConfig,"");
					logger.info("藏宝点快速通关奖励，以邮件发送奖励, 结果:{}", sendOK);
				}
			}
		}
		logger.info("藏宝点id:{}挑战成功，分配奖励，并重置藏宝点信息");

		// 通关设置
		hyTreasure.openTime = null;
		hyTreasure.progress = 0;
		hyTreasure.passTimes += 1;
		
		HuangyePve hyCfg = huangyePveMap.get(hyPveCfg.id);
		String treaName = HeroService.getNameById(hyCfg == null ? "" : hyCfg.nameId+"");
		String eventStr = AllianceMgr.inst.lianmengEventMap.get(12).str
				.replaceFirst("%d", treaName);
		AllianceMgr.inst.addAllianceEvent(member.lianMengId, eventStr);
	}
	public int getPassGuanQiaAward(int rank, int pointId){
		HuangyePve hy = huangyePveMap.get(pointId);
		if(hy == null){
			return -1;
		}
		HuangyeRank r = huangyeRankMap.get(rank);
		if(r == null){
			return -1;
		}
		switch(r.id){
			case 1: return hy.rank1Award;
			case 2: return hy.rank2Award;
			case 3: return hy.rank3Award;
			case 4: return hy.rank4Award;
			case 5: return hy.rank5Award;
		}
		return -1;
	}

	private void refreshHYTreasureNpcData(List<HYTreasureNpc> treasureNpcList) {
		for(HYTreasureNpc treaNpc : treasureNpcList) {
			HuangyeNpc hyNpcCfg = huangYeNpcMap.get(treaNpc.npcId);
			if(hyNpcCfg == null) {
				logger.error("找不到huangyeNpc配置数据，npcId:{}", treaNpc.npcId);
				continue;
			}
			EnemyTemp enemyTemp = PveMgr.inst.id2Enemy.get(hyNpcCfg.enemyId);
			if(enemyTemp == null) {
				logger.error("找不到enemyTemp配置数据，enemyId:{} from HuangyeNpc, npcId:{}", hyNpcCfg.enemyId, treaNpc.npcId);
				continue;
			}
			treaNpc.remainHp = enemyTemp.getShengming();
			HibernateUtil.save(treaNpc);
		}
	}

/*	private List<HYTreasureDamage> getTopThreeDamage(long id) {
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session s = sessionFactory.getCurrentSession();
		Transaction tr = s.beginTransaction();
		List<HYTreasureDamage> list = new ArrayList<HYTreasureDamage>();
		try {
			Criteria c = s.createCriteria(HYTreasureDamage.class)
					.add(Restrictions.eq("treasureId", id))
					.addOrder(Order.desc("damage"));
			c.setFirstResult(0);
			c.setMaxResults(3);
			list = c.list();
			tr.commit();
		} catch (Exception e) {
			tr.rollback();
			e.printStackTrace();
		}
		if (list.size() != 0) {
			return list;
		}
		return list;
	}*/

/*	private void putAward2Store(int lianmengId, AwardTemp calcV) {
		for(Map.Entry<Integer, HuangyeAward> entry : huangyeAwardMap.entrySet()) {
			HuangyeAward awardCfg = entry.getValue();
			if(calcV.getItemId() == awardCfg.itemId) {
				HYRewardStore store = HibernateUtil.find(HYRewardStore.class, " where lianmengId="+lianmengId +" and site="+awardCfg.site);
				if(store == null) {
					store = new HYRewardStore(lianmengId, awardCfg.site, calcV.getItemNum(), new Date());
				} else{
					store.amount += calcV.getItemNum();
				}
				HibernateUtil.save(store);
				logger.info("荒野奖励库添加物品，type:{}, itemId:{},itemNum:{}", calcV.getItemType(), calcV.getItemId(), calcV.getItemNum());
			}
		}
	}*/

	private int getTreasureProgress(List<HYTreasureNpc> treasureNpcList) {
		int totalHP = 0;
		int remainHPTotal = 0;
		for(HYTreasureNpc npc : treasureNpcList) {
			HuangyeNpc hyNpcCfg = huangYeNpcMap.get(npc.npcId);
			if(hyNpcCfg == null) {
				logger.error("hyNpcCfg未发现npcid:{}的配置", npc.npcId);
				continue;
			}
			EnemyTemp enemyTemp = PveMgr.inst.id2Enemy.get(hyNpcCfg.enemyId);
			if(enemyTemp == null) {
				logger.error("ememyTemp未发现id:{}的配置", hyNpcCfg.enemyId);
				continue;
			}
			remainHPTotal += npc.remainHp;
			totalHP += enemyTemp.getShengming();
		}
		
		double progress = 1.0 * remainHPTotal / totalHP;
		BigDecimal b = new BigDecimal(progress); 
		if(remainHPTotal > totalHP/2) {
			b = b.setScale(2, BigDecimal.ROUND_DOWN);
		} else {
			b = b.setScale(2, BigDecimal.ROUND_CEILING);
		}
		progress = b.doubleValue() * 100;
		return (int) progress;
	}
	
/*	public void changeHYFogNum(int lianmengId, int treasureBattleLevel) {
		HYFog huangyeFog = HibernateUtil.find(HYFog.class, lianmengId);
		if(huangyeFog == null) {
			return;
		}
		for (Map.Entry<Integer, HuangyeFog> entry : huangyeFogMap.entrySet()) {
			HuangyeFog fogCfg = entry.getValue();
			if(fogCfg.cangbaoLv <= treasureBattleLevel) {
				huangyeFog.addHaveFogId(fogCfg.fogId);
			}
		}
		HibernateUtil.save(huangyeFog);
	}*/

	public void delHYInfo(int lianmengId) {
		HYFog huangyeFog = HibernateUtil.find(HYFog.class, lianmengId);
		List<HYTreasure> hyTreasureList = HibernateUtil.list(HYTreasure.class, " where lianMengId="+lianmengId);
		List<HYTreasureTimes> hyTreasureTimesList = HibernateUtil.list(HYTreasureTimes.class, " where lianmengId="+lianmengId);
		List<HYResourceAlliance> hyResAlncList = HibernateUtil.list(HYResourceAlliance.class, " where lianMengId="+lianmengId);
		if(huangyeFog != null) {
			HibernateUtil.delete(huangyeFog);
		}
		for(HYTreasure hyTrea : hyTreasureList) {
			HibernateUtil.delete(hyTrea);
			List<HYTreasureNpc> npcList = HibernateUtil.list(HYTreasureNpc.class, " where treasureId="+hyTrea.id);
			for(HYTreasureNpc npc : npcList) {
				HibernateUtil.delete(npc);
			}
			List<HYTreasureDamage> damageList = HibernateUtil.list(HYTreasureDamage.class, " where treasureId="+hyTrea.id);
			for(HYTreasureDamage damage : damageList) {
				HibernateUtil.delete(damage);
			}
		}
		for(HYTreasureTimes hyTimes : hyTreasureTimesList) {
			HibernateUtil.delete(hyTimes);
		}
		// 资源点删除 (需要看是否是匹配到的，修改资源点mutilNum)
//		List<HYResource> deleteResoureList = new ArrayList<HYResource>();
//		for(HYResourceAlliance hyra : hyResAlncList) {
//			HYResource hyRes = HibernateUtil.find(HYResource.class, " where id="+hyra.resourceId);
//			if(hyRes.multiNums == 1) {	//当前资源点只有该联盟一个看到
//				deleteResoureList.add(hyRes);
//				continue;
//			} 
//			HYResourceAlliance otherRa = HibernateUtil.find(HYResourceAlliance.class, 
//					" where lianMengId <>"+lianmengId + " and resourceId="+hyRes.id);
//			hyRes.multiNums -= 1;
//			hyRes.curHoldId = otherRa.lianMengId;
//			HibernateUtil.save(hyRes);
//			HibernateUtil.delete(hyra);
//		}
//		for(HYResource hyRes : deleteResoureList) {
//			HibernateUtil.delete(hyRes);
//			List<HYResourceJZYongbing> ybList = HibernateUtil.list(HYResourceJZYongbing.class,
//					" where resourceId=" + hyRes.id);
//			for(HYResourceJZYongbing yb : ybList) {
//				HibernateUtil.delete(yb);
//			}
//			List<HYResourceNpc> npcList = HibernateUtil.list(HYResourceNpc.class,
//					" where resourceId=" + hyRes.id);
//			for(HYResourceNpc npc : npcList) {
//				HibernateUtil.delete(npc);
//			}
//		}
//		List<HYRewardStore> storeList = HibernateUtil.list(HYRewardStore.class,
//				" where lianmengId=" + lianmengId);
//		for(HYRewardStore store : storeList) {
//			Redis.getInstance().del(CACHE_HYSTORE_APPLY + lianmengId + "_" + store.site);
//			HibernateUtil.delete(store);
//		}
		// 藏宝点 的开启和最大等级记录无关 20150909
//		HYTreasureRecord tRecord = HibernateUtil.find(HYTreasureRecord.class, lianmengId);
//		if(tRecord != null) {
//			HibernateUtil.delete(tRecord);
//		}
	}


/*	public void resourceChangeReq(int cmd, IoSession session, Builder builder) {
		ResourceChange.Builder request = (qxmobile.protobuf.HuangYeProtos.ResourceChange.Builder) builder;
		long resourceId = request.getId();
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			logger.error("未找到君主，cmd:{}", cmd);
			return;
		}

		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
		if(member == null) {
			logger.error("未加入联盟，不能进行荒野求生1");
			return;
		}
		int lianmengId = member.lianMengId;
		AllianceBean alliance = HibernateUtil.find(AllianceBean.class, lianmengId);
		if (alliance == null) {
			sendError(session, cmd, "请求信息有误");
			logger.error("找不到联盟信息，不能进行荒野求生2，联盟Id:{}", lianmengId);
			return;
		}
		ResourceChangeResp.Builder response = ResourceChangeResp.newBuilder();
		int title = member.title;
		if(title != AllianceMgr.TITLE_LEADER && title != AllianceMgr.TITLE_DEPUTY_LEADER){
			logger.error("没有权限，不能更换资源点");
			response.setResult(2);
			session.write(response.build());
			return;
		}
		HYResource hyResource = HibernateUtil.find(HYResource.class, resourceId);
		if(hyResource == null) {
			logger.error("更换的资源点不存在，id:{}", resourceId);
			return;
		}
		
		HYResourceAlliance resAlnc = HibernateUtil.find(HYResourceAlliance.class, " where resourceId="+resourceId +" and lianMengId="+member.lianMengId);
		if(resAlnc == null) {
			logger.error("没有找到所属该联盟的资源点，resourceId:{},allianceId:{}", resourceId, member.lianMengId);
			return;
		}
		HuangyePvp pvpCfg = huangyePvpMap.get(resAlnc.idOfFile);
		if(alliance.build < pvpCfg.refreshCost) {
			logger.error("建设值不足，不能更换资源点");
			response.setResult(1);
			session.write(response.build());
			return;
		}
		
		HYResource matchResource = matchHYResource(member.lianMengId, hyResource.npcLevel - 5,hyResource.npcLevel - 1);
		if(matchResource != null) {
			resAlnc.resourceId = matchResource.id;
			hyResource.multiNums -= 1;
			matchResource.multiNums += 1;
			HibernateUtil.save(hyResource);
			HibernateUtil.save(resAlnc);
			HibernateUtil.save(matchResource);
		} else {
			int level = (hyResource.npcLevel - 5) <= 0 ? 1 : (hyResource.npcLevel - 5);
			matchResource = new HYResource(level, 1);
			HibernateUtil.insert(matchResource);
		}
		List<HYTreasureTimes> hyTimesList = HibernateUtil.list(HYTreasureTimes.class,
				" where lianmengId = " + alliance.id + " and pointId=" + hyResource.id
				+ " and type=" + TYPE_RESOURCE);
		for(HYTreasureTimes times : hyTimesList) {
//			times.pointId = matchResource.id;
			times.times = RESOURCE_DAY_TIMES;
			HibernateUtil.save(times);
		}
		
		sendResourceChangeResp(session, hyResource, junzhu.id, lianmengId, RESOURCE_DAY_TIMES);
	}*/
	
	/*private void sendResourceChangeResp(IoSession session, HYResource hyResource, 
			long junzhuId, int lianmengId, int times) {
		ResourceChangeResp.Builder response = ResourceChangeResp.newBuilder();
		response.setResult(0);
		BattleResouceResp.Builder battleResResp = BattleResouceResp.newBuilder();
		battleResResp.setResourceId(hyResource.id);
		battleResResp.setTimesOfDay(times);
		battleResResp.setTotalTimes(RESOURCE_DAY_TIMES);
		int produce = (int) (CanShu.HUANGYEPVP_PRODUCE_P);
		battleResResp.setProduce(produce);
		if(hyResource.curHoldId != 0 && hyResource.curHoldId != lianmengId) {
			Date date = new Date();
			if(com.manu.dynasty.util.DateUtils.daysBetween(hyResource.holdStartTime, date) >= 3) {
				battleResResp.setIsChange(1);
			}
		} else {
			battleResResp.setIsChange(0);
		}
		
		//我方秘宝id，MiBao配置文件中的id
		BuZhenHYPvp mibaoList = HibernateUtil.find(BuZhenHYPvp.class, junzhuId);
		battleResResp.setZuheId(mibaoList.zuheId);
		//我方雇佣兵列表
		HYResourceJZYongbing jzYongbing = HibernateUtil.find(HYResourceJZYongbing.class, 
				" where junzhuId="+junzhuId + " and resourceId=" + hyResource.id);
		if(jzYongbing == null) {
			int[] bingArray = getSoldiers(hyResource.npcLevel);
			if(bingArray.length != 4) {
				logger.error("获取的雇佣兵数量不为4");
				return;
			}
			jzYongbing = new HYResourceJZYongbing(lianmengId, hyResource.id, junzhuId, 
					bingArray[0], bingArray[1], bingArray[2], bingArray[3]);
		}
		battleResResp.addYongBingList(jzYongbing.bing1);
		battleResResp.addYongBingList(jzYongbing.bing2);
		battleResResp.addYongBingList(jzYongbing.bing3);
		battleResResp.addYongBingList(jzYongbing.bing4);
		//npc信息
		List<HYResourceNpc> resourceNpcList = HibernateUtil.list(HYResourceNpc.class, " where resourceId=" + hyResource.id);
		if(resourceNpcList.size() == 0) {
			List<HuangyePvpNpc> npcList = getPvpNpcList(hyResource.npcLevel);
			for(HuangyePvpNpc npcCfg : npcList) {
				int[] bingArray = getSoldiers(npcCfg.level);
				HYResourceNpc resourceNpc = new HYResourceNpc(lianmengId, hyResource.id, npcCfg.id,
						0, bingArray[0], bingArray[1], bingArray[2], bingArray[3]);
				resourceNpcList.add(resourceNpc);
				HibernateUtil.save(resourceNpc);
			}
		}
		for(HYResourceNpc npc : resourceNpcList) {
			ResourceNpcInfo.Builder npcResp = ResourceNpcInfo.newBuilder();
			npcResp.setBossId(npc.bossId);
			npcResp.setBattleSuccess(npc.battleSuccess);
			npcResp.addYongBingId(npc.bing1);
			npcResp.addYongBingId(npc.bing2);
			npcResp.addYongBingId(npc.bing3);
			npcResp.addYongBingId(npc.bing4);
			if(npc.battleJZId > 0) {
				JunZhu battleJZ = HibernateUtil.find(JunZhu.class, npc.battleJZId);
				npcResp.setBattleName(battleJZ.name);
			} else {
				npcResp.setBattleName("");
			}
			battleResResp.addResNpcInfo(npcResp.build());
		}
		response.setResResp(battleResResp);
		session.write(response.build());
	}*/

	/**
	 * 荒野藏宝点，每个藏宝点，历史伤害输出最大值
	 * @param id
	 * @param builder
	 * @param session
	 */
	public void getMaxDamageRank(int cmd, IoSession session,Builder builder) {
		MaxDamageRankReq.Builder req = (MaxDamageRankReq.Builder)builder;
		long dbId = req.getId();
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			logger.error("未找到君主，cmd:{}", cmd);
			return;
		}
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
		if (member == null || member.lianMengId <= 0) {
			sendError(session, cmd, "你还没有加入联盟");
			logger.error("君主:{} 未加入联盟，", junzhu.name);
			return;
		}
		String where = " where treasureId="+dbId;
		List<HYTreasureDamage> list = HibernateUtil.list(HYTreasureDamage.class, where);
		Collections.sort(list);
		int rank = 1;
		MaxDamageRankResp.Builder resp = MaxDamageRankResp.newBuilder();
		DamageInfo.Builder info = null;
		for(HYTreasureDamage treaDa: list){
			info = DamageInfo.newBuilder();
			JunZhu other = HibernateUtil.find(JunZhu.class, treaDa.junzhuId);
			if(other == null){
				continue;
			}
			info.setJunZhuName(other.name);
			info.setRank(rank++);
			info.setDamage(treaDa.historyMaxDamage);
			resp.addDamageInfo(info);
		}
		session.write(resp.build());
	}

/////////////////////购买挑战次数////////////////////////
	public void dealHyBuyBattleTimesReq(int id, Builder builder, IoSession session){
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			logger.error("荒野购买挑战次数失败：君主不存在");
			return;
		}
		AlliancePlayer p = HibernateUtil.find(AlliancePlayer.class, jz.id);
		if(p == null || p.lianMengId <= 0){
			logger.error("荒野购买挑战次数失败：君主：{}没有联盟", jz.id);
			return;
		}
		HYTreasureTimes hyTimes = HibernateUtil.find(HYTreasureTimes.class, jz.id);
		if(hyTimes == null){
			hyTimes = new HYTreasureTimes(jz.id, TREASURE_DAY_TIMES, p.lianMengId);
		}else{
			resetHYTreasureTimes(hyTimes, p.lianMengId);
		}
		HyBuyBattleTimesResp.Builder resp = HyBuyBattleTimesResp.newBuilder();
		VipFuncOpen vipData = VipMgr.vipFuncOpenTemp.get(VipData.can_buy_huagnye_times);
		int vipL = vipData == null? Integer.MAX_VALUE: vipData.needlv;
		if(jz.vipLevel < vipL){
			//  0成功 ，1vip等级不足，2表示元宝不足  3今日够买次数已经用尽 4表示数据错误
			resp.setIsSuccess(1);
			// // 1-可买，发送下面信息，2-vip等级不够，不发，3-购买次数已经用尽，不发
			resp.setBuyCiShuInfo(2);
			session.write(resp.build());
			return;
		}
		int[] data = getBuyADDChanceData(jz.vipLevel, 
				hyTimes.buyBattleHuiShu, VipData.buy_huangye_times, PurchaseConstants.BUY_HUANGYE_BATTLE);
		if(data != null){
			int can = data[0];
			if (can == 0) {
				// 今日购买回数已经用完，无法再购买
				resp.setIsSuccess(3);
				// 今日购买回数已经用完，无法再购买
				resp.setBuyCiShuInfo(3);
				logger.error("玩家：{}购买荒野挑战'回数'失败：当日可购买'回数'已用完：{}", jz.id, data[3]);
			}else{
				int count = data[1];
				int yuanbao = data[2];
				boolean ok = PvpMgr.inst.isBuySuccess(jz, yuanbao, session, 
						YBType.YB_BUY_HUANGYE__BATTLE, "购买荒野挑战机会");
				if(!ok){
					// 元宝不足
					resp.setIsSuccess(2);
					resp.setBuyCiShuInfo(1);
					logger.error("玩家：{}购买荒野挑战'回数'失败：元宝不足，需要元宝{}", jz.id, yuanbao);
				}else{
					resp.setIsSuccess(0);
					hyTimes.times += count;
					hyTimes.buyBattleHuiShu += 1;
					HibernateUtil.save(hyTimes);
					
					logger.info("玩家：{}购买荒野挑战'回数'成功，今日挑战总次数:{}, 已经购买掠夺回数：{}",
							jz.id, hyTimes.times, hyTimes.buyBattleHuiShu);
					
					resp.setTimesOfDay(hyTimes.times - hyTimes.used);
//					resp.setTotalTimes(hyTimes.times);
					resp.setTotalTimes(TREASURE_DAY_TIMES);
					data = getBuyADDChanceData(jz.vipLevel, 
							hyTimes.buyBattleHuiShu, VipData.buy_huangye_times, PurchaseConstants.BUY_HUANGYE_BATTLE);
					if(data != null && data[0] == 1){
						resp.setBuyCiShuInfo(1);
						resp.setBuyNextCiShu(data[1]);
						resp.setBuyNextMoney(data[2]);
						resp.setLeftBuyCiShu(data[3] - hyTimes.buyBattleHuiShu);
					}else{
						logger.error("君主：{}购买荒野挑战'回数'失败，purchase配置文件无响应条目", jz.id);
						// 购买次数不够
						resp.setBuyCiShuInfo(3);
					}
				}
			}
		}else{
			// 数据出错，购买失败
			resp.setIsSuccess(4);
			// 购买次数不够
			resp.setBuyCiShuInfo(3);
			logger.error("君主：{}购买荒野挑战'回数'失败，purchase配置文件无响应条目", jz.id);
		}
		session.write(resp.build());
	}
	
	public int[] getBuyADDChanceData(int vipLev, int buyHuiShu, 
			int vipSelfType, int purchaseType){
		return PvpMgr.inst.getBuyADDChanceData(vipLev, buyHuiShu, vipSelfType
				,purchaseType);
	}
	
	public int getYuanBao(int buyCount, int type){
		int yuan =  PurchaseMgr.inst.getNeedYuanBao(type, buyCount);
		return yuan;
	}
}
