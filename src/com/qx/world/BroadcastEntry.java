package com.qx.world;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * 定时广播设置
 * @author 康建虎
 *
 */
@Entity
public class BroadcastEntry {
	@Id
	public int id;
	public Date startTime;
	public Date endTime;
	public boolean open = false;
	public Date lastSendTime;
	public int intervalMinutes;
	public String content;
}
