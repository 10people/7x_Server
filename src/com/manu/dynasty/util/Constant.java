/**
 * 
 */
package com.manu.dynasty.util;

import java.net.InetAddress;

/**
 * 腾讯接口相关参数
 * 
 * @author zhaiyong
 * 
 */
public class Constant {

	public static String APP_ID = null;
	public static String APP_KEY = null;
	// 大区ID
	public static String AREA_ID = null;
	// 电脑管家查询接口分配参数
	public static String SRC_ID = "30002";
	// 电脑管家接口参数
	public static String QQ_MGR_VER = "2318";
	// 游戏服务器main sever
	public static String SVR_IP = null;
	// 开放平台sever地址
	public static String SERVER_NAME = "119.147.19.43";// 这是测试地址。正式环境：openapi.tencentyun.com
	// 游戏联盟上报地址
	public static String UNION_SERVER_NAME = "http://union.tencentlog.com/cgi-bin/Register.cgi";
	// 货物图片服务器
	public static String GOODS_SERVER_NAME = "119.147.19.43";
	// 货物图片格式
	public static String GOODS_PIC_TYPE = ".jpg";
	// 开平返回信息格式
	public static String FORMAT_JSON = "json";
	// 分区发货配置中的分区id，应用不分区则输入0
	//public static String ZONE_ID = null;
	// 联盟交易佣金
	public static String FEE = null;

	public static Boolean USE_OP_TRANS;// 是否使用开放平台交易

	public static final String APP_NAME = "app100635048";
	// 腾讯各种开放平台,根据开放平台的更新会有调整
	//public static final String PF_ALL="all";//全部
	public static final String PF_QZONE = "qzone"; // 'QQ空间' ,1
	public static final String PF_PENGYOU = "pengyou";// 腾讯朋友,2
	public static final String PF_TAPP = "tapp";// 微博,3
	public static final String PF_QPLUS = "qplus";// Q+ ,4
	public static final String PF_QQGAME = "qqgame";// QQ游戏,10
	public static final String PF_3366 = "3366";// 11
	public static final String PF_MAIN = "website";// 游戏官网,12
	public static final String PF_IOS = "iOS";// 13
	public static final String PF_ANDROID = "Android";// 14

	public static final String PF_MANYOU = "manyou";// 漫游 ,15
	public static final String PF_KAPP = "kapp";// 开心 ,101
	public static final String PF_UNION = "union";// 游戏联盟 ,17

	public static final String PAY_FIRST_RECHARGE = "PAY_FIRST_RECHARGE";// 首次充值状态

	public static final boolean taskDebug = false;

	public static final String OPENAPI_VERSION = "v3";

	public static String firstRechargeDiscountId = null; //首冲礼包id
	
	public static String blueYearId = null; //年费蓝钻礼包id
	
	public static String blueMonthId = null; //月费蓝钻礼包
	
	public static String qqBlueOpenId = null; //蓝钻开通礼包
	public static String qqYellowOpenId = null; //黄钻开通礼包
	public static String qqVipOpenId = null; //会员开通礼包	

	// (1,'QQ空间')
	// (2,'腾讯朋友')
	// (3,'腾讯微博')
	// (4,'腾讯QPlus')
	// (8,'手机空间')
	// (9,'手机朋友')
	// (10,'QQ游戏')
	// (11,'3366')
	// (12,'游戏官网')
	// (13,'iOS')
	// (14,'Android')
	// (15,'漫游')
	// (16,'游戏人生')
	// (17,'腾讯游戏联盟')
	// (101,'开心网')

