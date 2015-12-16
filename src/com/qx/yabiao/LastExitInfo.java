package com.qx.yabiao;

import com.manu.dynasty.template.YunbiaoTemp;

public class LastExitInfo {
	public int remainLife;
	public float posX;
	public float posY;
	public float posZ;
	public int safeArea;
	public long time;
	
	public LastExitInfo(int safeArea, int remainLife, float posX, float posY, float posZ) {
		super();
		this.remainLife = remainLife;
		this.posX = posX;
		this.posY = posX;
		this.posZ = posZ;
		this.safeArea = safeArea;
		time = System.currentTimeMillis();
	}
	
	
	public int getAddLife() {
		if(safeArea > 0) {
			int sceonds = (int) ((System.currentTimeMillis() - time) / 1000);
			sceonds = Math.max(0, sceonds);
			return YunbiaoTemp.saveArea_recoveryPro * sceonds;
		}
		return 0;
	}
}
