package com.qx.huangye.shop;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.ActLog;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.Shop.BuyGoodReq;
import qxmobile.protobuf.Shop.BuyGoodResp;
import qxmobile.protobuf.Shop.ShopReq;
import qxmobile.protobuf.Shop.ShopResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.Dangpu;
import com.manu.dynasty.template.DangpuCommon;
import com.manu.dynasty.template.Duihuan;
import com.manu.dynasty.template.GongXunDuihuan;
import com.manu.dynasty.template.HuangYeDuihuan;
import com.manu.dynasty.template.LMGongXianDuihuan;
import com.manu.dynasty.util.DateUtils;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.award.AwardMgr;
import com.qx.bag.BagMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.pawnshop.GoodsInfo;
import com.qx.persistent.HibernateUtil;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.pvp.PvpMgr;
import com.qx.util.JsonUtils;
import com.qx.util.RandomUtil;
import com.qx.yuanbao.YBType;

public class ShopMgr {
	// 荒野商店
	public static final int huangYe_shop_type = 1;
	// 联盟商店
	public static final int lianMeng_shop_type = 2;
	// 联盟战商店
	public static final int lianmeng_battle_shop_type = 3;
	// 百战商店
	public static final int baizhan_shop_type=  4;
//	// 普通商店
//	public static final int common_shop_type = 5;
	// 神秘商店
	public static final int mysterious_shop_type = 6;
	
	public Map<Integer, List<BaseDuiHuan>> hyShopListMap = new HashMap<Integer, List<BaseDuiHuan>>();
	public Map<Integer, BaseDuiHuan> hyShopMap = new HashMap<Integer, BaseDuiHuan>();
	
	public Map<Integer, List<BaseDuiHuan>> lmShopListMap = new HashMap<Integer, List<BaseDuiHuan>>();
	public Map<Integer, BaseDuiHuan> lmShopMap = new HashMap<Integer, BaseDuiHuan>();
	
	public Map<Integer, List<BaseDuiHuan>>  gongxunShopListMap= new HashMap<Integer, List<BaseDuiHuan>>();
	public Map<Integer, BaseDuiHuan> gongxunShopMap = new HashMap<Integer, BaseDuiHuan>();
	
	public Map<Integer, List<BaseDuiHuan>>  baiZhanShopListMap= new HashMap<Integer, List<BaseDuiHuan>>();
	public Map<Integer, BaseDuiHuan> baiZhanShopMap = new HashMap<Integer, BaseDuiHuan>();
	
	// 普通商店
//	public Map<Integer, List<BaseDuiHuan>>  commonShopListMap= new HashMap<Integer, List<BaseDuiHuan>>();
	public Map<Integer, BaseDuiHuan> commonShopMap = new HashMap<Integer, BaseDuiHuan>();
	
	// 神秘商店
	public Map<Integer, List<BaseDuiHuan>>  mysteriousShopListMap= new HashMap<Integer, List<BaseDuiHuan>>();
	public Map<Integer, BaseDuiHuan> mysteriousShopMap = new HashMap<Integer, BaseDuiHuan>();

	public static final int shop_space = 10;
	
	// 神秘商店的购买类型： 0： 铜币购买， 1 元宝购买
	public static int tongBi_buy = 0;
	public static int yuanBao_buy = 1;
	
	public static Logger logger = LoggerFactory.getLogger(ShopMgr.class);
	public static ShopMgr inst;
	public enum Money{
		huangYeBi, lianMengGongXian, gongXun, weiWang
	}

	public ShopMgr() {
		initData();
		inst = this;
	}

