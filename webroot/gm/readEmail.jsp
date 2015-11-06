<%@page import="com.qx.timeworker.TimeWorker"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.manu.dynasty.template.ExpTemp"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="com.qx.email.Email"%>
<%@page import="com.qx.email.EmailMgr"%>
<%@page import="com.qx.bag.BagGrid"%>
<%@page import="qxmobile.protobuf.TimeWorkerProtos.TimeWorkerResponse"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>query or delete email</title>
</head>
<body>
	<%
		String name = request.getParameter("account");
			name = name == null ? "" : name;
			if (session.getAttribute("name") != null && name.length() == 0) {
		name = (String) session.getAttribute("name");
			}
	%>
	<form action="">
		账号<input type="text" name="account" value="<%=name%>"><br>
		<button type="submit">查询</button>
	</form>
	<%
		if (name != null && name.length() > 0) {
		Account account = HibernateUtil.getAccount(name);
		if (account == null) {
	%>没有找到<%
		//HibernateUtil.saveAccount(name);
		} else {
			session.setAttribute("name", name);
	%>账号<%=account.getAccountId()%>:<%=account.getAccountName()%>
	<%
		JunZhu junzhu = HibernateUtil.find(JunZhu.class, account.getAccountId() * 1000 + GameServer.serverId);
			List<Email> mailList = null;
			if (junzhu == null) {
				out.println("没有君主");
			} else {
				if (junzhu.level == 0 || junzhu.shengMingMax == 0) {
					JunZhuMgr.inst.fixCreateJunZhu(junzhu.id, junzhu.name, junzhu.roleId, junzhu.guoJiaId);
				}
				String action = request.getParameter("action");
				
				if ("reqEmailList".equals(action)) {
					mailList = HibernateUtil.list(Email.class, " where receiverId = " + junzhu.id + " and isDelete = "+EmailMgr.DELETE_FALSE);
					String v = request.getParameter("v");
				} else if ("deleteMail".equals(action)) {
					int id = Integer.parseInt(request.getParameter("value"));
					Email mail = HibernateUtil.find(Email.class, " where id = " + id);
					if(mail == null){
						return;
					}
					mail.setIsDelete(2);
					HibernateUtil.save(mail);
				} 
				JunZhuMgr.inst.calcAtt(junzhu);
				out.println("&nbsp;君主id：" + junzhu.id);
				out.println("<br/>");
				ExpTemp expTemp = TempletService.getInstance()
						.getExpTemp(1, junzhu.level);
				out.println("等级：" + junzhu.level + "<br/>");
				int v = 0;
				if (expTemp != null)
					v = expTemp.getNeedExp();
				String input = request.getParameter("v");
				if (input == null)
					input = "1";
				out.println("查看邮件列表：");
				out.println("<a href='?action=reqEmailList'>查询</a><br/>");
				out.println("<a href='?action=getMaxId'>查看最大id值</a>");
				if(mailList == null){
					out.println("<b>请先查询</b><br/>");
				}else if(mailList.size() == 0){
					out.println("<b>没有邮件</b><br/>");
				}else{
					out.append("<table border='1'>");
					out.append("<tr>");
					out.append("<th>mail ID</th><th>标题</th><th>内容</th><th>发送时间</th><th>发件人</th><th>是否已读</th>");
					out.append("</tr>");
					int lastHeroId = 0;
					for(Email mail : mailList){
						out.append("<tr>");
						out.append("<td>");		out.println(mail.getId());		out.append("</td>");
						out.append("<td>");		out.println(mail.getTitle());	out.append("</td>");
						out.append("<td>");		out.println(mail.getContent());		out.append("</td>");
						out.append("<td>");		out.println(mail.sendTime);		out.append("</td>");
						out.append("<td>");		out.println(mail.getSenderName());		out.append("</td>");
						out.append("<td>");		out.println(mail.isReaded);		out.append("</td>");
						out.append("<td>");		out.println("<a href='?action=deleteMail&value=" );
	%><%=mail.getId()%> <% out.append("'>删除</a>");		out.append("</td>");
							out.append("<tr>");
						}
						out.append("</table>");
					}
					
					/*
					 */
				}
			}
		}
	%>
</body>
</html>