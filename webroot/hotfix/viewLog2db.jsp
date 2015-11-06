<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="log.Log2DBBean"%>
<%@page import="java.util.List"%>
<%@page import="log.Log2DB"%>
<%@page import="java.io.FileWriter"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.util.Date"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="net.sf.json.JSONObject" %>
<%@include file="../myFuns.jsp" %>
<% //日志里处理request parameter没有做编码处理，导致不能导入到数据库
List<Log2DBBean> list = HibernateUtil.list(Log2DBBean.class, 
		"where rowCnt = 0 and result != '文件不存在' and result != '成功'");
out("size:"+list.size());br();
tableStart();
for(Log2DBBean bean : list){
	trS();
	td(bean.id);td(bean.fileName);td(bean.importTime); td(bean.result);td(bean.rowCnt);
	td("<a href='parseRegLog.jsp?id="+bean.id+"'>fix</a>");
	trE();
	//out("<iframe src='parseRegLog.jsp?id="+bean.id+"'></iframe>");
}
tableEnd();
%>