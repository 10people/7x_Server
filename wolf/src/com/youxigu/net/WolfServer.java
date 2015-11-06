package com.youxigu.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.ExceptionMonitor;
import org.apache.mina.common.IoAcceptorConfig;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youxigu.boot.Config;

/**
 * 提供最方便使用的接口：
 * 1 监听的端口号；
 * 2 多少个线程处理消息；
 * 3 处理消息的逻辑；
 * 4 协议实现
 * 5 启动服务器；
 * 6 关闭服务器
 * 
 * @author wuliangzhu
 *
 */
public class WolfServer {
	private static Logger logger = LoggerFactory.getLogger(WolfServer.class);
	
	private SocketAcceptor acceptor = null; 
	public String ip;
	public int port;
	public IoHandler handler;
	public String serverName;
	public ProtocolCodecFactory codecFactory;
	private SocketContext context = new SocketContext();
	private ExecutorService exector;

	public boolean isSingle = true;
	public boolean tcpNoDelay = true;

	public int threadNum = 4;

	public List<IWolfService> serviceList;

	
	public static WolfServer create() {
		String portStr = Config.get(Config.PROPERTY_SERVER_PORT);
		
//		String dbSynMode = Config.get("dbSynMode");
//		if (dbSynMode != null && Integer.parseInt(dbSynMode) == 1) {
//			portStr = Config.get("dbSynPort");
//			System.out.println("启动了DbSynMode，监听端口为：" + portStr);
//		}
//		if (portStr == null) {
//			logger.error("服务器没有绑定端口，无法创建");
//			
//			return null;
//		}
		
		int port = 0; 
		try {
		 port = Integer.parseInt(portStr);
		} catch (Exception e) {
			logger.error("端口格式不正确，无法创建服务器");
			
			return null;
		}
		
		IoHandler handler = Config.getServerIoHandler();
		ProtocolCodecFactory codec = Config.getCodecFactory();
		String name = Config.get("name");

		WolfServer server = new WolfServer();
		server.ip = Config.get("internalIp");
		server.port = port;
		server.handler = handler;
		server.codecFactory = codec;
		server.serverName = name;
		String isSingleStr = Config.get("isSingleThread");
		server.serviceList = Config.getServiceList();
		String threadNumStr = Config.get("threadNum"); // 用来启动的线程数量
		String tcpNoDelayStr = Config.get("wolf.tcpNoDelay"); // 是否
		server.isSingle = true;
		if (isSingleStr != null) {
			String tmp = isSingleStr.trim();
			if (tmp.length() > 0) {
				int isSingleInt = Integer.parseInt(tmp);
				server.isSingle = isSingleInt == 1;
			}
		}
		if (threadNumStr != null) {
			try {
				server.threadNum = Integer.parseInt(threadNumStr);
			}catch (Exception e) {
				logger.warn("配置的线程数量格式不正确，启用默认线程数量：4！！");
			}
		}
       	server.tcpNoDelay = "true".equalsIgnoreCase(tcpNoDelayStr);
       	
		return server;
	}
	
	/**
	 * 启动服务，如果isSingleThread == 0 启动多线程
	 */
	public void start(){
		try {
			startImpl(isSingle);
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("服务器启动失败！！");
		}
	}

	private void startImpl(boolean single) {
		logger.info("开始启动服务器...");
		logger.info("开始设置监听端口和线程池...");
		if (single) {
			acceptor = new SocketAcceptor();
		}else {
			String threadPrefix = "wolfServer[" + this.port + "]";
			acceptor = new SocketAcceptor(threadNum, (exector = Executors.newFixedThreadPool(threadNum + 1, new NamingThreadFactory(threadPrefix)))); 
			logger.info("启用多线程处理IO, threadNum -> {}", threadNum);
		}
		
        IoAcceptorConfig config = new SocketAcceptorConfig();
       	config.setThreadModel(ThreadModel.MANUAL);
        
       	//add by wangweihua 2011-11-16 begin
  		((SocketSessionConfig)config.getSessionConfig()).setTcpNoDelay(tcpNoDelay);
  		logger.info("tcpNoDelay : {}",tcpNoDelay);
       	//add by wangweihua 2011-11-16 end       	
       	
        DefaultIoFilterChainBuilder chain = config.getFilterChain();
        //使用字符串编码
        logger.info("开始设置协议处理程序...");
        chain.addLast("codec", new ProtocolCodecFilter(this.codecFactory));
        
        // 配置handler的 logger 来关闭 无用的日志
        LoggingFilter loggingFilter = new LoggingFilter();
        chain.addLast("logging", loggingFilter); 
		
      //启动HelloServer
        try {
        	System.out.println("开始启动服务, ip=" + ip + ", port=" + port);
			acceptor.bind(new InetSocketAddress(ip, port), handler, config);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			ExceptionMonitor.getInstance().exceptionCaught(e);
		} catch (IOException e) {
			e.printStackTrace();
			ExceptionMonitor.getInstance().exceptionCaught(e);
		}
		
		logger.info("开始设置逻辑处理程序...");
		if (handler instanceof IInitListener) {
			IInitListener i = (IInitListener)handler;
			i.init(context);
		}
		
		if (handler instanceof WolfMessageChain) {
			WolfMessageChain wmc = WolfMessageChain.class.cast(handler);
			
			wmc.services = serviceList;
			for (IWolfService service : wmc.services) {
				if (service instanceof IInitListener) {
					IInitListener.class.cast(service).init(context);
				}
			}
			
			wmc.init(context);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run () {
				if (exector != null) {
					stop();
					exector.shutdown();					
					logger.info("wolf Server ExectorService released!!");
				}
			}
		}));
        logger.info("{} started on port {} -> single "  + single, serverName, port);
        logger.info("服务器启动成功...");
	}
	
	public void stop() {
		acceptor.unbindAll();
		if (handler != null && handler instanceof IInitListener) {
			IInitListener l = (IInitListener)handler;
			l.shutdown();
		}
	}

	public IoHandler getHandler() {
		return handler;
	}
}
