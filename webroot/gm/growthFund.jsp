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
    <%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>成长基金</title>

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
	<strong>修改购买状态</strong>
    <form>
		<p>
			<input type="hidden" name="action" value="setBuyStatus"/>
			状态<select name="buystatus">
				<option value="1">购买</option>
				<option value="0">没有购买</option>
			</select>
			君主Id<input type="text" name="jzId" value=""/>
			<button type="submit">修改</button>
		</p>
	</form>
	 <%
 	 String action = request.getParameter("action");
	 if("jijinStatus".equals(action)){
		 long jzid = Integer.parseInt(request.getParameter("jzId"));
		 GrowthFundBuyBean gBuyBean = HibernateUtil.find(GrowthFundBuyBean.class,jzid);
		 JunZhu junZhu = HibernateUtil.find(JunZhu.class,jzid);
	    	tableStart();
	    		trS();
	    			ths("成长基金购买状态");
	    		trE();
	    		trS();
	    			if(gBuyBean == null){
	    				td("没有购买");
	    			}else {
	    				td("已经购买");
	    			}
	    		trE();
	    	tableEnd();
	    if(gBuyBean != null){
	    	List<ChengZhangJiJin> list = TempletService.getInstance().listAll(ChengZhangJiJin.class.getSimpleName());
	    	tableStart();
	    		trS();
	    			ths("等级奖励");
	    			ths("完成进度");
	    			ths("最大进度");
	    			ths("奖励状态");
	    		trE();
	    		for(ChengZhangJiJin chengZhangJiJin:list){
	    			trS();
	    			td("角色达到" + chengZhangJiJin.level);
	    			td(junZhu.level);
	    			td(chengZhangJiJin.level);
	    			GrowthFundBean gBean = HibernateUtil.find(GrowthFundBean.class,"where jzId=" + jzid + " and level=" + chengZhangJiJin.level);
	    			if(gBean == null){
	    				td("未领取");
	    			}else{
	    				td("已领取");
	    			}
	    			trE();
	    		}
	    	tableEnd();
	    }
	 }else if("setBuyStatus".equals(action)){
		 long jzid = Integer.parseInt(request.getParameter("jzId"));
		 JunZhu junZhu = HibernateUtil.find(JunZhu.class,jzid);
		 String buystatus = request.getParameter("buystatus");
		 if(junZhu != null){
			 if("0".equals(buystatus)){
				 GrowthFundBuyBean gBuyBean = HibernateUtil.find(GrowthFundBuyBean.class,jzid);
				 if(gBuyBean != null){
					 HibernateUtil.delete(gBuyBean);
				 }
			 }else{
				 GrowthFundBuyBean gBuyBean = HibernateUtil.find(GrowthFundBuyBean.class,jzid);
				 if(gBuyBean == null){
					 gBuyBean = new GrowthFundBuyBean();
					 gBuyBean.buyTime = new Date();
					 gBuyBean.jzId = jzid;
					 HibernateUtil.insert(gBuyBean);
				 }
			 }
		 }
	 }
    %>
</body>
</html>