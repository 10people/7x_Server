<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="com.qx.activity.XianShiActivityMgr"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.qx.alliance.AllianceMgr"%>
<%@page import="com.manu.dynasty.template.LianmengEvent"%>
<%@page import="java.util.Map"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	
<%

List<JunZhu> list =HibernateUtil.list(JunZhu.class, "where 1=1");
Map<String,Integer> logCountMap= new HashMap<String, Integer>();
for (JunZhu jz : list) {
	String loginCount= Redis.getInstance().get(XianShiActivityMgr
			.XIANSHI7DAY_KEY + jz.id);
	Integer tcount=logCountMap.get(loginCount);
	if(tcount==null){
		tcount=1;
	}else{
		tcount++;
	}
	logCountMap.put(loginCount, tcount);
}
 for (String key : logCountMap.keySet()) {
		out.println("登录天数："+key+"         人数:"+ logCountMap.get(key)+"<br>");
}
// Map<Integer, LianmengEvent> mapA=AllianceMgr.inst.lianmengEventMap;
// out.println(mapA);
// for (Integer key : mapA.keySet()) {
// 	out.println("key"+key+"         id:"+ mapA.get(key).ID+"         value:"+ mapA.get(key).str);
%>
</body>
</html>