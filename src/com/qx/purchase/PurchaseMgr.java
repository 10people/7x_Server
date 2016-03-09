package com.qx.purchase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.JunZhuProto.BuyTimesInfo;
import qxmobile.protobuf.Shop.BuyMibaoPointResp;
import qxmobile.protobuf.Shop.BuyTongbiDataResp;
import qxmobile.protobuf.Shop.LianXuBuyTongbiResp;
import qxmobile.protobuf.Shop.PurchaseFail;
import qxmobile.protobuf.Shop.TongbiResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.Jiangli;
import com.manu.dynasty.template.JunzhuShengji;
import com.manu.dynasty.template.Purchase;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.SessionAttKey;
import com.qx.award.AwardMgr;
import com.qx.equip.web.UserEquipAction;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.mibao.MibaoLevelPoint;
import com.qx.mibao.MibaoMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.persistent.MC;
import com.qx.task.DailyTaskCondition;
import com.qx.task.DailyTaskConstants;
import com.qx.vip.VipData;
import com.qx.vip.VipMgr;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

public class PurchaseMgr {

	public static final int TIAN_GONG_TU_ITMEID = 900005;

	public static final int Tong_Bi_CODE = 1;

	public static final int Ti_Li_CODE = 2;

	public static final int MIBAO_POINT_CODE = 16;

	public static final int YOUXIA_TIMES = 17;

	public static final int TREASURE_CODE_SMALL = 1;

	public static final int TREASURE_CODE_MIDDLE = 2;

	public static final int TREASURE_CODE_BIG = 3;

	public static Logger log = LoggerFactory.getLogger(PurchaseMgr.class);

	/** 购买物品所花元宝 **/
	public Map<Integer/* 购买类型 */, List<Purchase>> purchaseMap;
	public Map<Integer/* 购买次数 */, Purchase> tiLiMap;
	public Map<Integer/* 购买次数 */, Purchase> tongBiMap;
	public Map<Integer/* 购买次数 */, Purchase> mibaoPointMap;
	/** 奖励列表 **/
	public Map<Integer, Jiangli> jiangliMap;

	public static PurchaseMgr inst;

	public PurchaseMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		@SuppressWarnings("unchecked")
		List<Purchase> purchases = TempletService.listAll(Purchase.class
				.getSimpleName());
		Map<Integer, List<Purchase>> purchaseMap = new HashMap<Integer, List<Purchase>>();
		Map<Integer/* 购买次数 */, Purchase> tiLiMap = new HashMap<Integer, Purchase>();
		Map<Integer/* 购买次数 */, Purchase> tongBiMap = new HashMap<Integer, Purchase>();
		Map<Integer/* 购买次数 */, Purchase> mibaoPointMap = new HashMap<Integer, Purchase>();

		for (Purchase p : purchases) {
			int type = p.getType();
			if (type == Ti_Li_CODE) {
				tiLiMap.put(p.getTime(), p);
			} else if (type == Tong_Bi_CODE) {
				tongBiMap.put(p.getTime(), p);
			} else if (type == MIBAO_POINT_CODE) {
				mibaoPointMap.put(p.getTime(), p);
			}
			List<Purchase> list = purchaseMap.get(p.getType());
			if (list == null) {
				list = new ArrayList<Purchase>();
				purchaseMap.put(p.getType(), list);
			}
			list.add(p);
		}

