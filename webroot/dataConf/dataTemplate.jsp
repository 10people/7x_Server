<%@page import="com.qx.account.AccountManager"%>
<%@page import="com.manu.dynasty.boot.GameServerInit"%>
<%@page import="com.manu.dynasty.util.DataLoader"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.lang.reflect.Modifier"%>
<%@page import="java.lang.reflect.Field"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.manu.dynasty.template.LegendPveTemp"%>
<%@page import="com.manu.dynasty.template.ZhuXian"%>
<%@page import="com.manu.dynasty.template.JunzhuShengji"%>
<%@page import="com.manu.dynasty.template.Keji"%>
<%@page import="com.manu.dynasty.template.PveTemp"%>
<%@page import="com.manu.dynasty.template.ShiBing"%>
<%@page import="com.manu.dynasty.template.BaseItem"%>
<%@page import="com.manu.dynasty.template.Jiangli"%>
<%@page import="com.manu.dynasty.template.EnemyTemp"%>
<%@page import="com.manu.dynasty.template.ZhuangBei"%>
<%@page import="com.manu.dynasty.template.HeroProtoType"%>
<%@page import="com.qx.hero.HeroMgr"%>
<%@page import="com.manu.dynasty.template.HeroGrow"%>
<%@page import="com.manu.dynasty.template.ItemTemp"%>
<%@page import="java.util.Map"%>
<%@page import="com.qx.pve.GuanQiaStartRewardBean"%>
<%@page import="qxmobile.protobuf.PveLevel.PveBattleOver"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="com.qx.pve.PveRecord"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>数据模板</title>
</head>
<body>
<%
Iterator<String> it0 = TempletService.templetMap.keySet().iterator();
int cnt0=0;
while(it0.hasNext()){
	cnt0++;
	String dl = it0.next();
	out("<a href='?clazz="+dl+"'>"+dl+"</a>|");
	if(cnt0==10){
		br();
		cnt0=0;
	}
}
%>
<br/>
<a href='?type=reload'>reload</a>|
<br/>
<a href='?type=junZhu'>君主</a>|
<a href='?type=item'>物品</a>|
<a href='?type=equip'>装备</a>|
<a href='?type=heroProto'>武将proto</a>|
<a href='?type=hero'>武将grow</a>|
<a href='?type=building'>建筑</a>|
<a href='?type=pveGuanQia'>PVE关卡</a>|
<a href='?type=pveGuanQiaCQ'>传奇关卡</a>|
<a href='?type=pveGuanQiaJinDu'>关卡进度</a>|
<a href='?type=EnemyTemp'>EnemyTemp</a>|
<a href='?type=dailyAward'>每日-累登奖励</a>|<br/>
<a href='?type=shiBin'>士兵</a>|
<a href='?type=ZhuXian'>主线</a>|
<a href='?type=wuJiangKeJi'>武将科技</a>|
<a href='?type=sensitiveWord'>sensitiveWord</a>|
<br/>
<form action="uploadConf.jsp" method="post"
                        enctype="multipart/form-data">
<input type="file" name="file" size="50" />
<input type="submit" value="上传" />
</form>
<%
String clazz = request.getParameter("clazz");
if(clazz != null){
	List<?> lz = TempletService.listAll(clazz);
	Field fs[] = null;
	tableStart();
	for(Object o : lz){
		trS();
		if(fs == null){
			fs = o.getClass().getDeclaredFields();
			trS();
			for(Field f: fs){
				f.setAccessible(true);
				if(Modifier.isStatic(f.getModifiers()))continue;
				td(f.getName());
			}
			trE();
		}
		for(Field f: fs){
			if(Modifier.isStatic(f.getModifiers()))continue;
			Object d = f.get(o);
			if(d==null){
				td("null");
			}else if(d instanceof int[]){
				td(Arrays.toString((int[])d));
			}else{
				td(d);
			}
		}
		trE();
	}
	tableEnd();
}

