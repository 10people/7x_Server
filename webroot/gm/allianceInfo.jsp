<%@page import="com.qx.alliance.AllianceBeanDao"%>
<%@page import="java.util.Date"%>
<%@page import="com.manu.dynasty.template.LianMeng"%>
<%@page import="com.qx.ranking.RankingMgr"%>
<%@page import="com.qx.util.TableIDCreator"%>
<%@page import="com.manu.dynasty.boot.GameServer"%>
<%@page import="com.qx.robot.RobotSession"%>
<%@page import="qxmobile.protobuf.AllianceProtos.CreateAlliance"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="java.util.List"%>
<%@page import="com.manu.network.PD"%>
<%@page import="com.qx.alliance.AllianceBean"%>
<%@page import="com.qx.alliance.AllianceMgr"%>
<%@page import="com.qx.alliance.AllianceVoteMgr"%>
<%@page import="qxmobile.protobuf.GuildProtos.Guild"%>
<%@page import="qxmobile.protobuf.AllianceProtos.CreateAlliance.Builder"%>
<%@page import="qxmobile.protobuf.AllianceProtos.DismissAlliance"%>
<%@page import="org.apache.mina.core.session.IoSession"%>
<%@page import="com.manu.network.SessionAttKey"%>
<%@page import="qxmobile.protobuf.AllianceProtos.ExitAlliance"%>
<%@page import="qxmobile.protobuf.AllianceProtos.FindAlliance"%>
<%@page import="com.manu.dynasty.hero.service.HeroService"%>
<%@page import="com.manu.dynasty.store.Redis"%>
<%@include file="/myFuns.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src="../ajax.min.js"></script>
<script>
function go(act){
	var v = document.getElementById(act).value;
	location.href = '?action='+act+"&v="+v;
}
function get(url, call){
	var xhr = createXHR();
	xhr.onreadystatechange = function()
	{
	    if (xhr.readyState === 4)
	    {
	        //document.getElementById('preview').innerHTML = xhr.responseText;
	        call(xhr);
	    }
	};
	xhr.open('GET', url, true);
	xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
	xhr.send();
}
function changeAlncName(obj){
	var	lmId=obj.parentElement.parentElement.cells[0].innerHTML;
	var	lmName=obj.parentElement.parentElement.cells[1].childNodes[0].value;
	var url = 'allianceInfo.jsp?lmId=' +lmId+'&lmName='+lmName+'&action=changAlncName';
	get(url);
}
</script>
<title>联盟信息</title>
</head>
<body>
	<%
