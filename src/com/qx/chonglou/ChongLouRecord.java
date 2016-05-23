package com.qx.chonglou;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ChongLouRecord {
	@Id
	public long junzhuId;
	
	/** 当前挑战的层数（将要打的） **/
	public int currentLevel;
	
	/** 最高挑战的层数(已经打过的) **/
	public int highestLevel;
	
	/** 首次打到最高层的时间**/
	public Date highestLevelFirstTime;
	
	/** 配置的秘宝技能 **/
	public int zuheSkillId;
	
	public Date lastBattleTime;
	
}
