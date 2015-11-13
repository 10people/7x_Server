package com.qx.pvp;


public class PVPConstant {
	/** 开服时初始npc的个数 **/
	public static final int TOTAL_NPC_COUNT = 5000;
	/**小卒的级别id, 是BaiZhan.xlsx中的id**/
	public static final int XIAO_ZU_JI_BIE = 1;
	/** 每天百战的总次数 **/
	public static int ZHAN_TOTAL_TIMES = 5;
	/** 百战挑战的间隔时间  单位是秒**/
	public static int INTERVAL_ZHAN_SECOND = 10 * 60;
	/** 百战生产奖励最多累计时间, 单位是秒 **/
	public static int PRODUCE_LIMIT_TIME = 7 * 24 * 3600;
	/** 百战兑换页面更新时间间隔 单位是秒**/
	public static int DUI_HUAN_TIME = 12 * 3600;
	
	// 0 元宝确定购买挑战次数
	// 1 元宝清除下次挑战的冷却时间，
	// 2 确定兑换（用威望兑换）
	// 3 刷新兑换商品列表
	// 4 确定领取生产奖励
	// 5 确定刷新挑战对手列表
	public static final int BUY_CHALLENGE_COUNT = 0;
	public static final int CLEAN_CHALLENGE_CD = 1;
	public static final int GET_PRODUCE_AWARD = 2;
	public static final int REFRESH_RANKS = 3;
	public static final int REFRESH_RANKS_FREE = 4;
	public static final int reget_ranks = 5;
	/**百战冷却清零**/
//	public static final int CLEAN_CD = 12;
	/**购买百战类型**/
//	public static final int BUY_BAI_ZHAN_COUNT = 3;
//	/**百战兑换刷新类型**/
//	public static final int DUI_HUAN_REFRESH = 14;
	
	/** 发送每日奖励邮件: 玩家登陆游戏 **/
	public static final byte LOGIN_SEND_EMAIL = 1;
	/** 发送每日奖励邮件: 每日21点在线玩家 **/
	public static final byte ONLINE_SEND_EMAIL = 0;
	
	/** 战斗记录保存上时间： (7天 ) 单位是秒**/
	public static final int REPLAY_SAVE_TIME = 7 * 24 * 3600;
	
	
	public static final int GONG_JI_WIN = 1;
	public static final int GONG_JI_LOSE = 2;
	public static final int FANG_SHOU_WIN = 3;
	public static final int FANG_SHOU_LOSE = 4;
	
}
