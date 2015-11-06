package com.qx.explore;

import com.manu.dynasty.template.CanShu;

public class ExploreConstant {
//	/**几种抽牌类型**/
//	public static final byte DRAW_TYPE_NUMBER = 3;
	/** 免费单抽 **/
	public static final byte FREE = 0;
	/** 免费单抽 + 付费单抽 **/
	public static final byte SIGLE = 1;
	/** 付费10连抽 **/
	public static final byte PAY = 10;
	/** 联盟单抽 **/
	public static final byte GUILD_1 = 11;
	/** 付费联盟10连抽 **/
	public static final byte GUILD_2 = 12;

	/** 免费抽取 每天有的领取次数**/
	public static final byte FREE_DRAW_NUMBER = 5;
	/** 付费单抽 每天拥有的免费抽取次数**/
//	public static final byte SIGLE_DRAW_NUMBER = 1;
	/**	付费十连抽的打折数 **/
	public static final byte PAY_DISCOUNT = 9;
	/**	付费联盟10连抽打折数 **/
	public static final byte GUILD_2_DISCOUNT = 9;
	
	// 对于无法探宝的几种情况
	/** 没有免费的 探宝次数**/
	public static final byte HAVE_NOT_FREE_NUMBER = 2;
	/**元宝不够**/
	public static final byte HAVE_NOT_ENOUGH_MONEY = 1;
	/**联盟探宝贡献值不够**/
	public static final byte HAVE_NOT_ENOUGH_GONGXIAN = 5;
	/**冷却时间未到**/
	public static final byte TIME_IS_NOT_COMING = 3;
	/** 数据出错 **/
	public static final byte DATA_PROBLEM = 4;
	/** 逆鳞精铁id， ItemTemp.xlsx中的id字段 **/
	public static final int IRON_ID = 920001; 
	/** 上古青铜id，ItemTemp.xlsx中的id字段 **/
	public static final int COPPER_ID = 920002;
	/**逆鳞精铁和上古青铜的award类型**/
	public static final int QIANG_HUA_TYPE = 9; 

	//几种探宝奖励id，都表示 AwardTemp.xlsx 中的 awardId 字段
	/**矿洞 探宝的 奖励 **/
	public static final int FREE_AWARDID = 9001;
	/**矿井探宝的普通奖励**/
	public static final int SIGLE_AWARDID_1 = 9002;
	/**矿井探宝的较好奖励 **/
	public static final int SIGLE_AWARDID_2 = 9003;
	/**矿井首抽奖励**/
	public static final int SIGLE_AWARDID_0 = 9004;
	/**矿洞首抽奖励**/
	public static final int FREE_AWARDID_0 = 9000;
	

	// 探宝的价钱  , 表示purchase.xlsx的id
	/** 购买十连抽的元宝数**/
	public static final int PAY_BUY_ID = 40002;
	/** 购买单抽的元宝数**/
	public static final int SIGLE_BUY_ID = 40001;
	/** 购买联盟单抽的贡献值**/
	public static final int GUILD_1_BUY_ID = 40011;
	/** 购买联盟10抽的贡献值**/
	public static final int GUILD_2_BUY_ID = 40012;


	/** 矿洞领取的时间间隔 单位是秒**/
	public static  int FREE_TIME_INTERVAL = CanShu.DIJI_TANBAO_REFRESHTIME;
	/** 矿井领取时间间隔 单位是秒**/
	public static  int SIGLE_TIME_INTERVAL = CanShu.GAOJI_TANBAO_REFRESHTIME;
}
