<%@page import="java.util.Arrays"%>
<%@page import="com.qx.jingmai.JmMgr"%>
<%@page import="com.manu.dynasty.template.Jingmai"%>
<%@page import="com.qx.jingmai.JmBean"%>
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
<style type="text/css">
table{float:left; margin-right: 20px;}
td{ border-bottom:1px solid;}
</style>
<script type="text/javascript">
function go(act){
	var v = document.getElementById(act).value;
	location.href = '?action='+act+"&v="+v;
}
</script>
<title>Insert title here</title>
</head>
<body>
	<%
		String action = request.getParameter("action");
		String name = request.getParameter("account");
		String itemId = request.getParameter("itemId");
		name = name == null ? "" : name;
		if (session.getAttribute("name") != null && name.length()==0) {
			name = (String) session.getAttribute("name");
		}
		itemId = itemId == null ? "" : itemId;
	%>
	<form action="">
		账号<input type="text" name="account" value="<%=name%>">
		<button type="submit">查询</button>
	</form>
<!-- 	<form action=""> -->
<!-- 		<input type="hidden" name="action" value="addItem"> <input -->
<%-- 			type="hidden" name="account" value="<%=name%>"> 物品/装备id<input --%>
<%-- 			type="text" name="itemId" value="<%=itemId%>"> --%>
<!-- 		<button type="submit">添加</button> -->
<!-- 	</form> -->
	<%
		if (name != null && name.length() > 0) {
			Account account = HibernateUtil.getAccount(name);
			if (account == null) {
	%>没有找到<%
		//HibernateUtil.saveAccount(name);
			} else {
				session.setAttribute("name", name);
	%><%=account.accountId%>:<%=account.accountName%>
	<br /> 经脉--------------
	<br />
	<%
		JmBean bean = HibernateUtil.find(JmBean.class, account.accountId);
		if(bean == null){
			out.println("没有经脉数据。<br/>");
			out.println("<a href='?action=createBean'>创建</a>");
			if("createBean".equals(request.getParameter("action"))){
				bean = new JmBean();
				bean.dbId = account.accountId;
				bean.daMai = 1;
				bean.zhouTian = 1;
				bean.xueWei = new int[20];
				HibernateUtil.save(bean);
			}else{
				return;
			}
		}
	%>
	大脉进度:<%=bean.daMai%>&nbsp;周天进度(当前不准确):<%=bean.zhouTian%>
	
	<br />
	<table>
		<tr>
			<th>序号</th>
			<th>加点</th>
			<th>操作</th>
		</tr>
		<%
			int cnt0 = 10;
			if (bean == null) {
				cnt0 = 0;
			}else if("add".equals(request.getParameter("action"))){
				int idx = Integer.parseInt(request.getParameter("idx"));
				bean.xueWei[idx] += 1;
				HibernateUtil.save(bean);
			}else if("addPoint".equals(request.getParameter("action"))){
				int v = Integer.parseInt(request.getParameter("v"));
				bean.point += v;
				HibernateUtil.save(bean);
			}else if("fill211".equals(request.getParameter("action"))){
				Arrays.fill(bean.xueWei,11);
				HibernateUtil.save(bean);
			}else if("reset".equals(request.getParameter("action"))){
				Arrays.fill(bean.xueWei,0);
				bean.daMai = 1;
				HibernateUtil.save(bean);
			}
			for (int i = 1; i <= cnt0; i++) {
		%><tr>
			<td><%=i%></td>
			<td><%=bean.xueWei[i]%></td>
			<td><a href='?action=add&idx=<%= i %>'>+1</a>&nbsp;
<%-- 				<a href='?action=add&idx=<%= i %>'>+1</a> --%>
			</td>
		</tr>
		<%
			}
					Jingmai effect = JmMgr.inst.calcEffect(bean);
					String input = request.getParameter("v");
					 if(input == null)input = "1";
					%>
					
	</table>
					经脉加成<br/>
					攻击：<%=effect.gongji %><br/>
					防御：<%=effect.fangyu %><br/>
					生命：<%=effect.shengming %><br/>
					统帅：<%=effect.tongli %><br/>
					武艺：<%=effect.yongli %><br/>
					智谋：<%=effect.mouli %><br/>
					<a href='?action=reset'>--重置--</a>
					<a href='?action=fill211'>--直升11周天--</a><br/>
					<br/>剩余点数：<%=bean.point %>&nbsp;<input type='text' id='addPoint' value='<%=input%>'/><input type='button' value='增加' onclick='go("addPoint")'/><br/><br/>
					<%
				}
			}
		%>
	<br />
</body>
</html>