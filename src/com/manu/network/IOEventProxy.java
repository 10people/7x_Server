package com.manu.network;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;

public class IOEventProxy extends IoFilterAdapter{

	@Override
	public void exceptionCaught(NextFilter nextFilter, IoSession session,
			Throwable cause) throws Exception {
		super.exceptionCaught(nextFilter, session, cause);
		IOEventMgr.inst.exceptionCaught(nextFilter, session, cause);
	}

	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session,
			Object message) throws Exception {
		super.messageReceived(nextFilter, session, message);
		IOEventMgr.inst.messageReceived(nextFilter, session, message);
	}
}
