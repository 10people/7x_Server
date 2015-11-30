package com.qx.gm;

/**
 * @ClassName: GMPD
 * @Description: 根据接口文档定义的GM协议号
 * @author 何金成
 * @date 2015年7月4日 下午6:13:37
 * 
 */
public class GMPD {
	/** 通用查询类接口（IDIP_QUERY_COMMON） **/
	public static final int IDIP_QUERY_ROLEINFO_REQ = 111321;// 玩家信息查询
	public static final int IDIP_QUERY_ROLELSTATUSE_REQ = 111322;// 角色状态查询
	/** 通用更新类接口（IDIP_UPDATE_COMMON） **/
	public static final int IDIP_DO_BAN_USR_SPEAK_REQ = 120002;// 禁言处理请求
	public static final int IDIP_DO_BAN_USR_REQ = 120003;// 封号处理请求
	public static final int IDIP_DO_BAN_USR_DOWN_REQ = 120009;// 踢玩家下线
	public static final int IDIP_DO_LIFTBAN_USR_SPEAK_REQ = 120005;// 解除禁言请求
	public static final int IDIP_DO_LIFTBAN_USR_REQ = 120006;// 解除封号请求
	/** MMOG专属更新类接口(IDIP_UPDATE_MMOG) **/
	public static final int IDIP_DO_SEND_MAIL_REQ = 120001;// 发送带附件邮件请求（邮件发道具）
	public static final int IDIP_DO_SEND_BARE_MAIL_REQ = 120007;// 发送不带附件邮件请求
	public static final int IDIP_DO_SEND_TEST_CODE_REQ = 120012;// 申请福利之验证信息
	public static final int IDIP_DO_SEND_WELFARE_REQ = 120013;// 申请福利请求
	public static final int IDIP_DO_SERVR_STATE_REQ = 120014;// 功能开关之单独功能服务器状态
	public static final int IDIP_DO_FUNC_SWITCH_REQ = 120015;// 功能开关设置
	/** 扩展更新类接口(IDIP_UPDATE_EXTEND) **/
	public static final int IDIP_DO_SEND_GM_NOTICE_REQ = 120004;// 发送系统公告信息请求
	public static final int IDIP_QUERY_VERSION_REQ = 120008;// 发送版本公告信息请求
	public static final int IDIP_DEL_VERSION_REQ = 120010;// 版本公告删除
	public static final int IDIP_DO_DEL_GM_NOTICE_REQ = 120011;// 系统公告删除
	/** 活动管理接口（IDIP_ACTIVITY） **/
	public static final int IDIP_ACTIVITY_COMPENSATION = 120028;// 全服补偿配置
	public static final int IDIP_ACTIVITY_TIMEDLOGIN = 120029;// 限时登录配置
	public static final int IDIP_ACTIVITY_TOPUP = 120030;// 累计充值配置
	public static final int IDIP_ACTIVITY_CONSUME = 120031;// 累计消费配置
	public static final int IDIP_ACTIVITY_TOPUP_RANK = 120032;// 充值排行配置
	public static final int IDIP_ACTIVITY_CONSUME_RANK = 120033;// 消费排行配置
	public static final int IDIP_ACTIVITY_INNER = 120034;// 内置活动配置
	public static final int IDIP_ACTIVITY_DEL = 120035;// 活动删除请求

}
