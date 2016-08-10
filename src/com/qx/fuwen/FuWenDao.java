package com.qx.fuwen;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.qx.huangye.HYTreasureRecord;
import com.qx.huangye.shop.PublicShop;
import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;
import com.sun.org.apache.commons.collections.LRUMap;

public class FuWenDao {

	public static FuWenDao inst = new FuWenDao();

	public Map<Long, List<FuWenBean>> fuwenCache = Collections.synchronizedMap(new LRUMap(5000));

	/**
	 * 获取君主的所有符文，不区分tab页面
	 * 
	 * @param jzId
	 * @return
	 */
	public List<FuWenBean> getFuwenAll(long jzId) {
		List<FuWenBean> fuwenAll = fuwenCache.get(jzId);
		if (fuwenAll != null) {
			return fuwenAll;
			
		}
		Object lock = Cache.getLock(FuWenBean.class, jzId);
		synchronized (lock) {
			fuwenAll = fuwenCache.get(jzId);
			if(fuwenAll == null) {
				fuwenAll = HibernateUtil.list(FuWenBean.class, " where junzhuId = " + jzId);
				fuwenCache.put(jzId, fuwenAll);
			}
		}
		return fuwenAll;
	}

	public List<FuWenBean> getFuwenByTab(long jzId, int tab) {
		List<FuWenBean> fuwenAll = getFuwenAll(jzId);
		List<FuWenBean> fuwenByTab = fuwenAll.stream().filter(item -> item.tab == tab).collect(Collectors.toList());
		return fuwenByTab;
	}
	
	public void insert(long jzId, FuWenBean fuWenBean) {
		List<FuWenBean> fuwenAll = getFuwenAll(jzId);
		fuwenAll.add(fuWenBean);
	}

}
