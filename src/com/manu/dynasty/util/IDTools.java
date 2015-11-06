package com.manu.dynasty.util;

import java.util.concurrent.atomic.AtomicInteger;

import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.store.Redis;

/**
 * 生成唯一Id的接口：
 * 1 用户ID，要求全局唯一。
 * 2 联盟Id，也是全局唯一。
 * 
 * 生成规则：
 * 服务器Id + 类型 + 时间
 * @author wuliangzhu
 *
 */
public class IDTools {
	public static final int serverId;

	public volatile static boolean isChange;
	
	public static AtomicInteger userIdG;
	public static AtomicInteger guildIdG ;
	
	static{
		serverId = GameServer.serverId << 23;
		userIdG = new AtomicInteger();
		guildIdG = new AtomicInteger();
	}
	public static int getUserId(){
		int t = userIdG.incrementAndGet();
		
		MMap.saveUserId(t);
		
		return serverId | t;
	}
	
	public static int getGuildId(){
		int t = guildIdG.incrementAndGet();
		
		MMap.saveGuildId(t);
		
		return serverId | t;
	}
	
	/**
	 * 检查用户名是否重复
	 * 
	 * 直接调用 add 方法就可以了，这样不用调用一次add，直接调用add，就可以知道是否成功
	 * @param name
	 * @return
	 */
	public static boolean isExist(String key, String name){
		Redis redis = Redis.getInstance();
		return redis.sexist(key, name);
	}
	
	public static boolean addName(String key, String name){
		Redis redis = Redis.getInstance();
		return redis.sadd(key, name);
	}	

}
