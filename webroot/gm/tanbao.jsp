<%@page import="com.qx.explore.TanBaoData"%>
<%@page import="com.qx.account.AccountManager"%>
<%@page import="qxmobile.protobuf.Explore.ExploreReq"%>
<%@page import="com.qx.explore.ExploreConstant"%>
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
                 byte tongbi = TanBaoData.tongBi_type;
                 byte yuanbao = TanBaoData.yuanBao_type;
              
                  out.println("探宝类型：1-铜币单抽, 2-铜币十连抽；3-元宝单抽，4-元宝十连抽");
                  br();
                  out.println("免费铜币单抽CD(单位是秒)："+ TanBaoData.tongBi_CD);
                  out.println("<input type='text' id='changeFCD' value='"+input
                          +"'/><input type='button' value='修改' onclick='go(\"changeFCD\")'/>");
                  out.println("免费元宝单抽CD(单位是秒)："+ TanBaoData.yuanBao_CD);
                  out.println("<input type='text' id='changeSCD' value='"+input
                          +"'/><input type='button' value='修改' onclick='go(\"changeSCD\")'/>");
                  
                  br();
                  int gongxian =0;
	    		 if("lookTanbao".equals(action)){ 
	    			 int v = Integer.parseInt(request.getParameter("v"));
	    			 List<ExploreMine> mineList = ExploreMgr.inst.getMineList(junZhuId);
	    		        Map<Long, ExploreMine> map = new HashMap<Long, ExploreMine>();
	    		        for(ExploreMine e: mineList){
	    		            map.put(e.id % ExploreMgr.space, e);
	    		        }
	    		        /*
	    		         *  铜币探宝
	    		         */
	    		         tableStart();
	                     trS();td("君主id");td("类型");
	                     td("免费抽奖时间");td("已用免费抽奖次数");
	                     td("付费抽奖累计概率");td("历史免费抽奖次数");td("历史付费抽奖次数（10连抽算10次）");
	                     trE();
	    		        int all = TanBaoData.tongBi_all_free_times;
	    		        ExploreMine e = map.get(TanBaoData.tongBi_type);
	    		        if(e != null){
	    		        	ExploreMgr.inst.resetExploreMine(e);
	    		        }else{
	    		        	e = new ExploreMine();
	    		        }
	    		        trS();td(e.id / 100);td("铜币抽奖");
                        td(e.lastFreeGetTime);td(e.usedFreeNumber);
                        td(e.totalProbability);td(e.historyFree);td(e.historyPay);
                        trE();

	    		        /*
	    		         * 元宝探宝
	    		         */
	    		        all = TanBaoData.yuanBao_all_free_times;
	    		        e = map.get(TanBaoData.yuanBao_type);
	    		        if(e != null){
                            ExploreMgr.inst.resetExploreMine(e);
                        }else{
                            e = new ExploreMine();
                        }
                        trS();td(e.id / 100);td("元宝抽奖");
                        td(e.lastFreeGetTime);td(e.usedFreeNumber);
                        td(e.totalProbability);td(e.historyFree);td(e.historyPay);
                        trE();
                        tableEnd();	                	
	    		 }else if("changeFCD".equals(action)){
	    			 int v = Integer.parseInt(request.getParameter("v"));
	    			 TanBaoData.tongBi_CD= v;
	    		 }else if("changeSCD".equals(action)){
	    			 int v = Integer.parseInt(request.getParameter("v"));
	    			 TanBaoData.yuanBao_CD = v;
	    		 }else if("tenChou".equals(action)){
	    			 int v = Integer.parseInt(request.getParameter("v"));
	    			 IoSession ss = AccountManager.getIoSession(junZhuId);
	    			 if(ss == null){
	    				 ss = new RobotSession();
	    				 ss.setAttribute("junzhuId", junZhuId);
	    			 }
	    			 ExploreReq.Builder bb = ExploreReq.newBuilder();
	    			 bb.setType(v);
	    			 ExploreMgr.inst.toExplore(0, ss, bb);
	    		 }
	    		
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
