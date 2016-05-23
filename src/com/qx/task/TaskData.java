package com.qx.task;
/**
 * 
 * This class is used for
 * @author wangZhuan
 * @version   
 *       9.0, 2014年10月11日 下午2:42:17
 */
public class TaskData {

	// begin
	/*
	 * 穿装备任务类型 ，对应Zhuxian.xml的doneType
	 */
	public static final short EQUIP_ADD = 5;
	/*
	 * 攻打关卡任务类型， 对应Zhuxian.xml的doneType
	 */
	public static final short PVE_GUANQIA = 2;
	/*
	 * 君主卡将升级任务类型，对应Zhuxian.xml的doneType
	 */
	public static final short JUNZHU_KEJI_PROMOTE = 6;
	/*
	 * 得到一个item，对应Zhuxian.xml的doneType
	 */
	public static final short get_item = 8;
	/*
	 * 强化完成任务类型，对应Zhuxian.xml的doneType
	 */
	public static final short ONE_QIANG_HAU = 9;
	/*
	 * 领取精英关卡奖励完成类型，对应Zhuxian.xml的doneType
	 * */
	public static final short GET_PVE_AWARD = 10;
	// 对话任务，对应Zhuxian.xml的doneType
	public static final short DIALOGUE = 4;
	/*
	 * 领取精英关卡奖励任务完成，对应Zhuxian.xml的doneType
	 */
	public static final short GET_JINGYING_AWARD =10;
	
	/*
	 *百战打了N场，对应Zhuxian.xml的doneType
	 */
	public static final short FINISH_BAIZHAN_N = 11;
	/*
	 *百战胜利N场，对应Zhuxian.xml的doneType
	 */
	public static final short SUCCESS_BAIZHAN_N = 12;
	
	/*
	 * 在过关斩将里打一场传奇关卡，对应Zhuxian.xml的doneType
	 */
	public static final short FINISH_CHUANQI = 13;
	// 洗练一次，对应Zhuxian.xml的doneType
	public static final short XILIAN_ONE_GONG =14;
	// 进阶一次弓，对应Zhuxian.xml的doneType
	public static final short JINJIE_ONE_GONG= 15;
	// 秘宝合成，对应Zhuxian.xml的doneType
	public static final short MIBAO_HECHENG = 16;
	// 秘宝升级，对应Zhuxian.xml的doneType
	public static final short MIBAO_SHENGJI = 17;
	// 秘宝生星级，对应Zhuxian.xml的doneType
	public static final short MIBAO_SEHNGXING = 18;
	
	/** 对应Zhuxian.xml的doneType**/
	public static final short get_x_mibao = 19;
	/** 对应Zhuxian.xml的doneType**/
	public static final short mibao_shengji_x = 20;
	/** 对应Zhuxian.xml的doneType**/
	public static final short mibao_shengStar_x = 21;
	/** 对应Zhuxian.xml的doneType**/
	public static final short baizhan_rank_n = 22;
	/** 对应Zhuxian.xml的doneType**/
	public static final short join_lianmeng = 23;
	/** 对应Zhuxian.xml的doneType**/
	public static final short buy_tili_1_times = 24;
	/** 对应Zhuxian.xml的doneType**/
	public static final short buy_tongbi_1_times = 25;
	/** 对应Zhuxian.xml的doneType**/
	public static final short junzhu_level_up = 1;
	
