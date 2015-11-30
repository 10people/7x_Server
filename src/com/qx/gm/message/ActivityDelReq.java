package com.qx.gm.message;

import com.qx.gm.util.CodeUtil;
import com.qx.gm.util.MD5Util;

public class ActivityDelReq {
	private int type;// 协议编号
	private int firm;// 表示厂商ID
	private int zone;// 大区号
	private int actid;// 活动记录ID
	private int acttype;// 活动类型(1-7)
	private String md5;// 加密

	/*
	 * 注解：活动类型： 1: 全服补偿配置 2：限时登录配置 3: 累计充值配置 4: 累计消费配置 5: 充值排行配置 6: 消费排行配置 7:
	 * 内置活动配置
	 */

	public boolean checkMd5() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(getType()).append(getFirm()).append(getZone())
				.append(getActid()).append(getActtype())
				.append(CodeUtil.MD5_KEY);
		if (!MD5Util.checkMD5(sBuffer.toString(), getMd5())) {// MD5验证
			return false;
		}
		return true;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getFirm() {
		return firm;
	}

	public void setFirm(int firm) {
		this.firm = firm;
	}

	public int getZone() {
		return zone;
	}

	public void setZone(int zone) {
		this.zone = zone;
	}

	public int getActid() {
		return actid;
	}

	public void setActid(int actid) {
		this.actid = actid;
	}

	public int getActtype() {
		return acttype;
	}

	public void setActtype(int acttype) {
		this.acttype = acttype;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

}
