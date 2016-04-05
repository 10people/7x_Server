package com.qx.fuwen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.bag.HashBag;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.FuWen.FuwenLanwei;
import qxmobile.protobuf.FuWen.FuwenResp;
import qxmobile.protobuf.FuWen.JunzhuAttr;
import qxmobile.protobuf.FuWen.OperateFuwenReq;
import qxmobile.protobuf.FuWen.QueryFuwenResp;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.Fuwen;
import com.manu.dynasty.template.FuwenJiacheng;
import com.manu.dynasty.template.FuwenOpen;
import com.manu.network.PD;
import com.manu.network.SessionManager;
import com.qx.account.FunctionOpenMgr;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.BagMgr;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.junzhu.JunZhuMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.pvp.PvpMgr;
import com.qx.timeworker.FunctionID;

public class FuwenMgr extends EventProc {
	public static FuwenMgr inst;
	public Logger logger = LoggerFactory.getLogger(FuwenMgr.class);
	public Map<Integer, Fuwen> fuwenMap = new HashMap<Integer, Fuwen>();
	public Map<Integer, FuwenOpen> fuwenOpenMap = new HashMap<Integer, FuwenOpen>();
	public Map<Integer, FuwenJiacheng> fuwenJiachengMap = new HashMap<Integer, FuwenJiacheng>();
	public Map<Integer, List<FuwenOpen>> fuwenSuitMap = new HashMap<Integer, List<FuwenOpen>>();
	public static int COMBINE_NUM = 4;// 4个符石合成一个高级符石
	public static String CACHE_FUWEN_LANWEI = "fuwen_position_";// 符石栏位缓存
	public static String CACHE_FUWEN_LOCK = "fuwen_lock_";// 符石锁定缓存
	public static int MaxFuwenLevel=11;//2016年3月25日 加入符文最高等级
	public Redis redis = Redis.getInstance();// Redis

