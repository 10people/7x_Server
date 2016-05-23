package com.qx.activity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class GrowthFundBuyBean {
	@Id
	public long jzId;
	public Date buyTime;
}
