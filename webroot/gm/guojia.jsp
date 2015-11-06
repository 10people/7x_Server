<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="org.apache.mina.core.session.IoSession"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="qxmobile.protobuf.Settings.ChangeGuojiaResp"%>
<%@page import="com.manu.network.msg.ProtobufMsg"%>
<%@page import="qxmobile.protobuf.Settings.ChangeGuojiaReq"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>国家</title>
</head>
<body>
	<%
	String name = request.getParameter("name");
	String ownerid = request.getParameter("ownerid");
	String guojiaId = request.getParameter("guojiaId");
	name = name == null ? "": name.trim();
	ownerid = (ownerid == null ? "":ownerid.trim());
	if(session.getAttribute("name") != null && name.length()==0 && ownerid.length()==0){
		name = (String)session.getAttribute("name");
	}
	%>
	<form action="">
		账号：<input type="text"  name="name"/> 
		国家id：<input type="text"  name="guojiaId"/>
		<button type="submit" >转国</button>
	</form>
	<%
	if(name != null && name.length()>0&&guojiaId != null && guojiaId.length()>0) {
		Account account = HibernateUtil.getAccount(name);
		if(account!=null){
			final IoSession fs = new RobotSession(){
				public WriteFuture write(Object message){
					setAttachment(message);
					synchronized(this){
						this.notify();
					}
					return null;
				}
			};
			fs.setAttribute(SessionAttKey.junZhuId, Long.valueOf(account.getAccountId()*1000+GameServer.serverId));
			ChangeGuojiaReq.Builder builder = ChangeGuojiaReq.newBuilder();
			builder.setGuojiaId(Integer.valueOf(guojiaId));
			synchronized(fs){
				BigSwitch.inst.route(PD.C_ZHUANGGUO_REQ, builder, fs);
			//	fs.wait();
			}
			ProtobufMsg msg = (ProtobufMsg)fs.getAttachment();
			ChangeGuojiaResp.Builder resp = (ChangeGuojiaResp.Builder)msg.builder;
			int result = resp.getResult();
			switch(result){
			case 0:
				%><p>转国成功</p><%
				break;
			case 101:
				%><p>转国失败，在联盟中</p><%
				break;
			case 102:
				%><p>转国失败，没有转国令</p><%
				break;
			default:
				break;
			}
		}
	}
	%>
</body>
</html>