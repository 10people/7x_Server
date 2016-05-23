package com.qx.vip;

public class VipData {

	/**
	 * vip等级对功能开启的限制
	 * (策划配置：VipFuncOpen.xml的key值)
	 */
	static public int yuanBao_xiLian = 1;    // key="1" desc="元宝洗练功能"
	static public int refresh_dangPu = 2;    // key="2" desc="刷新当铺功能"
	static public int world_chat = 3;        // key="3" desc="世界聊天"
	static public int change_name = 4;       // key="4" desc="更换名字"
	static public int suoDing_xiLian_shuXing = 5; // key="5" desc="锁定洗练属性" 
	static public int qiangHua = 6;          //  key="6" desc="一键强化"
	static public int qianCheng_moBai = 7;   //key="7" desc="虔诚膜拜" 
	static public int jinJie_ZhuangBei = 8;  //key="8" desc="进阶装备免费无损" 
	static public int clean_baiZhan_time = 9; //key="9" desc="元宝消除百战等待时间" 
	static public int reset_pve_times = 10;   //key="10" desc="传奇关卡挑战次数重置"
	static public int dingLi_moBai = 11;      // key="11" desc="顶礼膜拜"
	static public int buy_huFu = 12;          // key="12" desc="购买虎符" 
	static public int lianXu_saoDant = 13;    //key="13" desc="连续扫荡" 
	static public int xiaoWu_show = 14;        //key="14" desc="小屋内装备秘宝展示"
	static public int baizhan_weiwang_add = 15; //key="15" desc="百战威望产出增加"
	static public int increase_fangwu_exp = 16; //key="16" desc="房屋经验产出增加" 
	static public int mibao_level_point = 17; //key="17" desc="购买秘宝升级点数" 
	static public int youxia_times = 18; //key="18" desc="游侠玩法次数" 
	static public int clear_lveDuo_CD = 21;
	static public int can_buy_huagnye_times = 22; //key="22" 购买荒野挑战次数需要的最低vip等级
	static public int buy_revive_all_life = 23; // 购买满血复活所需VIP等级

	
	/*
	 * vip等级不同，次数也不同的操作
	 * (程序员自定义)
	 */
	static public int bugMoneyTime = 1;
	static public int bugTiliTime = 2;
	static public int bugBaizhanTime = 3;
	static public int yujueDuihuan = 4;
	static public int saodangFree = 5;
	static public int xilianLimit = 6 ;
	static public int legendPveRefresh =7;
	static public int YBxilianLimit = 8;
	static public int dangpuRefreshLimit =9;
	static public int baizhanPara = 10;
	static public int fangWubuildNum = 11;
	static public int mibaoCountLimit = 12;
	static public int youXiaTimesGet = 13;
	static public int yabiaoTimes = 14;
	static public int jiebiaoTimes = 15;
	static public int askHelpTimes = 16;
	static public int buyLveDuoTimes = 17;
	static public int buy_huangye_times = 18;
	static public int buy_ybmanxue_times = 19;//购买押镖场景满血复活次数
	static public final int buy_ybblood_times = 20;//购买押镖场景血瓶次数
	static public final int buy_revive_times = 21;//购买押镖场景原地复活次数
	static public final int buy_jianShezhi_times = 22;
	static public final int buy_Hufu_times =23;
}
