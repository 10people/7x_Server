package com.youxigu.net.codec;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * 用于一般的网络发送
 * 
 * @author wuliangzhu
 *
 */
public class SimpleObjectCodicFactory implements ProtocolCodecFactory {
	private SimpleObjectCoder coder = null;
	
	public SimpleObjectCodicFactory() {
		this.coder = new SimpleObjectCoder();
	}
	
	public ProtocolDecoder getDecoder() throws Exception {
		return coder;
	}

	public ProtocolEncoder getEncoder() throws Exception {
		return coder;
	}

}
