<%@page import="com.qx.robot.RobotSession"%>
<%@page import="org.apache.mina.core.session.IoSession"%>
<%@page import="java.io.IOException"%>
<%@page import="java.io.Writer"%>
<%!
Writer out;
HttpServletRequest req;
protected void setOut(Writer w){
	out = w;
}
void out(Object s) throws IOException{
	out.append(s.toString());
}
void td(Object s) throws IOException{
	if(s == null)s="null";
	out.append("<td>");
	out.append(s.toString());
	out.append("</td>");
}
void tdS() throws IOException{
	out.append("<td>");
}
void tdE() throws IOException{
	out.append("</td>");
}
void trS() throws IOException{
	out.append("<tr>");
}
void trE() throws IOException{
	out.append("</tr>");
}
void tableEnd() throws IOException{
	out.append("</table>");
}
void tableStart() throws IOException{
	out.append("<table border='1' style='border-collapse:collapse;'>");
}
void space()throws IOException{
	out.append("&nbsp;");
}

void input(String id, String value)throws IOException{
	out("<input type='text' id='");out(id);out("' value='");out(value);out("'/>");
}
String button(String text, String click){
	return "<input type='button' value='"+text+"' onclick=\""+click+"\"/>";
}
void br()throws IOException{
	out.append("<br/>");
}
void ths(String s) throws IOException{
	String[] arr = s.split(",");
	for(String v : arr){
		out.append("<th>");
		out.append(v);
		out.append("</th>");
	}
}
IoSession createSession(long jzId){
	RobotSession fs = new RobotSession(){
		public org.apache.mina.core.future.WriteFuture write(Object message){
			setAttachment(message);
			return null;
		}
	};
	fs.setAttribute(com.manu.network.SessionAttKey.junZhuId, jzId);
	return fs;
}
String getString(String key, String def){
	String v = req.getParameter(key);
	return v == null ? def : v;
}
void showStackTrace(Exception e)throws IOException{
	StackTraceElement[] stack = e.getStackTrace();
	for(StackTraceElement element : stack){
        out("\t"+element+"\n");
    }
}
void atag(Object s,String href) throws IOException{
	if(s == null)s="null";
	out.append("<a href="+href+">");
	out.append(s.toString());
	out.append("</a>");
}
void alert(Object s) throws IOException{
	if(s == null) return;
	out.append("<script>alert('"+s.toString()+"');</script>");
}
void redirect(Object s) throws IOException{
	if(s == null) return;
	out.append("<script>window.location='"+s.toString()+"'</script>");
}
%>
<%
setOut(out);
req = request;
%>