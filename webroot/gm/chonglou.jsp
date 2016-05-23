<%@page import="java.util.Set"%>
<%@page import="com.qx.ranking.RankingMgr"%>
<%@page import="com.qx.huangye.shop.ShopMgr"%>
<%@page import="com.qx.chonglou.ChongLouMgr"%>
<%@page import="com.qx.chonglou.ChongLouRecord"%>
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
			location.href = '?action=' + act + '&value=' + node.value;
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
	<input type="button" onclick="go('saodang')" value="扫荡"><br/>
	结果：0-成功，1-扫荡次数已用完,2-不需要扫荡，当前层数大于历史最高层数，3-还未打通任何楼层<br/><br/>
	填写当前层数：<input type="text" id="updateCurLevel">
	<input type="button" onclick="go('updateCurLevel')" value="修改当前挑战层数"/><br/>
	填写历史最高层数：<input type="text"  id="updateHighLevel">
	<input type="button" onclick="go('updateHighLevel')" value="修改历史最高挑战层数"/><br/><br/>
	
	<%	
	
		ChongLouRecord record = HibernateUtil.find(ChongLouRecord.class, junZhu.id);
		if(record == null) {
			record = ChongLouMgr.inst.insertChongLouRecord(junZhu);
		}
		String action = request.getParameter("action");
		if("saodang".equals(action)) {
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
				BigSwitch.inst.route(PD.CHONG_LOU_SAO_DANG_REQ, null, fs);
			//	fs.wait();
			}
			ProtobufMsg resp = (ProtobufMsg)fs.getAttachment();
			ChongLouSaoDangResp.Builder respBuilder = (ChongLouSaoDangResp.Builder)resp.builder;
			int result = respBuilder.getResult();
			out.println("扫荡结果：" + result);
			tableStart();
			trS();td("itemType");td("itemId");td("itemNum");trE();
			List<ChongLouSaoDangAward> list = respBuilder.getAwardsList();
			if(list == null || list.size() == 0) {
				out.println("");
			}
			for(ChongLouSaoDangAward award : list) {
				trS();td(award.getItemType());td(award.getItemId());td(award.getItemNum());trE();
			}
			tableEnd();
			//out.println(resp.builder);
			
		} else if("updateCurLevel".equals(action)) {
			String value = request.getParameter("value");
			if(value == null || value.equals("")) {
				out.println("Error : 请填写要修改的层数");
				return;
			}
			record.currentLevel = Integer.parseInt(value);
			HibernateUtil.save(record);
		} else if("updateHighLevel".equals(action)) {
			String value = request.getParameter("value");
			if(value == null || value.equals("")) {
				out.println("Error : 请填写要修改的层数");
				return;
			}
			record.highestLevel = Integer.parseInt(value);
			Redis.getInstance().set(ChongLouMgr.inst.CACHE_CHONGLOU_HIGHEST_LAYER + junZhu.id, String.valueOf(record.highestLevel));
			HibernateUtil.save(record);
		}
		
		out.println("<br/><br/>");
		
		tableStart();
		trS();td("君主");td("当前挑战层数");td("历史最高挑战层数");td("最后一次挑战时间");trE();
		trS();td(junZhu.name);td(record.currentLevel);td(record.highestLevel);
			  	td(record.lastBattleTime == null? "还未挑战过":record.lastBattleTime);trE();
		tableEnd();		
		
		/*   调试用代码
		Redis.getInstance().del(RankingMgr.CHONGLOU_RANK+"_0");
		Set<String> set = Redis.getInstance().zrange(RankingMgr.CHONGLOU_RANK+"_0", 0, 20);
		if(set == null) {
			out.println("没有数据");
		}else {
			out.println("数据大小："+set.size());
			for(String id : set) {
				out.println(id + " ");
			}
		}
		**/
			
	%>
</body>
</html>