package com.qx.huangye;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "BuZhenHYPve")
public class BuZhenHYPve {
	
	@Id
	public long junzhuId;
	public long pos1;
	public long pos2;
	public long pos3;
	public int zuheId;
}
