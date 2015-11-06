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

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
  <script type="text/javascript">
   function go(act){
	    var v = document.getElementById(act).value;
        location.href = 'gm/baizhan.jsp?action='+act+"&v="+v;
   }
</script>
    <title>获取pvp前N名次玩家</title>
  </head>
  
  <body>


<%String rankmin ="";
String rankmax ="";
%>
    <form action="">
    <br/>
     (请输出大于等于1的数字)
    <br/>
        最低名次 <input type="text" name="rankmin" value="<%=rankmin%>">&nbsp;和&nbsp;
        最高名次 <input type="text" name="rankmax" value="<%=rankmax%>">&nbsp;
        <br/>
      
    <button type="submit">查询</button>
    </form>
    

<% 
rankmin = request.getParameter("rankmin");
rankmax = request.getParameter("rankmax");
    if(rankmin != null && rankmin.length()>0 && rankmax != null && rankmax.length()>0){
        long rmin = Long.parseLong(rankmin);
        long rmax = Long.parseLong(rankmax);
    	// Set<String> elem = DB.zrangebyscore_(KEY, tenRanks[k]-1, tenRanks[k]-1);
       Set<String> elems =  PvpMgr.inst.DB.zrangebyscore_( PvpMgr.inst.KEY, rmin-1, rmax-1);
       if(elems == null){
    	   out("没有找到");
       }else{
    	   br();
    	   out("输入的名次是： " + rmin + " ;" + rmax);
    	   br();
    	   br();
    	   tableStart();
    	   trS();
           td("id");td("姓名");td("名次");td("国家");td("roleId");
           trE();
    	   for(String s: elems){
    		   String[] sss = s.split("_");
               long playerId = Long.parseLong(sss[1]);
               String name = "";
               int guojiaId = 0;
               long rankkk  = 0;
               int roldId = 0;
               if("npc".equals(sss[0])){
                   // NPC
                   BaiZhanNpc npc = PvpMgr.inst.npcs.get((int)playerId);
                   String nameInt = npc.name;
                   name  = HeroService.heroNameMap.get(nameInt).Name;
                   guojiaId = npc.getGuoJiaId((int)playerId);
                   rankkk = (int)PvpMgr.inst.getPvpRankById(-playerId);
                   roldId = npc.getRoleId((int)playerId);
               }else{
            	   JunZhu junzhu = HibernateUtil.find(JunZhu.class, playerId);
            	   name = junzhu.name;
            	   guojiaId = junzhu.guoJiaId;
            	   rankkk = (int)PvpMgr.inst.getPvpRankById(playerId);
            	   roldId = junzhu.roleId;
               }
               trS();
               td((int)playerId);td(name);td(rankkk);td(guojiaId);td(roldId);
               trE();
    	   }
    	   tableEnd();
       }
    }
%>
  </body>
</html>
