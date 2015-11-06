<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.alliance.AllianceBean"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="com.qx.ranking.RankingMgr"%>
<%@page import="qxmobile.protobuf.Ranking.LianMengInfo"%>
<%@page import="java.util.List"%>
<%@page import="qxmobile.protobuf.Ranking.RankingResp"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="qxmobile.protobuf.Ranking.RankingReq"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="org.apache.mina.core.session.IoSession"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<%
String refresh = request.getParameter("refresh");
if(refresh!=null&&refresh.length()>0){
	List<AllianceBean> list = HibernateUtil.list(AllianceBean.class,"where creatorId like '%"+GameServer.serverId+"'");
	for(AllianceBean alliance:list){
		RankingMgr.inst.resetLianMengRankRedis(alliance.id);
		RankingMgr.inst.resetLianmengSWDayRankRedis(alliance, 0);
		RankingMgr.inst.resetLianmengSWWeekRankRedis(alliance, 0);
	}
	response.sendRedirect("ranking.jsp");
}
%>
<form action="">
<input type="hidden"  name="refresh" value="1"/>
<button type="submit">刷新排序</button>
</form>


<%
String name = request.getParameter("name");
if(name!=null){
	name=new String(name.getBytes("ISO-8859-1"),"UTF-8"); 
}
String pageNoStr = request.getParameter("pageNo");
String guojiaIdStr = request.getParameter("guojiaId");
String rankTypeStr = request.getParameter("rankType");
int pageNo = (pageNoStr==null||pageNoStr.length()==0)?0:Integer.parseInt(pageNoStr);
int guojiaId = (guojiaIdStr==null||guojiaIdStr.length()==0)?0:Integer.parseInt(guojiaIdStr);
int rankType = (rankTypeStr==null||rankTypeStr.length()==0)?0:Integer.parseInt(rankTypeStr);
%>
<p>
请求信息
</p>
联盟名：<%=name %>|
页码：<%=pageNo %>|
国家id：<%=guojiaId %>|
排序类型：<%=rankType %>
<%
	final IoSession fs = new RobotSession(){
		public WriteFuture write(Object message){
			setAttachment(message);
			synchronized(this){
				this.notify();
			}
			return null;
		}
	};
	RankingReq.Builder builder = RankingReq.newBuilder();
	builder.setRankType(rankType);
	if(name==null||name.length()==0){
		builder.setPageNo(pageNo);
		builder.setGuojiaId(guojiaId);
	} else{
		builder.setName(name);
	}
	synchronized(fs){
		BigSwitch.inst.route(PD.RANKING_REP, builder, fs);
	//	fs.wait();
	}
	RankingResp resp = (RankingResp)fs.getAttachment();
	%>
	<p>
		当前页码：<%=resp.getPageNo() %>
		总页数：<%=resp.getPageCount() %>
	</p>
	<table border="1">
		<tr>
			<th>名次</th>
			<th>key</th>
			<th>score</th>
			<th>国家</th>
			<th>联盟名</th>
			<th>等级</th>
			<th>声望</th>
			<th>人数</th>
			<th>操作</th>
		</tr>
		<%
		List<LianMengInfo> junList = resp.getMengListList();
		for(LianMengInfo meng:junList){
		%>
		<tr>
			<td><%=meng.getRank() %></td>
			<td><%=meng.getMengId() %></td>
			<td><%DecimalFormat format=new DecimalFormat("#,##0.00");%><%=format.format(Redis.getInstance().zscore(RankingMgr.LIANMENG_RANK+"_"+guojiaId, meng.getMengId()+"")) %></td>
			<td><%=meng.getGuoJiaId() %></td>
			<td><%=meng.getMengName() %></td>
			<td><%=meng.getLevel() %></td>
			<td><%=meng.getShengWang() %></td>
			<td><%=meng.getMember() %></td>
			<td><a href="rank4lmplayer.jsp?mengId=<%=meng.getMengId()%>">查看成员</a></td>
		</tr>	
		<%
		}
		%>
	</table>
	
	<%	
%>

</body>
</html>