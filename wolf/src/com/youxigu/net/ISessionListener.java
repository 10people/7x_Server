package com.youxigu.net;

public interface ISessionListener {
	void open(Response response);
	void close(Response response);
}
