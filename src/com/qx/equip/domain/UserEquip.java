package com.qx.equip.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

@Entity
@Table(name = "UserEquip")
public class UserEquip implements MCSupport{
	/**
	 * 
	 */
	public static final long serialVersionUID = 3657431881760493277L;

	// @GenericGenerator(name = "generator", strategy = "native", parameters = {
	// @Parameter(name = "unsaved-value", value = "0") })
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "equipId", unique = true, nullable = false)
	public long equipId;

	@Column(name = "userId", nullable = false, columnDefinition = "int(16) default 0")
	public long userId;

	@Column(name = "templateId", nullable = false, columnDefinition = "int(16) default 0")
	public int templateId;

	/**
	 * 是否被装备到武将或者君主身上,=0未装备,>0 装备到的武将ID,=-1 装备到君主身上
	 */
	@Column(name = "equiped", nullable = false, columnDefinition = "int(16) default 0")
	public int equiped;

	/**
	 * 强化获得的总经验
	 */
	@Column(name = "exp", nullable = false, columnDefinition = "int(16) default 0")
	public int exp;

	/**
	 * 当前的强化级别
	 */
	@Column(name = "level", nullable = false, columnDefinition = "int(16) default 0")
	public int level;

	
	/**
	 * 当前洗练的武力
	 */
	@Column(name = "wuli", nullable = false, columnDefinition = "int(16) default 0")
	public int wuli;
	
	/**
	 * 当前洗练的统力
	 */
	@Column(name = "tongli", nullable = false, columnDefinition = "int(16) default 0")
	public int tongli;
	
	
	/**
	 * 当前洗练的谋力
	 */
	@Column(name = "mouli", nullable = false, columnDefinition = "int(16) default 0")
	public int mouli;
	
	@Column(name = "wqSH", nullable = false, columnDefinition = "int(16) default 0")
	public int wqSH;//	武器伤害加深
	
	@Column(name = "wqJM", nullable = false, columnDefinition = "int(16) default 0")
	public int wqJM;//	武器伤害减免
	
	@Column(name = "wqBJ", nullable = false, columnDefinition = "int(16) default 0")
	public int wqBJ;//	武器暴击加深
	
	@Column(name = "wqRX", nullable = false, columnDefinition = "int(16) default 0")
	public int wqRX;//	武器暴击加深
	
	@Column(name = "jnSH", nullable = false, columnDefinition = "int(16) default 0")
	public int jnSH;//	技能伤害加深
	
	@Column(name = "jnJM", nullable = false, columnDefinition = "int(16) default 0")
	public int jnJM;//	技能伤害减免
	
	@Column(name = "jnBJ", nullable = false, columnDefinition = "int(16) default 0")
	public int jnBJ;//	技能暴击加深
	
	@Column(name = "jnRX", nullable = false, columnDefinition = "int(16) default 0")
	public int jnRX;//	技能暴击减免
	//以下1.0版本改变洗练增加
	public String hasXilian;//已有的洗练属性
	public int  xianlianzhi;//洗练值(洗练值用于下一条属性出现概率的计算)
	//TODO 以下 1.1可能不加入
	@Column(name = "wqBJL", nullable = false, columnDefinition = "float(16) default 0")
	public float wqBJL;//	武器暴击率
	@Column(name = "jnBJL", nullable = false, columnDefinition = "float(16) default 0")
	public float jnBJL;//	技能暴击率
	@Column(name = "wqMBL", nullable = false, columnDefinition = "float(16) default 0")
	public float wqMBL;//	武器免暴率
	@Column(name = "jnMBL", nullable = false, columnDefinition = "float(16) default 0")
	public float jnMBL;//	技能免暴率
	@Column(name = "sxJiaCheng", nullable = false, columnDefinition = "float(16) default 0")
	public float sxJiaCheng;//技能冷却缩减      => 1.1改成属性加成
	//TODO 1.1要废弃的属性
	@Column(name = "jnCDReduce", nullable = false, columnDefinition = "float(16) default 0")
	public float jnCDReduce;//技能冷却缩减   
	//以上1.0版本改变洗练增加
	
	//对应装备上的5个宝石的孔
	public long Jewel0 = -1;
	public long Jewel1 = -1;
	public long Jewel2 = -1;
	public long Jewel3 = -1;
	public long Jewel4 = -1;
	
	//装备的进阶经验
	public int JinJieExp = 0;
	@Override
	public long getIdentifier() {
		return equipId;
	}













}
