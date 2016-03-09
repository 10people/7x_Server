<%@page import="com.qx.yabiao.YaBiaoBean"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript">
function go(act){
	var v = document.getElementById(act).value;
	location.href = '?action='+act+"&v="+v;
}

</script>
</head>
<body>
<%
setOut(out);
String name = request.getParameter("account");
name = name == null ? "": name.trim();
String accIdStr = request.getParameter("accId");// 用户id
accIdStr = (accIdStr == null ? "":accIdStr.trim());
if(session.getAttribute("name") != null && name.length()==0 && accIdStr.length()==0){
	name = (String)session.getAttribute("name");
}
%>
  	<form action="">
	  	账号<input type="text" name="account" value="<%=name%>">&nbsp;或&nbsp;
	  	君主ID<input type="text" name="accId" value="<%=accIdStr%>">
	  	<button type="submit">查询</button>
	</form>
<%
	Account account = null;
	if(name != null && name.length()>0){
		account = HibernateUtil.getAccount(name);
	}else if(accIdStr.length()>0){
		account = HibernateUtil.find(Account.class, (Long.valueOf(accIdStr) - GameServer.serverId) / 1000);
		if(account != null)name = account.getAccountName();
	}
do{
	if(account == null){
		out("没有找到");
		break;
	}
	session.setAttribute("name", name);
	long jzId = account.getAccountId() * 1000 + GameServer.serverId;
	JunZhu junzhu = HibernateUtil.find(JunZhu.class, jzId);
	if(junzhu == null){
		out.println("没有君主");
		break;
	}
	YaBiaoBean ybBean = HibernateUtil.find(YaBiaoBean.class, jzId);
	if(ybBean!=null){
	 String action = request.getParameter("action");
	 boolean sendInfo = true;
	 if("upFuli1".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 System.out.println(v);
		YaBiaoBean bean = HibernateUtil.find(YaBiaoBean.class, jzId);
		bean.todayFuliTimes1=v;
		HibernateUtil.save(bean);
	 }
	 else if("upFuli2".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 System.out.println(v);
		YaBiaoBean bean = HibernateUtil.find(YaBiaoBean.class, jzId);
		bean.todayFuliTimes2=v;
		HibernateUtil.save(bean);
	 }
	 else if("upFuli3".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 System.out.println(v);
		YaBiaoBean bean = HibernateUtil.find(YaBiaoBean.class, jzId);
		bean.todayFuliTimes3=v;
		HibernateUtil.save(bean);
	 }
		YaBiaoBean ybBean2 = HibernateUtil.find(YaBiaoBean.class, jzId);
		int v = 0;
		tableStart();
		trS();
		td("今日领取福利次数1");
		td("<input type='text' id='upFuli1' value='"
				+ybBean2.todayFuliTimes1
				+ "'/><input type='button' value='修改' onclick='go(\"upFuli1\")'/><br/>");
		trE();
		trS();
		trS();
		td("今日领取福利次数2");
		td("<input type='text' id='upFuli2' value='"
				+ybBean2.todayFuliTimes2
				+ "'/><input type='button' value='修改' onclick='go(\"upFuli2\")'/><br/>");
		trE();
		trS();
		trS();
		td("今日领取福利次数3");
		td("<input type='text' id='upFuli3' value='"
				+ybBean2.todayFuliTimes3
				+ "'/><input type='button' value='修改' onclick='go(\"upFuli3\")'/><br/>");
		trE();
		trS();
		td("押镖剩余次数");
		td("<input type='text' id='upFuli' value='"
				+ybBean2.remainYB
				+ "'/><br/>");
		trE();
		tableEnd();
	}else{
		out("当前君主押镖未开启"+jzId);
	}
}while(false);
%>