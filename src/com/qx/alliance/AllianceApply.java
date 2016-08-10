package com.qx.alliance;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.qx.persistent.DBHash;
import com.qx.persistent.MCSupport;

@Entity
@Table(name = "AllianceApply")
public class AllianceApply implements MCSupport,DBHash {
	
	public static final long serialVersionUID = 7057558954854094852L;
	
	@Id
	public long junzhuId;
	public String allianceIds;
	@Transient
	public static String SPLIT_SYMBOL = "_";
	
	/** 申请的联盟ID列表 */
	@Transient
	public transient Set<Integer> allianceIdSet = null;

	/**
	 * 更新字符串
	 * 
	 * @param formatSet
	 */
	public String updateformat(Set<Integer> formatSet) {
		if (formatSet == null || formatSet.isEmpty()) {
			return StringUtils.EMPTY;
		}

		StringBuilder builder = new StringBuilder();
//		synchronized (formatSet) {
			for (long mailId : formatSet) {
				builder.append(mailId).append(SPLIT_SYMBOL);
			}
//		}
		if (builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}
	
	/**
	 * 格式解析
	 * 
	 * @param format          格式字符串
	 * @param formatSet       缓存
	 * @return Set<Integer>
	 */
	public Set<Integer> getCacheSet(String format, Set<Integer> formatSet) {
		if (formatSet != null) {
			return formatSet;
		}
		
//		synchronized (this) {
			if (formatSet == null) {
				formatSet = Collections.synchronizedSet(new HashSet<Integer>());
				if (StringUtils.isBlank(format)) {
					return formatSet;
				}
				
				String[] array = format.split(SPLIT_SYMBOL);
				for (String element : array) {
					formatSet.add(Integer.valueOf(element));
				}
			}
//		}
		return formatSet;
	}
	
	/**
	 * 更新申请的联盟列表
	 * @param allianceId
	 */
	public void addAllianceId(int allianceId) {
		this.getAllianceIdSet().add(allianceId);
		this.allianceIds = this.updateformat(this.allianceIdSet);
	}
	
	/**
	 * 删除申请的联盟
	 * @param allianceId
	 */
	public void removeAlliance(int allianceId) {
		this.getAllianceIdSet().remove(allianceId);
		this.allianceIds = this.updateformat(this.allianceIdSet);
	}
	
	/**
	 * 删除申请的全部联盟
	 */
	public void removeAllAlliance() {
		this.getAllianceIdSet().clear();
		this.allianceIds = this.updateformat(this.allianceIdSet);
	}
	
	public boolean isAllianceExist(int allianceId) {
		return getAllianceIdSet().contains(allianceId);
	}
	
	public int getAllianceNum() {
		this.allianceIdSet = getAllianceIdSet();
		return allianceIdSet.size();
	}
	
	public Set<Integer> getAllianceIdSet() {
		allianceIdSet = getCacheSet(allianceIds, allianceIdSet);
		return allianceIdSet;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (junzhuId ^ (junzhuId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AllianceApply other = (AllianceApply) obj;
		if (junzhuId != other.junzhuId)
			return false;
		return true;
	}

	@Override
	public long getIdentifier() {
		return junzhuId;
	}

	@Override
	public long hash() {
		return junzhuId;
	}
	
	
	
}
