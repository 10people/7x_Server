package com.qx.yabiao;

import java.io.Serializable;
import java.util.Date;

/**
 * @author yuquanshui
 * 
 */

public class YaBiaoHistory implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public long junzhuId;
	public int junzhuZhanLi;
	public int junzhuLevel;
	public long enemyId;
	public int enemyRoleId;
	public String enemyName;
	public String enemyLianMengName;
	public int enemyZhanLi;
	public int enemyLevel;
	public int result;
	public int shouyi;
	public int horseType;
	public Date battleTime;
	public int enemyGuojiaId;
}
