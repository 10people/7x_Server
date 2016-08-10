package com.qx.alliancefight;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

import com.qx.persistent.HibernateUtil;
public class WildCityBeanDao {
	public static WildCityBeanDao inst = new WildCityBeanDao();
	public static Map<Integer, Map<Integer,WildCityBean>> cache = Collections.synchronizedMap(new LRUMap(5000)); 
	public Map<Integer, WildCityBean> getMap(int pid) {
		Map<Integer, WildCityBean> ret = cache.get(pid);
		if (ret != null) {
			return ret;
		}
		List<WildCityBean> list = HibernateUtil.list(WildCityBean.class, "where lmId=" + pid);
		ret = Collections.synchronizedMap(new LinkedHashMap<Integer, WildCityBean>(list.size()));
		for (WildCityBean v : list) {
			ret.put(v.cityId, v);
		}
		cache.put(pid, ret);
		return ret;
	}
	public void save(WildCityBean wildCityBean) {
		Map<Integer, WildCityBean> ret = cache.get(wildCityBean.lmId);
		if(ret == null){
			ret = Collections.synchronizedMap(new LinkedHashMap<Integer, WildCityBean>());
			cache.put(wildCityBean.lmId, ret);
		}
		ret.put(wildCityBean.cityId, wildCityBean);
	}
}
