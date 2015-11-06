package com.qx.mibao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="mibao_skill")
public class MiBaoSkillDB {
	@Id 
	// id = junzhuId * 10 + zuHeId
	public long id;
	/**是否手动解锁： true： 解锁， false ： 没有解锁*/
	public boolean hasClear = false;
	/**是否手动进阶过： true：已经进阶了，false：没有**/
	public boolean hasJinjie = false;
	
}
