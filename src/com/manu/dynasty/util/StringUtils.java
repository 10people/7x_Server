package com.manu.dynasty.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class StringUtils {

	/**
	 * 一个静态的模式仓库，减少Pattern的compile 次数
	 */
	private static final Map<String, Pattern> patterns = new HashMap<String, Pattern>();
	static {
		patterns.put(",", Pattern.compile(","));
		patterns.put(":", Pattern.compile(":"));
		patterns.put(";", Pattern.compile(";"));
		patterns.put("\n", Pattern.compile("\n"));
		patterns.put("_", Pattern.compile("_"));
	}

	public static int getStringGBKLen(String str) {
		if (str == null) {
			return 0;
		}
		try {
			return str.getBytes("GBK").length;
		} catch (Exception e) {
			return str.getBytes().length;
		}
	}

	public static String[] split(String source, String regex) {
		Pattern pattern = patterns.get(regex);
		if (pattern == null) {
			pattern = Pattern.compile(regex);
			patterns.put(regex, pattern);
		}
		return pattern.split(source, 0);
	}

	public static String join(Collection datas, String regex) {
		StringBuilder sb = new StringBuilder();
		int len = datas.size();
		for (Object data : datas) {
			sb.append(data);
			if (len != 1) {
				sb.append(regex);
			}
			len--;
		}
		return sb.toString();

	}
	
	public static String join(long[] ids, String regex) {
		StringBuilder sb = new StringBuilder();
		int len = ids.length;
		for (long data : ids) {
			sb.append(data);
			if (len != 1) {
				sb.append(regex);
			}
			len--;
		}
		return sb.toString();

	}	
	public static String join(int[] ids, String regex) {
		StringBuilder sb = new StringBuilder();
		int len = ids.length;
		for (int data : ids) {
			sb.append(data);
			if (len != 1) {
				sb.append(regex);
			}
			len--;
		}
		return sb.toString();

	}
}
