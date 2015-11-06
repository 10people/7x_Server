package com.qx.util;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JsonUtils {
	
	public static String objectToStr(Object obj) {
		if(obj == null) {
			return "";
		}
		JSONObject jsonObject = JSONObject.fromObject(obj);
		String str = jsonObject.toString();
		return str;
	}
	
	public static <T> T strToJavaBean(String jsonStr, Class<T> clazz) {
		if(jsonStr == null || jsonStr.equals("")) {
			return null;
		}
		JSONObject jsonObject = JSONObject.fromObject(jsonStr);
		T bean = (T) JSONObject.toBean(jsonObject, clazz);
		T t = bean;
		return t;
	}
	
	public static String listToStr(List list) {
		if(list == null || list.size() == 0) {
			return "";
		}
		JSONArray jsonArray = JSONArray.fromObject(list);
		String str = jsonArray.toString();
		return str;
	}
	
	public static <T> List<T> strToList(String str, Class<T> clazz) {
		if(str == null || str.equals("")) {
			return null;
		}
		JSONArray jsonArray = JSONArray.fromObject(str);
		List<T> list = JSONArray.toList(jsonArray, clazz);
		return list;
	}
}