		Map<Integer, Jiangli> jiangliMap = new HashMap<Integer, Jiangli>();
		List<Jiangli> jiangliList = TempletService.listAll(Jiangli.class.getSimpleName());
		for (Jiangli jiangli : jiangliList) {
			jiangliMap.put(jiangli.getId(), jiangli);
		}
		this.purchaseMap = purchaseMap;
		this.jiangliMap = jiangliMap;
		this.tongBiMap = tongBiMap;
		this.tiLiMap = tiLiMap;
		this.mibaoPointMap = mibaoPointMap;
	}

	public void buyTiLi(int code, IoSession session, Builder builder) {
		long junZhuId = getJunZhuIdBySession(session);
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		TiLi tiLi = HibernateUtil.find(TiLi.class, junZhuId);
		if (tiLi == null) {
			tiLi = new TiLi();
			tiLi.setDbId(junZhuId);
			tiLi.setDate(new Date(System.currentTimeMillis()));
			tiLi.setNum(0);
			// 添加缓存
			MC.add(tiLi, junZhuId);
			HibernateUtil.insert(tiLi);
		}

		Date lastDate = tiLi.getDate();
		Date curDate = new Date();
		// change 20150901
		if(DateUtils.isTimeToReset(lastDate, CanShu.REFRESHTIME_PURCHASE)){
			tiLi.setNum(0);
		}else{
			int maxBuy = VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel,
					VipData.bugTiliTime);
			if (tiLi.getNum() >= maxBuy) {
				error(session, code, "今天购买次数达到最大，不能再次购买体力");
				return;
			}
		}

		boolean ret = consume_Tili(tiLi.getNum(), session,  curDate,junZhu);
		if (ret) {
			tiLi.setDate(curDate);
			tiLi.setNum(tiLi.getNum() + 1);
			log.info("玩家{}第{}次购买体力成功", junZhuId, tiLi.getNum());
			HibernateUtil.save(tiLi);
			HibernateUtil.save(junZhu);
			JunZhuMgr.inst.sendMainInfo(session);
			// 主线任务：购买1次体力
			EventMgr.addEvent(ED.buy_tili_1_times, new Object[] { junZhu.id });
			// 每日任务中记录完成一次购买体力
			EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
					junZhuId, DailyTaskConstants.buy_tili, 1));
		} else {
			log.info("玩家{}第{}次购买体力失败", junZhuId, tiLi.getNum());
		}
		sendInfo(0, session, null);
	}
	
	/**
	 * @Description 单次购买铜币
	 * @param code
	 * @param session
	 * @param builder
	 */
	public void buyTongBi(int code, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			error(session, Tong_Bi_CODE, "君主信息异常");
		}
		long junZhuId = junZhu.id;
		log.info("玩家{} 单次购买铜币开始", junZhuId);
		TongBi tongBi = HibernateUtil.find(TongBi.class, junZhuId);
		Date today = Calendar.getInstance().getTime();
		if (tongBi == null) {
			tongBi = new TongBi();
			tongBi.setDbId(junZhuId);
			tongBi.setDate(today);
			tongBi.setNum(0);
			// 添加到缓存
			MC.add(tongBi, junZhuId);
			HibernateUtil.insert(tongBi);
		} else {
			Date lastDate = tongBi.getDate();
			// change 20150901
			if(DateUtils.isTimeToReset(lastDate, CanShu.REFRESHTIME_PURCHASE)){
				tongBi.setNum(0);
			}else{
				int maxBuy = VipMgr.INSTANCE.getValueByVipLevel(
						junZhu.vipLevel, VipData.bugMoneyTime);
				if (tongBi.getNum() >= maxBuy) {
					sendBuyTongbiResp(session, 1,null);
					log.error("今天购买铜币次数达到最大，vipLevel:{},已经购买次数:{}",
							junZhu.vipLevel, maxBuy);
					return;
				}
			}
		}
		Purchase p = getBuyConf(tongBi.getNum()+1, Tong_Bi_CODE);
		if (p == null) {
			log.info("玩家{}第{}次购买铜币失败，未找到配置", junZhuId, tongBi.getNum());
			error(session, Tong_Bi_CODE, "购买出现问题");
			return;
		}
		// 公式 from 数值策划
		// 第N次购买获得的铜币=(【君主等级系数】×【购买次数系数】×【暴击倍数】) / 100
		if (p.getYuanbao() > junZhu.yuanBao) {
			log.info("玩家{}第{}次购买铜币失败，元宝不足 ", junZhuId, tongBi.getNum());
			sendBuyTongbiResp(session, 2,null);
			return;
		}
		YuanBaoMgr.inst.diff(junZhu, -p.getYuanbao(), 0,getPriceConf(Tong_Bi_CODE), YBType.YB_BUY_WUPIN, "购买铜币");
		int baoJi = UserEquipAction.getInstance().getAddValue(3, 0);
		int xishu = 1;
		JunzhuShengji conf = JunZhuMgr.inst.getJunzhuShengjiByLevel(junZhu.level);
		if(conf != null){
			xishu = conf.moneyXishu;
		}
		int getTongbi = (xishu * p.getNumber() * baoJi)/ 100;
		checkBaoJiBroadcast(junZhu, baoJi);
		log.info("计算铜币数量{} x {} x {} = {}",xishu, p.getNumber(), baoJi, getTongbi);
		junZhu.tongBi = junZhu.tongBi + getTongbi;
		log.info("[{}]增加铜币{}", junZhu.id, getTongbi);
		//保存
		tongBi.setDate(today);
		tongBi.setNum(tongBi.getNum() + 1);
		log.info("玩家{}第{}次购买铜币成功 ", junZhuId, tongBi.getNum());
		HibernateUtil.save(tongBi);
		HibernateUtil.save(junZhu);
		//推送
		TongbiResp.Builder tbResp=TongbiResp.newBuilder();
		tbResp.setBaoji(baoJi);
		tbResp.setCost(p.getYuanbao());
		tbResp.setShumu(getTongbi);
		sendBuyTongbiResp(session, 0, tbResp);
		sendTongBiData(0, session, null);
		JunZhuMgr.inst.sendMainInfo(session);
		// 主线任务：购买1次铜币
		EventMgr.addEvent(ED.buy_tongbi_1_times, new Object[] { junZhu.id });
		// 每日任务中记录完成一次购买铜币
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
				junZhuId, DailyTaskConstants.buy_tongbi_id, 1));
	}
	
	public void checkBaoJiBroadcast(JunZhu junZhu, int baoJi) {
		EventMgr.addEvent(ED.BUY_TongBi_BaoJi, new Object[]{junZhu, baoJi});
	}

	/**
	 * @Description 连续购买铜币
	 * @param code
	 * @param session
	 * @param builder
	 */
	public void buyTongBiLianXu(int code, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		int code4Tongbi=Tong_Bi_CODE;
		if (junZhu == null) {
			error(session, code4Tongbi, "君主信息异常");
			return;
		}
		log.info("玩家{}连续购买铜币开始", junZhu.id);
		long junZhuId =junZhu.id;
		TongBi tongBi = HibernateUtil.find(TongBi.class, junZhuId);
		Date today = Calendar.getInstance().getTime();
		int maxBuyTimes= VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel, VipData.bugMoneyTime);
		if (tongBi == null) {
			tongBi = new TongBi();
			tongBi.setDbId(junZhuId);
			tongBi.setDate(today);
			tongBi.setNum(0);
			// 添加到缓存
			MC.add(tongBi, junZhuId);
			HibernateUtil.insert(tongBi);
		} else {
			Date lastDate = tongBi.getDate();
			// change 20150901
			if(DateUtils.isTimeToReset(lastDate, CanShu.REFRESHTIME_PURCHASE)){
				tongBi.setNum(0);
			}else{
				if (tongBi.getNum() >= maxBuyTimes) {
					sendBuyTongbiResp(session, 1, null);
					log.error("今天购买铜币次数达到最大，vipLevel:{},已经购买次数:{}",junZhu.vipLevel, maxBuyTimes);
					return;
				}
			}
		}
		int time=tongBi.getNum();
		Purchase p = getBuyConf(time, code4Tongbi);
		if(time==0){
			 p = getBuyConf(1, code4Tongbi);
		}
		if (p == null) {
			log.info("玩家{}连续购买铜币失败，未找到配置", junZhuId);
			error(session, Tong_Bi_CODE, "购买出现问题");
			return;
		}
		//当前阶段买一次铜币的花费的元宝数目
		int nowlevelCost=p.getYuanbao();
		List<Purchase> costList=new ArrayList<Purchase>();
		int cost4all=0;
		int buyCount=0;
		for (int i = time+1; i <=maxBuyTimes; i++) {
			Purchase p4temp=  getBuyConf(i, code4Tongbi);
			if(i == time+1&&p4temp!=null&&p4temp.getYuanbao()!=nowlevelCost){
				nowlevelCost=p4temp.getYuanbao();
			}
			if(p4temp==null||p4temp.getYuanbao()!=nowlevelCost){
				break;
			}
			costList.add(p4temp);
			cost4all+=p4temp.getYuanbao();
			buyCount++;
		}
		if (cost4all> junZhu.yuanBao) {
			log.error("玩家{}连续购买铜币失败,元宝不够", junZhuId);
			sendBuyTongbiResp(session, 2, null);
			return;
		}
		YuanBaoMgr.inst.diff(junZhu, -cost4all, 0,getPriceConf(code4Tongbi), YBType.YB_BUY_WUPIN, "连续购买铜币");
		LianXuBuyTongbiResp.Builder resp = LianXuBuyTongbiResp.newBuilder();
		// 公式 from 数值策划
		// 第N次购买获得的铜币=(【君主等级系数】×【购买次数系数】×【暴击倍数】) / 100
		int xishu = 1;
		JunzhuShengji conf = JunZhuMgr.inst.getJunzhuShengjiByLevel(junZhu.level);
		if(conf != null){
			xishu = conf.moneyXishu;
		}
		int shouyi=0;
		for (int i = 0; i < buyCount; i++) {
			Purchase p4Cost=costList.get(i);
			if(p4Cost==null){
				log.error("玩家{}连续购买第{}次购买铜币出错，未找到Purchase配置",junZhu.id,i);
				continue;
			}
			int baoJi = UserEquipAction.getInstance().getAddValue(3, 0);
			int getTongbi = (xishu * p4Cost.getNumber() * baoJi)/ 100;
			log.info("连续购买铜币，计算铜币数量{} x {} x {} = {}",xishu, p4Cost.getNumber(), baoJi, getTongbi);
			junZhu.tongBi = junZhu.tongBi + getTongbi;
			log.info("[{}]连续购买铜币，增加铜币{}", junZhu.id, getTongbi);
			TongbiResp.Builder tbResp=TongbiResp.newBuilder();
			tbResp.setBaoji(baoJi);
			tbResp.setCost(p4Cost.getYuanbao());
			tbResp.setShumu(getTongbi);
			resp.addTongbi(tbResp.build());
			shouyi+=getTongbi;
			checkBaoJiBroadcast(junZhu, baoJi);
		}
		//保存
		tongBi.setDate(today);
		tongBi.setNum(tongBi.getNum() + buyCount);
		HibernateUtil.save(tongBi);
		HibernateUtil.save(junZhu);
		//推送
		resp.setResult(0);
		session.write(resp.build());
		sendTongBiData(0, session, null);
		JunZhuMgr.inst.sendMainInfo(session);
		log.info("玩家{}连续--{}次购买铜币成功,花费--{}，收益--{}", junZhu.id, buyCount,cost4all,shouyi);
		// 主线任务：购买1次铜币
		EventMgr.addEvent(ED.buy_tongbi_1_times, new Object[] { junZhu.id });
		// 每日任务中记录完成一次购买铜币
		EventMgr.addEvent(ED.DAILY_TASK_PROCESS, new DailyTaskCondition(
				junZhuId, DailyTaskConstants.buy_tongbi_id, 1));
	}
	
	
	/**
	 * @Description 单次购买铜币
	 * @param time
	 * @param session
	 * @param date
	 * @param junZhu
	 * @return
	 */
	protected boolean consume_Tongb1(int time, IoSession session,  Date date,
			JunZhu junZhu) {
		
		return true;
	}
	/**
	 * @param time
	 * @param session
	 * @param date
	 * @param junZhu
	 * @return
	 */
	protected boolean consume_Tili(int time, IoSession session,  Date date,
			JunZhu junZhu) {
		int code=Ti_Li_CODE;
		time = time + 1;
		Purchase p = getBuyConf(time, code);
		if (p == null) {
			error(session, code, "购买出现问题");
			return false;
		}

		if (junZhu == null) {
			error(session, code, "君主信息异常");
			return false;
		}
		boolean yuanBaoEnough = true;
		if (p.getYuanbao() > junZhu.yuanBao) {
			log.error("元宝不够");
			yuanBaoEnough = false;
			return false;
		}
		switch (code) {
		case Ti_Li_CODE:
			if (junZhu.tiLi >= JunZhuMgr.TiLiMax) {
				return false;
			}
			if (!yuanBaoEnough) {
				return false;
			}
			JunZhuMgr.inst.updateTiLi(junZhu, p.getNumber(), "买体力");
			log.info("[{}]增加体力{}", junZhu.id, p.getNumber());
			YuanBaoMgr.inst.diff(junZhu, -p.getYuanbao(), 0,
					getPriceConf(code), YBType.YB_BUY_WUPIN, "购买体力");
			break;
		default:
			return false;
		}

		return true;
	}


	protected void sendBuyTongbiResp(IoSession session, int result,TongbiResp.Builder tbResp) {
		LianXuBuyTongbiResp.Builder resp = LianXuBuyTongbiResp.newBuilder();
		resp.setResult(result);
		if(tbResp!=null){
			resp.addTongbi(tbResp.build());
		}
		session.write(resp.build());
	}

	public int calcTongBi(JunZhu junZhu, float xishu) {
		List<JunzhuShengji> list = TempletService.listAll(JunzhuShengji.class.getSimpleName());
		int idx = junZhu.level - 1;// 配置文件从1级开始的
		JunzhuShengji conf = list.get(idx);
		if (conf == null) {
			return 0;
		}
		// 第N次购买获得的铜币=【君主等级系数】×【购买次数系数】×【暴击倍数】
		int baoJi = UserEquipAction.getInstance().getAddValue(3, 0);
		int ret = Math.round(conf.xishu * xishu * baoJi);
		log.info("计算铜币数量 {} x {} x {} = {}", conf.xishu, xishu, baoJi, ret);
		return ret;
	}

	/**
	 * @Title: getPriceConf
	 * @Description: 获取物品单价
	 * @param code
	 * @return
	 * @return int
	 * @throws
	 */
	public int getPriceConf(int code) {
		Map<Integer, Purchase> map = null;
		switch (code) {
		case Ti_Li_CODE:
			map = tiLiMap;
			break;
		case Tong_Bi_CODE:
			map = tongBiMap;
			break;
		case MIBAO_POINT_CODE:
			map = mibaoPointMap;
			break;
		default:
			return -1;
		}
		Purchase p = map.get(1);
		return p.getYuanbao();
	}

	protected Purchase getBuyConf(int time, int code) {
		Map<Integer, Purchase> map = null;
		switch (code) {
		case Ti_Li_CODE:
			map = tiLiMap;
			break;
		case Tong_Bi_CODE:
			map = tongBiMap;
			break;
		case MIBAO_POINT_CODE:
			map = mibaoPointMap;
			break;
		default:
			return null;
		}
		Purchase p = null;
		int maxTimes = map.size();
		if (time > maxTimes) {
			p = map.get(maxTimes);
		} else {
			p = map.get(time);
		}
		return p;
	}

	/**
	 * 获取purchase配置
	 * 
	 * @param type	购买的类型
	 * @param times	购买的次数
	 * @return 未找到购买类型的配置返回null，购买的次数超过最大配置次数，则返回最大次数配置
	 */
	public Purchase getPurchaseCfg(int type, int times) {
		List<Purchase> list = purchaseMap.get(type);
		if (list == null || list.size() == 0) {
			log.error("未找到type:{}的purchase配置", type);
			return null;
		}
		int size = list.size();
		Collections.sort(list);
		if (times > size) {
			return list.get(size - 1);
		}
		Purchase purchaseCfg = null;
		for (Purchase p : list) {
			if (p.getTime() == times) {
				purchaseCfg = p;
			}
		}
		return purchaseCfg;
	}

	/**
	 * 错误处理。
	 * 
	 * @param session
	 * @param code
	 * @param msg
	 */
	protected void error(IoSession session, int code, String msg) {
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(code);
		test.setErrorDesc(msg);
		session.write(test.build());
	}

	protected void purchaseFail(IoSession session, int type) {
		PurchaseFail.Builder fail = PurchaseFail.newBuilder();
		fail.setType(type);
		session.write(fail.build());
	}

	protected long getJunZhuIdBySession(IoSession session) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		return junZhuId == null ? -1 : junZhuId;
	}

	public int getTiLiTime(JunZhu junzhu) {
		TiLi tiLi = HibernateUtil.find(TiLi.class, junzhu.id);
		int maxbuy = VipMgr.INSTANCE.getValueByVipLevel(junzhu.vipLevel,
				VipData.bugTiliTime);
		
		if (tiLi == null ||
				DateUtils.isTimeToReset(tiLi.getDate(),
						CanShu.REFRESHTIME_PURCHASE)){
			return maxbuy;
		}
		return maxbuy - tiLi.getNum();
	}

	public void sendInfo(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		long junZhuId = getJunZhuIdBySession(session);
		TiLi t = HibernateUtil.find(TiLi.class, junZhuId);
		TongBi b = HibernateUtil.find(TongBi.class, junZhuId);
		MibaoLevelPoint levelPoint = HibernateUtil.find(MibaoLevelPoint.class,
				junZhuId);
		qxmobile.protobuf.JunZhuProto.BuyTimesInfo.Builder ret = BuyTimesInfo
				.newBuilder();
		int maxTili = VipMgr.INSTANCE.getValueByVipLevel(jz.vipLevel,
				VipData.bugTiliTime);
		int maxTongbi = VipMgr.INSTANCE.getValueByVipLevel(jz.vipLevel,
				VipData.bugMoneyTime);

		// change 20150901
		if(t == null){
			ret.setTiLi(maxTili);
		}else if(DateUtils.isTimeToReset(t.getDate(), CanShu.REFRESHTIME_PURCHASE)){
			ret.setTiLi(maxTili);
		}else{
			ret.setTiLi(maxTili - t.getNum());
		}
		if(b == null){
			ret.setTongBi(maxTongbi);
		}else if(DateUtils.isTimeToReset(b.getDate(), CanShu.REFRESHTIME_PURCHASE)){
			ret.setTongBi(maxTongbi);
		}else{
			ret.setTongBi(maxTongbi - b.getNum());
		}
//		if (t == null) {
//
//			ret.setTiLi(maxTili);
//		} else if (TimeUtil.isSameDay(t.getDate(), today)) {
//			ret.setTiLi(maxTili - t.getNum());
//		} else {
//			ret.setTiLi(maxTili);
//		}
//		if (b == null) {
//			ret.setTongBi(maxTongbi);
//		} else if (TimeUtil.isSameDay(b.getDate(), today)) {
//			ret.setTongBi(maxTongbi - b.getNum());
//		} else {
//			ret.setTongBi(maxTongbi);
//		}
		
		int levelPointTimes = 0;
		if(levelPoint != null) {
			Date lastDate = levelPoint.lastBuyTime;
			//change 20150901
			if(DateUtils.isTimeToReset(lastDate, CanShu.REFRESHTIME_PURCHASE)){
				levelPoint.dayTimes = 0;
				HibernateUtil.save(levelPoint);
			}
			levelPointTimes = levelPoint.dayTimes;
		}
		Purchase tiLiConf = getBuyConf(maxTili - ret.getTiLi() + 1, Ti_Li_CODE);
		Purchase tongBiConf = getBuyConf(maxTongbi - ret.getTongBi() + 1,
				Tong_Bi_CODE);
		Purchase levelPointConf = getBuyConf(levelPointTimes + 1,
				MIBAO_POINT_CODE);
		if (tiLiConf == null) {
			log.error("Purchase 体力 ，配置错误");
			ret.setTiLiHuaFei(0);
			ret.setTiLiHuoDe(0);
		} else {
			ret.setTiLiHuaFei(tiLiConf.getYuanbao());
			ret.setTiLiHuoDe((int) tiLiConf.getNumber());
		}
		if (tongBiConf == null) {
			log.error("Purchase 铜币 ，配置错误");
			ret.setTongBiHuaFei(0);
			ret.setTongBiHuoDe(0);
		} else {
			ret.setTongBiHuaFei(tongBiConf.getYuanbao());
			ret.setTongBiHuoDe((int) tongBiConf.getNumber());
		}
		if (levelPointConf == null) {
			log.error("Purchase 秘宝点数 ，配置错误");
			ret.setMibaoHuaFei(0);
			ret.setMibaoHuoDe(0);
		} else {
			ret.setMibaoHuaFei(levelPointConf.getYuanbao());
			int maxPointValue = VipMgr.INSTANCE.getValueByVipLevel(jz.vipLevel,
					VipData.mibaoCountLimit);
			ret.setMibaoHuoDe(maxPointValue);
		}
		session.write(ret.build());
	}

