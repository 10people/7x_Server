<%@page import="com.qx.account.Account"%>
<%@page import="com.qx.alliance.BigHouse"%>
<%@page import="com.qx.alliance.HouseBean"%>
<%@page import="com.qx.junzhu.JunZhu"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.alliance.AlliancePlayer"%>
<%@page import="com.qx.pvp.PvpBean"%>
<%@page import="java.util.List"%>
<%@include file="/myFuns.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src="../ajax.min.js"></script>
<script>
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
function houseOp(jzId, op){
	var url = 'houseOper.jsp?op='+op+'&jzId='+jzId;
	get(url, function(xhr){
		document.getElementById('preview').innerHTML = xhr.responseText;
	});
}
function getBigExperience(){
	var jzId =document.getElementById('bigjzId').value;
	var url = 'houseOper.jsp?op=getBigExperience&jzId='+jzId;
	get(url, function(xhr){
		document.getElementById('preview').innerHTML = xhr.responseText;
	});
}
//访客信息
function getAndchangeHVInfo(){
	var jzId =document.getElementById('vjzId').value;
	var url = 'houseOper.jsp?op=getAndchangeHVInfo&jzId='+jzId;
	get(url, function(xhr){
		document.getElementById('preview').innerHTML = xhr.responseText;
	});
}
function clearPrUpTime(jzId){
	var url = 'houseOper.jsp?op=clearPrUpTime&jzId='+jzId;
	get(url, function(xhr){
		document.getElementById('preview').innerHTML = xhr.responseText;
	});
}
var upHouseTime="";
function setUpHouseTime(obj){
	upHouseTime=obj.value;
}
function changePrUpTime(jzId){
	var url = 'houseOper.jsp?op=changePrUpTime&jzId='+jzId+'&upHouseTime='+upHouseTime;
	get(url, function(xhr){
		document.getElementById('preview').innerHTML = xhr.responseText;
		upHouseTime="";
	});
}

