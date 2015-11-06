package com.qx.alliancefight;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.manu.dynasty.template.LMZBuildingTemp;

public class CampsiteInfo {
	public int id;									// 营地id，lmzBuildingTemp表的id
	public int cursorPos;							// 当前游标位置：1-红方，2-蓝方，0-代表中间初始
	public int cursorDir;							// 游标移动方向：1-红方，2-蓝方，3-静止
	public int perSecondsHoldValue;					// 占领值每秒增长的速度
	public int curHoldValue;						// 当前占领值,大于0代表红方，小于0代表蓝方值
	public int x;
	public int z;
	public int radius;								// 营地所见区域半径
	public int zhanlingzhiMax;						// 占领值最大值
	public int criticalValue;						// 占领值临界值，达到则算被占领
	public int zhanlingzhiAdd;						// 优势方占领值每秒增长值
	public int scoreAdd;							// 占领后提供积分每秒增长值
	public long lastHoldValueChangeTime;			// 占领值上次更新时间，单位-毫秒
	public Map<Integer, Set<Integer>>  allianceNumMap = null;		// 所见区域内各联盟人数情况，<联盟id, 联盟成员set>
	
	public CampsiteInfo(int cursorDir, int cursorPos, LMZBuildingTemp build) {
		this.cursorDir = cursorDir;
		this.cursorPos = cursorPos;
		this.perSecondsHoldValue = 0;
		this.curHoldValue = 0;
		this.x = build.x;
		this.z = build.y;
		this.id = build.id;
		this.radius = build.radius;
		this.scoreAdd = build.scoreAdd;
		this.zhanlingzhiMax = build.zhanlingzhiMax;
		this.criticalValue = build.criticalValue;
		this.zhanlingzhiAdd = build.zhanlingzhiAdd;
		allianceNumMap = new HashMap<Integer, Set<Integer>>();
		lastHoldValueChangeTime = System.currentTimeMillis();
	}
	
	public void updateCurHoldValue() {
		
	}
	
	
}
