<%@page import="com.qx.ranking.RankingGongJinMgr"%>
<%@page import="com.qx.purchase.TongBi"%>
<%@page import="com.qx.task.DailyTaskMgr"%>
<%@page import="java.lang.reflect.Field"%>
<%@page import="com.qx.task.DailyTaskActivity"%>
<%@page import="com.qx.yabiao.YBBattleBean"%>
<%@page import="com.qx.yabiao.YaBiaoHuoDongMgr"%>
<%@page import="com.manu.dynasty.template.CanShu"%>
<%@page import="com.qx.persistent.MC"%>
<%@page import="com.qx.timeworker.TimeWorker"%>
<%@page import="com.qx.activity.XianShiActivityMgr"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@page import="com.qx.yuanbao.YBType"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.qx.junzhu.PlayerTime"%>
<%@page import="com.qx.world.PosInfo"%>
<%@page import="com.qx.yuanbao.YuanBaoMgr"%>
<%@page import="com.qx.vip.PlayerVipInfo"%>
<%@page import="com.qx.vip.VipMgr"%>
<%@page import="qxmobile.protobuf.JunZhuProto.BuyTimesInfo"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.manu.network.BigSwitch"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="org.apache.mina.core.future.WriteFuture"%>
<%@page import="com.qx.account.FunctionOpenMgr"%>
<%@page import="com.manu.network.SessionUser"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.qx.pve.PveMgr"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.manu.dynasty.template.ExpTemp"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.purchase.XiLian"%>
<%@page import="com.qx.yabiao.YaBiaoBean"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.account.Account"%>
<%@page import="com.qx.timeworker.TimeWorkerMgr"%>
<%@page import="com.manu.dynasty.template.FunctionOpen"%>
<%@page import="com.qx.alliance.AlliancePlayer"%>
<%@page import="qxmobile.protobuf.TimeWorkerProtos.TimeWorkerResponse"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.qx.purchase.PurchaseMgr"%>
<%@page import="com.qx.alliance.AllianceMgr"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript">
function go(act){
	var v = document.getElementById(act).value;
	location.href = '?action='+act+"&v="+v;
}

</script>
<title>Insert title here</title>
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
%>
  	<form action="">
	  	账号<input type="text" name="account" value="<%=name%>">&nbsp;或&nbsp;
	  	君主ID<input type="text" name="accId" value="<%=accIdStr%>">
	  	<button type="submit">查询</button>
	</form>
<%
	Account account = null;
	if(name != null && name.length()>0){
		account = HibernateUtil.getAccount(name);
	}else if(accIdStr.length()>0){
		account = HibernateUtil.find(Account.class, (Long.valueOf(accIdStr) - GameServer.serverId) / 1000);
		if(account != null)name = account.getAccountName();
	}
