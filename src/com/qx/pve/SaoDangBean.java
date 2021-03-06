package com.qx.pve;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;

@Entity
@Table(name = "SaoDang")
public class SaoDangBean implements DBHash {
	@Id
	public long jzId;
	
	public Date saoDangResetTime;//扫荡次数重置时间
	
	public int jySaoDangTimes;//今日扫了几次
	public int jyAllSaoDangTimes = 0;// 历史扫荡普通关卡总次数 
	
	@Override
	public long hash() {
		return jzId;
	}
}
