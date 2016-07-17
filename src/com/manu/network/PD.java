package com.manu.network;

/**
 * Protocol Definition（协议号定义）
 * @author 康建虎
 */
public class PD {
	/**
	 * 注册已知的协议类型。
	 */
	public static void init(){
		ParsePD.makeMap();
		PDOldMap.map();
	}
	
	public static final short DEBUG_PROTO_WITHOUT_CONTENT		= 100;
	public static final short DEBUG_PROTO_WITHOUT_CONTENT_RET	= 101;
	
	public static final short C_TEST_DELAY = 110;
	public static final short S_TEST_DELAY = 111;
	
	public static final short C_DROP_CONN = 120;//客户端主动断开连接
	
	public static final short C_InitProc = 301;
	public static final short S_InitProc = 302;
	public static final short C_zlgdlc = 303;//客户端终止加密过程
	public static final short S_zlgdlc = 305;//服务器告知客户端过程有误。
	
	////战斗过程加密协议号
	public static final short C_klwhy_1 = 502;
	public static final short C_klwhy_2 = 503;
	public static final short C_klwhy_3 = 504;
	public static final short C_klwhy_4 = 505;
	public static final short C_klwhy_5 = 506;
	public static final short C_klwhy_6 = 507;
	public static final short C_klwhy_7 = 508;
	public static final short C_klwhy_8 = 509;
	public static final short C_klwhy_9 = 512;
	public static final short C_klwhy_10 = 522;
	public static final short[] C_RND_PROT = {C_klwhy_1,C_klwhy_2,C_klwhy_3,C_klwhy_4,C_klwhy_5,C_klwhy_6,
		C_klwhy_7,C_klwhy_8,C_klwhy_9,C_klwhy_10};
	//战斗过程加密协议号
	
	public static final short CHARGE_OK = 808;
	public static final short C_OPEN_CREATE_ROLE = 888;
	
	public static final short GET_QQ_INFO = 900;
	public static final short GET_LV_INFO = 910;
	public static final short GET_REQ_INFO = 920;
	public static final short GET_LV_REWARD = 930;
	
	public static final short TEST_CONN = 10001;
	public static final short S_Broadcast = 10003;
	
	public static final short S_USE_ITEM = 11001;//使用物品获得哪些物品。
	
	/**
	 * 错误返回信息
	 */
	public static final short S_ERROR = 10010;
	public static final short C_XG_TOKEN = 10101;
	public static final short S_Message = 20000;
	/**
	 * 客户端发送聊天
	 */
	public static final short C_Send_Chat = 20001;
	/**
	 * 服务器通知客户端聊天消息
	 */
	public static final short S_Send_Chat = 20002;
	
	/**
	 * 获取聊天记录。
	 */
	public static final short C_Get_Chat_Log = 20003;
	public static final short S_Send_Chat_Log = 20004;
	
	/** 各聊天频道发送聊天信息未成功的错误返回 **/
	public static final short S_SEND_CHAT_ERROR = 20005;
	
	public static final short C_GET_CHAT_CONF = 20104;
	public static final short S_GET_CHAT_CONF = 20105;
	public static final short C_GET_RECENT_CONTACTS = 20107;
	public static final short S_GET_RECENT_CONTACTS = 20108;
	public static final short C_SETTING_CHAT = 20109;
	public static final short C_CHAT_SETTING_REQ = 20111;
	public static final short S_CHAT_SETTING_RESP = 20112;
	
	
	/**
	 * 国战 Socket C 获取 用户信息
	 */
	public static final short C_Call_User_Info = 21101;
	
	/**
	 * 国战 Socket S 发送  用户信息
	 */
	public static final short S_Send_User_Info = 21001;
	
	/**
	 * 国战 Socket C 请求 城池信息 
	 */
	public static final short C_Call_City_Info = 21102;
	
	/**
	 * 国战 Socket C 请求 城池信息 
	 */
	public static final short C_Call_City_List_Info = 21103;
	
	/**
	 * 国战 Socket C 玩家移动
	 */
	public static final short C_Call_User_Move = 21104;
	
	/**
	 * 国战 Socket C 玩家发起攻占、修城动作
	 */
	public static final short C_Call_User_Action = 21105;
	
	/**
	 * 国战 Socket C 玩家发起攻击
	 */
	public static final short C_Call_User_Attack = 21106;
	
	/**
	 * 国战 Socket C 玩家请求查看战报或离开
	 */
	public static final short C_Call_Check_Report = 21107;
	
	/**
	 * 国战 Socket C 玩家请求获取个人奖励
	 */
	public static final short C_Call_Fetch_Award = 21108;
	
	/**
	 * 国战 Socket S 发送  城池信息
	 */
	public static final short S_Send_City_Info = 21003;
	
	/**
	 * 国战 Socket S 发送  城池列表信息
	 */
	public static final short S_Send_City_List_Info = 21007;
	
	/**
	 * 国战 Socket S 发送 城池中用户列表
	 */
	public static final short S_Send_City_UserList = 21006;
	
	/**
	 * 国战 Socket S 发送 城池状态Map列表
	 */
	public static final short S_Send_City_State_Maps = 21008;
	
	/**
	 * 国战 Socket S 发送 国家信息
	 */
	public static final short S_Send_Country = 21009;
	
	/**
	 * 国战 Socket S 发送 国家信息列表
	 */
	public static final short S_Send_Country_List = 21010;
	
	/**
	 * 国战 Socket S 发送 国战战斗结果
	 */
	public static final short S_Send_Combat_Result = 21011;
	
	/**
	 * 国战 Socket S 发送 玩家动作执行结果
	 */
	public static final short S_Send_Action_Result = 21012;
	
	/**
	 * 国战 Socket S 发送 战报记录
	 */
	public static final short S_Send_Report = 21013;
	
	/**
	 * 国战 Socket S 发送 城池玩家变动信息
	 */
	public static final short S_Send_City_User_Change = 21014;
	
	/**
	 * 国战 Socket S 发送 领取到的奖励
	 */
	public static final short S_Send_Personal_Award = 21015;
	/**
	 * 国战 Socket S 发送 个人战报
	 */
	public static final short S_Send_Combat_Record = 21016;
	
	public static final short NationalWarInfoListResId = 21002;
	public static final short NationalScheduleResId = 21004;
	public static final short NationalScheduleListResId = 21005;
	
	/**
	 * 场景相关协议号
	 */
	public static final short LMZ_ChengHao = 21771;
	public static final short LMZ_OVER = 21775;
	public static final short LMZ_SCORE_OVER = 21778;
	public static final short LMZ_BUY_XueP = 21781;
	public static final short LMZ_BUY_ZhaoHuan = 21782;
	public static final short LMZ_SCORE_ONE = 21789;
	public static final short LMZ_SCORE_LIST = 21790;
	public static final short LMZ_FuHuo = 21791;
	public static final short LMZ_CMD_LIST = 21792;
	public static final short LMZ_CMD_ONE = 21793;
	public static final short LMZ_ZhaoHuan = 21794;
	public static final short LMZ_fenShen = 21796;
	public static final short C_ENTER_LMZ = 21801;
	public static final short AOE_SKILL = 21803;
	public static final short SKILL_PREPARE = 21804;
	public static final short SKILL_STOP = 21805;
	//public static final short C_BID = 21851;
	public static final short POS_JUMP = 21901;
	public static final short Enter_Scene = 22000;
	public static final short Enter_Scene_Confirm = 22001;
	public static final short Spirite_Move = 22002;
	public static final short Exit_Scene = 22003;
	public static final short Enter_HouseScene = 22004;
	public static final short Exit_HouseScene = 22005;
	public static final short Enter_YBScene = 22009;
	public static final short Exit_YBScene = 22010;
	public static final short Enter_TBBXScene = 22011;
	public static final short Exit_TBBXScene = 22012;
	
	public static final short S_HEAD_STRING = 22101;
	public static final short S_HEAD_INFO = 22103;
	
	public static final short C_CHOOSE_SCENE = 22301 ;
	public static final short S_CHOOSE_SCENE = 22302 ;
	public static final short C_SCENE_GETALL = 22303 ;
	public static final short S_SCENE_GETALL = 22304 ;
	/**
	 * pve相关协议号
	 */
	public static final short Battle_Pve_Init_Req = 23000;
	public static final short B_Hero = 23001;
	public static final short B_Soldier = 23002;
	public static final short B_Troop = 23003;
	public static final short Battle_Init = 23004;
	
	public static final short Battle_Pvp_Init_Req = 23005;
	public static final short Battle_Pvp_Init = 23006;
	/** 请求过关斩将最大关卡id */
	public static final short PVE_MAX_ID_REQ = 23007;
	/** 返回过关斩将最大关卡id */
	public static final short PVE_MAX_ID_RESP = 23008;
	
	//账号协议
	public static final short ACC_REG = 23101;
	public static final short ACC_REG_RET = 23102;
	public static final short ACC_LOGIN = 23103;
	public static final short ACC_LOGIN_RET = 23104;
	public static final short CREATE_ROLE_REQUEST = 23105;
	public static final short CREATE_ROLE_RESPONSE = 23106;
	public static final short ROLE_NAME_REQUEST = 23107;
	public static final short ROLE_NAME_RESPONSE = 23108;
	public static final short S_ACC_login_kick = 23110;
	
