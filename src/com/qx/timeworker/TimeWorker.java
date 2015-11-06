package com.qx.timeworker;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

/**
 * 定时请求任务
 * @author lizhaowen
 *
 */
@Entity
@Table(name = "TimeWorker")
public class TimeWorker implements MCSupport{
	/**
	 * @Fields serialVersionUID : TODO
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public long junzhuId;

	public Date lastAddTiliTime;
	
	/**
	 * 剩余次数
	 */
	public int xilianTimes;
	
	public Date lastAddXilianTime;
	
	public long getJunzhuId() {
		return junzhuId;
	}

	public void setJunzhuId(long junzhuId) {
		this.junzhuId = junzhuId;
	}

	public Date getLastAddTiliTime() {
		return lastAddTiliTime;
	}

	public void setLastAddTiliTime(Date lastAddTiliTime) {
		this.lastAddTiliTime = lastAddTiliTime;
	}

	public int getXilianTimes() {
		return xilianTimes;
	}

	public void setXilianTimes(int xilianTimes) {
		this.xilianTimes = xilianTimes;
	}

	public Date getLastAddXilianTime() {
		return lastAddXilianTime;
	}

	public void setLastAddXilianTime(Date lastAddXilianTime) {
		this.lastAddXilianTime = lastAddXilianTime;
	}

	@Override
	public long getIdentifier() {
		return junzhuId;
	}
	
}
