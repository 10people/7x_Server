<%@page import="com.qx.account.AccountManager"%>
<%@page import="qxmobile.protobuf.Explore.ExploreReq"%>
<%@page import="com.qx.explore.ExploreConstant"%>
<%@page import="qxmobile.protobuf.Explore.ExploreAwardsInfo"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.explore.ExploreMgr"%>
<%@page import="com.qx.explore.ExploreMine"%>
<%@page import="com.qx.alliance.AlliancePlayer"%>
<%@include file="/myFuns.jsp"%>
<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
out("basePath is " + basePath);
pageContext.setAttribute("basePath",basePath); 
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html> 
  <head>
  <base href="<%=basePath%>">
  <script type="text/javascript">
   function go(act){
	    var biaoqian = document.getElementById(act);
	    var v = 0;
	    if(biaoqian != null){
	    	 v = biaoqian.value;
	    }
     //   location.href = '${pageScope.basePath}gm/tanbao.jsp?action='+act+"&v="+v;
	    location.href = 'gm/tanbao.jsp?action='+act+"&v="+v;
   }
   </script>
    <title>探宝信息显示</title>
  </head>
  
  <body>
  <%
   String name = request.getParameter("account");
   name = name == null ? "": name.trim();

   if(session.getAttribute("name") != null && name.length()==0){
	    name = (String)session.getAttribute("name");
   }
   %>
    <form action="">
        账号<input type="text" name="account" value="<%=name%>">&nbsp;或&nbsp;
    <button type="submit">查询</button>
    </form>
<%
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
    	
    	 String action = request.getParameter("action");
    	 if(junzhu == null){
    	    out.println("没有君主");
    	 }else{	
    		     %><br> 君主id是：<%=junzhu.id%> <br>君主姓名是：<%=junzhu.name%><%
    		    br();
    		     String input = request.getParameter("v");
                 if(input == null){
                     input = "0";
                 }
                 byte FREE = ExploreConstant.FREE;
                 byte SIGLE = ExploreConstant.SIGLE;
                 byte PAY = ExploreConstant.PAY;
                 byte GUILD_1 = ExploreConstant.GUILD_1;
                 byte GUILD_2 = ExploreConstant.GUILD_2;
                  out.println("探宝类型有5种--->  "+ " 免费单抽： " + FREE+" , "+" 付费单抽： " + SIGLE+" , "
                          +" 付费10连抽： " + PAY+" , "
                          +" 付费联盟单抽：" + GUILD_1+" , "
                          +" 付费联盟10连抽：" + GUILD_2);
                  br();
                  br();
                  out.println("<input type='text' id='lookTanbao' value='"+input
                          +"'/><input type='button' value='查看' onclick='go(\"lookTanbao\")'/>");
                  out("(请填写以上存在的探宝类型)");
                  out("<br/>");
                  br();
                  out.println("免费单抽CD(单位是秒)："+ ExploreConstant.FREE_TIME_INTERVAL);
                  out.println("<input type='text' id='changeFCD' value='"+input
                           +"'/><input type='button' value='修改' onclick='go(\"changeFCD\")'/>");
                  br();
                  int gongxian =0;
                  AlliancePlayer guild = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
                  if(guild != null ){
                      gongxian = guild.gongXian;
                 }
	    		 if("lookTanbao".equals(action)){ 
	    			 int v = Integer.parseInt(request.getParameter("v"));
	    			 List<ExploreMine> list = ExploreMgr.inst.getMineList(junZhuId);
	    			 ExploreMine mine = ExploreMgr.inst.getMineFromList(list, v);
	    			 out("您要查看的探宝类型是： " + v);
	    			 br();
	                 if(mine != null){
	                	 br();
	                	 out("探宝类型是:  "  + mine.getType());
	                	 br();
	                	 out("对于这个类型今日已经免费领取次数： " + mine.getTimes());
	                	 br();
                         out("对于这个类型上次免费领取时间： " + mine.getLastGetTime());
                         br();
                         out("对于这个类型历史总共抽取次数： " + mine.getAllTimes());
	                 }else if(v == GUILD_1 || v == GUILD_2){
	                	 br();
	                	 out("您没有联盟，所以还没有这个类型的探宝数据");
	                 }else{
	                	 br();
	                	 out("现在没有数据,提示：首先进入探宝界面才会生成探宝数据");
	                }
	    		 }else if("changeFCD".equals(action)){
	    			 int v = Integer.parseInt(request.getParameter("v"));
	    			 ExploreConstant.FREE_TIME_INTERVAL= v;
	    		 }else if("changeSCD".equals(action)){
	    			 int v = Integer.parseInt(request.getParameter("v"));
	    			 ExploreConstant.SIGLE_TIME_INTERVAL = v;
	    		 }else if("addGongxian".equals(action)){
	    			 int v = Integer.parseInt(request.getParameter("v"));
	    			 if(guild != null ){
		    			 guild.gongXian += v;
		    	         HibernateUtil.save(guild);
	    			 }else{
	    				 br();
                         out("没有联盟所以没有贡献值");
	    			 }
	    		 }else if("tenChou".equals(action)){
	    			 int v = Integer.parseInt(request.getParameter("v"));
	    			 IoSession ss = AccountManager.getIoSession(junZhuId);
	    			 if(ss == null){
	    				 ss = new RobotSession();
	    				 ss.setAttribute("junzhuId", junZhuId);
	    			 }
	    			 ExploreReq.Builder bb = ExploreReq.newBuilder();
	    			 bb.setType(v);
	    			 bb.setIsBuy(true);
	    			 if((byte)v == ExploreConstant.FREE){
	    				 bb.setIsBuy(false);
	    			 }
	    			 ExploreMgr.inst.toExplore(0, ss, bb);
	    		 }
	    		
                 br();
                 out.println("添加贡献值："+ gongxian);
                 out.println("<input type='text' id='addGongxian' value='"+input
                          +"'/><input type='button' value='修改' onclick='go(\"addGongxian\")'/>");
                 br();
                 br();
                 out("点击进行某种抽奖操作：(请填写以上存在的探宝类型)");
                 br();
                 out("(在线情况可在背包中查看所得奖励),元宝或者联盟的贡献值不够会导致抽奖失败");
                 br();
                 out.println("<input type='text' id='tenChou' value='"+input
                         +"'/><input type='button' value='一次某类型抽奖' onclick='go(\"tenChou\")'/>");
                
                 br();
    		 }
    }
    %>
  </body>
</html>
