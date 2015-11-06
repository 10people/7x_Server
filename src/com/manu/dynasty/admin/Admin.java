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
	private static final long serialVersionUID = -8626982160229423853L;
	@Id
	private long id;
	private String name;
	private String pwd;
	private Date predate;// 上次登录时间
	private Date updatetime;// 注册时间
	private long createuser;// 注册人

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}

	public long getCreateuser() {
		return createuser;
	}

	public void setCreateuser(long createuser) {
		this.createuser = createuser;
	}

	public Date getPredate() {
		return predate;
	}

	public void setPredate(Date predate) {
		this.predate = predate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	@Override
	public long getIdentifier() {
		// TODO Auto-generated method stub
		return 0;
	}
}
