package com.qx.alliancefight;

import java.util.HashSet;
import java.util.Set;

public class ScoreInfo {
	public String allianceName;
	public int allianceId;
	public int teamId;
	public int score;
	public int perSecondAddRate;
	public int bornPointX;
	public int bornPointZ;
	public long lastChangeTime;

	public Set<CampsiteInfo> holdCampsite;
	public Set<Long> junzhuIdSet;
	
	public ScoreInfo(int allianceId, int teamId, int bornPointX,
			int bornPointZ, String allianceName) {
		this.allianceId = allianceId;
		this.teamId = teamId;
		this.score = 0;
		this.perSecondAddRate = 0;
		this.holdCampsite = new HashSet<CampsiteInfo>();
		this.bornPointX = bornPointX;
		this.bornPointZ = bornPointZ;
		this.allianceName = allianceName;
		junzhuIdSet = new HashSet<Long>();
		lastChangeTime = System.currentTimeMillis();
	}
	
	public void addJunZhuId(long junzhuId) {
		junzhuIdSet.add(junzhuId);
	}
	
	public boolean containJunZhu(long junzhuId) {
		return junzhuIdSet.contains(junzhuId);
	}

	public void changeScore() {
		int interval = (int) ((System.currentTimeMillis() - lastChangeTime) / 1000);
		if(interval >= 1) {
			int addValue = interval / 1 * perSecondAddRate;
			score += addValue;
			score = Math.min(score, AllianceFightMgr.lmzConfig.scoreMax);
			lastChangeTime = System.currentTimeMillis();
		}
	}

}
