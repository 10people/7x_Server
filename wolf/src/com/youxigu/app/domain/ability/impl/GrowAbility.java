package com.youxigu.app.domain.ability.impl;

import com.youxigu.app.domain.ability.Attributes;
import com.youxigu.app.domain.ability.IAbility;

/**
 * 农作物的生长，根据不同时间，显示不同
 * 
 * @author wuliangzhu
 *
 */
public class GrowAbility extends Attributes implements IAbility {

	@Override
	protected void addAttribute() {
		this.add("startTime", 0, "long");		
	}
	
	public int getState(){
		long startTime = this.get("startTime");
		long now = System.currentTimeMillis();
		
		int growTime = (int)(now - startTime);
		
		return growTime / 4; // 应该用IDataLoader进行计算
	}
}
