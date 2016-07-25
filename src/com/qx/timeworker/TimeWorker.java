package com.qx.timeworker;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

/**
 * 定时请求任务
 * @author lizhaowen
 *
 */
@Entity
@Table(name = "TimeWorker")
public class TimeWorker implements MCSupport{
	/**
	 * @Fields serialVersionUID : TODO
	 */
	public static final long serialVersionUID = 1L;

	@Id
	public long junzhuId;

	public Date lastAddTiliTime;
	
	/**
	 * 剩余次数
	 */
	public int xilianTimes;
	
	public Date lastAddXilianTime;
	
	@Override
	public long getIdentifier() {
		return junzhuId;
	}
	
}
