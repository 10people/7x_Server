<%@page import="com.qx.email.Email"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="java.util.Date"%>
<%@page import="com.qx.email.EmailMgr"%>
<%@page import="com.manu.dynasty.template.Mail"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>send email to player</title>
	<script type="text/javascript">
		function changeType(typeValue) {
			if(typeValue == 80000) {
				document.getElementById("sendEmail").style.display = "";
			} else {
				document.getElementById("sendEmail").style.display = "none";
			}
		}
	</script>
</head>
<body>
	<%
		String action = request.getParameter("action");
		if (action != null && action.equals("sendEmail")) {
			String receiver = request.getParameter("receiver");//.getBytes("ISO-8859-1"), "utf-8")
			String content = request.getParameter("content");
			String type = request.getParameter("type");
			String senderName = request.getParameter("senderName");
			String fujian = request.getParameter("fujian");
			String param = request.getParameter("param");
			if (receiver == null || receiver.equals("") || 
					content == null || content.equals("") ||
					type == null || type.equals("")) {
				out.println("有内容是空的（附件可以为空）");
				return;
			} else {
				receiver = new String(receiver.getBytes("ISO-8859-1"), "utf-8");
				content = new String(content.getBytes("ISO-8859-1"), "utf-8");
				type = new String(type.getBytes("ISO-8859-1"), "utf-8");
				senderName = new String(senderName.getBytes("ISO-8859-1"), "utf-8");
				
				if(fujian == null) {
					fujian = "";
				}
				fujian = new String(fujian.getBytes("ISO-8859-1"), "utf-8");
				JunZhu junzhu = HibernateUtil.find(JunZhu.class," where name='" + receiver + "'", false);
				if (junzhu == null) {
					out.println("没有君主:"+receiver);
					return;
				}
				
				
				int typeint = Integer.parseInt(type);
				Mail mailConfig = EmailMgr.INSTANCE.getMailConfig(typeint);
				if(mailConfig == null) {
					out.print("mail.xml配置文件找不到type=" + type + "的数据");
					return;
				} 
				String sender = mailConfig.sender;
				if(typeint == 80000) {
					if(senderName == null || senderName.equals("")) {
						out.println("玩家邮件 发件人君主名不能 是空的（附件可以为空）");
						return;
					}
					sender = senderName;
				}
				
				boolean isSuccess = EmailMgr.INSTANCE.sendMail(junzhu.name, content, fujian, sender, mailConfig,
						param);
				if(isSuccess) {
					out.println("<font color='red'>发送成功！</font>");
				}else{
					out.println("<font color='red'>发送失败！</font>");
				}
			}
		}
	%>

	<form name="sendemailForm" action="">
		君主昵称：<input type="text" name="receiver" /> <br /> 
		邮件内容：<textarea rows="4" cols="30" name="content"></textarea><br />
		类型：<input type="text" name="type" onblur="changeType(this.value);" /><br/>
		<span id="sendEmail" style="display:none">发件人姓名：<input type="text" name="senderName" /></span><br/>
		附件：<input type="text" name="fujian"/>附件填写格式（物品type:物品id:数量#物品type:物品id:数量）type指的是读哪个表<br/>
		邮件参数（可以为空）：<input type="text" name="param"/><br/>
		<input type="hidden" name="action" value="sendEmail"/>
		<input type="submit" value="发送" />
		<input type="reset" value="重置" /><br/>
	</form>
</body>
</html>