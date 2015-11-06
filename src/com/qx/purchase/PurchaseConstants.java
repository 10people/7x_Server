package com.qx.purchase;

public class PurchaseConstants {
	// 1购买铜币，2购买体力，3购买百战，4购买天工图，5中袋宝箱，6大袋5连抽，7大袋20连抽,10洗练
	// 15 百战刷新一批对手
	/*
	 * 来自策划：Purchase.xml的type
	 * begin
	 */
	public static int TONGBI = 1;
	public static int TILI = 2;
	public static int BAIZHAN = 3;
	public static int TIANGONGTU = 4;
	public static int TREASURE_MIDDLE = 5;
	public static int TREASURE_BIG_5 = 6;
	public static int TREASURE_BIG_20 = 7;
	public static int XILIAN = 10;
	
	/**
	 * 传奇关卡重置
	 */
	public static int CHUANQI_REST = 11;
	public static int BAIZHAN_CD = 12;
	public static int PAWNSHOP_REFRESH = 13;
	public static int BAIZHAN_REFRESH_ENEMYS = 15;
	public static int YOUXIA_TIMES = 17;
	
	public static int LVE_DUO_CD = 21;
	public static int LVE_DUO_BATTLE = 20;
	public static int refresh_HY_shop = 23; // 荒野商店刷新货物的类型
	public static int BUY_HUANGYE_BATTLE = 22; // 购买荒野挑战机会的type
	
	public static int refresh_LianMeng_shop = 25; // 联盟商店刷新货物的类型
	public static int refresh_LianMeng_battle_shop = 24;// 联盟战商店刷新货物的类型
	// end
	
	/** 小袋宝箱每日免费领取次数上限 **/
	public static final int SMALL_TIMES_MAX = 5;
	
	/** 宝箱每日购买次数刷新时间，单位：时 **/
	public static final int TREASURE_REFRESH_TIME = 12;
	
	/** 小宝箱两次领奖时间的等待时间，单位-秒 **/ 
	public static final int SMALL_WAIT_TIME = 10 * 60;
	
	/** 中宝箱两次领奖时间的等待时间，单位-秒 **/
	public static final int MIDDLE_WAIT_TIME = 24 * 60 * 60;
}
