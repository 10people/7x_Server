package com.youxigu.net;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.util.UtilDate;
//import com.youxigu.boot.Config;

/**
 * 获取配置的服务队列来处理消息
 * 
 * @author wuliangzhu
 *
 */
public class WolfMessageChain extends IoHandlerAdapter implements IInitListener{
	private static Logger logger = LoggerFactory.getLogger(WolfMessageChain.class);
	private static Logger perfLogger = LoggerFactory.getLogger("perf");
	
	Queue<IoSession> clientList = new ArrayBlockingQueue<IoSession>(50);
	List<IWolfService> services;
	private Worker worker = null;
	/**
	 * 初始化Services列表
	 * 
	 */
	public void init(SocketContext context) {
		worker = new Worker();
		Thread t = new Thread(worker);
		t.setName("wolf-main");
		t.setDaemon(true);
		t.start();		
	}
	

	@Override
	public void shutdown() {

		
	}
	
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		worker.addRequest(new Request(session, message));
	}

	private void handleMessageImpl(IoSession session, Object message) {
		Response response = new Response(session);
		if (this.services == null || this.services.size() == 0) {
			return;
		}
		
		int size = this.services.size();
		IWolfService service = null;
		
		boolean handled = false;
		
		long s = 0;
		if (perfLogger.isDebugEnabled()) {
			s = System.currentTimeMillis(); // 开始时间
		}
		for (int i = 0; i < size; i++) {
			service = this.services.get(i);
			if (service.handleMessage(response, message)) {
				handled = true;
				break;
			}
		}
		
		if (perfLogger.isDebugEnabled()) {
			long e = System.currentTimeMillis();
			long lag = e - s;
			if (lag > 1) {
				perfLogger.debug("{} 处理耗时过长:{}", message, lag);
			}
		}
		
		if (!handled) {
			logger.error("收到没有service处理的消息：" + message);
		}
	}

	/**
	 * 可以动态添加Service，这个是作为优先级最低加入
	 * 
	 * @param service
	 */
	public void addLast(IWolfService service) {
		this.services.add(this.services.size(), service);
	}
	
	/**
	 * 加入优先级最高的service
	 * @param service
	 */
	public void addFirst(IWolfService service) {
		this.services.add(0, service);
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		super.exceptionCaught(session, cause);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		if(session != null){
			System.out.println("[" + UtilDate.datetime2Text(new Date()) + "] 断开连接，address:" + session.getRemoteAddress());
		}
		int size = this.services.size();
		IWolfService service = null;
		for (int i = 0; i < size; i++) {
			service = this.services.get(i);

			if (service instanceof ISessionListener) {
				ISessionListener sl = ISessionListener.class.cast(service);
				sl.close(new Response(session));
			}
		}
		session.close();
		
		// 注销Session
		WolfMessageChain chain = WolfMessageChain.class.cast(session.getHandler());
		chain.clientList.remove(session);
		
		SessionDaemon.notifyDeamon(session);
		
//		if (Config.isWolfServer()) {
//			SessionMgr.sessionClosed(session);
//		}
	}
	
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		if (this.services == null)
			return;
		
		int size = this.services.size();
		IWolfService service = null;
		for (int i = 0; i < size; i++) {
			service = this.services.get(i);
			if (service instanceof ISessionListener) {
				ISessionListener sl = ISessionListener.class.cast(service);
				sl.open(new Response(session));
			}
		}
		if(session != null){
			System.out.println("[" + UtilDate.datetime2Text(new Date()) + "] 连接成功，address:" + session.getRemoteAddress());
		}
	}
	
	/**
	 * 给特定session发送消息
	 * @param sessionId
	 * @param message
	 */
	void send(IoSession session, Object message) {
		if (session != null && session.isConnected()){
			session.write(message);
		}
	}
	
	/**
	 * 给除了指定sessionId以外的所有session发送消息
	 * @param excludeSessionId
	 * @param message
	 */
	void broadcast(IoSession excludesSession, Object message) {
		try {
			for (IoSession session : this.clientList) {
				if (session != excludesSession && session.isConnected()) {
					session.write(message);
				}
			}
		}catch (Exception e) {
			logger.error("broadcast error:" + e.getMessage());
		}
	}
	
	private class Worker implements Runnable {
		private CircularQueue<Request> mq = new CircularQueue<Request>(10000);
		public void addRequest(Request req) {
			try {
				mq.offer(req);
			}catch (Exception e) {
				logger.error("请求队列加入失败：{},继续尝试100ms" + e);
				mq.offer(req);
			}
		}
		
		public void run () {
			Request req = null;
			logger.info("logic worker started!!!");
			while (true) {
				try {
					if ((req = mq.take()) != null) {
						handleMessageImpl(req.session, req.message);
						
						List<Request> reqList = mq.pollBulk();
						for (Request tmpReq : reqList) {
							handleMessageImpl(tmpReq.session, tmpReq.message);
						}
					}
				} catch (Exception e) {
					logger.error(e.toString(), e);
					continue;
				}
			}
		}
	}
	
	static class Request {
		public Request(IoSession s, Object o) {
			this.session = s;
			this.message = o;
		}
		public IoSession session;
		public Object message;
	}

}
