<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.pve.BuZhenBean"%>
<%@page import="com.qx.pve.PveRecord"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.manu.dynasty.template.BaseItem"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.qx.bag.EquipGrid"%>
<%@page import="com.qx.bag.BagGrid"%>
<%@page import="java.util.List"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.qx.bag.BagMgr"%>
<%@page import="com.qx.bag.Bag"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%
		BagMgr.maxGridCount = BagMgr.spaceFactor - 1;
		String action = request.getParameter("action");
		String name = request.getParameter("account");
		String itemId = request.getParameter("itemId");
		name = name == null ? "" : name;
		if (session.getAttribute("name") != null && name.length() == 0) {
			name = (String) session.getAttribute("name");
		}
		itemId = itemId == null ? "" : itemId;
	%>
	<form action="">
		账号<input type="text" name="account" value="<%=name%>">
		<button type="submit">查询</button>
	</form>
	<!-- 	<form action=""> -->
	<!-- 		<input type="hidden" name="action" value="addItem"> <input -->
	<%-- 			type="hidden" name="account" value="<%=name%>"> 物品/装备id<input --%>
	<%-- 			type="text" name="itemId" value="<%=itemId%>"> --%>
	<!-- 		<button type="submit">添加</button> -->
	<!-- 		&nbsp;<a href='../dataConf/dataTemplate.jsp?type=equip' target="_blank">装备列表</a> -->
	<!-- 	</form> -->
	<%
	long pid = 0;
		if (name != null && name.length() > 0) {
			Account account = HibernateUtil.getAccount(name);
			if (account == null) {
	%>没有找到<%
		//HibernateUtil.saveAccount(name);
			} else {
				session.setAttribute("name", name);
				pid = account.getAccountId()*1000+GameServer.serverId;
	%><%=pid%>:<%=account.getAccountName()%>
	<br /> pve信息--------------
	<br />
	<%
		List<PveRecord> list = HibernateUtil.list(PveRecord.class,
						"where uid=" + pid);
				if ("wear".equals(action)) {
				} else if ("takeoff".equals(action)) {
				} else if ("delete".equals(action)) {
					int dbId = Integer.parseInt(request.getParameter("dbId"));
					PveRecord o= list.remove(dbId);
					HibernateUtil.delete(o);
				}
	%>
	<%
		//
				if ("addItem".equals(action)) {
				}
				//
				int cnt = list.size();
	%><table border='1'>
		<tr>
			<th>dbId</th>
			<th>关卡Id</th>
			<th>星级</th>
			<th>星级奖励领取</th>
			<th>传奇通过</th>
			<th>传奇通过次数</th>
			<th>传奇星星</th>
			<th>精英扫荡次数</th>
			<th>传奇扫荡次数</th>
			<th>传奇重置次数</th>
			<th>op</th>
		</tr>
		<%
			for (int i = 0; i < cnt; i++) {
						PveRecord bg = list.get(i);
		%><tr>
			<td><%=bg.dbId%></td>
			<td><%=bg.guanQiaId%></td>
			<td><%=String.format("%03d",bg.star)%></td>
			<td><%//String.format("%03d",bg.startRewardState)%></td>
			<td><%=bg.chuanQiPass?"是":"否"%></td>
			<td><%=bg.cqPassTimes%></td>
			<td><%=bg.cqStar%></td>
			<td><%=bg.jySaoDangTimes%></td>
			<td><%=bg.cqSaoDangTimes%></td>
			<td><%=bg.cqResetTimes%></td>
			<td><a href="?action=delete&dbId=<%=i%>">删</a>
			</td>
		</tr>
		<%
			}
				}
		%>
	</table>
		布阵信息
	<%
	BuZhenBean bean = HibernateUtil.find(BuZhenBean.class, pid);
		if(bean == null){
			out.println("-未设置");
		}else{
			out.print(bean.pos1+",");
			out.print(bean.pos2+",");
			out.print(bean.pos3+",");
			out.print(bean.pos4+",");
			out.print(bean.pos5);
		}
		}
	%>
</body>
</html>