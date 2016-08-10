package com.qx.alliancefight;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

import com.qx.persistent.HibernateUtil;

public class CityBeanDao {
	public static CityBeanDao inst = new CityBeanDao();
	public static Map<Integer, CityBean> cache = Collections.synchronizedMap(new LRUMap(100));
	public static boolean isInit = false;
	public Map<Integer, CityBean> getMap() {
		if (isInit) {
			return cache;
		}
		List<CityBean> list = HibernateUtil.list(CityBean.class, "");
		for (CityBean cityBean : list) {
			cache.put(cityBean.cityId, cityBean);
		}
		isInit = true;
		return cache;
	}
	public CityBean getCityBeanById(int cityId) {
		CityBean bean = getMap().get(cityId);
		return bean;
	}
}
