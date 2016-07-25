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
	<input id="resetChongLouRank" type="button" value="重置 重楼排行榜" onclick="go('resetChongLouRank')" />
	<font color="red">不需要重置重楼排行榜不要点</font>
	<br /><br />
	<p>
		<a href="rank4ChongLou.jsp?guojia=0">刷新</a>
		<a href="rank4ChongLou.jsp?guojia=0">周</a>
		<a href="rank4ChongLou.jsp?guojia=1">齐</a>
		<a href="rank4ChongLou.jsp?guojia=2">楚</a>
		<a href="rank4ChongLou.jsp?guojia=3">燕</a>
		<a href="rank4ChongLou.jsp?guojia=4">韩</a>
		<a href="rank4ChongLou.jsp?guojia=5">赵</a>
		<a href="rank4ChongLou.jsp?guojia=6">魏</a>
		<a href="rank4ChongLou.jsp?guojia=7">秦</a>
	</p>

	<%
		String action = request.getParameter("action");
		if(action != null) {
			if("resetChongLouRank".equals(action)) {
				for(int i = 0; i <= 7; i++) {
					Redis.getInstance().del(RankingMgr.CHONGLOU_RANK + "_" + i);
				}
				RankingMgr.inst.loadChongLouRank();
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
		Set<String> rankList = Redis.getInstance().ztop(RankingMgr.CHONGLOU_RANK + "_" + guojiaId, RankingMgr.RANK_MAXNUM);
		
		tableStart();
		trS();
		td("当前国家");td(guojiaName);
		trE();
		trS();
		td("名次");
		td("君主名");
		td("等级");
		td("最高重楼层");
		td("时间");
		td("cache member");
		td("cache score");
		trE();
		int rank = 0;
		try{
			for (String member : rankList) {
				JunZhu jz = HibernateUtil.find(JunZhu.class, Long.parseLong(member));
				if (jz == null) {
					continue;
				}
				trS();
				ChongLouRecord chongLouRecord = ChongLouMgr.inst.getChongLouRecord(jz.id);
				// 去掉等级限制，否则会出排行榜数据显示排行和redis中数据不符合的bug
				if (rank <= RankingMgr.RANK_MAXNUM) {// 过滤筛选范围
					rank += 1;
					td(rank);
					td(jz.name);
					td(jz.level);
					td(chongLouRecord.highestLevel);
					td(DateUtils.date2Text(chongLouRecord.highestLevelFirstTime, "yyyy-MM-dd HH:mm:ss"));
					td(member);
					td(Redis.getInstance().zscore(RankingMgr.CHONGLOU_RANK + "_" + guojiaId, member));
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