	// 获取用户信息
	public static final String CGI_USER_INFO = "/v3/user/get_info";
	// 获取登陆用户vip信息
	public static final String CGI_USER_VIP = "/v3/user/total_vip_info";
	// 获取黄钻vip信息
	public static final String CGI_IS_VIP = "/v3/user/is_vip";
	// 验证登陆状态
	public static final String CGI_IS_LOGIN = "/v3/user/is_login";
	// 验证是否公益月捐用户
	public static final String CGI_IS_MONTHLY_USER = "/v3/user/is_monthlyuser";
	// 验证用户是否从选区登陆
	public static final String CGI_IS_AREA_LOGIN = "/v3/user/is_area_login";
	// 验证好友邀请
	public static final String CGI_VERIFY_INVKEY = "/v3/spread/verify_invkey";
	// 获取交易token
	public static final String CGI_BUY_GOODS = "/v3/pay/buy_goods";
	// 获取道具交易token
	public static final String CGI_EXCHANGE_GOODS = "/v3/pay/exchange_goods";
	// 应用发货通知
	public static final String CGI_PAY_CONFIRM_DELIVERY = "/v3/pay/confirm_delivery";
	// QQ提醒：判断用户是否安装/开启了“qq提醒”
	public static final String CGI_IS_REMINDER = "/v3/spread/is_reminder_set";
	// QQ提醒：发送qq提醒
	public static final String CGI_SET_REMINDER = "/v3/spread/set_reminder_set";
	// 查询游戏币余额
	public static final String CGI_PAY_GET_BALANCE = "/v3/pay/get_balance";
	// 赠送游戏币
	public static final String CGI_PAY_PRESENT = "/v3/pay/send_present";
	// 发送公会聊天内容到qq群
	public static final String CGI_QQGROUP_SEND_MSG = "/v3/qqgroup/send_msg";
	// 设置群成员名片
	public static final String CGI_QQGROUP_SET_NAMECARD = "/v3/qqgroup/set_namecard";
	// 解绑定联盟的qq群
	public static final String CGI_UNBIND_QQGROUP = "/v3/qqgroup/unbind_qqgroup";
	// 查询是否为群成员
	public static final String CGI_IS_QQGROUP_MEMBER = "/v3/qqgroup/is_group_member";
	
	public static final String CGI_TASK_MARKET = "/v3/pay/inform_task_completed";
	// 发送微博
	public static final String CGI_WEIBO_SEND = "/v3/t/add_t";

	// 微博收听
	public static final String CGI_WEIBO_IDOL = "/v3/relation/add_idol";

	// 获取领取礼包的token
	public static final String CGI_GET_TOKEN = "/v3/pay/get_token";
	
	//查询应用是否添加到qq主面板
	public static final String CGI_IS_APP_ONPANEL = "/v3/spread/is_app_onpanel";

	public static final String CGI_CSEC_QUERY = "v3/csec/punish_query";
	public static final String CGI_CSEC_QUERY_TYPE_0 = "0";// 0表示游戏不需要对这个用户进行深入打击
	public static final String CGI_CSEC_QUERY_TYPE_1 = "1"; // 1表示限制登录
	public static final String CGI_CSEC_QUERY_TYPE_2 = "2"; // 2表示禁止发言

	// qqgame
	public static final String CGI_QQGAME_SHARE = "/v3/spread/set_request"; // 发送游戏事件
	public static final String CGI_QGAME_FRIEND = "/v3/relation/get_app_friends"; // 获取qqgame关系链，包括im及qqgame好友
	public static final String CGI_QGAME_FEED = "/v3/spread/set_feed"; //
	public static final String CGI_QGAME_REQUEST = "/v3/spread/set_request"; //
	public static final String CGI_QGAME_LIMITATION = "/v3/spread/get_limitation";// 获取应用发送feed/request当前值
	public static final String CGI_QGAME_NOTIFICATION = "/v3/message/send_notification";// 向玩家发送游戏通知
	public static final String CGI_QGAME_FIGURE = "/v3/user/get_figure";// 获取当前用户自定义头像

	public static final String PROTOCOL_HTTP = "http";
	public static final String PROTOCOL_HTTPS = "https";
	// /public static final String USER_IP = "127.0.0.1";

	// 发送请求时，开平javaSDK目前不支持get方法。发送请求是统一使用post
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";

