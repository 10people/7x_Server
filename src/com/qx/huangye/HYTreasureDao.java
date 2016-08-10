package com.qx.huangye;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.map.LRUMap;

import com.qx.huangye.shop.PublicShop;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;

public class HYTreasureDao {
	public static HYTreasureDao inst = new HYTreasureDao();
	public Map<Integer, List<HYTreasure>> treasureCache = Collections.synchronizedMap(new LRUMap(5000));

	public List<HYTreasure> getTreasure(int lianMengId) {
		List<HYTreasure> treasureList = treasureCache.get(lianMengId);
		if (treasureList != null) {
			return treasureList;
		}
		Object lock = Cache.getLock(HYTreasure.class, lianMengId);
		synchronized (lock) {
			treasureList = treasureCache.get(lianMengId);
			if(treasureList == null) {
				treasureList = HibernateUtil.list(HYTreasure.class, " where lianMengId=" + lianMengId);
				treasureCache.put(lianMengId, treasureList);
			}
		}
		return treasureList;
	}

	public List<HYTreasure> getByJunZhuId(long junzhuId) {
		List<HYTreasure> result = Collections.EMPTY_LIST;
		treasureCache.values().stream()//
				.forEach(item -> item.stream()//
						.forEach(e -> {
							if (e.battleJunzhuId == junzhuId) {
								result.add(e);
							}
						}));
		return result;
	}

	public HYTreasure getByTreasureId(int lianMengId, long treasureId) {
		List<HYTreasure> treasureList = getTreasure(lianMengId);
		Optional<HYTreasure> optional = treasureList.stream().filter(item -> item.id == treasureId).findAny();
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	public HYTreasure getByGuanQiaId(int lianMengId, int guanQiaId) {
		List<HYTreasure> treasureList = getTreasure(lianMengId);
		Optional<HYTreasure> optional = treasureList.stream().filter(item -> item.guanQiaId == guanQiaId).findAny();
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	public void insert(int lianMengId, HYTreasure hyTreasure) {
		List<HYTreasure> treasureList = getTreasure(lianMengId);
		treasureList.add(hyTreasure);
	}

	public void deleteByLianMengId(int lianMengId) {
		treasureCache.remove(lianMengId);
	}
}
