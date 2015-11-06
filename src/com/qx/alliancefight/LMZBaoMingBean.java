package com.qx.alliancefight;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class LMZBaoMingBean {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int dbId;
	public int lmId;
	public int season;//第几届
	public String lmName;
	public long mengZhuId;
	public String mengZhuName;
	public Date baoMingTime;
}
