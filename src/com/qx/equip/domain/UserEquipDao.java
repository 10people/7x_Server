package com.qx.equip.domain;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.map.LRUMap;

import com.qx.persistent.HibernateUtil;
import com.qx.util.TableIDCreator;

public class UserEquipDao {
	public static Map<Long,List<UserEquip>> userEquipCache = Collections.synchronizedMap(new LRUMap(5000));
	
	public static List<UserEquip> list(long junZhuId){
		List<UserEquip> res = null;
		res = userEquipCache.get(junZhuId);
		if(res != null){
			return res;
		}
		List<UserEquip> dbList = HibernateUtil.list(UserEquip.class, "where userId = " + junZhuId);
		res = Collections.synchronizedList(new LinkedList<UserEquip>());
		res.addAll(dbList);
		userEquipCache.put(junZhuId, res);
		return res;
	}
	
	public static UserEquip find(long junZhuId , int equipId){
		UserEquip res = null ;
		List<UserEquip> ueList = list(junZhuId);
		Optional<UserEquip> op =  ueList.stream().filter(ue->ue.templateId == equipId).findAny();
		if(op.isPresent()){
			res = op.get();
		}
		return res ;
	}
	
	public static UserEquip find(long junZhuId , long dbId){
		UserEquip res = null ;
		List<UserEquip> ueList = list(junZhuId);
		Optional<UserEquip> op =  ueList.stream().filter(ue->ue.equipId == dbId).findAny();
		if(op.isPresent()){
			res = op.get();
		}
		return res ;
	}
	
	public static void insert(UserEquip ue){
		List<UserEquip> ueList = list(ue.userId);
		ue.equipId = TableIDCreator.getTableID(UserEquip.class, 1);
		ueList.add(ue);
		HibernateUtil.insert(ue);
	}
}
