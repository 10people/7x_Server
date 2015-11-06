<%@page import="com.manu.dynasty.template.BaiZhanNpc"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.manu.dynasty.store.MemcachedCRUD"%>
<%@page import="com.qx.pvp.PVPConstant"%>
<%@page import="com.manu.dynasty.base.service.CommonService"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.pvp.PvpMgr"%>
<%@page import="com.qx.pvp.PvpDuiHuanBean"%>
<%@page import="com.qx.pvp.PvpBean"%>
<%@page import="com.qx.account.Account"%>
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
   function go(act){
        var v = document.getElementById(act).value;
        location.href = 'gm/deleteTable.jsp?action='+act+"&v="+v;
   }
</script>
    <title> </title>
  </head>
  <body>
<%
        
if (PvpMgr.DB.exist_(PvpMgr.KEY)){
	 PvpMgr.DB.del(PvpMgr.KEY);
	List<BaiZhanNpc> list = TempletService.listAll(
	        BaiZhanNpc.class.getSimpleName());
	for (int index = list.size() - 1; index > -1; index--){
	    BaiZhanNpc npc = list.get(index);
	    int minRank = npc.minRank;
	    int maxRank = npc.maxRank;
	    for (int i = minRank; i <= maxRank; i++){
	        // i指的是排名，而"npc_" + i中的i指的是npc的id，i的值等于PvpBean中的rank值
	         PvpMgr.DB.zadd(PvpMgr.KEY, i-1, "npc_" + i);
	    }
	}
}
int serverid= GameServer.serverId;
List<PvpBean> list = HibernateUtil.list(PvpBean.class, "");
 for(PvpBean bean: list){
	 if(bean.junZhuId % 1000 ==serverid){
		   HibernateUtil.delete(bean);
	 }
 }
 List<PvpDuiHuanBean> list2 = HibernateUtil.list(PvpDuiHuanBean.class, "");
 for(PvpDuiHuanBean bean: list2){
	 if(bean.junZhuId % 1000 ==serverid){
		    HibernateUtil.delete(bean);
	 }
 }
        %>
    
  </body>
</html>
