package com.qx.hero;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qx.persistent.MCSupport;

@Entity
@Table(name = "WjKeJi")
public class WjKeJi implements MCSupport{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8376034556312314144L;
	public static Logger log = LoggerFactory.getLogger(WjKeJi.class);
	@Id
	private long junZhuId;
	/**
	 * 低级科技信息，攻击、防御、生命。
	 */
	private int attack;
	private int defense;
	private int hp;
	
	public long getJunZhuId() {
		return junZhuId;
	}
	public void setJunZhuId(long junZhuId) {
		this.junZhuId = junZhuId;
	}

	/**
	 * 低级科技共用的冷却时间。
	 * 这里的数据其实不是剩余的CD时间，而是到这个时间点的时候cd会为0.
	 * 是指将来的某个时间点。
	 */
	private long CD;
	
	private int zhiMou;
	private int wuYi;
	private int tongShuai;

	private int zhiMouExp;
	private int wuYiExp;
	private int tongShuaiExp;
	
	public int goldenJingPo;
	public WjKeJi(){
		
	}
	public int getZhiMou() {
		return zhiMou;
	}

	public void setZhiMou(int zhiMou) {
		this.zhiMou = zhiMou;
	}

	public int getWuYi() {
		return wuYi;
	}

	public void setWuYi(int wuYi) {
		this.wuYi = wuYi;
	}

	public int getTongShuai() {
		return tongShuai;
	}

	public void setTongShuai(int tongShuai) {
		this.tongShuai = tongShuai;
	}

	public int getZhiMouExp() {
		return zhiMouExp;
	}

	public void setZhiMouExp(int zhiMouExp) {
		this.zhiMouExp = zhiMouExp;
	}

	public int getWuYiExp() {
		return wuYiExp;
	}

	public void setWuYiExp(int wuYiExp) {
		this.wuYiExp = wuYiExp;
	}

	public int getTongShuaiExp() {
		return tongShuaiExp;
	}

	public void setTongShuaiExp(int tongShuaiExp) {
		this.tongShuaiExp = tongShuaiExp;
	}


	public int getAttack() {
		return attack;
	}

	public void setAttack(int attack) {
		this.attack = attack;
	}

	public int getDefense() {
		return defense;
	}

	public void setDefense(int defense) {
		this.defense = defense;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		this.hp = hp;
	}

	public long getCD() {
		return CD;
	}

	public void setCD(long cD) {
		CD = cD;
	}

	public int getGoldenJingPo() {
		return goldenJingPo;
	}

	public void setGoldenJingPo(int goldenJingPo) {
		this.goldenJingPo = goldenJingPo;
	}
	
	/**
	 * 根据要升级的科技类型，获取该科技类型已升到的配置文件中的科技id
	 * 
	 * @param type
	 * @return
	 */
	public int getTechIdByType(int type) {
		int techId = 0;
		switch (type) {
			case HeroMgr.ATTACK:
				techId = this.getAttack();
				break;
			case HeroMgr.DEFENSE:
				techId = this.getDefense();
				break;
			case HeroMgr.HP:
				techId = this.getHp();
				break;
			case HeroMgr.ZHIMOU:
				techId = this.getZhiMou();
				break;
			case HeroMgr.WUYI:
				techId = this.getWuYi();
				break;
			case HeroMgr.TONGSHUAI:
				techId = this.getTongShuai();
				break;
			default:
				break;
		}
		return techId;
	}

	@Override
	public long getIdentifier() {
		return junZhuId;
	}
}
