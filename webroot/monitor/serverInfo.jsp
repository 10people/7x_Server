<%@page import="com.qx.vip.VipMgr"%>
<%@page import="com.qx.util.DelayedSQLMgr"%>
<%@page import="com.qx.task.GameTaskMgr"%>
<%@page import="java.util.concurrent.ThreadPoolExecutor"%>
<%@page import="java.util.concurrent.ExecutorService"%>
<%@page import="com.qx.persistent.DBSaver"%>
<%@page import="com.qx.event.EventMgr"%>
<%@page import="com.manu.network.TXSocketMgr"%>
<%@page import="com.qx.account.AccountManager"%>
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
//

String act = request.getParameter("act");
if("changeModelCD".equals(act)){
	String size = request.getParameter("sizePerSc");
	SettingsMgr.changeModelCD = Long.parseLong(size)*1000;
}else if("changeProcSize".equals(act)){
	int want = Integer.parseInt(request.getParameter("sizePerSc"));
	TXSocketMgr.inst.fixPoolSize(want);
}else if("doJob".equals(act)){
	com.qx.quartz.job.ClearPromptJob.doIt = !com.qx.quartz.job.ClearPromptJob.doIt;
}else if("fakeCharge".equals(act)){
	VipMgr.allowFakeCharge = !VipMgr.allowFakeCharge;
}else if("ysdk".equals(act)){
	if("ysdktest.qq.com".equals(com.qx.yuanbao.YSDK.serverName)){
		com.qx.yuanbao.YSDK.serverName = "ysdk.qq.com";
		com.qx.yuanbao.YSDK.appkey_pay = "ISNJdn2BFLRZhdmz8cE6t0cuNpC9FpGU";
	}else{
		com.qx.yuanbao.YSDK.serverName = "ysdktest.qq.com";
		com.qx.yuanbao.YSDK.appkey_pay = "kISosZBMXjentmMNfZGYKp1332zwQzK4";
	}
}
 %>
 <br/>

 <form action='' method="post">
<input type="hidden" name="act" value="changeProcSize">
 NIOProcessor个数:<input  type='number' name='sizePerSc' value='<%=TXSocketMgr.inst.procsArr.length %>'/>
<button type='submit' >修改</button>
</form>
EventQ:<br/>
<%
for(ThreadPoolExecutor e : EventMgr.es){
%>
	已调度:<%=e.getTaskCount() %>,
	已完成:<%=e.getCompletedTaskCount() %>,
	排队中:<%=e.getQueue().size() %>
	<br/>
<%} %>
DBSaver:<br/>
<%
for(ThreadPoolExecutor e : DBSaver.es){
%>
	已调度:<%=e.getTaskCount() %>,
	已完成:<%=e.getCompletedTaskCount() %>,
	排队中:<%=e.getQueue().size() %>
	<br/>
<%} %>
任务处理器:
	最高线程数:<%=GameTaskMgr.es.getLargestPoolSize() %>
已调度:<%=GameTaskMgr.es.getTaskCount() %>,
	已完成:<%=GameTaskMgr.es.getCompletedTaskCount() %>,
	排队中:<%=GameTaskMgr.es.getQueue().size() %>
 <br/>
DelayedSQLMgr:
	最高线程数:<%=DelayedSQLMgr.es.getLargestPoolSize() %>
已调度:<%=DelayedSQLMgr.es.getTaskCount() %>,
	已完成:<%=DelayedSQLMgr.es.getCompletedTaskCount() %>,
	排队中:<%=DelayedSQLMgr.es.getQueue().size() %>
 <br/>
 <br/>
 <form action='' method="post">
<input type="hidden" name="act" value="changeModelCD">
 切换形象CD时间（秒）：:<input  type='number' name='sizePerSc' value='<%=SettingsMgr.changeModelCD/1000 %>'/>
<button type='submit' >修改</button>
</form>
<%=com.qx.yuanbao.YSDK.serverName %>
<%=com.qx.yuanbao.YSDK.appkey_pay %>
<a href='?act=ysdk'>切换</a>
<br/>
<%=com.qx.quartz.job.ClearPromptJob.doIt %>
<a href='?act=doJob'>切换-清理3分钟失效的通知</a>
<br/>
fakeCharge<%=VipMgr.allowFakeCharge %>
<a href='?act=fakeCharge'>切换</a>
</body>
</html>
<%
//AccountManager.inst.initSensitiveWordAndIllegalityName();
%>