package com.qx.alliancefight;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(indexes={@Index(columnList="jzId")})
public class LMZAwardBean {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public int dbId;
	public long jzId;
	public int cityId;
	public int warType;//	//0-镇守 1-进攻 2-未发生战斗
	
	public int result = 4;	//rewardType = 0:0-失败 1-成功   rewardType = 1:杀敌数
	public int rewardNum = 5;	//奖励
	//public int time = 6;	//0-今天 1-昨天 2-前天
	public int getState = 7;	//0-未领取 1-已领取
	
	public Date dt;
	
	public int fromType;//1联盟；2个人
}
