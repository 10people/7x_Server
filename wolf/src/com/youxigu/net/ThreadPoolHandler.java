package com.youxigu.net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 为了防止逻辑处理对接受数据包接受的影响，这里在收到Task之后我把task提交给一个线程池处理
 * 这个方法提供了messageReceiver的默认实现
 * 
 * @author wuliangzhu
 *
 */
public class ThreadPoolHandler  {
	private static ExecutorService pool;
	static {
		int n_processor = Runtime.getRuntime().availableProcessors();
		pool = Executors.newFixedThreadPool(n_processor);
	}
	
	@SuppressWarnings("unchecked")
	public static Future execute(Runnable task) {
		return pool.submit(task);
	}
	
	public static ExecutorService executor() {
		return pool;
	}
	
	public static void shutdown() {
		if (pool != null)
			pool.shutdown();
	}
	
}
