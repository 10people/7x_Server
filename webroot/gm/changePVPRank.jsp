<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="com.qx.alliance.AllianceBean"%>
<%@page import="com.qx.ranking.RankingMgr"%>
<%@page import="com.manu.dynasty.template.BaiZhanNpc"%>
<%@page import="com.qx.pvp.PVPConstant"%>
<%@page import="com.manu.dynasty.base.service.CommonService"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="qxmobile.protobuf.PveMessage.UserPve"%>
<%@page import="qxmobile.protobuf.HeroMessage.UserHero"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.qx.pvp.PvpMgr"%>
<%@page import="com.qx.pvp.PvpDuiHuanBean"%>
<%@page import="com.qx.pvp.PvpBean"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="com.qx.account.Account"%>
<%@page import="qxmobile.protobuf.ZhanDou.PvpZhanDouInitReq"%>
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
  <base href="<%=basePath%>">
  <script type="text/javascript">
</script>
    <title>pvp名次修改</title>
  </head>
  
  <body>


<%String rank1 ="";
String rank2 ="";
%>
    <form action="">
   <br/>
   <br/>
      修改名次<input type="text" name="rank1" value="<%=rank1%>">&nbsp;和&nbsp;
        目标名次 <input type="text" name="rank2" value="<%=rank2%>">&nbsp;
 <br/> 
 <br/>    
    <button type="submit">交换</button>
    </form>
<%
rank1 = request.getParameter("rank1");
rank2 = request.getParameter("rank2");
    if(rank1 != null && rank1.length()>0 && rank2 != null && rank2.length()>0){
    	if("".equals(rank1)) {
    		out("请输入");
    		return;
    	}
    	if("".equals(rank2)) {
            out("请输入");
            return;
        }
    	int rank1Int = Integer.parseInt(rank1);
    	int rank2Int = Integer.parseInt(rank2);
    	if(rank2Int >= rank1Int){
    		out("改变的名次必须比当前名次靠前");
    	}else{
    		long junId = 0;
    		Set<String> elem = Redis.getInstance().zrangebyscore_(PvpMgr.KEY, rank1Int-1, rank1Int-1);
    		if(elem == null){
    		   out("elem 是空");
    		 }else{
    		    br();
    		    for (String str : elem) {
    		         if(str!=null){
    		            String[] sss = str.split("_");
    		            if("npc".equals(sss[0])){
    		            	junId = -Long.parseLong(sss[1]);
    		            	out("交换的是npc, id是： " + junId);
                            br();
    		            }else if("jun".equals(sss[0])){
    		            	junId = Long.parseLong(sss[1]);
    		            	out("交换的是君主, id是： " + junId);
                            br();
    		         }
    		    }
    		  }
    		    br();
    		    out("junId:" + junId);
    		    br();
    		 }
    		 long huanId = 0;
    		 boolean npc = true;
    		 Set<String> elem2 = Redis.getInstance().zrangebyscore_(PvpMgr.KEY, rank2Int-1, rank2Int-1);
             if(elem2 == null){
                out("elem2 是空");
                br();
              }else{
                 br();
                 for (String str : elem2) {
                      if(str!=null){
                         String[] sss = str.split("_");
                         if("npc".equals(sss[0])){
                        	 huanId = -Long.parseLong(sss[1]);
                        	npc = true;
                        	out("被交换的是npc, id是： " + huanId);
                        	br();
                         }else if("jun".equals(sss[0])){
                        	 huanId = Long.parseLong(sss[1]);
                             npc = false;
                             out("被交换的是君主, id是： " + huanId);
                             br();
                      }
                 }
               }
              }
             long changeId = junId;
             long beChangeId = huanId;
             int changeRank = rank1Int;
             int beChangeRank= rank2Int;
             out("修改之前： " + changeId +"，的名次是：" +  PvpMgr.inst.getPvpRankById(changeId));br();
             out("修改之前： " + beChangeId +"，的名次是：" +  PvpMgr.inst.getPvpRankById(beChangeId));br();
             synchronized (PvpMgr.pvpRankLock) { 
            	 PvpMgr.inst.changeRankOfRedis(changeId, beChangeId, changeRank, beChangeRank);
             
             }
           out("修改之后： " + changeId +"，的名次是：" +  PvpMgr.inst.getPvpRankById(changeId));br();
           out("修改之后： " + beChangeId +"，的名次是：" +  PvpMgr.inst.getPvpRankById(beChangeId));br();

          
    	}
    }
%>
  </body>
</html>
