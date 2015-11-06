package com.qx.junzhu;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PlayerTime")
public class PlayerTime {
	@Id
	private long junzhuId;
	private Date createRoleTime; // 创建角色时间
	private Date zhunchengTime; // 进入主城时间  2015年7月28日加
	private Date dayFirstTime; // 当日首次登陆时间
	private Date loginTime; // 本次登录时间
	private Date logoutTime; // 本次退出时间
	// 2015年7月4日 更改Date为long，记录在线毫秒数
	private long totalOnlineTime; // 总在线时间

	public PlayerTime() {
	}

	public PlayerTime(long junzhuId) {
		Date date = new Date();
		this.junzhuId = junzhuId;
		this.createRoleTime = date;
		this.dayFirstTime = date;
		this.loginTime = date;
	}

	public long getJunzhuId() {
		return junzhuId;
	}

	public void setJunzhuId(long junzhuId) {
		this.junzhuId = junzhuId;
	}

	public Date getDayFirstTime() {
		return dayFirstTime;
	}

	public void setDayFirstTime(Date dayFirstTime) {
		this.dayFirstTime = dayFirstTime;
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public Date getLogoutTime() {
		return logoutTime;
	}

	public void setLogoutTime(Date logoutTime) {
		this.logoutTime = logoutTime;
	}

	public long getTotalOnlineTime() {
		return totalOnlineTime;
	}

	public void setTotalOnlineTime(long totalOnlineTime) {
		this.totalOnlineTime = totalOnlineTime;
	}

	public Date getCreateRoleTime() {
		return createRoleTime;
	}

	public void setCreateRoleTime(Date createRoleTime) {
		this.createRoleTime = createRoleTime;
	}

	public Date getZhunchengTime() {
		return zhunchengTime;
	}

	public void setZhunchengTime(Date zhunchengTime) {
		this.zhunchengTime = zhunchengTime;
	}

}
