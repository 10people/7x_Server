<%@page import="com.qx.explore.TanBaoData"%>
<%@page import="com.qx.account.AccountManager"%>
<%@page import="qxmobile.protobuf.Explore.ExploreReq"%>
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
              
                
                  out.println("免费铜币单抽CD(单位是秒)："+ TanBaoData.tongBi_CD);
                  out.println("<input type='text' id='changeFCD' value='"+input
                          +"'/><input type='button' value='修改' onclick='go(\"changeFCD\")'/>");
                  br();
                  out.println("免费元宝单抽CD(单位是秒)："+ TanBaoData.yuanBao_CD);
                  out.println("<input type='text' id='changeSCD' value='"+input
                          +"'/><input type='button' value='修改' onclick='go(\"changeSCD\")'/>");
                  
                  br();
	    		 if("lookTanbao".equals(action)){ 
	    			              	
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
	    			 int i =0;
	    					 ExploreMgr.inst.toExplore(0, ss, bb);
	    			 if(i == 0){
	    				 %><script>alert("探宝成功");</script><%;
	    			 }else if(i == -1){%><script>alert("君主没有登录游戏");</script><%}
	    			 else if(i == -2){%><script>alert("探宝类型不存在");</script><%}
	    			 else if(i == -3){%><script>alert("铜币单抽失败，铜币不足");</script><%}
	    			 else if(i == -4){%><script>alert("铜币10抽失败，铜币不足");</script><%}
	    			 else if(i == -5){%><script>alert("元宝单抽失败，元宝不足");</script><%}
	    			 else if(i == -6){%><script>alert("元宝十连抽失败，元宝不足");</script><%}
	    		 }
	    		 /* int v = Integer.parseInt(request.getParameter("v"));
                 List<ExploreMine> mineList = ExploreMgr.inst.getMineList(junZhuId);
                    Map<Long, ExploreMine> map = new HashMap<Long, ExploreMine>();
                    for(ExploreMine e: mineList){
                        map.put(e.id % ExploreMgr.space, e);
                    }
                    */
                    /*
                     *  铜币探宝
                     
                     */
                     junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
                     out("探宝记录");
                    br();
                     tableStart();
                     trS();td("君主id");td("类型");td("单抽花费");td("十抽花费");td("玩家货币");
                     td("上次免费抽奖时间");td("已用免费抽奖次数");
                     td("付费抽奖累计概率");td("（元宝免费、铜币总）抽奖次数");td("元宝付费抽奖次数");td("元宝保底抽次数（10连抽算10次）");
                     trE();
                    int all = TanBaoData.tongBi_all_free_times;
                    ExploreMine e = ExploreMgr.inst.getMineByType(junZhuId, TanBaoData.tongBi_type);
                    if(e != null){
                        ExploreMgr.inst.resetExploreMine(e);
                    }else{
                        e = ExploreMgr.inst.intMineForType(junZhuId, TanBaoData.tongBi_type);
                    }
                    trS();td(e.id / ExploreMgr.space);td("铜币抽奖");td( ExploreMgr.inst.getCost(1));
                    td(ExploreMgr.inst.getCost(2));td("铜币" + junzhu.tongBi);
                    td(e.lastFreeGetTime);td(e.usedFreeNumber);
                    td(e.totalProbability);td(e.historyFree);td("XXX");td("XXX");
                    trE();

                    /*
                     * 元宝探宝
                     */
                    all = 0;//TanBaoData.yuanBao_all_free_times;
                    e = ExploreMgr.inst.getMineByType(junZhuId, TanBaoData.yuanBao_type);
                    if(e != null){
                        ExploreMgr.inst.resetExploreMine(e);
                    }else{
                        e = ExploreMgr.inst.intMineForType(junZhuId, TanBaoData.yuanBao_type);
                    }
                    trS();td(e.id / ExploreMgr.space);td("元宝抽奖");
                    td( ExploreMgr.inst.getCost(3));
                    td(ExploreMgr.inst.getCost(4));td("元宝："+junzhu.yuanBao);
                    td(e.lastFreeGetTime);td(e.usedFreeNumber);
                    td(e.totalProbability);td(e.historyFree);td(e.historyPay);td(e.historyBaoDi);
                    trE();
                    tableEnd();   
	    		 br();
	    		  out.println("探宝类型：1-铜币单抽, 2-铜币十连抽；3-元宝单抽，4-元宝十连抽");
                  br();
                 br();
                 out("点击进行某种抽奖操作：(请填写以上存在的探宝类型)");
                 br();
                 br();
                 input = "1";
                 out.println("<input type='text' id='tenChou' value='"+input
                         +"'/><input type='button' value='一次某类型抽奖' onclick='go(\"tenChou\")'/>");
                
                 br();
    		 }
    }
    %>
  </body>
</html>
