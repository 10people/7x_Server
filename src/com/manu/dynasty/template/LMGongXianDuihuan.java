package com.manu.dynasty.template;

import com.qx.huangye.shop.BaseDuiHuan;

public class LMGongXianDuihuan extends BaseDuiHuan{
	public int needLv;
	public int max;
	public int getMax(){
		return max;
	}
	public int getNeedLv(){
		System.out.print("是的，调用了：needLv =  " + needLv);
		return needLv;
	}
}
