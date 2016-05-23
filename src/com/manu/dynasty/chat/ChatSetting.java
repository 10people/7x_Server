package com.manu.dynasty.chat;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ChatSetting {
	@Id
	public long junZhuId;
	public boolean wolrd;
	public boolean lianMeng;
	public boolean siLiao;
	public boolean wifiAutoPlayer;

}
