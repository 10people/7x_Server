package com.manu.network;

import java.io.IOException;
import java.util.List;

import log.CunLiangLog;
import log.OurLog;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.google.protobuf.MessageLite.Builder;
import com.manu.network.msg.AbstractMessage;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
import com.qx.huangye.HYTreasure;
import com.qx.junzhu.JunZhu;
import com.qx.log.LogMgr;
import com.qx.persistent.HibernateUtil;
import com.qx.world.Scene;

public class IOHandlerImpl implements IoHandler  {
	public static IOHandlerImpl inst = new IOHandlerImpl();
	public boolean closeIdleSession = true;
	public static Logger log = LoggerFactory.getLogger(IOHandlerImpl.class);
	public IOHandlerImpl(){
		inst = this;
	}
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		SessionManager.getInst().addSession(session);		
	}
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		log.error("sessionClosed sid {}, jzId {}",session.getId(), session.getAttribute(SessionAttKey.junZhuId));
		SessionManager.getInst().removeSession(session);
		playerExitScene(session);		
	}
	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		if(closeIdleSession){
			log.info("sid {} idle, now close it", session.getId());
			session.close(true);
		}
	}
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		if (cause instanceof IOException) {
			log.warn("IOException {} {}", session, cause);
			playerExitScene(session);
			return;
		}
		log.error("exceptionCaught sid {}, jzId {}",session.getId(), session.getAttribute(SessionAttKey.junZhuId));
		log.error("异常出现：", cause);
		if (!(cause instanceof java.io.IOException)) {
			// System.exit(0);
		}		
	}
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		if (message instanceof Builder) {
			MessageMgr.getInst().addMessage(session, (Builder) message);
		} else if (message instanceof ProtobufMsg) {
			ProtobufMsg mf = (ProtobufMsg) message;
			if (LogMgr.receiveLog) {
				LogMgr.inst.setReceiveProtoIdLog(session, mf);// 记录协议号
			}
			BigSwitch.getInst().route(mf.id, mf.builder, session);
		} else if (message instanceof AbstractMessage) {
			BigSwitch.getInst().route((AbstractMessage) message);
		} else {
			log.debug("未知的消息类型 {}", message);
		}		
	}
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		if (message instanceof Builder) {
			MessageMgr.getInst().addMessage(session, (Builder) message);
		} else if (message instanceof Message) {
			if (LogMgr.sendLog) {
				Message mf = (Message) message;
				LogMgr.inst.setSendProtoIdLog(session, mf);// 记录协议号
			}
		} else if (message instanceof ProtobufMsg) {
			if (LogMgr.sendLog) {
				ProtobufMsg mf = (ProtobufMsg) message;
				LogMgr.inst.setSendProtoIdLog(session, mf);// 记录协议号
			}
		} else if (message instanceof AbstractMessage) {
			// BigSwitch.getInst().route((AbstractMessage) message);
		} else {
			log.debug("发送消息 {},class {}", message, message.getClass());
		}		
	}
	
	public void playerExitScene(IoSession session) {
		BigSwitch.inst.scMgr.playerExitScene(session);
		Long junZhuId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if (junZhuId != null) {
			IoSession pre = AccountManager.sessionMap.get(junZhuId);
			if(pre == session){
				AccountManager.sessionMap.remove(junZhuId);
			}else{
				//多重登录，此时map里放的已经不是当前参数中的session
			}
			// 是否在藏宝点挑战
			List<HYTreasure> treasureList = HibernateUtil.list(
					HYTreasure.class, " where battleJunzhuId=" + junZhuId);
			for (HYTreasure t : treasureList) {
				t.battleJunzhuId = 0;
				HibernateUtil.save(t);
			}
			///经分log
			JunZhu junzhu = HibernateUtil.find(JunZhu.class, junZhuId);
			if(junzhu != null){
				long OnlineTime = (System.currentTimeMillis() - session.getCreationTime())/1000;
				OurLog.log.PlayerLogout(OnlineTime, junzhu.level, 0, String.valueOf(junZhuId));
				CunLiangLog.inst.logout(junZhuId, OnlineTime, junzhu.tongBi);
			}
			
			Scene scene = (Scene) session.getAttribute(SessionAttKey.Scene);
			if(scene != null && scene.name.contains("YB")) {
				scene.saveExitYBInfo(junzhu.id);
			}
		}
	}
}
