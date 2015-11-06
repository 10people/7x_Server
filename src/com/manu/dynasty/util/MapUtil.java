package com.manu.dynasty.util;

import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author 李康
 * 2013-8-30 下午02:27:01
 *
 */
public class MapUtil {

	/**
	 * 把newMap的值加到map中
	 * @param map
	 * @param newMap
	 * @return  map
	 */
	public static  Map<Integer, Integer> addAllIntegerIntegerMap(Map<Integer, Integer> map,  Map<Integer, Integer> newMap){
		if (newMap == null){
			return map;
		}
		Set<Integer> keySet = newMap.keySet();
		for(int id : keySet){
			int newNum = newMap.get(id);
			if(map.containsKey(id)){
				int oldNum = map.get(id);
				map.put(id, oldNum + newNum);
			} else {
				map.put(id, newNum);
			}
		}
		return map;
	}


	/**
	 * 把newMap的值加到map中
	 * @param map
	 * @param newMap
	 * @return
	 */
	public static  Map<Integer, Float> addAllIntegerFloatMap(Map<Integer, Float> map,  Map<Integer, Float> newMap){
		if (newMap == null){
			return map;
		}
		Set<Integer> keySet = newMap.keySet();
		for(int id : keySet){
			float newNum = newMap.get(id);
			if(map.containsKey(id)){
				float oldNum = map.get(id);
				map.put(id, oldNum + newNum);
			} else {
				map.put(id, newNum);
			}
		}
		return map;
	}

	/**
	 * 
	 * @param map
	 * @param newMap
	 * @return
	 */
	public static  Map<String, Float> addAllStringFloatMap(Map<String, Float> map,  Map<String, Float> newMap){
		if (newMap == null){
			return map;
		}
		Set<String> keySet = newMap.keySet();
		for(String id : keySet){
			float newNum = newMap.get(id);
			if(map.containsKey(id)){
				float oldNum = map.get(id);
				map.put(id, oldNum + newNum);
			} else {
				map.put(id, newNum);
			}
		}
		return map;
	}
	
	
	/**
	 * 获取两个数直接的随即值
	 * @param begin
	 * @param end
	 * @return
	 */
	public static Integer randmonRangeValue(Integer begin, Integer end) {
		Random rand = new Random();
		int offset = end - begin;
		return rand.nextInt(offset) + begin;
	}
}