	public static final short channel_LOGIN = 23113;//从渠道登录
	
	//pve章节协议
	public static final short PVE_PAGE_REQ = 23201;
	public static final short PVE_PAGE_RET = 23202;
	
	public static final short PVE_BATTLE_OVER_REPORT = 23203;
	public static final short PVE_STAR_REWARD_INFO_REQ = 23204;
	public static final short PVE_STAR_REWARD_INFO_RET = 23205;
	public static final short PVE_STAR_REWARD_GET = 23206;
	public static final short PVE_STAR_REWARD_GET_RET = 23207;
	
	public static final short PVE_GuanQia_Request = 23210;
	public static final short PVE_GuanQia_Info = 23211;
	public static final short C_PVE_Reset_CQ = 23212;
	public static final short S_PVE_Reset_CQ = 23213;
	
	public static final short C_PVE_SAO_DANG = 23220;
	public static final short S_PVE_SAO_DANG = 23222;
	
	public static final short C_YuanJun_List_Req = 23230;
	public static final short S_YuanJun_List = 23231;
	
	public static final short C_BuZhen_Report = 23240;//客户端向服务器发送布阵信息
	public static final short C_MIBAO_SELECT = 23241;//客户端向服务器发送秘宝选择信息
	public static final short S_MIBAO_SELECT_RESP = 23242;//服务器发送秘宝选择信息
	
	/** pve战斗请求 **/
	public static final short ZHANDOU_INIT_PVE_REQ = 24201;
	/** pvp战斗请求 **/
	public static final short ZHANDOU_INIT_PVP_REQ = 24202;
	/** 掠夺战斗请求**/
	public static final short ZHANDOU_INIT_LVE_DUO_REQ = 24203;
	/**掠夺-驱逐战斗请求*/
	public static final short qu_zhu_req = 24204;
	
	/** pve、pvp、押镖、劫镖、掠夺、荒野战斗请求返回数据 **/
	public static final short ZHANDOU_INIT_RESP = 24151;
	
	/** 领取通章奖励*/
	public static final short has_get_zhangJie_award_req = 24152;
	public static final short has_get_zhangJie_award_resp = 24153;
	public static final short get_passZhangJie_award_req = 24154;
	public static final short get_passZhangJie_award_resp = 24155;
	/** 请求未领取过通关奖励章节列表 */
	public static final short C_NOT_GET_AWART_ZHANGJIE_REQ = 24156;
	/** 返回未领取过通关奖励章节列表 */
	public static final short S_NOT_GET_AWART_ZHANGJIE_RESP = 24157;
	//通关奖励
	public static final short BattlePveResult_Req = 23300;
	public static final short Award_Item = 23301;
	public static final short BattlePve_Result = 23302;
	
	public static final short C_Report_battle_replay = 23310;
	public static final short C_Request_battle_replay = 23311;
	
	public static final short PLAYER_STATE_REPORT = 23401;
	
	public static final short C_APP_SLEEP = 23411;//客户端报告进入后台
	public static final short C_APP_WAKEUP = 23413;//客户端报告程序进入前台
	
	public static final short PLAYER_SOUND_REPORT = 23501;
	public static final short C_get_sound = 23505;
	public static final short S_get_sound = 23507;
	
	public static final short JunZhuInfoReq = 23601;
	public static final short JunZhuInfoRet = 23602;
	public static final short JunZhuAttPointReq = 23603;
	public static final short JunZhuAttPointRet = 23604;
	public static final short JunZhuAddPointReq = 23605;
	
	public static final short C_KeJiInfo = 23650;
	public static final short S_KeJiInfo = 23651;
	public static final short C_KeJiUp = 23652;
	
	public static final short C_EquipAdd = 23701;
	public static final short C_EquipRemove = 23702;
	public static final short S_EquipInfo = 23703;
	public static final short S_BagInfo = 23704;
	public static final short C_BagInfo = 23705;
	public static final short C_EquipInfo = 23706;//装备列表
	public static final short S_BAG_CHANGE_INFO = 23707;		// 背包变化信息
	
	public static final short C_EquipDetailReq = 23710;
	public static final short S_EquipDetail = 23711;
	
	public static final short C_EquipInfoOtherReq = 23712;// 别人装备详情
	public static final short S_EquipInfoOther = 23713;
	
	////Equip
	//装备列表
	public static final short C_EQUIP_LIST = 24001;
	public static final short S_EQUIP_LIST = 24002;
	//装备强化
	public static final short C_EQUIP_UPGRADE = 24003;
	public static final short S_EQUIP_UPGRADE = 24004;
	public static final short C_EQUIP_XiLian = 24012;
	public static final short S_EQUIP_XiLian = 24013;
	public static final short C_EQUIP_JINJIE = 24015;
	public static final short S_EQUIP_JINJIE = 24016;
	public static final short S_EQUIP_XILIAN_ERROR = 24018;
	public static final short C_EQUIP_UPALLGRADE=24019;//一键强化
	public static final short S_EQUIP_UPALLGRADE=24020;//一键强化返回
	
	public static final short C_EQUIP_BAOSHI_REQ = 24021 ;//装备相关宝石镶嵌请求
	public static final short S_EQUIP_BAOSHI_RESP = 24022 ;//装备相关宝石镶嵌返回
	
	public static final short C_JingMai_info = 24100;
	public static final short C_JingMai_up = 24101;
	public static final short S_JingMai_info = 24103;
	
	//mail sys protocol code
	public static final short C_REQ_MAIL_LIST = 25003;
	public static final short S_REQ_MAIL_LIST = 25004;
//	public static final short C_DELETE_MAIL = 25005;
	public static final short S_DELETE_MAIL = 25006;
	public static final short C_MAIL_GET_REWARD = 25007;
	public static final short S_MAIL_GET_REWARD = 25008;
	public static final short S_MAIL_NEW = 25010;
	public static final short C_SEND_EAMIL = 25011;
	public static final short S_SEND_EAMIL = 25012;
	public static final short C_READED_EAMIL = 25013;
	public static final short S_READED_EAMIL = 25014;
	public static final short C_EMAIL_RESPONSE = 25015;
	public static final short S_EMAIL_RESPONSE = 25016;
	
	public static final short C_SHOW_WU_QI = 25021;
	public static final short S_SHOW_WU_QI = 25022;
	
	//武将
	public static final short HERO_INFO_REQ 			= 26001;
	public static final short HERO_DATA 				= 26002;
	public static final short HERO_INFO 				= 26003;
	public static final short WUJIANG_TECHINFO_REQ  	= 26004;
	public static final short WUJIANG_TECHLEVELUP_REQ   = 26005;
	public static final short WUJIANG_LEVELUP_REQ		= 26006;
	public static final short HERO_ACTIVE_REQ = 26007;
	public static final short JINGPO_REFRESH_REQ = 26008;			//已废弃
	public static final short WUJIANG_TECH_SPEEDUP_REQ = 26009;

	
	public static final short WUJIANG_TECHINFO_RESP 	= 26054;
	public static final short WUJIANG_TECHLEVELUP_RESP  = 26055;
	public static final short WUJIANG_LEVELUP_RESP 		= 26056;
	public static final short HERO_ACTIVE_RESP = 26057;
	public static final short JINGPO_REFRESH_RESP = 26058;
	public static final short WUJIANG_TECH_SPEEDUP_RESP= 26059;
	
	// 掠夺协议
	public static final short LVE_DUO_INFO_REQ = 26060;
	public static final short LVE_DUO_INFO_RESP = 26061;
	public static final short LVE_CONFIRM_REQ = 26062;
	public static final short LVE_CONFIRM_RESP = 26063;
	public static final short LVE_BATTLE_RECORD_REQ = 26064;
	public static final short LVE_BATTLE_RECORD_RESP = 26065;
	public static final short LVE_GO_LVE_DUO_REQ = 26066;
	public static final short LVE_GO_LVE_DUO_RESP = 26067;
	public static final short LVE_BATTLE_END_REQ = 26068;
	public static final short LVE_NEXT_ITEM_REQ = 26069;
	public static final short LVE_NEXT_ITEM_RESP = 26070;
	public static final short LVE_BATTLE_END_RESP = 26071;
	public static final short LVE_NOTICE_CAN_LVE_DUO = 26072;
	public static final short LVE_HELP_REQ = 26073;
	public static final short LVE_HELP_RESP = 26074;
	// 掠夺战斗请求协议是：24203（和pve，pvp写在一起定义）;
	// 掠夺战斗请求返回协议时：24151
	
