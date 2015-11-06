package com.manu.dynasty.chat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.Chat.ChatPct;

import com.manu.dynasty.store.Redis;

/**
 * 频道日志，用于存库。
 * @author 康建虎
 *
 */
public abstract class ChatChLog {
	public long maxLogSize = 10000;
	public static Logger log = LoggerFactory.getLogger(ChatMgr.class);
	public AtomicInteger chatIdGen;
	public String key;
	public ChatChLog(String chKey){
		key = chKey;
		initId();
	}
	public void initId(){
		List<ChatPct.Builder> list = (List<qxmobile.protobuf.Chat.ChatPct.Builder>) 
				Redis.getInstance().lrange(key, ChatPct.getDefaultInstance(), -1, -1);
		if(list.size() == 0){
			chatIdGen = new AtomicInteger(0);
			log.info("{} 没有聊天记录", key);
		}else{
			qxmobile.protobuf.Chat.ChatPct.Builder cb = list.get(0);
			chatIdGen = new AtomicInteger(cb.getSeq());
			log.info("{} 有聊天记录，id值到了 {}",key, cb.getSeq());
		}
	}
	
	protected void saveChatRecord(ChatPct.Builder cm) {
		//存库。
		cm.setSeq(chatIdGen.incrementAndGet());
		Long sizeAfterAdd = Redis.getInstance().rpush(key, cm.build());
		if(sizeAfterAdd>maxLogSize){
			Redis.getInstance().lpop(key);
		}
	}
}