//	/**
//	 * 获取宝箱购买信息
//	 * 
//	 * @param cmd
//	 * @param session
//	 * @param builder
//	 */
//	public void sendTreasureInfos(int cmd, IoSession session, Builder builder) {
//		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
//		if (junZhuId == null) {
//			log.error("未发现君主，cmd:{}", cmd);
//			return;
//		}
//		List<Treasure> treasureList = HibernateUtil.list(Treasure.class,
//				" where junZhuId=" + junZhuId);
//		if (treasureList == null || treasureList.size() == 0) {
//			// 初始化宝箱信息
//			treasureList = insertTreasureInfo(junZhuId);
//		}
//		BuyTreasureInfosResp.Builder response = BuyTreasureInfosResp
//				.newBuilder();
//		for (Treasure t : treasureList) {
//			TreasureInfo.Builder treasureInfo = TreasureInfo.newBuilder();
//			treasureInfo.setType(t.getType());
//			treasureInfo.setIsGet(t.isGet());
//			treasureInfo.setTimes(t.getTimes());
//			treasureInfo.setCountDown(t.getCountDown());
//			response.addTreasureInfo(treasureInfo.build());
//		}
//		// 获取各个宝箱购买所花的元宝
//		TreasureCost.Builder costBuilder = TreasureCost.newBuilder();
//		costBuilder.setCostYuan(getNeedYuanBao(
//				PurchaseConstants.TREASURE_MIDDLE, 1));
//		costBuilder.setCostYuan5(getNeedYuanBao(
//				PurchaseConstants.TREASURE_BIG_5, 1));
//		costBuilder.setCostYuan20(getNeedYuanBao(
//				PurchaseConstants.TREASURE_BIG_20, 1));
//		costBuilder.setCostTiangongTu(0);
//		response.setTreasureCost(costBuilder.build());
//		session.write(response.build());
//	}
//
//	/**
//	 * 插入宝箱购买信息
//	 * 
//	 * @param junZhuId
//	 * @return
//	 */
//	protected List<Treasure> insertTreasureInfo(Long junZhuId) {
//		List<Treasure> treasureList = new ArrayList<Treasure>();
//		// 宝箱类型：1-小袋宝箱，2-中袋宝箱，3-大袋宝箱
//		Treasure small = new Treasure(junZhuId, TREASURE_CODE_SMALL);
//		Treasure middle = new Treasure(junZhuId, TREASURE_CODE_MIDDLE);
//		treasureList.add(small);
//		treasureList.add(middle);
//		HibernateUtil.save(small);
//		HibernateUtil.save(middle);
//		return treasureList;
//	}

