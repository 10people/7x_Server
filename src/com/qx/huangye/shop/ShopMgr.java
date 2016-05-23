package com.qx.huangye.shop;

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
import com.manu.dynasty.template.Dangpu;
import com.manu.dynasty.template.DangpuCommon;
import com.manu.dynasty.template.Duihuan;
import com.manu.dynasty.template.GongXunDuihuan;
import com.manu.dynasty.template.HuangYeDuihuan;
import com.manu.dynasty.template.ItemTemp;
import com.manu.dynasty.template.LMGongXianDuihuan;
import com.manu.dynasty.template.MibaoSuiPian;
import com.manu.dynasty.template.VIP;
import com.manu.dynasty.template.WuBeiFang;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.PD;
import com.manu.network.SessionManager;
import com.manu.network.SessionUser;
import com.qx.account.FunctionOpenMgr;
import com.qx.alliance.AllianceMgr;
import com.qx.alliance.AlliancePlayer;
import com.qx.alliance.building.JianZhuLvBean;
import com.qx.award.AwardMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.mibao.MiBaoDB;
import com.qx.mibao.MibaoMgr;
import com.qx.pawnshop.GoodsInfo;
import com.qx.persistent.HibernateUtil;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.pvp.PvpMgr;
import com.qx.task.DailyTaskMgr;
import com.qx.timeworker.FunctionID;
import com.qx.util.JsonUtils;
import com.qx.util.RandomUtil;
import com.qx.vip.VipData;
import com.qx.vip.VipMgr;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoInfo;
import com.qx.yuanbao.YuanBaoMgr;

import log.ActLog;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.PawnShop.PawnShopGoodsSell;
import qxmobile.protobuf.PawnShop.SellGoodsInfo;
import qxmobile.protobuf.Shop.BuyGoodReq;
import qxmobile.protobuf.Shop.BuyGoodResp;
import qxmobile.protobuf.Shop.ShopReq;
import qxmobile.protobuf.Shop.ShopResp;
import qxmobile.protobuf.Shop.WubeiFangAwardInfo;
import qxmobile.protobuf.Shop.WubeiFangBuy;
import qxmobile.protobuf.Shop.WubeiFangBuyResp;
import qxmobile.protobuf.Shop.WubeiFangInfo;
import qxmobile.protobuf.Shop.WubeiFangInfoResp;

public class ShopMgr extends EventProc {
	// 荒野商店
	public static final int huangYe_shop_type = 1;
	// 联盟商店
	public static final int lianMeng_shop_type = 2;
	// 联盟战商店
	public static final int lianmeng_battle_shop_type = 3;
	// 百战商店
	public static final int baizhan_shop_type=  4;
//	// 普通商店
	public static final int common_shop_type = 5;
	// 神秘商店
	public static final int mysterious_shop_type = 6;
	
	public int[] shopTypes = {lianMeng_shop_type, lianmeng_battle_shop_type, baizhan_shop_type,
								common_shop_type, mysterious_shop_type};
	
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
	public List<DangpuCommon> commonDangPuList;
	public Map<Integer, List<WuBeiFang>> wuBeiFangMapById;
	
	// 神秘商店
	public Map<Integer, List<BaseDuiHuan>>  mysteriousShopListMap= new HashMap<Integer, List<BaseDuiHuan>>();
	public Map<Integer, BaseDuiHuan> mysteriousShopMap = new HashMap<Integer, BaseDuiHuan>();

	public static final int shop_space = 10;
	
	// 神秘商店的购买类型： 0： 铜币购买， 1 元宝购买
	public static int tongBi_buy = 0;
	public static int yuanBao_buy = 1;
	
//	/** 目前当铺只能出售的物品类型 **/
	public static final int SELL_TYPE = 21;
	
	/**除了普通商店的物品，其他商店商品默认最大购买次数**/
	public static final int BUY_TIMES = 1;
	
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
		Map<Integer, BaseDuiHuan> hyShopMap = new HashMap<Integer, BaseDuiHuan>();
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
		this.hyShopMap = hyShopMap;
		
		List<LMGongXianDuihuan> list = TempletService
				.listAll(LMGongXianDuihuan.class.getSimpleName());
		Map<Integer, BaseDuiHuan> lmShopMap = new HashMap<Integer, BaseDuiHuan>();
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
		this.lmShopMap = lmShopMap;
		
		List<GongXunDuihuan> list2 = TempletService
				.listAll(GongXunDuihuan.class.getSimpleName());
		Map<Integer, BaseDuiHuan> gongxunShopMap = new HashMap<Integer, BaseDuiHuan>();
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
		this.gongxunShopMap = gongxunShopMap;
		
