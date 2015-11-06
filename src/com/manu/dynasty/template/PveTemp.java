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
	
	
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getBigName() {
		return bigName;
	}
	public void setBigName(String bigName) {
		this.bigName = bigName;
	}
	public String getBigDesc() {
		return bigDesc;
	}
	public void setBigDesc(String bigDesc) {
		this.bigDesc = bigDesc;
	}
	public int getBigId() {
		return bigId;
	}
	public void setBigId(int bigId) {
		this.bigId = bigId;
	}
	public String getSmaName() {
		return smaName;
	}
	public void setSmaName(String smaName) {
		this.smaName = smaName;
	}
	public String getSmaDesc() {
		return smaDesc;
	}
	public void setSmaDesc(String smaDesc) {
		this.smaDesc = smaDesc;
	}
	public int getSmaId() {
		return smaId;
	}
	public void setSmaId(int smaId) {
		this.smaId = smaId;
	}
	public int getChapType() {
		return chapType;
	}
	public void setChapType(int chapType) {
		this.chapType = chapType;
	}
	public int getUseHp() {
		return useHp;
	}
	public void setUseHp(int userHp) {
		this.useHp = userHp;
	}
	public int getMonarchLevel() {
		return monarchLevel;
	}
	public void setMonarchLevel(int monarchLevel) {
		this.monarchLevel = monarchLevel;
	}
	public int getFrontPoint() {
		return frontPoint;
	}
	public void setFrontPoint(int frontPoint) {
		this.frontPoint = frontPoint;
	}
	public int getOpenCondition() {
		return openCondition;
	}
	public void setOpenCondition(int openCondition) {
		this.openCondition = openCondition;
	}
	public int getMoney() {
		return money;
	}
	public void setMoney(int money) {
		this.money = money;
	}
	public int getExp() {
		return exp;
	}
	public void setExp(int exp) {
		this.exp = exp;
	}
	public String getAwardId() {
		return awardId;
	}
	public void setAwardId(String awardId) {
		this.awardId = awardId;
		this.awardConf = TempletService.parseAwardString(this.awardId);
	}
	public String getFirstAwardId() {
		return firstAwardId;
	}
	public void setFirstAwardId(String firstAwardId) {
		this.firstAwardId = firstAwardId;
		this.firstAwardConf = TempletService.parseAwardString(this.firstAwardId);
	}
	public int getNpcId() {
		return npcId;
	}
	public void setNpcId(int npcId) {
		this.npcId = npcId;
	}
	public int getBossId() {
		return bossId;
	}
	public void setBossId(int bossId) {
		this.bossId = bossId;
	}
	public int getLandId() {
		return landId;
	}
	public void setLandId(int landId) {
		this.landId = landId;
	}
	public int getPower() {
		return power;
	}
	public void setPower(int power) {
		this.power = power;
	}
	public int getStar1() {
		return star1;
	}
	public void setStar1(int star1) {
		this.star1 = star1;
	}
	public int getStar2() {
		return star2;
	}
	public void setStar2(int star2) {
		this.star2 = star2;
	}
	public int getStar3() {
		return star3;
	}
	public void setStar3(int star3) {
		this.star3 = star3;
	}
	public int getIcon() {
		return icon;
	}
	public void setIcon(int icon) {
		this.icon = icon;
	}
	@Override
	public void build() {
		setAwardId(this.awardId);
		setFirstAwardId(this.firstAwardId);
	}
}
