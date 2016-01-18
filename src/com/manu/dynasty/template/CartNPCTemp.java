package com.manu.dynasty.template;

import com.qx.junzhu.JunZhu;

public class CartNPCTemp {
	public int id;
	public int quality;//无用
	public int level;
	public int gongjiType;
	public int gongji;
	public int fangyu;
	public int shengming;
	public int power;//战力
	public int wqSH; 
	public int  wqJM;
	public int jnSH;
	public int  jnJM;
	public int  wqBJ;
	public int  wqRX;
	public int  jnBJ;
	public int  jnRX;
	public String skills;
	public String skill1;
	public String skill2;
	public String skill3;
	public String skill4;
	public String skill5;
	public String yuansu;
	public String name;
	//待策划加入 2015年12月10日
	public int roleId;
	
	public JunZhu valueOfJunZhu(long junzhuId) {
		JunZhu junzhu = new JunZhu();
		junzhu.id = junzhuId;
		junzhu.shengMingMax = this.shengming;
		junzhu.fangYu = this.fangyu;
		junzhu.gongJi = this.gongji;
		junzhu.wqSH = this.wqSH;
		junzhu.wqJM = this.wqJM;
		junzhu.wqBJ = this.wqBJ;
		junzhu.wqRX = this.wqRX;
		junzhu.jnBJ = this.jnBJ;
		junzhu.jnJM = this.jnJM;
		junzhu.jnRX = this.jnRX;
		junzhu.jnSH = this.jnSH;
		junzhu.level = this.level;
		return junzhu;
	}
}
