<%@page import="java.util.Date"%>
<%@page import="com.qx.alliance.LmTuTeng"%>
<%@page import="com.qx.yuanbao.YBType"%>
<%@page import="com.qx.vip.VipMgr"%>
<%@page import="com.qx.vip.VipData"%>
<%@page import="qxmobile.protobuf.MoBaiProto.MoBaiInfo"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.qx.yuanbao.YuanBaoMgr"%>
<%@page import="com.qx.alliance.MoBaiMgr"%>
<%@page import="com.manu.dynasty.store.MemcachedCRUD"%>
<%@page import="com.qx.alliance.AlliancePlayer"%>
<%@page import="java.io.IOException"%>
<%@page import="java.io.Writer"%>
<%@page import="java.util.List"%>
<%@page import="com.manu.dynasty.template.LianmengMobai"%>
<%@page import="com.qx.alliance.MoBaiBean"%>
<%@page import="com.qx.account.FunctionOpenMgr"%>
<%@page import="com.manu.network.SessionUser"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.qx.pve.PveMgr"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.manu.dynasty.template.ExpTemp"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.qx.timeworker.TimeWorkerMgr"%>
<%@page import="qxmobile.protobuf.TimeWorkerProtos.TimeWorkerResponse"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.alliance.AllianceBean"%>
<%@page import=" com.qx.alliance.LMMoBaiInfo"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript">
function go(act){
	var v = "";
	var ele = document.getElementById(act);
	if(ele) v=ele.value;
	location.href = '?action='+act+"&v="+v;
}
</script>
<title>Insert title here</title>
</head>
<body>
<%!
int updateMobaiLevel(int lmId, int buffNum, Date time) {
	{//增加联盟累计膜拜次数
		LmTuTeng tt = HibernateUtil.find(LmTuTeng.class, lmId);
		if(tt == null){
			tt = new LmTuTeng();
			tt.lmId = lmId;
			tt.dTime = time;
			tt.times = buffNum;
			HibernateUtil.insert(tt);
		}else{
			tt.times+=buffNum;
			HibernateUtil.update(tt);
		}
		return tt.times;
	}
}
%>
<%
setOut(out);
	String name = request.getParameter("account");
name = name == null ? "": name.trim();
String accIdStr = request.getParameter("accId");// 用户id
accIdStr = (accIdStr == null ? "":accIdStr.trim());
if(session.getAttribute("name") != null && name.length()==0 && accIdStr.length()==0){
	name = (String)session.getAttribute("name");
}
%>
  <form action="">
	  	账号<input type="text" name="account" value="<%=name%>">&nbsp;或&nbsp;
	  	账号ID<input type="text" name="accId" value="<%=accIdStr%>">
	  	<button type="submit">查询</button>
	  </form>
