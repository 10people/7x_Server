package com.qx.task;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 */
@Entity
@Table(name="DailyTaskActivity")
public class DailyTaskActivity {
	@Id
	public long jid;
	/*
	 * 这两个字段是变化的，所以调用的时候需要 调用 DailyTaskMgr.getTodayHuoYueDu
	 */
	protected int todyHuoYue;
	protected int weekHuoYue;

	public Date lastResetDaily;
	public Date lastResetWeek;
 	public boolean isGet1;// true 已经领取，fasle没有
	public boolean isGet2;
	public boolean isGet3;
	public boolean isGet4;
	public boolean isGet5;
	public boolean isGet6;
	public boolean isGet7;
}
