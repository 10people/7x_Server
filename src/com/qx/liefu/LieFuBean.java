package com.qx.liefu;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class LieFuBean {
	@Id
	public long junzhuId;

	public int type1UseTimes; 			// 代表今日猎符总次数
	@Column(nullable = false, columnDefinition = "INT default 0")
	public int type1TotalTimes; 		//  历史总次数
	
	/** 第2种抽取状态， 1-可以点击，0-不可以点击 **/
	@Column(nullable = false, columnDefinition = "INT default 0")
	public int type2State; 	
	/** 第2种抽取状态， 今日已抽取次数 **/
	@Column(nullable = false, columnDefinition = "INT default 0")
	public int type2UseTimes; 	
	@Column(nullable = false, columnDefinition = "INT default 0")
	public int type2TotalTimes; 	
	
	/** 第3种抽取状态， 1-可以点击，0-不可以点击 **/
	@Column(nullable = false, columnDefinition = "INT default 0")
	public int type3State;	
	/** 第3种抽取状态， 今日已抽取次数 **/
	@Column(nullable = false, columnDefinition = "INT default 0")
	public int type3UseTimes; 	
	
	/** 第4种抽取状态， 1-可以点击，0-不可以点击 **/
	@Column(nullable = false, columnDefinition = "INT default 0")
	public int type4State;	
	/** 第4种抽取状态， 今日已抽取次数 **/
	@Column(nullable = false, columnDefinition = "INT default 0")
	public int type4UseTimes; 	
	
	public Date lastActionTime;
	
	@Column(nullable = false, columnDefinition = "INT default 0")
	public int totalTimes;

}