//long id11 = TableIDCreator.getTableID(AllianceBean.class, 10000L);
//out.println(id11);
%>

	<b>联盟信息</b>
	<br /> 以下条件二选一:
	<br />
	<table>
		<tr>
			<td>
				<form action="" name="create" method="post">
					联盟ID:<input type="text" name="id"> <input type="submit"
						name="create" value="查询"> <input type="hidden"
						name="action" value="findById" />
				</form> <br />
				<form action="" name="create" method="post">
					联盟名字:<input type="text" name="name"> <input type="submit"
						name="create" value="查询"> <input type="hidden"
						name="action" value="findByName" />
				</form>
			</td>

			<td><b>修改联盟成员人数：现在是方便测试用</b>
				<form action="" name="create" method="post">
					联盟id:<input type="text" name="lmId"> <br /> 现有人数:<input
						type="text" name="have"> <br /> <input type="submit"
						value="修改人数"> <input type="hidden" name="action"
						value="updateMems" />
				</form></td>
			<td><b>修改联盟升级时间</b>
				<form action="" name="create" method="post">
					联盟id:<input type="text" name="lmId"> <br /> 时间:<input
						type="text" name="status"><br />时间填负数缩短时间，单位-秒 <br /> <input
						type="submit" value="修改"> <input type="hidden"
						name="action" value="updateStatus" />
				</form></td>
			<td><b>修改联盟等级</b>
				<form action="" name="create" method="post">
					联盟id:<input type="text" name="lmId"> <br /> 等级:<input
						type="text" name="level">(1 - 10之间 ) <br /> <input
						type="submit" value="修改等级"> <input type="hidden"
						name="action" value="updateLevel" />
				</form></td>
		</tr>
		<tr>
			<td>
				<hr /> <b>添加经验</b>
				<form action="" name="addExp" method="post">
					联盟Id:<input type="text" name="lmId"> <br /> 添加经验值:<input
						type="text" name="exp"> <br /> <input type="submit"
						value="添加经验"> <input type="hidden" name="action"
						value="addExp" />
				</form>
			</td>
			<td>
				<hr /> <b>修改虎符数量</b>
				<form action="" name="updateHufu" method="post">
					联盟Id:<input type="text" name="lmId"> <br /> 虎符数量:<input
						type="text" name="hufu"> <br /> <input type="submit"
						value="修改虎符数量"> <input type="hidden" name="action"
						value="updateHufu" />
				</form>
			</td>


			<td>
				<hr /> <b>修改升级加速已使用的次数</b>
				<form action="" name="updateUpGrdRMTM" method="post">
					联盟Id:<input type="text" name="lmId"> <br /> 剩余次数:<input
						type="text" name="upgradeUsedTimes"> <br /> <input
						type="submit" value="修改升级加速已使用的次数"> <input type="hidden"
						name="action" value="updateUpGrdRMTM" />
				</form>
			</td>
		</tr>
		<tr>
			<td>
				<hr /> <b>创建联盟</b>
				<form action="" name="create" method="post">
					君主Id:<input type="text" name="jzId"> <br /> 联盟名字:<input
						type="text" name="name"> <br /> 联盟icon:<select name="icon">
						<option>9001</option>
						<option>9002</option>
						<option>9003</option>
						<option>9004</option>
						<option>9005</option>
					</select> <br /> <input type="submit" value="创建"> <input
						type="hidden" name="action" value="create" />
				</form>
			</td>
			<td>
				<hr /> <b>退出联盟</b>
				<form action="" name="create" method="post">
					联盟id:<input type="text" name="lmId"> <br /> 君主Id:<input
						type="text" name="jzId"> <br /> <input type="submit"
						value="退出联盟"> <input type="hidden" name="action"
						value="exit" />
				</form>
			</td>
			<td>
				<hr /> <b>修改联盟建设值</b>
				<form action="" name="create" method="post">
					联盟id:<input type="text" name="lmId"> <br /> 建设值:<input
						type="text" name="build"> <br /> <input type="submit"
						value="修改建设值"> <input type="hidden" name="action"
						value="updateBuild" />
				</form>
			</td>
			<td>
				<hr /> <b>联盟选举结算（模拟选举结束）</b>
				<form action="" name="create" method="post">
					联盟id:<input type="text" name="lmId"> <br /> <input
						type="submit" value="结束联盟选举"> <input type="hidden"
						name="action" value="voteOver" />
				</form>
			</td>
		</tr>
	</table>
	<hr />
	<table>
		<%	
		tableStart();
 			trS();td("清除玩家退出联盟cd时间，君主id:");td("<input type='text' id='clearPlayerExit' ");td("<input type='button' id='clearPlayerExit' value='清除' onclick='go(\"clearPlayerExit\")'/>");trE();
 			trS();td("清除联盟所有事件，联盟id:");td("<input type='text' id='clearEvent' ");td("<input type='button' id='clearEvent' value='清除' onclick='go(\"clearEvent\")'/>");trE();
 			trS();td("清除联盟转国cd时间，联盟id:");td("<input type='text' id='clearChangeCountry'");td("<input type='button' id='clearChangeCountry' value='清除' onclick='go(\"clearChangeCountry\")'/>");trE();
	 	tableEnd();
		
			out.append("备注：升级剩余时间<=0表示已经升级完毕，大于0表示剩余时间<br/><br/>");
			out.append("<table border='1'>");
			out.append("<tr>");
			out.append("<th>联盟ID</th><th>联盟名称</th><th>IconId</th><th>盟主ID</th>");
			out.append("<th>声望</th><th>现有成员</th><th>公告</th><th>建设值</th><th>最大成员</th>");
			out.append("<th>等级</th><th>exp</th><th>虎符数量</th><th>升级剩余时间-秒</th><th>加速升级已使用次数</th><th>所属国家</th><th>成员详情</th><th>申请列表</th><th>招募状态</th>");
			out.append("</tr>");
			AllianceBean lianmeng = null;
			request.setCharacterEncoding("utf-8"); 
			response.setCharacterEncoding("utf-8"); 
			String action = request.getParameter("action");
			if("findById".equals(action)) {
				String idString = request.getParameter("id");
				if(idString == null || "".equals(idString)) {
					return;
				}
				int id = Integer.parseInt(idString);
				lianmeng = AllianceBeanDao.inst.getAllianceBean(id);
			} else if("findByName".equals(action)) {
				String name = request.getParameter("name");
				if(name == null || "".equals(name)) {
					return;
				}
				lianmeng = HibernateUtil.find(AllianceBean.class, " where name='" + name +"'", false);
			} else if("exit".equals(action)) {
				String lmId = request.getParameter("lmId");
				String jzId = request.getParameter("jzId");
				if(lmId == null || "".equals(lmId)) {
					return;
				}
				if(jzId == null || "".equals(jzId)) {
					return;
				}
				ExitAlliance.Builder c2s = ExitAlliance.newBuilder();
				c2s.setId(Integer.parseInt(lmId));
				RobotSession rsion = new RobotSession();
				rsion.setAttribute(SessionAttKey.junZhuId, Long.parseLong(jzId));
				AllianceMgr.inst.exitAlliance(PD.EXIT_ALLIANCE, rsion, c2s);
				
			} else if("addExp".equals(action)) {
				String lmId = request.getParameter("lmId");
				String exp = request.getParameter("exp");
				if(lmId == null || "".equals(lmId)) {
					return;
				}
				if(exp == null || "".equals(exp)) {
					return;
				}
				AllianceBean allianceBean = AllianceBeanDao.inst.getAllianceBean(Integer.parseInt(lmId));
				AllianceMgr.inst.addAllianceExp(Integer.parseInt(exp), allianceBean);
			}else if("updateHufu".equals(action)) {
				String lmId = request.getParameter("lmId");
				String hufuNum = request.getParameter("hufu");
				if(lmId == null || "".equals(lmId)) {
					return;
				}
				if(hufuNum == null || "".equals(hufuNum)) {
					return;
				}
				AllianceBean allianceBean = AllianceBeanDao.inst.getAllianceBean(Integer.parseInt(lmId));
				if(allianceBean == null) {
					out.println("找不到联盟，id:"+lmId);
					return;
				}
				allianceBean.hufuNum = Integer.parseInt(hufuNum);
				HibernateUtil.save(allianceBean);
			}
			else if("updateUpGrdRMTM".equals(action)) {
				String lmId = request.getParameter("lmId");
				String upgradeRemainTM = request.getParameter("upgradeUsedTimes");
				if(lmId == null || "".equals(lmId)) {
					return;
				}
				if(upgradeRemainTM == null || "".equals(upgradeRemainTM)) {
					return;
				}
				AllianceBean allianceBean = AllianceBeanDao.inst.getAllianceBean(Integer.parseInt(lmId));
				if(allianceBean == null) {
					out.println("找不到联盟，id:"+lmId);
					return;
				}
				allianceBean.upgradeUsedTimes = Integer.parseInt(upgradeRemainTM);
				HibernateUtil.save(allianceBean);
			}
			
			
			
			else if("create".equals(action)) {
				String name = request.getParameter("name");
				String icon = request.getParameter("icon");
				String jzId = request.getParameter("jzId");
				if(name == null || "".equals(name)) {
					return;
				}
				if(icon == null || "".equals(icon)) {
					return;
				}
				if(jzId == null || "".equals(jzId)) {
					return;
				}
				CreateAlliance.Builder c2s = CreateAlliance.newBuilder();
				c2s.setName(name);
				c2s.setIcon(Integer.parseInt(icon));
				RobotSession rsion = new RobotSession();
				rsion.setAttribute(SessionAttKey.junZhuId, Long.parseLong(jzId));
				AllianceMgr.inst.createAlliance(PD.CREATE_ALLIANCE, rsion, c2s);
				
			} else if("updateMems".equals(action)) {
				String lmId = request.getParameter("lmId");
				String have = request.getParameter("have");
				if(lmId == null || lmId.equals("") 
						|| have == null || have.equals("")) {
					return;
				}
				lianmeng = AllianceBeanDao.inst.getAllianceBean(Integer.parseInt(lmId));
				if(lianmeng == null) {
					return;
				}
				lianmeng.members = Integer.parseInt(have);
				HibernateUtil.save(lianmeng);
			} else if("updateStatus".equals(action)) {
				String lmId = request.getParameter("lmId");
				String time = request.getParameter("status");
				if(lmId == null || lmId.equals("") 
						|| time == null || time.equals("")) {
					return;
				}
				lianmeng = AllianceBeanDao.inst.getAllianceBean(Integer.parseInt(lmId));
				if(lianmeng == null) {
					return;
				}
				
				if(lianmeng.upgradeTime != null) {
					out.println("time ：" +time);
					long setTime = lianmeng.upgradeTime.getTime() + Integer.parseInt(time)*1000;
					out.println("setTime ：" +setTime/1000);
					if(setTime <= 0) {
						lianmeng.upgradeTime = null;
					} else {
						lianmeng.upgradeTime.setTime(setTime);
					}
				}else {
					out.print("联盟< " + lianmeng.name + " >还未处在升级状态中");
				}
				HibernateUtil.save(lianmeng);
			} else if("updateLevel".equals(action)) {
				String lmId = request.getParameter("lmId");
				String level= request.getParameter("level");
				if(lmId == null || lmId.equals("") 
						|| level == null || level.equals("")) {
					return;
				}
				lianmeng = AllianceBeanDao.inst.getAllianceBean(Integer.parseInt(lmId));
				if(lianmeng == null) {
					return;
				}
				lianmeng.level = Integer.parseInt(level);
				AllianceMgr.inst.changeAllianceApplyState(lianmeng);
				HibernateUtil.save(lianmeng);
				RankingMgr.inst.resetLianMengLevelRedis(lianmeng.id, lianmeng.level);
				RankingMgr.inst.resetLianMengRankRedis(lianmeng.id);
			} else if("updateBuild".equals(action)) {
				String lmId = request.getParameter("lmId");
				String build= request.getParameter("build");
				if(lmId == null || lmId.equals("") 
						|| build == null || build.equals("")) {
					return;
				}
				lianmeng = AllianceBeanDao.inst.getAllianceBean(Integer.parseInt(lmId));
				if(lianmeng == null) {
					return;
				}
				lianmeng.build = Integer.parseInt(build);
				HibernateUtil.save(lianmeng);
				RankingMgr.inst.resetLianMengLevelRedis(lianmeng.id, lianmeng.level);
			} else if("voteOver".equals(action)) {
				String lmId = request.getParameter("lmId");
				if(lmId == null || lmId.equals("")) {
					return;
				}
				AllianceVoteMgr.inst.voteOver(Integer.parseInt(lmId));
			} else if("delAlnc".equals(action)) {
				int lianmengId = Integer.parseInt(request.getParameter("id"));
				long jzId = Long.parseLong(request.getParameter("jzId"));
				DismissAlliance.Builder c2s = DismissAlliance.newBuilder();
				c2s.setId(lianmengId);
				RobotSession rsion = new RobotSession();
				rsion.setAttribute(SessionAttKey.junZhuId, jzId);
				AllianceMgr.inst.dismissAlliance(PD.DISMISS_ALLIANCE, rsion, c2s);
			} else if("changAlncName".equals(action)) {
				String lmId = request.getParameter("lmId");
				String lmName = request.getParameter("lmName");
				if(!"".equals(lmId)&&!"".equals(lmName)){
					lmName=new String(lmName.getBytes("ISO-8859-1"),"UTF-8");
 					lianmeng = AllianceBeanDao.inst.getAllianceBean(Integer.parseInt(lmId));
					lianmeng.name=lmName;
					HibernateUtil.save(lianmeng);
				}
			} else if("clearPlayerExit".equals(action)) {
				String v = request.getParameter("v");
				if(v == null || v.equals("")){
					return;
				}
				Redis.getInstance().del(AllianceMgr.ALLIANCE_EXIT + Integer.parseInt(v));
			} else if("clearChangeCountry".equals(action)) {
				String v = request.getParameter("v");
				if(v == null || v.equals("")){
					return;
				}
				Redis.getInstance().del(AllianceMgr.ALLIANCE_CHANGE_GUOJIA + Integer.parseInt(v));
			} else if("clearEvent".equals(action)) {
				String v = request.getParameter("v");
				if(v == null || v.equals("")){
					return;
				}
				Redis.getInstance().del(AllianceMgr.ALLIANCE_EVENT + Integer.parseInt(v));
			}
			if (lianmeng != null) {
				int memberMax = AllianceMgr.inst.getAllianceMemberMax(lianmeng.level);
				String country = HeroService.getNameById(lianmeng.country+"");
				Date date = new Date();
				int remainTime = lianmeng.upgradeTime == null ? -1 : (int)((lianmeng.upgradeTime.getTime() - date.getTime()) / 1000);
				out.append("" + remainTime);
		%>

		<tr>
			<td><%=lianmeng.id%></td>
			<td><%=lianmeng.name%></td>
			<td><%=lianmeng.icon%></td>
			<td><%=lianmeng.creatorId%></td>
			<td><%=lianmeng.reputation%></td>
			<td><%=lianmeng.members%></td>
			<td><%=lianmeng.notice%></td>
			<td><%=lianmeng.build%></td>
			<td><%=memberMax%></td>
			<td><%=lianmeng.level%></td>
			<td><%=lianmeng.exp%></td>
			<td><%=lianmeng.hufuNum%></td>
			<td><%=remainTime%></td>
			<td><%=lianmeng.upgradeUsedTimes%></td>
			<td><%=country%></td>
			<!-- 超链接传递参数 &两边不能加空格 -->
			<td><a
				href="lmmemberlist.jsp?id=<%=lianmeng.id%>&name=<%=lianmeng.name%>">查看</a>
			</td>
			<%
					} else//显示所有联盟信息
					{

						List<AllianceBean> list = HibernateUtil.list(
								AllianceBean.class, "");
						if (list != null) {
							for (AllianceBean alnc : list) {
								if(alnc.creatorId % 1000 != GameServer.serverId)
									continue;
								out.append("<tr><td>");
								out.append("" + alnc.id);
								out.append("</td>");
								out.append("<td>");
								out.append("" + alnc.name);
								out.append("</td>");
								out.append("<td>");
								out.append("" + alnc.icon);
								out.append("</td>");
								out.append("<td>");
								out.append("" + alnc.creatorId);
								out.append("</td>");
								out.append("<td>");
								out.append("" + alnc.reputation);
								out.append("</td>");
								out.append("<td>");
								out.append("" + alnc.members);
								out.append("</td>");
								out.append("<td>");
								out.append("" + alnc.notice);
								out.append("</td>");
								out.append("<td>");
								out.append("" + alnc.build);
								out.append("</td>");
								out.append("<td>");
								int memberMax = AllianceMgr.inst.getAllianceMemberMax(alnc.level);
								out.append("" + memberMax);
								out.append("</td>");
								out.append("<td>");
								out.append("" + alnc.level);
								out.append("</td>");
								out.append("<td>");
								out.append("" + alnc.exp);
								out.append("</td>");
								out.append("<td>");
								out.append("" + alnc.hufuNum);
								out.append("</td>");
								out.append("<td>");
								Date date = new Date();
								int remainTime = alnc.upgradeTime == null ? -1 : (int)((alnc.upgradeTime.getTime() - date.getTime()) / 1000);
								out.append("" + remainTime);
								out.append("</td>");
								out.append("<td>");
								out.append("" + alnc.upgradeUsedTimes);//加速升级剩余次数
								out.append("</td>");
								out.append("<td>");
								String country = HeroService.getNameById(alnc.country+"");
								out.append(country);
								out.append("</td>");
								out.append("<td>");
								out.append("<a href=lmmemberlist.jsp?id=" + alnc.id + "&name=" + alnc.name+ ">查看</a>");
								out.append("</td>");
								out.append("<td><a href=lmAppliesList.jsp?id=" + alnc.id + "&name=" + alnc.name + ">查看</a></td>");
								out.append("<td>" + alnc.isAllow +"</td>");
								out.append("<td><a href=allianceInfo.jsp?id=" + alnc.id + "&jzId="+alnc.creatorId+"&action=delAlnc" + ">删除</a></td>");
								out.append("<td><input type='button' value='联盟改名' onclick='changeAlncName(this)'/></td>");

							}
						}
					}
					out.append("</table>");
				%>
		</tr>
	</table>

</body>
</html>