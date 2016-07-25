package com.qx.gm.message;

public class QueryRoleStatusResp {
	public String uin;// 账号ID
	public String name;// 角色名字
	public int level;// 等级
	public int areaid;// 大区id
	public int status;// 角色状态（角色状态有3中，1正常 2 角色封停中 3角色禁言中）
	public int topup;// 充值金额
	public int code;// 返回码，成功返回100
	public int online;// 是否在线(0-不在线，1-在线)
	public long onlinetime;// 累计在线时间（分钟）

	public int getOnline() {
		return online;
	}

	public void setOnline(int online) {
		this.online = online;
	}

	public long getOnlinetime() {
		return onlinetime;
	}

	public void setOnlinetime(long onlinetime) {
		this.onlinetime = onlinetime;
	}

	public String getUin() {
		return uin;
	}

	public void setUin(String uin) {
		this.uin = uin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getAreaid() {
		return areaid;
	}

	public void setAreaid(int areaid) {
		this.areaid = areaid;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getTopup() {
		return topup;
	}

	public void setTopup(int topup) {
		this.topup = topup;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

}
