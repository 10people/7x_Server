package com.qx.huangye;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.qx.util.TableIDCreator;

/**
 * 荒野藏宝点
 * @author lizhaowen
 *
 */
@Entity
public class HYTreasureDamage implements Comparable<HYTreasureDamage>{
	@Id
	public long id;//2015年4月17日16:57:30int改为long
	
	public long treasureId;//2015年4月17日16:57:30改为long
	
	public long junzhuId;
	
	public int damage; // 累计伤害总值
	public int  historyMaxDamage;

//	public int lianMengId;    // 不记录联盟id和君主名字，有可能有变动
//	public String junzhuName;

	public HYTreasureDamage() {
		super();
	}
	//2015年4月17日16:57:30int改为long
	public HYTreasureDamage(long treasureId, long junzhuId, int damage) {
		super();
		//改自增主键为指定
		//2015年4月17日16:57:30int改为long
		this.id=(TableIDCreator.getTableID(HYTreasureDamage.class, 1L));

		this.treasureId = treasureId;
		this.junzhuId = junzhuId;
		this.damage = damage;
		// 最大伤害值初始值等于 damage
		this.historyMaxDamage = damage;
	}
	@Override
	public int compareTo(HYTreasureDamage o) {
		return o.historyMaxDamage - this.historyMaxDamage;
	}
	
}
