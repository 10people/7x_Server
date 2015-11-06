package com.youxigu.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import com.youxigu.boot.WolfConfig;
import com.youxigu.net.ResultMgr.TaskFuture;

public class WolfClient {
	private static Logger logger = LoggerFactory.getLogger(WolfClient.class);
	
	private static final String DEFAULT_CONFIG = "client.properties";
	private IoSession session;
	private int timeout = 30000; // 30s连接超时时间
	public String serverIp;
	public int serverPort;
	public IoHandler handler;
	private SocketConnector connector;
	private ExecutorService exector;
	private SocketContext context = new SocketContext();
	private ResultMgr resultMgr = new ResultMgr();

	public List<IWolfService> services;

	public ProtocolCodecFactory codecFactory;

	public boolean tcpNoDelay = true;
	
	public String toString() {
		return "WolfClient[" + serverIp + ":" + serverPort + "]";
	}
	
	public static WolfClient create() {
		return create(DEFAULT_CONFIG);
	}
	
	public static WolfClient create(String path) {
		WolfConfig config = WolfConfig.create(path);
		String ip = config.getServerIp();
		
		String serverPort = config.get(WolfConfig.PROPERTY_SERVER_PORT);
		int port = Integer.parseInt(serverPort == null ? new Integer(config.getPort()).toString() : serverPort);
		
		WolfClient ret = create(ip, port, config);
		return ret;
	}
	
	private void setSession(IoSession session) {
		this.session = session;
		if (this.session != null) {
			this.session.setAttribute("wolfClient", this);
		}
	}
	
	public IoSession getSession(){
		return this.session;
	}
	
	/**
	 * 连接一个服务器，并指定处理接收到的消息的处理方法
	 * 
	 * @param ip
	 * @param port
	 * @param config
	 * @return
	 */
	public static WolfClient create(String ip, int port, WolfConfig config) {
		WolfClient client = new WolfClient();
		client.serverIp = ip;
		client.serverPort = port;
		client.handler = config.getClientIoHandler();
		client.services = config.getServiceList();
		client.codecFactory = config.getCodecFactory();
		client.tcpNoDelay = "true".equalsIgnoreCase(config.get("wolf.tcpNoDelay")); // 是否
		return client;
	}
	
	public void start() {
		String threadPrefix = "wolfClient[" + this.serverIp + ":" + this.serverPort + "]";
		connector = new SocketConnector(4, (exector = Executors.newFixedThreadPool(5, new NamingThreadFactory(threadPrefix)))); 
		logger.info("启用多线程处理IO, threadNum -> {}", 4);
		
		SocketConnectorConfig config = new SocketConnectorConfig();
		DefaultIoFilterChainBuilder chain = config.getFilterChain();
		config.setThreadModel(ThreadModel.MANUAL);

		//add by wangweihua 2011-11-16 begin
       	((SocketSessionConfig)config.getSessionConfig()).setTcpNoDelay(tcpNoDelay);
       	logger.info("启用tcpNoDelay {}",tcpNoDelay);
      //add by wangweihua 2011-11-16 end
		
		chain.addLast("logger", new LoggingFilter());
		chain.addLast("codec", new ProtocolCodecFilter(codecFactory)); // 设置编码过滤器
		connector.setDefaultConfig(config);
		
		final ConnectFuture cf = connector.connect(new InetSocketAddress(
				this.serverIp, this.serverPort), handler);// 建立连接
		logger.info("连接中：" + serverIp + ":" + serverPort);
		long start = System.currentTimeMillis();
		while (true) { 
				if (!cf.isConnected()) {
					if ((System.currentTimeMillis() - start) > timeout){
						logger.warn("连接超时：" + serverIp + ":" + serverPort);
						break;
					}
					continue;
				}

				this.setSession(cf.getSession());
				logger.info("连接成功：" + serverIp + ":" + serverPort);
				
				if (handler instanceof WolfMessageChain) {
					WolfMessageChain wmc = WolfMessageChain.class.cast(handler);
					
					wmc.services = services;//
					for (IWolfService service : wmc.services) {
						if (service instanceof IInitListener) {
							IInitListener.class.cast(service).init(context);
						}
					}
					
					wmc.init(context);					
				}
				
				// 添加守护线程
				SessionDaemon.startReconnectDaemon(this);
				
				break;
		}
	}
	
