package com.qx.test.main;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.BigSwitch;
import com.manu.network.MessageMgr;
import com.manu.network.PD;
import com.manu.network.msg.AbstractMessage;
import com.manu.network.msg.ProtobufMsg;
import com.qx.test.message.MessageDispatcher;

public class ClientHandler extends IoHandlerAdapter{
	
	public ClientHandler() {
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		System.out.println("ClientHandler.exceptionCaught()");
		cause.printStackTrace();
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		if(message instanceof Builder){
			MessageMgr.getInst().addMessage(session, (Builder) message);
		}else if(message instanceof ProtobufMsg){
			ProtobufMsg mf = (ProtobufMsg) message;
//			MessageDispatcher.msgDispatcher(mf.id, mf.builder,session);
			MessageDispatcher dispatcher = (MessageDispatcher) session.getAttribute("router");
			dispatcher.msgDispatcher(mf.id, mf.builder, session);
		}else if(message instanceof AbstractMessage){
			BigSwitch.getInst().route((AbstractMessage) message);
		}
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		super.messageSent(session, message);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		super.sessionClosed(session);
//		System.out.println("ClientHandler.sessionClosed()");
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		super.sessionCreated(session);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		super.sessionIdle(session, status);
		//System.out.println("ClientHandler.sessionIdle()");
		session.write(PD.TEST_CONN);
		GameClient cl = (GameClient) session.getAttribute("CL", null);
		if(cl!=null){
			cl.lasdPveId = 0;
			cl.testTask.tryIds.clear();
			System.out.println("===========reset sid "+session.getId());
		}
		Integer times = (Integer) session.getAttribute("TIMES", 0);
		times++;
		if(times>5){
			System.out.println("================================!!!!!!!!!!!!!!===cut off sid "+session.getId());
			Main.autoDone(cl);
		}
		session.setAttribute("TIMES", times);
		session.write(PD.C_TaskReq);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
//		System.out.println("session opened");
	}

}
