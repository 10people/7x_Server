package com.manu.network;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;

/**
 * 补丁在这里拦截请求。
 * @author 康建虎
 *
 */
public class PatchMgr {
	public static PatchMgr inst = new PatchMgr();
	public PatchMgr(){
		
	}
	/**
	 * @param id
	 * @param builder
	 * @param session
	 * @return 如果有补丁进行了拦截，则返回true
	 */
	public boolean route(int id, Builder builder, IoSession session) {
		boolean hit = true;
		switch(id){
		//
		//在这里加入case进行拦截
		//
		default:
			hit = false;
			break;
		}
		return hit;
	}
}
