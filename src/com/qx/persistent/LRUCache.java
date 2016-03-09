package com.qx.persistent;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

/**
 * @author 康建虎
 *
 * @param <T>
 */
public class LRUCache<T> {
	public boolean useCache = true;
	public Map<Long, T> bagCache = Collections.synchronizedMap(new LRUMap(1000));
	public T load(Long id, LRUCacheMissFunction<T> f){
		if(useCache){
			T t = bagCache.get(id);
			if(t != null){
				return t;
			}
		}
		T ret = f.load();
		if(useCache){
			bagCache.put(id, ret);
		}
		return ret;
	}
}
