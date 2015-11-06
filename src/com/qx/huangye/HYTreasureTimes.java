package com.qx.huangye;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * 每个联盟中的每个玩家对所有关卡共用每日挑战次数
 *
 */
@Entity
@Table(name="hy_treasure_times")
public class HYTreasureTimes {
	@Id
	public long junzhuId;
	public int lianmengId;
	public int times; //总挑战次数
	public Date lastResetTime;
	public int used; // 已用挑战次数
	public int buyBattleHuiShu; //当日已经购买挑战的次数
	public int allBattleTimes; 
	
	public HYTreasureTimes() {
		super();
	}

	public HYTreasureTimes(long junzhuId,int times, int lianmengId) {
		this.junzhuId = junzhuId;
		this.times = times;
		this.lastResetTime = new Date();
		this.lianmengId = lianmengId;
		this.used = 0;
		this.buyBattleHuiShu = 0;
		this.allBattleTimes = 0;
	}
	
}
