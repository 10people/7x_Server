package com.qx.yabiao;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

@Entity
@Table(name = "YaBiaoInfo")
public class YaBiaoInfo implements MCSupport {
	/**
	 * @Fields serialVersionUID : TODO
	 */
	private static final long serialVersionUID = 1L;
	@Id
	public long junZhuId;
	/*当日已经参加押镖的次数*/
	public int usedYB;
	/*当日剩余参加押镖的次数*/
	public int remainYB; 
	/*当日已经参加劫镖的次数*/
	public int usedJB;
	/*当日剩余参加劫镖的次数*/
	public int remainJB;
	/*当日已经参加协助的次数*/
	public int usedXZ;
	/*当日剩余参加协助的次数*/
	public int remainXZ;
	/*当日已经请求协助的次数*/
	public int usedAskXZ;
	/*当日剩余请求协助的次数*/
	public int remainAskXZ;
	/*镖车类型*/
	public int horseType;
	/*镖车的价值（百分比）*/
	public int  worth;
	@Column(nullable = false, columnDefinition = "int default -1")
	public int zuheId; //防守技能
	@Column(nullable = false, columnDefinition = "int default -1")
	public int gongJiZuHeId; //攻击技能
	/*镖车的血量*/
	public int  hp;
	public int  hudun;
	public int  hudunMax;
	/*上次押镖的时间*/
//	public Date lastYBDate; 没用字段
	/*上次劫镖镖的时间--计算劫镖冷却*/
	public Date lastJBDate;
	/*本日购买押镖次数*/
	public int todayBuyYBTimes;
	/*本日购买劫镖次数*/
	public int todayBuyJBTimes;
	/*上次查看运镖信息的时间 --处理运镖数据刷新*/
	public Date lastShowTime;

	public boolean isNew4History;//是否有新历史记录
	public boolean isNew4Enemy;//是否有新仇人
	

	@Override
	public long getIdentifier() {
		return junZhuId;
	}
}