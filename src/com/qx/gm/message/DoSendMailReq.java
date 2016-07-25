package com.qx.gm.message;

import java.util.List;

import com.qx.gm.util.CodeUtil;
import com.qx.gm.util.MD5Util;

public class DoSendMailReq {
	public int type;// 协议编号
	public int firm;// 表示厂商ID
	public int zone;// 大区号
	public String rolename;// 角色名,玩家名称
	public int levlemin;// 等级最小值
	public int levlemax;// 等级最大值
	public String subject;// 主题
	public String text;// 正文信息
	public List<MailProp> prop;// 道具信息数组

	public boolean checkMd5(String prop4Md5) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(getType()).append(getFirm()).append(getZone())
				.append(getRolename()).append(getLevlemin())
				.append(getLevlemax()).append(getSubject()).append(getText())
				.append(prop4Md5).append(CodeUtil.MD5_KEY);
		if (!MD5Util.checkMD5(sBuffer.toString(), getMd5())) {// MD5验证
			return false;
		}
		return true;
	}

	public List<MailProp> getProp() {
		return prop;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setProp(List<MailProp> prop) {
		this.prop = prop;
	}

	public String md5;// 加密

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

	public String getRolename() {
		return rolename;
	}

	public void setRolename(String rolename) {
		this.rolename = rolename;
	}

	public int getLevlemin() {
		return levlemin;
	}

	public void setLevlemin(int levlemin) {
		this.levlemin = levlemin;
	}

	public int getLevlemax() {
		return levlemax;
	}

	public void setLevlemax(int levlemax) {
		this.levlemax = levlemax;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

}
