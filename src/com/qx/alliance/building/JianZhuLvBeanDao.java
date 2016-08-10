package com.qx.alliance.building;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;

public class JianZhuLvBeanDao {
	public static JianZhuLvBeanDao inst = new JianZhuLvBeanDao();
	public Map<Integer, JianZhuLvBean> jianZhuCache = Collections.synchronizedMap(new LRUMap(200));

	public JianZhuLvBean getJianZhuBean(int lianMengId) {
		JianZhuLvBean bean = jianZhuCache.get(lianMengId);
		if (bean != null) {
			return bean;
		}

		Object lock = Cache.getLock(JianZhuLvBean.class, lianMengId);
		synchronized (lock) {
			if (bean == null) {
				bean = HibernateUtil.find(JianZhuLvBean.class, lianMengId);
				jianZhuCache.put(lianMengId, bean);
			}
		}
		return bean;
	}

	public void insertJianZhuBean(int lianMengId, JianZhuLvBean bean) {
		jianZhuCache.put(lianMengId, bean);
	}
}