	// -----------------------罗盘数据上报cgi-----------------------------------------------
	public static Boolean isReport = true;
	// 通用cgi
	public static final String CMP_REPORT = "http://tencentlog.com/stat/report.php";
	// 用户登陆
	public static final String CMP_LOGIN = "http://tencentlog.com/stat/report_login.php";
	// 用户注册
	public static final String CMP_REG = "http://tencentlog.com/stat/report_register.php";
	// 接受邀请
	public static final String CMP_ACCEPT = "http://tencentlog.com/stat/report_accept.php";
	// 邀请他人注册
	public static final String CMP_INVITE = "http://tencentlog.com/stat/report_invite.php";
	// 支付消费
	public static final String CMP_CONSUME = "http://tencentlog.com/stat/report_consume.php";
	// 充值支付
	public static final String CMP_RECHARE = "http://tencentlog.com/stat/report_recharge.php";
	// 退出
	public static final String CMP_QUIT = "http://tencentlog.com/stat/report_quit.php";
	// 上报在线人数
	public static final String CMP_ONLINE = "http://tencentlog.com/stat/report_online.php";

	// -------------------------------密友成就--------------------------------------------
	// 密友君主等级
	public static final int CHUM_ACHIEVE_LEVEL = 1;
	// 密友千重楼级别
	public static final int CHUM_ACHIEVE_MANSIONLEVEL = 2;
	// 密友城池等级
	public static final int CHUM_ACHIEVE_CITYLEVEL = 3;
	// 密友支付
	public static final int CHUM_ACHIEVE_PAY = 4;
	// 密友至尊等级
	public static final int CHUM_ACHIEVE_VIPLEVEL = 5;

	// -----------------------------任务集市类型--------------------------------------------
	public static final String QQ_TASK_USERLV = "userlv";

	public static final String QQ_TASK_CASLV = "caslv";

	public static final String QQ_TASK_TOWERLV = "towerlv";

	public static final int QQ_UNION_VERSION = 1; // 腾讯游戏联盟版本

	public static final int QQ_COMPASS_VERSION = 1; // 罗盘上报版本

	public static final String QQ_UNION_LOG_TYPE = "UNION"; // 腾讯游戏联盟tlog类型

	public static final String QQ_COMPASS_LOG_TYPE = "COMPASS"; // 罗盘上报tlog类型

	public void setUseOpenPlatformTrans(boolean useOpenPlatformTrans) {
		USE_OP_TRANS = useOpenPlatformTrans;
	}

	public void setAppId(String aPPID) {
		if (aPPID == null) {
			return;
		}
		aPPID = aPPID.trim();
		if (aPPID.length() != 0) {
			APP_ID = aPPID;
		}
	}

	public void setAppKey(String aPPKEY) {
		APP_KEY = aPPKEY;
	}

	// public void setAppurl(String sERVERNAME) {
	// SERVER_NAME = sERVERNAME;
	// }

	public void setAreaId(String areaId) {
		AREA_ID = areaId;
	}

	public void setSvrIp(String svrIp) {
		SVR_IP = svrIp;
	}

	public void setServerName(String serverName) {
		if (serverName == null) {
			return;
		}
		serverName = serverName.trim();
		if (serverName.length() != 0) {
			SERVER_NAME = serverName;
		}
	}

	public void setGoodsServerName(String goodsServerName) {
		GOODS_SERVER_NAME = goodsServerName;
	}

	public void setGoodsPicType(String goodsPicType) {
		GOODS_PIC_TYPE = goodsPicType;
	}

	/**
	 * @param format_json
	 *            the fORMAT_JSON to set
	 */
	public static void setFORMAT_JSON(String format_json) {
		FORMAT_JSON = format_json;
	}

	public static int getPFIntValue(String platform) {
		int domain = 0;
		if (null != platform) {
			String pf = platform.trim();
			if (pf.equalsIgnoreCase(PF_QZONE)) {
				domain = 1;
			} else if (pf.equalsIgnoreCase(PF_PENGYOU)) {
				domain = 2;
			} else if (pf.equalsIgnoreCase(PF_TAPP)) {
				domain = 3;
			} else if (pf.equalsIgnoreCase(PF_QPLUS)) {
				domain = 4;
			} else if (pf.equalsIgnoreCase(PF_QQGAME)) {
				domain = 10;
			} else if (pf.equalsIgnoreCase(PF_3366)) {
				domain = 11;
			} else if (pf.equalsIgnoreCase(PF_MAIN)) {
				domain = 12;
			} else if (pf.equalsIgnoreCase(PF_IOS)) {
				domain = 13;
			} else if (pf.equalsIgnoreCase(PF_ANDROID)) {
				domain = 14;
			} else if (pf.startsWith(PF_MANYOU)) {
				domain = 15;
			} else if (pf.startsWith(PF_UNION)) {
				domain = 17;
			} else if (pf.equalsIgnoreCase(PF_KAPP)) {
				domain = 101;
			}

		}
		return domain;
	}

