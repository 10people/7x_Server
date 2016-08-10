package com.qx.activity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.qx.junzhu.ChengHaoBean;
import com.qx.persistent.HibernateUtil;
import com.sun.org.apache.commons.collections.LRUMap;

public class levelUpGiftBeanDao {
	public static levelUpGiftBeanDao inst = new levelUpGiftBeanDao();
	public static Map<Long, Map<Integer, LevelUpGiftBean>> cache = Collections.synchronizedMap(new LRUMap(5000));

	public Map<Integer, LevelUpGiftBean> getMap(long pid) {
		Map<Integer, LevelUpGiftBean> ret = cache.get(pid);
		if (ret != null) {
			return ret;
		}
		List<LevelUpGiftBean> list = HibernateUtil.list(LevelUpGiftBean.class, " where jzId=" + pid);
		ret = Collections.synchronizedMap(new LinkedHashMap<Integer, LevelUpGiftBean>(list.size()));
		for (LevelUpGiftBean levelUpGiftBean : list) {
			ret.put(levelUpGiftBean.level, levelUpGiftBean);
		}
		cache.put(pid, ret);
		return ret;
	}
	public void save(LevelUpGiftBean levelUpGiftBean) {
		Map<Integer, LevelUpGiftBean> ret = cache.get(levelUpGiftBean.jzId);
		if(ret == null){
			ret = Collections.synchronizedMap(new LinkedHashMap<Integer, LevelUpGiftBean>());
			cache.put(levelUpGiftBean.jzId, ret);
		}
		ret.put(levelUpGiftBean.level, levelUpGiftBean);
	}
}