<%
	Account account = null;
	if(name != null && name.length()>0){
		account = HibernateUtil.getAccount(name);
	}else if(accIdStr.length()>0){
		account = HibernateUtil.find(Account.class, Long.valueOf(accIdStr));
		if(account != null)name = account.getAccountName();
	}
	{
	if(account == null){
%>没有找到<%
	//HibernateUtil.saveAccount(name);
	}else{
		session.setAttribute("name", name);
%>账号<%=account.getAccountId()%>:<%=account.getAccountName()%><%
	long junZhuId = account.getAccountId() * 1000 + GameServer.serverId;
		JunZhu junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
		 AlliancePlayer member = null;
		 member = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
		 if(member == null || member.lianMengId<=0){
			 out("没有加入联盟");
			 return;
		 }
		 int bufferLevel = 0;
		 LmTuTeng tt = HibernateUtil.find(LmTuTeng.class, member.lianMengId);
		 if(tt!=null){
			 bufferLevel = tt.times;
		 }
		AllianceBean alliance = HibernateUtil.find(AllianceBean.class, member.lianMengId);
	 if(junzhu == null){
 		out.println("没有君主");
	 }else{
	 if(junzhu.level == 0 || junzhu.shengMingMax == 0){
		 JunZhuMgr.inst.fixCreateJunZhu(junZhuId, junzhu.name, junzhu.roleId, junzhu.guoJiaId);
			 
	 }
	 String action = request.getParameter("action");
	 boolean sendInfo = true;
	 if("addTongBi".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 junzhu.tongBi += v;
		 HibernateUtil.save(junzhu);
	 }else if("addTiLi".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 JunZhuMgr.inst.updateTiLi(junzhu, v, "jsp后台管理");
		 HibernateUtil.save(junzhu);
	 }else if("addExp".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 JunZhuMgr.inst.addExp(junzhu, v);
	 }else if("addYuanBao".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 //junzhu.yuanBao += v;
		YuanBaoMgr.inst.diff(junzhu,  v, 0,0,YBType.YB_GM_ADDYB,"后台膜拜添加元宝");
		 HibernateUtil.save(junzhu);
	 }else if("setGod".equals(action)){
		 PveMgr.godId = junzhu.id;
	 }else if("addBufferLevel".equals(action)) {
		 int v = Integer.parseInt(request.getParameter("v"));
		 bufferLevel = updateMobaiLevel(member.lianMengId, v, new Date());
	 }else{
		 sendInfo = false;
	 }
	 if(sendInfo){
		 SessionUser u = SessionManager.getInst().findByJunZhuId(junzhu.id);
		 if(u!= null){
		 	JunZhuMgr.inst.sendMainInfo(u.session);
		 }
	 }
	 
	 out.println("&nbsp;君主id："+junzhu.id+" &nbsp; 名字:"+junzhu.name);
	 
	 
	 
	 String input = request.getParameter("v");
	 if(input == null)input = "1";
	 br();
	 tableStart();
	 if(member == null) {
		 trS();td("整个联盟膜拜次数:："+bufferLevel);tdS();input("addBufferLevel",input);out("没有联盟，无法操作");tdE();trE();
	 } else {
		 trS();td("整个联盟膜拜次数:："+bufferLevel);tdS();input("addBufferLevel",input);out("<input type='button' value='增加' onclick='go(\"addBufferLevel\")'/><br/>");tdE();trE();
	 }
	 trS();td("铜币："+junzhu.tongBi);tdS();input("addTongBi",input);out("<input type='button' value='增加' onclick='go(\"addTongBi\")'/><br/>");tdE();trE();//td("<a href='?action=addTongBi'>+100</a><br/>");
	 trS();td("元宝："+junzhu.yuanBao);td("<input type='text' id='addYuanBao' value='"+input+"'/><input type='button' value='增加' onclick='go(\"addYuanBao\")'/><br/>");trE();//td("<a href='?action=addYuanBao'>+100</a><br/>");
	 trS();td("体力："+junzhu.tiLi);td("<input type='text' id='addTiLi' value='"+input+"'/><input type='button' value='增加' onclick='go(\"addTiLi\")'/><br/>");trE();//td("<a href='?action=addTiLi'>+100</a><br/>");
	 tableEnd();
	 //-----------------
	 if(member == null){
		 out("没有联盟");
	 }else{
		 br();
		 out("联盟id:"+member.lianMengId);
		 space();
		 out("");out("");
		 br();
		 br();
	 }
	 {//阶段奖励
		 
	 }
	 MoBaiBean bean = HibernateUtil.find(MoBaiBean.class, junzhu.id);
	 if(bean == null){
		 out("尚无膜拜信息");
	 }else{
		 if("clearTB".equals(action)){
			 bean.tongBiTime = null;
			 HibernateUtil.save(bean);
		 }else if("clearYB".equals(action)){
			 bean.yuanBaoTime = null;
			 HibernateUtil.save(bean);
		 }else if("clearYu".equals(action)){
			 bean.yuTime = null;
			 HibernateUtil.save(bean);
		 }
		 tableStart();
		 ths("类型,时间,操作");
		 trS();
		 td("铜币膜拜时间");td(bean.tongBiTime); td(button("清除","go('clearTB')"));
		 trE();
		 trS();
		 td("元宝膜拜时间");td(bean.yuanBaoTime); td(button("清除", "go('clearYB')"));
		 trE();
		 trS();
		 td("玉膜拜时间");td(bean.yuTime); td("<input type='button' value='清除' onclick='go(\"clearYu\")'/><br/>");
		 trE();
		 trS();
		 td("玉膜拜已用次数");td(bean.yuTimes); td("<input type='button' value='清除' onclick='go(\"clearYuTimes\")'/><br/>");
		 trE();
		 trS();
		 int vipAddTimes = VipMgr.INSTANCE.getValueByVipLevel(junzhu.vipLevel, VipData.yujueDuihuan);
		 td("VIP增加玉膜拜次数");td(vipAddTimes);td("");
		 trE();
		 trS();
		 td("阶段1领取时间:");td(bean.step1time);td("");
		 td("阶段2领取时间:");td(bean.step2time);td("");
		 td("阶段3领取时间:");td(bean.step3time);td("");
		 trE();
		 tableEnd();
		 br();
		 ///////////////
		 IoSession ss = createSession(bean.junZhuId);
		 BigSwitch.inst.moBaiMgr.sendMoBaiInfo(0, ss, null);
		 MoBaiInfo msg = (MoBaiInfo)ss.getAttachment();
		 out("发给客户端的数据是"+msg);
		 
		 br();
	 }
		 }
	}
}
	List<LianmengMobai> confList = TempletService.listAll(LianmengMobai.class.getSimpleName());
	if(confList == null){
		out("配置缺失！！！！！！！");
	}else{
		tableStart();
		ths("type,描述,花费数量,奖励,buff");
		for(LianmengMobai conf : confList){
			out("<tr>");
			td(conf.type);
			td(conf.desc);
			td(conf.needNum);
			td(conf.award);
			td(conf.buffNum);
			out("</tr>");
		}
		tableEnd();
	}
%>
</body>
</html>