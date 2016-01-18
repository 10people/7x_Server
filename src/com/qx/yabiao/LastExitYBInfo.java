package com.qx.yabiao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.manu.dynasty.template.CanShu;
import com.manu.dynasty.template.YunbiaoTemp;
import com.manu.dynasty.util.DateUtils;

@Entity
public class LastExitYBInfo {
	@Id
	public long junzhuId;
	public int remainLife;
	public float posX;
	public float posY;
	public float posZ;
	public int safeArea;
	public long lastExitTime;
	
	
	public LastExitYBInfo() {
		super();
	}

	public LastExitYBInfo(long junzhuId, int safeArea, int remainLife, float posX, float posY, float posZ) {
		super();
		this.junzhuId = junzhuId;
		this.remainLife = remainLife;
		this.posX = posX;
		this.posY = posX;
		this.posZ = posZ;
		this.safeArea = safeArea;
		lastExitTime = System.currentTimeMillis();
	}
	
	public int getAddLife(int totalLife) {
		if(safeArea > 0) {
			int sceonds = (int) ((System.currentTimeMillis() - lastExitTime) / 1000);
			sceonds = Math.max(0, sceonds);
			int times = sceonds / YunbiaoTemp.saveArea_recovery_interval;
			return (int) (totalLife * (YunbiaoTemp.saveArea_recoveryPro / 100) * times);
		}
		return 0;
	}
	
	public void updateInfo(int safeArea, int remainLife, float posX, float posY, float posZ) {
		this.remainLife = remainLife;
		this.posX = posX;
		this.posY = posX;
		this.posZ = posZ;
		this.safeArea = safeArea;
		lastExitTime = System.currentTimeMillis();
	}

	public boolean isReset() {
		return !DateUtils.isSameSideOfX(new Date(lastExitTime), new Date(), CanShu.REFRESHTIME_PURCHASE);
	}
}
