package com.qx.huangye;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

import com.qx.huangye.shop.PublicShop;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;

public class HYTreasureRecordDao {
	public static HYTreasureRecordDao inst = new HYTreasureRecordDao();

	public Map<Integer, HYTreasureRecord> recordCache = Collections.synchronizedMap(new LRUMap(2000));

	public HYTreasureRecord getTreasureRecord(int lianMengId) {
		HYTreasureRecord treasureRecord = recordCache.get(lianMengId);
		if (treasureRecord != null) {
			return treasureRecord;
		}
		Object lock = Cache.getLock(HYTreasureRecord.class, lianMengId);
		synchronized (lock) {
			treasureRecord = recordCache.get(lianMengId);
			if (treasureRecord == null) {
				treasureRecord = HibernateUtil.find(HYTreasureRecord.class, lianMengId);
				recordCache.put(lianMengId, treasureRecord);
			}
		}
		return treasureRecord;
	}

	public void insert(int lianMengId, HYTreasureRecord treasureRecord) {
		recordCache.put(lianMengId, treasureRecord);
	}

	public void delete(int lianMengId) {
		recordCache.remove(lianMengId);
	}
}
