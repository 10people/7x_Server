package com.qx.equip.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;
import com.qx.persistent.MCSupport;
/**
 * 记录玩家上次洗练，并且没有进行确认或取消操作的装备
 * @author lizhaowen
 *
 */
@Entity
@Table(name = "EquipXiLian")
public class EquipXiLian implements DBHash{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public long id;//2015年4月17日16:57:30int改为long
	public long equipId;
	public long junZhuId;
	public int tongShuiAdd;
	public int wuYiAdd;
	public int zhiMouAdd;
	
	public int wqSHAdd;			//（武器伤害加深）属性洗练值
	public int wqJMAdd;			//（武器伤害减免）属性洗练值
	public int wqBJAdd;			//（武器暴击加深）属性洗练值
	public int wqRXAdd;			//（武器暴击加深）属性洗练值
	public int jnSHAdd;			//（技能伤害加深）属性洗练值
	public int jnJMAdd;			//（技能伤害减免）属性洗练值
	public int jnBJAdd;			//（技能暴击加深）属性洗练值
	public int jnRXAdd;			//（技能暴击减免）属性洗练值
	//2015年10月30日 以下5个属性不需要存到洗练表中 一旦洗出自动保存到UserEquip 且保持不变
	@Column(name = "wqBJLAdd", nullable = false, columnDefinition = "int(16) default 0")
	public int wqBJLAdd;//	武器暴击率
	@Column(name = "jnBJLAdd", nullable = false, columnDefinition = "int(16) default 0")
	public int jnBJLAdd;//	技能暴击率
	@Column(name = "wqMBLAdd", nullable = false, columnDefinition = "int(16) default 0")
	public int wqMBLAdd;//	武器免暴率
	@Column(name = "jnMBLAdd", nullable = false, columnDefinition = "int(16) default 0")
	public int jnMBLAdd;//	技能免暴率
	@Column(name = "sxJiaChengAdd", nullable = false, columnDefinition = "int(16) default 0")
	public int sxJiaChengAdd;//	技能冷却缩减      => 1.1改成属性加成
	
	//TODO 以下废弃只是为了数据库存储不报错存在
	@Column(name = "wqBJL", nullable = false, columnDefinition = "int(16) default 0")
	public int wqBJL;//	武器暴击率
	@Column(name = "jnBJL", nullable = false, columnDefinition = "int(16) default 0")
	public int jnBJL;//	技能暴击率
	@Column(name = "wqMBL", nullable = false, columnDefinition = "int(16) default 0")
	public int wqMBL;//	武器免暴率
	@Column(name = "jnMBL", nullable = false, columnDefinition = "int(16) default 0")
	public int jnMBL;//	技能免暴率
	@Column(name = "jnCDReduce", nullable = false, columnDefinition = "int(16) default 0")
	public int jnCDReduce;//	技能冷却缩减
	//TODO 以上废弃只是为了数据库存储不报错存在
	@Override
	public long hash() {
		// TODO Auto-generated method stub
		return junZhuId;
	}
}
