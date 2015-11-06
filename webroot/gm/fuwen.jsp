<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="qxmobile.protobuf.FuWen.FuwenResp"%>
<%@page import="qxmobile.protobuf.FuWen.OperateFuwenReq"%>
<%@page import="com.manu.dynasty.template.FuwenJiacheng"%>
<%@page import="com.manu.dynasty.template.FuwenOpen"%>
<%@page import="com.qx.fuwen.FuwenMgr"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="qxmobile.protobuf.FuWen.Fuwen"%>
<%@page import="qxmobile.protobuf.FuWen.JunzhuAttr"%>
<%@page import="qxmobile.protobuf.FuWen.FuwenLanwei"%>
<%@page import="java.util.List"%>
<%@page import="qxmobile.protobuf.FuWen.QueryFuwenResp"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.manu.network.PD"%>
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
<title>符文</title>
<script type="text/javascript">
	function loadFuwen(itemId){
		var lanweiId = document.getElementById("lanweiId").value;
		location.href="fuwen.jsp?type=5&itemId="+itemId+"&lanweiId="+lanweiId+"";
	}
	function combineFuwen(type){
		var itemId = document.getElementById("itemId").value;
		var cbLanweiId = document.getElementById("cbLanweiId").value;
		location.href="fuwen.jsp?type="+type+"&itemId="+itemId+"&lanweiId="+cbLanweiId+"";
	}
	function showCache(){
		var lanweicache = document.getElementById("lanweicache");
		var lanwei = document.getElementById("lanwei");
		var lockcache = document.getElementById("lockcache");
		if(lanweicache.style.display=='none'){
			lanweicache.style.display='block';
			lanwei.style.display='none';
		} else{
			lanweicache.style.display='none';
			lanwei.style.display='block';
		}
		if(lockcache.style.display=='none'){
			lockcache.style.display='block';
		} else{
			lockcache.style.display='none';
		}
	}
</script>
</head>
<body>
<form action="">
	账号：<input type="text" name="actName"/>
	<button type="submit">查询</button>
</form>
<p>
	符文Id:<input type="text" id="itemId" placeHolder="要合成的符文id"/> 
	符文栏位Id:<input type="text" id="cbLanweiId" placeHolder="符文id所在栏位id(背包中写0)"/>
	<button onclick="combineFuwen('3')">合成</button>
	<button onclick="combineFuwen('4')">一键合成</button>
