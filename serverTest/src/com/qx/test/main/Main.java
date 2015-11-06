package com.qx.test.main;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.manu.network.PD;
import com.manu.network.ProtoBuffDecoder;
import com.manu.network.TXCodecFactory;


public class Main {
	public static AtomicInteger watchCnt;
	public static AtomicInteger conFailCnt = new AtomicInteger(0);;
	public static AtomicInteger regOkCnt = new AtomicInteger(0);
	public static AtomicInteger loginOkCnt = new AtomicInteger(0);
	public static AtomicInteger rndNameCnt = new AtomicInteger(0);
	public static AtomicInteger createRoleOkCnt = new AtomicInteger(0);
	public static AtomicInteger enterSceneCnt = new AtomicInteger(0);
	public static long startTime;
	public static IoConnector net;
	
	public static void main(String[] args) throws Exception{
		net = setup();
//		InetSocketAddress addr = new InetSocketAddress("192.168.3.80", 8586);
		InetSocketAddress addr = new InetSocketAddress("192.168.0.176", 8586);
		int cnt = 1;
		watchCnt = new AtomicInteger(cnt);
		String head = "2TestLoad2"+new Random().nextInt(99999);
		 startTime = System.currentTimeMillis();
		for(int i = 0; i < cnt; i++) {
			GameClient c = new GameClient(head+i);
			c.log = false;//i<2;
			c.launch(net, addr);
		}
		do{
			long cur = System.currentTimeMillis();
			System.out.println("时间:"+(cur - startTime)+
					" 连接:"+net.getManagedSessionCount()+
					" 失败连接:"+conFailCnt+
					" 已注册:"+regOkCnt+
					" 已登录:"+loginOkCnt+
					" 取名字:"+rndNameCnt+
					" 已创建角色:"+createRoleOkCnt+
					" 已进入场景:"+enterSceneCnt
					);
			Thread.sleep(1000*1);
		}while(net.isActive());
	}
	
	public static void finish(){
		long end = System.currentTimeMillis();
		System.out.println("共使用时间 "+(end-startTime));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		net.dispose();
	}

	public static IoConnector setup() {
		PD.init();
		final IoConnector connector = new NioSocketConnector();
		connector.setConnectTimeoutMillis(5000);
		final ProtoBuffDecoder protoBuffDecoder = new ProtoBuffDecoder();
		ProtocolCodecFactory codecFactory = new TXCodecFactory(){
			@Override
			public ProtocolDecoder getDecoder(IoSession s) throws Exception {
				return protoBuffDecoder;
			}
		};
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(codecFactory));
		ClientHandler clientHandler = new ClientHandler();
		connector.setHandler(clientHandler);
		return connector;
	}
}
