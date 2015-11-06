package com.qx.pawnshop;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import log.ActLog;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.PawnShop.PawnShopGoodsSell;
import qxmobile.protobuf.PawnShop.PawnshopGoodsBuy;
import qxmobile.protobuf.PawnShop.PawnshopGoodsBuyResp;
import qxmobile.protobuf.PawnShop.PawnshopGoodsList;
import qxmobile.protobuf.PawnShop.PawnshopRefeshResp;
import qxmobile.protobuf.PawnShop.SellGoodsInfo;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.AwardTemp;
import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.Dangpu;
import com.manu.dynasty.template.DangpuCommon;
import com.manu.dynasty.template.ItemTemp;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.qx.alliance.AlliancePlayer;
import com.qx.award.AwardMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.purchase.PurchaseConstants;
import com.qx.purchase.PurchaseMgr;
import com.qx.util.JsonUtils;
import com.qx.util.RandomUtil;
import com.qx.vip.VipData;
import com.qx.vip.VipMgr;
import com.qx.yuanbao.YBType;
import com.qx.yuanbao.YuanBaoMgr;

public class PawnshopMgr {
	protected Logger logger = LoggerFactory.getLogger(PawnshopMgr.class);

	public static PawnshopMgr inst;

	/** 有联盟玩家随机出物品列表<位置id，dangpu信息> **/
	public Map<Integer, List<Dangpu>> allianceGoodsMap;

	/** 无联盟玩家随机出物品列表<位置id，dangpu信息> **/
	public Map<Integer, List<Dangpu>> nonAllianceGoodsMap;

	public List<DangpuCommon> commonGoodsList;

	/** 无联盟玩家可以获得物品的flag标志 **/
	public static final int FLAG = 1;
	public static final int YUAN_BAO = 1;
	public static final int GONG_XIAN = 2;
	public static final int TONG_BI = 0;
	/** 目前当铺只能出售的物品类型 **/
	public static final int SELL_TYPE = 21;
	/** 当铺随机概率权重和 **/
	public static final int GAILV_WEIGHT_SUM = 10000;

	public PawnshopMgr() {
		inst = this;
		initData();
	}

	// map按值比较器
	protected class MapKeyComparator implements Comparator<Integer> {
		@Override
		public int compare(Integer o1, Integer o2) {
			int i = o1;
			int j = o2;
			if (i > j) {
				return 1;
			} else if (i < j) {
				return -1;
			}
			return 0;
		}
	}

	public void initData() {
		List<Dangpu> dangpuList = TempletService.listAll(Dangpu.class
				.getSimpleName());
		if (dangpuList == null) {
			logger.error("dangpu配置文件加载出错");
			return;
		}
		Map<Integer, List<Dangpu>> allianceGoodsMap = new TreeMap<Integer, List<Dangpu>>(
				new MapKeyComparator());
		Map<Integer, List<Dangpu>> nonAllianceGoodsMap = new TreeMap<Integer, List<Dangpu>>(
				new MapKeyComparator());
		for (Dangpu dangpu : dangpuList) {
			int site = dangpu.getSite();
			// 有联盟
			List<Dangpu> haveAlliace = allianceGoodsMap.get(site);
			if (haveAlliace == null) {
				haveAlliace = new ArrayList<Dangpu>();
				allianceGoodsMap.put(site, haveAlliace);
			}
			haveAlliace.add(dangpu);

			// 无联盟
			List<Dangpu> nonAlliace = nonAllianceGoodsMap.get(site);
			if (nonAlliace == null) {
				nonAlliace = new ArrayList<Dangpu>();
				nonAllianceGoodsMap.put(site, nonAlliace);
			}
			if (dangpu.getFlag() == FLAG) {
				nonAlliace.add(dangpu);
			}
		}
		this.allianceGoodsMap = allianceGoodsMap;
		this.nonAllianceGoodsMap = nonAllianceGoodsMap;

		List<DangpuCommon> dangpuCommonList = TempletService
				.listAll(DangpuCommon.class.getSimpleName());
		List<DangpuCommon> commonGoodsList = new ArrayList<DangpuCommon>();
		for (DangpuCommon dpc : dangpuCommonList) {
			commonGoodsList.add(dpc);
		}
		Collections.sort(commonGoodsList);
		this.commonGoodsList = commonGoodsList;
	}

