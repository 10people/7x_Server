package com.manu.network;

import java.util.LinkedList;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.network.msg.ProtobufMsg;

public class IOLog extends IoFilterAdapter{
	public int idCnt = 5;
	public static IOLog inst;
	public static Logger log = LoggerFactory.getLogger(IOLog.class);
	public IOLog(){
		inst = this;
		TXSocketMgr.inst.acceptor.getFilterChain().remove("loglastpd");
		TXSocketMgr.inst.acceptor.getFilterChain().addLast("loglastpd", this);
	}
	
	@Override
	public void exceptionCaught(NextFilter nextFilter, IoSession session,
			Throwable cause) throws Exception {
		super.exceptionCaught(nextFilter, session, cause);
		LinkedList<Object> list = (LinkedList<Object>) session.getAttribute("IOLogIdKey");
		if(list == null){
			log.info("list is null, when {}", cause);
			return;
		}
		StringBuffer sb = new StringBuffer();
		for(Object o : list){
			if(o instanceof ProtobufMsg){
				ProtobufMsg mf = (ProtobufMsg) o;
				sb.append(mf.id);
				sb.append(",");
			}
		}
		log.info("ids {} when {}",sb.toString(), cause);
	}

	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session,
			Object message) throws Exception {
		super.messageReceived(nextFilter, session, message);
		LinkedList<Object> list = (LinkedList<Object>) session.getAttribute("IOLogIdKey");
		if(list == null){
			list = new LinkedList<Object>();
			session.setAttribute("IOLogIdKey", list);
		}
		list.addLast(message);
		if(list.size()>idCnt){
			list.remove();
		}
	}
}
