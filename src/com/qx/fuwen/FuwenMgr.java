package com.qx.fuwen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.store.Redis;
import com.manu.dynasty.template.Fuwen;
import com.manu.dynasty.template.FuwenDuihuan;
import com.manu.dynasty.template.FuwenOpen;
import com.manu.dynasty.template.FuwenTab;
import com.manu.network.SessionManager;
import com.qx.account.FunctionOpenMgr;
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
import com.qx.persistent.HibernateUtil;
import com.qx.pvp.PvpMgr;
import com.qx.timeworker.FunctionID;

import qxmobile.protobuf.FuWen.FuwenDuiHuan;
import qxmobile.protobuf.FuWen.FuwenDuiHuanResp;
import qxmobile.protobuf.FuWen.FuwenEquipAll;
import qxmobile.protobuf.FuWen.FuwenEquipAllResp;
import qxmobile.protobuf.FuWen.FuwenInBag;
import qxmobile.protobuf.FuWen.FuwenInBagResp;
import qxmobile.protobuf.FuWen.FuwenLanwei;
import qxmobile.protobuf.FuWen.FuwenResp;
import qxmobile.protobuf.FuWen.FuwenRongHeReq;
import qxmobile.protobuf.FuWen.FuwenRongHeResp;
import qxmobile.protobuf.FuWen.FuwenUnloadAll;
import qxmobile.protobuf.FuWen.FuwenUnloadAllResp;
import qxmobile.protobuf.FuWen.JunzhuAttr;
import qxmobile.protobuf.FuWen.OperateFuwenReq;
import qxmobile.protobuf.FuWen.QueryFuwen;
import qxmobile.protobuf.FuWen.QueryFuwenResp;
import qxmobile.protobuf.JunZhuProto.JunZhuInfoRet;

public class FuwenMgr extends EventProc {
	public static FuwenMgr inst;
	public Logger logger = LoggerFactory.getLogger(FuwenMgr.class);
	public Map<Integer, Fuwen> fuwenMap;
	public Map<Integer, FuwenOpen> fuwenOpenMap;
	
	/** 符文镶嵌孔，<tabId, FuwenOpen> **/
	public Map<Integer, List<FuwenOpen>> fuwenOpenMapByTab;
	/*public Map<Integer, FuwenJiacheng> fuwenJiachengMap;
	public Map<Integer, List<FuwenOpen>> fuwenSuitMap;*/
	public Map<Integer, FuwenTab> fuwenTabMap;
	public Map<Integer, FuwenDuihuan> fuwenDuihuanMap;
	public static int COMBINE_NUM = 4;// 4个符文合成一个高级符文
	public static String CACHE_FUWEN_LANWEI = "fuwen_position_";// 符文栏位缓存
	public static String CACHE_FUWEN_LOCK = "fuwen_lock_";// 符文锁定缓存
	public static int MaxFuwenLevel = 9;
	public Redis redis = Redis.getInstance();// Redis
	
	public int RED_IS_CAN_EQUIP = 1;			// 当前有未镶嵌符文的栏位且有可镶嵌的符文
	public int RED_IS_CAN_UPGRADE = 2;			// 已镶嵌的符文中有可以融合升级
	public int RED_IS_CAN_REPLACE = 4;			// 当前栏位有更高级的符文可以替换

	public FuwenMgr() {
		inst = this;
		initData();
	}

	public void initData() {
		List<Fuwen> fuwenList = TempletService.listAll(Fuwen.class .getSimpleName());
		Map<Integer, Fuwen> fuwenMap = new HashMap<Integer, Fuwen>(fuwenList.size());
		for (Fuwen fuwen : fuwenList) {
			fuwenMap.put(fuwen.fuwenID, fuwen);
		}
		this.fuwenMap = fuwenMap;
		
		List<FuwenOpen> fuwenOpenList = TempletService.listAll(FuwenOpen.class.getSimpleName());
		Map<Integer, FuwenOpen> fuwenOpenMap = new HashMap<Integer, FuwenOpen>(fuwenOpenList.size());
		Map<Integer, List<FuwenOpen>> fuwenOpenMapByTab = new HashMap<Integer, List<FuwenOpen>>(fuwenOpenList.size());
//		Map<Integer, List<FuwenOpen>> fuwenSuitMap = new HashMap<Integer, List<FuwenOpen>>();
		for (FuwenOpen fuwenOpen : fuwenOpenList) {
			fuwenOpenMap.put(fuwenOpen.id, fuwenOpen);
			
			/*int suitIndex = fuwenOpen.id / 100;
			List<FuwenOpen> tmp = fuwenSuitMap.get(suitIndex);
			tmp = (null == tmp) ? new ArrayList<FuwenOpen>() : tmp;
			tmp.add(fuwenOpen);
			fuwenSuitMap.put(suitIndex, tmp);*/
			
			List<FuwenOpen> openList = fuwenOpenMapByTab.get(fuwenOpen.tab);
			if(openList == null) {
				openList = new ArrayList<>();
				fuwenOpenMapByTab.put(fuwenOpen.tab, openList);
			}
			openList.add(fuwenOpen);
		}
		this.fuwenOpenMap = fuwenOpenMap;
		this.fuwenOpenMapByTab = fuwenOpenMapByTab;
//		this.fuwenSuitMap = fuwenSuitMap;
		
		/*List<FuwenJiacheng> fuwenJiachengList = TempletService .listAll(FuwenJiacheng.class.getSimpleName());
		Map<Integer, FuwenJiacheng> fuwenJiachengMap = new HashMap<Integer, FuwenJiacheng>(fuwenJiachengList.size());
		for (FuwenJiacheng fuwenJiacheng : fuwenJiachengList) {
			fuwenJiachengMap.put(fuwenJiacheng.getLevelMin(), fuwenJiacheng);
		}
		this.fuwenJiachengMap = fuwenJiachengMap;*/

		List<FuwenTab> fuwenTabList = TempletService .listAll(FuwenTab.class.getSimpleName());
		Map<Integer, FuwenTab> fuwenTabMap = new HashMap<Integer, FuwenTab>(fuwenTabList.size());
		for (FuwenTab fuwenTab : fuwenTabList) {
			fuwenTabMap.put(fuwenTab.tab, fuwenTab);
		}
		this.fuwenTabMap = fuwenTabMap;

		List<FuwenDuihuan> fuwenDuihuanList = TempletService .listAll(FuwenDuihuan.class.getSimpleName());
		Map<Integer, FuwenDuihuan> fuwenDuihuanMap = new HashMap<Integer, FuwenDuihuan>(fuwenDuihuanList.size());
		for (FuwenDuihuan duihuan : fuwenDuihuanList) {
			fuwenDuihuanMap.put(duihuan.ID, duihuan);
		}
		this.fuwenDuihuanMap = fuwenDuihuanMap;
		
		
	}

	/**
	 * @Title: queryFuwen
	 * @Description: 查询符文
	 * @param cmd
	 * @param session
	 * @param builder
	 * @return void
	 * @throws
	 */
	public void queryFuwen(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.info("查询符文页签信息失败，君主不存在");
			return;
		}
		QueryFuwen.Builder request = (qxmobile.protobuf.FuWen.QueryFuwen.Builder) builder;
		int tab = request.getTab();
		
