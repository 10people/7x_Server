package com.qx.persistent;

@FunctionalInterface
public interface LRUCacheMissFunction<T> {
	T load();
}
