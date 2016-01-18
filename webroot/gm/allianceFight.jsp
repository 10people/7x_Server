<%@page import="qxmobile.protobuf.AllianceFightProtos.FightAttackReq"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.qx.alliancefight.AllianceFightMgr"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.qx.alliancefight.CdTimeMgr"%>
<%@page import="com.qx.alliancefight.AllianceFightMatch"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.alliancefight.LMZBaoMingBean"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<script type="text/javascript">
	function go(act){
		var v = document.getElementById(act).value;
		location.href = '?action='+act+"&v="+v;
	}
</script>
</head>
<body>
联盟战复活管理：状态-<%=BigSwitch.inst.cdTimeMgr.isOpenCdTimeMgr()%>__<a href="?act=openCdtime">开启</a><br/>
<a href="?act=castSkill&skillId=101">释放技能101</a>
<a href="?act=castSkill&skillId=102">释放技能102</a>
<a href="?act=castSkill&skillId=103">释放技能103</a>
<br/>
联盟战匹配：<a href="?act=fightMatch">开始匹配</a>
<%
	String act = request.getParameter("act");
	if("openCdtime".equals(act)) {
		BigSwitch.inst.cdTimeMgr.setOpenCdTimeMgr(true);
		BigSwitch.inst.cdTimeMgr.start();
	} else if("fightMatch".equals(act)) {
		boolean isClear = HibernateUtil.clearTable(AllianceFightMatch.class);
		if(!isClear) {
			out.print("战斗匹配失败");
		} else{
			BigSwitch.inst.allianceFightMgr.matchFight();
			out.print("战斗匹配成功！");
		}
	} else if("updateState".equals(act)) {
		String curState = request.getParameter("curState");
		if(curState == null || curState.equals("")) {
			return;			
		}
		BigSwitch.inst.allianceFightMgr.fightState = Integer.parseInt(curState); 
	} else if("updateDayState".equals(act)) {
		String curState = request.getParameter("dayState");
		if(curState == null || curState.equals("")) {
			return;			
		}
		BigSwitch.inst.allianceFightMgr.dayFightState = Integer.parseInt(curState); 
	} else if("castSkill".equals(act)) {
		String skillIdStr = request.getParameter("skillId");
		int skillId = Integer.parseInt(skillIdStr);
		IoSession cliSession = SessionManager.inst.getIoSession(27002L);
		FightAttackReq.Builder cliRequest = FightAttackReq.newBuilder();
		if(skillId == 101) {
	   	} else if(skillId == 102) {
	   	}
		cliRequest.setSkillId(skillId);
		BigSwitch.inst.allianceFightMgr.activeFight(1, cliSession, cliRequest);
		
	}
%>

<%
List<AllianceFightMatch> matchlist = HibernateUtil.list(AllianceFightMatch.class, "");
out("数量:"+matchlist.size());br();
tableStart();
ths("id,联盟id1,,联盟id2,赛程");
for(AllianceFightMatch bean : matchlist){
	trS();
	td(bean.id); td(bean.allianceId1); td("vs"); td(bean.allianceId2); td(bean.battleRound);
	trE();
}
tableEnd();
%>
<br/>
<br/>
联盟战状态（赛程，0-无，1-32强，2-16强，3-8强，4-4强，5-半决赛，6-三四名比赛，7-决赛，8-报名）：<br/>
当前状态：<%=BigSwitch.inst.allianceFightMgr.fightState %>
<form action="allianceFight.jsp">
		<input type="text" name="curState"/>
		<input type="hidden" name="act" value="updateState"/>
		<input type="submit" value="修改"/>
</form>
<br/>
<br/>
今日联盟战状态（是指今日有赛程，0-未开始，1-正在进行中，2-已经结束）：<br/>
当前状态：<%=BigSwitch.inst.allianceFightMgr.dayFightState %>
<form action="allianceFight.jsp">
		<input type="text" name="dayState"/>
		<input type="hidden" name="act" value="updateDayState"/>
		<input type="submit" value="修改"/>
</form>
<br/>
<br/>
已报名联盟列表
<%
List<LMZBaoMingBean> list = HibernateUtil.list(LMZBaoMingBean.class, "");
out("数量:"+list.size());br();
tableStart();
ths("联盟id,联盟名称,报名人id,报名人名称,报名时间,第几届");
for(LMZBaoMingBean bean : list){
	trS();
	td(bean.lmId); td(bean.lmName); td(bean.mengZhuId); td(bean.mengZhuName); td(bean.baoMingTime);
	td(bean.season);
	trE();
}
tableEnd();
%>
<br/>
赛程表
<%
List<AllianceFightMatch> listM = HibernateUtil.list(AllianceFightMatch.class, "");
out("数量:"+list.size());br();
tableStart();
ths("联盟id,vs,联盟id");
for(AllianceFightMatch bean : listM){
	trS();
	td(bean.allianceId1);
	td("VS");
	td(bean.allianceId2);
	trE();
}
tableEnd();
%>
	<a href="allianceFightRules.jsp">联盟战规则</a>
</body>
</html>