<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.manu.dynasty.admin.Admin"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>注册</title>
<script type="text/javascript">
	function check(){
		var name = document.getElementById("name");
		var pwd = document.getElementById("pwd");
		if(name.value==""){
			alert("请填写用户名");
			return false;
		}
		if(pwd.value==""){
			alert("请填写密码");
			return false;
		}
	}
</script>
</head>
<body>
	<center>
	<h2>七雄后台用户修改</h2>
	<%
	String id = request.getParameter("id");
	Admin admin = HibernateUtil.find(Admin.class, Long.valueOf(id));
	%>
	<form action="../admin" method="post" onsubmit="return check()">
		<table>
			<tr><td>用户名：</td><td><input type="text" id="name" name="name" value="<%=admin.getName()%>"/></td></tr>
			<tr><td>密   码：</td><td><input type="password" id="pwd" name="pwd" value="<%=admin.getPwd()%>"/></td></tr>
			<tr><td colspan="2"><button type="submit">修改</button><input type="hidden" name="action" value="update"/><input type="hidden" name="id" value="<%=admin.getId()%>"/></td></tr>
		</table>
	</form>
	</center>
</body>
</html>