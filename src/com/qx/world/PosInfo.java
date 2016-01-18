package com.qx.world;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class PosInfo {
	@Id
	public long jzId;
	public float x;
	public float y;
	public float z;
	//
	@Column(columnDefinition = "INT default 0")
	public int showWuQi;
}
