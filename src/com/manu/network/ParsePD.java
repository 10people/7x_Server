package com.manu.network;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ParsePD {
	public static Map<Integer, String> pdMap = new HashMap<Integer, String>();
	public static String getName(int id){
		return pdMap.get(id);
	}
	public static void makeMap(){
		Field[] fs = PD.class.getDeclaredFields();
		try{
			for(Field f : fs){
				if(! f.getType().isPrimitive())continue;
				Short v = (Short)f.get(null);
				pdMap.put(v.intValue(), f.getName());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