		QueryFuwenResp.Builder response = QueryFuwenResp.newBuilder();
		
		FuwenTab fuwenTab = fuwenTabMap.get(tab);
		if(fuwenTab == null) {
			logger.error("查询符文页签信息失败，找不到fuwentab为:{}的配置", tab);
			return;
		}
		if(junZhu.level < fuwenTab.level) { 
			logger.error("查询符文页签信息失败，君主:{} 等级:{} 不满足符文页面:{}的等级:{}条件",junZhu.id, junZhu.level,tab,fuwenTab.level);
			response.setResult(1);
			response.setTab(tab);
			response.setZhanli(0);
			session.write(response.build());
			return;
		}
		
		sendQueryFuwenResp(session, junZhu, tab);
	}

	public void sendQueryFuwenResp(IoSession session, JunZhu junZhu, int tab) {
		QueryFuwenResp.Builder response = QueryFuwenResp.newBuilder();
		queryFuwen(junZhu, response, tab);
		response.setResult(0);
		response.setTab(tab);
		session.write(response.build());
	}

	/**
	 * @Title: operateFuwen
	 * @Description: 操作符文
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
		int action = request.getAction();
		long bagId = request.getBagId();
		int tab = request.getTab();
		int lanweiId = request.getLanweiId();
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		boolean actionSucceed = false;
		switch (action) {
		// 5-装备符文，6-卸下符文
//		case 1:
//			actionSucceed = lockFuwen(junZhu, bagId, response,bag);
//			break;
//		case 2:
//			actionSucceed = unlockFuwen(junZhu, bagId, response, bag);
//			break;
//		case 3:
//			actionSucceed = combineFuwen(junZhu, bagId, lanweiId, response, bag);
//			break;
//		case 4:
//			actionSucceed = yijianCombineFuwen(junZhu, bagId, response,bag);
//			break;
		case 5:
			actionSucceed = equipFuwen(session, junZhu, bagId, tab, lanweiId, response,bag);
			break;
		case 6:
			actionSucceed = unloadFuwen(session, junZhu, tab, lanweiId, response,bag);
			break;
		default:
			break;
		}
		if (actionSucceed) {
			HibernateUtil.update(junZhu);
			JunZhuMgr.inst.sendMainInfo(session,junZhu);
			//BagMgr.inst.sendBagInfo(session, bag);
			JunZhuInfoRet.Builder jzB = JunZhuMgr.jzInfoCache.get(junZhu.id);
			int zhanLi = jzB==null?0:jzB.getZhanLi();
			response.setZhanli(zhanLi);
		} else {
			response.setResult(2);
		}
		session.write(response.build());
	}

	// 查询符文
	public void queryFuwen(JunZhu jz, QueryFuwenResp.Builder response, int tab) {
		// 战力
		JunZhuInfoRet.Builder jzB = JunZhuMgr.jzInfoCache.get(jz.id);
		int zhanLi = jzB==null?0:jzB.getZhanLi();
		response.setZhanli(zhanLi);
		// 对应符文页签上的栏位
		List<FuWenBean> fuWenBeanList = FuWenDao.inst.getFuwenByTab(jz.id, tab);
		if (fuWenBeanList == null || fuWenBeanList.size() == 0) {// 符文栏位初始化
			fuWenBeanList = initFuwenLanwei(jz, tab);
			FuWenDao.inst.fuwenCache.put(jz.id, fuWenBeanList);
		}
		
		Bag<BagGrid> bag = BagMgr.inst.loadBag(jz.id);
		List<FushiInBagInfo> fuwensInBag = getFushiInBag(bag);
		for (FuWenBean fwBean : fuWenBeanList) {
			FuwenLanwei.Builder lanweiBuilder = FuwenLanwei.newBuilder();
			lanweiBuilder.setLanweiId(fwBean.lanWeiId);
			lanweiBuilder.setItemId(fwBean.itemId);
			lanweiBuilder.setExp(fwBean.exp);
			// 判断符文红点推送
			int state = getLanweiPushState(fwBean, fwBean.itemId, fuwensInBag);
			if (state == RED_IS_CAN_EQUIP || state == RED_IS_CAN_UPGRADE) {
				lanweiBuilder.setFlag(true);
			} else {
				lanweiBuilder.setFlag(false);
			}
			response.addLanwei(lanweiBuilder);
		}
		/* 2016年4月28日 10:52:34 不需要发送君主的总属性值
		// 君主的总属性值
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
		response.addAttr(attr);*/
		// 所有符文的加成属性值
		JunzhuAttr.Builder jcAttr = getFuwenAttr(fuWenBeanList);
		jcAttr.setType(2);
		response.addAttr(jcAttr);
	}

	public void loadFuwenInBag(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.info("君主不存在");
			return;
		}
		sendFuwenInBagInfo(session, junZhu);
	}

	public void sendFuwenInBagInfo(IoSession session, JunZhu junZhu) {
		FuwenInBagResp.Builder response = FuwenInBagResp.newBuilder();
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		List<BagGrid> bagItemList = bag.grids;
		if (bagItemList != null && bagItemList.size() > 0) {
			for (BagGrid bagItem : bagItemList) {
				if (bagItem.type == 8 && bagItem.cnt > 0) {
					Fuwen fuwen = fuwenMap.get(bagItem.itemId);
					if(fuwen == null) {
						logger.error("查找背包的符文错误，找不到符文id为:{}的配置", bagItem.itemId);
						continue;
					}
					FuwenInBag.Builder fuwenBuilder = FuwenInBag.newBuilder();
					fuwenBuilder.setBagId(bagItem.dbId);
					fuwenBuilder.setItemId(fuwen.fuwenID);
					fuwenBuilder.setExp((int) bagItem.instId);
					fuwenBuilder.setCnt(bagItem.cnt);
					response.addFuwenList(fuwenBuilder);
				}
			}
		}
		session.write(response.build());
	}

	public FuwenInBag.Builder isFuwenInList(List<FuwenInBag.Builder> list, int itemId) {
		for (FuwenInBag.Builder fuwen : list) {
			if (fuwen.getItemId() == itemId) {
				return fuwen;
			}
		}
		return null;
	}

	// 锁定符文
	public boolean lockFuwen(JunZhu junZhu, int itemId,
			FuwenResp.Builder response, Bag<BagGrid> bag) {
		int itemCount = BagMgr.inst.getItemCount(bag, itemId);
		if (itemCount == 0) {
			logger.info("锁定符文，背包中不存在符文{}", itemId);
			response.setReason("背包中不存在此符文");
			return false;
		}
		if (redis.lexist(CACHE_FUWEN_LOCK + junZhu.id, String.valueOf(itemId))) {
			response.setReason("符文已经被锁定");
			return false;
		}
		redis.lpush_(CACHE_FUWEN_LOCK + junZhu.id, String.valueOf(itemId));// 添加
		logger.info("君主{}锁定符文{}", junZhu.id, itemId);
		return true;
	}

	// 解锁符文
	public boolean unlockFuwen(JunZhu junZhu, int itemId,
			FuwenResp.Builder response,Bag<BagGrid> bag) {
		int itemCount = BagMgr.inst.getItemCount(bag, itemId);
		if (itemCount == 0) {
			logger.info("解锁符文，背包中不存在符文{}", itemId);
			response.setReason("背包中不存在此符文");
			return false;
		}
		if (!redis.lexist(CACHE_FUWEN_LOCK + junZhu.id, String.valueOf(itemId))) {
			logger.info("解锁符文，符文{}没有锁定", itemId);
			return false;
		}
		redis.lrem(CACHE_FUWEN_LOCK + junZhu.id, 0, String.valueOf(itemId));// 移除
		logger.info("君主{}解锁符文{}", junZhu.id, itemId);
		return true;
	}

	// 装备符文
	public boolean equipFuwen(IoSession session, JunZhu junZhu, long bagId, int tab, int lanweiId,
			FuwenResp.Builder response, Bag<BagGrid> bag) {
		int itemCount = 0;
		int itemId = 0;
		int itemExp = 0;
		List<BagGrid> gridList = bag.grids;
		for(BagGrid grid : gridList){
			if(grid.dbId == bagId){
				itemCount = grid.cnt;
				itemId = grid.itemId;
				itemExp = (int) grid.instId;
				break;
			}
		}
		
		if (itemCount == 0) {
			logger.info("镶嵌符文失败，背包中不存在符文bag的dbId:{}, itemId:{}", bagId, itemId);
			response.setResult(2);
			return false;
		}
		Fuwen fuwen = fuwenMap.get(itemId);
		if(fuwen == null) {
			logger.error("镶嵌符文失败，找不到fuwen表id为:{}的配置", itemId);
			return false;
		}
		FuwenOpen lanweiCfg = fuwenOpenMap.get(lanweiId);
		if(lanweiCfg == null) {
			logger.error("镶嵌符文失败，找不到FuwenOpen表id为:{}的配置", lanweiId);
			return false;
		}

		List<FuWenBean> fuWenBeanList = FuWenDao.inst.getFuwenByTab(junZhu.id, tab);
		if (fuWenBeanList == null || fuWenBeanList.size() == 0) {
			logger.error("镶嵌符文失败，君主{}未开启符文页签:{}", junZhu.id,tab);
			response.setResult(3);
			return false;
		}
		
		// 判断同属性栏位
		if (fuwen.inlayColor != lanweiCfg.inlayColor) {
			logger.info("镶嵌符文失败，符文栏位{}属性与符文属性不一致，不能镶嵌", lanweiId, fuwen.fuwenID);
			response.setResult(4);
			return false;
		}
		
		boolean replace = false;
		FuWenBean fwBean = getFuwenBeanByLanweiId(lanweiId, fuWenBeanList);
		if(fwBean == null) {
			logger.info("镶嵌符文失败，找不到君主:{} lanweiId:{}的FuwenBean", junZhu.id, lanweiId);
			return false;
		}
		if (fwBean != null && fwBean.itemId > 0) {
			logger.info("镶嵌符文操作，符文栏位{}已经有符文，本次为替换符文", lanweiId);
			replace = true;
		}
		if(replace) {
			Fuwen equipedFuwenCfg = fuwenMap.get(fwBean.itemId);
			if(equipedFuwenCfg == null) {
				logger.error("镶嵌符文失败，找不到fuwen表id为:{}的配置2", itemId);
				return false;
			}
			if(equipedFuwenCfg.color > fuwen.color) {
				logger.error("镶嵌符文失败，被替换的符文的品质大于要镶嵌的符文");
				response.setResult(6);
				return false;
			}
			if(equipedFuwenCfg.color == fuwen.color
					&& equipedFuwenCfg.shuxingValue >= fuwen.shuxingValue) {
				logger.error("镶嵌符文失败，被替换的符文的等级或者经验值大于要镶嵌的符文");
				response.setResult(4);
				return false;
			}
		}
		
		response.setResult(0);
		session.write(response.build());
		
		int beforeItemId = fwBean.itemId;
		int beforeExp = fwBean.exp;
		fwBean.itemId = fuwen.fuwenID;
		fwBean.exp = itemExp;
		HibernateUtil.save(fwBean);
		logger.info("君主{}镶嵌符文{}到栏位{}", junZhu.id, itemId, lanweiId);
		// 从背包消耗一个符文
		BagMgr.inst.removeItemByBagdbId(session, bag, "镶嵌消耗一个符文", bagId, 1, junZhu.level);
		if(replace) {
			BagMgr.inst.addItem(session, bag, beforeItemId, 1, beforeExp, junZhu.level, "镶嵌符文替换下的");
		}
		sendFuwenInBagInfo(session, junZhu);
		// 主线任务: 装备任意一个符文 20190916
		EventMgr.addEvent(junZhu.id, ED.wear_fushi, new Object[] { junZhu.id });
		return true;
	}

	public void equipFuwenAll(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.info("君主不存在");
			return;
		}
		
		FuwenEquipAll.Builder request = (qxmobile.protobuf.FuWen.FuwenEquipAll.Builder) builder;
		int tab = request.getTab();
		
		FuwenEquipAllResp.Builder response = FuwenEquipAllResp.newBuilder();
		FuwenTab fuwenTab = fuwenTabMap.get(tab);
		if(fuwenTab == null) {
			logger.error("一键镶嵌符文失败，找不到fuwentab为:{}的配置", tab);
			return;
		}
		if(junZhu.level < fuwenTab.level) { 
			logger.error("一键镶嵌符文失败，君主:{} 等级:{} 不满足符文页面:{}的等级:{}条件",junZhu.id, junZhu.level,tab,fuwenTab.level);
			response.setResult(2);
			session.write(response.build());
			return;
		}
		
		boolean performed = false;
		// 获取背包物品是符文的格子
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		List<BagGrid> fuwenGridList = new ArrayList<>();
		List<BagGrid> gridList = bag.grids;
		for(BagGrid grid : gridList){
			if(grid.type == AwardMgr.type_fuWen){
				fuwenGridList.add(grid);
			}
		}
		
		List<FuWenBean> fuWenBeanList = FuWenDao.inst.getFuwenByTab(junZhu.id, tab);
		for(FuWenBean fwBean : fuWenBeanList) {
			FuwenOpen fuwenOpenCfg = fuwenOpenMap.get(fwBean.lanWeiId);
			if(fuwenOpenCfg == null) {
				logger.error("一键镶嵌符文失败，找不到fuwenOpen表id为:{}的配置", fwBean.lanWeiId);
				continue;
			}
			
			if(fwBean.itemId > 0) {					// 表示原来已经镶嵌了符文，接下来做替换操作
				Fuwen fuwen = fuwenMap.get(fwBean.itemId);
				if(fuwen == null) {
					continue;
				}
				for(BagGrid grid : fuwenGridList){
					Fuwen fuwenCfgInBag = fuwenMap.get(grid.itemId);
					if(fuwenCfgInBag == null) {
						logger.error("一键镶嵌符文失败，找不到fuwen表id为:{}的配置", grid.itemId);
						continue;
					}
					// 提供的属性不一致的不能替换
					if(fuwenCfgInBag.inlayColor != fuwenOpenCfg.inlayColor) {
						continue;
					}
					// 低于当前符文的品质的不能替换
					if(fuwenCfgInBag.color < fuwen.color) {				
						continue;
					}
					// 品质相同时，提供的属性值比当前的小，不能替换
					if(fuwenCfgInBag.color == fuwen.color &&
							fuwenCfgInBag.shuxingValue <= fuwen.shuxingValue) {
						continue;
					}
					int beforeItemId = fwBean.itemId;
					int beforeExp = fwBean.exp;
					fwBean.itemId = grid.itemId;
					fwBean.exp = (int) grid.instId;
					HibernateUtil.save(fwBean);
					BagMgr.inst.removeItemByBagdbId(session, bag, "镶嵌消耗一个符文", grid.dbId, 1, junZhu.level);
					BagMgr.inst.addItem(session, bag, beforeItemId, 1, beforeExp, junZhu.level, "镶嵌符文替换下的");
					performed = true;
				}		
			} else {										// 表示当前位置还没有镶嵌符文
				BagGrid maxExpGrid = null;	// 当前符文经验最大的
				for(BagGrid grid : fuwenGridList){
					Fuwen fuwenCfg = fuwenMap.get(grid.itemId);
					if(fuwenCfg == null) {
						logger.error("一键镶嵌符文失败，找不到fuwen表id为:{}的配置", grid.itemId);
						continue;
					}
					if(fuwenCfg.inlayColor != fuwenOpenCfg.inlayColor) {
						continue;
					}
					if(maxExpGrid == null) {
						maxExpGrid = grid;
						continue;
					}
					Fuwen fuwenCfg4Grid = fuwenMap.get(maxExpGrid.itemId);
					if(fuwenCfg4Grid == null) {
						logger.error("一键镶嵌符文失败，找不到fuwen表id为:{}的配置2", grid.itemId);
						continue;
					}
					// 选择品质较大的
					if(fuwenCfg.color > fuwenCfg4Grid.color) {
						maxExpGrid = grid;
						continue;
					} 
					// 品质相同，选择等级最大的
					if(fuwenCfg.color == fuwenCfg4Grid.color) {
						if(fuwenCfg.fuwenLevel > fuwenCfg4Grid.fuwenLevel) {
							maxExpGrid = grid;
							continue;
						}
						// 提供属性值相同，选择经验值最大的
						if(fuwenCfg.fuwenLevel == fuwenCfg4Grid.fuwenLevel) {
							if(grid.instId > maxExpGrid.instId) {
								maxExpGrid = grid;
							}
						}
					}
				}
				if(maxExpGrid != null){				// 为null表示没有此类型的符文
					fwBean.itemId = maxExpGrid.itemId;
					fwBean.exp = (int) maxExpGrid.instId;
					HibernateUtil.save(fwBean);
					BagMgr.inst.removeItemByBagdbId(session, bag, "镶嵌消耗一个符文", maxExpGrid.dbId, 1, junZhu.level);
					performed = true;
				}
			}
		}
		
