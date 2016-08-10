package com.qx.huangye.shop;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.mysql.jdbc.util.LRUCache;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;

public class PublicShopDao {
	public static PublicShopDao inst = new PublicShopDao();

	public Map<Long, List<PublicShop>> shopCache = Collections.synchronizedMap(new LRUCache(5000));

	public List<PublicShop> getShopList(long junzhuId) {
		List<PublicShop> shopList = shopCache.get(junzhuId);
		if (shopList != null) {
			return shopList;
		}

		Object lock = Cache.getLock(PublicShop.class, junzhuId);
		synchronized (lock) {
			shopList = shopCache.get(junzhuId);
			if (shopList == null) {
				shopList = HibernateUtil.list(PublicShop.class, " where junZhuId=" + junzhuId);
				shopCache.put(junzhuId, shopList);
			}
		}
		return shopList;
	}

	public PublicShop getShopByType(long junzhuId, int type) {
		PublicShop shop = null;
		List<PublicShop> shopList = getShopList(junzhuId);
		Optional<PublicShop> optional = shopList.stream().filter(item -> item.type == type).findAny();
		if (optional.isPresent()) {
			shop = optional.get();
		}
		return shop;
	}

	public void insertShop(long junzhuId, PublicShop shop) {
		List<PublicShop> shopList = shopCache.get(junzhuId);
		shopList.add(shop);
	}

}