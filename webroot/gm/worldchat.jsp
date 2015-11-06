<%@page import="com.manu.network.BigSwitch"%>
<%@page import="qxmobile.protobuf.Chat.ChatPct"%>
<%@page import="qxmobile.protobuf.Chat.ChatPct.Channel"%>
<%@page import="com.manu.network.PD"%>
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
String act=request.getParameter("act");
if("sendWordMsg".equals(act)){
	String jzId = request.getParameter("jzId");
	String chatContent = request.getParameter("chatContent");
	if(jzId==null||"".equals(jzId)){
		out("君主id为空");
		return;
	} 
	IoSession fs = createSession( Long.parseLong(jzId));
	ChatPct.Builder b = ChatPct.newBuilder();
	Channel value = Channel.valueOf(3);
	b.setChannel(value);
	b.setSenderId(Long.parseLong(jzId));
	b.setContent(chatContent);
	BigSwitch.inst.route(PD.C_Send_Chat, b, fs);;
	out("OK.");
	out.append("已发送：<br/>");
	out.append("<br/>");
	out.append("<br/>");
	out.append("<pre>");
	out.append(chatContent);
	out.append("</pre>");
	out.append("<br/>");
}
%>

 <form action="">
 <INPUT TYPE="hidden" name="act" value="sendWordMsg">
        聊天内容<br>
        <TEXTAREA NAME="chatContent" ROWS="5" COLS="40"></TEXTAREA>
	 <br>
<!--          聊天頻道为<select name="chetChannel" > -->
<!--  	 	 <option value="setSysTime">修改时间</option> -->
<!--  		 <option value="resetTime">重置时间</option> -->
<!-- 	 </select>  -->
        
	 <br>
        君主id<input type="text" name="jzId" >
       <br> 
      
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<button type="submit">提交</button>
    </form>
 
</body>
</html>