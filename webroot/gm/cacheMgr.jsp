<%@page import="com.qx.account.FunctionOpenMgr"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="com.manu.network.SessionUser"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.manu.network.PD"%>
<%@page import="java.util.List"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="java.util.Set"%>
<%@page import="com.manu.dynasty.store.MemcachedCRUD"%>
<%@page import="com.qx.huangye.HYMgr"%>
<%@page import="com.qx.pvp.PvpBean"%>
<%@page import="com.qx.huangye.HYRewardStore"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.persistent.MC"%>
<%@page import="qxmobile.protobuf.HuangYeProtos.ReqRewardStore"%>
<%@page import="com.qx.account.Account"%>
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
<title>Insert title here</title>
</head>
<body>
	<%
			MC.delete(PvpBean.class, 28671001);
	/*
		List<PvpBean> pvpBeanList = HibernateUtil.list(PvpBean.class, "");
		for(PvpBean bean : pvpBeanList) {
			MC.delete(PvpBean.class, bean.getIdentifier());
		}
		for(PvpBean bean : pvpBeanList) {
			HibernateUtil.find(PvpBean.class, bean.junZhuId);
		}
		for(PvpBean bean : pvpBeanList) {
			HibernateUtil.find(PvpBean.class, bean.junZhuId);
		}
	*/
	%>
		
  </body>
</html>