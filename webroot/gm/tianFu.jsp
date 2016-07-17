<%@page import="com.qx.junzhu.JzKeji"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="qxmobile.protobuf.JunZhuProto.TalentUpLevelReq"%>
<%@page import="com.qx.junzhu.TalentMgr"%>
<%@page import="com.qx.persistent.MC"%>
<%@page import="com.qx.junzhu.TalentPoint"%>
<%@page import="com.qx.junzhu.TalentAttr"%>
<%@page import="com.manu.dynasty.base.service.CommonService"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
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
        location.href = 'tianFu.jsp?action='+act+"&v="+v;
   }
</script>
    <title>天赋</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  </head>
  
  <body>
  <%
  String action = request.getParameter("action");
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
    String input = request.getParameter("v");
    Account account = null;
    if(name != null && name.length()>0){
        account = HibernateUtil.getAccount(name);
    }
    if(account == null){
    %>没有找到<%
    }else{
        session.setAttribute("name", name);
        %><br>注册账号：<%=account.getAccountName()%><br> 账号id：<%=account.getAccountId()%><%
         long junZhuId = account.getAccountId() * 1000 + GameServer.serverId;
         JunZhu junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
         if(junzhu == null){
            out.println("没有君主");
         }else{
             %><br> 君主id是：<%=junzhu.id%> <br>君主姓名是：<%=junzhu.name%><%
            TalentAttr ta = HibernateUtil.find(TalentAttr.class, junzhu.id);
             br();
             br();
             if("addwuyijingqi".equals(action)){
                 int v = Integer.parseInt(request.getParameter("v"));
                 if(ta == null){
                	 ta = new TalentAttr(junzhu.id);
                	 ta.wuYiJingQi = v;
                	 MC.add(ta, junzhu.id);
                     HibernateUtil.insert(ta);
                 }else{
                	ta.wuYiJingQi += v;
                	HibernateUtil.save(ta);
                 }
                 TalentMgr.instance.noticeTalentCanLevUp(junzhu.id);
             }
             if("addjingongdianshu".equals(action)){
                 int v = Integer.parseInt(request.getParameter("v"));
                 if(ta == null){
                     ta = new TalentAttr(junzhu.id);
                     ta.jinGongDianShu = v;
                     MC.add(ta, junzhu.id);
                     HibernateUtil.insert(ta);
                 }else{
                    ta.jinGongDianShu += v;
                    HibernateUtil.save(ta);
                 }
                 TalentMgr.instance.noticeTalentCanLevUp(junzhu.id);
             }
             if("addtipojingqi".equals(action)){
                 int v = Integer.parseInt(request.getParameter("v"));
                 if(ta == null){
                     ta = new TalentAttr(junzhu.id);
                     ta.tiPoJingQi = v;
                     MC.add(ta, junzhu.id);
                     HibernateUtil.insert(ta);
                 }else{
                    ta.tiPoJingQi += v;
                    HibernateUtil.save(ta);
                 }
                 TalentMgr.instance.noticeTalentCanLevUp(junzhu.id);
             }
             if("addfangshoudianshu".equals(action)){
                 int v = Integer.parseInt(request.getParameter("v"));
                 if(ta == null){
                     ta = new TalentAttr(junzhu.id);
                     ta.fangShouDianShu = v;
                     MC.add(ta, junzhu.id);
                     HibernateUtil.insert(ta);
                 }else{
                    ta.fangShouDianShu += v;
                    HibernateUtil.save(ta);
                 }
                 TalentMgr.instance.noticeTalentCanLevUp(junzhu.id);
             } else if("upgradePointId".equals(action)) {
                 int v = Integer.parseInt(request.getParameter("v"));
            	 TalentUpLevelReq.Builder upgradeReq = TalentUpLevelReq.newBuilder();
            	 upgradeReq.setPointId(v);
            	 IoSession ioSession = SessionManager.inst.getIoSession(junzhu.id);
            	 if(ioSession == null) {
            		 ioSession = new RobotSession();
            	 }
            	 TalentMgr.instance.doTalentUpLevel(ioSession, upgradeReq);
             }
            br();
             
             out.println("武艺精气：");
             if(ta == null)out(0);
             else out(ta.wuYiJingQi);
             out.println("&nbsp<input type='text' id='addwuyijingqi' value='"+input
                     +"'/>&nbsp<input type='button' value='增加' onclick='go(\"addwuyijingqi\")'/><br/>");
             br();
             out.println("进攻点数：");
             if(ta == null)out(0);
             else out(ta.jinGongDianShu);
             out.println("&nbsp<input type='text' id='addjingongdianshu' value='"+input
                     +"'/>&nbsp<input type='button' value='增加' onclick='go(\"addjingongdianshu\")'/><br/>");
             br();                 
             out.println("体魄精气：");
             if(ta == null)out(0);
             else out(ta.tiPoJingQi);
             out.println("&nbsp<input type='text' id='addtipojingqi' value='"+input
                     +"'/>&nbsp<input type='button' value='增加' onclick='go(\"addtipojingqi\")'/><br/>");
             br();           
             out.println("防守点数：");
             if(ta == null)out(0);
             else out(ta.fangShouDianShu);
             out.println("&nbsp<input type='text' id='addfangshoudianshu' value='"+input
                     +"'/>&nbsp<input type='button' value='增加' onclick='go(\"addfangshoudianshu\")'/><br/>");
             br();
             br();
             out.println("pointId：&nbsp<input type='text' id='upgradePointId' value='"+input
                     +"'/>&nbsp<input type='button' value='升级' onclick='go(\"upgradePointId\")'/><br/>");
             br();
             out("拥有的天赋列表：");
             br();
             String where = "where junZhuId = " + junzhu.id;
             List<TalentPoint> listT = HibernateUtil.list(TalentPoint.class, where);
             tableStart();
             trS();
             td("pointId ");td("等级");
             trE();
             for(TalentPoint t:listT){
            	 trS();
                 td(t.point);td(t.level);
                 trE();
             }
             tableEnd();
         }
    }
    %> 
  </body>
</html>
