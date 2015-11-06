package com.qx.gm.util;

/**
 * @ClassName: CodeUtil
 * @Description: GM返回码
 * @author 何金成
 * @date 2015年7月2日 上午10:57:35
 * 
 */
public class CodeUtil {
	public static final int SUCCESS = 100;// 成功
	public static final int NONE_INFO = 101;// 没有输入查找信息
	public static final int NONE_ACCOUNT = 102;// 没有账号
	public static final int NONE_JUNZHU = 103;// 没有君主
	public static final int JUNZHU_OFFLINE = 104;// 君主不在线
	public static final int ALREADY_FENGTING = 105;// 已经处于封停状态
	public static final int ALREADY_JINYAN = 106;// 已经处于禁言状态
	public static final int EMAIL_FAILED = 107;// 邮件发送失败
	public static final int SPLIT_ERROR = 108;// 用错了分隔符
	public static final int FUJIN_COUNT_ERROR = 109;// 邮件附件的数量不能为0

	public static final int DB_ERROR = 905;// 数据库错误
	public static final int PARAM_ERROR = 906;// 参数错误
	public static final int UNKNOW_ERROR = 907;// 未知错误
	public static final int MD5_ERROR = 908;// MD5验证不对
	public static final int NOT_DEFINE_CODE = 909;// 未定义的协议号
	public static final String MD5_KEY = "youxigu123";// MD5秘钥
}
