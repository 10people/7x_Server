package com.manu.dynasty.template;

public abstract class BaseItem {
	public static int TYPE_ITEM = 10000;
	public static int TYPE_WuQi_CaiLiao = 1;
	public static int TYPE_FangJu_CaiLiao = 2;
	public static int TYPE_JinJie_CaiLiao = 6;
	public static int TYPE_EQUIP = 20000;
	public static int TYPE_HeroProto = 70000;
	/** 11 ~ 15 换卡材料： 古卷*/
	public static int TYPE_GU_JUAN_1 = 11;
	public static int TYPE_GU_JUAN_2 = 12;
	public static int TYPE_GU_JUAN_3 = 13;
	public static int TYPE_GU_JUAN_4 = 14;
	public static int TYPE_GU_JUAN_5 = 15;
	public abstract int getId();
	public abstract String getName();
	public abstract int getType();
	public abstract int getPinZhi();
	public abstract int getIconId();
	public int getRepeatNum(){return 1;};//默认不堆叠 
}
