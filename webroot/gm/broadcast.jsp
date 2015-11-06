<%@page import="com.qx.util.TableIDCreator"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.world.BroadcastEntry"%>
<%@page import="java.util.List"%>
<%@page import="com.qx.world.BroadcastMgr"%>
<%@page import="xg.push.XG"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<%!
SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
Date getTime(String key, HttpServletRequest request){
	String v = request.getParameter(key);
	if(v == null)return null;
	Date ret = null;
	try{
		ret = fmt.parse(v);
	}catch(Exception e){}
	return ret;
}
%>
<%
String act = request.getParameter("act");
if("send".equals(act)){
	String content = request.getParameter("content");
	if(content != null){
		BroadcastMgr.inst.send(content);
		out("发送成功");
	}
}else if("add".equals(act)){
	BroadcastEntry be = new BroadcastEntry();
	be.id = (int)TableIDCreator.getTableID(BroadcastEntry.class, 1);
	be.startTime = getTime("startTime",request);
	be.endTime = getTime("endTime",request);
	be.intervalMinutes = Integer.parseInt(request.getParameter("intervalMinutes"));
	be.content = request.getParameter("content");
	if(be.startTime == null || be.endTime == null || be.content == null){
		out("参数错误");
	}else{
		HibernateUtil.save(be);
		out("添加成功");
	}
	br();
}else if("openclose".equals(act)){
	String id = getString("id", null);
	if(id != null){
		BroadcastEntry be = HibernateUtil.find(BroadcastEntry.class, "where id="+id);
		if(be != null){
			be.open = !be.open;
			HibernateUtil.save(be);
		}
	}
}else if("delete".equals(act)){
	String id = getString("id", null);
	if(id != null){
		BroadcastEntry be = HibernateUtil.find(BroadcastEntry.class, "where id="+id);
		if(be != null){
			HibernateUtil.delete(be);
		}
	}
}
%>
<form action="" method="post">
<input type="text" name='content'>
<input type="hidden" name="act" value="send">
<input type="submit" value="推送给所有在线玩家">
</form>
添加定时广播:<br/>
时间格式：2015-08-08 22:23:00
<form action="" method="post">
内　　容<input type="text" name='content' value='<%=getString("content", "") %>' width="600px"><br/>
开始时间<input type="text" name='startTime' value='<%=getString("startTime", "") %>'><br/>
结束时间<input type="text" name='endTime' value='<%=getString("endTime", "") %>'><br/>
间隔分钟<input type="text" name='intervalMinutes' value='<%=getString("intervalMinutes", "") %>'><br/>
状态：默认关闭，添加后再手动开启<br/>
<input type="hidden" name="act" value="add">
<input type="submit" value="添加">
</form>
<%
List<BroadcastEntry> confList = HibernateUtil.list(BroadcastEntry.class,"");
tableStart();
ths("ID,内容,开始时间,结束时间,间隔分钟,上次发送时间,状态,操作");
for(BroadcastEntry be : confList){
trS();
td(be.id);td(be.content);td(fmt.format(be.startTime));td(fmt.format(be.endTime));
td(be.intervalMinutes);
td(be.lastSendTime == null ? "" : fmt.format(be.lastSendTime));
String hh11 = "<a href='?act=openclose&id=XXX'>YYY</a>";
hh11 = hh11.replace("XXX", String.valueOf(be.id));
hh11 = hh11.replace("YYY", be.open?"开启":"关闭");
td(hh11);
String hh22 = "<a href='?act=delete&id=XXX'>删除</a>";
hh22 = hh22.replace("XXX", String.valueOf(be.id));
td(hh22);
trE();
}
tableEnd();
%>
</body>
</html>