package com.qx.junzhu;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ChengHaoBean {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long dbId;//
	public long jzId;
	public int tid;
	public char state;//U
	public Date expireTime;
}
