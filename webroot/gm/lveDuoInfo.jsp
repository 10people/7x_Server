<%@page import="com.qx.pvp.LveDuoBean"%>
<%@page import="com.manu.dynasty.base.service.CommonService"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="qxmobile.protobuf.HeroMessage.UserHero"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@include file="/myFuns.jsp"%>
<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
  <!--这个会导致 href='?xxx=www' 产生的路径不是当前页面。 base href="<%=basePath%>">   -->
  <script type="text/javascript">
   function go(act){
	    var v = document.getElementById(act).value;
        location.href = 'lveDuoInfo.jsp?action='+act+"&v="+v;
   }
</script>
    <title>掠夺测试主页</title>
  </head>
  
  <body>
  <%
   String input = request.getParameter("v");
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
    	%><br>注册账号：<%=account.accountName%><br> 账号id：<%=account.accountId%><%
    	 long junZhuId = account.accountId * 1000 + GameServer.serverId;
    	 JunZhu junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
    	 if(junzhu == null){
    	    out.println("没有君主");
    	 }else{

    		 %><br> 君主id是：<%=junzhu.id%> <br>君主姓名是：<%=junzhu.name%><%
    		 LveDuoBean bean = HibernateUtil.find(LveDuoBean.class, junZhuId);
    		 
    		 if(bean==null){
    			 out.println("君主还没有掠夺或者被掠夺过");
    		 }else{
    			 br();
    			 br();
                 out("当日已经掠夺的次数:" + bean.usedTimes);
                 br();br();
                 out("当日可以掠夺的总次数:" + bean.todayTimes);
                 br();br();
                 out("上次主动掠夺的时间:" + bean.lastBattleTime);
                 br();br();
                 out("上次被动掠夺的结束时间:" + bean.lastBattleEndTime);
                 br();br();
                 br();br();
                 out("今日已经购买掠夺的‘回数’:" + bean.buyBattleHuiShu);
                 br();
                 br();
                 out("今日已购买清除掠夺CD的次数 :" + bean.buyClearCdCount);
                 br();
                 br();
                 out("是否有新的战斗记录:" + bean.lastBattleEndTime);
                 br();
                 br();
                 out("历史总共战斗的次数: " + bean.hisAllBattle);
                 br();
                 br();
                 out("上次resetLveDuoBean的时间: " + bean.lastRestTime);
                 br();
               
    		 }
         }
    }
    %> 
  </body>
</html>
