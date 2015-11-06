<%@page import="com.manu.dynasty.chat.ChatMgr"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>发送系统聊天</title>
</head>
<body>
<%String act=request.getParameter("act");
if("sendSysMsg".equals(act)){
	out.append("<a href=''>返回</a><br/>");
	String msg = request.getParameter("msg");
	if(msg == null){
		out.append("消息是null<br/>");
		return;
	}
	msg = msg.trim();
	if(msg.isEmpty()){
		out.append("消息不能为空<br/>");
		return;
	}
	ChatMgr.getInst().sendSysChat(msg);
	out.append("已发送：<br/>");
	out.append("<br/>");
	out.append("<br/>");
	out.append("<pre>");
	out.append(msg);
	out.append("</pre>");
	out.append("<br/>");
	return;
}%>
消息将显示在《聊天-世界频道》里<br/>
<FORM METHOD=POST ACTION="">
<INPUT TYPE="hidden" name="act" value="sendSysMsg">
<TABLE>
<TR>
	<TD>内容</TD>
	<TD><TEXTAREA NAME="msg" ROWS="5" COLS="40"></TEXTAREA></TD>
</TR>
<TR>
	<TD></TD>
	<TD><INPUT TYPE="submit" value="提交"></TD>
</TR>
</TABLE>

</FORM>

</body>
</html>