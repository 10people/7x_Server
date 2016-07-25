package com.qx.vip;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.ChongZhi;
import com.manu.dynasty.template.VIP;
import com.manu.dynasty.template.VIPGift;
import com.manu.dynasty.template.VipFuncOpen;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.PD;
import com.qx.activity.ShouchongInfo;
import com.qx.activity.ShouchongMgr;
import com.qx.award.AwardMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;
import com.qx.task.DailyTaskMgr;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

import qxmobile.protobuf.VIP.ChongTimes;
import qxmobile.protobuf.VIP.GetVipRewardReq;
import qxmobile.protobuf.VIP.GetVipRewardResp;
import qxmobile.protobuf.VIP.RechargeReq;
import qxmobile.protobuf.VIP.RechargeResp;
import qxmobile.protobuf.VIP.VipInfoResp;
import qxmobile.protobuf.ZhangHao.RegRet;

public class VipMgr {
	public static boolean allowFakeCharge = false;
	public static VipMgr INSTANCE;
	/** VIP配置文件信息，<vip等级，vip信息> **/
	public static Map<Integer, VIP> vipTemp;
	public static Map<Integer, ChongZhi> chongZhiTemp;
	public static Map<Integer, VipFuncOpen> vipFuncOpenTemp;
	public Map<Integer, VIPGift> vipGiftMap;
	public static Logger log = LoggerFactory.getLogger(VipMgr.class);

	public static ChongZhi yueka;
	public static int yuekaid = 1;
	public static int zhoukaid = 2;
	public static int zhongShenKa = 199999;//不存在
	public static int maxVip = 1;
	/** 第一次购买额外赠送元宝类型 **/
	public static int TYPE_ADD_EXTRA_FIRST = 2;
	public static int buy_yueka_limit_days = 5;
	public static int tili_buling = 29; //补领体力

	public static ThreadLocal<VipRechargeRecord> yueKaRecord = new ThreadLocal<VipRechargeRecord>();

	public VipMgr() {
		INSTANCE = this;
		allowFakeCharge = GameServer.cfg.get("loginServer").contains("192.168.3.80");
		initData();
	}

	public void initData() {
		// 加载VIP配置文件信息
		@SuppressWarnings("unchecked")
		List<VIP> vipList = TempletService.listAll(VIP.class.getSimpleName());
		Map<Integer, VIP> temp = new HashMap<Integer, VIP>();
		for (VIP vip : vipList) {
			temp.put(vip.lv, vip);
			if (vip.lv > maxVip) {
				maxVip = vip.lv;
			}
		}
		vipTemp = temp;
		// 加载ChongZhi配置文件信息
		setChongZhiTemp();
		// 加载VipFuncOpen配置文件信息
		setVipFuncOpenTemp();
		
		List<VIPGift> vipGiftList = TempletService.listAll(VIPGift.class.getSimpleName());
		Map<Integer, VIPGift> vipGiftMap = new HashMap<>();
		for(VIPGift gift : vipGiftList) {
			vipGiftMap.put(gift.vip, gift);
		}
		this.vipGiftMap = vipGiftMap;
	}

	@SuppressWarnings("unchecked")
	public void setChongZhiTemp() {
		List<ChongZhi> list = TempletService.listAll(ChongZhi.class
				.getSimpleName());
		Map<Integer, ChongZhi> temp = new HashMap<Integer, ChongZhi>();
		for (ChongZhi cz : list) {
			temp.put(cz.id, cz);
			if (cz.type == yuekaid) {
				yueka = cz;
			}
		}
		chongZhiTemp = temp;
	}

	@SuppressWarnings("unchecked")
	public void setVipFuncOpenTemp() {
		List<VipFuncOpen> list = TempletService.listAll(VipFuncOpen.class
				.getSimpleName());
		Map<Integer, VipFuncOpen> temp = new HashMap<Integer, VipFuncOpen>();
		for (VipFuncOpen func : list) {
			temp.put(func.key, func);
		}
		vipFuncOpenTemp = temp;
	}

	public void sendError(IoSession session, int cmd, String string) {
		qxmobile.protobuf.ZhangHao.RegRet.Builder ret = RegRet.newBuilder();
		ret.setUid(cmd);
		ret.setName(string);
		session.write(ret.build());
	}

