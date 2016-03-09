package com.qx.huangye.shop;

public class BaseDuiHuan {
	public int id;
	public int itemType;
	public int itemId;
	public int itemNum;
	public int needNum;
	public int weight;
	public int site;
	public int type;

	public int getNeedLv(){
		return 0;
	}
	public int getMax(){
		return 0;
	}
	public int getVIP(){
		return -1;
	}
}
