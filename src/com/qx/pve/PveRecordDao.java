package com.qx.pve;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

import com.qx.persistent.HibernateUtil;
import com.qx.task.WorkTaskBean;

/**
 * 负责管理PVE关卡数据记录。
 * FIXME 需要缓存。
 * @author 康建虎
 *
 */
public class PveRecordDao {
	public static Map<Long,Map<Integer, PveRecord>> cache = Collections.synchronizedMap(new LRUMap(5000));
	
	public Map<Integer, PveRecord> getRecords(Long pid) {
		Map<Integer, PveRecord> ret = cache.get(pid);
		if(ret != null){
			return ret;
		}
		List<PveRecord> list = HibernateUtil.list(PveRecord.class, "where uid="+pid);
		ret = Collections.synchronizedMap(new LinkedHashMap<Integer, PveRecord>(list.size()));
		
		for(PveRecord v : list){
			ret.put(v.guanQiaId, v);
		}
		cache.put(pid, ret);
		return ret;
	}

	public PveRecord get(long jzId, int gqId){
		Map<Integer, PveRecord> map = getRecords(jzId);
		return map.get(gqId);
	}
}
