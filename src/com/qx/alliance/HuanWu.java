package com.qx.alliance;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class HuanWu {
	@Id
	public long jzId;
	public String jzName;
	public int lmId;
	public String slot1;
	public String slot2;
	public String slot3;
	public String slot4;
	public String slot5;
}
