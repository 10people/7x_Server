package com.manu.dynasty.template;

public class MiBao {
	public int id;			//秘宝ID；秘宝品质不同，秘宝ID不同
	public int tempId;		//秘宝类型ID；秘宝品质不同，秘宝类型ID相同
	public int icon;
	public int initialStar;//秘宝获得时的初始星级
	public float initialGrow;//秘宝获得时的初始成长
	public int pinzhi;		//秘宝的品质
	public int zuheId;		//秘宝所属的组合套ID
	public int dengji;		//所需君主等级
	public int gongji;		
	public int fangyu;
	public int shengming;	//基础属性初值
	public double gongjiRate;	//基础属性系数
	public double fangyuRate;
	public double shengmingRate;
	public int maxLv;	//进阶等级
	public int expId;		//升级消耗ID，从ExpTemp表中获取升级消耗
	public int suipianId;	//秘宝对应的碎片
	public int nameId;
	public int unlockType;
	public int unlockValue;
}
