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
<%
String url = "onlineCnt.jsp";
if(!com.manu.dynasty.filter.FilterMgr.exclusion.contains(url)){
	//需要登录后添加白名单
	com.manu.dynasty.filter.FilterMgr.exclusion.add(url);
}
%>
连接数量:<%=SessionManager.getInst().sessionMap.size() %><br/>
君主数量:<%=AccountManager.sessionMap.size() %><br/>
</body>
</html>