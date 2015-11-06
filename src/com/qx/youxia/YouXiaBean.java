package com.qx.youxia;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "YouXiaBean")
public class YouXiaBean {
	@Id
	public long id;
	public long junzhuId;
	/**
	 * 游侠玩法的类型
	 */
	public int type;
	
	/**
	 * 今日剩余玩法次数
	 */
	public int times;
	
	/**
	 * 上次玩的时间
	 */
	public Date lastBattleTime;
	
	/**
	 * 今日已经购买的次数
	 */
	public int buyTimes;
	
	/**
	 * 上次购买玩法次数的时间
	 */
	public Date lastBuyTime;
	/**
	 * 历史总共赢得次数
	 */
	public int allWinTimes;
}
