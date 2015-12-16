package com.qx.pvp;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="lve_duo")
public class LveDuoBean  {//implements MCSupport {
	/**
	 * @Fields serialVersionUID : TODO
	 */
	private static final long serialVersionUID = 1L;
	@Id
	public long junzhuId;
	/**当日已掠夺次数*/
	public int usedTimes;
	/**当日掠夺总次数*/
	public int todayTimes;
	/**今日已经购  买战斗权利的"回"数*/
	public int buyBattleHuiShu;
	/** 今日已购买 清除CD的次数 */
	public int buyClearCdCount;
	public int fangShouZuHeId;
	public int gongJiZuHeId = -1;
	public int todayWin;
	public int hisWin;
	public int hisAllBattle;
	@Column(nullable = true)
	public Date lastRestTime;
	@Column(nullable = true)
	public Date lastBattleTime; //攻击数据
	public Date lastBattleEndTime; // 防守数据
	public boolean hasRecord; // true: 有新记录，false:没有
	
	/**获取贡金排行奖励的时间*/
	public Date getGongJinTime;

//	@Override
//	public long getIdentifier() {
//		// TODO Auto-generated method stub
//		return junzhuId;
//	}
	public LveDuoBean(){}
	public LveDuoBean(long junzhuId){
		this.junzhuId = junzhuId;
		this.todayTimes = LveStaticData.free_all_battle_times;
		this.fangShouZuHeId = -1;
		this.gongJiZuHeId = -1;
		
	} 
}
