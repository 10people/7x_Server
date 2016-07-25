package com.qx.alliance;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.qx.persistent.HibernateUtil;
import com.sun.faces.util.LRUMap;

public class AlliancePlayerDao {
	public static AlliancePlayerDao inst = new AlliancePlayerDao();
	public Map<Long, AlliancePlayer> alliancePlayerCache = Collections.synchronizedMap(new LRUMap<>(5000));
	public Map<Integer, List<AlliancePlayer>> allianceMembersCache = Collections.synchronizedMap(new LRUMap<>(200));
	
	public AlliancePlayer getAlliancePlayer(long junZhuId) {
		AlliancePlayer alliancePlayer = alliancePlayerCache.get(junZhuId);
		if(alliancePlayer == null) {
			alliancePlayer = HibernateUtil.find(AlliancePlayer.class, junZhuId);
		}
		alliancePlayerCache.put(junZhuId, alliancePlayer);
		return alliancePlayer;
	}
	
	public List<AlliancePlayer> getMembers(int allianceId) {
		List<AlliancePlayer> memberList = allianceMembersCache.get(allianceId);
		if(memberList == null) {
			memberList = HibernateUtil.list(AlliancePlayer.class, " where lianMengId=" + allianceId);
		}
		allianceMembersCache.put(allianceId, memberList);
		return memberList;
	}
	
	public void exitAlliance(long junZhuId, int allianceId) {
		List<AlliancePlayer> memberList = getMembers(allianceId);
		for(AlliancePlayer member : memberList) {
			if(member.junzhuId == junZhuId) {
				memberList.remove(member);
				break;
			}
		}
	}
	
	public void joinAlliance(AlliancePlayer member, int allianceId) {
		if(member.lianMengId != allianceId) {
			return;
		}
		List<AlliancePlayer> memberList = getMembers(allianceId);
		if(memberList != null) {
			memberList.add(member);
		}
	}
	
	public void dismissAlliance(int allianceId) {
		allianceMembersCache.remove(allianceId);
	}
}
