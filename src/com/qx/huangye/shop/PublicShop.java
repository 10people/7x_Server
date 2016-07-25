package com.qx.huangye.shop;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;




@Entity
@Table(name = "public_shop")
public class PublicShop implements DBHash {

	// 荒野商店
	//public static final int huangYe_shop_type = 1;
	// 联盟商店
	//public static final int lianMeng_shop_type=  2;
	// 
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long id;
	
	public long junZhuId;
	
	public int type;
	
	@Column(name = "goodsInfo", nullable = true, columnDefinition = "varchar(10240)")
	public String goodsInfo; // 商品info

	/*
	 * 荒野商店：荒野币
	 * 联盟贡献，读AlliancePalyer.gongxian
	 * 联盟战商店：功勋
	 * 百战商店: 威望
	 */
	public int money = 0; 

	public Date nextAutoRefreshTime; // 下次自动刷新时间

	public int buyNumber; // 今日购买刷新的货物的次数
	public Date lastResetShopTime; // 更新购买刷新货物次数的时间 
	
	public int buyGoodTimes = 0; // 累计购买物品的次数
	@Column(nullable = true)
	public Date openTime;

	@Override
	public long hash() {
		return junZhuId;
	}
}