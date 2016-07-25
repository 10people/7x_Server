<%@page import="java.util.Map"%>
<%@page import="com.google.protobuf.InvalidProtocolBufferException"%>
<%@page import="qxmobile.protobuf.Chat.ChatPct"%>
<%@page import="java.util.Date"%>
<%@page import="com.manu.dynasty.chat.ChatInfo"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.manu.dynasty.chat.SensitiveFilter"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.qx.account.AccountManager"%>
<%@page import="com.qx.account.FunctionOpenMgr"%>
<%@page import=" qxmobile.protobuf.Chat.JoinToBlacklist"%>
<%@page import="com.qx.robot.RobotSession"%>
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
				name = account.accountName;
		}
		long junZhuId = 0;
		if (account != null) {
			session.setAttribute("name", name);
			out("账号");
			out(account.accountId);
			out("：");
			out(account.accountName);
			out(", 密码：");
			out(account.accountPwd);
			junZhuId = account.accountId * 1000 + GameServer.serverId;
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
				JunZhu jz = HibernateUtil.find(JunZhu.class, operId);
				if(jz == null) {
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

    <%
    		
	if(action != null ) {
		if(action.equals("getBlacklist")) {
			String operIdStr = request.getParameter("id");
			if(operIdStr == null || operIdStr.equals("")) {
				out.println("有内容为空！");
				return;
			}
			long operId = Long.parseLong(operIdStr.trim());
			JunZhu jz = HibernateUtil.find(JunZhu.class, operId);
			if(jz == null) {
				out.println("玩家不存在");
				return;
			}
			Set<String> ids = Redis.getInstance().sget("ChatBlackList:id:" + jz.id);
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
		} else if("updateUseTimes".equals(action)) {
			ChatInfo chatInfo = HibernateUtil.find(ChatInfo.class, junzhu.id);
			if(chatInfo == null) {
				chatInfo = new ChatInfo();
				chatInfo.jzId = junzhu.id;
				chatInfo.lastTime = new Date();
				chatInfo.useTimes = 1;
				HibernateUtil.insert(chatInfo);
			}
			String value = request.getParameter("v");
			if(value == null || value.equals("")) {
				out.println("异常：填写的值不能为空");
				return;
			}
			chatInfo.useTimes = Integer.parseInt(value);
			HibernateUtil.save(chatInfo);
			IoSession ioSession = SessionManager.inst.getIoSession(junzhu.id);
			if(ioSession != null) {
				ChatMgr.inst.sendChatConf(0, ioSession, null);
			}
		}
	}
    
    %>
    
    
    <%
		ChatInfo info = HibernateUtil.find(ChatInfo.class, junzhu.id);
		if(info == null) {
			info = new ChatInfo();
			info.jzId = junzhu.id;
			info.lastTime = new Date();
			info.useTimes = 1;
			HibernateUtil.insert(info);
		}
		out.println("世界聊天已经使用的次数:" + info.useTimes);
		out.println("<input type='text' id='updateUseTimes' />");
		out.println("<input type='button' id='updateUseTimes' value='修改' onclick='go(\"updateUseTimes\")'/><br/><br/>");
	
	%>
	
	 <form action="">
	  	被屏蔽君主Id：<input type="text" name="pingbiId" ><br/>
	  	君主Id：<input type="text" name="id" ><br/>
	  	<input type="hidden" name="action" value="pingbi" >
	  	<button type="submit">确定</button>
  	</form>
  	
  		查看黑名单列表：
  	 <form action="">
	  	君主Id：<input type="text" name="id" ><br/>
	  	<input type="hidden" name="action" value="getBlacklist" >
	  	<button type="submit">确定</button>
 	 </form>
 	 <br/>
 	<form action="">
	  	频道：<input type="text" name="chId" >1:私聊，2：世界，3：广播，4：联盟<br/>
	  	seq:<input type="text" name="seq" ><br/>
	  	<input type="hidden" name="action" value="getSound" >
	  	<button type="submit">查询</button>
 	 </form>
 	 <%
 	 	if(action != null && action.equals("getSound")) {
 	 		String chId = request.getParameter("chId");
 	 		int chIdInt = Integer.parseInt(chId);
 	 		String seq = request.getParameter("seq");
 	 		String key = "";
 	 		switch(chIdInt) {
 	 		case 1:
 	 			key = ChatMgr.getInst().chSiLiao.key;
 	 			break;
 	 		case 2:
 	 			key = ChatMgr.getInst().chWorld.key;
 	 			break;
 	 		case 3:
 	 			key = ChatMgr.getInst().chBroadcast.key;
 	 			break;
 	 		case 4:
 	 			key = ChatMgr.getInst().chLianMeng.key;
 	 			break;
 	 		}
 	 		
 			byte[] soundBytes = Redis.getInstance().hget(key.getBytes(), String.valueOf(seq).getBytes());
 			if(soundBytes == null || soundBytes.length == 0) {
	 			out.println("没有语音数据");
 			} else {
	 			ChatPct.Builder chatPct = ChatPct.newBuilder();
	 			try {
	 				chatPct.mergeFrom(soundBytes);
	 			} catch (InvalidProtocolBufferException e) {
					out.println("查询出错");
	 			} 
	 			String sound = chatPct.getSoundData();
	 			if(sound == null || sound.equals("")) {
		 			out.println("语音长度为0");
	 			} else {
		 			out.println("长度：" + sound.length() + "<br/>");
		 			out.println("语音：" + sound);
	 			}
 			}
 	 	}
 	 
 	 %>
 	 
 	 <br/><br/>
 	 <form action="">
 	 	1：世界，2：联盟，3：私聊<br/>
	  	聊天频道：<input type="text" name="ch" />
	  	<input type="hidden" name="action" value="getChatDatas" >
	  	<button type="submit">查询聊天数据</button>
 	 </form>
 	 
 	 <%
	 	if(action != null && action.equals("getChatDatas")) {
	 		String ch = request.getParameter("ch");
	 		int chInt = Integer.parseInt(ch);
	 		String key = "";
	 		switch(chInt) {
	 		case 1:		key = ChatMgr.getInst().chWorld.key; 		break;
	 		case 2:		key = ChatMgr.getInst().chLianMeng.key; 	break;
	 		case 3:		key = ChatMgr.getInst().chSiLiao.key; 		break;
	 		}
	 		
	 		Map<byte[], byte[]> dataMap = Redis.getInstance().hgetAll(key.getBytes());
	 		tableStart();
	 			trS();td("seq");td("聊天文字内容");td("聊天语音长度");td("发送君主id");trE();
	 			Set<byte[]> keys = dataMap.keySet();
	 			for(byte[] keybyte : keys) {
	 				trS();
	 				td(new String(keybyte));
	 				byte[] datas = dataMap.get(keybyte);
					if(datas == null) {
			 			td("保存的聊天数据为null");
					} else {
		 				ChatPct.Builder chatPct = ChatPct.newBuilder();
		 				chatPct.mergeFrom(datas);
			 			td(chatPct.getContent());
			 			td(chatPct.getSoundData().length());
			 			td(chatPct.getSenderId());
					}
		 			trE();
	 			}
	 		
	 		tableEnd();
	 	}
 	 %>
</body>
</html>