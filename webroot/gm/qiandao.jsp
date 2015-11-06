<%@page import="qxmobile.protobuf.Qiandao"%>
<%@page import="java.util.Date"%>
<%@page import="qxmobile.protobuf.Qiandao.QiandaoAward"%>
<%@page import="java.util.List"%>
<%@page import="com.qx.activity.QiandaoMgr"%>
<%@page import="qxmobile.protobuf.Qiandao.QiandaoResp"%>
<%@page import="qxmobile.protobuf.Qiandao.GetQiandaoResp"%>
<%@page import="com.manu.network.msg.ProtobufMsg"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="org.apache.mina.core.session.IoSession"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>签到管理</title>
</head>
<body>
	<form>
		<p>
			账号： <input type="text" name="qname"/> 或 君主id： <input type="text" name="qownerid"/> <button type="submit">签到</button>
		</p>
		<p>
			账号： <input type="text" name="name"/> 或 君主id： <input type="text" name="ownerid"/> <button type="submit">查看</button>
		</p>
	</form>
	<%
	// 日期测试开关 
	String debugDate = request.getParameter("debugDate");
	if(debugDate!=null&&debugDate.length()>0){
		QiandaoMgr.DATE_DEBUG = Boolean.valueOf(debugDate);
	}
	if(!QiandaoMgr.DATE_DEBUG){
		%>
		<p>日期调试：<a href="qiandao.jsp?debugDate=true">开启</a>|关闭</p>
		<%
	} else{
		%>
		<p>日期调试：开启|<a href="qiandao.jsp?debugDate=false">关闭</a></p>
		<%
	}
	
	%>
	
	<%
	String year = request.getParameter("year");
	String month = request.getParameter("month");
	String day = request.getParameter("day");
	String hour = request.getParameter("hour");
	%>
	<%
	if(year!=null&&year.length()>0&&month!=null&&month.length()>0&&day!=null&&day.length()>0){
		// 修改日期
		Date date = QiandaoMgr.debugDate;
		date.setYear(Integer.valueOf(year)-1900);
		date.setMonth(Integer.valueOf(month)-1);
		date.setDate(Integer.valueOf(day));
		date.setHours(Integer.valueOf(hour));
	}
	%>
	<%
	if(QiandaoMgr.DATE_DEBUG){
		String refreshDate = request.getParameter("refreshDate");
		if(refreshDate!=null&&refreshDate.length()>0){
			QiandaoMgr.debugDate = new Date();// 刷新调试日期
		}
		%>
		<p>
		当前调试日期是：<%=QiandaoMgr.debugDate.getYear()+1900 %>年-<%=QiandaoMgr.debugDate.getMonth()+1 %>月-<%=QiandaoMgr.debugDate.getDate() %>日 <%=QiandaoMgr.debugDate.getHours() %>点<a href="qiandao.jsp?refreshDate=1">刷新日期</a>
		</p>
		
		<p>
		<form action="">
			<input type="number" name="year" value="<%=QiandaoMgr.debugDate.getYear()+1900 %>">年
			<input type="number" name="month" value="<%=QiandaoMgr.debugDate.getMonth()+1 %>">月
			<input type="number" name="day" value="<%=QiandaoMgr.debugDate.getDate() %>">日
			<input type="number" name="hour" value="<%=QiandaoMgr.debugDate.getHours() %>">点
			<button type="submit">修改调试日期</button>
		</form>
		</p>
		<%
	}
	%>
	
	<%
	// 查看
	String name = request.getParameter("name");
	String ownerid = request.getParameter("ownerid");
	name = name == null ? "": name.trim();
	ownerid = (ownerid == null ? "":ownerid.trim());
	if(session.getAttribute("name") != null && name.length()==0 && ownerid.length()==0){
		name = (String)session.getAttribute("name");
	}
	// 签到
	String qname = request.getParameter("qname");
	String qownerid = request.getParameter("qownerid");
	
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
		%><p>查看没有找到账号</p><%
	}else{
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
		fs.setAttribute(SessionAttKey.junZhuId, Long.valueOf(account.getAccountId()*1000+GameServer.serverId));
		synchronized(fs){
			BigSwitch.inst.route(PD.C_GET_QIANDAO_REQ, null, fs);
		//	fs.wait();
		}
		ProtobufMsg msg = (ProtobufMsg)fs.getAttachment();
		GetQiandaoResp.Builder resp = (GetQiandaoResp.Builder)msg.builder;
		%>
		<table border="1">
			<tr><th>君主id</th><th>君主名字</th><th>累计签到天数</th><th>今天日期</th></tr>
			<tr>
			<td><%=account.getAccountId()*1000+GameServer.serverId %></td>
			<td><%=account.getAccountName() %></td>
			<td><%=resp.getCnt() %></td>
			<td><%=""+resp.getCurDate() +"号"%></td>
			</tr>
		</table>
		<br>
		<table border="1">
			<tr><td colspan="8"><center><%=resp.getAwardList().size()!=0?resp.getAwardList().get(0).getMonth()+"月奖励列表":"没有配置奖励" %></center></td></tr>
			<tr><th>id</th><th>月份</th><th>日期</th><th>奖励类型</th><th>奖励id</th><th>奖励数量</th><th>双倍奖励领取等级</th><th>是否签到</th></tr>
		<%
		List<QiandaoAward> list = resp.getAwardList();
		for(QiandaoAward award:list){
		%>
				<tr>
					<td><%=award.getId() %></td>
					<td><%=award.getMonth() %></td>
					<td><%=award.getDay() %></td>
					<td><%=award.getAwardType() %></td>
					<td><%=award.getAwardId() %></td>
					<td><%=award.getAwardNum() %></td>
					<td><%=award.getVipDouble() %></td>
					<td><%=award.getState() %></td>
				</tr>
		<%
		}%>
		</table>
		<%
	}
	%>
	<%
	Account account2 = null;
	if(qname != null && qname.length()>0){
		account2 = HibernateUtil.getAccount(qname);
		if(account2!=null){
			qownerid = ""+(account2.getAccountId()*1000+GameServer.serverId);
		}
	}else if(qownerid != null && qownerid.length()>0){
		account2 = HibernateUtil.find(Account.class, (Long.valueOf(qownerid) - GameServer.serverId) / 1000);
	}
	if(account2 == null){
		%><p>签到没有找到账号</p><%
	}else{
		final IoSession fs = new RobotSession(){
			public WriteFuture write(Object message){
				setAttachment(message);
				synchronized(this){
					this.notify();
				}
				return null;
			}
		};
		fs.setAttribute(SessionAttKey.junZhuId, Long.valueOf(account2.getAccountId()*1000+GameServer.serverId));
		synchronized(fs){
			BigSwitch.inst.route(PD.C_QIANDAO_REQ, null, fs);
		//	fs.wait();
		}
		ProtobufMsg msg = (ProtobufMsg)fs.getAttachment();
		QiandaoResp.Builder resp = (QiandaoResp.Builder)msg.builder;
		switch(resp.getResult()){
		case QiandaoMgr.SUCCESS:
			%>
			<p>今日签到成功</p>
			<table border="1">
				<tr><td colspan="7">签到奖励</td></tr>
				<tr><th>id</th><th>月份</th><th>日期</th><th>奖励类型</th><th>奖励id</th><th>奖励数量</th><th>双倍奖励等级</th><th>是否签到</th></tr>
				<%
				List<QiandaoAward> list = resp.getAwardList();
				for(QiandaoAward award:list){
				%>
					<tr>
						<td><%=award.getId() %></td>
						<td><%=award.getMonth() %></td>
						<td><%=award.getDay() %></td>
						<td><%=award.getAwardType() %></td>
						<td><%=award.getAwardId() %></td>
						<td><%=award.getAwardNum() %></td>
						<td><%=award.getVipDouble() %></td>
						<td><%=award.getState() %></td>
					</tr>
				<%
				}
				%>
			</table>
			<%
			break;
		case QiandaoMgr.ERROR_EXIST:
			%><p>今日已签到过</p><%
			break;
		default:
			break;
		}
	}
	%>
	</body>
</html>