package com.manu.dynasty.template;

public class QianDao {
	private int id;
	private int month;
	private int day;
	private int awardType;
	private int awardId;

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getAwardType() {
		return awardType;
	}

	public void setAwardType(int awardType) {
		this.awardType = awardType;
	}

	public int getAwardId() {
		return awardId;
	}

	public void setAwardId(int awardId) {
		this.awardId = awardId;
	}

	public int getAwardNum() {
		return awardNum;
	}

	public void setAwardNum(int awardNum) {
		this.awardNum = awardNum;
	}

	public int getVipDouble() {
		return vipDouble;
	}

	public void setVipDouble(int vipDouble) {
		this.vipDouble = vipDouble;
	}

	private int awardNum = 1;
	private int vipDouble = 1;
}
