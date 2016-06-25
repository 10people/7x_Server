<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.manu.network.SessionUser"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="qxmobile.protobuf.JewelProtos.EquipOperationReq"%>
<%@page import="com.manu.dynasty.template.Fuwen"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.qx.equip.jewel.JewelMgr"%>
<%@page import="com.qx.bag.Bag"%>
<%@page import="com.qx.bag.BagMgr"%>
<%@page import="com.qx.bag.BagGrid"%>
<%@page import="com.qx.bag.EquipMgr"%>
<%@page import="com.qx.bag.EquipGrid"%>

<%@page import="java.util.ArrayList"%>
<%@page import="com.qx.equip.domain.UserEquip"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="java.util.List"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.junzhu.JunZhu"%>


<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>非临时工文件，可以乱点</title>
</head>
<body>
	<h2 style="color: red;">进行镶嵌、卸下、合成操作需要登录</h2>

	<%
		//创建、读取参数用
		String name = request.getParameter("account");
		String accIdStr = request.getParameter("accId");
		String actionStr = request.getParameter("action");
		String jewelDbIdStr = request.getParameter("jewelDbId");
		String equipDbIdStr = request.getParameter("equipDbId");
		String possionIdStr = request.getParameter("possionId");
		String cailiaoListStr = request.getParameter("cailiaoList");
		String tuijianStr = request.getParameter("tuijianId");
		List<BagGrid> jewelList = new ArrayList<BagGrid>() ;
		Bag<EquipGrid> equips = null ;
		
		
		
	%>
	<%
		//获取账号信息用
		name = name == null ? "": name.trim();
		accIdStr = (accIdStr == null ? "":accIdStr.trim());
		if(session.getAttribute("name") != null && name.length()==0 && accIdStr.length()==0){
			name = (String)session.getAttribute("name");
		}
		
		
		Account account = null;
		JunZhu jz = null ;
		if(name != null && name.length()>0){
			account = HibernateUtil.getAccount(name);
		}else if(accIdStr != null && accIdStr.length()>0){
			account = HibernateUtil.find(Account.class, Long.valueOf(accIdStr));
			if(account != null)name = account.getAccountName();
		}
		if(account == null){
			%>没有找到<%
		}else{
			session.setAttribute("name", name);
			long junZhuId = account.getAccountId() * 1000 + GameServer.serverId;
			jz = HibernateUtil.find(JunZhu.class, junZhuId);
		}

		if(jz!= null){
			if( actionStr != null){
				if(actionStr.equals("qingli")){
					if( equipDbIdStr != null && equipDbIdStr.length() != 0 ){
						EquipGrid eg = HibernateUtil.find(EquipGrid.class, Long.parseLong(equipDbIdStr));
						if(eg != null){
							UserEquip ue = HibernateUtil.find(UserEquip.class, eg.instId);
							if(ue != null){
								ue.Jewel0 = -1;
								ue.Jewel1 = -1;
								ue.Jewel2 = -1;
								ue.Jewel3 = -1;
								ue.Jewel4 = -1;
								HibernateUtil.save(ue);
							}
						}
						
					}
				}
				if(actionStr.equals("tuijian")){
					if(tuijianStr!= null && tuijianStr.length()>0){
						Object[] tuijian = JewelMgr.inst.getXiangQianTuiJian(jz.id);
						if(tuijian != null){
							int[] jindu = (int[])tuijian[0];
							out.println(jindu[0] + "  "+jindu[1]);
							int[] worst = (int[])tuijian[1];
							out.println(worst[0]+ "  "+worst[1] + "  "+worst[2]);
						}
					}
				}
				SessionUser su = SessionManager.inst.findByJunZhuId(jz.id);
				if(su != null){
					if(actionStr.equals("xiangqian")){
						if( jewelDbIdStr != null && equipDbIdStr != null && possionIdStr != null
							&& jewelDbIdStr.length() != 0 && equipDbIdStr.length() != 0 && possionIdStr.length() != 0 ){
							EquipOperationReq.Builder req = EquipOperationReq.newBuilder();
							req.setType(4);
							req.setJewelId(Long.parseLong(jewelDbIdStr));
							req.setEqulpId(Long.parseLong(equipDbIdStr));
							req.setPossionId(Integer.parseInt(possionIdStr));
							JewelMgr.inst.handle((int)PD.C_EQUIP_BAOSHI_REQ, su.session, req);
						}
					}else if (actionStr.equals("xiexia")){
						if( equipDbIdStr != null && possionIdStr != null
							&& equipDbIdStr.length() != 0 && possionIdStr.length() != 0 ){
							EquipOperationReq.Builder req = EquipOperationReq.newBuilder();
							req.setType(5);
							req.setEqulpId(Long.parseLong(equipDbIdStr));
							req.setPossionId(Integer.parseInt(possionIdStr));
							JewelMgr.inst.handle((int)PD.C_EQUIP_BAOSHI_REQ, su.session, req);
						}
					}else if(actionStr.equals("yijianxiangqian")){
						if( equipDbIdStr != null && equipDbIdStr.length() != 0 ){
							EquipOperationReq.Builder req = EquipOperationReq.newBuilder();
							req.setType(2);
							req.setEqulpId(Long.parseLong(equipDbIdStr));
							JewelMgr.inst.handle((int)PD.C_EQUIP_BAOSHI_REQ, su.session, req);
						}
					}else if(actionStr.equals("yijianxiexia")){
						if( equipDbIdStr != null && equipDbIdStr.length() != 0 ){
							EquipOperationReq.Builder req = EquipOperationReq.newBuilder();
							req.setType(3);
							req.setEqulpId(Long.parseLong(equipDbIdStr));
							JewelMgr.inst.handle((int)PD.C_EQUIP_BAOSHI_REQ, su.session, req);
						}
					}else if(actionStr.equals("hecheng")){
						if( equipDbIdStr != null && possionIdStr != null && cailiaoListStr != null
							&& equipDbIdStr.length() != 0 && possionIdStr.length() != 0	){
							EquipOperationReq.Builder req = EquipOperationReq.newBuilder();
							req.setType(6);
							req.setEqulpId(Long.parseLong(equipDbIdStr));
							req.setPossionId(Integer.parseInt(possionIdStr));
							String[] list = cailiaoListStr.split("#");
							if(list != null && list.length != 0){
								for(String s : list){
									String[] grid = s.split(",");
									if(grid != null && grid.length == 2){
										long jewelId = Long.parseLong(grid[0]);
										int jewelNum = Integer.parseInt(grid[1]);
										for(int i = 0 ; i < jewelNum ; i++){
											req.addCailiaoList(jewelId);
										}
									}
								}
							}
							out.print(PD.C_EQUIP_BAOSHI_REQ);
							out.print(su.session);
							out.print(req);
							BigSwitch.inst.jewelMgr.handle((int)PD.C_EQUIP_BAOSHI_REQ, su.session, req);
						}
					}
				}
			}
			//加载玩家的装备、背包信息
			equips = EquipMgr.inst.loadEquips(jz.id);
			
			Bag<BagGrid> bag = BagMgr.inst.loadBag(jz.id);
			jewelList = JewelMgr.inst.getAllJewel(bag);
		}
	%>
	<form action="">
		账号<input type="text" name="account" value="<%=name%>">&nbsp;或&nbsp;
	  	君主ID<input type="text" name="accId" value="<%=jz==null ? "":jz.id%>">
	  	<button type="submit">查询</button>
	</form>
	<br>
	<form action="">
		<input type="hidden" name="action" value="tuijian">
		君主ID<input type="text" name="tuijianId" value="<%=jz==null ? "":jz.id%>">
		<button type="submit">获取镶嵌推荐</button>
	</form>
	=========================================玩家镶嵌宝石信息====================================================
	<br>
	<br>
	<table border='1'>
		<tr>
		<th> 装备ID </th>
		<th> 宝石孔0 </th><th>孔颜色</th>
		<th> 宝石孔1 </th><th>孔颜色</th>
		<th> 宝石孔2 </th><th>孔颜色</th>
		<th> 宝石孔3 </th><th>孔颜色</th>
		<th> 宝石孔4 </th><th>孔颜色</th>
		</tr>
		<%
		if(equips != null){
			for(EquipGrid eg : equips.grids ){
				if(eg == null){
					continue;
				}
				if(eg.itemId <= 0 ){
					continue;
				}
				UserEquip ue = HibernateUtil.find(UserEquip.class, eg.instId);
				int[] holesInfo = JewelMgr.inst.equipMap.get(eg.itemId);
				List<Long> jewelInfo = JewelMgr.inst.getJewelOnEquip(ue);
				%>
				<tr>
				<td><%=eg.dbId %></td>
				<%
				for(int i = 0 ; i < JewelMgr.inst.Max_Jewel_Num_One_Equip ; i++){
					%>
					<td align="left"><%=jewelInfo.get(i)<= 0?"未镶嵌":(int)(jewelInfo.get(i)>>32)+" exp:"+(int)(jewelInfo.get(i)&Integer.MAX_VALUE) %></td>
					<td align="center"><%=holesInfo[i] == 1?"红":holesInfo[i]==2 ?"黄":holesInfo[i]==3?"绿":""%></td>
					<%
				}
			}
		}
		%>
	</table>
	<br>
	==============================================数据清理========================================================
	<br>
	<br>
	<form action="">
		<input type="hidden" name="action" value="qingli">
		装备ID：<input type="text" name="equipDbId" value="<%=equipDbIdStr == null?"":equipDbIdStr%>"size="10">
		<button type="submit">删除镶嵌信息</button>处理脏数据用，会直接删除装备的镶嵌信息，不会走卸下，宝石会丢失！！！
	</form>
	<br>
	==========================================玩家背包宝石信息====================================================
	<br>
	<br>
	<table border='1'>
		<tr>
		<th>宝石ID</th>
		<th>道具ID</th>
		<th>宝石名称</th>
		<th>宝石经验</th>
		<th>宝石颜色</th>
		<th>宝石数量</th>
		</tr>
		<%
		for(BagGrid bg : jewelList){
			int itemId = bg.itemId;
			Fuwen peizhi = JewelMgr.inst.jewelMap.get(bg.itemId);
			if(peizhi == null ){
				continue;
			}
			int color = peizhi.getInlayColor();
			%>
			<tr>
				<td><%=bg.dbId %></td>
				<td><%=bg.itemId %></td>
				<td><%=bg.dbId<=0 ? "" : BigSwitch.getInst().bagMgr.getItemName(bg.itemId)%></td>
				<td><%=bg.instId < 0 ? 0 : bg.instId %></td>
				<td><%=color == 1?"红":color==2?"黄":color==3?"绿":"无"%></td>
				<td><%=bg.cnt %></td>

			</tr>
			<%
		}		
		%>
	</table>
	<br>
	===============================================镶嵌==========================================================
	<br>
	<br>
	<form action="">
		<input type="hidden" name="action" value="xiangqian">
		装备ID：<input type="text" name="equipDbId" value="<%=equipDbIdStr == null?"":equipDbIdStr%>" size="10">
		孔ID：<input type="text" name="possionId" value="<%=possionIdStr == null?"":possionIdStr%>" size="10">
		宝石ID：<input type="text" name="jewelDbId" value="<%=jewelDbIdStr == null?"":jewelDbIdStr%>" size="10">
		<button type="submit">镶嵌</button>
	</form>
	<form action="">
		<input type="hidden" name="action" value="xiexia">
		装备ID：<input type="text" name="equipDbId" value="<%=equipDbIdStr == null?"":equipDbIdStr%>" size="10">
		孔ID：<input type="text" name="possionId" value="<%=possionIdStr == null?"":possionIdStr%>" size="10">
		<button type="submit">卸下</button>
	</form>
	<form action="">
		<input type="hidden" name="action" value="yijianxiangqian">
		装备ID：<input type="text" name="equipDbId" value="<%=equipDbIdStr == null?"":equipDbIdStr%>" size="10">
		<button type="submit">一键镶嵌</button>
	</form>
	<form action="">
		<input type="hidden" name="action" value="yijianxiexia">
		装备ID：<input type="text" name="equipDbId" value="<%=equipDbIdStr == null?"":equipDbIdStr%>" size="10">
		<button type="submit">一键卸下</button>
	</form>
	<br>
	===============================================合成==========================================================
	<br>
	<br>
	<form action="">
		<input type="hidden" name="action" value="hecheng">
		装备ID：<input type="text" name="equipDbId" value="<%=equipDbIdStr == null?"":equipDbIdStr%>" size="10">
		孔ID：<input type="text" name="possionId" value="<%=possionIdStr == null?"":possionIdStr%>" size="10">
		<br>
		材料：<input type="text" name="cailiaoList" value="<%=cailiaoListStr == null?"":cailiaoListStr%>" size="100">
		<button type="submit">合成</button>
		<br>填写格式：宝石ID，宝石数量#宝石ID，宝石数量……
	</form>
	
	
</body>
</html>