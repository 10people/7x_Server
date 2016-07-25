package com.qx.alliancefight;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

@Entity
public class AllianceFightApply {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int id;
	
	/** 第几届比赛 */
	public int fightNum;
	
	/** 报名联盟 格式 id_id_id */
	public String applyIds;
	
	/** 联盟 格式 id_id_id */
	public String outIds;
	
	@Transient
	public static final String SPLIT_SYMBOL = "_";
	
	@Transient
	public Set<Integer> applyIdSet = null;

	@Transient
	public Set<Integer> outIdSet = null;

	public Set<Integer> getApplyIdSet() {
		if (applyIdSet != null) {
			return applyIdSet;
		}
		
		if (applyIdSet == null) {
			applyIdSet = Collections.synchronizedSet(new HashSet<Integer>());
			if (StringUtils.isBlank(applyIds)) {
				return applyIdSet;
			}
			
			String[] array = applyIds.split(SPLIT_SYMBOL);
			for (String element : array) {
				applyIdSet.add(Integer.valueOf(element));
			}
		}
		return applyIdSet;
	}
	
	public Set<Integer> getOutIdSet() {
		if (outIdSet != null) {
			return outIdSet;
		}
		
		if (outIdSet == null) {
			outIdSet = Collections.synchronizedSet(new HashSet<Integer>());
			if (StringUtils.isBlank(outIds)) {
				return outIdSet;
			}
			
			String[] array = outIds.split(SPLIT_SYMBOL);
			for (String element : array) {
				outIdSet.add(Integer.valueOf(element));
			}
		}
		return outIdSet;
	}
	
	public String updateformat(Set<Integer> formatSet) {
		if (formatSet == null || formatSet.isEmpty()) {
			return StringUtils.EMPTY;
		}

		StringBuilder builder = new StringBuilder();
		for (long id : formatSet) {
			builder.append(id).append(SPLIT_SYMBOL);
		}
		if (builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}

	public void setAllianceIdSet(Set<Integer> allianceIdSet) {
		this.applyIdSet = allianceIdSet;
	}
	
	public void addApplyId(int allianceId) {
		this.getApplyIdSet().add(allianceId);
		this.applyIds = this.updateformat(applyIdSet);
	}
	
	public boolean isApply(int allianceId) {
		return this.getApplyIdSet().contains(allianceId);
	}
	
	public void addOutId(int allianceId) {
		this.getOutIdSet().add(allianceId);
		this.outIds = this.updateformat(outIdSet);
	}
	
	
}
