<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.qx.equip.web.UserEquipAction"%>
<%@page import="com.manu.dynasty.template.XilianShuxing"%>
<%@page import="com.qx.equip.domain.UserEquip"%>
<%@page import="com.qx.alliance.HuanWu"%>
<%@page import="com.manu.network.SessionUser"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.qx.purchase.XiLian"%>
<%@page import="com.qx.timeworker.TimeWorkerMgr"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.manu.dynasty.util.Config"%>
<%@page import="com.manu.dynasty.template.ZhuangBei"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.manu.dynasty.template.BaseItem"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.qx.bag.EquipGrid"%>
<%@page import="com.qx.bag.BagGrid"%>
<%@page import="java.util.List"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.qx.bag.BagMgr"%>
<%@page import="com.qx.bag.Bag"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	<%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%
		BagMgr.maxGridCount = BagMgr.spaceFactor - 1;
		String action = request.getParameter("action");
		String itemId = request.getParameter("itemId");
		itemId = itemId == null ? "" : itemId.trim();
		String itemNum = request.getParameter("itemNum");
		itemNum = null==itemNum?"0":itemNum;
		String jzId = request.getParameter("jzId");
		if(jzId == null){
			jzId = session.getAttribute("jzId").toString();
		}
		jzId = jzId == null ? "" : jzId;
		jzId=jzId.trim();
	%>
	<h2 style="color: red">进行操作时,请保证账号已登录</h2>
	<form action="">
		君主id<input type="text" name="jzId" value="<%=jzId%>">
		<button type="submit">查询</button>
	</form>
	<form action="">
		<input type="hidden" name="action" value="addItem"> <input
			type="hidden" name="jzId" value="<%=jzId%>"> 物品/装备id<input
			type="text" name="itemId" value="<%=itemId%>">数量：<input type="number" value='1' name="itemNum"/>
		<button type="submit">添加</button>
		&nbsp;<a href='../dataConf/dataTemplate.jsp?type=equip' target="_blank">装备列表</a>
	</form>
	<form action="">
	<input type="hidden" name="jzId" value="<%=jzId%>"> 
		<input type="hidden" name="action" value="clearUnUsedBags"> 
		<button type="submit">清理无用背包格子</button>
	</form>
	<form action="">
		<input type="hidden" name="jzId" value="<%=jzId%>"> 
		<input type="hidden" name="action" value="clearBags"> 
		<button type="submit">清空背包</button>
	</form>
	<form action="">
		<input type="hidden" name="jzId" value="<%=jzId%>"> 
		<input type="hidden" name="action" value="pushBagsInfo"> 
		<button type="submit">推送背包信息</button>
	</form>
	<%
		if (jzId.matches("\\d+")) {
			{
				long junzhuId = Long.parseLong(jzId);
				JunZhu junzhu = HibernateUtil.find(JunZhu.class, junzhuId);
	%>
	<br /> 身上装备--------------
	<br />
	<%
		Bag<BagGrid> bag = BigSwitch.getInst().bagMgr.loadBag(junzhuId);
		Bag<EquipGrid> equips = BigSwitch.getInst().equipMgr.loadEquips(junzhuId);
				if ("wear".equals(action)) {
					int idx = Integer.parseInt(request.getParameter("bagIndex"));
					SessionUser su = SessionManager.inst.findByJunZhuId(junzhuId);
					if(su!=null){
						BigSwitch.inst.equipMgr.equipAdd(su.session,equips, bag, idx);
					}
				}else if ("takeoff".equals(action)) {
					int idx = Integer.parseInt(request.getParameter("bagIndex"));
					SessionUser su = SessionManager.inst.findByJunZhuId(junzhuId);
					if(su!=null){
						BigSwitch.inst.equipMgr.equipRemove(su.session, equips, bag, idx);
					}
				}else if ("delete".equals(action)) {
					int idx = Integer.parseInt(request.getParameter("bagIndex"));
					BagGrid bg = bag.grids.get(idx);
					bg.instId = bg.itemId = bg.cnt = 0;
					HibernateUtil.save(bg);
				}
				else if ("clearUnUsedBags".equals(action)) {
				List<BagGrid> list = bag.grids;
				for(BagGrid bg : list){
						if (bg.itemId<=0 || bg.cnt<=0) {
							HibernateUtil.delete(bg);
						}
					}
		    	} else if ("clearBags".equals(action)) {
					List<BagGrid> list = bag.grids;
					for (BagGrid bg : list) {
						HibernateUtil.delete(bg);
					}
		   		}else if("pushBagsInfo".equals(action)){
					SessionUser su = SessionManager.inst.findByJunZhuId(junzhuId);
					if(su!=null){
						BagMgr.inst.sendBagInfo(0, su.session, null);
						BagMgr.inst.sendEquipInfo(0, su.session, null);
						JunZhuMgr.inst.sendMainInfo(su.session);
					}
		   		}
				List<EquipGrid> list0 = equips.grids;
				int cnt0 = list0.size();
	%><table border='1'>
		<tr>
			<th>dbId</th>
			<th>itemId</th>
			<th>名称</th>
			<th>op</th>
			<th>instId</th>
			<th>进阶材料</th>
			<th>强化等级</th>
			<th>当前强化等级经验</th>
			<th>洗练获得的属性</th>
		</tr>
		<%
			EquipGrid empty = new EquipGrid();
			for (int i = 0; i < cnt0; i++) {
						EquipGrid bg = list0.get(i);
						if(bg == null){
							bg = empty;
						}
						int targetItemId=bg.itemId;
						UserEquip ue =bg.instId>0? HibernateUtil.find(UserEquip.class, bg.instId):null;
						String xilianStr="无装备";
						if(bg.dbId!=0){
							xilianStr = ue == null ? "没洗练信息" : ue
									.getHasXilian() == null ? "" : ue
									.getHasXilian();
							if ("".equals(xilianStr)) {
								XilianShuxing shuxing = UserEquipAction.instance.xilianShuxingMap
										.get(targetItemId);
								if (shuxing == null) {
									xilianStr = "没找到配置";
								} else {
									xilianStr = shuxing.Shuxing1;
								}
							}
						}
						xilianStr=	xilianStr.replace("A", "武器伤害加深；");
						xilianStr=	xilianStr.replace("B", "武器伤害抵抗；");
						xilianStr=	xilianStr.replace("C", "武器暴击加深；");
						xilianStr=	xilianStr.replace("D", "武器暴击抵抗；");
						xilianStr=	xilianStr.replace("E", "技能伤害加深；");
						xilianStr=	xilianStr.replace("F", "技能伤害抵抗；");
						xilianStr=	xilianStr.replace("G", "技能暴击加深；");
						xilianStr=	xilianStr.replace("H", "技能暴击抵抗；");
						xilianStr=	xilianStr.replace("O", "武器暴击率；");
						xilianStr=	xilianStr.replace("P", "技能暴击率；");
						xilianStr=	xilianStr.replace("Q", "武器免暴率；");
						xilianStr=	xilianStr.replace("R", "技能免暴率；");
						xilianStr=	xilianStr.replace("S", "技能冷却缩减；");
		%><tr>
			<td><%=bg.dbId%></td>
			<td><%=bg.itemId%></td>
			<td><%=bg.dbId<=0 ? "" : BigSwitch.getInst().bagMgr
								.getItemName(bg.itemId) %></td>
			<td><a href="?action=takeoff&jzId=<%=jzId%>&bagIndex=<%=i%>">脱下</a></td>
			<%
			   Long instId=bg.instId;
			   if(instId>0){
			%>
			<td><a target='_blank' href="ShowUserEquip.jsp?instId=<%=bg.dbId%>"><%=bg.instId%></a> </td>
			<% 
			   }else{
				   %>
				   <td><%=bg.instId%> </td>
				   <%
			   }
			%>
			<td><%=bg.itemId<=0 ? "-" :
				(TempletService.itemMap.get(bg.itemId)==null)?
						"没有找到配置":
							((ZhuangBei)TempletService.itemMap.get(bg.itemId)).getJinjieItem()
							%> </td>
			<td><%=ue==null?"没强化信息":ue.getLevel()%> </td>
			<td><%=ue==null?"没强化信息":ue.getExp()%> </td>
		<%
			   if(instId>0){
			%>
			<td><a target='_blank' href="ShowUserEquip.jsp?instId=<%=bg.dbId%>"><%=xilianStr%> </a> </td>
				<% 
			   }else{
				   %>
				   <td><%=xilianStr%> </td>
				   <%
			   }%>
		</tr>
		<%
			}
		%>
	</table>
	背包物品--------------
	<br />
	<%
		//
				if ("addItem".equals(action)&&Integer.valueOf(itemNum)>=0) {
					int cnt = Integer.valueOf(itemNum);
					long instId = -1;
					int iid = Integer.parseInt(itemId);
					BaseItem it = TempletService.itemMap.get(iid);
					if(cnt>888){
						out.println("<br/>数量太大，最大888<bar/>");
					}else if (it != null) {
						BigSwitch.inst.bagMgr
								.addItem(bag, iid, cnt, instId,  junzhu.level, "jsp页面添加");
								//addItem(bag, iid, cnt, instId, junzhu.level);
						SessionUser su = SessionManager.inst.findByJunZhuId(bag.ownerId);
						if(su!=null){
							BigSwitch.inst.bagMgr.sendBagInfo(0, su.session, null);
							JunZhuMgr.inst.sendMainInfo(su.session);
						}
						out.println("<br/>已添加"+Integer.valueOf(itemNum)+"个<bar/>" + it.getName());
					} else {
						out.println("<br/>物品没有找到<bar/>");
					}
				}
				//
				List<BagGrid> list = bag.grids;
				int cnt = list.size();
				out.print("背包物品数量：" + cnt);
				String bagCntKey = "BagCnt#"+bag.ownerId;
				Object mcO = com.manu.dynasty.store.MemcachedCRUD.getMemCachedClient().get(bagCntKey);
				out.print("&nbsp;MemoryCache保存的BagCnt:" + mcO);
	%><table border='1'>
		<tr>
			<th>dbId</th>
			<th>itemId</th>
			<th>名称</th>
			<th>cnt</th>
			<th>op</th>
			<th>instId</th>
			<th>db type</th>
		</tr>
		<%
			for (int i = 0; i < cnt; i++) {
						BagGrid bg = list.get(i);
		%><tr>
			<td><%=bg.dbId%></td>
			<td><%=bg.itemId%></td>
			<td><%=bg.dbId<=0 ? "" : BigSwitch.getInst().bagMgr
								.getItemName(bg.itemId)%></td>
			<td><%=bg.cnt==0?"没有啦":bg.cnt%></td>
			<td>
			<a href="?action=wear&jzId=<%=jzId%>&bagIndex=<%=i%>">穿上</a>|
			---<a href="?action=delete&jzId=<%=jzId%>&bagIndex=<%=i%>">删</a>---|
			</td>
			<%
			   Long instId=bg.instId;
			   if(instId>0){
			%>
			<td><a href="ShowUserEquip.jsp?instId=<%=instId%>"><%=bg.instId%></a> </td>
			<% 
			   }else{
				   %>
				   <td><%=bg.instId%> </td>
				   <%
			   }
			%>
			<td><%=bg.type%></td>
		</tr>
		<%
			}
		%>
	</table>
	<br/>
	<%HuanWu hw = HibernateUtil.find(HuanWu.class, junzhuId);
	if(hw != null){
		out("换物箱信息：");
		out("联盟id:"+hw.lmId+"--->");
		out("1-"+hw.slot1+";");
		out("2-"+hw.slot2+";");
		out("3-"+hw.slot3+";");
		out("4-"+hw.slot4+";");
		out("5-"+hw.slot5+";");
	}
	%>
	<br/>
	
	<%
		JunZhu junZhu = HibernateUtil.find(JunZhu.class, junzhuId);
		out("免费洗练次数剩余:");out(TimeWorkerMgr.instance.getXilianTimes(junZhu));
		XiLian xiLian = HibernateUtil.find(XiLian.class, junzhuId);
		if(xiLian == null){
			out("没有洗练数据。");
		}else{
			out("上次洗练时间:");out(xiLian.getDate());
			out("元宝洗练次数:");out(xiLian.getNum());
		}
		if (cnt == 0) {
					//BigSwitch.getInst().bagMgr.addItem(bag, 100001, 1, 1);//test codes
				}
				if (cnt0 == 0) {
					EquipGrid eg = new EquipGrid();
					eg.dbId = 0;
					//HibernateUtil.save(eg);//test codes
				}
			}
		}
	%>
</body>
</html>