package com.qx.junzhu;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;

@Entity
@Table(name = "PlayerTime")
public class PlayerTime implements DBHash{
	@Id
	public long junzhuId;
	public Date createRoleTime; // 创建角色时间
	public Date zhunchengTime; // 进入主城时间  2015年7月28日加
	public Date dayFirstTime; // 当日首次登陆时间
	public Date loginTime; // 本次登录时间
	public Date logoutTime; // 本次退出时间
	// 2015年7月4日 更改Date为long，记录在线毫秒数
	public long totalOnlineTime; // 总在线时间

	public PlayerTime() {
	}

	public PlayerTime(long junzhuId) {
		Date date = new Date();
		this.junzhuId = junzhuId;
		this.createRoleTime = date;
		this.dayFirstTime = date;
		this.loginTime = date;
	}

	@Override
	public long hash() {
        return junzhuId;
	}
}
