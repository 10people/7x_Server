package com.qx.pve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import log.ActLog;
import log.OurLog;

import org.apache.commons.lang.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.BattlePveInit.BattleReplayData;
import qxmobile.protobuf.BattlePveInit.BattleReplayReq;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.PveLevel.BuZhenReport;
import qxmobile.protobuf.PveLevel.GetPveStarAward;
import qxmobile.protobuf.PveLevel.GuanQiaInfo;
import qxmobile.protobuf.PveLevel.GuanQiaInfoRequest;
import qxmobile.protobuf.PveLevel.GuanQiaMaxId;
import qxmobile.protobuf.PveLevel.MibaoSelect;
import qxmobile.protobuf.PveLevel.MibaoSelectResp;
import qxmobile.protobuf.PveLevel.PveBattleOver;
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

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.hero.service.HeroService;
import com.manu.dynasty.store.MemcachedCRUD;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.MiBao;
import com.manu.dynasty.template.Purchase;
import com.manu.dynasty.template.PveStar;
import com.manu.dynasty.template.PveTemp;
import com.manu.dynasty.template.VIP;
import com.manu.dynasty.template.ZhuXian;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.SessionAttKey;
import com.qx.account.FunctionOpenMgr;
import com.qx.achievement.AchievementCondition;
import com.qx.achievement.AchievementConstants;
import com.qx.award.AwardMgr;
import com.qx.bag.BagMgr;
import com.qx.battle.PveMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.huangye.BuZhenHYPve;
import com.qx.huangye.BuZhenHYPvp;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.mibao.MibaoMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.pvp.LveDuoMgr;
import com.qx.pvp.PvpMgr;
import com.qx.secure.AntiCheatMgr;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.task.GameTaskMgr;
import com.qx.util.TableIDCreator;
import com.qx.vip.VipMgr;
import com.qx.yabiao.YaBiaoHuoDongMgr;
import com.qx.youxia.BuZhenYouXia;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

/**
 * @author 康建虎
 * 
 */
