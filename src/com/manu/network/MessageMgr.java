package com.manu.network;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;

/**
 * 消息管理器
 * @author 康建虎
 *
 */
public class MessageMgr {
	public static MessageMgr inst;
	public static MessageMgr getInst(){
		if(inst == null){
			inst = new MessageMgr();
		}
		return inst;
	}
	public MessageMgr(){
		
	}
	public void addMessage(IoSession ses, Builder msg) {
		
	}
	
}
