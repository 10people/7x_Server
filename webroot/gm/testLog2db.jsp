<%@page import="log.OurLog"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.qx.http.LoginServ"%>
<%@page import="log.DBHelper"%>
<%@page import="log.Log2DB"%>
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
//DBHelper.url = "jdbc:mysql://127.0.0.1/sourcedata_tlog_newdb";
//DBHelper.user = "devuser";
//DBHelper.password = "devuser";
////////////
out("数据库地址:"+DBHelper.url);br();
out("用户名:"+DBHelper.user);br();
out("密码:"+DBHelper.password);br();
String act = request.getParameter("act");
if("import".equals(act)){
	Log2DB ld = new Log2DB();
	ld.saveAll2db();
	out.println("ok");
}else if("showAcc2ch".equals(act)){
	br();
	out("数量:"+LoginServ.accId2channelCode.size());br();
	out("acc->channel coe");br();
	synchronized(LoginServ.accId2channelCode){
		Iterator<Entry<Long, Integer>> it = LoginServ.accId2channelCode.entrySet().iterator();
		while(it.hasNext()){
			Entry<Long, Integer> e = it.next();
			out(e.getKey()+"->"+e.getValue());br();
		}
	}
	String RoleId = "14001";
	out("should be:"+OurLog.chCode.getChannelCode(RoleId));
}
%>
</body>
</html>