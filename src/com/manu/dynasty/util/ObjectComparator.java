package com.manu.dynasty.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 此类可为String、Map和其他实体类的队列进行排序，支持多字段排序。
 * 类似sql：order by column1 asc, column2 desc, ...
 * 排序前需提供排序的字段（String对象排序除外），也可以自己指定每个字段的排序规则，
 * 对于Map结构的数据需给出要排序字段的key，对于实体类需给出要排序字段的get方法，只支持无参数的get方法。
 * 排序优先顺序是按照字段list的顺序。
 * 
 * String排序举例：
 * 将stringList中的String对象按照默认字母顺序排序
 * List<String> stringList;
 * Comparator comparator = new ObjectComparator();//默认升序
 * Collections.sort(stringList, comparator);
 * 
 * Map排序举例：
 * 将mapList按照map中的key1和key2两个字段排序，第一个字段升序，第二个字段降序
 * List<Map> mapList;
 * List<String> columnList = UtilMisc.toList("key1", "key2");
 * List<Integer> columnOrder = UtilMisc.toList(ObjectComparator.ASC, ObjectComparator.DESC);
 * Comparator comparator = new ObjectComparator(columnList, columnOrder);
 * Collections.sort(mapList, comparator);
 * 
 * 实体类排序举例：
 * 以User为例，按照rankId升序，userName升序 排序
 * List<User> userList;
 * List<String> columnList = UtilMisc.toList("getRankId", "getUserName");//对应排序的字段必须有get方法
 * List<Integer> columnOrder = UtilMisc.toList(ObjectComparator.ASC, ObjectComparator.ASC);
 * Comparator comparator = new ObjectComparator(columnList, columnOrder);
 * Collections.sort(userList, comparator);
 * 1//等价于下面代码：
 * 1// Comparator comparator = new ObjectComparator(columnList);或者
 * 1// Comparator comparator = new ObjectComparator(columnList, ASC);
 * 1// Collections.sort(userList, comparator);
 * 
 * @author wuyj
 *
 */
public class ObjectComparator implements Comparator<Object>{
	
	private List<String> columnList;//排序的字段名，此list的字段顺序即为排序优先顺序
	private List<Integer> columnOrder;//字段升序或降序，顺序与columnList中字段对应，顺序一致
	
	/**
	 * 升序
	 */
	public static final int ASC = 0;
	
	/**
	 * 降序
	 */
	public static final int DESC = 1;
	
	/**
	 * 仅排序队列为java.long.String对象时，使用此构造器
	 * 默认升序
	 */
	public ObjectComparator(){
		this(ASC);
	}
	
	/**
	 * 仅排序队列为java.long.String对象时，使用此构造器
	 * @param order 排序规则
	 */
	public ObjectComparator(int order){
		this("", order);
	}
	
	/**
	 * 所有排序字段默认排序规则为升序
	 * @param columnList要排序的字段名列表
	 */
	public ObjectComparator(List<String> columnList){
		this(columnList, ASC);
	}
	
	/**
	 * 对单个字段排序，提供一个方便的构造方法
	 * @param column 实体类字段的get方法名，map的key
	 * @param order
	 */
	@SuppressWarnings("unchecked")
	public ObjectComparator(String column, int order){
		this(getColumnListPara(column), order);
	}
	
	private static List<String> getColumnListPara(String column){
		List<String> list = new ArrayList<String>();
		list.add(column);
		return list;
	}
	
	/**
	 * 所有排序字段都按照一个升降序规则
	 * @param columnList 要排序的字段名列表
	 * @param order 所有字段的统一排序规则
	 */
	public ObjectComparator(List<String> columnList, int order){
		this(columnList, orderList(order, columnList.size()));
	}
	
	/**
	 * 获得一个ObjectComparator
	 * @param columnList 要排序的字段名列表
	 * @param columnOrder 字段升降序规则列表
	 */
	public ObjectComparator(List<String> columnList, List<Integer> columnOrder){
		this.columnList = columnList;
		this.columnOrder = columnOrder;
	}
	
	private static List<Integer> orderList(int order, int length){
		List<Integer> list = new ArrayList<Integer>();
		for(int i = 0; i < length; i++){
			list.add(order);
		}
		return list;
	}

	public int compare(Object o1, Object o2) {
		int i = 0;
		for(String column : columnList){
			Object value1 = this.getValue(o1, column);
			Object value2 = this.getValue(o2, column);
			try {
				int result = this.compareValue(value1, value2);
				if(result != 0){
					if(columnOrder.get(i) == ASC){
						return result; //升序
					}else{
						return -result; //降序
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			i++;
		}
		return 0;
	}
	
	/**
	 * 比较两个参数，返回 -1 0 1
	 * 参数类型支持 数字、字符串、日期
	 * @param value1
	 * @param value2
	 * @return
	 * value1 < value2 返回负整数
	 * value1 = value2 返回 0
	 * value1 > value2 返回正整数
	 * @throws Exception 没有匹配的类型
	 */
	public int compareValue(Object value1, Object value2) throws Exception{
		//数字
		if(isNumber(value1) && isNumber(value2)){
			double d = Double.parseDouble(value1.toString()) - Double.parseDouble(value2.toString());
			if(d > 0){
				return (int)Math.ceil(d);
			}else if(d < 0){
				return (int)Math.floor(d);
			}
			return 0;
		}
		
		//字符串
		if(value1 instanceof String && value2 instanceof String){
			return value1.toString().compareTo(value2.toString());
		}
		
		//日期
		if(value1 instanceof Date && value2 instanceof Date){
			long result = ((Date)value1).getTime() - ((Date)value2).getTime();
			if(result > 0){
				return 1;
			}else if(result < 0){
				return -1;
			}else{
				return 0;
			}
		}
		throw new Exception(value1.getClass().getName() + "," + value2.getClass().getName() + " error type !"); //$NON-NLS-2$
	}
	
	/**
	 * 判断参数是否为数字
	 * @param obj
	 * @return 是数字返回true
	 */
	public boolean isNumber(Object obj){
		if(obj instanceof Integer || 
				obj instanceof Long || 
				obj instanceof Short || 
				obj instanceof Byte || 
				obj instanceof Double || 
				obj instanceof Float){
			return true;
		}
		return false;
	}
	
	/**
	 * 获取obj对象中的字段column的数据
	 * @param obj 可为Map、实体类、String
	 * @param column
	 * @return
	 */
	public Object getValue(Object obj, String column){
		if(obj instanceof Map){
			return ((Map)obj).get(column);
		}else if(obj instanceof String){
			return obj;
		}else{
			try {
				Method method = obj.getClass().getMethod(column);
				return method.invoke(obj);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
