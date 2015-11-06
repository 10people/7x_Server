<%@page import="com.qx.quartz.job.LogPerMinuteJob"%>
<%@page import="org.quartz.Trigger"%>
<%@page import="org.quartz.impl.matchers.GroupMatcher"%>
<%@page import="org.quartz.JobKey"%>
<%@page import="org.quartz.Scheduler"%>
<%@page import="java.lang.reflect.Field"%>
<%@page import="com.qx.quartz.SchedulerMgr"%>
<%@page import="com.qx.account.AccountManager"%>
<%@page import="com.qx.world.Scene"%>
<%@page import="java.util.Enumeration"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.qx.world.Player"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@include file="/myFuns.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>监控</title>
</head>
<body>
	<%
		Field f = SchedulerMgr.class.getDeclaredField("scheduler");
		f.setAccessible(true);
		Scheduler scheduler = (Scheduler) f.get(SchedulerMgr.inst);
		for (String groupName : scheduler.getJobGroupNames()) {

			for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher
					.jobGroupEquals(groupName))) {

				String jobName = jobKey.getName();
				String jobGroup = jobKey.getGroup();

				//get job's trigger
				Class c = scheduler.getJobDetail(jobKey).getJobClass();
				out(c);
				br();
				for(Trigger t : scheduler.getTriggersOfJob(jobKey)){
					space();out("--"+t.getKey());
					if(c == LogPerMinuteJob.class){
						//scheduler.unscheduleJob(t.getKey());
					}
					br();
				}
			}
		}
	%>

</body>
</html>