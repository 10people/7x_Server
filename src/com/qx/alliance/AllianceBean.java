package com.qx.alliance;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Alliance")
public class AllianceBean{// implements MCSupport {

	private static final long serialVersionUID = -3613633650658686833L;

	@Id
	public int id;
	public int icon;
	@Column(unique = true)
	public String name;
	public long creatorId;// 创建者君主Id
	public Date createTime;
	public int country;
	public int level;
	public int exp;// 当前经验
	public int build;// 建设
	public int members;// 成员数量
	
	/** 副盟主数量 */
	@Column(columnDefinition = "INT default 0")
	public int deputyLeaderNum;
	public String notice = "";// 公告
	public int reputation;// 声望
	public int minApplyLevel;
	public int minApplyJunxian;;
	public String attach = "";// 招募公告
	public int isAllow;// 是否开启招募 0-关闭，1-开启
	public int isShenPi;// 是否需要审批:0-需要，1-不需要

	@Column(columnDefinition = "INT default " + AllianceConstants.STATUS_NORMAL)
	public int status;// 联盟状态：0-正常，1-选举报名，2-投票
	public Date voteStartTime;// 投票开始时间
	public Date applyStartTime;// 盟主申请开始时间

//	@Override
//	public long getIdentifier() {
//		return id;
//	}

}
