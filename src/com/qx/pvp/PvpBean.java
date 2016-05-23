package com.qx.pvp;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "pvp_bean")
public class PvpBean{

	@Id
	public long junZhuId;
	/*当日已经参加百战的次数*/
	public int usedTimes;
	/*当日剩余参加百战的次数*/
	public int remain; 
	/*军衔等级，对应baizhan.xml的jibie字段*/
	public int junXianLevel = -1;

//	@Column(nullable = false, columnDefinition = "int default -1")
//	public int zuheId; //防守技能
	@Column(nullable = false, columnDefinition = "int default -1")
	public int gongJiZuHeId; //攻击技能
	public int highestRank;//当前最高排名
	public int lastHighestRank; // 上一次最高排名
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
	public int allBattleTimes; // 主动战斗历史次数

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
	
	public Date lastGetAward; // 上次计算领取生产奖励的时间
	public Date lastCalculateAward; // 上次计算生产奖励的时间
	public int leiJiWeiWang; //生产奖励累计威望值
	public int getProduceWeiWangTimes = 0; // 累计领取威望奖励的次数

//	public int dailyaward;
	public int rankAward; // 累计的最高排名奖励
	
}