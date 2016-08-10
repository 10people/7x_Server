package com.qx.alliancefight;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.qx.persistent.DBHash;

@Entity
public class CityBean implements DBHash{
	@Id
	public int cityId;
	public int lmId;
	public Date occupyTime;
	public int atckLmId;//-100表示今日已结束
	@Override
	public long hash() {
		return cityId;
	}
}