		List<Duihuan> list3 = TempletService
				.listAll(Duihuan.class.getSimpleName());
		Map<Integer, BaseDuiHuan> baiZhanShopMap = new HashMap<Integer, BaseDuiHuan>();
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
		this.baiZhanShopMap = baiZhanShopMap;
		commonDangPuList = TempletService
				.listAll(DangpuCommon.class.getSimpleName());
		Map<Integer, BaseDuiHuan> commonShopMap = new HashMap<Integer, BaseDuiHuan>();
		for (DangpuCommon dh : commonDangPuList) {
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
		this.commonShopMap = commonShopMap;
	
		List<Dangpu> list5 = TempletService
				.listAll(Dangpu.class.getSimpleName());
		Map<Integer, BaseDuiHuan> mysteriousShopMap = new HashMap<Integer, BaseDuiHuan>();
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
		this.mysteriousShopMap = mysteriousShopMap;
		
		List<WuBeiFang> wuBeiFangList = TempletService.listAll(WuBeiFang.class.getSimpleName());
		Map<Integer, List<WuBeiFang>> wuBeiFangMapById = new HashMap<>();
		for(WuBeiFang wbf : wuBeiFangList) {
			List<WuBeiFang> wbfList = wuBeiFangMapById.get(wbf.id);
			if(wbfList == null) {
				wbfList = new ArrayList<>();
				wuBeiFangMapById.put(wbf.id, wbfList);
			}
			wbfList.add(wbf);
		}
		this.wuBeiFangMapById = wuBeiFangMapById;
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
		
		// 协议里的操作
		int[] rightType = { 10, 20, 30, 40, 50, 60,
							11, 21, 31, 41, 61};
		boolean correctType = false;
		for(int tt : rightType) {
			if(tt == type) {
				correctType = true;
				break;
			}
		}
		ShopResp.Builder resp = ShopResp.newBuilder();
		if(!correctType) {
			logger.error("商店操作请求失败，请求的操作类型:{}不存在", type);
			resp.setMsg(2);
			session.write(resp.build());
			return;
		}
		boolean correctShopType = verifyShopType(bigType);
		if(!correctShopType) {
			logger.error("商店操作请求失败，请求的商店类型:{}不存在", bigType);
			resp.setMsg(2);
			session.write(resp.build());
			return;
		}
		
		PublicShop bean = HibernateUtil.find(PublicShop.class, jz.id * shop_space + bigType);
		if (bean == null) {
			bean = initShopInfo(jz.id, bigType);
		} else {
			// 检查是否更新
			resetHYShopBean(bean);
		}
		List<GoodsInfo> goods = getGoodsInfo(bean);
		int money = getMoney(bigType, jz, bean);
		/*
		 * 普通商店类型： 5，普通商店永不刷新，所以普通商店物品客户端处理显示
		 */
		/*
		 * type == X1: 花费money刷新商店商品列表
		 */
		if (type == 11 || type == 21 || type == 31 || type == 41 || type == 61) {
			int vipLimitCount = VipMgr.INSTANCE.getValueByVipLevel(jz.vipLevel,
					VipData.dangpuRefreshLimit);
			if(bean.buyNumber >= vipLimitCount){
				resp.setMsg(13);
				logger.info("玩家id{},姓名 {},商店类型：{}，用货币刷新商品列表失败：刷新次数用完", jz.id, jz.name, bigType);
				session.write(resp.build());
				return;
			}
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
			bean.lastResetShopTime = new Date();
			HibernateUtil.save(bean);
	
			logger.info("玩家id{},姓名 {},商店类型：{}， 用货币刷新商品列表，花费货币：{}", jz.id, jz.name,
					bigType, needYB);
			resp.setMsg(12);
		}else{
		
			/*
			 * type == X0: 请求商品兑换页面
			 */
			String s = bean.goodsInfo;
			if(type == 50 && s == null){ // 普通商店特殊处理（普通商店没有自动刷新和手动刷新功能）
				goods = getRandomGoodsList(bigType);
				bean.goodsInfo = setGoodsInfo(goods);
			}else{
				// 9点或者21点主动刷新
				if (s == null
						|| "".equals(s)
						|| System.currentTimeMillis() >= bean.nextAutoRefreshTime
								.getTime()) {
					// 自动刷新货物
					goods = getRandomGoodsList(bigType);
					bean.goodsInfo = setGoodsInfo(goods);
					bean.nextAutoRefreshTime = getNextNineTime(new Date());
				}
			}
			bean.openTime = new Date();
			HibernateUtil.save(bean);
			resp.setMsg(0);
		}
		fillDuiHuanInfo(bean, resp, goods, bigType);
		resp.setMoney(money);
		// 神秘商店也发送铜币数
		if(bigType == mysterious_shop_type){
			resp.setTongbi(jz.tongBi);
		}
		// 联盟商铺发送联盟商铺等级
		if(bigType == lianMeng_shop_type){
			AlliancePlayer player = HibernateUtil.find(AlliancePlayer.class, jz.id);
			if (player == null || player.lianMengId <= 0) {
				resp.setLmshopLv(0);
			}else{
				JianZhuLvBean jianzhu = HibernateUtil.find(JianZhuLvBean.class, player.lianMengId);
				resp.setLmshopLv(jianzhu == null ? 1: jianzhu.shangPuLv);
			}
		}
		session.write(resp.build());
	}

