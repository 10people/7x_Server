<%@page import="com.manu.dynasty.store.MemcachedCRUD"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.List"%>
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
<%@page import="com.manu.dynasty.template.YouxiaOpenTime"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.qx.timeworker.TimeWorkerMgr"%>
<%@page import="com.qx.youxia.BuZhenYouXia"%>
<%@page import="com.qx.alliance.AlliancePlayer"%>
<%@page import="qxmobile.protobuf.TimeWorkerProtos.TimeWorkerResponse"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.youxia.YouXiaBean"%>
<%@page import="com.qx.youxia.YouXiaMgr"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.qx.util.TableIDCreator"%>
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
<title>游侠管理</title>
</head>
<body>
<%=MemcachedCRUD.getInstance().getObject("com.qx.youxia.YouXiaBean#id") %>
<%!
	public void updateLastBattleTime(String time, int type, Map<Integer, YouXiaBean> yxBeanMap) throws Exception{
		 if(time == null || "".equals(time)) {
			 out.write("<br/><br/><font color='red'>修改错误:时间不能填空</font>");
			 return;
		 }
		 YouXiaBean bean = yxBeanMap.get(type);
		 if(bean != null && bean.lastBattleTime != null) {
			 bean.lastBattleTime = new Date(bean.lastBattleTime.getTime() + Long.parseLong(time) * 1000);
			 HibernateUtil.save(bean);
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
	out("账号");out(account.getAccountId());out("：");out(account.getAccountName());
	out("密码：");out(account.getAccountPwd());
	long junZhuId = account.getAccountId() * 1000 + GameServer.serverId;
	JunZhu junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
	if(junzhu == null){
		out.println("没有君主");
		break;
	}
	if(junzhu.level == 0 || junzhu.shengMingMax == 0){
		JunZhuMgr.inst.fixCreateJunZhu(junZhuId, junzhu.name, junzhu.roleId, junzhu.guoJiaId);
	}
	List<YouXiaBean> yxBeanList = HibernateUtil.list(YouXiaBean.class, " where junzhuId = " + junzhu.id);
	if(yxBeanList.size() == 0) {
		List<YouxiaOpenTime> openList = TempletService.listAll(YouxiaOpenTime.class.getSimpleName());
		for(YouxiaOpenTime yot : openList) {
			YouXiaBean yxBean = new YouXiaBean();
			yxBean.id = TableIDCreator.getTableID(YouXiaBean.class, 1L);
			yxBean.junzhuId = junzhu.id;
			yxBean.type = yot.id;
			yxBean.times = yot.maxTimes;
			yxBean.lastBattleTime = null;
			Date date = new Date();
			yxBean.lastBuyTime = date;
			HibernateUtil.insert(yxBean);
			yxBeanList.add(yxBean);
		}
	}
	
	Map<Integer, YouXiaBean> yxBeanMap = new HashMap<Integer, YouXiaBean>();
	for(YouXiaBean bean : yxBeanList) {
		yxBeanMap.put(bean.type, bean);
	}
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 String action = request.getParameter("action");
	 boolean sendInfo = true;
	 if("addJinbi".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 YouXiaBean yxb = yxBeanMap.get(1);
		 yxb.times += v;
		 HibernateUtil.save(yxb);
	 }else if("addCailiao".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 YouXiaBean yxb = yxBeanMap.get(2);
		 yxb.times += v;
		 HibernateUtil.save(yxb);
	 }else if("addJingqi".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 YouXiaBean yxb = yxBeanMap.get(3);
		 yxb.times += v;
		 HibernateUtil.save(yxb);
	 }else if("add4".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 YouXiaBean yxb = yxBeanMap.get(4);
		 yxb.times += v;
		 HibernateUtil.save(yxb);
	 }else if("addJingqi".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 YouXiaBean yxb = yxBeanMap.get(5);
		 yxb.times += v;
		 HibernateUtil.save(yxb);
	 }else if("updateJinbi".equals(action)){
		 String dayStr = request.getParameter("v");
		 YouXiaMgr.inst.youxiaOpenTimeMap.get(1).OpenDay = dayStr;
	 }else if("updateCailiao".equals(action)){
		 String dayStr = request.getParameter("v");
		 YouXiaMgr.inst.youxiaOpenTimeMap.get(2).OpenDay = dayStr;
	 }else if("updateJingqi".equals(action)){
		 String dayStr = request.getParameter("v");
		 YouXiaMgr.inst.youxiaOpenTimeMap.get(3).OpenDay = dayStr;
	 }else if("update4".equals(action)){
		 String dayStr = request.getParameter("v");
		 YouXiaMgr.inst.youxiaOpenTimeMap.get(4).OpenDay = dayStr;
	 }else if("update5".equals(action)){
		 String dayStr = request.getParameter("v");
		 YouXiaMgr.inst.youxiaOpenTimeMap.get(5).OpenDay = dayStr;
	 }else if("updateJinbiTime".equals(action)){
		 updateLastBattleTime(request.getParameter("v"), 1, yxBeanMap);
	 }else if("updateCailiaoTime".equals(action)){
		 updateLastBattleTime(request.getParameter("v"), 2, yxBeanMap);
	 }else if("updateJingqiTime".equals(action)){
		 updateLastBattleTime(request.getParameter("v"), 3, yxBeanMap);
	 }else if("update4Time".equals(action)){
		 updateLastBattleTime(request.getParameter("v"), 4, yxBeanMap);
	 }else if("update5Time".equals(action)){
		 updateLastBattleTime(request.getParameter("v"), 5, yxBeanMap);
	 }else {
		 sendInfo = false;
	 }
	 if(sendInfo){
		 SessionUser u = SessionManager.getInst().findByJunZhuId(junzhu.id);
		 if(u!= null){
		 	JunZhuMgr.inst.sendMainInfo(u.session);
		 }
	 }
	 JunZhuMgr.inst.calcJunZhuTotalAtt(junzhu);
	 AlliancePlayer alncPlayer = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
	 String guojiaName = HeroService.getNameById(junzhu.guoJiaId+"");
	 out.println("&nbsp;君主id："+junzhu.id+" &nbsp; 名字:"+junzhu.name+"&nbsp; 国家:" + guojiaName);
	 out.println("GodId:"+PveMgr.godId+"<br/>");
	 ExpTemp expTemp = TempletService.getInstance().getExpTemp(1, junzhu.level);
	 out.println("等级："+junzhu.level+"<br/>");
	 int v = 0;
	 if(expTemp != null)v =expTemp.getNeedExp();
	 String input = request.getParameter("v");
	 if(input == null)input = "1";
	 String country = HeroService.getNameById(junzhu.guoJiaId+"");
	 tableStart();
	 trS();td("玩法类型");td("今日可玩次数");td("");td("玩法开放星期数");td("修改，必须按同样格式");td("上次挑战时间");td("改变时间：可以为负数(单位:秒)");trE();
	 trS();td("洗劫权贵");td(yxBeanMap.get(1).times);td("<input type='text' id='addJinbi' value='"+input+"'/><input type='button' value='增加' onclick='go(\"addJinbi\")'/><br/>");
	 	td(YouXiaMgr.inst.youxiaOpenTimeMap.get(1).OpenDay);td("<input type='text' id='updateJinbi' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateJinbi\")'/><br/>");
	 	td(yxBeanMap.get(1).lastBattleTime == null ? "从未挑战过" : simpleDateFormat.format(yxBeanMap.get(1).lastBattleTime));
	 	td("<input type='text' id='updateJinbiTime' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateJinbiTime\")'/><br/>");
	 trE();
	 
	 trS();td("完璧归赵");td(yxBeanMap.get(2).times);td("<input type='text' id='addCailiao' value='"+input+"'/><input type='button' value='增加' onclick='go(\"addCailiao\")'/><br/>");
	 	td(YouXiaMgr.inst.youxiaOpenTimeMap.get(2).OpenDay);td("<input type='text' id='updateCailiao' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateCailiao\")'/><br/>");
	 	td(yxBeanMap.get(2).lastBattleTime == null ? "从未挑战过" : simpleDateFormat.format(yxBeanMap.get(2).lastBattleTime));
	 	td("<input type='text' id='updateCailiaoTime' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateCailiaoTime\")'/><br/>");
	 trE();
	 
	 trS();td("剿灭叛军");td(yxBeanMap.get(3).times);td("<input type='text' id='addJingqi' value='"+input+"'/><input type='button' value='增加' onclick='go(\"addJingqi\")'/><br/>");
	 	td(YouXiaMgr.inst.youxiaOpenTimeMap.get(3).OpenDay);td("<input type='text' id='updateJingqi' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateJingqi\")'/><br/>");//out.println("<a href='?action=addYuanBao'>+100</a><br/>");
	 	td(yxBeanMap.get(3).lastBattleTime == null ? "从未挑战过" : simpleDateFormat.format(yxBeanMap.get(3).lastBattleTime));
	 	td("<input type='text' id='updateJingqiTime' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateJingqiTime\")'/><br/>");
	 trE();

	 trS();td("完璧归赵");td(yxBeanMap.get(4).times);td("<input type='text' id='add4' value='"+input+"'/><input type='button' value='增加' onclick='go(\"add4\")'/><br/>");
	 	td(YouXiaMgr.inst.youxiaOpenTimeMap.get(4).OpenDay);td("<input type='text' id='updateJingqi' value='"+input+"'/><input type='button' value='修改' onclick='go(\"update4\")'/><br/>");//out.println("<a href='?action=addYuanBao'>+100</a><br/>");
	 	td(yxBeanMap.get(4).lastBattleTime == null ? "从未挑战过" : simpleDateFormat.format(yxBeanMap.get(4).lastBattleTime));
	 	td("<input type='text' id='update4Time' value='"+input+"'/><input type='button' value='修改' onclick='go(\"update4Time\")'/><br/>");
	 trE();

	 trS();td("横扫六合");td(yxBeanMap.get(5).times);td("<input type='text' id='add5' value='"+input+"'/><input type='button' value='增加' onclick='go(\"add5\")'/><br/>");
	 	td(YouXiaMgr.inst.youxiaOpenTimeMap.get(5).OpenDay);td("<input type='text' id='updateJingqi' value='"+input+"'/><input type='button' value='修改' onclick='go(\"update5\")'/><br/>");//out.println("<a href='?action=addYuanBao'>+100</a><br/>");
	 	td(yxBeanMap.get(5).lastBattleTime == null ? "从未挑战过" : simpleDateFormat.format(yxBeanMap.get(5).lastBattleTime));
	 	td("<input type='text' id='update5Time' value='"+input+"'/><input type='button' value='修改' onclick='go(\"update5Time\")'/><br/>");
	 trE();
	 tableEnd();
	 br();
	//	fs.wait();
	//
	PlayerTime pt = HibernateUtil.find(PlayerTime.class,junzhu.id);
	if(pt != null && pt.getLoginTime() != null){
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String setV = request.getParameter("loginTime");
		if(setV!=null){
			Date dt = sf.parse(setV);
			pt.setLoginTime(dt);
			HibernateUtil.save(pt);
		}
		out("<form action=''>");
		out("上次登录时间:<input type='text' name='loginTime' value='"+sf.format(pt.getLoginTime())+"'/>");
		out("<input type='submit' value='修改'/>");
		out("</form>");
	}else{
		out("从未登录");
	}
	// 地图坐标管理 
	PosInfo pos = HibernateUtil.find(PosInfo.class,junzhu.id);
	if(pos == null){
		out("尚无坐标信息");
		break;
	}
	// 修改坐标 
	String posX = request.getParameter("posX");
	String posY = request.getParameter("posY");
	String posZ = request.getParameter("posZ");
	if(posX!=null&&posX.length()>0&&posY!=null&&posY.length()>0&&posZ!=null&&posZ.length()>0){
		pos.x=Float.valueOf(posX);
		pos.y=Float.valueOf(posY);
		pos.z=Float.valueOf(posZ);
		HibernateUtil.save(pos);
		%>
		<p>位置已修改</p>
		<%
	}
	%>
	<form action="">
	<table border="1">
		<tr><th>jzId</th><th>x</th><th>y</th><th>z</th></tr>
		<tr>
		<td><%=junzhu.id %></td>
		<td><input name="posX" value="<%=pos.x %>"/></td>
		<td><input name="posY" value="<%=pos.y %>"/></td>
		<td><input name="posZ" value="<%=pos.z %>"/></td>
		</tr>
	</table>
	<button type="submit">修改数据库坐标</button>
	</form>
	<%
}while(false);
%>
</body>
</html>