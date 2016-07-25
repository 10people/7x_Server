<%@page import="qxmobile.protobuf.FuWen.FuwenDuiHuanResp"%>
<%@page import="qxmobile.protobuf.FuWen.FuwenDuiHuan"%>
<%@page import="com.manu.dynasty.template.FuwenDuihuan"%>
<%@page import="qxmobile.protobuf.FuWen.FuwenRongHeResp"%>
<%@page import="qxmobile.protobuf.FuWen.FuwenRongHeReq"%>
<%@page import="qxmobile.protobuf.FuWen.FuwenUnloadAllResp"%>
<%@page import="qxmobile.protobuf.FuWen.FuwenUnloadAll"%>
<%@page import="qxmobile.protobuf.FuWen.FuwenEquipAllResp"%>
<%@page import="qxmobile.protobuf.FuWen.FuwenEquipAll"%>
<%@page import="qxmobile.protobuf.FuWen.FuwenInBag"%>
<%@page import="qxmobile.protobuf.FuWen.FuwenInBagResp"%>
<%@page import="com.manu.dynasty.template.Fuwen"%>
<%@page import="qxmobile.protobuf.FuWen.QueryFuwen"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="java.util.Map"%>
<%@page import="com.manu.dynasty.template.FuwenTab"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="qxmobile.protobuf.FuWen.FuwenResp"%>
<%@page import="qxmobile.protobuf.FuWen.OperateFuwenReq"%>
<%@page import="com.manu.dynasty.template.FuwenJiacheng"%>
<%@page import="com.manu.dynasty.template.FuwenOpen"%>
<%@page import="com.qx.fuwen.FuwenMgr"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="qxmobile.protobuf.FuWen.JunzhuAttr"%>
<%@page import="qxmobile.protobuf.FuWen.FuwenLanwei"%>
<%@page import="java.util.List"%>
<%@page import="qxmobile.protobuf.FuWen.QueryFuwenResp"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="org.apache.mina.core.session.IoSession"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>符文</title>
<script type="text/javascript">
	function loadFuwen(itemId){
		var lanweiId = document.getElementById("lanweiId").value;
		location.href="fuwen.jsp?type=5&itemId="+itemId+"&lanweiId="+lanweiId+"";
	}
	function xiangQianFuwen(type){
		var bagId = document.getElementById("bagId").value;
		var cbLanweiId = document.getElementById("cbLanweiId").value;
		var tab = document.getElementById("tab").value;
		location.href="fuwen.jsp?type="+type+"&bagId="+bagId+"&lanweiId="+cbLanweiId+"&tab="+tab;
	}
	function equipAll(type){
		var tab = document.getElementById("equipAllTab").value;
		location.href="fuwen.jsp?type="+type+"&equipAllTab="+tab;
	}
	function unloadAll(type){
		var tab = document.getElementById("unloadAllTab").value;
		location.href="fuwen.jsp?type="+type+"&unloadAllTab="+tab;
	}
	function duihuan(type){
		var duihuanFuwenId = document.getElementById("duihuanFuwenId").value;
		location.href="fuwen.jsp?type="+type+"&duihuanFuwenId="+duihuanFuwenId;
	}
	function ronghe(type){
		var tab = document.getElementById("rongheTab").value;
		var lanweiId = document.getElementById("rongheLanweiId").value;
		var bagId = document.getElementById("rongheBagId").value;
		var count = document.getElementById("rongheBagCount").value;
		location.href="fuwen.jsp?type="+type+"&rongheTab="+tab + "&rongheLanweiId="
				+lanweiId+"&rongheBagId="+bagId + "&rongheBagCount="+count;
	}
	function showCache(){
		var lanweicache = document.getElementById("lanweicache");
		var lanwei = document.getElementById("lanwei");
		var lockcache = document.getElementById("lockcache");
		if(lanweicache.style.display=='none'){
			lanweicache.style.display='block';
			lanwei.style.display='none';
		} else{
			lanweicache.style.display='none';
			lanwei.style.display='block';
		}
		if(lockcache.style.display=='none'){
			lockcache.style.display='block';
		} else{
			lockcache.style.display='none';
		}
	}
</script>
</head>
<body>
<form action="">
	账号：<input type="text" name="actName" value="${session.name }"/>
	<button type="submit">查询</button>
</form>
<p>
	符文页签(1or2or3):<input type="text" id="tab" placeHolder="符文页签"/>
	符文栏位Id:<input type="text" id="cbLanweiId" placeHolder="符文镶嵌孔的id"/>
	背包dbId:<input type="text" id="bagId" placeHolder="背包的id"/> 
	<button onclick="xiangQianFuwen('xiangqian')">镶嵌</button>
</p>
<p>
	页签tab:<input type="text" id="equipAllTab" placeHolder="符文页签id"/> 
	<button onclick="equipAll('equipAll')">一键镶嵌</button>
</p>
<p>
	页签tab:<input type="text" id="unloadAllTab" placeHolder="符文页签id"/> 
	<button onclick="unloadAll('unloadAll')">一键拆卸</button>
