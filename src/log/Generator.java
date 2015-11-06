package log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Generator {
	public static void main(String[] args) throws Exception{
		readTemplate();
		String file = "F:/workspace/qxmobile/src/log/rawsql.xml";
		String java = "F:/workspace/qxmobile/src/log/gen2.java";
//		String file = "F:/workspace/qxmobile/src/log/七雄无双管理端log表.xml";
//		String java = "F:/workspace/qxmobile/src/log/gen.java";
		BufferedWriter bw = new BufferedWriter(new FileWriter(java));
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuffer loggers = new StringBuffer();
		String pre = br.readLine();
		bw.write("package log;\r\n");
		bw.write("import java.util.Calendar;\r\n");
		bw.write("import java.text.SimpleDateFormat;\r\n");
		bw.write("import org.slf4j.Logger;\r\n");
		bw.write("import org.slf4j.LoggerFactory;\r\n");
		bw.write("import net.sf.json.JSONArray;\r\n");
		bw.write("public class gen {\r\n");
		List<String> logNames = new ArrayList<String>();
		do{
			String line = br.readLine();
			if(line == null){
				break;
			}
			line = line.trim();
			if(line.startsWith("<struct")){
				int s = line.indexOf("\"")+1;
				int e = line.indexOf("\"", s);
				String name = line.substring(s,e);
				logNames.add(name);
				loggers.append("	public static Logger "+name+" = LoggerFactory.getLogger(\""+name+"\");\r\n");
				StringBuffer entryS = new StringBuffer();
				int entryIdx = 1;
				ArrayList<String> params = new ArrayList<String>();
				do{
					line = br.readLine();
					line = line.trim();
					if(line.startsWith("</struct")){
						bw.write("	public void "+name+"(long RoleId, String RoleName, String vopenid");
						appendParams(bw, params);
						bw.write("){");	bw.newLine();
						bw.write("//"); bw.write(line);	bw.newLine();
						bw.write("		"+name+".info(\"");
						for(int i=1;i<entryIdx;i++){
							bw.write(i == 1 ? "{}":",{}");
						}
						bw.write("\"\r\n");//花括号格式字符串结束
						bw.write("		//"+name+".info{1,{2,{3,{4,{5,{6,{7,{8,{9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30\r\n");
						bw.write(entryS.toString());//bw.newLine();
						bw.write(");");
						bw.write("//"); bw.write(line);	bw.newLine();
						bw.write("	}");bw.newLine();
						break;
					}else if(line.startsWith("<entry")){
						writeVar(entryS, line, params); 
						entryS.append(String.valueOf(entryIdx)); entryS.append(line);	entryS.append("\r\n");
						entryIdx ++;
					}
				}while(true);
			}else{
				bw.write("//"); bw.write(line);	bw.newLine();
			}
			pre = line;
			//
		}while(true);
		bw.append(loggers.toString());
		bw.append("\r\n");
		bw.append("public int GameSvrId = 1; \r\n");
		bw.append("public int ZoneID = 1; \r\n");
		bw.append("public SimpleDateFormat fmt = new SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\");\r\n");
		bw.write("}\r\n");
		bw.write("/*\r\n");
		for(String s: logNames){
			String rep = template.replaceAll("XXX", s);
			bw.write(rep);
		}
		bw.write("*/\r\n");
		br.close();
		bw.close();
	}

	private static void appendParams(BufferedWriter bw, ArrayList<String> params) throws IOException {
		for(String s: params){
			bw.write(s);
		}
	}

	public static void writeVar(StringBuffer entryS, String line, ArrayList<String> params) {
		entryS.append(",");
		if(line.contains("GameSvrId")){
			entryS.append("GameSvrId");
		}else if(line.contains("ZoneID")){
			entryS.append("ZoneID");
		}else if(line.contains("RoleName")){
			entryS.append("RoleName");
		}else if(line.contains("RoleId")){
			entryS.append("RoleId");
		}else if(line.contains("vopenid")){
			entryS.append("vopenid");
		}else if(line.contains("dtEventTime")){
			entryS.append("fmt.format(Calendar.getInstance().getTime())");
		}else{
			String type = getTag(line, "type=");
			String name = getTag(line, "name");
			if(type.equals("string"))type = "String";
			if(type.equals("text"))type = "JSONArray";
			entryS.append(name);
			params.add(","+type+" "+name);
		}
		entryS.append("//");
	}
	
	private static String getTag(String line, String tag) {
		int s = line.indexOf(tag);
		s = line.indexOf("\"", s)+1;
		int e = line.indexOf("\"", s);
		return line.substring(s,e);
	}

	public static void readTemplate()throws Exception{
		String file = "F:/workspace/qxmobile/src/log/logXml.txt";
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuffer sb = new StringBuffer();
		do{
			String line = br.readLine();
			if(line == null)break;
			sb.append(line);
			sb.append("\r\n");
		}while(true);
		br.close();
		template = sb.toString();
	}
	
	public static String template = "";
}
