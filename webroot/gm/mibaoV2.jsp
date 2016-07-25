<%@page import="com.qx.mibao.v2.MiBaoV2Bean"%>
<%@page import="qxmobile.protobuf.ErrorMessageProtos.ErrorMessage"%>
<%@page import="com.manu.dynasty.template.MiBaoNew"%>
<%@page import="qxmobile.protobuf.MibaoProtos.MibaoInfo"%>
<%@page import="qxmobile.protobuf.MibaoProtos.MibaoInfoResp"%>
<%@page import="com.manu.network.msg.ProtobufMsg"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="com.qx.mibao.v2.MiBaoV2Mgr"%>
<%@page import="org.apache.mina.core.session.IoSession"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.manu.network.SessionUser"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.award.AwardMgr"%>
<%@page import="com.manu.dynasty.template.AwardTemp"%>
<%@page import="com.manu.dynasty.template.MibaoSuiPian"%>
<%@page import="com.manu.dynasty.template.MibaoStar"%>
<%@page import="com.manu.dynasty.template.MiBao"%>
<%@page import="com.qx.mibao.MibaoMgr"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="com.qx.mibao.MiBaoDB"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import=" com.qx.util.TableIDCreator"%>
<%@page import="com.qx.account.Account"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	<%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>秘宝</title>
</head>
<body>
	<%
	String name = request.getParameter("account");
	name = name == null ? "": name.trim();
	String accIdStr = request.getParameter("accId");// 用户id
	accIdStr = (accIdStr == null ? "":accIdStr.trim());
	String mbIdStr = request.getParameter("mbId");
	String mbNumStr = request.getParameter("mbNum");
