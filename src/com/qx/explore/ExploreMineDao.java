package com.qx.explore;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;
import com.qx.persistent.HibernateUtil;

public class ExploreMineDao {
	public static ExploreMineDao inst = new ExploreMineDao();
	public static Map<Long,ExploreMine> exploreMineCache = Collections.synchronizedMap(new LRUMap(5000));

	public ExploreMine getExploreMine(Long id ){
		ExploreMine  eBean = exploreMineCache.get(id) ;
		if(eBean != null ){
			return eBean;
		}
		eBean = HibernateUtil.find(ExploreMine.class, "where id = " + id ) ;
		exploreMineCache.put(id, eBean) ;
		return eBean;
	}

	public void save( ExploreMine aBean ){
		exploreMineCache.put(aBean.id, aBean);
		HibernateUtil.insert(aBean);
	}
}
