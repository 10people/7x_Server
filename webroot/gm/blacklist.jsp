<%@page import="com.manu.dynasty.chat.ChatMgr"%>
<%@page import="qxmobile.protobuf.Chat.BlackJunzhuInfo"%>
<%@page import="qxmobile.protobuf.Chat.GetBlacklistResp"%>
<%@page import="qxmobile.protobuf.Chat.BlacklistResp"%>
<%@page import="qxmobile.protobuf.Chat.CancelBlack"%>
<%@page import="qxmobile.protobuf.Chat.JoinToBlacklist"%>
<%@page import="qxmobile.protobuf.FriendsProtos.GetFriendListReq"%>
<%@page import="qxmobile.protobuf.FriendsProtos.FriendResp"%>
<%@page import="qxmobile.protobuf.FriendsProtos.RemoveFriendReq"%>
<%@page import="qxmobile.protobuf.FriendsProtos.GetFriendListResp"%>
<%@page import="com.manu.network.msg.ProtobufMsg"%>
<%@page import="qxmobile.protobuf.FriendsProtos.AddFriendReq"%>
<%@page import="qxmobile.protobuf.FriendsProtos.FriendJunzhuInfo"%>
<%@page import="java.util.List"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="org.apache.mina.core.session.IoSession"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>好友管理</title>
</head>
<body>
	<%
	// 屏蔽玩家
	String addName = request.getParameter("addName");
	String junzhuName = request.getParameter("junzhuName");
	// 查询屏蔽玩家
	String name = request.getParameter("name");
	String ownerid = request.getParameter("ownerid");
	name = name == null ? "": name.trim();
	ownerid = (ownerid == null ? "":ownerid.trim());
	if(session.getAttribute("name") != null && name.length()==0 && ownerid.length()==0){
		name = (String)session.getAttribute("name");
	}
	// 解除屏蔽玩家
	String rmId = request.getParameter("rmId");
	String rmJunzhuId = request.getParameter("rmJunzhuId");
	// 修改屏蔽玩家上限 
	String maxnum = request.getParameter("maxnum");
	%>
	<form action="">
		账号：<input type="text"  name="junzhuName"/> 
		<input type="hidden" name="name" value="<%=name%>"/>
		<button type="submit" >屏蔽</button>  账号：<input type="text"  name="addName"/>
	</form>
	<%
	if(addName != null && addName.length()>0&&junzhuName != null && junzhuName.length()>0) {
		Account account = HibernateUtil.getAccount(junzhuName);
		Account addAccount = HibernateUtil.getAccount(addName);
		if(account!=null&&addAccount!=null){
			final IoSession fs = new RobotSession(){
				public WriteFuture write(Object message){
					setAttachment(message);
					synchronized(this){
						this.notify();
					}
					return null;
				}
			};
			fs.setAttribute(SessionAttKey.junZhuId, Long.valueOf(account.accountId*1000+GameServer.serverId));
			JoinToBlacklist.Builder builder = JoinToBlacklist.newBuilder();
			builder.setJunzhuId(addAccount.accountId*1000+GameServer.serverId);
			synchronized(fs){
				BigSwitch.inst.route(PD.C_JOIN_BLACKLIST, builder, fs);
			//	fs.wait();
			}
			ProtobufMsg msg = (ProtobufMsg)fs.getAttachment();
			BlacklistResp.Builder resp = (BlacklistResp.Builder)msg.builder; 
			if(resp.getResult()==0){
				%><p><%=junzhuName %>屏蔽<%=addName %>成功</p><%
			} else{
				switch((int)resp.getResult()){
				case 101:
					%><p><%=junzhuName %>屏蔽<%=addName %>失败，已经屏蔽该玩家</p><%
					break;
				case 102:
					%><p><%=junzhuName %>屏蔽<%=addName %>失败，不能屏蔽自己</p><%
					break;
				case 103:
					%><p><%=junzhuName %>屏蔽<%=addName %>失败，屏蔽玩家数量达到上限</p><%
					break;
				case 104:
					%><p><%=junzhuName %>屏蔽<%=addName %>失败，要屏蔽的君主不存在</p><%
					break;
				default:
					break;
				}
				
			}
		}
	} 
	%>
	=========================================================================
	<%
	if(rmId != null && rmId.length()>0&&rmJunzhuId != null && rmJunzhuId.length()>0){
		final IoSession fs = new RobotSession(){
			public WriteFuture write(Object message){
				setAttachment(message);
				synchronized(this){
					this.notify();
				}
				return null;
			}
		};
		fs.setAttribute(SessionAttKey.junZhuId, Long.valueOf(rmId));
		CancelBlack.Builder builder = CancelBlack.newBuilder();
		builder.setJunzhuId(Long.valueOf(rmJunzhuId));
		synchronized(fs){
			BigSwitch.inst.route(PD.C_CANCEL_BALCK, builder, fs);
		//	fs.wait();
		}
		ProtobufMsg msg = (ProtobufMsg)fs.getAttachment();
		BlacklistResp.Builder resp = (BlacklistResp.Builder)msg.builder;
		if(resp.getResult()==0){// 取消成功
			%><p>解除屏蔽成功</p><%
		} else{// 取消失败
			switch((int)resp.getResult()){
			case 104:
				%><p>解除屏蔽失败，要解除的君主不存在</p><%
				break;
			case 105:
				%><p>解除屏蔽失败，君主没有被屏蔽</p><%
				break;
			default:
				break;
			}
		}
	}
	%>
	<form action="">
		按账号：<input type="text"  name="name"/> 或君主id：<input type="text"  name="ownerid"/>
		<button type="submit" >查询屏蔽玩家</button>
	</form>
	<%
	Account account = null;
	if(name != null && name.length()>0){
		account = HibernateUtil.getAccount(name);
		if(account!=null){
			ownerid = ""+(account.accountId*1000+GameServer.serverId);
		}
	}else if(ownerid != null && ownerid.length()>0){
		account = HibernateUtil.find(Account.class, (Long.valueOf(ownerid) - GameServer.serverId) / 1000);
	}
	if(account == null){
		%><p>没有找到账号</p><%
	}else{ 
		%>账号<%=ownerid%>:<%=account.accountName%>
		<% 
		session.setAttribute("name", name);
		final IoSession fs = new RobotSession(){
			public WriteFuture write(Object message){
				setAttachment(message);
				synchronized(this){
					this.notify();
				}
				return null;
			}
		};
		fs.setAttribute(SessionAttKey.junZhuId, Long.valueOf(ownerid));
		synchronized(fs){
			BigSwitch.inst.route(PD.C_GET_BALCKLIST, null, fs);
		//	fs.wait();
		}
		ProtobufMsg msg = (ProtobufMsg)fs.getAttachment();
		GetBlacklistResp.Builder resp = (GetBlacklistResp.Builder)msg.builder;
		%>
		<%
		 List<BlackJunzhuInfo> list = (List<BlackJunzhuInfo>)resp.getJunzhuInfoList(); %>
		<p>共有屏蔽玩家<%=list.size()%>/<%=resp.getBlackMax() %>个<p>
	<table border="1">
		<tr><th>君主id</th><th>头像id</th><th>名字</th><th>联盟名字</th><th>等级</th><th>vip等级</th><th>百战军衔</th><th>国家</th><th>战利</th><th>操作</th></tr>
		<%
		for(BlackJunzhuInfo info:list){
		%>
		<tr>
		<td><%=info.getJunzhuId() %></td>
		<td><%=info.getIconId() %></td>
		<td><%=info.getName() %></td>
		<td><%=info.getLianMengName() %></td>
		<td><%=info.getLevel() %></td>
		<td><%=info.getVipLv() %></td>
		<td><%=info.getJunXian() %></td>
		<td><%=info.getGuojia() %></td>
		<td><%=info.getZhanLi() %></td>
		<td>
		<form action="">
			<input type="hidden" name="name" value="<%=name%>"/>
			<input type="hidden" name="rmId" value="<%=ownerid%>"/>
			<input type="hidden" name="rmJunzhuId" value="<%=info.getJunzhuId()%>"/>
			<button type="submit">解除屏蔽</button>
		</form>
		</td>
		</tr>
		<%} %>
	</table>
	<%} %>
	<form action="">
		<p>屏蔽玩家数量上限： <input type="text" name="maxnum" value="" /><button type="submit">修改</button></p>
	</form>
	<%
		if(maxnum!=null&&maxnum.length()>0){
			ChatMgr.MAX_BlACK_NUM = Integer.parseInt(maxnum);
			%><p>屏蔽玩家上限被修改为<%=ChatMgr.MAX_BlACK_NUM %></p><%
		}
	%>
</body>
</html>