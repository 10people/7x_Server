<%@page import="java.util.Iterator"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.qx.alliance.AllianceBean"%>
<%@page import="com.qx.huangye.shop.PublicShop"%>
<%@page import="com.qx.alliance.AlliancePlayer"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.qx.huangye.HYTreasure"%>
<%@page import="com.qx.huangye.HYTreasureNpc"%>
<%@page import="com.qx.huangye.HYTreasureDamage"%>
<%@page import="com.qx.huangye.HYTreasureTimes"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="org.hibernate.Hibernate"%>
<%@page import="com.manu.dynasty.template.HuangyePve"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.qx.account.FunctionOpenMgr"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="com.manu.network.SessionUser"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="org.apache.commons.lang.time.DateUtils"%>
<%@page import="com.qx.huangye.HYMgr"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript">
function go(act,id){
	var v = document.getElementById(act).value;
	location.href = '?action='+act+"&v="+v+"&id="+id;
}

</script>
<title>荒野管理</title>
</head>
<body>
<%
	setOut(out);
	String lmIdStr = request.getParameter("lmId");
	if(lmIdStr == null) {
		lmIdStr = "";
	}
	if(session.getAttribute("lmId") != null) {
		lmIdStr = (String)session.getAttribute("lmId");
	}
%>
 <br/>
  <br/>
    <form action="">
    <button type="submit" name="action" value="cancel">取消荒野挑战条件限制</button>
    <%
    String action = request.getParameter("action");
     if(action != null && action.equals("cancel")){
    	 Map<Integer, HuangyePve> dataMap = new HashMap<Integer, HuangyePve>();
    	 List<HuangyePve> listHYPve = TempletService.listAll(HuangyePve.class.getSimpleName());
         for (HuangyePve p : listHYPve) {
        	 HuangyePve p2 = new HuangyePve();
        	 p2.id=         p.id;        
        	 p2.lv=         p.lv;        
        	 p2.nameId=     p.nameId;    
        	 p2.descId=     p.descId;    
        	 p2.icon=       p.icon;      
        	 p2.condition=  p.condition; 
        	 p2.openCost=   p.openCost;  
        	 p2.npcId=      p.npcId;     
        	 p2.award=      p.award;     
        	 p2.rank1Award= p.rank1Award;
        	 p2.rank2Award= p.rank2Award;
        	 p2.rank3Award= p.rank3Award;
        	 p2.rank4Award= p.rank4Award;
        	 p2.rank5Award= p.rank5Award;
        	 p2.fastAward=  p.fastAward; 
        	 p2.killAward=  p.killAward; 
        	 p2.soundId=    p.soundId;   
        	 p2.sceneId=    p.sceneId;   
        	 p2.power=     p.power;      
        	 p2.configId=  p.configId;   
        	 p2.positionX=  p.positionX; 
        	 p2.positionY=  p.positionY; 
        	 p2.pveId=      100101;     
        	 p2.paraK=      p.paraK;     
             dataMap.put(p2.id, p2);
         }
         HYMgr.huangyePveMap = dataMap;
    	 out("成功取消");
     }
     %>
     <br/>
       <br/>
      </form>
          <form action="">
    <button type="submit" name="action" value="uncancel">恢复荒野挑战条件限制</button>
    <%
     if(action != null && action.equals("uncancel")){
    	 Map<Integer, HuangyePve> dataMap = new HashMap<Integer, HuangyePve>();
    	 List<HuangyePve> listHYPve = TempletService.listAll(HuangyePve.class.getSimpleName());
         for (HuangyePve p : listHYPve) {
        	 dataMap.put(p.id, p);
         }
         HYMgr.huangyePveMap = dataMap;//huangyePveMap
         out("成功恢复限制");
     }
     %>
      </form>
       <br/>
        <br/>
         <br/>
    <form action="">
<!--  藏宝点每日可挑战次数  <input type="text" name="cishu" >
    <button type="submit" name="action" value="changeTiaoZhanCiShu">增加</button> -->

	<%
	 if(action != null && action.equals("changeTiaoZhanCiShu")){
         String cishu = request.getParameter("cishu");
         if(cishu!=null && !"".equals(cishu)){
        	 int cishuint = Integer.parseInt(cishu.trim());
             HYMgr.TREASURE_DAY_TIMES += cishuint;
         }  
     }
