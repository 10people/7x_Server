<%@page import="com.manu.network.msg.ProtobufMsg"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.beans.PropertyDescriptor"%>
<%@page import="java.lang.reflect.Field"%>
<%@page import="java.util.Arrays"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.SessionUser"%>
<%@page import="java.util.List"%>
<%@page import="org.apache.mina.core.session.IoSession"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.qx.log.LogMgr"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>protoIdLog</title>
</head>
<body>
<%
// 查看
String name = request.getParameter("name");
String ownerid = request.getParameter("ownerid");
name = (name == null ? "": name.trim());
ownerid = (ownerid == null ? "":ownerid.trim());
if(session.getAttribute("name") != null && name.length()==0 && ownerid.length()==0){
	name = (String)session.getAttribute("name");
}

%>
	<form>
		<p>
			账号： <input type="text" name="name" value="<%=name%>"/> 或 君主id： <input type="text" name="ownerid"/> <button type="submit">查看</button>
		</p>
	</form>
	<%
	
	Account account = null;
	if(name != null && name.length()>0){
		account = HibernateUtil.getAccount(name);
		if(account!=null){
			ownerid = ""+(account.getAccountId()*1000+GameServer.serverId);
		}
	}else if(ownerid != null && ownerid.length()>0){
		account = HibernateUtil.find(Account.class, (Long.valueOf(ownerid) - GameServer.serverId) / 1000);
		if(account != null)name = account.getAccountName();
	}
	if(account == null){
		%><p>没有找到账号</p><%
	}else{
		session.setAttribute("name", name);
		SessionUser sessionUser = SessionManager.inst.findByJunZhuId(Long.valueOf(ownerid));
		if(null==sessionUser){
			%>
			<p>用户未登录</p>
			<%
		}else{
			List<ProtobufMsg> list = LogMgr.inst.getReceiveProtoLog(sessionUser.session);
			%>
			<table border="1">
			<tr>
				<th>协议号</th>
				<th>长度</th>
				<th>协议名称</th>
				<th>协议内容</th>
			</tr>
			<%
			if(null==list){
				%>
				<tr><td colspan="4"><p>没有协议号记录</p></td></tr>
				<%
			} else{
				Class clazz = Class.forName("com.manu.network.PD");
				Field[] fields = clazz.getDeclaredFields(); 
				Map<Integer,String> fieldMap = new HashMap<Integer,String>();
				for (Field field : fields) {  
					Object obj = field.get(field.getName());
					if(obj instanceof Integer){
						fieldMap.put(field.getInt(field.getName()), field.getName());
					}else if(obj instanceof Short){
						fieldMap.put((int)field.getShort(field.getName()), field.getName());
					}
		        }  
				for(ProtobufMsg msg:list){
					int protoId = msg.id;
					int length = 0;
					String content = "null";
					if(msg.builder!=null){
						length = msg.builder.build().toByteArray().length;
						content = com.googlecode.protobuf.format.JsonFormat.printToString((com.google.protobuf.Message) msg.builder.build());
					}
					if(protoId!=22002){// 过滤精灵移动请求 
						%>
						<tr>
							<td><%=protoId %></td>
							<td><%=length %></td>
							<td><%=fieldMap.get(protoId) %></td>
							<td><%=content %></td>
						</tr>
						<%
					}
				}
				%>
				</table>
				<%
			}
		}
	}
	%>
</body>
</html>