<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="java.util.List"%>
<%@page import="com.qx.ranking.RankingMgr"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title></title>
</head>
<body>
	<%
		int num = 50;
	%>

	<%
		String refresh = request.getParameter("refresh");
		if (refresh != null && refresh.length() > 0) {
			List<JunZhu> list = HibernateUtil.list(JunZhu.class, "");
			for (JunZhu jz : list) {
				RankingMgr.inst.resetLevelRankRedis(jz);
			}
		}

		String numStr = request.getParameter("num");
		if (numStr != null && numStr.length() > 0) {
			num = Integer.parseInt(numStr);
		}
	%>
	<form action="">
		<p>
			君主等级排行前<input type="number" name="num" value="<%=num%>" maxlength="5" />位
		</p>
		<p>
			<button type="submit">查看</button>
		</p>
	</form>
	<a href="rank4junlevel.jsp?refresh=1">刷新君主等级榜</a>
	<hr />
	<%
		List<JunZhu> junzhus = null;//RankingMgr.inst.getJunzhuLevelRank(num);
		if (junzhus == null) {
	%>暂无君主等级排行信息<%
		} else {
	%>
	<h2>
		前<%=num%>位君主平均等级为<%=RankingMgr.inst.getTopJunzhuAvgLevel(num)%></h2>
	<table>
		<tr>
			<th>排名</th>
			<th>君主id</th>
			<th>君主名</th>
			<th>君主等级</th>
		</tr>
		<%
			int index = 1;
				for (JunZhu jz : junzhus) {
		%>
		<tr>
			<td><%=index%></td>
			<td><%=jz.id%></td>
			<td><%=jz.name%></td>
			<td><%=jz.level%></td>
		</tr>
		<%
			index++;
				}
		%>
	</table>
	<%
		}
	%>
</body>
</html>