	@SuppressWarnings("unchecked")
	public void initData() {
		/*
		 * 荒野商店数据
		 */
		List<HuangYeDuihuan> yhShopList = TempletService
				.listAll(HuangYeDuihuan.class.getSimpleName());
		for (HuangYeDuihuan dh : yhShopList) {
			int site = dh.site;
			List<BaseDuiHuan> dList = hyShopListMap.get(site);
			if (dList == null) {
				dList = new ArrayList<BaseDuiHuan>();
				dList.add(dh);
				hyShopListMap.put(dh.site, dList);
			} else {
				dList.add(dh);
			}
			hyShopMap.put(dh.id, dh);
		}
		
		List<LMGongXianDuihuan> list = TempletService
				.listAll(LMGongXianDuihuan.class.getSimpleName());
		for (LMGongXianDuihuan dh : list) {
			int site = dh.site;
			List<BaseDuiHuan> dList = lmShopListMap.get(site);
			if (dList == null) {
				dList = new ArrayList<BaseDuiHuan>();
				dList.add(dh);
				lmShopListMap.put(dh.site, dList);
			} else {
				dList.add(dh);
			}
			lmShopMap.put(dh.id, dh);
		}
		
		List<GongXunDuihuan> list2 = TempletService
				.listAll(GongXunDuihuan.class.getSimpleName());
		for (GongXunDuihuan dh : list2) {
			int site = dh.site;
			List<BaseDuiHuan> dList = gongxunShopListMap.get(site);
			if (dList == null) {
				dList = new ArrayList<BaseDuiHuan>();
				dList.add(dh);
				gongxunShopListMap.put(dh.site, dList);
			} else {
				dList.add(dh);
			}
			gongxunShopMap.put(dh.id, dh);
		}
		
		List<Duihuan> list3 = TempletService
				.listAll(Duihuan.class.getSimpleName());
		for (Duihuan dh : list3) {
			int site = dh.site;
			List<BaseDuiHuan> dList = baiZhanShopListMap.get(site);
			if (dList == null) {
				dList = new ArrayList<BaseDuiHuan>();
				dList.add(dh);
				baiZhanShopListMap.put(dh.site, dList);
			} else {
				dList.add(dh);
			}
			baiZhanShopMap.put(dh.id, dh);
		}
		List<DangpuCommon> list4 = TempletService
				.listAll(DangpuCommon.class.getSimpleName());
		for (DangpuCommon dh : list4) {
//			int site = dh.site;
//			List<BaseDuiHuan> dList = commonShopListMap.get(site);
//			if (dList == null) {
//				dList = new ArrayList<BaseDuiHuan>();
//				dList.add(dh);
//				commonShopListMap.put(dh.site, dList);
//			} else {
//				dList.add(dh);
//			}
			commonShopMap.put(dh.id, dh);
		}
	
		List<Dangpu> list5 = TempletService
				.listAll(Dangpu.class.getSimpleName());
		for (Dangpu dh : list5) {
			int site = dh.site;
			List<BaseDuiHuan> dList = mysteriousShopListMap.get(site);
			if (dList == null) {
				dList = new ArrayList<BaseDuiHuan>();
				dList.add(dh);
				mysteriousShopListMap.put(dh.site, dList);
			} else {
				dList.add(dh);
			}
			mysteriousShopMap.put(dh.id, dh);
		}
	}

