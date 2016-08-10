package com.qx.pve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.store.MemcachedCRUD;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.LegendPveTemp;
import com.manu.dynasty.template.MibaoSkill;
import com.manu.dynasty.template.Purchase;
import com.manu.dynasty.template.PveBigAward;
import com.manu.dynasty.template.PveStar;
import com.manu.dynasty.template.PveTemp;
import com.manu.dynasty.template.VIP;
import com.manu.dynasty.template.ZhuXian;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.FunctionOpenMgr;
import com.qx.achievement.AchievementCondition;
import com.qx.achievement.AchievementConstants;
import com.qx.award.AwardMgr;
import com.qx.award.DropRateBean;
import com.qx.award.DropRateDao;
import com.qx.chonglou.ChongLouMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.huangye.BuZhenHYPve;
import com.qx.huangye.BuZhenHYPvp;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.junzhu.JzKeji;
import com.qx.mibao.MiBaoSkillDB;
import com.qx.mibao.MibaoMgr;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.pvp.LveDuoMgr;
import com.qx.pvp.PvpMgr;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.task.GameTaskMgr;
import com.qx.util.TableIDCreator;
import com.qx.vip.VipMgr;
import com.qx.yabiao.YaBiaoHuoDongMgr;
import com.qx.youxia.BuZhenYouXia;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

import qxmobile.protobuf.BattlePveInit.BattleReplayData;
import qxmobile.protobuf.BattlePveInit.BattleReplayReq;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.PveLevel.BuZhenReport;
import qxmobile.protobuf.PveLevel.GetNotGetAwardZhangJieResp;
import qxmobile.protobuf.PveLevel.GetPassZhangJieAwardReq;
import qxmobile.protobuf.PveLevel.GetPveStarAward;
import qxmobile.protobuf.PveLevel.GuanQiaInfo;
import qxmobile.protobuf.PveLevel.GuanQiaInfoRequest;
import qxmobile.protobuf.PveLevel.GuanQiaMaxId;
import qxmobile.protobuf.PveLevel.MibaoSelect;
import qxmobile.protobuf.PveLevel.MibaoSelectResp;
import qxmobile.protobuf.PveLevel.PvePageReq;
import qxmobile.protobuf.PveLevel.PveSaoDangAward;
import qxmobile.protobuf.PveLevel.PveSaoDangReq;
import qxmobile.protobuf.PveLevel.PveSaoDangRet;
import qxmobile.protobuf.PveLevel.PveStarAwardItem;
import qxmobile.protobuf.PveLevel.PveStarAwardSate;
import qxmobile.protobuf.PveLevel.PveStarAwards;
import qxmobile.protobuf.PveLevel.PveStarGetSuccess;
import qxmobile.protobuf.PveLevel.ResetCQTimesBack;
import qxmobile.protobuf.PveLevel.ResetCQTimesReq;
import qxmobile.protobuf.PveLevel.SaoDangAwardItem;
import qxmobile.protobuf.PveLevel.Section;
import qxmobile.protobuf.PveLevel.StarInfo;
import qxmobile.protobuf.PveLevel.YuanJun;
import qxmobile.protobuf.PveLevel.YuanZhuListReturn;

/**
 * @author 康建虎
 * 
 */
public class PveGuanQiaMgr {
	public static Map<Integer, BattleReplayData> replayCache = new HashMap<Integer, BattleReplayData>();
	public static Logger log = LoggerFactory.getLogger(PveGuanQiaMgr.class.getSimpleName());
	public GuanQiaStartRewardBean[] startRewardStatus;
	public PveRecordDao recordMgr;
	public static PveRecord nullRecord;
	public static Map<Integer, PveStar> starMap;
	public static byte DEFAULT_CAN_FIGHT_CHUANQI = 1; // 默认可以攻打的传奇关卡章节id

	public PveGuanQiaMgr() {
		nullRecord = new PveRecord();
		// 改主键不自增
		// 2015年4月17日16:57:30int改为long
		nullRecord.dbId = (TableIDCreator.getTableID(PveRecord.class, 1L));
		nullRecord.star = -1;
		recordMgr = new PveRecordDao();
		startRewardStatus = new GuanQiaStartRewardBean[4];// 3/6/9/12
		for (int i = 3; i <= 12; i += 3) {
			GuanQiaStartRewardBean r = new GuanQiaStartRewardBean();
			r.start = i;
			startRewardStatus[i / 3 - 1] = r;
		}
		initData();
	}

	public void initData() {
		List<?> list = TempletService.listAll("PveStar");
		starMap = new HashMap<Integer, PveStar>();
		for (Object o : list) {
			PveStar s = (PveStar) o;
			s.parsedArr = AwardMgr.inst.parseAwardConf(s.award);
			starMap.put(s.starId, s);
		}
	}

	public void addAcheEvent(long pid, int star, Integer guanQiaId) {
		PveTemp pveTemp = PveMgr.inst.getId2Pve().get(guanQiaId);
		int zhangjie = pveTemp.bigId;
		int acheType = -1;
		if (zhangjie == 1)
			return;
		switch (zhangjie) {
		case 2:
			acheType = AchievementConstants.type_guoguan_2_star;
			break;
		case 3:
			acheType = AchievementConstants.type_guoguan_3_star;
			break;
		case 4:
			acheType = AchievementConstants.type_guoguan_4_star;
			break;
		case 5:
			acheType = AchievementConstants.type_guoguan_5_star;
			break;
		case 6:
			acheType = AchievementConstants.type_guoguan_6_star;
			break;
		case 7:
			acheType = AchievementConstants.type_guoguan_7_star;
			break;
		case 8:
			acheType = AchievementConstants.type_guoguan_8_star;
			break;
		case 9:
			acheType = AchievementConstants.type_guoguan_9_star;
			break;
		case 10:
			acheType = AchievementConstants.type_guoguan_10_star;
			break;
		case 11:
			acheType = AchievementConstants.type_guoguan_11_star;
			break;
		case 12:
			acheType = AchievementConstants.type_guoguan_12_star;
			break;
		case 13:
			acheType = AchievementConstants.type_guoguan_13_star;
			break;
		case 14:
			acheType = AchievementConstants.type_guoguan_14_star;
			break;
		case 15:
			acheType = AchievementConstants.type_guoguan_15_star;
			break;
		case 16:
			acheType = AchievementConstants.type_guoguan_16_star;
			break;
		case 17:
			acheType = AchievementConstants.type_guoguan_17_star;
			break;
		case 18:
			acheType = AchievementConstants.type_guoguan_18_star;
			break;
		default:
			break;
		}
		EventMgr.addEvent(pid,ED.ACHIEVEMENT_PROCESS, new AchievementCondition(pid, acheType, star));
		EventMgr.addEvent(pid,ED.ACHIEVEMENT_PROCESS,
				new AchievementCondition(pid, AchievementConstants.type_guoguan_times, 1));

	}

	public List<PveRecord> getUserRecords(int uid) {
		// HibernateUtil.
		return null;
	}

	public int findBigIndex(int id) {
		List<PveTemp> list = TempletService.listAll(PveTemp.class.getSimpleName());
		int idx = -1;
		for (PveTemp p : list) {
			idx++;
			if (p.bigId == id) {
				break;
			}
		}
		return idx;
	}

