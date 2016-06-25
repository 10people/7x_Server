<%@page import="java.util.HashMap"%>
<%@page import="java.util.Collection"%>
<%@page import="com.qx.alliancefight.CampsiteInfo"%>
<%@page import="com.qx.world.FightScene"%>
<%@page import="com.qx.world.SceneMgr"%>
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
<a href="?act=broadcastBattleInfo&scId=0">广播阵地信息</a>
<a href="?act=castSkill&skillId=102">广播积分信息</a>
<a href="?act=castSkill&skillId=103">释放技能103</a>
<br/>
阵地信息:1 是复活点；2 是外层营地；3 是内层营地；4 是基地；5 是光墙。
占领  1时守方； 2是攻方。
<%
String act = request.getParameter("act");
String scId = request.getParameter("scId");
do{
	if(scId == null){
		break;
	}
	out("<a href='?scId="+scId+"'>刷新</a>");
	FightScene fs = BigSwitch.inst.scMgr.fightScenes.get(Integer.valueOf(scId));
	if(fs == null){
		out("not found:"+scId);
		break;
	}
	if("clearFenShenCD".equals(act)) {
		fs.fenShenCDMap = new HashMap();
	}else if("clearPrepareTime".equals(act)) {
		fs.prepareMS = 0;
		//
		/*
		int winLmId = fs.blueLmId;
		int gxAll = 999;
		int gxGJ = 10;
		//fs.cityId = 510302;//ming
		fs.cityId = 510110;//ye
		fs.makeLMAward(winLmId, fs.redLmId, gxAll, gxGJ);
		fs.makeLMAward(winLmId, fs.blueLmId, gxAll, gxGJ);
		//
		winLmId = fs.redLmId;
		fs.makeLMAward(winLmId, fs.redLmId, gxAll, gxGJ);
		fs.makeLMAward(winLmId, fs.blueLmId, gxAll, gxGJ);
		*/
	}
	br();
	out("状态：(-1准备；10比赛中;500结束;)"+fs.step);br();
	out("倒计时秒："+(fs.fightEndTime-System.currentTimeMillis())/1000);br();
	out("胜负：1守；2攻;0比赛中--"+fs.winSide);
	Collection<CampsiteInfo> list = fs.campsiteInfoList.values();
	tableStart();
	ths("id,攻占值,调整数值,临界值,占领方");
	String dir = request.getParameter("v");
	String sIdStr = request.getParameter("sId");
	int sId = sIdStr == null ? 0 : Integer.parseInt(sIdStr);
	for(CampsiteInfo info : list){
		if("moveCusor".equals(act) && info.id==sId) {
			if("L".equals(dir)){
				info.cursorPos-=1;
				info.cursorDir=1;
				//info.cursorDir-=1;
			}else{
				info.cursorPos+=1;
				info.cursorDir=2;
				//info.cursorDir+=1;
			}
		}
		trS();
		td(info.id);
		td(info.cursorPos);
		td("<a href='?scId="+scId+"&act=moveCusor&v=L&sId="+info.id+"'>左</a>-<a href='?scId="+scId+"&act=moveCusor&v=R&sId="+info.id+"'>右</a>");
		td(info.zhanlingzhiMax);
		td(info.curHoldValue);
		trE();
	}
	tableEnd();
}while(false);
%>
<br/>
<%
	if("openCdtime".equals(act)) {
		BigSwitch.inst.cdTimeMgr.setOpenCdTimeMgr(true);
		BigSwitch.inst.cdTimeMgr.start();
	} else if("fixTakeTowerSpeed".equals(act)) {
		FightScene.fixTakeTowerSpeed = Integer.parseInt(request.getParameter("v"));
		FightScene.fenShenCD = Integer.parseInt(request.getParameter("fenShenCD"));
		FightScene.dayFreeFuHuoTimes = Integer.parseInt(request.getParameter("dayFreeFuHuoTimes"));
	} else if("updateState".equals(act)) {
	} else if("updateDayState".equals(act)) {
	} else if("broadcastBattleInfo".equals(act)) {
		BigSwitch.inst.scMgr.fightScenes.get(0).broadcastBattleInfo();
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
<a href='?scId=<%=scId %>&act=clearPrepareTime'>结束准备时间</a>--
<a href='?scId=<%=scId %>&act=clearFenShenCD'>清空分身CD</a>--
<br/>
<br/>
<form action="allianceFight.jsp">
		额外增加占塔速度<input type="text" name="v" value="<%=FightScene.fixTakeTowerSpeed%>"/>
<br/>		分身CD<input type="text" name="fenShenCD" value="<%=FightScene.fenShenCD%>"/>
<br/>		原地复活次数<input type="text" name="dayFreeFuHuoTimes" value="<%=FightScene.dayFreeFuHuoTimes%>"/>
		<input type="hidden" name="act" value="fixTakeTowerSpeed"/>
		<input type="hidden" name="scId" value="<%=scId%>"/>
<br/>		<input type="submit" value="修改"/>
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