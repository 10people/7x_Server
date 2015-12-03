package com.qx.pvp;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
@Entity
@Table(name="lve_zhandou_r")
public class LveZhanDouRecord {
	@Id
	public int zhandouId;
	public long gongJiJunId; // 挑战者（攻击方）
	public long fangShouJunId; //被挑战者（防守方）
	/*战斗发生的时间(结束时间)*/
	public Date time;
	// 1 攻击胜利 2攻击失败
	public int result1;

	public boolean isHateGuoJia;

	/*
	 * 当 result1 == PVPConstant.GONG_JI_WIN：： 
	 * 		攻击方得到值，防守方损失相应的值 
	 * 
	 * 当  result1 == PVPConstant.GONG_JI_LOSE
	 *  	攻击方得到保底值，防守方零损失
	 * 
	 */
	public int gongJiGetGongjin;
	public int gongjiGetGuoSW;
	public int gongJiGetMengJianShe;
	

	public LveZhanDouRecord(){}
	
	public LveZhanDouRecord(int zhandouId, long gongJiJunId, long fangShouJunId,
			Date time, int result1)
	{
		this.zhandouId = zhandouId;
		this.gongJiJunId = gongJiJunId;
		this.fangShouJunId = fangShouJunId;
		this.time = time;
		this.result1 = result1;
	}

}
