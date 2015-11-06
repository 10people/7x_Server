package com.qx.huangye;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "HYResourceTimes")
public class HYResourceTimes {
	@Id
	public long junzhuId;
	
	/** 今日已使用挑战次数 */
	public int times;
	
	/** 上次挑战时间 */
	public Date lastChallengeTime;

	public HYResourceTimes() {
		super();
	}

	public HYResourceTimes(long junzhuId, int times, Date lastChallengeTime) {
		super();
		this.junzhuId = junzhuId;
		this.times = times;
		this.lastChallengeTime = lastChallengeTime;
	}

}
