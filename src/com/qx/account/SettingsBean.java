package com.qx.account;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SettingsBean {
	@Id
	public long id;
	public String str;
}
