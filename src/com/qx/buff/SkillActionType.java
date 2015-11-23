package com.qx.buff;

public enum SkillActionType {
	WEAPON_DANAGE(1),
	SKILL_DAMAGE(2),
	TREAT(3),
	ATTR_BUFF(4),
	ATTR_DEBUFF(5);
	
	private int actionType;
	
	private SkillActionType(int actionType) {
		this.actionType = actionType;
	}

	public static SkillActionType getSkillActionType(int actionType) {
		SkillActionType[] types = SkillActionType.values();
		SkillActionType result = null;
		for(SkillActionType t : types) {
			if(actionType == t.actionType) {
				result = t;
				break;
			}
		}
		return result;
	}
	
	
}
