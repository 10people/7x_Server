package com.qx.activity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * @Description 为了实现自动关闭以服务器时间为准的活动用的类
 *
 */
@Entity
@Table(name = "GlobalActivityBean")
public class GlobalActivityBean {
	@Id
	public int id;
	public Date startTime;
}