	public void getPageInfo(int id, IoSession session, Builder builder) {
		PvePageReq.Builder req = (qxmobile.protobuf.PveLevel.PvePageReq.Builder) builder;
		Section.Builder b = Section.newBuilder();
		List<PveTemp> list = TempletService.listAll(PveTemp.class.getSimpleName());
		int reqSection = req.getSSection();
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			log.error("session without player id {}", session);
			return;
		}
		Map<Integer, PveRecord> rMap = recordMgr.getRecords(junZhuId);
		log.info("{} PVE记录大小 {},请求章节{}", junZhuId, rMap.size(), reqSection);
		int maxCqPassId = 0;
		int maxGuanQiaId = 0;
		int curGuanQiaBigId = 0;
		int curCqBigId = DEFAULT_CAN_FIGHT_CHUANQI;// 可打的传奇关卡id，默认可以攻打第一章
		{
			Iterator<PveRecord> it = rMap.values().iterator();
			while (it.hasNext()) {
				PveRecord pr = it.next();
				maxGuanQiaId = Math.max(maxGuanQiaId, pr.guanQiaId);
				if (pr.chuanQiPass) {
					maxCqPassId = Math.max(pr.guanQiaId, maxCqPassId);
				}
			}
			if (maxCqPassId > 0) {
				Iterator<PveTemp> confIt = list.iterator();
				while (confIt.hasNext()) {
					PveTemp conf = (PveTemp) confIt.next();
					if (maxGuanQiaId == conf.id) {
						curGuanQiaBigId = conf.bigId;
						if (confIt.hasNext()) {
							PveTemp confNext = confIt.next();
							// 还是同一章节传奇关卡得-1
							if (confNext.bigId == conf.bigId) {
								curGuanQiaBigId -= 1;
							}
						}
						break;
					}
				}
				Iterator<PveTemp> confIt2 = list.iterator();
				while (confIt2.hasNext()) {
					PveTemp conf = (PveTemp) confIt2.next();
					if (conf.id == maxCqPassId) {
						curCqBigId = conf.bigId;
						if (confIt2.hasNext()) {
							PveTemp confNext = confIt2.next();
							// 如果下一配置不是同一章节则big+1
							if (confNext.bigId != conf.bigId) {
								curCqBigId = confNext.bigId;
							}
						}
						break;
					}
				}
			}
			curCqBigId = Math.min(curGuanQiaBigId, curCqBigId);
			curCqBigId = Math.max(DEFAULT_CAN_FIGHT_CHUANQI, curCqBigId);
		}

		int zhangJieIdMax = getZhangJieIdMax(list, rMap);
		if (rMap.size() == 0) {
			if (reqSection <= 0) {
				reqSection = 1;
			}
		} else if (reqSection <= 0) {
			reqSection = zhangJieIdMax;
			// 如果有未领取通关奖励的章节id，则发送最小的章节信息
			int realZhangJieId = reqSection;
			for (int i = 1; i < reqSection; i++) {
				Integer lastGuanQiaId = PveMgr.lastGuanQiaOfZhang.get(i);
				for (Map.Entry<Integer, PveRecord> entry : rMap.entrySet()) {
					Integer zhangJieId = entry.getKey();
					if (lastGuanQiaId != null && zhangJieId.equals(lastGuanQiaId)) {
						PveRecord record = entry.getValue();
						if (!record.isGetAward) {
							realZhangJieId = Math.min(realZhangJieId, i);
						}
					}
				}
			}
			reqSection = realZhangJieId;
		}
		int size = list.size();
		int maxSec = list.get(size - 1).bigId;
		int i = findBigIndex(reqSection);// 查找指定章节的起始关卡位置。
		if (i < 0) {
			reqSection = maxSec;
			i = findBigIndex(reqSection);// 查找指定章节的起始关卡位置。
		}
		b.setSSection(reqSection);
		// 领奖之后才能打关卡
		int maxRwIdx = FunctionOpenMgr.inst.getMaxAwardRenWuOrderIdx(junZhuId);
		for (; i < size; i++) {
			PveTemp pveTemp = list.get(i);
			if (pveTemp.bigId != reqSection) {
				break;
			}

			PveRecord r = rMap.get(pveTemp.id);
			if (r == null) {
				r = nullRecord;
			}
			qxmobile.protobuf.PveLevel.Level.Builder lb = qxmobile.protobuf.PveLevel.Level.newBuilder();
			lb.setSLevel(pveTemp.monarchLevel);
			lb.setSPass(r.star >= 0);
			lb.setType(pveTemp.chapType);
			lb.setGuanQiaId(pveTemp.id);
			lb.setSStarNum(r.star);
			// 1是精英关卡，才有胜利评价。
			lb.setWinLevel(r.star);
			// 星级及奖励领取情况
			String achieveStr = String.valueOf(r.achieve);
			achieveStr = StringUtils.leftPad(achieveStr, 3, "0");
			String achieveRewardStateStr = String.valueOf(r.achieveRewardState);
			achieveRewardStateStr = StringUtils.leftPad(achieveRewardStateStr, 3, "0");
			for (int index = 0; index < 3; index++) {
				StarInfo.Builder starInfo = StarInfo.newBuilder();
				if (index == 0) {
					starInfo.setStarId(pveTemp.star1);
				} else if (index == 1) {
					starInfo.setStarId(pveTemp.star2);
				} else if (index == 2) {
					starInfo.setStarId(pveTemp.star3);
				}
				if (achieveStr.charAt(index) == '1') {
					starInfo.setFinished(true);
				} else {
					starInfo.setFinished(false);
				}
				if (achieveRewardStateStr.charAt(index) == '1') {
					starInfo.setGetRewardState(true);
				} else {
					starInfo.setGetRewardState(false);
				}
				lb.addStarInfo(starInfo);
			}
			// 传奇关卡星级信息
			String cqStarStr = String.valueOf(r.cqStar);
			cqStarStr = StringUtils.leftPad(cqStarStr, 3, "0");
			String cqStarRewardStateStr = String.valueOf(r.cqStarRewardState);
			cqStarRewardStateStr = StringUtils.leftPad(cqStarRewardStateStr, 3, "0");
			LegendPveTemp legendPveTemp = PveMgr.inst.legendId2Pve.get(pveTemp.id);
			if (legendPveTemp != null) {
				for (int index = 0; index < 3; index++) {
					StarInfo.Builder starInfo = StarInfo.newBuilder();
					if (index == 0) {
						starInfo.setStarId(legendPveTemp.star1);
					} else if (index == 1) {
						starInfo.setStarId(legendPveTemp.star2);
					} else if (index == 2) {
						starInfo.setStarId(legendPveTemp.star3);
					}
					if (cqStarStr.charAt(index) == '1') {
						starInfo.setFinished(true);
					} else {
						starInfo.setFinished(false);
					}
					if (cqStarRewardStateStr.charAt(index) == '1') {
						starInfo.setGetRewardState(true);
					} else {
						starInfo.setGetRewardState(false);
					}
					lb.addCqStarInfo(starInfo);
				}
			}

			lb.setChuanQiPass(r.chuanQiPass);// 传奇关卡信息
			lb.setPingJia(r.cqWinLevel);// 传奇关卡星级评价
			if (pveTemp.RenWuLimit <= 0) {
				lb.setRenWuId(0);
			} else {
				ZhuXian rwConf = GameTaskMgr.inst.zhuxianTaskMap.get(pveTemp.RenWuLimit);
				if (rwConf == null) {
					lb.setRenWuId(0);// 没有该任务，可以攻打。
				} else if (rwConf.orderIdx <= maxRwIdx) {
					// 有该主线任务并且该任务已完成
					lb.setRenWuId(0);
				} else {
					lb.setRenWuId(pveTemp.RenWuLimit);
				}
			}
			b.addSAllLevel(lb);
			// if(r == nullRecord){
			// break;//已经找到了一个没有打过的关卡。
			// }
		}
		//
		for (GuanQiaStartRewardBean starInfo : startRewardStatus) {
			PveStarAwardSate.Builder stb = PveStarAwardSate.newBuilder();
			stb.setSStarNum(starInfo.start);
			stb.setSState(starInfo.pick);
			b.addSStarAwardState(stb);
		}

