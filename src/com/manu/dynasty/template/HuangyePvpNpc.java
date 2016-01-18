package com.manu.dynasty.template;

import com.qx.world.GameObject;

public class HuangyePvpNpc extends GameObject{
	public int id;
	public int icon;
	public int zhiye;
	public int name;
	public int description;
	public int level;
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
	public int mibao1;
	public int mibao2;
	public int mibao3;
	public int mibaoLv;
	public int mibaoStar;
	public int weapon1;
	public int weapon2;
	public int weapon3;
	public int model;
	@Override
	public String getName() {
		return "";
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
		this.fangyu = fangyu;
	}
	@Override
	public void setGongji(int gongji) {
		this.gongji = gongji;
	}
	@Override
	public void setShengming(int shengming) {
		this.shengming = shengming;
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
