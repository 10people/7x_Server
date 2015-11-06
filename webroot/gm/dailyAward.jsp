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
		 %>账号<%=account.getAccountId()%>:<%=account.getAccountName()%><br/><%
		 DailyAwardBean bean = HibernateUtil.find(DailyAwardBean.class, account.getAccountId());
		 if(bean == null){
			 out.println("没有相关记录");
		 }else{
			 String action = request.getParameter("action");
			 if("clearDaily".equals(action)){
				 bean.preDaily = null;//Calendar.getInstance().getTime();
				 HibernateUtil.save(bean);
			 }else if("clearPreLogin".equals(action)){
				 bean.preLogin= null;//Calendar.getInstance().getTime();
				 HibernateUtil.save(bean);
			 }else if("addLogin".equals(action)){
				 bean.leiJiLogin += 1;
				 HibernateUtil.save(bean);
			 }else if("set0Login".equals(action)){
				 bean.leiJiLogin = 0;
				 HibernateUtil.save(bean);
			 }
			 out.println("领取每日奖励时间："+bean.preDaily);
			 out.println("<a href='?action=clearDaily'>清除</a><br/>");
			 out.println("<br/>");
			 
			 out.println("领取累登奖励次数："+bean.leiJiLogin);
			 out.println("<a href='?action=addLogin'>增加</a>|"+
					 "<a href='?action=set0Login'>置0</a>"+
			 			"<br/>");
			 
			 out.println("领取累登奖励时间："+bean.preLogin);
			 out.println("<a href='?action=clearPreLogin'>清除</a><br/>");
			 out.println("<br/>");
			 /*
			 */
		 }
	}
}
%>
</body>
</html>