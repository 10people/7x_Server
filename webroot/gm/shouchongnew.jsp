<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.activity.*"%>
<%@page import="com.qx.vip.PlayerVipInfo" %>
    <%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>

</head>
<body>
	<%
	setOut(out);
	
	String name = request.getParameter("account");
	String accIdStr = request.getParameter("accId");// 用户id
	if(name == null && accIdStr == null){
		name = (String)session.getAttribute("name");
	}
	accIdStr = (accIdStr == null ? "":accIdStr.trim());
	name = name == null ? "": name.trim();
	Account account = null;
	if(name != null && name.length()>0){
		account = HibernateUtil.getAccount(name);
	}else if(accIdStr.length()>0){
		account = HibernateUtil.find(Account.class, (Long.valueOf(accIdStr) - GameServer.serverId) / 1000);
		if(account != null)name = account.accountName;
	}
	JunZhu junzhu = null;
do{
	long junZhuId = 0;
	if(account != null){
		session.setAttribute("name", name);
		out("账号");out(account.accountId);out("：");out(account.accountName);
		out("密码：");out(account.accountPwd);
		junZhuId = account.accountId * 1000 + GameServer.serverId;
	}else if(accIdStr.matches("\\d+")){
		junZhuId = Long.parseLong(accIdStr);
	}else{
		out("没有找到");
		break;
	}
	junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
	if(junzhu == null){
		out.println("没有君主");
		break;
	}
}while(false);
	%>
	<form>
		<p>
			账号： <input type="text" name="account" value="<%=name%>"/> 或 君主id： <input type="text" name="accId" value="<%=accIdStr%>"/> <button type="submit">查看</button>
		</p>
	</form>
    <%
    String action = request.getParameter("action");
    	String scstatus = request.getParameter("scstatus");
    	if("setShouChongStatus".equals(action) && scstatus != ""){
//     		ShouchongMgr.SHOUCHONG_STATUS = Integer.parseInt(scstatus);
    		redirect("shouchongnew.jsp");
    	}
    %>
     <%
    	tableStart();
    		trS();
    			ths(" 首冲状态 ");
    		trE();
    		if(junzhu != null){
    			trS();
    			ShouchongInfo info = HibernateUtil.find(ShouchongInfo.class,"where junzhuId=" + junzhu.id + "");
    			//0没充值 1充值没领取 2已领取
    			if(ShouchongMgr.instance.getShouChongState(info) == 0){
    				td(" 没有充值 ");
    			}else if(ShouchongMgr.instance.getShouChongState(info) == 1){
    				td(" 充值没有领取 ");
    			}else if(ShouchongMgr.instance.getShouChongState(info) == 2){
    				td(" 已经领取 ");
    			}
    			trE();
    		}
    	tableEnd();
    %>
    <br>
    <br>
<!--     <strong>修改首冲状态</strong> -->
<!--     <form> -->
<!-- 		<input type="hidden" name="action" value="setShouChongStatus"/> -->
<!-- 		<select name="scstatus"> -->
<!-- 			<option value="0">没有充值</option> -->
<!-- 			<option value="1">已经充值没有领取</option> -->
<!-- 			<option value="2">已经领取</option> -->
<!-- 		</select> -->
<!-- 		<button type="submit">修改</button> -->
<!-- 	</form> -->
</body>
</html>