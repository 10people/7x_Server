package com.qx.alliancefight;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class AllianceFightRules {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	/** 规则名字 */
	private String ruleName;
	
	/** 规则value，如果是时间值则格式为hh:mm(并且对于类似起始终止同一个操作的，一般都是指在同一天的时间点) */
	private String ruleValue;

	
	public AllianceFightRules() {
		super();
	}

	public AllianceFightRules(String ruleName, String ruleValue) {
		super();
		this.ruleName = ruleName;
		this.ruleValue = ruleValue;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getRuleValue() {
		return ruleValue;
	}

	public void setRuleValue(String ruleValue) {
		this.ruleValue = ruleValue;
	}

	@Override
	public String toString() {
		return "AllianceFightRules [id=" + id + ", ruleName=" + ruleName
				+ ", ruleValue=" + ruleValue + "]";
	}
	
}
