package com.qx.gm.message;

import java.util.List;

public class QueryRoleInfoResp {
	private String uin;// 账号ID
	private int rid;// 角色id
	private String name;// 角色名字
	private String gender;// 性别
	private int job;// 角色职业
	private String gang;// 帮会
	private int money;// 元宝
	private int copper;// 铜币
	private int topup;// 累计充值
	private int level;// 角色等级
	private int viplevel;// 至尊等级
	private int exp;// 角色的主经验
	private String registertime;// 角色注册时间
	private String lastlogintime;// 角色最后登陆时间
	private List<RoleBackpack> backpack;// 背包道具数组（包括name，id，num）
	private List<RoleEquip> equip;// 装备信息数组（包括name，id，level）
	private List<RolePet> pet;// 宠物信息数组（包括name，id，level）
	private int code;// 返回码，成功返回100

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

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
