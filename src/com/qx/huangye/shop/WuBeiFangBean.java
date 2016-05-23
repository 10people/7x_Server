package com.qx.huangye.shop;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

//type类型(与配置文件必须一样):1：装备铺,2：珍宝行,3：石料店,4：益精堂
@Entity
public class WuBeiFangBean {
	@Id
	public long junzhuId;
	
	@Column(nullable = false, columnDefinition = "INT default 0")
	public int type1UseTimes; 
	
	@Column(nullable = false, columnDefinition = "INT default 0")
	public int type2UseTimes; 
	
	@Column(nullable = false, columnDefinition = "INT default 0")
	public int type3UseTimes; 
	
	@Column(nullable = false, columnDefinition = "INT default 0")
	public int type4UseTimes; 
	
	/** 最后一次购买时间，用于判断次数是否需要重置 **/
	public Date lastBuyTime;
}
