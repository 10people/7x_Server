<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.account.Account"%>
<%@page import="java.util.List"%>
<%@page import="com.qx.yuanbao.YuanBaoInfo"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>元宝日志</title>
</head>
<body>
	<h3>元宝变动日志记录</h3>
	<br>
	<form action="">
		按账号 <input type="text" name="name"> 或 按君主id <input type="text" name="ownerid"> <button type="submit">查询</button>
	</form>
	<br>
	<table border="1">
		<%
			StringBuffer where = new StringBuffer("where 1=1");
			String ownerid = request.getParameter("ownerid");
			String name = request.getParameter("name");
			name = name == null ? "": name.trim();
			ownerid = (ownerid == null ? "":ownerid.trim());
			if(session.getAttribute("name") != null && name.length()==0 && ownerid.length()==0){
				name = (String)session.getAttribute("name");
			}
			if(null!=ownerid&&!"".equals(ownerid)){// 按ownerid查询 
				where.append(" and ownerid="+ownerid+"");
			}
			if(null!=name&&!"".equals(name)){// 按账号查询 
				Account account = HibernateUtil.getAccount(name);
				if(null!=account){
					where.append(" and ownerid="+(account.getAccountId()*1000+GameServer.serverId)+"");
				} else{
					where = new StringBuffer("1!=1");
				}
			}
			if(!where.toString().endsWith("where 1=1")){// 如果加了查询条件 
				List<YuanBaoInfo> list = (List<YuanBaoInfo>) HibernateUtil.list(
						YuanBaoInfo.class, where.toString());
						if(null!=list&&list.size()!=0){
							session.setAttribute("name", name);
						%>
						<tr>
						<th>自增id</th>
						<th>君主id</th>
						<th>账号</th>
						<th>元宝变动前</th>
						<th>元宝增量</th>
						<th>元宝变动后</th>
						<th>变动时间</th>
						<th>变动原因</th>
					</tr>
						<%
							for (YuanBaoInfo info : list) {
								Account account = HibernateUtil.find(Account.class,
									(info.getOwnerid() - GameServer.serverId) / 1000);// 获取账号
					%><tr>
						<td><%=info.getDbId()%></td>
						<td><%=info.getOwnerid()%></td>
						<td><%=account.getAccountName()%></td>
						<td><%=info.getYuanbaoBefore()%></td>
						<td><%=info.getYuanbaoChange()%></td>
						<td><%=info.getYuanbaoAfter()%></td>
						<td><%=info.getTimestamp()%></td>
						<td><%=info.getReason()%></td>
					</tr>
					<%
							}
						} else{
							out.print("没有元宝记录");
						}
				}
		%>
	</table>
</body>
</html>