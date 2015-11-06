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
<%@page import="com.qx.battle.PveMgr"%>
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
function setLueduo(act){
	location.href = '?action='+act;
}

</script>
<title>Insert title here</title>
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
out("掠夺开启时间为"+CanShu.OPENTIME_LUEDUO+"到"+CanShu.CLOSETIME_LUEDUO);
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
	long junZhuId = account.getAccountId() * 1000 + GameServer.serverId;
	JunZhu junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
	if(junzhu == null){
		out.println("没有君主");
		break;
	}
	if(junzhu.level == 0 || junzhu.shengMingMax == 0){
		JunZhuMgr.inst.fixCreateJunZhu(junZhuId, junzhu.name, junzhu.roleId, junzhu.guoJiaId);
	}
	ResourceGongJin gjBean =HibernateUtil.find(ResourceGongJin.class, junzhu.id);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	if(gjBean!=null){
	 String action = request.getParameter("action");
	 boolean sendInfo = true;
	 if("upgongjin".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 log.info("修改君主{}的贡金为{}===》{}",gjBean.junzhuId,gjBean.gongJin,v);
		 gjBean.gongJin=v;
		 HibernateUtil.save(gjBean);
	 }
	 if("upjxTime".equals(action)){
			String time = request.getParameter("v");
			if (time != null) {
				log.info("修改君主{}的今日捐献时间为{}===》{}",gjBean.junzhuId,gjBean.juanXianTime,time);
				gjBean.juanXianTime = sdf.parse(time);
				HibernateUtil.save(gjBean);
			}
		}
		if ("uptodayJX".equals(action)) {
			int v = Integer.parseInt(request.getParameter("v"));
			log.info("修改君主{}的今日捐献贡金为{}===》{}",gjBean.junzhuId,gjBean.todayJX,v);
			gjBean.todayJX = v;
			HibernateUtil.save(gjBean);
		}
		if ("uptodayJXTimes".equals(action)) {
			int v = Integer.parseInt(request.getParameter("v"));
			log.info("修改君主{}的今日捐献贡金次数为{}===》{}",gjBean.junzhuId,gjBean.todayJXTimes,v);
			gjBean.todayJXTimes = v;
			HibernateUtil.save(gjBean);
		}
		if ("uplastJX".equals(action)) {
			int v = Integer.parseInt(request.getParameter("v"));
			log.info("修改君主{}的贡金为上次捐献贡金{}===》{}",gjBean.junzhuId,gjBean.lastJX,v);
			gjBean.lastJX = v;
			HibernateUtil.save(gjBean);
		}
		if ("uplastGetDayAwardTime".equals(action)) {
		
			String time = request.getParameter("v");
			log.info("修改君主{}的贡金为上次领取每日奖励时间{}===》{}",gjBean.junzhuId,gjBean.getDayAwardTime,time);
			if (time != null) {
				gjBean.getDayAwardTime = sdf.parse(time);
				HibernateUtil.save(gjBean);
			}
		}
		if ("upthisWeekJX".equals(action)) {
			int v = Integer.parseInt(request.getParameter("v"));
			log.info("修改君主{}的本周捐献贡金为{}===》{}",gjBean.junzhuId,gjBean.thisWeekJX,v);
			gjBean.thisWeekJX = v;
			HibernateUtil.save(gjBean);
		}
		if ("uplastWeekJX".equals(action)) {
			int v = Integer.parseInt(request.getParameter("v"));
			log.info("修改君主{}的上周捐献贡金为{}===》{}",gjBean.junzhuId,gjBean.lastWeekJX,v);
			gjBean.lastWeekJX = v;
			HibernateUtil.save(gjBean);
		}
		if ("uplastGetWeekAward".equals(action)) {
			String time = request.getParameter("v");
			log.info("修改君主{}的上周捐献贡金为{}===》{}",gjBean.junzhuId,gjBean.getWeekAwardTime,time);
			if (time != null) {
				gjBean.getWeekAwardTime =  sdf.parse(time);
				HibernateUtil.save(gjBean);
			}
		}
		if ("openShangjiao".equals(action)) {
			CanShu.OPENTIME_LUEDUO= "8:00";
			CanShu.CLOSETIME_LUEDUO="24:00";
			log.info("掠夺开启时间为"+CanShu.OPENTIME_LUEDUO+"到"+CanShu.CLOSETIME_LUEDUO);
			out("掠夺开启时间为"+CanShu.OPENTIME_LUEDUO+"到"+CanShu.CLOSETIME_LUEDUO);
		}
		if ("closeShangjiao".equals(action)) {
			CanShu.OPENTIME_LUEDUO="0:00" ;
			CanShu.CLOSETIME_LUEDUO="2:00";
			log.info("掠夺开启时间为"+CanShu.OPENTIME_LUEDUO+"到"+CanShu.CLOSETIME_LUEDUO);
			out("掠夺开启时间为"+CanShu.OPENTIME_LUEDUO+"到"+CanShu.CLOSETIME_LUEDUO);
		}
		int v = 0;
		tableStart();
		trS();
		td("贡金");
		td("<input type='text' id='upgongjin' value='"
				+ gjBean.gongJin
				+ "'/><input type='button' value='修改' onclick='go(\"upgongjin\")'/><br/>");
		trE();
		trS();
		td("捐献时间");
		td("<input type='text' id='upjxTime' value='"
				+ gjBean.juanXianTime
				+ "'/><input type='button' value='修改' onclick='go(\"upjxTime\")'/><br/>");
		trE();
		trS();
		td("本日捐献");
		td("<input type='text' id='uptodayJX' value='"
				+ gjBean.todayJX
				+ "'/><input type='button' value='修改' onclick='go(\"uptodayJX\")'/><br/>");
		trE();
		trS();
		td("本日捐献次数");
		td("<input type='text' id='uptodayJXTimes' value='"
				+ gjBean.todayJXTimes
				+ "'/><input type='button' value='修改' onclick='go(\"uptodayJXTimes\")'/><br/>");
		trE();
		trS();
		td("上次捐献");
		td("<input type='text' id='uplastJX' value='"
				+ gjBean.lastJX
				+ "'/><input type='button' value='修改' onclick='go(\"uplastJX\")'/><br/>");
		trE();
		
		trS();
		td("本周捐献");
		td("<input type='text' id='upthisWeekJX' value='"
				+ gjBean.thisWeekJX
				+ "'/><input type='button' value='修改' onclick='go(\"upthisWeekJX\")'/><br/>");
		trE();
		trS();
		td("上周捐献");
		td("<input type='text' id='uplastWeekJX' value='"
				+ gjBean.lastWeekJX
				+ "'/><input type='button' value='修改' onclick='go(\"uplastWeekJX\")'/><br/>");
		trE();
		trS();
		td("上次领取每日奖励时间");
		td("<input type='text' id='uplastGetDayAwardTime' value='"
				+ gjBean.getDayAwardTime
				+ "'/><input type='button' value='修改' onclick='go(\"uplastGetDayAwardTime\")'/><br/>");
		trE();
		trS();
		td("上次领取周奖励时间");
		td("<input type='text' id='uplastGetWeekAward' value='"
				+ gjBean.getWeekAwardTime
				+ "'/><input type='button' value='修改' onclick='go(\"uplastGetWeekAward\")'/><br/>");
		trE();
		trS();
		td("上缴开启关闭");
		td("'<input type='button' value='开启' onclick='setLueduo(\"openShangjiao\")'/><br/>");
		td("'<input type='button' value='关闭' onclick='setLueduo(\"closeShangjiao\")'/><br/>");
		trE();
		tableEnd();
	}else{
		out("当前君主国家贡金功能未开启");
	}
%>
	
	<%
}while(false);
%>
</body>
</html>