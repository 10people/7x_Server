package com.youxigu.net;

import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import com.youxigu.concurrent.Request;
import com.youxigu.concurrent.RequestDispatcher;


public class RequestDispatcherChain extends IoHandlerAdapter {
	
	private RequestDispatcher dispatcher;
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		if (this.dispatcher != null) {
			this.dispatcher.dispatch((Request)message);
		}
	}
	
}
