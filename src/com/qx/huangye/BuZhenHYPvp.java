package com.qx.huangye;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;

@Entity
@Table(name = "BuZhenHYPvp")
public class BuZhenHYPvp implements DBHash {

	@Id
	public long junzhuId;
	public long pos1;
	public long pos2;
	public long pos3;
	public int zuheId;

	@Override
	public long hash() {
		return junzhuId;
	}
}
