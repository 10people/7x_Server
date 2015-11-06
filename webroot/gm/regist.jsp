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
	<h2>七雄后台用户注册</h2>
	<form action="../admin" method="post" onsubmit="return check()">
		<table>
			<tr><td>用户名：</td><td><input type="text" id="name" name="name"/></td></tr>
			<tr><td>密   码：</td><td><input type="password" id="pwd" name="pwd"/></td></tr>
			<tr><td colspan="2"><button type="submit">注册</button><input type="hidden" name="action" value="regist"/></td></tr>
		</table>
	</form>
	</center>
</body>
</html>