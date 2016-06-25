package com.qx.timeworker;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;

public class FunctionID4Open {
	public static Logger log = LoggerFactory.getLogger(FunctionID4Open.class);
	public static final int fuli = 311;
	//推送某功能开启 和前段约定code为负数时红点消失
	public static void pushOpenFunction(long jzId,IoSession session,int Code){
//		log.info("向君主{}推送某功能开启--<{}>可以出现",jzId,Code);
		ErrorMessage.Builder resp=ErrorMessage.newBuilder();
		resp.setErrorCode(Code);
		ProtobufMsg pm = new ProtobufMsg();
		pm.id = PD.FUNCTION_OPEN_NOTICE;
		pm.builder = resp;
		session.write(pm);
	}
}
