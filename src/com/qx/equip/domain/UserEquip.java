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
	private static final long serialVersionUID = 3657431881760493277L;

	// @GenericGenerator(name = "generator", strategy = "native", parameters = {
	// @Parameter(name = "unsaved-value", value = "0") })
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "equipId", unique = true, nullable = false)
	private int equipId;

	@Column(name = "userId", nullable = false, columnDefinition = "int(16) default 0")
	private long userId;

	@Column(name = "templateId", nullable = false, columnDefinition = "int(16) default 0")
	private int templateId;

	/**
	 * 是否被装备到武将或者君主身上,=0未装备,>0 装备到的武将ID,=-1 装备到君主身上
	 */
	@Column(name = "equiped", nullable = false, columnDefinition = "int(16) default 0")
	private int equiped;

	/**
	 * 强化获得的总经验
	 */
	@Column(name = "exp", nullable = false, columnDefinition = "int(16) default 0")
	private int exp;

	/**
	 * 当前的强化级别
	 */
	@Column(name = "level", nullable = false, columnDefinition = "int(16) default 0")
	private int level;

	
	/**
	 * 当前洗练的武力
	 */
	@Column(name = "wuli", nullable = false, columnDefinition = "int(16) default 0")
	private int wuli;
	
	/**
	 * 当前洗练的统力
	 */
	@Column(name = "tongli", nullable = false, columnDefinition = "int(16) default 0")
	private int tongli;
	
	
	/**
	 * 当前洗练的谋力
	 */
	@Column(name = "mouli", nullable = false, columnDefinition = "int(16) default 0")
	private int mouli;
	
	@Column(name = "wqSH", nullable = false, columnDefinition = "int(16) default 0")
	private int wqSH;//	武器伤害加深
	
	@Column(name = "wqJM", nullable = false, columnDefinition = "int(16) default 0")
	private int wqJM;//	武器伤害减免
	
	@Column(name = "wqBJ", nullable = false, columnDefinition = "int(16) default 0")
	private int wqBJ;//	武器暴击加深
	
	@Column(name = "wqRX", nullable = false, columnDefinition = "int(16) default 0")
	private int wqRX;//	武器暴击加深
	
	@Column(name = "jnSH", nullable = false, columnDefinition = "int(16) default 0")
	private int jnSH;//	技能伤害加深
	
	@Column(name = "jnJM", nullable = false, columnDefinition = "int(16) default 0")
	private int jnJM;//	技能伤害减免
	
	@Column(name = "jnBJ", nullable = false, columnDefinition = "int(16) default 0")
	private int jnBJ;//	技能暴击加深
	
	@Column(name = "jnRX", nullable = false, columnDefinition = "int(16) default 0")
	private int jnRX;//	技能暴击减免
	//以下1.0版本改变洗练增加
	private String hasXilian;//已有的洗练属性
	private int  xianlianzhi;//洗练值(洗练值用于下一条属性出现概率的计算)
	//TODO 以下 1.1可能不加入
	@Column(name = "wqBJL", nullable = false, columnDefinition = "float(16) default 0")
	private float wqBJL;//	武器暴击率
	@Column(name = "jnBJL", nullable = false, columnDefinition = "float(16) default 0")
	private float jnBJL;//	技能暴击率
	@Column(name = "wqMBL", nullable = false, columnDefinition = "float(16) default 0")
	private float wqMBL;//	武器免暴率
	@Column(name = "jnMBL", nullable = false, columnDefinition = "float(16) default 0")
	private float jnMBL;//	技能免暴率
	@Column(name = "sxJiaCheng", nullable = false, columnDefinition = "float(16) default 0")
	private float sxJiaCheng;//技能冷却缩减      => 1.1改成属性加成
	//TODO 1.1要废弃的属性
	@Column(name = "jnCDReduce", nullable = false, columnDefinition = "float(16) default 0")
	private float jnCDReduce;//技能冷却缩减   
	//以上1.0版本改变洗练增加
	public int getEquipId() {
		return equipId;
	}

	public void setEquipId(int equipId) {
		this.equipId = equipId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getTemplateId() {
		return templateId;
	}

	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}

	public int getEquiped() {
		return equiped;
	}

	public void setEquiped(int equiped) {
		this.equiped = equiped;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getWuli() {
		return wuli;
	}

	public void setWuli(int wuli) {
		this.wuli = wuli;
	}

	public int getTongli() {
		return tongli;
	}

	public void setTongli(int tongli) {
		this.tongli = tongli;
	}

	public int getMouli() {
		return mouli;
	}

	public void setMouli(int mouli) {
		this.mouli = mouli;
	}

	public int getWqSH() {
		return wqSH;
	}

	public void setWqSH(int wqSH) {
		this.wqSH = wqSH;
	}

	public int getWqJM() {
		return wqJM;
	}

	public void setWqJM(int wqJM) {
		this.wqJM = wqJM;
	}

	public int getWqBJ() {
		return wqBJ;
	}

	public void setWqBJ(int wqBJ) {
		this.wqBJ = wqBJ;
	}

	public int getWqRX() {
		return wqRX;
	}

	public void setWqRX(int wqRX) {
		this.wqRX = wqRX;
	}

	public int getJnSH() {
		return jnSH;
	}

	public void setJnSH(int jnSH) {
		this.jnSH = jnSH;
	}

	public int getJnJM() {
		return jnJM;
	}

	public void setJnJM(int jnJM) {
		this.jnJM = jnJM;
	}

	public int getJnBJ() {
		return jnBJ;
	}

	public void setJnBJ(int jnBJ) {
		this.jnBJ = jnBJ;
	}

	public int getJnRX() {
		return jnRX;
	}

	public void setJnRX(int jnRX) {
		this.jnRX = jnRX;
	}

	@Override
	public long getIdentifier() {
		return equipId;
	}

	public String getHasXilian() {
		return hasXilian;
	}

	public void setHasXilian(String hasXilian) {
		this.hasXilian = hasXilian;
	}

	public int getXianlianzhi() {
		return xianlianzhi;
	}

	public void setXianlianzhi(int xianlianzhi) {
		this.xianlianzhi = xianlianzhi;
	}

	public float getWqBJL() {
		return wqBJL;
	}

	public void setWqBJL(float wqBJL) {
		this.wqBJL = wqBJL;
	}

	public float getJnBJL() {
		return jnBJL;
	}

	public void setJnBJL(float jnBJL) {
		this.jnBJL = jnBJL;
	}

	public float getWqMBL() {
		return wqMBL;
	}

	public void setWqMBL(float wqMBL) {
		this.wqMBL = wqMBL;
	}

	public float getJnMBL() {
		return jnMBL;
	}

	public void setJnMBL(float jnMBL) {
		this.jnMBL = jnMBL;
	}

	public float getSxJiaCheng() {
		return sxJiaCheng;
	}

	public void setSxJiaCheng(float sxJiaCheng) {
		this.sxJiaCheng = sxJiaCheng;
	}

	public float getJnCDReduce() {
		return jnCDReduce;
	}

	public void setJnCDReduce(float jnCDReduce) {
		this.jnCDReduce = jnCDReduce;
	}





}
