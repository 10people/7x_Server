package com.qx.vip;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class VipGiftbag {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long id;
	public long junzhuId;
	public int vipLevel;
}
