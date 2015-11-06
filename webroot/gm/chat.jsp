<%@page import="com.manu.dynasty.chat.SensitiveFilter"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.qx.account.AccountManager"%>
<%@page import="com.qx.account.FunctionOpenMgr"%>
<%@page import=" qxmobile.protobuf.Chat.JoinToBlacklist"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="com.manu.network.SessionUser"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.manu.dynasty.chat.ChatMgr"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="java.util.Set"%>
<%@page import="com.qx.alliance.AlliancePlayer"%>
<%@page import="com.qx.alliance.AllianceBean"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
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
<title>Insert title here</title>
</head>
<body>
	<%
	BigSwitch.inst.accMgr.getSensitiveWord().add("尖阁列岛");
	SensitiveFilter.getInstance().initKeyWord();
		String action = request.getParameter("action");
		if(action != null ) {
			if(action.equals("pingbi")) {
				String blackedIdStr = request.getParameter("pingbiId");
				String operIdStr = request.getParameter("id");
				if(blackedIdStr == null || blackedIdStr.equals("") ||
						operIdStr == null || operIdStr.equals("")) {
					out.println("有内容为空！");
					return;
				}
				
				long blackedId = Long.parseLong(blackedIdStr.trim());
				long operId = Long.parseLong(operIdStr.trim());
				JunZhu blacked = HibernateUtil.find(JunZhu.class, blackedId);
				if(blacked == null) {
					out.println("被屏蔽的玩家不存在");
					return;
				}
				JunZhu junzhu = HibernateUtil.find(JunZhu.class, operId);
				if(junzhu == null) {
					out.println("玩家不存在");
					return;
				}
				RobotSession serverSession = new RobotSession();
				serverSession.setAttribute(SessionAttKey.junZhuId, operId);
				JoinToBlacklist.Builder req = JoinToBlacklist.newBuilder();
				req.setJunzhuId(blackedId);
				boolean isSuccess = ChatMgr.inst.joinBlacklist(PD.C_JOIN_BLACKLIST, serverSession, req,false);
				if(isSuccess) {
					out.println("添加成功");
				} else {
					out.println("添加失败");
				}
			}
		}
		
	
	%>

  <form action="">
	  	被屏蔽君主Id：<input type="text" name="pingbiId" ><br/>
	  	君主Id：<input type="text" name="id" ><br/>
	  	<input type="hidden" name="action" value="pingbi" ><br/>
	  	<button type="submit">确定</button>
  </form>
  	
  	查看黑名单列表：
  	 <form action="">
	  	君主Id：<input type="text" name="id" ><br/>
	  	<input type="hidden" name="action" value="getBlacklist" ><br/>
	  	<button type="submit">确定</button>
 	 </form>
    <%
    		
	if(action != null ) {
		if(action.equals("getBlacklist")) {
			String operIdStr = request.getParameter("id");
			if(operIdStr == null || operIdStr.equals("")) {
				out.println("有内容为空！");
				return;
			}
			long operId = Long.parseLong(operIdStr.trim());
			JunZhu junzhu = HibernateUtil.find(JunZhu.class, operId);
			if(junzhu == null) {
				out.println("玩家不存在");
				return;
			}
			Set<String> ids = Redis.getInstance().sget("ChatBlackList:id:" + junzhu.id);
			if(ids == null || ids.size() == 0) {
				out.println("黑名单为空");
			} else {
				tableStart();
				ths("君主id,君主名字,等级,iconId,联盟名");
				
				for(String id : ids) {
					JunZhu blacker = HibernateUtil.find(JunZhu.class, Long.parseLong(id));
					AlliancePlayer member = HibernateUtil.find(AlliancePlayer.class, blacker.id);
					String lianmengName = "无";
					if(member == null || member.lianMengId <= 0) {
					} else {
						AllianceBean alnc = HibernateUtil.find(AllianceBean.class, member.lianMengId);
						lianmengName = alnc.name;
					}
					trS();
					td(blacker.id);td(blacker.name); td(blacker.level);td(blacker.level);td(lianmengName);
					trE();
				}
				tableEnd();
				br();
			}
		}
	}
    
    %>
</body>
</html>