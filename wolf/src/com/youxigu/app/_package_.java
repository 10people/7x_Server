package com.youxigu.app;

public class _package_ {
	/**
	 * 添加对同步sql程序的容错方案：
	 * 1 对于出错的sql，要缓存到容错buf中；
	 * 2 如果有新加入的具备相同key的sql进来，就删除容错中的sql；
     * 3 每次执行sql的时候把容错的sql合并到新的sql中
     * 
     * 添加一新的ErrorManager，3个接口：
     * 1 备份错误sql：同步出错的时候调用：
     * 2 删除存在的sql：有更新sql过来的时候调用：
     * 3 获取错误的sql：同步时获取
	 */
}
