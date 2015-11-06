package com.manu.dynasty.util;

import org.codehaus.jackson.map.ObjectMapper;

public class JacksonUtils {
	private static ObjectMapper mapper = new ObjectMapper();

	public static String toJson(Object value) {
		try {
			return mapper.writeValueAsString(value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T toJavaBean(String json, Class<T> targetCls) {
		try {
			return mapper.readValue(json, targetCls);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
