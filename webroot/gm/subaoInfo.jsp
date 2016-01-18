<%@page import="com.qx.prompt.PromptMsgMgr"%>
<%@page import="qxmobile.protobuf.Prompt.SuBaoMSG"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.manu.network.SessionUser"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.qx.prompt.PromptMSG"%>
<%@page import="java.util.List"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	<%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%
		String jzId = request.getParameter("jzId");
		jzId = jzId == null ? "" : jzId;
		jzId=jzId.trim();
	%>
	<%		String action = request.getParameter("action");
			if (action != null && action.equals("sendSuBao")) {
			String otherJzId = request.getParameter("otherJzId");
			String eventId = request.getParameter("eventId");
			String horseType = request.getParameter("horseType");
			String param = request.getParameter("param");
			if (jzId == null || jzId.equals("") || 
				eventId == null || eventId.equals("")) {
				out.println("<font color='red'>有内容是空的</font>");
			} else {
				jzId = new String(jzId.getBytes("ISO-8859-1"), "utf-8");
				otherJzId = new String(otherJzId.getBytes("ISO-8859-1"), "utf-8");
				eventId = new String(eventId.getBytes("ISO-8859-1"), "utf-8");
				if(horseType!=null&&!"".equals(horseType)){
					horseType = new String(horseType.getBytes("ISO-8859-1"), "utf-8");
				}else{
					horseType="1";
				}
				if(param!=null&&!"".equals(param)){
					param = new String(param.getBytes("ISO-8859-1"), "utf-8");
				}else{
					param="";
				}
				long JzId1= jzId==null||"".equals(jzId)?0:Long.parseLong(jzId);
				long JzId2= otherJzId==null||"".equals(otherJzId)?0:Long.parseLong(otherJzId);
				String[] params=param.split(",");
				PromptMSG msg = PromptMsgMgr.inst.saveLMKBByCondition(JzId1, JzId2, params, Integer.parseInt(eventId), Integer.parseInt(horseType) );
				if(msg!=null) {
					SessionUser su = SessionManager.inst.findByJunZhuId(JzId1);
					if (su != null){
						// 联盟成员的君主id
						SuBaoMSG.Builder subao = SuBaoMSG.newBuilder();
						subao=PromptMsgMgr.inst.makeSuBaoMSG(subao, msg);
						su.session.write(subao.build());
					}
					out.println("<font color='red'>发送成功！</font>");
				}else{
					out.println("<font color='red'>发送失败！</font>");
				}
			}
	}
	%>
	<form action="">
		君主id<input type="text" name="jzId" value="<%=jzId%>">
		<button type="submit">查询</button>
	</form>
	<br>
	
	<form name="sendsuBaoForm" action="">
	<table border='1'>
		<tr>
		<td>君主id&nbsp;</td>
		<td><input type="text" name="jzId" value='<%=jzId%>' /> 
		  <input type="hidden" name="action" value="sendSuBao"/>
		 </td>
		 </tr>
		<tr>	
		<td>其他君主id </td>
		<td><input type="text" name="otherJzId" value='' />  </td>
		</tr>
		<tr>
		<td>事件类型（ReportTemp的event）  </td>
		<td><input type="text" name="eventId" value='' /></td>
		</tr>
		<tr>	
		<td>马车类型（1-5中的任意数可不填）</td>
		<td><input type="text" name="horseType" value='' /></td>
		</tr> 
		<tr>	
		<td title="用英文“,”分开，参数1： 君主名字1； 参数2： 君主名字2；  参数3：铜币收入 ；参数4： 运镖者马车的价值存储 ； 参数5：拼接安慰奖励的原马车价值；">其他参数（详细说明：鼠标放这别动  ）</td>
		<td><input type="text" name="param" value='' /></td>
		</tr>
		<tr><td colspan="2"><input type="submit" value="发送" /></td>
		</tr>
		</table>
	</form>
	<%
		if (jzId.matches("\\d+")) {
				long junzhuId = Long.parseLong(jzId);
				JunZhu junzhu = HibernateUtil.find(JunZhu.class, junzhuId);
				List<PromptMSG> msgList = HibernateUtil.list(PromptMSG.class, "where  jzId="+jzId+"");
	%><table border='1'>
		<tr>
			<th>id</th>
			<th>内容</th>
			<th>jzId</th>
			<th>jzName</th>
			<th>otherJzId</th>
			<th>otherJName</th>
			<th>配置Id</th>
			<th>事件Id</th>
			<th>award</th>
			<th>realCondition</th>
			<th>cartWorth</th>
			<th>创建时间</th>
		</tr>
		<%
			for (PromptMSG msg:msgList) {
		%>	
		<tr>
			<td><%=msg.id%></td>
			<td><%=msg.content%></td>
			<td><%=msg.jzId%></td>
			<td><%=msg.jzName1%></td>
			<td><%=msg.otherJzId%></td>
			<td><%=msg.jzName2%></td>
			<td><%=msg.configId%></td>
			<td><%=msg.eventId%></td>
			<td><%=msg.award%></td>
			<td><%=msg.realCondition%></td>
			<td><%=msg.cartWorth%></td>
			<td><%=msg.addTime%></td>
		</tr>	
		<%}%>
	</table>
	<%} %>
</body>
</html>