package com.qx.task;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

@Entity
@Table
public class DailyTaskBean implements MCSupport{
	private static final long serialVersionUID = 1L;
	@Id
	// id = id * 100 + renWuId;
	// renWuId = id % 100;
	// junzhuid = (id -renWuId )/100
	public long dbId;
	public int jundu;
	public boolean isFinish;
	public boolean isGetReward;
	public int type;
	public Date time;	//玩家最近一次请求每日任务时间，用于判断是否刷新每日任务
	/*
	 * 百战的每日对手的国家
	 */
	public int duiShouGuoJia1;
	public int duiShouGuoJia2;
	
	// 贡金的条件进度
	@Column(columnDefinition = "INT default 1")
	public int gongJinCondition = 1;

	public long getIdentifier() {
		return dbId;
	}
}
