package com.youxigu.app.cache;

/**
 * 缓存保证对单个数据块修改的原子性，数据块有可以分为小区域，比如：玩家数据，宠物数据，建筑数据，以及 背包数据
 * 
 * bucket 分为 Scope ，scope 具备 field field 具备Value 其实这个功能 Redis的hash就可以做到，只不过
 * 你要负责数据结构的组织
 * 
 * 这个层负责数据结构的组织，更新条件的检查，数据的更新。保证数据的线程安全
 * @author wuliangzhu
 *
 */
public interface ICommand {
	public void update(int uid, String scope, String field, int value); // 
}
