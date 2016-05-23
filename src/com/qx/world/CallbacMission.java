package com.qx.world;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;

public class CallbacMission extends Mission{
	public CallbackFromScene c;
	public CallbacMission(){
		super(0, null, null);
	}

	public void doIt() {
		c.doIt();
	}
}
