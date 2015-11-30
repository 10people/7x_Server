package com.qx.cdkey;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class CDKeyInfo {
	@Id
	private long keyId;
	private String cdkey;
	private Date deadDate;
	private String awards;// 奖励，type:itemId:count#type:itemId:count(类型，id,数量#类型，id,数量)
	private int chanId;// 渠道id
	private long jzId;// 使用者
	private Date createDate;// 创建日期

	public Date getDeadDate() {
		return deadDate;
	}

	public void setDeadDate(Date deadDate) {
		this.deadDate = deadDate;
	}

	public String getAwards() {
		return awards;
	}

	public void setAwards(String awards) {
		this.awards = awards;
	}

	public long getJzId() {
		return jzId;
	}

	public void setJzId(long jzId) {
		this.jzId = jzId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public int getChanId() {
		return chanId;
	}

	public void setChanId(int chanId) {
		this.chanId = chanId;
	}

	public long getKeyId() {
		return keyId;
	}

	public void setKeyId(long keyId) {
		this.keyId = keyId;
	}

	public String getCdkey() {
		return cdkey;
	}

	public void setCdkey(String cdkey) {
		this.cdkey = cdkey;
	}

}
