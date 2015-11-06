package com.youxigu.app.cache.data;

/**
 * field类型: int
 * 			 long 
 * 			 flout
 * 			 double
 * 			 string
 * 
 * @author wuliangzhu
 *
 */
public interface IScope {
	void addField(String key, String fieldType);
	
	IFieldValue get(String key);
	
	void save();
	void load();
}
