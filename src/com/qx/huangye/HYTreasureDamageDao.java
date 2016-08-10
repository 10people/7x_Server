package com.qx.huangye;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.map.LRUMap;

import com.qx.huangye.shop.PublicShop;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;

public class HYTreasureDamageDao {
	public static HYTreasureDamageDao inst = new HYTreasureDamageDao();
	
	public Map<Long, List<HYTreasureDamage>> damageCache = Collections.synchronizedMap(new LRUMap(5000));

	public List<HYTreasureDamage> getDamageList(long treasureId) {
		List<HYTreasureDamage> damageList = damageCache.get(treasureId);
		if(damageList != null) {
			return damageList;
		}
		Object lock = Cache.getLock(HYTreasureDamage.class, treasureId);
		synchronized (lock) {
			damageList = damageCache.get(treasureId);
			if(damageList == null) {
				damageList = HibernateUtil.list(HYTreasureDamage.class, " where treasureId=" + treasureId);
				damageCache.put(treasureId, damageList);
			}
		}
		return damageList;
	}

	public HYTreasureDamage getDamageByJunZhuId(long treasureId, long junzhuId) {
		List<HYTreasureDamage> damageList = getDamageList(treasureId);
		Optional<HYTreasureDamage> optional = damageList.stream()//
				.filter(item -> item.junzhuId == junzhuId)//
				.findAny();
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	public void insert(long treasureId, HYTreasureDamage treasureDamage) {
		List<HYTreasureDamage> damageList = getDamageList(treasureId);
		damageList.add(treasureDamage);
	}

	public void deleteByTreasureId(long treasureId) {
		damageCache.remove(treasureId);
	}

}
