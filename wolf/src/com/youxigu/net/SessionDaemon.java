package com.youxigu.net;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用来监护连接是否正常，若不正常，则进行重连：重连次数无限等待，每次重连间隔 3s
 * 
 * 利用startReconnectDamon来进行启动重连线程
 * 
 * 如果为了启动快速，可以在断开连接的时候 notifyDaemon
 * 
 * @author wuliangzhu
 *
 */
public class SessionDaemon implements Runnable{
	private static Logger logger = LoggerFactory.getLogger(SessionDaemon.class);
	private static ConcurrentMap<WolfClient, SessionDaemon> daemonMap = new ConcurrentHashMap<WolfClient, SessionDaemon>();
	
	private WolfClient client;
	private boolean stop = false;
	
	public void run() {		
		logger.info("reconnect worker start:" + client.toString() + " " + client.hashCode());
		
		while (!stop) {
			if (client.isAlive()) {
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			
			if(!client.reconnect()){
				try {
					logger.error("3s 后进行重试！！");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					logger.error(e.toString(), e);
				}
			}else {				
				// 连接成功要删除原来的连接对象
				logger.info("reconnect success:" + client.toString() + " " + client.hashCode());
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void stopReconnect(WolfClient client) {
		SessionDaemon old = daemonMap.get(client);
		if (old != null) {
			old.stop = true;
			
			daemonMap.remove(client);
		}
	}
	
	public static void startReconnectDaemon(WolfClient client) {
		logger.error("start Reconnect");
		if (client == null)
			return;
		
		synchronized (client) {
			SessionDaemon old = daemonMap.get(client);
			if (old != null) {
				logger.error("start Reconnect: 重连线程已经存在");
				return;
			}
			
			SessionDaemon daemon = new SessionDaemon();
			daemon.client = client;
			Thread t = new Thread(daemon);
			t.setName("sessionDaemon：" + client.toString() + " " + client.hashCode());
			t.setDaemon(true);
			t.start();
			
			daemonMap.putIfAbsent(client, daemon);
			
			// 设置线程
			IoSession session = client.getSession();
			session.setAttribute("daemonThread", t);			
		}
	}
	
	/**
	 * 唤醒连接管理线程，进行重连处理
	 * @param session
	 */
	public static void notifyDeamon(IoSession session) {
		Object o = null;
		if (session != null && (o = session.getAttribute("daemonThread")) != null) {
			Thread t = (Thread)o;
			if (t.isAlive()) {
				t.interrupt();
			}
		}
	}
}
