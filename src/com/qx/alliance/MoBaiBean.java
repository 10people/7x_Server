package com.qx.alliance;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MoBaiBean {
	@Id
	public long junZhuId;
	public Date tongBiTime;
	public Date yuanBaoTime;
	public Date yuTime;
	/**
	 * 玉膜拜已用次数。
	 */
	public int yuTimes;
}
