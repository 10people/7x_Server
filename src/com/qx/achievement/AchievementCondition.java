package com.qx.achievement;

/**
 * 成就进度推送
 * @author lizhaowen
 *
 */
public class AchievementCondition {
	private long junzhuId;
	private int type;
	private int jinduAdd;
	/**
	 * @param junzhuId		君主id
	 * @param type			成就类型
	 * @param jinduAdd		进度增加次数
	 */
	public AchievementCondition(long junzhuId, int type, int jinduAdd) {
		super();
		this.junzhuId = junzhuId;
		this.type = type;
		this.jinduAdd = jinduAdd;
	}
	public int getType() {
		return type;
	}
	public int getJinduAdd() {
		return jinduAdd;
	}
	public long getJunzhuId() {
		return junzhuId;
	}
	
}
