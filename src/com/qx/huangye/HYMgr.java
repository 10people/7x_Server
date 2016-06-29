package com.qx.huangye;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.EnemyTemp;
import com.manu.dynasty.template.GongjiType;
import com.manu.dynasty.template.HuangYe;
import com.manu.dynasty.template.HuangYeGuYongBing;
import com.manu.dynasty.template.HuangyeAward;
import com.manu.dynasty.template.HuangyeNpc;
import com.manu.dynasty.template.HuangyePve;
import com.manu.dynasty.template.HuangyeRank;
import com.manu.dynasty.template.HuangyeRankAward;
import com.manu.dynasty.template.LianmengEvent;
import com.manu.dynasty.template.Mail;
import com.manu.dynasty.template.VipFuncOpen;
import com.manu.dynasty.util.DateUtils;
import com.manu.dynasty.util.MathUtils;
import com.manu.network.BigSwitch;
import com.qx.account.FunctionOpenMgr;
import com.qx.alliance.AllianceBean;
import com.qx.alliance.AllianceMgr;
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
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.pve.PveMgr;
import com.qx.pve.PveRecord;
import com.qx.pvp.PvpMgr;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.timeworker.FunctionID;
import com.qx.vip.VipData;
import com.qx.vip.VipMgr;
import com.qx.yuanbao.YBType;

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

/**
 * 荒野求生
 * 
 * @author lizhaowen
 * x~0.99版本荒野玩法： 迷雾功能+资源点+藏宝点;
 * 1.0之后只有藏宝点相关玩法
 * 所以class中被注释掉的代码是1.0版本去掉的功能相关代码  change 20150907 by wangZhuan
 */
public class HYMgr extends EventProc{
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
	public Object treasureBattleLock = new Object();
//	public Object resourceBattleLock = new Object();
	public Object openTreasureLock = new Object(); // 藏宝点激活或者打开锁

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
	public Map<Integer, HuangyeRankAward> huangyeRankAwardMap = new HashMap<Integer, HuangyeRankAward>();
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
		huangyePveMap = new HashMap<Integer, HuangyePve>();
		for (HuangyePve HYPve : listHYPve) {
			huangyePveMap.put(HYPve.id, HYPve);
		}
		huangyePveList = listHYPve;

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
		
