package com.manu.dynasty.template;

import com.qx.world.GameObject;

public class XunHanCheng extends GameObject{
	public int id;
	public int icon;
	public String name; 
	public String desc;
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
	public int mibaoZuhe;
	public int mibaoZuheLv;
	public int power;
	public int weapon1;
	public int weapon2;
	public int weapon3;
	public int model;
	public int pugongHeavy;
	public int skill1Heavy;
	public int skill2Heavy;
	public int pugongLight;
	public int skill1Light;
	public int skill2Light;
	public int pugongRange;
	public int skill1Range;
	public int skill2Range;
	
	@Override
	public int getPugongHeavy() {
		return pugongHeavy;
	}
	@Override
	public int getSkill1Heavy() {
		return skill1Heavy;
	}
	@Override
	public int getSkill2Heavy() {
		return skill2Heavy;
	}
	@Override
	public int getPugongLight() {
		return pugongLight;
	}
	@Override
	public int getSkill1Light() {
		return skill1Light;
	}
	@Override
	public int getSkill2Light() {
		return skill2Light;
	}
	@Override
	public int getPugongRange() {
		return pugongRange;
	}
	@Override
	public int getSkill1Range() {
		return skill1Range;
	}
	@Override
	public int getSkill2Range() {
		return skill2Range;
	}

	public long getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getIcon() {
		return icon;
	}
	public void setIcon(int icon) {
		this.icon = icon;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getGongji() {
		return gongji;
	}
	public void setGongji(int gongji) {
		this.gongji = gongji;
	}
	public int getFangyu() {
		return fangyu;
	}
	public void setFangyu(int fangyu) {
		this.fangyu = fangyu;
	}
	public int getShengming() {
		return shengming;
	}
	public void setShengming(int shengming) {
		this.shengming = shengming;
	}
	public int getWqSH() {
		return wqSH;
	}
	public void setWqSH(int wqSH) {
		this.wqSH = wqSH;
	}
	public int getWqJM() {
		return wqJM;
	}
	public void setWqJM(int wqJM) {
		this.wqJM = wqJM;
	}
	public int getWqBJ() {
		return wqBJ;
	}
	public void setWqBJ(int wqBJ) {
		this.wqBJ = wqBJ;
	}
	public int getWqRX() {
		return wqRX;
	}
	public void setWqRX(int wqRX) {
		this.wqRX = wqRX;
	}
	public int getJnSH() {
		return jnSH;
	}
	public void setJnSH(int jnSH) {
		this.jnSH = jnSH;
	}
	public int getJnJM() {
		return jnJM;
	}
	public void setJnJM(int jnJM) {
		this.jnJM = jnJM;
	}
	public int getJnBJ() {
		return jnBJ;
	}
	public void setJnBJ(int jnBJ) {
		this.jnBJ = jnBJ;
	}
	public int getJnRX() {
		return jnRX;
	}
	public void setJnRX(int jnRX) {
		this.jnRX = jnRX;
	}
//	public int getMibao1() {
//		return mibao1;
//	}
//	public void setMibao1(int mibao1) {
//		this.mibao1 = mibao1;
//	}
//	public int getMibao2() {
//		return mibao2;
//	}
//	public void setMibao2(int mibao2) {
//		this.mibao2 = mibao2;
//	}
//	public int getMibao3() {
//		return mibao3;
//	}
//	public void setMibao3(int mibao3) {
//		this.mibao3 = mibao3;
//	}
	public int getPower() {
		return power;
	}
	public void setPower(int power) {
		this.power = power;
	}
	public int getWeapon1() {
		return weapon1;
	}
	public void setWeapon1(int weapon1) {
		this.weapon1 = weapon1;
	}
	public int getWeapon2() {
		return weapon2;
	}
	public void setWeapon2(int weapon2) {
		this.weapon2 = weapon2;
	}
	public int getWeapon3() {
		return weapon3;
	}
	public void setWeapon3(int weapon3) {
		this.weapon3 = weapon3;
	}
	@Override
	public int getGuoJiaId(int i) {
		return 0;
	}
	@Override
	public int getRoleId(int i) {
		return model;
	}
}
