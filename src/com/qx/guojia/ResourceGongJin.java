package com.qx.guojia;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;
@Entity
@Table(name="resource_gongjin")
public class ResourceGongJin implements DBHash{
	@Id
	public long junzhuId;
//	/**拥有的贡金: 调用无效*/
//	public int gongJin;
//	/**上次计算贡金的时间*/
//	public Date lastGetGongJinTime;
//	/**当日捐献贡金数目*/
//	public int todayJX;
//	public int todayJXTimes;
//	/**当周捐献贡金数目*/
//	public int thisWeekJX;
//	/**上日捐献贡金数目*/
//	public int lastJX;
//	/**上周捐献贡金数目*/
//	public int lastWeekJX;

//	public Date lastRestTime;
//	/**捐献时间*/
//	public Date juanXianTime;
	/*
	 * 根据国家声望获取的奖励
	 */
	/**获取周奖励的时间*/
	public Date getWeekAwardTime;
	/**获取日奖励的时间*/
	public Date getDayAwardTime;
	@Override
	public long hash() {
		return junzhuId;
	}
}
