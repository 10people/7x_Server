<%@page import="java.util.Date"%>
<%@page import="com.manu.dynasty.util.DateUtils"%>
<%@page import="com.manu.dynasty.template.VIP"%>
<%@page import="qxmobile.protobuf.VIP.RechargeReq"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.vip.PlayerVipInfo"%>
<%@page import="com.qx.vip.VipRechargeRecord"%>
<%@page import="java.util.List"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.qx.vip.VipMgr"%>
<%@include file="/myFuns.jsp"%>
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
%>账号<%=account.accountId%>:<%=account.accountName%><%
		JunZhu junzhu = HibernateUtil.find(JunZhu.class, (long)account.accountId*1000+GameServer.serverId);
		PlayerVipInfo playerVipInfo = null;
		 if(junzhu == null){
			 out.println("没有君主");
		 }else{
			 if(junzhu.level == 0 || junzhu.shengMingMax == 0){
				 JunZhuMgr.inst.fixCreateJunZhu((int)junzhu.id, junzhu.name, junzhu.roleId, junzhu.guoJiaId);
			 }
			 String action = request.getParameter("action");
			 if("vipRecharge".equals(action)){
				 int v = Integer.parseInt(request.getParameter("v")); 
				 RobotSession ss = new RobotSession();
				 ss.setAttribute("junzhuId", junzhu.id);
				 RechargeReq.Builder builder = RechargeReq.newBuilder();
				 int type = -1;
				 switch(v){
				 case 6: type = 1;break;
				 case 28: type = 2;break;
				 case 68: type = 3;break;
				 case 98: type = 4;break;
				 case 198: type =5;break;
				 case 328: type = 6;break;
				 case 648: type = 7;break;
				 }
				 if(type == -1){
		
				 }else{
					     builder.setAmount(v);
					      builder.setType(type);
				    VipMgr.INSTANCE.recharge(0, ss, builder);
				 }
			 }else if("jiangji".equals(action)){
				 int v = Integer.parseInt(request.getParameter("v")); 
				 if(v <0 )return;
				 if(v == 0){
					 PlayerVipInfo  vipInfo = HibernateUtil.find(PlayerVipInfo.class, junzhu.id);
					 if(vipInfo != null){
						 HibernateUtil.delete(vipInfo);
					 }
				 }
				 VIP vip = VipMgr.vipTemp.get(v);
	            if(vip == null){
	                return;
	            }
	           
	            PlayerVipInfo  vipInfo = HibernateUtil.find(PlayerVipInfo.class, junzhu.id);
            	if(vipInfo == null){
            		vipInfo = new PlayerVipInfo();
		            vipInfo.accId = junzhu.id;
		            vipInfo.sumAmount = 0;
		            vipInfo.level = v;
		            vipInfo.vipExp = vip.needNum;
		            HibernateUtil.save(vipInfo);
	            }else{
            	   vipInfo.level = v;
                   vipInfo.vipExp = vip.needNum;
                   HibernateUtil.save(vipInfo);
	            }
            	junzhu.vipLevel = v;
                HibernateUtil.save(junzhu);
			 }else if("yuekaday".equals(action)){
				    int v = Integer.parseInt(request.getParameter("v")); 
				    PlayerVipInfo playerVinfo = VipMgr.INSTANCE.getPlayerVipInfo(junzhu.id);
					playerVinfo.yueKaRemianDay = v < 0 ? 0 : v;
					HibernateUtil.save(playerVinfo);
			 }else if("zhoukaday".equals(action)){
				    int v = Integer.parseInt(request.getParameter("v")); 
				    PlayerVipInfo playerVinfo = VipMgr.INSTANCE.getPlayerVipInfo(junzhu.id);
					playerVinfo.zhouKaRemianDay = v < 0 ? 0 : v;
					HibernateUtil.save(playerVinfo);
			 }

			playerVipInfo = HibernateUtil.find(PlayerVipInfo.class, junzhu.id);
 			out.println("&nbsp;君主id："+junzhu.id);out.println("<br/>");
 			out.println("等级："+junzhu.level+"<br/>");
 			int v = 0;
 			String input = request.getParameter("value");
 			if(input == null){
	 			input = "648";
 			}
 			
 			out.println("充值总额："+ (playerVipInfo == null ? 0 : playerVipInfo.sumAmount));out.println("<br/>");
 			out.println("vip等级："+ (playerVipInfo == null ? 0 : playerVipInfo.level));out.println("<br/>");
 			out.println("月卡剩余天数："+ (playerVipInfo == null ? 0 : playerVipInfo.yueKaRemianDay));out.println("<br/>");
 			out.println("周卡剩余天数："+ (playerVipInfo == null ? 0 : playerVipInfo.zhouKaRemianDay));out.println("<br/>");
 			br(); %><div style="color: #FF0000">(充值可以提升VIP等级) <br>
 			只能选择::6， 28， 68， 98， 198， 328， 648::<br>
 			充值（单位：rmb）：（将会模仿游戏中的充值接口给予充值）</div><%
 			br();
 			out.println("<input type='text' id='vipRecharge' value='"+input+"'/><input type='button' value='充值' onclick='go(\"vipRecharge\")'/><br/><br/<hr/>");
 			%><div style="color: #FF0000">修改VIP等级:</br>可以修改到1~xxx(看配置最大值),任意VIP等级，仅仅修改的是VIP等级和对应的VIP经验，君主元宝数不会发生任何变化</div>
 			<div style="color: #FF0000">填写0可以删除VIP信息</div><%
 			input = request.getParameter("value");
            if(input == null){
                input = junzhu.vipLevel +"";
            }
 			out.println("<input type='text' id='jiangji' value='"+input+"'/><input type='button' value='修改VIP等级' onclick='go(\"jiangji\")'/><br/><br/<hr/>");
 			input = request.getParameter("value");
 			if(input == null){
 				input = "" + (playerVipInfo == null ? 0 : playerVipInfo.yueKaRemianDay);
 			}
 			out.println("<input type='text' id='yuekaday' value='"+input+"'/><input type='button' value='修改月卡剩余天数' onclick='go(\"yuekaday\")'/><br/><br/<hr/>");

 			input = request.getParameter("value");
 			if(input == null){
 				input = "" + (playerVipInfo == null ? 0 : playerVipInfo.zhouKaRemianDay);
 			}
 			out.println("<input type='text' id='zhoukaday' value='"+input+"'/><input type='button' value='修改周卡剩余天数' onclick='go(\"zhoukaday\")'/><br/><br/<hr/>");
 			
 			List<VipRechargeRecord> recordList = HibernateUtil.list(VipRechargeRecord.class, "  where accId= " + junzhu.id);
 			out.append("充值记录：");
 			out.append("<table border='1'>");
			out.append("<tr>");
			out.append("<th>ID</th><th>accId</th><th>curAmount</th><th>sumAmount</th><th>vip等级</th><th>时间</th>");
			out.append("</tr>");
			int lastHeroId = 0;
			for(VipRechargeRecord record : recordList){
				out.append("<tr>");
				out.append("<td>");		out.println(record.id);		out.append("</td>");
				out.append("<td>");		out.println(record.accId);	out.append("</td>");
				out.append("<td>");		out.println(record.curAmount);		out.append("</td>");
				out.append("<td>");		out.println(record.sumAmount);		out.append("</td>");
				out.append("<td>");		out.println(record.level);		out.append("</td>");
				out.append("<td>");		out.println(record.time);		out.append("</td>");
				out.append("<tr>");
			}
			out.append("</table>");
		}
	}
}
%>
</body>
</html>