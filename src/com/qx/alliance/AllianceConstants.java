package com.qx.alliance;

public class AllianceConstants {
	/** 联盟状态-正常 **/
	public static final int STATUS_NORMAL = 0;
	/** 联盟状态-选举报名 **/
	public static final int STATUS_APPLY = 1;
	/** 联盟状态-投票 **/
	public static final int STATUS_VOTING = 2;
	
	/** 已经投票 **/
	public static final int VOTED_TURE = 1;
	/** 未投票 **/
	public static final int VOTED_FALSE = 0;
	/** 投票弃权 **/
	public static final int VOTED_GIVE_UP = 2;
	
	/** 已经报名 **/
	public static final int BAOMING_TRUE = 1;
	/** 未报名 **/
	public static final int BAOMING_FALSE = 0;

	/** 联盟招募需要审批 **/
	public static final int NEED_SHENPI = 0;
	/** 联盟招募不需要审批 **/
	public static final int NO_NEED_SHENPI = 1;

	/** 联盟申请最大数量 **/
	public static final int APPPLY_NUM_MAX = 3;
	
	/** 需要提示联盟选举结果 **/
	public static final int VOTE_DIALOG = 1;
	/** 不需要提示联盟选举结果 **/
	public static final int VOTE_DIALOG_NOT = 0;
	
	
	
}