	/*
	 * 荒野、或者联盟商店百战商店、联盟战商店页面请求
	 */
	public void dealGetShopInfoReq(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			logger.error("玩家请求商店页面出错：君主不存在");
			return;
		}
		ShopReq.Builder req = (ShopReq.Builder) builder;
		int type = req.getType();
		int bigType = type / shop_space;
		PublicShop bean = HibernateUtil.find(PublicShop.class, jz.id * shop_space + bigType);
		if (bean == null) {
			bean = initShopInfo(jz.id, bigType);
		} else {
			// 检查是否更新
			resetHYShopBean(bean);
		}
		ShopResp.Builder resp = ShopResp.newBuilder();
		List<GoodsInfo> goods = getGoodsInfo(bean);
		int money = getMoney(bigType, jz, bean);
		/*
		 * 普通商店类型： 5，普通商店永不刷新，所以普通商店物品客户端处理显示
		 */
		/*
		 * type == X1: 花费money刷新商店商品列表
		 */
		if (type == 11 || type == 21 || type == 31 || type == 41 || type == 61) {
			int needYB = getRefreshNeedHYMoney(bean, bigType);
			if (needYB > money) {
				// money不足，不能手动刷新
				resp.setMsg(11);
				logger.info("玩家id{},姓名 {},商店类型：{}，用货币刷新商品列表失败：货币不足", jz.id, jz.name, bigType);
				session.write(resp.build());
				return;
			}
			// 特殊处理：神秘商店刷新花费元宝
			if(type == 61){
				PvpMgr.inst.isBuySuccess(jz, 
						needYB, session, YBType.YB_PAWN_REFRESH, "神秘商店刷新");
				money = jz.yuanBao;
			}else{
				money -= needYB;
				setMoney(bigType, jz.id, bean, money);
			}
			// 刷新货物
			goods = getRandomGoodsList(bigType);
			bean.goodsInfo = setGoodsInfo(goods);
			// 今日刷新货物次数加1
			bean.buyNumber += 1;
			HibernateUtil.save(bean);
	
			logger.info("玩家id{},姓名 {},商店类型：{}， 用货币刷新商品列表，花费货币：{}", jz.id, jz.name,
					bigType, needYB);
			resp.setMsg(12);
		}else{
			/*
			 * type == X0: 请求商品兑换页面
			 */
			String s = bean.goodsInfo;
			// 9点或者21点主动刷新
			if (s == null
					|| "".equals(s)
					|| System.currentTimeMillis() >= bean.nextAutoRefreshTime
							.getTime()) {
				// 自动刷新货物
				goods = getRandomGoodsList(bigType);
				bean.goodsInfo = setGoodsInfo(goods);
				bean.nextAutoRefreshTime = getNextNineTime(new Date());
				HibernateUtil.save(bean);
			}
			resp.setMsg(0);
		}
		fillDuiHuanInfo(bean, resp, goods, bigType);
		resp.setMoney(money);
		// 神秘商店也发送铜币数
		if(bigType == mysterious_shop_type){
			resp.setTongbi(jz.tongBi);
		}
		session.write(resp.build());
	}

	private int getRefreshNeedHYMoney(PublicShop bean, int shop_type) {
		int number = bean.buyNumber;
		number += 1;
		int money = PurchaseMgr.inst.getNeedYuanBao(getPurchaseType(shop_type),
				number);
		return money;
	}

	public int getPurchaseType(int shop_type) {
		switch (shop_type) {
		case huangYe_shop_type:
			return PurchaseConstants.refresh_HY_shop;
		case lianMeng_shop_type:
			return PurchaseConstants.refresh_LianMeng_shop;
		case lianmeng_battle_shop_type:
			return PurchaseConstants.refresh_LianMeng_battle_shop;
		case baizhan_shop_type:
			return PurchaseConstants.refresh_baizhan_shop;
		case mysterious_shop_type:
			return PurchaseConstants.PAWNSHOP_REFRESH;
		}
		return 0;
	}

	public PublicShop initShopInfo(long jid, int shop_type) {
		PublicShop bean = new PublicShop();
		bean.id = jid * shop_space + shop_type;
		bean.goodsInfo = setGoodsInfo(getRandomGoodsList(shop_type));
		bean.nextAutoRefreshTime = getNextNineTime(new Date());
		bean.lastResetShopTime = new Date();
		bean.buyNumber = 0;
		bean.setMoney(0);
		HibernateUtil.save(bean);
		logger.info("玩家：{}商店类型：{}数据生成成功", jid, shop_type);
		return bean;
	}

	public List<GoodsInfo> getGoodsInfo(PublicShop hyShop) {
		return JsonUtils.strToList(hyShop.goodsInfo, GoodsInfo.class);
	}

	public String setGoodsInfo(List<GoodsInfo> goodsList) {
		return JsonUtils.listToStr(goodsList);
	}

	/*
	 * 随机刷新荒野商店列表
	 */
	protected List<GoodsInfo> getRandomGoodsList(int shop_type) {
		List<GoodsInfo> goodsList = new ArrayList<GoodsInfo>();
		Map<Integer, List<BaseDuiHuan>> map = getShopGoodsListMap(shop_type);
		for (Map.Entry<Integer, List<BaseDuiHuan>> entry : map.entrySet()) {
			List<BaseDuiHuan> list = entry.getValue();
			int gailv = RandomUtil.getRandomNum(10000);
			int sum = 0;
			for (BaseDuiHuan dp : list) {
				sum += dp.weight;
				if (gailv < sum) {
					GoodsInfo ginfo = new GoodsInfo();
					ginfo.setId(dp.id);
					ginfo.setSell(false);
					goodsList.add(ginfo);
					break;
				}
			}
		}
		return goodsList;
	}

	public void resetHYShopBean(PublicShop bean) {
		if (DateUtils.isTimeToReset(bean.lastResetShopTime,
				CanShu.REFRESHTIME_PURCHASE)) {
			bean.buyNumber = 0;
			bean.lastResetShopTime = new Date();
			HibernateUtil.save(bean);
		}
	}

	public void fillDuiHuanInfo(PublicShop bean, ShopResp.Builder resp,
			List<GoodsInfo> list, int shop_type) {
		resp.setNextRefreshNeedMoney(getRefreshNeedHYMoney(bean, shop_type));
		int time = (int) (bean.nextAutoRefreshTime.getTime() - System
				.currentTimeMillis()) / 1000;
		time = time < 0 ? 0 : time;
		resp.setRemianTime(time);
		qxmobile.protobuf.Shop.DuiHuanInfo.Builder duihuan = null;
		for (GoodsInfo goods : list) {
			Map<Integer, BaseDuiHuan> m = getShopGoodsMap(shop_type);
			if(m == null){
				logger.error("shop_type: 的 map 为null{}", shop_type);
				continue;
			}
			BaseDuiHuan d = m.get(goods.getId());
			if(d == null){
				logger.error("goods.getId(): 的 d 为null{}", goods.getId());
				continue;
			}
			duihuan = qxmobile.protobuf.Shop.DuiHuanInfo.newBuilder();
			duihuan.setId(goods.getId());
 			duihuan.setSite(d.site);
			duihuan.setIsChange(!goods.isSell());
			resp.addGoodsInfos(duihuan);
		}
	}

	public Map<Integer, List<BaseDuiHuan>> getShopGoodsListMap(int shop_type) {
		switch (shop_type) {
		case 1:
			return hyShopListMap;
		case 2:
			return lmShopListMap;
		case 3: 
			return gongxunShopListMap;
		case 4:
			return baiZhanShopListMap;
		case 6:
			return mysteriousShopListMap;
		}
		return null;
	}

	public Map<Integer, BaseDuiHuan> getShopGoodsMap(int shop_type) {
		switch (shop_type) {
		case 1:
			return hyShopMap;
		case 2:
			return lmShopMap;
		case 3:
			return gongxunShopMap;
		case 4:
			return baiZhanShopMap;
		case 5:
			return commonShopMap;
		case 6:
			return mysteriousShopMap;
		}
		return null;
	}

	public void dealBuyGoodReq(int id, Builder builder, IoSession session) {
		JunZhu jz = JunZhuMgr.inst.getJunZhu(session);
		if (jz == null) {
			logger.error("商店购买物品失败：君主不存在");
			return;
		}
		BuyGoodReq.Builder req = (BuyGoodReq.Builder) builder;
		int goodId = req.getGoodId();
		int bigType = req.getType();

		BaseDuiHuan dh = getShopGoodsMap(bigType).get(goodId);
		if (dh == null) {
			logger.error("玩家{}， 商店类型:{}购买物品失败：BaseDuiHuan 子类表id是{}无数据", jz.id,
					bigType, goodId);
			return;
		}
		BuyGoodResp.Builder resp = BuyGoodResp.newBuilder();

		int money = dh.needNum;
		/*
		 * 普通商店物品
		 */
		if(bigType == 5){
			boolean ok = PvpMgr.inst.isBuySuccess(jz, money, session,
					YBType.YB_BUY_WUPIN, "元宝购买普通商店物品");
			// 添加物品
			if(ok){
				AwardTemp a = new AwardTemp();
				a.setId(111);
				a.setItemId(dh.itemId);
				a.setItemType(dh.itemType);
				a.setItemNum(dh.itemNum);
				AwardMgr.inst.giveReward(session, a, jz);

				resp.setMsg(1);
				resp.setRemianMoney(jz.yuanBao);
				logger.info("玩家id{}, 用元宝：{}购买普通商店物品：{}成功", jz.id, money, goodId);
			} else {
				resp.setMsg(0);
				logger.info("玩家id{}, 用元宝：{}购买普通商店物品:{}失败：元宝不足",jz.id, money, goodId);
			}
			session.write(resp.build());
			return;
		}
		/*
		 * 其他几种商店物品购买
		 */
		int oldMoney = 0;
		PublicShop bean = HibernateUtil.find(PublicShop.class, jz.id * shop_space + bigType);
		if (bean == null) {
			bean = initShopInfo(jz.id, bigType);
		}
		// 判断是否售罄
		List<GoodsInfo> goodsL = getGoodsInfo(bean);
		GoodsInfo buyg = null;
		for(GoodsInfo  g: goodsL){
			if(g.getId() == goodId){
				// 已经卖完
				if(g.isSell()){
					// 已经售罄
					resp.setMsg(2);
					session.write(resp.build());
					return;
				}
				buyg = g;
				break;
			}
		}
		if(buyg == null){
			// 购买物品不存在
			resp.setMsg(3);
			session.write(resp.build());
			return;
		}
		oldMoney = getMoney(bigType, jz, bean);
		// 神秘商品的铜币购买
		if(bigType == mysterious_shop_type && dh.type == tongBi_buy){
			oldMoney = jz.tongBi;
		}
		if(oldMoney >= money) {
			final int preV = oldMoney;
			// 神秘商店的铜币购买
			if(bigType == mysterious_shop_type && dh.type == tongBi_buy){
				jz.tongBi -= money;
				HibernateUtil.save(jz);
				JunZhuMgr.inst.sendMainInfo(session);
				oldMoney = jz.tongBi;
			}else if(bigType == mysterious_shop_type && dh.type == yuanBao_buy){
				// 普通商店元宝购买或者神秘商店元宝购买
				PvpMgr.inst.isBuySuccess(jz, money, session, YBType.YB_BUY_WUPIN, "元宝购买神秘商店物品");
				oldMoney = jz.yuanBao;
			}else{
				// 其他类型购买
				oldMoney -= money;
				setMoney(bigType, jz.id, bean, oldMoney);
			}

			buyg.setSell(true);
			bean.goodsInfo = setGoodsInfo(goodsL);
			bean.buyGoodTimes += 1; // 历史购买物品次数增加
			HibernateUtil.save(bean);

			// 添加物品
			AwardTemp a = new AwardTemp();
			a.setId(111);
			a.setItemId(dh.itemId);
			a.setItemType(dh.itemType);
			a.setItemNum(dh.itemNum);
			AwardMgr.inst.giveReward(session, a, jz);

			/* 购买成功 */
			resp.setMsg(1);
			resp.setRemianMoney(oldMoney);
			logger.info("玩家id{},姓名 {}, 商店类型:{}用货币购买物品[{}]成功 货币{}", jz.id, jz.name,
					bigType, dh.itemId, money);
			String itemName = BagMgr.inst.getItemName(dh.itemId);
			ActLog.log.ChallengeExchange(jz.id, jz.name, ActLog.vopenid,
					dh.itemId, itemName, dh.itemNum, preV, oldMoney);
			if(bigType == baizhan_shop_type){
				// 主线任务: 消耗一次威望（在威望商店里购买1次物品）20190916
				EventMgr.addEvent(ED.pay_weiWang , new Object[] { jz.id});
			}
			// 购买的要是宝石，就判定是否有任务完成
			if(bigType == mysterious_shop_type && a.getItemType() == AwardMgr.type_baoShi){
				EventMgr.addEvent(ED.pawnshop_buy, new Object[] { jz.id});
				logger.info("君主:{}在神秘商铺购买宝石,宝石id：{}成功, 判定是否有购买宝石任务", jz.id, a.getItemId());
			}
		} else {
			/* 0：不足 */
			resp.setMsg(0);
			logger.info("玩家id{},姓名 {},  商店类型:{},goodId :{}用货币购买物品失败：货币不足",
					jz.id, jz.name, bigType, goodId);
		}
		session.write(resp.build());
	}

