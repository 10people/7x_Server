package com.manu.dynasty.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.manu.util.Util;
import com.manu.util.UtilDate;

/**
 * Most Often Used Functions
 * @author jikai
 * Nov 2, 2006
 */
public class CommonUtil {
	// add by wlz
	public static final Random rand = new Random();
	public static final Random rand4Drop = new Random();

	/** Check whether string s is empty. */
	public static boolean isEmpty(String s) {
		return ((s == null) || (s.trim().length() == 0));
	}

	/** Check whether collection c is empty. */
	public static boolean isEmpty(Collection c) {
		return ((c == null) || (c.size() == 0));
	}

	/** Check whether string s is NOT empty. */
	public static boolean isNotEmpty(String s) {
		return ((s != null) && (s.trim().length() > 0));
	}

	/** Check whether collection c is NOT empty. */
	public static boolean isNotEmpty(Collection c) {
		return ((c != null) && (c.size() > 0));
	}

	/** Check whether int num is NOT Zero. */
	public static boolean isNotZero(int num) {
		return ((num != 0) && (num > 0));
	}

	public static String[] split(String str, String split) {
		if (str == null)
			return null;
		if (split == null)
			return new String[] { str };

		StringTokenizer tokenizer = new StringTokenizer(str, split);
		List<String> tmp = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			tmp.add(tokenizer.nextToken());
		}

