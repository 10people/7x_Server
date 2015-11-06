<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="java.util.List"%>
<%@page import="com.qx.alliance.AllianceBean"%>
<%@page import="com.qx.alliance.AllianceMgr"%>
<%@page import="com.manu.dynasty.template.LianMeng"%>
<%@page import="java.util.Map"%>
<%@page import="qxmobile.protobuf.GuildProtos.Guild"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>联盟信息</title>
</head>
<body>
	联盟信息
	<br> 以下条件二选一:
	<form action="">
		<table>
			<tr>
				<td>
					<table align="left">
						<tr>
							<td>联盟ID</td>
							<td><input type="text" name="guildId"></td>
						</tr>
						<tr>
							<td>联盟名字</td>
							<td><input type="text" name="guildName"></td>
						</tr>
						<tr>
							<td colspan="2" align="center"><input type="submit"
								name="chaxun" value="查询"></td>
						</tr>
					</table>
				</td>
				
			</tr>
			<tr>	
				<td>
					<table align="left">
						<tr>
							<td>联盟名字</td>
							<td><input type="text" name="alncName"></td>
						</tr>
						<tr>
							<td>联盟icon</td>
							<td>
								<select name="alncIcon">
									<option>9001</option>
									<option>9002</option>
									<option>9003</option>
									<option>9004</option>
									<option>9005</option>
								</select>
							</td>
						</tr>
						<tr>
							<td colspan="2" align="center"><input type="button"
								name="chaxun" value="创建"></td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td>
					<%
						out.append("<table border='1'>");
						out.append("<tr><th>联盟ID</th><th>联盟名称</th><th>联盟旗帜ID</th><th>盟主ID</th><th>声望</th><th>现有成员</th><th>公告Inner</th><th>公告Outer</th><th>最大成员</th>");
						out.append("<th>等级</th><th>exp</th><th>成员详情</th></tr>");
						String idString = request.getParameter("guildId");
						String guildName = request.getParameter("guildName");
						Map<Integer, LianMeng>	lianMengMap=AllianceMgr.inst.getLianMengMap();		
						//如果提交查询则只显示查询结果
						if (request.getParameter("chaxun") != null) {
							int guildId = 0;
							try {
								guildId = Integer.parseInt(idString);
							} catch (Exception e) {
								out.println("请确认您输入的是数字" + "<br>");
							}
							AllianceBean lianmeng = HibernateUtil.find(AllianceBean.class,
									guildId);
							if (lianmeng != null) {
								
					%>
				
			<tr>
				<td><%=lianmeng.id%></td>
				<td><%=lianmeng.name%></td>
				<td><%=lianmeng.icon%></td>
				<td><%=lianmeng.creatorId%></td>
				<td><%=lianmeng.reputation%></td>
				<td><%=lianmeng.members%></td>
				<td><%=lianmeng.notice%></td>
				<td><%=lianmeng.notice%></td>
				<td><%=lianMengMap.get(lianmeng.level)%></td>
				<td><%=lianmeng.level%></td>
				<td><%=lianmeng.exp%></td>
				<!-- 超链接传递参数 &两边不能加空格 -->
				<td><a
					href="lmmemberlist.jsp?guildId=<%=lianmeng.id%>&guildName=<%=lianmeng.name%>">查看</a></td>
				<%
					}
					} else//显示所有联盟信息
					{

						List<AllianceBean> list = HibernateUtil.list(
								AllianceBean.class, "");
						if (list != null) {
							for (AllianceBean lianmeng : list) {
								out.append("<tr><td>");
								out.append("" + lianmeng.id);
								out.append("</td>");
								out.append("<td>");
								out.append(lianmeng.name);
								out.append("</td>");
								out.append("<td>");

								out.append("" + lianmeng.icon);

								out.append("</td>");
								out.append("<td>");
								out.append("" + lianmeng.creatorId);
								out.append("</td>");
								out.append("<td>");
								out.append("" + lianmeng.reputation);
								out.append("</td>");
								out.append("<td>");
								out.append("" + lianmeng.members);
								out.append("</td>");
								out.append("<td>");
								out.append("" + lianmeng.notice);
								out.append("</td>");
								out.append("<td>");
								out.println("" + lianmeng.notice);
								out.append("</td>");
								out.append("<td>");
								out.append("" + lianMengMap.get(lianmeng.level));
								out.append("</td>");
								out.append("<td>");
								out.append("" + lianmeng.level);
								out.append("</td>");
								out.append("<td>");
								out.append("" + lianmeng.exp);
								out.append("</td>");

							}
						}
					}
					out.append("</table>");
				%>
				</td>
			</tr>
		</table>
	</form>

</body>
</html>