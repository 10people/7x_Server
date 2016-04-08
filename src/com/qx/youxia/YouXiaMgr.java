package com.qx.youxia;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.EnemyTemp;
import com.manu.dynasty.template.GongjiType;
import com.manu.dynasty.template.GuanQiaJunZhu;
import com.manu.dynasty.template.Purchase;
import com.manu.dynasty.template.YouxiaNpcTemp;
import com.manu.dynasty.template.YouxiaOpenTime;
import com.manu.dynasty.template.YouxiaPveTemp;
import com.manu.dynasty.template.YouxiaSaodangTemp;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.FunctionOpenMgr;
import com.qx.award.AwardMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.pve.PveGuanQiaMgr;
import com.qx.pve.PveMgr;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.timeworker.FunctionID;
import com.qx.util.TableIDCreator;
import com.qx.vip.VipData;
import com.qx.vip.VipMgr;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

import log.ActLog;
import qxmobile.protobuf.BattlePveResult.BattleResult;
import qxmobile.protobuf.PveLevel.PveSaoDangAward;
import qxmobile.protobuf.PveLevel.PveSaoDangRet;
import qxmobile.protobuf.YouXiaProtos.YouXiaGuanQiaInfoReq;
import qxmobile.protobuf.YouXiaProtos.YouXiaGuanQiaInfoResp;
import qxmobile.protobuf.YouXiaProtos.YouXiaInfo;
import qxmobile.protobuf.YouXiaProtos.YouXiaInfoResp;
import qxmobile.protobuf.YouXiaProtos.YouXiaSaoDangReq;
import qxmobile.protobuf.YouXiaProtos.YouXiaTimesBuyReq;
import qxmobile.protobuf.YouXiaProtos.YouXiaTimesBuyResp;
import qxmobile.protobuf.YouXiaProtos.YouXiaTimesInfoReq;
import qxmobile.protobuf.YouXiaProtos.YouXiaTimesInfoResp;
import qxmobile.protobuf.YouXiaProtos.YouXiaTypeInfoReq;
import qxmobile.protobuf.YouXiaProtos.YouXiaTypeInfoResp;
import qxmobile.protobuf.ZhanDou.BattleYouXiaResultReq;
import qxmobile.protobuf.ZhanDou.DroppenItem;
import qxmobile.protobuf.ZhanDou.Group;
import qxmobile.protobuf.ZhanDou.Node;
import qxmobile.protobuf.ZhanDou.NodeProfession;
import qxmobile.protobuf.ZhanDou.NodeType;
import qxmobile.protobuf.ZhanDou.YouXiaZhanDouInitReq;
import qxmobile.protobuf.ZhanDou.ZhanDouInitResp;

public class YouXiaMgr extends EventProc {
	public static YouXiaMgr inst;
	public Logger logger = LoggerFactory.getLogger(YouXiaMgr.class);

	public Map<Integer, YouxiaPveTemp> youxiaPveTempMap = new HashMap<Integer, YouxiaPveTemp>();
	public Map<Integer, List<YouxiaPveTemp>> youxiaPveTempListByType = new HashMap<Integer, List<YouxiaPveTemp>>();
	public Map<Integer, YouxiaNpcTemp> youxiaNpcTempMap = new HashMap<Integer, YouxiaNpcTemp>();
	public Map<Integer, List<YouxiaNpcTemp>> youxiaNpcMapByNpcId = new HashMap<Integer, List<YouxiaNpcTemp>>();
	public Map<Integer, YouxiaOpenTime> youxiaOpenTimeMap = new HashMap<Integer, YouxiaOpenTime>();
	// 玩家进入战斗前，提前算好怪物掉落奖励 <junZhuId, <postionId, List<AwardTemp>>>
	public Map<Long, Map<Integer, List<AwardTemp>>> dropAwardMapBefore = new HashMap<Long, Map<Integer, List<AwardTemp>>>();

	public static final int DROP_ALL = 0; // 全部掉落
	public static final int DROP_ORDER = 1; // 顺次掉落，超过最大配置数量，则从头开始

	public static final int GUANQIA_PASS = 1;
	public static final int GUANQIA_NOT_PASS = 0;

	public static final int BATTLE_WIN = 1;

	public YouXiaMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		Map<Integer, YouxiaPveTemp> youxiaPveTempMap = new HashMap<Integer, YouxiaPveTemp>();
		Map<Integer, List<YouxiaPveTemp>> youxiaPveTempListByType = new HashMap<Integer, List<YouxiaPveTemp>>();
		List<YouxiaPveTemp> youxiaPveTempList = TempletService.listAll(YouxiaPveTemp.class.getSimpleName());
		for (YouxiaPveTemp pveTemp : youxiaPveTempList) {
			youxiaPveTempMap.put(pveTemp.id, pveTemp);

			List<YouxiaPveTemp> pveList = youxiaPveTempListByType.get(pveTemp.bigId);
			if (pveList == null) {
				pveList = new ArrayList<YouxiaPveTemp>();
				youxiaPveTempListByType.put(pveTemp.bigId, pveList);
			}
			pveList.add(pveTemp);
		}
		this.youxiaPveTempMap = youxiaPveTempMap;
		this.youxiaPveTempListByType = youxiaPveTempListByType;

		Map<Integer, YouxiaNpcTemp> youxiaNpcTempMap = new HashMap<Integer, YouxiaNpcTemp>();
		Map<Integer, List<YouxiaNpcTemp>> youxiaNpcMapByNpcId = new HashMap<Integer, List<YouxiaNpcTemp>>();
		List<YouxiaNpcTemp> youxiaNpcTempList = TempletService.listAll(YouxiaNpcTemp.class.getSimpleName());
		for (YouxiaNpcTemp npcTemp : youxiaNpcTempList) {
			youxiaNpcTempMap.put(npcTemp.id, npcTemp);

			List<YouxiaNpcTemp> npcTempList = youxiaNpcMapByNpcId.get(npcTemp.npcId);
			if (npcTempList == null) {
				npcTempList = new ArrayList<YouxiaNpcTemp>();
				youxiaNpcMapByNpcId.put(npcTemp.npcId, npcTempList);
			}
			npcTempList.add(npcTemp);
		}
		this.youxiaNpcTempMap = youxiaNpcTempMap;
		this.youxiaNpcMapByNpcId = youxiaNpcMapByNpcId;

