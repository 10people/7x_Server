<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="com.qx.ranking.RankingMgr"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="qxmobile.protobuf.Ranking.BaiZhanInfo"%>
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
	List<JunZhu> list = HibernateUtil.list(JunZhu.class,"where id like '%"+GameServer.serverId+"'");
	for(JunZhu jz:list){
		RankingMgr.inst.resetBaizhanRankRedis(jz);
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
int pageNo = (pageNoStr==null||pageNoStr.length()==0)?0:Integer.parseInt(pageNoStr);
int guojiaId = (guojiaIdStr==null||guojiaIdStr.length()==0)?0:Integer.parseInt(guojiaIdStr);
%>
<p>请求信息</p>
君主名：<%=name %>|
页码：<%=pageNo %>|
国家ID：<%=guojiaId %>

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
	builder.setRankType(3);
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
			<th>君主名</th>
			<th>军衔</th>
			<th>军衔等级</th>
			<th>军衔排名</th>
			<th>胜场</th>
			<th>威望</th>
			<th>君主详情</th>
		</tr>
		<%
		List<BaiZhanInfo> junList = resp.getBaizhanListList();
		for(BaiZhanInfo jz:junList){
		%>
		<tr>
			<td><%=jz.getRank() %></td>
			<td><%=jz.getJunZhuId() %></td>
			<td><%DecimalFormat format=new DecimalFormat("#,##0.00");%><%=format.format(Redis.getInstance().zscore(RankingMgr.BAIZHAN_RANK+"_"+guojiaId, jz.getJunZhuId()+"")) %></td>
			<td><%=jz.getGuojiaId() %></td>
			<td><%=jz.getName() %></td>
			<td><%=jz.getJunxian() %></td>
			<td><%=jz.getJunxianLevel() %></td>
			<td><%=jz.getJunxianRank() %></td>
			<td><%=jz.getWinCount() %></td>
			<td><%=jz.getWeiwang() %></td>
			<td><a href="rank4JzDetail.jsp?jzId=<%=jz.getJunZhuId()%>">查看</a></td>
		</tr>	
		<%
		}
		%>
	</table>
	
	<%	
%>


</body>
</html>