package com.youxigu.app.domain.ability;

/**
 * 
 * 用于模板数据获取，如果有功能需要模板数据，则需要IDataLoader来进行数据获取
 * 
 * @author wuliangzhu
 *
 */
public interface IDataLoader {
	public Object get(int... args);
}
