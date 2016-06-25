<%@page import="com.qx.yabiao.YaBiaoRobot"%>
<%@page import="com.qx.world.Scene"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="com.qx.world.Player"%>
<%@page import="com.qx.equip.domain.EquipXiLian"%>
<%@page import="com.qx.bag.EquipGrid"%>
<%@page import="com.manu.dynasty.template.CanShu"%>
<%@page import="qxmobile.protobuf.UserEquipProtos.XiLianRes"%>
<%@page import="com.qx.equip.web.UserEquipAction"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.equip.domain.UserEquip"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>显示用户装备</title>
<script type="text/javascript">
	// function go(){
	// 	var v = document.getElementById("yuanbaoxilian").value;
	// 	var href= location.href;
	// 	location.href = href+"&v="+v;
	// }
</script>
</head>
<body>
	<%
		String uid = request.getParameter("uid");
		uid = StringUtils.isEmpty(uid) ? "0" : uid;
		String ybScId = request.getParameter("ybScId");
		ybScId = StringUtils.isEmpty(ybScId) ? "0" : ybScId;
		int thisUid = Integer.parseInt(uid);
		int thisybScId = Integer.parseInt(ybScId);
		Scene sc = BigSwitch.inst.ybMgr.yabiaoScenes.get(thisybScId);
		if (sc != null) {
			Player p = sc.players.get(thisUid);
			if (p != null) {
	%>
	<table border="1" align="left">
		<tr>
			<td>jzId :<%=p.jzId%></td>
		</tr>
		<tr>
			<td>生命 :<%=p.currentLife%></td>
		</tr>
		<tr>
			<td>生命上限 :<%=p.totalLife%></td>
		</tr>
		<tr>
			<td>坐标 :<%=p.posX+"---"+p.posY+"---"+p.posZ%></td>
		</tr>
		<tr>
			<td>名字 :<%=p.name%></td>
		</tr>
		<tr>
			<td>联盟id :<%=p.allianceId%></td>
		</tr>
		<tr>
			<td>session :<%=p.session%></td>
		</tr>
		<tr>
			<td>userId :<%=p.userId%></td>
		</tr>
		<tr>
			<td>roleId :<%=p.roleId%></td>
		</tr>
<!-- 		<tr> -->
<%-- 			<td>pState :<%=p.pState%></td> --%>
<!-- 		</tr> -->
		<tr>
			<td>chengHaoId :<%=p.chengHaoId%></td>
		</tr>
		<tr>
			<td>联盟名字 :<%=p.lmName%></td>
		</tr>
		<tr>
			<td>vip :<%=p.vip%></td>
		</tr>
		<tr>
			<td>等级 :<%=p.jzlevel%></td>
		</tr>
		<tr>
			<td>zhiWu :<%=p.zhiWu%></td>
		</tr>
		<tr>
			<td>safeArea :<%=p.safeArea%></td>
		</tr>
		<tr>
			<td>战力 :<%=p.zhanli%></td>
		</tr>
		<tr>
			<td>国家 :<%=p.guojia%></td>
		</tr>
		<%
		if(p.roleId == Scene.YBRobot_RoleId){
			YaBiaoRobot temp = (YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(p.jzId);
		%>
		<tr>
			<td>马车价值 :<%=p.worth%></td>
		</tr>
		<tr>
			<td>马车类型 :<%=p.horseType%></td>
		</tr>
		<% }%>
		<tr>
			<td>血瓶数目 :<%=p.xuePingRemain%></td>
		</tr>
		<tr>
			<td>visibleIDs :<%=p.visbileUids%></td>
		</tr>


	</table>
	<%
		}else{%>
			<p>马车已经到达终点或者被摧毁</p>
		<%}
		}else{%>
		<p>马车已经到达终点或者被摧毁<</p>	
		<%}%>
</body>
</html>