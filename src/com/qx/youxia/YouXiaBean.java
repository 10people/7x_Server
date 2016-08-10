package com.qx.youxia;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.qx.persistent.DBHash;

@Entity
@Table(name = "YouXiaBean",indexes={@Index(name="junzhuId",columnList="junzhuId")})
public class YouXiaBean implements DBHash {
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
	public int allBattleTimes;
	@Override
	public long hash() {
		return junzhuId;
	}
	
}