function agreeEx(keeperJzId, buyer){
	var url = 'houseOper.jsp?op=agreeEx&buyer='+buyer+"&keeper="+keeperJzId;
	get(url, function(xhr){
		document.getElementById('preview').innerHTML = xhr.responseText;
	});
}
function rejectEx(keeperJzId, buyer){
	var url = 'houseOper.jsp?op=rejectEx&buyer='+buyer+"&keeper="+keeperJzId;
	get(url, function(xhr){
		document.getElementById('preview').innerHTML = xhr.responseText;
	});
}
function applyEx(keeperJzId){
	var buyer = document.getElementById('buyer').value;
	//console.info(keeperJzId+":"+buyer);
	var url = 'houseOper.jsp?op=applyEx&buyer='+buyer+"&keeper="+keeperJzId;
	get(url, function(xhr){
		document.getElementById('preview').innerHTML = xhr.responseText;
	});
}
function viewApply(keeperJzId){
	var url = 'houseOper.jsp?op=viewApply&keeper='+keeperJzId;
	get(url, function(xhr){
		document.getElementById('preview').innerHTML = xhr.responseText;
	});
}
function showExReqInput(keeperJzId){
	var str="请输入买家ID<input type='text' id='buyer'/>";
	str += "<input type='button' value='发起申请' onclick='applyEx("+keeperJzId+")'/>";
	document.getElementById('preview').innerHTML = str;
}
function set2sell(jzId,location, lmId){
	var url = 'houseOper.jsp?op=set2sell&lmId='+lmId+"&jzId="+jzId+"&location="+location;
	get(url, function(xhr){
		document.getElementById('preview').innerHTML = xhr.responseText;
	});
}
function exchangeBHW(){
	var str="请玩家ID<input type='text' id='buyer'/>";
	str += "<input type='button' value='发起申请' onclick='exchangeBigHouseWorth()'/>";
	document.getElementById('preview').innerHTML = str;
}
function exchangeBigHouseWorth(){
	var buyer = document.getElementById('buyer').value;
	var url = 'houseOper.jsp?op=exchangeBHW&buyer='+buyer;
	get(url, function(xhr){
		document.getElementById('preview').innerHTML = xhr.responseText;
	});
}
function exchangeEh(){
	var str="请输入买家ID<input type='text' id='buyer'/><br>";
	str +="请输入空房子位置<input type='text' id='elocation'/>";
	str += "<input type='button' value='发起申请' onclick='exchangeEmptyhouse()'/>";
	document.getElementById('preview').innerHTML = str;
}
function exchangeEmptyhouse(){
	var buyer = document.getElementById('buyer').value;
	var elocation = document.getElementById('elocation').value;
	var url = 'houseOper.jsp?op=exchangeEh&buyer='+buyer+"&elocation="+elocation;
	get(url, function(xhr){
		document.getElementById('preview').innerHTML = xhr.responseText;
	});
}
function addGx(jzId, lmId){
	var url = 'houseOper.jsp?op=addGx&lmId='+lmId+"&jzId="+jzId;
	var input = document.getElementById("addGxInput");
	var cnt = 10;
	if(input){
		cnt = input.value;
		url += '&cnt='+cnt;
	}else{
		var str = "请在此输入数量:<input type='text' value='9' id='addGxInput'/>";
		document.getElementById('preview').innerHTML = str;
		return;
	}
	get(url, function(xhr){
		document.getElementById('gx'+jzId).innerHTML = xhr.responseText;
	});
}
function giveHouse(jzId,lmId){
	var xhr = createXHR();
	xhr.onreadystatechange = function()
	{
	    if (xhr.readyState === 4)
	    {
	        document.getElementById('preview').innerHTML = xhr.responseText;
	    }
	};
	xhr.open('GET', 'houseOper.jsp?lmId='+lmId+"&jzId="+jzId, true);
	xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
	xhr.send();
}
function showExBigHouseReqInput(targetJzId){
	var str="请输入买家ID<input type='text' id='buyerId'/>";
	str += "<input type='button' value='发起申请' onclick='paiBigHouse("+targetJzId+")'/>";
	document.getElementById('preview').innerHTML = str;
}
function paiBigHouse(targetJzId){
	var buyerId = document.getElementById('buyerId').value;
	var url = "houseOper.jsp?op=paiBigHouse&targetJzId="+targetJzId+"&buyerId="+buyerId;
	get(url, function(xhr){
		document.getElementById('preview').innerHTML = xhr.responseText;
	});
}
function getBatchInfo(){
	var url = "houseOper.jsp?op=getBatchInfo";
	get(url, function(xhr){
		document.getElementById('preview').innerHTML = xhr.responseText;
	});
}
</script>
<title>显示联盟成员列表</title>
</head>
<body>
<a href=''>刷新</a>
<%! String getStateName(int s){
	switch(s){
	case HouseBean.ForSell:return "待售";
	case HouseBean.ForUse:return "自住";
	case HouseBean.Drop:return "荒废";
	case HouseBean.KingSell:return "强售";
	}
	return ""+s;
}
%>
<%
String s=request.getParameter("id");
String guildName = new String(request.getParameter("name").getBytes("ISO8859-1"), "UTF-8");
out.print(s+":"+guildName);
%>
	显示联盟成员列表
	<br/>
	备注:成员身份：0-成员，1-副盟主，2-盟主<br/>
	<table align="left" border="1">
		<tr>
			<td>账号</td>
			<td>成员ID</td>
			<td>成员名称</td>
			<td>成员等级</td>
			<td>成员贡献</td>
			<td>成员军衔</td>
			<td>成员身份</td>
			<td>房屋坐标ID</td>
			<td>第一任主人</td>
			<td>第一任主人入住时间</td>
		</tr>
		<%
			if (s != null) {
				int guildId=Integer.parseInt(s);
				List<AlliancePlayer> list = HibernateUtil.list(
						AlliancePlayer.class, "where lianMengId= " + guildId);
				if (list != null) {
					for (AlliancePlayer ap : list) {
						JunZhu jz = HibernateUtil.find(JunZhu.class, ap.junzhuId);
						PvpBean pvpBean = HibernateUtil.find(PvpBean.class, jz.id);
						out.print("<tr><td>");
						Account acc = HibernateUtil.find(Account.class, jz.id/1000);
						if(acc == null){
							out("没有找到帐号，accId:"+jz.id/1000);							
						} else {
							out(acc.accountName);
						}
						tdE();
						tdS();
						out.print("" + ap.junzhuId);
						out.print("</td>");
						out.print("<td>");
						out.print("" + jz.name);
						out.print("</td>");
						out.print("<td>");
						out.print("" + jz.level);
						out.print("</td>");
						out.print("<td>");
						String addGx = "<input type='button' value='增加' onclick='addGx(xxx)'/>";
						addGx = addGx.replace("xxx", ap.junzhuId+","+s);
						out.print("<div style='float:left' id='gx"+ap.junzhuId+"'>" + ap.gongXian+"</div>"+addGx);
						out.print("</td>");
						out.print("<td>");
						out.print("" + (pvpBean == null ? -1 : pvpBean.junXianLevel));
						out.print("</td>");
						out.print("<td>");
						out.print("" + ap.title);
						out.print("</td>");
						HouseBean bean = HibernateUtil.find(HouseBean.class, ap.junzhuId);
						if(bean == null){
							String btn = "<input type='button' value='分配' onclick='giveHouse(xxx)'/>";
							btn = btn.replace("xxx", ap.junzhuId+","+s);
							td(btn);
						}else{
							String btn = "";
							if(bean.state == HouseBean.ForUse){
								btn = "<input type='button' value='待售' onclick='set2sell(xxx,yyy)'/>";
								btn = btn.replace("xxx", ap.junzhuId+"");
								btn = btn.replace("yyy", bean.location+","+s);
							}else if(bean.state == HouseBean.ForSell||bean.state == HouseBean.Drop){
								btn = "<input type='button' value='申请' onclick='showExReqInput(xxx)'/>"
										+"<input type='button' value='查看申请列表' onclick='viewApply(xxx)'/>";
								btn = btn.replace("xxx", ap.junzhuId+","+s);
							}
							String preUpTime = bean.preUpTime == null ? "" : 
								"<input type='button' value='上次装修' onclick='clearPrUpTime(xxx)'/>"+bean.preUpTime.toLocaleString();
							preUpTime = preUpTime.replace("xxx", bean.jzId+"");
							String changeUpTime = bean.preUpTime == null ? "" : 
								"<input type='button' value='修改上次装修时间' onclick='changePrUpTime(xxx)'/>"+
								"<input type='text'value='" +bean.preUpTime.toLocaleString()+"'onchange='setUpHouseTime(this)'/>";
								changeUpTime = changeUpTime.replace("xxx", bean.jzId+"");
							td(bean.location+"-"+getStateName(bean.state)+(bean.open?"-开":"-关")+btn+preUpTime+changeUpTime);
						}
						out.print("<td>");
						out.print("" +bean.firstOwner);
						out.print("</td>");
						out.print("<td>");
						out.print("" +bean.firstHoldTime);
						out.print("</td>");
						out.print("</tr>");
					}
				}else{
					out.print("该联盟无成员");
				}
			}else{
				out.print("kong000000000000");
			}
		%>
	</table>
	<div id='preview'>
		-
	</div>
	<div style='clear:both'></div>
	<br/>
	<%
	//================
