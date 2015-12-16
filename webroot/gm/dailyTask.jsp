<%@page import="com.qx.activity.ActivityMgr"%>
<%@page import="com.manu.dynasty.template.DescId"%>
<%@page import="java.util.Map"%>
<%@page import="com.manu.dynasty.template.RenWu"%>
<%@page import="javax.swing.text.Document"%>
<%@page import="com.qx.event.EventMgr"%>
<%@page import="com.qx.task.DailyTaskConstants"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
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
<%@include file="/myFuns.jsp"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>query or delete email</title>
<script type="text/javascript">
function go(act){
	location.href = '?action='+act;
}
function ss(){
	alert("增加成功");
}
function ff(){
	alert("增加失败");
}

</script>
 
</head>
<body>
	<%
		String name = request.getParameter("account");
		name = name == null ? "" : name;
		if (session.getAttribute("name") != null && name.length() == 0) {
			  name = (String) session.getAttribute("name");
			}
	%>
	<form action="dailyTask.jsp">
		账号<input type="text" name="account" value="<%=name%>"><br>
		<button type="submit">查询</button>
	<%
	tableStart();
	trS();
	td("任务id");td("名称");td("描述");//td("类型");
	td("条件");td("奖励");td("联盟贡献");
	trE();
	List<Integer> ids = DailyTaskMgr.taskIdArr;
	Map<Integer, RenWu> m= DailyTaskMgr.renWuMap;
	for(Integer i: ids){
		RenWu r = m.get(i);
		if(r != null){
			trS();
			td(r.id);td(r.name); //td(r.funDesc);
			DescId desc = ActivityMgr.descMap.get(r.funDesc);
	        if(desc != null){
	        	td(desc.getDescription());
	        }
			//td(r.type);
			td(r.condition);td(r.jiangli);td(r.LmGongxian);
			trE();
		}
	}
	tableEnd();
	br();
	out("---------------------------------------------");
	    Account account = null;
	    if(name != null && name.length()>0){
	        account = HibernateUtil.getAccount(name);
	    }
	    if(account == null){
    %>没有找到
    <%
    }else{

        session.setAttribute("name", name);
        %><br>注册账号：<%=account.getAccountName()%><br> 账号id：<%=account.getAccountId()%><%
         long junZhuId = account.getAccountId() * 1000 + GameServer.serverId;
         JunZhu junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
         if(junzhu == null){
            out("没有君主");
         }else{
             %><br> 君主id是：<%=junzhu.id%> <br>君主姓名是：<%=junzhu.name%><%
			String action = request.getParameter("action");
             br();br();
             String v1 = "";
             String v2 = "";
             br();
             %>任务Id：<input type='text' id='rewu' name='rewu' value='<%=v1%>' /><% 
             br();
             br();
              %>增加的次数：<input type='text' id='jinduA' name='jinduA' value='<%=v2%>'/>
          <% 
             br();
             //out("<input type='button' value='增加' onclick='go(\"addJindu\")' />");
           //  out("<input type='button' value='增加' onclick='go(\"addJindu\")' />");
            %> <input type="Submit" value="增加"> <%
       %></form>
       <form action="">
       <%
             br();
       String vs = request.getParameter("rewu");
       String vm = request.getParameter("jinduA");
             if(vs != null && !vs.equals(""))
             {
            	int vv = Integer.parseInt(vs);
            	int vn = Integer.parseInt(vm);
            	try{
		             EventMgr.addEvent(ED.DAILY_TASK_PROCESS, 
		                     new DailyTaskCondition(junZhuId, vv, vn));
	                 out("<script language=\"javascript\" type=\"text/javascript\">ss()</script>");
            	}catch(Exception e){
            		out("<script language=\"javascript\" type=\"text/javascript\">ff()</script>");
            	}
	             
             }
             List<DailyTaskBean> tasks  = DailyTaskMgr.INSTANCE.getDailyTasks(junZhuId);
             if(tasks == null || tasks.size() == 0){
                 // 减小压力不存数据库
                 tasks = DailyTaskMgr.INSTANCE.initTaskList(junZhuId);
             }else{
                 // 重置 task
                 DailyTaskMgr.INSTANCE.resetOrAddTaskList(junZhuId, tasks);
             }
			tableStart();
			trS();
               td("dbId");//td("任务描述");
               td("进度");td("是否完成");td("是否领奖");td("类型");td("上次进本条任务的时间");
               trE();
			for(DailyTaskBean task: tasks){
                RenWu rr = DailyTaskMgr.INSTANCE.renWuMap.get((int)(task.dbId%100));
                 trS();
                 td(task.dbId);
               //  td(rr.funDesc);
                 td(task.jundu);
                 td(task.isFinish);
                 td(task.isGetReward);
                 td(task.type);
                 td(task.time);
                 trE();
			}
			tableEnd();
			}
		}
	%>
	</form>
</body>
</html>