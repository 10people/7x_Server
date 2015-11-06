package com.youxigu.app.domain.ability.impl;

import com.youxigu.app.domain.ability.Attributes;
import com.youxigu.app.domain.ability.IAbility;

/**
 * 可以配备装备，做好装备配备的检查
 * 装备位置统称为slot，装备上有个属性也是slot，只有这2个对上才能装备
 * 
 * 也可以进行扩展，技能也可以用这个，如果技能和装备具备同样的业务逻辑
 * @author wuliangzhu
 *
 */
public class EquipAbility extends Attributes implements IAbility {
	
	@Override
	protected void addAttribute() {
		this.add("1", 0);
		this.add("2", 0);
		this.add("3", 0);
		this.add("4", 0);
		this.add("5", 0);
	}

	/**
	 * 穿上给定的装备， 返回原来的装备
	 * 
	 * @param equipId
	 * @param slotId
	 * @return 0 表示原来没有装备；-1 表示slotId无效
	 */
	public int equip(int equipId, int slotId){		
		String attr = slotId + "";
		
		if (!this.has(attr)) {
			return -1; // 
		}
		
		int oldEquip = this.get(attr);
		
		this.set(attr, equipId);
		
		return oldEquip;
	}
	
	public int unEquip(int slotId){
		String attr = slotId + "";
	
		if (!this.has(attr)) {
			return -1; // 
		}
		
		int oldEquip = this.get(attr);
		
		this.set(attr, 0);
		
		return oldEquip;
	}
}
