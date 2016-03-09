<%@page import="xg.push.XG"%>
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
String act = request.getParameter("act");
if("send".equals(act)){
	String content = request.getParameter("content");
	if(content != null){
		org.json.JSONObject ret = XG.inst.pushAllIos(content);
		out("发送结果:"+ret);
	}
}else if("tiliGive12".equals(act)){
	XG.inst.pushGetTili(10);
	out("发送完毕");
}else if("tiliGive18".equals(act)){
	XG.inst.pushGetTili(20);
	out("发送完毕");
}else if("switchOpenClose".equals(act)){
	XG.pushOpen = !XG.pushOpen;
}
%>
信鸽功能：<%=XG.pushOpen ? "已开启" : "已关闭" %>
&nbsp;<a href='?act=switchOpenClose'>切换</a><br/>
<form action="xin-ge.jsp" method="post">
<input type="text" name='content'>
<input type="hidden" name="act" value="send">
<input type="submit" value="推送给所有IOS设备">
</form>
<a href='?act=tiliGive12'>推送中午领体力</a><br/>
<a href='?act=tiliGive18'>推送晚饭领体力</a><br/>
</body>
</html>