		Map<Integer, HuangyeRankAward> huangyeRankAwardMap = new HashMap<Integer, HuangyeRankAward>();
		List<HuangyeRankAward> huangyeRankAwardList =  TempletService.listAll(HuangyeRankAward.class.getSimpleName());
		for(HuangyeRankAward cfg: huangyeRankAwardList){
			huangyeRankAwardMap.put(cfg.rank, cfg);
		}
		this.huangyeRankAwardMap = huangyeRankAwardMap;
	}
	

	public void sendError(IoSession session, int cmd, String msg) {
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
//		FunctionOpen o = FunctionOpenMgr.openMap.get(FunctionOpenMgr.HUANG_YE_QIU_SHENG);
//		if(junzhu.level < o.lv) {
//			sendError(session, cmd, "不能进入荒野求生，需等级达到" + o.lv+"级");
//			logger.error("不能进入荒野求生，需等级达到"+ o.lv +"级");
//			return;
//		}
	
		
		HYTreasureRecord record = HibernateUtil.find(HYTreasureRecord.class, lianMengId);
		if(record == null) {
			record = new HYTreasureRecord();
			record.lianMengId = lianMengId;
			record.curGuanQiaId = huangYePve_first_pointId;
			HibernateUtil.insert(record);
		}
		
		HYTreasure hyTreasure = HibernateUtil.find(HYTreasure.class, 
				" where lianMengId=" + alliance.id +" and guanQiaId=" + record.curGuanQiaId
				, false);
		if(hyTreasure == null) {
			hyTreasure = initHYTreasure(alliance.id, record.curGuanQiaId);
		}
	
		OpenHuangYeResp.Builder response = OpenHuangYeResp.newBuilder();
		HuangYeTreasure.Builder treaResp = HuangYeTreasure.newBuilder();
		treaResp.setGuanQiaId(hyTreasure.guanQiaId);
		treaResp.setIsOpen(hyTreasure.isOpen() ? OPEN : CLOSE);
		treaResp.setJindu(hyTreasure.progress);
		treaResp.setId(hyTreasure.id);
		response.setTreasure(treaResp);
		response.setAllianceBuild(alliance.build);

		HYTreasureTimes times = HibernateUtil.find(HYTreasureTimes.class, junzhu.id);
		if(times == null){
			times = new HYTreasureTimes();
			times.junzhuId = junzhu.id;
			times.times = TREASURE_DAY_TIMES;
			HibernateUtil.insert(times);
		}else{
			resetHYTreasureTimes(times, lianMengId);
		}
		response.setRemianTimes(times.times - times.used);
		response.setAllTimes(TREASURE_DAY_TIMES);
		response.setBuyCiShuInfo(3);
		session.write(response.build());
		
		/*
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
		*/
	}

	public HYTreasure initHYTreasure(int lianMengId, int guanQiaId) {
		HYTreasure hyTreasure = new HYTreasure(lianMengId, guanQiaId);
		HibernateUtil.insert(hyTreasure);
		return hyTreasure;
	}

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
			wjNode.setHp(bing.shengming * bing.lifebarNum);
			wjNode.setNodeName(PvpMgr.inst.getNPCName(bing.name));
			wjNode.setHpNum(bing.lifebarNum);
			wjNode.setAppearanceId(bing.modelApID);
			wjNode.setNuQiZhi(0);
			wjNode.setMibaoCount(0);
			wjNode.setMibaoPower(0);
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
			HuangyePve hyPveCfg = huangyePveMap.get(hyTrea.guanQiaId);
			if(hyPveCfg == null){
				response.setResult(1);
				session.write(response.build());
				logger.error("找不到huangyepve的数值配置,guanQiaId:{}", hyTrea.guanQiaId);
				return;
			}
			int openCost = hyPveCfg.openCost;
			if (alliance.build < openCost) {
				response.setResult(1);
				session.write(response.build());
				logger.error("联盟：{}，建设值：{} 不足，不能打开藏宝点, guanQiaId:{}",
						alliance.id, alliance.build, hyTrea.guanQiaId);
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
		int guanQiaId = request.getIdOfFile();
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
			if(guanQiaId > huangYePve_first_pointId){
				String where = "where lianMengId = " + member.lianMengId + 
						" and guanQiaId = " + (guanQiaId -1);
				HYTreasure lastTr = HibernateUtil.find(HYTreasure.class, where, false);
				if (lastTr == null || lastTr.passTimes <= 0) {
					//0-成功，1-建设值不足, 2-上一关没有通关，3-已经处于激活状态
					response.setResult(2);
					session.write(response.build());
					logger.error("上一关：{}从未通关过，所以不能激活本关卡，junzhuID{}， 联盟id：{}", 
							guanQiaId -1, junzhu.id, member.lianMengId);
					return;
				}
			}
			String where = "where lianMengId = " + member.lianMengId + 
					" and guanQiaId = " + guanQiaId;
			hyTrea = HibernateUtil.find(HYTreasure.class, where, false);
			if (hyTrea != null) {
				response.setResult(3);
				session.write(response.build());
				logger.error("藏宝点已经处于激活状态，无需再次激活，junzhuid:{},guanQiaId:{}, 联盟id：{}", 
						junzhu.id, guanQiaId, member.lianMengId);
				return;
			}
			HuangyePve hyPveCfg = huangyePveMap.get(guanQiaId);
			if(hyPveCfg == null){
				response.setResult(1);
				session.write(response.build());
				logger.error("找不到huangyepve的数值配置, 请程序员速速查看，guanQiaId:{}", guanQiaId);
				return;
			}
			if (alliance.build < hyPveCfg.openCost) {
				response.setResult(1);
				session.write(response.build());
				logger.error("联盟建设值不足，不能激活藏宝点,junzhuid:{},guanQiaId:{}, 联盟id：{}", 
						junzhu.id, guanQiaId, member.lianMengId);
				return;
			}
			int openCost = hyPveCfg.openCost;
			hyTrea = new HYTreasure(member.lianMengId, guanQiaId);
			HibernateUtil.insert(hyTrea);
			// 应该不会死锁
			AllianceMgr.inst.changeAlianceBuild(alliance, -openCost);
	
			logger.info("君主:{}激活了藏宝点:{},消耗建设值:{}", junzhu.name, hyTrea.id, openCost);
			response.setResult(0);
			response.setId(hyTrea.id);
			response.setBuildValue(alliance.build);
			session.write(response.build());

			String zhiWei = member.title == AllianceMgr.TITLE_LEADER ? "盟主": "副盟主";
			HuangyePve hyCfg = huangyePveMap.get(hyPveCfg.id);
			String treaName = HeroService.getNameById(hyCfg == null ? "" : hyCfg.nameId+"");
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
			HYTreasureRecord record = HibernateUtil.find(HYTreasureRecord.class, hyTreasure.lianMengId);
			if(record == null || record.curGuanQiaId != hyTreasure.guanQiaId) {
				logger.info("藏宝点已关闭1,hyTreasureID：{}", treasureId);
				PveMgr.inst.sendZhanDouInitError(session, "请求的关卡未开启");
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
			huangyePveCfg = huangyePveMap.get(hyTreasure.guanQiaId);
			if(huangyePveCfg == null) {
				logger.error("huangyePve配置信息有误");
				PveMgr.inst.sendZhanDouInitError(session, "数据配置有误1");
				return;
			}
			// 是否是否挑战条件没问题
			/*int guanqiaId = huangyePveCfg.pveId;
			PveRecord r = HibernateUtil.find(PveRecord.class,
					"where guanQiaId=" + guanqiaId + " and uid=" + junzhu.id);
			if(r == null){
				logger.warn("玩家：{}荒野挑战藏宝点treasureId:{}, pve关卡id：{}没有通关，无法挑战",
						junzhu.id, hyTreasure.id, guanqiaId);
				PveMgr.inst.sendZhanDouInitError(session, "挑战条件不够：必须通关："+ guanqiaId);
				return;
			}*/
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
			node.setHpNum(hyNpcCfg.lifebarNum);
			node.setAppearanceId(hyNpcCfg.modelApID);
			node.setNuQiZhi(0);
			node.setMibaoCount(0);
			node.setMibaoPower(0);
			node.setArmor(hyNpcCfg.armor);
			node.setArmorMax(hyNpcCfg.armorMax);
			node.setArmorRatio(hyNpcCfg.armorRatio);
			node.setHYK(hyNpcCfg.parak);
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
		// 荒野战添加到每日任务中
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, 
				new DailyTaskCondition(hyTimes.junzhuId, DailyTaskConstants.huangye_2_id, 1));
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
		HuangyePve hyPveCfg = huangyePveMap.get(hyTreasure.guanQiaId);
		if (hyPveCfg == null) {
			logger.error("huangyePve未找到匹配类型：id:{}", hyTreasure.guanQiaId);
			return;
		}
		
		HYTreasureRecord record = HibernateUtil.find(HYTreasureRecord.class, member.lianMengId);
		if(record == null) {
			logger.error("找不到联盟:{}的荒野记录", member.lianMengId);
			return;
		}
		if(hyTreasure.progress <= 0 || record.curGuanQiaId != hyTreasure.guanQiaId){
			logger.error("请求荒野关卡信息失败，已经通关hyTreasureID:{}，当前关卡进度:{},请求的关卡ID:{}", treasureId,
					record.curGuanQiaId, hyTreasure.guanQiaId);
			return;
		}
		HYTreasureBattleResp.Builder response = HYTreasureBattleResp.newBuilder();
		List<HYTreasureNpc> treasureNpcList = HibernateUtil.list(HYTreasureNpc.class, " where treasureId=" + hyTreasure.id);
		if(treasureNpcList == null || treasureNpcList.size() == 0){
			treasureNpcList = initTreasureNpc(hyTreasure, hyPveCfg);
		}
		fillTreasureNpcResp(response, treasureNpcList);
	
		response.setJindu(hyTreasure.progress);
		response.setRemainTime(getKuaiSuPassRemainTime(hyTreasure, hyPveCfg));
		// 挑战条件
//		int guanqiaId = hyPveCfg.pveId;
//		PveRecord r = HibernateUtil.find(PveRecord.class,
//				"where guanQiaId=" + guanqiaId + " and uid=" + junzhu.id);
//		if(r == null){
//			response.setConditionIsOk(false);
//		}else{
			response.setConditionIsOk(true);
//		}
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
		response.setTotalTimes(TREASURE_DAY_TIMES);
		response.setBuyCiShuInfo(3);
		session.write(response.build());
		/*
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
		*/
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
		if(d == null || now > (d.getTime() + (CanShu.MAXTIME_HUANGYE_PVE+PvpMgr.inst.PVP_BATTLE_DELAY_TIME) * 1000)){
			hyTreasure.battleJunzhuId = 0;
			HibernateUtil.save(hyTreasure);
			return false;
		}
		return true;
	}
	/*
	 * 返回单位是秒
	 */
	public int getKuaiSuPassRemainTime(HYTreasure hyTreasure, HuangyePve hyPveCfg){
		int time = hyPveCfg.fastTimeLimit * 60 * 60;//单位秒
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
		int maxBoci = 1;
		for(HYTreasureNpc npc : treasureNpcList) {
			if(npc.remainHp > 0){
				minBoci = Math.min(npc.boCi, minBoci);
				maxBoci = Math.max(npc.boCi, maxBoci);
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
			npcInfoResp.setTotalHP(enemyTemp.getShengming() * hyNpcCfg.lifebarNum);
			response.addNpcInfos(npcInfoResp);
		}
		response.setThisBoCi(minBoci);
		response.setAllBoCi(maxBoci);
	}

	public void resetHYTreasureTimes(HYTreasureTimes hyTimes, int nowLianmengId) {
		if(hyTimes == null) {
			return;
		}
		// 判断君主是否换过联盟，更新数据
		if(hyTimes.lianmengId != nowLianmengId){
			logger.info("君主：{}，换联盟，旧联盟id：{}，新联盟id：{}，因此HYTreasureTimes字段联盟id更新",
					hyTimes.junzhuId, hyTimes.lianmengId, nowLianmengId);
			hyTimes.lianmengId = nowLianmengId;
			HibernateUtil.save(hyTimes);
		}
		if(DateUtils.isTimeToReset(hyTimes.lastResetTime, CanShu.REFRESHTIME_PURCHASE)){
			hyTimes.times = TREASURE_DAY_TIMES;
			hyTimes.lastResetTime = new Date();
			hyTimes.used = 0;
			hyTimes.buyBattleHuiShu = 0;
			HibernateUtil.save(hyTimes);
			logger.info("玩家：{}reset自己HYTreasureTimes对象成功，时间是：{}", hyTimes.junzhuId, new Date());
		}
	}
	
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
					hyTreasure.id, npcCfg.id, enemyCfg.getShengming() * npcCfg.lifebarNum, npcCfg.boCi); 
			HibernateUtil.insert(treasureNpc);
			treasureNpcList.add(treasureNpc);
		}
		return treasureNpcList;
	}
	
	public List<HuangyeNpc> findHuangyeNpcFromConfig(int npcId) {
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
		HuangyePve hyPveCfg = huangyePveMap.get(hyTreasure.guanQiaId);
		if(hyPveCfg == null){
			logger.error("HuangyePve配置不存在{}", treasureId);
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
		// 超过关卡规定时间+延迟一分钟的视为失败
		if(hyTreasure.battleJunzhuId != junzhu.id || 
				(System.currentTimeMillis()-hyTreasure.battleBeginTime.getTime())/1000 
					> CanShu.MAXTIME_HUANGYE_PVE + PvpMgr.inst.PVP_BATTLE_DELAY_TIME) {
			isPass = 0;
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
			pveBattleSuccessProcess(session, junzhu, response, member, hyTreasure, hyPveCfg);
			refreshHYTreasureNpcData(treasureNpcList);
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
		int getLianMengGongxian = 0;
		getAwardList.addAll(AwardMgr.inst.parseAwardConf(hyPveCfg.fightAward));
		for(AwardTemp award : getAwardList) {
			if(award.getItemId() == AwardMgr.ITEM_LIAN_MENG_GONGXIAN) {//联盟贡献不放在奖励物品上
				getLianMengGongxian += award.getItemNum();
			} else {
				AwardMgr.inst.fillBattleAwardInfo(response, award);
			}
		}
		response.setMoney(getLianMengGongxian);
		response.setExp(0);
		response.setTongbi(0);
		session.write(response.build());
		for(AwardTemp award : getAwardList) {
			AwardMgr.inst.giveReward(session, award, junzhu, false);
		}
		JunZhuMgr.inst.sendMainInfo(session,junzhu);
		dropAwardMapBefore.remove(junzhu.id);
	}

	public void pveBattleSuccessProcess(IoSession session, JunZhu junzhu, 
			BattleResult.Builder response,
			AlliancePlayer member,
			HYTreasure hyTreasure, HuangyePve hyPveCfg) {
		Mail mailConfig = null;
		
		boolean sendOK = false;
		// 快速通关
		boolean isFastPass = false;
		if(hyTreasure.openTime != null){
			long passTime = System.currentTimeMillis() - hyTreasure.openTime.getTime();
			if(passTime <= hyPveCfg.fastTimeLimit * 60L * 60 * 1000){
				isFastPass = true;
			}
		}
		//通关奖，有伤害记录均获得奖励
		String where = " where treasureId=" + hyTreasure.id + " ORDER BY damage DESC";
		List<HYTreasureDamage> list = HibernateUtil.list(HYTreasureDamage.class, where);
		Collections.sort(list);
		int rank = 1;
		String treaName = HeroService.getNameById(hyPveCfg == null ? "" : hyPveCfg.nameId+"");
		List<AwardTemp> tongGuanAwardList = AwardMgr.inst.parseAwardConf(hyPveCfg.award);
		for(HYTreasureDamage d: list){
			JunZhu getJunzhu = HibernateUtil.find(JunZhu.class, d.junzhuId);
			mailConfig = EmailMgr.INSTANCE.getMailConfig(20002);
			String fujian = "";
			if(mailConfig != null) {
				float awardRatio = getPassGuanQiaAward(rank, hyTreasure.guanQiaId);
				for(AwardTemp award : tongGuanAwardList) {
					fujian += award.getItemType() + ":" + award.getItemId()
							+":" + (int)(Math.ceil(award.getItemNum()*awardRatio))+"#";
				}
				String content = mailConfig.content.replace("***", treaName)
													.replace("A", rank+"");
				rank++;
				fujian = fujian.substring(0, fujian.length() - 1);
				sendOK = EmailMgr.INSTANCE.sendMail(getJunzhu.name, content, fujian, mailConfig.sender, mailConfig,"");
				logger.info("藏宝点通关奖励，以邮件发送奖励, 结果:{}", sendOK);
			}
			// 快速通关奖励
			if(isFastPass) { 
				mailConfig = EmailMgr.INSTANCE.getMailConfig(20003);
				String content = mailConfig.content.replace("***", treaName);
				if(mailConfig != null) {
					sendOK = EmailMgr.INSTANCE.sendMail(getJunzhu.name, content, hyPveCfg.perFastAward, mailConfig.sender, mailConfig,"");
					logger.info("藏宝点快速通关奖励，以邮件发送奖励, 结果:{}", sendOK);
				}
			}
		}
		for(HYTreasureDamage damage : list) {
			logger.info("荒野关卡:{}已经通关，则删除该关卡的伤害排行", hyTreasure.id);
			HibernateUtil.delete(damage);
		}
		
		// 给联盟发送奖励
		List<AwardTemp> lianMengAwardList = AwardMgr.inst.parseAwardConf(hyPveCfg.killAward);
		if(isFastPass) {
			lianMengAwardList.addAll(AwardMgr.inst.parseAwardConf(hyPveCfg.fastAward));
		}
		for(AwardTemp award : lianMengAwardList) {
			AwardMgr.inst.giveReward(session, award, junzhu, false, false);
		}
		
		logger.info("藏宝点id:{}挑战成功，分配奖励，并重置藏宝点信息");

		// 通关设置
		hyTreasure.openTime = null;
		hyTreasure.progress = 100;
		hyTreasure.passTimes += 1;
		HibernateUtil.save(hyTreasure);
		// 开启下一个关卡
		HYTreasure nextGuanQia = HibernateUtil.find(HYTreasure.class,
				" where lianMengId="+ member.lianMengId + " and guanQiaId=" + hyPveCfg.nextGuanqiaID);
		if(nextGuanQia == null) {
			nextGuanQia = new HYTreasure(member.lianMengId, hyPveCfg.nextGuanqiaID);
			HibernateUtil.insert(nextGuanQia);
		} else {
			nextGuanQia.openTime = new Date();
			nextGuanQia.progress = 100;
			HibernateUtil.save(nextGuanQia);
		}
		// 更新关卡进度记录
		HYTreasureRecord record = HibernateUtil.find(HYTreasureRecord.class, member.lianMengId);
		record.curGuanQiaId = hyPveCfg.nextGuanqiaID;
		HibernateUtil.save(record);
		
		String eventStr = AllianceMgr.inst.lianmengEventMap.get(12).str
				.replaceFirst("%d", treaName);
		AllianceMgr.inst.addAllianceEvent(member.lianMengId, eventStr);
		EventMgr.addEvent(ED.HY_PASS_GUAN_QIA, new Object[]{member.lianMengId, hyPveCfg.nextGuanqiaID});
	}
	
	public float getPassGuanQiaAward(int rank, int pointId){
		HuangyePve hy = huangyePveMap.get(pointId);
		if(hy == null){
			return 1;
		}
		HuangyeRankAward r = huangyeRankAwardMap.get(rank);
		if(r == null){
			logger.error("获得荒野排行奖励错误，找不到排行为:{}的奖励配置", rank);
			return 1;
		}
		return r.award;
	}

	public void refreshHYTreasureNpcData(List<HYTreasureNpc> treasureNpcList) {
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
			treaNpc.remainHp = enemyTemp.getShengming() * hyNpcCfg.lifebarNum;
			HibernateUtil.save(treaNpc);
		}
	}

/*	public List<HYTreasureDamage> getTopThreeDamage(long id) {
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

/*	public void putAward2Store(int lianmengId, AwardTemp calcV) {
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

	public int getTreasureProgress(List<HYTreasureNpc> treasureNpcList) {
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
			totalHP += enemyTemp.getShengming() * hyNpcCfg.lifebarNum;
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
	}


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
			AlliancePlayer otherPlayer = HibernateUtil.find(AlliancePlayer.class, treaDa.junzhuId);
			if(otherPlayer == null || otherPlayer.lianMengId != member.lianMengId){
				HibernateUtil.delete(treaDa);
				continue;
			}
			info.setJunZhuName(other.name);
			info.setRank(rank++);
			info.setDamage(treaDa.damage);
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

	@Override
	public void proc(Event param) {
			switch (param.id) {
				case ED.REFRESH_TIME_WORK:
					IoSession session = (IoSession) param.param;
					if(session == null){
						break;
					}
					JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
					if(jz == null){
						break;
					}
					boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.LianMeng, jz.id, jz.level);
					if(!isOpen){
						break;
					}
					// FIXME 有没有别的方法。
					AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, jz.id);
					if (member == null || member.lianMengId <= 0) {
						break;
					}
					int lianMengId = member.lianMengId;
					AllianceBean alliance = HibernateUtil.find(AllianceBean.class, lianMengId);
					if (alliance == null) {
						break;
					}
					if (alliance.level < open_HY_lianMeng_level) {
						break;
					}
					List<HYTreasure> hyTreaList = HibernateUtil.list(HYTreasure.class,
							" where lianMengId=" + lianMengId);
					for(HYTreasure t: hyTreaList){
						if(!t.isOpen()) {
							continue;
						}
						if(t.progress == 0) {
							continue;
						}
						// 是否有人正在挑战
						boolean has = hasSomeOneInBattle(t);
						if(has) {
							continue;
						}
						HuangyePve huangyePveCfg = huangyePveMap.get(t.guanQiaId);
						if(huangyePveCfg == null) {
							continue;
						}
						// 是否是否挑战条件没问题
						int guanqiaId = huangyePveCfg.pveId;
						PveRecord r = HibernateUtil.find(PveRecord.class,
								"where guanQiaId=" + guanqiaId + " and uid=" + jz.id);
						if(r == null){
							continue;
						}
						// 挑战次数是否足够
						HYTreasureTimes hyTimes = HibernateUtil.find(HYTreasureTimes.class, jz.id);
						boolean ok = false;
						if(hyTimes == null){
							ok = true;
						}else{
							resetHYTreasureTimes(hyTimes, lianMengId);
							if(hyTimes.times > hyTimes.used) { 
								ok = true;
							}
						}
						if(ok){
							FunctionID.pushCanShowRed(jz.id, session, FunctionID.HuangYeQiuSheng);
							break;
						}
					}
					break;
				case ED.LIANMENG_UPGRADE_LEVEL:
					Integer allianceId = (Integer) param.param;
					if(allianceId == null) {
						return;
					}
					AllianceBean bean = HibernateUtil.find(AllianceBean.class, allianceId);
					if (bean.level < open_HY_lianMeng_level) {
						return;
					}
					String where = "where lianMengId = " + allianceId + 
							" and guanQiaId = " + huangYePve_first_pointId;
					HYTreasure treasure = HibernateUtil.find(HYTreasure.class, where, false);
					if(treasure == null) {
						treasure = new HYTreasure(allianceId, huangYePve_first_pointId);
						HibernateUtil.insert(treasure);
					}
					if (bean.level == open_HY_lianMeng_level) {
						EventMgr.addEvent(ED.HY_PASS_GUAN_QIA, new Object[]{bean.id, huangYePve_first_pointId});
					}
					break;
				case ED.HY_PASS_GUAN_QIA:
					Object[] objs = (Object[]) param.param;
					Integer lmId = (Integer) objs[0];
					Integer guanQiaId = (Integer) objs[1];
					HuangyePve pveCfg = huangyePveMap.get(guanQiaId);
					if(pveCfg == null) {
						return;
					}
					String guanQiaName = HeroService.getNameById(pveCfg.nameId+"");
					Mail mailCfg = EmailMgr.INSTANCE.getMailConfig(21005);
					if(mailCfg != null){
						String content = mailCfg.content.replace("***", guanQiaName);
						List<AlliancePlayer> memberList = AllianceMgr.inst.getAllianceMembers(lmId);
						for(AlliancePlayer pla: memberList){
							JunZhu junzhu = HibernateUtil.find(JunZhu.class, pla.junzhuId);
							boolean sendOK = EmailMgr.INSTANCE.sendMail(junzhu.name, content,
										"", mailCfg.sender, mailCfg ,"");
							logger.info("荒野求生通关邮件通知，发送给玩家：{}, 结果:{}", junzhu.id, sendOK);
						}
					}
					// 添加联盟事件
					LianmengEvent lmEvent = AllianceMgr.inst.lianmengEventMap.get(11);
					if(lmEvent != null) {
						String eventStr = lmEvent.str == null ? "" : lmEvent.str;
						eventStr = eventStr.replaceFirst("%d", guanQiaName);
						AllianceMgr.inst.addAllianceEvent(lmId, eventStr);
					}
					break;
			}
		
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
		EventMgr.regist(ED.LIANMENG_UPGRADE_LEVEL, this);
		EventMgr.regist(ED.HY_PASS_GUAN_QIA, this);
	}
}
