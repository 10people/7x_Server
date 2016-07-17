<%@page import="java.util.List"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="qxmobile.protobuf.Ranking.GetRankResp"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="qxmobile.protobuf.Ranking.GetRankReq"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="com.qx.guojia.GuoJiaBean"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="com.qx.alliance.AllianceBean"%>
<%@page import="com.qx.ranking.RankingMgr"%>
<%@page import="com.manu.dynasty.template.BaiZhanNpc"%>
<%@page import="com.qx.pvp.PVPConstant"%>
<%@page import="com.manu.dynasty.base.service.CommonService"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="qxmobile.protobuf.PveMessage.UserPve"%>
<%@page import="qxmobile.protobuf.HeroMessage.UserHero"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.qx.pvp.PvpMgr"%>
<%@page import="com.qx.pvp.PvpBean"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="com.qx.account.Account"%>
<%@page import="qxmobile.protobuf.ZhanDou.PvpZhanDouInitReq"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@include file="/myFuns.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<base href="<%=basePath%>">

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript">
	function go(act) {
		var v = document.getElementById(act).value;
		location.href = 'gm/baizhan.jsp?action=' + act + "&v=" + v;
	}
</script>
<title>获取pvp前N名次玩家</title>
</head>

<body>
	<%
		String name = request.getParameter("name");
		String pageNoStr = request.getParameter("pageNo");
		String guojiaIdStr = request.getParameter("guojiaId");
		String rankTypeStr = request.getParameter("rankType");
		pageNoStr = (pageNoStr == null || pageNoStr.length() == 0) ? "0" : pageNoStr;
		guojiaIdStr = (guojiaIdStr == null || guojiaIdStr.length() == 0) ? "0" : guojiaIdStr;
		rankTypeStr = (rankTypeStr == null || rankTypeStr.length() == 0) ? "0" : rankTypeStr;
		int pageNo = Integer.parseInt(pageNoStr);
		int guojiaId = Integer.parseInt(guojiaIdStr);
		int rankType = Integer.parseInt(rankTypeStr);
		if (rankType != 0) {
			switch (rankType) {
			case 1:// 君主榜
				if (name == null) {
					response.sendRedirect("rank4junzhu.jsp?pageNo=" + pageNo + "&guojiaId=" + guojiaId + "");
				} else {
					response.sendRedirect("rank4junzhu.jsp?name=" + name + "");
				}
				break;
			case 2:// 联盟榜
				if (name == null) {
					response.sendRedirect(
							"rank4lianmeng.jsp?rankType=2&pageNo=" + pageNo + "&guojiaId=" + guojiaId + "");
				} else {
					response.sendRedirect("rank4lianmeng.jsp?rankType=2&name=" + name + "");
				}
				break;
			case 201:// 联盟榜（按声望排序）
				if (name == null) {
					response.sendRedirect(
							"rank4lianmeng.jsp?rankType=201&pageNo=" + pageNo + "&guojiaId=" + guojiaId + "");
				} else {
					response.sendRedirect("rank4lianmeng.jsp?rankType=201&name=" + name + "");
				}
				break;
			case 3:// 过关榜
				if (name == null) {
					response.sendRedirect("rank4baizhan.jsp?pageNo=" + pageNo + "&guojiaId=" + guojiaId + "");
				} else {
					response.sendRedirect("rank4baizhan.jsp?name=" + name + "");
				}
				break;
			case 4:
				if (name == null) {
					response.sendRedirect("rank4guoguan.jsp?pageNo=" + pageNo + "&guojiaId=" + guojiaId + "");
				} else {
					response.sendRedirect("rank4guoguan.jsp?name=" + name + "");
				}
				break;
			default:
				break;
			}
		}
	%>
	<%
		String pageSize = request.getParameter("pageSize");
		String junRange = request.getParameter("junRange");
		String junMinLevel = request.getParameter("junMinLevel");
		if (pageSize != null && pageSize.length() != 0) {
			RankingMgr.PAGE_SIZE = Integer.parseInt(pageSize);
		}
		if (junRange != null && junRange.length() != 0) {
			RankingMgr.RANK_MAXNUM = Integer.parseInt(junRange);
		}
		if (junMinLevel != null && junMinLevel.length() != 0) {
			RankingMgr.RANK_MINLEVEL = Integer.parseInt(junMinLevel);
		}
	%>
	<hr />
	<p>
		<a href="gm/rank4junlevel.jsp" style="color: #FF0000">查看君主等级榜</a> &nbsp;&nbsp;&nbsp; 
		<a href="gm/rank4ChongLou.jsp" style="color: #FF0000">查看重楼排行榜</a> &nbsp;&nbsp;&nbsp; 
		<a href="gm/rank4baizhan.jsp" style="color: #FF0000">查看百战排行榜</a> &nbsp;&nbsp;&nbsp; 
		<a href="gm/rank4alliance.jsp" style="color: #FF0000">查看联盟排行榜</a> &nbsp;&nbsp;&nbsp; 
		<!-- <a href="gm/baizhanRankN.jsp" style="color: #FF0000" target='target'>百战名次排行榜</a> &nbsp;&nbsp;&nbsp; -->
		<!-- <a href="gm/baizhanRankN.jsp" style="color: #FF0000" target='target'>个人贡金/联盟贡金排行榜</a><br /> -->
	</p>
	<hr />
	<form action="">
		<p>
		<h3>参数设置</h3>
		<p>
			每页条目：<input type="text" name="pageSize"
				value="<%=RankingMgr.PAGE_SIZE%>" />| 筛选最大数量：<input type="text"
				name="junRange" value="<%=RankingMgr.RANK_MAXNUM%>" />| 君主最低等级：<input
				type="text" name="junMinLevel" value="<%=RankingMgr.RANK_MINLEVEL%>" />
		</p>
		<p>
			<input type="submit" value="修改" />
		</p>
	</form>
	=====================================================
	<form action="">
		<p>
		<h3>正常刷新加载</h3>
		<p>
			输入请求的页码：<input type="number" name="pageNo" />
		</p>
		<p>
			输入国家id：<input type="number" name="guojiaId" />
		</p>
		<input name="rankType" type="radio" value="1">君主榜 <input
			name="rankType" type="radio" value="2">联盟榜 <input
			name="rankType" type="radio" value="201">联盟榜(按声望) <input
			name="rankType" type="radio" value="3">百战榜 <input
			name="rankType" type="radio" value="4">过关榜 <input
			type="submit" value="查看" />
		</p>
	</form>
	=====================================================
	<form action="">
		<p>
		<h3>按名字查询</h3>
		<p>
			输入名字：<input type="text" name="name" />
		</p>
		<input name="rankType" type="radio" value="1">君主榜 <input
			name="rankType" type="radio" value="2">联盟榜 <input
			name="rankType" type="radio" value="201">联盟榜(按声望) <input
			name="rankType" type="radio" value="3">百战榜 <input
			name="rankType" type="radio" value="4">过关榜 <input
			type="submit" value="查看" />
		</p>
	</form>
	=====================================================
	<form action="">
		<p>
		<h3>获取排名</h3>
		<p>
			输入id：<input type="text" name="queryId" /> 输入国家id：<input type="text"
				name="queryGjId" />
		</p>
		<input name="queryType" type="radio" value="1">君主榜 <input
			name="queryType" type="radio" value="2">联盟榜 <input
			name="queryType" type="radio" value="201">联盟榜(按声望) <input
			name="queryType" type="radio" value="3">百战榜 <input
			name="queryType" type="radio" value="4">过关榜 <input
			type="submit" value="查看" />
		</p>
	</form>
	<%
		String queryIdStr = request.getParameter("queryId");
		String queryGjIdStr = request.getParameter("queryGjId");
		String queryTypeStr = request.getParameter("queryType");
		queryIdStr = (queryIdStr == null || queryIdStr.length() == 0) ? "0" : queryIdStr;
		queryGjIdStr = (queryGjIdStr == null || queryGjIdStr.length() == 0) ? "0" : queryGjIdStr;
		queryTypeStr = (queryTypeStr == null || queryTypeStr.length() == 0) ? "0" : queryTypeStr;
		int queryId = Integer.parseInt(queryIdStr);
		int queryGjId = Integer.parseInt(queryGjIdStr);
		int queryType = Integer.parseInt(queryTypeStr);
		if (queryId != 0) {
			final IoSession fs = new RobotSession() {
				public WriteFuture write(Object message) {
					setAttachment(message);
					synchronized (this) {
						this.notify();
					}
					return null;
				}
			};
			GetRankReq.Builder builder = GetRankReq.newBuilder();
			builder.setRankType(queryType);
			builder.setId(queryId);
			builder.setGuojiaId(queryGjId);
			synchronized (fs) {
				BigSwitch.inst.route(PD.GET_RANK_REQ, builder, fs);
				//	fs.wait();
			}
			GetRankResp resp = (GetRankResp) fs.getAttachment();
	%>
	<p>
		<%=resp.getRank()%>
	</p>
	<%
		}
	%>
	=====================================================
	<br>
	<%
		String rankmin = "";
		String rankmax = "";
		String typea = "";
		out("(排行类型: 战力排行:1; 联盟排行： 2)");
	%>
	<form action="">
		<br /> <br /> 最低名次 <input type="text" name="rankmin"
			value="<%=rankmin%>">&nbsp;和&nbsp; 最高名次 <input type="text"
			name="rankmax" value="<%=rankmax%>">&nbsp; <br /> <br /> 排行类型
		<input type="text" name="typea" value="<%=typea%>">&nbsp;

		<button type="submit">查询</button>
	</form>

	<%
		rankmin = request.getParameter("rankmin");
		rankmax = request.getParameter("rankmax");
		typea = request.getParameter("typea");

		if (rankmin != null && rankmin.length() > 0 && rankmax != null && rankmax.length() > 0 && typea != null) {
			if ("".equals(rankmin)) {
				out("请输入");
				return;
			}
			if ("".equals(rankmax)) {
				out("请输入");
				return;
			}
			if ("".equals(typea)) {
				out("请输入");
				return;
			}
			long rmin = Long.parseLong(rankmin);
			long rmax = Long.parseLong(rankmax);
			int type = Integer.parseInt(typea);
			if (rmin > rmax) {
				out("最低名次的值必须大于最高名次");
			} else {
				br();
				out("输入的开始名次是： " + rmin);
				br();
				out("输入的结束名次是： " + rmax);
				br();
				String diffType = null;
				String info = null;
				switch (type) {
				case 1:
					diffType = RankingMgr.zhanliRank;
					info = "战力";
					break;
				case 2:
					diffType = RankingMgr.lianMengLevel;
					info = "联盟等级";
					break;
				}
				if (diffType != null) {
					Map<String, Double> map = null;//RankingMgr.inst.getPaiHangOfType(rmin, rmax, diffType);     
					if (map == null || map.size() == 0) {
						out("没有找到当前范围内的名次");
					} else {
						br();
						tableStart();
						trS();
						td("id");
						td("排名");
						td("姓名");
						td(info);
						trE();
						if (type == 1) {
							for (Map.Entry<String, Double> entry : map.entrySet()) {
								String idN = entry.getKey();
								Double zhanli = entry.getValue();
								trS();
								td(idN.split("_")[0]);
								td(rmin++);
								td(idN.split("_")[1]);
								td(zhanli);
								trE();
							}
						} else if (type == 2) {
							for (Map.Entry<String, Double> entry : map.entrySet()) {
								String idN = entry.getKey();
								Double level = entry.getValue();
								int mengId = Integer.parseInt(idN == null ? "-1" : idN);
								AllianceBean alncBean = HibernateUtil.find(AllianceBean.class, mengId);
								if (alncBean == null) {
									continue;
								}
								trS();
								td(idN);
								td(rmin++);
								td(alncBean.name);
								td(level);
								trE();
							}
							tableEnd();
						}
					}
				}
			}
		}
	%>
	=====================================================
	<%
		List<GuoJiaBean> dayRank = RankingMgr.inst.getGuojiaRank(RankingMgr.GUOJIA_DAY_RANK);
		List<GuoJiaBean> lastDayRank = RankingMgr.inst.getGuojiaRank(RankingMgr.GUOJIA_DAY_LAST_RANK);
		List<GuoJiaBean> weekRank = RankingMgr.inst.getGuojiaRank(RankingMgr.GUOJIA_WEEK_RANK);
		List<GuoJiaBean> lastWeekRank = RankingMgr.inst.getGuojiaRank(RankingMgr.GUOJIA_WEEK_LAST_RANK);
		Map<Integer, List<AllianceBean>> gjLianmengDayMap = new HashMap<Integer, List<AllianceBean>>();
		Map<Integer, List<AllianceBean>> gjLianmengLastDayMap = new HashMap<Integer, List<AllianceBean>>();
		Map<Integer, List<AllianceBean>> gjLianmengWeekMap = new HashMap<Integer, List<AllianceBean>>();
		Map<Integer, List<AllianceBean>> gjLianmengLastWeekMap = new HashMap<Integer, List<AllianceBean>>();
		for (int i = 0; i <= 7; i++) {
			List<AllianceBean> lianmengDayRank = RankingMgr.inst.getLianmengSWRank(RankingMgr.LIANMENG_SW_DAY_RANK,
					i);
			List<AllianceBean> lianmengLastDayRank = RankingMgr.inst
					.getLianmengSWRank(RankingMgr.LIANMENG_SW_LAST_DAY_RANK, i);
			List<AllianceBean> lianmengWeekRank = RankingMgr.inst
					.getLianmengSWRank(RankingMgr.LIANMENG_SW_WEEK_RANK, i);
			List<AllianceBean> lianmengLastWeekRank = RankingMgr.inst
					.getLianmengSWRank(RankingMgr.LIANMENG_SW_LAST_WEEK_RANK, i);
			if (lianmengDayRank != null) {
				gjLianmengDayMap.put(i, lianmengDayRank);
			}
			if (lianmengLastDayRank != null) {
				gjLianmengLastDayMap.put(i, lianmengLastDayRank);
			}
			if (lianmengWeekRank != null) {
				gjLianmengWeekMap.put(i, lianmengWeekRank);
			}
			if (lianmengLastWeekRank != null) {
				gjLianmengLastWeekMap.put(i, lianmengLastWeekRank);
			}
		}
	%>
	<br>
	<table border="1" style="margin-left: 10px; float: left">
		<tr>
			<th colspan="3">国家今日日榜</th>
		</tr>
		<tr>
			<th>国家id</th>
			<th>国家名字</th>
			<th>声望</th>
		</tr>
		<%
			for (GuoJiaBean gj : dayRank) {
		%>
		<tr>
			<td><%=gj.guoJiaId%></td>
			<td><%=HeroService.getNameById("" + gj.guoJiaId)%></td>
			<td><%=gj.shengWang%></td>
		</tr>
		<%
			}
		%>
	</table>
	<table border="1" style="margin-left: 10px; float: left">
		<tr>
			<th colspan="3">国家昨日日榜</th>
		</tr>
		<tr>
			<th>国家id</th>
			<th>国家名字</th>
			<th>声望</th>
		</tr>
		<%
			for (GuoJiaBean gj : lastDayRank) {
		%>
		<tr>
			<td><%=gj.guoJiaId%></td>
			<td><%=HeroService.getNameById("" + gj.guoJiaId)%></td>
			<td><%=gj.shengWang%></td>
		</tr>
		<%
			}
		%>
	</table>
	<table border="1" style="margin-left: 10px; float: left">
		<tr>
			<th colspan="3">国家本周周榜</th>
		</tr>
		<tr>
			<th>国家id</th>
			<th>国家名字</th>
			<th>声望</th>
		</tr>
		<%
			for (GuoJiaBean gj : weekRank) {
		%>
		<tr>
			<td><%=gj.guoJiaId%></td>
			<td><%=HeroService.getNameById("" + gj.guoJiaId)%></td>
			<td><%=gj.shengWang%></td>
		</tr>
		<%
			}
		%>
	</table>
	<table border="1" style="margin-left: 10px; float: left">
		<tr>
			<th colspan="3">国家上周周榜</th>
		</tr>
		<tr>
			<th>国家id</th>
			<th>国家名字</th>
			<th>声望</th>
		</tr>
		<%
			for (GuoJiaBean gj : lastWeekRank) {
		%>
		<tr>
			<td><%=gj.guoJiaId%></td>
			<td><%=HeroService.getNameById("" + gj.guoJiaId)%></td>
			<td><%=gj.shengWang%></td>
		</tr>
		<%
			}
		%>
	</table>
	<div style="clear: both"></div>
	<br>
	<%
		for (int i = 1; i <= 7; i++) {
	%>
	<table border="1" style="margin-left: 10px; float: left">
		<%
			if (i == 0) {
		%>
		<tr>
			<th colspan="3">联盟今日日榜总榜</th>
		</tr>
		<%
			} else {
		%>
		<tr>
			<th colspan="3"><%=HeroService.getNameById("" + i)%>国联盟今日日榜</th>
		</tr>
		<%
			}
		%>
		<tr>
			<th>联盟id</th>
			<th>联盟名</th>
			<th>声望</th>
		</tr>
		<%
			List<AllianceBean> gjLmDayList = gjLianmengDayMap.get(i);
				if (gjLmDayList != null) {
					for (AllianceBean lm : gjLmDayList) {
		%>
		<tr>
			<td><%=lm.id%></td>
			<td><%=lm.name%></td>
			<td><%=lm.reputation%></td>
		</tr>
		<%
			}
				}
		%>
	</table>
	<%
		}
	%>
	<div style="clear: both"></div>
	<br>
	<%
		for (int i = 1; i <= 7; i++) {
	%>
	<table border="1" style="margin-left: 10px; float: left">
		<%
			if (i == 0) {
		%>
		<tr>
			<th colspan="3">联盟昨日日榜总榜</th>
		</tr>
		<%
			} else {
		%>
		<tr>
			<th colspan="3"><%=HeroService.getNameById("" + i)%>国联盟昨日日榜</th>
		</tr>
		<%
			}
		%>
		<tr>
			<th>联盟id</th>
			<th>联盟名</th>
			<th>声望</th>
		</tr>
		<%
			List<AllianceBean> gjLmLastDayList = gjLianmengLastDayMap.get(i);
				if (gjLmLastDayList != null) {
					for (AllianceBean lm : gjLmLastDayList) {
		%>
		<tr>
			<td><%=lm.id%></td>
			<td><%=lm.name%></td>
			<td><%=lm.reputation%></td>
		</tr>
		<%
			}
				}
		%>
	</table>
	<%
		}
	%>
	<div style="clear: both"></div>
	<br>
	<%
		for (int i = 1; i <= 7; i++) {
	%>
	<table border="1" style="margin-left: 10px; float: left">
		<%
			if (i == 0) {
		%>
		<tr>
			<th colspan="3">联盟本周榜总榜</th>
		</tr>
		<%
			} else {
		%>
		<tr>
			<th colspan="3"><%=HeroService.getNameById("" + i)%>国联盟本周周榜</th>
		</tr>
		<%
			}
		%>
		<tr>
			<th>联盟id</th>
			<th>联盟名</th>
			<th>声望</th>
		</tr>
		<%
			List<AllianceBean> gjLmWeekList = gjLianmengWeekMap.get(i);
				if (gjLmWeekList != null) {
					for (AllianceBean lm : gjLmWeekList) {
		%>
		<tr>
			<td><%=lm.id%></td>
			<td><%=lm.name%></td>
			<td><%=lm.reputation%></td>
		</tr>
		<%
			}
				}
		%>
	</table>
	<%
		}
	%>
	<div style="clear: both"></div>
	<br>
	<%
		for (int i = 1; i <= 7; i++) {
	%>
	<table border="1" style="margin-left: 10px; float: left">
		<%
			if (i == 0) {
		%>
		<tr>
			<th colspan="3">联盟上周周榜总榜</th>
		</tr>
		<%
			} else {
		%>
		<tr>
			<th colspan="3"><%=HeroService.getNameById("" + i)%>国联盟上周周榜</th>
		</tr>
		<%
			}
		%>
		<tr>
			<th>联盟id</th>
			<th>联盟名</th>
			<th>声望</th>
		</tr>
		<%
			List<AllianceBean> gjLmLastWeekList = gjLianmengLastWeekMap.get(i);
				if (gjLmLastWeekList != null) {
					for (AllianceBean lm : gjLmLastWeekList) {
		%>
		<tr>
			<td><%=lm.id%></td>
			<td><%=lm.name%></td>
			<td><%=lm.reputation%></td>
		</tr>
		<%
			}
				}
		%>
	</table>
	<%
		}
	%>
	<div style="clear: both"></div>
	<br>
</body>
</html>
