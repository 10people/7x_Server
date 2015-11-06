<%@page import="com.manu.network.msg.ProtobufMsg"%>
<%@page import="com.googlecode.protobuf.format.JsonFormat"%>
<%@page import="com.google.protobuf.Message"%>
<%@page import="java.util.List"%>
<%@page import="com.qx.log.LogMgr"%>
<%@page import="com.manu.dynasty.util.ProtobufUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>protomsg</title>

</head>
<body>
	<%
	String index = request.getParameter("index");
	String sessionId = request.getParameter("sessionId");
	%>
	<%
	StringBuffer content = new StringBuffer();
	Object obj = LogMgr.inst.getSendMsgByIndex(Long.valueOf(sessionId),Integer.valueOf(index)); 
	if(obj instanceof Message){
		Message message  = (Message)obj;
		content.append(JsonFormat.printToString(message));
	} else if(obj instanceof ProtobufMsg){
		ProtobufMsg message  = (ProtobufMsg)obj;
		content.append(JsonFormat.printToString((Message) message.builder.build()));
	}
	%>
	<h3>消息内容</h3>
	<p><%=content %></p>
	<button onclick="javascript:history.go(-1)">返回</button>
	<!-- 
	<p>
		<iframe src="http://json.cn/" width="100%" height="800px">
		</iframe>
	</p>
	 -->
</body>
</html>