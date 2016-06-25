<%@page import="com.manu.dynasty.chat.SensitiveFilter"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.io.BufferedInputStream"%>
<%@page import="com.qx.account.AccountManager"%>
<%@page import="java.io.File"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<%
out.append("pre:"+AccountManager.inst.sensitiveWord.size());

String headPath = AccountManager.class.getClassLoader()
.getResource("/").getPath();
AccountManager.inst.initSensitiveWordAndIllegalityName();
List<String> sensitiveWord = new ArrayList<String>(AccountManager.inst.sensitiveWord);
File f = new File(headPath + "/pbc2.txt");

	BufferedInputStream br = 
			new BufferedInputStream(new FileInputStream(f));
	BufferedReader r = new BufferedReader(new InputStreamReader(br, "UTF-8"));
	do{
		String line = r.readLine();
		if(line == null)break;
		String arr[] = line.split("	");
		for(String s : arr){
			s = s.trim();
			if(s.isEmpty())continue;
			sensitiveWord.add(s);
		}
	}while(true);
AccountManager.inst.sensitiveWord = sensitiveWord;
SensitiveFilter.getInstance().initKeyWord();
/*
*/
out.append("<br/>after:"+AccountManager.inst.sensitiveWord.size());
%>
</body>
</html>