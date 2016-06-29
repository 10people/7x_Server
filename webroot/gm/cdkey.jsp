<%@page import="java.util.Calendar"%>
<%@page import="com.qx.cdkey.CDKeyInfo"%>
<%@page import="java.util.Date"%>
<%@page import="com.qx.cdkey.CDKeyMgr"%>
<%@page import="com.qx.award.AwardMgr"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="java.util.List"%>
<%@page import="qxmobile.protobuf.ShouChong.AwardInfo"%>
<%@page import="qxmobile.protobuf.CDKey.GetCDKeyAwardResp"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="qxmobile.protobuf.CDKey.GetCDKeyAwardReq"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@include file="/myFuns.jsp"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>CDKey</title>
</head>
<body>
	<%
	setOut(out);
	String name = request.getParameter("account");
	name = name == null ? "": name.trim();
	String accIdStr = request.getParameter("accId");// 用户id
	accIdStr = (accIdStr == null ? "":accIdStr.trim());
	if(session.getAttribute("name") != null && name.length()==0 && accIdStr.length()==0){
		name = (String)session.getAttribute("name");
	}
	
	String chanIdStr = request.getParameter("chanId");
	String deadDateStr = request.getParameter("deadDate");
	String numStr = request.getParameter("num");
	String awards = request.getParameter("awards");
	String action = request.getParameter("action");
	int cnt = HibernateUtil.getCount("select count(1) from CDKeyInfo where jzId>0");
	%>
	<h3>兑换CDKey物品</h3>
	已领取：<%=cnt %>---<a href='?action=reset'>重置领取记录</a>
	<script type="text/javascript">
		function checkGetAward(){
			var key = document.getElementById("key").value;
			if(key==''){
				alert('请输入CDKey');
				return false;
			}
			return true;
		}
	</script>
	<form action="" onsubmit="return checkGetAward()">
		账号<input type="text" name="account" value="<%=name%>">&nbsp;或&nbsp;
		君主ID<input type="text" name="accId" value="<%=accIdStr%>"> <br/>
		CDKey<input type="text" id="key" name="key" /> 
		<input type="hidden" name="action" value="getAward"/>
		<button type="submit">兑换</button>
	</form>
	<hr/>
	<a href="cdkeys.jsp" target="_blank">查看当前CDKey</a>
	<hr />
	<form action="">
		<%
		String maxLength = request.getParameter("maxLength");
		if(maxLength!=null&&maxLength.length()>0){
			CDKeyMgr.KEY_LENGTH = Integer.parseInt(maxLength);
		}
		%>
		CDKey长度：<input type="number" name="maxLength" value="<%=CDKeyMgr.KEY_LENGTH%>"/><button type="submit">修改</button>
	</form>
	<hr />
	<h3>一键生成CDKey</h3>
	<script type="text/javascript">
		function check(){
			var chanId = document.getElementById("chanId").value;
			var deadDate = document.getElementById("deadDate").value;
			var num = document.getElementById("num").value;
			var awards = document.getElementById("awards").value;
			if(chanId == ''){
				alert('请输入渠道id');
				return false;
			}
			if(deadDate == ''){
				alert('请输入截止日期');
				return false;
			}
			if(num == ''){
				alert('请输入cdkey的数量');
				return false;
			}
			if(awards == ''){
				alert('请输入奖励配置');
				return false;
			}
			return true;
		}
	</script>
	<form action="" onsubmit="return check()">
		渠道id：<input type="number" id="chanId" name="chanId"/><br/>
		截止日期：<input type="text" id="deadDate" name="deadDate" value="<%=Calendar.getInstance().get(Calendar.YEAR)%>-<%=Calendar.getInstance().get(Calendar.MONTH)+1%>-<%=Calendar.getInstance().get(Calendar.DAY_OF_MONTH)%>"/><span style="color:red">格式：xxxx-xx-xx</span><br/>
		cdkey数量：<input type="number" id="num" name="num" value="10"/><br/>
		物品：<input type="text" id="awards" name="awards"/><span style="color:red">按照格式 type:itemId:count#type:itemId:count(类型，id,数量#类型，id,数量)</span><br/>
		<input type="hidden" name="action" value="generate"/>
		<button type="submit">一键生成CDKey</button>
	</form>
	<hr/>
	<br/><br/>
	<%
	Account account = null;
	if(name != null && name.length()>0){
		account = HibernateUtil.getAccount(name);
	}else if(accIdStr.length()>0){
		account = HibernateUtil.find(Account.class, (Long.valueOf(accIdStr) - GameServer.serverId) / 1000);
		if(account != null)name = account.getAccountName();
	}
	if(action==null){
		return;
	}
	do{
		if(action.equals("getAward")){
			long junZhuId = 0;
			if(account != null){
				session.setAttribute("name", name);
				out("账号");out(account.getAccountId());out("：");out(account.getAccountName());
				out("密码：");out(account.getAccountPwd());
				junZhuId = account.getAccountId() * 1000 + GameServer.serverId;
			}else if(accIdStr.matches("\\d+")){
				junZhuId = Long.parseLong(accIdStr);
			}else{
				out("没有找到");
				break;
			}
			JunZhu junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
			if(junzhu == null){
				out.println("没有君主");
				break;
			}
			
			String key = request.getParameter("key");
			if(key != null && key.length() > 0){
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
				GetCDKeyAwardReq.Builder builder = GetCDKeyAwardReq.newBuilder();
				builder.setCdkey(key);
				synchronized(fs){
					BigSwitch.inst.route(PD.C_GET_CDKETY_AWARD_REQ, builder, fs);
				//	fs.wait();
				}
				GetCDKeyAwardResp resp = (GetCDKeyAwardResp)fs.getAttachment();
				int result = resp.getResult();
				List<AwardInfo> awardList = resp.getAwardsList();
				switch(result){
				case 0:
					out.println("兑换码"+key+"兑换成功,获得以下物品");
					%>
					<table border="1">
						<tr><th>物品id</th><th>物品type</th><th>数量</th></tr>
						<%
						for(AwardInfo award:awardList){
						%>
						<tr>
							<td><%=award.getAwardId() %></td>
							<td><%=award.getAwardType() %></td>
							<td><%=award.getAwardNum() %></td>
						</tr>
						<%
						}
						%>
					</table>
					<%
					break;
				case 1:
					out.println(resp.getErrorMsg());
					break;
				default:
					break;
				}
			}
		} else if(action.equals("reset")){
			String sql = "update CDKeyInfo set jzId=0";
			HibernateUtil.executeSql(sql);
			out("已重置");
		} else if(action.equals("generate")){
			int chanId = Integer.parseInt(chanIdStr);
			Date deadDate = new Date(Integer.parseInt(deadDateStr.split("-")[0])-1900,Integer.parseInt(deadDateStr.split("-")[1])-1,Integer.parseInt(deadDateStr.split("-")[2]));
			int num = Integer.parseInt(numStr);
			
			List<CDKeyInfo> keyList = CDKeyMgr.inst.generateCDKey(chanId, deadDate, num, awards,"TK");
			if(keyList!=null&&keyList.size()>0){
				out.println("生成的CDKey信息如下：");
			}
			out.println("<br/>渠道id："+chanId+"<br/>");
			out.println("截止日期："+deadDate.toLocaleString()+"<br/>");
			out.println("cdkey数量："+num+"<br/>");
			out.println("物品："+awards+"<br/>");
			out.println("key："+"<br/>");
			for(CDKeyInfo keyInfo:keyList){
				out.println(keyInfo.getCdkey()+"<br/>");
			}
		}
	}while(false);
	%>
</body>
</html>
