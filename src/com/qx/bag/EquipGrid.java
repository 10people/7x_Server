package com.qx.bag;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

/**
 * 穿在君主身上的装备格子。
 * @author 康建虎
 *
 */
@Entity
@Table(name = "EquipGrid")
public class EquipGrid implements MCSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1193514388284900931L;
	@Id
	public long dbId;
	/**
	 * 物品id 配置表ID
	 */
	public int itemId;
	/**
	 * 这个可以和装备强化表UserEquip的主键equipId关联起来。
	 */
	public long instId;
	@Override
	public long getIdentifier() {
		return dbId;
	}
}
