<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.manu.dynasty.template.YunbiaoTemp"%>
<%@page import="com.qx.ranking.RankingMgr"%>
<%@page import="java.util.Comparator"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.qx.yabiao.YaBiaoRobotProduceMgr"%>
<%@page import="com.manu.dynasty.template.MaJu"%>
<%@page import="com.qx.buff.BuffMgr"%>
<%@page import="com.manu.dynasty.template.Skill"%>
<%@page import="com.qx.alliancefight.AllianceFightMgr"%>
<%@page import="com.qx.yabiao.YBRobotMgr"%>
<%@page import="com.qx.yabiao.YaBiaoHuoDongMgr"%>
<%@page import="com.qx.world.Scene"%>
<%@page import="java.util.Enumeration"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.qx.world.Player"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Set"%>
<%@page import="com.qx.yabiao.YaBiaoRobot"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>监控</title>
</head>
<body>
<%
	String  act= request.getParameter("act");
if("switchOpen".equals(act)){
	BigSwitch.inst.ybMgr.openFlag = !BigSwitch.inst.ybMgr.openFlag;
}else if("productMache".equals(act)) {
	//new YBrobotManageJob().execute(null);
	YaBiaoRobotProduceMgr.inst.produceCart();
}
	String fuliFlag=request.getParameter("fuliFlag");
	String fuli=YaBiaoHuoDongMgr.FULITIME_FLAG?"1":"0";
	if(fuliFlag!=null){
		int canshu=Integer.parseInt(fuliFlag);
		if(1==canshu){
//	 		BigSwitch.inst.ybMgr.setfuliFlagState(canshu);
			BigSwitch.inst.ybMgr.setFuLiState(true);
			YunbiaoTemp.incomeAdd_startTime2="0:0";
			YunbiaoTemp.incomeAdd_endTime2="24:00";
		}else if(0==canshu) {
			
			List<YunbiaoTemp> list = TempletService.listAll(YunbiaoTemp.class.getSimpleName());
			Map<String, YunbiaoTemp> map = new HashMap<String, YunbiaoTemp>();
			for(YunbiaoTemp yb: list){
				map.put(yb.getKey(), yb);
			}
			YunbiaoTemp.incomeAdd_startTime2=map.get("incomeAdd_startTime2").value;
			YunbiaoTemp.incomeAdd_endTime2=map.get("incomeAdd_endTime2").value;
			BigSwitch.inst.ybMgr.setFuLiState(false);
		}
	}else{
		fuliFlag=fuli;
	}
	String saveArea_people_max=request.getParameter("saveArea_people_max");
	saveArea_people_max=saveArea_people_max==null?""+(int)YunbiaoTemp.saveArea_people_max:saveArea_people_max;
	int peopleCanshu=Integer.parseInt(saveArea_people_max);
	if(YunbiaoTemp.saveArea_people_max!=peopleCanshu){
		YunbiaoTemp.saveArea_people_max=peopleCanshu;
	}
	String interval=request.getParameter("interval");
	interval=interval==null?""+YaBiaoRobotProduceMgr.interval:interval;
	int timeInterval=Integer.parseInt(interval);
	if(YaBiaoRobotProduceMgr.interval!=timeInterval){
		YaBiaoRobotProduceMgr.interval=timeInterval;
	}
	
	String normal=request.getParameter("normal");
	MaJu mabian4Normal=YaBiaoHuoDongMgr.majuMap.get(910007);
	normal=normal==null?""+mabian4Normal.value2:normal;
	int tmpNormal=Integer.parseInt(normal);
	if(tmpNormal!=mabian4Normal.value2){
		mabian4Normal.value2=tmpNormal;
	}
	MaJu mabian4Senior=YaBiaoHuoDongMgr.majuMap.get(910008);
	String senior=request.getParameter("senior");
	senior=senior==null?""+mabian4Senior.value2:senior;
	int tmpSenior=Integer.parseInt(senior);
	if(tmpSenior!=mabian4Senior.value2){
		mabian4Senior.value2=tmpSenior;
	}
