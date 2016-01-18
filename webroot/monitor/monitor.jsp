<%@page import="com.qx.account.AccountManager"%>
<%@page import="com.qx.world.Scene"%>
<%@page import="java.util.Enumeration"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.qx.world.Player"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="java.util.Iterator"%>
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
连接数量:<%=SessionManager.getInst().sessionMap.size() %><br/>
君主数量:<%=AccountManager.sessionMap.size() %><br/>
<br/>
-<br/>
<table border='1'  style='border-collapse:collapse;'>
<tr>
<th>序号</th><th>userId</th><th>userName</th><th>状态</th><th>坐标</th><th>君主id</th>
</tr>

<%
int cnt = 0;
Enumeration<Integer>  ki = BigSwitch.inst.scMgr.lmCities.keys();
Enumeration<Long>  houseki = BigSwitch.inst.scMgr.houseScenes.keys();
Enumeration<Integer>  fightki = BigSwitch.inst.scMgr.fightScenes.keys();
while(ki.hasMoreElements()){
	Integer lmId = ki.nextElement();
	Scene sc = BigSwitch.inst.scMgr.lmCities.get(lmId);
	Iterator<Integer> it2 = sc.players.keySet().iterator();
	out("<tr><td colspan='5'>"+sc.name+"</td></tr>");
	cnt += sc.players.size();
	int idx = 0;
	while(it2.hasNext()){
		idx++;
		Integer key = it2.next();
		Player p = sc.players.get(key);
		out.append("<tr>");
		String acc = p.getName();
		int uid = key;
		out.append("<td>");		out.append(""+idx);						out.append("</td>");
		out.append("<td>");		out.append(String.valueOf(uid));		out.append("</td>");
		out.append("<td>");		out.append(acc);		out.append("</td>");
		out.append("<td>");		out.append(p.pState.name());		out.append("</td>");
		td(p.getPosX()+","+p.getPosY()+","+p.getPosZ());
		td(p.jzId);
		out.append("<tr>");
	}
}
while(fightki.hasMoreElements()){
	Integer fightId = fightki.nextElement();
	Scene sc = BigSwitch.inst.scMgr.fightScenes.get(fightId);
	Iterator<Integer> it2 = sc.players.keySet().iterator();
	out("<tr><td colspan='5'>"+sc.name+"</td></tr>");
	cnt += sc.players.size();
	int idx = 0;
	while(it2.hasNext()){
		idx++;
		Integer key = it2.next();
		Player p = sc.players.get(key);
		out.append("<tr>");
		String acc = p.getName();
		int uid = key;
		out.append("<td>");		out.append(""+idx);						out.append("</td>");
		out.append("<td>");		out.append(String.valueOf(uid));		out.append("</td>");
		out.append("<td>");		out.append(acc);		out.append("</td>");
		out.append("<td>");		out.append(p.pState.name());		out.append("</td>");
		td(p.getPosX()+","+p.getPosY()+","+p.getPosZ());
		out.append("<tr>");
	}
}

while(houseki.hasMoreElements()){
		Long houseId =houseki.nextElement();
		Scene houseSc = BigSwitch.inst.scMgr.houseScenes.get(houseId);
		Iterator<Integer> itHouse = houseSc.players.keySet().iterator();
		out("<tr><td colspan='5'>"+houseSc.name+"</td></tr>");
		cnt += houseSc.players.size();
		int idhx = 0;
		while(itHouse.hasNext()){
		idhx++;
		Integer key = itHouse.next();
		Player p = houseSc.players.get(key);
		out.append("<tr>");
		String acc = p.getName();
		int uid = key;
		out.append("<td>");		out.append(""+idhx);						out.append("</td>");
		out.append("<td>");		out.append(String.valueOf(uid));		out.append("</td>");
		out.append("<td>");		out.append(acc);		out.append("</td>");
		out.append("<td>");		out.append(p.pState.name());		out.append("</td>");
		td(p.getPosX()+","+p.getPosY()+","+p.getPosZ());
		out.append("<tr>");
	}
}
%>
</table>
非战斗玩家数量:<%=cnt %><br/>
</body>
</html>