<%@page import="com.manu.dynasty.chat.ChatChLog"%>
<%@page import="qxmobile.protobuf.Chat.CGetChat"%>
<%@page import="qxmobile.protobuf.Chat.ChatPct"%>
<%@page import="com.manu.network.msg.ChatMsg"%>
<%@page import="com.manu.dynasty.chat.ChatMgr"%>
<%@page import="java.util.List"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>聊天监控</title>
</head>
<body>
<%
String act = request.getParameter("act");
if("clear".equals(act)){
	Long cnt = Redis.getInstance().del(ChatMgr.getInst().chWorld.key);
	out.append("个数:"+cnt+" - 已清空<br/>");
}
%>
聊天记录数量限制是<%=ChatMgr.getInst().chWorld.maxLogSize %><br/>
<%
int start = -10;
int end = -1;
//
String fs = request.getParameter("fs");//force start
String fe = request.getParameter("fe");//force end, take as want cnt
List<?> list;
if(fs != null && fe != null){
	CGetChat.Builder builder = CGetChat.newBuilder();
	start = Integer.parseInt(fs);
	end = Integer.parseInt(fe);
	builder.setStart(start);
	builder.setEnd(end);
	list = ChatMgr.getInst().getChatLog(builder);
	out.append("尝试获取日志，开始 "+start+" 数量 "+end+"<br/>");
}else{
	list = Redis.getInstance().lrange(ChatMgr.getInst().chWorld.key, ChatPct.getDefaultInstance(), start, end);
}
//
int size = list.size();
out.append("显示的条目数量:"+size+"<br/>");
out.append("<table border='1'>");
out.append("<tr><th>序号</th><th>时间</th><th>发起ID</th><th>发起名称</th><th>接收ID</th><th>接收名称</th>");
out.append("<th>频道</th><th>内容</th></tr>");
ChatPct.Builder cm = null;
for(int i=0; i<size; i++){
	out.append("<tr>");
	cm = (ChatPct.Builder)list.get(i);
	out.append("<td>");	out.append(""+cm.getSeq());			out.append("</td>");
	out.append("<td>");	out.append(cm.getDateTime());			out.append("</td>");
	out.append("<td>");	out.append(""+cm.getSenderId());		out.append("</td>");
	out.append("<td>");	out.append(cm.getSenderName());		out.append("</td>");
	out.append("<td>");	out.append(""+cm.getReceiverId());		out.append("</td>");
	out.append("<td>");	out.append(cm.getReceiverName());		out.append("</td>");
	out.append("<td>");	out.append(cm.getChannel().toString());		out.append("</td>");
	out.append("<td>");	out.append(cm.getContent());		out.append("</td>");
	out.append("</tr>");
}
out.append("</table>");
if(cm != null){
	out.append("<br/><a target='_blank' href='?fs="+cm.getSeq()+"&fe=5'>测试获取日志</a>");
}
%>
<br/>
<br/>
<br/>
<a href='?act=clear'>清空</a>
</body>
</html>