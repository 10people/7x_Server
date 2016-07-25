package com.qx.gm.message;

import java.util.List;

public class OperateTopupResp extends BaseResp{
	public String uin;// 账号ID
	public String rolename;// 角色名称
	public List<TopupRecords> records;// 充值信息

	public void setRecords(List<TopupRecords> records) {
		this.records = records;
	}

	public List<TopupRecords> getRecords() {
		return records;
	}

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

}
