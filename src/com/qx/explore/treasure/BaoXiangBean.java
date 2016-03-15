package com.qx.explore.treasure;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BaoXiangBean {
	@Id
	public long id;
	public long jzId;
	public String jzName;
	public Date chouJiangTime;//十连抽时间
	public Date openTime;
	public int amount;
	@Column(columnDefinition = "INT default 5")
	public int total;
}
