package com.youxigu.net;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocketContext {
	private Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();
	
	public void put(String key, Object value) {
		this.attributes.put(key, value);
	}
	
	public <T> T get(String key, Class<T> clazz) {
		return clazz.cast(attributes.get(key));
	}
}
