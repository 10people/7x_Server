package com.qx.youxia;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.map.LRUMap;

import com.qx.persistent.HibernateUtil;
import com.qx.pvp.ZhanDouRecord;
import com.qx.task.WorkTaskBean;

public class YouXiaBeanDao {
	public static Map<Long , List<YouXiaBean> > youXiaBeanCaChe = Collections.synchronizedMap(new LRUMap(5000));
	
	public static List<YouXiaBean> list(long junZhuId){
		List<YouXiaBean> res = null ;
		res = youXiaBeanCaChe.get(junZhuId);
		if(res != null){
			return res;
		}
		 List<YouXiaBean> list = HibernateUtil.list(YouXiaBean.class, " where junzhuId = " + junZhuId );
		 res = Collections.synchronizedList(new LinkedList<YouXiaBean>());
		 res.addAll(list);
		 youXiaBeanCaChe.put(junZhuId, res);
		return res;
	}
	
	public static YouXiaBean find(long junZhuId , int type){
		YouXiaBean res = null ;
		List<YouXiaBean> list = list(junZhuId);
		Optional<YouXiaBean> op  = list.stream().filter(t->t.type == type).findAny();
		if(op.isPresent()){
			res = op.get();
		}
		return res;
	}
	
	public static void insert(YouXiaBean youXiaBean){
		List<YouXiaBean> list = list(youXiaBean.junzhuId);
		list.add(youXiaBean);
		HibernateUtil.insert(youXiaBean);
	}
	
	public static void delete(YouXiaBean youXiaBean){
		List<YouXiaBean> list = list(youXiaBean.junzhuId);
		list.remove(youXiaBean);
		HibernateUtil.delete(youXiaBean);
	}
	
}
