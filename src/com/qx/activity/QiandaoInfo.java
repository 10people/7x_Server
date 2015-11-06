package com.qx.activity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

@Entity
@Table(name = "Qiandaoinfo")
public class QiandaoInfo implements MCSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1881219285366565822L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private long junzhuId;// 君主id
	private Date preQiandao;// 上次签到时间
	private String qiandaoDate;// 签到日期 :分割月日，#分割两个日期
	private String getDoubleDate;// 领取双倍奖励的日期,分割同上

	public String getGetDoubleDate() {
		return getDoubleDate;
	}

	public void setGetDoubleDate(String getDoubleDate) {
		this.getDoubleDate = getDoubleDate;
	}

	public String getQiandaoDate() {
		return qiandaoDate;
	}

	public void setQiandaoDate(String qiandaoDate) {
		this.qiandaoDate = qiandaoDate;
	}

	private int leijiQiandao;// 累计签到

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

	public Date getPreQiandao() {
		return preQiandao;
	}

	public void setPreQiandao(Date preQiandao) {
		this.preQiandao = preQiandao;
	}

	public int getLeijiQiandao() {
		return leijiQiandao;
	}

	public void setLeijiQiandao(int leijiQiandao) {
		this.leijiQiandao = leijiQiandao;
	}

	@Override
	public long getIdentifier() {
		// TODO Auto-generated method stub
		return id;
	}

}
