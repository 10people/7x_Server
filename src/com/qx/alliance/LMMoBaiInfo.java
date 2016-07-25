package com.qx.alliance;

import java.io.Serializable;
import java.util.Date;

public class LMMoBaiInfo implements  Serializable {
	public static final long serialVersionUID = 1L;
	public Date lastDate;
	public int buffLevel;
	public Date getLastDate() {
		return lastDate;
	}
	public void setLastDate(Date lastDate) {
		this.lastDate = lastDate;
	}
	public int getBuffLevel() {
		return buffLevel;
	}
	public void setBuffLevel(int buffLevel) {
		this.buffLevel = buffLevel;
	}
	
	
}
