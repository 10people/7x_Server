<%@page import="com.qx.purchase.PurchaseConstants"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.purchase.PurchaseMgr"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.List"%>
<%@page import="com.qx.purchase.Treasure"%>
<%@page import="java.util.Calendar"%>
<%@page import="com.qx.award.DailyAwardBean"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.manu.dynasty.template.ExpTemp"%>
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
String name = request.getParameter("account");
name = name == null ? "": name;
if(session.getAttribute("name") != null && name.length()==0){
	name = (String)session.getAttribute("name");
}
%>
  <form action="">
	  	账号<input type="text" name="account" value="<%=name%>"><br>
	  	<button type="submit">查询</button>
	  </form>
<%
if(name != null && name.length()>0){
	Account account = HibernateUtil.getAccount(name);
	if(account == null){
		%>没有找到<%
		//HibernateUtil.saveAccount(name);
	}else{
		session.setAttribute("name", name);
		 %>账号<%=account.accountId%>:<%=account.accountName%><br/><%
			long junZhuId = account.getIdentifier()*1000+GameServer.serverId;
		 List<Treasure> treasureList = HibernateUtil.list(Treasure.class, " where junZhuId=" + junZhuId);
		 %>
		 <table border='1'>
		 <tr><th>类型</th><th>次数</th><th>CD</th><th>时间</th><th>是否可领取</th><th>op</th></tr>
		 <%
			String action = request.getParameter("action");
			String id = request.getParameter("id");
		 for(Treasure bean:treasureList){
			 out.println("<tr>");
		 if(bean == null){
			 out.println("没有相关记录");
		 }else{
			 if(String.valueOf(bean.getId()).equals(id)){
				 if("clear".equals(action)){
					 bean.setTimes(0);
					 int time = bean.getType() == PurchaseMgr.TREASURE_CODE_SMALL
							 ? PurchaseConstants.SMALL_WAIT_TIME:PurchaseConstants.MIDDLE_WAIT_TIME;
					 long calcPreTime = System.currentTimeMillis() - (time-20)*1000;
					 bean.setLastGetTime(new Date(calcPreTime));
					 HibernateUtil.save(bean);
				 }else if("clearPreLogin".equals(action)){
					 //bean.preLogin= null;//Calendar.getInstance().getTime();
					 HibernateUtil.save(bean);
				 }else if("addLogin".equals(action)){
					 //bean.leiJiLogin += 1;
					 HibernateUtil.save(bean);
				 }else if("set0Login".equals(action)){
					 //bean.leiJiLogin = 0;
					 HibernateUtil.save(bean);
				 }
			 }
			 out.println("<td>"+bean.getType()+"</td>");
			 out.println("<td>"+bean.getTimes()+"</td>");
			 out.println("<td>"+bean.getCountDown()+"</td>");
			 out.println("<td>"+bean.getLastGetTime()+"</td>");
			 out.println("<td>"+(bean.isGet()?"是":"否")+"</td>");
			 out.println("<td><a href='?action=clear&id="+bean.getId()+"'>清除</a></td>");
			 out.println("<br/>");
			 
		 }
		 out.println("</tr>");
		 }
		 %>
		 </table>
		 <%
	}
}
%>
</body>
</html>