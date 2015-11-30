﻿﻿﻿﻿﻿﻿<%@page import="com.manu.dynasty.template.HuoDong"%>
<%@page import="java.util.Map"%>
<%@page import="com.qx.activity.ActivityMgr"%>
<%@page import="com.qx.activity.ShouchongMgr"%>
<%@page import="com.qx.activity.XianShiActivityMgr"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="qxmobile.protobuf.Activity.ActivityInfo"%>
<%@page import="com.manu.dynasty.template.XianshiControl"%>
<%@page import="com.manu.dynasty.template.QiriQiandaoControl"%>
<%@page import="java.util.List"%>
<%@page import="qxmobile.protobuf.Activity.GetActivityListResp"%>
<%@page import="com.manu.network.msg.ProtobufMsg"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.qx.yabiao.YaBiaoHuoDongMgr"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="org.apache.mina.core.session.IoSession"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src="../ajax.min.js"></script>
<script>
function get(url, call){
	var xhr = createXHR();
	xhr.onreadystatechange = function()
	{
	    if (xhr.readyState === 4)
	    {
	        //document.getElementById('preview').innerHTML = xhr.responseText;
	        call(xhr);
	    }
	};
	xhr.open('GET', url, true);
	xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
	xhr.send();
}
function changeActivityName(id,obj){
	var	Name=obj.parentElement.parentElement.cells[1].childNodes[0].value;
	var url = 'activity.jsp?huodongId=' +id+'&Name='+Name+'&action=changActivityName';
	get(url);
}
</script>
</head>
<body>
	<%
		String key = request.getParameter("key");
		String xsKey = request.getParameter("xsKey");
		String state = request.getParameter("state");
		String xianshistate = request.getParameter("xianshistate");
		key = (null==key?"":key);
		xsKey = (xsKey==null?"":xsKey);
		state = (null==state?"0":state);
		xianshistate = (xianshistate==null?"10":xianshistate);
		String action = request.getParameter("action");
		if("changActivityName".equals(action)) {
			String huodongId = request.getParameter("huodongId");
			if (huodongId != null) {
				String Name = request.getParameter("Name");
				if (Name != null) {
					XianshiControl xsControl = XianShiActivityMgr.xsControlMap.get(Integer.parseInt(huodongId));
					if(xsControl!=null){
// 						Name= new String(Name.toString().getBytes("iso8859-1"), "utf-8");
						xsControl.setName(Name);
					}
				}

			}
		}
	%>
	<table border="1">
	<tr>
		<th>id</th>
		<th>活动名称</th>
		<th>活动描述</th>
		<th>奖励描述</th>
		<th>活动状态</th>
		<th>操作</th>
	</tr>
	<%
	Map<Integer, HuoDong> activityMap = ActivityMgr.activityMap;
	if(key.length()>0&&state.length()>0){// 更改活动开启关闭状态 
		HuoDong tmp = activityMap.get(Integer.valueOf(key));
		tmp.setHuoDongStatus(Integer.valueOf(state));
		activityMap.put(Integer.valueOf(key),tmp);
	}
	for(Integer id:activityMap.keySet()){
		HuoDong huoDong = activityMap.get(id);
		%>
		<tr>
		<td><%=huoDong.getId() %></td>
		<td><%=huoDong.getTitle() %></td>
		<td><%=huoDong.getDesc() %></td>
		<td><%=huoDong.getAwardDesc() %></td>
		<td>
		<%
		switch(huoDong.getId()){
		case ActivityMgr.ACT_QIANDAO:
		case ActivityMgr.ACT_SHOUCHONG:
			if(huoDong.getHuoDongStatus()==0){
				%><a href="activity.jsp?key=<%=id%>&state=<%=1%>">开启</a>|关闭<%
			}else{
				%>开启|<a href="activity.jsp?key=<%=id%>&state=<%=0%>">关闭</a><%
			}
			break;
		case ActivityMgr.ACT_OTHER:
			%>----<%
			break;
		}
		%>
		</td>
		<td>
		<%
		switch(huoDong.getId()){
		case ActivityMgr.ACT_QIANDAO:
			%><a href="qiandao.jsp">管理</a><%
			break;
		case ActivityMgr.ACT_SHOUCHONG:
			%><a href="shouchong.jsp">管理</a><%
			break;
		case ActivityMgr.ACT_OTHER:
			%>----<%
			break;
		}
		%>
		</td>
		</tr>
		<%
	}
	%>
