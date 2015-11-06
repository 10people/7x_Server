<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>登录</title>
</head>
<body>
<form action="login?action=login" method="post">
	<center>
		<h2>七雄后台登录</h2>
		<table>
			<tr><td>用户名：</td><td><input id="name" type="text" name="name" value=""/></td></tr>
			<tr><td>密   码：</td><td><input id="pwd" type="password" name="pwd" value=""/></td></tr>
			<tr><td colspan="2"><button type="submit">登录</button></td></tr>
		</table>
	</center>
</form>
<%
	String failed = request.getParameter("failed");
	if(failed!=null&&failed.length()>0){
		%>
		<script type="text/javascript">
			alert("用户名或密码错误！");
		</script>
		<%
	}
	%>
</body>
</html>