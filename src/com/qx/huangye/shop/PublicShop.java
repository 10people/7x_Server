package com.qx.huangye.shop;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;




@Entity
@Table(name = "public_shop")
public class PublicShop{

	// id = junzhuId * 10 + type 
	// 荒野商店
	//public static final int huangYe_shop_type = 1;
	// 联盟商店
	//public static final int lianMeng_shop_type=  2;
	// 
	@Id
	public long id;
	@Column(name = "goodsInfo", nullable = true, columnDefinition = "varchar(10240)")
	public String goodsInfo; // 商品info

	/*
	 * 荒野商店：荒野币
	 * 联盟贡献，读AlliancePalyer.gongxian
	 * 联盟战商店：功勋
	 * 百战商店: 威望
	 */
	private int money = 0; 

	public Date nextAutoRefreshTime; // 下次自动刷新时间

	public int buyNumber; // 今日购买刷新的货物的次数
	public Date lastResetShopTime; // 更新购买刷新货物次数的时间 
	
	public int buyGoodTimes = 0; // 累计购买物品的次数
	@Column(nullable = true)
	public Date openTime;

	protected int getMoney(){
		return money;
	}
	protected void setMoney(int money){
		this.money = money;
	}
}