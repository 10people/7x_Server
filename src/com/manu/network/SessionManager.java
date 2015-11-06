package com.manu.network;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.chat.ChatMgr;
import com.qx.event.ED;
import com.qx.event.EventMgr;
import com.qx.junzhu.PlayerTime;
import com.qx.persistent.HibernateUtil;

/**
 * Session管理器
 * 
 * @author 康建虎
 * 
 */
public class SessionManager {
	public static Logger log = LoggerFactory.getLogger(SessionManager.class);
	private AtomicLong sessionIdGen;
	public static SessionManager inst;

	public static SessionManager getInst() {
		return inst;
	}

	public ConcurrentHashMap<Long, SessionUser> sessionMap;

	public SessionManager() {
		inst = this;
		PD.init();
		sessionMap = new ConcurrentHashMap<Long, SessionUser>();
		sessionIdGen = new AtomicLong(0);
		log.info("启动SessionManager");
	}

	public SessionUser addSession(IoSession ses) {
		Long sessionId = Long.valueOf(SessionManager.getInst().genSessionId());
		ses.setAttribute(SessionAttKey.sessionId, sessionId);
		SessionUser ret = new SessionUser();
		ret.session = ses;
		ret.sessoinId = sessionId;
		synchronized (sessionMap) {
			sessionMap.put(sessionId, ret);
		}
		ChatMgr.getInst().addUser(ret);
		return ret;
	}
	
	public IoSession getIoSession(Long junZhuId) {
		SessionUser su = findByJunZhuId(junZhuId);
		if(su == null) {
			return null;
		}
		return su.session;
	}

	public SessionUser findByJunZhuId(Long junZhuId) {
		synchronized (sessionMap) {
			Iterator<SessionUser> it = sessionMap.values().iterator();
			while (it.hasNext()) {
				SessionUser u = it.next();
				Long v = (Long) u.session.getAttribute(SessionAttKey.junZhuId);
				if (v != null && v.longValue() == junZhuId.longValue()) {
					return u;
				}
			}
		}
		return null;
	}

	public long genSessionId() {
		return sessionIdGen.getAndIncrement();
	}
	
	/**
	 * 君主是否在线
	 * @param junZhuId
	 * @return	true-在线，false-不在线
	 */
	public boolean isOnline(long junZhuId) {
		SessionUser sessionUser = findByJunZhuId(junZhuId);
		return sessionUser != null;
	}
	
	public void removeSession(IoSession ses) {
		Long sid = (Long) ses.getAttribute(SessionAttKey.sessionId);
		SessionUser ret = null;
		synchronized (sessionMap) {
			ret = sessionMap.remove(sid);
			Long id = (Long) ses.getAttribute(SessionAttKey.junZhuId);
			// 2015年7月4日 14：09 添加在线时间统计
			if (id != null) {
				EventMgr.addEvent(ED.ACC_LOGOUT, id);
			}
		}
		if (ret == null) {
			log.warn("session user should not be null, {}", sid);
			return;
		}

		ChatMgr.getInst().removeUser(ret);
	}
	
	/**
	 * 取得玩家离线时间
	 * @param junZhuId
	 * @return	返回玩家离线时间，单位-秒
	 */
	public int getOfflineTime(long junZhuId) {
		if(isOnline(junZhuId)) {
			return 0;
		}
		PlayerTime playerTime = HibernateUtil.find(PlayerTime.class, junZhuId);
		if(playerTime == null) {
			log.error("找不到君主:{}的playerTime信息", junZhuId);
			return 0;
		}
		Date date = new Date();
		Date logoutDate = playerTime.getLogoutTime();
		if(logoutDate == null) {
			return 0;
		}
		if(logoutDate.after(date)) {
			return 0;
		}
		int offlineTime = (int) ((date.getTime() - logoutDate.getTime()) / 1000);
		return offlineTime;
	}

	public void closeAll() {
		synchronized (sessionMap) {
			Iterator<SessionUser> it = sessionMap.values().iterator();
			while (it.hasNext()) {
				SessionUser u = it.next();
				it.remove();
				u.session.close(true);
			}
		}
		log.info("关闭所有session");
	}

	public List<Long> getAllOnlineJunZhuId() {
		List<Long> list = new LinkedList();
		synchronized (sessionMap) {
			for (SessionUser user : sessionMap.values()) {
				list.add((Long) user.session
						.getAttribute(SessionAttKey.junZhuId));
			}
		}
		return list;
	}

	public List<SessionUser> getAllSessions() {
		List<SessionUser> list = new LinkedList<SessionUser>();
		synchronized (sessionMap) {
			for (SessionUser user : sessionMap.values()) {
				list.add(user);
			}
		}
		return list;
	}
}
