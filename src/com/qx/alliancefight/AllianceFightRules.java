package com.qx.alliancefight;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class AllianceFightRules {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	
	/** 规则名字 */
	public String ruleName;
	
	/** 规则value，如果是时间值则格式为hh:mm(并且对于类似起始终止同一个操作的，一般都是指在同一天的时间点) */
	public String ruleValue;

	
	public AllianceFightRules() {
		super();
	}

	public AllianceFightRules(String ruleName, String ruleValue) {
		super();
		this.ruleName = ruleName;
		this.ruleValue = ruleValue;
	}
	@Override
	public String toString() {
		return "AllianceFightRules [id=" + id + ", ruleName=" + ruleName
				+ ", ruleValue=" + ruleValue + "]";
	}
	
}
