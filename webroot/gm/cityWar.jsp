<%@page import="com.qx.alliance.AllianceBean"%>
<%@page import="com.qx.world.FightNPCScene"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.qx.world.SceneMgr"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.qx.alliance.AllianceMgr"%>
<%@page import="org.apache.taglibs.standard.tag.common.core.ForEachSupport"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.manu.dynasty.template.JCZCity" %>
<%@page import="com.qx.alliancefight.*" %>
<%@page import="com.manu.dynasty.util.DateUtils" %>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.account.Account"%>
<%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>城池信息</title>
<script type="text/javascript">
	function hideAwardInfo(){
		var node = document.getElementById("awardInfo");
		if(node.style.display == "block"){
			node.style.display = "none";
		}  else {
			node.style.display = "block";
		}
	}
</script>
</head>
<body>
	<%
	setOut(out);
	
	String name = request.getParameter("account");
	String accIdStr = request.getParameter("accId");// 用户id
	if(name == null && accIdStr == null){
		name = (String)session.getAttribute("name");
	}
	accIdStr = (accIdStr == null ? "":accIdStr.trim());
	name = name == null ? "": name.trim();
	Account account = null;
	if(name != null && name.length()>0){
		account = HibernateUtil.getAccount(name);
	}else if(accIdStr.length()>0){
		account = HibernateUtil.find(Account.class, (Long.valueOf(accIdStr) - GameServer.serverId) / 1000);
		if(account != null)name = account.accountName;
	}
	JunZhu junzhu = null;