/* 	 out(HYMgr.TREASURE_DAY_TIMES); */
	 %>
	   </form>
	   <br/>
	      <br/>
	    <form action="">
	    联盟id<input type="text" name="lmId" value="<%=lmIdStr%>">&nbsp;
	            <button type="submit" name="action" value="getlianmengT">查询</button>
	            <br/>
	  </form>
	  <%
	     br();
	if(action != null && action.equals("getlianmengT")&& lmIdStr!= null && !lmIdStr.equals("")) {
		lmIdStr = lmIdStr.trim();
		session.setAttribute("lmId", lmIdStr);
		int lmId = Integer.parseInt(lmIdStr);
		AllianceBean b = HibernateUtil.find(AllianceBean.class, lmId);
		if(b == null){
			out("联盟id错误，没有这个联盟");
			return;
		}
		List<HYTreasure> treasureList = HibernateUtil.list(HYTreasure.class, " where lianMengId="+lmId);
		Map<Integer, HYTreasure> mappp = new HashMap<Integer, HYTreasure>();
        for(HYTreasure t: treasureList){
        	mappp.put(t.idOfFile, t);
        }
		String input = request.getParameter("v");
		if(input == null) {
			input = "";
		}
		out.println("荒野藏宝点信息：");
		br();
		tableStart();
	 	trS();
	 	td("配置文件id");td("激活状态");td("dbId");td("历史通关次数");td("藏宝点本次开启的时间");td("进度");
	 	td("快速通关剩余时间(单位:秒)");td("正在挑战的君主Id");td("本次开始挑战的时间");
	 	trE();
	 	for(HuangyePve hy: HYMgr.huangyePveList){
	 		trS();
            int idOfFile = hy.id;
            td(idOfFile);
            HYTreasure trDB = mappp.get(idOfFile);
            if(trDB != null){
            	Date openTime = trDB.openTime;
                //1-可以被激活（没有激活状态）; 0-不可以被激活（没有激活状态），2-已经激活过了
            	td("已经激活");
            	td(trDB.id);
            	td(trDB.passTimes);
                td(openTime==null?"没有开启":openTime);
                td(openTime==null?"已经通关":trDB.progress);
                td(openTime==null?"无":HYMgr.inst.getKuaiSuPassRemainTime(trDB));
                td(trDB.battleJunzhuId<=0?"无人在挑战": trDB.battleJunzhuId);
                td(trDB.battleJunzhuId<=0?"无":trDB.battleBeginTime);
            }else{
                if(idOfFile == HYMgr.huangYePve_first_pointId){
                    td("没有激活, 但是是第一关，可以被激活");
                    td("-");td("-");td("-");td("-");td("-");td("-");td("-");
                }else{
                    HYTreasure trDBlast = mappp.get(idOfFile -1);
                    // 上一关没有被激活或者没有通关过，下一关不可以被激活
                    if(trDBlast == null || trDBlast.passTimes <= 0){
                       td("没有激活，且不可激活");
                       td("-");td("-");td("-");td("-");td("-");td("-");td("-");
                    }else{
                        // 可以被激活
                    	td("没有激活，但上一关已通关，本关可以被激活");
                    	td("-");td("-");td("-");td("-");td("-");td("-");td("-");
                    }
                }
            }
         trE();
	 }
       tableEnd();
	}
	%> 
<% 
	String dbid = request.getParameter("cangbaodidbid");
	if(session.getAttribute("dbidddd") != null) {
		dbid = (String)session.getAttribute("dbidddd");
	 }
%>
	<br/>
<br/>
<br/>
      <form action="">
   藏宝点 dbid: <input type="text" name="cangbaodidbid" value="<%=dbid%>">&nbsp;
              <button type="submit" name="action" value="getCangBaoDian">查询</button>
              <br/>
    </form>  <br/>
          <br/>
	<%
	if(action != null && action.equals("getCangBaoDian")) {
		if(dbid != null && !"".equals(dbid) && !"null".equals(dbid)){
			 session.setAttribute("dbidddd", dbid);
			  long trid = Long.parseLong(dbid);
			  // 藏宝点伤害信息
              List<HYTreasureDamage> damageList = HibernateUtil.list(HYTreasureDamage.class, 
            		    "where treasureId = " + trid);
              if(damageList == null || damageList.size() == 0) {
                   out("现在还没有该藏宝点伤害信息,还没有被打过");
              }else{
	              tableStart();
	              trS();
	              td("id");td("君主id"); td("君主姓名 ");td("累计伤害总值");td("历史最高伤害值");
	              trE();
	              for(HYTreasureDamage daaa: damageList){
	            	  JunZhu jz = HibernateUtil.find(JunZhu.class, daaa.junzhuId);
	            	  if(jz == null){
	            		  continue;
	            	  }
		              trS();
		              td(daaa.id);td(daaa.junzhuId);td(jz.name);
		              td(daaa.damage);td(daaa.historyMaxDamage);
		              trE();
	              }
	              tableEnd();
              }
              //藏宝点npc信息
              HYTreasure tr = HibernateUtil.find(HYTreasure.class, trid);
              if(tr == null) return;
              List<HYTreasureNpc> npcList = HibernateUtil.list(HYTreasureNpc.class, 
                        "where treasureId = " + trid);
              HuangyePve hyPveCfg = HYMgr.huangyePveMap.get(tr.idOfFile);
              if(npcList == null || npcList.size() == 0) {
            	  npcList = HYMgr.inst.initTreasureNpc(tr, hyPveCfg);
              }else{
                  tableStart();
                  trS();
                  td("id");td("HuangyeNpc.id"); td("在地图中的位置 ");td("剩余血量");td("波次");
                  trE();
                  for(HYTreasureNpc npc: npcList){
                      trS();
                      td(npc.id);td(npc.npcId);td(npc.position);
                      td(npc.remainHp);td(npc.boCi);
                      trE();
                  }
                  tableEnd();
              }
		}
		
	}
	%>
	<%
	String accountN ="";
	request.getParameter("accountName");
