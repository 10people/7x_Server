package com.qx.alliance;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name="HouseApply",indexes={@Index(name="keeperId",columnList="keeperId")})
public class HouseApplyBean {
	//v.	一位玩家同时只能申请同一个房屋进行交换；
	@Id
	public long buyerId;
	public long keeperId;
	public long emailId;//2015年4月17日16:57:30int改为long
	public Date dt;
}
