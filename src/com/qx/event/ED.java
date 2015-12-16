package com.qx.event;

/**
 * Event Id Definition
 * @author 康建虎
 *
 */
public class ED {
	public static final int TAN_BAO_JIANG_LI = 100;
	
	public static final int MIBAO_UP_STAR = 110;
	
	public static final int GAIN_MIBAO = 111;
	public static final int GAIN_ITEM = 112;
	public static final int GAIN_CHENG_HAO = 113;
	public static final int JUAN_XIAN_GONG_JIN = 114;
	public static final int YU_MO_BAI = 115;
	public static final int YA_BIAO_SUCCESS = 116;
	public static final int YOU_XIA_SUCCESS = 117;
	public static final int BAI_ZHAN_RANK_UP = 118;
	public static final int MIBAO_HECHENG_BROADCAST = 119;
	
	public static final int ACC_LOGIN = 10010;
	/** 成就事件 **/
	public static final int ACHIEVEMENT_PROCESS = 10101;
	/** 每日任务事件 **/
	public static final int DAILY_TASK_PROCESS = 10102;


	/** 穿装备事件**/
	public static final int EQUIP_ADD = 10103;
	/** 攻打关卡任务事件 **/
	public static final int PVE_GUANQIA = 10104;
	/**
	 * 升级君主科技任务事件
	 */
	public static final int JUNZHU_KEJI_PROMOTE = 10105;

	
	
	/** 向别的服务器发送消息 **/
	/*
	 *  角色创建成功向注册服务器发送消息
	 */
	public static final int CREATE_JUNZHU_SUCCESS = 10106;
	/**获取xx物品事件完成*/
	public static final int get_item_finish = 10108;
	/**完成一次强化 2015.7.2*/
	public static final int QIANG_HUA_FINISH = 10109;
	/**玩家退出游戏事件*/
	public static final int ACC_LOGOUT = 10110;
	/** 君主登陆事件 **/
	public static final int JUNZHU_LOGIN = 10011;
	
	/**启动服务器事件 */
	public static final int START_SERVER = 10012;
	/**服务器关闭*/
	public static final int END_SERVER = 10013;
	
	/**领取固定精英关卡奖励完成任务事件*/
	public static final int GET_START_PVE_AWARD = 10014;
	
	public static final int Join_LM = 10123;
	public static final int Leave_LM = 10124;
	
	/**战胜传奇关卡*/
	public static final int CHUANQI_GUANQIA_SUCCESS= 10125;
	/**完成N次百战，无论输赢*/
	public static final int FINISH_BAIZHAN_N= 10126;

	// 主线任务：完成一次洗练
	public static final int XILIAN_ONE_GONG =10127;
	// 主线任务：完成一次进阶一次弓
	public static final int JINJIE_ONE_GONG= 10128;
	// 主线任务：完成一次秘宝合成
	public static final int MIBAO_HECHENG = 10129;
	// 主线任务：完成一次秘宝升级
	public static final int MIBAO_SHENGJI = 10130;
	// 主线任务：完成一次秘宝生星级
	public static final int MIBAO_SEHNGXING = 10131;
//	 主线任务:百战胜利N次，
	public static final int SUCCESS_BAIZHAN_N = 10132;
	
	/*
	 * 主线任务事件
	 */
	public static final int get_x_mibao = 10133;
	public static final int mibao_shengji_x = 10134;
	public static final int mibao_shengStar_x = 10135;
	public static final int baizhan_rank_n = 10136;
	public static final int buy_tili_1_times = 10137;
	public static final int buy_tongbi_1_times = 10138;
	
	public static final int junzhu_level_up = 10139;
	/*
	 * 主线任务： 20150630
	 */
	public static final int finish_youxia_x = 10140;
	public static final int finish_yunbiao_x = 10141;
	public static final int finish_jiebiao_x = 10142;
	public static final int tianfu_level_up_x = 10143;

	
	/** 联盟解散事件 */
	public static final int DISMISS_ALLIANCE = 10144;
	/**上缴侠义值事件*/
	public static final int SHANGJIAO_XIAYI = 10145;
	
