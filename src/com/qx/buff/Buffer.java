package com.qx.buff;

import com.qx.junzhu.JunZhu;


/**
 * BUFF对象
 * 
 * @author lzw
 */
public class Buffer {

	/** buffID对应配置表的buffId */
	private int id;
	
	/** buff类型，buff or debuff */
	private int type;
	
	/** 附加值/伤害量. 有正负号 */
	private int damage;
	
	/** 跳动周期. (当跳动周期 > 0时, 表示需要周期计算. 单位: 毫秒) */
	private int cycle;
	
	/** 起始生效时间. 单位: 毫秒 */
	private long startTime;
	
	/**  效果时间. 单位: 毫秒 */
	private long endTime;
	
	/** 释放该Buffer的君主 */
	private JunZhu castJunzhu;
	
	/** 携带该Buffer的君主Id */
	private JunZhu carryJunzhu;
	
	/** 释放该Buffer的单位类型 */
	private int unitType = -1;
	
	/** 上次结算的时间 */
	private volatile long lastCalcTime;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public long getLastCalcTime() {
		return lastCalcTime;
	}

	public void setLastCalcTime(long lastCalcTime) {
		this.lastCalcTime = lastCalcTime;
	}

	public int getCycle() {
		return cycle;
	}

	public void setCycle(int cycle) {
		this.cycle = cycle;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public JunZhu getCastJunzhu() {
		return castJunzhu;
	}

	public void setCastJunzhu(JunZhu castJunzhu) {
		this.castJunzhu = castJunzhu;
	}

	public JunZhu getCarryJunzhu() {
		return carryJunzhu;
	}

	public void setCarryJunzhu(JunZhu carryJunzhu) {
		this.carryJunzhu = carryJunzhu;
	}

	public int getUnitType() {
		return unitType;
	}

	public void setUnitType(int unitType) {
		this.unitType = unitType;
	}


	/**
	 * @param id				效果ID
	 * @param type				buff类型
	 * @param revise			误差时间(单位: 毫秒).
	 * @param damage			施放该效果时造成的伤害量/附加量
	 * @param cycle				跳动周期
	 * @param endTime			效果结束时间
	 * @param castJunzhuId		释放者君主id
	 * @param carryJunzhu		携带buff的君主
	 * @return
	 */
	public static Buffer valueOf(int id, int type, int revise, int damage, int cycle, long endTime, JunZhu castJunzhu, JunZhu carryJunZhu) {
		long currentTimeMillis = System.currentTimeMillis();
		Buffer buffer = new Buffer();
		buffer.id = id;
		buffer.cycle = cycle;
		buffer.type = type;
		buffer.damage = damage;
		buffer.castJunzhu = castJunzhu;
		buffer.carryJunzhu = carryJunZhu;
		buffer.endTime = endTime + revise;
		buffer.startTime = currentTimeMillis + revise;
		buffer.lastCalcTime = currentTimeMillis + revise;
		return buffer;
	}
	
	
	/**
	 * Buffer 生效时间.
	 * 
	 * @return 
	 */
	public boolean isStart() {
		return System.currentTimeMillis() >= this.getStartTime();
	}
	
	/**
	 * 是否超时
	 * 
	 * @return		true-已超时, false-未超时
	 */
	public boolean isTimeOut() {
		return System.currentTimeMillis() >= this.getEndTime();
	}
}
