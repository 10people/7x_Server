package com.qx.pve;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;


@Entity
@Table(name = "BuZhen")
public class BuZhenBean implements MCSupport{
	/**
	 * @Fields serialVersionUID : TODO
	 */
	public static final long serialVersionUID = 1L;
	@Id
	public long id;
	public long pos1;
	public long pos2;
	public long pos3;
	public long pos4;
	public long pos5;
	@Override
	public long getIdentifier() {
		return id;
	}
}
