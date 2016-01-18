package com.manu.dynasty.chat;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ChatInfo {
	@Id
	public long jzId;
	public Date lastTime;
	public int useTimes;
}
