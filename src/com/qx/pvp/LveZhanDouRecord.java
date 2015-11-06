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
	public int lostGongJin;
	public boolean isHateGuoJia;
	public int addMengShengWang;
	public int addGuoShengWang;
	public int lostMengJianShe;
	
	
	public LveZhanDouRecord(){}
	
	public LveZhanDouRecord(int zhandouId, long gongJiJunId, long fangShouJunId,
			Date time, int result1, int lostGongJin)
	{
		this.zhandouId = zhandouId;
		this.gongJiJunId = gongJiJunId;
		this.fangShouJunId = fangShouJunId;
		this.time = time;
		this.result1 = result1;
		this.lostGongJin = lostGongJin;
	}

}
