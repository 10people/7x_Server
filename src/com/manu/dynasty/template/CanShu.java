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
	
	public static int MAXTIME_PVE;
	public static int MAXTIME_BAIZHAN;
	public static int MAXTIME_HUANGYE_PVE;
	public static int MAXTIME_HUANGYE_PVP;
	
	
	public static int CHAT_INTERVAL_TIME;
	
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
	
	/*
	 * 免费探宝CD时间
	 */
	public static int TONGBI_TANBAO_REFRESHTIME;
	public static int YUANBAO_TANBAO_REFRESHTIME;
	public static int TONGBI_TANBAO_FREETIMES;
	
	public static int XILIANADD_MIN;
	public static int XILIANADD_MAX;
	
	public static int VIPLV_ININT;
	public static int IS_YUEKA_INIT;
	// 充值月卡持续天数
	public static int YUEKA_TIME;
	
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
	
	public static int  REFRESHTIME_PURCHASE;
	public static int WEIWANG_INIT;
	public static double WUQI_BAOJILV;
	public static double JINENG_BAOJILV;
	
	
	/*押镖相关参数--结束*/
	
	public String key;
	public String value;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
