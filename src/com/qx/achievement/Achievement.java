package com.qx.achievement;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

@Entity
@Table(name="Achievement")
public class Achievement implements MCSupport{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8620633312424981760L;
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	private int chengjiuId;
	private long junZhuId;
	private int type;
	private int jindu;
	private boolean isFinish;
	private boolean isGetReward;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getJunZhuId() {
		return junZhuId;
	}
	public void setJunZhuId(long junZhuId) {
		this.junZhuId = junZhuId;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getChengjiuId() {
		return chengjiuId;
	}
	public void setChengjiuId(int chengjiuId) {
		this.chengjiuId = chengjiuId;
	}
	public int getJindu() {
		return jindu;
	}
	public void setJindu(int jindu) {
		this.jindu = jindu;
	}
	public boolean isFinish() {
		return isFinish;
	}
	public void setFinish(boolean isFinish) {
		this.isFinish = isFinish;
	}
	public boolean isGetReward() {
		return isGetReward;
	}
	public void setGetReward(boolean isGetReward) {
		this.isGetReward = isGetReward;
	}
	@Override
	public long getIdentifier() {
		return id;
	}
	
}
