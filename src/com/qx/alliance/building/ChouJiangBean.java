package com.qx.alliance.building;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ChouJiangBean {
	@Id
	public long jzId;
	public Date createTime;
	
	@Column(length=1024)
	public String str;
	
	@Column(columnDefinition = "INT default 0")
	public int todayUsedTimes;
	public int todayLeftTimes;
	public int historyAll;
}
