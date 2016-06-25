<%@page import="com.qx.liefu.LieFuBean"%>
<%@page import="qxmobile.protobuf.LieFuProto.LieFuAward"%>
<%@page import="qxmobile.protobuf.LieFuProto.LieFuActionResp"%>
<%@page import="qxmobile.protobuf.LieFuProto.LieFuActionReq"%>
<%@page import="qxmobile.protobuf.LieFuProto.LieFuActionInfo"%>
<%@page import="qxmobile.protobuf.LieFuProto.LieFuActionInfoResp"%>
<%@page import="qxmobile.protobuf.ChongLouPve.ChongLouSaoDangAward"%>
<%@page import="com.manu.network.msg.ProtobufMsg"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="qxmobile.protobuf.ChongLouPve.ChongLouSaoDangResp"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="java.util.HashSet"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.account.Account"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="java.util.List"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@include file="/myFuns.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript">
	function go(act) {
		var node = document.getElementById(act);
		if(node == null) {
			location.href = '?action=' + act;
		} else {
			location.href = '?action=' + act + "&v=" + node.v;
		}
	}
</script>
<title>千重楼</title>
</head>
<body>
	<%
		setOut(out);
		String name = request.getParameter("account");
		String accIdStr = request.getParameter("accId");// 用户id
		if (name == null && accIdStr == null) {
			name = (String) session.getAttribute("name");
		}
		accIdStr = (accIdStr == null ? "" : accIdStr.trim());
		name = name == null ? "" : name.trim();
	%>
	<form action="">
		账号<input type="text" name="account" value="<%=name%>">&nbsp;或&nbsp;
		君主ID<input type="text" name="accId" value="<%=accIdStr%>">
		<button type="submit">查询</button>
	</form>

	<%
		Account account = null;
		if (name != null && name.length() > 0) {
			account = HibernateUtil.getAccount(name);
		} else if (accIdStr.length() > 0) {
			account = HibernateUtil.find(Account.class, (Long.valueOf(accIdStr) - GameServer.serverId) / 1000);
			if (account != null)
				name = account.getAccountName();
		}
		long junZhuId = 0;
		if (account != null) {
			session.setAttribute("name", name);
			out("账号");
			out(account.getAccountId());
			out("：");
			out(account.getAccountName());
			out(", 密码：");
			out(account.getAccountPwd());
			junZhuId = account.getAccountId() * 1000 + GameServer.serverId;
		} else if (accIdStr.matches("\\d+")) {
			junZhuId = Long.parseLong(accIdStr);
		} else {
			out("没有找到");
			return;
		}
		JunZhu junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
		if (junzhu == null) {
			out.println("没有君主");
			return;
		}

		long jzid = junzhu.id;
		out.append("<br/><br/><br/>");
		JunZhu junZhu = HibernateUtil.find(JunZhu.class, jzid);
		if (null == junZhu) {
			out.println("找不到君主");
			return;
		}
	%>
	
	猎符类型：1，2，3，4	<br/>
	猎符类型：<input type="text" id="doAction" />
	<input type="button" onclick="go('doAction')" value="执行猎符操作"/> <br/> <br/>
	猎符类型：1，2，3，4	<br/>
	猎符类型：<input type="text" id="resetTypeFreeTimes" />
	<input type="button" onclick="go('resetTypeFreeTimes')" value="重置免费次数"/> <br/> <br/>
	<input type="button" onclick="go('resetTypeFreeTimesAll')" value="重置免费次数"/> <br/> <br/>
	<%
		String action = request.getParameter("action");
		if("doAction".equals(action)) {
			String type = request.getParameter("v");
			if(type == "" || type == null) {
				out.println("type不能为空");
				return;
			}
			
			final IoSession fs = new RobotSession(){
				public WriteFuture write(Object message){
					setAttachment(message);
					synchronized(this){
						this.notify();
					}
					return null;
				}
			};
			fs.setAttribute(SessionAttKey.junZhuId, Long.valueOf(account.getAccountId()*1000+GameServer.serverId));
			synchronized(fs){
				LieFuActionReq.Builder req = LieFuActionReq.newBuilder();
				req.setType(Integer.parseInt(type));
				BigSwitch.inst.route(PD.LieFu_Action_req, req, fs);
			//	fs.wait();
			}
			out.println("结果返回，0-成功，1-铜币不足 <br/>");
			out.println("猎符类型， -1 表示没有类型被激活<br/>");
			out.println("猎符类型的状态，0-不可使用，1-可以使用 <br/>");
			LieFuActionResp resp = (LieFuActionResp)fs.getAttachment();
			List<LieFuAward> awardList = resp.getLieFuAwardListList();
			tableStart();
			trS();td("结果：");td(resp.getResult());trE();
			trS();td("当前猎符类型：");td(resp.getType());trE();
			trS();td("当前猎符类型状态：");td(resp.getTypeState());trE();
			trS();td("被激活的猎符类型：");td(resp.getNextType());trE();
			trS();td("被激活的猎符类型状态：");td(resp.getNextTypeState());trE();
			trS();td("奖励类型");td("奖励itemId");td("奖励数量");trE();
			for(LieFuAward award : awardList) {
				trS();td(award.getItemType());td(award.getItemId());td(award.getItemNum());;trE();
			}
			tableEnd();
		} else if("resetTypeFreeTimes".equals(action)) {
			String typeStr = request.getParameter("v");
			if(typeStr == "" || typeStr == null) {
				out.println("type不能为空");
				return;
			}
			LieFuBean liefuBean = HibernateUtil.find(LieFuBean.class, jzid);
			if(liefuBean == null) {
				out.println("猎符未开启");
				return;
			}
			int type = Integer.parseInt(typeStr);
			switch(type) {
			case 1:
				liefuBean.type1UseTimes = 0;
			break;
			case 2:
				liefuBean.type2UseTimes = 0;
			break;
			case 3:
				liefuBean.type3UseTimes = 0;
			break;
			case 4:
				liefuBean.type4UseTimes = 0;
			break;
			}
			HibernateUtil.save(liefuBean);
		} else if("resetTypeFreeTimesAll".equals(action)) {
			LieFuBean liefuBean = HibernateUtil.find(LieFuBean.class, jzid);
			if(liefuBean == null) {
				out.println("猎符未开启");
				return;
			}
			liefuBean.type1UseTimes = 0;
			liefuBean.type2UseTimes = 0;
			liefuBean.type3UseTimes = 0;
			liefuBean.type4UseTimes = 0;
			liefuBean.dayUseTimes = 0;
			HibernateUtil.save(liefuBean);
		}
	%>
	<br/><br/><br/>
	<a href="?action=actionInfo" onclick="go('actionInfo')">请求猎符操作主页面信息</a> 
	<%
		if("actionInfo".equals("actionInfo")) {
			final IoSession fs = new RobotSession(){
				public WriteFuture write(Object message){
					setAttachment(message);
					synchronized(this){
						this.notify();
					}
					return null;
				}
			};
			fs.setAttribute(SessionAttKey.junZhuId, Long.valueOf(account.getAccountId()*1000+GameServer.serverId));
			synchronized(fs){
				BigSwitch.inst.route(PD.LieFu_Action_Info_Req, null, fs);
			//	fs.wait();
			}
			LieFuActionInfoResp resp = (LieFuActionInfoResp)fs.getAttachment();
			tableStart();
			trS();td("猎符类型");td("花费铜币");td("状态");td("免费次数");trE();
			List<LieFuActionInfo> list = resp.getLieFuActionInfoList();
			for(LieFuActionInfo actionInfo : list) {
				trS();td(actionInfo.getType());td(actionInfo.getCost());td(actionInfo.getState());td(actionInfo.getFreeTimes());trE();
			}
			tableEnd();
		}
		
		
	%>
</body>
</html>