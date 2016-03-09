package com.qx.activity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
@Entity
@Table(name = "GlobalActivityBean")
public class GlobalActivityBean {
	@Id
	public int id;
	public Date startTime;
}