if(session.getAttribute("name") != null && name.length()==0 && accIdStr.length()==0){
	name = (String)session.getAttribute("name");
}
%>
	<form action="">
		账号<input type="text" name="account" value="<%=name%>">&nbsp;或&nbsp;
		账号ID<input type="text" name="accId" value="<%=accIdStr%>">
		<button type="submit">查询</button>
	</form>
	
	<br>
	<%
	Account account = null;
	if(name != null && name.length()>0){
		account = HibernateUtil.getAccount(name);
	}else if(accIdStr.length()>0){
		account = HibernateUtil.find(Account.class, Long.valueOf(accIdStr));
		if(account != null)name = account.accountName;
	}
	if(account == null){
		%>没有找到<%
	}else{
		session.setAttribute("name", name);
		%>账号<%=account.accountId%>:<%=account.accountName%>
		<br/>
		<table>
			<tr>
				<td>
					<br/>
				</td>
			</tr>
		</table>
		<%
		//--------------------------------------
		long junZhuId = account.accountId * 1000 + GameServer.serverId;
		%>君主id:<%= junZhuId%><% 
		JunZhu jz= HibernateUtil.find(JunZhu.class, junZhuId);
		if(jz != null){
			String action = request.getParameter("action");
			if("add".equals(action)){
				int mibaoId = Integer.parseInt(request.getParameter("mibaoId").trim());
				//int num = Integer.parseInt(request.getParameter("num"));
				AwardTemp a = new AwardTemp();
				a.awardId = 0;
				a.itemId = mibaoId;
				a.itemNum = 1;
				a.itemType = AwardMgr.TYPE_NEW_MI_BAO;
				IoSession su = SessionManager.inst.findByJunZhuId(junZhuId);
                if(su!=null){
                    AwardMgr.inst.giveReward(su, a, jz, false, false);
                }else{
                	AwardMgr.inst.giveReward(null, a, jz, false, false);
                }
                out.println("<br/>添加成功<br/>");
			}else if("addSuipian".equals(action)){
				int suipianId = Integer.parseInt(request.getParameter("spId"));
                int num = 1;
                AwardTemp a = new AwardTemp();
                a.awardId = 0;
                a.itemId = suipianId;
                a.itemNum = num;
                a.itemType = AwardMgr.TYPE_NEW_MOBAI_SUIPIAN;
                IoSession su = SessionManager.inst.findByJunZhuId(junZhuId);
                if(su!=null){
                    AwardMgr.inst.giveReward(su, a, jz, false, false);
                }else{
                    AwardMgr.inst.giveReward(null, a, jz, false, false);
                }
			}else if("pltianjia".equals(action)){
				
				if( mbIdStr != null && mbNumStr != null ){
					int mbId = Integer.parseInt(mbIdStr);
	                int num = Integer.parseInt(mbNumStr);
	                MiBaoNew mbTemp = MiBaoV2Mgr.inst.confMap.get(mbId);
	                if(mbTemp != null ){
	                	AwardTemp a = new AwardTemp();
	                	a.awardId = 0;
	                	a.itemId = mbId + 10000;
	                	a.itemNum = num;
	                	a.itemType = AwardMgr.TYPE_NEW_MOBAI_SUIPIAN;
		                IoSession su = SessionManager.inst.findByJunZhuId(junZhuId);
		                if(su!=null){
		                    AwardMgr.inst.giveReward(su, a, jz, false, false);
		                }else{
		                    AwardMgr.inst.giveReward(null, a, jz, false, false);
		                }
	                }
				}
                
			} else if("update".equals(action)) {
				int tempId = Integer.parseInt(request.getParameter("tempId"));
				int mibaoId = Integer.parseInt(request.getParameter("mibaoId"));
				MiBaoDB miBaoDB = HibernateUtil.find(MiBaoDB.class, " where tempId=" + tempId+" and ownerId="+junZhuId);
				miBaoDB.miBaoId = mibaoId;
				HibernateUtil.save(miBaoDB);
			} else if("jiHuo".equals(action)) {
				IoSession ss = createSession(jz.id);
				ErrorMessage.Builder msg = ErrorMessage.newBuilder();
				msg.setErrorDesc(request.getParameter("dbId"));
				MiBaoV2Mgr.inst.jiHuo(0, ss, msg);
			} else if("jihuoMiShu".equals(action)) {
				IoSession ss = createSession(jz.id);
				ss.setAttribute("MiBaoV2MgrJiHuoMiShu", Boolean.TRUE);
				MiBaoV2Mgr.inst.miShuJiHuo(0, ss, null);
			} else if("delMibao".equals(action)) {
				long dbId = Long.parseLong(request.getParameter("dbId"));
				MiBaoDB miBaoDB = HibernateUtil.find(MiBaoDB.class, dbId);
				HibernateUtil.delete(miBaoDB);
			} else if("check".equals(action)){
			//	long jzId= Long.parseLong(request.getParameter("jzId".trim()));
				int mbId= Integer.parseInt(request.getParameter("mbId".trim()));
				long dbId= Long.parseLong(request.getParameter("dbId".trim()));
				
				List<MiBaoV2Bean> list = HibernateUtil.list(MiBaoV2Bean.class, "where ownerId = "+junZhuId+" and miBaoId = "+ mbId);
				for(int i= 0 ; i < list.size() ; i++){
					if(list.get(i).dbId != dbId && !list.get(i).main){
						HibernateUtil.delete(list.get(i));
					}
				}
				out("啦啦啦啦啦，清理成功！！！"+mbId +""+dbId);
			}
		}
		IoSession ss = createSession(jz.id);
		if(MiBaoV2Mgr.inst == null)new MiBaoV2Mgr();
		MiBaoV2Mgr.inst.sendMainInfo(0, ss, null);
		ProtobufMsg msg = (ProtobufMsg)ss.getAttachment();
		MibaoInfoResp.Builder info = (MibaoInfoResp.Builder)msg.builder;
		 out.println("<br/>");
		out.println("当前秘宝个数:"+info.getMiBaoListCount());
		out("秘术已激活到"+info.getLevelPoint());
		Object mark = ss.getAttribute("MiBaoV2MgrJiHuoMiShu");
		if(mark!= null){
			out("<a href=?action=jihuoMiShu>激活秘术</a>");
		}
		%>
		<table border='1'>
		<tr><th>数据库id</th><th>秘宝ID</th><th>碎片id</th>
		<th>名称</th><th>数量</th><th>合成需要数量</th><th>已激活</th>
		<th>操作</th></tr>
		<% 
		for(int i=0; i<info.getMiBaoListCount();i++){
			MibaoInfo bean = info.getMiBaoList(i);
			String mibaoName = "";
				MiBaoNew mibao = MiBaoV2Mgr.inst.confMap.get(bean.getMiBaoId());
				mibaoName = HeroService.getNameById(mibao.nameId+"");

			%>
			<tr>
			<td><%=bean.getDbId() %></td>
			<td><%=bean.getMiBaoId() %></td>
			<td><%=mibao.suipianId %></td>
			<td><%=mibaoName %></td>
			<td><%=bean.getSuiPianNum() %></td>
			<td><%=bean.getNeedSuipianNum() %></td>
			<td><%=bean.getStar() %></td>
			<td><% out.append("<a href=mibaoV2.jsp?dbId=" + bean.getDbId() + "&action=delMibao>删除</a>"); %>-
			<% out.append("<a href=mibaoV2.jsp?spId=" + (bean.getMiBaoId()+10000) + "&action=addSuipian>加碎片</a>"); %>
			<% out.append("<a href=mibaoV2.jsp?dbId=" + (bean.getDbId()) + "&action=jiHuo>激活</a>"); %>
			<% out.append("<a href=mibaoV2.jsp?dbId=" + (bean.getDbId()) + "&mbId="+bean.getMiBaoId()+"&action=check>清理</a>"); %>
			</td>
			</tr>
			<%
		}
		%>
		
		</table>
		<%
		List<MiBaoV2Bean> mbList = HibernateUtil.list(MiBaoV2Bean.class, "where ownerId = " + jz.id + "order by miBaoId");
		if(mbList != null && mbList.size() >0){
			%>
			<table border='1'>
			<tr><th>数据库id</th><th>秘宝ID</th><th>碎片数量</th><th>已激活</th><th>当前收集中</th></tr>
			<% 
			for( MiBaoV2Bean mbBean : mbList){
				%>
				<tr>
				<td><%= mbBean.dbId %></td>
				<td><%= mbBean.miBaoId %></td>
				<td><%= mbBean.suiPianNum %></td>
				<td><%= mbBean.active ? "是":"否" %></td>
				<td><%= mbBean.main ? "是":"否"  %></td>
				<%
			}
		}
		%>
		<br>
		<form action="">
		<input type="hidden" name="action" value="pltianjia">
		秘宝id<input type="text" name="mbId" value=<%=mbIdStr == null ?"": mbIdStr%>>
		碎片数量<input type="text" name="mbNum" value=<%=mbNumStr == null ?"": mbNumStr%>>
		<button type="submit">添加</button>
		</form>
		<br>
		
		<%
	}
	%>
	<%//--------------------------------------------------------------------- %>
</body>
</html>