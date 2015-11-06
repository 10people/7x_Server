<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map"%>
<%@page import="java.lang.reflect.Method"%>
<%@page import="javax.persistence.Id"%>
<%@page import="javax.persistence.GeneratedValue"%>
<%@page import ="org.hibernate.internal.SessionFactoryImpl"%>
<%@page import ="org.hibernate.metadata.ClassMetadata"%>
<%@page import ="org.hibernate.persister.entity.AbstractEntityPersister"%>
<%@page import="org.hibernate.SessionFactory"%>
<%@page import="java.lang.reflect.Field"%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@include file="/myFuns.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title>Insert title here</title>
</head>
<body>
      显示相关类
	<br/><br/>
	<table align="left" border="1">
		<tr>
			<td>类名</td>
			<td>包名</td>
			<td>主键</td>
			<td>主键是否自增</td>
			<td>实现MCSupport接口</td>
		</tr>
	<%
  	 SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	 Map<String, ClassMetadata>  map = (Map<String, ClassMetadata>) sessionFactory.getAllClassMetadata();
	    for(String entityName : map.keySet()){
	        SessionFactoryImpl sfImpl = (SessionFactoryImpl) sessionFactory;
	        String tableName = ((AbstractEntityPersister)sfImpl.getEntityPersister(entityName)).getTableName();
	        Class className=null;
	        try{
	        	className=Class.forName(entityName);
	        }catch (Exception e) {
	            e.printStackTrace();
	        }
	        Class<?> face[]=className.getInterfaces();
	      	boolean  isMCSupport=false;
	        for (int i = 0; i < face.length; i++) {
	        	if("com.qx.persistent.MCSupport".equals(face[i].getName())){
	        		isMCSupport=true;
	        	}
	        }
	        String idName=null;
	        boolean isAutoAdd=false;
//获取类的属性
	        Field[] fields = className.getDeclaredFields();
			for(Field f : fields){
				//获取字段中包含fieldMeta的注解
				Id meta = f.getAnnotation(Id.class);
				if(meta!=null){
					idName=f.getName();
					GeneratedValue  generatedValue= f.getAnnotation(GeneratedValue.class);
					if(generatedValue!=null){
						isAutoAdd=true;
					}
				}
			}
			
	       %>
	
	       <%
	      	out.print("<td>");
			out.print("" +tableName);
			out.print("</td>");
			
			out.print("<td>");
			out.print("" +entityName);
			out.print("</td>");
			
			out.print("<td>");
			out.print(""+idName);
			out.print("</td>");
			
			out.print("<td>");
			out.print(isAutoAdd?"<font color='red'>是</font>":"<font color='green'>否</font>");
			out.print("</td>");
			
			out.print("<td>");
			out.print(isMCSupport?"<font color='green'>是</font>":"<font color='red'>否</font>");
			out.print("</td>");
			
			out.print("</tr>");
	        }
%>
</table>
</body>
</html>
