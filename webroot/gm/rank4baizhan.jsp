<%@page import="com.qx.huangye.shop.ShopMgr"%>
<%@page import="com.qx.pvp.PvpBean"%>
<%@page import="com.qx.pvp.PvpMgr"%>
<%@page import="java.util.Set"%>
<%@page import="qxmobile.protobuf.Ranking"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="com.qx.ranking.RankingMgr"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="qxmobile.protobuf.Ranking.BaiZhanInfo"%>
<%@page import="java.util.List"%>
<%@page import="qxmobile.protobuf.Ranking.RankingResp"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="qxmobile.protobuf.Ranking.RankingReq"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="org.apache.mina.core.session.IoSession"%>
<%@include file="/myFuns.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script type="text/javascript">
	function go(act) {
		var node = document.getElementById(act);
		if (node.value != null) {
			location.href = '?action=' + act + "&v=" + node.value;
		} else {
			location.href = '?action=' + act;
		}
	}
</script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>

	<input id="resetBaizhanRank" type="button" value="重置 百战排行榜" onclick="go('resetBaizhanRank')" />
	<font color="red">不需要重置百战排行榜不要点</font>
	<br />
	<br />
	
	<p>
		<a href="rank4baizhan.jsp?guojia=0">刷新</a>
		<a href="rank4baizhan.jsp?guojia=0">周</a>
		<a href="rank4baizhan.jsp?guojia=1">齐</a>
		<a href="rank4baizhan.jsp?guojia=2">楚</a>
		<a href="rank4baizhan.jsp?guojia=3">燕</a>
		<a href="rank4baizhan.jsp?guojia=4">韩</a>
		<a href="rank4baizhan.jsp?guojia=5">赵</a>
		<a href="rank4baizhan.jsp?guojia=6">魏</a>
		<a href="rank4baizhan.jsp?guojia=7">秦</a>
	</p>

	<%
		String action = request.getParameter("action");
		if(action != null && action.equals("resetBaizhanRank")) {
			for(int i = 0; i <= 7; i++) {
				Redis.getInstance().del(RankingMgr.BAIZHAN_RANK + "_" + i);
			}
			RankingMgr.inst.loadBaizhanRank();
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
		Set<String> rankList = Redis.getInstance().zrange(RankingMgr.BAIZHAN_RANK + "_" + guojiaId);
		
		tableStart();
		trS();
		td("当前国家");td(guojiaName);
		trE();
		trS();
		td("名次");
		td("君主名");
		td("军衔");
		td("百战排名");
		td("胜场");
		td("威望");
		trE();
		int rank = 0;
		for (String member : rankList) {
			long jzId = Long.parseLong(member);
			JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
			if(jz == null){
				continue;
			}
			rank += 1;
			trS();
			td(rank);
			td(jz.name);
			td(PvpMgr.inst.getJunXianName(jz.id));
			td(PvpMgr.inst.getPvpRankById(jzId));
			PvpBean bean = HibernateUtil.find(PvpBean.class, jzId);
			if(bean!=null){
				td(bean.allWin);
			} else {
				td(0);
			}
			int wei = ShopMgr.inst.getMoney(ShopMgr.Money.weiWang, jz.id, null);
			td(wei);
			trE();
		}
		tableEnd();
	%>


</body>
</html>