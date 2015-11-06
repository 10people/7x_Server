<%@page import="com.qx.alliance.HouseMgr"%>
<%@page import="qxmobile.protobuf.House.Apply"%>
<%@page import="qxmobile.protobuf.House.ApplyInfos"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="java.util.concurrent.atomic.AtomicBoolean"%>
<%@page import="qxmobile.protobuf.House.ExchangeHouse"%>
<%@page import="qxmobile.protobuf.House.ExchangeEHouse"%>
<%@page import="qxmobile.protobuf.House.HouseVisitorInfo"%>
<%@page import="qxmobile.protobuf.House.HouseExpInfo"%>
<%@page import="qxmobile.protobuf.House.SetHouseState"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="com.qx.alliance.AlliancePlayer"%>
<%@page import="com.qx.alliance.HouseBean"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.qx.vip.PlayerVipInfo"%>
<%@page import="com.qx.vip.VipRechargeRecord"%>
<%@page import="java.util.List"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.qx.vip.VipMgr"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title>Insert title here</title>
</head>
<body>
<%
	String op = request.getParameter("op");
	String jzId = request.getParameter("jzId");
	String lmId = request.getParameter("lmId");
	if("addGx".equals(op)){
		AlliancePlayer ap = HibernateUtil.find(AlliancePlayer.class, Long.parseLong(jzId));
		if(ap == null){
			out("0");
		}else{
			String v = request.getParameter("cnt");
			int i = v == null ? 10 : Integer.parseInt(v);
			ap.gongXian += i;
			HibernateUtil.save(ap);
			out(ap.gongXian);
		}
	}else if("fenPeiBigHouse".equals(op)){
		int id = Integer.parseInt(jzId);
		BigSwitch.inst.houseMgr.fenPeiBigHouse(id);
		out("OK.");
	}else if("viewApply".equals(op)){
		String keeper = request.getParameter("keeper");
		final IoSession fs = new RobotSession(){
			public WriteFuture write(Object message){
				setAttachment(message);
				synchronized(this){
					this.notify();
				}
				return null;
			}
		};
		fs.setAttribute(SessionAttKey.junZhuId, Long.valueOf(keeper));
		synchronized(fs){
			BigSwitch.inst.route(PD.C_HOUSE_APPLY_LIST, null, fs);
			fs.wait();
		}
		ApplyInfos o = (ApplyInfos)fs.getAttachment();
		int cnt = o.getListCount();
		out("房子主人id:"+keeper);
		out("&nbsp;申请个数:"+cnt);
		if(cnt>0){
			tableStart();
			ths("申请者ID,名字,时间,op");
			for(int i=0; i<cnt;i ++){
				Apply a = o.getList(i);
				trS();
				td(a.getJzId()+"");
				td(a.getName());
				td(a.getDate());
				String btn = "<input type='button' onclick='agreeEx(xxx)' value='同意'/>";
				btn += "<input type='button' onclick='rejectEx(xxx)' value='拒绝'/>";
				btn = btn.replace("xxx", keeper+","+a.getJzId());
				td(btn);
				trE();
			}
			tableEnd();
		}
	}else if("agreeEx".equals(op)){
		String buyer = request.getParameter("buyer");
		String keeper = request.getParameter("keeper");
		//10同意；20拒绝；30无视；
		BigSwitch.inst.houseMgr.answerEx(Long.parseLong(keeper),Long.parseLong(buyer), 10);
		out("已同意");
	}else if("rejectEx".equals(op)){
		String buyer = request.getParameter("buyer");
		String keeper = request.getParameter("keeper");
		//10同意；20拒绝；30无视；
		BigSwitch.inst.houseMgr.answerEx(Long.parseLong(keeper),Long.parseLong(buyer), 20);
		out("已拒绝");
	}else if("exchangeEh".equals(op)){
		String buyer = request.getParameter("buyer");
		String elocation = request.getParameter("elocation");
		IoSession fs = createSession( Long.parseLong(buyer));
		ExchangeEHouse.Builder b = ExchangeEHouse.newBuilder();
		b.setTargetloc(Integer.parseInt(elocation));
		BigSwitch.inst.route(PD.C_EHOUSE_EXCHANGE_RQUEST, b, fs);
		out("ok");
	}
	else if("exchangeBHW".equals(op)){//衰减房屋价值
		String buyer = request.getParameter("buyer");
		IoSession fs = createSession( Long.parseLong(buyer));
		BigSwitch.inst.route(PD.C_CHANGE_BIGHOUSE_WORTH, null, fs);
		out("ok");
	}else if("applyEx".equals(op)){
		String buyer = request.getParameter("buyer");
		String keeper = request.getParameter("keeper");
		IoSession fs = createSession( Long.parseLong(buyer));
		ExchangeHouse.Builder b = ExchangeHouse.newBuilder();
		b.setTargetId(Long.parseLong(keeper));
		BigSwitch.inst.route(PD.C_HOUSE_EXCHANGE_RQUEST, b, fs);
		out("ok");
	}else if("paiBigHouse".equals(op)){//竞拍大房子
		String targetJzId = request.getParameter("targetJzId");
		String buyerId = request.getParameter("buyerId");
		IoSession fs = createSession( Long.parseLong(buyerId));
		ExchangeHouse.Builder b = ExchangeHouse.newBuilder();
		b.setTargetId(Long.parseLong(targetJzId));
		BigSwitch.inst.route(PD.C_Pai_big_house, b, fs);
		out("ok");
	}else if("getBatchInfo".equals(op)){//获取房屋信息
		String jzid ="29108040";
		IoSession fs = createSession( Long.parseLong(jzid));
		BigSwitch.inst.route(PD.C_LM_HOUSE_INFO, null, fs);
		out("ok");
	}
	else if("clearPrUpTime".equals(op)){
		long id = Long.parseLong(jzId);
		HouseBean bean = HibernateUtil.find(HouseBean.class, id);
		if(bean != null){
			bean.preUpTime = null;
			HibernateUtil.save(bean);
		}
		out("ok");
	}else if("changePrUpTime".equals(op)){
		long id = Long.parseLong(jzId);
		String upHouseTime = request.getParameter("upHouseTime");
		HouseBean bean = HibernateUtil.find(HouseBean.class, id);
		if(bean != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date d = sdf.parse(upHouseTime);
			bean.preUpTime = d;
			HibernateUtil.save(bean);
		}
		out("ok");
		
	}else if("set2sell".equals(op)){
		IoSession fs = createSession( Long.parseLong(jzId));
		SetHouseState.Builder builder = SetHouseState.newBuilder();
		builder.setTargetId(Long.parseLong(jzId));
		String location = request.getParameter("location");
		builder.setLocationId(Integer.parseInt(location));
		builder.setState(HouseBean.ForSell);
		BigSwitch.inst.route(PD.C_Set_House_state, builder, fs);
		out("ok");
	}else if("getBigExperience".equals(op)) {
		String junZhuId=jzId;
		IoSession fs = createSession( Long.parseLong(junZhuId));
 		BigSwitch.inst.route(PD.C_GET_BIGHOUSE_EXP, null,fs);
// 		BigSwitch.inst.houseMgr.gainExpFromBigHouse(PD.C_GET_BIGHOUSE_EXP,fs);
		out("bigOK.");
		
	}else if("getAndchangeHVInfo".equals(op)) {
		IoSession fs = createSession( Long.parseLong(jzId));
		HouseVisitorInfo.Builder builder = HouseVisitorInfo.newBuilder();
 		BigSwitch.inst.route(PD.C_GetHouseVInfo,builder,fs);
// 		BigSwitch.inst.houseMgr.getAndchangeHVInfo(PD.C_GetAndChangeHVInfo,fs,null);
		out("visitorOK.");
		
	}else{
		out("君主ID:"+jzId);
		HouseBean bean = BigSwitch.inst.houseMgr.giveDefaultHouse(Integer.parseInt(lmId), Long.parseLong(jzId));
		if(bean != null){
			out("成功分配到 : "+bean.location);
		}
	}
%>
</body>
</html>