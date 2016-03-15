package com.manu.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.util.ExceptionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author 康建虎
 *
 */
public class TXSocketMgr {
	public static Logger log = LoggerFactory.getLogger(TXSocketMgr.class);
	public static TXSocketMgr inst;
	public static TXSocketMgr getInst(){
		if(inst == null){
			inst = new TXSocketMgr();
		}
		return inst;
	}
	public NioSocketAcceptor acceptor;
//	public WolfServer server;

	public void start() throws IOException{
		if(acceptor != null){
			log.error("acceptor is not null, do nothing.");
			return;
		}
		Properties p = new Properties();
		InputStream in = TXSocketMgr.class.getResourceAsStream("/txSocket.properties");
		Reader r = new InputStreamReader(in, Charset.forName("UTF-8"));
		p.load(r);
		in.close();
		int port = Integer.parseInt(p.getProperty("port"));
		IoHandler handler = new TXIoHandler();
		TXCodecFactory codecFactory = new TXCodecFactory();
		//
		acceptor = new NioSocketAcceptor();
//		OrderedThreadPoolExecutor orderedThreadPoolExecutor = new OrderedThreadPoolExecutor();
//		ExecutorFilter executorFilter = new ExecutorFilter( orderedThreadPoolExecutor );
//		acceptor.getFilterChain().addLast("executor", executorFilter);
		//TODO keep alive filter KeepAliveFilter mina提供的心跳接口。
		acceptor.getSessionConfig().setSoLinger(0);
		//Sets idle time for the specified type of idleness in seconds.
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 60*5);//5分钟
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilterFix(codecFactory.encoder, codecFactory.decoder));
//		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		acceptor.setHandler(handler);
		ExceptionMonitor.setInstance(new QXExceptionMonitor());
		//ip = "0.0.0.0";//腾讯要求
		acceptor.bind(new InetSocketAddress("0.0.0.0", port));
		log.info("启动socket，端口 {}", port);
		
//		server = new WolfServer();
//		server.ip = "0.0.0.0";//腾讯要求
//		server.port = port;
//		server.handler = handler;
//		server.codecFactory = codecFactory;
//		server.serverName = "腾讯网关";
//		
//		server.start();
	}
}
