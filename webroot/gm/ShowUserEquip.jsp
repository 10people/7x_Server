<%@page import="com.qx.equip.domain.EquipXiLian"%>
<%@page import="com.qx.bag.EquipGrid"%>
<%@page import="com.manu.dynasty.template.CanShu"%>
<%@page import="qxmobile.protobuf.UserEquipProtos.XiLianRes"%>
<%@page import="com.qx.equip.web.UserEquipAction"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="com.qx.equip.domain.UserEquip"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>显示用户装备</title>
<script type="text/javascript">
// function go(){
// 	var v = document.getElementById("yuanbaoxilian").value;
// 	var href= location.href;
// 	location.href = href+"&v="+v;
// }

</script>
</head>
<body>
	<%
// 		String v = request.getParameter("v");
// 		if (v != null) {
// 			int xilianzhi4yuanbao = Integer.parseInt(v);
// 			CanShu.XILIANZHI_YUANBAO = xilianzhi4yuanbao;
// 		}
		String s = request.getParameter("instId");
	%>
	<form action="">
<%-- 	<input type='text' id='yuanbaoxilian' value='<%=CanShu.XILIANZHI_YUANBAO %>'/> --%>
<!-- 	<input type='button' value='修改' onclick='go()'/> -->
		<table border="1" align="left">
			<tr>
<!-- 				<td>equipId</td> -->
<!-- 				<td>exp</td> -->
<!-- 				<td>强化等级</td> -->
<!-- 				<td>templateId</td> -->
<!-- 				<td>君主ID</td> -->
<!-- 				<td>谋力</td> -->
<!-- 				<td>统帅</td> -->
<!-- 				<td>武艺</td> -->
				
<!-- 				<td>武器伤害</td> -->
<!-- 				<td>武器减免</td> -->
<!-- 				<td>武器暴击</td> -->
<!-- 				<td>武器抵抗</td> -->
<!-- 				<td>技能伤害</td> -->
<!-- 				<td>技能减免</td> -->
<!-- 				<td>技能暴击</td> -->
<!-- 				<td>技能抵抗</td> -->
					<td>EquipGridID       </td>
					<td>zhuangbeiID   </td>