String type = request.getParameter("type");
if("item".equals(type)){
	List<ItemTemp> list = TempletService.listAll(ItemTemp.class.getSimpleName());
	out.append("<table border='1'>");
	out.append("<tr>");
	out.append("<th>ID</th><th>名称</th><th>类型</th>");
	out.append("</tr>");
	for(ItemTemp t : list){
		out.append("<tr>");
		out.append("<td>");		out.println(t.id);	out.append("</td>");
		out.append("<td>");		out.println(HeroService.getNameById(t.name));		out.append("</td>");
		out.append("<td>");		out.println(t.getType());	out.append("</td>");
		out.append("<tr>");
	}
	out.append("</table>");
	out("<pre>");
	for(ItemTemp t : list){
		out.print(t.id);
		out(",");out(HeroService.getNameById(t.name));out("\n");
	}
	out("</pre>");
}else if("sensitiveWord".equals(type)){
	for(String v:BigSwitch.inst.accMgr.getSensitiveWord()){
		out(v);br();
	}
}else if("reload".equals(type)){
	GameServerInit.reloadData();out("-reload ok-");
}else if("junZhu".equals(type)){
	List<JunzhuShengji> list = TempletService.listAll(JunzhuShengji.class.getSimpleName());
	out.append("<table border='1'>");
	out.append("<tr>");
	out.append("<th>等级</th><th>攻击</th><th>防御</th><th>生命</th>");
	out.append("</tr>");
	for(JunzhuShengji t : list){
		out.append("<tr>");
		out.append("<td>");		out.println(t.lv);	out.append("</td>");
		out.append("<td>");		out.println(t.gongji);		out.append("</td>");
		out.append("<td>");		out.println(t.fangyu);		out.append("</td>");
		out.append("<td>");		out.println(t.getShengming());	out.append("</td>");
		out.append("<tr>");
	}
	out.append("</table>");
}else if("ZhuXian".equals(type)){
	List<ZhuXian> list = TempletService.listAll(ZhuXian.class.getSimpleName());
	out.append("<table border='1'>");
	out.append("<tr>");
	out.append("<th>ID</th><th>orderIdx</th><th>标题</th><th>TriggerType</th><th>TriggerCond</th><th>奖励</th>");
	out.append("</tr>");
	for(ZhuXian t : list){
		out.append("<tr>");
		out.append("<td>");		out.println(t.getId());	out.append("</td>");
		td(t.orderIdx);
		out.append("<td>");		out.println(t.getTitle());		out.append("</td>");
		out.append("<td>");		out.println(t.getTriggerType());		out.append("</td>");
		out.append("<td>");		out.println(t.getTriggerCond());	out.append("</td>");
		out.append("<td>");		out.println(t.getAward());	out.append("</td>");
		out.append("<tr>");
	}
	out.append("</table>");
	br();
	out("<pre>");
	for(ZhuXian t : list){
		if(t.type != 0)continue;
		String ss = t.getTitle();
		ss = ss.substring(ss.indexOf("[-]")+4);
		out(t.getId());out(",");out(ss);out("\n");
	}
	out("</pre>");
}else if("EnemyTemp".equals(type)){
	List<EnemyTemp> list = TempletService.listAll(EnemyTemp.class.getSimpleName());
	out.append("<table border='1'>");
	out.append("<tr>");
	out.append("<th>ID</th><th>名称</th><th>生命</th>");
	out.append("</tr>");
	for(EnemyTemp t : list){
		out.append("<tr>");
		out.append("<td>");		out.println(t.getId());	out.append("</td>");
		out.append("<td>");		out.println(HeroService.getNameById(t.getName()));		out.append("</td>");
		out.append("<td>");		out.println(t.getShengming());	out.append("</td>");
		out.append("<tr>");
	}
	out.append("</table>");
}else if("wuJiangKeJi".equals(type)){
	List<Keji> list = TempletService.listAll(Keji.class.getSimpleName());
	out.append("<table border='1'>");
	out.append("<tr>");
	out.append("<th>ID</th><th>名称</th>");
	out.append("</tr>");
	for(Keji t : list){
		out.append("<tr>");
		out.append("<td>");		out.println(t.getId());	out.append("</td>");
		out.append("<td>");		out.println(HeroService.getNameById(t.getName()));		out.append("</td>");
		out.append("<tr>");
	}
	out.append("</table>");
}else if("shiBin".equals(type)){
	List<ShiBing> list = TempletService.listAll(ShiBing.class.getSimpleName());
	out.append("<table border='1'>");
	out.append("<tr>");
	out.append("<th>ID</th><th>速度</th><th>数量</th>");
	out.append("</tr>");
	for(ShiBing t : list){
		out.append("<tr>");
		out.append("<td>");		out.println(t.getId());	out.append("</td>");
		out.append("<td>");		out.println(t.getMoveSpeed());	out.append("</td>");
		out.append("<td>");		out.println(t.getNum());	out.append("</td>");
		out.append("<tr>");
	}
	out.append("</table>");
}else if("dailyAward".equals(type)){
	List<Jiangli> list = TempletService.listAll(Jiangli.class.getSimpleName());
	out.append("<table border='1'>");
	out.append("<tr>");
	out.append("<th>ID</th><th>名称</th><th>个数</th>");
	out.append("</tr>");
	for(Jiangli t0 : list){
		out.append("<tr><td colspan='3'>"+HeroService.getNameById(t0.getName())+"</td>");out.append("</tr>");
		String txt = t0.getItem();
		String[] parts = txt.split("#");
		for(String v:parts){
			String[] nums = v.split(":");
			int t = Integer.parseInt(nums[0]);
			int id = Integer.parseInt(nums[1]);
			int cnt = Integer.parseInt(nums[2]);
			String iName = ""+id;
			switch(t){
			case 0:
			case 2:
				BaseItem it = TempletService.itemMap.get(id);
				if(it != null){
					iName = it.getName();
				}
				break;
			case 7:
				HeroProtoType proto = HeroMgr.tempId2HeroProto.get(id);
				if(proto != null){
					iName = proto.getHeroName()+"";
				}
				break;
			}
			out.append("<tr>");
			out.append("<td>");		out.println(id);	out.append("</td>");
			out.append("<td>");		out.println(HeroService.getNameById(iName));		out.append("</td>");
			out.append("<td>");		out.println(cnt);	out.append("</td>");
			out.append("</tr>");
		}
	}
	out.append("</table>");
}else if("equip".equals(type)){
	List<ZhuangBei> list = TempletService.listAll(ZhuangBei.class.getSimpleName());
	out.append("<table border='1'>");
	out.append("<tr>");
	out.append("<th>ID</th><th>名称</th><th>部位</th><th>进阶材料</th>");
	out.append("<th>攻击</th><th>防御</th><th>生命</th>");
	out.append("<th>武器伤害</th><th>武器减免</th><th>武器暴击</th>");
	out.append("<th>武器韧性</th><th>技能伤害</th><th>技能减免</th>");
	out.append("<th>技能暴击</th><th>技能韧性</th><th>强化等级经验配置ID</th>");
	out.append("<th>提供强化经验值</th>");
	out.append("</tr>");
	for(ZhuangBei t : list){
		out.append("<tr>");
		out.append("<td>");		out.println(t.getId());	out.append("</td>");
		out.append("<td>");		out.println(HeroService.getNameById(t.getName()));		out.append("</td>");
		out.append("<td>");		out.println(t.getBuWei());	out.append("</td>");
		out.append("<td>");		out.println(t.getJinjieItem());	out.append("</td>");
		out.append("<td>");		out.println(t.getGongji());	out.append("</td>");
		out.append("<td>");		out.println(t.getFangyu());	out.append("</td>");
		out.append("<td>");		out.println(t.getShengming());	out.append("</td>");
		out.append("<td>");		out.println(t.getWqSH());	out.append("</td>");
		out.append("<td>");		out.println(t.getWqJM());	out.append("</td>");
		out.append("<td>");		out.println(t.getWqBJ());	out.append("</td>");
		out.append("<td>");		out.println(t.getWqRX());	out.append("</td>");
		out.append("<td>");		out.println(t.getJnSH());	out.append("</td>");
		out.append("<td>");		out.println(t.getJnJM());	out.append("</td>");
		out.append("<td>");		out.println(t.getJnBJ());	out.append("</td>");
		out.append("<td>");		out.println(t.getJnRX());	out.append("</td>");
		out.append("<td>");		out.println(t.getExpId());	out.append("</td>");
		out.append("<td>");		out.println(t.getExp());	out.append("</td>");
		out.append("<tr>");
	}
	out.append("</table>");
}else if("hero".equals(type)){
	List<HeroGrow> list = TempletService.listAll(HeroGrow.class.getSimpleName());
	out.append("<table border='1'>");
	out.append("<tr>");
	out.append("<th>ID</th><th>名称</th><th>star</th>");
	out.append("</tr>");
	for(HeroGrow t : list){
		HeroProtoType proto = HeroMgr.id2Hero.get(t.getHeroId());
		out.append("<tr>");
		out.append("<td>");		out.println(t.getId());		out.append("</td>");
		out.append("<td>");		out.println(HeroService.getNameById(proto.getName()));	out.append("</td>");
		out.append("<td>");		out.println(t.getStar());		out.append("</td>");
		out.append("<tr>");
	}
	out.append("</table>");
}else if("heroProto".equals(type)){
	List<HeroProtoType> list = TempletService.listAll(HeroProtoType.class.getSimpleName());
	out.append("<table border='1'>");
	out.append("<tr>");
	out.append("<th>ID</th><th>名称</th>");
	out.append("</tr>");
	for(HeroProtoType t : list){
		out.append("<tr>");
		out.append("<td>");		out.println(t.getId());		out.append("</td>");
		out.append("<td>");		out.println(HeroService.getNameById(t.getName()));	out.append("</td>");
		out.append("<tr>");
	}
	out.append("</table>");
}else if("pveGuanQia".equals(type)){
	List<PveTemp> list = TempletService.listAll(PveTemp.class.getSimpleName());
	out.append("<table border='1'>");
	out.append("<tr>");
	out.append("<th>章节</th><th>模板ID</th><th>章节名称</th><th>关卡名称</th><th>精英？</th><th>敌方武将ID</th><th>money</th><th>exp</th>");
	out.append("</tr>");
	for(PveTemp t : list){
		out.append("<tr>");
		out.append("<td>");		out.println(t.getBigId());		out.append("</td>");
		out.append("<td>");		out.println(t.getId());		out.append("</td>");
		out.append("<td>");		out.println(HeroService.getNameById(t.bigName));		out.append("</td>");
		out.append("<td>");		out.println(HeroService.getNameById(t.getSmaName()));		out.append("</td>");
		out.append("<td>");		out.println(t.getChapType() == 1 ? "是" : "");		out.append("</td>");
		out.append("<td>");		out.println(t.getNpcId());		out.append("</td>");
		out.append("<td>");		out.println(t.getMoney());		out.append("</td>");
		out.append("<td>");		out.println(t.getExp());		out.append("</td>");
		out.append("<tr>");
	}
	out.append("</table>");
	br();
	out("<pre>");
	for(PveTemp t : list){
		out(t.getId());out(",");out(HeroService.getNameById(t.getSmaName()));out("\n");
	}
	out("</pre>");
	br();
}else if("pveGuanQiaCQ".equals(type)){
	List<PveTemp> list = TempletService.listAll(LegendPveTemp.class.getSimpleName());
	out.append("<table border='1'>");
	out.append("<tr>");
	out.append("<th>章节</th><th>模板ID</th><th>章节名称</th><th>关卡名称</th><th>精英？</th><th>敌方武将ID</th><th>money</th><th>exp</th>");
	out.append("</tr>");
	for(PveTemp t : list){
		out.append("<tr>");
		out.append("<td>");		out.println(t.getBigId());		out.append("</td>");
		out.append("<td>");		out.println(t.getId());		out.append("</td>");
		out.append("<td>");		out.println(HeroService.getNameById(t.getBigName()));		out.append("</td>");
		out.append("<td>");		out.println(HeroService.getNameById(t.getSmaName()));		out.append("</td>");
		out.append("<td>");		out.println(t.getChapType() == 1 ? "是" : "");		out.append("</td>");
		out.append("<td>");		out.println(t.getNpcId());		out.append("</td>");
		out.append("<td>");		out.println(t.getMoney());		out.append("</td>");
		out.append("<td>");		out.println(t.getExp());		out.append("</td>");
		out.append("<tr>");
	}
	out.append("</table>");
}else if("pveGuanQiaJinDu".equals(type)){
	Map<Integer, PveRecord> records = BigSwitch.pveGuanQiaMgr.recordMgr.getRecords(1L);
	out.append("<table border='1'>");
	out.append("<tr>");
	out.append("<th>序号</th><th>通过？</th><th>获得星级</th><th>设置为 通过/未过</th>");
	out.append("</tr>");
	int i = 0;
	String op = request.getParameter("op");
	for(PveRecord t : records.values()){
		i++;
		if(String.valueOf(i).equals(op)){
			PveBattleOver.Builder b = PveBattleOver.newBuilder();
			b.setSPass(t.star>=0 ? false : true);
			if(!b.getSPass())t.star = -1;
			b.setSSection(i-1);
			BigSwitch.inst.pveMgr.battleOver(0, new RobotSession(), b);
		}
		out.append("<tr>");
		out.append("<td>");		out.println(i);		out.append("</td>");
		out.append("<td>");		out.println(t.star>=0);		out.append("</td>");
		out.append("<td>");		out.println(t.star);		out.append("</td>");
		out.append("<td>");		out.println("<a href='?type=pveGuanQiaJinDu&op="+i+"'>设置</a>");		out.append("</td>");
		out.append("<tr>");
	}
	out.append("</table>");
	GuanQiaStartRewardBean[] startRewardStatus = BigSwitch.pveGuanQiaMgr.startRewardStatus;
	out.append("<br/>");
	out.append("星星数奖励状态<br/>");
	for(GuanQiaStartRewardBean starInfo : startRewardStatus){
		out.append("星"+starInfo.start+" 领取了么 ？" + (starInfo.pick ? "是" : "no"));
		out.append("<br/>");
	}
}
%>
</body>
</html>