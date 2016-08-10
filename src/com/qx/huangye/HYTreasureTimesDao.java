package com.qx.huangye;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.map.LRUMap;

import com.qx.huangye.shop.PublicShop;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;

public class HYTreasureTimesDao {
	public static HYTreasureTimesDao inst = new HYTreasureTimesDao();

	public Map<Integer, List<HYTreasureTimes>> timesCache = Collections.synchronizedMap(new LRUMap(200));

	public List<HYTreasureTimes> getTimesList(int lianMengId) {
		List<HYTreasureTimes> hyTreasureTimes = timesCache.get(lianMengId);
		if (hyTreasureTimes != null) {
			return hyTreasureTimes;
		}
		Object lock = Cache.getLock(HYTreasureTimes.class, lianMengId);
		synchronized (lock) {
			hyTreasureTimes = timesCache.get(lianMengId);
			if (hyTreasureTimes == null) {
				hyTreasureTimes = HibernateUtil.list(HYTreasureTimes.class, " where lianmengId=" + lianMengId);
				timesCache.put(lianMengId, hyTreasureTimes);
			}
		}
		return hyTreasureTimes;
	}

	public HYTreasureTimes getByJunZhuId(int lianMengId, long junzhuId) {
		List<HYTreasureTimes> hyTreasureTimes = getTimesList(lianMengId);
		Optional<HYTreasureTimes> optional = hyTreasureTimes.stream().filter(item -> item.junzhuId == junzhuId)
				.findAny();
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	public void insert(int lianMengId, HYTreasureTimes treasureTimes) {
		List<HYTreasureTimes> hyTreasureTimes = getTimesList(lianMengId);
		hyTreasureTimes.add(treasureTimes);
	}

}
