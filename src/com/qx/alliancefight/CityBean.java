package com.qx.alliancefight;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CityBean {
	@Id
	public int cityId;
	public int lmId;
	public Date occupyTime;
	public int atckLmId;//-100表示今日已结束
}
