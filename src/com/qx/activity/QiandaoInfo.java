package com.qx.activity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;
import com.qx.persistent.MCSupport;

@Entity
@Table(name = "Qiandaoinfo")
public class QiandaoInfo implements MCSupport,DBHash {
	/**
	 * 
	 */
	public static final long serialVersionUID = 1881219285366565822L;
	@Id
	public long id;//用作君主id
	public Date preQiandao;// 上次签到时间
	public String qiandaoDate;// 签到日期 :分割月日，#分割两个日期
	public String getDoubleDate;// 领取双倍奖励的日期,分割同上
	public int historyQianDao;
	public int leijiQiandao;// 累计签到
	@Override
	public long getIdentifier() {
		// TODO Auto-generated method stub
		return id;
	}
	@Override
	public long hash() {
		return id;
	}

}
