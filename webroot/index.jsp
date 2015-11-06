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
<%
if(request.getParameter("frame")==null){
%>
<a href="main.jsp">导航</a><br/>
<%}else{ %>
<pre>
几个Linux操作的问题：
1、用什么工具远程操作Linux服务器？
2、用1中的工具，如何设置IP、端口、用户名、密码、编码、界面颜色？
3、命令cd,mv,rm,cp,unzip,kill,netstat,ps,top,tail,head,grep,alias都是干什么的？
4、如何查看一个命令的帮助信息？
5、如何修改Linux上的文本 ？
6、vi、vim怎么用？
7、如何编写Linux脚本？
8、~/.bashrc文件有什么用？
--
外网启动memcached ./memcached -d -r -u root -vv>>m.log 2>&1
清空redis数据  先运行 redis-cli，在里面执行FLUSHDB 
参考 http://blog.chinaunix.net/uid-192452-id-4034867.html
</pre>
<%} %>
</body>
</html>