		List<YouxiaOpenTime> youxiaOpenTimeList = TempletService.listAll(YouxiaOpenTime.class.getSimpleName());
		Map<Integer, YouxiaOpenTime> youxiaOpenTimeMap = new HashMap<Integer, YouxiaOpenTime>();
		for (YouxiaOpenTime openTime : youxiaOpenTimeList) {
			youxiaOpenTimeMap.put(openTime.id, openTime);
		}
		this.youxiaOpenTimeMap = youxiaOpenTimeMap;
	}

	public void battleOverReport(int id, IoSession session, Builder builder) {
		BattleYouXiaResultReq.Builder request = (qxmobile.protobuf.ZhanDou.BattleYouXiaResultReq.Builder) builder;
		int guanQiaId = request.getId();
		int win = request.getResult();
		int killNum = request.getScore();
		List<Integer> dropList4NpcPos = request.getDropeenItemNpcsList();

		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("找不到君主，junZhuId:{}", session.getAttribute(SessionAttKey.junZhuId));
			return;
		}
		YouxiaPveTemp pveTemp = youxiaPveTempMap.get(guanQiaId);
		if (pveTemp == null) {
			logger.error("游侠战斗结算失败，找不到游侠章节:{}配置错误，zhangJieId:{}", guanQiaId);
			return;
		}

		Map<Integer, Integer> npcDeadTimesMap = new HashMap<Integer, Integer>(); // npc被杀死次数记录
		List<AwardTemp> getAwardList = new ArrayList<AwardTemp>();

		BattleResult.Builder response = BattleResult.newBuilder();
		int getTongbi = 0;
		int getExp = 0;
		if (win == BATTLE_WIN) { // 只有胜利才会给奖励
			// 怪物掉落奖励
			Map<Integer, List<AwardTemp>> npcDropAwardMap = dropAwardMapBefore.get(junZhu.id);
			int deadNum = Math.min(dropList4NpcPos.size(), pveTemp.maxNum);
			for (int i = 0; i < deadNum; i++) {
				Integer npcPos = dropList4NpcPos.get(i);
				Integer deadTimes = npcDeadTimesMap.get(npcPos);
				if (deadTimes == null) {
					deadTimes = 1;
					npcDeadTimesMap.put(npcPos, deadTimes);
				} else {
					deadTimes += 1;
					npcDeadTimesMap.put(npcPos, deadTimes);
				}
				YouxiaNpcTemp npcTempCfg = getNpcTempByNpcidAndPosition(pveTemp.npcId, npcPos);
				if (npcTempCfg == null) {
					logger.error("找不到游侠pveId:{},npcId:{},pos:{}的配置", pveTemp.bigId, pveTemp.npcId, npcPos);
					continue;
				}
				List<AwardTemp> posNpcDropAward = npcDropAwardMap.get(npcPos);
				if (posNpcDropAward != null) {
					// 根据掉落类型获取应该获得的奖励
					List<AwardTemp> npcDropAwardList = getNpcDropAward(posNpcDropAward, deadTimes, npcTempCfg.droptype);
					getAwardList.addAll(npcDropAwardList);
				}
			}
			// 配置的关卡奖励
			List<AwardTemp> guanQiaAwardList = AwardMgr.inst.getHitAwardList(pveTemp.awardId, ",", "=");
			for (AwardTemp award : guanQiaAwardList) {
				getAwardList.add(award);
			}

			// 更新游侠玩法信息
			// 判断今日挑战次数
			// YouXiaBean yxBean = HibernateUtil.find(YouXiaBean.class,
			// " where junzhuId=" + junZhu.id + " and type ="
			// + pveTemp.bigId);
			List<YouXiaBean> youXiaInfoList = HibernateUtil.list(YouXiaBean.class, " where junzhuId = " + junZhu.id);
			int allwin = 0;
			YouXiaBean yxBean = null;
			for (YouXiaBean you : youXiaInfoList) {
				allwin += you.allWinTimes;
				if (you.type == pveTemp.bigId) {
					yxBean = you;
				}
			}
			if (yxBean == null) {
				logger.error("youxiaBean初始化失败， junzhuId:{} type:{}", junZhu.id, pveTemp.bigId);
			} else {
				yxBean.times = Math.max(0, yxBean.times - 1);
				yxBean.lastBattleTime = new Date();
				yxBean.allWinTimes += 1;
				HibernateUtil.save(yxBean);
				allwin++; // 记录本次
				// 主线任务
				EventMgr.addEvent(ED.finish_youxia_x, new Object[] { junZhu.id, allwin });
				// 每日任务：完成1次游侠活动
				EventMgr.addEvent(ED.DAILY_TASK_PROCESS,
						new DailyTaskCondition(junZhu.id, DailyTaskConstants.youXia_activity, 1));
			}
			// 游侠关卡记录
			YouXiaRecord yxRecord = HibernateUtil.find(YouXiaRecord.class,
					" where junzhuId=" + junZhu.id + " and guanQiaId=" + guanQiaId);
			int pass = isGuanQiaPass(win, killNum, pveTemp, yxRecord);
			if (yxRecord == null) {
				yxRecord = new YouXiaRecord();
				yxRecord.setId(TableIDCreator.getTableID(YouXiaRecord.class, 1));
				yxRecord.setGuanQiaId(guanQiaId);
				yxRecord.setJunzhuId(junZhu.id);
				yxRecord.setScore(killNum);
				yxRecord.setType(pveTemp.bigId);
				yxRecord.setPass(pass);
				HibernateUtil.insert(yxRecord);
			} else {
				int before = yxRecord.getScore();
				int nowScore = Math.max(before, killNum);// 跟上次比存储数量较大的
				nowScore = Math.min(nowScore, pveTemp.maxNum);// 最大的数量不能超过配置表的最大数量
				yxRecord.setType(pveTemp.bigId);
				yxRecord.setScore(nowScore);
				yxRecord.setPass(pass);
				HibernateUtil.save(yxRecord);
			}
			if ((pveTemp.bigId == 1 && killNum >= pveTemp.maxNum)
					||pveTemp.bigId != 1) {
				EventMgr.addEvent(ED.YOU_XIA_SUCCESS, new Object[] { junZhu, guanQiaId });
			}
		}
		for (AwardTemp award : getAwardList) {
			if (award.getItemId() == AwardMgr.ITEM_TONGBI_ID) {
				getTongbi += award.getItemNum();
			} else if (award.getItemId() == AwardMgr.ITEM_EXP_ID) {
				getExp += award.getItemNum();
			} else {
				AwardMgr.inst.fillBattleAwardInfo(response, award);
			}
		}

		logger.info("游侠战斗结算成功，{}:{}战斗结束，关卡{}，droppenList.size:{} 铜币{}，经验{}，结果{}", junZhu.id, junZhu.name, guanQiaId,
				dropList4NpcPos.size(), getTongbi, getExp, win);
		switch (pveTemp.bigId) {
		case 1:
			ActLog.log.LootRich(junZhu.id, junZhu.name, ActLog.vopenid, "", guanQiaId, win == 1 ? 1 : 2, 1, getTongbi);
			break;
		case 2:
			ActLog.log.KillRobber(junZhu.id, junZhu.name, ActLog.vopenid, "", guanQiaId, win == 1 ? 1 : 2, 1, 0, 0);
			break;
		case 3:
			ActLog.log.KillRebelArmy(junZhu.id, junZhu.name, ActLog.vopenid, "", guanQiaId, win == 1 ? 1 : 2, 1, 0, 0);
			break;
		}

		response.setExp(getExp);
		response.setMoney(getTongbi);
		session.write(response.build());

		for (AwardTemp award : getAwardList) {
			logger.info("游侠战斗结算奖励，君主:{} 得到奖励 awardId:{}, 类型:{},id:{},数量:{}", junZhu.id, award.getAwardId(),
					award.getItemType(), award.getItemId(), award.getItemNum());
			AwardMgr.inst.giveReward(session, award, junZhu, false);
		}
		JunZhuMgr.inst.sendMainInfo(session);
		dropAwardMapBefore.remove(junZhu.id);
	}

	public int isGuanQiaPass(int win, int killNum, YouxiaPveTemp pveTemp, YouXiaRecord yxRecord) {
		switch (pveTemp.bigId) {
		case 1:
			if(yxRecord != null && yxRecord.getPass() == GUANQIA_PASS) {
				return GUANQIA_PASS;
			}
			if (killNum >= pveTemp.maxNum) {
				return GUANQIA_PASS;
			}
			break;
		case 2:
		case 3:
		case 4:
		case 5:
			if (win == BATTLE_WIN) {
				return GUANQIA_PASS;
			}
			break;
		}
		return GUANQIA_NOT_PASS;
	}

	/**
	 * 根据该npc死亡次数，获取该npc掉落的物品奖励
	 * 
	 * @param npcAwardList
	 *            该npc掉落物品序列
	 * @param deadTimes
	 *            该npc死亡次数
	 * @param isLoop
	 *            该npc是否是重复掉落
	 * @return
	 */
	public List<AwardTemp> getNpcDropAward(List<AwardTemp> npcAwardList, int deadTimes, int dropType) {
		List<AwardTemp> awardList = new ArrayList<AwardTemp>();
		int size = npcAwardList.size();
		if (npcAwardList == null || size == 0) {
			return awardList;
		}
		switch (dropType) {
		case DROP_ALL:
			return npcAwardList;
		case DROP_ORDER:
			int index = Math.max(0, deadTimes % size - 1);
			awardList.add(npcAwardList.get(index));
			return awardList;
		default:
			return awardList;
		}
	}

	public YouxiaNpcTemp getNpcTempByNpcidAndPosition(int npcId, Integer npcPos) {
		YouxiaNpcTemp npcTemp = null;
		List<YouxiaNpcTemp> npcList = youxiaNpcMapByNpcId.get(npcId);
		for (YouxiaNpcTemp cfg : npcList) {
			if (cfg.position == npcPos) {
				npcTemp = cfg;
				break;
			}
		}
		return npcTemp;
	}

	public void battleInitRequest(int id, IoSession session, Builder builder) {
		YouXiaZhanDouInitReq.Builder request = (qxmobile.protobuf.ZhanDou.YouXiaZhanDouInitReq.Builder) builder;
		int zhangJieId = request.getChapterId();

		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("找不到君主，junZhuId:{}", session.getAttribute(SessionAttKey.junZhuId));
			PveMgr.inst.sendZhanDouInitError(session, "找不到君主信息");
			return;
		}

		YouxiaPveTemp pveTemp = youxiaPveTempMap.get(zhangJieId);
		if (pveTemp == null) {
			logger.error("游侠战斗信息请求失败，找不到章节:{}配置", zhangJieId);
			PveMgr.inst.sendZhanDouInitError(session, "数据配置错误1");
			return;
		}

		YouxiaOpenTime yxOpen = youxiaOpenTimeMap.get(pveTemp.bigId);
		if (yxOpen == null) {
			logger.error("游侠战斗信息请求失败，youxiaOpenTime找不到id:{}配置", pveTemp.bigId);
			PveMgr.inst.sendZhanDouInitError(session, "数据配置错误2");
			return;
		}
		// 判断君主等级是否满足该类型游戏玩法等级
		if (junZhu.level < yxOpen.openLevel) {
			logger.error("游侠战斗信息请求失败，君主等级不满:{}，章节:{}", yxOpen.openLevel, zhangJieId);
			PveMgr.inst.sendZhanDouInitError(session, "君主等级未达到等级要求");
			return;
		}
		/*
		 * // 判断今日是否开启本活动玩法 // 2015年12月25日 11:46:50 改为按等级开放 int dayNum =
		 * Calendar.getInstance().get(Calendar.DAY_OF_WEEK); if (dayNum == 1) {
		 * dayNum = 7; } else { dayNum -= 1; } if
		 * (!yxOpen.OpenDay.contains(String.valueOf(dayNum))) { logger.error(
		 * "游侠战斗信息请求失败，本日未开放该活动type:{}玩法。今日星期{}, 要求:{}", pveTemp.bigId,dayNum,
		 * pveTemp.openDay); PveMgr.inst.sendZhanDouInitError(session,
		 * "活动今日未开放"); return; }
		 */

		// 判断今日挑战次数
		// YouXiaBean yxBean = HibernateUtil.find(YouXiaBean.class,
		// " where junzhuId=" + junZhu.id + " and type =" + pveTemp.bigId);
		List<YouXiaBean> youXiaInfoList = HibernateUtil.list(YouXiaBean.class, " where junzhuId = " + junZhu.id);
		int allBattle = 0;
		YouXiaBean yxBean = null;
		for (YouXiaBean you : youXiaInfoList) {
			allBattle += you.allBattleTimes;
			if (you.type == pveTemp.bigId) {
				yxBean = you;
			}
		}
		if (yxBean == null) {
			logger.error("游侠战斗信息请求失败,youxiaBean初始化失败， junzhuId:{} type:{}", junZhu.id, pveTemp.bigId);
			PveMgr.inst.sendZhanDouInitError(session, "游侠数据错误1！");
			return;
		}
		if (yxBean.times <= 0) {
			logger.error("游侠战斗信息请求失败，今日游侠type:{}挑战次数已用完", pveTemp.bigId);
			PveMgr.inst.sendZhanDouInitError(session, "今日挑战次数已用完！");
			return;
		}

		// 判断cd时间
		Date date = new Date();
		if (yxBean.lastBattleTime != null) {
			int intervalTime = (int) ((date.getTime() - yxBean.lastBattleTime.getTime()) / 1000);
			if (intervalTime < yxOpen.CD) {
				logger.error("游侠战斗信息请求失败，该玩法还处于cd时间，不能进入");
				PveMgr.inst.sendZhanDouInitError(session, "该玩法处于冷却时间，稍后再来！");
				return;
			}
		}

		// 判断君主等级是否满足游侠关卡条件
		if (junZhu.level < pveTemp.monarchLevel) {
			logger.error("游侠战斗信息请求失败，君主等级:{}没有达到该关卡:{}要求等级:{}", junZhu.level, pveTemp.id, pveTemp.monarchLevel);
			PveMgr.inst.sendZhanDouInitError(session, "您的等级不符合要求！");
			return;
		}

		List<YouxiaNpcTemp> npcList = youxiaNpcMapByNpcId.get(pveTemp.npcId);
		if (npcList == null || npcList.size() == 0) {
			logger.error("游侠战斗信息请求失败，章节怪物配置为空，zhangjieId:{}, npcId:{}", zhangJieId, pveTemp.npcId);
			PveMgr.inst.sendZhanDouInitError(session, "数据配置错误3");
			return;
		}

		ZhanDouInitResp.Builder resp = ZhanDouInitResp.newBuilder();
		resp.setZhandouId(PveMgr.battleIdMgr.incrementAndGet());// 战斗id 后台使用
		resp.setMapId(pveTemp.sceneId);
		resp.setLimitTime(pveTemp.time);

		// 填充敌方数据
		List<AwardTemp> getAwardList = new ArrayList<AwardTemp>();
		// 填充己方数据（战斗数据和秘宝信息数据）
		Group.Builder selfTroop = Group.newBuilder();
		List<Node> selfs = new ArrayList<Node>();
		int selfFlagId = 1;
		int zuheId = getSaveMibaoZuheId(junZhu.id, pveTemp.bigId);
		PveMgr.inst.fillJunZhuDataInfo(resp, session, selfs, junZhu, selfFlagId++, zuheId, selfTroop);

		int index = 0;
		Group.Builder enemyTroop = Group.newBuilder();
		List<Node> enemys = new ArrayList<Node>();
		Map<Integer, List<AwardTemp>> npcDropAward = new HashMap<Integer, List<AwardTemp>>();
		for (YouxiaNpcTemp npcTemp : npcList) {
			Node.Builder node = Node.newBuilder();
			NodeType nodeType = NodeType.valueOf(npcTemp.type);
			if (nodeType == null) {
				logger.error("游侠战斗信息请求失败，nodeType与npcTemp的type值不一致，npcTemp.type:{}", npcTemp.type);
				continue;
			}
			NodeProfession nodeProfession = NodeProfession.valueOf(npcTemp.profession);
			if (nodeProfession == null) {
				logger.error("游侠战斗信息请求失败，" + "nodeProfession与npcTemp的Profession值不一致，npcTemp.Profession:{}",
						npcTemp.profession);
				continue;
			}

			if (nodeType == NodeType.PLAYER) { // 模拟玩家npc
				GuanQiaJunZhu guanQiaJunZhu = PveMgr.inst.id2GuanQiaJunZhu.get(npcTemp.enemyId);
				if (guanQiaJunZhu == null) {
					logger.error("找不到id:{}的GuanQiaJunZhu配置", npcTemp.enemyId);
					return;
				}
				if (npcTemp.ifTeammate == 1) {
					PveMgr.inst.fillNPCPlayerDataInfo(selfs, guanQiaJunZhu, selfFlagId++, npcTemp);
				} else {
					PveMgr.inst.fillNPCPlayerDataInfo(enemys, guanQiaJunZhu, npcTemp.position, npcTemp);
				}
			} else {
				EnemyTemp enemyTemp = PveMgr.inst.id2Enemy.get(npcTemp.enemyId);
				if (enemyTemp == null) {
					logger.error("游侠战斗信息请求失败，enemy表未发现id为:{}的配置", npcTemp.enemyId);
					continue;
				}
				node.setModleId(npcTemp.modelId);// npc模型id
				node.setNodeType(nodeType);
				node.setNodeProfession(nodeProfession);
				node.setHp(enemyTemp.getShengming() * npcTemp.lifebarNum);
				node.setNodeName(npcTemp.name + "");
				node.setHpNum(npcTemp.lifebarNum);
				node.setAppearanceId(npcTemp.modelApID);
				node.setNuQiZhi(0);
				node.setMibaoCount(0);
				node.setMibaoPower(0);
				GongjiType gongjiType = PveMgr.inst.id2GongjiType.get(npcTemp.gongjiType);
				PveMgr.inst.fillDataByGongjiType(node, gongjiType);
				PveMgr.inst.fillGongFangInfo(node, enemyTemp);

				String skills = npcTemp.skills;
				if (skills != null && !skills.equals("")) {
					String[] skillList = skills.split(",");
					for (String s : skillList) {
						int skillId = Integer.parseInt(s);
						PveMgr.inst.addNodeSkill(node, skillId);
					}
				}
				List<AwardTemp> npcAwardList = AwardMgr.inst.getHitAwardList(npcTemp.award, ",", "=");
				npcDropAward.put(npcTemp.position, npcAwardList);
				int size = npcAwardList.size();
				for (int i = 0; i < size; i++) {
					AwardTemp awardTemp = npcAwardList.get(i);
					DroppenItem.Builder dropItem = DroppenItem.newBuilder();
					dropItem.setId(index);
					dropItem.setCommonItemId(awardTemp.getItemId());
					dropItem.setNum(awardTemp.getItemNum());
					node.addDroppenItems(dropItem);
					getAwardList.add(awardTemp);
					index++;
				}
				node.setDroppenType(npcTemp.droptype);
				if (npcTemp.ifTeammate == 1) {
					node.addFlagIds(selfFlagId++);
					selfs.add(node.build());
				} else {
					node.addFlagIds(npcTemp.position);
					enemys.add(node.build());
				}
			}
		}

		dropAwardMapBefore.put(junZhu.id, npcDropAward);
		logger.info("游侠战斗信息请求成功，君主:{}进入游侠战斗:{},可获得奖励:{}", junZhu.name, //
				zhangJieId, getAwardList);
		enemyTroop.addAllNodes(enemys);
		enemyTroop.setMaxLevel(999);
		resp.setEnemyTroop(enemyTroop);

		selfTroop.addAllNodes(selfs);
		selfTroop.setMaxLevel(BigSwitch.pveGuanQiaMgr.getGuanQiaMaxId(junZhu.id));
		resp.setSelfTroop(selfTroop);
		session.write(resp.build());

		yxBean.allBattleTimes += 1;
		HibernateUtil.save(yxBean);
		allBattle++; // 记录本次
		// 参加游侠任务
		EventMgr.addEvent(ED.go_youxia, new Object[] { junZhu.id, allBattle, pveTemp.bigId });
	}

	public int getSaveMibaoZuheId(long junzhuId, int type) {
		int zuheId = 0;
		BuZhenYouXia buzhen = HibernateUtil.find(BuZhenYouXia.class, junzhuId);
		if (buzhen == null) {
			buzhen = new BuZhenYouXia();
			buzhen.junzhuId = junzhuId;
			HibernateUtil.save(buzhen);
			return zuheId;
		}
		switch (type) {
		case 1:
			zuheId = buzhen.jinBiZuheId;
			break;
		case 2:
			zuheId = buzhen.caiLiaoZuheId;
			break;
		case 3:
			zuheId = buzhen.jingQiZuheId;
			break;
		case 4:
			zuheId = buzhen.type4;
			break;
		case 5:
			zuheId = buzhen.type5;
			break;
		default:
			logger.error("游侠保存秘宝获取失败，不是游侠活动类型，type:{}", type);
			break;
		}
		return zuheId;
	}

	public void youxiaInfoRequest(int id, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("找不到君主，junZhuId:{}", session.getAttribute(SessionAttKey.junZhuId));
			return;
		}
		List<YouXiaBean> youXiaInfoList = HibernateUtil.list(YouXiaBean.class, " where junzhuId = " + junZhu.id);
		YouXiaInfoResp.Builder response = YouXiaInfoResp.newBuilder();
		Date date = new Date();

		for (Map.Entry<Integer, YouxiaOpenTime> entry : youxiaOpenTimeMap.entrySet()) {
			YouxiaOpenTime openTimeCfg = entry.getValue();
			YouXiaInfo.Builder youxiaInfo = YouXiaInfo.newBuilder();
			youxiaInfo.setId(openTimeCfg.id);
			youxiaInfo.setOpenDay(openTimeCfg.OpenDay);
			youxiaInfo.setOpenTime(openTimeCfg.OpenTime);
			YouXiaBean beanInfo = null;
			for (YouXiaBean bean : youXiaInfoList) {
				if (bean.type == openTimeCfg.id) {
					beanInfo = bean;
					break;
				}
			}
			if (beanInfo == null) {
				beanInfo = new YouXiaBean();
				beanInfo.id = TableIDCreator.getTableID(YouXiaBean.class, 1L);
				beanInfo.junzhuId = junZhu.id;
				beanInfo.type = openTimeCfg.id;
				beanInfo.times = openTimeCfg.maxTimes;
				beanInfo.lastBattleTime = null;
				beanInfo.lastBuyTime = date;
				HibernateUtil.insert(beanInfo);
			} else {
				refreshTimes(openTimeCfg, beanInfo);
			}
			if (beanInfo.lastBattleTime == null) {
				youxiaInfo.setRemainColdTime(0);
			} else {
				int coldTime = 0;
				int intervalTime = (int) ((date.getTime() - beanInfo.lastBattleTime.getTime()) / 1000);
				if (intervalTime >= openTimeCfg.CD) {
					coldTime = 0;
				} else {
					coldTime = openTimeCfg.CD - intervalTime;
				}
				youxiaInfo.setRemainColdTime(coldTime);
			}
			int zuheId = getSaveMibaoZuheId(junZhu.id, openTimeCfg.id);
			youxiaInfo.setZuheId(zuheId);
			youxiaInfo.setOpen(true);

			youxiaInfo.setRemainTimes(beanInfo.times);
			response.addYouXiaInfos(youxiaInfo);
		}
		session.write(response.build());
	}

	public void refreshTimes(YouxiaOpenTime openTimeCfg, YouXiaBean beanInfo) {
		// 取得配置的小时数、分钟数
		String[] time = openTimeCfg.OpenTime.split(":");
		if (time.length != 2) {
			logger.error("游侠配置文件有误，openTime:{}", openTimeCfg.OpenTime);
			return;
		}
		int hourCfg = Integer.parseInt(time[0]);
		int minutescfg = Integer.parseInt(time[1]);

		Calendar calendar = Calendar.getInstance();
		Date nowDate = calendar.getTime();
		calendar.set(Calendar.HOUR_OF_DAY, hourCfg);
		calendar.set(Calendar.MINUTE, minutescfg);
		Date updateDate = calendar.getTime();
		// 是否需要更新次数
		if (beanInfo.lastBattleTime != null && !nowDate.before(updateDate)
				&& beanInfo.lastBattleTime.before(updateDate)) {
			if (beanInfo.times < openTimeCfg.maxTimes) {
				beanInfo.times = openTimeCfg.maxTimes;
				HibernateUtil.save(beanInfo);
			}
		}
	}

	public boolean isOpen(YouxiaOpenTime openTimeCfg, YouXiaBean beanInfo) {
		String openDay = openTimeCfg.OpenDay;
		String openTime = openTimeCfg.OpenTime;
		if (openDay == null || openDay.equals("")) {
			return false;
		}
		boolean open = false;
		// 取得现在的星期数、小时数、分钟数
		Calendar calendar = Calendar.getInstance();
		int nowDay = calendar.get(Calendar.DAY_OF_WEEK);
		int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
		int nowMinutes = calendar.get(Calendar.MINUTE);
		nowDay -= 1;
		if (nowDay <= 0) {
			nowDay = 7;
		}

		// 取得配置的小时数、分钟数
		String[] time = openTime.split(":");
		if (time.length != 2) {
			logger.error("游侠配置文件有误，openTime:{}", openTime);
			return false;
		}
		int hourCfg = Integer.parseInt(time[0]);
		int minutescfg = Integer.parseInt(time[1]);

		// 判断今天是否开放玩法
		if (openDay.contains(String.valueOf(nowDay))) {
			// 现在的时间是大于或者等于配置的时间
			// 则是开放并判断是否要刷新次数
			if (nowHour > hourCfg || (nowHour == hourCfg && nowMinutes >= minutescfg)) {
				open = true;
				calendar.set(Calendar.HOUR_OF_DAY, hourCfg);
				calendar.set(Calendar.MINUTE, minutescfg);
				Date updateDate = calendar.getTime();
				// 是否需要更新次数
				if (beanInfo.lastBattleTime != null && beanInfo.lastBattleTime.before(updateDate)) {
					beanInfo.times = openTimeCfg.maxTimes;
					HibernateUtil.save(beanInfo);
				}
			} else {
				// 现在的时间小于配置的时间，则判断前一天是否也是开放的，是的话当前玩法要开放
				if (nowHour < hourCfg || (nowHour == hourCfg && nowMinutes < minutescfg)) {
					if (openDay.contains(String.valueOf(getPreWeekday(nowDay)))) {
						open = true;
					}
				}
			}
		} else {
			// 如果今天不开放玩法，则判断前一天是否在开放星期数里
			if (openDay.contains(String.valueOf(getPreWeekday(nowDay)))) {
				// 现在的时间小于配置的时间，表示还在开放
				if (nowHour < hourCfg || (nowHour == hourCfg && nowMinutes < minutescfg)) {
					open = true;
				}
			}
		}
		return open;
	}

	/**
	 * 返回输入星期数的前一天
	 * 
	 * @param nowDay
	 *            1-7之间 1-7分别表示星期一...星期日
	 * @return 返回前一天，如果是-1表示输入有误
	 */
	public int getPreWeekday(int nowDay) {
		if (nowDay < 1 || nowDay > 7) {
			logger.error("输入的日期有错误， nowDay:{}", nowDay);
			return -1;
		}
		if (nowDay == 1) {
			return 7;
		} else {
			return nowDay - 1;
		}
	}

	public void timesInfoRequest(int id, IoSession session, Builder builder) {
		YouXiaTimesInfoReq.Builder request = (qxmobile.protobuf.YouXiaProtos.YouXiaTimesInfoReq.Builder) builder;
		int type = request.getType();
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("找不到君主，junZhuId:{}", session.getAttribute(SessionAttKey.junZhuId));
			return;
		}
		YouXiaBean yxBean = HibernateUtil.find(YouXiaBean.class, " where junzhuId=" + junZhu.id + " and type =" + type);
		if (yxBean == null) {
			logger.error("游侠次数请求失败，未找到游侠数据，type:{}", type);
			return;
		}
		Date date = new Date();
		Date lastBuyTime = yxBean.lastBuyTime == null ? date : yxBean.lastBuyTime;
		// change 20150901
		if (DateUtils.isTimeToReset(lastBuyTime, CanShu.REFRESHTIME_PURCHASE)) {
			yxBean.buyTimes = 0;
			HibernateUtil.save(yxBean);
		}
		int maxBuyTimes = VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel, VipData.youXiaTimesGet);
		Purchase purchaseCfg = PurchaseMgr.inst.getPurchaseCfg(PurchaseConstants.YOUXIA_TIMES, yxBean.buyTimes + 1);
		if (purchaseCfg == null) {
			logger.error("游侠次数请求失败，未找到type:{}的purchase配置", PurchaseConstants.YOUXIA_TIMES);
			return;
		}
		int needCost = purchaseCfg.getYuanbao();
		int getTimes = purchaseCfg.getNumber();
		int remainBuyTimes = yxBean.buyTimes >= maxBuyTimes ? 0 : maxBuyTimes - yxBean.buyTimes;
		YouXiaTimesInfoResp.Builder response = YouXiaTimesInfoResp.newBuilder();
		response.setType(type);
		response.setCost(needCost);
		response.setRemainBuyTimes(remainBuyTimes);
		response.setGetTimes(getTimes);
		session.write(response.build());
	}

	public void buyTimes(int id, IoSession session, Builder builder) {
		YouXiaTimesBuyReq.Builder request = (qxmobile.protobuf.YouXiaProtos.YouXiaTimesBuyReq.Builder) builder;
		int type = request.getType();
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("找不到君主，junZhuId:{}", session.getAttribute(SessionAttKey.junZhuId));
			return;
		}
		YouXiaBean yxBean = HibernateUtil.find(YouXiaBean.class, " where junzhuId=" + junZhu.id + " and type =" + type);
		if (yxBean == null) {
			logger.error("游侠购买次数失败，未找到游侠数据，type:{}", type);
			return;
		}
		YouXiaTimesBuyResp.Builder response = YouXiaTimesBuyResp.newBuilder();
		int maxBuyTimes = VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel, VipData.youXiaTimesGet);
		if (yxBean.buyTimes >= maxBuyTimes) {
			response.setResult(2);
			session.write(response.build());
			return;
		}
		Purchase purchaseCfg = PurchaseMgr.inst.getPurchaseCfg(PurchaseConstants.YOUXIA_TIMES, yxBean.buyTimes + 1);
		if (purchaseCfg == null) {
			logger.error("游侠购买次数失败，未找到type:{}的purchase配置", PurchaseConstants.YOUXIA_TIMES);
			return;
		}
		int needMoney = purchaseCfg.getYuanbao();
		if (junZhu.yuanBao < needMoney) {
			response.setResult(1);
			session.write(response.build());
			logger.info("游侠购买次数失败，君主:{} 元宝:{},不足:{}", junZhu.id, junZhu.yuanBao, needMoney);
			return;
		}
		Date date = new Date();
		yxBean.buyTimes += 1;
		yxBean.times += purchaseCfg.getNumber();
		yxBean.lastBuyTime = date;
		logger.info("游侠购买次数成功，君主:{}购买了游侠玩法次数，type:{}，花费元宝:{}", junZhu.name, type, needMoney);
		HibernateUtil.save(yxBean);

		YuanBaoMgr.inst.diff(junZhu, -needMoney, 0, needMoney, YBType.YB_BUY_YOUXIA_CISHU, "购买游侠玩法次数");
		HibernateUtil.save(junZhu);
		JunZhuMgr.inst.sendMainInfo(session);

		response.setResult(0);
		response.setType(type);
		response.setRamainTimes(yxBean.times);
		session.write(response.build());
	}

	public void saoDang(int id, IoSession session, Builder builder) {
		YouXiaSaoDangReq.Builder request = (qxmobile.protobuf.YouXiaProtos.YouXiaSaoDangReq.Builder) builder;
		int guanQiaId = request.getGuanQiaId();

		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("找不到君主，junZhuId:{}", session.getAttribute(SessionAttKey.junZhuId));
			return;
		}

		YouxiaPveTemp pveTempCfg = youxiaPveTempMap.get(guanQiaId);
		if (pveTempCfg == null) {
			logger.error("游侠扫荡失败，未找到id:{}的游侠pve", guanQiaId);
			return;
		}

		List<YouXiaBean> youXiaInfoList = HibernateUtil.list(YouXiaBean.class, " where junzhuId = " + junZhu.id);
		int allwin = 0;
		int allBattle = 0;
		YouXiaBean yxBean = null;
		for (YouXiaBean you : youXiaInfoList) {
			allwin += you.allWinTimes;
			allBattle += you.allBattleTimes;
			if (you.type == pveTempCfg.bigId) {
				yxBean = you;
			}
		}

		// YouXiaBean yxBean = HibernateUtil.find(YouXiaBean.class,
		// " where junzhuId=" + junZhu.id + " and type =" + pveTempCfg.bigId);
		if (yxBean == null) {
			logger.error("youxiaBean初始化失败， junzhuId:{} type:{}", junZhu.id, pveTempCfg.bigId);
			return;
		}
		if (yxBean.times <= 0) {
			logger.error("游侠扫荡失败，今日游侠type:{}挑战次数已用完", pveTempCfg.bigId);
			PveMgr.inst.sendZhanDouInitError(session, "今日挑战次数已用完！");
			BigSwitch.pveGuanQiaMgr.sendPveAndYouxiaSaoDangFail(PD.S_YOUXIA_SAO_DANG_RESP, session, 1, guanQiaId, 0, 0);
			return;
		}
		Date date = new Date();
		int getTongbi = 0;
		int getExp = 0;
		PveSaoDangRet.Builder response = PveSaoDangRet.newBuilder();
		response.setGuanQiaId(guanQiaId);
		List<AwardTemp> awards = new ArrayList<AwardTemp>(1);
		if (pveTempCfg.bigId == 1) {// 只有金币关跟最高击杀成绩有关
			YouXiaRecord yxRecord = HibernateUtil.find(YouXiaRecord.class,
					" where junzhuId=" + junZhu.id + " and guanQiaId=" + guanQiaId);
			if (yxRecord != null) {
				String awardList = "";
				List<YouxiaSaodangTemp> sdList = TempletService.listAll(YouxiaSaodangTemp.class.getSimpleName());
				for (YouxiaSaodangTemp temp : sdList) {
					if (temp.GuanqiaId == guanQiaId && temp.HighestScore == yxRecord.getScore()) {
						awardList = temp.SaodangAward;
						break;
					}
				}
				if (!awardList.equals("")) {
					awards = AwardMgr.inst.parseAwardConf(awardList, "#", ":");
				}
			} else {
				logger.info("游侠扫荡奖励未命中，君主:{},章节:{},最好成绩:{}", junZhu.id, guanQiaId, 0);
				PveSaoDangAward.Builder awardResp = PveSaoDangAward.newBuilder();
				awardResp.setMoney(0);
				awardResp.setExp(0);
				response.addAwards(awardResp);
			}
		} else {
			awards = AwardMgr.inst.getHitAwardList(pveTempCfg.awardId, ",", "=");
		}
		PveSaoDangAward.Builder awardResp = PveSaoDangAward.newBuilder();
		int tongBiTemp = 0;
		int expTemp = 0;
		for (AwardTemp award : awards) {
			if (award.getItemId() == AwardMgr.ITEM_TONGBI_ID) {// 铜币
				getTongbi += award.getItemNum();
				tongBiTemp += award.getItemNum();
			} else if (award.getItemId() == AwardMgr.ITEM_EXP_ID) {// 经验
				expTemp += award.getItemNum();
				getExp += award.getItemNum();
			} else {
				BigSwitch.pveGuanQiaMgr.fillSaoDangAward(awardResp, award);
				AwardMgr.inst.giveReward(session, award, junZhu, false);
				logger.info("游侠扫荡命中奖励 awardId:{}, 添加", award.getAwardId());
			}
		}
		awardResp.setMoney(tongBiTemp);
		awardResp.setExp(expTemp);
		response.addAwards(awardResp);

		yxBean.times = Math.max(0, yxBean.times - 1);
		;
		yxBean.lastBattleTime = date;
		yxBean.allWinTimes += 1;
		yxBean.allBattleTimes += 1;
		HibernateUtil.save(yxBean);
		logger.info("游侠扫荡成功，君主:{}扫荡章节:{}成功，剩余次数:{}", junZhu.id, guanQiaId, yxBean.times);
		junZhu.tongBi += getTongbi;
		JunZhuMgr.inst.addExp(junZhu, getExp);
		HibernateUtil.save(junZhu);
		JunZhuMgr.inst.sendMainInfo(session);
		response.setAllTime(0);
		response.setEndTime(0);
		response.setResult(0);
		ProtobufMsg protobufMsg = new ProtobufMsg();
		protobufMsg.id = PD.S_YOUXIA_SAO_DANG_RESP;
		protobufMsg.builder = response;
		session.write(protobufMsg);
		/*
		 * 扫荡一次加入每日任务
		 */
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS,
				new DailyTaskCondition(junZhu.id, DailyTaskConstants.youXia_activity, 1));
		EventMgr.addEvent(ED.finish_youxia_x, new Object[] { junZhu.id, allwin + 1 });
		EventMgr.addEvent(ED.go_youxia, new Object[] { junZhu.id, allBattle + 1, pveTempCfg.bigId });
	}

	public void requestGuanQiaInfo(int id, IoSession session, Builder builder) {
		YouXiaGuanQiaInfoReq.Builder request = (qxmobile.protobuf.YouXiaProtos.YouXiaGuanQiaInfoReq.Builder) builder;
		int guanQiaId = request.getGuanQiaId();
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("找不到君主，junZhuId:{}", session.getAttribute(SessionAttKey.junZhuId));
			return;
		}
		YouxiaPveTemp pveTempCfg = youxiaPveTempMap.get(guanQiaId);
		if (pveTempCfg == null) {
			logger.error("游侠关卡信息请求失败，未找到id:{}的游侠pve", guanQiaId);
			return;
		}

		YouxiaOpenTime openCfg = youxiaOpenTimeMap.get(pveTempCfg.bigId);
		if (openCfg == null) {
			logger.error("游侠关卡信息请求失败，youxiaOpenTime配置有误，找不到id:{}", pveTempCfg.bigId);
			return;
		}

		YouXiaBean yxBean = HibernateUtil.find(YouXiaBean.class,
				" where junzhuId=" + junZhu.id + " and type =" + pveTempCfg.bigId);
		if (yxBean == null) {
			logger.error("游侠关卡信息请求失败，youxiaBean初始化失败， junzhuId:{} type:{}", junZhu.id, pveTempCfg.bigId);
			return;
		}

		// 判断cd时间
		Date date = new Date();
		int coldTime = 0;
		if (yxBean.lastBattleTime != null) {
			int intervalTime = (int) ((date.getTime() - yxBean.lastBattleTime.getTime()) / 1000);
			if (intervalTime >= openCfg.CD) {
				coldTime = 0;
			} else {
				coldTime = openCfg.CD - intervalTime;
			}
		}

		YouXiaRecord yxRecord = HibernateUtil.find(YouXiaRecord.class,
				" where junzhuId=" + junZhu.id + " and guanQiaId=" + guanQiaId);
		int bestScore = 0;
		if (yxRecord != null) {
			bestScore = yxRecord.getScore();
		}
		boolean isSaoDang = true;
		if (yxRecord == null || yxRecord.getPass() != 1 || coldTime > 0) {
			isSaoDang = false;
		}
		YouXiaGuanQiaInfoResp.Builder response = YouXiaGuanQiaInfoResp.newBuilder();
		response.setBestScore(bestScore);
		response.setSaoDang(isSaoDang);
		response.setTime(coldTime);
		session.write(response.build());
	}

	public void isCanBattleYouXia(JunZhu junZhu, IoSession session) {
		if (junZhu == null) {
			return;
		}
		List<YouXiaBean> list = HibernateUtil.list(YouXiaBean.class, " where junzhuId=" + junZhu.id);
		HashMap<Integer, YouXiaBean> typeMap = new HashMap<Integer, YouXiaBean>();
		list.forEach(b -> typeMap.put(b.type, b));
		for (Integer zhangJieId : youxiaPveTempMap.keySet()) {
			YouxiaPveTemp pveTemp = youxiaPveTempMap.get(zhangJieId);
			if (pveTemp == null) {
				continue;
			}
			// 判断君主等级是否满足条件
			if (junZhu.level < pveTemp.monarchLevel) {
				continue;
			}
			YouXiaBean yxBean = typeMap.get(pveTemp.bigId);
			/*
			 * // 判断今日挑战次数 YouXiaBean yxBean =
			 * HibernateUtil.find(YouXiaBean.class, " where junzhuId=" +
			 * junZhu.id + " and type =" + pveTemp.bigId);
			 */
			if (yxBean == null || yxBean.times <= 0) {
				continue;
			}

			YouxiaOpenTime yxOpen = youxiaOpenTimeMap.get(pveTemp.bigId);
			if (yxOpen == null) {
				continue;
			}
			// 判断今日是否开启本活动玩法
			int dayNum = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
			if (dayNum == 1) {
				dayNum = 7;
			} else {
				dayNum -= 1;
			}
			if (!yxOpen.OpenDay.contains(String.valueOf(dayNum))) {
				continue;
			}
			// 判断cd时间
			Date date = new Date();
			if (yxBean.lastBattleTime != null) {
				int intervalTime = (int) ((date.getTime() - yxBean.lastBattleTime.getTime()) / 1000);
				if (intervalTime < yxOpen.CD) {
					continue;
				}
			}
			FunctionID.pushCanShowRed(junZhu.id, session, FunctionID.youXiaStatus);
			return;
		}
	}

	@Override
	public void proc(Event evt) {
		switch (evt.id) {
		case ED.REFRESH_TIME_WORK:
			IoSession session = (IoSession) evt.param;
			if (session == null) {
				break;
			}
			JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
			if (jz == null) {
				break;
			}
			boolean isOpen = FunctionOpenMgr.inst.isFunctionOpen(FunctionID.shiLian, jz.id, jz.level);
			if (!isOpen) {
				logger.info("君主：{}--游侠：{}的功能---未开启,不推送", jz.id, FunctionID.shiLian);
				break;
			}
			isCanBattleYouXia(jz, session);
			break;
		default:
			logger.error("错误事件参数", evt.id);
			break;
		}

	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
	}

	public void requestTypePassInfo(int id, IoSession session, Builder builder) {
		YouXiaTypeInfoReq.Builder request = (qxmobile.protobuf.YouXiaProtos.YouXiaTypeInfoReq.Builder) builder;
		int type = request.getType();
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("游侠玩法通关信息请求失败，找不到君主，junZhuId:{}", session.getAttribute(SessionAttKey.junZhuId));
			return;
		}

		YouxiaOpenTime openCfg = youxiaOpenTimeMap.get(type);
		if (openCfg == null) {
			logger.error("游侠玩法通关信息请求失败，youxiaOpenTime配置有误，type:{}", type);
			return;
		}

		List<YouxiaPveTemp> pveTempList = youxiaPveTempListByType.get(type);
		if (pveTempList == null) {
			logger.error("游侠玩法通关信息请求失败，游侠关卡找不到，type:{}", type);
			return;
		}

		List<YouXiaRecord> yxRecordList = HibernateUtil.list(YouXiaRecord.class,
				" where junzhuId=" + junZhu.id + " and type=" + type);
		YouXiaTypeInfoResp.Builder response = YouXiaTypeInfoResp.newBuilder();
		response.setType(type);
		if (yxRecordList == null) {
			session.write(response.build());
			return;
		}

		for (YouXiaRecord record : yxRecordList) {
			for (YouxiaPveTemp pveCfg : pveTempList) {
				if (record.getGuanQiaId() == pveCfg.id && record.getPass() == GUANQIA_PASS) {
					response.addPassGuanQiaId(record.getGuanQiaId());
				}
			}
		}
		session.write(response.build());
	}

}
