package com.qx.cdkey;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class CDKeyInfo {
	@Id
	public long keyId;
	public String cdkey;
	public Date deadDate;
	public String awards;// 奖励，type:itemId:count#type:itemId:count(类型，id,数量#类型，id,数量)
	public int chanId;// 渠道id
	public long jzId;// 使用者
	public Date createDate;// 创建日期
}
