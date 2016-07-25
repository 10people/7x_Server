<%@page import="org.hibernate.criterion.Restrictions"%>
<%@page import="com.qx.account.AccountManager"%>
<%@page import="org.hibernate.criterion.Order"%>
<%@page import="org.hibernate.Criteria"%>
<%@page import="org.hibernate.Query"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.hibernate.Transaction"%>
<%@page import="org.hibernate.Session"%>
<%@page import="org.hibernate.SessionFactory"%>
<%@page import="com.manu.dynasty.template.ZhuXian"%>
<%@page import="com.qx.task.GameTaskMgr"%>
<%@page import="qxmobile.protobuf.GameTask"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.task.WorkTaskBean"%>
<%@page import="com.qx.task.DailyTaskCondition"%>
<%@page import="com.qx.achievement.AchievementCondition"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.manu.dynasty.template.ExpTemp"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.task.DailyTaskMgr"%>
<%@page import="com.qx.task.DailyTaskBean"%>
<%@page import="com.qx.event.Event"%>
<%@page import="com.qx.event.ED"%>
<%@page import="com.qx.account.Account"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>急救站</title>
<script type="text/javascript">
function go(act, id, type){
	if(act == null || act == "") {
		alert("act不能为空");
		return;
	}
	//if(type == null || act == "") {
	//	alert("act不能为空");'
		//return;
	//}
	
	var node = document.getElementById(act+id);
	var v = node.value;
	location.href = '?action='+act+"&v="+v +"&t="+type;
}
</script>

</head>
<body>
	<%
	SessionFactory sessionFactory=HibernateUtil.getSessionFactory();
	Session s = sessionFactory.getCurrentSession();
	Transaction tr = s.beginTransaction();
	List<WorkTaskBean> list = new ArrayList<WorkTaskBean>();
	try{
		String where = "";
		Criteria c = s.createCriteria(WorkTaskBean.class);
		c.add(Restrictions.gt("dbId", Long.valueOf(11100000)));
		c.addOrder(Order.desc("tid"));
		c.setFirstResult(0);
		c.setMaxResults(2000);
    	list = c.list();
    	tr.commit();
	}catch(Exception e){
		tr.rollback();
		e.printStackTrace();
	}
	%>
	<table>
	<th>id</th><th>账号</th><th>密码</th><th>任务进度</th><th>所在服务器</th>
	<%
	for(WorkTaskBean b : list){
		long dbId = b.dbId;
		long accId = dbId/(GameTaskMgr.spaceFactor*1000);
		Account acc = HibernateUtil.find(Account.class, accId);
		ZhuXian task = GameTaskMgr.inst.zhuxianTaskMap.get(b.tid);
		int svr = (int)((dbId/GameTaskMgr.spaceFactor)%1000);
		String svrName = ""+svr;
		switch(svr){
		case 1:svrName = "建虎";			break;
		case 2:svrName = "照文";			break;
		case 3:svrName = "内网";			break;
		case 35:svrName = "王转";			break;
		}
	%>
	<tr>
	<td><%=accId %></td><td><%=acc==null ? "null":acc.accountName %></td><td><%=acc==null ? "null":acc.accountPwd %></td>
	<td><%=task==null ? "null":task.title %></td>
	<td><%=svrName %>
	<tr>
	<%} %>
	</table>
</body>
</html>