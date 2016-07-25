<%@page import="com.manu.dynasty.admin.Admin"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>小七雄-顶部</title>
</head>
<body>
<%Admin admin = (Admin)session.getAttribute("admin"); %>
欢迎来到小七雄后台
&nbsp;<a href='http://192.168.3.80/' target='top'>路由</a>
&nbsp;服务器启动时间：<%=System.getProperty("serverStartTime") %>-可作为服务器更新时间的参考
&nbsp;&nbsp;&nbsp;&nbsp;
欢迎<%=admin.name %>登录&nbsp;&nbsp;<a style="margin-right:0px" href="admin?action=list" target='target'>后台用户管理</a>|<a style="margin-right:5px" href="admin?action=logout" target='_parent'>注销</a>
</body>
</html>