		b.setMaxCqPassId(curCqBigId);
		b.setSectionMax(zhangJieIdMax);
		session.write(b.build());
		log.info("{} 请求章节 {}", junZhuId, reqSection);
	}

	public int getZhangJieIdMax(List<PveTemp> list, Map<Integer, PveRecord> rMap) {
		int result = 1;
		// 计算最大的关卡ID
		Iterator<Integer> it = rMap.keySet().iterator();
		while (it.hasNext()) {
			Integer integer = (Integer) it.next();
			result = Math.max(result, integer);
		}
		// 根据最大的关卡ID计算章节
		boolean checkEnd = false;
		for (PveTemp p : list) {
			if (p.id == result) {
				result = p.bigId;
				checkEnd = true;
			} else if (p.id > result) {// 还没找到合适的，但是已经出现下一级关卡了。
				// 2015年4月7日15:51:59 发现就数据的pve记录已经不在了。
				result = p.bigId;
				break;
			} else if (checkEnd) {// 如果第一章节都打完了，就是下面这个情况。
				if (p.bigId != result) {
					result = p.bigId;
				}
				break;
			}
		}
		return Math.max(result, 1);
	}

	public void queryStartRewards(int id, IoSession session, Builder builder) {
		GetPveStarAward.Builder b = (qxmobile.protobuf.PveLevel.GetPveStarAward.Builder) builder;
		PveStarAwards.Builder ret = PveStarAwards.newBuilder();
		int star = b.getSStarNum();
		PveStarAwardItem.Builder ib = PveStarAwardItem.newBuilder();
		ib.setSAwardName("元宝");
		ib.setSAwardNum(0);
		switch (star) {
		case 3:
			ib.setSAwardNum(20);
			PveStarAwardItem.Builder ib2 = PveStarAwardItem.newBuilder();
			ib2.setSAwardName("铜币");
			ib2.setSAwardNum(10000);
			ret.addSAwards(ib2);
			break;
		case 6:
			ib.setSAwardNum(30);
			break;
		case 9:
			ib.setSAwardNum(50);
			break;
		case 12:
			ib.setSAwardNum(80);
			break;
		}
		ret.addSAwards(ib);
		session.write(ret.build());
		log.info("sent start reward {}", star);
	}

	public void lingQuStartRewards(int id, IoSession session, Builder builder) {
		GetPveStarAward.Builder b = (qxmobile.protobuf.PveLevel.GetPveStarAward.Builder) builder;
		// int star = b.getSStarNum();
		// int starIdx = star % 10;
		// int guanQiaId = star / 10;
		int guanQiaId = b.getGuanQiaId();
		int starId = b.getSStarNum();
		boolean chuanQiMark = b.getIsChuanQi();
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			return;
		}
		JunZhu jz = HibernateUtil.find(JunZhu.class, junZhuId);
		if (jz == null) {
			log.error("君主不存在");
			return;
		}
		PveRecord r = BigSwitch
				.pveGuanQiaMgr.recordMgr.get(junZhuId, guanQiaId);
		if (r == null) {
			sendError(session, "您没有完成此关卡 " + guanQiaId);
			return;
		}
		log.info("{}请求领取星级奖励guanQiaId-{},星级id-{},", junZhuId, guanQiaId, starId);
		PveTemp pveTemp = null;
		if (chuanQiMark) {
			pveTemp = PveMgr.inst.legendId2Pve.get(guanQiaId);
		} else {
			pveTemp = PveMgr.inst.getId2Pve().get(guanQiaId);
		}
		if (pveTemp == null) {
			log.error("请求pve章节id错误，zhangJieId:{}", guanQiaId);
			return;
		}
		int star1 = pveTemp.star1;
		int star2 = pveTemp.star2;
		int star3 = pveTemp.star3;
		int getStar = chuanQiMark ? r.cqStar : r.achieve;
		int starRewardState = chuanQiMark ? r.cqStarRewardState : r.achieveRewardState;
		int key = 0;
		if (starId == star1) {
			key = 100;
		} else if (starId == star2) {
			key = 10;
		} else if (starId == star3) {
			key = 1;
		} else {
			sendError(session, "请求数据错误 starId:" + starId);
			return;
		}
		if ((key & getStar) != key) {
			log.info("星级数据错误key {}, match {} star {}", key, (key & getStar), getStar);
			sendError(session, "您还没有获得这个星星。");
			return;
		}
		if ((key & starRewardState) == key) {
			log.info("已领数据错误key {}, match {} startRewardState {}", key, (key & starRewardState), starRewardState);
			PveStarGetSuccess.Builder ret = PveStarGetSuccess.newBuilder();
			ret.setGuanQiaId(guanQiaId);
			ret.setSResult(false);
			ret.setSStarNum(b.getSStarNum());
			session.write(ret.build());
			return;
		}
		if (chuanQiMark) {
			r.cqStarRewardState += key;
		} else {
			r.achieveRewardState += key;
		}
		HibernateUtil.save(r);
		log.info("{}领取星级奖励 guanQiaId{}, starId{}", junZhuId, guanQiaId, starId);
		// 获得物品奖励配置
		PveStar ps = starMap.get(starId);
		if (ps == null) {
			log.info("该关卡:{}没有星级:{}物品奖励", guanQiaId, starId);
			return;
		}
		// 发送物品奖励
		addAward(ps, session, jz);
		JunZhuMgr.inst.sendMainInfo(session,jz, false);
		PveStarGetSuccess.Builder ret = PveStarGetSuccess.newBuilder();
		ret.setGuanQiaId(guanQiaId);
		ret.setSResult(true);
		ret.setSStarNum(b.getSStarNum());
		session.write(ret.build());
		log.info("ling qu start reward {}", b.getSStarNum());
		// 添加完成精英领取奖励任务
		EventMgr.addEvent(junZhuId,ED.GET_START_PVE_AWARD, new Object[] { junZhuId, guanQiaId });
	}

	public void addAward(PveStar ps, IoSession session, JunZhu jz) {
		List<AwardTemp> awardList = ps.parsedArr;
		for (AwardTemp award : awardList) {
			AwardMgr.inst.giveReward(session, award, jz,false);
			log.info("君主:{}, 领取星级奖励物品:{}, 数量：{}", jz.id, award.itemId, award.itemNum);
		}
	}

	public void fillAwardTemp(String[] gInfo, AwardTemp awardTemp) {
		int type = Integer.parseInt(gInfo[0]);
		awardTemp.itemType = type;
		awardTemp.itemNum = Integer.parseInt(gInfo[2]);
		switch (type) {
		case 10:// type为10时，ginfo[1]表示的是AwardTemp表的awardId
			AwardTemp temp = AwardMgr.inst.calcAwardTemp(Integer.parseInt(gInfo[1]));
			if (temp != null) {
				awardTemp.itemId = temp.itemId;
				awardTemp.itemType = temp.itemType;
			}
			break;
		default:
			awardTemp.itemId = Integer.parseInt(gInfo[1]);
			break;
		}
	}

	public void saveReplay(int id, IoSession session, Builder builder) {
		BattleReplayData.Builder b = (qxmobile.protobuf.BattlePveInit.BattleReplayData.Builder) builder;
		replayCache.put(b.getBattleId(), b.build());
		log.info("add cache {}", b.getBattleId());
	}

	public void sendReplay(int id, IoSession session, Builder builder) {
		BattleReplayReq.Builder b = (qxmobile.protobuf.BattlePveInit.BattleReplayReq.Builder) builder;
		BattleReplayData msg = replayCache.get(b.getBattleId());
		if (msg == null) {
			log.error("没有找到回放数据{}", b.getBattleId());
			return;
		}
		session.write(msg);
		log.info("发送回放数据 {}", msg.getBattleId());
	}
	public PveRecord getR(long jzId, int qid){
		return recordMgr.get(jzId, qid);
	}
	public void saoDang(int id, IoSession session, Builder builder) {
		PveSaoDangReq.Builder req = (qxmobile.protobuf.PveLevel.PveSaoDangReq.Builder) builder;
		int guanQiaId = req.getGuanQiaId();
		int saoDangTimes = req.getTimes();

		boolean cq = false;
		if (guanQiaId < 0) {
			cq = true;
			guanQiaId = -guanQiaId;
		}
		PveTemp conf = null;
		if (cq) {
			conf = PveMgr.inst.legendId2Pve.get(guanQiaId);
		} else {
			conf = PveMgr.inst.id2Pve.get(guanQiaId);
		}

		if (conf == null) {
			sendError(session, "没有找到关卡" + guanQiaId);
			return;
		}
		// 1是精英关卡，
		if (cq) {
		} else if (conf.chapType != 1) {
			sendError(session, "不是精英关卡，不能扫荡");
			return;
		}
		int costTiLi = conf.useHp * saoDangTimes;
		if (costTiLi <= 0) {
			return;
		}
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			return;
		}
		if (junzhu.tiLi < costTiLi) {
			sendError(session, "体力不足");
			return;
		}
		PveRecord r = getR(junzhu.id, guanQiaId);
		if (r == null || r.star < 0) {
			sendError(session, "请先打通此关卡。");
			return;
		}
		if (cq && (r.cqPassTimes >= CanShu.DAYTIMES_LEGENDPVE
				|| r.cqPassTimes + saoDangTimes > CanShu.DAYTIMES_LEGENDPVE)) {
			log.info("传奇关:{}今日通关次数已满", guanQiaId);
			sendPveAndYouxiaSaoDangFail(PD.S_PVE_SAO_DANG, session, 2, guanQiaId, 0, 0);
			return;
		}
		SaoDangBean sd = HibernateUtil.find(SaoDangBean.class, junzhu.id);
		boolean first = false;
		if (sd == null) {
			sd = insertSaoDangBean(junzhu.id);
			first = true;
		}
		Date today = new Date();
		VIP vip = VipMgr.INSTANCE.getVIPByVipLevel(junzhu.vipLevel);
		if (vip == null) {
			vip = new VIP();
			vip.saodangFree = 10;
		}
		// change 20150901
		if (DateUtils.isTimeToReset(sd.saoDangResetTime, CanShu.REFRESHTIME_PURCHASE)) {
			sd.jySaoDangTimes = 0;
			sd.saoDangResetTime = today;
		} else {
			// 今日已扫荡次数是否已经超过最大次数 今日已扫荡次数加上本次扫荡次数是否超过最大次数
			if (sd.jySaoDangTimes >= vip.saodangFree || sd.jySaoDangTimes + saoDangTimes > vip.saodangFree) {
				sendPveAndYouxiaSaoDangFail(PD.S_PVE_SAO_DANG, session, 1, guanQiaId, vip.saodangFree,
						sd.jySaoDangTimes);
				return;
			}
		}
		
		PveSaoDangRet.Builder ret = PveSaoDangRet.newBuilder();
		ret.setResult(0);
		ret.setGuanQiaId(guanQiaId);
		ret.setEndTime(sd.jySaoDangTimes);
		ret.setAllTime(vip.saodangFree);
		int sdTimesLoop = saoDangTimes;
		int getExpTotal = 0;
		int getTongBiTotal = 0;
		List<AwardTemp> getAwardList = new ArrayList<>();
		Map<Integer, DropRateBean> dropRateMap = DropRateDao.inst.getMap(junzhu.id);
		while (sdTimesLoop > 0) {
			getTongBiTotal += conf.money;
			getExpTotal += conf.exp;
			PveSaoDangAward.Builder awardBuilder = PveSaoDangAward.newBuilder();
			int getExp = conf.exp;
			int getTongbi = conf.money;
			int[] arr = conf.awardConf;
			if (guanQiaId == 100203 && first) {// 新手引导要求必须出道具
				first = false;
				int[] force = Arrays.copyOf(arr, arr.length + 2);
				force[arr.length] = 10000;
				force[arr.length + 1] = 100;
				arr = force;
				log.info("{} 首次扫荡，强制掉落", junzhu.id);
			}
			List<Integer> hitAwardIdList = AwardMgr.inst.getHitAwardId(arr, junzhu.id, dropRateMap);
			for (Integer awardId : hitAwardIdList) {
				AwardTemp calcV = AwardMgr.inst.calcAwardTemp(awardId);
				if (calcV == null) {
					continue;
				}
				if (calcV.itemId == AwardMgr.ITEM_TONGBI_ID) {// 铜币
					getTongbi += calcV.itemNum;
					getTongBiTotal += calcV.itemNum;
				} else if (calcV.itemId == AwardMgr.ITEM_EXP_ID) {// 经验
					getExp += calcV.itemNum;
					getExpTotal += calcV.itemNum;
				} else {
					fillSaoDangAward(awardBuilder, calcV);
					AwardMgr.inst.battleAwardCounting(getAwardList, calcV);
					log.info("扫荡命中奖励 awardId:{}, 添加", calcV.awardId);
				}
			}
			awardBuilder.setExp(getExp);
			awardBuilder.setMoney(getTongbi);
			ret.addAwards(awardBuilder);
			sdTimesLoop -= 1;
		}
		for(DropRateBean bean : dropRateMap.values()) {
			if(bean.dbOp == 'I'){
				// 在getHitAwardId方法里已经将是新记录的数据放到缓存里了
				HibernateUtil.insert(bean);
				bean.dbOp = 'N';
			}else if(bean.dbOp == 'U'){
				HibernateUtil.update(bean);
				bean.dbOp = 'N';
			}
		}
		junzhu.tongBi += getTongBiTotal;
		JunZhuMgr.inst.addExp(junzhu, getExpTotal);
		JunZhuMgr.inst.updateTiLi(junzhu, -costTiLi, "扫荡");
		HibernateUtil.update(junzhu);
		JunZhuMgr.inst.sendMainInfo(session, junzhu,false);
		sd.jySaoDangTimes += saoDangTimes;
		sd.jyAllSaoDangTimes += saoDangTimes;
		Cache.saoDangCache.put(junzhu.id, sd);
		HibernateUtil.save(sd);
		if (cq) {
			// 传奇关卡需要处理挑战次数
			r.cqPassTimes += saoDangTimes;
			HibernateUtil.save(r);
		}
		for(AwardTemp award : getAwardList) {
			AwardMgr.inst.giveReward(session, award, junzhu, false);
		}
		//BagMgr.inst.sendBagInfo(0, session, null);
		session.write(ret.build());
		log.info("{}扫荡[{}]关卡{}次数{}", junzhu.id, cq ? "传奇" : "精英", guanQiaId, req.getTimes());
		//
		if (cq) {
			// 每日任务中记录完成传奇关卡一次（不论输赢）
			EventMgr.addEvent(junzhu.id, ED.DAILY_TASK_PROCESS,
					new DailyTaskCondition(junzhu.id, DailyTaskConstants.chuanqi_guanqia_3, req.getTimes()));
		} else {
			// 每日任务中记录完成过关斩将1次(无论输赢)
			EventMgr.addEvent(junzhu.id, ED.DAILY_TASK_PROCESS,
					new DailyTaskCondition(junzhu.id, DailyTaskConstants.guoguan_5_id, req.getTimes()));
			// 主线任务: 扫荡1次普通关卡（任意关卡都行） 20190916
			EventMgr.addEvent(junzhu.id, ED.saoDang, new Object[] { junzhu.id });
		}
		
	}

	public int[] fixAwardConf(int guanQiaId, PveTemp conf, boolean first) {
		int[] awardConf = conf.awardConf;
		if (guanQiaId == 100203 && first) {// 新手引导要求必须出道具
			int[] force = Arrays.copyOf(awardConf, awardConf.length + 2);
			force[awardConf.length] = 10000;
			force[awardConf.length + 1] = 100;
			awardConf = force;
		}
		return awardConf;
	}

	public void sendPveAndYouxiaSaoDangFail(int cmd, IoSession session, int result, int guanQiaId, int allTimes,
			int endTimes) {
		PveSaoDangRet.Builder ret = PveSaoDangRet.newBuilder();
		ret.setResult(result);
		ret.setGuanQiaId(guanQiaId);
		ret.setEndTime(allTimes);
		ret.setAllTime(endTimes);
		ProtobufMsg protobufMsg = new ProtobufMsg();
		protobufMsg.id = cmd;
		protobufMsg.builder = ret;
		session.write(protobufMsg);
	}

	public void fillSaoDangAward(PveSaoDangAward.Builder award, AwardTemp calcV) {
		if (calcV != null) {
			// 如果有重复的奖励，则直接增加奖励的数量
			List<SaoDangAwardItem.Builder> haveAwardList = award.getAwardItemsBuilderList();
			for (SaoDangAwardItem.Builder item : haveAwardList) {
				if (item.getItemType() == calcV.itemType && item.getItemId() == calcV.itemId) {
					item.setItemNum(item.getItemNum() + calcV.itemNum);
					item.build();
					return;
				}
			}
			SaoDangAwardItem.Builder sdAwardItem = SaoDangAwardItem.newBuilder();
			sdAwardItem.setItemType(calcV.itemType);
			sdAwardItem.setItemId(calcV.itemId);
			sdAwardItem.setItemNum(calcV.itemNum);
			award.addAwardItems(sdAwardItem);
		}
	}

	public void sendError(IoSession session, String msg) {
		if (session == null) {
			log.warn("session is null: {}", msg);
			return;
		}
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(1);
		test.setErrorDesc(msg);
		session.write(test.build());
		log.debug("sent keji info");
	}

	public void sendError(IoSession session, String msg, short PDid, int errorCode) {
		if (session == null) {
			log.warn("session is null: {}", msg);
			return;
		}
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(errorCode);
		test.setErrorDesc(msg);
		ProtobufMsg pm = new ProtobufMsg();
		pm.id = PDid;
		pm.builder = test;
		session.write(pm);
	}

	public void sendGuanQiaInfo(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求关卡信息错误1，找不到君主");
			return;
		}
		qxmobile.protobuf.PveLevel.GuanQiaInfo.Builder ret = GuanQiaInfo.newBuilder();
		GuanQiaInfoRequest.Builder req = (qxmobile.protobuf.PveLevel.GuanQiaInfoRequest.Builder) builder;
		int guanQiaId = req.getGuanQiaId();
		ret.setGuanQiaId(guanQiaId);
		PveTemp conf = PveMgr.inst.id2Pve.get(guanQiaId);
		if (req.getType() == 2) {// 传奇
			conf = PveMgr.inst.legendId2Pve.get(guanQiaId);
		}
		if (conf == null) {
			sendError(session, "没有这个关卡:" + guanQiaId);
			log.error("请求关卡信息错误2，未找到请求的关卡id:{}信息配置,关卡类型:{}", guanQiaId, req.getType());
			return;
		}

		ret.setDesc(HeroService.getNameById(conf.smaDesc));
		ret.setTili(conf.useHp);
		long junZhuId = jz.id;
		BuZhenMibaoBean mibaoBean = HibernateUtil.find(BuZhenMibaoBean.class, junZhuId);
		if (mibaoBean == null) {
			mibaoBean = insertBuZhenMibaoBean(jz);
		}
		int zuheId = mibaoBean == null ? -1 : mibaoBean.zuheId;
		ret.setZuheId(zuheId);
		// FIXME 0.97不需要了，但先留着
		for (int i = 0; i < 3; i++) {
			ret.addMibaoIds(-1L);
		}
		//
		PveRecord r = getR(jz.id, guanQiaId);
		if (r == null) {
			r = nullRecord;
		}
		SaoDangBean sd = HibernateUtil.find(SaoDangBean.class, jz.id);
		if (sd == null) {
			sd = insertSaoDangBean(jz.id);
		}
		Purchase purchaseCfg = PurchaseMgr.inst.getPurchaseCfg(PurchaseConstants.CHUANQI_REST, r.cqResetTimes + 1);
		if (purchaseCfg == null) {
			log.error("找不到purchase配置，type:{}", PurchaseConstants.CHUANQI_REST);
			return;
		}

		Date today = new Date();
		// change 20150901
		if (DateUtils.isTimeToReset(r.cqResetDate, CanShu.REFRESHTIME_PURCHASE)) {
			r.cqResetTimes = 0;
			r.cqPassTimes = 0;
			r.cqResetDate = today;
			HibernateUtil.save(r);
		}
		VIP vip = VipMgr.INSTANCE.getVIPByVipLevel(jz.vipLevel);
		int cqResetLeft = 0;
		int jySaoDangDayTimes = 10;
		int cqSaoDangDayTimes = 10;
		if (vip != null) {
			cqResetLeft = Math.max(0, vip.legendPveRefresh - r.cqResetTimes);
			jySaoDangDayTimes = Math.max(0, vip.saodangFree);
			cqSaoDangDayTimes = Math.max(0, vip.saodangFree);
		}
		// change 20150901
		if (DateUtils.isTimeToReset(sd.saoDangResetTime, CanShu.REFRESHTIME_PURCHASE)) {
			sd.jySaoDangTimes = 0;
			HibernateUtil.save(sd);
		}
		qxmobile.protobuf.PveLevel.GuanQiaInfo.Builder lb = ret;
		lb.setCqResetPay(purchaseCfg.yuanbao);
		lb.setCqPassTimes(r.cqPassTimes);
		lb.setCqDayTimes(CanShu.DAYTIMES_LEGENDPVE);
		lb.setCqResetTimes(r.cqResetTimes);
		lb.setCqResetLeft(cqResetLeft);
		lb.setJySaoDangDayTimes(jySaoDangDayTimes);
		lb.setJySaoDangUsedTimes(sd.jySaoDangTimes);
		lb.setCqSaoDangDayTimes(cqSaoDangDayTimes);
		lb.setCqSaoDangUsedTimes(sd.jySaoDangTimes);
		lb.setAcheive(r.achieve);
		lb.setAcheiveRewardState(r.achieveRewardState);
		session.write(ret.build());
		log.info("{} 关卡id {} 类型 {} 消耗体力 {}", jz.id, guanQiaId, req.getType(), conf.useHp);
	}

	public SaoDangBean insertSaoDangBean(long junzhuId) {
		SaoDangBean sd = new SaoDangBean();
		sd.jzId = junzhuId;
		sd.saoDangResetTime = new Date();
		HibernateUtil.insert(sd);
		Cache.saoDangCache.put(junzhuId, sd);
		return sd;
	}

	/**
	 * 布阵选择武将，暂时不用
	 * 
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void setBuZhen(int id, IoSession session, Builder builder) {
		BuZhenReport.Builder req = (qxmobile.protobuf.PveLevel.BuZhenReport.Builder) builder;
		List<Integer> list = req.getWuJiangIdsList();
		if (list.size() != 5) {
			log.error("布阵信息不对，需要5个，实际{}", list.size());
			return;
		}
		session.setAttribute(SessionAttKey.buZhenWuJiangIds, list);
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			return;
		}
		BuZhenBean bean = HibernateUtil.find(BuZhenBean.class, junZhuId);
		if (bean == null) {
			bean = new BuZhenBean();
			bean.id = junZhuId;
			// 将新建立的对象存入缓存
			MC.add(bean, junZhuId);
			HibernateUtil.insert(bean);
		}
		bean.pos1 = list.get(0);
		bean.pos2 = list.get(1);
		bean.pos3 = list.get(2);
		bean.pos4 = list.get(3);
		bean.pos5 = list.get(4);
		HibernateUtil.save(bean);
		log.info("设置布阵信息{}", list);
	}

	/**
	 * 布阵选择秘宝
	 * 
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void setMibaoSelect(int cmd, IoSession session, Builder builder) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			return;
		}
		MibaoSelect.Builder req = (qxmobile.protobuf.PveLevel.MibaoSelect.Builder) builder;
		int battleType = req.getType();
		List<Long> mibaoIds = req.getMibaoIdsList();
		int zuheId = req.getZuheSkill();
		if (zuheId <= 0) {
			sendMibaoSelectResp(0, battleType, session, zuheId);
			log.error("玩家选择了无效的秘宝技能id", zuheId);
			return;
		}
		MibaoSkill s = MibaoMgr.inst.mibaoSkillMap.get(zuheId);
		if (s == null) {
			sendMibaoSelectResp(0, battleType, session, zuheId);
			log.error("MibaoSkill.xml没有秘宝组合Id:{}的数据", zuheId);
			return;
		}

		// 君主秘宝技能有可能没有手动激活
		MiBaoSkillDB sd = MibaoMgr.inst.getActiveSkillFromDB(junzhu.id, zuheId);
		if (sd == null) {
			sendMibaoSelectResp(0, battleType, session, zuheId);
			log.error("君主{}该秘宝技能{}未激活", junzhu.id, zuheId);
			return;
		}
		// 1-过关斩将，2-百战千军，3-荒野藏宝点，4-荒野资源点,5-百战攻击， 6-押镖防守，7-押镖攻击，
		// 8-游侠金币关，9-游侠材料关，10-游侠精气关，13-游侠完璧归赵，14-游侠横扫六和,11-掠夺防守，12-掠夺攻击
		switch (battleType) {
		case 1:
			saveMibao4Pve(session, mibaoIds, junzhu, zuheId);
			break;
		// 百战防守组合技能
		case 2:
			PvpMgr.inst.saveFangShouMiBao(junzhu.id, mibaoIds, zuheId);
			break;
		case 3:
			saveMibao4HYPve(session, mibaoIds, junzhu.id, zuheId);
			break;
		case 4:
			saveMibao4HYPvp(session, mibaoIds, junzhu.id, zuheId);
			break;
		// 百战攻击组合技能
		case 5:
			PvpMgr.inst.saveGongJiMiBao(junzhu.id, mibaoIds, zuheId);
			break;
		case 6:
			YaBiaoHuoDongMgr.inst.saveFangShouMiBao(junzhu, mibaoIds, zuheId);
			break;
		case 7:
			YaBiaoHuoDongMgr.inst.saveGongJiMiBao(junzhu, mibaoIds, zuheId);
			break;
		case 8:
		case 9:
		case 10:
		case 13:
		case 14:
			saveMibao4YouXia(battleType, junzhu.id, zuheId);
			break;
		case 11:
			LveDuoMgr.inst.saveLveDuoFSZuheId(zuheId, junzhu.id);
			break;
		case 12:
			LveDuoMgr.inst.saveLveDuoGJZuheId(zuheId, junzhu.id);
			break;
		case 15:
			ChongLouMgr.inst.saveMibao4ChongLou(zuheId, junzhu);
			break;
		default:
			log.error("战斗前秘宝保存请求类型错误！未发现type:{}的战斗类型", battleType);
			sendMibaoSelectResp(0, battleType, session, zuheId);
			return;
		}
		sendMibaoSelectResp(1, battleType, session, zuheId);
	}

	public void saveMibao4YouXia(int battleType, long junzhuId, int zuheId) {
		BuZhenYouXia buzhen = HibernateUtil.find(BuZhenYouXia.class, junzhuId);
		if (buzhen == null) {
			buzhen = insertBuZhenYouXia(junzhuId);
		}
		switch (battleType) {
		case 8:
			buzhen.jinBiZuheId = zuheId;
			break;
		case 9:
			buzhen.caiLiaoZuheId = zuheId;
			break;
		case 10:
			buzhen.jingQiZuheId = zuheId;
			break;
		case 13:
			buzhen.type4 = zuheId;
			break;
		case 14:
			buzhen.type5 = zuheId;
			break;
		default:
			log.error("秘宝保存不是游侠活动的关卡");
			return;
		}
		HibernateUtil.save(buzhen);
	}

	public BuZhenYouXia insertBuZhenYouXia(long junzhuId) {
		BuZhenYouXia buzhen = new BuZhenYouXia();
		buzhen.junzhuId = junzhuId;
		buzhen.caiLiaoZuheId = -1;
		buzhen.jinBiZuheId = -1;
		buzhen.jingQiZuheId = -1;
		buzhen.type4 = -1;
		buzhen.type5 = -1;
		HibernateUtil.insert(buzhen);
		Cache.buZhenYouXiaCache.put(junzhuId, buzhen);
		return buzhen;
	}

	public void saveMibao4HYPve(IoSession session, List<Long> mibaoIds, long junzhuId, int zuheId) {
		BuZhenHYPve buzhenHYpve = HibernateUtil.find(BuZhenHYPve.class, junzhuId);
		if (buzhenHYpve == null) {
			buzhenHYpve = insertBuZhenHYPve(junzhuId);
		}
		buzhenHYpve.zuheId = zuheId;
		HibernateUtil.save(buzhenHYpve);
		log.info("君主id:{}设置了荒野资源点秘宝选择", junzhuId);

	}

	public BuZhenHYPve insertBuZhenHYPve(long junzhuId) {
		BuZhenHYPve buzhenHYpve;
		buzhenHYpve = new BuZhenHYPve();
		buzhenHYpve.junzhuId = junzhuId;
		Cache.buZhenHYPve.put(junzhuId, buzhenHYpve);
		return buzhenHYpve;
	}

	public void saveMibao4HYPvp(IoSession session, List<Long> mibaoIds, long junzhuId, int zuheId) {
		BuZhenHYPvp buzhenHYpvp = HibernateUtil.find(BuZhenHYPvp.class, junzhuId);
		if (buzhenHYpvp == null) {
			buzhenHYpvp = insetBuZhenHYPvp(junzhuId);
		}
		buzhenHYpvp.zuheId = zuheId;
		HibernateUtil.save(buzhenHYpvp);
		log.info("君主id:{}设置了荒野资源点秘宝选择", junzhuId);
	}

	public BuZhenHYPvp insetBuZhenHYPvp(long junzhuId) {
		BuZhenHYPvp buzhenHYpvp;
		buzhenHYpvp = new BuZhenHYPvp();
		buzhenHYpvp.junzhuId = junzhuId;
		Cache.buZhenHYPvp.put(junzhuId, buzhenHYpvp);
		return buzhenHYpvp;
	}

	public void sendMibaoSelectResp(int result, int type, IoSession session, int zuheId) {
		MibaoSelectResp.Builder response = MibaoSelectResp.newBuilder();
		response.setSuccess(result);
		response.setType(type);
		response.setZuheSkill(zuheId);
		session.write(response.build());
	}

	public void saveMibao4Pve(IoSession session, List<Long> list, JunZhu jz, int zuheId) {
		session.setAttribute(SessionAttKey.buZhenMibaoIds, list);
		BuZhenMibaoBean bean = HibernateUtil.find(BuZhenMibaoBean.class, jz.id);
		if (bean == null) {
			bean = insertBuZhenMibaoBean(jz);
		}
		bean.zuheId = zuheId;
		HibernateUtil.save(bean);
		log.info("设置布阵秘宝信息{}", list);
		// 刷新PVE战力数据
		JunZhuMgr.inst.sendPveMibaoZhanli_2(jz, session, bean);
	}

	public BuZhenMibaoBean insertBuZhenMibaoBean(JunZhu jz) {
		BuZhenMibaoBean bean = new BuZhenMibaoBean();
		bean.id = jz.id;
		bean.zuheId = -1;
		MemcachedCRUD.getMemCachedClient().set(BuZhenMibaoBean.class.getSimpleName() + "#" + jz.id, bean);
		Cache.buZhenMiBaoCache.put(jz.id, bean);
		HibernateUtil.insert(bean);
		return bean;
	}

	public void sendYuanJunList(int id, Builder builder, IoSession session) {
		YuanZhuListReturn.Builder ret = YuanZhuListReturn.newBuilder();
		{
			YuanJun.Builder yj = YuanJun.newBuilder();
			yj.setId(101);
			yj.setName("测试机器人01");
			yj.setLevel(11);
			ret.addList(yj);
		}
		{
			YuanJun.Builder yj = YuanJun.newBuilder();
			yj.setId(102);
			yj.setName("测试机器人02");
			yj.setLevel(12);
			ret.addList(yj);
		}
		session.write(ret.build());
	}

	public void resetChuanQiTimes(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		ResetCQTimesReq.Builder req = (qxmobile.protobuf.PveLevel.ResetCQTimesReq.Builder) builder;
		int guanQiaId = req.getGuanQiaId();
		PveRecord r = getR(jz.id, guanQiaId);
		if (r == null || r.chuanQiPass == false) {
			sendError(session, "您还没有打通这一关:" + guanQiaId);
			return;
		}

		Purchase purchaseCfg = PurchaseMgr.inst.getPurchaseCfg(PurchaseConstants.CHUANQI_REST, r.cqResetTimes + 1);
		if (purchaseCfg == null) {
			log.error("找不到purchase配置，type:{}", PurchaseConstants.CHUANQI_REST);
			return;
		}
		int needYuanBao = purchaseCfg.yuanbao;
		if (jz.yuanBao < needYuanBao) {
			log.error("元宝不足:{}， 不能进行第{}次 传奇关卡重置", needYuanBao, r.cqResetTimes + 1);
			return;
		}
		YuanBaoMgr.inst.diff(jz, -needYuanBao, 0, needYuanBao, YBType.YB_CHUANQI_RESET,
				"进行第" + (r.cqResetTimes + 1) + "次传奇关卡次数重置");
		HibernateUtil.update(jz);
		JunZhuMgr.inst.sendMainInfo(session, jz, false);

		r.cqPassTimes = 0;
		r.cqResetTimes += 1;
		r.cqResetDate = new Date();
		HibernateUtil.save(r);
		log.info("{} 重置传奇关卡{}次数", jz.id, guanQiaId);
		ResetCQTimesBack.Builder ret = ResetCQTimesBack.newBuilder();
		Purchase nextPurchaseCfg = PurchaseMgr.inst.getPurchaseCfg(PurchaseConstants.CHUANQI_REST, r.cqResetTimes + 1);
		ret.setGuanQiaId(guanQiaId);
		int nextCost = needYuanBao;
		if (nextPurchaseCfg == null) {
			log.error("找不到purchase配置，type:{}", PurchaseConstants.CHUANQI_REST);
		} else {
			nextCost = nextPurchaseCfg.yuanbao;
		}
		ret.setCqResetPay(nextCost);
		session.write(ret.build());
	}

	public int getGuanQiaMaxId(long junzhuId) {
		JunzhuPveInfo jzPveInfo = HibernateUtil.find(JunzhuPveInfo.class, junzhuId);
		if (null != jzPveInfo) {
			return jzPveInfo.commonChptMaxId;
		}
		return 0;
	}

	/**
	 * 获取所有 普通关卡星星数
	 * 
	 * @param junzhuId
	 * @return
	 */
	public int getGuanQiaSumStart(long junzhuId) {
		Map<Integer, PveRecord> list = recordMgr.getRecords(junzhuId);
		int sum = 0;
		if (list == null || list.size() == 0) {
			return sum;
		}
		for (PveRecord record : list.values()) {
			int start = record.achieve;
			sum += calcStarNum(start);
		}
		return sum;
	}

	/**
	 * 获取传奇关卡+普通关卡 总星星数
	 * 
	 * @param list
	 * @return
	 */
	public int getAllGuanQiaStartSum(long jzId) {
		Map<Integer, PveRecord> list = BigSwitch.pveGuanQiaMgr.recordMgr.getRecords(jzId);
		int sum = 0;
		if (list == null || list.size() == 0) {
			return sum;
		}
		for (PveRecord record : list.values()) {
			sum += calcStarNum(record.achieve);
			if (record.chuanQiPass) {
				sum += calcStarNum(record.cqStar);
			}
		}
		return sum;
	}

	public int calcStarNum(int start) {
		int result = 0;
		switch (start) {
		case 1:
		case 10:
		case 100:
			result = 1;
			break;
		case 11:
		case 101:
		case 110:
			result = 2;
			break;
		case 111:
			result = 3;
			break;
		default:
			break;
		}
		return result;
	}

	/**
	 * 请求过关斩将最大id
	 * 
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void getPveMaxId(int id, IoSession session, Builder builder) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		JunzhuPveInfo junzhuPveInfo = HibernateUtil.find(JunzhuPveInfo.class, junzhu.id);
		if (junzhu == null) {
			log.error("找不到君主");
			return;
		}
		int  legendChptrMaxId = 0;
		int  commonChptrMaxId =0;
		if(junzhuPveInfo != null){
			legendChptrMaxId = junzhuPveInfo.legendChptMaxId;
			commonChptrMaxId = junzhuPveInfo.commonChptMaxId;
		}
		
		/*Map<Integer, PveRecord> rMap = recordMgr.getRecords(junzhu.id);
		int maxCqPassId = 0;
		int maxGuanQiaId = 0;
		Iterator<PveRecord> it = rMap.values().iterator();
		while (it.hasNext()) {
			PveRecord pr = it.next();
			maxGuanQiaId = Math.max(maxGuanQiaId, pr.guanQiaId);
			if (pr.chuanQiPass) {
				maxCqPassId = Math.max(pr.guanQiaId, maxCqPassId);
			}
		}*/
		GuanQiaMaxId.Builder response = GuanQiaMaxId.newBuilder();
		response.setChuanQiId(legendChptrMaxId);
		response.setCommonId(commonChptrMaxId);
		session.write(response.build());
	}

	/**
	 * 
	 * @param session
	 * @param builder
	 *            1: 配置错误， 2： 没有通关, 3: 已经领取了奖励， 4 可以领奖
	 */
	public void hasGetPassZhangJieAward(IoSession session, Builder builder) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			log.error("找不到君主");
			return;
		}
		GetPassZhangJieAwardReq.Builder b = (GetPassZhangJieAwardReq.Builder) builder;
		int zhangjieId = b.getZhangJieId();
		Integer guanqiaID = PveMgr.lastGuanQiaOfZhang.get(zhangjieId);
		if (guanqiaID == null) {
			sendError(session, "配置错误", PD.has_get_zhangJie_award_resp, 1);
			return;
		}
		PveRecord r = getR(junzhu.id, guanqiaID);
		if (r == null) {
			// 没有通关
			sendError(session, "章节没有通关", PD.has_get_zhangJie_award_resp, 2);
			return;
		}
		if (r.isGetAward) {
			// 已经领取了通章奖励
			sendError(session, "已经领取通章奖励", PD.has_get_zhangJie_award_resp, 3);
			return;
		}
		sendError(session, "没有领取通章奖励", PD.has_get_zhangJie_award_resp, 4);
	}

	/**
	 * * 1: 配置错误， 2： 没有通关, 3: 已经领取了奖励， 4 可以领奖 5: 领取失败， 6， 领取成功
	 * 
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void getPassZhangJieAward(int id, IoSession session, Builder builder) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			log.error("找不到君主");
			return;
		}
		GetPassZhangJieAwardReq.Builder b = (GetPassZhangJieAwardReq.Builder) builder;
		int zhangjieId = b.getZhangJieId();
		Integer guanqiaID = PveMgr.lastGuanQiaOfZhang.get(zhangjieId);
		if (guanqiaID == null) {
			sendError(session, "配置错误", PD.get_passZhangJie_award_resp, 1);
			log.error("玩家：{}领取通章：{}奖励领取失败，没有获得相关章节的关卡", junzhu.id, zhangjieId);
			return;
		}
		PveRecord r = getR(junzhu.id, guanqiaID);
		// 已经通关
		if (r == null) {
			// 没有通关
			sendError(session, "章节没有通关", PD.get_passZhangJie_award_resp, 2);
			log.error("玩家：{}领取通章：{}奖励领取失败，没有通过最后一关", junzhu.id, zhangjieId);
			return;
		}
		// 是不是已经领取
		if (r.isGetAward) {
			// 已经领取了通章奖励
			sendError(session, "已经领取通章奖励", PD.get_passZhangJie_award_resp, 3);
			log.error("玩家：{}领取通章：{}奖励领取失败，已经领取通章奖励", junzhu.id, zhangjieId);
			return;
		}
		// 领奖配置
		PveBigAward p = PveMgr.passAwardMap.get(zhangjieId);
		if (p == null) {
			sendError(session, "奖励配置错误", PD.get_passZhangJie_award_resp, 1);
			log.error("玩家：{}领取通章：{}奖励领取失败，PveBigAward配置错误", junzhu.id, zhangjieId);
			return;
		}
		// 领奖
		boolean ok = AwardMgr.inst.giveReward(session, p.award, junzhu);
		if (!ok) {
			sendError(session, "领奖失败", PD.get_passZhangJie_award_resp, 5);
			log.error("玩家：{}领取通章：{}奖励领取失败，AwardMgr加奖励失败", junzhu.id, zhangjieId);
			return;
		}
		r.isGetAward = true;
		HibernateUtil.save(r);
		log.info("玩家：{}领取章节：{}，通章奖励：{},领取成功", junzhu.id, zhangjieId, p.award);
		sendError(session, "领奖成功", PD.get_passZhangJie_award_resp, 0);
		// 通章奖励时间
		EventMgr.addEvent(junzhu.id,ED.get_pass_PVE_zhang_award, new Object[] { junzhu.id });
	}

	public void notGetAwardZhangJieRequest(int id, IoSession session, Builder builder) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if (junzhu == null) {
			log.error("找不到君主");
			return;
		}
		GetNotGetAwardZhangJieResp.Builder response = GetNotGetAwardZhangJieResp.newBuilder();
		List<PveTemp> list = TempletService.listAll(PveTemp.class.getSimpleName());
		Map<Integer, PveRecord> rMap = recordMgr.getRecords(junzhu.id);
		int zhangJieIdMax = getZhangJieIdMax(list, rMap);
		for (int i = 1; i <= zhangJieIdMax; i++) {
			Integer lastGuanQiaId = PveMgr.lastGuanQiaOfZhang.get(i);
			for (Map.Entry<Integer, PveRecord> entry : rMap.entrySet()) {
				Integer zhangJieId = entry.getKey();
				if (lastGuanQiaId != null && zhangJieId.equals(lastGuanQiaId)) {
					PveRecord record = entry.getValue();
					if (!record.isGetAward) {
						response.addZhangJiaId(i);
					}
				}
			}
		}
		session.write(response.build());
	}
}
