<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>无双后台</title>
</head>
<body>
	<h1>无双后台</h1>
	<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
	<c:choose>
		<c:when test='${param.frame==null}'>
			<a href="main.jsp">导航</a>
			<br />
		</c:when>
		<c:otherwise>
			<pre>
			外网启动memcached ./memcached -d -r -u root -vv>>m.log 2>&1
			清空redis数据  先运行 redis-cli，在里面执行FLUSHDB 
			参考 http://blog.chinaunix.net/uid-192452-id-4034867.html		
		</pre>
		</c:otherwise>
	</c:choose>
</body>
</html>