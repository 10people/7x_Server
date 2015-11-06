<%@page import="qxmobile.protobuf.NoticeProtos.VersionNoticeInfo"%>
<%@page import="com.qx.notice.VersionNotice"%>
<%@page import="java.util.List"%>
<%@page import="qxmobile.protobuf.NoticeProtos.GetVersionNoticeResp"%>
<%@page import="com.manu.network.msg.ProtobufMsg"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="org.apache.mina.core.session.IoSession"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>版本公告</title>
</head>
<body>
	<%
	final IoSession fs = new RobotSession(){
		public WriteFuture write(Object message){
			setAttachment(message);
			synchronized(this){
				this.notify();
			}
			return null;
		}
	};
	synchronized(fs){
		BigSwitch.inst.route(PD.C_GET_VERSION_NOTICE_REQ, null, fs);
	//	fs.wait();
	}
	ProtobufMsg msg = (ProtobufMsg)fs.getAttachment();
	GetVersionNoticeResp.Builder resp = (GetVersionNoticeResp.Builder)msg.builder;
	%>
	<table border="1">
	<tr><th>标题</th><th>标签</th><th>内容</th><th>顺序</th></tr>
	<%
	List<VersionNoticeInfo> list = resp.getNoticeList();
	for(VersionNoticeInfo noticeInfo:list){
		%>
		<tr>
			<td><%=noticeInfo.getTitle() %></td>
			<td><%=noticeInfo.getTag() %></td>
			<td><%=noticeInfo.getContent() %></td>
			<td><%=noticeInfo.getOrder() %></td>
		</tr>
		<%
	}
	%>
	</table>
</body>
</html>