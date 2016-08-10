package com.manu.dynasty.template;

import com.qx.huangye.shop.BaseDuiHuan;

public class LMGongXianDuihuan extends BaseDuiHuan{
	public int needLv;
	public int max;
	
	@Override
	public int getMax() {
		return max;
	}
	@Override
	public int getNeedLv() {
		return needLv;
	}
}