do{
	long junZhuId = 0;
	if(account != null){
		session.setAttribute("name", name);
		out("账号");out(account.getAccountId());out("：");out(account.getAccountName());
		out("密码：");out(account.getAccountPwd());
		junZhuId = account.getAccountId() * 1000 + GameServer.serverId;
	}else if(accIdStr.matches("\\d+")){
		junZhuId = Long.parseLong(accIdStr);
	}else{
		out("没有找到");
		break;
	}
	JunZhu junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
	if(junzhu == null){
		out.println("没有君主");
		break;
	}
	session.setAttribute("jzId", junZhuId);
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
		 JunZhuMgr.inst.updateTiLi(junzhu, v, "后台修改");
		 HibernateUtil.save(junzhu);
	 }else if("updateLevel".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 junzhu.level = v;
		 HibernateUtil.save(junzhu);
	 }else if("addExp".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 JunZhuMgr.inst.addExp(junzhu, v);
	 }else if("addYuanBao".equals(action)){
		 int v = Integer.parseInt(request.getParameter("v"));
		 //junzhu.yuanBao += v;
		 YuanBaoMgr.inst.diff(junzhu,  v, 0,0,YBType.YB_GM_ADDYB,"后台玩家信息添加元宝");
		 HibernateUtil.save(junzhu);
	 }else if("setGod".equals(action)){
		 PveMgr.godId = junzhu.id;
	 }else if("updateLianmeng".equals(action)){
		 AlliancePlayer alncPlayer = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
		 if(alncPlayer == null) {
	 return;
		 }
		 Redis.getInstance().sremove(AllianceMgr.inst.CACHE_MEMBERS_OF_ALLIANCE + alncPlayer.lianMengId,
		 alncPlayer.junzhuId + "");
		 int nowAllianceId = Integer.parseInt(request.getParameter("v"));
		 alncPlayer.lianMengId = nowAllianceId;
		 HibernateUtil.save(alncPlayer);
		 if(nowAllianceId > 0) {
	 Redis.getInstance().sadd(AllianceMgr.inst.CACHE_MEMBERS_OF_ALLIANCE + alncPlayer.lianMengId,
		 alncPlayer.junzhuId + "");
		 }
	 }else if("updateYaBiaoCount".equals(action)){
		 YaBiaoBean yb = HibernateUtil.find(YaBiaoBean.class, junzhu.id);
		 if(yb == null) {
	 			return;
		 }
		 yb.remainYB = Integer.parseInt(request.getParameter("v"));
		 HibernateUtil.save(yb);
	 }else if("updateJieBiaoCount".equals(action)){
		 YBBattleBean yb =YaBiaoHuoDongMgr.inst.getYBZhanDouInfo(junzhu.id, junzhu.vipLevel);
		 yb.remainJB4Award = Integer.parseInt(request.getParameter("v"));
		 HibernateUtil.save(yb);
	 }else if("updateBloodCount".equals(action)){
		 YBBattleBean yb =YaBiaoHuoDongMgr.inst.getYBZhanDouInfo(junzhu.id, junzhu.vipLevel);
		 yb.xueping4uesd = Integer.parseInt(request.getParameter("v"));
		 yb.buyblood4Vip=0;
		 yb.bloodTimes4Vip=0;
		 HibernateUtil.save(yb);
	 }else if("updateXilianCount".equals(action)){
		 XiLian xilian = PurchaseMgr.inst.getXiLian(junzhu.id);
		 int num = Integer.parseInt(request.getParameter("v"));
		 xilian.setNum(num);
		 HibernateUtil.save(xilian);
	 }else if("updateXilianShiCount".equals(action)){
		 XiLian xilian = PurchaseMgr.inst.getXiLian(junzhu.id);
		 int num = Integer.parseInt(request.getParameter("v"));
		 xilian.setXlsCount(num);
		 HibernateUtil.save(xilian);
	 }else if("updateTodyHuoYue".equals(action)){
		 DailyTaskActivity acti = HibernateUtil.find(DailyTaskActivity.class, junzhu.id);
		 int num = Integer.parseInt(request.getParameter("v"));
		 if(acti==null){
			 acti=new DailyTaskActivity();
			 acti.jid=junzhu.id;
		 }else{
			 DailyTaskMgr.INSTANCE.resetDailyTaskActivity(acti);
		 }
		acti.setTodyHuoYue(num);
		acti.lastResetDaily = new Date();
		HibernateUtil.save(acti);
	 }else if("updateWeekHuoYue".equals(action)){
		 DailyTaskActivity acti = HibernateUtil.find(DailyTaskActivity.class, junzhu.id);
		 if(acti==null){
			 acti=new DailyTaskActivity();
			 acti.jid=junzhu.id;
		 }else{
			 DailyTaskMgr.INSTANCE.resetDailyTaskActivity(acti);
		 }
		 int num = Integer.parseInt(request.getParameter("v"));
		  acti.setWeekHuoYue(num);
		  acti.lastResetWeek = new Date();
		 HibernateUtil.save(acti);
	 } else if("updateTongBiTimes".equals(action)){
		 TongBi tongBi = HibernateUtil.find(TongBi.class, junzhu.id);
		 if(tongBi != null) {
			 int v = Integer.parseInt(request.getParameter("v"));
			 tongBi.setNum(v);
		 }
		 HibernateUtil.save(tongBi);
	 } else if("updateMianfeiXilianCount".equals(action)){
		 TimeWorker xilianWorker = HibernateUtil.find(TimeWorker.class,junzhu.id);
	if (xilianWorker == null) {
		xilianWorker = new TimeWorker();
		xilianWorker.setJunzhuId(junzhu.id);
		Date date = new Date(System.currentTimeMillis());
		xilianWorker.setLastAddTiliTime(date);
		xilianWorker.setLastAddXilianTime(date);
		xilianWorker.setXilianTimes(CanShu.FREE_XILIAN_TIMES_MAX);
		// 添加缓存
		MC.add(xilianWorker, junzhu.id);
		HibernateUtil.insert(xilianWorker);
	}
		 int num = Integer.parseInt(request.getParameter("v"));
		 xilianWorker.setXilianTimes(num);
		 HibernateUtil.save(xilianWorker);
	 } else{
		 sendInfo = false;
	 }
	 if(sendInfo){
		 SessionUser u = SessionManager.getInst().findByJunZhuId(junzhu.id);
		 if(u!= null){
		 	JunZhuMgr.inst.sendMainInfo(u.session);
		 }
	 }
	 JunZhuMgr.inst.calcJunZhuTotalAtt(junzhu);
	 AlliancePlayer alncPlayer = HibernateUtil.find(AlliancePlayer.class, junzhu.id);
	 YaBiaoBean ybbean = HibernateUtil.find(YaBiaoBean.class, junzhu.id);
	 YBBattleBean jbBean =YaBiaoHuoDongMgr.inst.getYBZhanDouInfo(junzhu.id, junzhu.vipLevel);
	 XiLian xilian = PurchaseMgr.inst.getXiLian(junzhu.id);
	 TongBi tongBi = HibernateUtil.find(TongBi.class, junzhu.id);
	TimeWorker xilianWorker = HibernateUtil.find(TimeWorker.class, junzhu.id);
	//2016年1月19日 活跃度
	DailyTaskActivity acti = HibernateUtil.find(DailyTaskActivity.class, junzhu.id);
	 String guojiaName = HeroService.getNameById(junzhu.guoJiaId+"");
	 out.println("&nbsp;君主id："+junzhu.id+" &nbsp; 名字:"+junzhu.name+"&nbsp; 国家:" + guojiaName);
	 out.println("<a href='chengHao.jsp?jzId="+junzhu.id+"'>查看称号</a>");
	 out.println("GodId:"+PveMgr.godId+"<br/>");
	 ExpTemp expTemp = TempletService.getInstance().getExpTemp(1, junzhu.level);
	 out.println("等级："+junzhu.level+"<br/>");
	 int v = 0;
	 if(expTemp != null)v =expTemp.getNeedExp();
	 String input = request.getParameter("v");
	 if(input == null)input = "1";
	 String country = HeroService.getNameById(junzhu.guoJiaId+"");
	 String loginCount= Redis.getInstance().get(XianShiActivityMgr.XIANSHI7DAY_KEY + junzhu.id);
	 loginCount=loginCount==null?"1":loginCount;
	 tableStart();
	 //trS();td("等级");td(junzhu.level);td("<input type='text' id='updateLevel' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateLevel\")'/><br/>");trE();
	 trS();td("经验");td(junzhu.exp+"/"+v);td("<input type='text' id='addExp' value='"+input+"'/><input type='button' value='增加' onclick='go(\"addExp\")'/><br/>");trE();
	 trS();td("铜币");td(junzhu.tongBi);td("<input type='text' id='addTongBi' value='"+input+"'/><input type='button' value='增加' onclick='go(\"addTongBi\")'/><br/>");trE();
	 trS();td("元宝");td(junzhu.yuanBao);td("<input type='text' id='addYuanBao' value='"+input+"'/><input type='button' value='增加' onclick='go(\"addYuanBao\")'/><br/>");trE();//out.println("<a href='?action=addYuanBao'>+100</a><br/>");
	 trS();td("体力");td(junzhu.tiLi);td("<input type='text' id='addTiLi' value='"+input+"'/><input type='button' value='增加' onclick='go(\"addTiLi\")'/><br/>");trE();//td("<a href='?action=addTiLi'>+100</a><br/>");
	 trS();td("每一分钟增加一点体力");td(junzhu.tiLi);td("<a href='?action=addTiLiInterval6'>+1</a><br/>");trE();
	 trS();td("vip");td(junzhu.vipLevel);td("<a href='vip.jsp'>修改vip等级</a>");trE();
	 trS();td("体力Max");td(junzhu.tiLiMax);td("<br/>");trE();
	 trS();td("攻击");td(junzhu.gongJi);//td("<a href='?action=setGod'>设为无敌</a><br/>");trE();
	 trS();td("防御");td(junzhu.fangYu);td("<br/>");trE();
	 trS();td("hpMax");td(junzhu.shengMingMax);td("<br/>");trE();
	 trS();td("联盟id");td(alncPlayer != null ? alncPlayer.lianMengId : 0);td("<input type='text' id='updateLianmeng' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateLianmeng\")'/><br/>");trE();
	 trS();td("剩余押镖次数");td(ybbean != null ?ybbean.remainYB: "未开启");td("<input type='text' id='updateYaBiaoCount' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateYaBiaoCount\")'/><br/>");trE();
	 trS();td("剩余劫镖次数");td(jbBean != null ?jbBean.remainJB4Award : "未开启");td("<input type='text' id='updateJieBiaoCount' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateJieBiaoCount\")'/><br/>");trE();
	 trS();td("本日购买血瓶总数");td(jbBean != null ?jbBean.buyblood4Vip : "未开启");trE();
	 trS();td("本日购买血瓶次数数");td(jbBean != null ?jbBean.bloodTimes4Vip : "未开启");trE();
	 trS();td("今日已用血瓶数,改完相关数据清零");td(jbBean != null ?jbBean.xueping4uesd : "未开启");td("<input type='text' id='updateBloodCount' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateBloodCount\")'/><br/>");trE();
	 trS();td("今日已购买铜币次数");td(tongBi != null ?tongBi.getNum():"还没有购买过");td("<input type='text' id='updateTongBiTimes' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateTongBiTimes\")'/><br/>");trE();
	 trS();td("当日免费洗练剩余次数");td(xilianWorker.getXilianTimes());td("<input type='text' id='updateMianfeiXilianCount' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateMianfeiXilianCount\")'/><br/>");trE();
	 trS();td("当日元宝洗练已使用次数");td(xilian.getNum());td("<input type='text' id='updateXilianCount' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateXilianCount\")'/><br/>");trE();
	 trS();td("当日洗练石洗练已使用次数");td(xilian.getXlsCount());td("<input type='text' id='updateXilianShiCount' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateXilianShiCount\")'/><br/>");trE();
	 Integer todyHuoYue=new Integer(0);
	 Integer weekHuoYue=new Integer(0);
	 Field[] field = DailyTaskActivity.class.getDeclaredFields();  
	 if(acti!=null){
		for(int i = 0 ; i < field.length; i++){  
			Field f = field[i];  
			f.setAccessible(true); 
			if("todyHuoYue".equals(f.getName())){
				todyHuoYue =(Integer) f.get(acti);
			}else if("weekHuoYue".equals(f.getName())){
				weekHuoYue= (Integer)f.get(acti);
			}
		}  
	 }
	 trS();td("日活跃度");td((Integer)todyHuoYue);td("<input type='text' id='updateTodyHuoYue' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateTodyHuoYue\")'/><br/>");trE();
	 trS();td("周活跃度");td((Integer)weekHuoYue);td("<input type='text' id='updateWeekHuoYue' value='"+input+"'/><input type='button' value='修改' onclick='go(\"updateWeekHuoYue\")'/><br/>");trE();
	 trS();td("国家");td(country);trE();
	 trS();td("七日奖励登录天数");td(loginCount);trE();
	 Double gj = RankingGongJinMgr.DB.zScoreGongJin(RankingGongJinMgr.gongJinPersonalRank, 
				junzhu.id+"");
	 trS();td("积分:");td(gj);trE();
	 tableEnd();
	 br();
	 tableStart();
	 trS();
	td("武器伤害");td(junzhu.wqSH);//武器伤害加深
	td("武器减免");td(junzhu.wqJM);//武器伤害减免
	td("武器暴击");td(junzhu.wqBJ);//武器暴击加深
	td("武器韧性");td(junzhu.wqRX);//武器暴击加深
	trE();
	trS();
	td("技能伤害");td(junzhu.jnSH);//技能伤害加深
	td("技能减免");td(junzhu.jnJM);//技能伤害加深
	td("技能暴击");td(junzhu.jnBJ);//技能伤害加深
	td("技能韧性");td(junzhu.jnRX);//技能伤害加深
	trE();
	tableEnd();
	 out.println("卡包积分："+junzhu.cardJiFen);out.println("<br/>");
	 /*
	 */
	 Object mcV = com.manu.dynasty.store.MemcachedCRUD.getMemCachedClient().get("RenWuOverId#"+junzhu.id);
	 out.println("memcache 中的最大完成任务id是:"+mcV);
	 out.println("<br/>memcache 中玩家背包的数量是:"+ com.manu.dynasty.store.MemcachedCRUD.getMemCachedClient().get("BagCnt#"+junzhu.id));
	 out.println("<br>默认开启的功能ID：<br>[");
	 int idsSize=FunctionOpenMgr.initIds.size();
	 for(int i=0; i<idsSize; i++){
		 for(FunctionOpen fo : FunctionOpenMgr.list){
	 if(fo.id==FunctionOpenMgr.initIds.get(i)){
		 out.print(fo.id+"-"+fo.desc+",");
	 }
		 }
	 }
	 out.println("]<br>");
	 qxmobile.protobuf.ZhangHao.LoginRet.Builder ret = qxmobile.protobuf.ZhangHao.LoginRet.newBuilder();
	 FunctionOpenMgr.inst.fillOther(ret, junzhu.level, junzhu.id);
	 out.println("<br>条件开启的功能ID：<br>[");
	 int otheridsSize=ret.getOpenFunctionIDList().size();
	 for(int j=0; j<otheridsSize; j++){
		 for(FunctionOpen fo : FunctionOpenMgr.list){
	 if(fo.id==ret.getOpenFunctionIDList().get(j)){
		 out.print(fo.id+"-"+fo.desc+",");
	 }
		 }
	 }
	 out.println("]<br>");
	 //
	 final IoSession fs = new RobotSession(){
		public WriteFuture write(Object message){
	setAttachment(message);
	synchronized(this){
		this.notify();
	}
	return null;
		}
	};
	fs.setAttribute(SessionAttKey.junZhuId, Long.valueOf(junzhu.id));
	synchronized(fs){
		BigSwitch.inst.route(PD.C_BUY_TIMES_REQ, null, fs);
	//	fs.wait();
	}
	BuyTimesInfo info = (BuyTimesInfo)fs.getAttachment();
	br();
	out("铜币剩余"+info.getTongBi()+"次,下次花费"+info.getTongBiHuaFei()+"购买"+info.getTongBiHuoDe());br();
	out("体力剩余"+info.getTiLi()+"次,下次花费"+info.getTiLiHuaFei()+"购买"+info.getTiLiHuoDe());br();
	//
	PlayerTime pt = HibernateUtil.find(PlayerTime.class,junzhu.id);
	if(pt != null && pt.getLoginTime() != null){
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String setV = request.getParameter("loginTime");
		if(setV!=null){
	Date dt = sf.parse(setV);
	pt.setLoginTime(dt);
	HibernateUtil.save(pt);
		}
		out("<form action=''>");
		out("上次登录时间:<input type='text' name='loginTime' value='"+sf.format(pt.getLoginTime())+"'/>");
		out("<input type='submit' value='修改'/>");
		out("</form>");
	}else{
		out("从未登录");
	}
	// 地图坐标管理 
	PosInfo pos = HibernateUtil.find(PosInfo.class,junzhu.id);
	if(pos == null){
		out("尚无坐标信息");
		break;
	}
	// 修改坐标 
	String posX = request.getParameter("posX");
	String posY = request.getParameter("posY");
	String posZ = request.getParameter("posZ");
	if(posX!=null&&posX.length()>0&&posY!=null&&posY.length()>0&&posZ!=null&&posZ.length()>0){
		pos.x=Float.valueOf(posX);
		pos.y=Float.valueOf(posY);
		pos.z=Float.valueOf(posZ);
		HibernateUtil.save(pos);
%>
		<p>位置已修改</p>
		<%
	}
	%>
	<form action="">
	<table border="1">
		<tr><th>jzId</th><th>x</th><th>y</th><th>z</th></tr>
		<tr>
		<td><%=junzhu.id %></td>
		<td><input name="posX" value="<%=pos.x %>"/></td>
		<td><input name="posY" value="<%=pos.y %>"/></td>
		<td><input name="posZ" value="<%=pos.z %>"/></td>
		</tr>
	</table>
	<button type="submit">修改数据库坐标</button>
	</form>
	<%
}while(false);

%>
</body>
</html>