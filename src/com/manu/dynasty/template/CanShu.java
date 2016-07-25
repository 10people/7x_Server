package com.manu.dynasty.template;

public class CanShu {
	public static double JUNZHU_PUGONG_QUANZHONG;
	public static double JUNZHU_PUGONG_BEISHU;;
	public static double JUNZHU_JINENG_QUANZHONG;
	public static double JUNZHU_JINENG_BEISHU;
	
	public static double WUJIANG_PUGONG_QUANZHONG;
	public static double WUJIANG_PUGONG_BEISHU;
	public static double WUJIANG_JINENG_QUANZHONG;
	public static double WUJIANG_JINENG_BEISHU;
	
	public static double SHIBING_PUGONG_QUANZHONG;
	public static double SHIBING_PUGONG_BEISHU;
	public static double SHIBING_JINENG_QUANZHONG;
	public static double SHIBING_JINENG_BEISHU; 
	
	public static double SHANGHAI_GONGSHI_XISHU1;
	public static double SHANGHAI_GONGSHI_XISHU2;
	public static double SHANGHAI_GONGSHI_XISHU3;
	
	public static double ZHANLI_M;
	public static double ZHANLI_C;
	public static double ZHANLI_R;
	public static double ZHANLI_L;
	public static double ZHANLI_K1;
	public static double ZHANLI_K2;
	public static double ZHANLI_M1;
	public static double ZHANLI_M2;
	
	public static double CHUSHIHUA_CHUANDAIZHUANGBEI_1;
	public static double CHUSHIHUA_CHUANDAIZHUANGBEI_2;
	public static double CHUSHIHUA_CHUANDAIZHUANGBEI_3;
	
	/** 增加体力间隔时间 **/
	public static int ADD_TILI_INTERVAL_TIME;	
	/** 每次增加体力值 **/
	public static int ADD_TILI_INTERVAL_VALUE;
	/** 自动添加装备免费洗练次数的值 **/
	public static int ADD_XILIAN_VALUE;
	/** 单次免费洗练值 **/
	public static int XILIANZHI_Free;
	/** 单次元宝洗练值**/
	public static int XILIANZHI_YUANBAO;
	/** 自动增加装备免费洗练次数的间隔时间 **/
	public static int ADD_XILIAN_INTERVAL_TIME;
	/** 每日洗练石洗练次数上限 **/
	public static int XILIANSHI_MAXTIMES;
	/** 洗练值上限 **/
	public static int XILIANZHI_MAX;
	
	/** 秘宝点数增加的间隔时间(单位秒) */
	public static int ADD_MIBAODIANSHU_INTERVAL_TIME;
	
	/** 装备免费洗练次数的上限 **/
	public static int FREE_XILIAN_TIMES_MAX;
	
	public static String REFRESHTIME_DANGPU;
	public static int LIANMENG_CREATE_COST;
	
	public static int BAIZHAN_LVEDUO_JIANSHEZHI;
	public static double BAIZHAN_LVEDUO_K;
	public static int BAIZHAN_NPC_WEIWANG;
	public static int BAIZHAN_WEIWANG_ADDLIMIT;
	public static int BAIZHAN_FIRSTWIN_YUANBAO;
	public static int BAIZHAN_FREE_TIMES;
	
	public static int MAXTIME_PVE;
	public static int MAXTIME_BAIZHAN;
	public static int MAXTIME_HUANGYE_PVE;
	public static int MAXTIME_HUANGYE_PVP;
	public static int DAYTIMES_LEGENDPVE;
	
	
	public static int CHAT_WORLD_INTERVAL_TIME;			//世界聊天最短间隔时间（秒）" 
	public static int CHAT_ALLIANCE_INTERVAL_TIME;		//联盟最短间隔时间（秒）" 
	public static int CHAT_BROADCAST_INTERVAL_TIME;		//广播频道最短间隔时间（秒）"
	public static int CHAT_SECRET_INTERVAL_TIME;		//私聊频道最短时间间隔（秒）" 
	public static int CHAT_MAX_WORDS;					//聊天输入最大字数（个） 
	
