package com.qx.equip.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * 记录玩家上次洗练，并且没有进行确认或取消操作的装备
 * @author lizhaowen
 *
 */
@Entity
@Table(name = "EquipXiLian")
public class EquipXiLian {
	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;//2015年4月17日16:57:30int改为long
	private long equipId;
	private long junZhuId;
	private int tongShuiAdd;
	private int wuYiAdd;
	private int zhiMouAdd;
	
	private int wqSHAdd;			//（武器伤害加深）属性洗练值
	private int wqJMAdd;			//（武器伤害减免）属性洗练值
	private int wqBJAdd;			//（武器暴击加深）属性洗练值
	private int wqRXAdd;			//（武器暴击加深）属性洗练值
	private int jnSHAdd;			//（技能伤害加深）属性洗练值
	private int jnJMAdd;			//（技能伤害减免）属性洗练值
	private int jnBJAdd;			//（技能暴击加深）属性洗练值
	private int jnRXAdd;			//（技能暴击减免）属性洗练值
	//2015年10月30日 以下5个属性不需要存到洗练表中 一旦洗出自动保存到UserEquip 且保持不变
	@Column(name = "wqBJLAdd", nullable = false, columnDefinition = "int(16) default 0")
	private int wqBJLAdd;//	武器暴击率
	@Column(name = "jnBJLAdd", nullable = false, columnDefinition = "int(16) default 0")
	private int jnBJLAdd;//	技能暴击率
	@Column(name = "wqMBLAdd", nullable = false, columnDefinition = "int(16) default 0")
	private int wqMBLAdd;//	武器免暴率
	@Column(name = "jnMBLAdd", nullable = false, columnDefinition = "int(16) default 0")
	private int jnMBLAdd;//	技能免暴率
	@Column(name = "sxJiaChengAdd", nullable = false, columnDefinition = "int(16) default 0")
	private int sxJiaChengAdd;//	技能冷却缩减      => 1.1改成属性加成
	
	//TODO 以下废弃只是为了数据库存储不报错存在
	@Column(name = "wqBJL", nullable = false, columnDefinition = "int(16) default 0")
	private int wqBJL;//	武器暴击率
	@Column(name = "jnBJL", nullable = false, columnDefinition = "int(16) default 0")
	private int jnBJL;//	技能暴击率
	@Column(name = "wqMBL", nullable = false, columnDefinition = "int(16) default 0")
	private int wqMBL;//	武器免暴率
	@Column(name = "jnMBL", nullable = false, columnDefinition = "int(16) default 0")
	private int jnMBL;//	技能免暴率
	@Column(name = "jnCDReduce", nullable = false, columnDefinition = "int(16) default 0")
	private int jnCDReduce;//	技能冷却缩减
	//TODO 以上废弃只是为了数据库存储不报错存在
	
	
	//2015年4月17日16:57:30int改为long
	public void setId(long id) {
		this.id = id;
	}
	public long getId() {
		return id;
	}
	public long getEquipId() {
		return equipId;
	}
	public void setEquipId(long equipId) {
		this.equipId = equipId;
	}
	public int getTongShuiAdd() {
		return tongShuiAdd;
	}
	public void setTongShuiAdd(int tongShuiAdd) {
		this.tongShuiAdd = tongShuiAdd;
	}
	public int getWuYiAdd() {
		return wuYiAdd;
	}
	public void setWuYiAdd(int wuYiAdd) {
		this.wuYiAdd = wuYiAdd;
	}
	public int getZhiMouAdd() {
		return zhiMouAdd;
	}
	public void setZhiMouAdd(int zhiMouAdd) {
		this.zhiMouAdd = zhiMouAdd;
	}
	public long getJunZhuId() {
		return junZhuId;
	}
	public void setJunZhuId(long junZhuId) {
		this.junZhuId = junZhuId;
	}
	public int getWqSHAdd() {
		return wqSHAdd;
	}
	public void setWqSHAdd(int wqSHAdd) {
		this.wqSHAdd = wqSHAdd;
	}
	public int getWqJMAdd() {
		return wqJMAdd;
	}
	public void setWqJMAdd(int wqJMAdd) {
		this.wqJMAdd = wqJMAdd;
	}
	public int getWqBJAdd() {
		return wqBJAdd;
	}
	public void setWqBJAdd(int wqBJAdd) {
		this.wqBJAdd = wqBJAdd;
	}
	public int getWqRXAdd() {
		return wqRXAdd;
	}
	public void setWqRXAdd(int wqRXAdd) {
		this.wqRXAdd = wqRXAdd;
	}
	public int getJnSHAdd() {
		return jnSHAdd;
	}
	public void setJnSHAdd(int jnSHAdd) {
		this.jnSHAdd = jnSHAdd;
	}
	public int getJnJMAdd() {
		return jnJMAdd;
	}
	public void setJnJMAdd(int jnJMAdd) {
		this.jnJMAdd = jnJMAdd;
	}
	public int getJnBJAdd() {
		return jnBJAdd;
	}
	public void setJnBJAdd(int jnBJAdd) {
		this.jnBJAdd = jnBJAdd;
	}
	public int getJnRXAdd() {
		return jnRXAdd;
	}
	public void setJnRXAdd(int jnRXAdd) {
		this.jnRXAdd = jnRXAdd;
	}
	
}
