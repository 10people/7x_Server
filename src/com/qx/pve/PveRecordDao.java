package com.qx.pve;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.qx.persistent.HibernateUtil;

/**
 * 负责管理PVE关卡数据记录。
 * FIXME 需要缓存。
 * @author 康建虎
 *
 */
public class PveRecordDao {

	public Map<Integer, PveRecord> getRecords(Long pid) {
		List<PveRecord> list = HibernateUtil.list(PveRecord.class, "where uid="+pid);
		Map<Integer, PveRecord> ret = new LinkedHashMap<Integer, PveRecord>(list.size());
		for(PveRecord v : list){
			ret.put(v.guanQiaId, v);
		}
		return ret;
	}

}
