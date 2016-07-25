<%@page import="com.qx.pve.JunzhuPveInfo"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.qx.pve.PveRecord"%>
<%@page import="org.hibernate.transform.Transformers"%>
<%@page import="com.qx.world.FightScene"%>
<%@page import="com.qx.util.RandomUtil"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="qxmobile.protobuf.AllianceFightProtos.PlayerScore"%>
<%@page import="org.hibernate.dialect.MySQL5Dialect"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@ page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.Map"%>
<%@include file="/myFuns.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%
setOut(out);
%>
	<div class="junZhuPveStarCount">
		<form action="">
			<input type="hidden" value ="reset" name ="countPveInfo">
			<button type="submit">计算君主过关信息</button>
		</form>
	</div>
	<%
	String countPveInfo = request.getParameter("countPveInfo");
	if(null == countPveInfo || "".equals(countPveInfo)){
		return;
	}
	List<JunZhu> list = HibernateUtil.list(JunZhu.class, "where 1 = 1");
	long count = list.size();
    for(JunZhu a:list){
      List<PveRecord> pveList = HibernateUtil.list(PveRecord.class,"where uid="+a.id+" order by guanQiaId DESC");
      if (null == pveList || pveList.size()<1){
    	  count--;
    	  continue;
      }
      JunzhuPveInfo jzPveInfo = HibernateUtil.find(JunzhuPveInfo.class,a.id);
      if(null == jzPveInfo){
    	  jzPveInfo = new JunzhuPveInfo(a.id);
      }
      int starCount = 0;
      int ptMax = 0;
      int cqMax = 0;
      starCount=BigSwitch.inst.pveGuanQiaMgr.getAllGuanQiaStartSum(a.id);
	  for(PveRecord pve:pveList){
		  // 计算总星数和最高传奇关卡最高普通关卡
				ptMax = Math.max(ptMax, pve.guanQiaId);
				// 普通关卡
				if(pve.chuanQiPass){
				// 传奇关卡
					cqMax = Math.max(cqMax, pve.guanQiaId);
				}
	}
	if(ptMax == jzPveInfo.commonChptMaxId && cqMax == jzPveInfo.legendChptMaxId && starCount == jzPveInfo.starCount)  
	{
		count --;
		continue;
	}
	jzPveInfo.commonChptMaxId = ptMax;
	jzPveInfo.legendChptMaxId = cqMax;
	jzPveInfo.starCount = starCount;
	HibernateUtil.save(jzPveInfo);
}
     out("总共 修改记录"+count+"条");
%>
</body>
</html>