if (s != null) {
	int guildId=Integer.parseInt(s);
	out("大房信息：");
	List<BigHouse> blist = HibernateUtil.list(BigHouse.class, "where lmId="+guildId);
	tableStart();
	ths("玩家ID,贡献,位置,价值,前任主人,上次价值,第一入住时间,上次入住时间,是否可进");
	for(BigHouse bh : blist){
		trS();
		td(bh.jzId);
		td(bh.gongXian);
		String btn = "<input type='button' value='申请' onclick='showExBigHouseReqInput(xxx)'/>";
		btn = btn.replace("xxx",bh.jzId+"");
		td(bh.location+btn);
		td(bh.hworth);
		td(bh.previousId);
		td(bh.previousWorth);
		td(bh.firstHoldTime);
		td(bh.previousHoldTime);
		td(bh.open);
		trE();
	}
// 	if(blist.size()==0){
		out("<input type='button' value='重新分配大房' onclick='houseOp("+guildId+",\"fenPeiBigHouse\");'/>");
// 	}
	out("<br/><input type='text' id='bigjzId'><input type='button' value='领大房经验' onclick='getBigExperience();'/>");
	out("<br/><input type='text' id='vjzId'><input type='button' value='获取访客信息' onclick='getAndchangeHVInfo();'/>");
	out("<br/><input type='button' value='与空房子交换' onclick='exchangeEh();'/>");
	out("<br/><input type='button' value='衰减高级房屋价值' onclick='exchangeBHW();'/>");
	out("<br/><input type='button' value='获取房屋信息' onclick='getBatchInfo();'/>");
	tableEnd();
}
	%>
</body>
</html>