//	/**
//	 * 购买宝箱
//	 * 
//	 * @param cmd
//	 * @param session
//	 * @param builder
//	 */
//	public void buyTreasure(int cmd, IoSession session, Builder builder) {
//		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
//		if (junZhuId == null) {
//			log.error("未发现君主id，cmd:{}", cmd);
//			return;
//		}
//		JunZhu junZhu = HibernateUtil.find(JunZhu.class, junZhuId);
//		if (junZhu == null) {
//			log.error("未发现君主，cmd:{}, junzhuId:{}", cmd, junZhuId);
//			return;
//		}
//		BuyTreasure.Builder request = (qxmobile.protobuf.Shop.BuyTreasure.Builder) builder;
//		int type = request.getType();
//		switch (type) {
//		case TREASURE_CODE_SMALL:
//			Treasure treasure = HibernateUtil.find(Treasure.class,
//					" where junZhuId=" + junZhuId + " and type=" + type);
//			if (!treasure.isGet()) {
//				log.info("现在还不能领取小袋奖励或者今日次数已用完");
//				purchaseFail(session, 3);
//				return;
//			}
//			getTreasureAward(treasure, session, 201, junZhu);
//			break;
//		case TREASURE_CODE_MIDDLE:
//			buyMiddleTreasure(session, junZhu, type, request.getAction());
//			break;
//		case TREASURE_CODE_BIG:
//			buyBigTreasure(session, junZhu, type, request.getAction());
//			break;
//		default:
//			log.error("type类型请求错误type value:{},cmd:{}", type, cmd);
//			break;
//		}
//	}

