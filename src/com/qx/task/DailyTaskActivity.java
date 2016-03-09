package com.qx.task;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.util.DateUtils;
import com.qx.persistent.HibernateUtil;

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
	protected boolean isGet1;// true 已经领取，fasle没有
	protected boolean isGet2;
	protected boolean isGet3;
	protected boolean isGet4;
	protected boolean isGet5;
	protected boolean isGet6;
	protected boolean isGet7;
	
	
	public int getTodyHuoYue() {
		if(this.lastResetDaily != null && DateUtils.isTimeToReset(this.lastResetDaily,
				CanShu.REFRESHTIME_PURCHASE)){
			this.todyHuoYue = 0;
		}
		return this.todyHuoYue;
	}
	public int getWeekHuoYue() {
		if(this.lastResetWeek != null){
			boolean isSameWeek = DateUtils.isSameWeek_CN(new Date(), this.lastResetWeek);
			if(!isSameWeek){
				this.weekHuoYue = 0;
			}
		}
		return this.weekHuoYue;
	}
	public void setTodyHuoYue(int todyHuoYue) {
		this.todyHuoYue = todyHuoYue;
	}
	public void setWeekHuoYue(int weekHuoYue) {
		this.weekHuoYue = weekHuoYue;
	}
}