do{
	long junZhuId = 0;
	if(account != null){
		session.setAttribute("name", name);
		out("账号");out(account.accountId);out("：");out(account.accountName);
		out("密码：");out(account.accountPwd);
		junZhuId = account.accountId * 1000 + GameServer.serverId;
	}else if(accIdStr.matches("\\d+")){
		junZhuId = Long.parseLong(accIdStr);
	}else{
		out("没有找到");
		break;
	}
	junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
	if(junzhu == null){
		out.println("没有君主");
		break;
	}
}while(false);
	%>
	<form>
		<p>
			账号： <input type="text" name="account" value="<%=name%>"/> 或 君主id： <input type="text" name="accId" value="<%=accIdStr%>"/> <button type="submit">查看</button>
		</p>
	</form>
	<strong>城池信息</strong><br /><br />
	<%
		//处理表单
		List<JCZCity> citySettings = TempletService.getInstance().listAll(JCZCity.class.getSimpleName());
		Map<Integer,JCZCity>jczmap = new HashMap<Integer,JCZCity>();
		for (JCZCity jczCity : citySettings) {
			jczmap.put(jczCity.id,jczCity);
		}
		String act = request.getParameter("action"); 
		if("bidJieSuan".equals(act)){ //结算
			BidMgr.inst.bidJieSuan();
			redirect("cityWar.jsp");
		}else if("setBidLmId".equals(act)){ //设置
			if(request.getParameter("cityId") == "" || request.getParameter("lmId") == ""){
				return;
			}else{
				boolean isEx = false;
				int cityId = Integer.parseInt(request.getParameter("cityId"));
				int lmId = Integer.parseInt(request.getParameter("lmId"));
				for(JCZCity jczCity:citySettings){
					if(jczCity.id == cityId){
						isEx = true;
						break;
					}
				}
				if(!isEx || "".equals(AllianceMgr.inst.getAllianceName(lmId))){
					alert("联盟ID或城池id不存在请检查");
				}else{
					CityBean cityBean = HibernateUtil.find(CityBean.class,cityId);
					if(cityBean == null){
						cityBean = new CityBean();
						cityBean.cityId = cityId;
						cityBean.lmId = 0; //没人占领
						cityBean.atckLmId = lmId;
						HibernateUtil.insert(cityBean);
					}else{
						if(cityBean.lmId != lmId){
							cityBean.atckLmId = lmId;
							HibernateUtil.update(cityBean);
						}
					}
				}
			}
			redirect("cityWar.jsp");
		}else if("clearScs".equals(act)){ //
			BigSwitch.inst.scMgr.fightScenes.clear();
			redirect("cityWar.jsp");
		}else if("setBidTime".equals(act)){ //设置时间段
			String time1 = request.getParameter("time1");
			String time2 = request.getParameter("time2");
			String time3 = request.getParameter("time3");
			String time4 = request.getParameter("time4");
			if(!"".equals(time1) && !"".equals(time2) && !"".equals(time3) && !"".equals(time4)){
				BidMgr.city_war_declaration_startTime = time1;
				BidMgr.city_war_preparation_startTime = time2;
				BidMgr.city_war_fighting_startTime = time3;
				BidMgr.city_war_fighting_endTime = time4;
				FightNPCScene.limitNPCCnt = Integer.parseInt(request.getParameter("limitNPCCnt"));
			}else{
				alert("输入为空设置失败");
			}
		}else if("clearBidData".equals(act)){
// 			String sql = "delete from " + BidBean.class.getSimpleName();
// 			HibernateUtil.executeSql(sql);
// 			//重置结算数据
// 			List<CityBean> cityList = HibernateUtil.list(CityBean.class,"where 1=1");
// 			if(cityList != null){
// 				for (CityBean cityBean : cityList) {
// 					cityBean.atckLmId = 0;
// 					HibernateUtil.update(cityBean);
// 				}
// 			}
// 			//重置进入战场数据
// 			List<EnterWarTimeBean> enterWarTimeList = HibernateUtil.list(EnterWarTimeBean.class,"where 1=1");
// 			if(enterWarTimeList != null){
// 				for (EnterWarTimeBean enterWarTimeBean : enterWarTimeList) {
// 					HibernateUtil.delete(enterWarTimeBean);
// 				}
// 			}
			BidMgr.inst.regularClearData();
			//野城战斗时间修改，每天只能打一次，重复测试清数据要把时间修改到今天之前
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_MONTH,-1);//今天之前的日期
			String dt = DateUtils.datetime2Text(calendar.getTime());
			String sql = "update " + WildCityBean.class.getSimpleName() + " set winTime='" + dt + "'";
			HibernateUtil.executeSql(sql);
		}else if("setWildZhanLing".equals(act)){
			String wildCityIdStr = request.getParameter("wildCityId");
			if(wildCityIdStr != null){
				int wildCityId = Integer.parseInt(wildCityIdStr);
				if(BidMgr.inst.jczmap.get(wildCityId) != null){
					redirect("cityWar.jsp");
				}
				AllianceBean  allianceBean = AllianceMgr.inst.getAllianceByJunZid(junzhu.id);
				if(allianceBean == null){
					redirect("cityWar.jsp");
				}
				WildCityBean wildCityBean = HibernateUtil.find(WildCityBean.class, "where lmId="+ allianceBean.id + " and cityId=" + wildCityId);
				if(wildCityBean == null){
					wildCityBean = new WildCityBean();
					wildCityBean.cityId = wildCityId;
					wildCityBean.lmId = allianceBean.id;
					wildCityBean.winTime = new Date();
					wildCityBean.isWin = 1;
					HibernateUtil.insert(wildCityBean);
				}else{
					if(wildCityBean.isWin != 1){
						wildCityBean.isWin = 1;
						HibernateUtil.update(wildCityBean);
					}
				}
			}
		}else if("setCityLmId".equals(act)){
			if(request.getParameter("cityId") == "" || request.getParameter("lmId") == ""){
				return;
			}else{
				boolean isEx = false;
				int cityId = Integer.parseInt(request.getParameter("cityId"));
				int lmId = Integer.parseInt(request.getParameter("lmId"));
				for(JCZCity jczCity:citySettings){
					if(jczCity.id == cityId){
						isEx = true;
						break;
					}
				}
				if(!isEx || "".equals(AllianceMgr.inst.getAllianceName(lmId))){
					alert("联盟ID或城池id不存在请检查");
				}else{
					CityBean cityBean = HibernateUtil.find(CityBean.class,cityId);
					if(cityBean == null){
						cityBean = new CityBean();
						cityBean.cityId = cityId;
						cityBean.lmId = 0; //没人占领
						cityBean.atckLmId = lmId;
						HibernateUtil.insert(cityBean);
					}else{
						if(cityBean.atckLmId != lmId){
							cityBean.lmId = lmId;
							HibernateUtil.update(cityBean);
						}
					}
				}
			}
			redirect("cityWar.jsp");
		}
		
		//显示城池列表
		Map<Integer,CityBean> citiesStateMap = new HashMap<Integer,CityBean>();
		tableStart();
		trS();
		ths("城池ID");
		ths("城池名字");
		ths("联盟等级限制");
		ths("状态");
		ths("占领联盟Id(npcId)");
		ths("联盟名字");
		ths("占领时间");
		ths("进攻联盟Id");
		ths("竞拍");
		ths("战斗详情");
		List<CityBean> cityBeans = HibernateUtil.list(CityBean.class,"");
		//转换一下格式方便处理
		if(cityBeans != null){
			for(CityBean cityBean:cityBeans){ 
				citiesStateMap.put(cityBean.cityId,cityBean);
			}
		}
		if(citySettings != null){
			for(JCZCity jczCity : citySettings){
				if(jczCity.type == 2) //野城
					continue;
				trS();
					td(jczCity.id);
					td("<a href='../gm/allianceFight.jsp?scId="+jczCity.id+"'>"+HeroService.getNameById(""+jczCity.name)+"</a>");
					td(jczCity.allianceLv);
					CityBean cityb = citiesStateMap.get(jczCity.id);
					if(cityb != null && cityb.lmId > 0){ //被占领
						td("联盟占领");
						td(cityb.lmId);
						td(AllianceMgr.inst.getAllianceName(cityb.lmId));
						Date date = ((CityBean)citiesStateMap.get(jczCity.id)).occupyTime;
						td(DateUtils.formatDateTime(date,"yyyy/MM/dd HH:MM:ss"));
					}else{
						td("NPC");
						td(jczCity.npcId);
						td("-");
						td("-");
					}
					if(cityb != null){
						if(cityb.atckLmId > 0){
							td(cityb.atckLmId);
						}else if(cityb.atckLmId==-100){
							td("今日战斗已结束");
						}else{
							td("无");
						}
					}else{
						td("无人宣战");
					}
					tdS();
					atag("竞拍详情","cityBidInfo.jsp?cityId="+jczCity.id);
					tdE();
					td("<a href='../gm/allianceFight.jsp?scId="+jczCity.id+"'>详情</a>");
				trE();
			}
		}
		tableEnd();
		