	/*
	 * 主线任务，对应Zhuxian.xml的doneType. add 20150630
	 */
	public static final short go_youxia = 30;
	public static final short finish_youxia_x = 31;
	public static final short finish_yunbiao_x = 35;
	public static final short finish_jiebiao_x = 36;
	public static final short tianfu_level_up = 37;
	public static final short tianfu_level_x = 38;
	public static final short qianghua_level_x = 39;
	// n个装备的品质大于等于固定品质，任务类型
	public static final short N_quality_ok = 40;
	// 指定部位装备的品质大于等于固定品质，任务类型
	public static final short one_quality_ok = 41;
	/*
	 *  主线任务，对应Zhuxian.xml的doneType. add 20190916
	 */
	public static final short get_produce_weiWang = 50;
	public static final short pay_weiWang = 51;
	public static final short active_mibao_skill = 52;
	public static final short saoDang = 53;
	public static final short wear_fushi = 54;
	public static final short mobai = 55;
	public static final short jibai = 71;
	public static final short fix_house = 57;
	public static final short get_house_exp = 58;
	public static final short have_total_guJuan = 59;
	public static final short battle_huang_ye = 60;
	public static final short lve_duo = 61;
	public static final short pawnshop_buy = 62;
	public static final short mibao_shengStar = 66;
	public static final short zhuangBei_x_qiangHua_N = 63;
	public static final short jinJie_jueSe_jiNeng = 65;
	public static final short get_pass_PVE_zhang_award = 64; // 领取通章奖励
	/**X个角色技能达到N级**/
	public static final int jueSe_x_jiNeng_n = 67;
	/**拥有X个N星秘宝**/
	public static final int miabao_x_star_n = 68;
	public static final short qiandao = 69; // 签到一次
	public static final short qiandao_get_v = 70;// 签到领取V特权奖励
	public static final short get_achieve = 72; // 领取成就奖励（即就是限时活动奖励）
	public static final short tanbao_oneTimes = 74;//元宝单抽x次
	public static final short tanbao_tenTimes = 75;//元宝10抽x次
	public static final short tongbi_oneTimes = 76;//铜币单抽x次
	public static final short tongbi_tenTimes = 77;//铜币10抽x次
	public static final short active_taozhuang = 78;//激活品质套装
	public static final short battle_shiLian_II = 79; //攻打试练II难度1次（拿符石）
	public static final short buy_lianMeng_shop = 80;//联盟商铺购物
	public static final short get_miBao_x_pinZhi_y = 100;// 获取X个品质Y的秘宝
	public static final short get_mbSuiPian_x_y = 103; //获取X个秘宝碎片Y
	public static final short get_miShu_pinZhi_y = 105;//获得一个品质Y的秘术
	public static final short done_qianChongLou = 110;//打一次千重楼
	public static final short use_baoShi_x = 115;//镶嵌X颗宝石
	public static final short use_baoShi_one_pinZhi_y = 116;//镶嵌1颗Y级得宝石
	public static final short done_wuBeiChouJiang = 120;//武备坊抽奖1次
	public static final short done_lieFu_x = 125;//猎符X次
	public static final short done_junChengZhan_x = 130;//联盟战X次
	// end
	
	
	/**对应XianshiControl.xml的doneTyp*/
	//首日在线时间
	public static final int zaixianlibao =42;
	/**对应XianshiControl.xml的doneTyp*/
	//登录天数
	public static final int qiriqiandao =43;
	/**对应XianshiControl.xml的doneTyp*/
	//过关斩将星数
	public static final int jingyingjixing =44;
	/**对应XianshiControl.xml的doneTyp*/
	//探宝次数
	public static final int tanbaocishu =45;
	//探宝1抽
	public static final int tanbao_oneTime = 74;
	//探宝10连抽
	public static final int tanbao_tenTime = 75;
	
//	/** 对应Zhuxian.xml的doneCond**/
//	public static int xiLian_itemId = 102001;
//	public static int jinJie_itemId = 102001;
//	/** 对应Zhuxian.xml的doneCond**/
//	public static int shengJi_mibao = 301011;
//	/** 对应Zhuxian.xml的doneCond**/
//	public static int heCheng_mibao = 301012;
//	/** 对应Zhuxian.xml的doneCond**/
//	public static int shengXing_mibao = 301022;
	public static int tanbao_itemId_1 = 301011;
//	public static int tanbao_itemId_2 = 301011;
//	public static int dangpPu_itemId = 921001;
//	public static int qianghua_itemId_1 = 101003;
//	public static int qianghua_itemId_2 = 101101;
//	/**
//	 * 对应Zhuxian.xml的doneCond
//	 * 秘宝升星级到达x
//	 */
//	public static int mibao_star_x_1 = 2;
//	public static int mibao_star_x_2 = 3;
////	/** 
////	 * 对应Zhuxian.xml的doneCond
////	 * 百战名次达到n
////	 */
//	public static int bai_n_1 = 3000;
//	public static int bai_n_2 = 1500;
//	public static int bai_n_3 = 750;
////	
////	/** 
////	 * 对应Zhuxian.xml的doneCond
////	 * 秘宝等级达到x
////	 */
//	public static int mibao_level_x_1 = 10;
//	public static int mibao_level_x_2 = 20;
	
}
