<%@page import="qxmobile.protobuf.ShouChong.ShouChongAward"%>
<%@page import="com.qx.activity.ShouchongMgr"%>
<%@page import="qxmobile.protobuf.ShouChong.AwardInfo"%>
<%@page import="com.qx.activity.ShouchongInfo"%>
<%@page import="java.util.List"%>
<%@page import="qxmobile.protobuf.ShouChong.GetShouchong"%>
<%@page import="com.manu.network.msg.ProtobufMsg"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="org.apache.mina.core.session.IoSession"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>首冲</title>
</head>
<body>
	<form>
		<p>
			账号： <input type="text" name="name"/> 或 君主id： <input type="text" name="ownerid"/> <button type="submit">查看</button>
		</p>
	</form>
	<%
	// 查看
	String name = request.getParameter("name");
	String ownerid = request.getParameter("ownerid");
	name = name == null ? "": name.trim();
	ownerid = (ownerid == null ? "":ownerid.trim());
	if(session.getAttribute("name") != null && name.length()==0 && ownerid.length()==0){
		name = (String)session.getAttribute("name");
	}
	// 领取
	String junzhuid = request.getParameter("junzhuid");
	
	Account account = null;
	if(name != null && name.length()>0){
		account = HibernateUtil.getAccount(name);
		if(account!=null){
			ownerid = ""+(account.getAccountId()*1000+GameServer.serverId);
		}
	}else if(ownerid != null && ownerid.length()>0){
		account = HibernateUtil.find(Account.class, (Long.valueOf(ownerid) - GameServer.serverId) / 1000);
	}
	if(account == null){
		%><p>查看没有找到账号</p><%
	}else{
		session.setAttribute("name", name);
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
		synchronized(fs){
			BigSwitch.inst.route(PD.C_GET_SHOUCHONG_REQ, null, fs);
		//	fs.wait();
		}
		ProtobufMsg msg = (ProtobufMsg)fs.getAttachment();
		GetShouchong.Builder resp = (GetShouchong.Builder)msg.builder;
		%>
		<%
		ShouchongInfo info = HibernateUtil.find(ShouchongInfo.class,
				"where junzhuId=" + ownerid + "");
		int state = ShouchongMgr.instance.getShouChongState(info);
		switch(state){
		case ShouchongMgr.STATE_NULL:
			%><p><%=name %>没有首冲记录</p><%
			break;
		case ShouchongMgr.STATE_AWARD:
			%>
				<form>
					<p>
						<input name="junzhuid" type="hidden" value="<%=ownerid%>"/>
						<button type="submit"><%=name %>还未领取奖励</button>
					</p>
				</form>
			<%
			break;
		case ShouchongMgr.STATE_FINISHED:
			%><p><%=name %>已完成首冲活动</p><%
			break;
		default:
			break;
		}
		%>
		<p>活动描述：<%=resp.getDesc() %></p>
		<table border="1">
		<tr>
			<th>奖励类型</th><th>奖励id</th><th>奖励数量</th>
		</tr>
		<%
		List<AwardInfo> list = resp.getAwardList();
		for(AwardInfo award:list){
		%>
				<tr>
					<td><%=award.getAwardType() %></td>
					<td><%=award.getAwardId() %></td>
					<td><%=award.getAwardNum() %></td>
				</tr>
		<%
		}%>
		</table>
		<%
	}
	%>
	<%
	Account account2 = null;
	if(junzhuid!=null&&junzhuid.length()>0){
		account2 = HibernateUtil.find(Account.class, (Long.valueOf(junzhuid) - GameServer.serverId) / 1000);
	}
	if(account2 == null){
		%><p>没有找到账号</p><%
	}else{
		final IoSession fs = new RobotSession(){
			public WriteFuture write(Object message){
				setAttachment(message);
				synchronized(this){
					this.notify();
				}
				return null;
			}
		};
		fs.setAttribute(SessionAttKey.junZhuId, Long.valueOf(account2.getAccountId()*1000+GameServer.serverId));
		synchronized(fs){
			BigSwitch.inst.route(PD.C_SHOUCHONG_AWARD_REQ, null, fs);
		//	fs.wait();
		}
		ProtobufMsg msg = (ProtobufMsg)fs.getAttachment();
		ShouChongAward.Builder resp = (ShouChongAward.Builder)msg.builder;
		switch(resp.getResult()){
		case ShouchongMgr.SUCCESS:
			%>
			<p>领取首冲奖励成功</p>
			<%
			break;
		case ShouchongMgr.FAILED_1:
			%>
			<p>领取首冲奖励失败</p>
			<%
			break;
		default:
			break;
		}
	}
	%>
</body>
</html>