String junid ="";
request.getParameter("junzhuID");
if(session.getAttribute("jid") != null) {
	junid = (String)session.getAttribute("jid");
 }
if(session.getAttribute("jac") != null) {
	accountN = (String)session.getAttribute("jac");
 }
accountN =request.getParameter("accountName");
junid =request.getParameter("junzhuID");
%>

<br/>
<br/>
<br/>
      <form action="">
   君主账号：<input type="text" name="accountName" value="<%=accountN%>">&nbsp;（或者填写）
  君主id:<input type="text" name="junzhuID" value="<%=junid%>">&nbsp;
 ( 君主查询荒野信息中可增加荒野挑战次数)
              <button type="submit" name="action" value="getJunzhu">查询</button>
              <br/>
    </form>  <br/>
          <br/>
      <%
      JunZhu jz = null;
      if(session.getAttribute("junzhu2") != null){
    	  jz = (JunZhu)session.getAttribute("junzhu2");
      }
      if(action != null && action.equals("getJunzhu")) {
          if(accountN != null && !"".equals(accountN)){
            session.setAttribute("jac", accountN);
              Account ac  = HibernateUtil.getAccount(accountN);
              if(ac!=null){
                 jz = HibernateUtil.find(JunZhu.class, ac.getAccountId() * 1000 + GameServer.serverId);
                if(jz != null) session.setAttribute("junzhu2", jz);
              }
          }
          if(junid != null && !"".equals(junid) && !"null".equals(junid)){
            session.setAttribute("jid", junid);
              long jd = Long.parseLong(junid);
              jz = HibernateUtil.find(JunZhu.class, jd);
              if(jz != null) session.setAttribute("junzhu2", jz);
          }
      }
      if(jz != null ){
          if(action != null && action.equals("addTime")){
                int v = Integer.parseInt(request.getParameter("v"));
               HYTreasureTimes hytimes = HibernateUtil.find(HYTreasureTimes.class, jz.id);
               if(hytimes != null){
                   hytimes.times += v;
                   HibernateUtil.save(hytimes);
               }
          }
      }

			if(jz != null){
				AlliancePlayer player = HibernateUtil.find(AlliancePlayer.class, jz.id);
				if(player != null && player.lianMengId > 0){
					out("君主的联盟id是 ： " + player.lianMengId);
					br();
					br();
					// 君主挑战数据
	                HYTreasureTimes hytimes = HibernateUtil.find(HYTreasureTimes.class, jz.id);
	                if(hytimes == null){
	                    out("现在没有玩家挑战次数相关数据");
	                }else{
	                    out("玩家挑战次数数据");
	                    HYMgr.inst.resetHYTreasureTimes(hytimes, player.lianMengId);
	                    tableStart();
	                    trS();
	                    td("总挑战次数");td("已经用挑战次数");td("已购买挑战次数");td("重置时间");
	                    trE();
	                    trS();
	                    td("<input type='text' id='addTime' value='"
	                            +hytimes.times+ "'/><input type='button' value='增加' onclick='go(\"addTime\")'/><br/>");
	                    td(hytimes.used);td(hytimes.buyBattleHuiShu);td(hytimes.lastResetTime);
	                    trE();
	                    tableEnd();
	                }
	                br();
	                br();
				}else{
					out("无联盟");
				}
				br();
				br();
		}
%>
	<br/>
	<table>
		<!-- 	<td>
			请求荒野测试：
				<form action="">
				  	联盟Id：<input type="text" name="lianmengId" ><br/>
				  	君主Id：<input type="text" name="junzhuId" ><br/>
				  	<input type="hidden" name="action" value="getHuangyeInfo" ><br/>
				  	<button type="submit">查询</button>
 				 </form>
				<hr/>
			</td> -->
	<!-- 		<td>
			请求荒野奖励库测试：
				<form action="">
				  	联盟Id：<input type="text" name="lianmengId" ><br/>
				  	君主Id：<input type="text" name="junzhuId" ><br/>
				  	<input type="hidden" name="action" value="getRewardStore" ><br/>
				  	<button type="submit">查询</button>
 				 </form>
				<hr/>
			</td>
			<td> -->
<!-- 			插入一条奖励信息：
				<form action="">
				  	联盟Id：<input type="text" name="lianmengId" ><br/>
				  	site：<input type="text" name="site" ><br/>
				  	数量：<input type="text" name="num" ><br/>
				  	<input type="hidden" name="action" value="insertStore" ><br/>
				  	<button type="submit">查询</button>
 				 </form>
				<hr/>
			</td>
			<td> -->
<!-- 			改变联盟奖励库某个site的奖励数量：
				<form action="">
				  	联盟Id：<input type="text" name="lianmengId" ><br/>
				  	site：<input type="text" name="site" ><br/>
				  	数量：<input type="text" name="num" ><br/>
				  	<input type="hidden" name="action" value="changeStoreNum" ><br/>
				  	<button type="submit">查询</button>
 				 </form>
				<hr/>
			</td> -->
	</table>
  	
  </body>
</html>