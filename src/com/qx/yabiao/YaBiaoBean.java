package com.qx.yabiao;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "YaBiaoBean01")
public class YaBiaoBean {// implements MCSupport
	@Id
	public long junZhuId;
	/*当日已经参加押镖的次数*/
	public int usedYB;
	/*当日剩余参加押镖的次数*/
	public int remainYB; 
	//2016年1月25日需求变更加入福利次数
	public int todayFuliTimes1; //本日领取时段1的福利次数 2016年1月28日策划去掉第一个时段
	public int todayFuliTimes2; //本日领取时段2的福利次数
	public int todayFuliTimes3; //本日领取时段3的福利次数
	@Column(columnDefinition = "INT default 0" )
	public int usedFuliTimes1; //本日已用时段1的福利次数
	@Column(columnDefinition = "INT default 0" )
	public int usedFuliTimes2; //本日已用时段2的福利次数
	@Column(columnDefinition = "INT default 0" )
	public int usedFuliTimes3; //本日已用时段3的福利次数

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
	@Override
	public String toString() {
		return "YaBiaoBean [junZhuId=" + junZhuId + ", usedYB=" + usedYB
				+ ", remainYB=" + remainYB + ", todayFuliTimes1="
				+ todayFuliTimes1 + ", todayFuliTimes2=" + todayFuliTimes2
				+ ", todayFuliTimes3=" + todayFuliTimes3 + ", usedFuliTimes1="
				+ usedFuliTimes1 + ", usedFuliTimes2=" + usedFuliTimes2
				+ ", usedFuliTimes3=" + usedFuliTimes3 + ", usedAskXZ="
				+ usedAskXZ + ", remainAskXZ=" + remainAskXZ + ", horseType="
				+ horseType + ", todayBuyYBTimes=" + todayBuyYBTimes
				+ ", lastShowTime=" + lastShowTime + ", isNew4History="
				+ isNew4History + ", isNew4Enemy=" + isNew4Enemy + "]";
	}
}