<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="java.util.Set"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.friends.FriendMgr"%>
<%@page import="com.manu.dynasty.chat.ChatMgr"%>
<%@page import="qxmobile.protobuf.FriendsProtos.GetFriendListReq"%>
<%@page import="qxmobile.protobuf.FriendsProtos.FriendResp"%>
<%@page import="qxmobile.protobuf.FriendsProtos.RemoveFriendReq"%>
<%@page import="qxmobile.protobuf.FriendsProtos.GetFriendListResp"%>
<%@page import="com.manu.network.msg.ProtobufMsg"%>
<%@page import="qxmobile.protobuf.PvpProto.AddChanceResp"%>
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
	// 关注好友
	String addName = request.getParameter("addName");
	String junzhuName = request.getParameter("junzhuName");
	// 查询关注好友
	String name = request.getParameter("name");
	String ownerid = request.getParameter("ownerid");
	String pageNo = request.getParameter("pageNo");
	String pageSize = request.getParameter("pageSize");
	name = name == null ? "": name.trim();
	ownerid = (ownerid == null ? "":ownerid.trim());
	if(session.getAttribute("name") != null && name.length()==0 && ownerid.length()==0){
		name = (String)session.getAttribute("name");
	}
	pageNo = (null==pageNo?"1":pageNo);// 默认第1页
	pageSize = (null==pageSize?"5":pageSize);// 默认1页5条
	// 取消关注好友
	String rmId = request.getParameter("rmId");
	String rmJunzhuId = request.getParameter("rmJunzhuId");
	// 修改好友上限 
	String maxnum = request.getParameter("maxnum");
	%>
	<form action="">
		账号：<input type="text"  name="junzhuName"/> 
		<input type="hidden" name="name" value="<%=name%>"/>
			<input type="hidden" name="pageNo" value="<%=pageNo%>"/>
			<input type="hidden" name="pageSize" value="<%=pageSize%>"/>
		<button type="submit" >关注</button>  账号：<input type="text"  name="addName"/>
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
			fs.setAttribute(SessionAttKey.junZhuId, Long.valueOf(account.getAccountId()*1000+GameServer.serverId));
			AddFriendReq.Builder builder = AddFriendReq.newBuilder();
			builder.setJunzhuId(addAccount.getAccountId()*1000+GameServer.serverId);
			synchronized(fs){
				BigSwitch.inst.route(PD.C_FRIEND_ADD_REQ, builder, fs);
			//	fs.wait();
			}
			ProtobufMsg msg = (ProtobufMsg)fs.getAttachment();
			FriendResp.Builder resp = (FriendResp.Builder)msg.builder; 
			if(resp.getResult()==0){
				%><p><%=junzhuName %>关注<%=addName %>成功</p><%
			} else{
				switch((int)resp.getResult()){
				case 101:
					%><p><%=junzhuName %>关注<%=addName %>失败，已经关注该好友，请不要重复关注</p><%
					break;
				case 102:
					%><p><%=junzhuName %>关注<%=addName %>失败，不能关注自己</p><%
					break;
				case 103:
					%><p><%=junzhuName %>关注<%=addName %>失败，好友数量达到上限</p><%
					break;
				case 104:
					%><p><%=junzhuName %>关注<%=addName %>失败，要关注的君主不存在</p><%
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
		RemoveFriendReq.Builder builder = RemoveFriendReq.newBuilder();
		builder.setJunzhuId(Long.valueOf(rmJunzhuId));
		synchronized(fs){
			BigSwitch.inst.route(PD.C_FRIEND_REMOVE_REQ, builder, fs);
		//	fs.wait();
		}
		ProtobufMsg msg = (ProtobufMsg)fs.getAttachment();
		FriendResp.Builder resp = (FriendResp.Builder)msg.builder;
		if(resp.getResult()==0){// 取消成功
			%><p>取消关注成功</p><%
		} else{// 取消失败
			switch((int)resp.getResult()){
			case 104:
				%><p>取消关注失败，要取消关注的君主不存在</p><%
				break;
			case 105:
				%><p>取消关注失败，君主没有被关注</p><%
				break;
			default:
				break;
			}
		}
	}
	%>
	<form action="">
		按账号：<input type="text"  name="name"  value="<%=name%>"/> 或君主id：<input type="text"  name="ownerid"/>，第<input type="text"  name="pageNo" value="<%=pageNo%>"/>页，每页<input type="text"  name="pageSize"  value="<%=pageSize%>"/>条
		<button type="submit" >分页查询好友</button>
	</form>
	<%
	Account account = null;
	if(name != null && name.length()>0){
		account = HibernateUtil.getAccount(name);
		if(account!=null){
			ownerid = ""+(account.getAccountId()*1000+GameServer.serverId);
		}
	}else if(ownerid != null && ownerid.length()>0){
		account = HibernateUtil.find(Account.class, (Long.valueOf(ownerid) - GameServer.serverId) / 1000);
	}
	if(account == null){
		%><p>没有找到账号</p><%
	}else if(pageNo != null && pageNo.length()>0&&pageSize != null || pageSize.length()>0){ 
		%>账号<%=ownerid%>:<%=account.getAccountName()%>
		<% 
		session.setAttribute("name", name);
		long junzhuId = account.getAccountId()* 1000 + GameServer.serverId;
		JunZhu junzhu = HibernateUtil.find(JunZhu.class, junzhuId);
		if(junzhu == null){
			out.append("没有君主");
			return;
		}
		Set<String> ids = Redis.getInstance().zrange(FriendMgr.CACHE_FRIEDNLIST_OF_JUNZHU + junzhuId);
		out.append("好友IDs："+ids.toString());
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
		GetFriendListReq.Builder builder = GetFriendListReq.newBuilder();
		builder.setPageNo(Integer.valueOf(pageNo));
		builder.setPageSize(Integer.valueOf(pageSize));
		synchronized(fs){
			BigSwitch.inst.route(PD.C_FRIEND_REQ, builder, fs);
		//	fs.wait();
		}
		ProtobufMsg msg = (ProtobufMsg)fs.getAttachment();
		GetFriendListResp.Builder resp = (GetFriendListResp.Builder)msg.builder;
		%>
		<p>共有好友<%=resp.getFriendCount()%>/<%=resp.getFriendMax() %>个<p>
		<%
		 List<FriendJunzhuInfo> list = (List<FriendJunzhuInfo>)resp.getFriendsList(); %>
	<table border="1">
		<tr><th>君主id</th><th>头像id</th><th>名字</th><th>联盟名字</th><th>等级</th><th>vip等级</th><th>百战军衔</th><th>国家</th><th>战利</th><th>操作</th></tr>
		<%
		for(FriendJunzhuInfo info:list){
		%>
		<tr>
		<td><%=info.getOwnerid() %></td>
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
			<input type="hidden" name="pageNo" value="<%=pageNo%>"/>
			<input type="hidden" name="pageSize" value="<%=pageSize%>"/>
			<input type="hidden" name="rmId" value="<%=ownerid%>"/>
			<input type="hidden" name="rmJunzhuId" value="<%=info.getOwnerid()%>"/>
			<button type="submit">取消关注</button>
		</form>
		</td>
		</tr>
		<%} %>
	</table>
	<%} 
	else{
	%>
	<p>没有输入分页信息</p>
	<%} %>
	<form action="">
		<p>好友玩家数量上限： <input type="text" name="maxnum" value="" /><button type="submit">修改</button></p>
	</form>
	<%
		if(maxnum!=null&&maxnum.length()>0){
			FriendMgr.MAX_FRIEND_NUM = Integer.parseInt(maxnum);
			%><p>好友玩家上限被修改为<%=FriendMgr.MAX_FRIEND_NUM %></p><%
		}
	%>
</body>
</html>