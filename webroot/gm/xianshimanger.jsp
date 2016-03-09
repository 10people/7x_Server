<%@page import="com.qx.activity.FuliConstant"%>
<%@page import="com.qx.activity.XianShiConstont"%>
<%@page import="com.manu.dynasty.template.XianshiHuodong"%>
<%@page import="com.manu.dynasty.template.XianshiControl"%>
<%@page import="com.qx.activity.XianShiBean"%>
<%@page import="java.util.List"%>
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
<%@page import="com.qx.pve.PveMgr"%>
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
function go(obj){
	var act = obj.parentElement.childNodes[1].id;
	var v = obj.parentElement.childNodes[1].value;
	var huodongId=obj.parentElement.childNodes[0].value;
	location.href = '?action='+act+"&v="+v+"&huodongId="+huodongId;
}
function goday(act){
	var v = document.getElementById(act).value;
	location.href = '?action='+act+"&v="+v;
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
%>
	后台输出：
			<%
			String isShowLog = request.getParameter("isShowLog");
			if(isShowLog!=null){
				XianShiActivityMgr.isShow = Boolean.valueOf(isShowLog);
			}
			if(XianShiActivityMgr.isShow){
				%>
				<a>开启</a>|<a href="xianshimanger.jsp?isShowLog=false">关闭</a>
				<%
			} else{
				%>
				<a href="xianshimanger.jsp?isShowLog=true">开启</a>|<a>关闭</a>
				<%
			}
			%>
			<br>
			当前封测红包活动状态为：<%=BigSwitch.inst.xsActivityMgr.isOpen4FengceHongBao ? "开启" : "关闭"%>
			<br>
			当前封测月卡活动状态为：<%=BigSwitch.inst.xsActivityMgr.isOpen4YueKa ? "开启" : "关闭"%>
			<br>
			当前封测体力活动状态为：<%=XianShiActivityMgr.instance.xshdCloseList.contains(FuliConstant.tilifuli)?"关闭":"开启"%>
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
	
	 String action = request.getParameter("action");
	 boolean sendInfo = true;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	PlayerTime playerTime = HibernateUtil.find(PlayerTime.class, junzhu.id);
	 if("upLoginTime".equals(action)){
		 String time = request.getParameter("v");
		 if (time != null) {
				log.info("修改君主{}的上次登录时间为{}===》{}",junzhu.id, playerTime.getLoginTime(),time);
				playerTime.setLoginTime(sdf.parse(time));
				HibernateUtil.save(playerTime);
			}
	 }
	 if("upTime".equals(action)){
			 int huodongId =Integer.parseInt(request.getParameter("huodongId"));
			XianShiBean xsBean = HibernateUtil.find(XianShiBean.class, huodongId + junzhu.id * 100);
			String time = request.getParameter("v");
			if (time != null) {
				log.info("修改君主{}的限时活动{}的时间为{}===》{}",junzhu.id,huodongId,xsBean.startDate,time);
				xsBean.startDate=sdf.parse(time);
				HibernateUtil.save(xsBean);
			}
		}
		
		List<XianShiBean> xianShiList=HibernateUtil.list(XianShiBean.class, "where junZhuId="+junzhu.id);
	
		
		int v = 0;
		tableStart();
		if(playerTime==null){
			out("君主没有登录时间数据<br>");
			return;
		}else{
			Date lastLogInTime = playerTime.getLoginTime();
		String dayCount=	XianShiActivityMgr.DB.get(XianShiActivityMgr.XIANSHI7DAY_KEY + junZhuId);
// 	boolean	isExist =XianShiActivityMgr.DB.lexist((XianShiActivityMgr.XIANSHIFINISH_KEY + junZhuId), XianShiConstont.QIRIQIANDAO_TYPE + "");
		if(lastLogInTime!=null){
				trS();
				td("连续登陆天数"+dayCount+"；上次登录时间");
				td("<input type='text' id='upLoginTime' value='"
					+ sdf.format(lastLogInTime)
					+ "'/><input type='button' value='修改' onclick='goday(\"upLoginTime\")'/><br/>");
				trE();
			}
		}
		for(XianShiBean xs : xianShiList){
			XianshiControl huoDong = XianShiActivityMgr.xsControlMap.get(xs.bigId);
			if(huoDong!=null){
			trS();
			td(huoDong.getName());
			td("活动参加时间");
			td("<input type='hidden' readonly='readonly'  value='"+xs.bigId+"'/>"+
			"<input type='text' id='upTime' value='"
					+ sdf.format(xs.startDate)
					+ "'/><input type='button' value='修改' onclick='go(this)'/><br/>");
			trE();
			}else{
				out("没有找到活动Id为"+xs.bigId+"的活动<br>");
			}
		}
		
		out("<br><br>");
		List<XianshiHuodong> xsList=XianShiActivityMgr.instance.bigActivityMap.get(1501000);
		for(XianshiHuodong xs : xsList){
			if(xs!=null){
				boolean	isYiling=false;
			boolean isKeling =XianShiActivityMgr.instance.DB.lexist((XianShiActivityMgr.XIANSHIKELING_KEY + junzhu.id), xs.getId()+ "");
// 			boolean isYiling =XianShiActivityMgr.instance.DB.lexist((XianShiActivityMgr.XIANSHIYILING_KEY + junzhu.id), xs.getId()+ "");
			trS();
			td(xs.getDesc());
			td(isYiling?"已领":(isKeling?"可领":"不可领"+ xs.getId()+"---"+XianShiActivityMgr.XIANSHIKELING_KEY + junzhu.id));
			trE();
			}
		}
		tableEnd();
%>
	
	<%
}while(false);
%>
</body>
</html>