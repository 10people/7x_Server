package com.qx.alliance;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "FengshanBean")
public class FengshanBean {
	@Id
	public long jzId;
	public Date lastResetTime;
	public boolean isGetFengShan1 ;
	public boolean isGetFengShan2 ;
}
