package com.qx.activity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

@Entity
@Table(name = "Shouchonginfo")
public class ShouchongInfo implements MCSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 44212269225573208L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private long junzhuId;// 君主id
	private Date date;// 首冲时间
	private int hasAward;// 是否领取奖励，0-未领取，1-已领取

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getJunzhuId() {
		return junzhuId;
	}

	public void setJunzhuId(long junzhuId) {
		this.junzhuId = junzhuId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getHasAward() {
		return hasAward;
	}

	public void setHasAward(int hasAward) {
		this.hasAward = hasAward;
	}

	@Override
	public long getIdentifier() {
		// TODO Auto-generated method stub
		return id;
	}

}
