package com.manu.dynasty.util;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import com.manu.util.Util;

public class MathUtils {

	/**
	 * 取得指定的两个数之间的最大值
	 * @param a
	 * @param b
	 * @return
	 */
    public static int getMax(int a, int b) {
        return Math.max(a, b);
    }

    /**
     * 取得指定的两个数之间的最小值
     * @param a
     * @param b
     * @return
     */
    public static int getMin(int a, int b) {
        return Math.min(a, b);
    }

    /**
     * 取指定的3个数中>最小<最大的拿的数
     * @param produce 任意值
     * @param max 最大
     * @param min 最小
     * @return
     */
    public static int getBetweenMaxAndMin(int produce, int max, int min) {
        return Math.max(min, Math.min(produce, max));
    }

    private static Random random = new Random();

    static {
        random.setSeed(System.currentTimeMillis());
    }

    /**
     * 随机一个 [0, max-1]的数
     * @param max
     * @return
     */
    public static int getRandom(int max) {
        return Math.abs(random.nextInt()) % max;
    }
    /**
     * 随机一个 [0, max-1]的数
     * @param long max
     * @return
     */
    public static long getRandom(long max) {
    	return Math.abs(random.nextLong()) % max;
    }
    
    /**
     * 在最大和最小值之间随机一个整数[min,max)不包含最大值
     * @param min
     * @param max
     * @return
     */
    public static int getRandomExMax(int min, int max) {
    	if(min >= max) {
    		return min;
    	}
        return Math.abs(random.nextInt()) % (max - min) + min;
    }
    
    /**
     * 在最大和最小值之间随机一个整数[min,max]包含最大值
     * @param max
     * @param min
     * @return
     */
    public static int getRandomInMax(int min,int max) {
    	if(min >= max) {
    		return min;
    	}
		return max > min ? Util.randInt(max - min + 1) + min : min;
	}
    
    /**
     * return a random value in [0, range-1]
     * @param array[n][m] n:对象id 例如某个道具的id,  m:出现的几率 例如15% 1.5% 0.15%
     * @param range 几率范围 例如100,1000,10000
     * @return 返回对象id
     */
    public static int getRandom(int[][] array,int range) {
		int rand = MathUtils.getRandom(range);
		int sum = 0;
		for (int i = 0; i < array.length; i++) {
			sum += array[i][1];
			if (rand < sum) {
				return array[i][0];
			}
		}
		return 0;
	}
    /**
     * return 是否出现
     * @param gailv 例如10
     * @param range 几率范围 例如100,1000,10000
     * @return 返回对象id
     */
    public static boolean getResultByGailv(int gailv ,int range) {
		int rand = MathUtils.getRandom(range);
		if(rand<=gailv){
			return true;
		}
		return false;
	}
    public static long getLong(Object obj){
    	long retu=0;
		if (obj != null) {
			if (obj instanceof Double) {
				retu = ((Double) obj).longValue();
			} else if (obj instanceof Integer) {
				retu = ((Integer) obj).longValue();
			} else if (obj instanceof Long) {
				retu = (Long) obj;
			} else {
				retu = Long.parseLong(obj.toString());
			}
		}
		return retu;
    }
    
    public static int[] getIntArray(Object[] params){
    	if (params==null){
    		return null;
    	}
    	int[] arr = new int[params.length];
    	for (int i=0;i<params.length;i++){
    		Object tmp = params[i];
    		if (tmp instanceof Integer){
    			arr[i]=(Integer)tmp;
    		}else{
    			arr[i]=Integer.parseInt(tmp.toString());
    		}
    	}
    	return arr;
    }
    
    public static String[] getStringArray(Object[] params){
    	if (params==null){
    		return null;
    	}
    	String[] arr = new String[params.length];
    	for (int i=0;i<params.length;i++){
    		Object tmp = params[i];
    		if (tmp instanceof String){
    			arr[i]=(String)tmp;
    		}else{
    			arr[i]=String.valueOf(tmp.toString());
    		}
    	}
    	return arr;
    }
   
    
	/**
	 * @Description  // 获取Map最大value的Key
	 * @param hateMap
	 * @return
	 */
	public static Integer getMax4Map(Map<Integer, Integer> hateMap) {
		int max=-1;
		int key=-1;
		for(Map.Entry<Integer, Integer> entry:hateMap.entrySet()){    
		    if(max<entry.getValue()){
		    	max=entry.getValue();
		    	key=entry.getKey();
		    }
		}   
		return key;
	}
	/**
	 * @Description  // 获取Map最小value的Key
	 * @param treeMap
	 * @return
	 */
	public static Integer getMin4Map(TreeMap<Integer, Integer> treeMap) {
		int minKey= treeMap.firstKey();
		int min=treeMap.get(minKey);
		for(Map.Entry<Integer, Integer> entry:treeMap.entrySet()){    
		    if(min>entry.getValue()){
		    	min=entry.getValue();
		    	minKey=entry.getKey();
		    }
		}   
		return minKey;
	}
}
