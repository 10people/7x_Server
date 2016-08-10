package com.qx.alliance.building;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.qx.persistent.DBHash;

@Entity
public class JianZhuLvBean implements DBHash {
	@Id
	public int lmId;

	public int keZhanLv;
	public int shuYuanLv;
	public int tuTengLv;
	public int shangPuLv;
	public int zongMiaoLv;

	@Override
	public long hash() {
		return lmId * 1000;
	}
}
