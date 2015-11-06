<%@page import="com.manu.dynasty.template.ZhuXian"%>
<%@page import="com.qx.task.GameTaskMgr"%>
<%@page import="com.manu.dynasty.base.TempletService"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.manu.dynasty.util.DataLoader"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<html>
<head>
<title>upFile</title>
</head>
<body bgcolor="#ffffff">
	<%
		//定义上载文件的最大字节
		int MAX_SIZE = 102400 * 102400;
		// 创建根路径的保存变量
		String rootPath;
		//声明文件读入类
		DataInputStream in = null;
		FileOutputStream fileOut = null;
		//取得客户端的网络地址
		String remoteAddr = request.getRemoteAddr();
		//获得服务器的名字
		String serverName = request.getServerName();

		//取得互联网程序的绝对地址
		String realPath = request.getRealPath(serverName);
		realPath = realPath.substring(0, realPath.lastIndexOf(File.separator));
		//创建文件的保存目录
		rootPath = realPath + File.separator+"upload"+File.separator;
		//取得客户端上传的数据类型
		String contentType = request.getContentType();
		try {
			if (contentType.indexOf("multipart/form-data") >= 0) {
				//读入上传的数据
				in = new DataInputStream(request.getInputStream());
				int formDataLength = request.getContentLength();
				if (formDataLength > MAX_SIZE) {
					out.println("<p> 上传的文件字节数不可以超过" + MAX_SIZE + "</p>");
					return;
				}
				//保存上传文件的数据
				byte dataBytes[] = new byte[formDataLength];
				int byteRead = 0;
				int totalBytesRead = 0;
				//上传的数据保存在byte数组
				while (totalBytesRead < formDataLength) {
					byteRead = in.read(dataBytes, totalBytesRead,
							formDataLength);
					totalBytesRead += byteRead;
				}
				//根据byte数组创建字符串
				String file = new String(dataBytes);
				//out.println(file);
				//取得上传的数据的文件名
				String saveFile = file.substring(file
						.indexOf("filename=\"") + 10);
				saveFile = saveFile.substring(0, saveFile.indexOf("\n"));
				saveFile = saveFile.substring(
						saveFile.lastIndexOf("\\") + 1,
						saveFile.indexOf("\""));
				int lastIndex = contentType.lastIndexOf("=");
				//取得数据的分隔字符串
				String boundary = contentType.substring(lastIndex + 1,
						contentType.length());
				if(!saveFile.endsWith(".xml")){
					out.println("只能上传xml文件。"+saveFile);
					return;
				}
				String time = new SimpleDateFormat("yyyy-MMdd-HHmmss").format(new Date());
				saveFile = saveFile.replace(".xml", time+".xml");
				//创建保存路径的文件名
				String fileName = rootPath + saveFile;
				out.print("保存在"+fileName+"<br/>");
				int pos;
				pos = file.indexOf("filename=\"");
				pos = file.indexOf("\n", pos) + 1;
				pos = file.indexOf("\n", pos) + 1;
				pos = file.indexOf("\n", pos) + 1;
				int boundaryLocation = file.indexOf(boundary, pos) - 4;
				//out.println(boundaryLocation);
				//取得文件数据的开始的位置
				int startPos = ((file.substring(0, pos)).getBytes()).length;
				//out.println(startPos);
				//取得文件数据的结束的位置
				int endPos = ((file.substring(0, boundaryLocation))
						.getBytes()).length;
				//out.println(endPos);
				//检查上载文件是否存在
				File checkFile = new File(fileName);
				if (checkFile.exists()) {
					out.println("<p>" + saveFile + "文件已经存在.</p>");
				}else{
					//检查上载文件的目录是否存在
					File fileDir = new File(rootPath);
					if (!fileDir.exists()) {
						fileDir.mkdirs();
					}
					//创建文件的写出类
					fileOut = new FileOutputStream(fileName);
					//保存文件的数据
					fileOut.write(dataBytes, startPos, (endPos - startPos));
					fileOut.close();
					out.println(saveFile + "文件成功上载.</p>");
				}
				//
				ByteArrayInputStream bis = new ByteArrayInputStream(dataBytes, startPos, (endPos - startPos));
				DataLoader dl = new DataLoader("com.manu.dynasty.template.","");
				List list = dl.loadFromStream(bis);
				out.println("<br/>解析到的对象个数:"+list.size()+"<br/>");
				if(list.size()>0){
					Object o = list.get(0);
					String key = o.getClass().getSimpleName();
					out.println(key);
					TempletService.templetMap.put(key, list);
					TempletService.getInstance().afterLoad();
					if(key.equals(ZhuXian.class.getSimpleName())){
						GameTaskMgr.inst.initData();
					}
				}
				bis.close();
				out.println("<br/>看到这行文字，新配置应该生效了！");
			} else {
				String content = request.getContentType();
				out.println("<p>上传的数据类型不是multipart/form-data</p>");
			}
		} catch (Exception ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			out.print("<br/>出错:"+sw.toString());
		}
	%>
	<br/>
	<a href='dataTemplate.jsp'>返回</a>
</body>
</html>