	public void getGoodsList(int cmd, IoSession session) {
		// 检查君主
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		// 先从数据库获取物品
		PawnshopBean shopBean = HibernateUtil.find(PawnshopBean.class, junZhu.id);
		List<GoodsInfo> goodsList = new ArrayList<GoodsInfo>();
		Date date = new Date();
		if (shopBean == null) {
			shopBean = new PawnshopBean();
			goodsList = getGoodsList(junZhu);
			shopBean.setJunzhuId(junZhu.id);
			shopBean.setLastRefreshTime(date);
			shopBean.setGoodsInfo(JsonUtils.listToStr(goodsList));
			shopBean.setAutoRefreshTime(date);
			logger.info("首次请求当铺物品信息, junzhuId:{}，时间:{}", junZhu.id, date);
			HibernateUtil.insert(shopBean);
		} else {
			// 是否到刷新时间点
			boolean isRefresh = isRefresh(shopBean, date);
			if (isRefresh) {
				goodsList = getGoodsList(junZhu);
				shopBean.setGoodsInfo(JsonUtils.listToStr(goodsList));
				shopBean.setAutoRefreshTime(date);
				logger.info("自动刷新当铺物品,junzhuId:{}，时间:{}", junZhu.id, date);
				HibernateUtil.save(shopBean);
			} else {
				goodsList = JsonUtils.strToList(shopBean.getGoodsInfo(), GoodsInfo.class);
			}
		}

		// change 20150901
		if(DateUtils.isTimeToReset(shopBean.getLastRefreshTime(), CanShu.REFRESHTIME_PURCHASE)){
			shopBean.setRefreshTimes(0);
			HibernateUtil.save(shopBean);
		}
//		if (!DateUtils.isSameDay(date, shopBean.getLastRefreshTime())) {
//			shopBean.setRefreshTimes(0);
//			HibernateUtil.save(shopBean);
//		}
		int refreshTimes = shopBean.getRefreshTimes();
		int needYuanBao = PurchaseMgr.inst.getNeedYuanBao(
				PurchaseConstants.PAWNSHOP_REFRESH, refreshTimes + 1);
		int seconds = getRefreshRemainSeconds(shopBean.getLastRefreshTime(),
				date);
		// 返回物品列表
		sendShopGoodsInfo(session, goodsList, needYuanBao, seconds, junZhu.id);
	}

	protected void sendShopGoodsInfo(IoSession session,List<GoodsInfo> goodsList, 
			int needYuanBao, int seconds, long junzhuId) {
		PawnshopGoodsList.Builder response = PawnshopGoodsList.newBuilder();
		// 神秘物品
		for (GoodsInfo goods : goodsList) {
			qxmobile.protobuf.PawnShop.GoodsInfo.Builder ginfo = qxmobile.protobuf.PawnShop.GoodsInfo
					.newBuilder();
			ginfo.setItemId(goods.getId());
			ginfo.setIsSoldOut(goods.isSell());
			response.addSecretInfo(ginfo);
		}
		// 普通物品
		for (DangpuCommon common : commonGoodsList) {
			qxmobile.protobuf.PawnShop.GoodsInfo.Builder ginfo = qxmobile.protobuf.PawnShop.GoodsInfo
					.newBuilder();
			ginfo.setItemId(common.getId());
			ginfo.setIsSoldOut(false);
			response.addNormalInfo(ginfo);
		}
		AlliancePlayer aplayer = HibernateUtil.find(AlliancePlayer.class, junzhuId);
		if(aplayer != null) {
			response.setGongXian(aplayer.gongXian);
		} else {
			response.setGongXian(0);
		}
		response.setRefreshCost(needYuanBao);
		response.setRefreshTime(seconds);
		session.write(response.build());
	}

