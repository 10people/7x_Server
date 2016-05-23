<%@page import="com.qx.alliance.AllianceMgr"%>
<%@page import="org.apache.taglibs.standard.tag.common.core.ForEachSupport"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.manu.dynasty.template.JCZCity" %>
<%@page import="com.qx.alliancefight.*" %>
<%@page import="com.manu.dynasty.util.DateUtils" %>
<%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%int cityId = Integer.parseInt(request.getParameter("cityId")); %>
	<strong>城池ID[<%=cityId %>]竞拍信息</strong>
	<%
		Calendar calendar = Calendar.getInstance();
		Date startdate = DateUtils.getDayStart(new java.sql.Timestamp(calendar.getTimeInMillis()));
		Date enddate = DateUtils.getDayEnd(new java.sql.Timestamp(calendar.getTimeInMillis()));
		List<BidBean> bidList = HibernateUtil.list(BidBean.class, "where cityId=" + cityId + " and bidTime>'" + startdate + "' and bidTime<'" + enddate +"'");
		if(bidList==null || bidList.size() <= 0){
			out.write("<br><p style='color:red;'>没有竞拍信息</p>");
		}else{
			tableStart();
			trS();
				ths("竞拍记录ID");
				ths("城池ID");
				ths("宣战联盟Id");
				ths("宣战联盟名字");
				ths("竞拍真实价格");
				ths("竞拍缓存价格");
				ths("竞拍时间");
			trE();
			for(BidBean bidBean:bidList){
				String alanceName = AllianceMgr.inst.getAllianceName(bidBean.lmId);
				trS();
					td(bidBean.dbId);
					td(bidBean.cityId);
					td(bidBean.lmId);
					td(alanceName);
					td(bidBean.priceReal);
					td(bidBean.priceCache);
					td(DateUtils.formatDateTime(bidBean.bidTime,"yyyy/MM/dd HH:MM:ss"));
				trE();
			}
			
		tableEnd();
		}
	%>
</body>
</html>