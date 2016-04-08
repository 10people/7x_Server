package com.qx.yabiao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "YaBiaoBackUp")
public class YaBiaoBackUp {
	@Id
	public long jzId;
	public int horseType;
	public long usedTime;
	public int pathId;
	public int node;
	/** 
	 * 是否需要恢复的标记 
	 * true:马车出发
	 * false:马车被消灭或者到达终点
	 */
	public boolean backupFlag;
	

}