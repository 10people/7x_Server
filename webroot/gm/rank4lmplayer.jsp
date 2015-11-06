<%@page import="qxmobile.protobuf.Ranking.JunZhuInfo"%>
<%@page import="java.util.List"%>
<%@page import="qxmobile.protobuf.Ranking.AlliancePlayerResp"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="qxmobile.protobuf.Ranking.AlliancePlayerReq"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="org.apache.mina.core.session.IoSession"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>联盟成员</title>
</head>
<body>
<%
String mengId = request.getParameter("mengId");
%>
<%
	IoSession fs = new RobotSession(){
		public WriteFuture write(Object message){
			setAttachment(message);
			synchronized(this){
				this.notify();
			}
			return null;
		}
	};
	AlliancePlayerReq.Builder builder = AlliancePlayerReq.newBuilder();
	builder.setMengId(Integer.parseInt(mengId));
	synchronized(fs){
		BigSwitch.inst.route(PD.RANKING_ALLIANCE_PLAYER_REQ, builder, fs);
	//	fs.wait();
	}
	AlliancePlayerResp resp = (AlliancePlayerResp)fs.getAttachment();
	%>
	<table border="1">
		<tr>
			<th>君主名</th>
			<th>职务</th>
			<th>等级</th>
			<th>贡献</th>
			<th>战斗力</th>
			<th>贡金</th>
		</tr>
		<%
		List<JunZhuInfo> junList = resp.getPlayerList();
		for(JunZhuInfo jz:junList){
		%>
		<tr>
			<td><%=jz.getName() %></td>
			<td><%=jz.getJob() %></td>
			<td><%=jz.getLevel() %></td>
			<td><%=jz.getGongxian() %></td>
			<td><%=jz.getZhanli() %></td>
			<td><%=jz.getGongjin() %></td>
		</tr>	
		<%
		}
		%>
	</table>
	
	<%	
%>

</body>
</html>