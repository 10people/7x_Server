<%@page import="xg.push.XG"%>
<%@page import="org.json.JSONObject"%>
<%@page import="com.tencent.xinge.XingeApp"%>
<%@page import="xg.push.XGParam"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%
	try{
		XGParam xgParam = XGParam.channels.get("XY");
		XingeApp push = new XingeApp(xgParam.accessId, xgParam.secretKey);
		String deviceToken = "a8796711228246586fd0fcf4390f3c994d666ac62da38308bc3433820eeeb5a6";
		JSONObject o = push.queryTokenTags(deviceToken);
		out("<pre>");
		out(o.toString(4));
		out("---");
		JSONObject o2 = push.queryTagTokenNum(XG.tag1TiLiGive);
		out(o2.toString(4));
		out("\n设备数量:");
		out(push.queryDeviceCount());
		out("</pre>");
		
	}catch(Exception e){
		showStackTrace(e);
	}
	%>
	
</body>
</html>