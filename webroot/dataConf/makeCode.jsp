<%@page import="com.qx.cdkey.CDKeyInfo"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="com.manu.dynasty.util.DateUtils"%>
<%@page import="com.qx.cdkey.CDKeyMgr"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<%!
HttpServletRequest req;
String get(String key){
	String v = req.getParameter(key);
	return v == null ? "" : v.trim();
}
%>
<%
req =  request;
List<CDKeyInfo> list = null;
do{
	String prefix = request.getParameter("prefix");
	if(prefix == null){
		break;
	}
	if(prefix.isEmpty()){
		out.append("请输入前缀");
		break;
	}
	String award = get("award");
	if(award.isEmpty()){
		out.append("请输入奖励");
		break;
	}
	String codeLen = get("codeLen");
	if(codeLen.isEmpty()){
		out.append("请输入长度");
		break;
	}
	String type = get("type");
	if(type.isEmpty()){
		out.append("请输入批次");
		break;
	}
	String cnt = get("cnt");
	if(cnt.isEmpty()){
		out.append("请输入批次");
		break;
	}
	String dt = get("dt");
	if(cnt.isEmpty()){
		out.append("请输入批次");
		break;
	}
	request.setAttribute("prefix", prefix);
	Date date = DateUtils.text2Datetime(dt);
	CDKeyMgr.KEY_LENGTH = Integer.parseInt(codeLen);
	if(CDKeyMgr.KEY_LENGTH<6){
		out.append("长度不能小于6");
		break;
	}
	list = CDKeyMgr.inst.generateCDKey(Integer.parseInt(type), date, Integer.parseInt(cnt), award,prefix);
}while(false);
String prefix = get("prefix");
String award = get("award");
String codeLen = get("codeLen");if(codeLen.isEmpty()){codeLen="8";}
String type = get("type");
%>
<body>
<h1>生成激活码</h1>
请先生成1个，去测试下结果是否正确！<br/>
<form action="">
<table>
<tr><td>前缀：</td><td><input type="text" name="prefix" value="<%=prefix%>"/></td><td></td></tr>
<tr><td>奖励：</td><td><input type="text" name="award" value="<%=award%>"/></td><td>type:itemId:count#type:itemId:count</td></tr>
<tr><td>长度：</td><td><input type="number" name="codeLen"  value="<%=codeLen%>"/></td><td>包含前缀</td></tr>
<tr><td>批次：</td><td><input type="number" name="type"  value="<%=type%>"/></td><td>填0表示随意领，大于0则同一玩家只能领一次此批次礼包</td></tr>
<tr><td>有效期至：</td><td><input type="text" name="dt" value="2016-8-1 20:40:26"/></td><td>格式:2016-3-21 20:40:26</td></tr>
<tr><td>数量：</td><td><input type="text" name="cnt" value="1"/></td><td>此次生成多少个</td></tr>
<tr><td></td><td><input type="submit" value="生成"/></td><td></td></tr>
</table>
</form>
<pre>
<%
if(list != null){
	out.append("生成成功，请拿一个去测试下奖励是否正确\n");
	for(int i=0;i<list.size();i++){
		CDKeyInfo info = list.get(i);
		out.append(info.getCdkey());out.append("\n");
	}
}
%>
已有批次：
</pre>
</body>
</html>