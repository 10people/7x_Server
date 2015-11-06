<%@page import="org.hibernate.SQLQuery"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.manu.dynasty.util.DateUtils"%>
<%@page import="org.hibernate.Transaction"%>
<%@page import="org.hibernate.Session"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>备份PlayerTime表</title>
</head>
<body>
<%
String url = "backupOnlineTime.jsp";
if(!com.manu.dynasty.filter.FilterMgr.exclusion.contains(url)){
	//需要登录后添加白名单
	com.manu.dynasty.filter.FilterMgr.exclusion.add(url);
}
Session ss = HibernateUtil.getSessionFactory().getCurrentSession();
Transaction tx = ss.beginTransaction();
try{
	SimpleDateFormat formatter = new SimpleDateFormat("MMdd_HHmmss");
	String tableName = "PlayerTime"+formatter.format(new Date());//
	SQLQuery query = ss.createSQLQuery("create table "+tableName+" (select * from PlayerTime)");
	int ret = query.executeUpdate();
	out("ret:"+ret);br();
	tx.commit();
	out("执行成功");
}catch(Exception e){
	tx.rollback();
	e.printStackTrace();
	out("执行失败");
}
%>
</body>
</html>