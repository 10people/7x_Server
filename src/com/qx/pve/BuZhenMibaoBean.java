package com.qx.pve;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.DBHash;
import com.qx.persistent.MCSupport;


@Entity
@Table(name = "BuZhenMibao")
public class BuZhenMibaoBean implements MCSupport,DBHash{
	/**
	 * @Fields serialVersionUID : TODO
	 */
	public static final long serialVersionUID = 1L;
	@Id
	public long id;
	public long pos1;
	public long pos2;
	public long pos3;
	public int zuheId; 	//秘宝组合id
	@Override
	public long getIdentifier() {
		return id;
	}
	@Override
	public long hash() {
		return id;
	}
}
