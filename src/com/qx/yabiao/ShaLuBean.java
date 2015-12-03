package com.qx.yabiao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ShaLuBean")
public class ShaLuBean {// 押镖杀戮相关数据存储
	@Id
	public long jzId;
	/*当然杀死仇人数目*/
	public int count;
	/*当日剩余参加押镖的次数*/
	public int remainYB; 
	public Date lastResetTime;
}