	// 百战千军协议类型
	/**请求百战 **/
	public static final short BAIZHAN_INFO_REQ		= 27001;
	/** **/
	public static final short BAIZHAN_INFO_RESP		= 27002;
	/** 请求 挑战 **/
	public static final short CHALLENGE_REQ 		= 27011;
	/** **/
	public static final short CHALLENGE_RESP 		= 27012;
	/**请求 百战千军中确定做某种事情**/
	public static final short CONFIRM_EXECUTE_REQ	= 27015;
	/** **/
	public static final short CONFIRM_EXECUTE_RESP	= 27016;
	/** 前台发送百战千军的结果**/
	public static final short BAIZHAN_RESULT	    = 27017;
	/** 挑战者状态 **/
//	public static final short PLAYER_STATE_REQ	    = 27018;//协议合并至CHALLENGE_REQ，无效2016-04-22
//	public static final short PLAYER_STATE_RESP	    = 27019;//协议合并至CHALLENGE_RESP，无效2016-04-22
	/** 战斗记录请求 **/
	public static final short ZHAN_DOU_RECORD_REQ      = 27022;
	/** 战斗记录响应 **/
	public static final short ZHAN_DOU_RECORD_RESP      = 27023;
	/**是27017的响应页面发送**/
	public static final short BAIZHAN_RESULT_RESP    =  27024;
	
//	// 刷新挑战对手列表请求
//	public static final short REFRESH_ENEMY_LIST_REQ	    = 27026;//协议合并至CONFIRM_EXECUTE_REQ，无效2016-04-22
//	public static final short REFRESH_ENEMY_LIST_RESP = 27027;//协议合并至CONFIRM_EXECUTE_RESP，无效2016-04-22
	
	
	public static final short C_LM_HOUSE_INFO = 27301;
	public static final short S_LM_HOUSE_INFO = 27302;
	public static final short S_LM_UPHOUSE_INFO = 27303;//更新房屋信息
	public static final short C_HOUSE_EXCHANGE_RQUEST = 27311;
	public static final short S_HOUSE_EXCHANGE_RESULT = 27312;
	public static final short C_HOUSE_APPLY_LIST = 27313;
	public static final short S_HOUSE_APPLY_LIST = 27314;
	public static final short C_AnswerExchange = 27321;
	
	public static final short C_Set_House_state = 27304;
	public static final short C_EnterOrExitHouse = 27305;
	public static final short C_GetHouseVInfo = 27306;//请求访客列表
	public static final short C_get_house_exp = 27307;//获取小房子经验
	public static final short S_house_exp = 27308;
	public static final short C_huan_wu_info = 27309;
	public static final short S_huan_wu_info = 27310;
	public static final short C_huan_wu_Oper = 27331;
	public static final short C_huan_wu_list = 27333;
	public static final short S_huan_wu_list = 27334;
	public static final short C_huan_wu_exchange = 27337;
	public static final short S_huan_wu_exchange = 27338;
	public static final short C_ExCanJuanJiangLi = 27341;
	public static final short S_ExCanJuanJiangLi = 27342;
	public static final short C_up_house = 27343;
	public static final short C_Pai_big_house = 27351;
	public static final short S_Pai_big_house = 27352;
	public static final short C_GET_BIGHOUSE_EXP = 27353;//获取大房子经验
	public static final short S_HouseVInfo = 27354;//发送访客列表
	public static final short C_ShotOffVisitor = 27355;//踢出访客
	public static final short S_ShotOffVisitor = 27356;//访客被踢
	public static final short C_EHOUSE_EXCHANGE_RQUEST =  27357;//请求交换空房屋
	public static final short S_EHOUSE_EXCHANGE_RESULT =  27358;//请求交换空房屋返回结果
	public static final short C_CHANGE_BIGHOUSE_WORTH =  27359;//请求衰减高级房屋价值
	public static final short C_CANCEL_EXCHANGE =  27360;//请求撤回交换房屋申请
	public static final short S_CANCEL_EXCHANGE =  27361;//请求撤回交换房屋申请返回结果
	public static final short C_get_house_info = 27362;//获取自己房子信息
	public static final short S_house_info = 27363;//推送房子信息
	//
	public static final short C_JIAN_ZHU_INFO = 27401;
	public static final short S_JIAN_ZHU_INFO = 27402;
	public static final short C_JIAN_ZHU_UP = 27403;
	public static final short S_JIAN_ZHU_UP = 27404;
	
	public static final short C_LMKJ_UP = 27503;//请求升级联盟科技
	public static final short S_LMKJ_UP = 27504;
	public static final short C_LMKJ_INFO = 27507;
	public static final short S_LMKJ_INFO = 27508;
	
	public static final short C_LM_CHOU_JIANG_1 = 27510;
	public static final short C_LM_CHOU_JIANG_N = 27511;
	public static final short S_LM_CHOU_JIANG = 27512;
	public static final short C_LM_CHOU_JIANG_INFO = 27513;
	public static final short S_LM_CHOU_JIANG_INFO = 27514;
	public static final short C_LMKEJI_JIHUO = 27515;
	public static final short S_LMKEJI_JIHUO = 27516;
	
	
	
	//
	//
	//联盟 28001~28099(预计)
	public static final short GET_UNION_INFO_REQ         = 28001;
	public static final short UNION_APPLY_JION_REQ       = 28002;
	public static final short GET_UNION_FRIEND_INFO_REQ  = 28003;
	public static final short UNION_EDIT_REQ             = 28004;
	public static final short UNION_INNER_EDIT_REQ 	 	 = 28005;
	public static final short UNION_OUTER_EDIT_REQ 	 	 = 28006;
	public static final short CREATE_UNION_REQ 		     = 28007;
	public static final short UNION_LEVELUP_REQ 		 = 28008;
	public static final short UNION_APPLY_REQ 			 = 28009;
	public static final short UNION_INVITE_REQ 		 	 = 28010;
	public static final short UNION_INVITED_AGREE_REQ 	 = 28011;
	public static final short UNION_INVITED_REFUSE_REQ   = 28012;
	public static final short UNION_QUIT_REQ 			 = 28013;
	public static final short UNION_DISMISS_REQ 		 = 28014;
	public static final short UNION_TRANSFER_REQ 		 = 28015;
	public static final short UNION_ADVANCE_REQ 		 = 28016;
	public static final short UNION_DEMOTION_REQ 		 = 28017;
	public static final short UNION_REMOVE_REQ 		 	 = 28018;
	public static final short UNION_DETAIL_INFO_REQ 	 = 28019;
	       
	public static final short GET_UNION_INFO_RESP 		 = 28051;
	public static final short UNION_APPLY_JION_RESP      = 28052;
	public static final short GET_UNION_FRIEND_RESP 	 = 28053;
	public static final short UNION_EDIT_RESP 			 = 28054;
	public static final short UNION_INNER_EDIT_RESP 	 = 28055;
	public static final short UNION_OUTER_EDIT_RESP 	 = 28056;
	public static final short CREATE_UNION_RESP 		 = 28057;
	public static final short UNION_LEVELUP_RESP 		 = 28058;
	public static final short UNION_APPLY_RESP 		 	 = 28059;
	public static final short UNION_INVITE_RESP 		 = 28060;
	public static final short UNION_INVITED_AGREE_RESP   = 28061;
	public static final short UNION_INVITED_REFUSE_RESP  = 28062;
	public static final short UNION_QUIT_RESP 			 = 28063;
	public static final short UNION_DISMISS_RESP 		 = 28064;
	public static final short UNION_TRANSFER_RESP 		 = 28065;
	public static final short UNION_ADVANCE_RESP 		 = 28066;
	public static final short UNION_DEMOTION_RESP 		 = 28067;
	public static final short UNION_REMOVE_RESP 		 = 28068;
	public static final short UNION_DETAIL_INFO 		 = 28069;
	
	public static final short C_get_daily_award_info	 = 28100;
	public static final short S_daily_award_info		 = 28110;
	public static final short C_get_daily_award			 = 28120;
	
	// ************  定时请求操作指令 	*************
	/** 玩家定时请求任务 **/
	public static final short C_TIMEWORKER_INTERVAL = 28301;
	/** 发送玩家定时请求结果 **/
	public static final short S_TIMEWORKER_INTERVAL = 28302;
	
	//**************  商城指令 	*******************
	//抽卡预留28201~28299
	public static final short BUY_CARDBAG_REQ			 = 28201;
	public static final short OPEN_CARDBAG_REQ			 = 28202;
	
	public static final short BUY_CARDBAG_RESP			 = 28251;
	public static final short OPEN_CARDBAG_RESP			 = 28252;
	
	/** 请求宝箱购买信息 **/
	public static final short BUY_TREASURE_INFOS_REQ = 28253;
	/** 返回宝箱购买信息 **/
	public static final short BUY_TREASURE_INFOS_RESP = 28254;
	/** 购买宝箱 **/
	public static final short BUY_TREASURE = 28255;
	/** 返回购买宝箱获得物品信息 **/
	public static final short BUY_TREASURE_RESP = 28256;
	/** 请求资源购买信息 **/
	public static final short BUY_RESOURCE_INFOS_REQ = 28257;
	/** 返回资源购买信息 **/
	public static final short BUY_RESOURCE_INFOS_RESP = 28258;
	/** 返回商城购买失败信息 **/
	public static final short PURCHASE_FAIL = 28260;
	
	
	/** 请求购买体力和铜币的次数 **/
	public static final short C_BUY_TIMES_REQ = 28321;
	/** 返回购买体力个铜币的次数 **/
	public static final short S_BUY_TIMES_INFO = 28322;
	/** 购买体力 **/
	public static final short C_BUY_TiLi = 28323;
	/** 购买铜币 **/
	public static final short C_BUY_TongBi = 28324;
	/** 购买铜币返回 不知道谁用的 **/
//	public static final short S_BUY_TongBi = 28325;
	/** 购买秘宝升级点数 **/
	public static final short C_BUY_MIBAO_POINT = 28327;
	/** 购买秘宝升级点数返回 **/
	public static final short S_BUY_MIBAO_POINT_RESP = 28328;
	/** 连续购买铜币 **/
	public static final short C_BUY_TongBi_LiXu = 28329;
	/** 连续购买铜币成功返回  ，失败返回和单次购买一样**/
	public static final short S_BUY_TongBi_LiXu = 28330;
	/** 购买铜币数据 **/
	public static final short C_BUY_TongBi_Data = 2831;
	/** 购买铜币数据返回  **/
	public static final short S_BUY_TongBi_Data = 2830;
	
