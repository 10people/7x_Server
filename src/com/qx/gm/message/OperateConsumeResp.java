package com.qx.gm.message;

import java.util.List;

public class OperateConsumeResp extends BaseResp{
	public String uin;// 账号ID
	public String rolename;// 角色名称
	public List<ConsumeRecords> records;// 消费信息

	public String getUin() {
		return uin;
	}

	public void setUin(String uin) {
		this.uin = uin;
	}

	public String getRolename() {
		return rolename;
	}

	public void setRolename(String rolename) {
		this.rolename = rolename;
	}

	public List<ConsumeRecords> getRecords() {
		return records;
	}

	public void setRecords(List<ConsumeRecords> records) {
		this.records = records;
	}

}