	/**
	 * 获取vip信息
	 */
	public void getVipInfo(int cmd, IoSession session, Builder builder) {
		long time = System.currentTimeMillis();
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("玩家不存在");
			return;
		}
		VipInfoResp.Builder resp = VipInfoResp.newBuilder();
		
		PlayerVipInfo playerVipInfo = getPlayerVipInfo(jz.id);
		int vipLevel = playerVipInfo.level;
		int needVipExp = 0;
		if (vipLevel == maxVip) {
			resp.setIsMax(true);
		} else {
			VIP v = vipTemp.get(vipLevel + 1);
			if (v == null) {
				log.error("根据lv:{},获取VIP配置数据出错:", vipLevel + 1);
			}
			needVipExp = v == null ? -1 : v.needNum;
			resp.setIsMax(false);
		}
		resp.setVipLevel(vipLevel);
		resp.setNeedYb(needVipExp);
		resp.setHasYb(playerVipInfo.vipExp);
		resp.setYueKaLeftDays(playerVipInfo.yueKaRemianDay);
		resp.setZhouKaLeftDays(playerVipInfo.zhouKaRemianDay);
		
		fillVIPinfo(resp, jz.id);
		List<VipGiftbag> giftbagList = HibernateUtil.list(VipGiftbag.class, " where junzhuId="+jz.id);
		for(VipGiftbag giftbag : giftbagList) {
			resp.addGetRewardVipList(giftbag.vipLevel);
		}
		log.info("获取玩家充值数据时间：{}", System.currentTimeMillis() - time);
		session.write(resp.build());
	}

	public void fillVIPinfo(VipInfoResp.Builder resp, long jzId) {
		ChongTimes.Builder chongInfo = null;
		String sql = "select type, count(1) as cnt from VipRechargeRecord where accId =" + jzId + " group by type";
		List<Map<String, Object>> list = (List<Map<String, Object>>) HibernateUtil.querySql(sql,Transformers.ALIAS_TO_ENTITY_MAP);
		Map<Integer, Integer> cntMap = new HashMap<Integer, Integer>(list.size());
		for(Map<String, Object> db: list){
			cntMap.put((Integer)db.get("type"), ((BigInteger)db.get("cnt")).intValue());
		}
		for (Integer id : chongZhiTemp.keySet()) {
			chongInfo = ChongTimes.newBuilder();
			chongInfo.setId(id);
			Integer cnt = cntMap.get(id);
			chongInfo.setTimes(cnt == null ? 0 : cnt);
			resp.addInfos(chongInfo);
		}
	}

	public void recharge(int cmd, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			log.error("玩家不存在");
			return;
		}
		RechargeReq.Builder req = (RechargeReq.Builder) builder;
		int chongZhiId = req.getType();
		log.info("收到的君主id是:{}, 本次的充值id是:{}", jz.id, chongZhiId);
		
		ChongZhi data = chongZhiTemp.get(chongZhiId);
		if (data == null) {
			log.error("ChongZhi配置中未找到相关数据条目:{}", chongZhiId);
			return;
		}
		if (data.addNum <= 0) {
			log.error("充值金额不能为0或者小于0,{},{}",jz.id,chongZhiId);
			return;
		}
		recharge(session, jz, data,true);
	}

	public void recharge(IoSession session, JunZhu jz, ChongZhi data, boolean doAdd) {
		RechargeResp.Builder resp = RechargeResp.newBuilder();
		int remainTotalDays = 0;
		PlayerVipInfo playerVipInfo = getPlayerVipInfo(jz.id);
				
		int addYB = data.addNum;
		Date date = new Date();
		String reason = "??";
		//各种卡是扣钱去买，其他则是充值，配合真正的充值调用。首冲和赠送在真实的充值中处理，这里不计算
		int type = 0;
		if (data.id == yuekaid) {
			remainTotalDays = CanShu.YUEKA_TIME + playerVipInfo.yueKaRemianDay;
			playerVipInfo.yueKaRemianDay += CanShu.YUEKA_TIME;
			if(playerVipInfo.lastUpdateYuekaTime == null) {
				playerVipInfo.lastUpdateYuekaTime = date;
			}
			reason = "购买月卡";
			type = YBType.YB_BUY_YUEKA; 
		} else if(data.id == zhongShenKa){
			if(playerVipInfo.haveZhongShenKa == 1) {
				resp.setIsSuccess(false);
				resp.setSumAoumnt(0);
				resp.setVipLevel(playerVipInfo.level);
				session.write(resp.build());
				log.error("充值失败，君主:"+jz.id+"已经购买过终身卡了");
				return;
			}
			playerVipInfo.haveZhongShenKa = 1;
			reason = "购买终身卡";
			type = YBType.YB_BUY_ZHONGSHENKA;
		} else if(data.id == zhoukaid){
			remainTotalDays = CanShu.ZHOUKA_TIME + playerVipInfo.zhouKaRemianDay;
			playerVipInfo.zhouKaRemianDay += CanShu.ZHOUKA_TIME;
			if(playerVipInfo.lastUpdateZhoukaTime == null) {
				playerVipInfo.lastUpdateZhoukaTime = date;
			}
			reason = "购买周卡";
			type = YBType.YB_BUY_ZHOUKA;
		}else{
			reason = "vip充值";
			type = YBType.YB_VIP_CHONGZHI;
		}
		if(doAdd){
			YuanBaoMgr.inst.diff(jz, addYB, data.addNum, 0, type, reason);
		}
		int vipExp = playerVipInfo.vipExp + data.addVipExp;
		log.info("玩家：{}，充值之前的vipExp：{}， 充值之后的vipExp：{}", jz.id, playerVipInfo.vipExp, vipExp);
		int vip = getVipLevel(playerVipInfo.level, vipExp);
		log.info("玩家：{}，充值之前的等级：{}， 充值之后的等级：{}", jz.id, jz.vipLevel, vip);
		jz.vipLevel = vip;
		HibernateUtil.update(jz);
		// 刷新首页玩家信息
		JunZhuMgr.inst.sendMainInfo(session,jz,false);

		/*
		 * 说明： PlayerVipInfo,VipRechargeRecord,JunZhu 三张表中的vipLeve的值都表示vip等级 ,
		 * 三者保持时时同步。
		 */
		int sumRmb = playerVipInfo.sumAmount + data.addNum;
		log.info("玩家：{}充值RMB，before：{}， after：{}", playerVipInfo.sumAmount, sumRmb);
		playerVipInfo.sumAmount = sumRmb;
		playerVipInfo.level = vip;
		playerVipInfo.vipExp = vipExp;
		HibernateUtil.save(playerVipInfo);

		VipRechargeRecord r = new VipRechargeRecord(jz.id, data.addNum, date,
				sumRmb, vip, data.id, addYB, remainTotalDays);
		HibernateUtil.save(r);
		log.info("玩家:{},充值人民币:{},成功，元宝数量:{}，现在玩家的元宝数:{}", jz.id, data.addNum,
				addYB, jz.yuanBao);
		// 充值成功，判断首冲 TODO int count = HibernateUtil.getColumnValueMaxOnWhere(BillHist.class, "save_amt", "where jzId="+junZhuId);>0
		ShouchongInfo info = HibernateUtil.find(ShouchongInfo.class, " where junzhuId=" + jz.id);
		if (ShouchongMgr.instance.getShouChongState(info) == 0) {// 未完成首冲
			ShouchongMgr.instance.finishShouchong(jz.id);
		}
		resp.setIsSuccess(true);
		resp.setSumAoumnt(addYB);
		resp.setVipLevel(vip);
		session.write(resp.build());
		// 每日任务列表中添加可以领取月卡奖励的条目
		if (data.id == yuekaid) {
			DailyTaskMgr.INSTANCE.taskListRequest(PD.C_DAILY_TASK_LIST_REQ, session);
		}
		if(data.id== yuekaid || data.id == zhoukaid){ //月卡周卡
			EventMgr.addEvent(jz.id,ED.ACTIVITY_MONTHCARD_REFRESH,session);//刷新月卡活动
		}
	}

	public PlayerVipInfo getPlayerVipInfo(long jzId) {
		PlayerVipInfo vipInfo = HibernateUtil.find(PlayerVipInfo.class, jzId);
		if (vipInfo == null) {
			vipInfo = new PlayerVipInfo();
			vipInfo.accId = jzId;
			vipInfo.sumAmount = 0;
			vipInfo.level = 0;
			vipInfo.vipExp = 0;
			Cache.playerVipInfoCaChe.put(vipInfo.accId, vipInfo);
			HibernateUtil.insert(vipInfo);
		} else { 
			// 更新月卡剩余天数
			if(DateUtils.isTimeToReset(vipInfo.lastUpdateYuekaTime, CanShu.REFRESHTIME)) {
				Date date = new Date();
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, CanShu.REFRESHTIME);
				int diffDays = DateUtils.daysBetween(vipInfo.lastUpdateYuekaTime, calendar.getTime());
				if(diffDays > 0 && vipInfo.yueKaRemianDay > 0) {
					vipInfo.yueKaRemianDay -= diffDays;
					vipInfo.yueKaRemianDay = vipInfo.yueKaRemianDay <= 0 ? 0 : vipInfo.yueKaRemianDay;
					vipInfo.lastUpdateYuekaTime = date;
					HibernateUtil.save(vipInfo);
					log.info("君主:{}的月卡在时间:{}扣除了{}天", jzId, date, diffDays);
				}
			}
			// 更新周卡信息
			if(DateUtils.isTimeToReset(vipInfo.lastUpdateZhoukaTime, CanShu.REFRESHTIME)) {
				Date date = new Date();
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, CanShu.REFRESHTIME);
				int diffDays = DateUtils.daysBetween(vipInfo.lastUpdateZhoukaTime, calendar.getTime());
				if(diffDays > 0 && vipInfo.zhouKaRemianDay > 0) {
					vipInfo.zhouKaRemianDay -= diffDays;
					vipInfo.zhouKaRemianDay = vipInfo.zhouKaRemianDay <= 0 ? 0 : vipInfo.zhouKaRemianDay;
					vipInfo.lastUpdateZhoukaTime = date;
					HibernateUtil.save(vipInfo);
					log.info("君主:{}的周卡在时间:{}扣除了{}天", jzId, date, diffDays);
				}
			}
		}
		return vipInfo;
	}

	public void addVipExp(JunZhu jz, int addExpValue){
		long jid = jz.id;
		
		PlayerVipInfo vipInfo = getPlayerVipInfo(jid);

		int vipExp = addExpValue + vipInfo.vipExp;
		log.info("玩家：{}，增加vip经验之前的vipExp：{}， 增加之后的vipExp：{}", jid, vipInfo.vipExp,
				vipExp);

		int vip = getVipLevel(vipInfo.level, vipExp);
		log.info("玩家：{}，增加vip经验之前的等级：{}， 增加vip经验之后的等级：{}", jid, jz.vipLevel, vip);

		/*
		 * 说明： PlayerVipInfo,JunZhu 三张表中的vipLeve的值都表示vip等级 ,
		 */
		vipInfo.level = vip;
		vipInfo.vipExp = vipExp;
		HibernateUtil.save(vipInfo);

		jz.vipLevel = vip;
		HibernateUtil.save(jz);
	}

	public int getVipLevel(int vipLevel, int vipExp) {
		if (maxVip == vipLevel) {
			log.info("已经是最高vip等级");
			return vipLevel;
		}
		int temp = vipLevel;
		int i = 0;
		while (i++ < 10) {
			VIP vold = vipTemp.get(temp);
			VIP vnew = vipTemp.get(temp + 1);
			if (vold == null || vnew == null) {
				log.error("根据lv:{}或者{},获取VIP配置数据出错:", temp, temp + 1);
				return temp;
			}
			if (vipExp < vnew.needNum && vipExp >= vold.needNum) {
				return temp;
			}
			temp++;
		}
		return vipLevel;
	}

	public VipRechargeRecord getLatestYuaKaRecord(long jid) {
		String where = " WHERE accId = " + jid + " AND type = " + yuekaid +" order by id desc";
		List<VipRechargeRecord> datas = HibernateUtil.list(
				VipRechargeRecord.class, where,0,1);
		if (datas == null || datas.size() == 0) {
			return null;
		}
		long time = 0;
		VipRechargeRecord record = datas.get(0);
		return record;
	}

	public int getBuyCount(int type, long jid) {
		String hql = "select count(record.type) from VipRechargeRecord  record  where record.type=" + type
    			+ " and record.accId =" + jid;
		int count = HibernateUtil.getCount(hql);
		return count;
	}

	/**
	 * 君主的vip等级是满足当前操作 例如：虔诚膜拜是否vip等级满足
	 * 
	 * @Title: isVipPermit
	 * @Description:
	 * @param key
	 *            : 例如: VipData.qianCheng_moBai
	 * @param junVipLevel
	 *            ： junzhu.vipLevel
	 * @return: true: vip等级够， false： vip等级不够
	 */
	public boolean isVipPermit(int key, int junVipLevel) {
		VipFuncOpen data = vipFuncOpenTemp.get(key);
		if (data == null) {
			log.error("key={}的VipFuncOpen配置数据不存在", key);
			return false;
		}
		int level = data.needlv;
		if (junVipLevel >= level) {
			return true;
		}
		return false;
	}
	
	public VipFuncOpen getVipFuncOpen(int type) {
		return vipFuncOpenTemp.get(type);
	}

	public VIP getVIPByVipLevel(int vipLevel) {
		VIP vip = vipTemp.get(vipLevel);
		if (vip == null) {
			log.error("vipLevel={}的VIP配置数据不存在", vipLevel);
		}
		return vip;
	}

	/**
	 * 根据玩家的vip等级，获取，不同vip等级下，某些操作所允许的最大次数或者数值 例如：购买铜币次数
	 * 
	 * @Title: getValueByVipLevel
	 * @Description:
	 * @param vipLevel
	 *            : junzhu.vipLevel
	 * @param typeInfo
	 *            : 例如：VipData.bugMoneyTime
	 * @return 当前玩家vip等级所对应操作的允许值
	 */
	public int getValueByVipLevel(int vipLevel, int typeInfo) {
		VIP vip = getVIPByVipLevel(vipLevel);
		if (vip == null) {
			return 0;
		}
		switch (typeInfo) {
		case 1:
			return vip.bugMoneyTime;
		case 2:
			return vip.bugTiliTime;
		case 3:
			return vip.bugBaizhanTime;
		case 4:
			return vip.yujueDuihuan;
		case 5:
			return vip.saodangFree;
		case 6:
			return vip.xilianLimit;
		case 7:
			return vip.legendPveRefresh;
		case 8:
			return vip.YBxilianLimit;
		case 9:
			return vip.dangpuRefreshLimit;
		case 11:
			return vip.FangWubuildNum;
		case 12:
			return vip.mibaoCountLimit;
		case 13:
			return vip.youxiaTimes;
		case 14:
			return vip.YunbiaoTimes;
		case 15:
			return vip.JiebiaoTimes;
		case 16:
			return vip.InviteAssistTimes;
		case 17:
			return vip.LveduoTimes;
		case 18:
			return vip.HuangyeTimes;
		case 19:
			return vip.resurgenceTimes;
		case VipData.buy_ybblood_times:
			return vip.BloodVialTimes;
		case VipData.buy_revive_times:
			return vip.resOnSiteTimes;
		case VipData.buy_jianShezhi_times:
			return vip.buyJianshezhi;
		case VipData.buy_Hufu_times:
			return vip.buyHufu;
		case VipData.chong_lou_exp_scale:
			return vip.chonglou_ExpScale;
		}
		return 0;
	}

	public double getDoubleValueByVipLevel(int vipLevel, int typeInfo) {
		VIP vip = getVIPByVipLevel(vipLevel);
		if (vip == null) {
			return 0;
		}
		switch (typeInfo) {
		case 10:
			return vip.baizhanPara;
		}
		return 0;
	}

	/**
	 * GM工具充值接口
	 * 
	 * @Title: gmAddRMB
	 * @Description:
	 * @param rmb
	 * @return
	 */
	public boolean gmAddRMB(int rmb, JunZhu jz) {
		if (jz == null)
			return false;
		long jid = jz.id;
		PlayerVipInfo vipInfo = getPlayerVipInfo(jid);
		int addYB = rmb * 10;
		if (addYB <= 0) {
			log.error("充值金额不能为0或者小于0");
			return false;
		}
		int nowYB = jz.yuanBao + addYB;
		YuanBaoMgr.inst
				.diff(jz, addYB, rmb, 0, YBType.YB_VIP_CHONGZHI, "vip充值");
		int vipExp = vipInfo.vipExp + addYB;
		log.info("GM:玩家：{}，充值之前的元宝：{}， 充值之后的元宝：{}", jid, jz.yuanBao, nowYB);
		log.info("GM:玩家：{}，充值之前的vipExp：{}， 充值之后的vipExp：{}", jid,
				vipInfo.vipExp, vipExp);

		int vip = getVipLevel(vipInfo.level, vipExp);
		log.info("GM:玩家：{}，充值之前的等级：{}， 充值之后的等级：{}", jid, jz.vipLevel, vip);
		jz.vipLevel = vip;
		HibernateUtil.save(jz);
		/*
		 * 说明： PlayerVipInfo,VipRechargeRecord,JunZhu 三张表中的vipLeve的值都表示vip等级 ,
		 * 三者保持时时同步。
		 */
		int sumRmb = vipInfo.sumAmount + rmb;
		log.info("GM:玩家：{}充值RMB，before：{}， after：{}", jid, vipInfo.sumAmount,
				sumRmb);
		vipInfo.sumAmount = sumRmb;
		vipInfo.level = vip;
		vipInfo.vipExp = vipExp;
		HibernateUtil.save(vipInfo);

		// GM工具充钱是-1
		VipRechargeRecord r = new VipRechargeRecord(jid, rmb, new Date(),
				sumRmb, vip, -1, addYB, 0);
		HibernateUtil.save(r);
		log.info("GM:玩家:{},充值人民币:{},成功，一次性增加了元宝:{}，现在玩家的元宝数:{}", jid, rmb,
				addYB, jz.yuanBao);
		return true;
	}

	public boolean hasYueKaAward(long jid) {
		VipRechargeRecord r = getLatestYuaKaRecord(jid);
		if (r == null) {
			return false;
		}
		int leftDay = r.yueKaValid - DateUtils.daysBetween(r.time, new Date());
		if (leftDay > 0) {
			return true;
		}
		return false;
	}

	public void getVipGiftbag(int cmd, IoSession session, Builder builder) {
		JunZhu junzhu = JunZhuMgr.inst.getJunZhu(session);
		if(junzhu == null) {
			log.error("领取vip礼包失败，找不到君主");
			return;
		}
		GetVipRewardReq.Builder request = (qxmobile.protobuf.VIP.GetVipRewardReq.Builder) builder;
		int vipLevel = request.getVipLevel();
		
		GetVipRewardResp.Builder response = GetVipRewardResp.newBuilder();
		PlayerVipInfo vipInfo = getPlayerVipInfo(junzhu.id);
		if(vipInfo.level < vipLevel) {
			log.error("领取vip礼包失败，君主{}的vip等级:{},要领取的vip等级是:{}", junzhu.id, vipInfo.level, vipLevel);
			response.setResult(2);
			session.write(response.build());
			return;
		}
		
		VipGiftbag giftbag = HibernateUtil.find(VipGiftbag.class, 
				" where junzhuId="+junzhu.id + " and vipLevel=" + vipLevel);
		if(giftbag != null) {
			log.error("领取vip礼包失败，君主{}的vip等级{}的礼包已经领取", junzhu.id, vipLevel);
			response.setResult(1);
			session.write(response.build());
			return;
		}
		
		VIPGift vipGift = vipGiftMap.get(vipLevel);
		if(vipGift == null) {
			log.error("领取vip礼包失败，找不到vip等级:{}的配置", vipLevel);
			return;
		}
		
		List<AwardTemp> awardList = AwardMgr.inst.parseAwardConf(vipGift.award, "#", ":");
		for(AwardTemp at : awardList) {
			AwardMgr.inst.giveReward(session, at, junzhu);
		}
		
		giftbag = new VipGiftbag();
		giftbag.junzhuId = junzhu.id;
		giftbag.vipLevel = vipLevel;
		HibernateUtil.insert(giftbag);
		response.setResult(0);
		session.write(response.build());
	}
}
