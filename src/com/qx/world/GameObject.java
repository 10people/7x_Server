package com.qx.world;

/**
 * 
 * This class is used for ...   
 * @author wangZhuan
 * @version   
 *       9.0, 2014年12月31日 上午10:36:41
 */
public interface GameObject {
	public String getName();
	public int getFangyu();
	public int getGongji();
	public int getShengming();
	public int getWqSH();
	public int getWqJM();
	public int getWqBJ();
	public int getWqRX();
	public int getJnSH();
	public int getJnJM();
	public int getJnBJ();
	public int getJnRX();
	public void setFangyu(int fangyu);
	public void setGongji(int gongji);
	public void setShengming(int shengming);
	public long getId();
	public int getGuoJiaId(int i);
	public int getRoleId(int i);
	public int getLevel();
}
