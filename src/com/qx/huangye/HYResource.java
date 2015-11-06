package com.qx.huangye;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.qx.util.TableIDCreator;

/**
 * 荒野资源点
 * @author lizhaowen
 *
 */
@Entity
public class HYResource {
	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
	public long id;//2015年4月17日16:57:30int改为long
	
	@Column(columnDefinition = "INT default 1" )
	public int multiNums;//能够看到的联盟数量
	
	/** 当前占领该资源点的联盟id，如果是npc占领就为0 **/
	@Column(columnDefinition = "INT default 0" )
	public int curHoldId;
	
	/** 联盟占领开始时间，若为null表示还没占领 **/
	public Date holdStartTime = null;
	
	/** 联盟占领资源点后上一次发放奖励的时间，若为null表示还没占领 **/
	public Date lastAllotTime = null;
	
	public int npcLevel;
	
	public HYResource() {
		super();
	}

	public HYResource(int npcLevel, int multiNums) {
		super();
		//改自增主键为指定
		//2015年4月17日16:57:30int改为long
		this.id=(TableIDCreator.getTableID(HYResource.class, 1L));
		this.npcLevel = npcLevel;
		this.multiNums = multiNums;
	}
}
