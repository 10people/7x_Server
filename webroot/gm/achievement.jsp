<%@page import="java.util.ArrayList"%>
<%@page import="com.qx.achievement.AchievementCondition"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.manu.dynasty.template.ExpTemp"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.qx.achievement.Achievement"%>
<%@page import="com.qx.achievement.AchievementMgr"%>
<%@page import="com.qx.event.Event"%>
<%@page import="com.qx.event.ED"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>query or delete email</title>
<script type="text/javascript">
function go(act, id, type){
	if(act == null || act == "") {
		alert("act不能为空");
		return;
	}
	//if(type == null || act == "") {
	//	alert("act不能为空");'
		//return;
	//}
	
	var node = document.getElementById(act+id);
	var v = node.value;
	location.href = '?action='+act+"&v="+v +"&t="+type;
}
</script>

</head>
<body>
	<%
		String name = request.getParameter("account");
			name = name == null ? "" : name;
			if (session.getAttribute("name") != null && name.length() == 0) {
		name = (String) session.getAttribute("name");
			}
	%>
	<form action="">
		账号<input type="text" name="account" value="<%=name%>"><br>
		<button type="submit">查询</button>
	</form>
	<%
		if (name != null && name.length() > 0) {
		Account account = HibernateUtil.getAccount(name);
		if (account == null) {
	%>没有找到<%
		//HibernateUtil.saveAccount(name);
		} else {
			session.setAttribute("name", name);
	%>账号<%=account.getAccountId()%>:<%=account.getAccountName()%>
	<%
		JunZhu junzhu = HibernateUtil.find(JunZhu.class, (long) account.getAccountId());
			List<Achievement> acheList = null;
			if (junzhu == null) {
				out.println("没有君主");
			} else {
				if (junzhu.level == 0 || junzhu.shengMingMax == 0) {
					JunZhuMgr.inst.fixCreateJunZhu((int)junzhu.id, junzhu.name, junzhu.roleId, junzhu.guoJiaId);
				}
				String action = request.getParameter("action");
				
				if ("reqAcheList".equals(action)) {
					String v = request.getParameter("v");
				} else if("addJindu".equals(action)) {
					int jinduAdd = Integer.parseInt(request.getParameter("v"));
					int type = Integer.parseInt(request.getParameter("t"));
					AchievementMgr.instance.acheProcess(new AchievementCondition(junzhu.id, type, jinduAdd));
				}
				JunZhuMgr.inst.calcJunZhuTotalAtt(junzhu);
				out.println("&nbsp;君主id：" + junzhu.id);
				out.println("<br/>");
				ExpTemp expTemp = TempletService.getInstance().getExpTemp(1, junzhu.level);
				out.println("等级：" + junzhu.level + "<br/><hr/>");
				acheList = HibernateUtil.list(Achievement.class, " where junZhuId =" + junzhu.id);
				if(acheList == null || acheList.size() == 0){
					acheList = new ArrayList<Achievement>();
				}
				
				out.append("注释 type说明：1参加过关次数;2参加百战次数;3参加野战次数;4洗练次数;5获得武将个数;<br/>" +
						"6~10白绿蓝紫橙满星武将个数;11初级科技等级;12高级科技等级;13君主等级;14~21经脉等级;<br/>"+
						"22野战战胜玩家次数;23野战复仇成功次数;24百战最高军衔;25百战进度;26~42 第2章~第18章星级累计个数<br/><hr/>"+
						"是否完成成就：1，完成。2，未完成。<br/><hr/> 	 是否领取奖励：1，已领取。2，未领取。<hr/>");
				//成就列表
				out.append("<table border='1'>");
				out.append("<tr>");
				out.append("<th>id</th><th>成就 ID</th><th>玩家id</th><th>是否完成该成就</th><th>是否领取奖励</th><th>进度</th><th>类型</th>");
				out.append("</tr>\n");
				int lastHeroId = 0;
				
				for(Achievement ache : acheList){
					out.append("<tr>");
					out.append("<td>"+ache.getId()+"</td>");		
					out.append("<td>"+ache.getChengjiuId()+"</td>");
					out.append("<td>"+ache.getJunZhuId()+"</td>");
					out.append("<td>"+ache.isFinish()+"</td>");
					out.append("<td>"+ache.isGetReward()+"</td>");
					out.append("<td>"+ache.getJindu()+"</td>");
					out.append("<td>"+ache.getType()+"</td>");
					out.append("<td><input type='text' value='' id='addJindu"+ache.getId()+"'/>");
					out.append("<input type='button' value='增加' onclick='go(\"addJindu\","+ache.getId() +"," + ache.getType() +")'/>");
					out.append("<tr>\n");
				}
				out.append("</table>");
			}
		}
			}
	%>
</body>
</html>