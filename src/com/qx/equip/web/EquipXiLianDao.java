package com.qx.equip.web;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

import com.qx.equip.domain.EquipXiLian;
import com.qx.persistent.HibernateUtil;

public class EquipXiLianDao {
	public static EquipXiLianDao inst = new EquipXiLianDao();
	public static Map<Long,List<EquipXiLian>> equipXiLianCache = Collections.synchronizedMap(new LRUMap(5000));
	
	public List<EquipXiLian> get( long junZhuId ){
		List<EquipXiLian>  res = equipXiLianCache.get(junZhuId) ;
		if(res != null ){
			return res;
		}
		List<EquipXiLian> list = HibernateUtil.list(EquipXiLian.class, "where junZhuId = " + junZhuId ) ;
		res = Collections.synchronizedList(new LinkedList<EquipXiLian>());
		res.addAll(list);
		equipXiLianCache.put(junZhuId, res) ;
		return res;
	}
	
	public EquipXiLian get(long junZhuId , long equipId){
		List<EquipXiLian>  list = get(  junZhuId );
		for(EquipXiLian exl : list ){
			if(exl.equipId == equipId){
				return exl;
			}
		}
		return null ;
	}
	
	public void delete( EquipXiLian equipXiLian ){
		List<EquipXiLian>  list = get( equipXiLian.junZhuId );
		list.remove(equipXiLian);
		HibernateUtil.delete(equipXiLian);
	}
	
	public void save( EquipXiLian equipXiLian ){
		List<EquipXiLian>  list = get( equipXiLian.junZhuId );
		list.add(equipXiLian);
		HibernateUtil.insert(equipXiLian);
	}
}
