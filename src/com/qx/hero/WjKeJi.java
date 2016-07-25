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
	public static final long serialVersionUID = 8376034556312314144L;
	public static Logger log = LoggerFactory.getLogger(WjKeJi.class);
	@Id
	public long junZhuId;
	/**
	 * 低级科技信息，攻击、防御、生命。
	 */
	public int attack;
	public int defense;
	public int hp;
	/**
	 * 低级科技共用的冷却时间。
	 * 这里的数据其实不是剩余的CD时间，而是到这个时间点的时候cd会为0.
	 * 是指将来的某个时间点。
	 */
	public long CD;
	
	public int zhiMou;
	public int wuYi;
	public int tongShuai;

	public int zhiMouExp;
	public int wuYiExp;
	public int tongShuaiExp;
	
	public int goldenJingPo;
	public WjKeJi(){
		
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
				techId = this.attack;
				break;
			case HeroMgr.DEFENSE:
				techId = this.defense;
				break;
			case HeroMgr.HP:
				techId = this.hp;
				break;
			case HeroMgr.ZHIMOU:
				techId = this.zhiMou;
				break;
			case HeroMgr.WUYI:
				techId = this.wuYi;
				break;
			case HeroMgr.TONGSHUAI:
				techId = this.tongShuai;
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