	//**************  成就指令  ***************
	/** 请求成就列表 **/
	public static final short C_ACHE_LIST_REQ = 28331;
	/** 返回成就列表 **/
	public static final short S_ACHE_LIST_RESP = 28332;
	/** 成就完成通知 **/
	public static final short S_ACHE_FINISH_INFORM = 28334;
	/** 领取成就奖励 **/
	public static final short C_ACHE_GET_REWARD_REQ = 28335;
	/** 领取成就奖励返回结果 **/
	public static final short S_ACHE_GET_REWARD_RESP = 28336;
	
	//***************  每日任务指令  *******************
	/** 请求每日任务列表 **/
	public static final short C_DAILY_TASK_LIST_REQ = 28341;
	/** 返回每日任务列表 **/
	public static final short S_DAILY_TASK_LIST_RESP = 28342;
	/** 每日任务完成通知 **/
	public static final short S_DAILY_TASK_FINISH_INFORM = 28344;
	/** 领取每日任务奖励 **/
	public static final short C_DAILY_TASK_GET_REWARD_REQ = 28345;
	/** 领取每日任务奖励返回结果 **/
	public static final short S_DAILY_TASK_GET_REWARD_RESP = 28346;
	/** 每日任务活跃度领奖*/
	public static final short dailyTask_get_huoYue_award_req = 28347;
	public static final short dailyTask_get_huoYue_award_resp = 28348;
	
	public static final short C_BuZhen_Hero_Req = 29401;
	public static final short S_BuZhen_Hero_Info = 29402;

	public static final short C_TaskReq = 29501;
	public static final short S_TaskList = 29502;
	public static final short S_TaskSync = 29503;//
	public static final short C_GetTaskReward = 29504;//
	public static final short S_GetTaskRwardResult = 29505;//
	public static final short C_TaskProgress = 29506;//客户端汇报任务进度
	
	public static final short C_YuJueHeChengRequest = 29509;
	public static final short S_YuJueHeChengResult = 29510;
	public static final short S_NEW_CHENGHAO = 29520;
	
	public static final short C_GET_HighLight_item_ids = 29523;
	public static final short S_GET_HighLight_item_ids = 29524;
	
	public static final short C_CLOSE_TAN_BAO_UI = 29531;
	
	public static final short NEW_MIBAO_INFO = 29551; 
	public static final short NEW_MIBAO_JIHUO = 29552; 
	public static final short NEW_MISHU_JIHUO = 29553; 
	public static final short S_SEND_MIBAO_INFO = 29554;
	//秘宝协议
	/** 秘宝激活请求 **/
	public static final short C_MIBAO_ACTIVATE_REQ = 29601;
	/** 秘宝激活结果返回 **/
	public static final short S_MIBAO_ACTIVATE_RESP = 29602;
	/** 秘宝信息请求 **/
	public static final short C_MIBAO_INFO_REQ = 29603;
	/** 秘宝信息返回 **/
	public static final short S_MIBAO_INFO_RESP = 29604;
	/** 秘宝升级请求 **/
	public static final short C_MIBAO_LEVELUP_REQ = 29605;
	/** 秘宝升级结果返回 **/
	public static final short S_MIBAO_LEVELUP_RESP = 29606;
	/** 秘宝升星请求 **/
	public static final short C_MIBAO_STARUP_REQ = 29607;
	/** 秘宝升星结果返回 **/
	public static final short S_MIBAO_STARUP_RESP = 29608;
	/** 别人秘宝信息请求 **/
	public static final short C_MIBAO_INFO_OTHER_REQ = 29609;
	/** 别人秘宝信息返回 **/
	public static final short S_MIBAO_INFO_OTHER_RESP = 29610;
	/** 手动激活（进阶）秘宝技能**/
	public static final short MIBAO_DEAL_SKILL_REQ = 29611;
	public static final short MIBAO_DEAL_SKILL_RESP = 29612;
	/**领奖（因为秘宝总星星数达到要求）*/
	public static final short GET_FULL_STAR_AWARD_REQ = 29613;
	public static final short GET_FULL_STAR_AWARD_RESP = 29614;
	
	//***************  探宝协议  *******************
	/**请求矿区主界面**/
	public static final short EXPLORE_INFO_REQ    = 30002;
	/**响应矿区主界面**/
	public static final short EXPLORE_INFO_RESP   = 30003;
	/**请求采矿**/
	public static final short EXPLORE_REQ         = 30004;
	/**响应不可以采矿**/
	public static final short EXPLORE_RESP        = 30005;
	/**响应发送采矿奖励信息**/
	public static final short EXPLORE_AWARDS_INFO = 30006;
	
	
	//当铺
	/** 卖出物品 **/
	public static final short PAWN_SHOP_GOODS_SELL = 30021;
	/** 卖出物品成功 **/
	public static final short PAWN_SHOP_GOODS_SELL_OK = 30022;
	/** 请求当铺物品列表 **/
	public static final short PAWN_SHOP_GOODS_LIST_REQ = 30023;
	/** 返回当铺物品列表 **/
	public static final short PAWN_SHOP_GOODS_LIST = 30024;
	/** 购买物品 **/
	public static final short PAWN_SHOP_GOODS_BUY = 30025;
	/** 购买物品成功 **/
	public static final short PAWN_SHOP_GOODS_BUY_RESP = 30026;
	/** 手动刷新当铺物品 **/
	public static final short PAWN_SHOP_GOODS_REFRESH = 30027;
	/** 手动刷新当铺物品 **/
	public static final short PAWN_SHOP_GOODS_REFRESH_RESP = 30028;
	
