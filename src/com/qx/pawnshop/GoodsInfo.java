package com.qx.pawnshop;

public class GoodsInfo {
	private int id;			// dangpu配置文件的id
	private boolean isSell;	//是否已售出，true-以售完
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isSell() {
		return isSell;
	}
	public void setSell(boolean isSell) {
		this.isSell = isSell;
	}
	
}
