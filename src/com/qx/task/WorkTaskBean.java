package com.qx.task;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 进行中的任务。
 * @author 康建虎
 *
 */
@Entity
@Table(name="WorkTask")
public class WorkTaskBean {
	/**
	 * 规则 玩家id*100+N。每个玩家可以最多拥有100个任务。
	 */
	@Id
	public long dbId;
	public int tid;//任务ID, 主线表中的id
	/*
	 * 0: 添加此任务，未完成
	 * -1：任务已经完成
	 * -2： 任务已经完成，且已经领奖。
	 * 已领奖的任务客户端看不到。
	 */
	public int progress;
}
