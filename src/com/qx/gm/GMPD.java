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
	public static final int IDIP_QUERY_ROLEINFO_REQ = 111321;
	public static final int IDIP_QUERY_ROLELSTATUSE_REQ = 111322;
	/** 通用更新类接口（IDIP_UPDATE_COMMON） **/
	public static final int IDIP_DO_BAN_USR_SPEAK_REQ = 120002;
	public static final int IDIP_DO_BAN_USR_REQ = 120003;
	public static final int IDIP_DO_BAN_USR_DOWN_REQ = 120009;
	public static final int IDIP_DO_LIFTBAN_USR_SPEAK_REQ = 120005;
	public static final int IDIP_DO_LIFTBAN_USR_REQ = 120006;
	/** MMOG专属更新类接口(IDIP_UPDATE_MMOG) **/
	public static final int IDIP_DO_SEND_MAIL_REQ = 120001;
	public static final int IDIP_DO_SEND_BARE_MAIL_REQ = 120007;
	/** 扩展更新类接口(IDIP_UPDATE_EXTEND) **/
	public static final int IDIP_DO_SEND_GM_NOTICE_REQ = 120004;
	public static final int IDIP_DO_DEL_GM_NOTICE_REQ = 120011;
	// public static final int IDIP_QUERY_VERSION_REQ = 120008;
	/** 行为查询接口（IDIP_OPREATE_EXTEND） **/
	public static final int IDIP_OPREATE_TOPUP_REQ = 15001;
	public static final int IDIP_OPREATE_CONSUME_REQ = 15002;
	public static final int IDIP_OPREATE_LOG_REQ = 15003;

}
