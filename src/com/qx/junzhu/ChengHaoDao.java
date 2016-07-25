package com.qx.junzhu;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.map.LRUMap;

import com.qx.persistent.HibernateUtil;

public class ChengHaoDao {
	public static ChengHaoDao inst = new ChengHaoDao();
	public static Map<Long, Map<Integer,ChengHaoBean>> cache = Collections.synchronizedMap(new LRUMap(5000));
	public ChengHaoBean getChengHaoBeanById(long jzId, int chenghaoId){
		Optional<ChengHaoBean> op = getMap(jzId).values().stream().filter(t->t.tid==chenghaoId).findAny();
		if(op.isPresent()){
			return op.get();
		}
		return null;
	}
	public ChengHaoBean getChengHaoBeanByState(long jzId, char state){
		Optional<ChengHaoBean> op = getMap(jzId).values().stream().filter(t->t.state==state).findAny();
		if(op.isPresent()){
			return op.get();
		}
		return null;
	}
	public Map<Integer, ChengHaoBean> getMap(Long pid) {
		Map<Integer, ChengHaoBean> ret = cache.get(pid);
		if (ret != null) {
			return ret;
		}
		List<ChengHaoBean> list = HibernateUtil.list(ChengHaoBean.class, "where jzId=" + pid);
		ret = Collections.synchronizedMap(new LinkedHashMap<Integer, ChengHaoBean>(list.size()));

		for (ChengHaoBean v : list) {
			ret.put(v.tid, v);
		}
		cache.put(pid, ret);
		return ret;
	}
	public void delete(ChengHaoBean chengHaoBean) {
		Map<Integer, ChengHaoBean> ret = cache.get(chengHaoBean.jzId);
		for(Integer key:ret.keySet()){
			if(ret.get(key).dbId == chengHaoBean.dbId){
				ret.remove(key);
				break;
			}
		}
		
	}
	public void save(ChengHaoBean chengHaoBean) {
		Map<Integer, ChengHaoBean> ret = cache.get(chengHaoBean.jzId);
		if(ret == null){
			ret = Collections.synchronizedMap(new LinkedHashMap<Integer, ChengHaoBean>());
		}
		ret.put(chengHaoBean.tid, chengHaoBean);
	}
}
