package com.yy;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class YYLvRewardInfo {
	@Id
	public long jzId;
	public String lvs;
}
