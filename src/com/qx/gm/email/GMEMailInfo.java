package com.qx.gm.email;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "gmemail")
public class GMEMailInfo {
	@Id
	public long id;
	public Date sendDate;
	public String name;
	public int minLevel;
	public int maxLevel;
	public String title;
	public String content;
	public String fujian;
	public int mailType;
}
