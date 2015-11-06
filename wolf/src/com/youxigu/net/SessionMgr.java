package com.youxigu.net;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youxigu.boot.Config;

/**
 * 还要启动一个线程用来管理gameSvr的连接情况，如果gameSvr没有影响，这边要关闭连接
 * 
 * @author wuliangzhu
 *
 */
public class SessionMgr implements Runnable{
	private static int TIME_OUT = 10000;
	private static Logger logger = LoggerFactory.getLogger(SessionMgr.class);
	private static Map<IoSession, Worker> sessionMap = new ConcurrentHashMap<IoSession, Worker>();
	
	static void sessionOpened(IoSession session) {
		// 登录Session
		WolfMessageChain chain = WolfMessageChain.class.cast(session.getHandler());
		chain.clientList.add(session);
		
		Worker worker = new Worker(session);
		sessionMap.put(session, worker);
		worker.start();
		
		session.setAttribute("hb", new HeartBeatState());
	}
	
	static void sessionClosed(IoSession session) {
		Worker worker = sessionMap.remove(session);
		if (worker != null)
			worker.stop();
		
	//	ClientConnection.unRegister(session);
	}
	
	/**
	 * 遍历所有的客户端，发送heartBeat请求，如果没有回应，直接断开，
	 * 发送频率可以配置
	 */
	public void run() {
		for (;;) {
			try {
				if(sessionMap == null) {
					break;
				}
				Set<IoSession> sessions = sessionMap.keySet();
				StringBuffer sb = new StringBuffer();
				for (IoSession session : sessions) {
					HeartBeatState hbState = (HeartBeatState)session.getAttribute("hb");
					sb.append(session.getRemoteAddress());
					sb.append("-> 状态：");
					sb.append(hbState.state == 0 ? "正常" : "超时");
					sb.append(" 超时次数：" + hbState.timeoutTimes);
					sb.append(";");
					
					if (hbState != null) {
						// 如果处于waiting，则timeouttimes++
						if (hbState.state == HeartBeatState.WAITING) {
							++hbState.timeoutTimes;
							logger.error("连接超时：" + session.getRemoteAddress());
						}
						
						if (hbState.state == HeartBeatState.IDLE) {
							hbState.timeoutTimes = 0;
						}
						
						// 如果次数大于3次就关闭连接
						if (hbState.state == HeartBeatState.WAITING && hbState.timeoutTimes == 3) {
							hbState.state = HeartBeatState.CLOSED;
							logger.error("连接超时，需要关闭：" + session.getRemoteAddress());
							session.close();
							
							continue;
						}
						
						session.write(HeartBeatTask.instance);
						hbState.state = HeartBeatState.WAITING;
					} 
				}
				try {
					Thread.sleep(TIME_OUT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(TIME_OUT);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} 
		}
	}
	
	/**
	 * 启动一个新的线程，设置线程名字sessionMgr
	 */
	public static void start() {
		String toStr = Config.get("heartBeatTimeout");
		if (toStr == null || toStr.trim().length() == 0) {

		}else {
			try {
				TIME_OUT = Integer.parseInt(toStr);
				TIME_OUT *= 1000;
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Thread thread = new Thread(new SessionMgr());
		thread.setName("sessionMgr");
		thread.setDaemon(true);
		thread.start();
	}
	
	/**
	 * 接受op请求，并按key进行分组，如果一次处理中同一个key的op有多个，只处理最后一个前几个直接抛弃；
	 * 
	 * 如果已经有一个请求为delete，则这个key关联的将不接受任何请求了
	 * 
	 * @author wuliangzhu
	 *
	 */
	static class Worker implements Runnable{
		private static int workerNum = 1; // 用来标识worker
		private CircularQueue<String> ops = new CircularQueue<String>(1000); // 用来接收外界的同步请求
		Thread thread;
		volatile boolean running = true; // 如果被打断设置为1 ，否则为0
		private IoSession session;
		private WolfMessageChain chain;
		
		Worker(IoSession session) {
			this.session = session;
			this.chain = WolfMessageChain.class.cast(session.getHandler());
		}
		
		public int getQueueStat() {
			return 0;
		}
		
		/**
		 * 1 如果没有请求，则等待；
		 * 2 如果有请求，则从头取出请求，并判断当前添加到事务的个数是否达到最大，如果是则直接进行事务执行与提交
		 */
		public void run () {
			logger.info("{} -> {} has started！！", thread.getName(), session);
			while (running) {
				try {
					List<String> opList = ops.pollBulk();
					if (opList.size() > 0) {
						//FlushKey key = new FlushKey(opList);				
						//chain.broadcast(session, key);
					}
					
					Thread.sleep(1);
				} catch (Exception e) {
					logger.error(e.toString(), e);
				}
			}

			logger.info("{} -> {} has stoped！！", thread.getName(), session);
		}
		
		public void start() {
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.setName("FlushKey-Demo-" + Worker.workerNum++);
			thread.start();
		}
		
		public void stop() {
			this.running = false;
		}
		public void add(String op) {
			ops.offer(op);
		}

	}
	
	static class HeartBeatState {
		public static final int IDLE = 0;
		public static final int WAITING = 1;
		public static final int CLOSED = 2;
		
		volatile int state = 0; // 休闲还是等待
		int timeoutTimes = 0; // 超时次数 
	}
}
