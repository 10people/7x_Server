package com.qx.junzhu;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;
import com.qx.persistent.MCSupport;

@Entity
@Table(name="talent_attr")
public class TalentAttr implements MCSupport,DBHash{
	/**
	 * @Fields serialVersionUID : TODO x
	 */
	public static final long serialVersionUID = 1L;
	@Id
	public long junId;
	// 武艺精气值
	public int wuYiJingQi;
	// 进攻点数
	public int jinGongDianShu;
	// 体魄精气值
	public int tiPoJingQi;
	// 防守点数
	public int fangShouDianShu;

	public TalentAttr(){}
	
	@Override
	public long getIdentifier() {
		return junId;
	}
	public TalentAttr(long junId){
		this.junId = junId;
	}

	@Override
	public long hash() {
		return junId;
	}
}
