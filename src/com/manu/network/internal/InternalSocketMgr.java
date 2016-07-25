package com.manu.network.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.handler.chain.ChainedIoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.network.TXSocketMgr;
//import com.youxigu.net.WolfClient;
//import com.youxigu.net.WolfServer;

/**
 * @author 康建虎
 *
 */
public class InternalSocketMgr {
	public static Logger eLog = LoggerFactory.getLogger("exception");
	public static Logger log = LoggerFactory.getLogger("stdout");
	public static InternalSocketMgr inst;
	/**
	 * 运营活动服务器
	 */
//	public WolfServer huoDongServer;
//	public WolfClient huoDongClient;
	public static InternalSocketMgr getInst(){
		if(inst == null){
			inst = new InternalSocketMgr();
		}
		return inst;
	}
	public InternalSocketMgr(){
		
	}
	
	/**
	 * 启动活动服务器，接收来自游戏服务器的链接。
	 */
	public void startSlaveSide(){
		Properties p = new Properties();
		InputStream in = TXSocketMgr.class.getResourceAsStream("/txSocket.properties");
		Reader r = new InputStreamReader(in, Charset.forName("UTF-8"));
		try {
			p.load(r);
		} catch (IOException e) {
			eLog.error("载入活动服务器配置出错.", e);
		}
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int port = Integer.parseInt(p.getProperty("huoDongServerPort"));
		IoHandler handler = new ChainedIoHandler();
		ProtocolCodecFactory codecFactory = new InternalCodecFactory();
		
//		WolfServer server = new WolfServer();
//		server.ip = p.getProperty("huoDongServerIP");
//		server.port = port;
//		server.handler = handler;
//		server.codecFactory = codecFactory;
//		server.serverName = p.getProperty("huoDongServerName");
//		
//		server.start();
//		huoDongServer = server;
//		log.warn("独立模块 start at {}:{}",server.ip,server.port);
	}
	
	/**
	 * 启动主服务器（即玩家默认进入的游戏服务器） 到  独立模块服务器的链接。 
	 */
	public void startMainSide(){
		Properties p = new Properties();
		InputStream in = TXSocketMgr.class.getResourceAsStream("/txSocket.properties");
		Reader r = new InputStreamReader(in, Charset.forName("UTF-8"));
		try {
			p.load(r);
		} catch (IOException e) {
			eLog.error("载入活动服务器配置出错.", e);
		}
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//
//		WolfClient client = new WolfClient();
//		client.serverIp = p.getProperty("huoDongServerIP");
//		client.serverPort = Integer.parseInt(p.getProperty("huoDongServerPort"));
//		client.handler = new InternalIoHandler();
//		client.services = Collections.EMPTY_LIST;
//		client.codecFactory = new InternalCodecFactory();
//		client.start();
//		huoDongClient = client;
//		
//		log.warn("连接至 {}:{}", client.serverIp, client.serverPort);
	}
}