	public boolean isAlive() {
		if (this.session != null && !this.session.isClosing() && this.session.isConnected()) {
			return true;
		}
		
		return false;
	}
	/**
	 * 提供重新建立连接的方法
	 * 连接成功返回 true，否则返回false
	 */
	synchronized boolean reconnect() {
		IoSession oldSession = this.session;
		
		if (this.isAlive()) {
			return true;
		}
		
		final ConnectFuture cf = connector.connect(new InetSocketAddress(
				this.serverIp, this.serverPort), handler);// 建立连接
		logger.info("重新连接中：" + serverIp + ":" + serverPort);
		long start = System.currentTimeMillis();
		while (true) {
				if (!cf.isConnected()) {
					if ((System.currentTimeMillis() - start) > timeout){
						logger.warn("连接超时：" + serverIp + ":" + serverPort);
						return false;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						logger.error(e.toString(), e);
					}
					continue;
				}

				this.setSession(cf.getSession());
				logger.info("连接成功：" + serverIp + ":" + serverPort);
				
				if (oldSession == null) {
					if (handler instanceof WolfMessageChain) {
						WolfMessageChain wmc = WolfMessageChain.class.cast(handler);
						wmc.init(context);
					}
				}
				
				return true;
		}
	}
	
	public void stop() {
		if (this.session != null)
			this.session.close();
		if (this.connector != null) {
			this.connector.setWorkerTimeout(1);
		}
		
		if (this.exector != null) {
			this.exector.shutdown();
			System.out.println("wolfClient threadPool shutdown!!");
		}
	}
	
	/**
	 * 可以发送任何类型数据了
	 * @param task
	 */
	public WriteFuture asynSendTask(Object task) {
		if (task != null) {
			if (this.session == null || this.session.isClosing() || !this.session.isConnected()) {
				if(/*!this.reconnect()*/true) {
					// SessionDaemon.startReconnect(this);
					throw new RuntimeException("连接已经断开，请重新建立连接！！");
				}
			}
			
			return this.session.write(task);
		}
		
		return null;
	}
	
	/**
	 * 同步的信息发送
	 *  1 发送数据的时候，会生成一个requestid,并且附带一个Future；
	 *  2 相应的时候恒为SetResult类型，把结果放到Future中，并把Future设置为ready；
	 *  
	 * @param serviceName 在ServiceLocator 注册的service名字
	 * @param methodName service的方法名字
	 * @param params 方法的参数 暂时不支持重载，所以方法的形参的显示类型一定要和实参的实际类型相同
	 * @param task
	 * @throws Exception 
	 */
	public <T> T sendTask(Class<T> resultType, String serviceName, String methodName, Object... params) throws Exception {
		SyncWolfTask task = new SyncWolfTask();
		task.setParams(params);
		task.setMethodName(methodName);
		task.setServiceName(serviceName);
		
		if (task != null && this.session != null) {
			if (this.session.isClosing() || !this.session.isConnected()) {
				synchronized(this){
					if(/*!this.reconnect()*/ true) {
						// SessionDaemon.startReconnect(this);
						throw new IOException("连接已经断开，请重新建立连接！！");
					}
				}
			}
			TaskFuture<Object> future = resultMgr.requestSent(task.getRequestId());
			this.session.write(task);
			
			int count = 0;
			
			if (logger.isDebugEnabled()) {
				logger.debug("同步请求开始：{} -> {}", serviceName, methodName);
			}
			
			try {
				future.get(20000, TimeUnit.MILLISECONDS);
			}catch (TimeoutException e) {
				resultMgr.requestCompleted(task.getRequestId(), e);
				throw e;
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("同步请求结束：{} ：{}", serviceName + "->" + methodName, future.isDone() + " " + count);
			}
			

			
			Object o = null;
			o = future.get();
			
			if (o == null) {
				return null;
			}
			
			if (o instanceof Exception) {
				throw Exception.class.cast(o);
			}
			
			return resultType.cast(o);
		} else if (this.session == null){
			logger.error("连接已经断开，请重新建立连接！！");
		}
		
		return null;
	}
	
	/**
	 * wolfserver连接心跳
	 * @author wuyj
	 *
	 */
	class WolfConnHeart extends Thread {
		WolfClient client;
		public WolfConnHeart(WolfClient c){
			this.client = c;
		}
		public void run(){
			while(client != null){
				try {
					client.asynSendTask(1);
					Thread.sleep(5 * 60 * 1000);
				} catch (Exception e) {
					e.printStackTrace();
					try {
						Thread.sleep(5 * 60 * 1000);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
}
