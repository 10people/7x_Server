package com.qx.junzhu;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.qx.persistent.MCSupport;
import com.qx.world.GameObject;

/**
 * @author 康建虎
 * 
 */
@Entity
@Table(name = "JunZhu")
public class JunZhu extends GameObject implements MCSupport, Cloneable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2374597839959110796L;
	@Id
	public long id;
	@Column(unique = true, nullable = false)
	public String name;
	public char gender;
	public int level;
	public long exp;
	public int tongBi;
	public int yuanBao;
	public int tiLi;
	@Transient
	public int tiLiMax;
	/**
	 * 可分配的属性点
	 */
	public int attPoint;
	/**
	 * 手动分配的点数。
	 */
	public int zhiLiAdd;
	/**
	 * 手动分配的点数。
	 */
	public int wuLiAdd;
	/**
	 * 手动分配的点数。
	 */
	public int zhengZhiAdd;
	@Transient
	public int gongJi;
	@Transient
	public int fangYu;
	public int shengMing;
	public int shengMingMax;

	@Transient
	public int wqSH;//	武器伤害加深
	@Transient
	public int wqJM;//	武器伤害减免
	@Transient
	public int wqBJ;//	武器暴击加深
	@Transient
	public int wqRX;//	武器暴击减免
	@Transient
	public int jnSH;//	技能伤害加深
	@Transient
	public int jnJM;//	技能伤害减免
	@Transient
	public int jnBJ;//	技能暴击加深
	@Transient
	public int jnRX;//	技能暴击减免

	public int cardJiFen;
	public int roleId;
	@Column(nullable = false, columnDefinition = "INT default 0")
	public int vipLevel;
	@Column(nullable = false, columnDefinition = "INT default 5")
	public int guoJiaId;

	@Override
	public long getIdentifier() {
		return id;
	}

	@Override
	public int getFangyu() {
		return fangYu;
	}

	@Override
	public int getGongji() {
		return gongJi;
	}

	@Override
	public int getShengming() {
		return shengMingMax;
	}

	@Override
	public int getWqSH() {
		return wqSH;
	}

	@Override
	public int getWqJM() {
		return wqJM;
	}

	@Override
	public int getWqBJ() {
		return wqBJ;
	}

	@Override
	public int getWqRX() {
		return wqRX;
	}

	@Override
	public int getJnSH() {
		return jnSH;
	}

	@Override
	public int getJnJM() {
		return jnJM;
	}

	@Override
	public int getJnBJ() {
		return jnBJ;
	}

	@Override
	public int getJnRX() {
		return jnRX;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setFangyu(int fangyu) {
		this.fangYu = fangyu;
	}

	@Override
	public void setGongji(int gongji) {
		this.gongJi = gongji;
	}

	@Override
	public void setShengming(int shengming) {
		this.shengMingMax = shengming;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public int getGuoJiaId(int i) {
		return guoJiaId;
	}

	@Override
	public int getRoleId(int i) {
		return roleId;
	}

	@Override
	public int getLevel() {
		return level;
	}
	
	public JunZhu clone(){
		JunZhu o = null;
		try {
			o = (JunZhu) super.clone();
		} catch (CloneNotSupportedException e) {
				e.printStackTrace();
		}
		return o;
	}
	
}
/*
 * 2014年4月17日19:16:15
 * 
 * CREATE TABLE `JunZhu` ( `id` bigint NOT NULL , `name` varchar(200) NOT NULL ,
 * `gender` char(255) NOT NULL COMMENT 'F=nv;M=nan' , `lianMengId` int NOT NULL
 * , `level` int NOT NULL , `exp` bigint NOT NULL , `attPoint` int NOT NULL ,
 * `zhiLiAdd` int NOT NULL , `wuLiAdd` int NOT NULL , `zhengZhiAdd` int NOT NULL
 * , PRIMARY KEY (`id`), INDEX `name` (`name`) ) ;
 */