	//***************** 联盟协议  ******************
	/** 从npc处点击查看联盟 **/
	public static final short ALLIANCE_INFO_REQ = 30100;
	/** 返回联盟信息， 给没有联盟的玩家返回此条信息 **/
	public static final short ALLIANCE_NON_RESP = 30101;
	/** 返回联盟信息， 给有联盟的玩家返回此条信息 **/
	public static final short ALLIANCE_HAVE_RESP = 30102;
	/** 验证联盟名字 **/
	public static final short CHECK_ALLIANCE_NAME = 30103;
	/** 返回验证联盟结果 **/
	public static final short CHECK_ALLIANCE_NAME_RESP = 30104;
	/** 创建联盟 **/
	public static final short CREATE_ALLIANCE = 30105;
	/** 返回创建联盟结果 **/
	public static final short CREATE_ALLIANCE_RESP = 30106;
	/** 查找联盟 **/
	public static final short FIND_ALLIANCE = 30107;
	/** 返回查找联盟结果 **/
	public static final short FIND_ALLIANCE_RESP = 30108;
	/** 申请联盟 **/
	public static final short APPLY_ALLIANCE = 30109;
	/** 返回申请联盟结果 **/
	public static final short APPLY_ALLIANCE_RESP = 30110;
	/** 取消加入联盟 **/
	public static final short CANCEL_JOIN_ALLIANCE = 30111;
	/** 返回取消加入联盟结果 **/
	public static final short CANCEL_JOIN_ALLIANCE_RESP = 30112;
	/** 退出联盟 **/
	public static final short EXIT_ALLIANCE = 30113;
	/** 退出联盟成功 **/
	public static final short EXIT_ALLIANCE_RESP = 30114;
	/** 查看联盟成员 **/
	public static final short LOOK_MEMBERS = 30115;
	/** 返回联盟成员信息 **/
	public static final short LOOK_MEMBERS_RESP = 30116;
	/** 开除成员**/
	public static final short FIRE_MEMBER = 30117;
	/** 开除成员返回**/
	public static final short FIRE_MEMBER_RESP = 30118;
	/** 升职成员**/
	public static final short UP_TITLE = 30119;
	/** 升职成员返回**/
	public static final short UP_TITLE_RESP = 30120;
	/** 降职成员**/
	public static final short DOWN_TITLE = 30121;
	/** 降职成员返回**/
	public static final short DOWN_TITLE_RESP = 30122;
	/** 查看申请联盟玩家**/
	public static final short LOOK_APPLICANTS = 30123;
	/** 查看申请联盟玩家结果返回**/
	public static final short LOOK_APPLICANTS_RESP = 30124;
	/** 拒绝申请**/
	public static final short REFUSE_APPLY = 30125;
	/** 拒绝申请返回**/
	public static final short REFUSE_APPLY_RESP = 30126;
	/** 同意申请**/
	public static final short AGREE_APPLY = 30127;
	/** 同意申请返回**/
	public static final short AGREE_APPLY_RESP = 30128;
	/** 修改公告**/
	public static final short UPDATE_NOTICE = 30129;
	/** 修改公告返回**/
	public static final short UPDATE_NOTICE_RESP = 30130;
	/** 解散联盟**/
	public static final short DISMISS_ALLIANCE = 30131;
	/** 解散联盟返回**/
	public static final short DISMISS_ALLIANCE_OK = 30132;
	/** 打开招募**/
	public static final short OPEN_APPLY = 30133;
	/** 打开招募返回**/
	public static final short OPEN_APPLY_RESP = 30134;
	/** 关闭招募**/
	public static final short CLOSE_APPLY = 30135;
	/** 关闭招募返回成功**/
	public static final short CLOSE_APPLY_OK = 30136;
	/** 转让联盟**/
	public static final short TRANSFER_ALLIANCE = 30137;
	/** 转让联盟返回**/
	public static final short TRANSFER_ALLIANCE_RESP = 30138;
	/** 盟主选举报名**/
	public static final short MENGZHU_APPLY = 30139;
	/** 盟主选举报名结果返回**/
	public static final short MENGZHU_APPLY_RESP = 30140;
	/** 盟主选举报名**/
	public static final short MENGZHU_VOTE = 30141;
	/** 盟主选举报名结果返回**/
	public static final short MENGZHU_VOTE_RESP = 30142;
	/** 放弃投票 **/
	public static final short GIVEUP_VOTE = 30143;
	/** 放弃投票结果返回 **/
	public static final short GIVEUP_VOTE_RESP = 30144;
	/** 立刻加入联盟 **/
	public static final short IMMEDIATELY_JOIN = 30145;
	/** 立刻加入联盟返回 **/
	public static final short IMMEDIATELY_JOIN_RESP = 30146;
	/** 加入联盟被批准通知 **/
	public static final short ALLIANCE_ALLOW_NOTIFY = 30147;
	/** 被联盟开除通知 **/
	public static final short ALLIANCE_FIRE_NOTIFY = 30148;
	/** 联盟虎符捐献 **/
	public static final short ALLIANCE_HUFU_DONATE = 30149;
	public static final short ALLIANCE_HUFU_DONATE_RESP = 30150;
	/** 有新的申请成员通知 **/
	public static final short ALLIANCE_HAVE_NEW_APPLYER = 30160;
	/** 联盟事件请求 */
	public static final short ALLINACE_EVENT_REQ = 30161;
	/** 联盟事件返回 */
	public static final short ALLINACE_EVENT_RESP = 30162;
	/** 联盟升级通知 */
	public static final short ALLIANCE_LEVEL_UP_NOTIFY = 30164;
	/** 联盟解散通知 **/
	public static final short ALLIANCE_DISMISS_NOTIFY = 30166;
	/** 联盟信息变更通知 */
	public static final short ALLIANCE_STATE_NOTIFY = 30167;
	/** 请求封禅信息 */
	public static final short C_ALLIANCE_FENGSHAN_REQ = 30168;
	/**  请求封禅信息 返回  */
	public static final short S_ALLIANCE_FENGSHAN_RESP = 30169;
	/** 请求封禅 */
	public static final short C_DO_ALLIANCE_FENGSHAN_REQ = 30170;
	/**  请求封禅 返回  */
	public static final short S_DO_ALLIANCE_FENGSHAN_RESP = 30171;
	/**  请求联盟目标信息  */
	public static final short C_ALLIANCE_TARGET_INFO = 30173;
	/**  联盟目标信息返回 */
	public static final short S_ALLIANCE_TARGET_INFO_Resp = 30174;
	/**  领取联盟等级目标奖励 */
	public static final short C_GET_ALLIANCEL_LEVEL_AWARD = 30175;
	/**  领取联盟等级目标奖励结果返回 */
	public static final short S_GET_ALLIANCEL_LEVEL_AWARD_RESP = 30176;
	/**  联盟邀请 */
	public static final short C_ALLIANCE_INVITE = 30177;
	/**  联盟邀请返回 */
	public static final short S_ALLIANCE_INVITE = 30178;
	/**  联盟邀请列表 */
	public static final short C_ALLIANCE_INVITE_LIST = 30179;
	/**  联盟邀请列表返回 */
	public static final short S_ALLIANCE_INVITE_LIST = 30180;
	/**  拒绝联盟邀请 */
	public static final short C_ALLIANCE_INVITE_REFUSE = 30181;
	/**  拒绝联盟邀请返回 */
	public static final short S_ALLIANCE_INVITE_REFUSE = 30182;
	/**  同意联盟邀请 */
	public static final short C_ALLIANCE_INVITE_AGREE = 30183;
	/**  同意联盟邀请返回 */
	public static final short S_ALLIANCE_INVITE_AGREE = 30184;
	/**  通知中同意联盟邀请返回 */
	public static final short S_ALLIANCE_INVITE_RESP = 30185;
	public static final short C_ALLIANCE_UPGRADE_LEVEL = 30189;
	public static final short S_ALLIANCE_UPGRADE_LEVEL = 30190;
	
	public static final short C_ALLIANCE_UPGRADELEVEL_SPEEDUP = 30191;
	public static final short S_ALLIANCE_UPGRADELEVEL_SPEEDUP = 30192;

	public static final short C_ALLIANCE_UPINFO_SPEEDUP = 30193;
	public static final short S_ALLIANCE_UPINFO_SPEEDUP = 30194;
	
	/**  获得联盟战准备信息*/
	public static final short C_ALLIANCE_CITYFIGHTINFO_REQ = 30186;
	/**  获得联盟战准备信息返回*/
	public static final short S_ALLIANCE_CITYFIGHTINFO_RESP = 30187;
	/**	 奖励信息请求*/
	public static final short C_CITYWAR_REWARD_REQ = 30188;
	/**竞拍页面操作请求返回*/
	public static final short S_CITYWAR_OPERATE_RESP = 30195;
	/**宣战情报推送返回*/
	public static final short S_CITYWAR_BID_MSG_RESP = 30196;
	/**	 奖励信息返回*/
	public static final short S_CITYWAR_REWARD_RESP = 30197;
	/**	 请求战报信息*/
	public static final short C_CITYWAR_GRAND_REQ = 30198;
	/**	 战报信息返回*/
	public static final short S_CITYWAR_GRAND_RESP = 30199;
	/**	竞拍页面请求*/
	public static final short C_CITYWAR_BID_REQ = 30200;
	/**竞拍页面请求返回*/
	public static final short S_CITYWAR_BID_RESP = 30210;
	/**竞拍页面操作请求*/
	public static final short C_CITYWAR_OPERATE_REQ = 30211;
	
	/**积分战报页面操作请求*/
	public static final short C_CITYWAR_SCORE_RESULT_REQ = 30212;
	/**积分战报页面请求返回*/
	public static final short S_CITYWAR_SCORE_RESULT_RESP = 30213;
	
	public static final short C_SETTINGS_GET = 30201;//客户端获取设置
	public static final short C_SETTINGS_SAVE = 30203;//客户端请求保存设置
	public static final short S_SETTINGS = 30204;//服务器发给客户端设置
	public static final short C_LM_CHANGE_COUNTRY = 30205;
	public static final short S_LM_CHANGE_COUNTRY_REQP = 30206;
	public static final short C_change_name = 30301;
	public static final short S_change_name = 30302;
	
	
	public static final short C_MoBai_Info = 4010;
	public static final short S_MoBai_Info = 4011;
	public static final short C_MoBai = 4012;
	public static final short C_GET_MOBAI_AWARD = 4022;
	/** 加入聊天黑名单 **/
	public static final short C_JOIN_BLACKLIST = 30151;
	/** 加入聊天黑名单返回 **/
	public static final short S_JOIN_BLACKLIST_RESP = 30152;
	/** 查看黑名单 **/
	public static final short C_GET_BALCKLIST = 30153;
	/** 返回黑名单列表 **/
	public static final short S_GET_BALCKLIST = 30154;
	/** 取消屏蔽 **/
	public static final short C_CANCEL_BALCK = 30155;
	/** 取消屏蔽结果 **/
	public static final short S_CANCEL_BALCK = 30156;
	
	
	//***************** 荒野求生协议  ******************
	public static final short HY_SHOP_REQ = 30390;
	public static final short HY_SHOP_RESP = 30391;
	public static final short HY_BUY_GOOD_REQ = 30392;
	public static final short HY_BUY_GOOD_RESP = 30393;
	public static final short ACTIVE_TREASURE_REQ = 30394;
	public static final short ACTIVE_TREASURE_RESP = 30395;
	public static final short MAX_DAMAGE_RANK_REQ = 30396;
	public static final short MAX_DAMAGE_RANK_RESP = 30397;
	public static final short HY_BUY_BATTLE_TIMES_REQ = 30398;
	public static final short HY_BUY_BATTLE_TIMES_RESP = 30399;
	/** 打开荒野 **/
	public static final short C_OPEN_HUANGYE = 30401;
	public static final short S_OPEN_HUANGYE = 30402;
//	/** 驱散迷雾 **/
//	public static final short C_OPEN_FOG = 30403;
//	public static final short S_OPEN_FOG = 30404;
	/** 开启藏宝点 **/
	public static final short C_OPEN_TREASURE = 30405;
	public static final short S_OPEN_TREASURE = 30406;
//	/** 请求奖励库 **/
//	public static final short C_REQ_REWARD_STORE = 30407;
//	public static final short S_REQ_REWARD_STORE = 30408;
//
//	/** 申请奖励 **/
//	public static final short C_APPLY_REWARD = 30409;
//	public static final short S_APPLY_REWARD = 30410;
//	/** 取消申请奖励 **/
//	public static final short C_CANCEL_APPLY_REWARD = 30411;
//	public static final short S_CANCEL_APPLY_REWARD = 30412;
//	/** 盟主分配奖励 **/
//	public static final short C_GIVE_REWARD = 30413;
//	public static final short S_GIVE_REWARD = 30414;
	/** 荒野pve-藏宝点挑战 **/
	public static final short C_HUANGYE_PVE = 30415;
	public static final short S_HUANGYE_PVE_RESP = 30416;
	/** 荒野pve-查看藏宝点信息 **/
	public static final short C_HYTREASURE_BATTLE = 30417;
	public static final short S_HYTREASURE_BATTLE_RESP = 30418;
	/** 荒野pve-藏宝点战斗结束 **/
	public static final short C_HUANGYE_PVE_OVER = 30419;
	public static final short S_HUANGYE_PVE_OVER_RESP = 30420;

