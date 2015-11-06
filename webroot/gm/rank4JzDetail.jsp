<%@page import="com.manu.network.msg.ProtobufMsg"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="qxmobile.protobuf.BagOperProtos.BagItem"%>
<%@page import="java.util.List"%>
<%@page import="qxmobile.protobuf.BagOperProtos.EquipInfo"%>
<%@page import="qxmobile.protobuf.Ranking.JunZhuInfo"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="qxmobile.protobuf.JunZhuProto.JunZhuInfoSpecifyReq"%>
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

<%String jzId = request.getParameter("jzId"); %>

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
JunZhuInfoSpecifyReq.Builder builder = JunZhuInfoSpecifyReq.newBuilder();
builder.setJunzhuId(Long.valueOf(jzId));
synchronized(fs){
	BigSwitch.inst.route(PD.JUNZHU_INFO_SPECIFY_REQ, builder, fs);
//	fs.wait();
}
ProtobufMsg msg = (ProtobufMsg)fs.getAttachment();
JunZhuInfo.Builder resp = (JunZhuInfo.Builder)msg.builder;
%>
<table border="1">
	<tr>
		<th>君主id</th>
		<th>名字</th>
		<th>联盟名</th>
		<th>攻击</th>
		<th>等级</th>
		<th>防御</th>
		<th>生命</th>
		<th>战力</th>
		<th>军衔</th>
		<th>贡金</th>
		<th>装备</th>
	</tr>
	<tr>
		<td><%=resp.getJunZhuId() %></td>
		<td><%=resp.getName() %></td>
		<td><%=resp.getLianMeng() %></td>
		<td><%=resp.getGongji() %></td>
		<td><%=resp.getLevel() %></td>
		<td><%=resp.getFangyu() %></td>
		<td><%=resp.getRemainHp() %></td>
		<td><%=resp.getZhanli() %></td>
		<td><%=resp.getJunxian() %></td>
		<td><%=resp.getGongjin() %></td>
		<td>
			<%
			EquipInfo equip = resp.getEquip();
			List<BagItem> list = equip.getItemsList();
			for(BagItem item:list){
				%><%=HeroService.getNameById(item.getName())%>,<%
			}
			%>
		</td>
	</tr>
</table>
</body>
</html>