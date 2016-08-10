package com.qx.alliance;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.map.LRUMap;

import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;

public class AllianceInviteBeanDao {
	public static AllianceInviteBeanDao inst = new AllianceInviteBeanDao();
	public static Map<Long, List<AllianceInviteBean>> cache = Collections.synchronizedMap((new LRUMap(1000)));

	public List<AllianceInviteBean> getList(long junZhuId) {
		List<AllianceInviteBean> list = cache.get(junZhuId);
		if (list != null) {
			return list;
		}
		Object lock = Cache.getLock(AllianceInviteBean.class, junZhuId);
		synchronized (lock) {
			list = cache.get(junZhuId);
			if (list == null) {
				list = HibernateUtil.list(AllianceInviteBean.class, " where junzhuId=" + junZhuId);
				cache.put(junZhuId, list);
			}
		}
		return list;
	}

	public void addInviteBean(long junZhuId, AllianceInviteBean bean) {
		if (getList(junZhuId).add(bean)) {
			HibernateUtil.insert(bean);
		}
	}

	public void removeInviteBean(long junZhuId, AllianceInviteBean bean) {
		if (getList(junZhuId).remove(bean)) {
			HibernateUtil.delete(bean);
		}
	}

	public AllianceInviteBean getInviteBean(long junZhuId, int allianceId) {
		Optional<AllianceInviteBean> optional = getList(junZhuId).stream()//
				.filter(item -> item.allianceId == allianceId)//
				.findAny();
		if(optional.isPresent()) {
			return optional.get();
		}
		return null;
	}
}
