package com.youxigu.boot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.mina.common.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youxigu.net.IWolfService;
import com.youxigu.net.WolfHandler;

public class Config {
	private static Logger logger = LoggerFactory.getLogger(Config.class);
	public static final String SERVER_CONFIG_PATH = "/server.properties"; // 服务器的配置路径
	public static final String PROPERTY_SERVER_PORT = "wolfPort";
	public static final String PROPERTY_SERVER_IP = "wolfServer";
	public static final String PROPERTY_SERVER_CODECFACTORY = "codecFactory";
	public static final String PROPERTY_SERVER_IOHANDLER = "serverIoHandler";
	public static final String PROPERTY_CLIENT_IOHANDLER = "clientIoHandler";
	public static final String PROPERTY_SERVER_RUN_SINGLE = "isSingleWorker";
	public static final String PROPERTY_SERVICE_PRE = "service_";
	public static final String PROPERTY_NODE_ADDRESS = "node_";
	public static final String PROPERTY_MAIN_CLASS = "mainClass";
	public static final String OVERRIDE_PATH = "TX_CONF_PATH";
	
	private static boolean loadFinished = false; // 如果配置完毕设置为true
	
	private static Properties data = new Properties();
	
	public static String get(String key) {
		if ((!loadFinished) && data.size() == 0) {
			Config.loadConfig();
		}
		
		return data.getProperty(key);
	}
	
	public static List<String> getList(String key) {
		List<String> ret = new LinkedList<String>();
		String tmp = null;
		for (int i = 0;;i++) {
			tmp = data.getProperty(key + "_" + i);
			if (tmp == null) {
				break;
			}
			
			ret.add(tmp);
		}
		
		return ret;
	}
	public static boolean isWolfServer() {
		String flag = Config.get("isWolfServer");
		if (flag == null || flag.trim().length() == 0) {
			return false;
		}
		try {
			int intValue = Integer.parseInt(flag);
			
			return intValue == 1 ? true : false;
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static void printConfig() {
		System.out.println("==================GameConf=====================");
		Set<Entry<Object, Object>> sets = data.entrySet();
		for (Entry<Object, Object> s : sets) {
			System.out.println(s.getKey() + ":" + s.getValue());
		}
	}
	
	public static void loadConfig() {
		InputStream stream = Config.class.getResourceAsStream(Config.SERVER_CONFIG_PATH);
		try {
			if (stream == null){
				stream = Config.class.getResourceAsStream("/conf/server.properties");
			}
			Config.data.load(stream);
			Config.loadOverride();
		} catch (Exception e) {
			logger.info("load config error!");
		}finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void loadOverride() {
		if (loadFinished) {
			return;
		}
		
		String overridePath = System.getenv(OVERRIDE_PATH);
		if (overridePath == null) return;
		
		String confPath = overridePath + "/conf.properties";		
		
		loadConf(confPath);
		
	}

	public static void loadConf(String confPath) {
		File file = null;
		InputStream fileInput = null;
		try {
			
			file = new File(confPath);
			System.out.println(file.getAbsolutePath());
			if (file.exists()) {
				fileInput = new FileInputStream(file);
				Config.data.load(fileInput);
				
				loadFinished = true;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (file != null && fileInput != null) {
				try {
					fileInput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String[] getCacheServerList() {
		String svrList = Config.get("cacheServers");
		if (svrList == null) {
			return new String[]{"cacheServer:11211"};
		}
		String[] ret = svrList.split(",");
		return ret;
	}
	
	public static int getPort() {
		String portStr = Config.get(Config.PROPERTY_SERVER_PORT);
		if (portStr == null) {
			logger.error("服务器没有绑定端口，无法创建");
			
			return -1;
		}
		
		int port = 0; 
		try {
		 port = Integer.parseInt(portStr);
		} catch (Exception e) {
			logger.error("端口格式不正确，无法创建服务器");
			
			return -1;
		}
		
		return port;
	}
	
	public static String getServerIp() {
		String ipStr = Config.get(Config.PROPERTY_SERVER_IP);
		if (ipStr == null) {
			logger.error("服务器没有绑定端口，无法创建");
			
			return null;
		}
		
		return ipStr;
	}
	
	public static String getDbSynServerIp() {
		String ip = Config.get("dbSynServer");
		if (ip == null) {
			ip = getServerIp();
		}
		
		return ip;
	}
	
	public static int getDbSynServerPort() {
		String port = Config.get("dbSynPort");
		if (port == null) {
			port = Config.get("serverPort");
		}
		
		return Integer.parseInt(port);
	}
	
	public static IoHandler getServerIoHandler() {
		String className = Config.get(Config.PROPERTY_SERVER_IOHANDLER);
		IoHandler handler = null;
		if (className != null) {
			try {
				handler = (IoHandler) Class.forName(className).newInstance();
			} catch (Exception e) {
				logger.warn("读取配置文件错误：" + Config.PROPERTY_SERVER_IOHANDLER, e);
			}
		}
		
		if (handler == null) {
			handler = new WolfHandler();
		}
		
		return handler;
	}
	
	public static IoHandler getClientIoHandler() {
		String className = Config.get(Config.PROPERTY_CLIENT_IOHANDLER);
		IoHandler handler = null;
		if (className != null) {
			try {
				handler = (IoHandler) Class.forName(className).newInstance();
			} catch (Exception e) {
				logger.warn("读取配置文件错误：" + Config.PROPERTY_CLIENT_IOHANDLER, e);
			}
		}
		
		if (handler == null) {
			handler = new WolfHandler();
		}
		
		return handler;
	}
	
	public static ProtocolCodecFactory getCodecFactory() {
		String className = Config.get(Config.PROPERTY_SERVER_CODECFACTORY);
		ProtocolCodecFactory codec = null;
		if (className != null) {
			try {
				codec = (ProtocolCodecFactory) Class.forName(className).newInstance();
			} catch (Exception e) {
				logger.warn("读取配置文件错误：" + Config.PROPERTY_SERVER_CODECFACTORY, e);
			}
		}
		if (codec == null) {
			codec = new ObjectSerializationCodecFactory();
		}
		if(codec instanceof ObjectSerializationCodecFactory){
			//设置decoder的maxObjectSize
			System.out.println("配置了ObjectSerializationCodecFactory，设置maxObjectSize为 " + Config.get("decoder.maxObjectSize"));
			((ObjectSerializationCodecFactory)codec).setDecoderMaxObjectSize(Integer.parseInt(Config.get("decoder.maxObjectSize")));
		}
		return codec;
	}
	
	public static List<IWolfService> getServiceList() {
		List<IWolfService> ret = new ArrayList<IWolfService>();
		for (int i = 0; ;i++) {
			String className = Config.get(Config.PROPERTY_SERVICE_PRE + i);
			if (className == null) {
				break;
			}
			
			try {
				logger.info("add Service:" + className);
				ret.add(IWolfService.class.cast((Class.forName(className).newInstance())));
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
		return ret;
	}
	
	public static List<String> getNodeList() {
		List<String> ret = new ArrayList<String>();
		for (int i = 0; ;i++) {
			String address = Config.get(Config.PROPERTY_NODE_ADDRESS + i);
			if (address == null) {
				break;
			}
			
			try {
				logger.info("add node:" + address);
				ret.add(address);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
		return ret;
	}
}