	public static double HUANGYEPVE_AWARD_X;
	public static double HUANGYEPVP_PRODUCE_P;
	public static double HUANGYEPVP_KILL_K;
	public static String HUANGYEPVP_AWARDTIME;
	public static int HUANGYEPVE_FASTCLEAR_TIME;
	public static int FANGWUJINGPAI_1;//房屋贡献值每日衰减率
	public static int FANGWUJINGPAI_2;//竞拍贡献与价值转化百分比
	public static int FANGWUJINGPAI_3;//竞拍房屋贡献值增率
	public static int FANGWUJINGPAI_4;//贡献值返还衰减
	public static int REFRESHTIME_GAOJIFANGWU;//高级房屋衰减价值时间
	/*
	 * 增加天赋点数所需要的等级
	 */
	public static int TIANFULV_DIANSHUADD1;
	public static int TIANFULV_DIANSHUADD2;
	public static int TIANFULV_DIANSHUADD3;
	public static int TIANFULV_DIANSHUADD4;
	public static int TIANFULV_DIANSHUADD5;
	
	public static int REFRESHTIME ;// 刷新次数的时间点
	// 掠夺 begin
	public static String OPENTIME_LUEDUO; //掠夺每日开放时间
	public static String CLOSETIME_LUEDUO; //掠夺每日结束时间
	public static int LUEDUO_MAXNUM;
	public static int LUEDUO_CD;
	public static int LUEDUO_PROTECTTIME;
	public static int LUEDUO_RECOVER_INTERVAL_TIME;
	public static double LUEDUO_RECOVER_PERCENT;
	public static double LUEDUO_RESOURCE_PERCENT;
	public static int LUEDUO_JIANSHE_REDUCE;
	public static int LUEDUO_RESOURCE_MINNUM;
	public static int LUEDUO_HAND_DAYMINNUM;
	public static int LUEDUO_HAND_WEEKMINNUM;
	public static String LUEDUO_DAYAWARD_GIVETIME;
	public static String LUEDUO_WEEKWARD_GIVETIME;
	public static String LUEDUO_PERSONRANKAWARD_GIVETIME;
	public static String LUEDUO_LIANMENGRANKAWARD_GIVETIME;
	
	public static float LUEDUO_CANSHU_L;
	public static float LUEDUO_CANSHU_N;
	public static float LUEDUO_CANSHU_M;
	public static float LUEDUO_CANSHU_X;
	public static float LUEDUO_CANSHU_Y;
	public static float LUEDUO_CANSHU_Z;
	public static float LUEDUO_CANSHU_A;
	public static float LUEDUO_CANSHU_B;
	public static float LUEDUO_CANSHU_C;
	public static int LUEDUO_GONGJIN_INIT;
	
	public static float LUEDUO_COMFORTED_AWARD_K;
	public static float LUEDUO_COMFORTED_AWARD_B;
	
	/*
	 * 免费探宝CD时间
	 */
	public static int TONGBI_TANBAO_REFRESHTIME;
	public static int YUANBAO_TANBAO_REFRESHTIME;
	public static int TONGBI_TANBAO_FREETIMES;
	public static int TONGBI_TANBAO_BAODI;
	public static int YUANBAO_TANBAO_BAODI;
	
	
	public static int XILIANADD_MIN;
	public static int XILIANADD_MAX;
	
	public static int VIPLV_ININT;
	public static int IS_YUEKA_INIT;
	// 充值月卡持续天数
	public static int YUEKA_TIME;
	public static int ZHOUKA_TIME;
	
	/* 排行榜相关参数 */
	// 排行榜搜索范围
	public static int RANK_MAXNUM;
	// 排行榜君主最低等级
	public static int RANK_MINLEVEL;
	
