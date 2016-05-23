package com.qx.vip;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class PlayerVipInfo {
	// 玩家id 也就是junzhuId
	@Id
	public long accId;
	// 充值总金额
	public int sumAmount;
	// VIP等级
	public int level;
	// vip经验
	@Column(columnDefinition = "INT default 0")
	public int vipExp;
	
	@Column(columnDefinition = "INT default 0")
	public int yueKaRemianDay;
	public Date lastUpdateYuekaTime;
	
	@Column(columnDefinition = "INT default 0")
	/** 是否购买了终身卡：0-没有，1-购买了 **/
	public int haveZhongShenKa;
}
