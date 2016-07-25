package com.qx.yabiao;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.qx.junzhu.ChengHaoBean;
import com.qx.persistent.HibernateUtil;
import com.qx.prompt.YaBiaoJunQing;
import com.sun.org.apache.commons.collections.LRUMap;

public class YaBiaoDao {
	public static YaBiaoDao inst = new YaBiaoDao();
	public static Map<Long, Map<Long, YaBiaoJunQing>> junQingCache = Collections.synchronizedMap(new LRUMap(5000));

	public Map<Long, YaBiaoJunQing> getJunQingMap(long pid) {
		Map<Long, YaBiaoJunQing> ret = junQingCache.get(pid);
		if (ret != null) {
			return ret;
		}
		List<YaBiaoJunQing> list = HibernateUtil.list(YaBiaoJunQing.class, "where lmId=" + pid);
		ret = Collections.synchronizedMap(new LinkedHashMap<Long, YaBiaoJunQing>(list.size()));

		for (YaBiaoJunQing yaBiaoJunQing : list) {
			ret.put(yaBiaoJunQing.id, yaBiaoJunQing);
		}
		junQingCache.put(pid, ret);
		return ret;
	}

	public void saveYaBiaoJunQing(YaBiaoJunQing yaBiaoJunQing) {
		Map<Long, YaBiaoJunQing> ret = junQingCache.get(yaBiaoJunQing.lmId);
		ret.put(yaBiaoJunQing.id, yaBiaoJunQing);
	}

	public void delJunQingByYbJzId(long jzId) {
		for (Long lmId : junQingCache.keySet()) {
			Map<Long, YaBiaoJunQing> ret = junQingCache.get(lmId);
			for (Long dbid : ret.keySet()) {
				if (ret.get(dbid).ybjzId == jzId) {
					ret.remove(dbid);
				}
			}
		}
	}

}
