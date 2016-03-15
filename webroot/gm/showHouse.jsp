<%@page import="qxmobile.protobuf.House.HouseExpInfo"%>
<%@page import="com.qx.alliance.HouseBean"%>
<%@page import="com.qx.ranking.RankingGongJinMgr"%>
<%@page import="com.manu.dynasty.template.CanShu"%>
<%@page import="com.qx.guojia.GuoJiaMgr"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="com.qx.guojia.ResourceGongJin"%>
<%@page import="com.qx.activity.XianShiActivityMgr"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="com.qx.yuanbao.YBType"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.qx.junzhu.PlayerTime"%>
<%@page import="com.qx.world.PosInfo"%>
<%@page import="com.qx.yuanbao.YuanBaoMgr"%>
<%@page import="com.qx.vip.PlayerVipInfo"%>
<%@page import="com.qx.vip.VipMgr"%>
<%@page import="qxmobile.protobuf.JunZhuProto.BuyTimesInfo"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="com.qx.account.FunctionOpenMgr"%>
<%@page import="com.manu.network.SessionUser"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.qx.pve.PveMgr"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.manu.dynasty.template.ExpTemp"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.purchase.XiLian"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.qx.timeworker.TimeWorkerMgr"%>
<%@page import="com.manu.dynasty.template.FunctionOpen"%>
<%@page import="com.qx.alliance.AlliancePlayer"%>
<%@page import="qxmobile.protobuf.TimeWorkerProtos.TimeWorkerResponse"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.qx.purchase.PurchaseMgr"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript">
function go(act){
	var v = document.getElementById(act).value;
	location.href = '?action='+act+"&v="+v;
}

</script>
</head>
<body>
<%
setOut(out);
Logger log = LoggerFactory.getLogger(GuoJiaMgr.class);
String name = request.getParameter("account");
name = name == null ? "": name.trim();
String accIdStr = request.getParameter("accId");// 用户id
accIdStr = (accIdStr == null ? "":accIdStr.trim());
if(session.getAttribute("name") != null && name.length()==0 && accIdStr.length()==0){
	name = (String)session.getAttribute("name");
}
%>
  	<form action="">
	  	账号<input type="text" name="account" value="<%=name%>">&nbsp;或&nbsp;
	  	君主ID<input type="text" name="accId" value="<%=accIdStr%>">
	  	<button type="submit">查询</button>
	</form>
<%
	Account account = null;
	if(name != null && name.length()>0){
		account = HibernateUtil.getAccount(name);
	}else if(accIdStr.length()>0){
		account = HibernateUtil.find(Account.class, (Long.valueOf(accIdStr) - GameServer.serverId) / 1000);
		if(account != null)name = account.getAccountName();
	}
do{
	if(account == null){
		out("没有找到");
		break;
	}
	session.setAttribute("name", name);
	long jzId = account.getAccountId() * 1000 + GameServer.serverId;
	JunZhu junzhu = HibernateUtil.find(JunZhu.class, jzId);
	if(junzhu == null){
		out.println("没有君主");
		break;
	}
	HouseBean hsBean = HibernateUtil.find(HouseBean.class, jzId);
	if(hsBean!=null){
	 String action = request.getParameter("action");
	 boolean sendInfo = true;
	 if("upHouseExp".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 System.out.println(v);
		HouseBean bean = HibernateUtil.find(HouseBean.class, jzId);
		bean.cunchuExp=v;
		HibernateUtil.save(bean);
	 }
		int v = 0;
		HouseBean hsBean2 = HibernateUtil.find(HouseBean.class, jzId);
		HouseExpInfo.Builder expInfo = HouseExpInfo.newBuilder();
		expInfo =BigSwitch.inst.houseMgr.makeHouseExpInfo(hsBean2);
		tableStart();
		trS();
		td("经验");
		td("<input type='text' id='upHouseExp' value='"
				+expInfo.getCur()
				+ "'/><input type='button' value='修改' onclick='go(\"upHouseExp\")'/><br/>");
		trE();
		trS();
		td("经验上限");
		td("<input type='text'  value='"
				+expInfo.getMax()
				+ "'/><br/>");
		trE();
		trS();
		td("存储经验");
		td("<input type='text'  value='"
				+hsBean2.cunchuExp
				+ "'/><br/>");
		trE();
		tableEnd();
	}else{
		out("当前君主房屋未开启"+jzId);
	}
}while(false);
%>