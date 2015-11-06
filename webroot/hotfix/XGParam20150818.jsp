<%@page import="xg.push.XGParam"%>
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
		XGParam before = XGParam.channels.get("TongBu");
		out.println("before :" + before+" , accessId - " + before.accessId + ", secretKey - " + before.secretKey);
		XGParam after = new XGParam(2200142040L, "03fa6c4bee7fa2c2ae44407e1ead5437", "TongBu");
		XGParam.channels.put("TongBu", after);
		out.println("after :" + XGParam.channels.get("TongBu"));
	%>
	
</body>
</html>