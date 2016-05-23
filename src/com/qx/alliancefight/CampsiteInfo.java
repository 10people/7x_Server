package com.qx.alliancefight;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.manu.dynasty.template.LMZBuildingTemp;

public class CampsiteInfo {
	public int id;									// 营地id，lmzBuildingTemp表的id
	public int cursorPos;							// 攻占值
	public int cursorDir;							// 移动方向 1左   2右：1-红方，2-蓝方，3-静止
	public int perSecondsHoldValue;					// 临界值
	public int curHoldValue;						// 占领方,大于0代表红方，小于0代表蓝方值
	public int x;
	public int z;
	public int radius;								// 营地所见区域半径
	public int zhanlingzhiMax;						// 占领值最大值
	public int criticalValue;						// 占领值临界值，达到则算被占领
	public int zhanlingzhiAdd;						// 优势方占领值每秒增长值
	public int scoreAdd;							// 占领后提供积分每秒增长值
	public LMZBuildingTemp conf;
	
	public CampsiteInfo(int whose,LMZBuildingTemp build) {
		this.cursorDir = 3;
		this.cursorPos = 0;
		this.perSecondsHoldValue = 0;
		this.curHoldValue = whose;
		this.x = build.x;
		this.z = build.y;
		this.id = build.id;
		this.radius = build.radius;
		this.scoreAdd = build.scoreAdd;
		this.zhanlingzhiMax = build.zhanlingzhiMax;
		this.criticalValue = build.criticalValue;
		this.zhanlingzhiAdd = build.zhanlingzhiAdd;
		conf = build;
	}
	
	public void updateCurHoldValue() {
		
	}
	
	
}