	/**排行榜刷新事件**/
	// 君主榜刷新事件
	public static final int JUN_RANK_REFRESH = 10147;
	// 联盟榜刷新事件
	public static final int LIANMENG_RANK_REFRESH = 10148;
	// 百战榜刷新事件
	public static final int BAIZHAN_RANK_REFRESH = 10149;
	// 过关榜刷新事件
	public static final int GUOGUAN_RANK_REFRESH = 10150;
	// 解散联盟联盟榜刷新事件
	public static final int REM_LIANMENG_RANK_REFRESH = 10151;
	// 国家日榜刷新事件
	public static final int GUOJIA_DAY_RANK_REFRESH = 10152;
	// 国家周榜刷新事件
	public static final int GUOJIA_WEEK_RANK_REFRESH = 10153;
	// 国家日榜重置事件
	public static final int GUOJIA_DAY_RANK_RESET = 10154;
	// 国家周榜重置事件
	public static final int GUOJIA_WEEK_RANK_RESET = 10155;
	// 联盟声望日榜刷新事件
	public static final int LIANMENG_DAY_RANK_REFRESH = 10156;
	// 联盟声望周榜刷新事件
	public static final int LIANMENG_WEEK_RANK_REFRESH = 10157;
	// 联盟声望日榜重置事件
	public static final int LIANMENG_DAY_RANK_RESET = 10158;
	// 联盟声望周榜重置事件
	public static final int LIANMENG_WEEK_RANK_RESET = 10159;
	// 君主转国国家榜刷新
	public static final int CHANGE_GJ_RANK_REFRESH = 10160;
	// 君主等级榜刷新
	public static final int JUNZHU_LEVEL_RANK_REFRESH = 101601;
	/**GM事件**/
	public static final int CHECK_EMAIL = 10161;
	//限时活动精英集星
	public static final int JINGYINGJIXING = 10162;	
	/**符文事件**/
	// 检查解锁符文
	public static final int CHECK_FUWEN_UOLOCK = 10163;
	public static final int FUSHI_PUSH = 101631;
	// 定时刷新任务事件
	public static final int REFRESH_TIME_WORK = 10164;
	/**获得军衔事件*/
	public static final int GET_JUNXIAN =  10165;
	/**押镖镖车被攻击事件*/
	public static final int BIAOCHE_BEIDA = 10170;
	/**押镖镖车被摧毁*/
	public static final int BIAOCHE_CUIHUI = 10171;
	/**押镖镖车从场景中移除*/
	public static final int BIAOCHE_END=10172;
	/**押镖镖车出发事件*/
	public static final int BIAOCHE_CHUFA = 10173;
	
	public static final int Lve_duo_fail = 10174; // 掠夺失败
	public static final int been_lve_duo = 10175; // 被掠夺
	// 主线任务事件
	public static final int get_produce_weiWang = 50;
	public static final int pay_weiWang = 51;
	public static final int active_mibao_skill = 52;
	public static final int saoDang = 53;
	public static final int wear_fushi = 54;
	public static final int mobai = 55;
	public static final int give_gongjin = 56;
	public static final int fix_house = 57;
	public static final int get_house_exp = 58;
	public static final int have_total_guJuan = 59;
	public static final int battle_huang_ye = 60;
	public static final int lve_duo = 61;
	public static final int pawnshop_buy = 62;
	/**任意N件装备强化到N级*/
	public static final int zhuangBei_x_qiangHua_N = 63;
	/**领取通章奖励**/
	public static final int get_pass_PVE_zhang_award = 64;
	/**进阶任意角色技能1次*/
	public static final int jinJie_jueSe_jiNeng = 65;
	/**指定秘宝升星一次： 指定秘宝id*/
	public static final int mibao_shengStar = 66;
	
}
