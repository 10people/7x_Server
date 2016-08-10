package com.qx.alliance;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.qx.persistent.Cache;
import com.qx.persistent.HibernateUtil;
import com.sun.faces.util.LRUMap;

import qxmobile.protobuf.AlliaceMemmber;

public class AlliancePlayerDao {
	public static AlliancePlayerDao inst = new AlliancePlayerDao();
	public Map<Long, AlliancePlayer> alliancePlayerCache = Collections.synchronizedMap(new LRUMap<>(5000));
	public Map<Integer, Set<AlliancePlayer>> allianceMembersCache = Collections.synchronizedMap(new LRUMap<>(200));

	public AlliancePlayer getAlliancePlayer(long junZhuId) {
		AlliancePlayer alliancePlayer = alliancePlayerCache.get(junZhuId);
		if (alliancePlayer == null) {
			alliancePlayer = HibernateUtil.find(AlliancePlayer.class, junZhuId);
		}
		alliancePlayerCache.put(junZhuId, alliancePlayer);
		return alliancePlayer;
	}

	public Set<AlliancePlayer> getMembers(int allianceId) {
		Set<AlliancePlayer> members = allianceMembersCache.get(allianceId);
		if (members != null) {
			return members;
		}
		Object lock = Cache.getLock(AlliancePlayer.class, allianceId);
		synchronized (lock) {
			members = allianceMembersCache.get(allianceId);
			if (members == null) {
				members = new HashSet<>();
				List<AlliancePlayer> memberList = HibernateUtil.list(AlliancePlayer.class,
						" where lianMengId=" + allianceId);
				members.addAll(memberList);
				allianceMembersCache.put(allianceId, members);
			}
		}
		return members;
	}

	public void exitAlliance(long junZhuId, int allianceId) {
		Set<AlliancePlayer> memberList = getMembers(allianceId);
		for (AlliancePlayer member : memberList) {
			if (member.junzhuId == junZhuId) {
				memberList.remove(member);
				break;
			}
		}
	}

	public void joinAlliance(AlliancePlayer member, int allianceId) {
		if (member.lianMengId != allianceId) {
			return;
		}
		Set<AlliancePlayer> memberList = getMembers(allianceId);
		if (memberList != null) {
			memberList.add(member);
		}
	}

	public void dismissAlliance(int allianceId) {
		allianceMembersCache.remove(allianceId);
	}
}
