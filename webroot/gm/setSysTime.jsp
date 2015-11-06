<%@page import="java.util.Date"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.io.IOException"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.qx.util.MySysTime"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%
	request.setCharacterEncoding("utf-8"); 
	response.setCharacterEncoding("utf-8");
	Long timeCount=MySysTime.getInstance().getTimeCount();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String action = request.getParameter("action");
	String time = request.getParameter("time");
%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>设置系统时间</title>
</head>
<body>
<%	
	
		
			if("setSysTime".equals(action)) {
				//out.println("设置时间开始--------------------");
				//out.println("设定的时间为"+time);
				Date now  =new Date();
				//out.println("现在的时间为"+sdf.format(now));
				Date setTime=sdf.parse(time);
				long addCount=now.getTime()-setTime.getTime();
				//out.println("原时间间隔为"+timeCount);
				//out.println("增加时间间隔为"+addCount);
				timeCount=timeCount+addCount;
				//out.println("设置后时间间隔为"+timeCount);
				 try {  
					 String cmd = "/bin/date -s '" +time+"'";
					 String[] comands = new String[] { "/bin/sh", "-c", cmd };
							 Process p = Runtime.getRuntime().exec(comands);
							 int code = p.waitFor();
				MySysTime.getInstance().setTimeCount(timeCount);
					//out.println("设置时间结束--------------------"+code);
				 } catch (IOException e) {  
					 e.printStackTrace();  
				 }

			} else if("resetTime".equals(action)){
				if(!"".equals(timeCount)){
					//out.println("重置时间开始~~~~~~~~~~~~~~~~~~~~~~");
					Date now  =new Date();
					//out.println("现在的时间为"+sdf.format(now));
					//out.println(sdf.format(now.getTime()));
					//out.println("时间参数为"+timeCount);
					String resetDate=sdf.format(now.getTime()+timeCount);
					//out.println("重置时间后，时间为"+resetDate);
	 				try {  
	 					String cmd = "/bin/date -s '" +resetDate+"'";
						String[] comands = new String[] { "/bin/sh", "-c", cmd };
						Process p = Runtime.getRuntime().exec(comands);
						int code = p.waitFor();
						MySysTime.getInstance().setTimeCount(0);
						//out.println("重置时间结束~~~~~~~~~~~~~~~~~~~~~~"+code);
					 	} catch (IOException e) {  
					 		e.printStackTrace();  
					 }
				}
			}
				%>
    <form action="">
        时间参数为<input type="text"  readonly="readonly" name="timeCount" value="<%=MySysTime.getInstance().getTimeCount()%>">
        <br> 时间设置为<input type="text" name="time" value="<%=sdf.format(new Date())%>">
       <br> 
      	操作类型为<select name="action" >
 	 	 <option value="setSysTime">修改时间</option>
 		 <option value="resetTime">重置时间</option>
	 </select> 
	 <br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<button type="submit">提交</button>
    </form>

</body>
</html>