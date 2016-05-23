<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.activity.*"%>
<%@page import="java.util.*"%>
<%@page import="com.manu.dynasty.template.*"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.qx.junzhu.JunZhu"%>
    <%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>冲级活动</title>

</head>
<body>
	 <strong>查询基金状态</strong>
    <form>
		<p>
			<input type="hidden" name="action" value="jijinStatus"/>
			君主Id<input type="text" name="jzId" value=""/>
			<button type="submit">查询</button>
		</p>
	</form>
	<br>
		<form action="" method="post">
			<input type="hidden" name="action" value="cleardata"/>
			<input type="submit" value="清除领奖记录"/>
		</form>
		<br>
	 <%
 	 String action = request.getParameter("action");
	 if("jijinStatus".equals(action)){
		 if(request.getParameter("jzId") == ""){
			 redirect("levelgift.jsp");
		 }
		 long jzid = Integer.parseInt(request.getParameter("jzId"));
		 JunZhu junZhu = HibernateUtil.find(JunZhu.class,jzid);
    	List<XianshiHuodong> list = TempletService.getInstance().listAll(XianshiHuodong.class.getSimpleName());
    	tableStart();
    		trS();
    			ths("等级奖励");
    			ths("完成进度");
    			ths("最大进度");
    			ths("奖励状态");
    		trE();
    		for(XianshiHuodong xianshiHuodong:list){
    			if(xianshiHuodong.getDoneType() != 1) continue;
    			trS();
    			td("君主达到" + xianshiHuodong.getDoneCondition());
    			td(junZhu.level);
    			td(xianshiHuodong.getDoneCondition());
    			LevelUpGiftBean gBean = HibernateUtil.find(LevelUpGiftBean.class,"where jzId=" + jzid + " and level=" + xianshiHuodong.getDoneCondition());
    			if(gBean == null){
    				td("未领取");
    			}else{
    				td("已领取");
    			}
    			trE();
    		}
    	tableEnd();
	 }else if("cleardata".equals(action)){
			String sql = "DELETE FROM "+LevelUpGiftBean.class.getSimpleName();
			HibernateUtil.executeSql(sql);
			alert(sql);
	}
    %>
</body>
</html>