/**
 *  处理玩家荒野币，功勋，威望 ，贡献值
 * @param bigType
 * @param jzId
 * @param bean
 * @param newMoney
 */
	public void setMoney(int bigType, long jzId, PublicShop bean, int newMoney){
		switch(bigType){
		case huangYe_shop_type: // 荒野：荒野币
		case lianmeng_battle_shop_type: // 联盟战：功勋
		case baizhan_shop_type: // 百战:威望
			if(bean == null){
				bean = HibernateUtil.find(PublicShop.class, jzId * shop_space + bigType);
				if(bean == null){
					bean = initShopInfo(jzId, bigType);
				}
			}
			bean.setMoney(newMoney);
			HibernateUtil.save(bean);
			logger.info("玩家id{},获取类型：{}的货币：{}", jzId, bigType, newMoney);
			break;
		case lianMeng_shop_type: // 联盟商店是贡献值
			AlliancePlayer p = HibernateUtil.find(AlliancePlayer.class, jzId);
			if(p == null){
				p = new AlliancePlayer();
				AllianceMgr.inst.initAlliancePlayerInfo(jzId, -1, p, 0);
				p.gongXian = newMoney;
				HibernateUtil.insert(p);
				logger.error("{} 无AlliancePlayer数据， 新建， 并获得联盟贡献：{} ", jzId, p.gongXian);
			}else{
				p.gongXian = newMoney;
				HibernateUtil.save(p);
			}
			logger.info("玩家id{},获取联盟贡献值：{}", jzId, newMoney);
			break;
		default:
			logger.error("setMoney type 类型出错");
			break;
		}
	}
	public int addMoney(ShopMgr.Money m, int shopType, long jzId, int addValue){
		int all = addValue + getMoney(m, jzId, null);
		setMoney(shopType, jzId, null, all);
		return all;
	}

	public int getMoney(int bigType, JunZhu jz , PublicShop bean){
		long jzId = jz.id;
		switch(bigType){
		case huangYe_shop_type: // 荒野：荒野币
		case lianmeng_battle_shop_type: // 联盟战：功勋
		case baizhan_shop_type: // 百战商店： 威望
			if(bean == null){
				bean = HibernateUtil.find(PublicShop.class, jzId * shop_space + bigType);
			}
			return bean == null? 0: bean.getMoney();
		case lianMeng_shop_type: // 联盟商店是贡献值
			AlliancePlayer p = HibernateUtil.find(AlliancePlayer.class, jzId);
			return p == null? 0: p.gongXian;
		case mysterious_shop_type:
			return jz.yuanBao;
		}
		return 0;
	}
	public int getMoney(ShopMgr.Money m, long jzId, PublicShop bean){
		int type = -1;
		switch(m){
		case huangYeBi:
			type = huangYe_shop_type;
			break;
		case gongXun:
			type=  lianmeng_battle_shop_type;
			break;
		case weiWang:
			type = baizhan_shop_type;
			break;
		case lianMengGongXian:
			AlliancePlayer p = HibernateUtil.find(AlliancePlayer.class, jzId);
			return p == null? 0: p.gongXian;
		}
		if(type != -1){
			if(bean == null){
				bean = HibernateUtil.find(PublicShop.class, jzId * shop_space + type);
			}
			return bean == null? 0: bean.getMoney();
		}
		return 0;
	}
	
	/**
	 * 获取距离date时间最近的下一个9点或者21点时间
	 * 
	 * @Title: getNextUpdateDuihuan
	 * @Description:
	 * @param date
	 * @return
	 */
	public Date getNextNineTime(Date date) {
		long time = date.getTime();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		int todayTime = hour * 3600 + min * 60 + second;

		if (hour < 9) {
			int leftTime = (9 * 3600 - todayTime) * 1000;
			time += leftTime;
			return new Date(time);
		}
		if (hour >= 9 && hour < 21) {
			int leftTime = (21 * 3600 - todayTime) * 1000;
			time += leftTime;
			return new Date(time);
		}
		if (hour >= 21) {
			int leftTime = (24 * 3600 - todayTime + 9 * 3600) * 1000;
			time += leftTime;
			return new Date(time);
		}
		return date;
	}
	public DangpuCommon getDangpuCommon(int id) {
		DangpuCommon dangpuCommon = (DangpuCommon)commonShopMap.get(id);
		return dangpuCommon;
	}
}
