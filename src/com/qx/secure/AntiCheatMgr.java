package com.qx.secure;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.BattleProg;
import qxmobile.protobuf.BattleProg.InProgress;
import qxmobile.protobuf.BattleProg.InitProc;

import com.google.protobuf.MessageLite.Builder;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;

public class AntiCheatMgr {
	public static boolean anti = false;
	public static boolean showLog = false;
	public static ConcurrentHashMap<Long, InitProc> progressMap = new ConcurrentHashMap<Long, BattleProg.InitProc>();
	public static Logger log = LoggerFactory.getLogger(AntiCheatMgr.class);
	public static Random rnd = new Random();

	public void initProc(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			return;
		}
		InitProc.Builder ret = InitProc.newBuilder();
		//下一次请求密语使用的协议号。
		short protoId = PD.C_RND_PROT[rnd.nextInt(PD.C_RND_PROT.length)];
		ret.setA(protoId);
		//服务器加密后的数据，下一次请求密语时发给服务器。
		long curTime = System.currentTimeMillis();
		long seed = protoId;
		seed = seed | (seed<<16) | (seed<<32) | (seed<<48);
		seed = seed ^ curTime;
		ret.setB(String.valueOf(seed));
		ret.setC(5000);
		InitProc msg = ret.build();
		progressMap.put(junZhuId, msg);
		session.write(msg);
		if(showLog)log.info("发送密语给 {},下次协议号{}", junZhuId,protoId);
	}

	public void stepProc(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			return;
		}
		InitProc pre = progressMap.get(junZhuId);
		if(pre == null){
			log.warn("AA没有初始化信息{}", junZhuId);
			if(anti){
				
			}
			return;
		}
		InProgress.Builder request = (qxmobile.protobuf.BattleProg.InProgress.Builder) builder;
		if(request.getA().equals(pre.getB())==false){
			//本次发来的密语和之前的约定不一致。
			log.warn("AA密语不一致，需要{}，实际{}，pid {}",
					pre.getB(), request.getA(), junZhuId);
			return;
		}
		long preTime = Long.parseLong(pre.getB());
		long seed = id;
		seed = seed | (seed<<16) | (seed<<32) | (seed<<48);
		preTime = seed ^ preTime;
		
		int preDiff = pre.getC();
		long cur = System.currentTimeMillis();
		long diff = cur - preTime;
		if(diff<preDiff){//本地测试居然间隔时间会精确地相等！
			log.warn("时间间隔不满足{}<{},pid {}",diff,preDiff,junZhuId);
			return;
		}
		if(id != pre.getA()){
			log.warn("协议号不匹配，需要{}，实际{}， pid{}",pre.getA(), id, junZhuId);
			return;
		}
		initProc(id, session, builder);
	}

	public void stopProc(int id, IoSession session, Builder builder) {
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(junZhuId == null){
			return;
		}
		InitProc pre = progressMap.get(junZhuId);
		if(pre == null){
			log.warn("BB没有初始化信息{}", junZhuId);
			if(anti){
				
			}
			return;
		}
		//因为终止是可能还没到下次约定的延迟时间，所以不检查时间。
		InProgress.Builder request = (qxmobile.protobuf.BattleProg.InProgress.Builder) builder;
		if(request == null){
			log.error("{}终止请求是null",junZhuId);
			return;
		}else if(request.getA() == null){
			log.error("{}终止请求getA是null",junZhuId);
			return;
		}
		if(request.getA().equals(pre.getB())==false){
			//本次发来的密语和之前的约定不一致。
			log.warn("BB密语不一致，需要{}，实际{}，pid {}",
					pre.getB(), request.getA(), junZhuId);
			return;
		}
		progressMap.remove(junZhuId);
		log.info("密语过程正常结束{}",junZhuId);
		session.setAttribute(SessionAttKey.antiCheatPass, Boolean.TRUE);
	}
}