	public static final short C_WUBEIFANG_INFO = 30421;
	public static final short S_WUBEIFANG_INFO_RESP = 30422;

	public static final short C_WUBEIFANG_BUY = 30423;
	public static final short S_WUBEIFANG_BUY_RESP = 30424;

	
	
	/**排行榜**/
	public static final short RANKING_REP = 30430;
	public static final short RANKING_RESP = 30431;
	
	/*
	 * 充值
	 */
	/**请求充值**/
	public static final short C_RECHARGE_REQ = 30432;
	public static final short S_RECHARGE_RESP = 30433;
	/**请求充值页面(vip信息)**/
	public static final short C_VIPINFO_REQ = 30434;
	public static final short S_VIPINFO_RESP = 30435;
	
	public static final short C_VIP_GET_GIFTBAG_REQ = 30439;
	public static final short S_VIP_GET_GIFTBAG_RESP = 30440;
	
	// 获取包含了pve秘宝的战力
	public static final short C_PVE_MIBAO_ZHANLI = 30436;
	public static final short S_PVE_MIBAO_ZHANLI = 30437;

	/*
	 * 套装
	 */
	public static final short tao_zhuang_Req = 30500;
	public static final short tao_zhuang_Resp = 30501;
	public static final short activate_tao_zhuang_req = 30502;
	public static final short activate_tao_zhuang_resp = 30503;
	/*
	 * 天赋
	 */
	public static final short TALENT_INFO_REQ = 30537;
	public static final short TALENT_INFO_RESP = 30538;
	public static final short TALENT_UP_LEVEL_REQ = 30539;
	public static final short TALENT_UP_LEVEL_RESP = 30540;
	// 通知玩家天赋可以升级
	public static final short NOTICE_TALENT_CAN_UP = 30541;
	// 通知玩家天赋不可以升级
	public static final short NOTICE_TALENT_CAN_NOT_UP = 30542;
	
	//好友协议
	/**获取好友列表**/
	public static final short C_FRIEND_REQ = 31001;
	public static final short S_FRIEND_RESP = 31002;
	/**请求添加好友**/
	public static final short C_FRIEND_ADD_REQ = 31003;
	public static final short S_FRIEND_ADD_RESP = 31004;
	/**请求删除好友**/
	public static final short C_FRIEND_REMOVE_REQ = 31005;
	public static final short S_FRIEND_REMOVE_RESP = 31006;
	
	public static final short C_GET_FRIEND_IDS = 31011;
	public static final short S_GET_FRIEND_IDS = 31012;
	
	public static final short qianDao_get_vip_present_req =  31999;
	public static final short qianDao_get_vip_present_resp = 32000;
	// 活动协议
	/**请求签到**/
	public static final short C_QIANDAO_REQ = 32001; 
	public static final short S_QIANDAO_RESP = 32002; 
	/**请求签到情况**/
	public static final short C_GET_QIANDAO_REQ = 32003; 
	public static final short S_GET_QIANDAO_RESP = 32004; 
	/**请求补签*/
	public static final short C_GET_QIANDAO_DOUBLE_REQ = 32005; 
	public static final short S_GET_QIANDAO_DOUBLE_RESP = 32006; 
	
	/**获取所有的活动列表**/
	public static final short C_GET_ACTIVITYLIST_REQ = 32101;
	public static final short S_GET_ACTIVITYLIST_RESP = 32102;
	/**获取首冲详情**/
	public static final short C_GET_SHOUCHONG_REQ = 32201;
	public static final short S_GET_SHOUCHONG_RESP = 32202;
	/**领取首冲奖励**/
	public static final short C_SHOUCHONG_AWARD_REQ = 32203;
	public static final short S_SHOUCHONG_AWARD_RESP = 32204;
	/**转国**/
	public static final short C_ZHUANGGUO_REQ = 32205;
	public static final short S_ZHUANGGUO_RESP = 32206;
	
	/**押镖**/
	public static final short C_YABIAO_INFO_REQ	= 3401;//请求押镖活动界面
	public static final short S_YABIAO_INFO_RESP= 3402;//请求押镖活动界面返回
	public static final short C_YABIAO_MENU_REQ = 3403;//请求选马界面
	public static final short S_YABIAO_MENU_RESP = 3404;//请求选马界面返回
	public static final short C_SETHORSE_REQ = 3405;//请求设置马匹
	public static final short S_SETHORSE_RESP = 3406;//请求设置马匹返回
	public static final short C_YABIAO_REQ = 3407;//请求开始押镖
	public static final short S_YABIAO_RESP = 3408;//请求开始押镖返回
	public static final short C_BUYXUEPING_REQ= 3409;//请求买血拼
	public static final short S_BUYXUEPING_RESP= 3410;//请求买血拼返回
	public static final short C_ENTER_YABIAOSCENE = 3411;//请求进入押镖场景
	public static final short C_ENTER_JBBATTLE_REQ = 3412;//请求劫镖返回
	public static final short S_BIAOCHE_INFO_RESP = 3413;//推送镖车信息
	public static final short S_BIAOCHE_STATE = 3417;//推送镖车战斗状态
	public static final short C_BIAOCHE_INFO = 3418;//请求镖车信息
	public static final short C_ZHANDOU_INIT_YB_REQ = 3419;//请求战斗配置
	public static final short C_YABIAO_RESULT = 3420;//请求战斗结算
	public static final short S_ZHANDOU_INIT_ERROR = 3421;//请求战斗错误返回
	public static final short S_YABIAO_ENTER_RESP = 3422;//有新的押镖者进入场景
	public static final short C_YABIAO_ENEMY_RSQ = 3423;//请求押镖仇人
	public static final short S_YABIAO_ENEMY_RESP = 3424;//请求押镖仇人返回
	public static final short C_YABIAO_BUY_RSQ = 3425;//请求够买押镖相关次数
	public static final short S_YABIAO_BUY_RESP = 3426;//请求够买押镖相关次数返回
	public static final short C_YABIAO_HISTORY_RSQ = 3427;//请求押镖历史
	public static final short S_YABIAO_HISTORY_RESP = 3428;//请求押镖历史返回
	public static final short C_YABIAO_HELP_RSQ = 3429;//请求押镖协助
	public static final short S_YABIAO_HELP_RESP = 3430;//请求押镖协助返回
	public static final short C_ANSWER_YBHELP_RSQ = 3431;//答复押镖协助
	public static final short S_ANSWER_YBHELP_RESP = 3432;//答复押镖协助返回
	public static final short C_TICHU_YBHELP_RSQ = 3433;//踢出押镖协助
	public static final short S_TICHU_YBHELP_RESP = 3434;//踢出押镖协助返回
	public static final short S_ASK_YABIAO_HELP_RESP = 3435;//答复请求押镖协助返回 
	public static final short C_YABIAO_MOREINFO_RSQ = 3436;//请求押镖某数据
	public static final short S_YABIAO_MOREINFO_RESP = 3437;//请求押镖某数据返回 
	public static final short S_TICHU_YBHELPXZ_RESP = 3438;//踢出押镖协助者给协助者返回
	public static final short S_PUSH_YBRECORD_RESP = 3439;//推送押镖战斗记录
	public static final short C_BUYHORSEBUFF_REQ = 3440;//请求购买马车buff
	public static final short S_BUYHORSEBUFF_RESP = 3441;//请求购买马车buff返回
	public static final short C_GETMABIANTYPE_REQ = 3442;//请求马鞭类型
	public static final short S_GETMABIANTYPE_RESP = 3443;//请求马鞭类型返回
	public static final short C_CARTJIASU_REQ = 3444;//请求镖车加速
	public static final short S_CARTJIASU_RESP = 3445;//请求镖车加速返回
	public static final short C_YABIAO_XIEZHUS_REQ = 3446;//请求协助君主列表
	public static final short S_YABIAO_XIEZHUS_RESP = 3447;//请求协助君主列表返回
	public static final short S_MengYouKuaiBao_PUSH=3448;	//盟友快报推送
	public static final short C_CHECK_YABIAOHELP_RSQ = 3449;//请求是否可以显示协助
	public static final short S_CHECK_YABIAOHELP_RESP = 3450;//请求是否可以显示协助返回 
	public static final short S_GAIN_YABIAO_FULI_RESP = 3451;//请求领取押镖 福利次数返回
	public static final short C_MOVE2BIAOCHE_REQ = 3452;//请求移动到镖车
	public static final short S_YABIAO_ENEMY_4_SIGN_RESP = 3453;//请求仇人信息返回（给客户端进行仇人标记用）
	/*========== 游侠战斗 ================*/ 
	/**
	 * 游侠战斗请求
	 */
	public static final short C_YOUXIA_INIT_REQ = 601;
	
