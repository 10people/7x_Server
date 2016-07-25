<%@page import="com.qx.junzhu.KeJiMgr"%>
<%@page import="com.qx.junzhu.JzKeji"%>
<%@page import="com.manu.dynasty.template.BaseItem"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.qx.bag.EquipGrid"%>
<%@page import="com.qx.bag.BagGrid"%>
<%@page import="java.util.List"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.qx.bag.BagMgr"%>
<%@page import="com.qx.bag.Bag"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
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
		String action = request.getParameter("action");
		String name = request.getParameter("account");
		String itemId = request.getParameter("itemId");
		name = name == null ? "" : name;
		if(session.getAttribute("name") != null){
			name = (String)session.getAttribute("name");
		}
		itemId = itemId == null ? "" : itemId;
	%>
	<h1>君主科技</h1>
	<form action="">
		账号<input type="text" name="account" value="<%=name%>">
		<button type="submit">查询</button>
	</form>
	<form action="">
		<input type="hidden" name="action" value="addItem"> <input
			type="hidden" name="account" value="<%=name%>"> 物品/装备id<input
			type="text" name="itemId" value="<%=itemId%>">
		<button type="submit">添加</button>
	</form>
	<%
		if (name != null && name.length() > 0) {
			Account account = HibernateUtil.getAccount(name);
			if (account == null) {
	%>没有找到<%
		//HibernateUtil.saveAccount(name);
			} else {
				session.setAttribute("name", name);
	%><%=account.accountId%>:<%=account.accountName%>
	<br /> 君主科技--------------
	<br />
	<%
	long start = account.accountId * KeJiMgr.spaceFactor;
	long end = start + KeJiMgr.keJiCnt;
	List<JzKeji> list0 = HibernateUtil.list(JzKeji.class, "where dbId>="+start+" and dbId<"+end);
	%>个数:<%=list0.size() %><br/>
	<table border='1'>
		<tr>
			<th>dbId</th>
			<th>科技ID</th>
			<th>已研究次数</th>
			<th>冷却（秒）</th>
		</tr>
		<%
		int cnt0 = list0.size();
			for (int i = 0; i < cnt0; i++) {
				JzKeji k = list0.get(i);
		%><tr>
			<td><%=k.dbId%></td>
			<td><%=k.kejiId%></td>
			<td><%=k.ciShu%></td>
			<td><%=k.lengQueTime/1000%></td>
		</tr>
		<%}
			}
			}
		%>
	</table>
	<br />
</body>
</html>