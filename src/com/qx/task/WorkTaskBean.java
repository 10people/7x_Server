package com.qx.task;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.manu.dynasty.template.ZhuXian;
import com.qx.persistent.DBHash;


@Entity
@Table(name = "WorkTask",indexes={@Index(name="jzid",columnList="jzid")})
public class WorkTaskBean implements DBHash{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)	
	/** 自增主键，用于删改任务 */
	public long dbId;
	
	/** 持有任务的君主ID */
	public long jzid;
	
	/** 任务ID, 主线表中的id */
	public int tid;//
	/**
	 * 0: 添加此任务，未完成。
	 * -1：任务已经完成。
	 * -2： 任务已经完成，且已经领奖。
	 * 已领奖的任务客户端看不到。
	 */
	public int progress;

	@Override
	public long hash() {
		return jzid;
	}
	
}
