package com.youxigu.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class FutureRequest<T> extends FutureTask<T> {

	public FutureRequest(Callable<T> callable) {
		super(callable);
	}

	@Override
	public void set(T v) {
		super.set(v);
	}

	@Override
	public void setException(Throwable t) {
		super.setException(t);
	}
}
