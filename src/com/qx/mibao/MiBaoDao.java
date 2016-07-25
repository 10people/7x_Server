package com.qx.mibao;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.map.LRUMap;

import com.qx.persistent.HibernateUtil;

public class MiBaoDao {
	public static MiBaoDao inst = new MiBaoDao();
	public static Map<Long, Map<Integer, MiBaoDB>> cache = Collections.synchronizedMap(new LRUMap(5000));

	public Map<Integer, MiBaoDB> getMap(Long pid) {
		Map<Integer, MiBaoDB> ret = cache.get(pid);
		if (ret != null) {
			return ret;
		}
		List<MiBaoDB> list = HibernateUtil.list(MiBaoDB.class, "where ownerId=" + pid);
		ret = Collections.synchronizedMap(new LinkedHashMap<Integer, MiBaoDB>(list.size()));

		for (MiBaoDB v : list) {
			ret.put(v.tempId, v);
		}
		cache.put(pid, ret);
		return ret;
	}

	public MiBaoDB getByMiBaoId(long jzId, int mibaoId) {
		Optional<MiBaoDB> op = getMap(jzId).values().stream().filter(t->t.miBaoId==mibaoId).findAny();
		if(op.isPresent()){
			return op.get();
		}
		return null;
	}
	public MiBaoDB get(long jzId, int tempId) {
		Map<Integer, MiBaoDB> map = getMap(jzId);
		return map.get(tempId);
	}
	
	public MiBaoDB getByDBId(long jzId, long dbId){
		Optional<MiBaoDB> op = getMap(jzId).values().stream().filter(t->t.dbId==dbId).findAny();
		return op.isPresent() ? op.get() : null;
	}
}
