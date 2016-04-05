<%@page import="org.json.JSONArray"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.qx.alliance.building.JianZhuMgr"%>
<%@page import="com.qx.alliance.building.ChouJiangBean"%>
<%@page import="com.qx.alliance.FengShanMgr"%>
<%@page import="com.qx.alliance.FengshanBean"%>
<%@page import="java.util.Date"%>
<%@page import="com.qx.alliance.LmTuTeng"%>
<%@page import="com.qx.yuanbao.YBType"%>
<%@page import="com.qx.vip.VipMgr"%>
<%@page import="com.qx.vip.VipData"%>
<%@page import="qxmobile.protobuf.MoBaiProto.MoBaiInfo"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.qx.yuanbao.YuanBaoMgr"%>
<%@page import="com.qx.alliance.MoBaiMgr"%>
<%@page import="com.manu.dynasty.store.MemcachedCRUD"%>
<%@page import="com.qx.alliance.AlliancePlayer"%>
<%@page import="java.io.IOException"%>
<%@page import="java.io.Writer"%>
<%@page import="java.util.List"%>
<%@page import="com.manu.dynasty.template.LianmengMobai"%>
<%@page import="com.qx.alliance.MoBaiBean"%>
<%@page import="com.qx.account.FunctionOpenMgr"%>
<%@page import="com.manu.network.SessionUser"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.qx.pve.PveMgr"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.manu.dynasty.template.ExpTemp"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.qx.timeworker.TimeWorkerMgr"%>
<%@page import="qxmobile.protobuf.TimeWorkerProtos.TimeWorkerResponse"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.alliance.AllianceBean"%>
<%@page import=" com.qx.alliance.LMMoBaiInfo"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript">
function go(act){
	var v = "";
	var ele = document.getElementById(act);
	if(ele) v=ele.value;
	location.href = '?action='+act+"&v="+v;
}
</script>
<title>Insert title here</title>
</head>
<body>
<%!
int updateMobaiLevel(int lmId, int buffNum, Date time) {
	{//增加联盟累计膜拜次数
		LmTuTeng tt = HibernateUtil.find(LmTuTeng.class, lmId);
		if(tt == null){
			tt = new LmTuTeng();
			tt.lmId = lmId;
			tt.dTime = time;
			tt.times = buffNum;
			HibernateUtil.insert(tt);
		}else{
			tt.times+=buffNum;
			HibernateUtil.update(tt);
		}
		return tt.times;
	}
}
%>
<%
setOut(out);
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
	  	账号ID<input type="text" name="accId" value="<%=accIdStr%>">
	  	<button type="submit">查询</button>
	  </form>
<%
	Account account = null;
	if(name != null && name.length()>0){
		account = HibernateUtil.getAccount(name);
	}else if(accIdStr.length()>0){
		account = HibernateUtil.find(Account.class, Long.valueOf(accIdStr));
		if(account != null)name = account.getAccountName();
	}
	{
	if(account == null){
%>没有找到<%
	//HibernateUtil.saveAccount(name);
	}else{
		session.setAttribute("name", name);
%>账号<%=account.getAccountId()%>:<%=account.getAccountName()%><%
	long junZhuId = account.getAccountId() * 1000 + GameServer.serverId;
		JunZhu junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
		 AlliancePlayer member = null;
		 member = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
		 if(member == null || member.lianMengId<=0){
			 out("没有加入联盟");
			 return;
		 }
		 int bufferLevel = 0;
		 LmTuTeng tt = HibernateUtil.find(LmTuTeng.class, member.lianMengId);
		 if(tt!=null){
			 bufferLevel = tt.times;
		 }
		AllianceBean alliance = HibernateUtil.find(AllianceBean.class, member.lianMengId);
	 if(junzhu == null){
 		out.println("没有君主");
	 }else{
	 if(junzhu.level == 0 || junzhu.shengMingMax == 0){
		 JunZhuMgr.inst.fixCreateJunZhu(junZhuId, junzhu.name, junzhu.roleId, junzhu.guoJiaId);
			 
	 }
	 String action = request.getParameter("action");
	 boolean sendInfo = true;
	 if("resetAward".equals(action)){
		 if(member != null) {
			int maxJiBaiTimes = JianZhuMgr.inst.getMaxJiBaiTimes(member);
			ChouJiangBean bean = HibernateUtil.find(ChouJiangBean.class, junzhu.id);
			JSONArray json = JianZhuMgr.inst.fillChouJiangBean();
			bean.createTime = new Date();
			bean.str = json.toString();
			bean.todayUsedTimes = 0;
			bean.todayLeftTimes = maxJiBaiTimes;
			HibernateUtil.update(bean);
		 }
	 }else if("addTiLi".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 JunZhuMgr.inst.updateTiLi(junzhu, v, "jsp后台管理");
		 HibernateUtil.save(junzhu);
	 }else if("addExp".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 JunZhuMgr.inst.addExp(junzhu, v);
	 }else if("addYuanBao".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 //junzhu.yuanBao += v;
		YuanBaoMgr.inst.diff(junzhu,  v, 0,0,YBType.YB_GM_ADDYB,"后台膜拜添加元宝");
		 HibernateUtil.save(junzhu);
	 }else if("setGod".equals(action)){
		 PveMgr.godId = junzhu.id;
	 }else if("addBufferLevel".equals(action)) {
		 int v = Integer.parseInt(request.getParameter("v"));
		 bufferLevel = updateMobaiLevel(member.lianMengId, v, new Date());
	 }else{
		 sendInfo = false;
	 }
	 if(sendInfo){
		 SessionUser u = SessionManager.getInst().findByJunZhuId(junzhu.id);
		 if(u!= null){
		 	JunZhuMgr.inst.sendMainInfo(u.session);
		 }
	 }
	 
	 out.println("&nbsp;君主id："+junzhu.id+" &nbsp; 名字:"+junzhu.name);
	 
	 
	 
	 String input = request.getParameter("v");
	 if(input == null)input = "1";
	 br();
	 //-----------------
	 if(member == null){
		 out("没有联盟");
	 }else{
		 br();
		 out("联盟id:"+member.lianMengId);
		 space();
		 out("");out("");
		 br();
		 br();
	 }
	 {//阶段奖励
		 
	 }
	 out.append("<input type='button' value='重置奖励列表' onclick='go(\"resetAward\")'/><br/><br/>");
	 
	 ChouJiangBean bean = HibernateUtil.find(ChouJiangBean.class, junzhu.id);
	 if(bean == null){
		 out("尚无祭拜信息");
	 }else{
		 if("resetTimes".equals(action)){
			 int total = bean.todayUsedTimes + bean.todayLeftTimes;
			 bean.todayUsedTimes = 0;
			 bean.todayLeftTimes = total;
			 HibernateUtil.save(bean);
		 }
		 tableStart();
		 trS();
		 td("今天使用次数");td(bean.todayUsedTimes); td(button("重置","go('resetTimes')"));
		 trE();
		 trS();
		 td("今天剩余次数");td(bean.todayLeftTimes); /* td(button("清除", "go('clearYB')")); */
		 trE();
		 trS();
		 td("历史总次数");td(bean.historyAll);/*  td("<input type='button' value='清除' onclick='go(\"clearYu\")'/><br/>"); */
		 trE();
		 tableEnd();
		 br();
		 ///////////////
		 IoSession ss = SessionManager.inst.getIoSession(junzhu.id);
		 if(ss != null) {
			 JianZhuMgr.inst.sendChouJiangInfo(PD.C_LM_CHOU_JIANG_INFO, ss, null);
		 }
		 
		 br();
	 }
		 }
	}
}
	
%>
</body>
</html>