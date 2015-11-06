<%@page import="org.apache.commons.collections.map.LRUMap"%>
<%@page import="com.qx.log.LogMgr"%>
<%@page import="java.lang.reflect.Method"%>
<%@page import="java.lang.reflect.Field"%>
<%@page import="org.apache.commons.collections.map.AbstractHashedMap"%>
<%@page import="com.manu.network.TXSocketMgr"%>
<%@page import="org.hibernate.SQLQuery"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.manu.dynasty.util.DateUtils"%>
<%@page import="org.hibernate.Transaction"%>
<%@page import="org.hibernate.Session"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>备份PlayerTime表</title>
</head>
<body>
<%!
String unlock(LRUMap map)throws Exception{
StringBuffer sb = new StringBuffer();
 Field	f = AbstractHashedMap.class.getDeclaredField("data");
 f.setAccessible(true);
 Object[] data = (Object[]) f.get(map);
 int len = data.length;
 Object e = data[0];
 //
 Class clz = Class.forName("org.apache.commons.collections.map.AbstractLinkedMap$LinkEntry");
 Class lz = clz.getSuperclass();
 //Field fn = clz.getDeclaredField("after");
 Field fn = lz.getDeclaredField("next");
 fn.setAccessible(true);
 Field fns [] = clz.getDeclaredFields();
 for(Field fff: fns){
	 sb.append(fff);
	 sb.append("<br/>");
 }
 //
 for(int i=0; i<len; i++){
	 e = data[i];
	 if(e == null){
		 sb.append(" elem "+i+" is null ");
	 }else{
		 sb.append("class :"+e.getClass().getName());
	 	sb.append(" a find "+fn.get(e));
	 	fn.set(e, null);
	 	sb.append(" after "+fn.get(e)+"<br/>");
	 }
	 sb.append("<br/>");
	 //if(e != null)break;
 }
 sb.append("xx1"+e+"22");
 //
 //Class clz = 
 Method[] ms = AbstractHashedMap.class.getDeclaredMethods();//
 for(Method m :ms){
	if(m.getName().equals("destroyEntry")) {
		m.setAccessible(true);
		m.invoke(map, e);
		sb.append("33");
	}
 }
 //(name, parameterTypes) "destroyEntry"; 
 
 return sb.toString();
 //map.values().iterator().next()
}
%>
<%
 //filter = 
//TXSocketMgr.inst.acceptor.getFilterChain().addLast("loglastpd", filter);
try{
	Field f = LogMgr.class.getDeclaredField("sendProtoIdMap");
	f.setAccessible(true);
	LRUMap map = (LRUMap) f.get(null);
	String ret = unlock(map);
	out(ret);
}catch(Exception e){
	out("22xxxfail:"+e);
	e.printStackTrace();
}
%>
</body>
</html>