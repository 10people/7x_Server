package com.qx.huangye;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.qx.util.TableIDCreator;

@Entity
public class HYRewardStore {
	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
	public long id;//2015年4月17日16:57:30int改为long
	public int lianmengId;
	public int site;
	public int amount;
	public Date lastAllotTime;
	
	public HYRewardStore() {

	}

	public HYRewardStore(int lianmengId, int site, int amount, Date lastAllotTime) {
		//改自增主键为指定
		//2015年4月17日16:57:30int改为long
		this.id=(TableIDCreator.getTableID(HYRewardStore.class, 1L));
		this.lianmengId = lianmengId;
		this.site = site;
		this.amount = amount;
		this.lastAllotTime = lastAllotTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lianmengId;
		result = prime * result + site;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HYRewardStore other = (HYRewardStore) obj;
		if (lianmengId != other.lianmengId)
			return false;
		if (site != other.site)
			return false;
		return true;
	}
	
}