%>

  	<form action="">
  	 	福利时间(0-关闭1-开启)<input type='text' name='fuliFlag' id='fuliFlag' value='<%=fuliFlag%>'/>
	  	<button type="submit">修改</button>
	</form>
  	<form action="">
  	 	安全区人数上限<input type='text' name='saveArea_people_max' id='saveArea_people_max' value='<%=saveArea_people_max%>'/>
	  	<button type="submit">修改</button>
	</form>
  	<form action="">
  	 	系统马车刷新时间(毫秒	)<input type='text' name='interval' id='interval' value='<%=interval%>'/>
	  	<button type="submit">修改</button>
	</form>
  	<form action="">
  	 	普通马鞭加速倍率（100%之上再加）<input type='text' name='normal'  value='<%=normal%>'/>
	  	<button type="submit">修改</button>
	</form>
  	<form action="">
  	 	高级马鞭加速倍率（100%之上再加）<input type='text' name='senior'  value='<%=senior%>'/>
	  	<button type="submit">修改</button>
	</form>
当前押镖状态为：<%=BigSwitch.inst.ybMgr.openFlag ? "开启" : "关闭"%>
，设置为<a href='?act=switchOpen'><%=BigSwitch.inst.ybMgr.openFlag ? "关闭" : "开启"%></a>
<br/>
<a href='?act=productMache'>手动生成 机器镖车</a>
<br/>
<form action="">
	攻击者君主id:<input type="text" name="attackId" />
	被攻击者君主id:<input type="text" name="targetId" />
	使用的技能id：<input type="text" name="skillId" />
	<input type="hidden" name="act" value="calcDamame"/>
	<input type="submit" value="计算伤害" >
</form>
<%
if("calcDamame".equals(act)) {
	long attackId = Long.parseLong(request.getParameter("attackId"));
	long targetId = Long.parseLong(request.getParameter("targetId"));
	int skillId = Integer.parseInt(request.getParameter("skillId"));
	
	JunZhu attack = HibernateUtil.find(JunZhu.class, attackId);
	JunZhu target = HibernateUtil.find(JunZhu.class, targetId);
	Skill skill =  BuffMgr.inst.getSkillById(skillId);
	int damage = BuffMgr.inst.calcSkillDamage(attack, target, skill, 0);
	out.println("造成的伤害值是:"+damage+"<br/>");
}
%>
<br/>
马车等级：<%=YaBiaoRobotProduceMgr.produceCartList.toString() %><br/>
服务器等级：<%=(int) RankingMgr.inst.getTopJunzhuAvgLevel(50) %><br/>
<%-- 劫镖人数:<%=BigSwitch.inst.ybMgr.jbJz2ScIdMap.size()%> 现在的代码jbJz2ScIdMap废弃 无用，无法快捷的统计劫镖人数 --%>
<br/>
<br/>
	后台输出：
<%
	String isShowLog = request.getParameter("isShowLog");
	if(isShowLog!=null){
		YBRobotMgr.isShowLog = Boolean.valueOf(isShowLog);
	}
	if(Boolean.valueOf(YBRobotMgr.isShowLog)){
%>
				<a>开启</a>|<a href="monitor4YaBiao.jsp?isShowLog=false">关闭</a>
				<%
					} else{
				%>
				<a href="monitor4YaBiao.jsp?isShowLog=true">开启</a>|<a>关闭</a>
				<%
					}
				%>
<br/>
<table border='1'  style='border-collapse:collapse;'>
<tr>
<th>序号</th><th>马车编号</th><th>等级</th><th>userId</th><th>userName</th>
<th>jzId</th>
<th>坐标</th><th>路线</th>
</tr>

