package com.youxigu.net;

import org.apache.mina.common.IoSession;

public final class Response {
	private IoSession session;
	public Response(IoSession session) {
		this.session = session;
	}
	
	public void write(Object message) {
		if (message != null) {
			if (this.session == null) {
				throw new RuntimeException("请先建立连接");
			}
			if (!session.isConnected() || session.isClosing()) {
				throw new RuntimeException("连接已经关闭，请重新建立连接");
			}
			
			this.session.write(message);
		}
	}
	
	public IoSession getSession() {
		return this.session;
	}
}
