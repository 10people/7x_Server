package com.manu.dynasty.chat;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
		key = chKey +"_hset:";
		initId();
	}
	public void initId(){
		/*List<ChatPct.Builder> list = (List<qxmobile.protobuf.Chat.ChatPct.Builder>) 
				Redis.getInstance().lrange(key, ChatPct.getDefaultInstance(), -1, -1);
		if(list.size() == 0){
			chatIdGen = new AtomicInteger(0);
			log.info("{} 没有聊天记录", key);
		}else{
			qxmobile.protobuf.Chat.ChatPct.Builder cb = list.get(0);
			chatIdGen = new AtomicInteger(cb.getSeq());
			log.info("{} 有聊天记录，id值到了 {}",key, cb.getSeq());
		}
		*/
		Map<byte[], byte[]> chatDataMap = Redis.getInstance().hgetAll(key.getBytes());
		if(chatDataMap == null || chatDataMap.size() == 0) {
			chatIdGen = new AtomicInteger(0);
		} else {
			int initId = 0;
			Set<byte[]> keySet = chatDataMap.keySet();
			for(byte[] key : keySet) {
				initId = Math.max(Integer.parseInt(new String(key)), initId);
			}
			chatIdGen = new AtomicInteger(initId + 1);
		}
	}
	
	protected void saveChatRecord(ChatPct.Builder cm) {
		//存库。
		cm.setSeq(chatIdGen.incrementAndGet());
		// 没有语音数据，不保存
		if(cm.getSoundLen() <= 0) {
			return;
		}
		Redis.getInstance().hset(key.getBytes(), String.valueOf(cm.getSeq()).getBytes(), cm.build());
		//Long sizeAfterAdd = Redis.getInstance().rpush(key, cm.build());
		Long sizeAfterAdd = Redis.getInstance().hlen(key.getBytes());
		if(sizeAfterAdd > maxLogSize){
			Map<byte[], byte[]> chatDataMap = Redis.getInstance().hgetAll(key.getBytes());
			int delId = Integer.MAX_VALUE;
			Set<byte[]> keySet = chatDataMap.keySet();
			for(byte[] key : keySet) {
				delId = Math.min(Integer.parseInt(new String(key)), delId);
			}
			Redis.getInstance().hdel(key.getBytes(), String.valueOf(delId).getBytes());
		}
	}
}