<%
	class LevelComparator implements Comparator {  
    public int compare(Object object1, Object object2) {// 实现接口中的方法  
    	Player p1 = (Player) object1; // 强制转换  
    	Player p2 = (Player) object2;  
    	if(p1 == null || p2==null)return 0;
    	int result=new Integer(p2.jzlevel).compareTo(new Integer(p1.jzlevel));
    	if(result==0){
    		YaBiaoRobot	tem1=(YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(p1.jzId);
    		YaBiaoRobot	tem2=(YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(p2.jzId);
    		result=new Integer(tem1.bcNPCNo).compareTo(new Integer(tem2.bcNPCNo));
    	}
        return   result;
    }  
}  
class SafeAreaComparator implements Comparator {  
    public int compare(Object object1, Object object2) {// 实现接口中的方法  
    	Player p2 = (Player) object1; // 强制转换  
    	Player p1 = (Player) object2;  
    	if(p1 == null || p2==null)return 0;
        return new Integer(p1.safeArea).compareTo(new Integer(p2.safeArea));  
    }  
} 
	int cnt = 0;
Enumeration<Integer>  ybkey = BigSwitch.inst.ybMgr.yabiaoScenes.keys();
out("<tr><td colspan='7'>"+"押镖列表</td></tr>");
while(ybkey.hasMoreElements()){
	Integer ybScId =ybkey.nextElement();
	Scene sc = BigSwitch.inst.ybMgr.yabiaoScenes.get(ybScId);
	Iterator<Integer> it2 = sc.players.keySet().iterator();
// 	 out.print("SceneID--"+ybScId+"押镖set--"+ybSet+"<br>");
// 	if(ybSet!=null){
// 		Iterator<Long> it = ybSet.iterator();
// 		while (it.hasNext()) {
// 		    Long str = it.next();
// 			YaBiaoRobot ybrobot=(YaBiaoRobot)BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(str);
// 		    out.print(str+"-"+ybrobot==null?"无":ybrobot.isBattle+";");
// 		}
// 	}
	//sc.players.clear();
	out("<tr><td colspan='7'>"+sc.name+"场景精灵数(人+马车总数)"+sc.players.size()+"</td></tr>");
	cnt += sc.players.size();
	int idx = 0;
	List<Player> cartList=new ArrayList<Player>();
	int checkFlag=-1;
	Player miss = new Player();
	miss.name = "miss";
	while(it2.hasNext()){
		Integer key = it2.next();
		Player p = sc.players.get(key);
		if(p == null){
			p.userId = key;
		}
 
	//Collections.sort(cartList, new LevelComparator());
		idx++;
		YaBiaoRobot	tem=(YaBiaoRobot) BigSwitch.inst.ybrobotMgr.yabiaoRobotMap.get(p.jzId);
		out.append("<tr>");
		String acc = p.getName();
		//int uid = key;
		out.append("<td>");
		out.append(""+idx);
		out.append("</td>");
		out.append("<td>");
		if(tem == null){
			out.append("马车NPC丢失:"+p.jzId);
			//it2.remove();
		}else if(checkFlag!=tem.bcNPCNo){
			checkFlag=tem.bcNPCNo;
			out.append("<font color='green'>"+tem.bcNPCNo+"</font>");
		}else{
			out.append("<font color='red'>"+tem.bcNPCNo+"</font>");
		}
		out.append("</td>");
		out.append("<td>");
		out.append(""+p.jzlevel);
		out.append("</td>");
		out.append("<td>"+"<a target='_blank' href='ShowPlay.jsp?uid="+p.userId+"&&ybScId="+ybScId+"'>");
		out.append(String.valueOf(p.userId));
		out.append("</td>");
		out.append("<td>");
		out.append(acc);
		out.append(p.roleId == Scene.YBRobot_RoleId ? "-车":"");
		out.append("</td>");
		td(p.jzId);
		out.append("<td>");
		out.append("x坐标--"+p.getPosX()+"y坐标--"+p.getPosY()+"z坐标--"+p.getPosZ());
		out.append("</td>");
		out.append("<td>");
		out.append(tem == null ? "" : ""+tem.pathId);
		out.append("</td>");
		
		out.append("<tr>");
	};
}

%>
</table>
押镖场景在线玩家数量:<%=cnt %><br/>
</body>
</html>