package com.qx.account;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.qx.persistent.MCSupport;


/**
 * 2015年7月15日14:16:58，这个类恢复回来，作为账号系统的缓存。只有在本服务器登录过的，才做记录。
 * @author 康建虎
 *
 */
@Entity
@Table(name = "accounts")
public class Account implements  MCSupport{
	
	private static final long serialVersionUID = -342820533566541836L;
	
	@Id
	@Column(name = "account_id", unique = true, nullable = false)
	private int accountId;
	
	@Column(name = "account_name", unique = true, nullable = false)
	private String accountName;
	
	@Column(name = "account_pwd", nullable = false)
	private String accountPwd;
	
	public Account(){}
	
	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	
	@Override
	public long getIdentifier() {
		return accountId;
	}
	public String getAccountPwd() {
		return accountPwd;
	}

	public void setAccountPwd(String accountPwd) {
		this.accountPwd = accountPwd;
	}	
}
