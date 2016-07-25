package com.qx.alliance;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.qx.persistent.DBHash;

@Entity
public class MoBaiBean implements DBHash{
	@Id
	public long junZhuId;
	public Date tongBiTime;
	public Date yuanBaoTime;
	public Date yuTime;
	/**
	 * 玉膜拜已用次数。
	 */
	public int yuTimes;
	//
	public Date step1time;//阶段性奖励领取时间，有值且是当天，则表示领取过了。
	public Date step2time;//阶段性奖励领取时间，有值且是当天，则表示领取过了。
	public Date step3time;//阶段性奖励领取时间，有值且是当天，则表示领取过了。
	@Override
	public long hash() {
		return junZhuId;
	}
}
