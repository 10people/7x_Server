package com.manu.dynasty.util;
/**
 * @author 李康
 * 2013-8-28 下午04:45:11
 *
 */
public enum BaseEffectEnum {

	ATK("atk"),  //攻
	DEF("def"),   //防
	HP("hp"),  //血
	CRITRATE("critRate"), // 暴击率
	DELHURT("delHurt"),// 减免伤害
	DODGERATE("dodgeRate"),// 闪避率
	HURTDEEP("hurtDeep");//伤害加深
	
	
	private String value;

	private BaseEffectEnum(String value){
		this.value = value;
	}
	
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
