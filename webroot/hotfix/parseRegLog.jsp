<%@page import="com.qx.persistent.HibernateUtil"%>
<%@page import="log.DBHelper"%>
<%@page import="java.io.File"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="log.Log2DBBean"%>
<%@page import="log.Log2DB"%>
<%@page import="java.io.FileWriter"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.util.Date"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="net.sf.json.JSONObject" %>
<%@include file="../myFuns.jsp" %>
<%!
Logger log = LoggerFactory.getLogger("parseRegLog.jsp");
public String save2db(final String file){
	//String file = logDir +"/"+ which;
	String which = file.substring(file.lastIndexOf("/")+1);
	File f = new File(file);
	if(f.exists() == false){
		return "fail:文件不存在";
	}
	String table = which.substring(0,which.indexOf("."));
    DBHelper db = null;
    String sql = "load data local infile '"+file+"' "
    		+ "into table `"+table+"` "
    		+ "fields terminated by ','";// enclosed by '\\'' lines terminated by '\\r\\n'";
    log.info("准备执行 {}",sql);
    try {
    	db = new DBHelper(sql);
    	if(db.conn == null || db.pst == null){
    		return "fail:数据库连接异常";
    	}else{
        	db.pst.execute();
        	int cnt = db.pst.getUpdateCount();
 
            log.info("{} into {} 执行结果完毕 cnt {}", file, table, cnt);
            //
            return "OK:"+cnt;
    	}
    }catch(Exception e){
    	log.error("执行出错.",e);
    	return "fail:"+e;
    } finally {
        if(db != null )db.close();
    }
}
%>
<% //日志里处理request parameter没有做编码处理，导致不能导入到数据库
String file = "/data/wushuang/test-server/logs_router/PlayerRegister.2015-08-25_15.log";
String id = request.getParameter("id");
Log2DBBean bean = null;
if(id != null){
	bean = HibernateUtil.find(Log2DBBean.class, Long.parseLong(id));
	if(bean == null){
		out("没有找到"+id);
		return;
	}
	file = bean.fileName;
}
String fileUtf = file.replace(".log", "utf.log");
if(bean != null && bean.result.equals("数据库连接异常")){
	out("重新导入：");
	fileUtf = bean.fileName;
}else{
String timeTag = fileUtf.substring(fileUtf.indexOf(".")+1, fileUtf.lastIndexOf("."));
FileWriter fw = new FileWriter(fileUtf);
FileReader fr = new FileReader(file);
try{
	BufferedReader br = new BufferedReader(fr);
	tableStart();
	int row = 0;
	do{
		row++;
		String line = br.readLine();
		if(line == null)break;
		trS();
		td(row);
//		String arr[] = line.split(",");
		String utf =new String(line.getBytes("ISO-8859-1"),"UTF-8");
		//utf = native2Ascii(utf);
		utf = utf.replace("\"", "~");
		utf = utf.replaceAll("[^\\u4E00-\\u9FA5\\p{ASCII}]+", "?");
//		fw.write(utf);
		String arr[] = utf.split(",");
		int len = arr.length;
		for(String s:arr){
			tdS();
			out(s);
			tdE();
		}
		fw.write(utf);
		fw.write("\n");
		trE();
	}while(true);
	tableEnd();
}catch(Exception e){
	e.printStackTrace();
}
fr.close();
fw.close();
out("utf file:"+fileUtf);br();
}
if(bean != null){
	String loadRet = save2db(fileUtf);
	out("load ret:"+loadRet);br();
	if(loadRet.startsWith("OK")){
		bean.result = "fix ok";
		bean.rowCnt = Integer.parseInt(loadRet.split(":")[1]);
		HibernateUtil.save(bean);
		out("update bean ok");br();
	}
}
%>