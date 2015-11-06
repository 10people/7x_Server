<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.manu.dynasty.admin.Admin"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>用户列表</title>
<script type="text/javascript">
	function del(id){
		var flag = confirm("是否删除用户");
		if(flag){
			location.href="admin?action=delete&id="+id+"";
		}
	}
</script>
</head>
<body>
<center>
	<h2>七雄后台管理</h2>
	<a href="gm/regist.jsp" target="target">注册</a>
	<table border="1" cellspacing="5">
		<th>序号</th><th>登录名</th><th>上次登录时间</th><th>修改时间</th><th>修改人</th><th>操作</th>
		<%
		List<Admin> list = (List<Admin>)request.getAttribute("list");
		for(int i=0;i<list.size();i++){
			Admin admin = list.get(i);
			%>
			<tr>
				<td><%=i+1 %></td>
				<td><%=admin.getName() %></td>
				<td><%
						if(null==admin.getPredate()){%>
						未登录
						<%} 
						else{%>
						<%=admin.getPredate()%>
						<%}%></td>
				<td><%=admin.getUpdatetime()%></td>
				<td>
				<%
				Admin tmp = HibernateUtil.find(Admin.class, admin.getCreateuser());
				if(null==tmp){%>
					用户不存在
				<%}else{%>
					<%=tmp.getName() %></td>
				<%}%>
				<td><a href="gm/updateadmin.jsp?id=<%=admin.getId() %>" target="target">修改</a>|<a href="#" onclick="del('<%=admin.getId() %>')">删除</a></td>
			</tr>
			<%
		}
		
		%>
	</table>
</center>
</body>
</html>