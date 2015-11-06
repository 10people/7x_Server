package com.youxigu.boot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.mina.common.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youxigu.net.IWolfService;
import com.youxigu.net.WolfHandler;

/**
 * 默认是wolf.cfg
 * 
 * 如果指定了，就以指定的为覆盖配置， TX_CONF_PATH 不需要配置了
 * @author wuliangzhu
 *
 */
public class WolfConfig {
	private static Logger logger = LoggerFactory.getLogger(WolfConfig.class);
	public static final String SERVER_CONFIG_PATH = "/wolf.cfg"; // 服务器的配置路径
	public static final String PROPERTY_SERVER_PORT = "serverPort";
	public static final String PROPERTY_SERVER_IP = "serverIp";
	public static final String PROPERTY_SERVER_CODECFACTORY = "codecFactory";
	public static final String PROPERTY_SERVER_IOHANDLER = "serverIoHandler";
	public static final String PROPERTY_CLIENT_IOHANDLER = "clientIoHandler";
	public static final String PROPERTY_SERVER_RUN_SINGLE = "isSingleWorker";
	public static final String PROPERTY_SERVICE_PRE = "service_";
	public static final String PROPERTY_NODE_ADDRESS = "node_";
	public static final String PROPERTY_MAIN_CLASS = "mainClass";
	public static final String OVERRIDE_PATH = "TX_CONF_PATH";
	
	private Properties data = new Properties();
	
	private WolfConfig(){};
	
	public static WolfConfig create(String file){
		WolfConfig config = new WolfConfig();
		config.loadConfig(file);
		
		return config;
	}
	
	public String get(String key) {
		return data.getProperty(key);
	}
	
	public boolean isWolfServer() {
		String flag = get("isWolfServer");
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
	
	public void loadConfig(String file) {
		InputStream stream = WolfConfig.class.getResourceAsStream("/wolf.cfg");
		try {
			if (stream == null){
				stream = WolfConfig.class.getResourceAsStream("/conf/wolf.cfg");
			}
			if (stream != null) {
				data.load(stream);
			}
			
			loadOverride(file);
		} catch (Exception e) {
			logger.info("load config error!");
		}finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void loadOverride(String f) {
		String overridePath = f;
		if (overridePath != null) {
			File file = null;
			InputStream fileInput = null;
			try {
				file = new File(overridePath);
				if (file.exists()) {
					fileInput = new FileInputStream(file);
					data.load(fileInput);
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
	}
	
	public String[] getCacheServerList() {
		String svrList = get("cacheServers");
		if (svrList == null) {
			return new String[]{"cacheServer:11211"};
		}
		String[] ret = svrList.split(",");
		return ret;
	}
	
	public int getPort() {
		String portStr = get(WolfConfig.PROPERTY_SERVER_PORT);
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
	
	public String getServerIp() {
		String ipStr = get(WolfConfig.PROPERTY_SERVER_IP);
		if (ipStr == null) {
			logger.error("服务器没有绑定端口，无法创建");
			
			return null;
		}
		
		return ipStr;
	}
	
	public IoHandler getServerIoHandler() {
		String className = get(WolfConfig.PROPERTY_SERVER_IOHANDLER);
		IoHandler handler = null;
		if (className != null) {
			try {
				handler = (IoHandler) Class.forName(className).newInstance();
			} catch (Exception e) {
				logger.warn("读取配置文件错误：" + WolfConfig.PROPERTY_SERVER_IOHANDLER, e);
			}
		}
		
		if (handler == null) {
			handler = new WolfHandler();
		}
		
		return handler;
	}
	
	public IoHandler getClientIoHandler() {
		String className = get(WolfConfig.PROPERTY_CLIENT_IOHANDLER);
		IoHandler handler = null;
		if (className != null) {
			try {
				handler = (IoHandler) Class.forName(className).newInstance();
			} catch (Exception e) {
				logger.warn("读取配置文件错误：" + WolfConfig.PROPERTY_CLIENT_IOHANDLER, e);
			}
		}
		
		if (handler == null) {
			handler = new WolfHandler();
		}
		
		return handler;
	}
	
	public ProtocolCodecFactory getCodecFactory() {
		String className = get(WolfConfig.PROPERTY_SERVER_CODECFACTORY);
		ProtocolCodecFactory codec = null;
		if (className != null) {
			try {
				codec = (ProtocolCodecFactory) Class.forName(className).newInstance();
			} catch (Exception e) {
				logger.warn("读取配置文件错误：" + WolfConfig.PROPERTY_SERVER_CODECFACTORY, e);
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
	
	public List<IWolfService> getServiceList() {
		List<IWolfService> ret = new ArrayList<IWolfService>();
		for (int i = 0; ;i++) {
			String className = get(WolfConfig.PROPERTY_SERVICE_PRE + i);
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
	
	public List<String> getNodeList() {
		List<String> ret = new ArrayList<String>();
		for (int i = 0; ;i++) {
			String address = get(WolfConfig.PROPERTY_NODE_ADDRESS + i);
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

