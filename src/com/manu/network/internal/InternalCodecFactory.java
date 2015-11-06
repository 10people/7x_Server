package com.manu.network.internal;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import com.manu.network.ProtoBuffDecoder;
import com.manu.network.ProtoBuffEncoder;

/**
 * @author 康建虎
 *
 */
public class InternalCodecFactory  implements ProtocolCodecFactory{
	
	public ProtocolDecoder decoder;
	public ProtocolEncoder encoder;
	
	public InternalCodecFactory(){
		encoder = new ProtoBuffEncoder();
		decoder = new ProtoBuffDecoder();
	}
	
	@Override
	public ProtocolDecoder getDecoder(IoSession s) throws Exception {
		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession s) throws Exception {
		return encoder;
	}
}
