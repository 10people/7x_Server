package com.qx.persistent;

/**
 * 使用这种形式进行延迟保存的，必须有缓存，否则可能导致下次查询不能获得最新的数据。
 * @author 康建虎
 *
 */
public interface DBHash {
	long hash();
}
