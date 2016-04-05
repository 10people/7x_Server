package com.qx.junzhu;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PrepareJz {
	@Id
	public long jzId;
	public Date dt;
}
