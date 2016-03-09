package com.qx.alliance;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class AllianceLevelAward {
	@Id
	public long junZhuId;
	
	/** 已经领取奖励的等级，格式：等级之间用_隔离开  **/
	public String getAwardLevel;
	
	/** 应该领取的目标奖励，小于该等级的表示都已经领取过了，为-1时表示联盟等级奖励都领取了 **/
	public int curLevel;
}
