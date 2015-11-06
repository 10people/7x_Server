package com.qx.equip.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class XilianFirstRecord {
	@Id
	public int id;
	public long junzhuId;
	public int type;//1-免费洗练，2-元宝洗练
	
	public XilianFirstRecord() {
		super();
	}

	public XilianFirstRecord(int id, long junzhuId, int type) {
		super();
		this.id = id;
		this.junzhuId = junzhuId;
		this.type = type;
	}
	
}
