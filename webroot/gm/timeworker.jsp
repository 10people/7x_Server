<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.manu.dynasty.template.ExpTemp"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.qx.timeworker.TimeWorkerMgr"%>
<%@page import="qxmobile.protobuf.TimeWorkerProtos.TimeWorkerResponse"%>
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
	String name = request.getParameter("account");
name = name == null ? "": name;
if(session.getAttribute("name") != null && name.length()==0){
	name = (String)session.getAttribute("name");
}
%>
  <form action="">
	  	账号<input type="text" name="account" value="<%=name%>"><br>
	  	<button type="submit">查询</button>
	  </form>
<%
	if(name != null && name.length()>0){
	Account account = HibernateUtil.getAccount(name);
	if(account == null){
%>没有找到<%
	//HibernateUtil.saveAccount(name);
	}else{
		session.setAttribute("name", name);
%>账号<%=account.getAccountId()%>:<%=account.getAccountName()%><%
	JunZhu junzhu = HibernateUtil.find(JunZhu.class, (long)account.getAccountId());
		 if(junzhu == null){
	 out.println("没有君主");
		 }else{
	 if(junzhu.level == 0 || junzhu.shengMingMax == 0){
		 JunZhuMgr.inst.fixCreateJunZhu((int)junzhu.id, junzhu.name, junzhu.roleId, junzhu.guoJiaId);
	 }
	 String action = request.getParameter("action");
	 if("addTiLiInterval6".equals(action)){
		 TimeWorkerMgr.instance.addTiLi(junzhu, null);
	 }else if("equipXilian".equals(action)){
		 TimeWorkerMgr.instance.subFreeXilianTimes(junzhu.id, 1);
	 }else if("addXilianTimes".equals(action)){
		 TimeWorkerMgr.instance.addEquipFreeXilianTimes(junzhu);
	 }
	 JunZhuMgr.inst.calcJunZhuTotalAtt(junzhu);
	 out.println("&nbsp;君主id："+junzhu.id);out.println("<br/>");
	 ExpTemp expTemp = TempletService.getInstance().getExpTemp(1, junzhu.level);
	 out.println("等级："+junzhu.level+"<br/>");
	 int v = 0;
	 if(expTemp != null)v =expTemp.getNeedExp();
	 String input = request.getParameter("v");
	 if(input == null)input = "1";
	 out.println("体力："+junzhu.tiLi);out.println("<input type='text' id='addTiLi' value='"+input+"'/><input type='button' value='增加' onclick='go(\"addTiLi\")'/><br/>");//out.println("<a href='?action=addTiLi'>+100</a><br/>");
	 out.println("每一分钟增加一点体力："+junzhu.tiLi);out.println("<a href='?action=addTiLiInterval6'>+1</a><br/>");
	 
	 out.println("免费装备洗练次数：" + TimeWorkerMgr.instance.getXilianTimes(junzhu) +"，上限是10次");
	 out.println("<a href='?action=equipXilian'>免费洗练一次</a>");out.println("<br/>");
	 out.println("次数小于10时，1小时增加一次：");out.println("<a href='?action=addXilianTimes'>增加洗练次数</a><br/>");
	 /*
	 */
		 }
	}
}
%>
</body>
</html>