package com.qx.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.collections.map.LRUMap;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ZhangHao.LoginRet;

import com.manu.dynasty.boot.GameServer;
import com.manu.network.BigSwitch;
import com.manu.network.SessionAttKey;


public class LoginServ implements Runnable{
	public static Map<String, Integer> chName2id = new HashMap<String, Integer>();
	static{
		//数字不能改，在/qxrouter/WebContent/channel/checkLogin.jsp里写死了
		chName2id.put("XY", 		100);
		chName2id.put("TongBu", 	200);
		chName2id.put("PP", 		300);
		chName2id.put("KuaiYong", 	400);
		chName2id.put("AiSi", 		500);
		chName2id.put("HaiMa", 		600);
		chName2id.put("IApple", 		700);
		chName2id.put("iTools", 		800);
		chName2id.put("TX", 		900);
		chName2id.put("GuangFang", 		0);
	}
	public static Map<Long, Integer> accId2channelCode = Collections.synchronizedMap(new LRUMap(10000));
	public static Logger log = LoggerFactory.getLogger(LoginServ.class);
	public static String host;
	public static int port;
	public int serverId;
	IoSession session;
	String name;
	public LoginServ(IoSession ss, String accName){
		serverId = GameServer.serverId;
		host =  GameServer.cfg.get("loginServer");
		port = GameServer.cfg.get("loginPort", 8090);
		name = accName;
		session = ss;
	}
	@Override
	public void run() {
		notifyAccountLogin();
	} 

	public void start(){
		new Thread(this, "checkLogin").start();
	}
	public void notifyAccountLogin(){
			JSONObject o = sendRequest();
			if(o == null){
				fail();
				return;
			}
			long accId = o.optLong("accId", -1);
			if(accId == -1){
				fail();
				return;
			}
			String channel = o.optString("channel","null");
			session.setAttribute(SessionAttKey.ACC_CHANNEL, channel);
			accId2channelCode.put(accId, getChannelCode(channel));
			log.info("sid {} , accId {} 登录成功",session.getId(), accId);
			BigSwitch.inst.accMgr.loginBackFromRouter(name, session, accId);
	}
	public Integer getChannelCode(String channel) {
		Integer ret = chName2id.get(channel);
		if(ret == null){
			ret = 0;
		}
		return ret;
	}
	public Integer getChCodeByRoleId(String roleId){
		long id = 0;
		try{
			id = Long.parseLong(roleId);
		}catch(Exception e){
			return 0;
		}
		return getChCodeByRoleId(id);
	}
	public Integer getChCodeByRoleId(long roleId){
		long accId = roleId/1000;
		Integer ret = accId2channelCode.get(accId);
		if(ret == null){
			ret = 0;
		}
		return ret;
	}
	public JSONObject sendRequest() {
		String msg = "{" + "\"msg\" : \"LOGIN\",\"serverId\":" + serverId + 
				 ",\"accountName\":\"" + name +"\"}";
		String page = "/qxrouter/gameSerInfo.jsp";
		{
			// 本地测试用
//				host = "192.168.0.176";
//				port = 8080;
		}
		log.info("sid {} [{}]请求登录", session.getId(), name);
		MyClient hc = new MyClient(host, port);
		String respMesg = hc.startServerSendRequest(page, msg);
		JSONObject o = null;
		try{
			o = JSONObject.fromObject(respMesg);
		}catch(Exception e){
			log.error("登录返回的字符串转换为json时出错", e);
		}
		return o;
	}
	private void fail() {
		LoginRet.Builder ret = LoginRet.newBuilder();
		ret.setCode(3);
		ret.setMsg("用户名错误。");
		session.write(ret.build());
		log.info("账号登录验证失败 sid {}, acc [{}]",session.getId(), name);
	}
}
