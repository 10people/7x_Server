<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.manu.network.msg.ProtobufMsg"%>
<%@page import="java.util.LinkedList"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="java.util.Date"%>
<%@page import="com.manu.network.SessionUser"%>
<%@page import="java.util.concurrent.ConcurrentHashMap"%>
<%@page import="com.qx.account.AccountManager"%>
<%@page import="com.qx.world.Scene"%>
<%@page import="java.util.Enumeration"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.qx.world.Player"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>监控</title>
</head>
<body>
连接数量:<%=SessionManager.getInst().sessionMap.size() %><br/>
君主数量:<%=AccountManager.sessionMap.size() %><br/>
<br/>
-<br/>
<%!
String getProtoIds(IoSession session){
	LinkedList<Object> list = (LinkedList<Object>) session.getAttribute("IOLogIdKey");
	if(list == null){
		return "-";
	}
	StringBuffer sb = new StringBuffer();
	for(Object o : list){
		if(o instanceof ProtobufMsg){
			ProtobufMsg mf = (ProtobufMsg) o;
			sb.append(mf.id);
			sb.append(",");
		}
	}
	return sb.toString();
}
%>
<%
SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
int cnt = 0;
Enumeration<Long>  houseki = BigSwitch.inst.scMgr.houseScenes.keys();

ConcurrentHashMap<Long, SessionUser> map = SessionManager.getInst().sessionMap;
tableStart();
synchronized(map){
	ths("sid,isConnected,LastReadTime,LastWriteTime,jz,protoIds,Kill?");
	for(SessionUser u : map.values()){
		trS();
		IoSession ss = u.session;
		td(ss.getId());
		td(ss.isConnected()); td(fmt.format(new Date(ss.getLastReadTime())));
		Date lwt = new Date(ss.getLastWriteTime());
		td(fmt.format(lwt));
		Object jz = ss.getAttribute(SessionAttKey.junZhuId);
		td(jz);
		td(getProtoIds(ss));
		td(lwt.getDate() == 26 && jz == null ? "1" : "");
		trE();
		if(lwt.getDate() == 26 && jz == null ){
			ss.close(true);
		}
	}
}
tableEnd();
//
br();
tableStart();
synchronized (AccountManager.sessionMap){
	for(Long jzId : AccountManager.sessionMap.keySet()){
		trS();
		IoSession ss = AccountManager.sessionMap.get(jzId);
		td(ss.getId());
		td(jzId);
		td(ss == null ? "null" : ss.isConnected());
		td(getProtoIds(ss));
		td(fmt.format(new Date(ss.getCreationTime())));
		td(fmt.format(new Date(ss.getLastReadTime())));
		trE();
	}
}
tableEnd();
%>
</body>
</html>