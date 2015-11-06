package com.qx.vip;

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
}