		return tmp.toArray(new String[tmp.size()]);
	}

	/**
	 * 随机返会一个float，一般位于0.8到1.2之间的 数 
	 * @param small
	 * @param big
	 * @return
	 */
	public static float randFloat() {
		return randFloatImpl(0.8f, 1.2f);
	}

	public static float randFloatImpl(float small, float big) {
		float range = big - small;
		return small + rand.nextFloat() * range;
	}

	public static int randInt(int range) {
		return Util.randInt(range);
	}

	public static int randInt4Drop(int range) {
		return rand4Drop.nextInt(range);
	}

	/**
	 * 判断是否是数字
	 * 
	 * */
	public static boolean isNumeric(String str) {

		Matcher isNum = NumberPattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	/**
	 * 格式化输出数字
	 * */
	public static String formatNumber(int number) {
		NumberFormat nf = NumberFormat.getInstance();
		return nf.format(number);
	}

	/**
	 * 保留小数点后2位
	 * */
	public static float formatFloatNum(double f) {
		DecimalFormat df = new DecimalFormat("###.00");
		return Float.parseFloat(df.format(f));
	}

	/**
	 * @param str
	 * @return 首字母大写的str
	 */
	public static String toFirstUpcaseStr(String str) {
		if (isEmpty(str))
			return null;
		return str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
	}

	public static boolean isMinMax(String str, int min, int max) {
		if (isEmpty(str))
			return false;
		else {
			if (str.length() < min || str.length() > max) {
				return false;
			}
		}
		return true;
	}

	public static boolean isPattern(String str, String pattern) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		return m.find();
	}

	/**
	 * 中文检查
	 * @param str
	 * @return
	 */
	public static boolean isChinese(String str) {
		String pattern = "([\u4e00-\u9fa5]|[a-zA-Z0-9]){6,8}";
		return isPattern(str, pattern);
	}

	/**
	 * 
	 * @version v1.9
	 * @param str
	 * @param defaultInt
	 * @return
	 */
	public static int parseInt(String str, int defaultInt) {
		int result = defaultInt;
		try {
			result = Integer.parseInt(str);
		} catch (Exception e) {
			result = defaultInt;
		}
		return result;
	}
	
	public boolean isNull(Object obj){
		return null == obj;
	}
	
	private final static Pattern pattern = Pattern.compile("【\\w+】");
	private final static Pattern NumberPattern = Pattern.compile("[0-9]*");
	public static String formatString(String message, Map para){
	    String msg = message;
	    Matcher matcher = pattern.matcher(msg);
	    
	    StringBuffer ret = new StringBuffer();
	    String key = null, value = null;
	    int start = 0, end = 0, lastIndex = 0;
	    while (matcher.find()){
	    	start = matcher.start();
	    	end = matcher.end();
	    	key = msg.substring(start + 1, end - 1);
	    	value = para.get(key).toString();
	    	
	    	ret.append(msg.substring(lastIndex, start));
	    	ret.append(value);
	    	
	    	lastIndex = end;
	    }
	    if (lastIndex < msg.length() - 1)
	    	ret.append(msg.substring(lastIndex));
		
	    return ret.toString();
	}
	
	public static long getBetweenMin(long start, long end){
		long s = (end - start) / 60;
		if((end - start) % 60 > 0){
			s += 1;
		}
		return s;
	}
	
	/**
	 * 分页
	 * @param datas
	 * @param firstResult
	 * @param pageSize
	 * @return
	 */
	public static List getPaginationList(List datas, int firstResult, int pageSize) {
		if(isEmpty(datas) || datas.size() < firstResult){
			return null;
		}
		List result = new ArrayList();
		for(int start = firstResult,  i = 0; start < datas.size() && i < pageSize; start++, i ++){
			result.add(datas.get(start));
		}
		return result;
	}
	
	/**
	 * print map
	 * @param map
	 * @return
	 */
	public static String printMap(Map map){
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		Iterator<Entry> it = map.entrySet().iterator();
		while(it.hasNext()){
			Entry en = it.next();
			sb.append(en.getKey());
			sb.append("=>");
			Object value = en.getValue();
			if(value.getClass().isArray()){
				Object[] array = (Object[]) value;
				sb.append(Arrays.toString(array));
			}else{
				sb.append(value);
			}
			sb.append("\r\n");
		}
		sb.append("}");
		return sb.toString();
	}
	
	public static String getRealIp(HttpServletRequest request){
		if (request == null)return null;
		
		String xRealIp = request.getHeader("x-forwarded-for");
		if (xRealIp == null){
			return request.getRemoteAddr();
		}
		
		return xRealIp;
	}
	
	/**
	 * 获取当前时间 yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String getNowDateTimeStr(){
		return UtilDate.datetime2Text(new Date());
	}
	
	public static final class Validator{
		public static boolean containsBlank(String str){
			String pattern = "\\s+";
			return isPattern(str, pattern);
		}
		
		
		/**
		 * 检查是否为空
		 * @param str
		 * @param errorString
		 */
		public static void checkEmpty(String str, String errorString){
			if(isEmpty(str)){
				throw new BaseException(errorString);
			}
		}
		
		/**
		 * 检查是否有空格
		 * @param str
		 * @param errorString
		 */
		public static void checkBlank(String str, String errorString){
			if(containsBlank(str)){
				throw new BaseException(errorString);
			}
		}
		
		public static void main(String[] args){
			
		}
	}
	
	/**
	 * 获取Map的json数据
	 * @param map
	 * @return
	 */
	public static String getMapJsonStr(Map map){
		if(map == null){
			return "";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		Iterator<Entry> it = map.entrySet().iterator();
		while(it.hasNext()){
			Entry en = it.next();
			sb.append("'").append(en.getKey()).append("'");
			sb.append(":");
			Object value = en.getValue();
			if(value.getClass().isArray()){
				Object[] array = (Object[]) value;
				sb.append(Arrays.toString(array));
			}else{
				sb.append("'").append(value).append("'");
			}
			sb.append(",");
		}
		if(sb.length() > 3){
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append("}");
		return sb.toString();
	}
	
	public static String getMapJsonStrForTLog(Map map){
		if(map == null){
			return "";
		}
		StringBuffer sb = new StringBuffer();
		Iterator<Entry> it = map.entrySet().iterator();
		while(it.hasNext()){
			Entry en = it.next();
			sb.append(en.getKey());
			sb.append(":");
			Object value = en.getValue();
			if(value.getClass().isArray()){
				Object[] array = (Object[]) value;
				sb.append(Arrays.toString(array));
			}else{
				sb.append(value);
			}
			sb.append(",");
		}
		if(sb.length() > 3){
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
	
	/**
	 * 取得比num大的最接近num的2的n次方
	 * @param num
	 * @return
	 */
	public static int getMinPower2(int num) {
		if (num < 0) {
			return Integer.MAX_VALUE;
		}

		int newNum = Integer.highestOneBit(num);
		newNum <<= (newNum < num ? 1 : 0);
		return newNum < 0 ? Integer.MAX_VALUE : newNum;

	}
	
	/**
	 * ip地址转换为主机字节码
	 * @param strIP
	 * @return
	 */
	public static long ipToLong(String strIP) 

	{  
	   long[] ip = new long[4]; 
	   //先找到ip地址字符串中.的位置
	   int position1 = strIP.indexOf(".");  
	   int position2 = strIP.indexOf(".", position1+1); 
	   int position3 = strIP.indexOf(".", position2+1); 
	   //将每个.之间的字符串转换成整型
	   ip[0] = Long.parseLong(strIP.substring(0,position1));  
	   ip[1] = Long.parseLong(strIP.substring(position1+1, position2)); 
	   ip[2] = Long.parseLong(strIP.substring(position2+1, position3)); 
	   ip[3] = Long.parseLong(strIP.substring(position3+1)); 
	   return (ip[0]<<24)+(ip[1]<<16)+(ip[2]<<8)+ip[3]; 
	} 
}
