package com.qx.pawnshop;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pawnshop")
public class PawnshopBean {
	@Id
	private long junzhuId;
	@Column(length = 10000)
	private String goodsInfo;
	private Date lastRefreshTime;	// 上次手动刷新事件
	private int refreshTimes; 		// 当日手动刷新累计次数
	private Date autoRefreshTime;	// 上次自动刷新时间

	public long getJunzhuId() {
		return junzhuId;
	}

	public void setJunzhuId(long junzhuId) {
		this.junzhuId = junzhuId;
	}

	public String getGoodsInfo() {
		return goodsInfo;
	}

	public void setGoodsInfo(String goodsInfo) {
		this.goodsInfo = goodsInfo;
	}

	public Date getLastRefreshTime() {
		return lastRefreshTime;
	}

	public void setLastRefreshTime(Date lastRefreshTime) {
		this.lastRefreshTime = lastRefreshTime;
	}

	public int getRefreshTimes() {
		return refreshTimes;
	}

	public void setRefreshTimes(int refreshTimes) {
		this.refreshTimes = refreshTimes;
	}

	public Date getAutoRefreshTime() {
		return autoRefreshTime;
	}

	public void setAutoRefreshTime(Date autoRefreshTime) {
		this.autoRefreshTime = autoRefreshTime;
	}
	
	
}
