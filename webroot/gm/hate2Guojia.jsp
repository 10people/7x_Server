<%@page import="com.qx.quartz.job.GuojiaSetDiDuiGuoJob"%>
<%@page import="com.qx.quartz.job.GuojiaChouhenJieSuanJob"%>
<%@page import="com.qx.guojia.GuoJiaMgr"%>
<%@page import="com.qx.guojia.GuoJiaBean"%>
<%@page import="java.util.List"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
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
	location.href = '?action='+act;
}

</script>
<title>Insert title here</title>
</head>
<body>
<%
		List<GuoJiaBean> gjList = HibernateUtil.list(GuoJiaBean.class,"where guoJiaId<10");
		List<GuoJiaBean> gjList1 = HibernateUtil.list(GuoJiaBean.class,"where guoJiaId>10 and guoJiaId<20");
		List<GuoJiaBean> gjList2 = HibernateUtil.list(GuoJiaBean.class,"where  guoJiaId>20 and guoJiaId<30");
		List<GuoJiaBean> gjList3 = HibernateUtil.list(GuoJiaBean.class,"where  guoJiaId>30");
		String action = request.getParameter("action");
		if("settleHate".equals(action)){
			GuojiaSetDiDuiGuoJob job=new GuojiaSetDiDuiGuoJob();
			job.execute(null);
		}
		tableStart();
		trS();
		out("<td colspan='9'><input type='button' style='color:red;'   value='结算仇恨' onclick='go(\"settleHate\")'/></td>");
		trE();
		trS();
		td("国家");
		td("对"+HeroService.getNameById("1")+"仇恨");
		td("对"+HeroService.getNameById("2")+"仇恨");
		td("对"+HeroService.getNameById("3")+"仇恨");
		td("对"+HeroService.getNameById("4")+"仇恨");
		td("对"+HeroService.getNameById("5")+"仇恨");
		td("对"+HeroService.getNameById("6")+"仇恨");
		td("对"+HeroService.getNameById("7")+"仇恨");
		td("敌对国");
		trE();
		trS();
		out("<td colspan='9'>本期仇恨</td>");
		trE();
		for (GuoJiaBean gjBean:gjList) {
			trS();
			td(HeroService.getNameById(""+gjBean.guoJiaId));
			td(gjBean.hate_1);
			td(gjBean.hate_2);
			td(gjBean.hate_3);
			td(gjBean.hate_4);
			td(gjBean.hate_5);
			td(gjBean.hate_6);
			td(gjBean.hate_7);
			td(HeroService.getNameById(""+gjBean.diDuiGuo_1)+";"+HeroService.getNameById(""+gjBean.diDuiGuo_2));
			trE();
		}
		trS();
		out("<td colspan='9'>上1期仇恨</td>");
		trE();
		for (GuoJiaBean gjBean:gjList1) {
			trS();
			td(HeroService.getNameById(""+(gjBean.guoJiaId-10)));
			td(gjBean.hate_1);
			td(gjBean.hate_2);
			td(gjBean.hate_3);
			td(gjBean.hate_4);
			td(gjBean.hate_5);
			td(gjBean.hate_6);
			td(gjBean.hate_7);
			td(HeroService.getNameById(""+(gjBean.diDuiGuo_1))+";"+HeroService.getNameById(""+(gjBean.diDuiGuo_2)));
			trE();
		}
		trS();
		out("<td colspan='9'>上2期仇恨</td>");
		trE();
		for (GuoJiaBean gjBean:gjList2) {
			trS();
			td(HeroService.getNameById(""+(gjBean.guoJiaId-20)));
			td(gjBean.hate_1);
			td(gjBean.hate_2);
			td(gjBean.hate_3);
			td(gjBean.hate_4);
			td(gjBean.hate_5);
			td(gjBean.hate_6);
			td(gjBean.hate_7);
			td(HeroService.getNameById(""+(gjBean.diDuiGuo_1))+";"+HeroService.getNameById(""+(gjBean.diDuiGuo_2)));
			trE();
		}
		trS();
		out("<td colspan='9'>上3期仇恨</td>");
		trE();
		for (GuoJiaBean gjBean:gjList3) {
			trS();
			td(HeroService.getNameById(""+(gjBean.guoJiaId-30)));
			td(gjBean.hate_1);
			td(gjBean.hate_2);
			td(gjBean.hate_3);
			td(gjBean.hate_4);
			td(gjBean.hate_5);
			td(gjBean.hate_6);
			td(gjBean.hate_7);
			td(HeroService.getNameById(""+(gjBean.diDuiGuo_1))+";"+HeroService.getNameById(""+(gjBean.diDuiGuo_2)));
			trE();
		}
		tableEnd();
%>
	
</body>
</html>