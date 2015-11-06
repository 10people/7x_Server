package com.qx.junzhu;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 君主科技。
 * @author 康建虎
 *
 */
@Entity
@Table(name = "JzKeji")
public class JzKeji {
	@Id
	public long dbId;
	public int kejiId;
	public int ciShu;
	/**
	 * 何时冷却完成。
	 */
	public long lengQueTime;
}
