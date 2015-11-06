<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%
int sid = GameServer.serverId;
String name = request.getServerName();
if("203.195.230.100".equals(name)){
	switch(sid){
	case 1: name="外网稳定";break;
	case 2: name="外网测试";break;
	}
}else if("203.195.204.128".equals(name)){
	name = "正式";
}else{
	switch(sid){
	case 1: name="建虎";break;
	case 2: name="照文";break;
	case 3: name="内网";break;
	case 35: name="王转";break;
	case 37: name="测试专用";break;
	case 38: name="峻宇";break;
	}
}

%>
<title><%=name %> - 小七雄后台</title>
</head>
<frameset rows="25,*">
	<frame src='top.jsp'>
	<frameset cols="155,*">
		<frame src='left.jsp'>
		<frame src='index.jsp?frame=1' name='target'>
	</frameset>
</frameset>
</html>