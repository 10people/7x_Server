<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.qx.alliance.AllianceBean"%>
<%@page import="com.qx.ranking.RankingGongJinMgr"%>
<%@page import="com.manu.dynasty.template.BaiZhanNpc"%>
<%@page import="com.qx.pvp.PVPConstant"%>
<%@page import="com.manu.dynasty.base.service.CommonService"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="qxmobile.protobuf.PveMessage.UserPve"%>
<%@page import="qxmobile.protobuf.HeroMessage.UserHero"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.qx.pvp.PvpMgr"%>
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


<%
String rankmin ="";
String rankmax ="";
String deleteId = "";

%>

	
    <form action="">
    <br/>
     (请输出大于等于1的数字)<div style="color: #FF0000">(但是如果想获取倒数x（x>=1）个名次玩家，请分别输入:-x 、 0)</div>
    <br/>
        最低名次 <input type="text" name="rankmin" value="<%=rankmin%>">&nbsp;和&nbsp;
        最高名次 <input type="text" name="rankmax" value="<%=rankmax%>">&nbsp;
        <br/>
      
    <button type="submit" name="action" value ="geren">百战等级名次排行查询</button>
    </form>
        <form action="">
    <br/>
     (请输出大于等于1的数字)
    <br/>
        最低名次 <input type="text" name="rankmin" value="<%=rankmin%>">&nbsp;和&nbsp;
        最高名次 <input type="text" name="rankmax" value="<%=rankmax%>">&nbsp;
        <br/>
      
    <button type="submit" name="action" value ="gerengongjin">个人贡金排行查询</button>
    </form>
       <form action="">
    <br/>
     (请输出大于等于1的数字)
    <br/>
        最低名次 <input type="text" name="rankmin" value="<%=rankmin%>">&nbsp;和&nbsp;
        最高名次 <input type="text" name="rankmax" value="<%=rankmax%>">&nbsp;
        <br/>
      
    <button type="submit" name="action" value ="lianmenggongjin">联盟贡金排行查询</button>
    </form>
    =========================================================================================
    <form action="">
	<b>当redis被清之后，重新生成排行榜用</b>
	<button type="submit" name="action" value ="makenew">重新生成排行榜</button>
	</form>

<% 
rankmin = request.getParameter("rankmin");
rankmax = request.getParameter("rankmax");
deleteId = request.getParameter("playerId");
String action = request.getParameter("action");
    if(rankmin != null && rankmin.length()>0 && rankmax != null && rankmax.length()>0){
        long rmin = Long.parseLong(rankmin);
        long rmax = Long.parseLong(rankmax);
    	// Set<String> elem = DB.zrangebyscore_(KEY, tenRanks[k]-1, tenRanks[k]-1);
    	if(action.equals("geren")){
    		Set<String> elems = null;
    		if(rmin < 0 && rmax<= 0 && rmin <= rmax){
    			 elems =  PvpMgr.DB.zrange(PvpMgr.KEY, (int)rmin, (int)rmax);
    		}else if(rmin > 0 && rmax>0 && rmin <= rmax){
		       elems =  PvpMgr.DB.zrangebyscore_( PvpMgr.KEY, rmin-1, rmax-1);
    		}
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
	               %>
	               	<td>
	               	<form action="">
					<button type="submit" name="action" value="delete">删除</button>
					<input type="hidden" name="playerId" value="<%=playerId%>">
					</form>
					</td>
	               <%
	               trE();
	    	   }
	    	   tableEnd();
	       }
    	}else
    	if(action.equals("gerengongjin")){
    		Map<String, Double> gongJinPermap = RankingGongJinMgr.inst.getPaiHangOfType( rmin-1, rmax-1,
    				RankingGongJinMgr. gongJinPersonalRank);
    		if(gongJinPermap == null){
                out("没有找到");
            }else{
                br();
                out("输入的名次是： " + rmin + " ;" + rmax);
                br();
                br();
                tableStart();
                trS();
                td("id");td("姓名");td("名次");td("贡金");
                trE();
                int ran= 1;
                for(Map.Entry<String, Double> entry: gongJinPermap.entrySet()){
                    String id = entry.getKey();
                    double value = entry.getValue();
                    long needId = Long.parseLong(id == null? "-1" : id);
                    JunZhu junzhu = HibernateUtil.find(JunZhu.class, needId);
                    if(junzhu != null){
                    	trS();
                        td(needId);
                       td(junzhu.name);
                        td(ran ++);
                        td((int)value);
                        trE();
                    }
                }
            }
    	}else
    	if(action.equals("lianmenggongjin")){
            Map<String, Double> gongJinPermap = RankingGongJinMgr.inst.getPaiHangOfType( rmin-1, rmax-1,
                    RankingGongJinMgr.gongJinAllianceRank);
            if(gongJinPermap == null){
                out("没有找到");
            }else{
                br();
                out("输入的名次是： " + rmin + " ;" + rmax);
                br();
                br();
                tableStart();
                trS();
                td("id");td("联盟姓名");td("名次");td("贡金");
                trE();
                int ran= 1;
                for(Map.Entry<String, Double> entry: gongJinPermap.entrySet()){
                    String id = entry.getKey();
                    double value = entry.getValue();
                    long needId = Long.parseLong(id == null? "-1" : id);
                    AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, needId);
                    if(alncBean != null){
                        trS();
                        td(needId);
                       td(alncBean.name);
                        td(ran ++);
                        td((int)value);
                        trE();
                    }
                }
            }
        }
    }
    if(action != null && action.equals("makenew")){
   		PvpMgr.inst.makeRank(TempletService.listAll(BaiZhanNpc.class.getSimpleName()));
   	}
    if(action != null && action.equals("delete")){
    	if(deleteId!=null && deleteId.length()>0){
    		Long jId = Long.parseLong(deleteId);
    		if(jId != null){
    			PvpMgr.inst.removePvpRedisData(jId);
    		}	
    	}
   		
   	}
%>
  </body>
</html>
