package com.youxigu.app.domain.ability.impl;

import com.youxigu.app.domain.ability.IAbility;
import com.youxigu.app.domain.ability.impl.data.LevelUpDataLoader;

/**
 * 具备升级能力：
 * 1 添加经验，如果经验到了升级的程度进行升级；
 * 2 查看等级，不但可以升级，还可以降级；
 * 
 * @author wuliangzhu
 *
 */
public class LevelUpAbility implements IAbility {
	private int curExp;
	private int maxExp; // 需要升级的经验
	private int level;
	private int levelUpType = 1;
	
	public int addExp(int exp) {
		int sum = this.curExp + exp;
		LevelUpDataLoader ludl = LevelUpDataLoader.get();
		
		while (sum >= maxExp) { // 进行升级
			sum -= maxExp;
			
			// 设置当前等级的最大经验
			level++;
			maxExp = (Integer)ludl.get(levelUpType, level);
		}
		
		if (maxExp > 0) {
			this.curExp = sum;
		}else if (maxExp == -2){
			this.curExp = 0;
		}else {
			System.out.println("error: levelupType not define");
		}
		
		return level;
	}
	
	public int getExp(){
		return this.curExp;
	}
	public int getLevel(){
		return this.level;
	}
}
