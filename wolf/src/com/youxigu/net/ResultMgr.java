package com.youxigu.net;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ResultMgr {
	private static Logger logger = LoggerFactory.getLogger(ResultMgr.class);
	private ConcurrentMap<Long, TaskFuture<Object>> requestMap = new ConcurrentHashMap<Long, TaskFuture<Object>>();
	
	public void requestCompleted(long requestId, Object result) {
		if (logger.isDebugEnabled()) {
			logger.debug("同步响应：{} -> {}", requestId, result);
		}
		
		TaskFuture<Object> future = requestMap.remove(requestId);
		if (future != null) {
			future.setResult(result);
			future.setReady(true);
		}else {
			logger.error("响应对应的请求已经超时或者已经响应过！！{} : {}", requestId, result);
		}
	}
	
	public synchronized TaskFuture<Object> requestSent(long requestId) throws Exception {
		TaskFuture<Object> future = new TaskFuture<Object>();
		requestMap.putIfAbsent(requestId, future);
		
		return future;
	}
	
	/**
	 * 标识出所有的同步请求，并设置一个异常
	 * 
	 * 加锁避免在清理完之后又有请求过来的情况
	 * @param e
	 */
	public synchronized  void notifyAllRequest(Exception e) {
		logger.error("连接已经关闭：清理所有等待的请求");
		Iterator<TaskFuture<Object>> ir = requestMap.values().iterator();
		while (ir.hasNext()) {
			TaskFuture<Object> future = ir.next();
			future.setReady(true);
			future.setResult(e);
		}
	}
	
	public static class TaskFuture<T> implements Future<T>{
		private final CountDownLatch latch = new CountDownLatch(1);
		private T result;
		private volatile boolean ready; // 多线程访问的同步问题
		public void setResult(T result) {
			this.result = result;
		}

		public void setReady(boolean ready) {
			this.ready = ready;
			latch.countDown();
		}

		/**
		 * 这种task不能取消，所以恒返回false
		 */
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}

		public T get() throws InterruptedException, ExecutionException {
			try {
				return this.get(30, TimeUnit.SECONDS);
			}catch (TimeoutException e) {
				System.out.println("===========time out============" + e.getMessage());
				return null;
			}
		}
		
		public T get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException,
				TimeoutException {
			if(!latch.await(timeout, unit)) {
				throw new TimeoutException("Timed out waiting for operation");
			}
			return result;
		}

		public boolean isCancelled() {
			return false;
		}

		public boolean isDone() {
			return ready;
		}
	}
}
