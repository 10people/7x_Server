<%@page import="java.util.LinkedList"%>
<%@page import="com.qx.pve.PveRecord"%>
<%@page import="com.manu.network.SessionManager"%>
<%@page import="com.manu.network.SessionUser"%>
<%@page import="qxmobile.protobuf.GameTask.GetTaskReward"%>
<%@page import="com.qx.account.AccountManager"%>
<%@page import="com.qx.account.FunctionOpenMgr"%>
<%@page import="com.qx.award.AwardMgr"%>
<%@page import="com.manu.dynasty.template.AwardTemp"%>
<%@page import="com.manu.dynasty.store.MemcachedCRUD"%>
<%@page import="com.manu.dynasty.template.ZhuXian"%>
<%@page import="com.qx.task.GameTaskMgr"%>
<%@page import="qxmobile.protobuf.GameTask"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.task.WorkTaskBean"%>
<%@page import="com.qx.task.DailyTaskCondition"%>
<%@page import="com.qx.achievement.AchievementCondition"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.qx.junzhu.JunZhuMgr"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="com.manu.dynasty.template.ExpTemp"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.task.DailyTaskMgr"%>
<%@page import="com.qx.task.DailyTaskBean"%>
<%@page import="com.qx.event.Event"%>
<%@page import="com.qx.event.ED"%>
<%@page import="com.qx.account.Account"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>query or delete email</title>
<script type="text/javascript">
function go(act, id, type){
	if(act == null || act == "") {
		alert("act不能为空");
		return;
	}
	//if(type == null || act == "") {
	//	alert("act不能为空");'
		//return;
	//}
	
	var node = document.getElementById(act+id);
	var v = node.value;
	location.href = '?action='+act+"&v="+v +"&t="+type;
}
</script>

</head>
<body>
	<%
	
