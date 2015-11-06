package com.youxigu.app;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 执行错误Sql的buffer，操作有三个
 * 1、添加
 * 2、删除
 * 3、获取
 * 
 * @author FengRui
 *
 * @param <T>
 */
public class ErrorManager<T> {
	
	private final int maxCapacity;
	
	private ConcurrentMap<Object, T> buffer;
	
	public ErrorManager(int maxCapacity) {
		this.buffer = new ConcurrentHashMap<Object, T>(maxCapacity);
		this.maxCapacity = maxCapacity;
	}
	
	public T remove(Object key) {
		return buffer.remove(key);
	}
	
	public void put(Object key, T value) {
		buffer.put(key, value);
	}

	/**
	 * 获取所有执行错误的操作
	 * @return
	 */
	public List<T> pollBulk() {
		List<T> bufferList = new LinkedList<T>();;
		if (this.size() > 0) {
			bufferList.addAll(buffer.values());
			this.buffer.clear();
		}
		return bufferList;
	}
	
	public int size() {
		return buffer.size();
	}
}
