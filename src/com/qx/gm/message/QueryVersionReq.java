package com.qx.gm.message;

import com.qx.gm.util.CodeUtil;
import com.qx.gm.util.MD5Util;

public class QueryVersionReq {
	public int type;// 协议编号
	public int firm;// 表示厂商ID
	public int zone;// 大区号
	public String title;// 公告标题
	public String content;// 公告的文字内容
	public int orders;// 公告顺序
	public String md5;// 加密

	public boolean checkMd5() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(getType()).append(getFirm()).append(getZone())
				.append(getTitle()).append(getContent()).append(getOrders())
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getOrders() {
		return orders;
	}

	public void setOrders(int orders) {
		this.orders = orders;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

}
