package com.qx.yabiao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "YaBiaoBean12")
public class YaBiaoBean {// implements MCSupport
	@Id
	public long junZhuId;
	/*当日已经参加押镖的次数*/
	public int usedYB;
	/*当日剩余参加押镖的次数*/
	public int remainYB; 
//2015年12月12日 1.1版本协助次数完全没限制了
//	/*当日已经参加协助的次数*/
//	public int usedXZ;
//	/*当日剩余参加协助的次数*/
//	public int remainXZ;
	/*当日已经请求协助的次数*/
	public int usedAskXZ;
	/*当日剩余请求协助的次数*/
	public int remainAskXZ;
	/*镖车类型*/
	public int horseType;
	/*本日购买押镖次数*/
	public int todayBuyYBTimes;

	/*上次查看运镖信息的时间 --处理运镖数据刷新*/
	public Date lastShowTime;

	public boolean isNew4History;//是否有新历史记录
	public boolean isNew4Enemy;//是否有新仇人
//	@Override
//	public long getIdentifier() {
//		return junZhuId;
//	}
}