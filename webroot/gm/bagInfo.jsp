<%@page import="java.util.Optional"%>
<%@page import="com.qx.util.TableIDCreator"%>
<%@page import="qxmobile.protobuf.Explore.Award"%>
<%@page import="com.qx.equip.web.UEConstant"%>
<%@page import="java.lang.reflect.Method"%>
<%@page import="java.lang.reflect.Field"%>
<%@page import="com.manu.dynasty.template.ZhuangbeiPinzhi"%>
<%@page import="com.qx.equip.domain.EquipXiLian"%>
<%@page import="com.qx.equip.jewel.JewelMgr"%>
<%@page import="com.qx.award.AwardMgr"%>
<%@page import="com.manu.dynasty.template.AwardTemp"%>
<%@page import="qxmobile.protobuf.UserEquipProtos.EquipJinJie"%>
<%@page import="com.qx.fuwen.FuwenMgr"%>
<%@page import="com.manu.dynasty.template.Fuwen"%>
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
		String equipStr = request.getParameter("equipId");
		String qianghuaStr = request.getParameter("qiangHuaExp");
		String jinJieStr = request.getParameter("jinJieExp");
		String cailiaoStr = request.getParameter("cailiao");
		if(jzId == null){
			Object jzObj = session.getAttribute("jzId");
			if(jzObj == null) {
				out.println("请先登录帐号！");
			}else {
				jzId = jzObj.toString();
			}
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
	<form action="">
		目标装备instID：<input type="text" name="equipId" value="<%%>">
		强化经验：<input type="text" name="qiangHuaExp" value="<%%>">
		进阶经验：<input type="text" name="jinJieExp" value="<%%>"> 
		<input type="hidden" name="action" value="addExp"> 
		<button type="submit">增加装备经验</button>
	</form>
	<form action="">
		目标装备instID：<input type="text" name="equipId" value="<%%>">
		进阶材料：<input type="text" name="cailiao" value="<%%>"> 
		<input type="hidden" name="action" value="jinjie"> 
		<button type="submit">装备进阶</button>
	</form>
	<form action="">
		<input type="hidden" name="action" value="addFuWen"> 
		<button type="submit">添加符文</button>不要乱点！！！会很卡！！！
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
					long idx = Long.parseLong(request.getParameter("bagIndex"));
					IoSession su = SessionManager.inst.findByJunZhuId(junzhuId);
					if(su!=null){
						BigSwitch.inst.equipMgr.equipAdd(su,equips, bag, idx);
					}
				}else if ("takeoff".equals(action)) {
					int idx = Integer.parseInt(request.getParameter("bagIndex"));
					IoSession su = SessionManager.inst.findByJunZhuId(junzhuId);
					if(su!=null){
						BigSwitch.inst.equipMgr.equipRemove(su, equips, bag, idx);
					}
				}else if ("delete".equals(action)) {
					long idx = Long.parseLong(request.getParameter("bagIndex"));
					BagGrid bg = BagMgr.inst.getBagGrid(bag, idx);
					IoSession ioSession = SessionManager.inst.getIoSession(junzhu.id);
					if(bg != null) {
						BagMgr.inst.removeItemByBagdbId(ioSession, bag, "jsp页面删除", idx, bg.cnt, junzhu.level);
						bg.instId = bg.itemId = bg.cnt = 0;
						HibernateUtil.save(bg);
					}
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
					IoSession su = SessionManager.inst.findByJunZhuId(junzhuId);
					if(su!=null){
						BagMgr.inst.sendBagInfo(0, su, null);
						BagMgr.inst.sendEquipInfo(0, su, null);
						JunZhuMgr.inst.sendMainInfo(su);
					}
		   		}else if("addExp".equals(action)){
		   			if(equipStr!=null && equipStr.length()> 0){
		   				long equipId= Long.parseLong(equipStr);
		   				UserEquip ue = HibernateUtil.find(UserEquip.class, equipId);
			   			if(ue != null){
			   				if(qianghuaStr!=null &&qianghuaStr.length()>0){
			   					int addExp = Integer.parseInt(qianghuaStr);
			   					int totalExp = ue.exp+addExp;
			   					ue.exp = totalExp;
			   				}
			   				if(jinJieStr != null&& jinJieStr.length()>0){
			   					int addExp = Integer.parseInt(jinJieStr);
			   					ue.JinJieExp += addExp;
			   				}
			   				HibernateUtil.save(ue);
			   			}
		   			}
		   		}else if("jinjie".equals(action)){
		   			if(cailiaoStr != null && cailiaoStr.length()>0){
		   				EquipJinJie.Builder builder = EquipJinJie.newBuilder();
		   				String[] cailiaos = cailiaoStr.split(",");
		   				builder.setEquipId(Long.parseLong(equipStr));
		   				for(String s : cailiaos){
		   					builder.addCailiaoList(Long.parseLong(s));
		   				}
		   				IoSession su = SessionManager.inst.getIoSession(junzhuId);
		   				if(su != null){
		   					UserEquipAction.instance.newEquipJinJie(0, su, builder);
		   				}
		   			}
		   		}else if("addFuWen".equals(action)){
		   			List<Fuwen> fuWenList = TempletService.getInstance().listAll(Fuwen.class.getSimpleName());
		   			for(Fuwen f : fuWenList){
		   				if(f.type == 8){
		   					AwardTemp a = new AwardTemp();
			   				a.awardId = 0;
			   				a.itemId = f.fuwenID;
			   				a.itemNum = 1;
			   				a.itemType = 8;
			   				AwardMgr.inst.giveReward(null, a, junzhu, false, false);
		   				}
		   			}
		   		}else if("xilian".equals(action)){
		   			String junZhuIdstr = request.getParameter("xljzId");
		   			String equipIdstr = request.getParameter("xlequipId");
		   			String itemIdstr = request.getParameter("xlitemId");
		   			String instIdstr = request.getParameter("xlinstId");
		   			long xlJzId = Long.parseLong(junZhuIdstr);
		   			long xlEquipDbId = Long.parseLong(equipIdstr);
		   			int xlEquipId = Integer.parseInt(itemIdstr);
		   			int xlInstId = Integer.parseInt(instIdstr);
		   			EquipXiLian xilian = HibernateUtil.find(EquipXiLian.class, "where junZhuId= " + xlJzId );
		   			if(xilian == null ){
		   				xilian = new EquipXiLian();
		   				xilian.equipId = xlEquipDbId;
		   				xilian.junZhuId = xlJzId;
		   				ZhuangBei equipTemp = TempletService.getInstance().getZhuangBei(xlEquipId);
		   				UserEquip xlUe = HibernateUtil.find(UserEquip.class, xlInstId);
		   				List<ZhuangbeiPinzhi> pinZhiList = TempletService.listAll(ZhuangbeiPinzhi.class.getSimpleName());
		   				ZhuangbeiPinzhi pinZhiTemp = null ;
		   				for(ZhuangbeiPinzhi zbpz : pinZhiList){
		   					if(zbpz.pinzhi == equipTemp.pinZhi ){
		   						pinZhiTemp = zbpz;
		   						break ;
		   					}
		   				}
		   				if(pinZhiTemp != null && xlUe != null && xlUe.hasXilian != null ){
		   					int maxXiLianZhi = new Double(pinZhiTemp.paraX).intValue();
			   				
			   				String xilianStr = xlUe.hasXilian;
			   				String names[] = {"wqSH","wqJM","wqBJ","wqRX",
									"jnSH","jnJM","jnBJ","jnRX"};
			   				Field[] fs = new Field[names.length];
			   				Method[] getPeiZhi = new Method[names.length];
			   				Method[] getUe = new Method[names.length];
			   				Method[] setXiLian = new Method[names.length];
			   				for(int i = 0 ; i< fs.length ; i++ ){
			   					Field f = null;
								try {
									f = UEConstant.class.getDeclaredField(names[i]);
								} catch (Exception e) {
									continue;
								}
								f.setAccessible(true);
								fs[i]=f;
								String name = f.getName();
								String mName = "get"+name.substring(0,1).toUpperCase()+name.substring(1);
								String setName = "set"+name.substring(0,1).toUpperCase()+name.substring(1)+"Add";
								
								Method m = null;
								Method mUE = null;
								Method mXL = null ;
								try {
									m = ZhuangBei.class.getDeclaredMethod(mName);
									mUE = UserEquip.class.getDeclaredMethod(mName);
									mXL = EquipXiLian.class.getDeclaredMethod(setName,int.class);
								} catch (Exception e) {
									continue;
								}
								getPeiZhi[i] = m;
								getUe[i] = mUE;
								setXiLian[i] = mXL;
			   				}
			   				for(int i = 0 ;i < fs.length ; i++){
			   					if(fs[i] == null ) continue;
			   					
			   					if(xilianStr.contains((String)fs[i].get(null))){
			   						try{
			   							
			   							int v1 = (Integer)getPeiZhi[i].invoke(equipTemp);
			   							int v2 = (Integer)getUe[i].invoke(xlUe);
			   							int needxiLian = maxXiLianZhi - v1 - v2 ;
			   							setXiLian[i].invoke(xilian, needxiLian);
			   						}catch(Exception e){
			   							continue;
			   						}
			   						
				   				}
			   				}
			   				long equipXiLianId = (TableIDCreator.getTableID(EquipXiLian.class, 1L));
			   				xilian.id = equipXiLianId;
			   				HibernateUtil.insert(xilian);
		   				}
		   			}else{
		   				HibernateUtil.delete(xilian);
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
			<th>进阶经验</th>
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
						int jinJieExp = 0;
						if(bg.dbId!=0){
							xilianStr = ue == null ? "没洗练信息" : ue
									.hasXilian == null ? "没洗练信息" : ue
									.hasXilian;
							if ("".equals(xilianStr)) {
								xilianStr = "没洗练信息";
							}
							jinJieExp = ue==null?0:ue.JinJieExp;
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
			<td><%=jinJieExp%></td>
			<td><%=ue==null?"没强化信息":ue.level%> </td>
			<td><%=ue==null?"没强化信息":ue.exp%> </td>
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
			<td><a href="?action=xilian&xljzId=<%=jzId%>&xlequipId=<%=bg.dbId%>&xlitemId=<%=bg.itemId%>&xlinstId=<%=bg.instId%>">洗满</a></td>
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
					if(it == null){
						out.print("没有这个道具，itemId："+itemId);
						return;
					}
					if(cnt>888){
						out.println("<br/>数量太大，最大888<bar/>");
					}else if (it != null) {
						Fuwen fuwen = FuwenMgr.inst.fuwenMap.get(iid);
						if(fuwen != null && fuwen.type != JewelMgr.Jewel_Type_Id) {
							instId = 0;
						}
						IoSession su = SessionManager.inst.findByJunZhuId(bag.ownerId);
						BigSwitch.inst.bagMgr
								.addItem(su,bag, iid, cnt, instId,  junzhu.level, "jsp页面添加");
								//addItem(bag, iid, cnt, instId, junzhu.level);
						if(su!=null && su != null){
							//BigSwitch.inst.bagMgr.sendBagInfo(0, su.session, null);
							JunZhuMgr.inst.sendMainInfo(su);
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
			<a href="?action=wear&jzId=<%=jzId%>&bagIndex=<%=bg.dbId%>">穿上</a>|
			---<a href="?action=delete&jzId=<%=jzId%>&bagIndex=<%=bg.dbId%>">删</a>---|
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
			out("上次洗练时间:");out(xiLian.date);
			out("元宝洗练次数:");out(xiLian.num);
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