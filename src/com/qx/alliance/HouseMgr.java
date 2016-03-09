package com.qx.alliance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.session.IoSession;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.House.AnswerExchange;
import qxmobile.protobuf.House.Apply;
import qxmobile.protobuf.House.ApplyInfos;
import qxmobile.protobuf.House.BatchSimpleInfo;
import qxmobile.protobuf.House.EnterOrExitHouse;
import qxmobile.protobuf.House.ExCanJuanJiangLi;
import qxmobile.protobuf.House.ExItemResult;
import qxmobile.protobuf.House.ExchangeEHouse;
import qxmobile.protobuf.House.ExchangeHouse;
import qxmobile.protobuf.House.ExchangeItem;
import qxmobile.protobuf.House.ExchangeResult;
import qxmobile.protobuf.House.HouseExpInfo;
import qxmobile.protobuf.House.HouseSimpleInfo;
import qxmobile.protobuf.House.HouseUpdateInfo;
import qxmobile.protobuf.House.HouseVisitorInfo;
import qxmobile.protobuf.House.HuanWuInfo;
import qxmobile.protobuf.House.LianMengBoxes;
import qxmobile.protobuf.House.OffVisitorInfo;
import qxmobile.protobuf.House.SetHouseState;
import qxmobile.protobuf.House.VisitorInfo;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.FangWu;
import com.manu.dynasty.template.FangWuInformation;
import com.manu.dynasty.template.Jiangli;
import com.manu.dynasty.template.LianMengKeJi;
import com.manu.dynasty.template.Mail;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.FunctionOpenMgr;
import com.qx.alliance.building.JianZhuMgr;
import com.qx.award.DailyAwardMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.email.Email;
import com.qx.email.EmailMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.junzhu.PlayerTime;
import com.qx.persistent.HibernateUtil;
import com.qx.purchase.PurchaseMgr;
import com.qx.pvp.PvpBean;
import com.qx.timeworker.FunctionID;
import com.qx.vip.VipData;
import com.qx.vip.VipMgr;
import com.qx.world.Mission;

public class HouseMgr extends EventProc implements Runnable {
	public static Logger log = LoggerFactory.getLogger(HouseMgr.class
			.getSimpleName());
	public LinkedBlockingQueue<Mission> missions = new LinkedBlockingQueue<Mission>();
	public ConcurrentHashMap<Long, Map<Long, IoSession>> playerInHouse = new ConcurrentHashMap<Long, Map<Long, IoSession>>();
	public ConcurrentHashMap<Integer, Date> lastGetHouse = new ConcurrentHashMap<Integer, Date>();
	public ConcurrentHashMap<Long, Long> inWhichHouse = new ConcurrentHashMap<Long, Long>();
	public Set<Integer> houseLocations;
	private static Mission exit = new Mission(0, null, null);
	public static ThreadLocal<Boolean> sentIsComplete = new ThreadLocal<Boolean>();
	public static int huanFangKa = 900016;
	public static int huFu = 910000;
	public static int fangwuSum = 20;// 50个小房子  2015年9月7日 房屋50改为20个

	public HouseMgr() {
		houseLocations = new HashSet<Integer>();
		for (int i = 1; i <= fangwuSum; i++) {// 50个小房子  2015年9月7日 房屋50改为20个
			houseLocations.add(i);
		}
		// Collections.addAll(houseLocations, 101,102,103,104,105);//五个大房子。
		new Thread(this, "HouseMgr").start();
	}

	public void exchangeRequest(JunZhu buyer, JunZhu seller) {
		{// 待售状态的房屋
			// 给卖家发送邮件，有效期七天

			// 扣除换房卡吗？
		}
		// //////////////////

		{// 荒废状态的房屋
			// 申请立即完成
		}

		{// 无主状态的房屋，立即完成

		}

	}

	/**
	 * iii. 自住状态的房屋，不可被盟友提起交换申请，但可以被盟主设置为待售状态； 处理盟主设置房屋为待售；处理关门锁门
	 * 
	 * @param builder
	 * @param session
	 * @param id
	 */
	public void setHouseForSell(int id, IoSession session, Builder builder) {
		Long curJzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (curJzId == null) {
			log.error("未找到设置房屋状态的君主id信息");
			return;
		}
		SetHouseState.Builder req = (qxmobile.protobuf.House.SetHouseState.Builder) builder;
		long targetId = req.getTargetId();
		HouseBean hb = HibernateUtil.find(HouseBean.class, targetId);
		if (hb == null) {
			log.error("未找到要设置的目标房屋的信息{}", targetId);
			return;
		}
		int lmId = hb.lmId;
		BigHouse bh = HibernateUtil.find(BigHouse.class, targetId);
		if (bh != null) {
			lmId = bh.lmId;
		}
		if (lmId <= 0) {
			log.info("[{}]已经不属于联盟", targetId);
			return;
		}
		long locationId = req.getLocationId();
		if (locationId == hb.location) {
			// 设置小房子状态
			boolean needSave = false;
			if (req.hasState()) {
				AlliancePlayer ap = HibernateUtil.find(AlliancePlayer.class,
						curJzId);
				if (ap == null) {
					log.info("未找到设置房屋状态的君主的联盟信息{}", targetId);
					return;
				}
				if (ap.title == AllianceMgr.TITLE_LEADER) {// 盟主操作
					if (hb.state != HouseBean.ForUse
							&& hb.state != HouseBean.ForSell
							&& hb.state != HouseBean.KingSell) {
						log.info("[{}]状态不对[{}]", curJzId, hb.state);
						return;
					}
					int newState = req.getState();
					if (newState != HouseBean.ForSell
							&& newState != HouseBean.ForUse
							&& newState != HouseBean.KingSell) {
						log.info("[{}]新状态不对[{}]", curJzId, newState);
						return;
					}
					if (hb.state == HouseBean.KingSell) {// 当前状态为盟主强售
						if (newState == HouseBean.ForUse) {
							hb.state = newState;
						} else {
							log.info("[{}]将房屋状态[{}]改为[{}]状态失败", curJzId,
									hb.state, newState);
							return;
						}
					} else if (hb.state == HouseBean.ForUse) {// 当前状态为自住
						hb.state = newState;
					} else if (hb.state == HouseBean.ForSell) {// 当前状态为出售
						if (newState == HouseBean.ForUse) {
							hb.state = newState;
						} else {
							log.info("[{}]将房屋状态[{}]改为[{}]状态失败", curJzId,
									hb.state, newState);
							return;
						}
					}
					log.info("盟主[{}]设置房屋{}状态为{}", curJzId, hb.jzId, newState);
					needSave = true;
					// 如果有大房子 小房子默认为锁门
					if (bh != null) {
						hb.open = false;
					} else {
						hb.open = req.getOpen4My() == 1;
					}
					log.info("盟主[{}]设置房屋{}盟友是否可进为{}", curJzId, hb.jzId, hb.open);
					if (needSave) {
						HibernateUtil.save(hb);
					}

					// 盟主设置房屋状态，更新房屋信息
					updateHouseInfo(lmId, 300, hb, null, null, null);
					log.info("盟主[{}]设置盟友{}的房屋状态，更新房屋信息给联盟{}的所有成员", curJzId,
							hb.jzId, lmId);
				} else {
					// 判断入盟是否超过3天
					if (isThreeDays(ap)) {
						// 入盟不足3天,无法进行房屋交换
						sendApplyResult(session, 301,
								PD.S_HOUSE_EXCHANGE_RESULT);
						return;
					}
					if (hb.state != HouseBean.ForUse
							&& hb.state != HouseBean.ForSell) {
						log.info("[{}]状态不对[{}]", curJzId, hb.state);
						return;
					}
					int newState = req.getState();
					if (newState != HouseBean.ForSell
							&& newState != HouseBean.ForUse) {
						log.info("[{}]新状态不对[{}]", curJzId, newState);
						return;
					}
					hb.state = newState;
					log.info("[{}]设置房屋{}状态为{}", curJzId, hb.jzId, newState);
					needSave = true;
					// 如果有大房子 小房子默认为锁门
					if (bh != null) {
						hb.open = false;
					} else {
						hb.open = req.getOpen4My() == 1;
					}
					log.info("[{}]设置房屋{}盟友是否可进为{}", curJzId, hb.jzId, hb.open);
					if (needSave) {
						HibernateUtil.save(hb);
					}

					// 盟友设置房屋状态，更新房屋信息
					updateHouseInfo(lmId, 300, hb, null, null, null);
					log.info("盟友{}设置房屋{}状态，更新房屋信息给联盟{}的所有成员", curJzId,
							hb.location, lmId);

				}
			}
		} else if (bh != null && locationId == bh.location) {// 大房子不能设置
			bh.open = req.getOpen4My() == 1;
			HibernateUtil.save(bh);
			log.info("[{}]设置da房屋{}盟友是否可进为{}", curJzId, bh.location, bh.open);
		} else {
			log.info("房屋{}位置不对", locationId);
		}
	}

