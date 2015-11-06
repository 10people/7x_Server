package com.qx.world;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;

public class Mission {
	public Mission(int code, IoSession session, Builder builder) {
		this.code = code;
		this.session = session;
		this.builer = builder;
	}
	public int code;
	public IoSession session;
	public Builder builer;
}
