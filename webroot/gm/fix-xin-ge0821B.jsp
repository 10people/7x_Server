<%@page import="xg.push.XGFixBEvt"%>
<%@page import="com.qx.event.EventProc"%>
<%@page import="java.util.List"%>
<%@page import="com.qx.event.ED"%>
<%@page import="com.qx.event.EventMgr"%>
<%@page import="xg.push.XGFixB"%>
<%@page import="xg.push.XGFixA"%>
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
if(XGFixB.prInst == null){
	XGFixB.prInst = XG.inst;
	XG.inst = new XGFixB();
	out("已修正 {}"+XG.inst);
}else{
	out("【之前】已修改"+XG.inst);
}
br();
new XGFixBEvt();
br();
List<EventProc> list = EventMgr.procs.get(ED.ACC_LOGIN);
out("size:"+list.size());br();
for(int i=0; i<list.size(); i++){
	EventProc p = list.get(i)	;
	out(p);br();
}
%>

</body>
</html>