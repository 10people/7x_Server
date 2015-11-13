//package com.qx.pvp;
//
//import java.util.Date;
//
//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.Table;
//
//import com.qx.persistent.MCSupport;
//
//@Entity
//@Table(name = "pvp_duihuan")
//public class PvpDuiHuanBean implements MCSupport{
//	/**
//	 * @Fields serialVersionUID : 
//	 */
//	private static final long serialVersionUID = 1L;
//	@Id
//	public long junZhuId;
//	
//	/*下次兑换刷新时间  */
////	public Date nextDuiHuanTime;
//	/*兑换的物品id 是Duihuan.xml中的id */
////	public String duiHuanId;
//	/*与兑换的物品id (duiHuanId)相对应, 1 表示可以兑换，0 表示不可以兑换*/
////	public String isBuy;
//	/*威望值，玩家拥有的一种货币，只有百战中用到*/
////	public int weiWang;
//
//	public Date lastGetAward; // 上次计算领取生产奖励的时间
//	public Date lastCalculateAward; // 上次计算生产奖励的时间
//
//	/* 今日已经得到的威望值*/
////	public int todayGetWeiWang;
//	/* 今日已经购买的刷新次数 */
////	public int buyNumber;
//	/*上次购买时间*/
////	public Date lastShowDuiHuanTime;
//
//	public int leiJiWeiWang; //生产奖励累计威望值
//
//	public int getProduceWeiWangTimes = 0; // 累计领取威望奖励的次数
//	public int buyGoodPayWeiWangTimes = 0; // 累计花威望购买物品的次数
//
//	@Override
//	public long getIdentifier() {
//		return junZhuId;
//	}
//}