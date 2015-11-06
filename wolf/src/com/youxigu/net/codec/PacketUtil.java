package com.youxigu.net.codec;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.mina.common.ByteBuffer;

public class PacketUtil {
	private static ConcurrentMap<String, Constructor<?>> constructorCache = new ConcurrentHashMap<String, Constructor<?>>();
	private static ConcurrentMap<String, Class<?>> classCache = new ConcurrentHashMap<String, Class<?>>();
	public static void putUTFString(ByteBuffer buffer, String message) {
		if (message == null)
			return;
		
		byte[] data = null;
		try {
			data = message.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		buffer.putInt(data.length);
		buffer.put(data);
	}
	
	
	public static String getUTFString(ByteBuffer buffer) {
		int len = buffer.getInt();
		byte[] data = new byte[len];
		buffer.get(data);
		
		try {
			return new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void putObject(ByteBuffer buffer, Serializable obj) {
		if (obj == null)
			return;
		
		try {
			String packageName = obj.getClass().getName();
			PacketUtil.putUTFString(buffer, packageName);
			obj.encodeFields(buffer);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Serializable getObject(ByteBuffer buffer) {
		String packageName = PacketUtil.getUTFString(buffer);
		Constructor<?> clazz = PacketUtil.constructorCache.get(packageName);
		if (clazz == null) {
			try {
				Class<?>[] empty = {};
				clazz = Class.forName(packageName).getConstructor(empty);
			} catch (Exception e) {
				e.printStackTrace();
				
				return null;
			}
			PacketUtil.constructorCache.putIfAbsent(packageName, clazz);
		}
		
		try {
			Serializable o = (Serializable)clazz.newInstance((Object[])null);
			o.decodeFields(buffer);
			
			return o;
		} catch (Exception e) {
			e.printStackTrace();
		} 		
		
		return null;
	}

}
