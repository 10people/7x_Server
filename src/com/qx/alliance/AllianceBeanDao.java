package com.qx.alliance;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.map.LRUMap;

import com.qx.junzhu.TalentPoint;
import com.qx.mibao.MiBaoDao;
import com.qx.persistent.HibernateUtil;

public class AllianceBeanDao {
	public static AllianceBeanDao inst = new AllianceBeanDao();
	public static Map<Integer,AllianceBean> allianceBeanCache = Collections.synchronizedMap(new LRUMap(5000));

	public AllianceBean getAllianceBean(Integer lmId ){
		AllianceBean  aBean = allianceBeanCache.get(lmId) ;
		if(aBean != null ){
			return aBean;
		}
		aBean = HibernateUtil.find(AllianceBean.class, "where id = " + lmId ) ;
		allianceBeanCache.put(lmId, aBean) ;
		return aBean;
	}

	public void save( AllianceBean aBean ){
		allianceBeanCache.put(aBean.id, aBean);
		HibernateUtil.insert(aBean);
	}
	
}
