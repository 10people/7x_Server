package com.qx.task;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 已完成的任务id集合
 * @author 康建虎
 *
 */
@Entity
@Table(name="FinishTask")
public class FinishTaskBean {
	@Id
	public long pid;
	public String ids;//已完成的任务ID，用#分割
}
