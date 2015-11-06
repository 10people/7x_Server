package com.qx.pvp;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

@Entity
@Table(name = "PvpBean")
public class PvpBean implements MCSupport {
	/**
	 * @Fields serialVersionUID : TODO
	 */
	private static final long serialVersionUID = 1L;
	@Id
	public long junZhuId;
////	要删除，/* 排名*/
	public int rank;
	/*当日已经参加百战的次数*/
	public int usedTimes;
	/*当日剩余参加百战的次数*/
	public int remain; 
	/*军衔等级*/
	public int junXianLevel = -1;

	/*mibaoDB.java中的miBaoId;//配置文件mibao中id字段，品质可以根据它来查到*/
	public long miBao1 = -1; // 
	public long miBao2 = -1;
	public long miBao3 = -1;
	@Column(nullable = false, columnDefinition = "int default -1")
	public int zuheId; //防守技能
	@Column(nullable = false, columnDefinition = "int default -1")
	public int gongJiZuHeId; //攻击技能
	public int highestRank;
	public int winToday; 
	@Column(columnDefinition = "INT default 0")
	public int allWin;
	/*今日已经购买百战的回数*/
	public int buyCount;
	/* 今日已购买 清除百战CD的次数 */
	public int cdCount;

	/*战斗记录是否被玩家查看： true ：表示被查看，false 表示没有 */
	@Column(nullable = false, columnDefinition = "boolean default true")
	public boolean isLook;

	/*上次百战的时间*/
	public Date lastDate;
	/*上次查看百战信息的时间*/
	public Date lastShowTime;

	/*上次发送每日奖励的时间*/
	public Date lastAwardTime;

	// 当日刷新对手列表的次数
	@Column(columnDefinition = "INT default 0")
	public int todayRefEnemyTimes;

	// 显示的威望值
	public int showWeiWang;
	// 结算的威望值
	public int lastWeiWang;
	// 总共参加百战的次数
	public int allBattleTimes;

	/*
	 * 对手
	 */
	public int rank1;
	public int rank2;
	public int rank3;
	public int rank4;
	public int rank5;
	public int rank6;
	public int rank7;
	public int rank8;
	public int rank9;
	public int rank10;
	@Column(columnDefinition = "DATETIME default '2014-12-01 00:00:00'")
	public Date initPvpTime;
	

	@Override
	public long getIdentifier() {
		return junZhuId;
	}
}