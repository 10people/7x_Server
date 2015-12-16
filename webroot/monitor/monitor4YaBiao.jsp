<%@page import="com.qx.yabiao.YBRobotMgr"%>
<%@page import="com.qx.yabiao.YaBiaoHuoDongMgr"%>
<%@page import="com.qx.world.Scene"%>
<%@page import="java.util.Enumeration"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.qx.world.Player"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Set"%>
<%@page import="com.qx.yabiao.YaBiaoRobot"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>监控</title>
</head>
<body>
<%
	String  act= request.getParameter("act");
if("switchOpen".equals(act)){
	BigSwitch.inst.ybMgr.openFlag = !BigSwitch.inst.ybMgr.openFlag;
}
String moreProfit=request.getParameter("moreProfit");
moreProfit=moreProfit==null?"100":moreProfit;
	int canshu=Integer.parseInt(moreProfit);
	BigSwitch.inst.ybMgr.setMoreProfitState(canshu);

%>

  	<form action="">
  	 	收益倍率<input type='text' name='moreProfit' id='moreProfit' value='<%=moreProfit%>'/>
	  	<button type="submit">修改</button>
	</form>
当前押镖状态为：<%=BigSwitch.inst.ybMgr.openFlag ? "开启" : "关闭"%>
，设置为<a href='?act=switchOpen'><%=BigSwitch.inst.ybMgr.openFlag ? "关闭" : "开启"%></a>
<br/>
押镖人数:<%=BigSwitch.inst.ybMgr.ybJzId2ScIdMap.size()%><br/>
<%-- 劫镖人数:<%=BigSwitch.inst.ybMgr.jbJz2ScIdMap.size()%> 现在的代码jbJz2ScIdMap废弃 无用，无法快捷的统计劫镖人数 --%>
<br/>
<br/>
	后台输出：
			<%
	String isShowLog = request.getParameter("isShowLog");
	if(isShowLog!=null){
		YBRobotMgr.isShowLog = Boolean.valueOf(isShowLog);
	}
	if(Boolean.valueOf(YBRobotMgr.isShowLog)){
%>
				<a>开启</a>|<a href="monitor4YaBiao.jsp?isShowLog=false">关闭</a>
				<%
					} else{
				%>
				<a href="monitor4YaBiao.jsp?isShowLog=true">开启</a>|<a>关闭</a>
				<%
					}
				%>
<br/>
<table border='1'  style='border-collapse:collapse;'>
<tr>
<th>序号</th><th>userId</th><th>userName</th>
</tr>

<%
	int cnt = 0;
Enumeration<Integer>  ybkey = BigSwitch.inst.ybMgr.yabiaoScenes.keys();
out("<tr><td colspan='5'>"+"押镖列表</td></tr>");
while(ybkey.hasMoreElements()){
	Integer ybScId =ybkey.nextElement();
	Scene sc = BigSwitch.inst.ybMgr.yabiaoScenes.get(ybScId);
	Iterator<Integer> it2 = sc.players.keySet().iterator();
	Set<Long> ybSet=BigSwitch.inst.ybMgr.ybJzList2ScIdMap.get(ybScId);
	 out.print("SceneID--"+ybScId+"押镖set--"+ybSet+"<br>");
	if(ybSet!=null){
		Iterator<Long> it = ybSet.iterator();
		while (it.hasNext()) {
		    Long str = it.next();
			YaBiaoRobot ybrobot=(YaBiaoRobot)BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(str);
		    out.print(str+"-"+ybrobot==null?"无":ybrobot.isBattle+";");
		}
	}
	
	out("<tr><td colspan='5'>"+sc.name+"</td></tr>");
	cnt += sc.players.size();
	int idx = 0;
	while(it2.hasNext()){
		idx++;
		Integer key = it2.next();
		Player p = sc.players.get(key);
		if(p.roleId==Scene.YBRobot_RoleId){
			out.append("<tr>");
			String acc = p.getName();
			int uid = key;
			out.append("<td>");
			out.append(""+idx);
			out.append("</td>");
			out.append("<td>");
			out.append(String.valueOf(uid));
			out.append("</td>");
			out.append("<td>");
			out.append(acc);
			out.append("</td>");
			out.append("<td>");
			out.append("x坐标--"+p.getPosX()+"y坐标--"+p.getPosY()+"z坐标--"+p.getPosZ());
			out.append("</td>");
			out.append("<tr>");
		}
	}
}
Enumeration<Integer>  ybkey1 = BigSwitch.inst.ybMgr.yabiaoScenes.keys();
out("<tr><td colspan='5'>"+"劫镖列表</td></tr>");
while(ybkey1.hasMoreElements()){
	Integer ybScId =ybkey1.nextElement();
	Scene sc = BigSwitch.inst.ybMgr.yabiaoScenes.get(ybScId);
	Iterator<Integer> it2 = sc.players.keySet().iterator();
// 	Set<Long> jbSet=BigSwitch.inst.ybMgr.jbJzList2ScIdMap.get(ybScId);
// 	if(jbSet!=null){
// 			Iterator<Long> it22 = jbSet.iterator();
// 			while (it22.hasNext()) {
// 				Long str = it22.next();
// 				out.print(str + ";");
// 			}
// 		}
		out("<tr><td colspan='5'>" + sc.name + "</td></tr>");
		int idx = 0;
		while (it2.hasNext()) {
			idx++;
			Integer key = it2.next();
			Player p = sc.players.get(key);
			if (p.roleId != Scene.YBRobot_RoleId) {
				out.append("<tr>");
				String acc = p.getName();
				int uid = key;
				out.append("<td>");
				out.append("" + idx);
				out.append("</td>");
				out.append("<td>");
				out.append(String.valueOf(uid));
				out.append("</td>");
				out.append("<td>");
				out.append(acc);
				out.append("</td>");
				out.append("<td>");
				out.append("x坐标--"+p.getPosX()+"y坐标--"+p.getPosY()+"z坐标--"+p.getPosZ());
				out.append("</td>");
				out.append("</td>");
				
				out.append("<tr>");
			}
		}
	}
%>
</table>
押镖场景在线玩家数量:<%=cnt %><br/>
</body>
</html>