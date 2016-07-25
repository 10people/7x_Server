package com.qx.alliancefight;

public class CdTime {
	public long junzhuId;
	
	public long endTime;
	
	public CdTime() {
		super();
	}

	public CdTime(long junzhuId, long endTime) {
		super();
		this.junzhuId = junzhuId;
		this.endTime = endTime;
	}

	public long getJunzhuId() {
		return junzhuId;
	}

	public void setJunzhuId(long junzhuId) {
		this.junzhuId = junzhuId;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	public boolean isTimeOut() {
		return this.endTime <= System.currentTimeMillis();
	}
	
	
}
