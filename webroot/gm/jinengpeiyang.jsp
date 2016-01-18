<%@page import="qxmobile.protobuf.JiNengPeiYang.HeroData"%>
<%@page import="qxmobile.protobuf.JiNengPeiYang.UpgradeJiNengResp"%>
<%@page import="qxmobile.protobuf.JiNengPeiYang.UpgradeJiNengReq"%>
<%@page import="com.manu.dynasty.template.JiNengPeiYang"%>
<%@page import="java.util.Map"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="com.qx.jinengpeiyang.JiNengPeiYangMgr"%>
<%@page import="java.util.List"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="qxmobile.protobuf.JiNengPeiYang.GetJiNengPeiYangQuality"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@include file="/myFuns.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>技能培养</title>
</head>
<body>
	<%
		setOut(out);
		String name = request.getParameter("account");
		name = name == null ? "" : name.trim();
		String accIdStr = request.getParameter("accId");// 用户id
		accIdStr = (accIdStr == null ? "" : accIdStr.trim());
		if (session.getAttribute("name") != null && name.length() == 0
				&& accIdStr.length() == 0) {
			name = (String) session.getAttribute("name");
		}

		String wuqiTypeStr = request.getParameter("wuqiType");
		String jinengTypeStr = request.getParameter("jinengType");
		int wuqiType = (wuqiTypeStr == null || wuqiTypeStr.length() == 0) ? 0
				: Integer.parseInt(wuqiTypeStr);
		int jinengType = (jinengTypeStr == null || jinengTypeStr.length() == 0) ? 0
				: Integer.parseInt(jinengTypeStr);
	%>
	<form action="" onsubmit="return checkGetAward()">
		账号<input type="text" name="account" value="<%=name%>">&nbsp;或&nbsp;
		君主ID<input type="text" name="accId" value="<%=accIdStr%>">&nbsp;
		<button type="submit">查看技能</button>
	</form>
	<%
		Account account = null;
		if (name != null && name.length() > 0) {
			account = HibernateUtil.getAccount(name);
		} else if (accIdStr.length() > 0) {
			account = HibernateUtil.find(Account.class,
					(Long.valueOf(accIdStr) - GameServer.serverId) / 1000);
			if (account != null)
				name = account.getAccountName();
		}

		/**突破技能**/
		if (wuqiType != 0 && jinengType != 0) {
			final IoSession fs = new RobotSession() {
				public WriteFuture write(Object message) {
					setAttachment(message);
					synchronized (this) {
						this.notify();
					}
					return null;
				}
			};
			fs.setAttribute(
					SessionAttKey.junZhuId,
					Long.valueOf(account.getAccountId() * 1000
							+ GameServer.serverId));
			UpgradeJiNengReq.Builder builder = UpgradeJiNengReq
					.newBuilder();
			builder.setSkillID(wuqiType);
			synchronized (fs) {
				BigSwitch.inst.route(PD.C_UPGRADE_JINENG_REQ, builder, fs);
				//	fs.wait();
			}
			UpgradeJiNengResp resp = (UpgradeJiNengResp) fs.getAttachment();
			int result = resp.getResult();
			String errorMsg = resp.getErrorMsg();
			if (result == 0) {
	%>突破成功<%
		} else {
	%><%=errorMsg%>
	<%
		}
		}

		do {
			long junZhuId = 0;
			if (account != null) {
				session.setAttribute("name", name);
				out("账号");
				out(account.getAccountId());
				out("：");
				out(account.getAccountName());
				out("密码：");
				out(account.getAccountPwd());
				junZhuId = account.getAccountId() * 1000
						+ GameServer.serverId;
			} else if (accIdStr.matches("\\d+")) {
				junZhuId = Long.parseLong(accIdStr);
			} else {
				out("没有找到");
				break;
			}
			JunZhu junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
			if (junzhu == null) {
				out.println("没有君主");
				break;
			}

			final IoSession fs = new RobotSession() {
				public WriteFuture write(Object message) {
					setAttachment(message);
					synchronized (this) {
						this.notify();
					}
					return null;
				}
			};
			fs.setAttribute(
					SessionAttKey.junZhuId,
					Long.valueOf(account.getAccountId() * 1000
							+ GameServer.serverId));
			synchronized (fs) {
				BigSwitch.inst.route(PD.C_GET_JINENG_PEIYANG_QUALITY_REQ,
						null, fs);
				//	fs.wait();
			}
			GetJiNengPeiYangQuality resp = (GetJiNengPeiYangQuality) fs
					.getAttachment();
			List<HeroData> jinengList = resp.getListHeroDataList();
	%>
	<table border="1">
		<tr>
			<th>wuqiType</th>
			<th>jinengType</th>
			<th>quality</th>
			<th>操作</th>
		</tr>
		<%
			for (HeroData jineng : jinengList) {
		%>
		<tr>
			<td><%=jineng.getSkillId()%></td>
			<td><%=jineng.getIsUp()%></td>
			<td><a
				href="jinengpeiyang.jsp?wuqiType=<%=jineng.getSkillId()%>">突破</a></td>
		</tr>
		<%
			}
		%>
	</table>
	<%
		} while (false);
	%>
</body>
</html>
