<%@page import="com.manu.dynasty.core.servlet.InitServlet"%>
<%@page import="java.util.Map"%>
<%@page import="java.net.InetSocketAddress"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.qx.event.EventMgr"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.qx.quartz.SchedulerMgr"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.manu.dynasty.store.MemcachedCRUD"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="com.manu.dynasty.util.ProtobufUtils"%>
<%@page import="qxmobile.protobuf.PacketProtos.Packet"%>
<%@page import="com.manu.network.internal.InternalSocketMgr"%>
<%@page import="com.manu.network.TXSocketMgr"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>腾讯网关</title>
</head>
<body>
<%
if(request.getParameter("close")!=null){
	//InitServlet.closeNet();
}
if(TXSocketMgr.getInst().acceptor == null){
	out("之前未启动");
}else{
	out("之前已启动");
}
%>
监听端口：
<%
TXSocketMgr.getInst().start();
InetSocketAddress ad = TXSocketMgr.getInst().acceptor.getLocalAddress();
if(ad != null){
	out(ad.getPort());
}else{
	out("Adress is null.");
}
%>
<br/>
线程信息：
<br/>
<%
ThreadGroup group =   
Thread.currentThread().getThreadGroup();  
ThreadGroup topGroup = group;  
// traverse the ThreadGroup tree to the top  
while ( group != null ) {  
topGroup = group;  
group = group.getParent();  
}  
// Create a destination array that is about  
// twice as big as needed to be very confident  
// that none are clipped.  
int estimatedSize = topGroup.activeCount() * 2;  
Thread[] slackList = new Thread[estimatedSize];  
// Load the thread references into the oversized  
// array. The actual number of threads loaded   
// is returned.  
int actualSize = topGroup.enumerate(slackList);  
// copy into a list that is the exact size  
tableStart();
ths("id,name,daemon");
for(int i=0; i<actualSize; i++){
	trS();
	Thread t = slackList[i];
	if(t == null)continue;
	td(t.getId());
	td(t.getName());
	td(t.isDaemon());
	trE();
}
tableEnd();
%>
<pre>
<% 
for(Map.Entry<Thread, StackTraceElement[]> stackTrace : Thread.getAllStackTraces().entrySet()){
    Thread thread = (Thread)stackTrace.getKey();
    StackTraceElement[] stack = (StackTraceElement[])stackTrace.getValue();
    if(thread.equals(Thread.currentThread())){
        continue;
    }
    out.print("\n线程："+thread.getName()+"\n");
    for(StackTraceElement element : stack){
        out.print("\t"+element+"\n");
    }
}
%>
</body>
</html>