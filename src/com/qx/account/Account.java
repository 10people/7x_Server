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
	
	public static final long serialVersionUID = -342820533566541836L;
	
	@Id
	@Column(name = "account_id", unique = true, nullable = false)
	public int accountId;
	
	@Column(name = "account_name", unique = true, nullable = false)
	public String accountName;
	
	@Column(name = "account_pwd", nullable = false)
	public String accountPwd;
	
	public Account(){}

	@Override
	public long getIdentifier() {
		return accountId;
	}
}
