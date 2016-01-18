package com.manu.dynasty.template;

import com.qx.world.GameObject;

public class HuangYeGuYongBing extends GameObject {
	public int id;
	public int icon;
	public int zhiye;
	public String name;
	public String description;
	public int needLv;
	public int quality;
	public int level;
	public int gongjiType;
	public int gongji;
	public int fangyu;
	public int shengming;
	public int wqSH;
	public int wqJM;
	public int wqBJ;
	public int wqRX;
	public int jnSH;
	public int jnJM;
	public int jnBJ;
	public int jnRX;
	public int skill1;
	public int skill2;
	public int skill3;
	public int skill4;
	public int skill5;
	public int power;
	public int modelId;
	public String skills;
	public int renshu;
	public int type;
	public int profession;
	public int lifebarNum = 1;
	public int modelApID;
	
	@Override
	public String getName() {
		return name;
	}
	@Override
	public int getFangyu() {
		return fangyu;
	}
	@Override
	public int getGongji() {
		return gongji;
	}
	@Override
	public int getShengming() {
		return shengming;
	}
	@Override
	public int getWqSH() {
		return wqSH;
	}
	@Override
	public int getWqJM() {
		return wqJM;
	}
	@Override
	public int getWqBJ() {
		return wqBJ;
	}
	@Override
	public int getWqRX() {
		return wqRX;
	}
	@Override
	public int getJnSH() {
		return jnSH;
	}
	@Override
	public int getJnJM() {
		return jnJM;
	}
	@Override
	public int getJnBJ() {
		return jnBJ;
	}
	@Override
	public int getJnRX() {
		return jnRX;
	}
	@Override
	public void setFangyu(int fangyu) {
	}
	@Override
	public void setGongji(int gongji) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setShengming(int shengming) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public long getId() {
		return id;
	}
	@Override
	public int getGuoJiaId(int i) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getRoleId(int i) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getLevel() {
		return level;
	}
}
