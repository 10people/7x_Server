<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.alliance.AllianceMgr"%>
<%@page import="com.qx.alliance.AllianceBean"%>
<%@page import="com.manu.dynasty.util.DateUtils"%>
<%@page import="com.qx.chonglou.ChongLouMgr"%>
<%@page import="com.qx.chonglou.ChongLouRecord"%>
<%@page import="java.util.Set"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="java.util.List"%>
<%@page import="com.qx.ranking.RankingMgr"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@include file="/myFuns.jsp"%>
<html>
<head>
<script type="text/javascript">
	function go(act) {
		var node = document.getElementById(act);
		if(node.value != null) {
			location.href = '?action=' + act + "&v=" + node.value;
		} else {
			location.href = '?action=' + act;
		}
	}
</script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title></title>
</head>
<body>
	<input id="resetChongLouRank" type="button" value="重置  联盟排行榜" onclick="go('resetChongLouRank')" />
	<font color="red">不需要重置联盟排行榜不要点</font>
	<br /><br />
	<p>
		<a href="rank4alliance.jsp?guojia=0">刷新</a>
		<a href="rank4alliance.jsp?guojia=0">周</a>
		<a href="rank4alliance.jsp?guojia=1">齐</a>
		<a href="rank4alliance.jsp?guojia=2">楚</a>
		<a href="rank4alliance.jsp?guojia=3">燕</a>
		<a href="rank4alliance.jsp?guojia=4">韩</a>
		<a href="rank4alliance.jsp?guojia=5">赵</a>
		<a href="rank4alliance.jsp?guojia=6">魏</a>
		<a href="rank4alliance.jsp?guojia=7">秦</a>
	</p>

	<%
		String action = request.getParameter("action");
		if(action != null) {
			if("resetChongLouRank".equals(action)) {
				for(int i = 0; i <= 7; i++) {
					Redis.getInstance().del(RankingMgr.LIANMENG_RANK + "_" + i);
				}
				List<AllianceBean> list = HibernateUtil.list(AllianceBean.class, "");
				for(AllianceBean bean : list) {
					if (bean.creatorId % 1000 != GameServer.serverId)
						continue;// 过滤掉不是本服务器的联盟//正式服务器数据库分开就没有这个问题。		
					if(bean.level == 1) {
						Redis.getInstance().set("allianceUpgradeTime_" + bean.id, bean.createTime.getTime()+"");
					}
					RankingMgr.inst.resetLianMengRankRedis(new Object[]{bean.id, bean.country});
				}
			}
		}
		
		String guojia = request.getParameter("guojia");
		int guojiaId = 0;
		String guojiaName = "周";
		if(guojia != null && !guojia.equals("")) {
			guojiaId = Integer.parseInt(guojia);
			switch(guojiaId) {
			case 0:
				guojiaName = "周";
				break;
			case 1:
				guojiaName = "齐";
				break;
			case 2:
				guojiaName = "楚";
				break;
			case 3:
				guojiaName = "燕";
				break;
			case 4:
				guojiaName = "韩";
				break;
			case 5:
				guojiaName = "赵";
				break;
			case 6:
				guojiaName = "魏";
				break;
			case 7:
				guojiaName = "秦";
				break;
			}
		}
		Set<String> rankList = Redis.getInstance().ztop(RankingMgr.LIANMENG_RANK + "_" + guojiaId, RankingMgr.RANK_MAXNUM);
		
		tableStart();
		trS();
		td("当前国家");td(guojiaName);
		trE();
		trS();
		td("名次");
		td("联盟名");
		td("等级");
		td("领土");
		td("人数/最大人数");
		td("cache member");
		td("cache score");
		trE();
		int rank = 0;
		try{
			for (String member : rankList) {
				AllianceBean alliance = HibernateUtil.find(AllianceBean.class, Integer.parseInt(member));
				if (alliance == null) {
					continue;
				}
				trS();
				// 去掉等级限制，否则会出排行榜数据显示排行和redis中数据不符合的bug
				if (rank <= RankingMgr.RANK_MAXNUM) {// 过滤筛选范围
					rank += 1;
					td(rank);
					td(alliance.name);
					td(alliance.level);
					td(AllianceMgr.inst.getCaptureCityCount(alliance.id));
					td(alliance.members +"/" + AllianceMgr.inst.getAllianceMemberMax(alliance.level));
					td(member);
					td(Redis.getInstance().zscore(RankingMgr.LIANMENG_RANK + "_" + guojiaId, member));
				}
				trE();
			}
			
		} catch(Exception e) {
			out.println("读取列表出错");
		}
		tableEnd();
	%>
</body>
</html>