	/**
	 * 游侠战斗请求返回
	 */
	public static final short S_YOUXIA_INIT_RESP = 602;
	
	/**
	 * 游侠战斗结果
	 */
	public static final short C_YOUXIA_BATTLE_OVER_REQ = 603;
	
	/**
	 * 游侠战斗结果返回
	 */
	public static final short S_YOUXIA_BATTLE_OVER_RESP = 604;
	
	/**
	 * 游戏玩法信息请求
	 */
	public static final short C_YOUXIA_INFO_REQ = 605;
	
	/**
	 * 游戏玩法信息请求返回
	 */
	public static final short S_YOUXIA_INFO_RESP = 606;
	
	public static final short C_YOUXIA_TIMES_INFO_REQ = 607;
	
	public static final short S_YOUXIA_TIMES_INFO_RESP = 608;
	
	public static final short C_YOUXIA_TIMES_BUY_REQ = 609;
	
	public static final short S_YOUXIA_TIMES_BUY_RESP = 610;
	
	public static final short C_YOUXIA_SAO_DANG_REQ = 611;

	public static final short S_YOUXIA_SAO_DANG_RESP = 612;
	
	public static final short C_YOUXIA_GUANQIA_REQ = 613;
	
	public static final short S_YOUXIA_GUANQIA_RESP = 614;
	
	public static final short C_YOUXIA_TYPE_INFO_REQ = 615;

	public static final short S_YOUXIA_TYPE_INFO_RESP = 616;
	public static final short C_YOUXIA_CLEAR_COOLTIME = 617;
	public static final short S_YOUXIA_CLEAR_COOLTIME_RESP = 618;
	/**限时活动**/
	public static final short C_XINSHOU_XIANSHI_INFO_REQ = 4001;//请求新手限时活动界面
	public static final short S_XINSHOU_XIANSHI_INFO_RESP = 4002;//请求新手限时活动界面返回
	public static final short C_XINSHOU_XIANSHI_AWARD_REQ = 4003;//请求领取新手限时活动奖励
	public static final short S_XINSHOU_XIANSHI_AWARD_RESP = 4004;//请求领取新手限时活动奖励返回
	public static final short C_XIANSHI_INFO_REQ = 4005;//请求限时活动界面
	public static final short S_XIANSHI_INFO_RESP = 4006;//请求限时活动界面返回
	public static final short C_XIANSHI_AWARD_REQ = 4007;//请求领取限时活动奖励
	public static final short S_XIANSHI_AWARD_RESP = 4008;//请求领取限时活动奖励返回	
	public static final short C_XIANSHI_REQ = 4009;//请求可开启的限时活动(首日/七日)
	public static final short S_XIANSHI_RESP = 4034;//请求可开启的限时活动(首日/七日)返回
	public static final short C_FULIINFO_REQ = 4020;//请求福利信息
	public static final short S_FULIINFO_RESP = 4021;//请求福利信息返回
	public static final short C_FULIINFOAWARD_REQ = 4030;//请求福利奖励
	public static final short S_FULIINFOAWARD_RESP = 4031;//请求福利奖励返回
	public static final short C_HONGBAONFO_REQ = 4032;//请求红包福利信息
	public static final short S_HONGBAONFO_RESP = 4033;//请求红包福利信息返回
	
	public static final short BAO_XIANG_PICKED_INFO = 4055;
	public static final short OPEN_ShiLian_FuBen = 4101;
	
	public static final short FIGHT_ATTACK_REQ = 4103;
	public static final short FIGHT_ATTACK_RESP = 4104;
	
	public static final short Life_Change = 4106;

	public static final short C_GET_BAO_XIANG = 4113;
	
	public static final short C_CHECK_CHARGE = 4901;
	
	/**公告**/
	public static final short C_GET_VERSION_NOTICE_REQ = 5001;//请求版本公告
	public static final short S_GET_VERSION_NOTICE_RESP = 5002;//请求版本公告返回
	
	public static final short C_GET_CUR_CHENG_HAO = 5101;
	public static final short S_GET_CUR_CHENG_HAO = 5102;
	public static final short C_LIST_CHENG_HAO = 5111;
	public static final short S_LIST_CHENG_HAO = 5112;
	public static final short C_USE_CHENG_HAO = 5121;
	public static final short DuiHuan_CHENGHAO = 5122;
	
	public static final short C_GET_UPACTION_DATA = 5131;
	public static final short S_UPACTION_DATA_0 = 5132;
	public static final short S_UPACTION_DATA_1 = 5133;
	public static final short S_UPACTION_DATA_2 = 5134;
	public static final short S_UPACTION_DATA_3 = 5135;
	
	//	捐献贡金
	public static final short C_GET_JUANXIAN_GONGJIN_REQ = 6001;//请求捐献贡金 
	public static final short S_GET_JUANXIAN_GONGJIN_RESP = 6002;//请求捐献贡金返回
	public static final short C_GET_JUANXIAN_DAYAWARD_REQ = 6007;//请求捐献贡金 日奖励
	public static final short S_GET_JUANXIAN_DAYAWARD_RESP = 6008;//请求捐献贡金日奖励返回
	// 国家主页
	public static final short GUO_JIA_MAIN_INFO_REQ = 6003;
	public static final short GUO_JIA_MAIN_INFO_RESP = 6004;
	public static final short GET_DAILY_RANK_AWARD_REQ = 6005;
	public static final short GET_DAILY_RANK_AWARD_RESP = 6006;
//	public static final short C_ISCAN_JUANXIAN_REQ = 6009;//请求是否可以捐献贡金 废弃
//	public static final short S_ISCAN_JUANXIAN_RESP = 6010;//是否可以捐献贡金返回
	
	public static final short RANKING_ALLIANCE_PLAYER_REQ=7001;// 联盟榜成员列表
	public static final short RANKING_ALLIANCE_PLAYER_RESP=7002;// 联盟榜成员列表返回
	public static final short GET_RANK_REQ=7003;// 请求名次
	public static final short GET_RANK_RESP=7004;// 请求名次返回
	
	/**符文**/
	public static final short C_QUERY_FUWEN_REQ=8001;// 请求符文主页信息
	public static final short S_QUERY_FUWEN_RESP=8002;// 返回符文主页信息
	public static final short C_OPERATE_FUWEN_REQ=8003;// 请求操作符文
	public static final short S_OPERATE_FUWEN_RESP=8004;// 返回操作符文结果
	public static final short C_LOAD_FUWEN_IN_BAG = 8005;		// 加载背包中的符文信息
	public static final short S_LOAD_FUWEN_IN_BAG_RESP = 8006;	// 返回背包中的符文信息
	public static final short C_FUWEN_RONG_HE = 8007;		
	public static final short S_FUWEN_RONG_HE_RESP = 8008;	
	public static final short C_FUWEN_DUI_HUAN = 8009;		
	public static final short S_FUWEN_DUI_HUAN_RESP = 8010;	
	public static final short C_FUWEN_EQUIP_ALL = 8011;		
	public static final short S_FUWEN_EQUIP_ALL_RESP = 8012;	
	public static final short C_FUWEN_UNLOAD_ALL = 8013;		
	public static final short S_FUWEN_UNLOAD_ALL_RESP = 8014;	
	
	/** 查看指定君主信息 */
	public static final short JUNZHU_INFO_SPECIFY_REQ = 23067;
	public static final short JUNZHU_INFO_SPECIFY_RESP = 23068;
	
	/**主页部分功能简单信息请求*/
	public static final short mainSimpleInfoReq = 23069;
	public static final short mainSimpleInfoResp = 23070;
	
	public static final short ENTER_FIGHT_SCENE = 22007;
	public static final short ENTER_FIGHT_SCENE_OK = 22008;
	
