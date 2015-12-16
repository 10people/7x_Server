
<%@page import="com.qx.alliance.AllianceMgr"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.alliance.AllianceApply"%>
<%@page import="com.qx.pvp.PvpBean"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>显示联盟成员列表</title>
</head>
<body>
	<h4>显示联盟申请成员列表</h4>
	<br>
	<table align="left" border="1">
		<tr>
			<td>联盟ID</td>
			<td>联盟名称</td>
			<td>成员ID</td>
			<td>成员名称</td>
			<td>成员等级</td>
			<td>成员军衔</td>
			<td>排名</td>
		</tr>
		<%
		request.setCharacterEncoding("utf-8"); 
		response.setCharacterEncoding("utf-8"); 
		out.print(request.getParameter("id"));
			if (request.getParameter("id") != null) {
				String s=request.getParameter("id");
				int guildId=Integer.parseInt(s);
				String guildName = new String(request.getParameter("name").getBytes("ISO-8859-1"),"UTF-8");
				List<AllianceApply> list = AllianceMgr.inst.getApplyers(guildId);
				if (list != null) {
					for (AllianceApply ap : list) {
						JunZhu jz = HibernateUtil.find(JunZhu.class, ap.junzhuId);
						PvpBean pvpBean = HibernateUtil.find(PvpBean.class, jz.id);
						out.print("<tr><td>"); 
						out.print("" + guildId);
						out.print("</td>");
						out.print("<td>");
						out.print("" + guildName);
						out.print("</td>");
						out.print("<td>");
						out.print("" + ap.junzhuId);
						out.print("</td>");
						out.print("<td>");
						out.print("" + jz.name);
						out.print("</td>");
						out.print("<td>");
						out.print("" + jz.level);
						out.print("</td>");
						out.print("<td>");
						out.print("" + (pvpBean == null ? -1 : pvpBean.junXianLevel));
						out.print("</td>");
						out.print("<td>");
						out.print("");// + (pvpBean == null ? -1 : pvpBean.rank));
						out.print("</td>");
						out.print("</tr>");
					}
				}else{
					out.print("该联盟无成员");
				}
			}else{
				out.print("kong000000000000");
			}
		%>
	</table>
</body>
</html>