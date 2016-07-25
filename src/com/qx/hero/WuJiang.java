package com.qx.hero;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.qx.persistent.MCSupport;

@Entity
@Table(name = "WuJiangs", schema = "qxmobile", indexes={@Index(name="owner_id",columnList="owner_id")})
public class WuJiang implements MCSupport {
	/**
	 * 
	 */
	public static final long serialVersionUID = -9023310186462522324L;

	@Id
	@Column(name = "db_id", unique = true, nullable = false)
	public long dbId;
	
	@Column(name = "hero_id", nullable = false)
	public int heroId;
	
	//基本属性。
	//@Column(name = "attack", nullable = false)
	@Transient
	public int attack;
	//@Column(name = "defense", nullable = false)
	@Transient
	public int defense;
	//@Column(name = "hp", nullable = false)
	@Transient
	public int hp;
	
	//有争议的属性。（不排除将来修改的可能）
	//@Column(name = "zhimou", nullable = false)
	@Transient
	public int zhimou;
	//@Column(name = "wuyi", nullable = false)
	@Transient
	public int wuyi;
	//@Column(name = "tongshuai", nullable = false)
	@Transient
	public int tongshuai;
	
	//标签
	//@Column(name = "label", nullable = false)
	@Transient
	public String label;
	
	//品质
	//@Column(name = "quality", nullable = false)
	@Transient
	public int quality;
	
	//星级数据。
	//@Column(name = "max_star_num", nullable = false)
	@Transient
	public int maxStarNum;

	/*
	//忠诚度
	//@Column(name = "loyal", nullable = false)
	@Transient
	public int loyal;
	*/
	//官职
	//@Column(name = "duty", nullable = false)
	@Transient
	public int duty;
	
	//技能  0~4个技能。
	//@Column(name = "skill1", nullable = false)
	@Transient
	public int skill1;
	//@Column(name = "skill2", nullable = false)
	@Transient
	public int skill2;
	//@Column(name = "skill3", nullable = false)
	@Transient
	public int skill3;
	//@Column(name = "skill4", nullable = false)
	@Transient
	public int skill4;
	
	
	//当前经验值
	@Column(name = "exp", nullable = false)
	public int exp;
	
	@Column(name = "owner_id", nullable = false, updatable=false)
	public long ownerId;

	public int heroGrowId;
	
	public int num;
	
	public boolean combine;
	public int zhanLi;
	
	public boolean isCombine() {
		return combine;
	}

	public void setCombine(boolean combine) {
		this.combine = combine;
	}

	public int getHeroGrowId() {
		return heroGrowId;
	}

	public void setHeroGrowId(int heroGrowId) {
		this.heroGrowId = heroGrowId;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public long getDbId() {
		return dbId;
	}

	public void setDbgId(long wuJiangId) {
		this.dbId = wuJiangId;
	}

	public int getZhimou() {
		return zhimou;
	}

	public void setZhimou(int zhimou) {
		this.zhimou = zhimou;
	}

	public int getWuyi() {
		return wuyi;
	}

	public void setWuyi(int wuyi) {
		this.wuyi = wuyi;
	}

	public int getTongshuai() {
		return tongshuai;
	}

	public void setTongshuai(int tongshuai) {
		this.tongshuai = tongshuai;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}

	public int getHeroId() {
		return heroId;
	}

	public void setHeroId(int heroId) {
		this.heroId = heroId;
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public int getMaxStarNum() {
		return maxStarNum;
	}

	public void setMaxStarNum(int maxStarNum) {
		this.maxStarNum = maxStarNum;
	}

/*
	public int getLoyal() {
		return loyal;
	}

	public void setLoyal(int loyal) {
		this.loyal = loyal;
	}
*/
	public int getDuty() {
		return duty;
	}

	public void setDuty(int duty) {
		this.duty = duty;
	}
	
	public int getSkill1() {
		return skill1;
	}

	public void setSkill1(int skill1) {
		this.skill1 = skill1;
	}

	public int getSkill2() {
		return skill2;
	}

	public void setSkill2(int skill2) {
		this.skill2 = skill2;
	}

	public int getSkill3() {
		return skill3;
	}

	public void setSkill3(int skill3) {
		this.skill3 = skill3;
	}

	public int getSkill4() {
		return skill4;
	}

	public void setSkill4(int skill4) {
		this.skill4 = skill4;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	@Override
	public long getIdentifier() {
		return dbId;
	}
}
