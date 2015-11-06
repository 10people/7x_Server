package com.youxigu.net;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 只提供2个有效操作
 * take：获取数据
 * offer: 添加数据
 * 
 * @author wuliangzhu
 *
 * @param <T>
 */
public class CircularQueue<T> {
	private BlockingQueue<T> buffer;
	
	public CircularQueue(int maxNum) {
		this.buffer = new ArrayBlockingQueue<T>(maxNum);
	}
	
	public T take() throws InterruptedException {
		return this.buffer.take();
	}
	
	public List<T> pollBulk() throws InterruptedException {
		List<T> objList = new LinkedList<T>();
		this.buffer.drainTo(objList);
				
		return objList;
	}

	public boolean offer(T obj) {
		return this.buffer.offer(obj);
	}
	
	public int size() {
		return this.buffer.size();
	}
}
