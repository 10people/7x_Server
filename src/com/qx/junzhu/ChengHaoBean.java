package com.qx.junzhu;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.qx.persistent.DBHash;

@Entity
public class ChengHaoBean implements DBHash{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long dbId;//
	public long jzId;
	public int tid;
	public char state;//U
	public Date expireTime;
	@Override
	public long hash() {
		return jzId;
	}
}
