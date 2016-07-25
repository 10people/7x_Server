<%@page import="com.qx.cdkey.CDKeyInfo"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="com.qx.cdkey.CDKeyMgr"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@include file="/myFuns.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title></title>
</head>
<body>
	<h3>条件搜索</h3>
	<form action="">
		使用者君主id：<input type="number" name="jzId"/><br/>
		创建日期>=<input type="text" name="startDate"/><=<input type="text" name="endDate"/><span style="color:red">格式：xxxx-xx-xx</span><br/>
		渠道id：<input type="number" name="chanId"/><br/>
		CDKey：<input type="text" name="key"/><br/>
		状态<input type="number" name="state"/><span style="color:red">状态 ，0-可兑换,1-已兑换，2-已过期</span><br/>
		<input type="hidden" name="action" value="query"/>
		<button type="submit">查询</button>
	</form>
	<hr/>
	<h3>按条件删除cdkey</h3>
	<form action="" onsubmit="return checkDel()">
		使用者君主id：<input type="number" name="jzId"/><br/>
		渠道id：<input type="number" name="chanId"/><br/>
		CDKey：<input type="text" name="key"/><br/>
		状态<input type="number" name="state"/><span style="color:red">状态 ，0-可兑换,1-已兑换，2-已过期</span><br/>
		<input type="hidden" name="action" value ="delete"/>
		<button type="submit">删除</button>
	</form>
	<script type="text/javascript">
		function checkDel(){
			var flag = confirm("确定要按照这些条件删除CDKey吗？");
			if(flag){
				return true;
			}
			return false;
		}
	</script>
	<%
	String jzIdStr = request.getParameter("jzId");
	String startDateStr = request.getParameter("startDate");
	String endDateStr = request.getParameter("endDate");
	String chanIdStr = request.getParameter("chanId");
	String stateStr = request.getParameter("state");
	String key = request.getParameter("key");
	String action =request.getParameter("action");
	
	do{
	if(action==null){
		break;
	}
	else if(action.equals("query")){
		Long jzId = (jzIdStr==null||jzIdStr.length()==0)?null:Long.parseLong(jzIdStr);
		Date startDate = (startDateStr==null||startDateStr.length()==0)?null:new Date(Integer.parseInt(startDateStr.split("-")[0]),Integer.parseInt(startDateStr.split("-")[1]),Integer.parseInt(startDateStr.split("-")[2]));
		Date endDate = (endDateStr==null||endDateStr.length()==0)?null:new Date(Integer.parseInt(endDateStr.split("-")[0]),Integer.parseInt(endDateStr.split("-")[1]),Integer.parseInt(endDateStr.split("-")[2]));
		Integer chanId = (chanIdStr==null||chanIdStr.length()==0)?null:Integer.parseInt(chanIdStr);
		Integer state = (stateStr==null||stateStr.length()==0)?null:Integer.parseInt(stateStr);
		key = (key==null||key.length()==0)?null:key;
		
		List<CDKeyInfo> keyList = CDKeyMgr.inst.getCDKeys(jzId, startDate, endDate,key, chanId, state);
		%>
		<table border="1">
		<tr><th colspan="6">按条件搜索的cdkey</th></tr>
		<tr>
			<th>渠道id</th>
			<th>创建日期</th>
			<th>截止日期</th>
			<th>使用者君主id</th>
			<th>CDKey</th>
			<th>奖励</th>
		</tr>
		<%
		if(keyList==null||keyList.size()==0){
			return;
		}
		for(CDKeyInfo keyInfo:keyList){
			%>
			<tr>
				<td><%=keyInfo.chanId %></td>
				<td><%=keyInfo.createDate.toLocaleString() %></td>
				<td><%=keyInfo.deadDate.toLocaleString() %></td>
				<td><%=keyInfo.jzId %></td>
				<td><%=keyInfo.cdkey%></td>
				<td><%=keyInfo.awards %></td>
			</tr>
			<%
		}
		%>
		</table>
		<%
		}
		else if(action.equals("delete")){
			Long jzId = (jzIdStr==null||jzIdStr.length()==0)?null:Long.parseLong(jzIdStr);
			Integer chanId = (chanIdStr==null||chanIdStr.length()==0)?null:Integer.parseInt(chanIdStr);
			Integer state = (stateStr==null||stateStr.length()==0)?null:Integer.parseInt(stateStr);
			key = (key==null||key.length()==0)?null:key;
			List<CDKeyInfo> keyList = CDKeyMgr.inst.deleteCDKey(jzId, key, chanId, state);
			%>
			<table border="1">
			<tr><th colspan="6">删除的cdkey</th></tr>
			<tr>
				<th>渠道id</th>
				<th>创建日期</th>
				<th>截止日期</th>
				<th>使用者君主id</th>
				<th>CDKey</th>
				<th>奖励</th>
			</tr>
			<%
			if(keyList==null||keyList.size()==0){
				return;
			}
			for(CDKeyInfo keyInfo:keyList){
				%>
				<tr>
					<td><%=keyInfo.chanId %></td>
					<td><%=keyInfo.createDate.toLocaleString() %></td>
					<td><%=keyInfo.deadDate.toLocaleString() %></td>
					<td><%=keyInfo.jzId %></td>
					<td><%=keyInfo.cdkey%></td>
					<td><%=keyInfo.awards %></td>
				</tr>
				<%
			}
			%>
			</table>
			<%
		}
	}while(false); %>
</body>
</html>
