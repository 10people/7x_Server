package com.manu.network;

import java.util.ArrayList;
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
import com.qx.account.AccountManager;
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
	public static SessionManager inst;

	public static SessionManager getInst() {
		return inst;
	}

	public ConcurrentHashMap<Long, SessionUser> sessionMap;

	public SessionManager() {
		inst = this;
		PD.init();
		sessionMap = new ConcurrentHashMap<Long, SessionUser>();
		log.info("启动SessionManager");
	}

	public SessionUser addSession(IoSession ses) {
		SessionUser ret = new SessionUser();
		ret.session = ses;
		synchronized (sessionMap) {
			sessionMap.put(ses.getId(), ret);
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
		IoSession ss = AccountManager.sessionMap.get(junZhuId);
		SessionUser su=null;
		if(ss != null){
			su = sessionMap.get(ss.getId());
		}
		return su;
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
		Long sid = ses.getId();
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
		TXSocketMgr.inst.acceptor.getManagedSessions().values().stream().forEach(s->s.close(true));
		log.info("关闭所有session");
	}

	public List<Long> getAllOnlineJunZhuId() {
		synchronized (AccountManager.sessionMap) {
			List<Long> list = new LinkedList(AccountManager.sessionMap.keySet());
			return list;
		}
	}

	public List<SessionUser> getAllSessions() {
		synchronized (sessionMap) {
			List<SessionUser> list = new ArrayList<SessionUser>(sessionMap.values());
			return list;
		}
	}
}
