package com.qx.alliancefight;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class BidBean {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public int dbId;
	
	public int cityId;
	public int lmId;
	public int priceReal; 
	public int priceCache;
	public Date bidTime;
	public int type; //1-普通城，2-野城
}
