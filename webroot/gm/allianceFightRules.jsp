<%@page import="java.util.List"%>
<%@page import="com.qx.alliancefight.AllianceFightRules"%>
<%@page import="com.qx.persistent.HibernateUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@include file="/myFuns.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<script type="text/javascript">
	function go(act){
		var v = document.getElementById(act).value;
		location.href = '?action='+act+"&v="+v;
	}
</script>
</head>
<body>
	<a href="allianceFight.jsp">返回联盟战管理页面</a> <br/><br/><br/>
	<form action="allianceFightRules.jsp">
		规则名称：<input  type="text" name="ruleName"/>规则值：<input type="text" name="ruleValue"/>
		<input type="hidden" name="action" value="addRule"/>
		<input type="submit" value="添加"/>
	</form>
	<br/><br/>
	<%
		String action = request.getParameter("action");
		if(action != null) {
			if(action.equals("addRule")) {
				String ruleName = request.getParameter("ruleName");
				String ruleValue = request.getParameter("ruleValue");
				if(ruleName == null || ruleName.equals("") || ruleValue == null || ruleValue.equals("")) {
					out.println("输入的内容不能为空");
				}
				AllianceFightRules fightRules = new AllianceFightRules(ruleName, ruleValue); 
				HibernateUtil.save(fightRules);
			}
		}
		List<AllianceFightRules> ruleList = HibernateUtil.list(AllianceFightRules.class, "");
		if(ruleList == null || ruleList.size() == 0) {
			out.println("联盟站规则为空");
		} else {
			tableStart();
				trS();td("规则id");td("规则名称");td("规则值");trE();
				for(AllianceFightRules rule : ruleList) {
					trS();td(rule.getId());td(rule.getRuleName());td(rule.getRuleValue());trE();
				}
			tableEnd();
		}

	%>
</body>
</html>