<tr>
<td>
			4
		</td>
		<td>
			押镖活动
		</td>
		<td>
			押镖活动开启状态
		</td>
		<td>
		金币奖励
		</td>
		<td>
			
			<%
			String receiveState = request.getParameter("receiveState");
			if(receiveState!=null){
				YaBiaoHuoDongMgr.openFlag = Boolean.valueOf(receiveState);
			}
			if(YaBiaoHuoDongMgr.openFlag){
				%>
				<a>开启</a>|<a href="activity.jsp?receiveState=false">关闭</a>
				<%
			} else{
				%>
				<a href="activity.jsp?receiveState=true">开启</a>|<a>关闭</a>
				<%
			}
			%>
		</td>
	<td>
		--
		</td>
	</tr>
	<%
		Map<Integer, QiriQiandaoControl> xs7DaysMap = XianShiActivityMgr.xs7DaysControlMap;
			if(xsKey.length()>0&&xianshistate.length()>0){// 更改活动开启关闭状态 
				QiriQiandaoControl tmp = xs7DaysMap.get(Integer.valueOf(xsKey));
			if (tmp != null) {
				Integer huodongTypeId= Integer.valueOf(xsKey);
				Integer huodongState= Integer.valueOf(xianshistate);
				XianShiActivityMgr.instance.changeXianShiHuoDongState(huodongTypeId, huodongState);
			}
		}
	if(xs7DaysMap != null)
		for (Integer id : xs7DaysMap.keySet()) {
			QiriQiandaoControl huoDong = xs7DaysMap.get(id);
	%>
		<tr>
		<td><%=huoDong.getId() %></td>
		<td><%=huoDong.getName()%></td>
		<td><%=huoDong.getName()%></td>
		<td><%=huoDong.getDesc()%></td>
		<td>
		<%
	
			if(XianShiActivityMgr.xshdCloseList.contains(huoDong.getId())){
				%><a href="activity.jsp?xsKey=<%=id%>&xianshistate=<%=10%>">开启</a>|关闭<%
			}else{
				%>开启|<a href="activity.jsp?xsKey=<%=id%>&xianshistate=<%=20%>">关闭</a><%
			}
		%>
		</td>
		<td>
		--
		</td>
		</tr>
		<%
	}
	%>
	<%
		Map<Integer, XianshiControl> xsControlMap = XianShiActivityMgr.xsControlMap;
			if(xsKey.length()>0&&xianshistate.length()>0){// 更改活动开启关闭状态 
			XianshiControl tmp = xsControlMap.get(Integer.valueOf(xsKey));
			if (tmp != null) {
				Integer huodongTypeId= Integer.valueOf(xsKey);
				Integer huodongState= Integer.valueOf(xianshistate);
				XianShiActivityMgr.instance.changeXianShiHuoDongState(huodongTypeId, huodongState);
			}
		}
	if(xsControlMap != null)
		for (Integer id : xsControlMap.keySet()) {
			XianshiControl huoDong = xsControlMap.get(id);
	%>
		<tr>
		<td><%=huoDong.getId() %></td>
		<td><%="<input style='color:blue' type='text'value='" +huoDong.getName()+"'/>"%></td>
		<td><%=huoDong.getName()%></td>
		<td><%=huoDong.getDesc()%></td>
		<td>
		<%
	
			if(XianShiActivityMgr.xshdCloseList.contains(huoDong.getId())){
				%><a href="activity.jsp?xsKey=<%=id%>&xianshistate=<%=10%>">开启</a>|关闭<%
			}else{
				%>开启|<a href="activity.jsp?xsKey=<%=id%>&xianshistate=<%=20%>">关闭</a><%
			}
		%>
		</td>
		<td>
		<input type='button' value='修改活动名称' onclick='changeActivityName(<%=id%>,this)'/>
		</td>
		</tr>
		<%
	}
	%>
	</table>
</body>
</html>