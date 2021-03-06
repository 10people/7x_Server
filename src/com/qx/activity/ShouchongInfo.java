package com.qx.activity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;
import com.qx.persistent.MCSupport;

@Entity
@Table(name = "Shouchonginfo")
public class ShouchongInfo implements DBHash{
	@Id
	public long junzhuId;// 君主id
	public Date date;// 首冲时间
	public int hasAward;// 是否领取奖励，0-未领取，1-已领取
	@Override
	public long hash() {
		return junzhuId;
	}

}
