package com.qx.huangye;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

@Entity
public class HYFog {
	
	@Id
	public int lianMengId;
	
//	@Column(columnDefinition = "TEXT")
	public String haveFogIds;
	
//	@Column(columnDefinition = "TEXT")
	public String openFogIds;
	
	@Transient
	private Set<Integer> haveFogIdSet;
	
	@Transient
	private Set<Integer> openFogIdSet;
	
	@Transient
	public static String SPLIT_SYMBOL = "_";
	
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
			for (long fogId : formatSet) {
				builder.append(fogId).append(SPLIT_SYMBOL);
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
	 * @return Set<Long>
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
	
	public void addHaveFogId(int fogId) {
		this.getHaveFogIdSet().add(fogId);
		this.haveFogIds = updateformat(haveFogIdSet);
	}
	
	public void addOpenFogId(int fogId) {
		this.getOpenFogIdSet().add(fogId);
		this.openFogIds = updateformat(openFogIdSet);
	}

	public Set<Integer> getOpenFogIdSet() {
		openFogIdSet = getCacheSet(openFogIds, openFogIdSet);
		return openFogIdSet;
	}

	public Set<Integer> getHaveFogIdSet() {
		haveFogIdSet = getCacheSet(haveFogIds, haveFogIdSet);
		return haveFogIdSet;
	}

	public boolean isFogOpen(int fogId) {
		boolean b1 = this.getHaveFogIdSet().contains(fogId);
		boolean b2 = this.getOpenFogIdSet().contains(fogId);
		return b1 && b2;
	}
	
	
}