	/*押镖相关参数--开始*/
	// 押镖开启时间
	public static String OPENTIME_YUNBIAO;
	// 押镖关闭时间
	public static String CLOSETIME_YUNBIAO;
	//每日计数回满重置的时间
	public static String REFRESHTIME_YUNBIAO;
	//运镖次数
	public static int YUNBIAO_MAXNUM;
	//劫镖次数
	public static int JIEBIAO_MAXNUM;
	//劫镖冷却时间
	public static int JIEBIAO_CD;
	//劫镖结果最大反馈时间
	public static int JIEBIAO_RESULTBACK_MAXTIME;
	//劫镖最长战斗时间
	public static int MAXTIME_JIEBIAO;
	public static int YUNBIAOASSISTANCE_INVITEDMAXNUM;
	public static int YUNBIAOASSISTANCE_MAXNUM;
	public static int YUNBIAOASSISTANCE_GAIN_SUCCEED;
	public static int YUNBIAOASSISTANCE_GAIN_FAIL;
	public static double YUNBIAOASSISTANCE_HPBONUS;
	public static int MAXTIME_LUEDUO;
	public static int LIANMENG_JIBAI_PRICE;
	
	public static int  REFRESHTIME_PURCHASE;
	public static int WEIWANG_INIT;
	public static double WUQI_BAOJILV;
	public static double JINENG_BAOJILV;
	public static int JION_ALLIANCE_LV_MINI;
	
	public static int WORLDCHAT_FREETIMES;
	public static int WORLDCHAT_PRICE;
	public static int BROADCAST_PRICE;
	/***房屋初始经验"*/
	public static int FANGWU_INITIAL_EXP;

	/* 驱逐*/
	public static int EXPEL_TIMELIMIT;
	public static int EXPEL_DAYTIMES;
	public static int EXPEL_CD;
	/*押镖相关参数--结束*/
	
	/* 
		<CanShu key="LUEDUO_AWARDEDCOMFORT_MAXTIMES" value="5" desc="安慰被掠夺盟友有奖励的次数" />
		<CanShu key="YUNBIAO_AWARDEDCOMFORT_MAXTIMES" value="5" desc="安慰被劫镖盟友有奖励的次数" />
	安慰盟友*/
	public static int LUEDUO_AWARDEDCOMFORT_MAXTIMES;
	public static int YUNBIAO_AWARDEDCOMFORT_MAXTIMES;
	public static int TILI_JILEI_SHANGXIAN;//体力积累上限" 2016年1月19日
	
	public static double CONGRATULATE_AWARD_K;//" desc="恭贺奖励系数K" />
	public static double CONGRATULATE_AWARD_B;//" desc="恭贺奖励系数B" />
	public static int BIAOQING_INTERVAL;//"" value="3" desc="表情持续时间（秒）" />
	public static int GREETED_CD;//"" value="60" desc="被打招呼的CD（秒）" />
	public static int LIANMENG_LVLUP_REDUCE;
	public static int EQUIPMENT_FREETIMES;
	public static int BAOSHI_FREETIMES;
	public static int QIANGHUA_FREETIMES;
	public static int JINGQI_FREETIMES;
	
	public static String LIEFU_FREETIMES_REFRESH;
	public static int CHANGE_COUNTRY_COST; 
	public static int YUEKA_YUANBAO; //月卡每日领取元宝数
	public static int ZHONGSHENKA_YUANBAO; //终身卡每日领取元宝数
	public static int ZHOUKA_YUANBAO; //周卡每日领取元宝数
	public static int MEIRI_DINGSHIZENGSONG_TILI; //每天定时送体力数
	public static int CHENGZHANGJIJIN_VIP; //购买成长基金VIP
	public static int CHENGZHANGJIJIN_COST; //购买成长基金花费元宝数
	public static int CHENGZHANGJIJIN_REBATE; //购买成长基金返还元宝数
	public static double LIANMXIAOWU_EXP; 
	public static String LIEFU_BLUE_FIRSTAWARD; 
	public static String LIEFU_GREEN_FIRSTAWARD; 
	public static String ZHUANGBEI_FIRSTAWARD; 
	public static int CHANGE_COUNTRY_CD; 
	public static int CHANGE_NAME_CD; 
	public static int SHILIAN_CLEARCD_COST; 
	public static int GUOGUAN_RANK_MINLEVEL; 
	public static int BAIZHAN_RANK_MINLEVEL; 
	public static int CHONGLOU_RANK_MINLEVEL; 
	public String key;
	public String value;
}
