package com.qx.buff;

import com.manu.dynasty.template.Buff;
import com.qx.junzhu.JunZhu;
import com.qx.world.Player;
import com.qx.world.Scene;


/**
 * BUFF对象
 * 
 * @author lzw
 */
public class Buffer {
	public Scene sc;
	public Buff buffConf;
	
	
	/** 起始生效时间. 单位: 毫秒 */
	public long startTime;
	
	/**  效果时间. 单位: 毫秒 */
	public long endTime;
	
	/** 释放该Buffer的君主 */
	public JunZhu castJunzhu;
	
	/** 携带该Buffer的君主Id */
	public JunZhu carryJunzhu;
	
	public int sceneUid;
	
	public Player carryPlayer;
	
	/** 释放该Buffer的单位类型 */
	public int unitType = -1;
	
	/** 上次结算的时间 */
	public volatile long lastCalcTime;
	public boolean stop;

	/**
	 * @param id				效果ID
	 * @param type				buff类型
	 * @param revise			误差时间(单位: 毫秒).
	 * @param damage			施放该效果时造成的伤害量/附加量
	 * @param cycle				跳动周期
	 * @param endTime			效果结束时间
	 * @param castJunzhuId		释放者君主id
	 * @param carryJunzhu		携带buff的君主
	 * @param sceneUid			场景里的id
	 * @return
	 */
	public static Buffer valueOf(Buff conf, int buffDuration, JunZhu castJunzhu, JunZhu carryJunZhu, int sceneUid) {
		Buffer buffer = new Buffer();
		buffer.buffConf = conf;
		buffer.castJunzhu = castJunzhu;
		buffer.carryJunzhu = carryJunZhu;
		buffer.startTime = System.currentTimeMillis()+conf.EffectTime;
		buffer.endTime = buffer.startTime + buffDuration;
		buffer.lastCalcTime = buffer.startTime;
		buffer.sceneUid = sceneUid;
		return buffer;
	}
}
