package com.qx.alliance;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

@Entity
@Table(name = "AlliancePlayer")
public class AlliancePlayer implements MCSupport{

	private static final long serialVersionUID = -9061389268512139511L;

	@Id
	public long junzhuId;
	
	/** 联盟id，<=0表示没有联盟 **/
	public int lianMengId;

	/** 身份：0-成员，1-副盟主，2-盟主 **/
	public int title;
	
	public Date joinTime;		//入盟时间
	
	public Date getTitleTime; // 任职时间

	@Column(columnDefinition = "INT default 0")
	public int gongXian;
	
//	public int level;// 2015-7-20 15：34 君主等级 2015年9月17日删除

	@Column(columnDefinition = "INT default " + AllianceConstants.BAOMING_FALSE)
	public int isBaoming;// 是否参加报名：0-未报名，1-已经报名

	public Date baomingTime;

	@Column(columnDefinition = "INT default " + AllianceConstants.VOTED_FALSE)
	public int isVoted;// 是否已经投过票：0-未投票，1-已投票，2-弃权

	@Column(columnDefinition = "INT default 0")
	public int voteNum;// 票数

	public Date lastVoteTime;// 最后得到投票时间
	
	@Column(columnDefinition = "INT default 0")
	public long voteJunzhuId;//若是投票了，投的玩家id
	
	@Column(columnDefinition = "INT default " + AllianceConstants.VOTE_DIALOG_NOT)
	public int isVoteDialog;//是否需要提示联盟选举结果0-不需要，1-需要

	@Override
	public long getIdentifier() {
		return junzhuId;
	}
	

}