<!-- 					<td>统帅     </td> -->
<!-- 					<td>武艺          </td> -->
<!-- 					<td>谋力        </td> -->
					<td>洗练所需元宝       </td>
					<td>元宝剩余洗练次数 </td>
					<td>免费洗练剩余次数 </td>
					<td>洗练值</td> 
					<td colspan=2>免费洗练定时请求倒计时时长。单位：秒          </td>
				
			</tr>
			<%
			if (s != null) {
				Long dbId = Long.parseLong(s);
				int xilianzhi=-1;
				EquipGrid eg=HibernateUtil.find(EquipGrid.class,dbId);
				
				long instId=eg.instId;
				UserEquip dbUe = HibernateUtil.find(UserEquip.class, instId);
				if(dbUe!=null){
					xilianzhi=dbUe.xianlianzhi;
				}
				XiLianRes.Builder ret=UserEquipAction.instance.getXiLianInfoByInstId(dbId);
				EquipXiLian equipXiLian = HibernateUtil.find(EquipXiLian.class,
						" where equipId=" + eg.dbId + " and junZhuId=" +dbUe.userId);
// 				out.print("&nbsp;equipXiLian:" + (equipXiLian!=null?equipXiLian.getEquipId():equipXiLian));
			%>
			<tr>
			<td><%=ret.getEquipId()          %><%=equipXiLian==null?"上次洗练结果已确认":"上次洗练结果未确认"%></td>
			<td><%=ret.getZhuangbeiID()         %></td>   
<%-- 			<td><%=ret.getTongShuai()           %></td> --%>
<%-- 			<td><%=ret.getWuYi()                %></td> --%>
<%-- 			<td><%=ret.getZhiMou()              %></td> --%>
			<td><%=ret.getYuanBao()             %></td>
			<td><%=ret.getYuanBaoTimes()        %></td>
			<td><%=ret.getFreeXilianTimes()     %></td>
			<td><%=xilianzhi   %></td>
			<td  colspan=2><%=ret.getTime()                %></td>
<!-- 			<tr> -->
<!-- 					<td>tongShuaiAdd  </td> -->
<!-- 					<td>wuYiAdd       </td> -->
<!-- 					<td>zhiMouAdd     </td> -->
				
<!-- 					<td>tongShuaiMax  </td> -->
<!-- 					<td>wuYiMax       </td> -->
<!-- 					<td>zhiMouMax     </td> -->
<!-- 			</tr> -->
<!-- 			<tr> -->
<%-- 				<td><%=ret.getTongShuaiAdd()        %></td> --%>
<%-- 				<td><%=ret.getWuYiAdd()             %></td> --%>
<%-- 				<td><%=ret.getZhiMouAdd()           %></td> --%>
			
<%-- 				<td><%=ret.getTongShuaiMax()        %></td> --%>
<%-- 				<td><%=ret.getWuYiMax()             %></td> --%>
<%-- 				<td><%=ret.getZhiMouMax()           %></td> --%>
<!-- 			</tr> -->
				<tr>
					<td>属性洗练增加数值最小值</td>
					<td>属性洗练增加数值最大值</td>
					<td>武器暴击率</td>
					<td>技能暴击率    </td>
					<td>武器免暴率   </td>
					<td>技能免暴率     </td>
					<td  colspan=2>技能冷却缩减    </td>
			
			</tr>
			<tr>
			<td><%=ret.getAddMin()              %></td>
			<td><%=ret.getAddMax()              %></td>
			<td><%=ret.getWqBJL()               %></td>
			<td><%=ret.getJnBJL()               %></td>
			<td><%=ret.getWqMBL()               %></td>
			<td><%=ret.getJnMBL()               %></td>
			<td  colspan=2><%=ret.getJnCDReduce()          %></td>
		
			</tr>
			 <tr>
			<td colspan=8 style="border: solid  red">**************************************************************************************分割线********************************************************************************</td>
		
			</tr>
		   
		    <tr >
		    
		  	<td >武器伤害加深          </td>
			<td>武器伤害减免          </td>
			<td >武器暴击加深          </td>
			<td>武器暴击加深          </td>
			<td >技能伤害加深          </td>
			<td>技能伤害减免          </td>
			<td >技能暴击加深          </td>
			<td>技能暴击减免          </td>
			</tr>
			<tr >
			<td ><%=ret.getWqSH()                %></td>
			<td><%=ret.getWqJM()                %></td>
			<td ><%=ret.getWqBJ()                %></td>
			<td ><%=ret.getWqRX()                %></td>
			<td  ><%=ret.getJnSH()                %></td>
			<td><%=ret.getJnJM()                %></td>
			<td  ><%=ret.getJnBJ()                %></td>
			<td><%=ret.getJnRX()                %></td>
			</tr>                                                           
			<tr >
			<td >属性洗练值     </td>    <!--（武器伤害加深）-->
			<td>属性洗练值        </td> <!--（武器伤害减免）-->
			<td >属性洗练值        </td> <!--（武器暴击加深）-->
			<td>属性洗练值        </td> <!--（武器暴击加深）-->
			<td >属性洗练值        </td> <!--（技能伤害加深）-->
			<td>属性洗练值        </td> <!--（技能伤害减免）-->
			<td >属性洗练值        </td> <!--（技能暴击加深）-->
			<td>属性洗练值        </td> <!--（技能暴击减免）-->
			
			</tr>
			
			<tr >
				<td ><%=ret.getWqSHAdd()             %></td>
				<td ><%=ret.getWqJMAdd()             %></td>
				<td ><%=ret.getWqBJAdd()             %></td>
				<td><%=ret.getWqRXAdd()             %></td>
				<td > <%=ret.getJnSHAdd()             %></td>
				<td><%=ret.getJnJMAdd()             %></td>
				<td  ><%=ret.getJnBJAdd()             %></td>
				<td><%=ret.getJnRXAdd()             %></td>
			
			</tr>
			<tr >
			<td >属性值能够洗练最大值</td><!--（武器伤害加深） -->
			<td>属性值能够洗练最大值</td><!--（武器伤害减免） -->
			<td >属性值能够洗练最大值</td><!--（武器暴击加深） -->
			<td>属性值能够洗练最大值</td><!--（武器暴击加深） -->
			<td >属性值能够洗练最大值</td><!--（技能伤害加深） -->
			<td>属性值能够洗练最大值</td><!--（技能伤害减免） -->
			<td >属性值能够洗练最大值</td><!--（技能暴击加深） -->
			<td >属性值能够洗练最大值</td><!--（技能暴击减免） -->
			
			</tr>
			<tr>
			<td ><%=ret.getWqSHMax()             %></td>
			<td><%=ret.getWqJMMax()             %></td>
			<td ><%=ret.getWqBJMax()             %></td>
			<td><%=ret.getWqRXMax()             %></td>
			<td ><%=ret.getJnSHMax()             %></td>
			<td><%=ret.getJnJMMax()             %></td>
			<td ><%=ret.getJnBJMax()             %></td>
			<td><%=ret.getJnRXMax()             %></td>
			  
			</tr>
			
			<tr>
			<td colspan=8 style="border: solid  red">**************************************************************************************分割线********************************************************************************</td>		
			</tr>
			
			<tr>
			<td>宝石插槽1</td>
			<td>宝石插槽2</td>
			<td>宝石插槽3</td>
			<td>宝石插槽4</td>
			<td>宝石插槽5</td>
			</tr>
			<tr>
			<td><%=dbUe.Jewel0%></td>
			<td><%=dbUe.Jewel1%></td>
			<td><%=dbUe.Jewel2%></td>
			<td><%=dbUe.Jewel3%></td>
			<td><%=dbUe.Jewel4%></td>
			</tr>
		</table>
	</form>
	<%
		}
	%>
</body>
</html>