package com.qx.award;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

import com.qx.persistent.HibernateUtil;

public class DropRateDao {
	
	public static DropRateDao inst = new DropRateDao();
	/** Map<Long, Map<Integer, DropRateBean>> <jzId, <groupId, dropRateBean>> **/
	public static Map<Long, Map<Integer, DropRateBean>> cache = Collections.synchronizedMap(new LRUMap(5000));
	
	public Map<Integer, DropRateBean> getMap(long jzId) {
		Map<Integer, DropRateBean> map = cache.get(jzId);
		if(map == null) {
			List<DropRateBean> dropBeanList = HibernateUtil.list(DropRateBean.class, " where jzId=" + jzId);
			map = Collections.synchronizedMap(new LinkedHashMap<>(dropBeanList.size()));
			for(DropRateBean bean : dropBeanList) {
				map.put(bean.groupId, bean);
			}
			cache.put(jzId, map);
		}
		return map;
	}
	
}
