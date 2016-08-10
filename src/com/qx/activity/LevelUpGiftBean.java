package com.qx.activity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.qx.persistent.DBHash;

@Entity
public class LevelUpGiftBean implements DBHash{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public int dbId;
	public long jzId;
	public int level;
	public int getState; //1代表应领取了
	public Date getTime;
	@Override
	public long hash() {
		return dbId;
	}
}
