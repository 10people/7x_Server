package com.manu.network.internal;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.MessageMgr;
import com.manu.network.SessionAttKey;
import com.manu.network.SessionManager;

/**
 * 内部服务器之间socket连接的处理器。
 * @author 康建虎
 *
 */
public class InternalIoHandler  extends IoHandlerAdapter {
	private static Logger eLogger = LoggerFactory.getLogger("exception");
	public static Logger log = LoggerFactory.getLogger("stdout");
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		super.messageReceived(session, message);
		log.debug("收到消息 {}", message);
		
		if(message instanceof Builder){
			MessageMgr.getInst().addMessage(session, (Builder) message);
		}else{
			eLogger.error("未知的消息类型 {}",message);
		}
	}
	
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		super.sessionCreated(session);
	}
}
