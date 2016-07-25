package com.qx.gm.message;

import java.util.List;

public class QueryRoleInfoResp extends BaseResp {
	public String uin;// 账号ID
	public int rid;// 角色id
	public String name;// 角色名字
	public String gender;// 性别
	public int job;// 角色职业
	public String gang;// 帮会
	public int money;// 元宝
	public int copper;// 铜币
	public int topup;// 累计充值
	public int level;// 角色等级
	public int viplevel;// 至尊等级
	public int exp;// 角色的主经验
	public String registertime;// 角色注册时间
	public String lastlogintime;// 角色最后登陆时间
	public List<RoleBackpack> backpack;// 背包道具数组（包括name，id，num）
	public List<RoleEquip> equip;// 装备信息数组（包括name，id，level）
	public List<RolePet> pet;// 宠物信息数组（包括name，id，level）

	public String getUin() {
		return uin;
	}

	public void setUin(String uin) {
		this.uin = uin;
	}

	public int getRid() {
		return rid;
	}

	public void setRid(int rid) {
		this.rid = rid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public String getGang() {
		return gang;
	}

	public void setGang(String gang) {
		this.gang = gang;
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public int getCopper() {
		return copper;
	}

	public void setCopper(int copper) {
		this.copper = copper;
	}

	public int getTopup() {
		return topup;
	}

	public void setTopup(int topup) {
		this.topup = topup;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getViplevel() {
		return viplevel;
	}

	public void setViplevel(int viplevel) {
		this.viplevel = viplevel;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public List<RoleBackpack> getBackpack() {
		return backpack;
	}

	public void setBackpack(List<RoleBackpack> backpack) {
		this.backpack = backpack;
	}

	public List<RoleEquip> getEquip() {
		return equip;
	}

	public String getRegistertime() {
		return registertime;
	}

	public void setRegistertime(String registertime) {
		this.registertime = registertime;
	}

	public String getLastlogintime() {
		return lastlogintime;
	}

	public void setLastlogintime(String lastlogintime) {
		this.lastlogintime = lastlogintime;
	}

	public void setEquip(List<RoleEquip> equip) {
		this.equip = equip;
	}

	public List<RolePet> getPet() {
		return pet;
	}

	public void setPet(List<RolePet> pet) {
		this.pet = pet;
	}

}
