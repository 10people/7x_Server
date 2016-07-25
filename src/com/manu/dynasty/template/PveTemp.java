package com.manu.dynasty.template;

import com.manu.dynasty.base.TempletService;

public class PveTemp implements SelfBuilder{
	public static final int[] emptyArr = new int[0];
	public int id;
	public String bigName;
	public String bigDesc;
	public int bigId;
	public String smaName;
	public String smaDesc;
	public int smaId;
	public int chapType;
	public int useHp;
	public int monarchLevel;
	public int frontPoint;
	public int openCondition;
	public int money;
	public int exp;
	public String awardId = "";
	public int[] awardConf = emptyArr;//数组，对应格式211=28,212=6,216=10
	public String firstAwardId = "";
	public int[] firstAwardConf = emptyArr;//数组，对应格式211=28,212=6,216=10
	public int npcId;
	public int bossId;
	public int landId;
	public int power;
	public int star1;
	public int star2;
	public int star3;
	public int icon;
	public int time;
	public int RenWuLimit;
	public int PowerLimit;
	public void setAwardId(String awardId) {
		this.awardId = awardId;
		this.awardConf = TempletService.parseAwardString(this.awardId);
	}
	public void setFirstAwardId(String firstAwardId) {
		this.firstAwardId = firstAwardId;
		this.firstAwardConf = TempletService.parseAwardString(this.firstAwardId);
	}
	@Override
	public void build() {
		setAwardId(this.awardId);
		setFirstAwardId(this.firstAwardId);
	}
}