%>

<br><br>
	<strong>设置城池进攻联盟</strong>
	<form action="" method="post">
		<input type="hidden" name="action" value="setBidLmId"/>
		城池ID<input type="text" name="cityId" value="<%=request.getParameter("cityId") == null ? "":request.getParameter("cityId")%>"/>
		进攻联盟ID<input type="text" name="lmId" value="<%=request.getParameter("lmId") == null ? "" : request.getParameter("lmId") %>"/>
		<button >设置</button>
	</form><br><br>
	<strong>设置城池占领</strong>
	<form action="" method="post">
		<input type="hidden" name="action" value="setCityLmId"/>
		城池ID<input type="text" name="cityId" value="<%=request.getParameter("cityId") == null ? "":request.getParameter("cityId")%>"/>
		占领联盟ID<input type="text" name="lmId" value="<%=request.getParameter("lmId") == null ? "" : request.getParameter("lmId") %>"/>
		<button >设置</button>
	</form>

<br>
	<strong>野城竞拍信息：</strong>
	<br>
	<br>
	<%
		if(junzhu != null){
			//野城信息
			long junzhuId = junzhu.id;
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE,-10); //前天
			String dt = DateUtils.datetime2Text(calendar.getTime());
			AllianceBean  allianceBean = AllianceMgr.inst.getAllianceByJunZid(junzhu.id);
			if(allianceBean != null){
				List<WildCityBean> wildCityBeans = HibernateUtil.list(WildCityBean.class,"where lmId="+ allianceBean.id);
				//转换一下格式方便处理
				Map<Integer,WildCityBean>wildCityMap = new HashMap<Integer,WildCityBean>();
				if(cityBeans != null){
					for(WildCityBean wildCityBean:wildCityBeans){ 
						wildCityMap.put(wildCityBean.cityId,wildCityBean);
					}
				}
				tableStart();
				trS();
				ths("城池ID");
				ths("城池名字");
				ths("联盟等级限制");
				ths("是否战胜");
				ths("宣战状态");
				ths("宣战时间");
				ths("设置已经战胜");
				trE();
				for(JCZCity jczCity : citySettings){
					if(jczCity.type == 1) //普通
						continue;
					trS();
						td(jczCity.id);
						td(HeroService.getNameById(""+jczCity.name));
						td(jczCity.allianceLv);
						if(wildCityMap.containsKey(jczCity.id) && wildCityMap.get(jczCity.id).winTime != null){
							td("已战胜");
						}else{
							td("未战胜");
						}
						BidBean bean = HibernateUtil.find(BidBean.class,"where cityId=" + jczCity.id + " and lmId=" + allianceBean.id);
						if(bean != null){
							td("已经宣战");
							td(DateUtils.date2Text(bean.bidTime, "yyyy-MM-dd HH:MM:ss"));
						}else{
							td("-");
							td("-");
						}
						td("<a href='?action=setWildZhanLing&wildCityId="+jczCity.id+"'>设置战胜</a>");
					trE();
				}
				tableEnd();
			}
		}	
	%>
	<br>
	<strong>奖励信息：</strong><input type="button" value="隐藏/显示" onclick='hideAwardInfo()'/>
	<br>
	<br>
	<%
		if(junzhu != null){
			//奖励信息
			long junzhuId = junzhu.id;
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE,-10); //前天
			String dt = DateUtils.datetime2Text(calendar.getTime());
			List<LMZAwardBean> list = HibernateUtil.list(LMZAwardBean.class,
					"where jzId=" + junzhuId + " and dt>'" +dt+ "' order by dt desc,fromType asc");
			out.print("<div id='awardInfo'>");
			tableStart();
			trS();
				ths("奖励ID");
				ths("君主ID");
				ths("城池ID");
				ths("战斗类型");
				ths("战斗结果");
				ths("奖励类型");
				ths("奖励数量");
				ths("奖励时间");
				ths("奖励状态");
			trE();
			for(LMZAwardBean lmzAwardBeans:list){
				trS();
					td(lmzAwardBeans.dbId);
					td(lmzAwardBeans.jzId);
					td(lmzAwardBeans.cityId);
					if(lmzAwardBeans.warType == 0){
						td("镇守");
					}else if(lmzAwardBeans.warType == 1){
						td("进攻");
					}else{
						td("未发生战斗");
					}
					if(lmzAwardBeans.fromType == 1){ //联盟奖励显示失败或成功，个人奖励代表杀敌数
						if(lmzAwardBeans.result == 0){
							td("失败");
						}else{
							td("成功");
						}
						td("联盟");
					}else{
						td("杀敌"+ lmzAwardBeans.result);
						td("个人");
					}
					td(lmzAwardBeans.rewardNum);
					td(DateUtils.date2Text(lmzAwardBeans.dt, "yyyy-MM-dd HH:MM:ss"));
					if(lmzAwardBeans.getState == 0){
						td("未领取");
					}else{
						td("已领取");
					}
				trE();
			}
			tableEnd();
			out.print("</div>");
		}	
	%>
	
	<br>
	<br>
	<strong>当前郡城战时间段:</strong><br><br>
	<%
		tableStart();
			trS();
				ths("宣战开始时间");
				ths("揭晓开始时间");
				ths("战斗开始时间");
				ths("战斗结束时间");
			trE();
			trS();
				td(BidMgr.city_war_declaration_startTime);
				td(BidMgr.city_war_preparation_startTime);
				td(BidMgr.city_war_fighting_startTime);
				td(BidMgr.city_war_fighting_endTime);
			trE();
		tableEnd();
	%>
	<br>
	<form action="" method="post">
		<input type="hidden" name="action" value="setBidTime"/>
		<strong>设置城池战时间段(<font color="red">格式不要输入错误，到了宣战时间点要手动点“结算”</font>) </strong><br><font color="red">*时间依次递减</font><br>
		宣战时间开始<input type="text" name="time1" value="<%=BidMgr.city_war_declaration_startTime%>"/><br>
		揭晓时间开始(宣战结束)<input type="text" name="time2" value="<%=BidMgr.city_war_preparation_startTime%>"/><br>
		战斗时间开始<input type="text" name="time3" value="<%=BidMgr.city_war_fighting_startTime%>"/><br>
		战斗结束<input type="text" name="time4" value="<%=BidMgr.city_war_fighting_endTime%>"/><br>
		野城NPC最大数量<input type="text" name="limitNPCCnt" value="<%=FightNPCScene.limitNPCCnt%>"/><br>
		<button >设置</button>
	</form><br>
	<form action="cityWar.jsp" method="post">
		竞拍结算    <input type="hidden" name="action" value="bidJieSuan"/><button >结算</button>
	</form><br>
	<a href="?action=clearBidData">清除竞拍&结算记录</a> <font color="red">	*测试之前一定要清数据！！！</font><br><br>
	<a href="?action=clearScs">清空已有战斗场景</a>
		<br><br>
		当前服务器时间：<%= DateUtils.datetime2Text(new Date()) %>
</body>
</html>