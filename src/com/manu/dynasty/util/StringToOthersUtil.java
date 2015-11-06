package com.manu.dynasty.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

/**
 * 字符串与其他类型的数组转换类
 * 
 * @author dingweiqi
 */
public class StringToOthersUtil {

	public static final String DELIM = ";";

	/**
	 * 将字符串转换成整型数组
	 * 
	 * @param str
	 * @param delim
	 */
	public static int[] stringToIntArr(String str, String delim) {
		int len = 0;
		StringTokenizer stk = new StringTokenizer(str, delim);
		len = stk.countTokens();
		int[] a = new int[len];
		for (int i = 0; i < len; i++) {
			a[i] = Integer.parseInt(stk.nextToken());
		}
		return a;

	}

	/**
	 * 将字符串转换成短整型数组
	 * 
	 * @param str
	 * @param delim
	 */
	public static short[] stringToShortArr(String str, String delim) {
		int len = 0;
		StringTokenizer stk = new StringTokenizer(str, delim);
		len = stk.countTokens();
		short[] a = new short[len];
		for (int i = 0; i < len; i++) {
			a[i] = Short.parseShort(stk.nextToken());
		}
		return a;

	}
	
	public static byte[] stringToByteArr(String str, String delim){
		int len = 0;
		StringTokenizer stk = new StringTokenizer(str, delim);
		len = stk.countTokens();
		byte[] a = new byte[len];
		for (int i = 0; i < len; i++) {
			a[i] = Byte.parseByte(stk.nextToken());
		}
		return a;
	}
	
	public static List<Integer> stringToIntList(String str, String delim){
		List<Integer> list = new ArrayList<Integer>();
		String[] strs = StringUtils.split(str, delim);
		for(String ss : strs){
			list.add(Integer.parseInt(ss));
		}
		return list;	
	}
	
	public static String intListToString(List<Integer> lst, String delim){
		int index = 0;
		int size = lst.size();
		StringBuffer sb = new StringBuffer();
		for(Integer id : lst){
			if(index != size - 1){
				sb.append(id);
				sb.append(delim);
			} else {
				sb.append(id);
			}
			index++;
		}
		return sb.toString();
	}
	
	public static String intArrListToString(List<int[]> lst, String delim1, String delim2){
		int index = 0;
		StringBuffer sb = new StringBuffer();
		for(int[] paramArr : lst){
			int j = 0;
			for(int param : paramArr){
				if (j != paramArr.length -1){
					sb.append(param);
					sb.append(delim1);
				} else {
					sb.append(param);
				}
				j++;
			}
			if (index != lst.size() -1){
				sb.append(delim2);
			}
			index++;
		}
		return sb.toString();
	}
	
	
	public static List<int[]> stringToListIntArr(String str, String delim1, String delim2){
		List<int[]> list = new ArrayList<int[]>();
		String[] strs = StringUtils.split(str, delim2);
		for(String param : strs){
			int[] paramArr = stringToIntArr(param, delim1);
			list.add(paramArr);
		}
		return list;
	}
}
