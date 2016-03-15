<%@page import="qxmobile.protobuf.ErrorMessageProtos.ErrorMessage"%>
<%@page import="com.manu.network.msg.ProtobufMsg"%>
<%@page import="com.qx.event.ED"%>
<%@page import="com.qx.event.EventMgr"%>
<%@page import="com.qx.explore.treasure.BaoXiangBean"%>
<%@page import="com.qx.explore.treasure.ExploreTreasureMgr"%>
<%@page import="com.qx.world.SceneMgr"%>
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
<%String act = request.getParameter("act");
if("modSizePerSc".equals(act)){
	String size = request.getParameter("sizePerSc");
	SceneMgr.sizePerSc = Integer.parseInt(size);
}
if(request.getParameter("clearQueue")!=null){
	ExploreTreasureMgr.inst.queue.clear();
	ExploreTreasureMgr.inst.aliveBaoXiangCnt.set(0);
}
if(request.getParameter("addBaoXiang")!=null){
	BaoXiangBean bean = new BaoXiangBean();
	bean.id = 0;
	bean.amount=0;
	ExploreTreasureMgr.inst.genBaoXiang(bean);
}
if(request.getParameter("debugOnOff")!=null){
	ExploreTreasureMgr.inst.debug = !ExploreTreasureMgr.inst.debug;
}
if(request.getParameter("fireEvent")!=null){
	EventMgr.addEvent(ED.tanbao_tenTimes, new Object[]{1003L, 1});
}
%>
<!-- 
<form action='' method="get">
<input type="hidden" name="act" value="addBaoXiang">
每层人数限制:<input  type='number' name='sizePerSc' value='<%=SceneMgr.sizePerSc %>'/>
<button type='submit' >修改</button>
</form>
<br/>
 -->
队列数量:<%=ExploreTreasureMgr.inst.queue.size() %>
<a href='<%=request.getRequestURI()%>'>刷新</a>
<br/>
存活宝箱数量<%=ExploreTreasureMgr.inst.aliveBaoXiangCnt.get()%>
,delay:<%=ExploreTreasureMgr.inst.delay.get() %>
<br/>
<%ProtobufMsg msg = ExploreTreasureMgr.inst.lastInfo;
if(msg != null){
	ErrorMessage.Builder info = (ErrorMessage.Builder)msg.builder	;
	out("下个宝箱信息:");
	out(info.getErrorCode());
	out("/");
	out(info.getCmd());
	out(",");
	out(info.getErrorDesc());
	out("秒");
}
	%>
<br/>
<a href='?clearOffLine=1'>清理离线</a>
<a href='?fireEvent=1'>触发十连</a>
<a href='?addBaoXiang=1'>增加宝箱</a>
<a href='?clearQueue=1'>清空队列</a>
<a href='?debugOnOff=1'><%=ExploreTreasureMgr.inst.debug?"关闭调试" : "开启调试" %></a>
<br/>
开启调试后，触发十连后5秒就开始刷第一波宝箱
<br/>
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
boolean clearOffLine = request.getParameter("clearOffLine") != null;
while(true){
	Scene sc = ExploreTreasureMgr.inst.scene;
	Iterator<Integer> it2 = sc.players.keySet().iterator();
	out("<tr><td colspan='6'>"+sc.name+"</td></tr>");
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
		IoSession ss = AccountManager.sessionMap.get(p.jzId);
		String online = ss != null && ss.isConnected() ? "" : "-离线";
		if(clearOffLine && online.length()>0){
			it2.remove();
			online += "-removed";
		}
		td(p.jzId+online);
		out.append("<tr>");
	}
	break;
}

%>
</table>
非战斗玩家数量:<%=cnt %><br/>
</body>
</html>