	public FuwenMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		List<Fuwen> fuwenList = TempletService.listAll(Fuwen.class
				.getSimpleName());
		List<FuwenOpen> fuwenOpenList = TempletService.listAll(FuwenOpen.class
				.getSimpleName());
		List<FuwenJiacheng> fuwenJiachengList = TempletService
				.listAll(FuwenJiacheng.class.getSimpleName());
		Map<Integer, Fuwen> fuwenMap = new HashMap<Integer, Fuwen>();
		Map<Integer, FuwenOpen> fuwenOpenMap = new HashMap<Integer, FuwenOpen>();
		Map<Integer, FuwenJiacheng> fuwenJiachengMap = new HashMap<Integer, FuwenJiacheng>();
		Map<Integer, List<FuwenOpen>> fuwenSuitMap = new HashMap<Integer, List<FuwenOpen>>();
		for (Fuwen fuwen : fuwenList) {
			fuwenMap.put(fuwen.getFuwenID(), fuwen);
		}
		for (FuwenOpen fuwenOpen : fuwenOpenList) {
			fuwenOpenMap.put(fuwenOpen.getLanweiID(), fuwenOpen);
			// suitMap
			int suitIndex = fuwenOpen.getLanweiID() / 100;
			List<FuwenOpen> tmp = fuwenSuitMap.get(suitIndex);
			tmp = (null == tmp) ? new ArrayList<FuwenOpen>() : tmp;
			tmp.add(fuwenOpen);
			fuwenSuitMap.put(suitIndex, tmp);
		}
		for (FuwenJiacheng fuwenJiacheng : fuwenJiachengList) {
			fuwenJiachengMap.put(fuwenJiacheng.getLevelMin(), fuwenJiacheng);
		}
		this.fuwenMap = fuwenMap;
		this.fuwenOpenMap = fuwenOpenMap;
		this.fuwenJiachengMap = fuwenJiachengMap;
		this.fuwenSuitMap = fuwenSuitMap;
	}

	/**
	 * @Title: queryFuwen
	 * @Description: 查询符石
	 * @param cmd
	 * @param session
	 * @param builder
	 * @return void
	 * @throws
	 */
	public void queryFuwen(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.info("君主不存在");
			return;
		}
		QueryFuwenResp.Builder response = QueryFuwenResp.newBuilder();
		queryFuwen(junZhu, response);
		session.write(response.build());
	}

	/**
	 * @Title: operateFuwen
	 * @Description: 操作符石
	 * @param cmd
	 * @param session
	 * @param builder
	 * @return void
	 * @throws
	 */
	public void operateFuwen(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.info("君主不存在");
			return;
		}
		OperateFuwenReq.Builder request = (OperateFuwenReq.Builder) builder;
		FuwenResp.Builder response = FuwenResp.newBuilder();
		int type = request.getType();
		boolean flag = false;
		int itemId = request.getItemId();
		int lanweiId = request.getLanweiId();
		switch (type) {
		// 1-锁定，2-解锁,3-普通合成，4-一键合成,5-装备符石，6-卸下符石
		case 1:
			flag = lockFuwen(junZhu, itemId, response);
			break;
		case 2:
			flag = unlockFuwen(junZhu, itemId, response);
			break;
		case 3:
			flag = combineFuwen(junZhu, itemId, lanweiId, response);
			break;
		case 4:
			flag = yijianCombineFuwen(junZhu, itemId, response);
			break;
		case 5:
			flag = loadFuwen(junZhu, itemId, lanweiId, response);
			break;
		case 6:
			flag = unloadFuwen(junZhu, lanweiId, response);
			break;
		default:
			break;
		}
		HibernateUtil.save(junZhu);
		JunZhuMgr.inst.sendMainInfo(session);
		BagMgr.inst.sendBagInfo(0, session, null);
		// 刷新君主榜
		EventMgr.addEvent(ED.JUN_RANK_REFRESH, junZhu);
		response.setZhanli(PvpMgr.inst.getZhanli(junZhu));
		if (flag) {
			response.setResult(0);
		} else {
			response.setResult(1);
		}
		session.write(response.build());
	}

	// 查询符石
	public void queryFuwen(JunZhu jz, QueryFuwenResp.Builder response) {
		// 　战力
		long zhanli = PvpMgr.inst.getZhanli(jz);
		response.setZhanli(zhanli);
		// 符石栏位
		List<String> list = redis.lgetList(CACHE_FUWEN_LANWEI + jz.id);
		if (list == null || list.size() == 0) {// 符文栏位初始化
			initFuwenLanwei(jz);
			list = redis.lgetList(CACHE_FUWEN_LANWEI + jz.id);
		}
		Bag<BagGrid> bag = BagMgr.inst.loadBag(jz.id);
		List<FushiInBagInfo> fuwens = getFushiInBag(bag);
		for (int lanweiId : fuwenOpenMap.keySet()) {
			Integer itemId = getItemIdByLanweiId(lanweiId, list);
			if (itemId != null) {
				FuwenLanwei.Builder lanweiBuilder = FuwenLanwei.newBuilder();
				lanweiBuilder.setLanweiId(lanweiId);
				lanweiBuilder.setItemId(itemId);
				// 判断符石红点推送
				List<Integer> shuxingList = new ArrayList<Integer>();
				List<FuwenOpen> opens = fuwenSuitMap.get(lanweiId / 100);// 获取同一个套装的栏位
				for (FuwenOpen open : opens) {
					int openItemId = getItemIdByLanweiId(open.getLanweiID(),
							list);
					if (openItemId > 0) {
						Fuwen openFuwen = fuwenMap.get(openItemId);
						shuxingList.add(openFuwen.getShuxing());
					}
				}
				int state = getLanweiPushState(lanweiId, itemId, fuwens,
						shuxingList);
				if (state == 1 || state == 2) {
					lanweiBuilder.setFlag(true);
				} else {
					lanweiBuilder.setFlag(false);
				}
				response.addLanwei(lanweiBuilder);
			}
		}
		// 总属性值
		JunzhuAttr.Builder attr = JunzhuAttr.newBuilder();
		attr.setType(1);
		attr.setGongji(jz.gongJi);
		attr.setFangyu(jz.fangYu);
		attr.setShengming(jz.shengMingMax);
		attr.setWqSH(jz.getWqSH());
		attr.setWqJM(jz.getWqJM());
		attr.setWqBJ(jz.getWqBJ());
		attr.setWqRX(jz.getWqRX());
		attr.setJnSH(jz.getJnSH());
		attr.setJnJM(jz.getJnJM());
		attr.setJnBJ(jz.getJnBJ());
		attr.setJnRX(jz.getJnRX());
		response.addAttr(attr);
		// 加成属性值
		JunzhuAttr.Builder jcAttr = getFuwenAttr(list);
		jcAttr.setType(2);
		response.addAttr(jcAttr);
		// 符石背包
		List<BagGrid> itemList = bag.grids;
		if (itemList != null && itemList.size() != 0) {
			for (BagGrid item : itemList) {
				if ((item.type == 7 || item.type == 8) && item.cnt > 0) {// TODO
																			// 宝石和符文的类型会有改变
					Fuwen fuwen = fuwenMap.get(item.itemId);
					int isLock = redis.lexist(CACHE_FUWEN_LOCK + jz.id,
							String.valueOf(item.itemId)) ? 1 : 2;
					qxmobile.protobuf.FuWen.Fuwen.Builder fuwenBuilder = qxmobile.protobuf.FuWen.Fuwen
							.newBuilder();
					fuwenBuilder.setItemId(fuwen.getFuwenID());
					fuwenBuilder.setIsLock(isLock);
					fuwenBuilder.setCnt(item.cnt);
					qxmobile.protobuf.FuWen.Fuwen.Builder tmpBuilder = isFuwenInList(
							response.getFuwensBuilderList(), fuwen.getFuwenID());
					if (tmpBuilder != null) {
						tmpBuilder.setCnt(tmpBuilder.getCnt()
								+ fuwenBuilder.getCnt());
					} else {
						response.addFuwens(fuwenBuilder);
					}
				}
			}
		}
	}

	protected qxmobile.protobuf.FuWen.Fuwen.Builder isFuwenInList(
			List<qxmobile.protobuf.FuWen.Fuwen.Builder> list, int itemId) {
		for (qxmobile.protobuf.FuWen.Fuwen.Builder fuwen : list) {
			if (fuwen.getItemId() == itemId) {
				return fuwen;
			}
		}
		return null;
	}

	// 锁定符石
	public boolean lockFuwen(JunZhu junZhu, int itemId,
			FuwenResp.Builder response) {
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		int itemCount = BagMgr.inst.getItemCount(bag, itemId);
		if (itemCount == 0) {
			logger.info("锁定符石，背包中不存在符石{}", itemId);
			response.setReason("背包中不存在此符石");
			return false;
		}
		if (redis.lexist(CACHE_FUWEN_LOCK + junZhu.id, String.valueOf(itemId))) {
			response.setReason("符石已经被锁定");
			return false;
		}
		redis.lpush_(CACHE_FUWEN_LOCK + junZhu.id, String.valueOf(itemId));// 添加
		logger.info("君主{}锁定符石{}", junZhu.id, itemId);
		return true;
	}

	// 解锁符石
	public boolean unlockFuwen(JunZhu junZhu, int itemId,
			FuwenResp.Builder response) {
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		int itemCount = BagMgr.inst.getItemCount(bag, itemId);
		if (itemCount == 0) {
			logger.info("解锁符石，背包中不存在符石{}", itemId);
			response.setReason("背包中不存在此符石");
			return false;
		}
		if (!redis.lexist(CACHE_FUWEN_LOCK + junZhu.id, String.valueOf(itemId))) {
			logger.info("解锁符石，符石{}没有锁定", itemId);
			return false;
		}
		redis.lrem(CACHE_FUWEN_LOCK + junZhu.id, 0, String.valueOf(itemId));// 移除
		logger.info("君主{}解锁符石{}", junZhu.id, itemId);
		return true;
	}

	// 装备符石
	public boolean loadFuwen(JunZhu junZhu, int itemId, int lanweiId,
			FuwenResp.Builder response) {
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		int itemCount = BagMgr.inst.getItemCount(bag, itemId);
		if (itemCount == 0) {
			logger.info("镶嵌符石失败，背包中不存在符石{}", itemId);
			response.setReason("背包中不存在此符石");
			return false;
		}
		Fuwen fuwen = fuwenMap.get(itemId);
		FuwenOpen lanwei = fuwenOpenMap.get(lanweiId);
		// redis 记录
		List<String> list = redis.lgetList(CACHE_FUWEN_LANWEI + junZhu.id);
		if (null == list || list.size() == 0) {
			logger.info("君主{}未开启符石系统", junZhu.id);
			response.setReason("符石系统未开启");
			return false;
		}
		Integer tmpItemId = getItemIdByLanweiId(lanweiId, list);
		if (tmpItemId != null) {
			if (tmpItemId.intValue() == -1) {
				logger.info("镶嵌符石失败，符石栏位{}未解锁", lanweiId);
				response.setReason("符石栏位未解锁");
				return false;
			}
			if (tmpItemId.intValue() != 0) {
				logger.info("镶嵌符石失败，符石栏位{}已镶嵌其他符石", lanweiId);
				response.setReason("符石栏位已镶嵌其他符石");
				return false;
			}
			// 判断同属性栏位
			if ((fuwen.getShuxing() <= 3 && lanwei.getLanweiType() > 10)
					|| (fuwen.getShuxing() > 3 && lanwei.getLanweiType() < 10)) {
				logger.info("镶嵌符石{}失败，符石栏位{}属性与符石属性不一致，不能镶嵌",
						fuwen.getFuwenID(), lanwei.getLanweiID());
				response.setReason("符石栏位属性与符石属性不一致");
				return false;
			}
			// 判断栏位是否有符石
			List<FuwenOpen> lanweiList = fuwenSuitMap.get(lanweiId / 100);
			for (FuwenOpen fuwenOpen : lanweiList) {
				Integer tmpId = getItemIdByLanweiId(fuwenOpen.getLanweiID(),
						list);
				if (tmpId != null && tmpId.intValue() > 0) {
					Fuwen tmpFuwen = fuwenMap.get(tmpId);
					if (fuwen.getShuxing() == tmpFuwen.getShuxing()) {
						logger.info("镶嵌符石{}失败，符石栏位{}已存在相同属性符石{}",
								fuwen.getFuwenID(), fuwenOpen.getLanweiID(),
								tmpFuwen.getFuwenID());
						response.setReason("同属性其他栏位已镶嵌相同属性符石");
						return false;
					}
				}
			}
			redis.lrem(CACHE_FUWEN_LANWEI + junZhu.id, 0, lanweiId + "#"
					+ tmpItemId);
			redis.lpush_(CACHE_FUWEN_LANWEI + junZhu.id, lanweiId + "#"
					+ itemId);
			HibernateUtil.save(junZhu);
			logger.info("君主{}镶嵌符石{}到栏位{}", junZhu.id, itemId, lanweiId);
			// 从背包消耗一个符石
			BagMgr.inst.removeItem(bag, itemId, 1, "镶嵌消耗一个符石", junZhu.level);
			// 主线任务: 装备任意一个符石 20190916
			EventMgr.addEvent(ED.wear_fushi, new Object[] { junZhu.id });
			return true;
		}
		return false;
	}

	// 卸下符石
	public boolean unloadFuwen(JunZhu junZhu, int lanweiId,
			FuwenResp.Builder response) {
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		// redis 记录
		List<String> list = redis.lgetList(CACHE_FUWEN_LANWEI + junZhu.id);
		if (null == list || list.size() == 0) {
			logger.info("君主{}未开启符石系统", junZhu.id);
			response.setReason("符石系统未开启");
			return false;
		}
		Integer itemId = getItemIdByLanweiId(lanweiId, list);
		if (itemId != null) {
			if (itemId.intValue() == -1) {
				logger.info("卸下符石，符石栏位{}未解锁", lanweiId);
				response.setReason("符石栏位未解锁");
				return false;
			}
			if (itemId.intValue() == 0) {
				logger.info("卸下符石，符石栏位{}没有镶嵌符石", lanweiId);
				response.setReason("符石栏位没有镶嵌符石");
				return false;
			}
			redis.lrem(CACHE_FUWEN_LANWEI + junZhu.id, 0, lanweiId + "#"
					+ itemId);
			redis.lpush_(CACHE_FUWEN_LANWEI + junZhu.id, lanweiId + "#0");// 栏位符石置空
			BagMgr.inst.addItem(bag, itemId, 1, -1, junZhu.level, "卸下符石");// 符石返回给背包
			logger.info("君主{}卸下{}栏位的符石{}", junZhu.id, lanweiId, itemId);
			return true;
		}
		return false;
	}

	// 合成符石
	public boolean combineFuwen(JunZhu junZhu, int itemId, int lanweiId,
			FuwenResp.Builder response) {
		// lock
		if (redis.lexist(CACHE_FUWEN_LOCK + junZhu.id, String.valueOf(itemId))) {
			logger.info("君主{}的符石{}已锁定，不能合成", junZhu.id, itemId);
			response.setReason("符石已锁定。");
			return false;
		}
		// level
		Fuwen fuwen = fuwenMap.get(itemId);
		if (fuwen.getFuwenNext() == -1) {
			logger.info("君主{}合成符石{}失败，符石已是最高等级{}", junZhu.id, itemId,
					fuwen.getFuwenLevel());
			response.setReason("符石已达到最高等级。");
			return false;
		}
		Fuwen fuwenNext = fuwenMap.get(fuwen.getFuwenNext());
		// Bag
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		int num = BagMgr.inst.getItemCount(bag, itemId);
		if (lanweiId == 0) {// 从背包合成
			if (num < COMBINE_NUM) {
				logger.info("君主{}要合成的符石{}数量{}不足最低数量{}", junZhu.id, itemId, num,
						COMBINE_NUM);
				response.setReason("符石数量不足最低合成数量。");
				return false;
			}
			BagMgr.inst.removeItem(bag, itemId, COMBINE_NUM, "普通合成消耗"
					+ COMBINE_NUM + "个符石", junZhu.level);
			BagMgr.inst.addItem(bag, fuwenNext.getFuwenID(), 1, -1,
					junZhu.level, "合成符石");
			logger.info("君主{}成功消耗{}个符石{}合成一个高级符石{}", junZhu.id, COMBINE_NUM,
					itemId, fuwenNext.getFuwenID());
			EventMgr.addEvent(ED.GAIN_ITEM,
					new Object[] { junZhu.id, fuwenNext.getFuwenID() });
		} else {// 从装备的栏位直接合成
			if (num < COMBINE_NUM - 1) {
				logger.info("君主{}要合成的符石{}数量{}不足最低数量{}", junZhu.id, itemId,
						num + 1, COMBINE_NUM);
				response.setReason("符石数量不足最低合成数量。");
				return false;
			}
			BagMgr.inst.removeItem(bag, itemId, COMBINE_NUM - 1, "普通合成消耗"
					+ (COMBINE_NUM - 1) + "个符石", junZhu.level);
			redis.lrem(CACHE_FUWEN_LANWEI + junZhu.id, 0, lanweiId + "#"
					+ itemId);
			redis.lpush_(CACHE_FUWEN_LANWEI + junZhu.id, lanweiId + "#"
					+ fuwenNext.getFuwenID());
			logger.info("君主{}成功消耗{}个符石{}合成一个高级符石{}", junZhu.id,
					COMBINE_NUM - 1, itemId, fuwenNext.getFuwenID());
			EventMgr.addEvent(ED.GAIN_ITEM,
					new Object[] { junZhu.id, fuwenNext.getFuwenID() });
		}
		return true;
	}

	// 一键合成符石
	public boolean yijianCombineFuwen(JunZhu junZhu, int itemId,
			FuwenResp.Builder response) {
		// lock
		if (redis.lexist(CACHE_FUWEN_LOCK + junZhu.id, String.valueOf(itemId))) {
			logger.info("君主{}的符石{}已锁定，不能合成", junZhu.id, itemId);
			response.setReason("符石已锁定");
			return false;
		}
		// level
		Fuwen fuwen = fuwenMap.get(itemId);
		if (fuwen.getFuwenNext() == -1) {
			logger.info("君主{}合成符石{}失败，符石已是最高等级{}", junZhu.id, itemId,
					fuwen.getFuwenLevel());
			response.setReason("符石已达到最高等级");
			return false;
		}
		Fuwen fuwenNext = fuwenMap.get(fuwen.getFuwenNext());
		// Bag
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		int num = BagMgr.inst.getItemCount(bag, itemId);
		if (num < COMBINE_NUM * 2) {// 背包没有符石
			logger.info("一键合成失败，君主{}背包符石{}数量不足一键合成最低要求数量{}", junZhu.id, itemId,
					COMBINE_NUM * 2);
			response.setReason("符石数量不足最低合成数量");
			return false;
		}
		int combineNum = num / COMBINE_NUM;
		int costNum = num - num % COMBINE_NUM;
		BagMgr.inst.removeItem(bag, itemId, costNum,
				"一键合成消耗" + costNum + "个符石", junZhu.level);
		BagMgr.inst.addItem(bag, fuwenNext.getFuwenID(), combineNum, -1,
				junZhu.level, "一键合成符石");
		logger.info("君主{}成功消耗{}个符石{}合成{}个高级符石{}", junZhu.id, costNum, itemId,
				combineNum, fuwenNext.getFuwenID());
		EventMgr.addEvent(ED.GAIN_ITEM,
				new Object[] { junZhu.id, fuwenNext.getFuwenID() });
		return true;
	}

	// 初始化符石栏位
	public void initFuwenLanwei(JunZhu jz) {
		long jzId = jz.id;
		for (int key : fuwenOpenMap.keySet()) {
			FuwenOpen lanwei = fuwenOpenMap.get(key);
			if (lanwei.getLevel() <= jz.level) {
				redis.lpush_(CACHE_FUWEN_LANWEI + jzId, lanwei.getLanweiID() + "#0");
			}else {
				redis.lpush_(CACHE_FUWEN_LANWEI + jzId, lanwei.getLanweiID() + "#-1");
			}
		}
		logger.info("君主{}初始化所有符石栏位", jz.id);
	}

	// 检查符石栏位解锁
	public void checkFuwenUnlock(JunZhu jz) {
		if (jz == null) {
			logger.error("事件：检查符石解锁，参数不正确");
			return;
		}
		for (int key : fuwenOpenMap.keySet()) {
			FuwenOpen fuwenOpen = fuwenOpenMap.get(key);
			if (fuwenOpen.getLevel() <= jz.level) {
				List<String> list = redis.lgetList(CACHE_FUWEN_LANWEI + jz.id);
				if (null == list || list.size() == 0) {
					initFuwenLanwei(jz);
					list = redis.lgetList(CACHE_FUWEN_LANWEI + jz.id);
					// logger.error("君主{}符石系统未开启", jz.id);
				}
				Integer itemId = getItemIdByLanweiId(fuwenOpen.getLanweiID(), list);
				if (itemId != null && itemId.intValue() == -1) {
					redis.lrem(CACHE_FUWEN_LANWEI + jz.id, 0, fuwenOpen.getLanweiID() + "#-1");
					redis.lpush_(CACHE_FUWEN_LANWEI + jz.id, fuwenOpen.getLanweiID() + "#0");
					logger.info("君主{}符石栏位{}解锁", jz.id, fuwenOpen.getLanweiID());
				}
			}
		}
	}

	// 获取符石加成属性
	public JunzhuAttr.Builder getFuwenAttr(List<String> lanweiList) {
		JunzhuAttr.Builder attrBuilder = JunzhuAttr.newBuilder();
		int gongji = 0;
		int fangyu = 0;
		int shengming = 0;
		int wqSH = 0;
		int wqJM = 0;
		int wqBJ = 0;
		int wqRX = 0;
		int jnSH = 0;
		int jnJM = 0;
		int jnBJ = 0;
		int jnRX = 0;
		Map<Integer, Double> pageSuitMap = getFuwenSuitMap(lanweiList);
		for (String lanwei : lanweiList) {// 遍历君主所有符石栏位
			int lanweiId = Integer.parseInt(lanwei.split("#")[0]);
			int itemId = Integer.parseInt(lanwei.split("#")[1]);
			if (itemId > 0) {
				Fuwen fuwen = fuwenMap.get(itemId);
				switch (fuwen.getShuxing()) {
				case 1:
					gongji += Math.rint(fuwen.getShuxingValue()
							* (1 + pageSuitMap.get(lanweiId / 100)));
					break;
				case 2:
					fangyu += Math.rint(fuwen.getShuxingValue()
							* (1 + pageSuitMap.get(lanweiId / 100)));
					break;
				case 3:
					shengming += Math.rint(fuwen.getShuxingValue()
							* (1 + pageSuitMap.get(lanweiId / 100)));
					break;
				case 4:
					wqSH += Math.rint(fuwen.getShuxingValue()
							* (1 + pageSuitMap.get(lanweiId / 100)));
					break;
				case 5:
					wqJM += Math.rint(fuwen.getShuxingValue()
							* (1 + pageSuitMap.get(lanweiId / 100)));
					break;
				case 6:
					wqBJ += Math.rint(fuwen.getShuxingValue()
							* (1 + pageSuitMap.get(lanweiId / 100)));
					break;
				case 7:
					wqRX += Math.rint(fuwen.getShuxingValue()
							* (1 + pageSuitMap.get(lanweiId / 100)));
					break;
				case 8:
					jnSH += Math.rint(fuwen.getShuxingValue()
							* (1 + pageSuitMap.get(lanweiId / 100)));
					break;
				case 9:
					jnJM += Math.rint(fuwen.getShuxingValue()
							* (1 + pageSuitMap.get(lanweiId / 100)));
					break;
				case 10:
					jnBJ += Math.rint(fuwen.getShuxingValue()
							* (1 + pageSuitMap.get(lanweiId / 100)));
					break;
				case 11:
					jnRX += Math.rint(fuwen.getShuxingValue()
							* (1 + pageSuitMap.get(lanweiId / 100)));
					break;
				default:
					break;
				}
			}
		}
		attrBuilder.setGongji(gongji);
		attrBuilder.setFangyu(fangyu);
		attrBuilder.setShengming(shengming);
		attrBuilder.setWqSH(wqSH);
		attrBuilder.setWqJM(wqJM);
		attrBuilder.setWqBJ(wqBJ);
		attrBuilder.setWqRX(wqRX);
		attrBuilder.setJnSH(jnSH);
		attrBuilder.setJnJM(jnJM);
		attrBuilder.setJnBJ(jnBJ);
		attrBuilder.setJnRX(jnRX);
		return attrBuilder;
	}

	// 获取符石套装效果
	public Map<Integer, Double> getFuwenSuitMap(List<String> lanweiList) {
		Map<Integer, Double> suitMap = new HashMap<Integer, Double>();// 符石页套装效果
		for (int suitIndex : fuwenSuitMap.keySet()) {
			List<FuwenOpen> suitLanweiList = fuwenSuitMap.get(suitIndex);
			boolean flag = true;
			int minLevel = 11;
			for (FuwenOpen fuwenOpen : suitLanweiList) {
				Integer itemiId = getItemIdByLanweiId(fuwenOpen.getLanweiID(),
						lanweiList);
				if (itemiId != null) {
					if (itemiId.intValue() <= 0) {
						flag = false;
						break;
					}
					Fuwen fuwen = fuwenMap.get(itemiId);
					minLevel = fuwen.getFuwenLevel() <= minLevel ? fuwen
							.getFuwenLevel() : minLevel;
				}
			}
			if (flag) {
				FuwenJiacheng jiacheng = fuwenJiachengMap.get(minLevel);
				suitMap.put(suitIndex, jiacheng.getAddition() / 10000);
			} else {
				suitMap.put(suitIndex, 0.0);
			}
		}
		return suitMap;
	}

	// 获取符石栏位上的符石id
	public Integer getItemIdByLanweiId(int lanweiId, List<String> lanweiList) {
		for (String lanwei : lanweiList) {// 遍历君主所有符石栏位
			int tmpLanweiId = Integer.parseInt(lanwei.split("#")[0]);
			int itemId = Integer.parseInt(lanwei.split("#")[1]);
			if (tmpLanweiId == lanweiId) {
				return itemId;
			}
		}
		return null;
	}

	// 获取符石所在的栏位id
	public Integer getLanweiIdByItemId(int itemId, List<String> lanweiList) {
		for (String lanwei : lanweiList) {// 遍历君主所有符石栏位
			int lanweiId = Integer.parseInt(lanwei.split("#")[0]);
			int tmpItemId = Integer.parseInt(lanwei.split("#")[1]);
			if (tmpItemId == itemId) {
				return lanweiId;
			}
			break;
		}
		return null;
	}

	/**
	 * @Title: getFuShiProgress
	 * @Description: 获取符石养成进度
	 * @param jzId
	 *            君主id
	 * @param type
	 *            1-符文，2-宝石
	 * @return
	 * @return double
	 * @throws
	 */
	public double getFuShiProgress(long jzId, int type) {
		double levelSum = getFushiCurLevel(jzId, type);
		double availableSum = getFushiMaxLevel(jzId, type);
		double progress = (levelSum / (availableSum * 11));
		return progress;
	}

	/**
	 * @Title: getFushiCurLevel
	 * @Description: 获取符石当前进度
	 * @param jzId
	 *            君主id
	 * @param type
	 *            1-符文，2-宝石
	 * @return
	 * @return int
	 * @throws
	 */
	public int getFushiCurLevel(long jzId, int type) {
		JunZhu junZhu = HibernateUtil.find(JunZhu.class, jzId);
		if (null == junZhu) {
			logger.info("君主不存在");
			return 0;
		}
		List<String> list = redis.lgetList(CACHE_FUWEN_LANWEI + jzId);
		if (list == null || list.size() == 0) {
			logger.info("君主{}未开启符石系统", junZhu.id);
			return 0;
		}
		int levelSum = 0;
		for (String lanwei : list) {
			int lanweiId = Integer.parseInt(lanwei.split("#")[0]);
			int itemId = Integer.parseInt(lanwei.split("#")[1]);
			FuwenOpen fuwenOpen = fuwenOpenMap.get(lanweiId);
			Fuwen fuwen = fuwenMap.get(itemId);
			int index = fuwenOpen.getLanweiID() / 100 % 10;
			if (index == type) {
				if (itemId > 0) {
					levelSum += fuwen.getFuwenLevel();
				}
			}
		}
		return levelSum;
	}

	/**
	 * @Title: getFushiMaxLevel
	 * @Description: 获取符石总进度
	 * @param jzId
	 *            君主id
	 * @param type
	 *            1-符文，2-宝石
	 * @return
	 * @return int
	 * @throws
	 */
	public int getFushiMaxLevel(long jzId, int type) {
		JunZhu junZhu = HibernateUtil.find(JunZhu.class, jzId);
		if (null == junZhu) {
			logger.info("君主不存在");
			return 0;
		}
		List<String> list = redis.lgetList(CACHE_FUWEN_LANWEI + jzId);
		if (list == null || list.size() == 0) {
			logger.info("君主{}未开启符石系统", junZhu.id);
			return 0;
		}
		int availableSum = 0;
		for (String lanwei : list) {
			int lanweiId = Integer.parseInt(lanwei.split("#")[0]);
			int itemId = Integer.parseInt(lanwei.split("#")[1]);
			FuwenOpen fuwenOpen = fuwenOpenMap.get(lanweiId);
			int index = fuwenOpen.getLanweiID() / 100 % 10;
			if (index == type) {
				if (itemId >= 0) {
					availableSum++;
				}
			}
		}
		return availableSum * 11;
	}

	/**
	 * @Title: getFuShiTuijian
	 * @Description: 获取符石推荐
	 * @param jzId
	 *            君主id
	 * @param type
	 *            1-符文，2-宝石
	 * @return
	 * @return List<Fuwen>
	 * @throws
	 */
	public List<Fuwen> getFuShiTuijian(long jzId, int type) {
		JunZhu junZhu = HibernateUtil.find(JunZhu.class, jzId);
		if (null == junZhu) {
			logger.info("君主不存在");
			return null;
		}
		List<String> list = redis.lgetList(CACHE_FUWEN_LANWEI + jzId);
		if (list == null || list.size() == 0) {
			logger.info("君主{}未开启符石系统", junZhu.id);
			return null;
		}
		// 添加装备上的符石
		List<Fuwen> fuwenList = new ArrayList<Fuwen>();
		for (String lanwei : list) {
			int itemId = Integer.parseInt(lanwei.split("#")[1]);
			Fuwen fuwen = fuwenMap.get(itemId);
			if (fuwen != null) {
				if (type == 1 && fuwen.getShuxing() > 3
						&& !fuwenList.contains(fuwen)) {// 符文
					fuwenList.add(fuwen);
				} else if (type == 2 && fuwen.getShuxing() <= 3
						&& !fuwenList.contains(fuwen)) {// 宝石
					fuwenList.add(fuwen);
				}
			}
		}
		// 添加背包中的符石
		/*List<Fuwen> fushiBag = getFushiInBag(jzId);
		for (Fuwen fuwen : fushiBag) {
			if (type == 1 && fuwen.getShuxing() > 3
					&& !fuwenList.contains(fuwen)) {// 符文
				fuwenList.add(fuwen);
			} else if (type == 2 && fuwen.getShuxing() <= 3
					&& !fuwenList.contains(fuwen)) {// 宝石
				fuwenList.add(fuwen);
			}
		}*/
		Collections.sort(fuwenList, new Comparator<Fuwen>() {// 排序
					@Override
					public int compare(Fuwen f1, Fuwen f2) {
						if (f1.getFuwenLevel() < f2.getFuwenLevel()) {
							return -1;
						} else if (f1.getFuwenLevel() == f2.getFuwenLevel()) {
							return 0;
						} else {
							return 1;
						}
					}
				});
		int size = fuwenList.size();
		if (size > 3) {
			fuwenList = fuwenList.subList(0, 3);
		}
		if(size > 0 && fuwenList.get(0).getFuwenLevel() == 11){
			fuwenList = new ArrayList<Fuwen>();
		}
		return fuwenList;
	}

	/**
	 * @Title: getEquipedFushi
	 * @Description: 获取已装备的符石
	 * @param jzId
	 * @return
	 * @return List<Fuwen>
	 * @throws
	 */
	public List<Fuwen> getFushiEquiped(long jzId) {
		JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
		if (null == jz) {
			logger.info("君主不存在");
			return null;
		}
		List<String> list = redis.lgetList(CACHE_FUWEN_LANWEI + jzId);
		if (list == null || list.size() == 0) {
			logger.info("君主{}未开启符石系统", jzId);
			return null;
		}
		List<Fuwen> fuwenList = new ArrayList<Fuwen>();
		for (String lanwei : list) {
			int itemId = Integer.parseInt(lanwei.split("#")[1]);
			if (itemId > 0) {
				Fuwen fuwen = fuwenMap.get(itemId);
				fuwenList.add(fuwen);
			}
		}
		return fuwenList;
	}

	/**
	 * @Title: getFushiInBag
	 * @Description: 获取背包的符石
	 * @param jzId
	 * @return
	 * @return List<Fuwen>
	 * @throws
	 */
	public List<FushiInBagInfo> getFushiInBag(Bag<BagGrid> bag) {
		List<FushiInBagInfo> fuwenInBagList = new ArrayList<FushiInBagInfo>();
		List<BagGrid> list = bag.grids;
		for (BagGrid item : list) {
			if ((item.type == 7 || item.type == 8) && item.cnt > 0) {
				Fuwen fuwen = fuwenMap.get(item.itemId);
				FushiInBagInfo fsInBag = new FushiInBagInfo(fuwen, item.cnt);
				fuwenInBagList.add(fsInBag);
			}
		}
		return fuwenInBagList;
	}

	/**
	 * @Title: getFushi  
	 * @Description: 获取所有的符石
	 * @param jzId
	 * @return
	 * @return List<Fuwen>
	 * @throws
	 */
	public List<Fuwen> getFushi(long jzId) {
		// FIXME 暂时没用到，方法意思也不明确，问题：获取背包的符石是否需要数量？
		Bag<BagGrid> bag = BagMgr.inst.loadBag(jzId);
		List<Fuwen> fuwenList = new ArrayList<Fuwen>();
		List<Fuwen> equipList = getFushiEquiped(jzId);
		List<FushiInBagInfo> bagList = getFushiInBag(bag);
		if (equipList != null) {
			fuwenList.addAll(getFushiEquiped(jzId));
		}
		if (bagList != null) {
			for(FushiInBagInfo fsInBag : bagList) {
				fuwenList.add(fsInBag.fuwen);
			}
		}
		return fuwenList;
	}

	public void pushFushi(JunZhu jz) {
		if(jz == null){
			return;
		}
		long jzId = jz.id;
		IoSession session = SessionManager.inst.getIoSession(jzId);
		if (null == session) {
			logger.info("君主{}符石推送异常,session为null", jzId);
			return;
		}
		boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.FuShi, jz.id, jz.level);
		if(!isOpen){
			return;
		}
		// 当前有未镶嵌符石的栏位且有可镶嵌的符石
		// 当前栏位有更高级的符石可以替换
		logger.info("向君主{}推送符石红点", jzId);
		int state = getTuisongState(jzId);
		if(state >= 1000){
			state -= 1000;
			session.write(PD.FUSHI_RED_NOTICE);
			logger.info("君主:{}有可合成的符石。", jzId);
		}
		switch (state) {
		case -1:
			logger.info("君主{}符石系统推送参数错误", jzId);
			break;
		case 0:
			logger.info("君主{}符石系统不需红点推送", jzId);
			break;
		case 1:
			session.write(PD.FUSHI_RED_NOTICE);
			logger.info("符石推送红点==>君主{}有未镶嵌符石的栏位且有可镶嵌的符石", jzId);
			break;
		case 2:
			session.write(PD.FUSHI_RED_NOTICE);
			logger.info("符石推送红点==>君主{}栏位有更高级的符石可以替换", jzId);
			break;
		default:
			break;
		}

	}

	/**
	 * @Title: getTuisongState
	 * @Description: 获取符石推送红点状态
	 * @param jzId
	 * @return -1-参数错误 0-不需推送 1-当前有未镶嵌符石的栏位且有可镶嵌的符石 2-当前栏位有更高级的符石可以替换
	 * @return int
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public int getTuisongState(long jzId) {
		JunZhu junZhu = HibernateUtil.find(JunZhu.class, jzId);
		if (null == junZhu) {
			logger.error("君主{}不存在", jzId);
			return -1;
		}
		List<String> list = redis.lgetList(CACHE_FUWEN_LANWEI + jzId);
		if (list == null || list.size() == 0) {
			logger.info("君主{}未开启符石系统", jzId);
			return -1;
		}
		Bag<BagGrid> bag = BagMgr.inst.loadBag(jzId);
		List<FushiInBagInfo> fuwens = getFushiInBag(bag);// 背包中的符石
		if (fuwens == null || fuwens.size() == 0) {
			logger.info("君主{}背包中没有符石", jzId);
			return -1;
		}
		HashBag counter = new HashBag();
		HashSet<Integer> addedLanWei = new HashSet<Integer>();
		int ret = 0;
		for (String lanwei : list) {
			int lanweiId = Integer.parseInt(lanwei.split("#")[0]);
			int itemId = Integer.parseInt(lanwei.split("#")[1]);
			if(itemId>0){
				//对已装备的符石计数
				Fuwen fuwenEquiped = fuwenMap.get(itemId);
				if(fuwenEquiped != null && fuwenEquiped.getFuwenLevel() < MaxFuwenLevel
						&& !addedLanWei.contains(itemId)) {
					addedLanWei.add(itemId);
					counter.add(itemId,1);
				}
			}
			List<Integer> shuxingList = new ArrayList<Integer>();
			List<FuwenOpen> opens = fuwenSuitMap.get(lanweiId / 100);// 获取同一个套装的栏位
			for (FuwenOpen open : opens) {
				int openItemId = getItemIdByLanweiId(open.getLanweiID(), list);
				if (openItemId > 0) {
					Fuwen openFuwen = fuwenMap.get(openItemId);
					shuxingList.add(openFuwen.getShuxing());
				}
			}
			ret = getLanweiPushState(lanweiId, itemId, fuwens,
					shuxingList);
			if (ret != 0) {
				break;
			}
		}
		//对只有在符石栏位的背包中的符石进行计数
		bag.grids.stream()
			.filter(item-> item != null && (item.type == 7 || item.type == 8) 
										&& item.cnt > 0 && counter.contains(item.itemId))
			.forEach(item->counter.add(item.itemId,item.cnt));
		//计数背包和已镶嵌符石的计数是否满足合成条件
		boolean hasCombine = counter.stream().anyMatch(id->counter.getCount(id)>=COMBINE_NUM);
		if(hasCombine){
			ret += 1000;
		}
		return ret;
	}

	/**
	 * @Title: getLanweiPushState
	 * @Description: 获取栏位推送状态 0-不需推送 1-背包中有可镶嵌此栏位的符石 2-背包中有相同属性更高级的符石
	 * @param lanweiId
	 * @param itemId
	 * @param fuwens
	 * @return
	 * @return int
	 * @throws
	 */
	public int getLanweiPushState(int lanweiId, int itemId, List<FushiInBagInfo> fuwens,
			List<Integer> shuxingList) {
		FuwenOpen fuwenOpen = fuwenOpenMap.get(lanweiId);
		if (itemId == 0) {// 栏位解锁且没有符石
			for (FushiInBagInfo fsInBag : fuwens) {// 遍历背包中的符石
				Fuwen fuwen = fsInBag.fuwen;
				boolean sameColor = fuwen.inlayColor == fuwenOpen.inlayColor;
				// 判断同属性栏位 
				if (((fuwen.getShuxing() <= 3 && fuwenOpen.getLanweiType() < 10 && sameColor) || (fuwen
						.getShuxing() > 3 && fuwenOpen.getLanweiType() > 10 && sameColor))
						&& !shuxingList.contains(fuwen.getShuxing())) {// 背包中有可镶嵌此栏位的符石
					return 1;
				}
			}
		} else if (itemId > 0) {// 栏位有符石
			Fuwen fuwenEquiped = fuwenMap.get(itemId);
			if(fuwenEquiped!=null&&fuwenEquiped.getFuwenLevel()==MaxFuwenLevel){
				logger.info("@@@@####--栏位有符石itemId--{},等级达到--{}级,返回0",itemId,fuwenEquiped.getFuwenLevel());
				return 0;
			}
			int sameCount = 1;
			for (FushiInBagInfo fsInBag : fuwens) {// 遍历背包中的符石
				Fuwen fuwen = fsInBag.fuwen;
				if(fuwen.getFuwenID() == fuwenEquiped.getFuwenID()) {
					sameCount += fsInBag.count;
				}
				if (fuwen.getShuxing() == fuwenEquiped.getShuxing()
						&& fuwen.getFuwenLevel() > fuwenEquiped.getFuwenLevel()) {// 背包中有相同属性更高级的符石
					return 2;
				}
			}
			if(sameCount >= COMBINE_NUM) {
				return 2;
			}
		}
		return 0;
	}
	
	public class FushiInBagInfo {
		private Fuwen fuwen;
		private int count;
		public FushiInBagInfo(Fuwen fuwen, int count) {
			this.fuwen = fuwen;
			this.count = count;
		}
	}
	
	@Override
	public void proc(Event event) {
		JunZhu jz = null;
		if (event.param != null && event.param instanceof JunZhu) {
			jz = (JunZhu) event.param;
		}
		switch (event.id) {
		case ED.CHECK_FUWEN_UOLOCK:
			checkFuwenUnlock(jz);
			break;
		case ED.FUSHI_PUSH:
			pushFushi(jz);
			break;
		default:
			break;
		}
	}

	@Override
	protected void doReg() {
		EventMgr.regist(ED.CHECK_FUWEN_UOLOCK, this);
		EventMgr.regist(ED.FUSHI_PUSH, this);
	}
}
