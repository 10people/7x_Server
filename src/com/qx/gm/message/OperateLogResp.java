package com.qx.gm.message;

import java.util.List;

public class OperateLogResp extends BaseResp{
	private String uin;// 账号ID
	private String rolename;// 角色名称
	private String dttm;// 行为操作时间
	private List<LogRecords> records;// 操作行为数据,Records为操作行为数据数组，具体包括内容由负责的产品和策划以及相关服务器人员人员制定。

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

	public String getDttm() {
		return dttm;
	}

	public void setDttm(String dttm) {
		this.dttm = dttm;
	}

	public List<LogRecords> getRecords() {
		return records;
	}

	public void setRecords(List<LogRecords> records) {
		this.records = records;
	}

}