	/**
	 * 是否到刷新时间点
	 * 
	 * @param shopBean
	 * @param date
	 * @return
	 */
	protected boolean isRefresh(PawnshopBean shopBean, Date date) {
		boolean isRefresh = false;
		Date lastRefreshTime = shopBean.getAutoRefreshTime();
		if(lastRefreshTime == null) {
			lastRefreshTime = date;
		}
		String timeStr = CanShu.REFRESHTIME_DANGPU;
		String[] splitRetime = timeStr.split(",");
		Calendar cal = Calendar.getInstance();
		for (String s : splitRetime) {
			String[] ss = s.split(":");
			int h = Integer.parseInt(ss[0]);
			int m = Integer.parseInt(ss[1]);
			cal.set(Calendar.HOUR_OF_DAY, h);
			cal.set(Calendar.MINUTE, m);
			cal.set(Calendar.SECOND, 0);
			Date d = cal.getTime();
			// 上次刷新的时间在给定之前，当前时间在给定时间之后或相同，则应该刷新当铺物品
			if (lastRefreshTime.before(d) && (date.after(d) || date.equals(d))) {
				isRefresh = true;
			}
		}
		return isRefresh;
	}

	protected int getRefreshRemainSeconds(Date last, Date now) {
		int second = -1;
		Calendar cal = Calendar.getInstance();
		second = getSeconds(cal, last, now);
		if (second == -1) {
			int day = cal.get(Calendar.DAY_OF_YEAR);
			cal.set(Calendar.DAY_OF_YEAR, day + 1);
			second = getSeconds(cal, last, now);
		}
		return second;
	}

	protected int getSeconds(Calendar cal, Date last, Date now) {
		int second = -1;
		String timeStr = CanShu.REFRESHTIME_DANGPU;
		String[] splitRetime = timeStr.split(",");
		for (String s : splitRetime) {
			String[] ss = s.split(":");
			int h = Integer.parseInt(ss[0]);
			int m = Integer.parseInt(ss[1]);
			cal.set(Calendar.HOUR_OF_DAY, h);
			cal.set(Calendar.MINUTE, m);
			cal.set(Calendar.SECOND, 0);
			Date d = cal.getTime();
			// 上次刷新的时间在给定之前 & 当前时间在给定时间前或相同
			if (now.before(d) || now.equals(d)) {
				second = (int) ((d.getTime() - now.getTime()) / 1000);
			}
		}
		return second;
	}

