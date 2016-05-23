<%@page import="com.qx.account.SettingsMgr"%>
<%@page import="org.apache.commons.pool.impl.GenericObjectPool"%>
<%@page import="redis.clients.jedis.JedisPool"%>
<%@page import="java.lang.reflect.Field"%>
<%@page import="com.qx.util.TableIDCreator"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.award.AwardMgr"%>
<%@page import="com.manu.dynasty.store.MemcachedCRUD"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title></title>
</head>
<body>
<h1>服务器信息</h1>
Redis:<%=Redis.getInstance().host %>:<%=Redis.getInstance().port %> <br/>
<%
try{
	Redis r = Redis.getInstance();
	Field f = Redis.class.getDeclaredField("pool");
	f.setAccessible(true);
	JedisPool pool = (JedisPool)f.get(r);
	//out.append("xxx:"+pool.toString());
	//
	br();
	Field fw = redis.clients.util.Pool.class.getDeclaredField("internalPool");
	fw.setAccessible(true);
	GenericObjectPool gp = (GenericObjectPool)fw.get(pool);
	out("getMaxActive:"+gp.getMaxActive());br();
	out("getMaxWait:"+gp.getMaxWait());br();
	out("getNumActive:"+gp.getNumActive());br();
	out("getNumIdle:"+gp.getNumIdle());br();
	out("getWhenExhaustedAction:"+gp.getWhenExhaustedAction());br();
	out("BLOCK="+GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
	out(";FAIL="+GenericObjectPool.WHEN_EXHAUSTED_FAIL);
	out(";GROW="+GenericObjectPool.WHEN_EXHAUSTED_GROW);
	br();
	//Redis.getInstance().exist_("test");
	//out.append("测试Redis OK<br/>");
}catch(Exception e){
	out.append("测试Redis 失败<br/>"+e);
}
%>
<hr/>
Mem:<%
for(String s : MemcachedCRUD.sockIoPool.getServers()){
	out.append(s+"<br/>");
}
try{
	MemcachedCRUD.getInstance().getMessageLite("test mem cache");
	out.append("测试MEMCache OK<br/>");
}catch(Exception e){
	out.append("测试MEMCache 失败<br/>"+e);
}
out("Router Mem:");
for(String s : TableIDCreator.sockIoPool.getServers()){
	out.append(s+"<br/>");
}
try{
	TableIDCreator.memCachedClient.get("xx");
	out.append("测试Router MEMCache OK<br/>");
}catch(Exception e){
	out.append("测试Router MEMCache 失败<br/>"+e);
}
//GameServer.cfg.loadConfig();
out.append("服务器编号："+GameServer.cfg.get("serverId")+"<br/>");
AwardMgr.cheatHit = true;
out.println("<br/> 已开启PVE cheat");
//

String act = request.getParameter("act");
if("changeModelCD".equals(act)){
	String size = request.getParameter("sizePerSc");
	SettingsMgr.changeModelCD = Long.parseLong(size)*1000;
}
 %>
 <br/>

 <form action='' method="post">
<input type="hidden" name="act" value="changeModelCD">
 切换形象CD时间（秒）：:<input  type='number' name='sizePerSc' value='<%=SettingsMgr.changeModelCD/1000 %>'/>
<button type='submit' >修改</button>
</form>
</body>
</html>