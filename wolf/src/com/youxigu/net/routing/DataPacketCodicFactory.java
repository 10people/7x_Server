package com.youxigu.net.routing;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class DataPacketCodicFactory implements ProtocolCodecFactory {
	private DataPacketCoder coder = null;
	
	public DataPacketCodicFactory() {
		this.coder = new DataPacketCoder();
	}
	
	public ProtocolDecoder getDecoder() throws Exception {
		return coder;
	}

	public ProtocolEncoder getEncoder() throws Exception {
		return coder;
	}

}
