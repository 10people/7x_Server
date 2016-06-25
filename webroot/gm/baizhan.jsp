<%@page import="com.manu.dynasty.template.BaiZhan"%>
<%@page import="com.qx.pvp.ZhanDouRecord"%>
<%@page import="com.qx.pvp.PVPConstant"%>
<%@page import="com.manu.dynasty.base.service.CommonService"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="qxmobile.protobuf.HeroMessage.UserHero"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.qx.pvp.PvpMgr"%>
<%@page import="com.qx.pvp.PvpBean"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="java.util.*"%>
<%@include file="/myFuns.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
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
        location.href = 'baizhan.jsp?action='+act+"&v="+v;
   }
</script>
    <title>PVP测试主页</title>
  </head>
  
  <body>
  <%
  String input = request.getParameter("v");
  //int level = 10;
  //br();
  //out.println("百战开启(君主等级需达到)："+level);
 // out.println("<input type='text' id='updateCon' value='"+input
 //         +"'/><input type='button' value='修改' onclick='go(\"updateCon\")'/><br/>");
 // out("<br/>");
  if(input == null){
      input = "0";
  }
  String action = request.getParameter("action");
  //if("updateCon".equals(action)){
    //  int v = Integer.parseInt(request.getParameter("v"));
      //PVPConstant.condition = v;
 // }
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
    	
    	 
    	 if(junzhu == null){
    	    out.println("没有君主");
    	 }else{
    		
    		 %><br> 君主id是：<%=junzhu.id%> <br>君主姓名是：<%=junzhu.name%><%
    		 PvpBean bean = HibernateUtil.find(PvpBean.class, junZhuId);
    		
    		 if(bean==null){
    			 out.println("君主没有百战数据");
    		 }else{
    			 int used = bean.usedTimes;
    			 int remain = bean.remain;
    			 br();
    			 
    			 br();
	    		if("changeZhanCD".equals(action)){
                     int v = Integer.parseInt(request.getParameter("v"));
                     PVPConstant.INTERVAL_ZHAN_SECOND = v;
                 }
	    		 else if("changeAwardLimitTime".equals(action)){
                     int v = Integer.parseInt(request.getParameter("v"));
                     PVPConstant.PRODUCE_LIMIT_TIME = v;
                 }
	    		 else if("changeDuiHuanCd".equals(action)){
                     int v = Integer.parseInt(request.getParameter("v"));
                     PVPConstant.DUI_HUAN_TIME = v;
                 }
	    		 else if("deleteBean".equals(action)){
	    			 try{
	    			 	HibernateUtil.delete(bean);
	    			 	PvpMgr.inst.removePvpRedisData(junZhuId);
	    			 }catch(Exception e){
	    				 e.printStackTrace();
	    			 }
	    			 out("已清除");
	    			 return;
	    		 }
	    		 else if("chavip".equals(action)){
                     int v = Integer.parseInt(request.getParameter("v"));
                     junzhu.vipLevel = v;
                     HibernateUtil.save(junzhu);
                 }else if("changeUsedTimes".equals(action)){
                	 bean.usedTimes = 0;
                	 bean.remain = PVPConstant.ZHAN_TOTAL_TIMES;
                	 used=  bean.usedTimes;
                	 remain  = bean.remain;
                	 HibernateUtil.save(bean);
                	 out("okkkkk");
                 }
	    		 
	    		
	    		br();
	    		%><!-- 
	    		<a href='?action=deleteBean'>清除（修复）此人数据</a><br/>
	    		 -->
	    		<%
	    		 
	    		 out.println("战斗CD(单位是秒)："+ PVPConstant.INTERVAL_ZHAN_SECOND);
                 out.println("<input type='text' id='changeZhanCD' value='"+input
                         +"'/><input type='button' value='修改' onclick='go(\"changeZhanCD\")'/><br/>");
                 out("<br/>");
                 
                 out.println("生产奖励累加时间限制(单位是秒)："+PVPConstant.PRODUCE_LIMIT_TIME);
                 out.println("<input type='text' id='changeAwardLimitTime' value='"+input
                         +"'/><input type='button' value='修改' onclick='go(\"changeAwardLimitTime\")'/><br/>");
                 out("<br/>");
                 out.println("兑换页面刷新CD："+PVPConstant.DUI_HUAN_TIME);
                 out.println("<input type='text' id='changeDuiHuanCd' value='"+input
                         +"'/><input type='button' value='修改' onclick='go(\"changeDuiHuanCd\")'/><br/>");
                 out("<br/>");
                 br();
                 out.println("VIP等级："+junzhu.vipLevel);
                 %>
                 <a href="vip.jsp">修改vip等级</a><br/>
                 <%
                 br();
                 out("排名" + (int)PvpMgr.DB.zrank(PvpMgr.KEY, "jun_"+junzhu.id));
                 %>
                 <a href="changePVPRank.jsp" target='target'>改变君主名次</a><br/>
                  <%  br();br();
                 br();
                 
                 BaiZhan bz = PvpMgr.inst.baiZhanMap.get(bean.junXianLevel);
                 String jxStr = bz == null ? "???" : HeroService.getNameById(bz.name);
                 out("军衔等级:" + bean.junXianLevel+"-"+jxStr);
                 br();br();
       
                 out("当日已经参加的百战次数:" + used +"  ");
                 out("当日剩余百战次数:" + remain + "  ");
                /*  out("<input type='button' value='恢复挑战次数' onclick='go(\"changeUsedTimes\")'/><br/>"); */
                %>
                 <button type="submit" name="action" value ="changeUsedTimes">恢复挑战次数</button>
                 <%
                 out("okkkkk33333333");
                 out("<br/>");
                 br();br();
                 out("上次百战的时间:" + bean.lastDate);
                 br();br();
                 out("历史最高排名记录:" + bean.highestRank);
                 br();br();
                 br();br();
                 out("今日已经购买百战的回数:" + bean.buyCount);
                 br();
                 br();
                 out("今日已购买清除百战CD的次数 :" + bean.cdCount);
                 br();
                 br();
                 br();
                 out("上次发送每日奖励的时间:" + bean.lastAwardTime);
                 br();
                 br();
                 out("/*战斗记录是否被玩家查看： true ：表示被查看，false 表示没有 */");
                 br();
                 out("玩家记录是否被查看:" + bean.isLook);
                 br();
                 br();
                 out("当日刷新对手列表的次数: " + bean.todayRefEnemyTimes);
                 br();
                 br();
                 out("战斗结算获取的威望值: " + bean.lastWeiWang);
                 br();
                 br();
                 br();
                 out("历史总共参加百战的次数: " + bean.allBattleTimes);
                 br();
                 br();
                 out("上次resetPvpBean的时间: " + bean.lastShowTime);
                 br();
	    		 br();
                 br();
                 out("对手名次显示：");
                 tableStart();
                 trS();
                 td(bean.rank1);
                 td(bean.rank2);
                 td(bean.rank3);
                 td(bean.rank4);
                 td(bean.rank5);
                 td(bean.rank6);
                 td(bean.rank7);
                 td(bean.rank8);
                 td(bean.rank9);
                 td(bean.rank10);
                 trE();
                 tableEnd();
                 br();
                 br();
                
                 br();
                 out("=============战斗回放记录===================");
                 br();
                 br();
                 String where2 = "where junzhuId = " + junZhuId + " or enemyId = " + junZhuId;
                 long now = System.currentTimeMillis() /1000;
                 List<ZhanDouRecord> recordList = HibernateUtil.list(ZhanDouRecord.class, where2);
                 if(recordList == null || recordList.size() == 0){
                	 br();
                	 out("没有战斗记录");
                	 br();
                 }else{
                	 tableStart();
                	 trS();td("战斗id");td("君主id");td("敌人id");
                	 td("战斗发生的时间");td("君主result");
                	 td("敌人result");td("君主名次变化值");td("敌人名次变化值");td("获取的威望值");
                	 td("对手损失的建设值");
                	 trE();
                	 for(ZhanDouRecord r: recordList){
                		 trS();
                		 td(r.zhandouId);
                		 td(r.junzhuId);
                		 td(r.enemyId);
                		 td(r.time);
                		 td(r.result1);
                		 td(r.junRankChangeV);
                		 td(r.enemyRankChangeV);
                		 td(r.getWeiWang);
                		 td(r.lostBuild);
                		 trE();
                	 }
                	 tableEnd();
                 }
    		 }
         }
    }
    %> 
  </body>
</html>