	/**
	 * 随机获取当铺物品
	 * 
	 * @param junZhu
	 * @return
	 */
	protected List<GoodsInfo> getGoodsList(JunZhu junZhu) {
		Map<Integer, List<Dangpu>> map = new TreeMap<Integer, List<Dangpu>>(
				new MapKeyComparator());
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class,
				junZhu.id);
		if (member == null || member.lianMengId <= 0) {
			map.putAll(nonAllianceGoodsMap);
		} else {
			map.putAll(allianceGoodsMap);
		}
		List<GoodsInfo> goodsList = new ArrayList<GoodsInfo>();
		for (Map.Entry<Integer, List<Dangpu>> entry : map.entrySet()) {
			List<Dangpu> list = entry.getValue();
			int gailv = RandomUtil.getRandomNum(GAILV_WEIGHT_SUM);
			int sum = 0;
			for (Dangpu dp : list) {
				sum += dp.getWeight();
				if (gailv < sum) {
					GoodsInfo ginfo = new GoodsInfo();
					ginfo.setId(dp.getId());
					ginfo.setSell(false);
					goodsList.add(ginfo);
					break;
				}
			}
		}
		return goodsList;
	}

	public void buyGoods(int cmd, IoSession session, Builder builder) {
		// 检查君主
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}
		PawnshopGoodsBuy.Builder request = (qxmobile.protobuf.PawnShop.PawnshopGoodsBuy.Builder) builder;
		int dangpuId = request.getItemId();
		int type = request.getType();
		switch (type) {
		case 0:// 购买普通物品
			buyCommon(cmd, session, junZhu, dangpuId);
			break;
		case 1:// 购买神秘物品
			buyMysterious(cmd, session, junZhu, dangpuId);
			break;
		default:
			break;
		}

	}

	protected void buyCommon(int cmd, IoSession session, JunZhu junZhu,
			int dangpuId) {
		DangpuCommon commonCfg = null;
		for(DangpuCommon common : commonGoodsList) {
			if(dangpuId == common.getId()) {
				commonCfg = common;
				break;
			}
		}
		if (commonCfg == null) {
			logger.error("未找到当铺里id为:{}的普通物品", dangpuId);
			return;
		}
		if(commonCfg.getItemId() == AwardMgr.ITEM_HU_FU_ID) {
			if(!VipMgr.INSTANCE.isVipPermit(VipData.buy_huFu, junZhu.vipLevel)) {
				logger.error("购买当铺普通物品id:{}是虎符，vip等级不足",  commonCfg.getItemId());
				sendPawnshopGoodsBuyResp(session, 4, 0, dangpuId, -1, junZhu.id);
				return;
			}
		}
		if (junZhu.yuanBao < commonCfg.getNeedNum()) {
			logger.error("购买当铺普通物品id:{}，元宝不足");
			sendPawnshopGoodsBuyResp(session, 1, 0, dangpuId, -1, junZhu.id);
			return;
		}

		YuanBaoMgr.inst.diff(junZhu, -commonCfg.getNeedNum(), 0,
				commonCfg.getNeedNum(), YBType.YB_PAWN_BUY_SHANGPIN, "当铺购买商品");
		HibernateUtil.save(junZhu);
		logger.info("junzhuId:{}购买了物品，dangpuId:{}，扣除yuanbao:{}", junZhu.id,
				dangpuId, commonCfg.getNeedNum());
		ActLog.log.ShopBuy(junZhu.id, junZhu.name, ActLog.vopenid, 1, commonCfg.getItemId(), BigSwitch.getInst().bagMgr
				.getItemName(commonCfg.getItemId()), commonCfg.getItemNum(), commonCfg.getNeedNum(), junZhu.yuanBao);
		JunZhuMgr.inst.sendMainInfo(session);
		AwardTemp award = new AwardTemp();
		award.setItemType(commonCfg.getItemType());
		award.setItemId(commonCfg.getItemId());
		award.setItemNum(commonCfg.getItemNum());
		AwardMgr.inst.giveReward(session, award, junZhu);
		sendPawnshopGoodsBuyResp(session, 0, 0, dangpuId, -1, junZhu.id);
	}

	protected void buyMysterious(int cmd, IoSession session, JunZhu junZhu,
			int dangpuId) {
		PawnshopBean shopBean = HibernateUtil.find(PawnshopBean.class,
				junZhu.id);
		if (shopBean == null) {
			logger.error("当铺购买失败，当铺没有物品。jinzhuId:{}", junZhu.id);
			sendError(cmd, session, "当铺购买失败");
			return;
		}
		List<GoodsInfo> goodsList = JsonUtils.strToList(
				shopBean.getGoodsInfo(), GoodsInfo.class);
		if (goodsList == null || goodsList.size() == 0) {
			logger.error("当铺购买失败，当铺没有物品。jinzhuId:{}", junZhu.id);
			sendError(cmd, session, "当铺购买失败");
			return;
		}

		GoodsInfo ginfo = null;
		for (GoodsInfo g : goodsList) {
			if (g.getId() == dangpuId) {
				if(g.isSell()==true){
					logger.error("当铺购买失败，当铺物品已售空。jinzhuId:{}", junZhu.id);
					sendError(cmd, session, "当铺该物品已售空");
					return;
				}
				ginfo = g;
				break;
			}
		}
		if (ginfo == null) {
			logger.error("未找到配置项，请求的dangpuId:{}", dangpuId);
			sendError(cmd, session, "请求数据错误");
			return;
		}

		Dangpu dangpu = null;
		List<Dangpu> dangpuList = TempletService.listAll(Dangpu.class.getSimpleName());
		for (Dangpu dp : dangpuList) {
			if (dp.getId() == dangpuId) {
				dangpu = dp;
				break;
			}
		}
		if (dangpu == null) {
			logger.error("未找到配置项，请求的dangpuId:{}", dangpuId);
			sendError(cmd, session, "请求数据错误");
			return;
		}
		int type = dangpu.getType();
		int price = dangpu.getNeedNum();
		switch (type) {
		case YUAN_BAO:
			if (junZhu.yuanBao < price) {
				sendPawnshopGoodsBuyResp(session, 1, 1, dangpuId, type, junZhu.id);
				logger.error("当铺购买物品失败，元宝不足，need:{}.junzhuHave:{]", price, junZhu.yuanBao);
				return;
			}
			YuanBaoMgr.inst.diff(junZhu, -price, 0, price,
					YBType.YB_PAWN_BUY_SHANGPIN, "当铺购买商品");
			HibernateUtil.save(junZhu);
			logger.info("junzhuId:{}购买了物品，dangpuId:{}，扣除yuanbao:{}", junZhu.id,
					dangpuId, price);
			JunZhuMgr.inst.sendMainInfo(session);
			ActLog.log.ShopBuy(junZhu.id, junZhu.name, ActLog.vopenid, 1, dangpu.getItemId(), BigSwitch.getInst().bagMgr
					.getItemName(dangpu.getItemId()), dangpu.getItemNum(), price, junZhu.yuanBao);
			break;
		case GONG_XIAN:
			// 判断君主贡献 扣除贡献
			AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class,
					junZhu.id);
			if (member.gongXian < price) {
				sendPawnshopGoodsBuyResp(session, 2, 1, dangpuId, type, junZhu.id);
				logger.error("当铺购买物品失败，贡献不足，need:{}.junzhuHave:{]", price,
						member.gongXian);
				return;
			}
			member.gongXian -= price;
			HibernateUtil.save(member);
			logger.info("junzhuId:{}购买了物品，dangpuId:{}，扣除贡献点:{}", junZhu.id,
					dangpuId, price);
			ActLog.log.ShopBuy(junZhu.id, junZhu.name, ActLog.vopenid, 2, dangpu.getItemId(), BigSwitch.getInst().bagMgr
					.getItemName(dangpu.getItemId()), dangpu.getItemNum(), price, member.gongXian);
			break;
		case TONG_BI:
			if (junZhu.tongBi < price) {
				sendPawnshopGoodsBuyResp(session, 3, 1, dangpuId, type, junZhu.id);
				logger.error("当铺购买物品失败，铜币不足，need:{}.junzhuHave:{]", price, junZhu.tongBi);
				return;
			}
			junZhu.tongBi -= price;
			HibernateUtil.save(junZhu);
			logger.info("junzhuId:{}购买了物品，dangpuId:{}，扣除铜币:{}", junZhu.id,
					dangpuId, price);
			JunZhuMgr.inst.sendMainInfo(session);
			ActLog.log.ShopBuy(junZhu.id, junZhu.name, ActLog.vopenid, 3, dangpu.getItemId(), BigSwitch.getInst().bagMgr
					.getItemName(dangpu.getItemId()), dangpu.getItemNum(), price, junZhu.tongBi);
			break;
		default:
			logger.error("购买类型不匹配，type:{}", type);
			break;
		}
		ginfo.setSell(true);
		shopBean.setGoodsInfo(JsonUtils.listToStr(goodsList));
		HibernateUtil.save(shopBean);

		AwardTemp award = new AwardTemp();
		award.setItemType(dangpu.getItemType());
		award.setItemId(dangpu.getItemId());
		award.setItemNum(dangpu.getItemNum());
		AwardMgr.inst.giveReward(session, award, junZhu);
		sendPawnshopGoodsBuyResp(session, 0, 1, dangpu.getId(), type, junZhu.id);
		// 购买的要是宝石，就判定是否有任务完成
		if(award.getItemType() == AwardMgr.type_baoShi){
			logger.info("君主:{}在神秘商铺购买宝石,宝石id：{}成功, 判定是否有购买宝石任务", junZhu.id, award.getItemId());
			EventMgr.addEvent(ED.pawnshop_buy, new Object[] { junZhu.id});
		}
	}

	// 添加参数mysteryType（神秘物品类型）和junzhuid
	protected void sendPawnshopGoodsBuyResp(IoSession session, int result,
			int type, int id, int mysteryType, long junZhuId) {
		PawnshopGoodsBuyResp.Builder response = PawnshopGoodsBuyResp
				.newBuilder();
		response.setResult(result);
		response.setType(type);
		response.setItemId(id);
		AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class,
				junZhuId);
		if (member == null) {
			response.setContribution(0);
		} else {
			response.setContribution(member.gongXian);
		}
		session.write(response.build());
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
		// 验证数据
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		for (SellGoodsInfo sginfo : sellList) {
			long bagId = sginfo.getBagId();
			int sellCount = sginfo.getCount();
			BagGrid bagGrid = null;
			List<BagGrid> gridList = bag.grids;
			for (BagGrid bg : gridList) {
				if (bg.getIdentifier() == bagId) {
					bagGrid = bg;
					ItemTemp item = TempletService.getInstance().getItemTemp(
							bg.itemId);
					if (item == null) {
						logger.error("itemTemp表获取不到该物品，传过来的itemId:{}",
								bg.itemId);
						sendError(cmd, session, "典当物品操作失败");
						return;
					}

					int itemType = item.getItemType();
					if (itemType != SELL_TYPE) {
						logger.error("当铺出售的物品类型错误，itemType:{}", itemType);
						sendError(cmd, session, "当铺只能出售玉诀");
						return;
					}

					if (bg.cnt < sellCount) {
						logger.error("背包里没有足够的物品itemId:{},数量:{}出售", bg.itemId,
								sellCount);
						sendError(cmd, session, "典当物品操作失败");
						return;
					}
					rightData.put(bagId, bagGrid);
					break;
				}
			}

			if (bagGrid == null) {
				logger.error("没有找到bag数据，bagDBid:{}", bagId);
				sendError(cmd, session, "典当物品操作失败");
				return;
			}

		}

		// 出售
		Date date = new Date();
		for (SellGoodsInfo sginfo : sellList) {
			long bagId = sginfo.getBagId();
			int sellCount = sginfo.getCount();
			BagGrid bagGrid = rightData.get(bagId);
			if (bagGrid == null) {
				logger.error("bagGrid为null， bagId:{}", bagId);
				continue;
			}
			ItemTemp itemTemp = TempletService.getInstance().getItemTemp(
					bagGrid.itemId);
			int sellPrice = itemTemp.getSellNum();
			int totalSellPrice = sellCount * sellPrice;
			YuanBaoMgr.inst.diff(junZhu, totalSellPrice, 0, sellPrice,
					YBType.YB_PAWN_SELL, "当铺出售商品");
			// 扣除背包物品
			BagMgr.inst.removeItem(bag, bagGrid.itemId, sellCount, "当铺典当物品",junZhu.level);
			logger.info("君主:{}在当铺典当物品itemId:{},数量:{},时间:{},获得元宝:{}",
					junZhu.name, bagGrid.itemId, sellCount, date,
					totalSellPrice);
			ActLog.log.Pawn(junZhu.id, junZhu.name, ActLog.vopenid, bagGrid.itemId, itemTemp.getName(), sellCount, 0, totalSellPrice);
		}
		HibernateUtil.save(junZhu);
		JunZhuMgr.inst.sendMainInfo(session);
		BagMgr.inst.sendBagInfo(cmd, session, builder);
		session.write(PD.PAWN_SHOP_GOODS_SELL_OK);
	}

	public void refreshPawnshop(int cmd, IoSession session, Builder builder) {
		// 检查君主
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.error("未发现君主，cmd:{}", cmd);
			return;
		}

		PawnshopBean shopBean = HibernateUtil.find(PawnshopBean.class, junZhu.id);
		if (shopBean == null) {
			logger.error("当铺购买失败，当铺没有物品。jinzhuId:{}", junZhu.id);
			sendError(cmd, session, "当铺购买失败");
			return;
		}
		PawnshopRefeshResp.Builder response = PawnshopRefeshResp.newBuilder();
		boolean isPermit = VipMgr.INSTANCE.isVipPermit(VipData.refresh_dangPu,
				junZhu.vipLevel);
		if (!isPermit) {
			logger.error("VIP等级不够，不能刷新当铺物品");
			response.setResult(1);
			session.write(response.build());
			return;
		}

		// change 20150901
		if(DateUtils.isTimeToReset(shopBean.getLastRefreshTime(), CanShu.REFRESHTIME_PURCHASE)){
			shopBean.setRefreshTimes(0);
		}
		Date curDate = new Date();
		int refreshTimes = shopBean.getRefreshTimes();
		int needYuanBao = PurchaseMgr.inst.getNeedYuanBao(
				PurchaseConstants.PAWNSHOP_REFRESH, refreshTimes + 1);
		if (needYuanBao == -1) {
			logger.error("purchase表配置有误，当铺刷新次数:{}", refreshTimes + 1);
			return;
		}
		int maxRefreshTimes = VipMgr.INSTANCE.getValueByVipLevel(
				junZhu.vipLevel, VipData.dangpuRefreshLimit);
		if(refreshTimes >= maxRefreshTimes) {
			response.setResult(3);
			session.write(response.build());
			logger.error("今日当铺刷新次数已达上限:{}", maxRefreshTimes);
			return;
		}
		if (junZhu.yuanBao < needYuanBao) {
			response.setResult(2);
			session.write(response.build());
			logger.error("junzhu:{}进行当铺第{}次刷新操作失败，元宝不足:{}", junZhu.name,
					refreshTimes + 1, needYuanBao);
			return;
		}

		YuanBaoMgr.inst.diff(junZhu, -needYuanBao, 0,
				PurchaseMgr.inst.getPrice(PurchaseConstants.PAWNSHOP_REFRESH),
				YBType.YB_PAWN_REFRESH, "当铺刷新");
		HibernateUtil.save(junZhu);
		JunZhuMgr.inst.sendMainInfo(session);
		logger.info("手动刷新当铺物品, 君主:{}，当日第{}次,花费元宝:{},时间:{}", junZhu.id, refreshTimes+1,
				needYuanBao, curDate);

		List<GoodsInfo> goodsList = getGoodsList(junZhu);
		shopBean.setGoodsInfo(JsonUtils.listToStr(goodsList));
		shopBean.setRefreshTimes(refreshTimes + 1);
		shopBean.setLastRefreshTime(curDate);
		HibernateUtil.save(shopBean);
		int need = PurchaseMgr.inst.getNeedYuanBao(
				PurchaseConstants.PAWNSHOP_REFRESH,
				shopBean.getRefreshTimes() + 1);
		int seconds = getRefreshRemainSeconds(shopBean.getLastRefreshTime(),
				curDate);
		sendShopGoodsInfo(session, goodsList, need, seconds, junZhu.id);
	}

}