//		sendFuwenInBagInfo(session, junZhu);
//		sendQueryFuwenResp(session, junZhu, tab);
		if(performed) {
			response.setResult(0);
			EventMgr.addEvent(junZhu.id, ED.wear_fushi, new Object[] { junZhu.id });
		} else {
			response.setResult(1);
		}
		JunZhuMgr.inst.sendMainInfo(session);
		session.write(response.build());
	}
	
	// 卸下符文
	public boolean unloadFuwen(IoSession session, JunZhu junZhu, int tab, int lanweiId,
			FuwenResp.Builder response,Bag<BagGrid> bag) {
		List<FuWenBean> fuWenBeanList = FuWenDao.inst.getFuwenByTab(junZhu.id, tab);
		if (fuWenBeanList == null || fuWenBeanList.size() == 0) {
			logger.info("君主{}未开启符文系统", junZhu.id);
			response.setResult(6);
			return false;
		}
		FuWenBean fwBean = getFuwenBeanByLanweiId(lanweiId, fuWenBeanList);
		if (fwBean == null) {
			logger.info("卸下符文，符文栏位{}没有镶嵌符文", lanweiId);
			response.setResult(7);
			return false;
		}
		response.setResult(1);
		session.write(response.build());
		int itemExp = fwBean.exp; 
		BagMgr.inst.addItem(session, bag, fwBean.itemId, 1, itemExp, junZhu.level, "卸下符文");
		fwBean.itemId = 0;
		fwBean.exp = 0;
		HibernateUtil.save(fwBean);
		sendFuwenInBagInfo(session, junZhu);
		logger.info("君主{}卸下{}栏位的符文{}", junZhu.id, lanweiId, fwBean.itemId);
		return true;
	}
	
	// 卸下符文
	public void unloadFuwenAll(int cmd, IoSession session, Builder builder) {
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (junZhu == null) {
			logger.info("君主不存在");
			return;
		}
		
		FuwenUnloadAll.Builder request = (qxmobile.protobuf.FuWen.FuwenUnloadAll.Builder) builder;
		int tab = request.getTab();
		
		FuwenUnloadAllResp.Builder response = FuwenUnloadAllResp.newBuilder();
		FuwenTab fuwenTab = fuwenTabMap.get(tab);
		if(fuwenTab == null) {
			logger.error("一键拆卸符文失败，找不到fuwentab为:{}的配置", tab);
			return;
		}
		if(junZhu.level < fuwenTab.level) { 
			logger.error("一键拆卸符文失败，君主:{} 等级:{} 不满足符文页面:{}的等级:{}条件",junZhu.id, junZhu.level,tab,fuwenTab.level);
			response.setResult(2);
			session.write(response.build());
			return;
		}
		
		List<FuWenBean> fuWenBeanList = FuWenDao.inst.getFuwenByTab(junZhu.id, tab);
		if (fuWenBeanList == null || fuWenBeanList.size() == 0) {
			logger.info("君主{}未开启符文系统", junZhu.id);
			return;
		}
		
		boolean performed = false;
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		for (FuWenBean fwBean : fuWenBeanList) {// 遍历君主所有符文栏位
			if(fwBean.itemId <= 0) {
				continue;
			}
			BagMgr.inst.addItem(session, bag, fwBean.itemId, 1, fwBean.exp, junZhu.level, "卸下符文");
			fwBean.itemId = 0;
			fwBean.exp = 0;
			HibernateUtil.save(fwBean);
			performed = true;
		}
		if(performed){
			response.setResult(0); 
		} else {
			response.setResult(1); 
		}
//		sendFuwenInBagInfo(session, junZhu);
//		sendQueryFuwenResp(session, junZhu, tab);
		JunZhuMgr.inst.sendMainInfo(session);
		session.write(response.build());
		logger.info("一键拆卸符文成功，君主{}卸下了页签:{}的所有符文", junZhu.id, tab);
		return;
	}

	// 合成符文
	public boolean combineFuwen(JunZhu junZhu, int itemId, int lanweiId,
			FuwenResp.Builder response,Bag<BagGrid> bag) {
		IoSession session = SessionManager.inst.getIoSession(junZhu.id);
		// lock
		if (redis.lexist(CACHE_FUWEN_LOCK + junZhu.id, String.valueOf(itemId))) {
			logger.info("君主{}的符文{}已锁定，不能合成", junZhu.id, itemId);
			response.setReason("符文已锁定。");
			return false;
		}
		// level
		Fuwen fuwen = fuwenMap.get(itemId);
		if (fuwen.fuwenNext == -1) {
			logger.info("君主{}合成符文{}失败，符文已是最高等级{}", junZhu.id, itemId,
					fuwen.fuwenLevel);
			response.setReason("符文已达到最高等级。");
			return false;
		}
		Fuwen fuwenNext = fuwenMap.get(fuwen.fuwenNext);
		// Bag
		int num = BagMgr.inst.getItemCount(bag, itemId);
		if (lanweiId == 0) {// 从背包合成
			if (num < COMBINE_NUM) {
				logger.info("君主{}要合成的符文{}数量{}不足最低数量{}", junZhu.id, itemId, num,
						COMBINE_NUM);
				response.setReason("符文数量不足最低合成数量。");
				return false;
			}
			BagMgr.inst.removeItem(session, bag, itemId, COMBINE_NUM, "普通合成消耗"
					+ COMBINE_NUM + "个符文", junZhu.level);
			BagMgr.inst.addItem(session, bag, fuwenNext.fuwenID, 1, -1,
					junZhu.level, "合成符文");
			logger.info("君主{}成功消耗{}个符文{}合成一个高级符文{}", junZhu.id, COMBINE_NUM,
					itemId, fuwenNext.fuwenID);
			EventMgr.addEvent(junZhu.id, ED.GAIN_ITEM,
					new Object[] { junZhu.id, fuwenNext.fuwenID });
		} else {// 从装备的栏位直接合成
			if (num < COMBINE_NUM - 1) {
				logger.info("君主{}要合成的符文{}数量{}不足最低数量{}", junZhu.id, itemId,
						num + 1, COMBINE_NUM);
				response.setReason("符文数量不足最低合成数量。");
				return false;
			}
			BagMgr.inst.removeItem(session, bag, itemId, COMBINE_NUM - 1, "普通合成消耗"
					+ (COMBINE_NUM - 1) + "个符文", junZhu.level);
			redis.lrem(CACHE_FUWEN_LANWEI + junZhu.id, 0, lanweiId + "#"
					+ itemId);
			redis.lpush_(CACHE_FUWEN_LANWEI + junZhu.id, lanweiId + "#"
					+ fuwenNext.fuwenID);
			logger.info("君主{}成功消耗{}个符文{}合成一个高级符文{}", junZhu.id,
					COMBINE_NUM - 1, itemId, fuwenNext.fuwenID);
			EventMgr.addEvent(junZhu.id, ED.GAIN_ITEM,
					new Object[] { junZhu.id, fuwenNext.fuwenID });
		}
		return true;
	}

	// 一键合成符文
	public boolean yijianCombineFuwen(JunZhu junZhu, int itemId,
			FuwenResp.Builder response,Bag<BagGrid> bag) {
		IoSession session = SessionManager.inst.getIoSession(junZhu.id);
		// lock
		if (redis.lexist(CACHE_FUWEN_LOCK + junZhu.id, String.valueOf(itemId))) {
			logger.info("君主{}的符文{}已锁定，不能合成", junZhu.id, itemId);
			response.setReason("符文已锁定");
			return false;
		}
		// level
		Fuwen fuwen = fuwenMap.get(itemId);
		if (fuwen.fuwenNext == -1) {
			logger.info("君主{}合成符文{}失败，符文已是最高等级{}", junZhu.id, itemId,
					fuwen.fuwenLevel);
			response.setReason("符文已达到最高等级");
			return false;
		}
		Fuwen fuwenNext = fuwenMap.get(fuwen.fuwenNext);
		// Bag
		int num = BagMgr.inst.getItemCount(bag, itemId);
		if (num < COMBINE_NUM * 2) {// 背包没有符文
			logger.info("一键合成失败，君主{}背包符文{}数量不足一键合成最低要求数量{}", junZhu.id, itemId,
					COMBINE_NUM * 2);
			response.setReason("符文数量不足最低合成数量");
			return false;
		}
		int combineNum = num / COMBINE_NUM;
		int costNum = num - num % COMBINE_NUM;
		BagMgr.inst.removeItem(session, bag, itemId, costNum,
				"一键合成消耗" + costNum + "个符文", junZhu.level);
		BagMgr.inst.addItem(session, bag, fuwenNext.fuwenID, combineNum, -1,
				junZhu.level, "一键合成符文");
		logger.info("君主{}成功消耗{}个符文{}合成{}个高级符文{}", junZhu.id, costNum, itemId,
				combineNum, fuwenNext.fuwenID);

		EventMgr.addEvent(junZhu.id, ED.GAIN_ITEM,
				new Object[] { junZhu.id, fuwenNext.fuwenID });
		return true;
	}

	// 初始化符文栏位
	public List<FuWenBean> initFuwenLanwei(JunZhu jz, int tab) {
		List<FuWenBean> fuWenBeanList = new ArrayList<>();
		FuwenTab fuwenTab = fuwenTabMap.get(tab);
		if(fuwenTab == null) {
			logger.error("初始化符文信息失败，找不到fuwentab为:{}的配置", tab);
			return fuWenBeanList;
		}
		if(jz.level < fuwenTab.level) {
			logger.error("初始化符文信息失败，君主:{}等级不满足符文页签:{}等级条件", jz.id, tab);
			return fuWenBeanList;
		}
		List<FuwenOpen> fuwenOpenList = fuwenOpenMapByTab.get(tab);
		for(FuwenOpen openCfg : fuwenOpenList) {
			FuWenBean fuWenBean = new FuWenBean();
			fuWenBean.junzhuId = jz.id;
			fuWenBean.tab = openCfg.tab;
			fuWenBean.itemId = 0;
			fuWenBean.lanWeiId = openCfg.id;
			fuWenBean.exp = 0;
			HibernateUtil.insert(fuWenBean);
			fuWenBeanList.add(fuWenBean);
		}
		logger.info("初始化君主:{}所有符文栏位", jz.id);
		return fuWenBeanList;
	}

	// 检查符文栏位解锁
	public void checkFuwenUnlock(JunZhu jz) {
		if (jz == null) {
			logger.error("事件：检查符文解锁，参数不正确");
			return;
		}
		
		//TODO 红点
		/*for (int key : fuwenOpenMap.keySet()) {
			FuwenOpen fuwenOpen = fuwenOpenMap.get(key);
			if (fuwenOpen.getLevel() <= jz.level) {
				List<String> list = redis.lgetList(CACHE_FUWEN_LANWEI + jz.id);
				if (null == list || list.size() == 0) {
					list = redis.lgetList(CACHE_FUWEN_LANWEI + jz.id);
					// logger.error("君主{}符文系统未开启", jz.id);
				}
			}
		}*/
	}

	// 获取符文加成属性
	public JunzhuAttr.Builder getFuwenAttr(List<FuWenBean> fuWenBeanList) {
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
		for (FuWenBean fwBean : fuWenBeanList) {// 遍历君主所有符文栏位
			int itemId = fwBean.itemId;
			if (itemId > 0) {
				Fuwen fuwen = fuwenMap.get(itemId);
				if(fuwen == null) {
					logger.error("找不到符文id为:{}的配置", itemId);
					continue;
				}
				double addValue = fuwen.shuxingValue;
				switch (fuwen.shuxing) {
				case 1:
					gongji += addValue;
					break;
				case 2:
					fangyu += addValue;
					break;
				case 3:
					shengming += addValue;
					break;
				case 4:
					wqSH += addValue;
					break;
				case 5:
					wqJM += addValue;
					break;
				case 6:
					wqBJ += addValue;
					break;
				case 7:
					wqRX += addValue;
					break;
				case 8:
					jnSH += addValue;
					break;
				case 9:
					jnJM += addValue;
					break;
				case 10:
					jnBJ += addValue;
					break;
				case 11:
					jnRX += addValue;
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

	/*  2016年4月28日 10:05:47，取消的符文套装效果
	// 获取符文套装效果
	public Map<Integer, Double> getFuwenSuitMap(List<FuWenBean> fuWenBeanList) {
		Map<Integer, Double> suitMap = new HashMap<Integer, Double>();// 符文页套装效果
		for (int suitIndex : fuwenSuitMap.keySet()) {
			List<FuwenOpen> suitLanweiList = fuwenSuitMap.get(suitIndex);
			boolean flag = true;
			int minLevel = 11;
			for (FuwenOpen fuwenOpen : suitLanweiList) {
				Integer itemiId = getItemIdByLanweiId(fuwenOpen.id, fuWenBeanList);
				if (itemiId != null) {
					if (itemiId.intValue() <= 0) {
						flag = false;
						break;
					}
					Fuwen fuwen = fuwenMap.get(itemiId);
					if(fuwen == null) {
						logger.error("找不到符文id为:{}的配置", itemiId);
						continue;
					}
					minLevel = fuwen.fuwenLevel <= minLevel ? fuwen
							.fuwenLevel : minLevel;
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
	}*/

	// 获取符文栏位上的符文id
	public FuWenBean getFuwenBeanByLanweiId(int lanweiId, List<FuWenBean> fuWenBeanList) {
		for (FuWenBean fwBean : fuWenBeanList) {// 遍历君主所有符文栏位
			if (fwBean.lanWeiId == lanweiId) {
				return fwBean;
			}
		}
		return null;
	}

	/**
	 * @Title: getFuShiProgress
	 * @Description: 获取符文养成进度
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
	 * @Description: 获取符文当前进度
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
		
		int totalProgress = 0;
		List<FuWenBean> fuWenBeanList = FuWenDao.inst.getFuwenAll(junZhu.id);
		if (fuWenBeanList == null || fuWenBeanList.size() == 0) {
			return 0;
		}
		
		for(FuWenBean fwBean : fuWenBeanList) {
			Fuwen fuwenCfg = null;
			if(fwBean.itemId <= 0) {
				continue;
			} 
			fuwenCfg = fuwenMap.get(fwBean.itemId);
			if(fuwenCfg == null) {
				continue;
			}
			int curProgress = fuwenCfg.fuwenLevel * fuwenCfg.pinzhi;
			totalProgress += curProgress;
		}
		return totalProgress;
	}

	/**
	 * @Title: getFushiMaxLevel
	 * @Description: 获取符文总进度
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
			logger.info("君主{}未开启符文系统", junZhu.id);
			return 0;
		}
		int availableSum = 0;
		for (String lanwei : list) {
			int lanweiId = Integer.parseInt(lanwei.split("#")[0]);
			int itemId = Integer.parseInt(lanwei.split("#")[1]);
			FuwenOpen fuwenOpen = fuwenOpenMap.get(lanweiId);
			if (itemId >= 0) {
				availableSum++;
			}
		}
		return availableSum * 11;
	}

	/**
	 * @Title: getFuShiTuijian
	 * @Description: 获取符文推荐
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
			logger.info("君主{}未开启符文系统", junZhu.id);
			return null;
		}
		// 添加装备上的符文
		List<Fuwen> fuwenList = new ArrayList<Fuwen>();
		for (String lanwei : list) {
			int itemId = Integer.parseInt(lanwei.split("#")[1]);
			Fuwen fuwen = fuwenMap.get(itemId);
			if (fuwen != null) {
				if (type == 1 && fuwen.shuxing > 3
						&& !fuwenList.contains(fuwen)) {// 符文
					fuwenList.add(fuwen);
				} else if (type == 2 && fuwen.shuxing <= 3
						&& !fuwenList.contains(fuwen)) {// 宝石
					fuwenList.add(fuwen);
				}
			}
		}
		// 添加背包中的符文
		/*List<Fuwen> fushiBag = getFushiInBag(jzId);
		for (Fuwen fuwen : fushiBag) {
			if (type == 1 && fuwen.shuxing > 3
					&& !fuwenList.contains(fuwen)) {// 符文
				fuwenList.add(fuwen);
			} else if (type == 2 && fuwen.shuxing <= 3
					&& !fuwenList.contains(fuwen)) {// 宝石
				fuwenList.add(fuwen);
			}
		}*/
		Collections.sort(fuwenList, new Comparator<Fuwen>() {// 排序
					@Override
					public int compare(Fuwen f1, Fuwen f2) {
						if (f1.fuwenLevel < f2.fuwenLevel) {
							return -1;
						} else if (f1.fuwenLevel == f2.fuwenLevel) {
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
		if(size > 0 && fuwenList.get(0).fuwenLevel == 11){
			fuwenList = new ArrayList<Fuwen>();
		}
		return fuwenList;
	}

	/**
	 * @Title: getEquipedFushi
	 * @Description: 获取已装备的符文
	 * @param jzId
	 * @return
	 * @throws
	 */
	public boolean  getFushiEquiped(long jzId) {
		List<FuWenBean> fuWenBeanList = FuWenDao.inst.getFuwenAll(jzId);
		if (fuWenBeanList == null || fuWenBeanList.size() == 0) {
			logger.info("君主{}未开启符文系统", jzId);
			return false;
		}
		for (FuWenBean fwBean : fuWenBeanList) {
			if(fwBean.itemId > 0) {
				return true;
			}
		}
		return false ;
	}

	/**
	 * @Title: getFushiInBag
	 * @Description: 获取背包的符文
	 * @param bag
	 * @return List<FushiInBagInfo>
	 * @throws
	 */
	public List<FushiInBagInfo> getFushiInBag(Bag<BagGrid> bag) {
		List<FushiInBagInfo> fuwenInBagList = new ArrayList<FushiInBagInfo>();
		List<BagGrid> list = bag.grids;
		for (BagGrid item : list) {
			if (item.type == 8 && item.cnt > 0) {
				Fuwen fuwen = fuwenMap.get(item.itemId);
				if(fuwen == null){
					continue;
				}
				FushiInBagInfo fsInBag = new FushiInBagInfo(fuwen, item.cnt);
				fuwenInBagList.add(fsInBag);
			}
		}
		return fuwenInBagList;
	}

	/**
	 * @Title: getFushi  
	 * @Description: 获取所有的符文
	 * @param jzId
	 * @return
	 * @return List<Fuwen>
	 * @throws
	 */
//	public List<Fuwen> getFushi(long jzId) {
//		// FIXME 暂时没用到，方法意思也不明确，问题：获取背包的符文是否需要数量？
//		Bag<BagGrid> bag = BagMgr.inst.loadBag(jzId);
//		List<Fuwen> fuwenList = new ArrayList<Fuwen>();
//		List<Fuwen> equipList = getFushiEquiped(jzId);
//		List<FushiInBagInfo> bagList = getFushiInBag(bag);
//		if (equipList != null) {
//			fuwenList.addAll(getFushiEquiped(jzId));
//		}
//		if (bagList != null) {
//			for(FushiInBagInfo fsInBag : bagList) {
//				fuwenList.add(fsInBag.fuwen);
//			}
//		}
//		return fuwenList;
//	}

	public void pushFushi(JunZhu jz) {
		if(jz == null){
			return;
		}
		long jzId = jz.id;
		IoSession session = SessionManager.inst.getIoSession(jzId);
		if (null == session) {
			logger.info("君主{}符文推送异常,session为null", jzId);
			return;
		}
		boolean isOpen=FunctionOpenMgr.inst.isFunctionOpen(FunctionID.FuWen, jz.id, jz.level);
		if(!isOpen){
			return;
		}
		
		int state = getTuisongState(jzId);
		// 当前有未镶嵌符文的栏位且有可镶嵌的符文
		if((state & RED_IS_CAN_EQUIP) == RED_IS_CAN_EQUIP) {
			logger.info("符文推送红点==>君主{}有未镶嵌符文的栏位且有可镶嵌的符文", jzId);
			FunctionID.pushCanShowRed(jzId, session, FunctionID.FU_WEN_EQUIP);
		}
		// 已镶嵌的符文中有可以融合升级
		if((state & RED_IS_CAN_UPGRADE) == RED_IS_CAN_UPGRADE) {
			logger.info("符文推送红点==>君主{}栏位有符文可以升级", jzId);
			FunctionID.pushCanShowRed(jzId, session, FunctionID.FU_WEN_UPGRADE);
		}
		// 当前栏位有更高级的符文可以替换
		if((state & RED_IS_CAN_REPLACE) == RED_IS_CAN_REPLACE) {
			logger.info("符文推送红点==>君主{}栏位有可替换的符文", jzId);
			FunctionID.pushCanShowRed(jzId, session, FunctionID.FU_WEN_TIHUAN);
		}
	}

	/**
	 * @Title: getTuisongState
	 * @Description: 获取符文推送红点状态
	 * @param jzId
	 * @return -1-参数错误 0-不需推送 1-当前有未镶嵌符文的栏位且有可镶嵌的符文 ，2-已镶嵌的符文中有可以融合升级的，3-当前栏位有更高级的符文可以替换
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
		Bag<BagGrid> bag = BagMgr.inst.loadBag(jzId);
		List<FushiInBagInfo> fuwens = getFushiInBag(bag);// 背包中的符文
		if (fuwens == null || fuwens.size() == 0) {
			logger.info("君主{}背包中没有符文", jzId);
			return 0;
		}
		
		int ret = 0;
		for(Map.Entry<Integer, FuwenTab> entry : fuwenTabMap.entrySet()) {
			if(junZhu.level < entry.getValue().level) {
				logger.info("君主{} tab:{}的符文为解锁", jzId, entry.getKey());
				continue;
			}
			List<FuWenBean> fuWenBeanList = FuWenDao.inst.getFuwenByTab(junZhu.id, entry.getKey());
			if (fuWenBeanList == null || fuWenBeanList.size() == 0) {
				continue;
			}
			for (FuWenBean fwBean : fuWenBeanList) {
				// 1-当前有未镶嵌符文的栏位且有可镶嵌的符文 ，2-已镶嵌的符文中有可以融合升级的
				int state = getLanweiPushState(fwBean, fwBean.itemId, fuwens);
				ret = ret | state;
				// 3-当前栏位有更高级的符文可以替换
				if(fwBean.itemId == 0) {
					continue;
				}
				Fuwen equipFuwen = fuwenMap.get(fwBean.itemId);
				if(equipFuwen == null) {
					continue;
				}
				for(FushiInBagInfo fsInfo : fuwens) {
					if(fsInfo.fuwen == null || fsInfo.fuwen.fuwenLevel == fsInfo.fuwen.levelMax) {
						continue;
					}
					if(fsInfo.fuwen.inlayColor != equipFuwen.inlayColor) {
						continue;
					}
					if(fsInfo.fuwen.color > equipFuwen.color) {
						ret = ret | RED_IS_CAN_REPLACE;
					}
					if(fsInfo.fuwen.color == equipFuwen.color &&
							fsInfo.fuwen.fuwenLevel > equipFuwen.fuwenLevel) {
						ret = ret | RED_IS_CAN_REPLACE;
					}
				}
			}
		}
		return ret;
	}

	/**
	 * @Title: getLanweiPushState
	 * @Description: 获取栏位推送状态 0-不需推送 1-背包中有可镶嵌此栏位的符文 2-背包中有相同属性更高级的符文
	 * @param lanweiId
	 * @param itemId
	 * @param fuwens
	 * @return
	 * @return int
	 * @throws
	 */
	public int getLanweiPushState(FuWenBean fuWenBean, int itemId, List<FushiInBagInfo> fuwensInBag) {
		FuwenOpen fuwenOpen = fuwenOpenMap.get(fuWenBean.lanWeiId);
		if (itemId == 0) {// 栏位解锁且没有符文
			for (FushiInBagInfo fsInBag : fuwensInBag) {// 遍历背包中的符文
				Fuwen fuwen = fsInBag.fuwen;
				if(fuwen == null) {
					logger.error("获取栏位推送状态错误，找不到符文id为:{}的配置", itemId);
					continue;
				}
				// 判断同属性栏位 
				if (fuwen.inlayColor == fuwenOpen.inlayColor) {// 背包中有可镶嵌此栏位的符文
					return RED_IS_CAN_EQUIP;
				}
			}
		} else if (itemId > 0) {// 栏位有符文
			Fuwen fuwenEquiped = fuwenMap.get(itemId);
			if(fuwenEquiped == null) {
				logger.error("获取栏位推送状态错误，找不到符文id为:{}的配置", itemId);
				return 0;
			}
			if(fuwenEquiped.fuwenLevel == MaxFuwenLevel){
				logger.info("栏位有符文itemId--{},等级达到--{}级,返回0",itemId,fuwenEquiped.fuwenLevel);
				return 0;
			}
			int provideExpTotal = 0;
			for (FushiInBagInfo fsInBag : fuwensInBag) {// 遍历背包中的符文
				Fuwen fuwen = fsInBag.fuwen;
				if(fuwen == null) {
					logger.error("获取栏位推送状态错误，找不到符文id为:{}的配置", itemId);
					continue;
				}
				if(fuwenEquiped.color < fuwen.color){
					continue;
				}
				provideExpTotal += (fuwen.exp * fsInBag.count);
				/*if (fuwen.inlayColor == fuwenEquiped.inlayColor
						&& fuwen.fuwenLevel > fuwenEquiped.fuwenLevel) {// 背包中有相同属性更高级的符文
					return 2;
				}*/
				//  2016年5月21日 21:38:38 改为只要可以升级就提示红点
				if(fuWenBean.exp + provideExpTotal >= fuwenEquiped.lvlupExp) {
					return RED_IS_CAN_UPGRADE;
				}
			}
		}
		return 0;
	}
	
	public class FushiInBagInfo {
		public Fuwen fuwen;
		public int count;
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
		case ED.JUNZHU_LOGIN:
			pushFushi(jz);
			break;
		default:
			break;
		}
	}

	@Override
	public void doReg() {
		EventMgr.regist(ED.CHECK_FUWEN_UOLOCK, this);
		EventMgr.regist(ED.JUNZHU_LOGIN, this);
	}

	public void rongHeFuwen(int id, IoSession session, Builder builder) {
		FuwenRongHeReq.Builder request = (qxmobile.protobuf.FuWen.FuwenRongHeReq.Builder) builder;
		int tab = request.getTab();
		int lanweiId = request.getLanweiId();
		List<FuwenInBag> bagList = request.getBagListList();
		
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (null == junZhu) {
			logger.error("符文兑换错误，君主不存在");
			return;
		}
		
		FuWenBean fwBean = null;
		List<FuWenBean> fuWenBeanList = FuWenDao.inst.getFuwenByTab(junZhu.id, tab);
		for(FuWenBean fwb : fuWenBeanList) {
			if(fwb.lanWeiId == lanweiId) {
				fwBean = fwb;
				break;
			}
		}
		FuwenRongHeResp.Builder response = FuwenRongHeResp.newBuilder();
		response.setTab(tab);
		response.setLanweiId(lanweiId);
		if(fwBean == null) {
			logger.error("符文融合失败，找不到要融合的符文bean，tab:{},lanweiId:{},junzhuId:{}", tab, lanweiId, junZhu.id);
			response.setResult(1);
			session.write(response.build());
			return;
		}
		Fuwen zhudongFuwen = fuwenMap.get(fwBean.itemId);
		if(zhudongFuwen == null) {
			logger.error("符文融合失败，找不到itemId为{}的符文配置1", fwBean.itemId);
			return;
		}
		
		int expTotal = 0;
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		List<BagGrid> bagItemList = bag.grids;
		if (bagItemList != null && bagItemList.size() > 0) {
			for(FuwenInBag fuwenInBag : bagList) {
				BagGrid xiaoHaoGrid = null;
				for (BagGrid bagItem : bagItemList) {
					if (bagItem.type != AwardMgr.type_fuWen) {
						continue;
					}
					if(bagItem.dbId == fuwenInBag.getBagId()) {
						xiaoHaoGrid = bagItem;
						break;
					}
				}
				if(xiaoHaoGrid == null) {
					logger.error("符文融合失败，找不到背包dbId为:{}的被消耗的符文", fuwenInBag.getBagId());
					response.setResult(2);
					session.write(response.build());
					return;
				}
				if(xiaoHaoGrid.cnt < fuwenInBag.getCnt()) {
					logger.error("符文融合失败，背包dbId为:{}的数量:{}不足:{}", fuwenInBag.getBagId(), xiaoHaoGrid.cnt, fuwenInBag.getCnt());
					response.setResult(3);
					session.write(response.build());
					return;
				}
				Fuwen xiaoHaoFuwen = fuwenMap.get(xiaoHaoGrid.itemId);
				if(xiaoHaoFuwen == null) {
					logger.error("符文融合失败，找不到itemId为{}的符文配置2", xiaoHaoGrid.itemId);
					return;
				}
				if(xiaoHaoFuwen.color > zhudongFuwen.color) {
					response.setResult(4);
					session.write(response.build());
					logger.error("符文融合失败，融合时选择的符文品质不能比当前的符文品质高", xiaoHaoGrid.itemId);
					return;
				}
				expTotal += (xiaoHaoGrid.instId + xiaoHaoFuwen.exp) * fuwenInBag.getCnt();
			}
		}
		for(FuwenInBag fuwenInBag : bagList) {
			BagMgr.inst.removeItemByBagdbId(session, bag, "符文融合被消耗", fuwenInBag.getBagId(), fuwenInBag.getCnt(), junZhu.level);
		}
		fwBean.exp += expTotal;
		Fuwen zhudongFWTemp = zhudongFuwen;
		while(fwBean.exp >= zhudongFWTemp.lvlupExp && zhudongFWTemp.fuwenLevel < zhudongFWTemp.levelMax) {
			fwBean.itemId = zhudongFWTemp.fuwenNext;
			fwBean.exp -= zhudongFWTemp.lvlupExp;
			zhudongFWTemp = fuwenMap.get(zhudongFWTemp.fuwenNext);
			if(zhudongFWTemp == null) {
				logger.error("符文融合操作错误，找不到itemId为{}的符文配置3");
				break;
			}
		}
		HibernateUtil.save(fwBean);
		logger.info("符文融合成功，符文tab:{},lanweiId:{}获得经验{}，itemId变为:{}", tab, lanweiId, expTotal, fwBean.itemId);
		//BagMgr.inst.sendBagInfo(session, bag);
		sendFuwenInBagInfo(session, junZhu);
		response.setResult(0);
		response.setItemId(fwBean.itemId);
		response.setExp(fwBean.exp);
		session.write(response.build());
		JunZhuMgr.inst.sendMainInfo(session);
	}

	public void duiHuanFuwen(int id, IoSession session, Builder builder) {
		FuwenDuiHuan.Builder request = (qxmobile.protobuf.FuWen.FuwenDuiHuan.Builder) builder;
		int fuwenItemId = request.getFuwenItemId();
		
		JunZhu junZhu = JunZhuMgr.inst.getJunZhu(session);
		if (null == junZhu) {
			logger.error("符文兑换错误，君主不存在");
			return;
		}
		FuwenDuiHuanResp.Builder response = FuwenDuiHuanResp.newBuilder();
		FuwenDuihuan fuwenDuihuan = fuwenDuihuanMap.get(fuwenItemId);
		if(fuwenDuihuan == null) {
			response.setResult(2);
			session.write(response.build());
			logger.error("兑换符文失败，找不到要兑换的符文碎片id:{}", fuwenItemId);
			return;
		}
		
		Fuwen fuwenCfg = fuwenMap.get(fuwenItemId);
		if(fuwenCfg == null) {
			logger.error("兑换符文失败，找不到要兑换的符文配置，itemId:{}", fuwenItemId);
			return;
		}
		
		Bag<BagGrid> bag = BagMgr.inst.loadBag(junZhu.id);
		int haveNum = BagMgr.inst.getItemCount(bag, fuwenDuihuan.itemID);
		if(haveNum < fuwenDuihuan.cost) {
			logger.error("兑换符文失败，碎片不足，要兑换的符文itemId:{} 需要:{},拥有:{}", fuwenItemId, fuwenDuihuan.cost, haveNum);
			response.setResult(1);
			session.write(response.build());
			return;
		}
		
		BagMgr.inst.removeItem(session, bag, fuwenDuihuan.itemID, fuwenDuihuan.cost, "符文甲片兑换了符文", junZhu.level);
		BagMgr.inst.addItem(session, bag, fuwenCfg.fuwenID, fuwenDuihuan.num, 0, junZhu.level, "符文甲片兑换的符文");
		//BagMgr.inst.sendBagInfo(session, bag);
		sendFuwenInBagInfo(session, junZhu);
		response.setResult(0);
		session.write(response.build());
		if(true) {//表示是橙色符文
			List<Integer> itemIdList = new ArrayList<>();
			itemIdList.add(fuwenItemId);
			EventMgr.addEvent(junZhu.id, ED.JIAPIAN_DUIHUAN_FUWEN, new Object[]{junZhu.name, itemIdList});
		}
	}
}
