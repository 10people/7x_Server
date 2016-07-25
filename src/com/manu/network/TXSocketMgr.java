package com.manu.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.SimpleIoProcessorPool;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioProcessor;
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
		acceptor = new NioSocketAcceptor(16);
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
		//
		try{
			parseProcessorPool();
		}catch(Exception e){
			log.error("解析错误",e);
		}
	}
	/* ----------------下面的代码是给从后台修改处理器个数用的 ----------------*/
	public SimpleIoProcessorPool<AbstractIoSession> pool;
	public Field poolArrField;
	public IoProcessor<?>[] procsArr;
	public void parseProcessorPool()throws Exception{
		NioSocketAcceptor acc = TXSocketMgr.inst.acceptor;
		Class<?> clz = acc.getClass().getSuperclass();
		Field f = clz.getDeclaredField("processor");
		f.setAccessible(true);
		pool = (SimpleIoProcessorPool<AbstractIoSession>)f.get(acc);
		poolArrField = SimpleIoProcessorPool.class.getDeclaredField("pool");
		poolArrField.setAccessible(true);
		procsArr = (IoProcessor<?>[])poolArrField.get(pool);
		//fixPoolSize(pool, poolArrField, procs);
	}

	public void fixPoolSize(int want)throws Exception{
		fixPoolSize(procsArr, want);
	}
	
	public void fixPoolSize(IoProcessor<?>[] procs,int want)
			throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InstantiationException,
			InvocationTargetException {
		//int want = 30;
		if(want<1){
			return;
		}
		if(procs.length<want){
			Field ef = SimpleIoProcessorPool.class.getDeclaredField("executor");
			ef.setAccessible(true);
			Executor executor = (Executor) ef.get(pool);
			IoProcessor<?>[] nps = new IoProcessor<?>[want];
			System.arraycopy(procs, 0, nps, 0, procs.length);
			for(int n=procs.length;n<want;n++){
				nps[n] = new NioProcessor(executor);
			}
			//
			poolArrField.set(pool,nps);
			procsArr = nps;
		}else if(procs.length == want){
			
		}else{
			IoProcessor<?>[] nps = new IoProcessor<?>[want];
			System.arraycopy(procs, 0, nps, 0, want);
			poolArrField.set(pool,nps);
			for(int i=want;i<procs.length; i++){
				procs[i].dispose();
			}
			procsArr = nps;
		}
	}
}