	/**
	 * a) 领取小房经验
	 * 
	 * @param builder
	 * @param session
	 * @param id
	 */
	public void gainExpFromHouse(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("领取小房经验未找到君主信息");
			return;
		}
		HouseBean selfBean = HibernateUtil.find(HouseBean.class, jz.id);
		if (selfBean == null) {
			log.error("领取小房经验未找到君主的小房子信息{}", jz.id);
			return;
		}
		qxmobile.protobuf.House.HouseExpInfo.Builder expInfo0 = makeHouseExpInfo(selfBean);
		int exp = expInfo0.getCur();
		if (exp > 0) {
			selfBean.preGainExpTime = new Date();
			selfBean.cunchuExp = 0;// 清空房屋中存储的上个联盟的经验
			HibernateUtil.save(selfBean);
		}
		JunZhuMgr.inst.addExp(jz, exp);
		log.info("{}领取房屋经验{}", jz.id, exp);
		HouseExpInfo.Builder expInfo = makeHouseExpInfo(selfBean);
		//2015年9月8日 返回君主当前贡献值
		AlliancePlayer ap = HibernateUtil.find(AlliancePlayer.class, jz.id);
		if (ap == null || ap.lianMengId <= 0){
			expInfo.setCurGongxian(0);
		}else{
			expInfo.setCurGongxian(ap.gongXian);
		}
		session.write(expInfo.build());
		// 主线任务: 成功领取1次房屋存储的君主经验算完成任务 20190916
		EventMgr.addEvent(ED.get_house_exp  , new Object[] { jz.id});
	}

	/**
	 * a) 领取大房经验
	 * 
	 * @param builder
	 * @param session
	 * @param id
	 */
	public void gainExpFromBigHouse(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("领取大房经验未找到君主信息");
			return;
		}
		// 查询大房子信息
		BigHouse selfBigHouse = HibernateUtil.find(BigHouse.class, jz.id);
		if (selfBigHouse == null) {
			log.error("领取大房经验未找到君主的大房子信息{}", jz.id);
			return;
		}
		// 查询小房子信息
		HouseBean selfBean = HibernateUtil.find(HouseBean.class, jz.id);
		if (selfBean == null) {
			log.error("领取大房经验未找到君主的小房子信息{}", jz.id);
			return;
		}
		qxmobile.protobuf.House.HouseExpInfo.Builder expInfo0 = makeBigHouseExpInfo(
				jz.id, selfBigHouse, selfBean, 1);
		int exp = expInfo0.getCur();
		if (exp > 0) {
			selfBean.preGainExpTime = new Date();
			selfBean.cunchuExp = 0;// 清空房屋中存储的上个联盟的经验
			HibernateUtil.save(selfBean);
		}
		JunZhuMgr.inst.addExp(jz, exp);
		log.info("{}领取高级房屋经验{}", jz.id, exp);
		HouseExpInfo.Builder expInfo = makeBigHouseExpInfo(jz.id, selfBigHouse,
				selfBean, 1);
		session.write(expInfo.build());
		// 主线任务: 成功领取1次房屋存储的君主经验算完成任务 20190916
		EventMgr.addEvent(ED.get_house_exp  , new Object[] { jz.id});
	}

	/**
	 * @Description: 获取自己的房屋信息
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void getHouseInfo(int id, IoSession session, Builder builder) {
		Long jzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (jzId == null) {
			log.error("获取自己的房屋信息，未找到君主id信息");
			return;
		}

		JunZhu junZhu = HibernateUtil.find(JunZhu.class, jzId);
		if (junZhu == null) {
			log.error("获取自己的房屋信息，未找到君主信息{}", jzId);
			return;
		}
		AlliancePlayer ap = HibernateUtil.find(AlliancePlayer.class, jzId);
		if (ap == null) {
			log.error("获取自己的房屋信息，未找到君主联盟信息{}", jzId);
			return;
		}
		if (ap.lianMengId <= 0) {
			log.error("获取自己的房屋信息,君主联盟信息错误{}", jzId);
			return;
		}
		HouseBean hb = HibernateUtil
				.find(HouseBean.class, "where jzId=" + jzId);
		BigHouse bh = HibernateUtil.find(BigHouse.class, "where jzId=" + jzId);
		BatchSimpleInfo.Builder ret = BatchSimpleInfo.newBuilder();
		HouseExpInfo.Builder expInfo = HouseExpInfo.newBuilder();
		if (bh == null) {
			expInfo = makeHouseExpInfo(hb);
		} else {
			expInfo = makeBigHouseExpInfo(jzId, bh, hb, 1);
		}
		//2015年9月8日 返回君主当前贡献值
		expInfo.setCurGongxian(ap.gongXian);
		ret.setExpInfo(expInfo);
		ProtobufMsg pm = new ProtobufMsg();
		pm.id = PD.S_house_info;
		pm.builder = ret;
		session.write(pm);
		log.info("发送{}房屋信息给{}", jzId, jzId);
	}

	/**
	 * @Description: 生成小房子信息
	 * @param hb
	 * @return
	 */
	public HouseExpInfo.Builder makeHouseExpInfo(HouseBean hb) {
		log.info("生成{}小屋经验", hb.jzId);
		List<?> fwList = TempletService.listAll(FangWu.class.getSimpleName());
		FangWu fwConf = (FangWu) fwList.get(hb.level - 1);
		HouseExpInfo.Builder expInfo = HouseExpInfo.newBuilder();
		expInfo.setLevel(hb.level);
		LianMengKeJi kjConf=JianZhuMgr.inst.getKeJiConfForFangWu(hb.lmId);
		int addLimit4keji=kjConf.value1;
		int exMax=fwConf.produceLimit +addLimit4keji;
		expInfo.setMax(exMax);
		Date preGetExpT = hb.preGainExpTime;
		if (hb.preGainExpTime == null) {
			preGetExpT = hb.firstHoldTime;
		}
		Date nowGainExpTime = new Date();
		if (preGetExpT == null) {
			expInfo.setCur(0);
		} else {
			long t = nowGainExpTime.getTime() - preGetExpT.getTime();// ms
			t = t / 1000;// second
			t = t / 60;// minu
			t = t * fwConf.produceSpeed / 60;// 时速
//			t = Math.min(t, fwConf.produceLimit);
			t = Math.min(t, exMax);
			expInfo.setCur((int) t);
		}
		//离开联盟时君主在房屋中攒的经验
		if (hb.cunchuExp > 0) {
			int tmpExp= Math.min(expInfo.getCur() + hb.cunchuExp, exMax);
			expInfo.setCur(tmpExp);
		}
		// vip等级对应的房屋升级次数
		JunZhu junZhu = HibernateUtil.find(JunZhu.class, hb.jzId);

		int vipUpTimes = VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel,
				VipData.fangWubuildNum);
		int sumConfUpTimes = vipUpTimes + fwConf.buildNum;
		expInfo.setLvupNeedExp(fwConf.exp);
		expInfo.setCurExp(hb.houseExp);
		expInfo.setLeftUpTimes(fwConf.buildNum - hb.todayUpTimes);
		if (hb.preUpTime == null) {
			expInfo.setLeftUpTimes(sumConfUpTimes);
			expInfo.setCoolTime(0);
		// change 20150901
		} else if (!DateUtils.isTimeToReset(hb.preUpTime, CanShu.REFRESHTIME_PURCHASE)) {
			expInfo.setLeftUpTimes(sumConfUpTimes - hb.todayUpTimes);
			long t = nowGainExpTime.getTime() - hb.preUpTime.getTime();
			t /= 1000;
			expInfo.setCoolTime((int) (t >= 3600 ? 0 : 3600 - t));
		} else {
			expInfo.setLeftUpTimes(sumConfUpTimes);
			expInfo.setCoolTime(0);
		}
		expInfo.setNeedGongXian(fwConf.needNum);
		expInfo.setGainHouseExp(fwConf.addNum);
		int kjLevel=kjConf.level;
		expInfo.setKejiLevel(kjLevel);
		return expInfo;
	}

	/**
	 * @Description: 生成大房子和小房子信息
	 * @param jzId
	 * @param bh
	 * @param hb
	 * @param alncLevel
	 *            默认联盟等级为1
	 * @return
	 */
	public HouseExpInfo.Builder makeBigHouseExpInfo(Long jzId, BigHouse bh,
			HouseBean hb, int alncLevel) {
		log.info("生成{}大屋经验", hb.jzId);
		List<?> fwList = TempletService.listAll(FangWu.class.getSimpleName());
		FangWu fwConf = (FangWu) fwList.get(hb.level - 1);
		int bigType = bh.location - 100;
		// 查询玩家的联盟成员自身
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, jzId);
		// 查询联盟信息
		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class,
				member.lianMengId);
		int lmLevel = alncLevel;
		if (alncBean != null) {
			lmLevel = alncBean.level;
		}
		int fwListSize = fwList.size();
		FangWu bigFWConf = new FangWu();
		for (int i = 0; i < fwListSize; i++) {
			// 获得大房子配置信息
			FangWu ObjFW = (FangWu) fwList.get(i);
			if (ObjFW.type == bigType && ObjFW.lianmengLv == lmLevel) {
				bigFWConf = ObjFW;
			}
		}

		HouseExpInfo.Builder expInfo = HouseExpInfo.newBuilder();
		expInfo.setLevel(hb.level);
		LianMengKeJi kjConf=JianZhuMgr.inst.getKeJiConfForFangWu(member.lianMengId);
		int addLimit4keji=kjConf.value1;
		int exMax=fwConf.produceLimit + bigFWConf.produceLimit+addLimit4keji;
		expInfo.setMax(exMax);
		// 设置领取区间，以小房子为准
		Date preGetExpT = hb.preGainExpTime;
		if (hb.preGainExpTime == null) {
			preGetExpT = hb.firstHoldTime;
		}
		Date today = new Date();
		if (preGetExpT == null) {
			expInfo.setCur(0);
		} else {
			long t = today.getTime() - preGetExpT.getTime();// ms
			t = t / 1000;// second
			t = t / 60;// minu
			t = (t * (fwConf.produceSpeed + bigFWConf.produceSpeed)) / 60;// 产出经验速度为大房子世俗+小房子时速
			t = Math.min(t, exMax);
			expInfo.setCur((int) t);
		}
		if (hb.cunchuExp > 0) {
			int tmpExp= Math.min(expInfo.getCur() + hb.cunchuExp, exMax);
			expInfo.setCur(tmpExp);
		}
		expInfo.setLvupNeedExp(fwConf.exp);
		// vip等级对应的房屋升级次数
		JunZhu junZhu = HibernateUtil.find(JunZhu.class, jzId);

		int vipUpTimes = VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel,
				VipData.fangWubuildNum);
		int sumConfUpTimes = vipUpTimes + fwConf.buildNum;
		expInfo.setCurExp(hb.houseExp);
		expInfo.setLeftUpTimes(sumConfUpTimes - hb.todayUpTimes);
		if (hb.preUpTime == null) {
			expInfo.setLeftUpTimes(sumConfUpTimes);
			expInfo.setCoolTime(0);
			// change 20150901
		} else if (!DateUtils.isTimeToReset(hb.preUpTime, CanShu.REFRESHTIME_PURCHASE)) {
			expInfo.setLeftUpTimes(sumConfUpTimes - hb.todayUpTimes);
			long t = today.getTime() - hb.preUpTime.getTime();
			t /= 1000;
			expInfo.setCoolTime((int) (t >= 3600 ? 0 : 3600 - t));
		} else {
			expInfo.setLeftUpTimes(sumConfUpTimes);
			expInfo.setCoolTime(0);
		}
		expInfo.setNeedGongXian(fwConf.needNum);
		expInfo.setGainHouseExp(fwConf.addNum);
		int kjLevel=kjConf.level;
		expInfo.setKejiLevel(kjLevel);
		return expInfo;
	}

	public void exchangeReply(IoSession seller) {
		int replayCode = 0;// 从客户端请求中获取响应码，
		switch (replayCode) {
		case 100:// 同意
			agree();
			break;
		case 200:// 拒绝
			reject();
			break;
		case 300:// 无视
			ignore();
			break;
		default:// 给客户端发送错误信息
			break;
		}
	}

	private void ignore() {
		// TODO Auto-generated method stub

	}

	private void reject() {
		// TODO Auto-generated method stub

	}

	private void agree() {
		// TODO Auto-generated method stub

	}

	/**
	 * @Description: 获取联盟房屋信息
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void getBatchInfo(int id, IoSession session, Builder builder) {
		Long jzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (jzId == null) {
			log.error("获取联盟房屋信息，未找到请求君主id信息");
			return;
		}
		AlliancePlayer ap = HibernateUtil.find(AlliancePlayer.class, jzId);
		if (ap == null) {
			log.error("获取联盟房屋信息，未找到请求君主的联盟信息{}", jzId);
			return;
		}
		if (ap.lianMengId <= 0) {
			log.error("获取联盟房屋信息，请求君主的联盟信息有误{}", jzId);
			return;
		}
		List<HouseBean> hs = HibernateUtil.list(HouseBean.class, "where lmId="
				+ ap.lianMengId);
		//2016年2月17日 因策划去掉大房子设计 暂时屏蔽大房子 //HibernateUtil.list(BigHouse.class, "where lmId="+ap.lianMengId);
		List<BigHouse> bigHs =Collections.EMPTY_LIST;
		BatchSimpleInfo.Builder ret = BatchSimpleInfo.newBuilder();
		HouseBean self = null;
		// 获取上次联盟成员获取房屋信息的时间
		Date lastGetDate = lastGetHouse.get(ap.lianMengId);
		Date now = new Date();
		// 如果为空设置为当前时间
		if (lastGetDate == null) {
			lastGetHouse.put(ap.lianMengId, now);
		} else {

			// 如果联盟成员上次请求房屋时间超过一小时，更新房屋状态
			long cha = (now.getTime() - lastGetDate.getTime());
			if (cha * 1.0 / (1000 * 60 * 60) > 1) {
				dropHouse(hs);
				lastGetHouse.replace(ap.lianMengId, now);
			}
		}
		// 初级房子列表
		for (HouseBean bean : hs) {
			HouseSimpleInfo.Builder sf = HouseSimpleInfo.newBuilder();
			sf.setLocationId(bean.location);
			sf.setJzId(bean.jzId);
			sf.setJzName(HibernateUtil.find(JunZhu.class, bean.jzId).name);
			// ???表示为老号需退出联盟重新加入 ???会自动赋值。
			sf.setFirstOwner(bean.firstOwner == null ? "???" : bean.firstOwner);
			String firstHoldTime = bean.firstHoldTime == null ? "???"
					: DateUtils.date2Text(bean.firstHoldTime, "yyyy年MM月dd日");
			sf.setFirstHoldTime(firstHoldTime);
			sf.setState(bean.state);
			sf.setOpen4My(bean.open);
			ret.addInfos(sf.build());
			if (bean.jzId == jzId.longValue()) {
				// 上线后荒废变为自住
				if (bean.state == HouseBean.Drop) {
					bean.state = HouseBean.ForUse;
					HibernateUtil.save(bean);
				}
				self = bean;
			}
		}

		BigHouse bigSelf = null;
		// 高级房子列表
		for (BigHouse bigBean : bigHs) {
			HouseSimpleInfo.Builder bigsf = HouseSimpleInfo.newBuilder();
			bigsf.setLocationId(bigBean.location);
			bigsf.setJzId(bigBean.jzId);
			if (bigBean.jzId > 0) {
				bigsf.setJzName(HibernateUtil.find(JunZhu.class, bigBean.jzId).name);
			} else {
				bigsf.setJzName("");
			}
			bigsf.setFirstOwner(bigBean.firstOwner);
			bigsf.setFirstHoldTime(DateUtils.date2Text(bigBean.firstHoldTime,
					"yyyy年mm月dd日"));
			bigsf.setState(bigBean.state);
			bigsf.setOpen4My(true);// 大房子一直可进
			bigsf.setHworth(bigBean.hworth);
			ret.addInfos(bigsf.build());
			if (bigBean.jzId == jzId) {
				bigSelf = bigBean;
			}
		}
		if (self == null) {
			log.error("{}没有自身的房屋信息{}", jzId);
			return;
		}
		HouseExpInfo.Builder expInfo = HouseExpInfo.newBuilder();
		if (bigSelf == null) {
			expInfo = makeHouseExpInfo(self);
		} else {
			expInfo = makeBigHouseExpInfo(jzId, bigSelf, self, 1);
		}
		//2015年9月8日 返回君主当前贡献值
		expInfo.setCurGongxian(ap.gongXian);
		ret.setExpInfo(expInfo);
		ProtobufMsg pm = new ProtobufMsg();
		pm.id = PD.S_LM_HOUSE_INFO;
		pm.builder = ret;
		session.write(pm);
		log.info("发送批量房屋信息x{}给{}", hs.size(), jzId);
	}

	/**
	 * @Description: 荒废小房子
	 * @param hsList
	 */
	public void dropHouse(List<HouseBean> hsList) {
		for (HouseBean bean : hsList) {
			PlayerTime pt = HibernateUtil.find(PlayerTime.class, bean.jzId);
			if (null == pt) {
				pt = new PlayerTime(bean.jzId);
				HibernateUtil.insert(pt);
			}
			Date now = new Date();
			if (pt.getLoginTime() == null)
				continue;
			long cha = (now.getTime() - pt.getLoginTime().getTime());
			if (cha * 1.0 / (1000 * 60 * 60) > 168) {
				log.info("荒废{}的初级房屋{}", bean.jzId, bean.location);
				bean.state = HouseBean.Drop;
				HibernateUtil.save(bean);
			}
		}
	}

	/**
	 * @Description: 衰减大房子价值
	 * @param lmId
	 */
	public void reduceBigHouseWorth(int lmId) {
		// 高级房子列表
		List<BigHouse> bigHs = HibernateUtil.list(BigHouse.class, "where lmId="
				+ lmId);

		for (BigHouse bigBean : bigHs) {
			// 衰减计算
			double hworth = bigBean.hworth;
			int hreduce = CanShu.FANGWUJINGPAI_1;// 衰减比率
			bigBean.hworth = (int) Math.rint(hworth * (100 - hreduce) / 100);
			HibernateUtil.save(bigBean);
			log.info("{}的高级房屋价值衰减，前后价值分别为{}-{}",bigBean.jzId, hworth, bigBean.hworth);
		}
		// 衰减大房子价值，更新房屋信息
		updateBigHouseInfo(lmId, 300);
		log.info("衰减大房子价值，更新房屋信息给联盟{}的所有成员", lmId);
	}

	/**
	 * 给联盟成员分配默认房屋。如果该玩家之前有房屋信息，则会重建。
	 * 
	 * @param lmId
	 * @param jzId
	 * @return
	 */
	public HouseBean giveDefaultHouse(int lmId, long jzId) {
		HouseBean bean = HibernateUtil.find(HouseBean.class, jzId);
		if (bean == null) {
			bean = new HouseBean();
			bean.jzId = jzId;
			//2016年3月7日  策划  为了过引导任务 要求加入 房屋初始经验  只有第一次会给
			bean.cunchuExp=CanShu.FANGWU_INITIAL_EXP;
		} else if (bean.lmId <= 0) {// 从其他联盟退出了。
		} else {
			log.error("该玩家已有房屋，属于联盟{}", bean.lmId);
			return null;
		}
		bean.lmId = lmId;
		bean.state = HouseBean.ForUse;
		bean.open = true;
		bean.level = bean.level == 0 ? 1 : bean.level;
		bean.firstHoldTime = new Date();
		JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
		if (jz == null) {
			log.error("分配房屋时未找到君主 {}", jzId);
			return null;
		}
		bean.firstOwner = jz.name;
		int loc = getRandomLocation(lmId);
		if (loc <= 0) {
			log.error("没有位置了 联盟id{}", lmId);
			return null;
		}
		bean.location = loc;
		HibernateUtil.save(bean);
		// 给玩家发邮件
		// 恭喜主人加入***联盟，接下来您可作为联盟的一员参加联盟的各项活动，与其他成员进行互动交友，
		// 也兴许您会找到您人生中的另一半哦！现在您随机获得了位于联盟城中的一座普通房屋aaa，
		// 如果您不满意自己房屋的位置，可以使用换房卡与其他联盟成员进行交换，
		// 也可以通过房屋拍卖获取豪宅作为自己的府邸。
		Mail cfg = EmailMgr.INSTANCE.getMailConfig(10021);
		String content = cfg.content.replace("***",
				HibernateUtil.find(AllianceBean.class, bean.lmId).name);
		content = content.replace("aaa", getFWName(loc, 101));

		String fuJian = "";
		boolean ok = EmailMgr.INSTANCE.sendMail(jz.name, content, fuJian,
				cfg.sender, cfg, "");
		log.info("发送邮件给{}成功? {}", jz.name, ok);
		log.info("给予{}房屋一套，坐标{}，联盟{}", jzId, loc, lmId);
		return bean;
	}

	public synchronized int getRandomLocation(int lmId) {
		List<HouseBean> hs = HibernateUtil.list(HouseBean.class, "where lmId="
				+ lmId);
		Set<Integer> seeds = new HashSet<Integer>(houseLocations);
		for (HouseBean h : hs) {
			seeds.remove(h.location);
		}
		if (seeds.size() == 0) {
			return -1;
		}
		// 随机分配
		Object[] obj = seeds.toArray();
		//2015年9月7日房屋不随机分配
//		Random rd = new Random();
		int index = 0;//rd.nextInt(obj.length);
		return Integer.valueOf(obj[index].toString());
	}

	@Override
	public void proc(Event evt) {
		switch (evt.id) {
		case ED.Join_LM:
			joinLM(evt);
			break;
		case ED.Leave_LM:
			leaveLM(evt);
			break;
		case ED.REFRESH_TIME_WORK:
			log.info("定时刷新房屋经验");
			IoSession session=(IoSession) evt.param;
			if(session==null){
				log.error("定时刷新房屋经验错误，session为null");
				break;
			}
			JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
			if(jz==null){
				log.error("定时刷新房屋经验错误，JunZhu为null");
				break;
			}
			long jzId=jz.id;
			int level=jz.level;
			boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.LianMeng, jzId, level);
			if(!isOpen){
				log.info("君主--{}的功能---{}未开启,不推送",jzId,FunctionID.LianMeng);
				return;
			}
			isCanLingqufangwuExp(jz, session);
			log.info("定时刷新房屋经验完成");
			break;
		default:
			log.error("错误事件参数",evt.id);
			break;
		}
	}

	/**
	 * @Description: 离开联盟
	 * @param param
	 */
	public void leaveLM(Event param) {
		Object[] oa = (Object[]) param.param;
		Long jzId = (Long) oa[0];
		Integer lmId = (Integer) oa[1];
		String lmName = (String) oa[2];
		Integer level = (Integer) oa[3];
		HouseBean hb = HibernateUtil.find(HouseBean.class, jzId);
		if (hb == null) {
			log.error("退出联盟时没有找到房屋信息{}:{}", jzId, lmId);
			return;
		}
		JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);

		if (jz == null) {
			log.error("退出联盟时没有找到君主信息{}:{}", jzId, lmId);
			return;
		}

		HouseExpInfo.Builder expInfo = HouseExpInfo.newBuilder();
		// 更新高级房屋信息
		BigHouse bh = HibernateUtil.find(BigHouse.class, jzId);
		if (bh == null) {
			expInfo = makeHouseExpInfo(hb);
			log.info("退出联盟时没有找到高级房屋信息{}:{}", jzId, lmId);
		} else {
			expInfo = makeBigHouseExpInfo(jzId, bh, hb, level);
			HibernateUtil.delete(bh);
			bh.jzId = -bh.jzId;
			bh.state = BigHouse.ForClose;
			HibernateUtil.save(bh);
			log.info("退出联盟时修改高级房屋信息{}:{}", jzId, lmId);
		}

		// 处理房屋交易以及邮件
		leaveLMEndHouseDeal(jz, hb);

		// 离开联盟时把君主未领取的经验存起来
		hb.cunchuExp = expInfo.getCur();
		hb.lmId = 0;
		HibernateUtil.save(hb);
		// 给玩家发邮件
		if (bh == null) {
			// 由于您已经离开***联盟，所以您原有的普通房屋的数据将被保存，
			// 在您加入新的联盟后会在新的联盟城中的随机位置重建。
			//2016年2月2日 策划去掉 离开联盟的房屋邮件
//			Mail cfg = EmailMgr.INSTANCE.getMailConfig(10022);
//			String content = cfg.content.replace("***", lmName);

//			String fuJian = "";
//			boolean ok = EmailMgr.INSTANCE.sendMail(jz.name, content, fuJian,cfg.sender, cfg, "");
//			log.info("发送小房子邮件给{}成功 {}", jz.name, ok);
		} else {
			//由于策划去掉大房屋以下代码理论上不会执行
			if (bh.previousId > 0) {
				// 返还贡献发送邮件
				AlliancePlayer curMember = HibernateUtil.find(
						AlliancePlayer.class, jzId);
				double returnWorth = bh.previousWorth * CanShu.FANGWUJINGPAI_2
						/ 100;// 竞拍价值*贡献与价值转化率
				// 返还贡献*返还贡献比率
				int returnGongXian = (int) Math.rint(returnWorth
						* CanShu.FANGWUJINGPAI_4 / 100);
				curMember.gongXian += returnGongXian;
				HibernateUtil.save(curMember);
				// 由于您已经离开****联盟，您原本拍下的豪宅aaa已被联盟收回，并返还您bbb点贡献值。
				// 您拥有的普通房屋的数据将被保存，在您加入新的联盟后将会被随机分配到某块土地上重建。
				Mail cfg = EmailMgr.INSTANCE.getMailConfig(10014);
				String content = cfg.content.replace("****",
						HibernateUtil.find(AllianceBean.class, (long)lmId).name);
				content = content.replace("aaa",
						getFWName(bh.location - 100, bh.location - 100));
				content = content
						.replace("bbb", String.valueOf(returnGongXian));
				String fuJian = "";
				boolean ok = EmailMgr.INSTANCE.sendMail(jz.name, content,
						fuJian, cfg.sender, cfg, "");
				log.info("发送竞拍大房子收回邮件给{}成功 {}", jz.name, ok);
			} else {
				// 由于您已经离开了****联盟，原来拥有的豪宅aaa已经被联盟收回，
				// 您原本拥有的普通房屋的数据将被保存，在您加入新的联盟后将会被随机分配到某块土地上重建。
				// 在您加入新的联盟后会在新的联盟城中的随机位置重建。
				Mail cfg = EmailMgr.INSTANCE.getMailConfig(10013);
				String content = cfg.content.replace("****", lmName);
				content = content.replace("aaa",
						getFWName(bh.location - 100, bh.location - 100));
				String fuJian = "";
				boolean ok = EmailMgr.INSTANCE.sendMail(jz.name, content,
						fuJian, cfg.sender, cfg, "");
				log.info("发送联盟分配的大房子收回邮件给{}成功 {}", jz.name, ok);
			}
		}
		// 离开联盟，推送房屋信息
		updateHouseInfoForLianMeng(lmId, 200, hb, bh);
		log.info("取消[{}]房屋和联盟[{}]的关联,更新所有联盟成员房屋信息", jzId, lmId);

	}

	public void joinLM(Event param) {
		Object[] oa = (Object[]) param.param;
		Long jzId = (Long) oa[0];
		Integer lmId = (Integer) oa[1];
		HouseBean hb = giveDefaultHouse(lmId, jzId);
		if (hb == null) {
			log.error("{}加入{}联盟，更新所有的联盟成员的房屋信息失败", jzId, lmId);
		} else {
			resetHuanWu(lmId, jzId);
			// 加入联盟，更新房屋信息
			updateHouseInfoForLianMeng(lmId, 100, hb, null);
			log.info("{}加入{}联盟，更新所有的联盟成员的房屋信息成功", jzId, lmId);
		}
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.Join_LM, this);
		EventMgr.regist(ED.Leave_LM, this);
		//定时刷新 2015年9月17日
		EventMgr.regist(ED.REFRESH_TIME_WORK, this);
	}

	/**
	 * @Description: 发送高级房屋交换成功邮件给买家
	 * @param buyerName
	 */
	public void sendMailToBigBuyer(String buyerName) {
		// 恭喜主人以雄厚的财力夺得这间豪宅的使用权，它是您身份的象征。此刻这间房屋的价值就是您拍下这间房屋使用的贡献值。
		// 24小时之后其他玩家可以拍买这间豪宅，如果豪宅被他人买走，您将被返还一部分拍买这间房屋时花销的贡献值。
		Mail cfg = EmailMgr.INSTANCE.getMailConfig(10010);
		String content = cfg.content;
		String fuJian = "";
		boolean ok = EmailMgr.INSTANCE.sendMail(buyerName, content, fuJian,
				cfg.sender, cfg, "");
		log.info("发送高级房屋交换成功邮件给{}成功? {}", buyerName, ok);
	}

	/**
	 * @Description: 发送初级房屋交换成功邮件给买家
	 * @param buyerName
	 * @param sellerName
	 */
	public void sendMailToBuyer(String buyerName, String sellerName) {
		// 您的房屋交换申请已经被玩家xxx接受，恭喜主人获得新房屋，我们快去看一看新家吧！
		Mail cfg = EmailMgr.INSTANCE.getMailConfig(10017);
		String content = cfg.content.replace("xxx", sellerName);
		String fuJian = "";
		boolean ok = EmailMgr.INSTANCE.sendMail(buyerName, content, fuJian,
				cfg.sender, cfg, "");
		log.info("发送恭喜换房邮件给{}成功? {}", buyerName, ok);
	}

	/**
	 * @Description: 查询高级房屋配置
	 * @param targetloc
	 * @param lmLevel
	 * @return
	 */
	public FangWu getBHConfig(int targetloc, int lmLevel) {
		// 配置列表
		List<?> fwList = TempletService.listAll(FangWu.class.getSimpleName());
		int fwListSize = fwList.size();

		// 获取当前高级房屋配置信息
		int bigType = targetloc - 100;
		FangWu bigFWConf = new FangWu();
		for (int i = 0; i < fwListSize; i++) {
			// 获得大房子配置信息
			FangWu ObjFW = (FangWu) fwList.get(i);
			if (ObjFW.type == bigType && ObjFW.lianmengLv == lmLevel) {
				bigFWConf = ObjFW;
			}
		}
		return bigFWConf;
	}

	/**
	 * @Description: 申请交换空房屋交换
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void applyChangeEH(int id, IoSession session, Builder builder) {
		ExchangeEHouse.Builder req = (qxmobile.protobuf.House.ExchangeEHouse.Builder) builder;
		int targetLoc = req.getTargetloc();
		if (targetLoc < 0 || targetLoc > fangwuSum) {
			log.error("没有找到目标空房屋信息{}", targetLoc);
			return;
		}
		Long curJzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (curJzId == null) {
			log.error("没有找到申请空房屋的君主id信息");
			return;
		}
		// 买家信息
		JunZhu curJz = JunZhuMgr.inst.getJunZhu(session);
		if (curJz == null) {
			log.error("没有找到申请空房屋的君主信息{}", curJzId);
			return;
		}
		// 判断入盟是否超过3天
		AlliancePlayer buyerMember = HibernateUtil.find(AlliancePlayer.class,
				curJzId);
		if (isThreeDays(buyerMember)) {
			// 入盟3天后,才可以进行房屋交换
			sendApplyResult(session, 300, PD.S_EHOUSE_EXCHANGE_RESULT);
			return;
		}
		Bag<BagGrid> bag = BagMgr.inst.loadBag(curJzId);
		int cnt = BagMgr.inst.getItemCount(bag, huanFangKa);
		if (cnt <= 0) {
			// "您没有换房卡"
			sendApplyResult(session, 10, PD.S_EHOUSE_EXCHANGE_RESULT);
			return;
		}
		HouseBean buyerHb = HibernateUtil.find(HouseBean.class, curJzId);
		// 换房
		if (buyerHb == null || buyerHb.lmId <= 0) {
			log.error("申请者的房屋信息有误{}", curJzId);
			return;
		}
		HouseApplyBean app = HibernateUtil.find(HouseApplyBean.class, curJzId);
		if (app != null) {
			// "只能发出一个申请"
			sendApplyResult(session, 100, PD.S_EHOUSE_EXCHANGE_RESULT);
			return;
		}
		HouseBean targetHb = new HouseBean();
		// 交换房屋信息
		int buyerLoc = buyerHb.location;
		targetHb.location = buyerLoc;
		targetHb.jzId = 0;
		// 坐标
		buyerHb.location = targetLoc;
		buyerHb.firstHoldTime = new Date();
		// 买方房子设定为自住
		buyerHb.state = HouseBean.ForUse;

		HibernateUtil.save(buyerHb);
		log.info("完成{}的房屋与空房屋交换，原位置{}-{}", curJzId, buyerLoc, targetLoc);

		reduceBagSendEmail(bag, curJz.name, targetLoc, curJz);
		// "申请成功"
		sendApplyResult(session, 0, PD.S_EHOUSE_EXCHANGE_RESULT);
		// 更新房屋信息
		updateHouseInfo(buyerMember.lianMengId, 100, buyerHb, null, null, null);
		updateHouseInfo(buyerMember.lianMengId, 200, null, targetHb, null, null);
		log.info("空小房屋交易成功，更新房屋信息给联盟{}的所有成员", buyerMember.lianMengId);
	}

	/**
	 * @Description: 扣除换房卡，发送成功换房邮件给买家
	 * @param bag
	 * @param curJz
	 * @param targetLoc
	 */
	public void reduceBagSendEmail(Bag<BagGrid> bag, String receiverName,
			int targetLoc, JunZhu buyer) {
		// 扣除购买者的换房卡
		BagMgr.inst.removeItem(bag, huanFangKa, 1, "换房", buyer.level);
		// 删除物品后推送背包信息给玩家
		sendBagAgain(bag.ownerId);
		// ======================================
		{// 给空房子买家发邮件
			// 恭喜主人换取无主的房屋aaa，快去新房子看一看吧！
			Mail cfg = EmailMgr.INSTANCE.getMailConfig(10020);
			String content = cfg.content.replace("aaa",
					getFWName(targetLoc, 101));
			String fuJian = "";
			boolean ok = EmailMgr.INSTANCE.sendMail(receiverName, content,
					fuJian, cfg.sender, cfg, "");
			log.info("发送成功换房邮件给{}成功? {}", receiverName, ok);
		}

	}

	/**
	 * @Description:申请房屋交换
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void applyExchange(int id, IoSession session, Builder builder) {
		ExchangeHouse.Builder req = (qxmobile.protobuf.House.ExchangeHouse.Builder) builder;
		long targetJzId = req.getTargetId();
		Long curJzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (curJzId == null) {
			log.error("没有找到申请小房屋交换的君主id信息");
			return;
		}
		// 买家信息
		JunZhu curJz = JunZhuMgr.inst.getJunZhu(session);
		if (curJzId == targetJzId || curJz == null) {
			log.error("申请小房屋交换的君主信息有误{}", curJzId);
			return;
		}
		// 判断入盟是否超过3天
		AlliancePlayer ap = HibernateUtil.find(AlliancePlayer.class, curJzId);
		if (ap == null) {
			log.error("没有找到申请小房屋交换的君主的联盟信息{}", curJzId);
			return;
		}

		if (isThreeDays(ap)) {
			// 入盟不足3天,无法进行房屋交换
			sendApplyResult(session, 300, PD.S_HOUSE_EXCHANGE_RESULT);
			return;
		}
		Bag<BagGrid> bag = BagMgr.inst.loadBag(curJzId);
		int cnt = BagMgr.inst.getItemCount(bag, huanFangKa);
		if (cnt <= 0) {
			// 没有换房卡
			sendApplyResult(session, 10, PD.S_HOUSE_EXCHANGE_RESULT);
			return;
		}
		HouseBean targetHb = HibernateUtil.find(HouseBean.class, targetJzId);
		if (targetHb == null) {
			log.error("没有找到目标房屋信息{}", targetJzId);
			return;
		}
		HouseBean curHb = HibernateUtil.find(HouseBean.class, curJzId);
		if (curHb == null) {
			log.error("当前玩家的房屋信息没有找到{}", curJzId);
			return;
		}
		if (curHb.lmId <= 0) {
			log.error("当前玩家的房屋信息联盟错误{}:{}", curJzId, curHb.lmId);
			return;
		}
		if (targetHb.lmId != curHb.lmId) {
			log.error("联盟id不匹配,发起者[{}]:[{}],目标[{}]:[{}]", curJzId, curHb.lmId,
					targetJzId, targetHb.lmId);
			return;
		}
		HouseApplyBean app = HibernateUtil.find(HouseApplyBean.class, curJzId);
		if (app != null) {
			// 只能发出一个申请
			sendApplyResult(session, 100, PD.S_HOUSE_EXCHANGE_RESULT);
			return;
		}

		// 卖家信息
		JunZhu targetJz = HibernateUtil.find(JunZhu.class, targetJzId);
		if (targetJz == null) {
			log.error("没有找到目标房屋的君主信息{}", targetJzId);
			return;
		}
		// 判断买家是不是盟主
		boolean isLeader = ap.title == AllianceMgr.TITLE_LEADER;
		if (isLeader) {// 盟主操作
			switch (targetHb.state) {
			case HouseBean.ForUse:
			case HouseBean.ForSell:
			case HouseBean.KingSell:
			case HouseBean.Drop:
				break;
			default:
				// 这个房子有些古怪，请稍后再来。
				sendApplyResult(session, 700, PD.S_HOUSE_EXCHANGE_RESULT);
				return;
			}
			changeHouseForLeader(session, curJz, targetJz, curHb, targetHb, bag);
			return;
		} else {// 非盟主操作
			switch (targetHb.state) {
			case HouseBean.ForSell:
			case HouseBean.KingSell:
				break;
			case HouseBean.ForUse:
				// "该房屋处于自住状态，不可以申请交换"
				sendApplyResult(session, 400, PD.S_HOUSE_EXCHANGE_RESULT);
				return;
			case HouseBean.Drop:// 荒废状态直接完成交换
				changeHouseOfDrop(session, curJz, targetJz, curHb, targetHb,
						bag);
				return;
			default:
				// "这个房子有些古怪，请稍后再来。"
				sendApplyResult(session, 700, PD.S_HOUSE_EXCHANGE_RESULT);
				return;
			}

			if (targetHb.state == HouseBean.KingSell) {
				// 给盟主发邮件
				// 您的部下yyy向您申请与xxx交换房屋，xxx的房屋是由您亲自强行挂牌待售的，
				// 您确定以一枚虎符作为给xxx的补偿批准这次交易吗？

				AllianceBean abean = HibernateUtil.find(AllianceBean.class,
						curJzId);
				long mzId = abean.creatorId;
				JunZhu mz = HibernateUtil.find(JunZhu.class, mzId);
				Mail cfg = EmailMgr.INSTANCE.getMailConfig(10004);
				String content = cfg.content.replace("yyy", curJz.name);
				content = content.replace("xxx", targetJz.name);
				String fuJian = "";
				boolean ok = EmailMgr.INSTANCE.sendMail(mz.name, content,
						fuJian, cfg.sender, cfg, "buyerId:" + curJzId
								+ ",keeperId:" + targetJzId);
				log.info("发送邮件给{}成功? {}", mz.name, ok);
			} else {
				// 给卖家发邮件
				// 玩家xxx想用自己的房屋aaa与您交换您的房屋bbb，您愿意接受这次交易吗？
				// 如果您接受这次交易，将会获得一枚虎符的补偿。
				Mail cfg = EmailMgr.INSTANCE.getMailConfig(10015);
				String content = cfg.content.replace("xxx", curJz.name);
				content = content
						.replace("aaa", getFWName(curHb.location, 101));
				content = content.replace("bbb",
						getFWName(targetHb.location, 101));
				String fuJian = "";
				boolean ok = EmailMgr.INSTANCE.sendMail(targetJz.name, content,
						fuJian, cfg.sender, cfg, "buyerId:" + curJzId);
				log.info("发送邮件给{}成功? {}", targetJz.name, ok);
			}

			app = new HouseApplyBean();
			app.buyerId = curJzId;
			app.keeperId = targetJzId;
			app.dt = Calendar.getInstance().getTime();
			app.emailId = EmailMgr.sentMail.get().getId();
			HibernateUtil.save(app);
			log.info("申请成功,发起者[{}]:[{}],目标[{}]:[{}]", curJzId, curHb.lmId,
					targetJzId, targetHb.lmId);

			// "申请成功"
			sendApplyResult(session, 0, PD.S_HOUSE_EXCHANGE_RESULT);
		}

	}

	/**
	 * @Description:完成与荒芜房子交换
	 * @param session
	 * @param curJz
	 * @param targetJz
	 * @param curHb
	 * @param targetHb
	 * @param bag
	 */
	public void changeHouseOfDrop(IoSession session, JunZhu curJz,
			JunZhu targetJz, HouseBean curHb, HouseBean targetHb,
			Bag<BagGrid> bag) {
		// 取消该房屋的申请
		List<HouseApplyBean> list = HibernateUtil.list(HouseApplyBean.class,
				"where keeperId =" + targetJz.id);
		for (HouseApplyBean b : list) {
			delHouseApplyAndEmail(b);
		}

		// 换房
		int targetLoc = targetHb.location;
		int buyerLoc = curHb.location;
		// 交换房屋信息

		// 坐标
		targetHb.location = buyerLoc;
		curHb.location = targetLoc;
		// 买方房子设定为自住
		curHb.state = HouseBean.ForUse;

		HibernateUtil.save(targetHb);
		HibernateUtil.save(curHb);
		log.info("完成{}与{}的房屋交换，原位置{}-{}", targetJz.id, curJz.id, targetLoc,
				buyerLoc);
		// 扣除购买者的换房卡
		BagMgr.inst.removeItem(bag, huanFangKa, 1, "换房", curJz.level);
		// 删除物品后推送背包信息给玩家
		BagMgr.inst.sendBagInfo(0, session, null);
		// ======================================
		{// 给买家发邮件
			// 恭喜主人与xxx成功交换被荒废已久的房屋aaa，快去大肆修葺一番吧！
			Mail cfg = EmailMgr.INSTANCE.getMailConfig(10019);
			String content = cfg.content.replace("xxx", targetJz.name);
			content = content.replace("aaa", getFWName(targetLoc, 101));
			String fuJian = "";
			boolean ok = EmailMgr.INSTANCE.sendMail(curJz.name, content,
					fuJian, cfg.sender, cfg, "");
			log.info("发送同意换房邮件给{}成功? {} ", curJz.name, ok);
		}
		{// 给荒废房屋主人发邮件
			// 告诉您一个不幸的消息。由于您长期不上线，原有的房屋已经荒废，并与xxx的房屋进行了置换，
			// 现在您的房屋是aaa，快去您的新家看一看吧，今后要好好打理您的房屋哦！
			Mail cfg = EmailMgr.INSTANCE.getMailConfig(10023);
			String content = cfg.content.replace("xxx", curJz.name);
			content = content.replace("aaa", getFWName(buyerLoc, 101));
			String fuJian = "30:" + huFu + ":1";
			boolean ok = EmailMgr.INSTANCE.sendMail(targetJz.name, content,
					fuJian, cfg.sender, cfg, "");
			log.info("发送同意换房邮件给{}成功? {} ", targetJz.name, ok);
		}
		// "申请成功"
		sendApplyResult(session, 0, PD.S_HOUSE_EXCHANGE_RESULT);
	}

	/**
	 * @Description: 盟主换房子
	 * @param session
	 * @param curJz
	 * @param targetJz
	 * @param buyerHb
	 * @param targetHb
	 * @param bag
	 */
	public void changeHouseForLeader(IoSession session, JunZhu curJz,
			JunZhu targetJz, HouseBean buyerHb, HouseBean targetHb,
			Bag<BagGrid> bag) {

		// 取消申请
		List<HouseApplyBean> list = HibernateUtil.list(HouseApplyBean.class,
				"where keeperId =" + targetJz.id);
		for (HouseApplyBean b : list) {
			delHouseApplyAndEmail(b);
		}
		// 换房
		int targetLoc = targetHb.location;
		int buyerLoc = buyerHb.location;
		targetHb.location = buyerLoc;
		buyerHb.location = targetLoc;
		// 卖家房子不是荒芜状态就变为自住（添加关闭状态后，按照策划修改逻辑）
		if (targetHb.state != HouseBean.Drop) {
			targetHb.state = HouseBean.ForUse;
		}
		// 盟主房子变为自住状态
		buyerHb.state = HouseBean.ForUse;

		HibernateUtil.save(targetHb);
		HibernateUtil.save(buyerHb);
		log.info("完成{}与{}的房屋交换，原位置{}-{}", targetJz.id, curJz.id, targetLoc,
				buyerLoc);
		// 扣除购买者的换房卡
		BagMgr.inst.removeItem(bag, huanFangKa, 1, "换房", curJz.level);
		// 删除物品后推送背包信息给玩家
		BagMgr.inst.sendBagInfo(0, session, null);
		{// 给盟主发邮件
			// 恭喜主人与xxx换房成功，我们快去新家看看吧！
			Mail cfg = EmailMgr.INSTANCE.getMailConfig(10002);
			String content = cfg.content.replace("xxx", targetJz.name);
			String fuJian = "";
			boolean ok = EmailMgr.INSTANCE.sendMail(curJz.name, content,
					fuJian, cfg.sender, cfg, "");
			log.info("发送恭喜换房邮件给{}成功? {}", curJz.name, ok);
			sendMailToBuyer(curJz.name, curJz.name);
		}
		{// 给被盟主换房的人发邮件
			// 盟主大人出于联盟管理的需要，将我们家的房屋xxx换成了yyy，为了弥补我们家的损失，
			// 盟主额外补偿了我们一个虎符。如果主人有异议，快去找盟主交涉呦！
			Mail cfg = EmailMgr.INSTANCE.getMailConfig(10001);
			String content = cfg.content
					.replace(
							"xxx",
							getFWName(targetHb.location - 100,
									targetHb.location - 100));
			content = content.replace("yyy",
					getFWName(buyerHb.location - 100, buyerHb.location - 100));
			String fuJian = "30:" + huFu + ":1";
			boolean ok = EmailMgr.INSTANCE.sendMail(targetJz.name, content,
					fuJian, cfg.sender, cfg, "");

			log.info("发送同意换房邮件给{}成功? {}", targetJz.name, ok);
		}
		// "申请成功"
		sendApplyResult(session, 0, PD.S_HOUSE_EXCHANGE_RESULT);

		// 更新房屋信息
		updateHouseInfo(buyerHb.lmId, 300, buyerHb, targetHb, null, null);
		log.info("盟主强买普通小房屋成功，更新房屋信息给联盟{}的所有成员", buyerHb.lmId);

	}

	/**
	 * @Description: 判断是否加入联盟是否超过3天
	 * @param buyerAp
	 * @return
	 */
	public boolean isThreeDays(AlliancePlayer buyerAp) {
		Date today = new Date();
		long cha = today.getTime() - buyerAp.joinTime.getTime();
		if (cha * 1.0 / (1000 * 60 * 60) > 72) {
			return false;
		}
		return true;
	}

	/**
	 * @Description: 申请交换房子返回结果
	 * @param session
	 * @param code
	 * @param id
	 */
	public void sendApplyResult(IoSession session, int code, int id) {
		ProtobufMsg returnMsg = new ProtobufMsg();
		ExchangeResult.Builder b = ExchangeResult.newBuilder();
		b.setCode(code);
		returnMsg.builder = b;
		returnMsg.id = id;
		session.write(returnMsg);
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
		log.info("退出HouseMgr");
	}

	public void shutdown() {
		missions.add(exit);
	}

	public void handle(Mission m) {
		int id = m.code;
		IoSession session = m.session;
		Builder builder = m.builer;
		switch (m.code) {
		case PD.C_LM_HOUSE_INFO:
			getBatchInfo(id, session, builder);
			break;
		case PD.C_Set_House_state:
			setHouseForSell(id, session, builder);
			break;
		case PD.C_HOUSE_EXCHANGE_RQUEST:
			applyExchange(id, session, builder);
			break;
		case PD.C_EHOUSE_EXCHANGE_RQUEST:
			applyChangeEH(id, session, builder);
			break;
		case PD.C_HOUSE_APPLY_LIST:
			gettApplyList(session);
			break;
		case PD.C_AnswerExchange:
			answerEx(id, session, builder);
			break;
		case PD.C_CANCEL_EXCHANGE:
			cancelApply(id, session, builder);
			break;
		case PD.C_EnterOrExitHouse:
			enterOrExitHouse(id, session, builder);
			break;
		case PD.C_ShotOffVisitor:
			shotOffVisitor(id, session, builder);
			break;
		case PD.C_GetHouseVInfo:
			getHouseVInfo(id, session, builder);
			break;
		case PD.C_get_house_exp:
			gainExpFromHouse(id, session, builder);
			break;
		case PD.C_get_house_info:
			getHouseInfo(id, session, builder);
			break;
		case PD.C_GET_BIGHOUSE_EXP:
			gainExpFromBigHouse(id, session, builder);
			break;
		case PD.C_huan_wu_info:
			sendHuanWu(id, session, builder);
			break;
		case PD.C_huan_wu_Oper:
			setHuanWu(id, session, builder);
			break;
		case PD.C_huan_wu_list:
			sendBoxList(id, session, builder);
			break;
		case PD.C_huan_wu_exchange: {
			exchangeBox(session, builder);
		}
			break;
		case PD.C_ExCanJuanJiangLi: {
			exCanJuan(session, builder);
		}
			break;
		case PD.C_up_house: {
			upHouse(session);
		}
			break;
		case PD.C_Pai_big_house:
			paiBigHouse(id, session, builder);
			break;
		default:
			log.error("未处理的消息{}", id);
			break;
		}
	}

	/**
	 * @Description:竞拍大房子
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void paiBigHouse(int id, IoSession session, Builder builder) {
		ExchangeHouse.Builder req = (qxmobile.protobuf.House.ExchangeHouse.Builder) builder;
		long targetJzId = req.getTargetId();
		// 目标高级房屋
		BigHouse targetBh = HibernateUtil.find(BigHouse.class, targetJzId);
		if (targetBh == null) {
			log.error("没有找到目标房屋信息{}:{}", targetJzId);
			return;
		}
		// 买家信息
		JunZhu buyerJz = JunZhuMgr.inst.getJunZhu(session);
		if (buyerJz == null) {
			log.error("没有找到竞拍君主-{}的大房子的买家信息", targetJzId);
			return;
		} else if (buyerJz.id == targetJzId) {
			log.error("不能竞拍自己的大房子{}", targetJzId);
			return;
		}
		// 买家房屋
		BigHouse buyerBh = HibernateUtil.find(BigHouse.class, buyerJz.id);
		if (buyerBh == null) {
			log.error("{}没有大房，不能拍其他大房", buyerJz.id);
			return;
		}
		if (buyerBh.lmId <= 0) {
			log.error("竞拍的目标大房子联盟信息有误{}:{}", targetJzId, buyerBh.lmId);
			return;
		}
		if (targetBh.lmId != buyerBh.lmId) {
			log.error("联盟id不匹配,发起者[{}]:[{}],目标[{}]:[{}]", buyerJz.id,
					buyerBh.lmId, targetJzId, targetBh.lmId);
			return;
		}

		// 查询玩家的联盟成员自身
		AlliancePlayer buyerMember = HibernateUtil.find(AlliancePlayer.class,
				buyerJz.id);
		// 查询目标高级房屋配置
		int zenglv = CanShu.FANGWUJINGPAI_3;// 竞拍价格相对房屋价值比率
		int jingpaiPrice = (int) Math.rint(((double) targetBh.hworth) * zenglv
				/ 100);
		if (buyerMember.gongXian < jingpaiPrice) {
			// 您当前的贡献值不足以竞拍此高级房屋
			sendApplyResult(session, 500, PD.S_Pai_big_house);
			return;
		}
		// 若为无主大房子直接拍下
		if (targetJzId < 0) {
			jingpaiFreeBigHouse(targetBh, buyerBh, buyerMember, buyerJz.name,
					targetJzId, jingpaiPrice, session);
			return;
		}
		// 判断房屋是否处于竞拍时间
		Date today = new Date();
		long cha = (today.getTime() - targetBh.previousHoldTime.getTime());
		if (cha * 1.0 / (1000 * 60 * 60) <= 24) {
			// 此高级房的使用权处于保护时间内无法被竞拍
			sendApplyResult(session, 600, PD.S_Pai_big_house);
			return;
		}
		// 目标房屋的主人信息
		JunZhu targetJz = HibernateUtil.find(JunZhu.class, targetJzId);
		if (targetJz == null) {
			log.error("没有找到目标大房屋的主人信息{}", targetJzId);
			return;
		}

		int targetLoc = targetBh.location;
		int buyerLoc = buyerBh.location;
		int curWorth = buyerBh.hworth;

		// 返还贡献发送邮件
		AlliancePlayer targetMember = HibernateUtil.find(AlliancePlayer.class,
				targetJzId);
		if (targetBh.previousWorth != 0) {// 目标房屋价值返还
			double returnWorth = targetBh.previousWorth
					* CanShu.FANGWUJINGPAI_2 / 100;// 竞拍价值*贡献与价值转化率
			// 返还贡献*返还贡献比率
			int returnGongXian = (int) Math.rint(returnWorth
					* CanShu.FANGWUJINGPAI_4 / 100);
			targetMember.gongXian += returnGongXian;
			HibernateUtil.save(targetMember);
			log.info("返还{}竞拍高级房屋花费{}", targetBh.previousId, returnGongXian);
			{// 给目标主人发邮件
				// 您的豪宅aaa已经被xxx以高价拍走，您如今居住在bbb。您当初以ccc的贡献值拍下的豪宅将返还ddd以补偿您的损失，
				// 同时扣除eee的贡献值作为房屋的折旧费。
				Mail cfg = EmailMgr.INSTANCE.getMailConfig(10009);
				String content = cfg.content.replace("aaa",
						getFWName(targetLoc - 100, targetLoc - 100));
				content = content.replace("xxx", buyerJz.name);
				content = content.replace("bbb",
						getFWName(buyerLoc - 100, buyerLoc - 100));
				content = content.replace("ccc",
						String.valueOf(targetBh.previousWorth));
				content = content
						.replace("ddd", String.valueOf(returnGongXian));
				content = content
						.replace(
								"eee",
								String.valueOf(targetBh.previousWorth
										- returnGongXian));
				String fuJian = "";
				boolean ok = EmailMgr.INSTANCE.sendMail(targetJz.name, content,
						fuJian, cfg.sender, cfg, "");

				log.info("发送高级房屋换房邮件给{}成功? {}", targetJz.name, ok);
			}
		} else {
			// 您的豪宅aaa已经被xxx以高价拍走了！此人竟然如此嚣张，
			// 胆敢与主人争抢豪宅的使用权！主人快把豪宅拍回来教训教训他！。
			Mail cfg = EmailMgr.INSTANCE.getMailConfig(10008);
			String content = cfg.content.replace("aaa",
					getFWName(targetLoc - 100, targetLoc - 100));
			content = content.replace("xxx", targetJz.name);
			String fuJian = "";
			boolean ok = EmailMgr.INSTANCE.sendMail(targetJz.name, content,
					fuJian, cfg.sender, cfg, "");
			log.info("发送高级房屋换房邮件给{}成功? {}", targetJz.name, ok);
		}
		if (buyerBh.previousWorth != 0) {// 目标房屋价值返还
			double returnWorth = targetBh.previousWorth
					* CanShu.FANGWUJINGPAI_2 / 100;// 竞拍价值*贡献与价值转化率
			// 返还贡献*返还贡献比率
			int returnGongXian = (int) Math.rint(returnWorth
					* CanShu.FANGWUJINGPAI_4 / 100);
			targetMember.gongXian += returnGongXian;
			HibernateUtil.save(targetMember);
			log.info("返还{}竞拍高级房屋花费{}", targetBh.previousId, returnGongXian);
			{// 给目标主人发邮件
				// 您已成功拍得豪宅bbb,您当初以ccc的贡献值拍下的aaa将返还ddd给您。
				Mail cfg = EmailMgr.INSTANCE.getMailConfig(10024);
				String content = cfg.content.replace("aaa",
						getFWName(buyerLoc - 100, buyerLoc - 100));
				content = content.replace("bbb",
						getFWName(targetLoc - 100, targetLoc - 100));
				content = content.replace("ccc",
						String.valueOf(buyerBh.previousWorth));
				content = content
						.replace("ddd", String.valueOf(returnGongXian));
				String fuJian = "";
				boolean ok = EmailMgr.INSTANCE.sendMail(buyerJz.name, content,
						fuJian, cfg.sender, cfg, "");

				log.info("发送高级房屋换房邮件给{}成功? {}", buyerJz.name, ok);
			}
		} else {
			// 给买家发邮件
			sendMailToBigBuyer(buyerJz.name);
		}

		// 扣除贡献(竞拍价格乘以贡献与房屋价值转换率)
		buyerMember.gongXian -= jingpaiPrice * CanShu.FANGWUJINGPAI_2;
		HibernateUtil.save(buyerMember);

		// 交换房屋位置
		buyerBh.location = targetLoc;
		targetBh.location = buyerLoc;
		// 更新贡献

		buyerBh.gongXian = buyerMember.gongXian;
		targetBh.gongXian = targetMember.gongXian;

		// 更新房子的前任主人id 、此次竞拍的价值、此次竞拍入住时间
		targetBh.previousId = 0;
		targetBh.previousWorth = 0;
		targetBh.previousHoldTime = today;
		buyerBh.previousId = buyerJz.id;
		buyerBh.previousWorth = jingpaiPrice;
		buyerBh.previousHoldTime = today;

		// 交换价值
		buyerBh.hworth = jingpaiPrice;
		targetBh.hworth = curWorth;

		HibernateUtil.save(targetBh);
		HibernateUtil.save(buyerBh);

		log.info("{}竞拍{}的高级房屋成功，竞拍前后分别为{}-{}", buyerJz.id, targetJzId,
				buyerBh.location, targetBh.location);
		// 大房竞拍成功
		sendApplyResult(session, 0, PD.S_Pai_big_house);

		// 更新联盟成员房屋信息
		updateHouseInfo(buyerMember.lianMengId, 300, null, null, buyerBh,
				targetBh);
		log.info("{}竞拍{}大房屋成功,更新房屋信息给{}联盟所有成员", buyerJz.id, targetJzId,
				buyerMember.lianMengId);
	}

	/**
	 * @Description: 竞拍无主高级房子
	 * @param targetBh
	 * @param buyerBh
	 * @param buyerMember
	 * @param curJzName
	 * @param targetJzId
	 * @param jingpaiPrice
	 * @param session
	 */
	public void jingpaiFreeBigHouse(BigHouse targetBh, BigHouse buyerBh,
			AlliancePlayer buyerMember, String curJzName, long targetJzId,
			int jingpaiPrice, IoSession session) {
		// 交换房屋位置和价值
		int targetLoc = targetBh.location;
		int buyerLoc = buyerBh.location;
		int curWorth = buyerBh.hworth;

		// 更新房子的前任主人id 、此次竞拍的价值、此次竞拍入住时间
		targetBh.previousId = 0;
		targetBh.previousWorth = 0;
		targetBh.previousHoldTime = buyerBh.previousHoldTime;
		buyerBh.previousId = targetJzId;
		buyerBh.previousWorth = jingpaiPrice;
		buyerBh.previousHoldTime = new Date();

		buyerBh.location = targetLoc;
		targetBh.location = buyerLoc;

		buyerBh.hworth = jingpaiPrice;
		targetBh.hworth = curWorth;

		// 扣除贡献
		buyerMember.gongXian -= jingpaiPrice;
		HibernateUtil.save(buyerMember);
		// 更新贡献
		buyerBh.gongXian = buyerMember.gongXian;
		targetBh.gongXian = 0;
		// 保存房屋信息
		HibernateUtil.save(targetBh);
		HibernateUtil.save(buyerBh);

		// 判断目标房屋价值是否返还，给买家发邮件
		if (buyerBh.previousWorth != 0) {
			double returnWorth = targetBh.previousWorth
					* CanShu.FANGWUJINGPAI_2 / 100;// 竞拍价值*贡献与价值转化率
			// 返还贡献*返还贡献比率
			int returnGongXian = (int) Math.rint(returnWorth
					* CanShu.FANGWUJINGPAI_4 / 100);
			buyerMember.gongXian += returnGongXian;
			HibernateUtil.save(buyerMember);
			log.info("返还{}竞拍高级房屋花费{}", targetBh.previousId, returnGongXian);
			{// 给目标主人发邮件
				// 您已成功拍得豪宅bbb，您当初以ccc的贡献值拍下的aaa将返还ddd给您。
				Mail cfg = EmailMgr.INSTANCE.getMailConfig(10024);
				String content = cfg.content.replace("aaa",
						getFWName(buyerLoc - 100, buyerLoc - 100));
				content = content.replace("bbb",
						getFWName(targetLoc - 100, targetLoc - 100));
				content = content.replace("ccc",
						String.valueOf(buyerBh.previousWorth));
				content = content
						.replace("ddd", String.valueOf(returnGongXian));
				String fuJian = "";
				boolean ok = EmailMgr.INSTANCE.sendMail(curJzName, content,
						fuJian, cfg.sender, cfg, "");

				log.info("发送高级房屋换房邮件给{}成功? {}", curJzName, ok);
			}
		} else {
			// 给买家发邮件
			sendMailToBigBuyer(curJzName);
		}
		log.info("{}竞拍无主的高级房屋成，竞拍前后位置分别为{}-{}", buyerMember.junzhuId, buyerLoc,
				targetLoc);
		// 大房竞拍成功
		sendApplyResult(session, 0, PD.S_Pai_big_house);
		// 更新联盟成员房屋信息
		updateHouseInfo(buyerMember.lianMengId, 300, null, null, buyerBh,
				targetBh);
		log.info("{}竞拍无主大房屋成功,更新房屋信息给{}联盟的所有联盟成员", buyerMember.junzhuId,
				buyerMember.lianMengId);
	}

	public void upHouse(IoSession session) {
		Long curJzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (curJzId == null) {
			log.error("没有找到升级当前房屋的君主id");
			return;
		}
		HouseBean selfBean = HibernateUtil.find(HouseBean.class, curJzId);
		if (selfBean == null)
			return;
		if (selfBean.level >= 10)
			return;
		int lv = Math.max(1, selfBean.level);
		List<?> fwList = TempletService.listAll(FangWu.class.getSimpleName());
		if (fwList == null) {
			log.error("没有房屋配置");
			return;
		}
		FangWu fwConf = (FangWu) fwList.get(lv - 1);
		if (fwConf.type != 101 || fwConf.lv != lv) {
			log.error("房屋配置表错误 type {}, lv {} , 期望type 101，lv{}", fwConf.type,
					fwConf.lv, lv);
			return;
		}
		AlliancePlayer ap = HibernateUtil.find(AlliancePlayer.class, curJzId);
		if (ap == null || ap.lianMengId <= 0)
			return;
		if (ap.gongXian < fwConf.needNum) {
			log.info("{}贡献值不足，需要{}实际有{}", curJzId, fwConf.needNum, ap.gongXian);
			return;
		}
		// vip等级对应的房屋升级次数
		JunZhu junZhu = HibernateUtil.find(JunZhu.class, curJzId);
		if (junZhu == null)
			return;
		int vipUpTimes = VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel,
				VipData.fangWubuildNum);
		int sumConfUpTimes = vipUpTimes + fwConf.buildNum;
		Date date = new Date();
		if (selfBean.preUpTime == null) {// 之前没装修过
			// change 20150901
		} else if (DateUtils.isTimeToReset(selfBean.preUpTime, CanShu.REFRESHTIME_PURCHASE)) {// 上次装修时间早于今天，重置次数。
			selfBean.todayUpTimes = 0;
		} else if (selfBean.todayUpTimes >= sumConfUpTimes) {// 今日次数已用完
			log.info("{}今日装修次数已用完{}", curJzId, selfBean.todayUpTimes);
			return;
		} else if ((date.getTime() - selfBean.preUpTime.getTime()) < fwConf.coolTime * 1000) {
			log.info("{}房屋装修冷却时间未到，上次时间{}", curJzId,
					selfBean.preUpTime.toLocaleString());
			return;
		}
		selfBean.todayUpTimes += 1;
		selfBean.preUpTime = date;
		synchronized (ap) {
			ap.gongXian -= fwConf.needNum;
			HibernateUtil.save(ap);
		}
		selfBean.houseExp += fwConf.addNum;
		if (selfBean.houseExp >= fwConf.exp) {
			selfBean.level += 1;
			selfBean.houseExp -= fwConf.exp;
			log.info("{}房屋升级到{}", curJzId, selfBean.level);
		}
		HibernateUtil.save(selfBean);
		log.info("{}花费{}增加房屋经验{}", curJzId, fwConf.needNum, fwConf.addNum);
		qxmobile.protobuf.House.HouseExpInfo.Builder info = makeHouseExpInfo(selfBean);
		//2015年9月8日 返回君主当前贡献值
		info.setCurGongxian(ap.gongXian);
		session.write(info.build());
		AllianceMgr.inst.requestAllianceInfo(0, session, null);
		// 主线任务: 成功装修1次房屋算完成任务（房屋升级） 20190916
		EventMgr.addEvent(ED.fix_house , new Object[] {curJzId});
	}

	public void exCanJuan(IoSession session, Builder builder) {
		ExCanJuanJiangLi.Builder req = (qxmobile.protobuf.House.ExCanJuanJiangLi.Builder) builder;
		if (req == null)
			return;
		int code = req.getCode();
		if (code < 1 || code > 5)
			return;
		int base = 940000 + code * 1000;
		Long curJzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (curJzId == null) {
			return;
		}
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null)
			return;
		Bag<BagGrid> bag = BagMgr.inst.loadBag(curJzId);
		for (int i = 1; i <= 5; i++) {
			int cnt = BagMgr.inst.getItemCount(bag, base + i);
			if (cnt < 1) {
				log.info("{}兑换残卷缺少{}", curJzId, base + i);
				return;
			}
		}
		for (int i = 1; i <= 5; i++) {
			BagMgr.inst.removeItem(bag, base + i, 1, "兑换残卷", jz.level);
		}
		int awardId = 200 + code * 10;
		Jiangli awardConf = PurchaseMgr.inst.jiangliMap.get(awardId);
		if (awardConf == null) {
			log.error("配置{}没有找到，奖励没有发给玩家{}", awardId, jz.id);
			return;
		}
		DailyAwardMgr.inst.giveAward(session, awardConf, jz);
		log.info("{}兑换了残卷{}，给予奖励{}", curJzId, base, awardId);
		ExItemResult.Builder msg = ExItemResult.newBuilder();
		msg.setCode(0);
		msg.setMsg("ok");
		ProtobufMsg pm = new ProtobufMsg();
		pm.id = PD.S_ExCanJuanJiangLi;
		pm.builder = msg;
		session.write(pm);
		// 删除物品后推送背包信息给玩家
		BagMgr.inst.sendBagInfo(0, session, null);
	}

	public void exchangeBox(IoSession session, Builder builder) {
		ExchangeItem.Builder req = (qxmobile.protobuf.House.ExchangeItem.Builder) builder;
		long targetJzId = req.getTargetJzId();
		int selfIdx = req.getSelfIdx();
		int targetIdx = req.getTargetIdx();
		if (selfIdx <= 0 || selfIdx >= 6)
			return;
		if (targetIdx <= 0 || targetIdx >= 6)
			return;
		Long curJzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (curJzId == null) {
			log.error("没有找到换物操作的君主id");
			return;
		}
		HuanWu selfBean = HibernateUtil.find(HuanWu.class, curJzId);
		HuanWu targetBean = HibernateUtil.find(HuanWu.class, targetJzId);
		if (selfBean == null || targetBean == null)
			return;
		String targetItemId = req.getTargetItemId();
		String selfItemId = req.getSelfItemId();
		if (targetItemId.equals(selfItemId))
			return;
		if (targetItemId.equals("0") || selfItemId.equals("0"))
			return;
		String targetCur = getBoxGridItem(targetIdx, targetBean);
		String selfCur = getBoxGridItem(selfIdx, selfBean);
		if (targetItemId.equals(targetCur) == false
				|| selfItemId.equals(selfCur) == false) {// 信息已经过时。
			log.warn("目标物品id和服务器不一致");
			ExItemResult.Builder em = ExItemResult.newBuilder();
			em.setCode(2);
			em.setMsg("目标物品id和服务器不一致");
			session.write(em.build());
			return;
		}
		setGrid(targetIdx, targetBean, Integer.parseInt(selfItemId));
		HibernateUtil.save(targetBean);
		setGrid(selfIdx, selfBean, Integer.parseInt(targetItemId));
		HibernateUtil.save(selfBean);
		ExItemResult.Builder em = ExItemResult.newBuilder();
		em.setCode(0);
		em.setMsg("换物箱交换成功。");
		session.write(em.build());
		log.info("{}用{}交换了{}的{}", curJzId, selfItemId, targetJzId, targetItemId);
	}

	public void sendBoxList(int id, IoSession session, Builder builder) {
		Long curJzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (curJzId == null) {
			log.error("没有找到请求换物箱列表的君主id");
			return;
		}
		AlliancePlayer ap = HibernateUtil.find(AlliancePlayer.class, curJzId);
		if (ap == null || ap.lianMengId <= 0) {
			log.error("请求换物箱列表的君主联盟信息有误{}", curJzId);
			return;
		}
		List<HuanWu> list = HibernateUtil.list(HuanWu.class, " where lmId ="
				+ ap.lianMengId);
		qxmobile.protobuf.House.LianMengBoxes.Builder ret = LianMengBoxes
				.newBuilder();
		for (HuanWu bean : list) {
			HuanWuInfo.Builder b = HuanWuInfo.newBuilder();
			if (bean.jzId == curJzId)
				continue;
			b.setJzId(bean.jzId);
			b.setJzName(bean.jzName);
			b.setSlot1(bean.slot1 == null ? "0" : bean.slot1);
			b.setSlot2(bean.slot2 == null ? "0" : bean.slot2);
			b.setSlot3(bean.slot3 == null ? "0" : bean.slot3);
			b.setSlot4(bean.slot4 == null ? "0" : bean.slot4);
			b.setSlot5(bean.slot5 == null ? "0" : bean.slot5);
			ret.addBoxList(b);
		}
		session.write(ret.build());
		log.info("发送盟友换物信息给{}", curJzId);
	}

	public void setHuanWu(int id, IoSession session, Builder builder) {
		qxmobile.protobuf.House.setHuanWu.Builder req = (qxmobile.protobuf.House.setHuanWu.Builder) builder;
		Long curJzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (curJzId == null) {
			log.error("没有找到设置换物箱的君主id");
			return;
		}
		JunZhu jz = HibernateUtil.find(JunZhu.class, curJzId);
		int gridIdx = req.getBoxIdx();
		int boxIdx = gridIdx;
		Bag<BagGrid> bag = BagMgr.inst.loadBag(curJzId);
		HuanWu bean = HibernateUtil.find(HuanWu.class, curJzId);
		int itemId = req.getItemId();
		switch (req.getCode()) {
		case 10: { // 10把物品放进箱子，20把箱子里的物品扯下
			int cnt = BagMgr.inst.getItemCount(bag, itemId);
			if (cnt < 1)
				return;
//2015年10月14日删除物品逻辑修正
//			BagMgr.inst.removeItem(bag, itemId, 1, "放入换物箱", jz.level);
			if (bean == null) {
				bean = new HuanWu();
				bean.jzId = curJzId;
				bean.jzName = HibernateUtil.find(JunZhu.class, curJzId).name;
				AlliancePlayer ap = HibernateUtil.find(AlliancePlayer.class,
						curJzId);
				if (ap == null) {
					log.error("没有找到设置换物箱的君主联盟信息{}", curJzId);
					return;
				}
				bean.lmId = ap.lianMengId;
				setGrid(1, bean, itemId);
				BagMgr.inst.removeItem(bag, itemId, 1, "放入换物箱", jz.level);
				HibernateUtil.save(bean);
				log.info("{}将{}放入换物箱{}", curJzId, itemId, boxIdx);
			} else {
				// 查找位置
				gridIdx = -1;// 放入时不用指定位置，自动查找一个可用位置。
				for (int i = 1; i <= 5; i++) {
					String grid = getBoxGridItem(i, bean);
					if (grid == null || grid.equals("0")) {
						gridIdx = i;
						break;
					}
				}
				if (gridIdx <= 0) {
					log.info("{}换物箱已满", bean.jzId);
					return;
				}
				String preOne = getBoxGridItem(gridIdx, bean);
				if (preOne == null || preOne.equals("0")) {// || );
					setGrid(gridIdx, bean, itemId);
					BagMgr.inst.removeItem(bag, itemId, 1, "放入换物箱", jz.level);
					HibernateUtil.save(bean);
					log.info("{}将{}放入换物箱{}，之前是空格子。", curJzId, itemId, gridIdx);
				} else if (preOne.equals(String.valueOf(itemId))) {// 和之前的一样，不做处理

				} else {// 之前的拿下来，新的放上去。
					int preItemId = Integer.parseInt(preOne);
					BagMgr.inst.addItem(bag, preItemId, 1, -1, jz.level,"放入换物箱");
					setGrid(gridIdx, bean, itemId);
					BagMgr.inst.removeItem(bag, itemId, 1, "放入换物箱", jz.level);
					HibernateUtil.save(bean);
					log.info("{}将{}放入换物箱{}，之前是{}。", curJzId, itemId, boxIdx,
							preItemId);
				}
			}
			BagMgr.inst.sendBagInfo(0, session, null);
			sendHuanWu(0, session, null);
		}
			break;
		case 20: {// 20把箱子里的物品扯下
			if (boxIdx <= 0 || boxIdx >= 6)
				return;
			if (bean == null)
				return;
			String preOne = getBoxGridItem(gridIdx, bean);
			if (preOne == null || preOne.length() <= 1) {
				log.warn("之前道具有误{}，idx{}", preOne, boxIdx);
				return;
			}
			;// 空格子
			int preItemId = Integer.parseInt(preOne);
			BagMgr.inst.addItem(bag, preItemId, 1, -1, jz.level,"换物箱替换");
			switch (gridIdx) {
			case 1:
				bean.slot1 = "0";
				break;
			case 2:
				bean.slot2 = "0";
				break;
			case 3:
				bean.slot3 = "0";
				break;
			case 4:
				bean.slot4 = "0";
				break;
			case 5:
				bean.slot5 = "0";
				break;
			}
			HibernateUtil.save(bean);
			log.info("{}将换物箱位置{}的{}取下", curJzId, boxIdx, itemId);
			BagMgr.inst.sendBagInfo(0, session, null);
			sendHuanWu(0, session, null);
			break;
		}
		default:
			log.error("unknow code {} for set huan wu", req.getCode());
			return;
		}
	}

	public String getBoxGridItem(int gridIdx, HuanWu bean) {
		String preOne = "0";
		switch (gridIdx) {
		case 1:
			preOne = bean.slot1;
			break;
		case 2:
			preOne = bean.slot2;
			break;
		case 3:
			preOne = bean.slot3;
			break;
		case 4:
			preOne = bean.slot4;
			break;
		case 5:
			preOne = bean.slot5;
			break;
		}
		return preOne;
	}

	public void setGrid(int gridIdx, HuanWu bean, int itemId) {
		switch (gridIdx) {
		case 1:
			bean.slot1 = String.valueOf(itemId);
			break;
		case 2:
			bean.slot2 = String.valueOf(itemId);
			break;
		case 3:
			bean.slot3 = String.valueOf(itemId);
			break;
		case 4:
			bean.slot4 = String.valueOf(itemId);
			break;
		case 5:
			bean.slot5 = String.valueOf(itemId);
			break;
		}
	}

	public void sendHuanWu(int id, IoSession session, Builder builder) {
		Long curJzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (curJzId == null) {
			log.error("没有请求自己换物箱的君主id");
			return;
		}
		HuanWu bean = HibernateUtil.find(HuanWu.class, curJzId);
		HuanWuInfo.Builder ret = HuanWuInfo.newBuilder();
		ret.setJzId(0);
		ret.setJzName("a");
		ret.setSlot1(bean == null ? "0" : bean.slot1 == null ? "0" : bean.slot1);
		ret.setSlot2(bean == null ? "0" : bean.slot2 == null ? "0" : bean.slot2);
		ret.setSlot3(bean == null ? "0" : bean.slot3 == null ? "0" : bean.slot3);
		ret.setSlot4(bean == null ? "0" : bean.slot4 == null ? "0" : bean.slot4);
		ret.setSlot5(bean == null ? "0" : bean.slot5 == null ? "0" : bean.slot5);
		session.write(ret.build());
		log.info("发送自己的换物信息给{}", curJzId);
	}

	/**
	 * @Description:加入联盟重新关联联盟换物箱信息
	 * @param jzId
	 * @param lmId
	 */
	public void resetHuanWu(int lmId, long jzId) {
		HuanWu bean = HibernateUtil.find(HuanWu.class, jzId);
		if (bean == null) {
			log.info("加入联盟{},君主{}没有换物箱信息", lmId, jzId);
			return;
		}
		bean.lmId = lmId;
		HibernateUtil.save(bean);
		log.info("加入联盟更新{}的换物箱信息", jzId);
	}

	/**
	 * @Description: 进出小屋
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void enterOrExitHouse(int id, IoSession session, Builder builder) {
		Long curJzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (curJzId == null) {
			log.error("未找到进出小屋的君主id信息");
			return;
		}
		EnterOrExitHouse.Builder req = (EnterOrExitHouse.Builder) builder;
		long houseId = req.getHouseId();
		switch (req.getCode()) {
		case 10: {// 10进入，20离开。
			Map<Long, IoSession> set = playerInHouse.get(houseId);
			if (set == null) {
				set = new ConcurrentHashMap<Long, IoSession>();
			}
			set.put(curJzId, session);
			playerInHouse.put(houseId, set);
			log.info("{}进入房屋{}", curJzId, houseId);
			inWhichHouse.put(curJzId, houseId);
		}
			break;
		case 20: {
			Map<Long, IoSession> set = playerInHouse.get(houseId);
			if (set != null) {
				set.remove(curJzId);
				log.info("{}退出房屋{}", curJzId, houseId);
			} else {
				log.error("{}退出房子是并不在该房子内{}", curJzId, houseId);
			}
			inWhichHouse.remove(curJzId);
		}
			break;
		default:
			log.error("未知的进入、离开房屋操作码{}", req.getCode());
			return;
		}
	}

	/**
	 * @Description: 获取访客列表
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void getHouseVInfo(int id, IoSession session, Builder builder) {
		Long houseId = (Long) session.getAttribute(SessionAttKey.junZhuId);// 获取当前用户的房子id即用户id
		if (houseId == null) {
			log.error("未找到请求访客列表的小屋的houseId信息");
			return;
		}
		log.info("{}请求获取访客列表",houseId);
		// 获取当前用户房子中的人物集合
		Map<Long, IoSession> map = playerInHouse.get(houseId);
		HouseVisitorInfo.Builder ret = HouseVisitorInfo.newBuilder();
		if(map!=null){
			for (Long jzid : map.keySet()) {
				if (jzid.equals(houseId))
					continue;// 不传本人信息
				VisitorInfo.Builder visitor = VisitorInfo.newBuilder();
				visitor.setJzId(jzid);
				JunZhu jz = HibernateUtil.find(JunZhu.class, jzid);
				visitor.setJzName(jz.name);
				visitor.setLevel(jz.level);
				// 军衔
				PvpBean bean = HibernateUtil.find(PvpBean.class, jzid);
				if (bean != null) {
					visitor.setJunxian(bean.junXianLevel);
				} else {
					// 没有军衔默认为小卒
					visitor.setJunxian(1);
				}

				AlliancePlayer abean = HibernateUtil.find(AlliancePlayer.class,
						jzid);
				if (abean != null) {
					visitor.setGuanxian(abean.title);// 官衔
					visitor.setGongxian(abean.gongXian);// 贡献
				} else {
					visitor.setGuanxian(0);// 官衔
					visitor.setGongxian(0);// 贡献
				}
				ret.addList(visitor);
			}
		}else{
			log.info("房屋houseId--{}中没有访客",houseId);
		}
		ProtobufMsg pm = new ProtobufMsg();
		pm.id = PD.S_HouseVInfo;
		pm.builder = ret;
		session.write(pm);
		log.info("发送自己的访客信息{}", houseId);
	}

	/**
	 * @Description: 踢出房屋
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void shotOffVisitor(int id, IoSession session, Builder builder) {
		Long houseId = (Long) session.getAttribute(SessionAttKey.junZhuId);// 获取当前用户的房子id即用户id
		if (houseId == null) {
			log.error("未找到进行踢出访客操作的小屋的houseId信息");
			return;
		}
		OffVisitorInfo.Builder req = (OffVisitorInfo.Builder) builder;
		long visitorId = req.getVisitorId();
		// 获取当前用户房子中的人物集合
		Map<Long, IoSession> set = playerInHouse.get(houseId);
		if (set != null) {
			if (visitorId > 0) {
				switch (req.getCode()) {
				case 20:
					set.remove(visitorId);
					SessionUser su = SessionManager.inst
							.findByJunZhuId(visitorId);
					if (su != null) {
						OffVisitorInfo.Builder vs = OffVisitorInfo.newBuilder();
						vs.setVisitorId(visitorId);
						vs.setHouseId(houseId);
						vs.setCode(10);// 10表示被踢出房间
						ProtobufMsg msg = new ProtobufMsg();
						msg.id = PD.S_ShotOffVisitor;
						msg.builder = vs;
						su.session.write(msg);
					}
					log.info("{}被逐出{}房屋", visitorId, houseId);
					break;
				case 10:
					log.info("{}逐出房屋操作码{}错误", visitorId, req.getCode());
				default:
					log.error("未知的逐出房屋操作码{}", req.getCode());
					return;
				}
			}
		} else {
			log.error("{}退出房子是并不在该房子内{}", visitorId, houseId);
		}
	}

	/**
	 * @Description: 离开、加入联盟更新房屋信息
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void updateHouseInfoForLianMeng(int lmId, int code, HouseBean hb,
			BigHouse bh) {
		List<AlliancePlayer> memberList = HibernateUtil.list(
				AlliancePlayer.class, "where lianMengId=" + lmId);
		for (AlliancePlayer member : memberList) {
			if (hb.jzId != member.junzhuId) {
				SessionUser su = SessionManager.inst
						.findByJunZhuId(member.junzhuId);
				if (su != null) {
					HouseUpdateInfo.Builder ret = HouseUpdateInfo.newBuilder();
					ret.addInfos(addHouseBeanInf(hb).build());
					if (bh != null) {
						ret.addInfos(addBigHouseInf(bh).build());
					}
					ret.setCode(code);
					ProtobufMsg pm = new ProtobufMsg();
					pm.id = PD.S_LM_UPHOUSE_INFO;
					pm.builder = ret;
					su.session.write(pm);
				}
			}
		}
	}

	/**
	 * @Description: 房屋交易、房屋状态改变,更新房屋信息
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void updateHouseInfo(int lmId, int code, HouseBean hb1,
			HouseBean hb2, BigHouse bh1, BigHouse bh2) {
		List<AlliancePlayer> memberList = HibernateUtil.list(
				AlliancePlayer.class, "where lianMengId=" + lmId);
		for (AlliancePlayer member : memberList) {
			SessionUser su = SessionManager.inst
					.findByJunZhuId(member.junzhuId);
			if (su != null) {
				HouseUpdateInfo.Builder ret = HouseUpdateInfo.newBuilder();
				if (hb1 != null) {
					ret.addInfos(addHouseBeanInf(hb1).build());
				}
				if (hb2 != null) {
					ret.addInfos(addHouseBeanInf(hb2).build());
				}
				if (bh1 != null) {
					ret.addInfos(addBigHouseInf(bh1).build());
				}
				if (bh2 != null) {
					ret.addInfos(addBigHouseInf(bh2).build());
				}
				ret.setCode(code);
				ProtobufMsg pm = new ProtobufMsg();
				pm.id = PD.S_LM_UPHOUSE_INFO;
				pm.builder = ret;
				su.session.write(pm);
			}
		}
	}

	/**
	 * @Description: 大房屋重新分配或衰减价值时更新房屋信息 （大房子数目不变）
	 * @param lmId
	 * @param code
	 */
	public void updateBigHouseInfo(int lmId, int code) {
		List<BigHouse> bHList = HibernateUtil.list(BigHouse.class,
				"where lmId=" + lmId);
		if (bHList.size() > 0) {
			passBigHouseInfo(lmId, bHList, code);
		}
	}

	/**
	 * @Description: 大房屋重新分配或衰减价值时更新房屋信息 （大房子数目增加）
	 * @param lmId
	 * @param code
	 */
	public void updateBigHouseInfo2(int lmId, List<BigHouse> bHList, int code) {
		passBigHouseInfo(lmId, bHList, code);
	}

	/**
	 * @Description: 推送房屋信息
	 * @param lmId
	 * @param bHList
	 * @param code
	 */
	public void passBigHouseInfo(int lmId, List<BigHouse> bHList, int code) {
		List<AlliancePlayer> memberList = BigSwitch.inst.allianceMgr
				.getAllianceMembers(lmId);
		HouseUpdateInfo.Builder ret = HouseUpdateInfo.newBuilder();
		// 高级房子列表
		for (BigHouse bigBean : bHList) {
			HouseSimpleInfo.Builder bigsf = HouseSimpleInfo.newBuilder();
			bigsf.setLocationId(bigBean.location);
			bigsf.setJzId(bigBean.jzId);
			if (bigBean.jzId > 0) {
				bigsf.setJzName(HibernateUtil.find(JunZhu.class, bigBean.jzId).name);
			} else {
				bigsf.setJzName("");
			}
			bigsf.setFirstOwner(bigBean.firstOwner);
			bigsf.setFirstHoldTime(DateUtils.date2Text(bigBean.firstHoldTime,
					"yyyy年mm月dd日"));
			bigsf.setState(bigBean.state);
			bigsf.setOpen4My(true);// 大房子一直可进
			bigsf.setHworth(bigBean.hworth);
			ret.addInfos(bigsf.build());
		}
		ret.setCode(code);
		for (AlliancePlayer member : memberList) {
			SessionUser su = SessionManager.inst
					.findByJunZhuId(member.junzhuId);
			if (su != null) {
				ProtobufMsg pm = new ProtobufMsg();
				pm.id = PD.S_LM_UPHOUSE_INFO;
				pm.builder = ret;
				su.session.write(pm);
			}
		}
	}

	private HouseSimpleInfo.Builder addHouseBeanInf(HouseBean hb) {
		HouseSimpleInfo.Builder sf = HouseSimpleInfo.newBuilder();
		sf.setLocationId(hb.location);
		sf.setJzId(hb.jzId);
		if (hb.jzId > 0) {
			sf.setJzName(HibernateUtil.find(JunZhu.class, hb.jzId).name);
			// ???表示为老号需退出联盟重新加入 ???会自动赋值。
			sf.setFirstOwner(hb.firstOwner == null ? "???" : hb.firstOwner);
			String firstHoldTime = hb.firstHoldTime == null ? "???" : DateUtils
					.date2Text(hb.firstHoldTime, "yyyy年MM月dd日");
			sf.setFirstHoldTime(firstHoldTime);
			sf.setState(hb.state);
			sf.setOpen4My(hb.open);
		} else {
			sf.setJzName("");
			sf.setState(1);
			sf.setOpen4My(false);
		}
		return sf;
	}

	private HouseSimpleInfo.Builder addBigHouseInf(BigHouse bh) {
		HouseSimpleInfo.Builder sf = HouseSimpleInfo.newBuilder();
		sf.setLocationId(bh.location);
		sf.setJzId(bh.jzId);
		sf.setJzName(HibernateUtil.find(JunZhu.class, bh.jzId).name);
		// ???表示为老号需退出联盟重新加入 ???会自动赋值。
		sf.setFirstOwner(bh.firstOwner == null ? "???" : bh.firstOwner);
		String firstHoldTime = bh.firstHoldTime == null ? "???" : DateUtils
				.date2Text(bh.firstHoldTime, "yyyy年MM月dd日");
		sf.setFirstHoldTime(firstHoldTime);
		sf.setState(bh.state);
		sf.setOpen4My(bh.open);
		return sf;
	}

	/**
	 * @Description: 撤销交换房屋的申请
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void cancelApply(int id, IoSession session, Builder builder) {
		Long curJzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (curJzId == null) {
			log.error("未找到撤销交换房屋的申请的君主id信息");
			return;
		}
		HouseApplyBean app = HibernateUtil.find(HouseApplyBean.class, curJzId);
		if (app == null) {
			log.info("请求不存在 {}", curJzId);
			// 请求不存在
			sendApplyResult(session, 900, PD.S_CANCEL_EXCHANGE);
			return;
		}
		HibernateUtil.delete(app);
		log.info("{}撤销对于{}房屋的申请", app.buyerId, app.keeperId);
		// "取消申请成功"
		sendApplyResult(session, 800, PD.S_CANCEL_EXCHANGE);
	}

	public void answerEx(int id, IoSession session, Builder builder) {
		Long curJzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (curJzId == null) {
			log.error("未找到同意交换房屋的申请的君主id信息");
			return;
		}
		AnswerExchange.Builder req = (qxmobile.protobuf.House.AnswerExchange.Builder) builder;
		int code = req.getCode();// //10同意；20拒绝；30无视；
		long buyerId = req.getJzId();
		answerEx(curJzId, buyerId, code);
	}

	// 10同意；20拒绝；30无视； 请勿直接调用这个方法，因为它不是线程安全的。
	/**
	 * @Description: 房主同意交换房屋
	 * @param curJzId
	 * @param buyerId
	 * @param code
	 */
	public void answerEx(long curJzId, long buyerId, int code) {
		sentIsComplete.remove();
		sentIsComplete.set(false);
		HouseApplyBean app = HibernateUtil.find(HouseApplyBean.class, buyerId);
		if (app == null) {
			log.info("请求不存在 {}", buyerId);
			return;
		}
		if (app.keeperId != curJzId) {
			log.info("该请求的房主 {} 和当前批复人 {} 不一致", app.keeperId, curJzId);
			return;
		}
		HouseBean targetHb = HibernateUtil.find(HouseBean.class, curJzId);
		if (targetHb == null) {
			log.warn("批复者 {} 房屋信息未找到", curJzId);
			return;
		}
		if (targetHb.lmId <= 0) {
			log.warn("批复者 {} 的房屋信息已不在联盟中", curJzId);
			return;
		}
		JunZhu curJz = HibernateUtil.find(JunZhu.class, curJzId);
		JunZhu buyer = HibernateUtil.find(JunZhu.class, buyerId);
		if (curJz == null || buyer == null)
			return;
		if (code == 20) {// 拒绝
			HibernateUtil.delete(app);
			log.info("{}拒绝了{} 的房屋申请", curJzId, buyerId);
			// 玩家xxx已经拒绝了您的房屋交换请求，我们再去寻找其他房源吧！
			Mail cfg = EmailMgr.INSTANCE.getMailConfig(10016);
			String content = cfg.content.replace("xxx", curJz.name);
			String fuJian = "";
			boolean ok = EmailMgr.INSTANCE.sendMail(buyer.name, content,
					fuJian, cfg.sender, cfg, "");
			log.info("发送拒绝邮件给{}成功? {}", buyer.name, ok);
			return;
		} else if (code == 30) {// 无视
			HibernateUtil.delete(app);
			log.info("{}无视了{} 的房屋申请", curJzId, buyerId);
			return;
		} else if (code != 10) {// 不是同意
			log.info("不可识别的操作码{}", code);
			return;
		}
		// 同意。
		HouseBean buyerHb = HibernateUtil.find(HouseBean.class, buyerId);
		if (buyerHb == null || buyerHb.lmId <= 0
				|| buyerHb.lmId != targetHb.lmId) {
			log.error("申请者的房屋信息有误{}", buyerId);
			return;
		}
		List<HouseApplyBean> list = HibernateUtil.list(HouseApplyBean.class,
				"where keeperId =" + curJzId);
		for (HouseApplyBean b : list) {
			delHouseApplyAndEmail(b);
		}
		int keeperLoc = targetHb.location;
		int buyerLoc = buyerHb.location;
		// 交换房屋信息
		targetHb.location = buyerLoc;
		buyerHb.location = keeperLoc;
		// 状态设置为自住
		targetHb.state = HouseBean.ForUse;
		buyerHb.state = HouseBean.ForUse;
		HibernateUtil.save(targetHb);
		HibernateUtil.save(buyerHb);
		log.info("完成{}与{}的房屋交换，原位置{}-{}", curJzId, buyerId, keeperLoc, buyerLoc);
		// 扣除购买者的换房卡
		Bag<BagGrid> bag = BagMgr.inst.loadBag(buyerId);
		int cnt = BagMgr.inst.getItemCount(bag, huanFangKa);
		if (cnt <= 0) {
			log.error("买家{}缺少换房卡,完成与{}的换房交易", buyerId, curJzId);
		}

		BagMgr.inst.removeItem(bag, huanFangKa, 1, "换房", buyer.level);
		// 删除物品后推送背包信息给玩家
		sendBagAgain(bag.ownerId);
		// ======================================
		{// 给买家发邮件
			// 您的房屋交换申请已经被玩家xxx接受，恭喜主人获得新房屋，我们快去看一看新家吧！
			Mail cfg = EmailMgr.INSTANCE.getMailConfig(10017);
			String content = cfg.content.replace("xxx", curJz.name);
			String fuJian = "";
			boolean ok = EmailMgr.INSTANCE.sendMail(buyer.name, content,
					fuJian, cfg.sender, cfg, "");
			log.info("发送恭喜换房邮件给{}成功? {}", buyer.name, ok);
		}
		{// 给卖家发邮件
			// 恭喜主人与玩家xxx成功交换房屋，您的房屋已经由aaa变为bbb。
			Mail cfg = EmailMgr.INSTANCE.getMailConfig(10018);
			String content = cfg.content.replace("xxx", buyer.name);
			content = content.replace("aaa", getFWName(targetHb.location, 101));
			content = content.replace("bbb", getFWName(buyerHb.location, 101));
			String fuJian = "30:" + huFu + ":1";
			boolean ok = EmailMgr.INSTANCE.sendMail(buyer.name, content,
					fuJian, cfg.sender, cfg, "");
			log.info("发送同意换房邮件给{}成功? {}", buyer.name, ok);
		}

		// 更新房屋信息
		updateHouseInfo(buyerHb.lmId, 300, buyerHb, targetHb, null, null);
		log.info("普通小房屋交易成功，更新房屋信息给联盟{}的所有成员", buyerHb.lmId);

		sentIsComplete.remove();
		sentIsComplete.set(true);
	}

	// 10同意；20拒绝；30无视； 请勿直接调用这个方法，因为它不是线程安全的。
	/**
	 * @Description: 盟主同意交换房屋
	 * @param curJzId
	 * @param targetId
	 * @param buyerId
	 * @param code
	 */
	public void leaderAnswerEx(long curJzId, long targetId, long buyerId,
			int code) {
		HouseApplyBean app = HibernateUtil.find(HouseApplyBean.class, buyerId);
		if (app == null) {
			log.info("请求不存在 {}", buyerId);
			return;
		}
		AlliancePlayer aPlayer = HibernateUtil.find(AlliancePlayer.class,
				buyerId);
		if (aPlayer.title != AllianceMgr.TITLE_LEADER) {
			log.info("当前批复人 {} 不是盟主", curJzId);
			return;
		}
		HouseBean targetHb = HibernateUtil.find(HouseBean.class, targetId);
		if (targetHb == null) {
			log.warn("卖家 {} 房屋信息未找到", targetId);
			return;
		}
		if (targetHb.lmId <= 0) {
			log.warn("卖家 {} 的房屋信息已不在联盟中", targetId);
			return;
		}
		JunZhu targetJz = HibernateUtil.find(JunZhu.class, targetId);
		JunZhu buyer = HibernateUtil.find(JunZhu.class, buyerId);
		if (targetJz == null || buyer == null)
			return;
		if (code == 20) {// 拒绝
			HibernateUtil.delete(app);
			log.info("{}拒绝了{} 的房屋申请", curJzId, buyerId);
			// 您与xxx的换房申请被盟主大人拒绝了，我们再去看看其他的好房源吧！
			Mail cfg = EmailMgr.INSTANCE.getMailConfig(10005);
			String content = cfg.content.replace("xxx", targetJz.name);
			String fuJian = "";
			boolean ok = EmailMgr.INSTANCE.sendMail(buyer.name, content,
					fuJian, cfg.sender, cfg, "");
			log.info("发送拒绝邮件给{}成功? {}", buyer.name, ok);
			return;
		} else if (code == 30) {// 无视
			HibernateUtil.delete(app);
			log.info("{}无视了{} 的房屋申请", curJzId, buyerId);
			return;
		} else if (code != 10) {// 不是同意
			log.info("不可识别的操作码{}", code);
			return;
		}
		// 同意。
		HouseBean buyerHb = HibernateUtil.find(HouseBean.class, buyerId);
		if (buyerHb == null || buyerHb.lmId <= 0
				|| buyerHb.lmId != targetHb.lmId) {
			log.error("申请者的房屋信息有误{}", buyerId);
			return;
		}
		List<HouseApplyBean> list = HibernateUtil.list(HouseApplyBean.class,
				"where keeperId =" + targetId);
		for (HouseApplyBean b : list) {
			delHouseApplyAndEmail(b);
		}
		int targetLoc = targetHb.location;
		int buyerLoc = buyerHb.location;
		// 交换房屋信息
		targetHb.location = buyerLoc;
		buyerHb.location = targetLoc;
		// 状态设置为自住
		targetHb.state = HouseBean.ForUse;
		buyerHb.state = HouseBean.ForUse;
		HibernateUtil.save(targetHb);
		HibernateUtil.save(buyerHb);
		log.info("完成{}与{}的房屋交换，原位置{}-{}", targetId, buyerId, targetLoc,
				buyerLoc);
		// 扣除购买者的换房卡
		Bag<BagGrid> bag = BagMgr.inst.loadBag(buyerId);
		BagMgr.inst.removeItem(bag, huanFangKa, 1, "换房", buyer.level);
		// 删除物品后推送背包信息给玩家
		sendBagAgain(bag.ownerId);
		// ======================================
		{// 给买家发邮件
			// 恭喜主人获得盟主大人的批准，成功与xxx交换了房屋，我们快去新家看看吧！
			Mail cfg = EmailMgr.INSTANCE.getMailConfig(10006);
			String content = cfg.content.replace("xxx", targetJz.name);
			String fuJian = "";
			boolean ok = EmailMgr.INSTANCE.sendMail(buyer.name, content,
					fuJian, cfg.sender, cfg, "");
			log.info("发送恭喜换房邮件给{}成功? {}", buyer.name, ok);
		}
		{// 给被卖房卖家发邮件
			// 告诉您一个不幸的消息。您的房屋aaa已经被盟主大人强行交换给了xxx，
			// 您获得了一枚虎符作为补偿。
			Mail cfg = EmailMgr.INSTANCE.getMailConfig(10007);
			String content = cfg.content.replace("aaa",
					getFWName(targetHb.location, 101));
			content = content.replace("xxx", buyer.name);
			String fuJian = "30:" + huFu + ":1";
			boolean ok = EmailMgr.INSTANCE.sendMail(targetJz.name, content,
					fuJian, cfg.sender, cfg, "");
			log.info("发送同意换房邮件给{}成功? {}", targetJz.name, ok);
		}
		// 更新房屋信息
		updateHouseInfo(buyerHb.lmId, 300, buyerHb, targetHb, null, null);
		log.info("盟主介入小房屋交易成功，更新房屋信息给联盟{}的所有成员", buyerHb.lmId);
	}

	/**
	 * @Description: 删除房屋交易请求以及相关邮件
	 * @param applyBean
	 */
	public void delHouseApplyAndEmail(HouseApplyBean applyBean) {
		HibernateUtil.delete(applyBean);
		log.info("取消{}对于房屋{}的申请", applyBean.buyerId, applyBean.keeperId);

		Email email = HibernateUtil.find(Email.class, applyBean.emailId);
		if (email == null) {
			log.error("要删除的邮件不存在，emailId:{}", applyBean.emailId);
			return;
		}
		HibernateUtil.delete(email);
		log.info("删除交易申请邮件，emailId:{}", applyBean.emailId);

	}

	/**
	 * @Description: 离开联盟完成房屋交易
	 * @param curJz
	 * @param curHb
	 */
	public void leaveLMEndHouseDeal(JunZhu curJz, HouseBean curHb) {
		log.info("离开联盟完成{}房屋交易", curJz.id);
		List<HouseApplyBean> list = HibernateUtil.list(HouseApplyBean.class,
				"where keeperId =" + curJz.id);
		int applySize = list.size();
		if (applySize == 0) {
			log.info("没有对于{}的房屋的交易申请", curJz.id);
			return;
		}
		boolean isFish = false;
		for (int i = 0; i < applySize; i++) {
			HouseApplyBean applyBean = list.get(i);
			HouseBean buyerHb = HibernateUtil.find(HouseBean.class,
					applyBean.buyerId);
			if (buyerHb.lmId == curHb.lmId && !isFish) {
				// 判断买家是否拥有换房卡
				Bag<BagGrid> bag = BagMgr.inst.loadBag(applyBean.buyerId);
				int cnt = BagMgr.inst.getItemCount(bag, huanFangKa);
				if (cnt <= 0) {
					continue;
				}
				// 完成交易
				int targetLoc = curHb.location;
				int buyerLoc = buyerHb.location;
				curHb.location = buyerLoc;
				buyerHb.location = targetLoc;

				buyerHb.state = HouseBean.ForUse;

				HibernateUtil.save(curHb);
				HibernateUtil.save(buyerHb);
				// 给买家发邮件，扣除换房卡
				JunZhu buyer = HibernateUtil.find(JunZhu.class, buyerHb.jzId);
				reduceBagSendEmail(bag, buyer.name, targetLoc, buyer);
				// 更新房屋信息
				updateHouseInfo(buyerHb.lmId, 300, buyerHb, null, null, null);
				log.info("离开联盟完成房屋交易，更新房屋信息给联盟{}的所有成员", buyerHb.lmId);
				isFish = true;
			}
			delHouseApplyAndEmail(applyBean);
		}
	}

	/**
	 * @Description:修正房屋名称
	 * @param loc
	 * @param type
	 *            101时表示小房子
	 * @return
	 */
	public String getFWName(int loc, int type) {
		// 修正房屋名称
		if (type == 101) {
			loc = 100 + loc;
		}
		List<?> list = TempletService.listAll(FangWuInformation.class
				.getSimpleName());
		if (list == null)
			return "fw:" + loc;
		int cnt = list.size();
		for (int i = 0; i < cnt; i++) {
			FangWuInformation f = (FangWuInformation) list.get(i);
			if (f.id == loc) {
				return f.name;
			}
		}
		return "FW#" + loc;
	}

	public void gettApplyList(IoSession session) {
		Long curJzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (curJzId == null) {
			log.error("未找到请求交换房屋的申请列表的君主id信息");
			return;
		}
		List<HouseApplyBean> list = HibernateUtil.list(HouseApplyBean.class,
				"where keeperId =" + curJzId);
		ApplyInfos.Builder ret = ApplyInfos.newBuilder();
		Apply.Builder a = Apply.newBuilder();
		for (HouseApplyBean b : list) {
			a.setJzId(b.buyerId);
			JunZhu jj = HibernateUtil.find(JunZhu.class, b.buyerId);
			a.setName(jj.name);
			a.setDate(b.dt.toLocaleString());
			ret.addList(a.build());
		}
		session.write(ret.build());
		log.info("给{}发送申请列表个数{}", curJzId, list.size());
	}

	public void addMission(int id, IoSession session, Builder builder) {
		Mission m = new Mission(id, session, builder);
		missions.add(m);
	}

	public void answerApply(IoSession session, JunZhu junZhu, Email email,
			int operCode) {
		// "buyerId:"+app.buyerId
		String param = email.param;
		if (param == null || param.startsWith("buyerId:") == false) {
			log.error("answerApply 邮件参数不对{},{}", email.getId(), param);
			return;
		}
		param = param.split(":")[1];
		answerEx(junZhu.id, Long.parseLong(param), operCode * 10);
	}

	public void leaderAnswerApply(IoSession session, JunZhu junZhu,
			Email email, int operCode) {
		String param = email.param;
		if (param == null || param.startsWith("buyerId:") == false) {
			log.error("answerQiangShouApply 邮件参数不对{},{}", email.getId(), param);
			return;
		}
		String buyerId = param.split(",")[0].split(":")[1];
		String keeperId = param.split(",")[1].split(":")[1];
		leaderAnswerEx(junZhu.id, Long.parseLong(keeperId),
				Long.parseLong(buyerId), operCode * 10);
	}

	public void fenPeiBigHouse(int lmId) {
		Session s = HibernateUtil.getSessionFactory().getCurrentSession();
		Transaction tr = s.beginTransaction();
		Criteria c = s.createCriteria(AlliancePlayer.class);
		c.add(Restrictions.eq("lianMengId", lmId));
		c.addOrder(Order.desc("gongXian"));
		c.setFirstResult(0);
		c.setMaxResults(5);
		List<?> gxList = null;
		try {
			gxList = c.list();
			tr.commit();
		} catch (Exception e) {
			tr.rollback();
			log.error("查询出错{}", e);
			return;
		}
		Set<Integer> bhLocaSet = new HashSet<Integer>();
		// 删除之前的大屋子数据。
		List<BigHouse> preHList = HibernateUtil.list(BigHouse.class,
				"where lmId=" + lmId);
		Map<Integer, String> firstOwnerMap = new HashMap<Integer, String>();
		Map<Integer, Date> firstHoldTimeMap = new HashMap<Integer, Date>();
		for (BigHouse h : preHList) {
			bhLocaSet.add(h.location);
			firstOwnerMap.put(h.location, h.firstOwner);
			firstHoldTimeMap.put(h.location, h.firstHoldTime);
			HibernateUtil.delete(h);
		}
		// preHCount记录之前大房子数目
		int preHCount = preHList.size();

		int cnt = gxList.size();
		Date today = new Date();

		// 查询联盟信息
		AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, lmId);
		int lmLevel = alncBean.level;

		// 高级房子列表
		for (int i = 0; i < cnt; i++) {
			AlliancePlayer ap = (AlliancePlayer) gxList.get(i);
			BigHouse bh = new BigHouse();
			bh.jzId = ap.junzhuId;
			bh.gongXian = ap.gongXian;
			bh.lmId = lmId;
			bh.state = BigHouse.ForUse;// 有人住
			bh.location = i + 101;// 101,102,103,104,105 五个大房子。
			bh.open = true;
			// 获取当前高级房屋配置信息
			FangWu bigFWConf = getBHConfig(bh.location, lmLevel);

			// 默认房屋价值
			bh.hworth = bigFWConf.value;
			bh.previousHoldTime = today;
			// 第一任主人 第一次入住时间取建盟后第一次分配房子的时间和主人
			bh.firstOwner = firstOwnerMap.get(bh.location) == null ? HibernateUtil
					.find(JunZhu.class, ap.junzhuId).name : firstOwnerMap
					.get(bh.location);
			bh.firstHoldTime = firstHoldTimeMap.get(bh.location) == null ? today
					: firstHoldTimeMap.get(bh.location);
			HibernateUtil.save(bh);
			log.info("联盟{}大房{}给予{}", lmId, i + 100, ap.junzhuId);
			// 设置小房子为关闭
			HouseBean hb = HibernateUtil.find(HouseBean.class, ap.junzhuId);
			if (hb == null) {
				log.error("未找到{}的初级房屋，设置初级房屋关门异常", ap.junzhuId);
				return;
			}
			hb.open = false;
			HibernateUtil.save(hb);
		}
		if (preHCount != cnt) {
			// 重新分配大房子，大房子数目增加，更新房屋信息
			List<BigHouse> bhNowList = HibernateUtil.list(BigHouse.class,
					"where  lmId=" + lmId);
			List<BigHouse> bhUpList = new ArrayList<BigHouse>();
			List<BigHouse> bhAddList = new ArrayList<BigHouse>();
			for (BigHouse h : bhNowList) {
				if (bhLocaSet.contains(h.location)) {
					bhUpList.add(h);
				} else {
					bhAddList.add(h);
				}
			}
			if (bhAddList.size() > 0) {
				// 推送增加的大房子信息
				updateBigHouseInfo2(lmId, bhAddList, 100);
			}
			// 推送更新的大房子信息
			if (bhUpList.size() > 0) {
				updateBigHouseInfo2(lmId, bhUpList, 300);
			}
		} else {
			// 重新分配大房子，更新房屋信息
			updateBigHouseInfo(lmId, 300);
		}
		log.info("重新分配大房子，更新房屋信息给联盟{}的所有成员", lmId);
	}

	/**
	 * @Description: 删除物品后推送背包信息给玩家
	 * @param jzId
	 */
	public  void sendBagAgain(long jzId) {
		SessionUser su = SessionManager.inst.findByJunZhuId(jzId);
		if (su != null) {
			log.info("从{}移除物品，推送背包信息给玩家", jzId);
			BagMgr.inst.sendBagInfo(0, su.session, null);
		}
	}
	/**
	 * @Description: 推送房屋经验可领
	 * @param jzId
	 */
	public void isCanLingqufangwuExp(JunZhu jz,IoSession session) {
		if(isCanLingqufangwuExp(jz)){
			FunctionID.pushCanShowRed(jz.id, session, FunctionID.LianMengHouse);
		}
	}
	/**
	 * @Description: 判断房屋经验是否可领
	 * @param jzId
	 */
	public boolean isCanLingqufangwuExp(JunZhu jz) {
		long jzId=jz.id;
		AlliancePlayer ap = HibernateUtil.find(AlliancePlayer.class, jzId);
		if (ap == null) {
			log.info("君主{}无联盟，不刷新房屋经验，ap == null", jzId);
			return false;
		}
		if (ap.lianMengId <= 0) {
			log.info("君主{}无联盟，不刷新房屋经验，ap.lianMengId <= 0", jzId);
			return false;
		}
		HouseBean hb = HibernateUtil.find(HouseBean.class, "where jzId=" + jzId);
		if(hb==null){ 
			log.info("君主{}没有房屋，无经验可以领取", jzId);
			return false;
		}
		if(hb.lmId<=0){
			log.info("君主{}离开联盟-没有房屋，无经验可以领取", jzId);
			return false;
		}
		BigHouse bh = HibernateUtil.find(BigHouse.class, "where jzId=" + jzId);
		HouseExpInfo.Builder expInfo = HouseExpInfo.newBuilder();
		if (bh == null) {
			expInfo = makeHouseExpInfo(hb);
		} else {
			log.error("君主{}有大房屋，不符合1.0版本需求Exception", jzId);
			if(bh.lmId<=0){
				log.info("君主{}离开联盟-没有大房屋，无经验可以领取", jzId);
				return false;
			}
			expInfo = makeBigHouseExpInfo(jzId, bh, hb, 1);
		}
		int exp = expInfo.getCur();
		int expMax=expInfo.getMax();
		if(exp>=expMax){
			log.info("君主{}房屋经验已满，可以领取", jzId);
			return true;
		}
		return false;
	}
	// /**
	// * @Description: 删除所有邮件
	// * @param applyBean
	// */
	// private void delEmail(){
	// List<Email> email = HibernateUtil.list(Email.class, "");
	// for (Email email2 : email) {
	// HibernateUtil.delete(email2);
	// log.info("删除邮件，emailId:{}",email2.getId());
	// }
	//
	// }
}
