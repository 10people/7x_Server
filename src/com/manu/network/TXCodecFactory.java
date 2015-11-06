package com.manu.network;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * @author 康建虎
 *
 */
public class TXCodecFactory implements ProtocolCodecFactory{
	
	public ProtocolDecoder decoder;
	public ProtocolEncoder encoder;

	public TXCodecFactory(){
		encoder = new ProtoBuffEncoder();
		decoder = new TXDecoder();
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
