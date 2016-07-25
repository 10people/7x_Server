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
	public static final long serialVersionUID = 8779102259388395353L;
	
	@Id
	@Column(name = "db_id", unique = true, nullable = false)
	public long dbId;
	public Date date;
	public int num;
	@Override
	public long getIdentifier() {
		return dbId;
	}
}
