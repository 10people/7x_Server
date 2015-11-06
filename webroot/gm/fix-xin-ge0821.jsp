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
if(XGFixA.prInst == null){
	XGFixA.prInst = XG.inst;
	XG.inst = new XGFixA();
	out("已修正 {}"+XG.inst);
}else{
	out("【之前】已修改"+XG.inst);
}
%>

</body>
</html>