	public int getRefreshNeedHYMoney(PublicShop bean, int shop_type) {
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

		if(shop_type == common_shop_type){
			for(DangpuCommon dp : commonDangPuList) {
				GoodsInfo ginfo = new GoodsInfo();
				ginfo.setId(dp.id);
				ginfo.num = dp.getMax();
				goodsList.add(ginfo);
			}
			return goodsList;
		}else{
			
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
						// 其他商店默认1
						ginfo.num = dp.getMax();
						goodsList.add(ginfo);
						break;
					}
				}
			}
			return goodsList;
		}
	}

	public void resetHYShopBean(PublicShop bean) {
		if (DateUtils.isTimeToReset(bean.lastResetShopTime,
				CanShu.REFRESHTIME_PURCHASE)) {
			// 重置 刷新购买次数
			bean.buyNumber = 0;
			bean.lastResetShopTime = new Date();
			// 普通商店重置每类物品的购买次数
			if(bean.id % shop_space == common_shop_type){
				List<GoodsInfo> goodsL = getGoodsInfo(bean);
				for(GoodsInfo  g: goodsL){
					BaseDuiHuan d = commonShopMap.get(g.getId());
					if(d == null){
						logger.error("commonShopMap 找不到配置：id是：{}", g.getId());
						continue;
					}
					g.num = d.getMax();
				}
				bean.goodsInfo = setGoodsInfo(goodsL);
			}
			HibernateUtil.save(bean);
		}
	}

	public void fillDuiHuanInfo(PublicShop bean, ShopResp.Builder resp,
			List<GoodsInfo> list, int shop_type) {
		// 普通商店没有自动和手动刷新设置
		if(shop_type != common_shop_type){
			resp.setNextRefreshNeedMoney(getRefreshNeedHYMoney(bean, shop_type));
			int time = (int) (bean.nextAutoRefreshTime.getTime() - System
					.currentTimeMillis()) / 1000;
			time = time < 0 ? 0 : time;
			resp.setRemianTime(time);
		}
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
 			duihuan.setIsChange(goods.num == 0? false: true);
 			duihuan.setRemainCount(goods.num);
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
		
		BuyGoodResp.Builder resp = BuyGoodResp.newBuilder();
		boolean correctShopType = verifyShopType(bigType);
		if(!correctShopType) {
			logger.error("商店购买物品失败，请求的商店类型:{}不存在", bigType);
			resp.setMsg(5);
			session.write(resp.build());
			return;
		}

		BaseDuiHuan dh = getShopGoodsMap(bigType).get(goodId);
		if (dh == null) {
			logger.error("玩家{}， 商店类型:{}购买物品失败：BaseDuiHuan 子类表id是{}无数据", jz.id,
					bigType, goodId);
			return;
		}

		int money = dh.needNum;
		/*
		 * 联盟商店商品是否开启
		 */
		if(bigType == lianMeng_shop_type){
			int levle = 0;
			AlliancePlayer player = HibernateUtil.find(AlliancePlayer.class, jz.id);
			if(player == null || player.lianMengId <= 0) {
				logger.error("联盟商店购买失败，君主:{}还没有加入联盟", jz.id);
				return;
			}
			if (player != null && player.lianMengId > 0) {
				JianZhuLvBean jianzhu = HibernateUtil.find(JianZhuLvBean.class, player.lianMengId);
				levle = jianzhu == null ? 1: jianzhu.shangPuLv;
			}
			if(levle < dh.getNeedLv()){
				logger.info("玩家id{}, 购买联盟商店物品失败，联盟商店物品：{}没有开启", jz.id, goodId);
				resp.setMsg(4);
				session.write(resp.build());
				return;
			}
		}
		
		// 判断vip是否能买
		if(jz.vipLevel < dh.getVIP()){
			resp.setMsg(6);
			logger.info("玩家id{}, 购买商店物品：{}失败，vip等级不够，需要vip等级：{}", 
					jz.id, goodId, dh.getVIP());
			session.write(resp.build());
			return;
		}
//		/*
//		 * 普通商店物品
//		 */
//		if(bigType == 5){
//			boolean ok = PvpMgr.inst.isBuySuccess(jz, money, session,
//					YBType.YB_BUY_WUPIN, "元宝购买普通商店物品");
//			// 添加物品
//			if(ok){
//				AwardTemp a = new AwardTemp();
//				a.setId(111);
//				a.setItemId(dh.itemId);
//				a.setItemType(dh.itemType);
//				a.setItemNum(dh.itemNum);
//				AwardMgr.inst.giveReward(session, a, jz);
//
//				resp.setMsg(1);
//				resp.setRemianMoney(jz.yuanBao);
//				logger.info("玩家id{}, 用元宝：{}购买普通商店物品：{}成功", jz.id, money, goodId);
//			} else {
//				resp.setMsg(0);
//				logger.info("玩家id{}, 用元宝：{}购买普通商店物品:{}失败：元宝不足",jz.id, money, goodId);
//			}
//			session.write(resp.build());
//			return;
//		}
//		/*
//		 * 其他几种商店物品购买
//		 */
		int oldMoney = 0;
		
		PublicShop bean = HibernateUtil.find(PublicShop.class, jz.id * shop_space + bigType);
		if (bean == null) {
			bean = initShopInfo(jz.id, bigType);
		}else{
			resetHYShopBean(bean);
		}
		/*
		 * 20160328  首次购买做特殊处理
		 */
		if(bigType == mysterious_shop_type && dh.itemType == AwardMgr.type_fuWen 
				&& bean.buyGoodTimes == 0){
			money = 0;
		}else if(bigType == lianMeng_shop_type && bean.buyGoodTimes == 0){
			money = 0;
		}//end
		// 判断是否售罄
		List<GoodsInfo> goodsL = getGoodsInfo(bean);
		GoodsInfo buyg = null;
		for(GoodsInfo  g: goodsL){
			if(g.getId() == goodId){
				// 已经卖完
				if(g.num <= 0){
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
				HibernateUtil.update(jz);
				JunZhuMgr.inst.sendMainInfo(session,jz);
				oldMoney = jz.tongBi;
			}else if(bigType == mysterious_shop_type && dh.type == yuanBao_buy){
				// 普通商店元宝购买或者神秘商店元宝购买
				PvpMgr.inst.isBuySuccess(jz, money, session, YBType.YB_BUY_WUPIN, "元宝购买神秘商店物品");
				oldMoney = jz.yuanBao;
			}else if(bigType == common_shop_type){
				PvpMgr.inst.isBuySuccess(jz, money, session, YBType.YB_BUY_WUPIN, "元宝购买普通商店物品");
				oldMoney = jz.yuanBao;
			}else{
				// 其他类型购买
				oldMoney -= money;
				setMoney(bigType, jz.id, bean, oldMoney);
			}

			buyg.num -- ;
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
			resp.setIsChange(buyg.num == 0? false: true);
			logger.info("玩家id{},姓名 {}, 商店类型:{}用货币购买物品[{}]成功 货币{}", jz.id, jz.name,
					bigType, dh.itemId, money);
			String itemName = BagMgr.inst.getItemName(dh.itemId);
			ActLog.log.ChallengeExchange(jz.id, jz.name, ActLog.vopenid,
					dh.itemId, itemName, dh.itemNum, preV, oldMoney);
			if(bigType == baizhan_shop_type){
				// 主线任务: 消耗一次威望（在威望商店里购买1次物品）20190916
				EventMgr.addEvent(ED.pay_weiWang , new Object[] { jz.id});
				sendMainIfo(session, jz.id, ShopMgr.Money.weiWang);
			}
			// 购买的要是宝石，就判定是否有任务完成
			else if(bigType == mysterious_shop_type && a.getItemType() == AwardMgr.type_fuWen){
				EventMgr.addEvent(ED.pawnshop_buy, new Object[] { jz.id});
				logger.info("君主:{}在神秘商铺购买符文,符文id：{}成功, 判定是否有购买符文任务", jz.id, a.getItemId());
			}else if(bigType == lianMeng_shop_type){
				EventMgr.addEvent(ED.LM_SHOP_BUY, new Object[]{jz, a, itemName});
			}else if(bigType == huangYe_shop_type){
				sendMainIfo(session, jz.id, ShopMgr.Money.huangYeBi);
			}
		} else {
			/* 0：不足 */
			resp.setMsg(0);
			logger.info("玩家id{},姓名 {},  商店类型:{},goodId :{}用货币购买物品失败：货币不足",
					jz.id, jz.name, bigType, goodId);
		}
		session.write(resp.build());
	}

	public boolean verifyShopType(int bigType) {
		boolean correctShopType = false;
		for(int type : shopTypes) {
			if(type == bigType) {
				correctShopType = true;
				break;
			}
		}
		return correctShopType;
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
		case common_shop_type:
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
	
	public void sellGoods(int cmd, IoSession session, Builder builder) {
		// 检查君主
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		PawnShopGoodsSell.Builder request = (qxmobile.protobuf.PawnShop.PawnShopGoodsSell.Builder) builder;
		List<SellGoodsInfo> sellList = request.getSellGinfoList();
		Map<Long, BagGrid> rightData = new HashMap<Long, BagGrid>();
		Map<Long, MiBaoDB> mibaoData = new HashMap<Long, MiBaoDB>();
	
		// 验证数据
		Bag<BagGrid> bag = null;
		long bagId = 0;
		int sellCount = 0;
		int goodsType = 0;
		List<BagGrid> gridList = new ArrayList<BagGrid>();
	
		for (SellGoodsInfo sginfo : sellList) {
			bagId = sginfo.getBagId();
			sellCount = sginfo.getCount();
			goodsType = sginfo.getGoodsType();
			if(goodsType == 1){// 背包物品
				if(bag == null){ //加载背包数据
					bag = BagMgr.inst.loadBag(junZhu.id);
					if(bag != null) gridList = bag.grids;
				}
				boolean has = false;
				for (BagGrid bg : gridList) {
					if (bg.getIdentifier() == bagId) {
						has = true;
						ItemTemp item = TempletService.getInstance().getItemTemp(
								bg.itemId);
						if (item == null) {
							logger.error("itemTemp表获取不到该物品，传过来的itemId:{}",
									bg.itemId);
							//sendError(cmd, session, "典当物品操作失败");
							return;
						}

						int itemType = item.getItemType();
						if (itemType != SELL_TYPE) {
							logger.error("当铺出售的物品类型错误，itemType:{}", itemType);
							//sendError(cmd, session, "当铺只能出售玉诀");
							return;
						}
		
						if (bg.cnt < sellCount) {
							logger.error("背包里没有足够的物品itemId:{},拥有数量:{},出售数量:{}",
									bg.itemId,bg.cnt,sellCount);
							//sendError(cmd, session, "典当物品操作失败");
							return;
						}
						rightData.put(bagId, bg);
						break;
					}
				}
				if (!has) {
					logger.error("没有找到bag数据，bagDBid:{}", bagId);
					//sendError(cmd, session, "典当物品操作失败");
					return;
				}
			}else if(goodsType == 2){ // 秘宝碎片
				MiBaoDB m = HibernateUtil.find(MiBaoDB.class, bagId);
				if(m == null){
					logger.error("指定典当秘宝碎片不存在dbId:{}", bagId);
					//sendError(cmd, session, "指定典当秘宝碎片不存在");
					return;
				}
				if(m.getSuiPianNum() < sellCount){
					logger.error("指定典当秘宝碎片数量不足,数量有:{},出售数量:{}", m.getSuiPianNum(), sellCount);
					//sendError(cmd, session, "指定典当秘宝碎片数量不足");
					return;
				}
				if(m.getStar() < MibaoMgr.mibao_first_full_star){
					logger.error("指定典当秘宝碎片星级不足,碎片星级:{},需要达到星级{}", m.getStar(), MibaoMgr.mibao_first_full_star);
					//sendError(cmd, session, "指定典当秘宝碎片星级不足");
					return;
				}
				mibaoData.put(bagId, m);
			}else{
				logger.error("出售的物品类型错误，goodsType:{}", goodsType);
				//sendError(cmd, session, "典当物品操作失败");
				return;
			}
		}
	
		// 出售
		Date date = new Date();
		boolean selBagGoods = false;
		boolean sellMibaoS = false;
		for (SellGoodsInfo sginfo : sellList) {
			bagId = sginfo.getBagId();
			sellCount = sginfo.getCount();
			goodsType = sginfo.getGoodsType();
			if(goodsType == 1){
				BagGrid bagGrid = rightData.get(bagId);
				if (bagGrid == null) {
					logger.error("bagGrid为null， bagId:{}", bagId);
					continue;
				}
				ItemTemp itemTemp = TempletService.getInstance().getItemTemp(
						bagGrid.itemId);
				int sellPrice = itemTemp.getSellNum();
				int totalSellPrice = sellCount * sellPrice;
				// 改为铜币， 20160126
//				YuanBaoMgr.inst.diff(junZhu, totalSellPrice, 0, sellPrice,
//						YBType.YB_PAWN_SELL, "当铺出售商品");
				junZhu.tongBi += totalSellPrice;
				// 扣除背包物品
				BagMgr.inst.removeItem(bag, bagGrid.itemId, sellCount, "当铺典当物品",junZhu.level);
			
				logger.info("君主:{}在当铺典当物品itemId:{},数量:{},时间:{},获得铜币:{}",
						junZhu.name, bagGrid.itemId, sellCount, date,
						totalSellPrice);
				ActLog.log.Pawn(junZhu.id, junZhu.name, ActLog.vopenid, bagGrid.itemId, itemTemp.getName(), sellCount, 0, totalSellPrice);
				selBagGoods = true;
			}else if(goodsType == 2){
				MiBaoDB m = mibaoData.get(bagId);
				if(m == null){
					continue;
				}
				MibaoSuiPian s =   MibaoMgr.inst.mibaoSuipianMap.get(m.getTempId());
				if(s == null){
					continue;
				}
				int tong = sellCount * s.recyclePrice;
				junZhu.tongBi += tong;
				m.setSuiPianNum(m.getSuiPianNum() - sellCount);
				HibernateUtil.save(m);
				logger.info("君主:{}在当铺典当秘宝碎片，碎片item:{},数量:{},时间:{},获得铜币:{}",
						junZhu.name, m.getTempId(), sellCount, date, tong);
				ActLog.log.Pawn(junZhu.id, junZhu.name, ActLog.vopenid, m.getTempId(), "秘宝碎片", sellCount, 0, tong);
				sellMibaoS = true;
			}
		}
		HibernateUtil.update(junZhu);
		JunZhuMgr.inst.sendMainInfo(session,junZhu);
		session.write(PD.PAWN_SHOP_GOODS_SELL_OK);

		if(selBagGoods) BagMgr.inst.sendBagInfo(session, bag);
		if(sellMibaoS) MibaoMgr.inst.mibaoInfosRequest(PD.C_MIBAO_INFO_REQ, session);
	}

	public void sendError(int cmd, IoSession session, String msg) {
		if (session == null) {
			logger.warn("session is null: {}", msg);
			return;
		}
		ErrorMessage.Builder test = ErrorMessage.newBuilder();
		test.setErrorCode(cmd);
		test.setErrorDesc(msg);
		session.write(test.build());
	}

	public void sendShopRed(JunZhu jz, IoSession session, int funcId){
		if(funcId == FunctionID.lianMeng_shop || funcId == FunctionID.huangYe_shop){
			AlliancePlayer p = HibernateUtil.find(AlliancePlayer.class, jz.id);
			if(p ==null || p.lianMengId<=0) {
				return;
			}
		}
		boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(funcId, 
				jz.id, jz.level);
		if(!isOpen){
			return;
		}
		FunctionID.pushCanShowRed(jz.id, session, funcId);
	}

	@Override
	public void proc(Event e) {
		switch(e.id){
		case ED.ACC_LOGIN:
			if (e.param != null && e.param instanceof Long) {
				long jzid = (Long) e.param;
				JunZhu junZhu = HibernateUtil.find(JunZhu.class, jzid);
				if (junZhu == null) {
					break;
				}
				SessionUser su = SessionManager.inst.findByJunZhuId(junZhu.id);
				if(su == null){
					break;
				}
				// 发送红点信息
				sendRedNotice(jzid, junZhu.level, su.session);
				// 发送威望值
				sendMainIfo(su.session,jzid,ShopMgr.Money.weiWang);
				// 发送荒野币
				sendMainIfo(su.session,jzid,ShopMgr.Money.huangYeBi);
				break;
			}
		}
	}

	public void sendRedNotice(long jzid, int level, IoSession session){
		long start = jzid * shop_space;
		long end = start + shop_space;
		List<PublicShop> list = HibernateUtil.list(PublicShop.class, 
				"where id>="+start+" and id<"+end);
		Date now = new Date();
		for(PublicShop shop: list){
			int type = (int)shop.id % shop_space;
			//不具有刷新功能
			if(type == lianmeng_battle_shop_type || type == common_shop_type){
				continue;
			}
			// 无联盟 不开商店
			if(type == lianMeng_shop_type || type == huangYe_shop_type){
				AlliancePlayer p = HibernateUtil.find(AlliancePlayer.class, jzid);
				if(p ==null || p.lianMengId<=0){
					continue;
				}
			}
			int funcid = getFuncId(type);
			boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(funcid, jzid, level);
			if(!isOpen){
				continue;
			}
			if(shop.openTime == null || isSendRed(shop.openTime, now)){
				FunctionID.pushCanShowRed(jzid, session, funcid);
			}
		}
	}
	
	public void sendMainIfo(IoSession session, long jzid , ShopMgr.Money m){
		int money = getMoney(m, jzid, null);
		switch(m){
		case huangYeBi:
			DailyTaskMgr.INSTANCE.sendError(session, money, PD.huangyeBi, money);
			break;
		case gongXun:
			DailyTaskMgr.INSTANCE.sendError(session, money, PD.gongxun, money);
			break;
		case weiWang:
			DailyTaskMgr.INSTANCE.sendError(session, money, PD.weiWang, money);
		default:
			break;
		}
	}
	
	public void sendWeiWang(IoSession session, long jzid){
		int money = getMoney(ShopMgr.Money.weiWang, jzid, null);
		DailyTaskMgr.INSTANCE.sendError(session, money, PD.weiWang, money);
	}
	
	public void sendHangYebi(IoSession session, long jzid){
		int money = getMoney(ShopMgr.Money.huangYeBi, jzid, null);
		DailyTaskMgr.INSTANCE.sendError(session, money, PD.huangyeBi, money);
	}
	
	public boolean isSendRed(Date lastopenTime, Date now){
		int nowhour = DateUtils.getHourOfDay(now);
		int lasthour = DateUtils.getHourOfDay(lastopenTime);
		if(DateUtils.isSameDay(lastopenTime, now)){
			if(0<=lasthour && lasthour<9 && 0<=nowhour && nowhour<9){
				return false;
			}
			if(9<=lasthour && lasthour<21 && 9<=nowhour && nowhour<21){
				return false;
			}
			if(21<=lasthour && lasthour<=23 && 21<=nowhour && nowhour<=23){
				return false;
			}
			//注意参数顺序第一个参数时间 大于第二个参数
		}else if(DateUtils.isBeforeDay(now.getTime(), lastopenTime.getTime())){
			if(lasthour>=21 && nowhour <9){
				return false;
			}
		}
		return true;
	}
	public int getFuncId(int type){
		int funcid = 0;
		switch(type){
		case mysterious_shop_type:
			funcid = FunctionID.mysterious_shop;
			break;
		case baizhan_shop_type:
			funcid = FunctionID.baizhan_shop;
			break;
		case lianMeng_shop_type:
			funcid = FunctionID.lianMeng_shop;
			break;
		case huangYe_shop_type:
			funcid = FunctionID.huangYe_shop;
			break;
		}
		return funcid;
	}
	@Override
	protected void doReg() {
		EventMgr.regist(ED.ACC_LOGIN, this);
	}

	public void requestWuBeiFangInfo(int cmd, Builder builder, IoSession session) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		WuBeiFangBean wuBeiFang = getWuBeiFangBean(junZhu.id);
		if(DateUtils.isTimeToReset(wuBeiFang.lastBuyTime, CanShu.REFRESHTIME)) {
			wuBeiFang.type1UseTimes = 0;
			wuBeiFang.type2UseTimes = 0;
			wuBeiFang.type3UseTimes = 0;
			wuBeiFang.type4UseTimes = 0;
			HibernateUtil.save(wuBeiFang);
		}
		WubeiFangInfoResp.Builder response = WubeiFangInfoResp.newBuilder();
		
		for(Integer type : wuBeiFangMapById.keySet()) {
			WubeiFangInfo.Builder wubeiFangBuilder = WubeiFangInfo.newBuilder();
			wubeiFangBuilder.setType(type);
			int remainFreeTimes = getRemainFreeTimes(type, wuBeiFang);
			wubeiFangBuilder.setFreeTimes(remainFreeTimes);
			wubeiFangBuilder.setCostYuanbao(getCostYuanBao(type, wuBeiFang));
			wubeiFangBuilder.setRemainYuanbaoBuyTimes(getRamainYuanBaoTimes(type, wuBeiFang, junZhu, remainFreeTimes));
			response.addWubeiFangInfoList(wubeiFangBuilder);
		}
		session.write(response.build());
	}

	public int getCostYuanBao(Integer type, WuBeiFangBean wuBeiFang) {
		//type类型(与配置文件必须一样):1：装备铺,2：珍宝行,3：石料店,4：益精堂
		int totalTimes = getWuBeiFangTypeUseTimesToday(wuBeiFang, type);
		WuBeiFang beiFang = getWuBeiFangConfig(type, totalTimes + 1);
		if(beiFang == null) {
			logger.error("配置文件查找错误，返回一个较大的元宝数量");
			return 10000;
		}
		return beiFang.cost;
	}

	public WuBeiFang getWuBeiFangConfig(Integer type, int totalTimes) {
		List<WuBeiFang> wbfList = wuBeiFangMapById.get(type);
		if(wbfList == null || wbfList.size() == 0) {
			logger.error("获取武备坊花费元宝失败，类型:{}", type);
			return null;
		}
		WuBeiFang beiFang = null;
		for(WuBeiFang wbf : wbfList) {
			if(wbf.times == totalTimes) {
				beiFang = wbf;
				break;
			}
		}
		if(beiFang == null) {
			logger.error("获取武备坊花费元宝失败，类型:{}，次数:{}", type, totalTimes);
			return null;
		}
		return beiFang;
	}

	public int getRamainYuanBaoTimes(Integer type, WuBeiFangBean wuBeiFang, JunZhu junZhu, int remainFreeTimes) {
		VIP vipCfg = VipMgr.INSTANCE.getVIPByVipLevel(junZhu.vipLevel);
		if(vipCfg == null) {
			logger.error("获取武备坊剩余次数失败，找不到vip等级为:{}的配置文件", junZhu.vipLevel);
			return 0;
		}
		int times = 0;
		//type类型(与配置文件必须一样):1：装备铺,2：珍宝行,3：石料店,4：益精堂
		switch(type) {
			case 1:
				if(wuBeiFang.type1UseTimes - CanShu.EQUIPMENT_FREETIMES >= 0) {
					times = vipCfg.buyEquipment - (wuBeiFang.type1UseTimes - CanShu.EQUIPMENT_FREETIMES);
				} else {
					times = vipCfg.buyEquipment;
				}
				break;
			case 2:
				if(wuBeiFang.type2UseTimes - CanShu.BAOSHI_FREETIMES >= 0) {
					times = vipCfg.buyBaoshi - (wuBeiFang.type2UseTimes - CanShu.BAOSHI_FREETIMES);
				} else {
					times = vipCfg.buyBaoshi;
				}
				break;
			case 3:
				if(wuBeiFang.type3UseTimes - CanShu.QIANGHUA_FREETIMES >= 0) {
					times = vipCfg.buyQianghua - (wuBeiFang.type3UseTimes - CanShu.QIANGHUA_FREETIMES);
				} else {
					times = vipCfg.buyQianghua;
				}
				break;
			case 4:
				if(wuBeiFang.type4UseTimes - CanShu.JINGQI_FREETIMES >= 0) {
					times = vipCfg.buyJingqi - (wuBeiFang.type4UseTimes - CanShu.JINGQI_FREETIMES);
				} else {
					times = vipCfg.buyJingqi;
				}
				break;
			default:
				logger.error("获取武备坊今日免费剩余次数失败，找不到type:{}", type);
				break;
		}
		times = times < 0 ? 0 : times;
		return times;
	}

	public int getRemainFreeTimes(Integer type, WuBeiFangBean wuBeiFang) {
		int freeTimesRemain = 0;
		int todayUseTimes = getWuBeiFangTypeUseTimesToday(wuBeiFang, type);
		if(todayUseTimes == -1) {
			return 0;
		}
		//type类型(与配置文件必须一样):1：装备铺,2：珍宝行,3：石料店,4：益精堂
		switch(type) {
			case 1:
				freeTimesRemain = CanShu.EQUIPMENT_FREETIMES - wuBeiFang.type1UseTimes;
				break;
			case 2:
				freeTimesRemain = CanShu.BAOSHI_FREETIMES - wuBeiFang.type2UseTimes;
				break;
			case 3:
				freeTimesRemain = CanShu.QIANGHUA_FREETIMES - wuBeiFang.type3UseTimes;
				break;
			case 4:
				freeTimesRemain = CanShu.JINGQI_FREETIMES - wuBeiFang.type4UseTimes;
				break;
			default:
				logger.error("获取武备坊今日免费剩余次数失败，找不到type:{}", type);
				break;
		}
		freeTimesRemain = freeTimesRemain < 0 ? 0 : freeTimesRemain;
		return freeTimesRemain;
	}

	public int getWuBeiFangTypeUseTimesToday(WuBeiFangBean wuBeiFang, Integer type) {
		int todayUseTimes = -1;
		//type类型(与配置文件必须一样):1：装备铺,2：珍宝行,3：石料店,4：益精堂
		switch(type) {
			case 1:
				todayUseTimes = wuBeiFang.type1UseTimes;
				break;
			case 2:
				todayUseTimes = wuBeiFang.type2UseTimes;
				break;
			case 3:
				todayUseTimes = wuBeiFang.type3UseTimes;
				break;
			case 4:
				todayUseTimes = wuBeiFang.type4UseTimes;
				break;
			default:
				logger.error("获取武备坊今日使用次数失败，找不到type:{}", type);
				break;
		}
		return todayUseTimes;
	}

	public WuBeiFangBean getWuBeiFangBean(long junZhuId) {
		WuBeiFangBean wuBeiFang = HibernateUtil.find(WuBeiFangBean.class, junZhuId);
		if(wuBeiFang == null) {
			wuBeiFang = new WuBeiFangBean();
			wuBeiFang.junzhuId = junZhuId;
			HibernateUtil.insert(wuBeiFang);
		}
		return wuBeiFang;
	}

	public void buyWuBeiFang(int cmd, Builder builder, IoSession session) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		WubeiFangBuy.Builder request = (qxmobile.protobuf.Shop.WubeiFangBuy.Builder) builder;
		int type = request.getType();
		
		WuBeiFangBean wuBeiFang = getWuBeiFangBean(junZhu.id);
		int remainFreeTimes = getRemainFreeTimes(type, wuBeiFang);
		boolean free = false;
		if(remainFreeTimes > 0) {
			free = true;
		} 
		
		WubeiFangBuyResp.Builder response = WubeiFangBuyResp.newBuilder();
		int totalTimes = getWuBeiFangTypeUseTimesToday(wuBeiFang, type);
		WuBeiFang beiFang = getWuBeiFangConfig(type, totalTimes + 1);
		if(beiFang == null) {
			response.setResult(3);
			session.write(response.build());
			logger.error("配置文件查找错误，返回一个较大的元宝数量");
			return;
		}
		
		int needCost = getCostYuanBao(type, wuBeiFang);
		if(!free) {
			int remainYuanBaoTimes = getRamainYuanBaoTimes(type, wuBeiFang, junZhu, remainFreeTimes);
			if(remainYuanBaoTimes <= 0) {
				response.setResult(2);
				session.write(response.build());
				logger.error("武备坊购买失败，今日次数用完，vip等级:{} 今日所用次数:{}", junZhu.vipLevel, totalTimes);
				return;
			}
			if(junZhu.yuanBao < needCost) {
				response.setResult(1);
				session.write(response.build());
				logger.error("武备坊购买失败，元宝不足vip等级:{} 今日所用次数:{},有元宝:{}",junZhu.vipLevel, totalTimes, junZhu.yuanBao);
				return;
			}
		}
		wuBeiFang.lastBuyTime = new Date();
		changeTimes(wuBeiFang, type, 1);
		HibernateUtil.save(wuBeiFang);
		
		List<AwardTemp> awardList = AwardMgr.inst.getHitAwardList(beiFang.awardID, ",", "=");
		logger.info("武备坊购买成功，君主:{}购买的type:{},获得:{}", junZhu.id, type, awardList);
		for(AwardTemp award : awardList) {
			WubeiFangAwardInfo.Builder awardBuilder = WubeiFangAwardInfo.newBuilder();
			awardBuilder.setItemType(award.getItemType());
			awardBuilder.setItemId(award.getItemId());
			awardBuilder.setItemNum(award.getItemNum());
			response.addAwardList(awardBuilder);
		}
		
		WubeiFangInfo.Builder wubeiFangBuilder = WubeiFangInfo.newBuilder();
		wubeiFangBuilder.setType(type);
		int newFreeTimes = getRemainFreeTimes(type, wuBeiFang);
		wubeiFangBuilder.setFreeTimes(newFreeTimes);
		wubeiFangBuilder.setCostYuanbao(getCostYuanBao(type, wuBeiFang));
		wubeiFangBuilder.setRemainYuanbaoBuyTimes(getRamainYuanBaoTimes(type, wuBeiFang, junZhu, newFreeTimes));
		response.setWubeiFangInfo(wubeiFangBuilder);
		
		response.setResult(0);
		session.write(response.build());
		for(AwardTemp award : awardList) {
			AwardMgr.inst.giveReward(session, award, junZhu, false, false);
		}
		if(!free) {
			YuanBaoMgr.inst.diff(junZhu, -needCost, 0, 0, 0, "进行武备坊购买");
		}
		JunZhuMgr.inst.sendMainInfo(session);
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		BagMgr.inst.sendBagInfo(session, bag);
		
		EventMgr.addEvent(ED.done_wuBeiChouJiang, new Object[]{junZhu.id});
	}

	public void changeTimes(WuBeiFangBean wuBeiFang, int type, int times) {
		//type类型(与配置文件必须一样):1：装备铺,2：珍宝行,3：石料店,4：益精堂
		switch(type) {
			case 1:
				wuBeiFang.type1UseTimes += times;
				break;
			case 2:
				wuBeiFang.type2UseTimes += times;
				break;
			case 3:
				wuBeiFang.type3UseTimes += times;
				break;
			case 4:
				wuBeiFang.type4UseTimes += times;
				break;
			default:
				logger.error("修改武备坊今日使用次数失败，找不到type:{}", type);
				break;
		}
	}
	

}
