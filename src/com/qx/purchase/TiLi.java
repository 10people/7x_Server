package com.qx.purchase;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

@Entity
@Table(name = "TiLi")
public class TiLi implements MCSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8779102259388395353L;
	
	@Id
	@Column(name = "db_id", unique = true, nullable = false)
	private long dbId;
	private Date date;
	private int num;
	public long getDbId() {
		return dbId;
	}
	public void setDbId(long dbId) {
		this.dbId = dbId;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	@Override
	public long getIdentifier() {
		return dbId;
	}
}