public class PveGuanQiaMgr {
	public static Map<Integer, BattleReplayData> replayCache = new HashMap<Integer, BattleReplayData>();
	public static Logger log = LoggerFactory.getLogger(PveGuanQiaMgr.class);
	public GuanQiaStartRewardBean[] startRewardStatus;
	public PveRecordDao recordMgr;
	public static PveRecord nullRecord;
	public static Map<Integer, PveStar> starMap;

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
			starMap.put(s.starId, s);
		}
	}

	public void battleOver(int id, IoSession session, Builder builder) {
		PveBattleOver.Builder b = (qxmobile.protobuf.PveLevel.PveBattleOver.Builder) builder;
		boolean ok = b.getSPass();
		Integer guanQiaId = (Integer) session
				.getAttribute(SessionAttKey.guanQiaId);
		if (guanQiaId == null) {
			return;
		}
		// AntiCheatMgr.anti = true;
		if (AntiCheatMgr.anti) {// 检查作弊
			Object o = session.removeAttribute(SessionAttKey.antiCheatPass);
			if (o == null) {
				sendError(session, "战斗过程异常！");
				return;
			}
		}
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		Long junZhuId = junZhu.id;
		Boolean chuanQiMark = (Boolean) session
				.getAttribute(SessionAttKey.chuanQiMark);
		if (junZhuId == null || guanQiaId == null) {
			log.error("null value , pid {}, guanQiaId{}", junZhuId, guanQiaId);
			return;
		}
		log.info("{}请求领取奖励{},类型{}", junZhuId, guanQiaId, chuanQiMark);

		PveTemp pveTemp = null;
		if (chuanQiMark != null && chuanQiMark) {
			pveTemp = PveMgr.inst.legendId2Pve.get(guanQiaId);
		} else {
			pveTemp = PveMgr.inst.getId2Pve().get(guanQiaId);
		}
		if (pveTemp == null) {
			log.error("请求pve章节id错误，zhangJieId:{}", guanQiaId);
			return;
		}
		ActLog.log.HeroBattle(junZhuId, junZhu.name, ActLog.vopenid, guanQiaId, pveTemp.smaName, ok?1:2, 1);
		int resultForLog;//0 失败；2首次；3再次
		if (ok) {
			PveRecord r = HibernateUtil.find(PveRecord.class,
					"where guanQiaId=" + guanQiaId + " and uid=" + junZhuId);
			if (r == null) {
				r = new PveRecord();
				// 改主键不自增
				r.dbId = TableIDCreator.getTableID(PveRecord.class, 1L);
				r.guanQiaId = guanQiaId;
				r.uid = junZhuId;
				resultForLog = 2;
			}else{
				resultForLog = 3;
			}
			if (chuanQiMark != null && chuanQiMark) {
				r.chuanQiPass = true;
				r.cqPassTimes += 1;
				r.cqStar = Math.max(r.cqStar, b.getStar());
				log.info("{}传奇关卡{}", junZhuId, guanQiaId);
			} else {
				r.starLevel = b.getStar();
				r.star = Math.max(r.star, b.getStar());
				r.achieve = r.achieve | b.getAchievement();
				chuanQiMark = false;
				
			}
			log.info("junZhuId {} 关卡{} 战斗结束 , 成功", junZhuId, guanQiaId);
			log.info("获得星级 star {}", b.getStar());
			HibernateUtil.save(r);
			//限时活动精英集星
			EventMgr.addEvent(ED.JINGYINGJIXING, junZhuId);

			// 战斗胜利扣除剩余应该消耗的体力
			int useTili = pveTemp.getUseHp();
			useTili -= 1;
			JunZhuMgr.inst.updateTiLi(junZhu, -useTili, "关卡胜利");
			HibernateUtil.save(junZhu);
			JunZhuMgr.inst.sendMainInfo(session);
			log.info("junzhu:{}在关卡zhangjieId:{}战斗胜利扣除体力:{}", junZhu.name,
					guanQiaId, useTili);
		} else {
			log.info("{} pve fail at {}", junZhuId, guanQiaId);
			resultForLog = 0;
		}
		OurLog.log.RoundFlow(ActLog.vopenid,guanQiaId.intValue(), 2, b.getStar(), 0, resultForLog,String.valueOf(junZhuId));
		AwardMgr.inst.getAward(guanQiaId, chuanQiMark, ok, session);
		if (chuanQiMark != null && chuanQiMark) {
			if (ok) {
				// 主线任务：完成传奇关卡并且胜利一次
				EventMgr.addEvent(ED.CHUANQI_GUANQIA_SUCCESS, new Object[] {
						junZhuId, guanQiaId });
			}
			// 每日任务中记录完成传奇关卡一次（不论输赢）
			EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
					junZhuId, DailyTaskConstants.chuanqi_guanqia_3, 1));
		}else{
			if(ok){
				// 主线任务：完成普通关卡并且胜利一次
				EventMgr.addEvent(ED.PVE_GUANQIA, new Object[] { junZhuId,
						guanQiaId });
			}
			// 每日任务中记录完成过关斩将1次(无论输赢)
			EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
					junZhuId, DailyTaskConstants.guoguan_5_id, 1));
		}
		// 2015-7-22 15:46 过关榜刷新
		EventMgr.addEvent(ED.GUOGUAN_RANK_REFRESH, junZhu);
	}

	private void addAcheEvent(long pid, int star, Integer guanQiaId) {
		PveTemp pveTemp = PveMgr.inst.getId2Pve().get(guanQiaId);
		int zhangjie = pveTemp.getBigId();
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
		EventMgr.addEvent(ED.ACHIEVEMENT_PROCESS, new AchievementCondition(pid,
				acheType, star));
		EventMgr.addEvent(ED.ACHIEVEMENT_PROCESS, new AchievementCondition(pid,
				AchievementConstants.type_guoguan_times, 1));

	}

	public List<PveRecord> getUserRecords(int uid) {
		// HibernateUtil.
		return null;
	}

	public int findBigIndex(int id) {
		List<PveTemp> list = TempletService.listAll(PveTemp.class
				.getSimpleName());
		int idx = -1;
		for (PveTemp p : list) {
			idx++;
			if (p.getBigId() == id) {
				break;
			}
		}
		return idx;
	}

	public void getPageInfo(int id, IoSession session, Builder builder) {
		PvePageReq.Builder req = (qxmobile.protobuf.PveLevel.PvePageReq.Builder) builder;
		Section.Builder b = Section.newBuilder();
		List<PveTemp> list = TempletService.listAll(PveTemp.class
				.getSimpleName());
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
		int curCqBigId = 2;// 可打的精英关卡id，默认可以攻打第二章
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
					if (maxGuanQiaId == conf.getId()) {
						curGuanQiaBigId = conf.getBigId();
						if (confIt.hasNext()) {
							PveTemp confNext = confIt.next();
							// 还是同一章节传奇关卡得-1
							if (confNext.getBigId() == conf.getBigId()) {
								curGuanQiaBigId -= 1;
							}
						}
						break;
					}
				}
				Iterator<PveTemp> confIt2 = list.iterator();
				while (confIt2.hasNext()) {
					PveTemp conf = (PveTemp) confIt2.next();
					if (conf.getId() == maxCqPassId) {
						curCqBigId = conf.getBigId();
						if (confIt2.hasNext()) {
							PveTemp confNext = confIt2.next();
							// 如果下一配置不是同一章节则big+1
							if (confNext.getBigId() != conf.getBigId()) {
								curCqBigId = confNext.getBigId();
							}
						}
						break;
					}
				}
			}
			curCqBigId = Math.min(curGuanQiaBigId, curCqBigId);
			curCqBigId = Math.max(2, curCqBigId);
		}
		if (rMap.size() == 0) {
			if (reqSection <= 0) {
				reqSection = 1;
			}
		} else if (reqSection <= 0) {
			// 计算最大的关卡ID
			Iterator<Integer> it = rMap.keySet().iterator();
			while (it.hasNext()) {
				Integer integer = (Integer) it.next();
				reqSection = Math.max(reqSection, integer);
			}
			// 根据最大的关卡ID计算章节
			boolean checkEnd = false;
			for (PveTemp p : list) {
				if (p.getId() == reqSection) {
					reqSection = p.getBigId();
					checkEnd = true;
				} else if (p.getId() > reqSection) {// 还没找到合适的，但是已经出现下一级关卡了。
					// 2015年4月7日15:51:59 发现就数据的pve记录已经不在了。
					reqSection = p.getBigId();
					break;
				} else if (checkEnd) {// 如果第一章节都打完了，就是下面这个情况。
					if (p.getBigId() != reqSection) {
						reqSection = p.getBigId();
					}
					break;
				}
			}
		}
		int size = list.size();
		int maxSec = list.get(size - 1).getBigId();
		int i = findBigIndex(reqSection);// 查找指定章节的其实关卡位置。
		if (i < 0) {
			reqSection = maxSec;
			i = findBigIndex(reqSection);// 查找指定章节的其实关卡位置。
		}
		b.setSSection(reqSection);
		// 领奖之后才能打关卡
		int maxRwIdx = FunctionOpenMgr.inst.getMaxAwardRenWuOrderIdx(junZhuId);
		for (; i < size; i++) {
			PveTemp t = list.get(i);
			if (t.getBigId() != reqSection) {
				break;
			}

			PveRecord r = rMap.get(t.getId());
			if (r == null) {
				r = nullRecord;
			}
			qxmobile.protobuf.PveLevel.Level.Builder lb = qxmobile.protobuf.PveLevel.Level
					.newBuilder();
			lb.setSLevel(t.getMonarchLevel());
			lb.setSPass(r.star >= 0);
			lb.setType(t.getChapType());
			lb.setGuanQiaId(t.getId());
			lb.setSStarNum(r.star);
			// 1是精英关卡，才有胜利评价。
			lb.setWinLevel(r.star);
			// 星级及奖励领取情况
			String achieveStr = String.valueOf(r.achieve);
			achieveStr = StringUtils.leftPad(achieveStr, 3, "0");
			String achieveRewardStateStr = String.valueOf(r.achieveRewardState);
			achieveRewardStateStr = StringUtils.leftPad(achieveRewardStateStr,
					3, "0");
			for (int index = 0; index < 3; index++) {
				StarInfo.Builder starInfo = StarInfo.newBuilder();
				if (index == 0) {
					starInfo.setStarId(t.getStar1());
				} else if (index == 1) {
					starInfo.setStarId(t.getStar2());
				} else if (index == 2) {
					starInfo.setStarId(t.getStar3());
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

			lb.setChuanQiPass(r.chuanQiPass);// 传奇关卡信息
			lb.setPingJia(r.cqStar);// 传奇关卡星级评价
			if (t.RenWuLimit <= 0) {
				lb.setRenWuId(0);
			} else {
				ZhuXian rwConf = GameTaskMgr.inst.zhuxianTaskMap
						.get(t.RenWuLimit);
				if (rwConf == null) {
					lb.setRenWuId(0);// 没有该任务，可以攻打。
				} else if (rwConf.orderIdx <= maxRwIdx) {
					// 有该主线任务并且该任务已完成
					lb.setRenWuId(0);
				} else {
					lb.setRenWuId(t.RenWuLimit);
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
		session.write(b.build());
		log.info("{} 请求章节 {}", junZhuId, reqSection);
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
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId == null) {
			return;
		}
		JunZhu jz = HibernateUtil.find(JunZhu.class, junZhuId);
		if (jz == null) {
			log.error("君主不存在");
			return;
		}
		PveRecord r = HibernateUtil.find(PveRecord.class, "where guanQiaId="
				+ guanQiaId + " and uid=" + junZhuId);
		if (r == null) {
			sendError(session, "您没有完成此关卡 " + guanQiaId);
			return;
		}
		log.info("{}请求领取星级奖励guanQiaId-{},星级id-{},", junZhuId, guanQiaId, starId);
		PveTemp pveTemp = PveMgr.inst.getId2Pve().get(guanQiaId);
		if (pveTemp == null) {
			log.error("请求pve章节id错误，zhangJieId:{}", guanQiaId);
			return;
		}
		int star1 = pveTemp.getStar1();
		int star2 = pveTemp.getStar2();
		int star3 = pveTemp.getStar3();
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
		if ((key & r.achieve) != key) {
			log.info("星级数据错误key {}, match {} star {}", key, (key & r.star),
					r.star);
			sendError(session, "您还没有获得这个星星。");
			return;
		}
		if ((key & r.achieveRewardState) == key) {
			log.info("已领数据错误key {}, match {} startRewardState {}", key,
					(key & r.achieveRewardState), r.achieveRewardState);
			PveStarGetSuccess.Builder ret = PveStarGetSuccess.newBuilder();
			ret.setGuanQiaId(guanQiaId);
			ret.setSResult(false);
			ret.setSStarNum(b.getSStarNum());
			session.write(ret.build());
			return;
		}
		r.achieveRewardState += key;
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
		PveStarGetSuccess.Builder ret = PveStarGetSuccess.newBuilder();
		ret.setGuanQiaId(guanQiaId);
		ret.setSResult(true);
		ret.setSStarNum(b.getSStarNum());
		session.write(ret.build());
		log.info("ling qu start reward {}", b.getSStarNum());
		// 添加完成精英领取奖励任务
		EventMgr.addEvent(ED.GET_START_PVE_AWARD, new Object[] { junZhuId,
				guanQiaId });
	}

	public void addAward(PveStar ps, IoSession session, JunZhu jz) {
		List<AwardTemp> awardList = new ArrayList<AwardTemp>();
		String awardStr = ps.award;
		String[] goodsList = awardStr.split("#");
		for (String goods : goodsList) {
			String[] gInfo = goods.split(":");
			if (gInfo.length < 3) {
				log.error("PveStar 的award数据配置有错");
				continue;
			}
			AwardTemp awardTemp = new AwardTemp();
			fillAwardTemp(gInfo, awardTemp);
			awardList.add(awardTemp);
		}
		for (AwardTemp award : awardList) {
			AwardMgr.inst.giveReward(session, award, jz);
			log.info("君主:{}, 领取星级奖励物品:{}, 数量：{}", jz.id, award.getItemId(),
					award.getItemNum());
		}
	}

	public void fillAwardTemp(String[] gInfo, AwardTemp awardTemp) {
		int type = Integer.parseInt(gInfo[0]);
		awardTemp.setItemType(type);
		awardTemp.setItemNum(Integer.parseInt(gInfo[2]));
		switch (type) {
		case 10:// type为10时，ginfo[1]表示的是AwardTemp表的awardId
			AwardTemp temp = AwardMgr.inst.calcAwardTemp(Integer
					.parseInt(gInfo[1]));
			awardTemp.setItemId(temp.getItemId());
			awardTemp.setItemType(temp.getItemType());
			break;
		default:
			awardTemp.setItemId(Integer.parseInt(gInfo[1]));
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
		} else if (conf.getChapType() != 1) {
			sendError(session, "不是精英关卡，不能扫荡");
			return;
		}
		int costTiLi = conf.getUseHp() * saoDangTimes;
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
		PveRecord r = HibernateUtil.find(PveRecord.class, "where uid = "
				+ junzhu.id + " and guanQiaId=" + guanQiaId);
		if (r == null || r.star < 0) {
			sendError(session, "请先打通次关卡。");
			return;
		}
		if (cq && r.cqPassTimes >= 3) {
			log.info("传奇关:{}今日已经通关3次", guanQiaId);
			return;
		}
		SaoDangBean sd = HibernateUtil.find(SaoDangBean.class, junzhu.id);
		boolean first = false;
		if (sd == null) {
			sd = new SaoDangBean();
			sd.jzId = junzhu.id;
			first = true;
		}
		Date today = new Date();
		VIP vip = VipMgr.INSTANCE.getVIPByVipLevel(junzhu.vipLevel);
		if (vip == null) {
			vip = new VIP();
			vip.saodangFree = 10;
		}
		// change 20150901
		if(DateUtils.isTimeToReset(sd.saoDangResetTime, CanShu.REFRESHTIME_PURCHASE)){
			sd.jySaoDangTimes = 0;
			sd.saoDangResetTime = today;
		}else{
			if (sd.jySaoDangTimes >= vip.saodangFree) {
				return;
			}
		}
		sd.jySaoDangTimes += saoDangTimes;
		sd.jyAllSaoDangTimes += saoDangTimes;
		HibernateUtil.save(sd);

		if (cq) {
			// 传奇关卡需要处理挑战次数+1
			r.cqPassTimes += saoDangTimes;
			HibernateUtil.save(r);
		}

		PveSaoDangRet.Builder ret = PveSaoDangRet.newBuilder();
		ret.setGuanQiaId(guanQiaId);
		ret.setEndTime(sd.jySaoDangTimes);
		ret.setAllTime(vip.saodangFree);
		int sdTimesLoop = saoDangTimes;
		int getExpTotal = 0;
		while (sdTimesLoop > 0) {
			junzhu.tongBi += conf.getMoney();
			getExpTotal += conf.getExp();
			PveSaoDangAward.Builder award = PveSaoDangAward.newBuilder();
			award.setExp(conf.getExp());
			award.setMoney(conf.getMoney());
			int[] arr = conf.awardConf;
			if(guanQiaId == 100203 && first){//新手引导要求必须出道具
				first = false;
				int[] force = Arrays.copyOf(arr, arr.length+2);
				force[arr.length] = 10000;
				force[arr.length+1] = 100;
				arr = force;
				log.info("{} 首次扫荡，强制掉落",junzhu.id);
			}
			List<Integer> hitAwardIdList = AwardMgr.inst.getHitAwardId(arr, junzhu.id);
			for (Integer awardId : hitAwardIdList) {
				AwardTemp calcV = AwardMgr.inst.calcAwardTemp(awardId);
					if (calcV != null) {
						AwardMgr.inst.giveReward(session, calcV, junzhu, false,false);
						SaoDangAwardItem.Builder sdAwardItem = SaoDangAwardItem
								.newBuilder();
						sdAwardItem.setItemType(calcV.getItemType());
						sdAwardItem.setItemId(calcV.getItemId());
						sdAwardItem.setItemNum(calcV.getItemNum());
						award.addAwardItems(sdAwardItem);
					}
			}
			ret.addAwards(award);
			sdTimesLoop -= 1;
		}

		JunZhuMgr.inst.addExp(junzhu, getExpTotal);
		JunZhuMgr.inst.updateTiLi(junzhu, -costTiLi, "扫荡");
		HibernateUtil.save(junzhu);
		JunZhuMgr.inst.sendMainInfo(session);
		BagMgr.inst.sendBagInfo(0, session, null);
		session.write(ret.build());
		log.info("{}扫荡[{}]关卡{}次数{}", junzhu.id, cq ? "传奇" : "精英", guanQiaId,
				req.getTimes());
		//
		if (cq) {
			// 每日任务中记录完成传奇关卡一次（不论输赢）
			EventMgr.addEvent(
					ED.DAILY_TASK_PROCESS,
					new DailyTaskCondition(junzhu.id,
							DailyTaskConstants.chuanqi_guanqia_3, req
									.getTimes()));
		} else {
			// 每日任务中记录完成过关斩将1次(无论输赢)
			EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
					junzhu.id, DailyTaskConstants.guoguan_5_id, req.getTimes()));
			// 主线任务: 扫荡1次普通关卡（任意关卡都行） 20190916
			EventMgr.addEvent(ED.saoDang  , new Object[] {junzhu.id});
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

	public void sendGuanQiaInfo(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("请求关卡信息错误1，找不到君主");
			return;
		}
		qxmobile.protobuf.PveLevel.GuanQiaInfo.Builder ret = GuanQiaInfo
				.newBuilder();
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

		ret.setDesc(HeroService.getNameById(conf.getSmaDesc()));
		ret.setTili(conf.getUseHp());
		long junZhuId = jz.id;
		BuZhenMibaoBean mibaoBean = HibernateUtil.find(BuZhenMibaoBean.class,
				junZhuId);
		int zuheId = mibaoBean == null ? -1 : mibaoBean.zuheId;
		ret.setZuheId(zuheId);
		// FIXME 0.97不需要了，但先留着
		for (int i = 0; i < 3; i++) {
			ret.addMibaoIds(-1L);
		}
		//
		PveRecord r = HibernateUtil.find(PveRecord.class, " where uid=" + jz.id
				+ " and guanQiaId=" + guanQiaId);
		if (r == null) {
			r = nullRecord;
		}
		SaoDangBean sd = HibernateUtil.find(SaoDangBean.class, jz.id);
		if (sd == null) {
			sd = new SaoDangBean();
		}
		Purchase purchaseCfg = PurchaseMgr.inst.getPurchaseCfg(
				PurchaseConstants.CHUANQI_REST, r.cqResetTimes + 1);
		if (purchaseCfg == null) {
			log.error("找不到purchase配置，type:{}", PurchaseConstants.CHUANQI_REST);
			return;
		}

		Date today = new Date();
		// change 20150901
		if(DateUtils.isTimeToReset(r.cqResetDate, CanShu.REFRESHTIME_PURCHASE)){
			r.cqResetTimes = 0;
			r.cqPassTimes = 0;
			r.cqResetDate = today;
			HibernateUtil.save(r);
		}
		VIP vip = VipMgr.INSTANCE.getVIPByVipLevel(jz.vipLevel);
		int cqResetLeft = 0;
		int jySaoDangDayTimes = 10;
		int cqSaoDangDayTimes = 10;
		if(vip != null) {
			cqResetLeft = Math.max(0, vip.legendPveRefresh - r.cqResetTimes);
			jySaoDangDayTimes = Math.max(0, vip.saodangFree);
			cqSaoDangDayTimes = Math.max(0, vip.saodangFree);
		}
		// change 20150901
		if(DateUtils.isTimeToReset(sd.saoDangResetTime, CanShu.REFRESHTIME_PURCHASE)){
			sd.jySaoDangTimes = 0;
			HibernateUtil.save(sd);
		}
		qxmobile.protobuf.PveLevel.GuanQiaInfo.Builder lb = ret;
		lb.setCqResetPay(purchaseCfg.getYuanbao());
		lb.setCqPassTimes(r.cqPassTimes);
		lb.setCqResetTimes(r.cqResetTimes);
		lb.setCqResetLeft(cqResetLeft);
		lb.setJySaoDangDayTimes(jySaoDangDayTimes);
		lb.setJySaoDangUsedTimes(sd.jySaoDangTimes);
		lb.setCqSaoDangDayTimes(cqSaoDangDayTimes);
		lb.setCqSaoDangUsedTimes(sd.jySaoDangTimes);
		lb.setAcheive(r.achieve);
		lb.setAcheiveRewardState(r.achieveRewardState);
		session.write(ret.build());
		log.info("{} 关卡id {} 类型 {} 消耗体力 {}",jz.id, guanQiaId, req.getType(),
				conf.getUseHp());
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
		List<MiBao> list = MibaoMgr.inst.zuheListMap.get(zuheId);
		if (list == null || list.size() == 0) {
			sendMibaoSelectResp(0, battleType, session, zuheId);
			log.error("没有秘宝组合Id:{}的数据", zuheId);
			return;
		}

		// for(Long id : mibaoIds) { // 验证秘宝正确性
		// if(id >= 0) { // id>=0 表示选择了秘宝
		// MiBaoDB mibaoDB = HibernateUtil.find(MiBaoDB.class, id);
		// if(mibaoDB == null) {
		// log.error("未发现秘宝，秘宝dbId:{}", id);
		// sendMibaoSelectResp(0, battleType, session, zuheId);
		// return;
		// }
		// }
		// }
		//TODO 君主秘宝技能未激活不能保存
		int oppoActiveMiBaoCount = MibaoMgr.inst.getActivateCountByZuheId(
				junzhu.id,zuheId);
		if(oppoActiveMiBaoCount<2){
			sendMibaoSelectResp(0, battleType, session, zuheId);
			log.error("君主{}该秘宝技能{}未激活", junzhu.id,zuheId);
			return;
		}
		// 1-过关斩将，2-百战千军，3-荒野藏宝点，4-荒野资源点 6-押镖防御秘宝 7-押镖攻击密保
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
			YaBiaoHuoDongMgr.inst.saveFangShouMiBao(junzhu.id, mibaoIds, zuheId);
			break;
		case 7:
			YaBiaoHuoDongMgr.inst.saveGongJiMiBao(junzhu.id, mibaoIds, zuheId);
			break;
		case 8:
		case 9:
		case 10:
			saveMibao4YouXia(battleType, junzhu.id, zuheId);
			break;
		case 11:
			LveDuoMgr.inst.saveLveDuoFSZuheId(zuheId,  junzhu.id);
			break;
		case 12:
			LveDuoMgr.inst.saveLveDuoGJZuheId(zuheId, junzhu.id);
			break;
		default:
			log.error("战斗前秘宝保存请求类型错误！未发现type:{}的战斗类型", battleType);
			sendMibaoSelectResp(0, battleType, session, zuheId);
			return;
		}
		sendMibaoSelectResp(1, battleType, session, zuheId);
	}

	private void saveMibao4YouXia(int battleType, long junzhuId, int zuheId) {
		BuZhenYouXia buzhen = HibernateUtil.find(BuZhenYouXia.class, junzhuId);
		if (buzhen == null) {
			buzhen = new BuZhenYouXia();
			buzhen.junzhuId = junzhuId;
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
		default:
			log.error("秘宝保存不是游侠活动的关卡");
			return;
		}
		HibernateUtil.save(buzhen);
	}

	private void saveMibao4HYPve(IoSession session, List<Long> mibaoIds,
			long junzhuId, int zuheId) {
		BuZhenHYPve buzhenHYpve = HibernateUtil.find(BuZhenHYPve.class,
				junzhuId);
		if (buzhenHYpve == null) {
			buzhenHYpve = new BuZhenHYPve();
			buzhenHYpve.junzhuId = junzhuId;
		}
		buzhenHYpve.zuheId = zuheId;
		HibernateUtil.save(buzhenHYpve);
		log.info("君主id:{}设置了荒野资源点秘宝选择", junzhuId);

	}

	private void saveMibao4HYPvp(IoSession session, List<Long> mibaoIds,
			long junzhuId, int zuheId) {
		BuZhenHYPvp buzhenHYpvp = HibernateUtil.find(BuZhenHYPvp.class,
				junzhuId);
		if (buzhenHYpvp == null) {
			buzhenHYpvp = new BuZhenHYPvp();
			buzhenHYpvp.junzhuId = junzhuId;
		}
		buzhenHYpvp.zuheId = zuheId;
		HibernateUtil.save(buzhenHYpvp);
		log.info("君主id:{}设置了荒野资源点秘宝选择", junzhuId);
	}

	private void sendMibaoSelectResp(int result, int type, IoSession session,
			int zuheId) {
		MibaoSelectResp.Builder response = MibaoSelectResp.newBuilder();
		response.setSuccess(result);
		response.setType(type);
		response.setZuheSkill(zuheId);
		session.write(response.build());
	}

	private void saveMibao4Pve(IoSession session, List<Long> list, JunZhu jz,
			int zuheId) {
		session.setAttribute(SessionAttKey.buZhenMibaoIds, list);
		BuZhenMibaoBean bean = HibernateUtil.find(BuZhenMibaoBean.class, jz.id);
		if (bean == null) {
			bean = new BuZhenMibaoBean();
			bean.id = jz.id;
			MemcachedCRUD.getMemCachedClient().set(
					BuZhenMibaoBean.class.getSimpleName() + "#" + jz.id, bean);
			HibernateUtil.insert(bean);
		}
		bean.zuheId = zuheId;
		HibernateUtil.save(bean);
		log.info("设置布阵秘宝信息{}", list);
		// 刷新PVE战力数据
		JunZhuMgr.inst.sendPveMibaoZhanli_2(jz, session, bean);
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
		PveRecord r = HibernateUtil.find(PveRecord.class, "where uid=" + jz.id
				+ " and guanQiaId=" + guanQiaId);
		if (r == null || r.chuanQiPass == false) {
			sendError(session, "您还没有打通这一关:" + guanQiaId);
			return;
		}

		Purchase purchaseCfg = PurchaseMgr.inst.getPurchaseCfg(
				PurchaseConstants.CHUANQI_REST, r.cqResetTimes + 1);
		if (purchaseCfg == null) {
			log.error("找不到purchase配置，type:{}", PurchaseConstants.CHUANQI_REST);
			return;
		}
		int needYuanBao = purchaseCfg.getYuanbao();
		if (jz.yuanBao < needYuanBao) {
			log.error("元宝不足:{}， 不能进行第{}次 传奇关卡重置", needYuanBao,
					r.cqResetTimes + 1);
			return;
		}
		YuanBaoMgr.inst.diff(jz, -needYuanBao, 0,
				PurchaseMgr.inst.getPrice(PurchaseConstants.CHUANQI_REST),
				YBType.YB_CHUANQI_RESET, "进行第" + (r.cqResetTimes + 1)
						+ "次传奇关卡次数重置");
		HibernateUtil.save(jz);
		JunZhuMgr.inst.sendMainInfo(session);

		r.cqPassTimes = 0;
		r.cqResetTimes += 1;
		r.cqResetDate = new Date();
		HibernateUtil.save(r);
		log.info("{} 重置传奇关卡{}次数", jz.id, guanQiaId);
		ResetCQTimesBack.Builder ret = ResetCQTimesBack.newBuilder();
		Purchase nextPurchaseCfg = PurchaseMgr.inst.getPurchaseCfg(
				PurchaseConstants.CHUANQI_REST, r.cqResetTimes + 1);
		ret.setGuanQiaId(guanQiaId);
		int nextCost = needYuanBao;
		if (nextPurchaseCfg == null) {
			log.error("找不到purchase配置，type:{}", PurchaseConstants.CHUANQI_REST);
		} else {
			nextCost = nextPurchaseCfg.getYuanbao();
		}
		ret.setCqResetPay(nextCost);
		session.write(ret.build());
	}

	public int getGuanQiaMaxId(long junzhuId) {
		List<PveRecord> list = HibernateUtil.list(PveRecord.class, "where uid="
				+ junzhuId);
		int maxId = 0;
		if (list == null || list.size() == 0) {
			return maxId;
		}
		for (PveRecord record : list) {
			int id = record.guanQiaId;
			maxId = Math.max(maxId, id);
		}
		return maxId;
	}
	public int getGuanQiaSumStart(long junzhuId) {
		List<PveRecord> list = HibernateUtil.list(PveRecord.class, "where uid=" + junzhuId);
		int sum = 0;
		if (list == null || list.size() == 0) {
			return sum;
		}
		for (PveRecord record : list) {
			int start = record.achieve;
			switch (start) {
			case 1:
				sum=sum+1;
				break;
			case 10:
				sum=sum+1;
				break;
			case 11:
				sum=sum+2;
				break;
			case 100:
				sum=sum+1;
				break;
			case 101:
				sum=sum+2;
				break;
			case 110:
				sum=sum+2;
				break;
			case 111:
				sum=sum+3;
				break;
			default:
				break;
			}
		}
		return sum;
	}
	
	/**
	 * 请求过关斩将最大id
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void getPveMaxId(int id, IoSession session, Builder builder) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if(junzhu == null) {
			log.error("找不到君主");
			return;
		}
		Map<Integer, PveRecord> rMap = recordMgr.getRecords(junzhu.id);
		int maxCqPassId = 0;
		int maxGuanQiaId = 0;
		Iterator<PveRecord> it = rMap.values().iterator();
		while (it.hasNext()) {
			PveRecord pr = it.next();
			maxGuanQiaId = Math.max(maxGuanQiaId, pr.guanQiaId);
			if (pr.chuanQiPass) {
				maxCqPassId = Math.max(pr.guanQiaId, maxCqPassId);
			}
		}
		GuanQiaMaxId.Builder response = GuanQiaMaxId.newBuilder();
		response.setChuanQiId(maxCqPassId);
		response.setCommonId(maxGuanQiaId);
		session.write(response.build());
	}
}
