package com.manu.dynasty.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;
import com.google.protobuf.MessageLite;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.util.ProtobufUtils;


/**
 * @author 李康
 * 2013-8-20 下午01:39:52
 *
 */
public class MemcachedCRUD {
	
	protected static Logger logger = LoggerFactory.getLogger(MemcachedCRUD.class);
	public static String poolName = "gameDBPool";
	protected static MemCachedClient memCachedClient;
	protected static MemcachedCRUD memcachedCRUD = new MemcachedCRUD();
	public static SockIOPool sockIoPool;
	static{
		sockIoPool = init(poolName,"cacheServers");
		memCachedClient = new MemCachedClient(poolName);
		//  if true, then store all primitives as their string value.
		memCachedClient.setPrimitiveAsString(true);
	}
	public static SockIOPool init(String poolName, String confKey){
		//缓存服务器
		String cacheServers= GameServer.cfg.getServerByName(confKey);
		String server[] = {"192.168.3.80:11211"};
		if (cacheServers == null || "".equals(cacheServers)) {
		}else{
			server[0]=cacheServers;
		}
		//创建一个连接池
		SockIOPool pool = SockIOPool.getInstance(poolName);
		logger.info("连接池{}缓存配置 {}",poolName,Arrays.toString(server));
		pool.setServers(server);//缓存服务器
		
		pool.setInitConn(50); //初始化链接数
		pool.setMinConn(50);  //最小链接数
		pool.setMaxConn(500);  //最大连接数
		pool.setMaxIdle(1000 * 60 * 60);//最大处理时间
		
		
		pool.setMaintSleep(3000);//设置主线程睡眠时,每3秒苏醒一次，维持连接池大小
		pool.setNagle(false);//关闭套接字缓存
		
		pool.setSocketTO(3000);//链接建立后超时时间
		pool.setSocketConnectTO(0);//链接建立时的超时时间
		
		pool.initialize();
		return pool;
	}
	
	MemcachedCRUD(){}
	
	public static MemcachedCRUD getInstance(){
		return memcachedCRUD;
	}
	
	/**
	 * save,update
	 * @param key
	 * @param obj
	 */
	public boolean saveMessageLite(String key, MessageLite msg){
		if (msg == null) {
			logger.error("save MessageLite: null->" + key);
			return false;
		}
		
		Object o = memCachedClient.get(key);
		byte[] data = ProtobufUtils.toByteArray(msg);
		boolean success = false;
		if(null != o){//存在替换掉
			success = memCachedClient.replace(key, data);
		}else{
			
			success = memCachedClient.add(key, data);
		}
		if (success) {			
			logger.info("{} 保存成功 {}", key, o == null ? "add" : "replace");
		}else {
			logger.info("{} 保存失败 {}", key, o == null ? "add" : "replace");
		}
		
		return success;
	}
	
	private static final long INTERVAL = 100;
	public boolean add(String key , Object o){
		return memCachedClient.add(key, o);
	}
	public boolean update(String key , Object o){
		return memCachedClient.replace(key, o);
	}
	public boolean saveObject(String key, Object msg){
		boolean o = memCachedClient.keyExists(key);
		if(o){//存在替换掉
			return memCachedClient.replace(key, msg);
		}else{
			return memCachedClient.add(key, msg);
		}
	}
	
	/**
	 * delete
	 * @param key
	 */
	public boolean deleteObject(String key){
		return memCachedClient.delete(key);
	}
	
	/**
	 * get
	 * @param key
	 * @return
	 */
	public MessageLite.Builder getMessageLite(String key){		
		byte[] obj = (byte[])memCachedClient.get(key);
		if(null == obj){
			return null;
		}
		return ProtobufUtils.parseFrom(obj.length, obj);
	}
	
	/**
	 * 通过key数组，批量获取数据
	 * 
	 * @param keys
	 * @return
	 */
	public List<MessageLite.Builder> getMultiMessageLite(String... keys){		
		Map<String, Object> tmp = (Map<String, Object>)memCachedClient.getMulti(keys);
		if(null == tmp){
			return null;
		}
		
		List<MessageLite.Builder> ret = new ArrayList<MessageLite.Builder>();
		for (Object o : tmp.values()) {
			byte[] obj = (byte[])o;
			ret.add(ProtobufUtils.parseFrom(obj.length, obj));
		}
		return ret;
	}
	
	public Object getObject(String key){		
		Object obj = memCachedClient.get(key);
		return obj;
	}
	
	public static MemCachedClient getMemCachedClient(){
		return memCachedClient;
	}
	
}
