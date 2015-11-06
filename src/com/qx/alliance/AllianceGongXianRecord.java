package com.qx.alliance;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class AllianceGongXianRecord {
	@Id
	private long junZhuId;
	
	@Column(columnDefinition = "INT default 0")
	private int curMonthGongXian;
	
	private Date curMonthFirstTime;

	public long getJunZhuId() {
		return junZhuId;
	}

	public void setJunZhuId(long junZhuId) {
		this.junZhuId = junZhuId;
	}

	public int getCurMonthGongXian() {
		return curMonthGongXian;
	}

	public void setCurMonthGongXian(int curMonthGongXian) {
		this.curMonthGongXian = curMonthGongXian;
	}

	public Date getCurMonthFirstTime() {
		return curMonthFirstTime;
	}

	public void setCurMonthFirstTime(Date curMonthFirstTime) {
		this.curMonthFirstTime = curMonthFirstTime;
	}
	
}
