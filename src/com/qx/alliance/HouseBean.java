package com.qx.alliance;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Index;
/*
 *离开联盟的房主的原房屋数据放在房主身上，当该玩家加入新的联盟或建立新的联盟后，在分配给其的随机土地上直接生成原等级的房屋。
 */
@Entity
@Table(indexes={@Index(name="lmId",columnList="lmId")})
public class HouseBean {
	public static final int ForSell = 10;
	public static final int ForUse = 20;
	public static final int Drop = 404;
	public static final int KingSell = 505;
	
	@Id
	public long jzId;//jun zhu id
	public int location;
	public int lmId;//联盟id
	public int exp;
	public int cunchuExp;//离开联盟时君主在房屋中攒的经验
	@Column(columnDefinition = "INT default 1")
	public int level;
	public int state;//10 待售；20 自住；获得房屋是默认为自住。 404 荒废（七天不上线）；505盟主强制为待售；
	public boolean open;
	public String firstOwner;
	public Date firstHoldTime;//第一次入住时间
	public Date preGainExpTime;//上次领取经验时间。
	//
	@Column(columnDefinition = "INT default 0")
	public int todayUpTimes;//今日已装修次数
	public Date preUpTime;//上次装修时间
	@Column(columnDefinition = "INT default 0")
	public int houseExp;
}