</p>
<p><button onclick="showCache()">显示Cache存储信息</button></p>
	<%
	String itemId = request.getParameter("itemId");
	String type = request.getParameter("type");
	String lanweiId = request.getParameter("lanweiId");
	itemId=itemId==null?"0":itemId;
	lanweiId=lanweiId==null?"0":lanweiId;
	
	String actName =request.getParameter("actName");
	actName = actName == null?"":actName;
	if(session.getAttribute("name") != null && actName.length()==0){
		actName = (String)session.getAttribute("name");
	}
	if(actName.length()>0){
		Account account = HibernateUtil.getAccount(actName);
		if(account!=null){
			session.setAttribute("name",actName);
			long junzhuId = account.getAccountId()* 1000 + GameServer.serverId;
			List<com.manu.dynasty.template.Fuwen> fuwenTj = FuwenMgr.inst.getFuShiTuijian(junzhuId, 1);
			List<com.manu.dynasty.template.Fuwen> baoshiTj = FuwenMgr.inst.getFuShiTuijian(junzhuId, 2);
			%>
			<p>符文进度:<%=FuwenMgr.inst.getFuShiProgress(junzhuId, 1) %></p>
			<p>宝石进度:<%=FuwenMgr.inst.getFuShiProgress(junzhuId, 2) %></p>
			<p>符文推荐:
			<%if(fuwenTj!=null)for(com.manu.dynasty.template.Fuwen fuwen:fuwenTj){%>
				<%=fuwen.getFuwenID() %>:<%=fuwen.getFuwenLevel() %>级
			<%} %>
			</p>
			<p>宝石推荐:
			<%if(baoshiTj!=null)for(com.manu.dynasty.template.Fuwen fuwen:baoshiTj){%>
				<%=fuwen.getFuwenID() %>:<%=fuwen.getFuwenLevel() %>级
			<%} %>
			</p>
			<%
			List<String> lanweis =  Redis.getInstance().lgetList(FuwenMgr.CACHE_FUWEN_LANWEI+junzhuId);
			List<String> fuwens = Redis.getInstance().lgetList(FuwenMgr.CACHE_FUWEN_LOCK+junzhuId);
			// 操作符文
			if(type!=null&&type.length()!=0){
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
				OperateFuwenReq.Builder operateBuilder = OperateFuwenReq.newBuilder();
				operateBuilder.setType(Integer.parseInt(type));
				operateBuilder.setItemId(Integer.parseInt(itemId));
				if(lanweiId!=null&&lanweiId.length()!=0){
					operateBuilder.setLanweiId(Integer.parseInt(lanweiId));
				}
				synchronized(fs){
					BigSwitch.inst.route(PD.C_OPERATE_FUWEN_REQ, operateBuilder, fs);
				//	fs.wait();
				}
				FuwenResp resp = (FuwenResp)fs.getAttachment();
				int result = resp.getResult();
				String reason = resp.getReason();
				if(result==0){
					System.out.println("jsp操作符文成功");
				} else if(result==1){
					System.out.println("jsp操作符文失败:" +reason);
				}
				System.out.println("reason:"+reason);
				response.sendRedirect("fuwen.jsp?result="+result+"&reason="+reason+"");
			}
			
			String resultStr = request.getParameter("result");
			String reasonStr = request.getParameter("reason");
			if(resultStr!=null&&resultStr.length()!=0){
				int result = Integer.parseInt(resultStr);
				if(result==0){
					%><p>操作成功</p><%
				} else if(result==1){
					%><p>操作失败 <%=reasonStr%></p><%
				}
			}
			
			// 查询
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
				BigSwitch.inst.route(PD.C_QUERY_FUWEN_REQ, null, fs);
			//	fs.wait();
			}
			QueryFuwenResp resp = (QueryFuwenResp)fs.getAttachment();
			long zhanli = resp.getZhanli();
			List<FuwenLanwei> lanweiList = resp.getLanweiList();
			List<JunzhuAttr> attrList = resp.getAttrList();
			List<Fuwen> fuwenList = resp.getFuwensList();
			%>
			<table border="1">
				<tr>
					<th>总战力</th>
					<th>符文栏位列表</th>
					<th>属性加成列表</th>
					<th>背包符文列表</th>
				</tr>
				<tr>
					<td><%=zhanli %></td>
					<td>
						<table border="1" id="lanwei">
							<tr>
								<th>符文栏位id</th>
								<th>符文id</th>
								<th>名字</th>
								<th>属性值</th>
								<th>等级</th>
							</tr>
							<%for(FuwenLanwei lanwei:lanweiList){%>
								<tr>
									<td><%=lanwei.getLanweiId() %></td>
									<td><a href="fuwen.jsp?lanweiId=<%=lanwei.getLanweiId()%>&type=6"><%=lanwei.getItemId() %></a></td>
									<td><%=lanwei.getItemId()>0?HeroService.getNameById(String.valueOf(FuwenMgr.inst.fuwenMap.get(lanwei.getItemId()).getName())):"" %></td>
									<td><%=lanwei.getItemId()>0?FuwenMgr.inst.fuwenMap.get(lanwei.getItemId()).getShuxingValue():""  %></td>
									<td><%=lanwei.getItemId()>0?FuwenMgr.inst.fuwenMap.get(lanwei.getItemId()).getFuwenLevel():""  %></td>
								</tr>
							<%} %>
						</table>
						<table border="1" id="lanweicache" style="display:none;float:left;margin:10px">
							<tr><th>符文栏位Cache</th></tr>
							<%for(String lanwei:lanweis){ %>
							<tr>
								<td><%=lanwei %></td>
							</tr>
							<%} %>
						</table>
					</td>
					<td>
						<table border="1">
							<tr>
								<th>类型</th>
								<th>攻击</th>
								<th>防御</th>
								<th>生命</th>
								<th>武器伤害加深</th>
								<th>武器伤害减免</th>
								<th>武器暴击加深</th>
								<th>武器暴击减免</th>
								<th>技能伤害加深</th>
								<th>武器伤害减免</th>
								<th>武器暴击加深</th>
								<th>武器暴击减免</th>
							</tr>
							<%for(JunzhuAttr attr:attrList){ %>
								<tr>
									<td><%if(attr.getType()==1){%>总属性<%}else if(attr.getType()==2){%>加成属性<%} %></td>
									<td><%=attr.getGongji() %></td>
									<td><%=attr.getFangyu() %></td>
									<td><%=attr.getShengming() %></td>
									<td><%=attr.getWqSH() %></td>
									<td><%=attr.getWqJM() %></td>
									<td><%=attr.getWqBJ() %></td>
									<td><%=attr.getWqRX() %></td>
									<td><%=attr.getJnSH() %></td>
									<td><%=attr.getJnJM() %></td>
									<td><%=attr.getJnBJ() %></td>
									<td><%=attr.getJnRX() %></td>
								</tr>
							<%} %>
							<tr></tr>
						</table>
					</td>
					<td>
						<table border="1" id="lockcache" style="display:none;float:left;margin:10px">
							<tr><th>符文锁定Cache</th></tr>
							<%for(String fuwen:fuwens){ %>
							<tr>
								<td><%=fuwen %></td>
							</tr>
							<%} %>
						</table>
						<table border="1">
							<tr><th colspan="6"><input type="text" id="lanweiId" placeHolder="输入栏位id，点击符石装备"></th></tr>
							<tr>
								<th>符文Id</th>
								<th>是否锁定</th>
								<th>名字</th>
								<th>属性值</th>
								<th>等级</th>
								<th>数量</th>
							</tr>
							<%for(Fuwen fuwen:fuwenList){ %>
								<tr>
									<td><%=fuwen.getItemId() %></td>
									<td><%if(fuwen.getIsLock()==1){%><a href="fuwen.jsp?itemId=<%=fuwen.getItemId()%>&type=2">锁定</a><%}else if(fuwen.getIsLock()==2){%><a href="fuwen.jsp?itemId=<%=fuwen.getItemId()%>&type=1">未锁定</a><%} %></td>
									<td><a href="#" onclick="javascript:loadFuwen('<%=fuwen.getItemId()%>')"><%=HeroService.getNameById(String.valueOf(FuwenMgr.inst.fuwenMap.get(fuwen.getItemId()).getName())) %></a></td>
									<td><%=FuwenMgr.inst.fuwenMap.get(fuwen.getItemId()).getShuxingValue() %></td>
									<td><%=FuwenMgr.inst.fuwenMap.get(fuwen.getItemId()).getFuwenLevel() %></td>
									<td><%=fuwen.getCnt() %></td>
								</tr>							
							<%} %>
						</table>
					</td>
				</tr>
			</table>
			<%
		}
	}else{
		%><p>账号没找到</p><%
	}
	%>
</body>
</html>