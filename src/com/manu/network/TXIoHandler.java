package com.manu.network;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handler cannot be set while the service is active. 
 * 启动后不能更换，所以代理给另外一个类吧
 * @author 康建虎
 * 
 */
public class TXIoHandler  implements IoHandler  {
	public static Logger log = LoggerFactory.getLogger(TXIoHandler.class);

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		IOHandlerImpl.inst.messageReceived(session, message);
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		IOHandlerImpl.inst.messageSent(session, message);
	};

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		IOHandlerImpl.inst.sessionCreated(session);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		IOHandlerImpl.inst.sessionOpened(session);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		IOHandlerImpl.inst.sessionClosed(session);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		IOHandlerImpl.inst.exceptionCaught(session, cause);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		IOHandlerImpl.inst.sessionIdle(session, status);		
	}

}