	/** 离开联盟战 */
	public static final short EXIT_FIGHT_SCENE = 22006;
	/** 请求联盟战信息 */
	public static final short ALLIANCE_FIGHT_INFO_REQ = 4201;
	/** 联盟站信息返回 */
	public static final short ALLIANCE_FIGHT_INFO_RESP = 4202;
	/** 联盟战报名 */
	public static final short ALLIANCE_FIGHT_APPLY = 4203;
	/** 报名结果返回 */
	public static final short ALLIANCE_FIGHT_APPLY_RESP = 4204;
	/** 请求联盟战战场信息 */
	public static final short ALLIANCE_BATTLE_FIELD_REQ = 4205;
	/** 返回联盟战战场信息 */
	public static final short ALLIANCE_BATTLE_FIELD_RESP = 4206;
	/** 联盟战有人死亡通知 */
	public static final short ALLIANCE_FIGHT_PLAYER_DEAD = 4207;
	/** 联盟战有人复活通知 */
	public static final short ALLIANCE_FIGHT_PLAYER_REVIVE = 4208;
	/** 联盟战历史战况请求 */
	public static final short ALLIANCE_FIGHT_HISTORY_REQ = 4209;
	/** 联盟战历史战况结果返回 */
	public static final short ALLIANCE_FIGHT_HISTORY_RESP = 4210;
	/** 联盟战上届排名请求 */
	public static final short ALLIANCE_FIGTH_LASTTIME_RANK = 4211;
	/** 联盟战上届排名返回 */
	public static final short ALLIANCE_FIGTH_LASTTIME_RANK_RESP = 4212;
	/** 联盟战战场信息消息推送 */
	public static final short ALLIANCE_BATTLE_FIELD_NOTIFY = 4214;
	/** 联盟战战斗结果返回*/
	public static final short ALLIANCE_BATTLE_RESULT = 4216;
	/** buffer信息*/
	public static final short BUFFER_INFO = 4217;
	/** 玩家请求复活 */
	public static final short PLAYER_REVIVE_REQUEST = 4218;
	/** 安全区回血操作 */
	public static final short SAFE_AREA_BOOLD_RETURN = 4219;
	/** 安全区回血操作返回 */
	public static final short SAFE_AREA_BOOLD_RETURN_RESP = 4222;
	
	

	/**红点推送通知协议号*/
	public static final short RED_NOTICE = 4220;
	public static final short FUSHI_RED_NOTICE = 4221;
	public static final short FUNCTION_OPEN_NOTICE = 4223;
	
	/**CDKey**/
	public static final short C_GET_CDKETY_AWARD_REQ = 4230;
	public static final short S_GET_CDKETY_AWARD_RESP = 4231;
	/**盟友快报*/
	public static final short C_MengYouKuaiBao_Req=4240;//请求盟友快报
	public static final short S_MengYouKuaiBao_Resq=4241;	//请求盟友快报返回
	public static final short Prompt_Action_Req = 4242; //快报中的行为请求
	public static final short Prompt_Action_Resp = 4243; //快报中行为请求返回
	/*
	 * 联盟军情
	 */
	public static final short alliance_junQing_req = 26075;
	public static final short alliance_junQing_resq = 26076;
	public static final short qu_zhu_battle_end_req = 26077; // 驱逐战斗结束
	public static final short qu_zhu_battle_end_resp = 26080; // 驱逐战斗结束返回
	public static final short go_qu_zhu_req = 26078; // 驱逐
	public static final short go_qu_zhu_resp = 26079; // 驱逐返回
	/**技能培养**/
	public static final short C_GET_JINENG_PEIYANG_QUALITY_REQ=4250;
	public static final short S_GET_JINENG_PEIYANG_QUALITY_RESP=4251;
	public static final short C_UPGRADE_JINENG_REQ=4252;
	public static final short S_UPGRADE_JINENG_RESP=4253;
	
	public static final short C_BUY_REVIVE_TIMES_REQ = 4254;
	public static final short S_BUY_REVIVE_TIMES_RESP = 4255;
	/**打招呼**/
	public static final short C_GREET_REQ = 5000;//向某人打招呼请求
	public static final short S_GREET_RESP = 5011;//向某人打招呼返回
	public static final short S_GREETEDANSWER_RESP = 5012;//打招呼的人收到的对方反馈信息

	public static final short S_GREET_ANSWER_RESP = 5015;//打招呼的人收到的答复
	public static final short S_INVITE_RESP = 5017;//邀请某人加入联盟请求返回
	
	public static final short weiWang = 5020;
	public static final short huangyeBi = 5021;
	public static final short gongxun = 5022;
	
	public static final short MODEL_INFO = 5031;
	public static final short CHANGE_MODEL= 5032;
	public static final short UNLOCK_MODEL= 5033;
	public static final short BD_CHANGE_MODEL= 5034;
	
	/** 重楼信息请求 **/
	public static final short CHONG_LOU_INFO_REQ = 5041;				
	/** 重楼信息返回 **/
	public static final short CHONG_LOU_INFO_RESP = 5042;
	/** 重楼扫荡 **/
	public static final short CHONG_LOU_SAO_DANG_REQ = 5043;
	/** 重楼扫荡返回 **/
	public static final short CHONG_LOU_SAO_DANG_RESP = 5044;
	/** 进入重楼战斗初始化 **/
	public static final short CHONG_LOU_BATTLE_INIT = 5045;
	/** 重楼战斗结果报告**/
	public static final short CHONG_LOU_BATTLE_REPORT = 5047;
	/** 重楼战斗结果返回**/
	public static final short CHONG_LOU_BATTLE_REPORT_REQP = 5048;
	
	/** 请求猎符操作信息页面 **/
	public static final short LieFu_Action_Info_Req = 5061;
	/** 请求猎符操作信息页面返回 **/
	public static final short LieFu_Action_Info_Resp = 5062;
	/** 执行猎符操作 **/
	public static final short LieFu_Action_req = 5063;
	/** 执行猎符操作返回 **/
	public static final short LieFu_Action_Resp = 5064;
		
	/**首冲页面详情请求*/
	public static final short ACTIVITY_FIRST_CHARGE_REWARD_REQ = 7011;
	/**首冲页面详情请求返回*/
	public static final short ACTIVITY_FIRST_CHARGE_REWARD_RESP = 7012;
	/**首冲页面领奖请求*/
	public static final short ACTIVITY_FIRST_CHARGE_GETREWARD_REQ = 7013;
	/**首冲页面领奖请求返回*/
	public static final short ACTIVITY_FIRST_CHARGE_GETREWARD_RESP = 7014;
	/**月卡请求*/
	public static final short ACTIVITY_MONTH_CARD_INFO_REQ= 7015;
	/**月卡请求返回*/
	public static final short ACTIVITY_MONTH_CARD_INFO_RESP = 7016;
	/**月卡领奖*/
	public static final short ACTIVITY_MONTH_CARD_REWARD_REQ = 7017;
	/**月卡领奖返回*/
	public static final short ACTIVITY_MONTH_CARD_REWARD_RESP = 7018;
	/**成长基金请求*/
	public static final short ACTIVITY_GROWTHFUND_INFO_REQ = 7019;
	/**成长基金请求返回*/
	public static final short ACTIVITY_GROWTHFUND_INFO_RESP = 7020;
	/**成长基金领奖请求*/	
	public static final short ACTIVITY_GROWTHFUND_GETREWARD_REQ = 7021;
	/**成长基金领奖请求返回*/
	public static final short ACTIVITY_GROWTHFUND_GETREWARD_RESP = 7022;
	/**购买成长基金请求*/
	public static final short ACTIVITY_GROWTHFUND_BUY_REQ = 7023;
	/**购买成长基金请求返回*/
	public static final short ACTIVITY_GROWTHFUND_BUY_RESP = 7024;
	/**体力详情请求*/
	public static final short ACTIVITY_STRENGTH_INFO_REQ = 7025;
	/**体力详情请求返回*/
	public static final short ACTIVITY_STRENGTH_INFO_RESP= 7026;
	/**体力领取请求*/
	public static final short ACTIVITY_STRENGTH_GET_REQ = 7027;
	/**体力领取请求返回*/
	public static final short ACTIVITY_STRENGTH_GET_RESP = 7028;
	/**领取冲级奖励*/
	public static final short ACTIVITY_LEVEL_GET_REQ = 7031;
	/**领取返回*/
	public static final short ACTIVITY_LEVEL_GET_RESP = 7032;
	/**冲级奖励请求*/
	public static final short ACTIVITY_LEVEL_INFO_REQ = 7029;
	/**冲级奖励请求返回*/
	public static final short ACTIVITY_LEVEL_INFO_RESP = 7030;
	/**活动列表请求*/
	public static final short ACTIVITY_FUNCTIONLIST_INFO_REQ = 7037;
	/**活动列表请求返回*/
	public static final short ACTIVITY_FUNCTIONLIST_INFO_RESP= 7038;
	/**成就奖励请求*/
	public static final short C_ACTIVITY_ACHIEVEMENT_INFO_REQ = 7033;
	/**成就奖励请求返回*/
	public static final short S_ACTIVITY_ACHIEVEMENT_INFO_RESP= 7034;
	/**领取成就奖励*/
	public static final short C_ACTIVITY_ACHIEVEMENT_GET_REQ = 7035;
	/**领取返回*/
	public static final short S_ACTIVITY_ACHIEVEMENT_GET_RESP = 7036;
	
}
