package com.qx.http;


import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.boot.GameServer;
import com.qx.junzhu.JunZhu;


public class CreateJunZhuSer implements Runnable{

	public static Logger log = LoggerFactory.getLogger(CreateJunZhuSer.class);
	public static String host;
	public static int port;
	public long jzId;
	public String name;
	public CreateJunZhuSer(long jzId0, String name0){
		host =  GameServer.cfg.get("loginServer");
		port = GameServer.cfg.get("loginPort", 8090);
		jzId = jzId0;
		name = name0;
	}
	public void start(){
		new Thread(this, "createJunZS").start();
	}
	@Override
	public void run() {
		notifyCreateJunZhu();
	} 
	public boolean notifyCreateJunZhu(){
			int serverId = GameServer.serverId;
			JSONObject p = new JSONObject();
			p.put("name", name);
			p.put("level", 1);
			p.put("id", jzId);
			p.put("serverId", serverId);
//			String msg = "{" +
//					"\"name\" : \"" + junZhu.name + 
//					"\", \"level\" :" + junZhu.level +
//					",\"id\": \""+junZhu.id +	"\",\"serverId\": "+ serverId +				
//					"}";
			String msg = p.toString();
			String page =  "/qxrouter/junZhuInfo.jsp";
			MyClient hc = new MyClient(host, port);
			boolean success = hc.sendRequest(page, msg);
			if (success){
				log.info("玩家：{}，在服务器{}建立君主，登录服务器收到消息并返回",msg, serverId);
			}
			else{
				log.info("玩家：{}，在服务器{}建立君主，登录服务器没有收到消息",msg, serverId);
			}
			return true;
		}

}
