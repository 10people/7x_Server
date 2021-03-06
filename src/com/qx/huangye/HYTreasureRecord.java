package com.qx.huangye;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.qx.persistent.DBHash;

/**
 * 联盟荒野挑战进度记录
 * 
 * @author lizhaowen
 * 
 */
@Entity
public class HYTreasureRecord implements DBHash {
	/** 联盟id */
	@Id
	public int lianMengId;

	/** 当前关卡 */
	public int curGuanQiaId;

	@Override
	public long hash() {
		return lianMengId * 1000;
	}

}
