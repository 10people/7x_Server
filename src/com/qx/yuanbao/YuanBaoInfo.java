package com.qx.yuanbao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class YuanBaoInfo {
	@Id
	// @GeneratedValue(strategy=GenerationType.AUTO)
	private long dbId;// 2015年4月17日16:57:30int改为long
	private long ownerid;
	private Date timestamp;
	private String reason;
	private int yuanbaoBefore;
	private int yuanbaoAfter;
	private int yuanbaoChange;
	private int price;// 2015年7月3日13:57 添加消费单价
	private int type;// 2015年7月3日14:12 添加元宝类型，具体类型在YBType.java里
	private int costMoney;// 2015年7月2日18:11 添加人民币金额消费记录，充值元宝和购买使用

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCostMoney() {
		return costMoney;
	}

	public void setCostMoney(int costMoney) {
		this.costMoney = costMoney;
	}

	// 2015年4月17日16:57:30int改为long
	public long getDbId() {
		return dbId;
	}

	public void setDbId(long dbId) {
		this.dbId = dbId;
	}

	public long getOwnerid() {
		return ownerid;
	}

	public void setOwnerid(long ownerid) {
		this.ownerid = ownerid;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public int getYuanbaoBefore() {
		return yuanbaoBefore;
	}

	public void setYuanbaoBefore(int yuanbaoBefore) {
		this.yuanbaoBefore = yuanbaoBefore;
	}

	public int getYuanbaoAfter() {
		return yuanbaoAfter;
	}

	public void setYuanbaoAfter(int yuanbaoAfter) {
		this.yuanbaoAfter = yuanbaoAfter;
	}

	public int getYuanbaoChange() {
		return yuanbaoChange;
	}

	public void setYuanbaoChange(int yuanbaoChange) {
		this.yuanbaoChange = yuanbaoChange;
	}
}
