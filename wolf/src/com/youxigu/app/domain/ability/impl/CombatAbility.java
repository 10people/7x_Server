package com.youxigu.app.domain.ability.impl;

import com.youxigu.app.domain.ability.Attributes;
import com.youxigu.app.domain.ability.IAbility;
/**
 * 一般如果需要战斗的实体，需要配备这个战斗属性
 * 
 * @author wuliangzhu
 *
 */
public class CombatAbility extends Attributes implements IAbility {
	@Override
	protected void addAttribute() {
		this.add("health", 100);
		this.add("defence", 1);
		this.add("attack", 2);
	}	
}
