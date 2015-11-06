<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.manu.network.ProtoBuffEncoder" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<%
String act = request.getParameter("act");
if(act != null && "changeState".equals(act)) {
	boolean state = ProtoBuffEncoder.isBattleInfoRecord();
	ProtoBuffEncoder.setBattleInfoRecord(!state);
}

out.println("战斗数据包大小记录开启状态：<a href='battlePackageInfo.jsp?act=changeState'> " + ProtoBuffEncoder.isBattleInfoRecord() +" <a/>- 点击切换状态");
if(ProtoBuffEncoder.isBattleInfoRecord()) {
	tableStart();
		trS();td("协议号");td("数据大小");trE();
		List<Map<Integer, Integer>> battleInfoList = ProtoBuffEncoder.getBattleInfoList();
		for(Map<Integer, Integer> map : battleInfoList) {
			trS();
				for(Map.Entry<Integer, Integer> entry : map.entrySet()) {
					td(entry.getKey());
					td(entry.getValue());
				}
			trE();
		}
	tableEnd();
}



%>

</body>
</html>