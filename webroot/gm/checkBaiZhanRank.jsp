<%@page import="com.qx.account.AccountManager"%>
<%@page import="qxmobile.protobuf.ZhangHao.LoginReq"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="org.apache.commons.httpclient.NameValuePair"%>
<%@page import="org.apache.commons.httpclient.methods.PostMethod"%>
<%@page import="org.apache.commons.httpclient.HttpMethod"%>
<%@page import="org.apache.commons.httpclient.HttpClient"%>
<%@page import="org.apache.commons.httpclient.HttpException"%>
<%@page import="com.qx.award.AwardMgr"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.manu.dynasty.template.MiBao"%>
<%@page import="com.qx.mibao.MibaoMgr"%>
<%@page import="com.manu.dynasty.template.AwardTemp"%>
<%@page import="com.manu.dynasty.template.CanShu"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.vip.VipRechargeRecord"%>
<%@page import="qxmobile.protobuf.VIP.RechargeReq"%> 
<%@page import="com.qx.vip.VipMgr"%>
<%@page import="qxmobile.protobuf.VIP"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="com.qx.ranking.RankingMgr"%>
<%@page import="com.qx.explore.ExploreMgr"%>
<%@page import="qxmobile.protobuf.PvpProto.BaiZhanResult"%>
<%@page import="com.qx.pvp.PvpMgr"%>
<%@page import="com.qx.http.CreateJunZhuSer"%>
<%@page import="qxmobile.protobuf.AllianceProtos.CreateAlliance"%>
<%@page import="com.qx.http.EndServ"%>
<%@page import="com.qx.http.StartServ"%>
<%@page import="com.qx.http.MyClient"%>
<%@page import="qxmobile.protobuf.ZhangHao.CreateRoleRequest"%>
<%@page import="com.manu.dynasty.template.BaiZhanNpc"%>
<%@page import="com.qx.pvp.PVPConstant"%>
<%@page import="com.manu.dynasty.base.service.CommonService"%>
<%@page import="com.qx.pvp.PvpBean"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="com.qx.account.Account"%>
<%@page import="qxmobile.protobuf.ZhanDou.PvpZhanDouInitReq"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@include file="/myFuns.jsp"%>
<%@ page language="java" import="java.util.*" pageEncoding="utf8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
  <base href="<%=basePath%>">
  <script type="text/javascript">
</script>
    <title>检查百战数据是否正确合理</title>
  </head>
  
  <body>


<%
int serId = GameServer.serverId;
int length = PvpMgr.inst.getMaxScore();
for(int i = 0; i<=length;i++){
	Set<String> elem = Redis.getInstance().zrangebyscore_("baiZhan_"+serId, i, i);
	if(elem == null){
		br();
	   out("分数是 :" + (i+1) +"的元素不存在");
	 }else{
		br();
		if(elem.size() != 1){
			out("警告！！分数是" + (i+1) + "的元素个数有: " +  elem.size() +"个;");
		}
		for (String str : elem) {
		     if(str!=null){
		    	String[] sss = str.split("_");
		    out("分数是" + (i+1) + "的元素包含:" + sss[0] + " + " + sss[1] +",");
		     }
		}
}
}

%>
  </body>
</html>
