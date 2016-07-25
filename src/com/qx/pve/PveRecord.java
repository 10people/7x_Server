package com.qx.pve;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "PveRecord",indexes={@Index(name="uid",columnList="uid")})
public class PveRecord {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long dbId;//2015年4月17日16:57:30int改为long
	
	public long uid;
	public int guanQiaId;
	public int star;
	public int achieve;
	@Column(columnDefinition="INT default 0")
	public int achieveRewardState;
	public boolean chuanQiPass = false;
	@Column(columnDefinition="INT default 0")
	public int cqPassTimes;				//传奇关卡通关次数。
	@Column(columnDefinition="INT default 0")
	public int cqWinLevel;				//传奇胜利等级-小胜、完胜..
	@Column(columnDefinition="INT default 0")
	public int cqStar;					//传奇星级评价
	@Column(columnDefinition="INT default 0")
	public int cqStarRewardState;		//传奇星级奖励领取状态
	@Column(columnDefinition="INT default 0")
	public int cqResetTimes;			//传奇关卡重置【已经】重置了几次
	public Date cqResetDate;			//上次重置时间。
	
	@Column(columnDefinition="INT default 0")
	public int starLevel;
	
	public Date saoDangResetTime;//扫荡次数重置时间
	@Column(columnDefinition="INT default 0")
	public int jySaoDangTimes;//今日精英扫了几次
	@Column(columnDefinition="INT default 0")
	public int cqSaoDangTimes;//今日传奇扫了几次
	
	/**该字段只针对章节的最后一关： 是否已经领取通章奖励*/
	public boolean isGetAward = false;
}
