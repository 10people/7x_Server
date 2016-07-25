package com.qx.mibao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;

@Entity
@Table(name="mibao_skill")
public class MiBaoSkillDB implements DBHash{
	@Id 
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long id;

	public long jId;
	public int zuHeId; // 已经激活的秘宝技能id， zuHeId 是 MibaoSkill 的id
	public int level;
//	/**是否手动解锁： true： 解锁， false ： 没有解锁*/
//	public boolean hasClear = false;
//	/**是否手动进阶过： true：已经进阶了，false：没有**/
//	public boolean hasJinjie = false;
	@Override
	public long hash() {
		return id;
	}

	
}
