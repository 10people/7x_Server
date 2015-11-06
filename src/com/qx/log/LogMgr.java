package com.qx.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.InitBinder;

import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLite.Builder;
import com.googlecode.protobuf.format.JsonFormat;
import com.manu.dynasty.util.ProtobufUtils;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.mibao.MibaoMgr;

/**
 * @author hejincheng
 * 
 */
public class LogMgr {
	public static LogMgr inst;
	public Logger logger = LoggerFactory.getLogger(LogMgr.class);
	public static Map receiveProtoIdMap = Collections.synchronizedMap(new LRUMap());
	public static Map sendProtoIdMap = Collections.synchronizedMap(new LRUMap());
	public static boolean receiveConsole = false;// 接受消息后台输出
	public static boolean sendConsole = false;// 发送消息后台输出
	public static boolean receiveLog = false;// 接收消息日志记录
	public static boolean sendLog = false;// 发送消息日志记录

	public LogMgr() {
		inst = this;
		initData();
	}

	public void initData() {

	}

	public void setReceiveProtoIdLog(IoSession session, ProtobufMsg msg) {
		if (msg.id != PD.Spirite_Move) {
			List<ProtobufMsg> list = getReceiveProtoLog(session);
			if (null == list) {
				list = new ArrayList<ProtobufMsg>();
			}
			list.add(msg);
			if (receiveConsole) {
				logger.info("session id {},receive id {}", session.getId(),
						msg.id);
			}
			receiveProtoIdMap.put(session.getId(), list);
		}
	}

	public List<ProtobufMsg> getReceiveProtoLog(IoSession session) {
		return (List<ProtobufMsg>) receiveProtoIdMap.get(session.getId());
	}

	public void setSendProtoIdLog(IoSession session, Message message) {
		int protoId = ProtobufUtils.protoClassToIdMap.get(message.getClass());
		if (protoId != PD.Spirite_Move) {
			// 取出list
			List list = (List) getSendProtoLog(session);
			if (null == list) {
				list = new ArrayList();
			}
			// 添加数据
			list.add(message);
			// 存回list
			if (sendConsole) {
				logger.info("session id {},send id {},message {}",
						session.getId(), protoId,
						JsonFormat.printToString(message));
			}
			sendProtoIdMap.put(session.getId(), list);
		}
	}

	public void setSendProtoIdLog(IoSession session, ProtobufMsg message) {
		if (message.id != PD.Spirite_Move) {
			// 取出list
			List list = (List) getSendProtoLog(session);
			if (null == list) {
				list = new ArrayList();
			}
			// 添加数据
			list.add(message);
			if (sendConsole) {
				logger.info("session id {},send id {},message {}", session
						.getId(), message.id, JsonFormat
						.printToString((Message) message.builder.build()));
			}
			// 存回list
			sendProtoIdMap.put(session.getId(), list);
		}
	}

	public List getSendProtoLog(IoSession session) {
		return (List) sendProtoIdMap.get(session.getId());
	}  
	
	public Object getSendMsgByIndex(long sessionId,int index){
		return ((List) sendProtoIdMap.get(sessionId)).get(index);
	}
	 
}
