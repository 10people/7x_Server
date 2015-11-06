package com.manu.dynasty.boot;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.util.Config;

/**
 * 1 线程安全
 *   1 创建角色，独立线程池；
 *   2 登录，下线独立线程池；
 *   3 玩家属性修改独立线程池；
 *   4 玩家数据加载或释放独立线程池；
 *   
 * 2 数据存放：
 *   玩家核心数据，线上用CMEM，测试用memcachedb.
 *   
 *   一般数据独立于区服用Redis存放。比如账号数据就要用Redis存放，单独的区服可以用他来管理数据
 *   合服处理： 读取一个区的账号，然后和另外一个区的账号进行角色合并
 *   
 * 
 * @author wuliangzhu
 *
 */
public class GameServer {
	public static boolean DEBUG = false;
	public static String confFileBasePath = "/";
	public static String ip;
	public static int port;
	public volatile static boolean shutdown;
	public static boolean rejectLogin = false; // 如果需要让登录失效，可以设置这个值
	public static boolean rejectRequest = false; // 如果服务器有问题，可以设置这个值，让请求失效
	
	public static int serverId = 1; // 服务器标示
	public static int nodeId = 1; // 
	
	
	public static int updateUserWorkerNum = 1;
	public static int createUserWorkerNum = 1;
	public static int loginWorkerNum = 1;
	public static int loadDataWorkerNum = 2;
	public static String mmapFile = "share.map";
	
	public static int maxOnlineNum = 1000; // 最大在线人数
	
	public static int monitorInterval = 60 * 1000;
	
	public static final int SESSION_EXPIRED_TIME = 30 * 60 * 1000; // 毫秒
	
	public static int SAVE_DB_INTERVAL = 30 * 1000; // 定时回写周期
	public static int userDataExpiredTime = 30 * 60 * 1000; // 30min
	public static Config cfg;
	protected static Logger log = LoggerFactory.getLogger(GameServer.class);
	// 服务器名称
	public static String serverName = "";
	
	public static void init(){
		log.info("new config.");
		cfg = new Config();
		log.info("new config.load");
		cfg.loadConfig();
		log.info("set self properties.");
		serverId = cfg.get("serverId", serverId);//Integer.parseInt(server.trim());
		nodeId = cfg.get("nodeId", nodeId);//Integer.parseInt(server.trim());
		
		updateUserWorkerNum = cfg.get("updateUserWorker", updateUserWorkerNum);//Integer.parseInt(uuwn.trim());
		
		createUserWorkerNum = cfg.get("createUserWorker", createUserWorkerNum);//Integer.parseInt(cwn.trim());
		
		loginWorkerNum = cfg.get("loginWorker", loginWorkerNum);//Integer.parseInt(lwn.trim());
		
		mmapFile = cfg.get("mmapfile", mmapFile);
		
		monitorInterval = cfg.get("monitorInterval", monitorInterval);//Integer.parseInt(conffile.trim());
		
		SAVE_DB_INTERVAL = cfg.get("saveInterval", SAVE_DB_INTERVAL);//Integer.parseInt(intervalStr.trim());
		
		userDataExpiredTime = cfg.get("userDataExpiredTime",userDataExpiredTime);//Integer.parseInt(intervalStr.trim());
	}
	
	public static void shutdown(){
		GameServer.shutdown = true;
//		CMDDispatcher.stop();
	}
}