//	protected void buyBigTreasure(IoSession session, JunZhu junZhu, int type,
//			int action) {
//		int needYuanbao = 0;
//		int jiangliId = 0;
//		switch (action) {
//		case 1:// 五连抽
//			needYuanbao = getNeedYuanBao(PurchaseConstants.TREASURE_BIG_5, 1);
//			jiangliId = 203;
//			break;
//		case 2:// 二十连抽
//			needYuanbao = getNeedYuanBao(PurchaseConstants.TREASURE_BIG_20, 1);
//			jiangliId = 204;
//			break;
//		default:
//			break;
//		}
//		if (needYuanbao == -1) {
//			log.error("购买大袋宝箱获取所需元宝数发生错误!");
//			purchaseFail(session, 1);
//			return;
//		}
//		if (junZhu.yuanBao < needYuanbao) {
//			log.error("购买大袋宝箱元宝不足");
//			purchaseFail(session, 1);
//			return;
//		}
//		getTreasureAward(null, session, jiangliId, junZhu);
//		// junZhu.yuanBao -= needYuanbao;
//		YuanBaoMgr.inst.diff(junZhu, -needYuanbao, 0,
//				getPrice(PurchaseConstants.TREASURE_BIG_20),
//				YBType.YB_BUY_WUPIN, "购买大袋宝箱");
//		HibernateUtil.save(junZhu);
//	}

