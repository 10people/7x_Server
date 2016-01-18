package com.manu.dynasty.template;

/**
 * @author lizhaowen
 *
 */
public class NpcTemp {
	public int id;
	public int npcId;
	public int enemyId;
	public int position;
	public int gongjiType;
	public int profession;
	public int type;
	public String skills;
	public int name;
	public int desc;
	public int modelId;
	public int ifTeammate;
	public String award;
	public int lifebarNum = 1;
	public int modelApID;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getNpcId() {
		return npcId;
	}
	public void setNpcId(int npcId) {
		this.npcId = npcId;
	}
	public int getEnemyId() {
		return enemyId;
	}
	public void setEnemyId(int enemyId) {
		this.enemyId = enemyId;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
}
