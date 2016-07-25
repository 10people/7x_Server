<%@page import="java.util.Date"%>
<%@page import="com.qx.explore.treasure.BXRecord"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
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
<script type="text/javascript">
function go(act){
	var v = "";
	var ele = document.getElementById(act);
	if(ele) v=ele.value;
	location.href = '?action='+act+"&v="+v;
}
</script>
<title>监控</title>
</head>
<body>
请求队列：<%=com.qx.explore.treasure.ExploreTreasureMgr.inst.scene.missions.size() %><br/>
人数：<%=com.qx.explore.treasure.ExploreTreasureMgr.inst.scene.players.size() %><br/>
<%String act = request.getParameter("act");
String action = request.getParameter("action");
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
	EventMgr.addEvent(1003L,ED.tanbao_tenTimes, new Object[]{1003L, 1});
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
非战斗玩家数量:<%=cnt %><br/><br/><br/>


<%
	out.append("<hr/>");
	setOut(out);
	String name = request.getParameter("account");
	String accIdStr = request.getParameter("accId");// 用户id
	if(name == null && accIdStr == null){
		name = (String)session.getAttribute("name");
	}
	accIdStr = (accIdStr == null ? "":accIdStr.trim());
	name = name == null ? "": name.trim();
%>
  	<form action="">
	  	账号<input type="text" name="account" value="<%=name%>">&nbsp;或&nbsp;
	  	君主ID<input type="text" name="accId" value="<%=accIdStr%>">
	  	<button type="submit">查询</button>
	</form>
<%
	Account account = null;
	if(name != null && name.length()>0){
		account = HibernateUtil.getAccount(name);
	}else if(accIdStr.length()>0){
		account = HibernateUtil.find(Account.class, (Long.valueOf(accIdStr) - GameServer.serverId) / 1000);
		if(account != null)name = account.accountName;
	}
	do{
		
		long junZhuId = 0;
		if (account != null) {
			session.setAttribute("name", name);
			out("账号");
			out(account.accountId);
			out("：");
			out(account.accountName);
			out(" - 密码：");
			out(account.accountPwd);
			junZhuId = account.accountId * 1000 + GameServer.serverId;
		} else if (accIdStr.matches("\\d+")) {
			junZhuId = Long.parseLong(accIdStr);
		} else {
			out("没有找到");
			break;
		}
		JunZhu junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
		if (junzhu == null) {
			out.println("没有君主");
			break;
		}
		session.setAttribute("jzId", junZhuId);
		BXRecord bean = HibernateUtil.find(BXRecord.class, junzhu.id);
		if(bean == null){
			out.println("还没有进行过十连抽");
		}else{//检查重置
		 	if("resetTimes".equals(action)) {
				bean.resetTime = new Date();
				bean.bxCnt = 0;
				bean.yuanBao = 0;
				HibernateUtil.update(bean);
			}
		}
		
		out.append("<br/><br/>");
		out.append("<input type='button' id='restTimes' value='重置今日十连副本' onclick='go(\"resetTimes\")'/><br/>");
		out.append("<b>会重置今日十连副本的使用次数和获得元宝数量<b/><br/>");
		tableStart();
		trS();
	 	td("今天使用次数");td(bean.bxCnt);
	 	trE();
	 	trS();
	 	td("今天获得元宝");td(bean.yuanBao); 
	 	trE();
	 	tableEnd();
	} while(false);
%>

</body>
</html>