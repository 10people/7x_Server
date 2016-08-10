package com.qx.yabiao;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;
import com.qx.persistent.MCSupport;

@Entity
@Table(name = "YBBattleBean29")
public class YBBattleBean implements DBHash{//押镖战斗相关数据存储
	@Id
	public long jzId;
	/**当然杀死仇人数目*/
	public int count4kill;
	/**当日已经参加劫镖的次数*/
	public int usedJB;
	//2015年12月12日 劫镖次数无限 不需要买
	/**当日剩余有奖的劫镖次数*/
	public int remainJB4Award;
	/**上次劫镖镖的时间--计算劫镖冷却*/
	public Date lastJBDate;
	
	public int fuhuo4uesd;//当日已用满血复活次数
	public int buyfuhuo4Vip;//当日购买复活次数 第n次
	public int fuhuoTimes4Vip;//当日购买的复活次数 总数
	
	public int xueping4uesd;//当日血瓶使用次数
	public int buyblood4Vip;//当日购买血瓶次数 第n次
	public int bloodTimes4Vip;//当日购买的血瓶次数 总数
	
	public int reviveOnDeadPos;
	public Date lastReviveOnDeadPosTime;
	
	public Date lastResetTime;
	@Column(nullable = false, columnDefinition = "int default -1")
	public int zuheId; //防守技能
	@Column(nullable = false, columnDefinition = "int default -1")
	public int gongJiZuHeId; //攻击技能
	/**以下为镖车道具**/
	public int baodi;//0表示没有 大于0表示此道具配置Id
	public int jiasu;//0表示没有 大于0表示此道具配置Id
	public int baohu;//0表示没有 大于0表示此道具配置Id
	@Override
	public long hash() {
		return jzId;
	}
}