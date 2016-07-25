package com.qx.pvp;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

import com.qx.persistent.HibernateUtil;

public class ZhanDouRecordDao {
	public static Map<Integer , ZhanDouRecord > zhanDouRecordCaChe = Collections.synchronizedMap(new LRUMap(5000));
	
	public static ZhanDouRecord find(int zhandouId){
		ZhanDouRecord res = null ;
		res = zhanDouRecordCaChe.get(zhandouId);
		if(res != null){
			return res;
		}else{
			if(zhanDouRecordCaChe.containsKey(zhandouId)){
				return null ;
			}
		}
		res = HibernateUtil.find(ZhanDouRecord.class, zhandouId);
		zhanDouRecordCaChe.put(zhandouId, res);
		return res;
	}
	
	public static void insert(ZhanDouRecord bean){
		zhanDouRecordCaChe.put(bean.zhandouId, bean);
		HibernateUtil.insert(bean);
	}
	
	public static void delete(ZhanDouRecord bean){
		zhanDouRecordCaChe.remove(bean.zhandouId);
		HibernateUtil.delete(bean);
	}
	
	public static List<ZhanDouRecord>  list( long junZhuId){
		List<ZhanDouRecord> res = new LinkedList<ZhanDouRecord>();
		for( ZhanDouRecord bean : zhanDouRecordCaChe.values()){
			if(bean.enemyId == junZhuId || bean.junzhuId == junZhuId){
				res.add(bean);
			}
		}
		return res;
	}
	
}
