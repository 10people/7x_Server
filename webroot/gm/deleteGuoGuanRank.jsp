<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="java.util.Set"%>
<%@page import="com.qx.ranking.RankingMgr"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>testScene</title>
</head>
<body>
<%

for(int i=0; i<=7;i++){
	RankingMgr.DB.del(RankingMgr.GUOGUAN_RANK+"_"+i);
	out("删除 过关帮 国家 i==" + i+" 成功");
}
%>

</body>
</html>