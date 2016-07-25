<%@page import="org.omg.IOP.ServiceContext"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.manu.dynasty.template.HeroProtoType"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.qx.hero.WuJiang"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.hero.HeroMgr"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.manu.dynasty.template.HeroGrow"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>add wujiang</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="add wujiang">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->

  </head>
  
  <body>
  <%
  String accountName = request.getParameter("account");
  if(accountName == null){
	  accountName = "";
  }
  if(session.getAttribute("name") != null && accountName.length()==0){
	  accountName = (String)session.getAttribute("name");
	}
  %>
  	<form name="form1" method="post">
    	添加武将<br>
    	   <p>
   			你的账号： <br> 
   			<input type="text" name="account" value="<%=accountName%>">
  		   </p>
    	   
    	   <p>
   			想添加武将的heroGrow的ID： --- <a href="dataConf/dataTemplate.jsp?type=hero">查看列表</a> <br>
   			<input type="text" name="heroGrowId"> 
   			数量：
   			<input type="text" name="cnt" value="1"> 
   			<br>
    
   			<input type="submit" value="提交"> 
   			<input type="reset" value="重置"> 
   			<br>
   		   </p>
   		   
   	  <%
   		      	  	String idStr = request.getParameter("heroGrowId");
   		      	     		      	     	  	int id = -1;
   		      	     		      	     	  	if(idStr != null){
   		      	     		      	     	  		try{
   		      	     		      	     	  			id = Integer.parseInt(idStr.trim());
   		      	     		      	     	  		}catch(Throwable e){
   		      	     		      	     	  			e.printStackTrace();
   		      	     		      	     	  			return;
   		      	     		      	     	  		}
   		      	     		      	     	  	}
   		      	     		      	     	  	HeroMgr heroMgr = new HeroMgr();
   		      	     		      	     	  	HeroGrow heroGrow = heroMgr.getHeroGrowById(id);
   		      	     		      	     	  	
   		      	     		      	     	  	
   		      	     		      	     	  	if(accountName == null){
   		      	     		      	     	  		out.append("请输入您的帐号<br>");
   		      	     		      	     	  		return;
   		      	     		      	     	  	}
   		      	     		      	     	  	Account account = HibernateUtil.getAccount(accountName.trim());
   		      	     		      	     	  	if(account == null){
   		      	     		      	     	  		out.append("账号不存在:"+accountName);
   		      	     		      	  	  		return;
   		      	     		      	     	  	}
   		      	     		      	     	  	session.setAttribute("name", accountName);
   		      	     		      	     	  	if(heroGrow == null){
   		      	     		      	     	  		out.append("没有对应的heroGrow,请确认id<br>");
   		      	     		      	     	  	}else{
   		      	     		      	     	  	
   		      	     		      	     	  	long junZhuId = account.accountId * 1000 + GameServer.serverId ;
   		      	     		      	     	  	int heroId = heroGrow.heroId;
   		      	     		      	     	  	//不管输入哪个星级的heroGrow，只给它0级的那个。
   		      	     		      	     	List<HeroGrow> list = HeroMgr.heroId2HeroGrow.get(heroId);
   		      	     		      	     	heroGrow = list.get(0);
   		      	     		      	     	  	WuJiang wuJiang = heroMgr.getWuJiangByHeroId(junZhuId, heroId);
   		      	     		      	     	  	//WuJiang wuJiang = heroMgr.getWuJiangByHeroGrow(heroGrow, accId);
   		      	     		      	     	  	if(wuJiang!=null){
   		      	     		      	     	  		wuJiang.setNum(wuJiang.getNum()+Integer.parseInt(request.getParameter("cnt")));
   		      	     		      	     	  		HibernateUtil.save(wuJiang);
   		      	     		      	     	  		//heroMgr.addWuJiangNum(wuJiang);
   		      	     		      	  	   	  	HeroProtoType heroProto = HeroMgr.inst.getHeroTypeById(wuJiang.getHeroId());
   		      	     		      	  	   	  	out.append("武将"+HeroService.getNameById(heroProto.getName())+"<br>");
   		      	     		      	     	  		out.append("武将添加成功-增加个数<br>");
   		      	     		      	     	  	}else{
   		      	     		      	     	  		wuJiang = heroMgr.createWuJiangBean(heroGrow, junZhuId );
   		      	     		      	     	  		wuJiang.setNum(Integer.parseInt(request.getParameter("cnt")));
   		      	     		      	     	  		heroMgr.addNewWuJiang(wuJiang, junZhuId);
   		      	     		      	     	  		out.append("武将添加成功-首次获得<br>");
   		      	     		      	     	  	}
   		      	     		      	     	  	}
   		      	  %>
    </form>
    <%
    List<HeroGrow> list2 = TempletService.listAll(HeroGrow.class.getSimpleName());
	out.append("<table border='1'>");
	out.append("<tr>");
	out.append("<th>grow ID</th><th>名称</th><th>heroId</th>");
	out.append("</tr>");
	int lastHeroId = 0;
	for(HeroGrow t : list2){
		if(t.id<31000)continue;
		if(t.heroId == lastHeroId){
			continue;
		}
		lastHeroId = t.heroId;
		HeroProtoType proto = HeroMgr.id2Hero.get(t.heroId);
		out.append("<tr>");
		out.append("<td>");		out.println(t.id);		out.append("</td>");
		out.append("<td>");		out.println(HeroService.getNameById(proto.getName()));	out.append("</td>");
		out.append("<td>");		out.println(t.heroId);		out.append("</td>");
		out.append("<tr>");
	}
	out.append("</table>");
    %>
  </body>
</html>
