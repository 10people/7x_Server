package com.qx.pve;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PveRecord")
public class PveRecord {
	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
	public long dbId;//2015年4月17日16:57:30int改为long
	
	public long uid;
	public int guanQiaId;
	public int star;
	public int achieve;
	@Column(columnDefinition="INT default 0")
	public int achieveRewardState;
	public boolean chuanQiPass = false;
	@Column(columnDefinition="INT default 0")
	public int cqPassTimes;//传奇关卡通关次数。
	@Column(columnDefinition="INT default 0")
	public int cqStar;//传奇星星
	@Column(columnDefinition="INT default 0")
	public int cqResetTimes;//传奇关卡重置【已经】重置了几次
	public Date cqResetDate;//上次重置时间。
	
	@Column(columnDefinition="INT default 0")
	public int starLevel;
	
	public Date saoDangResetTime;//扫荡次数重置时间
	@Column(columnDefinition="INT default 0")
	public int jySaoDangTimes;//今日精英扫了几次
	@Column(columnDefinition="INT default 0")
	public int cqSaoDangTimes;//今日传奇扫了几次
}
