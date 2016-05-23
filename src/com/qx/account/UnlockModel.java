package com.qx.account;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UnlockModel {
	@Id
	public long jzId;
	public String ids;
	public Date lastChangeTime;
}
