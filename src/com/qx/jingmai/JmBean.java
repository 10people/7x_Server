package com.qx.jingmai;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

/**
 * 静脉存库。
 * @author 康建虎
 *
 */
@Entity
@Table(name = "JingMai")
public class JmBean implements MCSupport{
	/**
	 * @Fields serialVersionUID : TODO
	 */
	public static final long serialVersionUID = 1L;
	/**
	 * 现在按同时只有一个大脉处理
	 */
	@Id
	public long dbId;
	/**
	 * 大脉号
	 */
	public int daMai;
	/**
	 * 第几周天了
	 */
	public int zhouTian;
	/**
	 * 每个穴位的加点情况。
	 */
	public int[] xueWei;
	/**
	 * 经脉点数。
	 */
	public int point;

	@Override
	public long getIdentifier() {
		return dbId;
	}
}
