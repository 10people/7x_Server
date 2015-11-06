package com.manu.dynasty.store;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import redis.clients.jedis.JedisPubSub;

public class RedisPubSubListener extends JedisPubSub {
	public ConcurrentMap<String, IMessageListener> messageListeners =
		new ConcurrentHashMap<String, IMessageListener>();
	
	public void registerListener(String channel, IMessageListener listener){
		this.messageListeners.put(channel, listener);
	}
	
	@Override
	public void onMessage(String channel, String message) {
		IMessageListener listener = this.messageListeners.get(channel);
		if (listener != null) {
			listener.onMessage(message);
		}
	}

	@Override
	public void onPMessage(String pattern, String channel, String message) {

	}

	@Override
	public void onPSubscribe(String pattern, int subscribedChannels) {


	}

	@Override
	public void onPUnsubscribe(String pattern, int subscribedChannels) {


	}

	@Override
	public void onSubscribe(String channel, int subscribedChannels) {

	}

	@Override
	public void onUnsubscribe(String channel, int subscribedChannels) {

	}

}
