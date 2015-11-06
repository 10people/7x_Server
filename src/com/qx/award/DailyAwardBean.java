package com.qx.award;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

@SuppressWarnings("serial")
@Entity
@Table(name = "DailyAward")
public class DailyAwardBean implements MCSupport {
	@Id
	public long junZhuId;
	
	public Date preDaily;
	
	public int leiJiLogin;
	public Date preLogin;
	@Override
	public long getIdentifier() {
		return junZhuId;
	}
}
