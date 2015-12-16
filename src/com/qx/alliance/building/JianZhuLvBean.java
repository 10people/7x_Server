package com.qx.alliance.building;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class JianZhuLvBean {
	@Id
	public int lmId;
	
	public int keZhanLv;
	public int shuYuanLv;
	public int tuTengLv;
	public int shangPuLv;
	public int zongMiaoLv;
}