String action = request.getParameter("action");
	
	if("fixTask".equals(action)) {
		out("111111111111111111111");
		String jzIdStr = request.getParameter("fixjzId").trim();
		if(jzIdStr != null && jzIdStr.length() != 0 ){
			List<WorkTaskBean> taskList = HibernateUtil.list(WorkTaskBean.class, "where jzid = " + jzIdStr );
			out(jzIdStr);
			for(WorkTaskBean bean : taskList){
				ZhuXian zx = GameTaskMgr.inst.zhuxianTaskMap.get(bean.tid);
				if(zx.doneType == 2 && bean.progress != -1){
					out("找到任务"+bean.tid +"需求关卡："+zx.doneCond);
					PveRecord pr = HibernateUtil.find(PveRecord.class, 
							"where guanQiaId = " + zx.doneCond +" and uid = " + bean.jzid);
					if (pr != null ){
						out("找到关卡记录"+pr.guanQiaId);
						GameTaskMgr.inst.dealTask(bean.jzid , bean, zx);
						IoSession jzsession = SessionManager.inst.findByJunZhuId(bean.jzid);
						if(jzsession != null ){
							out("更新任务记录");
							GameTaskMgr.inst.sendTaskList(0, jzsession, null);
						}
					}
				}
			}
		}
	}else if("remove".equals(action)){
		GameTaskMgr.GameTaskCache.clear();
	}
		String name = request.getParameter("account");
			name = name == null ? "" : name;
			if (session.getAttribute("name") != null && name.length() == 0) {
		name = (String) session.getAttribute("name");
			}
	%>
	<form action="">
		账号<input type="text" name="account" value="<%=name%>">		<button type="submit">查询</button>
	</form>
	<%
		if (name != null && name.length() > 0) {
		Account account = HibernateUtil.getAccount(name);
		if (account == null) {
	%>没有找到<%
		//HibernateUtil.saveAccount(name);
		} else {
			session.setAttribute("name", name);
	%>账号<%=account.accountId%>:<%=account.accountName%>
	<form action="?action=addBean" method="POST">
		任务模板ID<input type="text" name="tid"/>
		<button type="submit" name="" >添加</button>
	</form>
	<form action="">
		<input type="hidden" name="action" value="fix">
		<button type="submit" name="" >修复任务完成记录</button>
	</form>
	<form action="">
		<input type="hidden" name="action" value="remove">
		<button type="submit" name="" >清除任务缓存</button>
	</form>
	<%
	
	
	long jid = account.accountId*1000+GameServer.serverId;
		JunZhu junzhu = HibernateUtil.find(JunZhu.class, jid);
			if (junzhu == null) {
				out.println("没有君主");
			} else {
				if (junzhu.level == 0 || junzhu.shengMingMax == 0) {
					JunZhuMgr.inst.fixCreateJunZhu((int)junzhu.id, junzhu.name, junzhu.roleId, junzhu.guoJiaId);
				}
				action = request.getParameter("action");
				
				if ("addBean".equals(action)) {
					String v = request.getParameter("tid");
					if(v != null & v.length()>0){
						int taskId = Integer.valueOf(v);
						ZhuXian zx = GameTaskMgr.inst.zhuxianTaskMap.get(taskId);
						GameTaskMgr.inst.addTask(junzhu.id, taskId, zx);
						IoSession ss = AccountManager.getIoSession(junzhu.id);
						if(ss != null)GameTaskMgr.inst.sendTaskList(0, ss, null);
					}
				} else if("delete".equals(action)) {
					int tid = Integer.parseInt(request.getParameter("dbId"));
					WorkTaskBean o = GameTaskMgr.inst.getTask(junzhu.id, tid);
					List<WorkTaskBean> taskList =  GameTaskMgr.GameTaskCache.get(junzhu.id);
					if(taskList != null ){
						out.print("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
						if(taskList.remove(o)){
							out.print("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
						};
					}
					HibernateUtil.delete(o);
				} else if("addProg".equals(action)) {
					//领奖
			        WorkTaskBean taskBean = HibernateUtil.find(WorkTaskBean.class, " where dbId=" + request.getParameter("dbId"));
			        if(taskBean != null){
			        	int taskId = taskBean.tid;
			        	GetTaskReward.Builder req = GetTaskReward.newBuilder();
			        	req.setTaskId(taskId);
			        	IoSession jzSession = SessionManager.inst.findByJunZhuId(junzhu.id);
						if(jzSession != null){
							GameTaskMgr.inst.getReward(0,jzSession, req);
						}
			        }
			        
				} else if("subProg".equals(action)) {
					String tidStr = request.getParameter("dbId");
					WorkTaskBean o = GameTaskMgr.inst.getTask(jid, Integer.parseInt(tidStr));
					if(o!=null)
					{
						ZhuXian task = GameTaskMgr.inst.zhuxianTaskMap.get(o.tid);
						if(task != null ){
							GameTaskMgr.inst.dealTask(jid, o, task);
						}
					}
					IoSession ss = AccountManager.getIoSession(junzhu.id);
					if(ss != null)GameTaskMgr.inst.sendTaskList(0, ss, null);
				} else if("fix".equals(action)) {
				//	Object mcV = MemcachedCRUD.getMemCachedClient().get("RenWuOverId#"+junzhu.id);
				//	Object AwardRenWuOverId = MemcachedCRUD.getMemCachedClient().get("AwardRenWuOverId#"+junzhu.id);
				//	if(mcV != null && AwardRenWuOverId != null){
				//		junzhu.maxTaskOverId = Integer.parseInt((String)mcV);
				//		junzhu.maxTaskAwardId = Integer.parseInt((String)AwardRenWuOverId);
				//		HibernateUtil.save(junzhu);
				//		out("啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊，任务记录已经修复");
				//		br();
				//		MemcachedCRUD.getMemCachedClient().delete("RenWuOverId#"+junzhu.id);
				//		MemcachedCRUD.getMemCachedClient().delete("AwardRenWuOverId#"+junzhu.id);
				//	}
				}else if("fixall".equals(action)) {
				//	Date begin = new Date();
				//	List<JunZhu> allJunZhu = HibernateUtil.list(JunZhu.class, "");
				//	for(JunZhu junZhu : allJunZhu ){
				//		Object mcV = MemcachedCRUD.getMemCachedClient().get("RenWuOverId#"+junZhu.id);
				//		Object AwardRenWuOverId = MemcachedCRUD.getMemCachedClient().get("AwardRenWuOverId#"+junZhu.id);
				//		if(mcV != null && AwardRenWuOverId != null){
				//			junzhu.maxTaskOverId = Integer.parseInt((String)mcV);
				//			junzhu.maxTaskAwardId = Integer.parseInt((String)AwardRenWuOverId);
				//			HibernateUtil.save(junZhu);
				//			br();
				//			MemcachedCRUD.getMemCachedClient().delete("RenWuOverId#"+junZhu.id);
				//			MemcachedCRUD.getMemCachedClient().delete("AwardRenWuOverId#"+junZhu.id);
				//		}
				//	}
				//	Date over = new Date();
				//	int time =(int)(over.getTime() - begin.getTime())/1000;
				//	out("所有人的任务都已修复，共耗时" + time + "秒");
				}else if("fixTask".equals(action)) {
					out("11111111");
					String jzIdStr = request.getParameter("fixjzId").trim();
					if(jzIdStr != null && jzIdStr.length() != 0 ){
						List<WorkTaskBean> taskList = HibernateUtil.list(WorkTaskBean.class, "where jzid = " + jzIdStr );
						out(jzIdStr);
						for(WorkTaskBean bean : taskList){
							ZhuXian zx = GameTaskMgr.inst.zhuxianTaskMap.get(bean.tid);
							if(zx.doneType == 2 && bean.progress != -1){
								out("找到任务"+bean.tid +"需求关卡："+zx.doneCond);
								PveRecord pr = HibernateUtil.find(PveRecord.class, 
										"where guanQiaId = " + zx.doneCond +" and uid = " + bean.jzid);
								if (pr != null ){
									out("找到关卡记录"+pr.guanQiaId);
									GameTaskMgr.inst.dealTask(jid, bean, zx);
									IoSession jzSession = SessionManager.inst.findByJunZhuId(bean.jzid);
									if(jzSession != null){
										out("更新任务记录");
										GameTaskMgr.inst.sendTaskList(0, jzSession, null);
									}
								}
							}
						}
					}
				}else if("getName".equals(action)) {
					List<JunZhu> nameList = new LinkedList<JunZhu>();
					List<WorkTaskBean> taskList = HibernateUtil.list(WorkTaskBean.class, "");
					for(WorkTaskBean bean : taskList){
						ZhuXian zx = GameTaskMgr.inst.zhuxianTaskMap.get(bean.tid);
						if(zx.doneType == 2 && bean.progress != -1){
							PveRecord pr = HibernateUtil.find(PveRecord.class, 
									"where guanQiaId = " + zx.doneCond +" and uid = " + bean.jzid);
							if (pr != null ){
								JunZhu jz = HibernateUtil.find(JunZhu.class, bean.jzid);
								if(jz != null ){
									nameList.add(jz);
								}
								
								
							//	bean.progress = -1 ;
							//	HibernateUtil.save(bean);
							//	SessionUser su = SessionManager.inst.findByJunZhuId(bean.jzid);
							//	if(su != null && su.session != null ){
							//		GameTaskMgr.inst.sendTaskList(0, su.session, null);
							//	}
							}
						}
					}
					for(JunZhu jz : nameList){
						out("君主ID："+jz.id+"   君主名字："+jz.name);
						br();
					}
				}
				JunZhuMgr.inst.calcJunZhuTotalAtt(junzhu);
				out.println("&nbsp;君主id：" + junzhu.id);
				ExpTemp expTemp = TempletService.getInstance().getExpTemp(1, junzhu.level);
				out.println("等级：" + junzhu.level + "");
				//
				br();
				
			//	out("缓存中已《完成》任务最大次序号RenWuOverId:"+junzhu.maxTaskOverId);
			//	br();
				
			//	out("缓存中已《领奖》任务最大次序号AwardRenWuOverId:"+junzhu.maxTaskAwardId);
			//	br();
				//
				out.append("<table border='1'>");
				out.append("<tr>");
				br();
				br();
				out("0：&nbsp;&nbsp;&nbsp;&nbsp;表示任务为开启，但没有完成");br();
				out("完成任务： 表示任务已完成，但是没有领取奖励");br();
				
				br();
				out("<完成任务>之后，才可以<领奖>");
				br();
				br();
				out("领奖之后，该任务从主线任务中自动清除");br();
				br();
				out.append("<th>数据库ID</th><th>模板ID</th><th>名称</th><th>进度</th><th>修改进度</th><th></th>");
				out.append("</tr>\n");
				int lastHeroId = 0;
				
				List<WorkTaskBean> list = HibernateUtil.list(WorkTaskBean.class, 
						"where jzid="+junzhu.id);
				for(WorkTaskBean bean : list){
					out.append("<tr>");
					out.append("<td>"+bean.dbId+"</td>");		
					out.append("<td>"+bean.tid+"</td>");	
					ZhuXian t = GameTaskMgr.inst.zhuxianTaskMap.get(bean.tid);
					out.append("<td>"+(t==null ? "not found":t.title)+"</td>");		
					out.append("<td>"+(bean.progress == 0? "未完成" : bean.progress == -1 ?"已完成":"已领奖")+"</td>");
					out.append("<td>&nbsp;<a href='?action=subProg&dbId="+bean.tid+"'>完成任务</a>&nbsp;<a href='?action=addProg&dbId="+bean.dbId+"'>领奖</a></td>");
					out.append("<td><a href='?action=delete&dbId="+bean.tid+"'>删除</a></td>");
					out.append("<tr>\n");
				}
				out.append("</table>");
			}
		}
			}
	%>
	---<br/>
	<%
		List<ZhuXian> list = TempletService.listAll(ZhuXian.class.getSimpleName());
	request.setAttribute("list", list);
	out.append("<table border='1'>");
	out.append("<tr>");
	out.append("<th>ID</th><th>orderIdx</th><th>名称</th>");
	out.append("</tr>");
	for(ZhuXian t : list){
		out.append("<tr>");
		out.append("<td>");		out.println(t.getId());	out.append("</td>");
		out.append("<td>");		out.println(t.orderIdx);	out.append("</td>");
		out.append("<td>");		out.println(t.title);		out.append("</td>");
		out.append("<tr>");
	}
	out.append("</table>");
	 %>
	
	<form action="">
		<input type="text" name="fixjzId" value="">
		<input type="hidden" name="action" value="fixTask">
		<button type="submit" name="" >修复任务</button>
	</form>
	<br>
	<form action="">
		<input type="hidden" name="action" value="getName">
		<button type="submit" name="" >获取错误账号</button>
	</form>
	
</body>
</html>