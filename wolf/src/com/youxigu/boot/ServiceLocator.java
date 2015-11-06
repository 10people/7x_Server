package com.youxigu.boot;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ServiceLocator {
	protected static Map<String, Object> services = new HashMap<String, Object>();
	
	public ServiceLocator() {
	}
	public static <T> T get(String name, Class<T> clazz) {
		Object o = services.get(name);
		return clazz.cast(o);
	}
	
	/**
	 * 这个的没有设计好，用来初始化所有的Service，等设计好了再改吧，现在先写死 了
	 * 
	 */
	public  void register() {
		services.put("testService", "com.youxigu.app.TestService");
		services.put("cacheService", "com.youxigu.cache.CacheService");
		services.put("remoteWolfService", "com.youxigu.net.RemoteWolfService");
		services.put("startJobService", "com.youxigu.node.job.StartJobService");
		services.put("synDbHeartService", "com.youxigu.app.SynDbHeartService");
		services.put("SMS4Tencent", "com.youxigu.sms.SMS4Tencent");
		Set<Entry<String, Object>> entrySet = services.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			try {
				entry.setValue(Class.forName((String) entry.getValue()).newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
