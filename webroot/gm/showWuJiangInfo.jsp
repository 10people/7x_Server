<%@page import="com.manu.dynasty.store.MemcachedCRUD"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="org.apache.mina.core.session.IoSession"%>
<%@page import="org.apache.mina.core.session.DummySession"%>
<%@page import="com.qx.hero.WuJiangKeJiMgr"%>
<%@page import="com.qx.hero.WjKeJi"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.qx.hero.HeroMgr"%>
<%@page import="com.manu.dynasty.template.HeroProtoType"%>
<%@page import="com.qx.hero.WuJiang"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.manu.dynasty.boot.GameServer" %>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<script type="text/javascript">
function go(act){
	var v = document.getElementById(act).value;
	location.href = '?action='+act+"&v="+v;
}
</script>
	<title>武将信息</title>
  </head>
  
  <body>
  <%!  
  String wuJiangCell(int a) {  
	  return HeroService.getNameById(WuJiangKeJiMgr.inst.getKeJi(a).name);
  }
  int wuJiangCellLevel(int a) {  
	  return WuJiangKeJiMgr.inst.id2Keji.get(a).level;
  }
%>
  <% 
  	String name = request.getParameter("account"); 
  	name = name == null ? "" : name;
  	if(session.getAttribute("name") != null && name.length()==0){
		name = (String)session.getAttribute("name");
	}
   %>
  	<form action="">
		账号<input type="text" name="account" value = <%= name%>>
		<button type="submit">查询</button>
	</form>
   <%
    Account account = null;
    if(name != null && name.length() > 0){
    	account = HibernateUtil.getAccount(name);
    }
    if(account == null){
    	%>账号输出有误，请确认后再输入<%
    }
    else{
    	session.setAttribute("name",name);
    	long junZhuId = account.accountId * 1000 + GameServer.serverId;
    	IoSession fs = new DummySession();
    	fs.setAttribute(SessionAttKey.junZhuId, junZhuId);
    	List<WuJiang> list = HeroMgr.inst.getWuJiangList(fs);
		String bagCntKey = "WuJiangCnt#" + junZhuId;
		Object mcO = MemcachedCRUD.getMemCachedClient().get(bagCntKey);
    	out.println("缓存武将个数:"+mcO+"<br/>");
    	out.println("武将个数:"+list.size()+"<br/>");
    	if(list.size() != 0){
    	%>
    		<table border = '1'>
    		 <tr>
    		   <th>dbId</th>
    		   <th>武将姓名</th>
    		   <th>HeroGrowId</th>
    		   <th>heroId</th>
    		   <th>星级</th>
    		   <th>激活</th>
    		   <th>数量</th>
    		   <th>攻击</th>
    		   <th>战力</th>
    		 </tr>
    	<%
    		for(WuJiang wuJiang : list){
    				//HeroMgr.getInstance().calcZhanLi(wuJiang);
    			if(wuJiang.zhanLi==0){
    			}
    			HeroProtoType hero = HeroMgr.inst.getHeroTypeById(wuJiang.getHeroId());
    			if(hero != null){%>
    			<tr>
    				<td><%= wuJiang.dbId %></td>
    			  <td><%= HeroService.getNameById(String.valueOf(hero.heroName)) %></td>
    			  <td><%= wuJiang.getHeroGrowId() %></td>
    			  <td><%= hero.heroId %></td>
    			  <td><%= wuJiang.getHeroGrowId()%10 %></td>
    			  <td><%= wuJiang.isCombine() ? "是" : "否" %></td>
    			  <td><%= wuJiang.getNum() %></td>
    			  <td><%= wuJiang.attack %></td>
    			  <td><%= wuJiang.zhanLi %></td>
    			</tr>
    			<%}
    		}%>
    		</table>
    	<%}%>
    		<br>
    		武将科技
    		<br>
    		<table border = '1'>
    	 	<tr>
    	   	<th>攻击</th>
    	   	<th>防御</th>
    	   	<th>生命</th>
    	   <th>智谋</th>
    	   	<th>武艺</th>
    	   	<th>统帅</th>
    	 	</tr>
    	 	<%
    	 	WjKeJi wjKeJi = HibernateUtil.find(WjKeJi.class, junZhuId);
    	 	if(wjKeJi == null){
    	 		wjKeJi = WuJiangKeJiMgr.inst.createDefaultBean(junZhuId);
    	 	}
    	 	String action = request.getParameter("action");
    		 if("addGoldenJingPo".equals(action)){
    			 int v = Integer.parseInt(request.getParameter("v"));
    			 wjKeJi.goldenJingPo = wjKeJi.goldenJingPo + v;
    			 HibernateUtil.save(wjKeJi);
    		 }
    	 	%>
    	 	<tr>
    	 	  <td><%= wjKeJi.attack%></td>
    	 	  <td><%= wjKeJi.defense%></td>
    	 	  <td><%= wjKeJi.hp%></td>
    	 	  <td><%= wjKeJi.zhiMou%></td>
    	 	  <td><%= wjKeJi.wuYi %></td>
    	 	  <td><%= wjKeJi.tongShuai%></td>
    	 	</tr>
    	 	<tr>
    	 	  <td><%= wuJiangCell(wjKeJi.attack) %></td>
    	 	  <td><%= wuJiangCell(wjKeJi.defense) %></td>
    	 	  <td><%= wuJiangCell(wjKeJi.hp) %></td>
    	 	  <td><%= wuJiangCell(wjKeJi.zhiMou) %></td>
    	 	  <td><%= wuJiangCell(wjKeJi.wuYi) %></td>
    	 	  <td><%= wuJiangCell(wjKeJi.tongShuai) %></td>
    	 	</tr>
    	 	<tr>
    	 	  <td><%= wuJiangCellLevel(wjKeJi.attack) %></td>
    	 	  <td><%= wuJiangCellLevel(wjKeJi.defense) %></td>
    	 	  <td><%= wuJiangCellLevel(wjKeJi.hp) %></td>
    	 	  <td><%= wuJiangCellLevel(wjKeJi.zhiMou) %></td>
    	 	  <td><%= wuJiangCellLevel(wjKeJi.wuYi) %></td>
    	 	  <td><%= wuJiangCellLevel(wjKeJi.tongShuai) %></td>
    	 	</tr>
    		</table>
    		金色精魄个数:<%= wjKeJi.goldenJingPo%>
    		<input type='text' id='addGoldenJingPo' value='10'/><input type='button' value='增加' onclick='go("addGoldenJingPo")'/><br/>
		<%}%>
  </body>
</html>
