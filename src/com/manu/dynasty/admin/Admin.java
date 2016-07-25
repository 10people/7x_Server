package com.manu.dynasty.admin;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;

@Entity
@Table(name = "qxadmin.adminuser")
public class Admin implements MCSupport {

	/**
	 * 
	 */
	public static final long serialVersionUID = -8626982160229423853L;
	@Id
	public long id;
	public String name;
	public String pwd;
	public Date predate;// 上次登录时间
	public Date updatetime;// 注册时间
	public long createuser;// 注册人
	@Override
	public long getIdentifier() {
		// TODO Auto-generated method stub
		return 0;
	}
}
