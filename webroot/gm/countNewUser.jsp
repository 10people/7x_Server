<%@page import="com.qx.world.FightScene"%>
<%@page import="com.qx.util.RandomUtil"%>
<%@page import="java.util.Random"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="qxmobile.protobuf.AllianceFightProtos.PlayerScore"%>
<%@page import="java.math.BigInteger"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.hibernate.dialect.MySQL5Dialect"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@ page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.Map"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<%
setOut(out);
%>
  <div class="userStatistics">
  	<form action="">
	  	请输入日期<input type="text" name="dateTime"><span>格式 如:2016-06-28</span>
	  	<button type="submit">查询</button>
	</form>
  </div> 	
<%
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String dateTime = request.getParameter("dateTime");
    if(dateTime == null || dateTime.equals("")){
    	return;
    }
    dateTime = (dateTime == null ? "":dateTime.trim());
    String sql = "select DATE(a.reportDate) as c,count(DISTINCT a.id) as d  from qxaccounts.ClientReg a ,qxaccounts.LoginHistory b where Date(b.loginTime)=Date('"+dateTime+"') and a.id = b.accId GROUP BY c";
    List<Map<String,Object>> list = HibernateUtil.querySql(sql);
    int counter =0;
    if(null == list || list.size() <=0){
    	out.println("没有相关数据");
    	return;
    }
    for(Map<String,Object> a:list){
    	counter = counter+Integer.valueOf(a.get("d").toString());
    }
    tableStart();
    trS();td(dateTime+"日 总登录人数:");td(counter);trE();
    for(Map<String,Object> a:list){
			 double s = Double.valueOf(a.get("d").toString())/counter*100;
			 BigDecimal bd = new BigDecimal(s);  
			 bd = bd.setScale(2,BigDecimal.ROUND_HALF_UP);   
			 trS();td("<font color='blue'>注册日期</font>");td(sdf.format(a.get("c")));td("<font color='blue'>注册人数</font>");td(a.get("d"));td("<font color='blue'>所占百分比</font>");td(bd+"%");trE();
		}
	tableEnd();
	
	  /* Map<Long, PlayerScore.Builder> personalScoreMap1 = new HashMap<>();
	   long s = 1000;
	   List<JunZhu> jzList = HibernateUtil.list(JunZhu.class, "where 1=1");
	   for(int i=0;i<30;i++){
		PlayerScore.Builder p = PlayerScore.newBuilder();
		p.setRank(i);
		p.setRoleName("大侠"+i);
		p.setKillCnt(RandomUtil.getRandomNum(2));
		p.setLianSha(RandomUtil.getRandomNum(2));
		p.setJiFen(RandomUtil.getRandomNum(2));
		p.setJzId(jzList.get(i).getId());
		int k = RandomUtil.getRandomNum(2);
		p.setSide(k==0?1:k);
		personalScoreMap1.put(s+i, p);
	}
	     FightScene.sortScore(personalScoreMap1); */
%>
</body>
</html>