//	protected void buyMiddleTreasure(IoSession session, JunZhu junZhu, int type,
//			int action) {
//		Treasure treasure = HibernateUtil.find(Treasure.class,
//				" where junZhuId=" + junZhu.id + " and type=" + type);
//		switch (action) {
//		case 1:
//			if (!treasure.isGet()) {
//				purchaseFail(session, 3);
//				return;
//			}
//			getTreasureAward(treasure, session, 202, junZhu);
//			break;
//		case 2:// 天工图购买
//			Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
//			int tianGongCount = BagMgr.inst.getItemCount(bag,
//					TIAN_GONG_TU_ITMEID);
//			int subTianGong = tianGongCount;
//			if (tianGongCount < 0) {
//				log.info("天工图数量不足，不能购买中袋宝箱");
//				purchaseFail(session, 2);
//				return;
//			}
//			getTreasureAward(null, session, 202, junZhu);
//			// 扣除天工图
//			for (BagGrid grid : bag.grids) {
//				if (grid.itemId == TIAN_GONG_TU_ITMEID) {
//					if (grid.cnt >= subTianGong) {
//						grid.cnt -= subTianGong;
//						HibernateUtil.save(grid);
//						break;
//					} else {
//						subTianGong -= grid.cnt;
//						grid.itemId = -1;
//						grid.cnt = 0;
//						HibernateUtil.save(grid);
//					}
//				}
//			}
//			break;
//		case 3:// 元宝购买
//			int needYuanbao = getNeedYuanBao(PurchaseConstants.TREASURE_MIDDLE,
//					treasure.getTimes() + 1);
//			if (needYuanbao == -1) {
//				log.error("购买中袋宝箱获取所需元宝数发生错误!");
//				purchaseFail(session, 1);
//				return;
//			}
//			if (junZhu.yuanBao < needYuanbao) {
//				log.error("购买中袋宝箱元宝不足");
//				purchaseFail(session, 1);
//				return;
//			}
//			getTreasureAward(null, session, 202, junZhu);
//			// junZhu.yuanBao -= needYuanbao;
//			YuanBaoMgr.inst.diff(junZhu, -needYuanbao, 0,
//					getPrice(PurchaseConstants.TREASURE_MIDDLE),
//					YBType.YB_BUY_WUPIN, "购买中袋宝箱");
//			HibernateUtil.save(junZhu);
//			break;
//		default:
//			break;
//		}
//	}

	/**
	 * 不同的vip等级对某操作的次数不同，花费也不同 那么根据玩家vip等级，获取对应操作的元宝数
	 * 
	 * @Title: getneedYanBaoByVip
	 * @Description:
	 * @param vipLevel
	 *            ： junzhu.vipLevel
	 * @param typeInfo
	 *            : 例如：VipData.bugMoneyTime
	 * @param purchaseType
	 *            ： 例如： PurchaseConstants.TONGBI
	 * @return: 元宝数， 为-1表示获取失败或者数据出错
	 */
	public int getNeedYanBaoByVip(int vipLevel, int typeInfo, int purchaseType) {
		int times = VipMgr.INSTANCE.getValueByVipLevel(vipLevel, typeInfo);
		int yuanbao = getNeedYuanBao(purchaseType, times);
		return yuanbao;
	}

	/**
	 * 获得购买某类型物品本次所需元宝数
	 * 
	 * @param type
	 *            物品类型
	 * @param times
	 *            本次购买次数
	 * @return 
	 */
	public int getNeedYuanBao(int type, int times) {
		List<Purchase> list = purchaseMap.get(type);
		if (list == null || list.size() == 0) {
			log.error("未找到type:{}的purchase配置", type);
			return Integer.MAX_VALUE;
		}
		Collections.sort(list);
		int size = list.size();
		if (times >= size) {
			return list.get(size - 1).getYuanbao();
		}
		for (Purchase p : list) {
			if (p.getTime() == times) {
				return p.getYuanbao();
			}
		}
		log.error("未找到type:{},times:{}的purchase配置", type, times);
		return Integer.MAX_VALUE;
	}

