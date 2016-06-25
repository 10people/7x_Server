<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.activity.*"%>
    <%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>

</head>
<body>
	<form>
		<p>
			账号： <input type="text" name="name"/> 或 君主id： <input type="text" name="jzid"/> <button type="submit">查看</button>
		</p>
	</form>
	 <%
  String action = request.getParameter("action");
   String name = request.getParameter("account");
   name = name == null ? "": name.trim();

   if(session.getAttribute("name") != null && name.length()==0){
        name = (String)session.getAttribute("name");
   }
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
            out.println("没有君主");
         }else{
            %><br> 君主id是：<%=junzhu.id%> <br>君主姓名是：<%=junzhu.name%>
          <%}
         }%>
    <br><br>
    <%
    	if("setShouChongStatus1".equals(action)){
    		String scstatus = request.getParameter("scstatus1");
    		if(scstatus != ""){
    			MonthCardMgr.minth_card1_status = Integer.parseInt(scstatus);
        		redirect("MonthCardMgr.jsp");
    		}
    	}
    if("setShouChongStatus2".equals(action)){
		String scstatus = request.getParameter("scstatus2");
		if(scstatus != ""){
			MonthCardMgr.minth_card2_status = Integer.parseInt(scstatus);
    		redirect("MonthCardMgr.jsp");
		}
	}
    %>
     <%
    	tableStart();
    		trS();
    			ths("月卡状态");
    		trE();
    		trS();
    			int status = MonthCardMgr.minth_card1_status;
    			//0没充值 1充值没领取 2已领取
    			if(status == 0){
    				td("没有充值");
    			}else if(status == 1){
    				td("充值没有领取");
    			}else if(status == 2){
    				td("已经领取");
    			}
    		trE();
    	tableEnd();
    	%>
    	 
    <strong>修改月卡状态</strong>
    <form>
		<p>
			<input type="hidden" name="action" value="setShouChongStatus1"/>
			<select name="scstatus1">
				<option value="0">没有充值</option>
				<option value="1">已经充值没有领取</option>
				<option value="2">已经领取</option>
			</select>
			<button type="submit">修改</button>
		</p>
	</form>
    	<%
    	tableStart();
		trS();
			ths("终身卡状态");
		trE();
		trS();
			int status2 = MonthCardMgr.minth_card2_status;
			//0没充值 1充值没领取 2已领取
			if(status2 == 0){
				td("没有充值");
			}else if(status2 == 1){
				td("充值没有领取");
			}else if(status2 == 2){
				td("已经领取");
			}
		trE();
	tableEnd();
    %>
	<strong>修改终身卡状态</strong>
    <form>
		<p>
			<input type="hidden" name="action" value="setShouChongStatus2"/>
			<select name="scstatus2">
				<option value="0">没有充值</option>
				<option value="1">已经充值没有领取</option>
				<option value="2">已经领取</option>
			</select>
			<button type="submit">修改</button>
		</p>
	</form>
</body>
</html>