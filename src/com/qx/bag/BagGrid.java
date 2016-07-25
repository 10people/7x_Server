package com.qx.bag;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;
import com.qx.persistent.MCSupport;

/**
 * 背包格子。
 * @author 康建虎
 *
 */
@Entity
@Table(name = "BagGrid")
public class BagGrid implements MCSupport,DBHash{
	/**
	 * 
	 */
	public static final long serialVersionUID = 3720156301351793689L;
	@Id
	public long dbId;
	/**
	 * 物品id
	 */
	public int itemId;
	/**
	 * 注意这个字段，目前只对装备强化时判定是否是材料有效（防具材料或武器材料）。
	 */
	public int type;
	public int cnt;
	
	/**
	 * type=8，代表符文经验
	 */
	public long instId;
	@Override
	public long getIdentifier() {
		return dbId;
	}
	@Override
	public long hash() {
		return dbId / 1000;
	}
}