//	/**
//	 * 获取宝箱奖励
//	 * 
//	 * @param treasure
//	 * @param session
//	 * @param jiangliId
//	 *            奖励id
//	 * @param junZhu
//	 */
//	protected void getTreasureAward(Treasure treasure, IoSession session,
//			int jiangliId, JunZhu junZhu) {
//		Jiangli jiangli = jiangliMap.get(jiangliId);
//		if (jiangli == null) {
//			log.error("没有找到对应的奖励配置信息，jiangli.id:{}", jiangliId);
//			return;
//		}
//		// TODO 这里需要判断背包是否能放下
//
//		List<AwardTemp> awardList = jiangLi2award(jiangli.getItem());
//		sendBuyTreasureResult(session, awardList, junZhu);
//		for (AwardTemp award : awardList) {
//			AwardMgr.inst.giveReward(session, award, junZhu);
//		}
//		if (treasure != null) { // 修改数据库宝箱购买信息
//			Date date = new Date();
//			treasure.setLastGetTime(date);
//			treasure.setTimes(treasure.getTimes() + 1);
//			log.info("君主：{},在时间:{}，领取的宝箱奖励,宝箱type:{} 次数{}", junZhu.id, date,
//					treasure.getType(), treasure.getTimes());
//			HibernateUtil.save(treasure);
//		}
//	}

	public List<AwardTemp> jiangLi2award(String jiangli) {
		List<AwardTemp> awardList = new ArrayList<AwardTemp>();
		String[] goodsList = jiangli.split("#");
		for (String goods : goodsList) {
			String[] gInfo = goods.split(":");
			AwardTemp awardTemp = new AwardTemp();
			fillAwardTemp(gInfo, awardTemp);
			awardList.add(awardTemp);
		}
		return awardList;
	}

	protected void fillAwardTemp(String[] gInfo, AwardTemp awardTemp) {
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
//
//	/**
//	 * 发送宝箱奖励信息
//	 * 
//	 * @param session
//	 * @param awardList
//	 * @param junZhu
//	 */
//	protected void sendBuyTreasureResult(IoSession session,
//			List<AwardTemp> awardList, JunZhu junZhu) {
//		BuyTreasureResp.Builder response = BuyTreasureResp.newBuilder();
//		for (AwardTemp awardTemp : awardList) {
//			TreasureAward.Builder tAward = TreasureAward.newBuilder();
//			int type = awardTemp.getItemType();
//			int itemId = awardTemp.getItemId();
//			tAward.setType(type);
//			tAward.setItemId(itemId);
//			if (type == 7) {// type等于7表示的是武将，这时需要判断是否有该武将，发给前端以做动画使用
//				HeroProtoType hero = HeroMgr.tempId2HeroProto.get(itemId);
//				WuJiang wuJiang = HeroMgr.inst.getWuJiangByHeroId(
//						(int) junZhu.id, hero.getHeroId());
//				if (wuJiang != null) {
//					tAward.setIsNewWuJiang(false);
//				} else {
//					tAward.setIsNewWuJiang(true);
//				}
//				HeroProtoType heroProtoType = HeroMgr.tempId2HeroProto
//						.get(itemId);
//				JingPo jingPo = HeroMgr.id2JingPo.get(heroProtoType
//						.getJingpoId());
//				tAward.setNums(jingPo.getFenjieNum());
//			} else {
//				tAward.setNums(awardTemp.getItemNum());
//			}
//			response.addTreasureAward(tAward.build());
//		}
//		session.write(response.build());
//	}

	/**
	 * 获得洗练次数
	 * 
	 * @param junzhuId
	 * @return
	 */
	public XiLian getXiLian(long junzhuId) {
		Date today = new Date(System.currentTimeMillis());
		XiLian xiLian = HibernateUtil.find(XiLian.class, junzhuId);
		if (xiLian == null) {
			xiLian = new XiLian();
			xiLian.setDbId(junzhuId);
			xiLian.setDate(today);
			xiLian.setNum(0);
			//2015年9月15日 当日洗练石洗练次数
			xiLian.setXlsCount(0);
			// 添加缓存
			MC.add(xiLian, junzhuId);
			HibernateUtil.insert(xiLian);
		} else {
			Date lastDate = xiLian.getDate();
			//change 20150901
			if(DateUtils.isTimeToReset(lastDate, CanShu.REFRESHTIME_PURCHASE)){
				xiLian.setNum(0);
				//2015年9月15日 当日洗练石洗练次数
				xiLian.setXlsCount(0);
			}
		}
		return xiLian;
	}

	/**
	 *  小于等于buyHuiShu的总共拥有次数number
	 * @Title: getAllUseNumbers 
	 * @Description:
	 * @param type
	 * @param buyHuiShu
	 * @return
	 */
	public int getAllUseNumbers(int type, int buyHuiShu) {
		List<Purchase> list = purchaseMap.get(type);
		int buyCiShu = 0;
		if (list == null) {
			return 0;
		}
		for (Purchase p : list) {
			if (p.getTime() <= buyHuiShu) {
				buyCiShu += p.getNumber();
			}
		}
		return buyCiShu;
	}

	public synchronized void buyMibaoPoint(int id, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		MibaoLevelPoint levelPoint = HibernateUtil.find(MibaoLevelPoint.class,
				junZhu.id);
		if (levelPoint == null) {
			log.error("购买秘宝升级点数失败，找不到君主Id:{}的MibaoLevelPoint的表信息", junZhu.id);
			return;
		}

		BuyMibaoPointResp.Builder resp = BuyMibaoPointResp.newBuilder();
		boolean isPermit = VipMgr.INSTANCE.isVipPermit(VipData.mibao_level_point, junZhu.vipLevel);
		if (!isPermit) {
			resp.setResult(2);
			session.write(resp.build());
			log.error("购买秘宝升级点数失败，君主:{}-vip等级:{}，当前还不能进行购买秘宝升级点数操作", junZhu.id, junZhu.vipLevel);
			return;
		}
		Date lastDate = levelPoint.lastBuyTime;
		Date curDate = new Date(System.currentTimeMillis());
		if (DateUtils.isTimeToReset(lastDate, CanShu.REFRESHTIME_PURCHASE)){
			levelPoint.dayTimes = 0;
		}
		MibaoMgr.inst.refreshLevelPoint(levelPoint, curDate, junZhu.vipLevel);
		Purchase p = getBuyConf(levelPoint.dayTimes + 1, MIBAO_POINT_CODE);
		int costYuanBao = p.getYuanbao();
		if (junZhu.yuanBao < costYuanBao) {
			resp.setResult(1);
			session.write(resp.build());
			log.error("购买秘宝升级点数失败，君主:{}-元宝:{}不足第{}次购买需要的元宝:{}", junZhu.id, junZhu.yuanBao,
					levelPoint.dayTimes + 1, costYuanBao);
			return;
		}

		int getValue = VipMgr.INSTANCE.getValueByVipLevel(junZhu.vipLevel,
				VipData.mibaoCountLimit);
		if (levelPoint.point >= getValue) {
			resp.setResult(3);
			session.write(resp.build());
			log.error("购买秘宝升级点数失败，君主:{}-vip等级:{}当前秘宝升级点数:{}已满", junZhu.id, junZhu.vipLevel,levelPoint.point);
			return;
		}
		int maxValue = getValue * 2;
		int subValue = 0;
		levelPoint.dayTimes += 1;
		levelPoint.point += getValue;
		if (levelPoint.point > maxValue) {
			subValue = levelPoint.point - maxValue;
			levelPoint.point = maxValue;
			getValue = getValue - subValue;
		}
		levelPoint.lastBuyTime = curDate;
		HibernateUtil.save(levelPoint);

		YuanBaoMgr.inst
				.diff(junZhu, -costYuanBao, 0, getPriceConf(MIBAO_POINT_CODE),
						YBType.YB_BUY_WUPIN, "购买秘宝升级点数");
		HibernateUtil.save(junZhu);
		JunZhuMgr.inst.sendMainInfo(session);

		log.info("秘宝升级点数购买成功，玩家{}第{}次购买秘宝升级点数，花费:{}元宝,得到{}点,舍去{}点", junZhu.id,
				levelPoint.dayTimes, costYuanBao, getValue, subValue);
		resp.setResult(0);
		session.write(resp.build());
		sendInfo(0, session, null);
	}
	
	/**
	 * @Description 获取铜币购买相关信息
	 * @param id
	 * @param session
	 * @param builder
	 */
	public void sendTongBiData(int id, IoSession session, Builder builder) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			return;
		}
		long junZhuId =jz.id;
		TongBi tb = HibernateUtil.find(TongBi.class, junZhuId);
		BuyTongbiDataResp.Builder resp = BuyTongbiDataResp.newBuilder();
		int maxCount4Tongbi = VipMgr.INSTANCE.getValueByVipLevel(jz.vipLevel,VipData.bugMoneyTime);
		if(tb!=null&&DateUtils.isTimeToReset(tb.getDate(), CanShu.REFRESHTIME_PURCHASE)){
			tb.setNum(0);
		}
		int nowCount=0;
		nowCount=tb==null?nowCount:tb.getNum();
		resp.setNowCount(nowCount);
		resp.setMaxCount(maxCount4Tongbi);
		Purchase p4conf = getBuyConf(nowCount + 1,Tong_Bi_CODE);
		if (p4conf == null) {
			log.error("Purchase 铜币 ，配置错误");
			resp.setCost(0);
			resp.setGetTonbi(0);
		} else {
			int baoJi = 1;
			int xishu = 1;
			JunzhuShengji conf = JunZhuMgr.inst.getJunzhuShengjiByLevel(jz.level);
			if(conf != null){
				xishu = conf.moneyXishu;
			}
			int getTongbi = (xishu * p4conf.getNumber() * baoJi)/ 100;
			resp.setCost(p4conf.getYuanbao());
			resp.setGetTonbi(getTongbi);
		}
		
		int time=nowCount;
		Purchase p = null;
		if(time==0){
			 p = getBuyConf(1, Tong_Bi_CODE);
		}else{
			p = getBuyConf(time, Tong_Bi_CODE);
		}
		if (p == null) {
			log.info("玩家{}连续购买铜币失败，未找到配置", junZhuId);
			error(session, Tong_Bi_CODE, "购买出现问题");
			return;
		}
		//当前阶段买一次铜币的花费的元宝数目
		int nowlevelCost=p.getYuanbao();
		int lianxuCount=0;
		for (int i = time+1; i <= maxCount4Tongbi; i++) {
			Purchase p4temp=  getBuyConf(i, Tong_Bi_CODE);
			if(i == time+1&&p4temp!=null&&p4temp.getYuanbao()!=nowlevelCost){
				nowlevelCost=p4temp.getYuanbao();
			}
			if(p4temp==null||p4temp.getYuanbao()!=nowlevelCost){
				break;
			}
			lianxuCount++;
		}
		resp.setLixuCount(lianxuCount);
		log.info("君主{}返回今日购买铜币次数--{}，最大铜币购买次数--{}，下次购买每次花费--{}，每次获得铜币--{}，可连续购买次数--{}",
				junZhuId,resp.getNowCount(),resp.getMaxCount(),resp.getCost(),resp.getGetTonbi(),resp.getLixuCount());
		session.write(resp.build());
	}
}
