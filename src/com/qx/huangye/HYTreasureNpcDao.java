package com.qx.huangye;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

import com.qx.huangye.shop.PublicShop;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;

/**
 * 荒野藏宝点
 * 
 * @author lizhaowen
 *
 */
public class HYTreasureNpcDao {
	public static HYTreasureNpcDao inst = new HYTreasureNpcDao();

	public Map<Long, List<HYTreasureNpc>> treasureNpcCache = Collections.synchronizedMap(new LRUMap(5000));

	public List<HYTreasureNpc> getTreasureNpcList(long treasureId) {
		List<HYTreasureNpc> npcList = treasureNpcCache.get(treasureId);
		if (npcList != null) {
			return npcList;
		}
		Object lock = Cache.getLock(HYTreasureNpc.class, treasureId);
		synchronized (lock) {
			npcList = treasureNpcCache.get(treasureId);
			if(npcList == null) {
				npcList = HibernateUtil.list(HYTreasureNpc.class, " where treasureId=" + treasureId);
				treasureNpcCache.put(treasureId, npcList);
			}
		}
		return npcList;
	}

	public void insertTreasureNpc(HYTreasureNpc npc, long treasureId) {
		List<HYTreasureNpc> npcList = getTreasureNpcList(treasureId);
		if (npcList == null) {
			npcList = new ArrayList<>();
		}
		npcList.add(npc);
	}

	public void deleteTreasureNpc(long treasureId) {
		treasureNpcCache.remove(treasureId);
	}

}
