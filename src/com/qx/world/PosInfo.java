package com.qx.world;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;

@Entity
@Table
public class PosInfo implements DBHash{
	@Id
	public long jzId;
	public float x;
	public float y;
	public float z;
	//
	@Column(columnDefinition = "INT default 0")
	public int showWuQi;
	@Override
	public long hash() {
		return jzId;
	}
}
