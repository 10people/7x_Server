<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.qx.alliance.AllianceMgr"%>
<%@page import="org.apache.taglibs.standard.tag.common.core.ForEachSupport"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.manu.dynasty.template.JCZCity" %>
<%@page import="com.qx.activity.*" %>
<%@page import="com.manu.dynasty.util.DateUtils" %>
<%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>城池信息</title>
</head>
<body>
	<strong>城池信息</strong><br /><br />
	<%
		//处理表单
		List<JCZCity> citySettings = TempletService.getInstance().listAll(JCZCity.class.getSimpleName());
		Map<Integer,JCZCity>jczmap = new HashMap<Integer,JCZCity>();
		for (JCZCity jczCity : citySettings) {
			jczmap.put(jczCity.getId(),jczCity);
		}
		String act = request.getParameter("action"); 
		if("setBidTime".equals(act)){ //设置时间段
			String time1s = request.getParameter("time1s");
			String time1e = request.getParameter("time1e");
			String time2s = request.getParameter("time2s");
			String time2e = request.getParameter("time2e");
			String time3s = request.getParameter("time3s");
			String time3e = request.getParameter("time3e");
			if(!"".equals(time1s) && !"".equals(time1e) && !"".equals(time2s) && !"".equals(time2e) && !"".equals(time3s) && !"".equals(time3e)){
				StrengthGetMgr.STRENGTH_GET_TIME1_START = time1s;
				StrengthGetMgr.STRENGTH_GET_TIME1_END = time1e;
				StrengthGetMgr.STRENGTH_GET_TIME2_START = time2s;
				StrengthGetMgr.STRENGTH_GET_TIME2_END = time2e;
				StrengthGetMgr.STRENGTH_GET_TIME3_START = time3s;
				StrengthGetMgr.STRENGTH_GET_TIME3_END = time3e;
			}else{
				alert("输入为空设置失败");
			}
		}else if("cleardata".equals(act)){
			String sql = "DELETE FROM "+StrengthGetBean.class.getSimpleName();
			HibernateUtil.executeSql(sql);
		}
%>
	<br>
	<strong>当前郡城战时间段:</strong><br><br>
	<%
		tableStart();
			trS();
				ths("第一阶段开始");
				ths("第一阶段结束");
				ths("第二阶段开始");
				ths("第二阶段结束");
				ths("第三阶段开始");
				ths("第三阶段结束");
			trE();
			trS();
				td(StrengthGetMgr.STRENGTH_GET_TIME1_START);
				td(StrengthGetMgr.STRENGTH_GET_TIME1_END);
				td(StrengthGetMgr.STRENGTH_GET_TIME2_START);
				td(StrengthGetMgr.STRENGTH_GET_TIME2_END);
				td(StrengthGetMgr.STRENGTH_GET_TIME3_START);
				td(StrengthGetMgr.STRENGTH_GET_TIME3_END);
			trE();
		tableEnd();
	%>
	<br>
	<form action="" method="post">
		<input type="hidden" name="action" value="setBidTime"/>
		<strong>设置城池战时间段(<font color="red">格式不要输入错误</font>) </strong><br><font color="red">*时间依次递减</font><br>
		第一阶段开始<input type="text" name="time1s" value="<%=StrengthGetMgr.STRENGTH_GET_TIME1_START%>"/><br>
		第一阶段结束<input type="text" name="time1e" value="<%=StrengthGetMgr.STRENGTH_GET_TIME1_END%>"/><br>
		第二阶段开始<input type="text" name="time2s" value="<%=StrengthGetMgr.STRENGTH_GET_TIME2_START%>"/><br>
		第二阶段结束<input type="text" name="time2e" value="<%=StrengthGetMgr.STRENGTH_GET_TIME2_END%>"/><br>
		第三阶段开始<input type="text" name="time3s" value="<%=StrengthGetMgr.STRENGTH_GET_TIME3_START%>"/><br>
		第三阶段结束<input type="text" name="time3e" value="<%=StrengthGetMgr.STRENGTH_GET_TIME3_END%>"/><br>
		<input type="submit" value="修改"/>
	</form>
		<br>
		<form action="" method="post">
			<input type="hidden" name="action" value="cleardata"/>
			<input type="submit" value="清除领奖记录"/>
		</form>
		<br>
		<br>
		当前服务器时间：<%=(new Date().toString()) %>
</body>
</html>