</p>
<p>
	页签tab:<input type="text" id="rongheTab" placeHolder="符文页签id"/> 
	栏位Id:<input type="text" id="rongheLanweiId" /> 
	背包Id:<input type="text" id="rongheBagId" /> 
	数量:<input type="text" id="rongheBagCount" /> 
	<button onclick="ronghe('ronghe')">融合</button>
</p>
<p>
	要兑换的符文id:<input type="text" id="duihuanFuwenId"/> 
	<button onclick="duihuan('duihuan')">兑换符文</button>
</p>

	<%
	
	String actName = request.getParameter("actName");
	actName = actName == null?"":actName;
	if(session.getAttribute("name") != null && actName.length()==0){
		actName = (String)session.getAttribute("name");
	}
	if(actName.length()>0){
		Account account = HibernateUtil.getAccount(actName);
		if(account!=null){
			session.setAttribute("name",actName);
			long junzhuId = account.accountId* 1000 + GameServer.serverId;
			JunZhu junzhu = HibernateUtil.find(JunZhu.class, junzhuId);
			if(junzhu == null) {
				out.println("找不到君主，junzhuId:" + junzhuId);
				return;
			}
			out.println("君主等级:" + junzhu.level +"<br/><br/>");
			

			final IoSession minaSession = new RobotSession(){
				public WriteFuture write(Object message){
					setAttachment(message);
					synchronized(this){
						this.notify();
					}
					return null;
				}
			};
			minaSession.setAttribute(SessionAttKey.junZhuId, junzhuId);
			
			String type = request.getParameter("type");
			if(type != null) {
				if(type.equals("xiangqian")) {
					String bagId = request.getParameter("bagId");
					String lanweiId = request.getParameter("lanweiId");
					String tab = request.getParameter("tab");
					if(bagId == null || lanweiId == null || tab == null ||
							bagId.equals("")|| lanweiId.equals("") || tab.equals("")) {
						out.println("error:穿戴符文缺少参数，回去把参数补全");
						return;
					} else {
						OperateFuwenReq.Builder req = OperateFuwenReq.newBuilder();
						req.setAction(5);
						req.setTab(Integer.parseInt(tab));
						req.setLanweiId(Integer.parseInt(lanweiId));
						req.setBagId(Integer.parseInt(bagId));
						FuwenMgr.inst.operateFuwen(PD.C_OPERATE_FUWEN_REQ, minaSession, req);
						FuwenResp fuwenResp = (FuwenResp)minaSession.getAttachment();
						if(fuwenResp == null){
							
								out.println("error: null");
								return;
						}
						int result = fuwenResp.getResult();
						if(result != 0) {
							out.println("符文穿戴失败，原因:" + fuwenResp.getReason());
						} else {
							out.println("符文穿戴成功");
						}
						
					}
				} else if(type.equals("equipAll")) {
					String tab = request.getParameter("equipAllTab");
					if(tab == null || tab.equals("")) {
						out.println("一键镶嵌tab页签不能为空");
						return;
					}
					FuwenEquipAll.Builder req = FuwenEquipAll.newBuilder();
					req.setTab(Integer.parseInt(tab));
					FuwenMgr.inst.equipFuwenAll(PD.C_FUWEN_EQUIP_ALL, minaSession, req);
					FuwenEquipAllResp resp = (FuwenEquipAllResp)minaSession.getAttachment();
					out.println("0-成功，1-失败，没有可穿戴的符文， 2-页签未解锁   --------->");
					out.println("一键镶嵌结果：" + resp.getResult());
				}  else if(type.equals("ronghe")) {
					String tab = request.getParameter("rongheTab");
					String lanweiId = request.getParameter("rongheLanweiId");
					String bagId = request.getParameter("rongheBagId");
					String count = request.getParameter("rongheBagCount");
					if(tab == null || tab.equals("")) {
						out.println("一键镶嵌tab页签不能为空");
						return;
					}
					if(lanweiId == null || lanweiId.equals("")) {
						out.println("一键镶嵌栏位Id不能为空");
						return;
					}
					if(bagId == null || bagId.equals("")) {
						out.println("一键镶嵌背包Id不能为空");
						return;
					}
					if(count == null || count.equals("")) {
						out.println("一键镶嵌数量不能为空");
						return;
					}
					FuwenRongHeReq.Builder req = FuwenRongHeReq.newBuilder();
					req.setTab(Integer.parseInt(tab));
					req.setLanweiId(Integer.parseInt(lanweiId));
					FuwenInBag.Builder fuwenInBag = FuwenInBag.newBuilder();
					fuwenInBag.setBagId(Integer.parseInt(bagId));
					fuwenInBag.setCnt(Integer.parseInt(count));
					fuwenInBag.setItemId(0);
					fuwenInBag.setExp(0);
					req.addBagList(fuwenInBag);
					FuwenMgr.inst.rongHeFuwen(PD.C_FUWEN_RONG_HE, minaSession, req);
					FuwenRongHeResp resp = (FuwenRongHeResp)minaSession.getAttachment();
					out.println("0-成功，1-找不到主动融合的符文，2-找不到被动融合的符文，3-数量不足   --------->");
					out.println("融合结果:" + resp.getResult());
				} else if(type.equals("duihuan")) {
					String duihuanFuwenId = request.getParameter("duihuanFuwenId");
					if(duihuanFuwenId == null || duihuanFuwenId.equals("")) {
						out.println("兑换符文的符文id不能为空");
						return;
					}
					FuwenDuiHuan.Builder req = FuwenDuiHuan.newBuilder();
					req.setFuwenItemId(Integer.parseInt(duihuanFuwenId));
					FuwenMgr.inst.duiHuanFuwen(PD.C_FUWEN_DUI_HUAN, minaSession, req);
					FuwenDuiHuanResp resp = (FuwenDuiHuanResp)minaSession.getAttachment();
					out.println("0-成功，1-甲片数量不足,2-要兑换的符文不存在  --------->");
					out.println("符文兑换结果：" + resp.getResult());
				}  
			}
			
			for(Map.Entry<Integer, FuwenTab> entry : FuwenMgr.inst.fuwenTabMap.entrySet()) {
				int tab = entry.getKey();
				if(junzhu.level < entry.getValue().level) {
					out.println("符文页签:" + tab + "还未解锁");
					continue;
				}
				QueryFuwen.Builder req = QueryFuwen.newBuilder();
				req.setTab(tab);
				FuwenMgr.inst.queryFuwen(PD.C_QIANDAO_REQ, minaSession, req);
				QueryFuwenResp resp = (QueryFuwenResp)minaSession.getAttachment();
				if(resp.getResult() != 0) {
					out.println("查询符文出错，返回状态：" + resp.getResult());
					continue;
				}
				tableStart();  trS();
				out.append("<td>");
				tableStart();
					trS();td("符文页签:" + tab);trE();
					trS();td("栏位id");td("itemId");td("符文名字");td("经验值");td("是否有红点");td("加成属性");td("属性加成值");trE();
					
					List<JunzhuAttr> attrList = resp.getAttrList();
					JunzhuAttr attrInf = null;
					if(attrList != null) {
						for(JunzhuAttr attr : attrList) {
							if(attr.getType() == 2) {
								attrInf = attr;
								break;
							}
						}
						
					}
					if(attrInf == null) {
						out.println("查询符文信息错误，符文属性加成为null");
						return;
					}
					
					List<FuwenLanwei> lanweiList = resp.getLanweiList();					
					for(FuwenLanwei fuwenLanwei : lanweiList) {
						trS();
							Fuwen fuwenCfg = FuwenMgr.inst.fuwenMap.get(fuwenLanwei.getItemId()); 
							td(fuwenLanwei.getLanweiId());td(fuwenLanwei.getItemId());
							td(HeroService.getNameById(fuwenCfg == null ? "无":fuwenCfg.name+""));
							td(fuwenLanwei.getExp());td(fuwenLanwei.getFlag());
							if(fuwenCfg == null) {
								td("无");td("无");
							} else {
								td(HeroService.getNameById(fuwenCfg.shuxingName+""));td(fuwenCfg.shuxingValue);
							}
						trE();
					}
				tableEnd();out.append("<td/>");
				
				out.append("<td>");
				tableStart();
					trS();td("当前符文页签属性加成总值:");trE();
					trS();td("加成属性");td("属性加成值");trE();
					trS();td("武器伤害加深");td(attrInf.getWqSH());trE();
					trS();td("武器伤害减免");td(attrInf.getWqJM());trE();
					trS();td("武器暴击加深");td(attrInf.getWqBJ());trE();
					trS();td("武器暴击减免");td(attrInf.getWqRX());trE();
					trS();td("技能伤害加深");td(attrInf.getJnSH());trE();
					trS();td("技能伤害减免");td(attrInf.getJnJM());trE();
					trS();td("技能暴击加深");td(attrInf.getJnBJ());trE();
					trS();td("技能暴击减免");td(attrInf.getJnRX());trE();
				tableEnd();out.append("<td/>");
				trE(); tableEnd();
			}
			
			FuwenMgr.inst.loadFuwenInBag(PD.C_LOAD_FUWEN_IN_BAG, minaSession, null);
			FuwenInBagResp fuwenInBagResp = (FuwenInBagResp)minaSession.getAttachment();
			if(fuwenInBagResp == null) {
				out.println("背包里没有符文");
			} else {
				List<FuwenInBag> fuwenInBagList = fuwenInBagResp.getFuwenListList();
				tableStart();
				trS();td("背包dbId");td("itemId");td("数量");td("经验");trE();
					for(FuwenInBag fuwenInBag : fuwenInBagList) {
						trS();
						td(fuwenInBag.getBagId());td(fuwenInBag.getItemId());
						td(fuwenInBag.getCnt());td(fuwenInBag.getExp());
						trE();
					}
				tableEnd();
			}
		}else{
			
		}
	}
	%>
</body>
</html>