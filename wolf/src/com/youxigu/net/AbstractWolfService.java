package com.youxigu.net;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 提供一个异步实现，handleMessage只是判断是否可以处理，这个实现会启动一个线程来处理各种请求，至于对具体请求的处理
 * 要靠自己实现
 * 
 * 扩展注意：
 * 1 可以重载handleMessage来确定是否处理指定的消息；
 * 2 可以实现doHandleMessage来处理具体逻辑，也可以在这里方法里确定是否启动线程池来处理请求
 * 3 可以重载 doForSessionClose doForSessionOpened来进行资源管理
 * 
 * 然后再配置文件里配置成Service
 * @author wuliangzhu
 *
 */
public abstract class AbstractWolfService implements IWolfService, ISessionListener, Runnable{
	private BlockingQueue<Command> messageQueue = new LinkedBlockingQueue<Command>(1000);
	@SuppressWarnings({"unchecked" })
	private Map<Class, Object> interestMap = new HashMap<Class, Object>(); // 放入要处理的类型
	protected Queue<Response> clients = new ConcurrentLinkedQueue<Response>();
	
	public void close(Response response) {
		this.clients.remove(response);
		
		this.doForSessionClosed(response);
	}

	public void open(Response response) {
		this.clients.remove(response);
		this.clients.add(response);
		
		this.doForSessionOpened(response);
	}

	public AbstractWolfService() {
		Thread thread = new Thread(this);
		thread.setDaemon(false);
		thread.start();
	}
	
	public void run() {
		Command command = null;
		while (true) {
			try {
				command = this.messageQueue.take();
				this.doHandleMessage(command.response, command.message);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * 具体的处理请求的逻辑
	 * 
	 * @param response
	 * @param message
	 */
	protected abstract void doHandleMessage(Response response, Object message);
	
	/**
	 * 处理一些某个连接断开的资源回收工作
	 * @param response
	 */
	protected void doForSessionClosed(Response response) {
		
	}
	
	/**
	 * 处理一些某个连接打开的资源开辟工作
	 * @param response
	 */
	protected void doForSessionOpened(Response response) {
		
	}
	/**
	 * 是否处理指定的Message,只是把消息放入队列，不会立即执行
	 */
	public boolean handleMessage(Response response, Object message) {
		if (response == null || message == null) {
			return false;
		}
		
		if (this.interestMap.get(message.getClass()) == null) {
			return false;
		}
		
		Command command = new Command(response, message);
		return this.messageQueue.offer(command);
	}

	
	static class Command {
		public Command(Response response, Object message) {
			this.response = response;
			this.message = message;
		}
		Response response;
		Object message;
	}
}
