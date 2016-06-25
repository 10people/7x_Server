package com.qx.alliancefight;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class EnterWarTimeBean {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public int dbId;
	public long jzId;
	public int cityId;
	public int lmId;
	public int isIn; //1 代表在战场，0不在在战场
	public Date enterTime;
}
