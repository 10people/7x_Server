package com.manu.dynasty.template;

import com.qx.world.GameObject;

/**
 * 
 * This class is used for ...   
 * @author wangZhuan
 * @version   
 *       9.0, 2014年12月1日 下午3:21:01
 *       <GuYongBing id="101001" icon="1" 
 *       zhiye="11" name="401001" 
 *       description="401001" needLv="5"
 *        quality="1" level="1" gongjiType="21" 
 *        gongji="27" fangyu="33" shengming="400"
 *         wqSH="0" wqJM="50" wqBJ="0" wqRX="0" jnSH="0"
 *          jnJM="0" jnBJ="0" jnRX="0" skill1="0" skill2="0"
 *           skill3="0" skill4="0" skill5="0" power="342" /> 
 */
public class GuYongBing implements GameObject{
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
	public int zhanweiLve ;
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return level;
	}
}