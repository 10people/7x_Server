<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="com.yy.YYMgr"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<%!
HttpServletRequest req;
String get(String key){
	String v = req.getParameter(key);
	return v == null ? "" : v.trim();
}
%>
<%
req =  request;
do{
	String info = request.getParameter("info");
	if(info == null){
		break;
	}
	info = get("info");
	String qq = get("qq");
	String faq = get("faq");
	Redis.getInstance().set("YYMgr.keFuInfo", info);
	Redis.getInstance().set("YYMgr.keFuQQ", qq);
	Redis.getInstance().set("YYMgr.faq", faq);
	YYMgr.inst.load();
}while(false);
String info = YYMgr.keFuInfo;
String qq = YYMgr.keFuQQ;
String faq = YYMgr.faq;
%>
<form action="" method="post">
<table>
<tr><td>客服信息：</td><td><textarea name="info"><%=info%></textarea></td><td></td></tr>
<tr><td>客服QQ：</td><td><input type="text" name="qq" value="<%=qq%>"/></td><td>只输入QQ号，会显示在界面【官方QQ群：】后面</td></tr>
<tr><td>常见问题：</td><td><textarea name="faq"><%=faq%></textarea></td><td></td></tr>
<tr><td></td><td><input type="submit" value="设置"/></td><td></td></tr>
</table>
</form>
</body>
</html>