	// 对于游戏联盟的特殊处理
	public static String getPfEx(String pf) {
		if (pf != null) {
			if (pf.startsWith(Constant.PF_UNION)) {
				int pos = pf.lastIndexOf("*");
				if (pos >= 0) {
					pf = pf.substring(pos + 1, pf.length());
					if (pf.startsWith(Constant.PF_UNION)) {
						pf = Constant.PF_UNION;
					}
				}
			}
		}
		return pf;
	}

	/**
	 * @param isReport
	 *            the isReport to set
	 */
	public void setReport(boolean report) {
		isReport = report;
	}

//	public void setZoneId(String zoneId) {
//		ZONE_ID = zoneId;
//	}

	/**
	 * 将域名转换为ip地址
	 * 
	 * @param dns
	 * @return
	 */
	public static String dnsToIp(String dns) {
		if (dns == null) {
			return null;
		}
		String ip = null;
		try {
			InetAddress[] address = InetAddress.getAllByName(dns);
			ip = address[0].getHostAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ip;
	}

	public static String getFirstRechargeDiscountId() {
		return firstRechargeDiscountId;
	}

	public void setFirstRechargeDiscountId(String firstRechargeDiscountId) {
		Constant.firstRechargeDiscountId = firstRechargeDiscountId;
	}

	/**
	 * @return the blueYearId
	 */
	public static String getBlueYearId() {
		return blueYearId;
	}

	/**
	 * @param blueYearId the blueYearId to set
	 */
	public void setBlueYearId(String blueYearId) {
		Constant.blueYearId = blueYearId;
	}

	/**
	 * @return the blueMonthId
	 */
	public static String getBlueMonthId() {
		return blueMonthId;
	}

	/**
	 * @param blueMonthId the blueMonthId to set
	 */
	public void setBlueMonthId(String blueMonthId) {
		Constant.blueMonthId = blueMonthId;
	}

	public void setQqBlueOpenId(String qqBlueOpenId) {
		Constant.qqBlueOpenId = qqBlueOpenId;
	}

	public void setQqYellowOpenId(String qqYellowOpenId) {
		Constant.qqYellowOpenId = qqYellowOpenId;
	}

	public void setQqVipOpenId(String qqVipOpenId) {
		Constant.qqVipOpenId = qqVipOpenId;
	}

	/*****************************************************************************************
	 * 武将属性定义
	 *****************************************************************************************/
	public static final int HERO_ATTRID_INDEX = 10000;
	public static final int HERO_ATTR_NUM = 20;
	public static final int HERO_SLOT_NUM = 20; // 实际从1开始 0 空闲不用. 装备
	public static final int HERO_SKILL_NUM = 10; // 实际从1开始 0 空闲不用.技能
	public static final int HERO_SLOT_SKILL_FROM = 1;
	public static final int ITEM_SLOT_NUM = 6; // 1 是 强化 234 是 宝石镶嵌
	
	public static final int EUQIP_STATUS_ON = 1;//装备穿戴状态
	
	public static final int HEROTYPE_MONARCH_1 = 1; //君主武将类型
	public static final int HEROTYPE_MONARCH_10 = 10; //君主武将类型
	
	public static final int HERO_STAGE_COUNT = 4; //进阶次数,当等于这个的时候才能晋封
	
	
	/*  资源常量定义   */
	public static final int RESOURCE_ID_TONGBI = 900001;
	public static final int RESOURCE_ID_YUANBAO = 900002;
	public static final int RESOURCE_ID_FANRONG = 910019;
	public static final int RESOURCE_ID_SHIQI = 910021;
	
	
	
}