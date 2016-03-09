package com.manu.dynasty.template;

public class MaJu {
	public int id;
	public int price;
	
	
	/**
	 * 1：保底收益
	 * 2：马鞭加速
	 * 3：无敌护罩
	 */
	public int FunctionType;
	
	/**
	 * 保底收益百分比 或者 马鞭加速时长 或者 保护罩保护时长
	 * 2015年12月10日加注释
	 */
	public int value1;
	
	/**
	 * 马鞭加速比率（乘以100的）或者保护罩CD时长
	 * 2015年12月10日加注释
	 */
	public int value2;
	
	/**
	 * 马鞭加速CD时长 
	 * 2015年12月10日加注释
	 */
	public int value3;
	
	/**
	 * 
	 * 2015年12月10日加注释 2016年1月25日马具收益加成
	 */
	public int value4;
	/**
	 * 马具收益加成
	 *  2016年1月25日马具收益加成
	 */
	public double profitPara;
	
}
