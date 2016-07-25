<%@page import="com.manu.network.SessionUser"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.qx.world.Scene"%>
<%@page import="com.manu.dynasty.chat.ChatMgr"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>testScene</title>
</head>
<body>
<%
//Scene.getInstance().exec(PD.Enter_Scene, null, null);
//out.println("22");

IoSession ss = SessionManager.getInst().findByJunZhuId(553002L   );
if(ss == null){
	out("null");
}else{
	ss.write(PD.S_ACC_login_kick);
	out("kick");
}
%>

</body>
</html>