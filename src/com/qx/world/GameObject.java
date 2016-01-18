package com.qx.world;

/**
 * 
 * This class is used for ...   
 * @author wangZhuan
 * @version   
 *       9.0, 2014年12月31日 上午10:36:41
 */
public abstract class GameObject {
	public abstract String getName();
	public abstract int getFangyu();
	public abstract int getGongji();
	public abstract int getShengming();
	public abstract int getWqSH();
	public abstract int getWqJM();
	public abstract int getWqBJ();
	public abstract int getWqRX();
	public abstract int getJnSH();
	public abstract int getJnJM();
	public abstract int getJnBJ();
	public abstract int getJnRX();
	public abstract void setFangyu(int fangyu);
	public abstract void setGongji(int gongji);
	public abstract void setShengming(int shengming);
	public abstract long getId();
	public abstract int getGuoJiaId(int i);
	public abstract int getRoleId(int i);
	public abstract int getLevel();
	
	/** 子类是npc类型的需要重新实现该方法 */
	public int getPugongHeavy() { return 1100;}
	
	/** 子类是npc类型的需要重新实现该方法 */
	public int getSkill1Heavy() { return 1200;}
	
	/** 子类是npc类型的需要重新实现该方法 */
	public int getSkill2Heavy() { return 1300;}
	
	/** 子类是npc类型的需要重新实现该方法 */
	public int getPugongLight() { return 2100;}
	
	/** 子类是npc类型的需要重新实现该方法 */
	public int getSkill1Light() { return 2200;}
	
	/** 子类是npc类型的需要重新实现该方法 */
	public int getSkill2Light() { return 2300;}
	
	/** 子类是npc类型的需要重新实现该方法 */
	public int getPugongRange() { return 3100;}
	
	/** 子类是npc类型的需要重新实现该方法 */
	public int getSkill1Range() { return 3200;}

	/** 子类是npc类型的需要重新实现该方法 */
	public int getSkill2Range() { return 3300;}
	
}
