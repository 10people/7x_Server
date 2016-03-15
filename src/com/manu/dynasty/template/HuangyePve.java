package com.manu.dynasty.template;


public class HuangyePve{
	/*
<HuangyePve id="100001" lv="1"
 nameId="720001" descId="720001" icon="720001" 
 condition="4" openCost="5000" npcId="201010"
  award="700101=100,700101=100" rank1Award="180" 
  rank2Award="160" rank3Award="140" rank4Award="120" 
  rank5Award="100" fastAward="30" killAward="50"
   soundId="100001" sceneId="406" power="284921"
    configId="1"
     positionX="1" 
     positionY="2" />

	 */
	public int id;
	public int lv;
	public int nameId;
	public int descId;
	public int icon;
	public int condition;
	public int openCost;
	public int npcId;
	public String award;
	public int rank1Award; 
	public int rank2Award; 
	public int rank3Award; 
	public int rank4Award; 
	public int rank5Award; 
	public int fastAward;
	public int killAward;
	public int soundId;
	public int sceneId;
	public int power;
	public int configId;
	public int positionX;
	public int positionY;
	public int pveId;
	public float paraK;
	public float huangYeBi_scale;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public int getOpenCost() {
		return openCost;
	}
	public void setOpenCost(int openCost) {
		this.openCost = openCost;
	}

	public int getNpcId() {
		return npcId;
	}
	public void setNpcId(int npcId) {
		this.npcId = npcId;
	}
	public String getAward() {
		return award;
	}

	public int getFastAward() {
		return fastAward;
	}
	public void setFastAward(int fastAward) {
		this.fastAward = fastAward;
	}

	public int getKillAward() {
		return killAward;
	}
	public void setKillAward(int killAward) {
		this.killAward = killAward;
	}
	public int getSceneId() {
		return sceneId;
	}
	public void setSceneId(int sceneId) {
		this.sceneId = sceneId;
	}
	public int getPower() {
		return power;
	}
	public void setPower(int power) {
		this.power = power;
	}

	
}
