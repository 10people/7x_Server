package com.manu.dynasty.template;

import com.qx.world.GameObject;

/**
 * 
 * This class is used for ...   
 * @author wangZhuan
 * @version   
 *       9.0, 2014年12月4日 下午12:00:40
 */
public class BaiZhanNpc implements GameObject{
	public int id;
	public int minRank;
	public int maxRank;
	public int icon;
	public int zhiye;
	public String name;
	public String description;
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
//	public int skill1;
//	public int skill2;
//	public int skill3;
//	public int skill4;
//	public int skill5;
	public int power;
//	public int mibao1;
//	public int mibao2;
//	public int mibao3;
	public int mibaoLv;
	public int mibaoStar;
	public int weapon1;
	public int weapon2;
	public int weapon3;
	public int type;
	public int profession;
	public int modleId;
	public int mibaoZuhe;
	public int mibaoZuheLv;
	// npc 的国家
	private int guoJia;
	private int roleId;
	
	@Override
	public int getGuoJiaId(int i){
		int guojia = i%7;
		return guojia == 0? 7:guojia;
	}
	@Override
	public int getRoleId(int i){
		int roleId = i%4;
		return roleId == 0? 4:roleId;
	}
	
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

	/**
	 * 返回的是 负数，pvpMgr中用到。
	 */
	@Override
	public long getId() {
		// 负值
		return -id;
	}

	@Override
	public int getLevel() {
		return level;
	}
	
}