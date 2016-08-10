package com.qx.alliance.building;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;

public class LMKJBeanDao {
	public static LMKJBeanDao inst = new LMKJBeanDao();
	public static Map<Integer, LMKJBean> cache = Collections.synchronizedMap(new LRUMap(5000));
	
	public LMKJBean getBean(int lianMengId) {
		LMKJBean bean = cache.get(lianMengId);
		if(bean != null) {
			return bean;
		}
		Object lock = Cache.getLock(LMKJBean.class, lianMengId);
		synchronized (lock) {
			bean = cache.get(lianMengId);
			if(bean == null) {
				bean = HibernateUtil.find(LMKJBean.class, lianMengId);
				cache.put(lianMengId, bean);
			}
		}
		return bean;
	}
	
	public void insertBean(int lianMengId, LMKJBean bean) {
		if(cache.get(lianMengId) != null) {
			return;
		}
		cache.put(lianMengId, bean);
		HibernateUtil.insert(bean);
	}
}
