package com.qx.gm.message;

public class DoSendBareMailReq {
	private int type;// 协议编号
	private int firm;// 表示厂商ID
	private int zone;// 大区号
	private String rolename;// 角色名,玩家名称
	private int levlemin;// 等级最小值
	private int levlemax;// 等级最大值
	private String subject;// 主题
	private String text;// 正文信息
	private String md5;// 加密

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

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
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
