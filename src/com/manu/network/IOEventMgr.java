package com.manu.network;

import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOEventMgr {
	public static IOEventMgr inst = new IOEventMgr();
	public boolean work = true;
	public static Logger log = LoggerFactory.getLogger(IOEventMgr.class);
	public void exceptionCaught(NextFilter nextFilter, IoSession session,
			Throwable cause) throws Exception {
		long cur = System.currentTimeMillis();
		log.info("last read {} write {}, cur {} PD {}",session.getLastReadTime(),session.getLastWriteTime(),
				cur,
				session.getAttribute("IOEventMgr_lastMsg"));
	}
	public void messageReceived(NextFilter nextFilter, IoSession session,
			Object message) throws Exception {
		session.setAttribute("IOEventMgr_lastMsg", message);
	}
}
