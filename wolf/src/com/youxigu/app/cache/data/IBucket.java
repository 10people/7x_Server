package com.youxigu.app.cache.data;


/**
 * 一个数据集合，程序保证一个Bucket的数据线程安全
 * 
 * @author wuliangzhu
 *
 */
public interface IBucket {
	IScope getScope(String name);
	void addScope(String name, IScope scope);
}
