package com.manu.network;

import org.apache.mina.core.session.IoSession;

/**
 * socket连接里的会话用户。
 * @author 康建虎
 *
 */
public class SessionUser {
	public Long sessoinId;
	public String account;
	public IoSession session;
}
