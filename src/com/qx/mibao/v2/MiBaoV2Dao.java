package com.qx.mibao.v2;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.map.LRUMap;

import com.qx.mibao.MiBaoDB;
import com.qx.persistent.HibernateUtil;

public class MiBaoV2Dao {
	public static MiBaoV2Dao inst = new MiBaoV2Dao();
	@SuppressWarnings("unchecked")
	public static Map<Long, Map<Integer, MiBaoV2Bean>> cache = Collections.synchronizedMap(new LRUMap(5000));

	public Map<Integer, MiBaoV2Bean> getMap(Long pid) {
		Map<Integer, MiBaoV2Bean> ret = cache.get(pid);
		if (ret != null) {
			return ret;
		}
		List<MiBaoV2Bean> list = HibernateUtil.list(MiBaoV2Bean.class, "where ownerId=" + pid);
		ret = Collections.synchronizedMap(new LinkedHashMap<Integer, MiBaoV2Bean>(list.size()));

		for (MiBaoV2Bean v : list) {
			ret.put(v.miBaoId, v);
		}
		cache.put(pid, ret);
		return ret;
	}
	
	public MiBaoV2Bean get(long jzId, int miBaoId) {
		Map<Integer, MiBaoV2Bean> map = getMap(jzId);
		return map.get(miBaoId);
	}
	

	public MiBaoV2Bean getByDBId(long jzId, long dbId){
		Optional<MiBaoV2Bean> op = getMap(jzId).values().stream().filter(t->t.dbId==dbId).findAny();
		return op.isPresent() ? op.get() : null;
	}
}
