package com.youxigu.net.routing;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.common.IoSession;


class Routing {
	public static final int ROUTING_SERVER_ID = -1;
	
	 static ConcurrentHashMap<Integer, IoSession> clients = new ConcurrentHashMap<Integer, IoSession>();
	 static ConcurrentHashMap<Integer, IoSession> users = new ConcurrentHashMap<Integer, IoSession>();
	 static ConcurrentHashMap<Integer, IoSession> backendServers = new ConcurrentHashMap<Integer, IoSession>();
	 private static AtomicInteger clientIdGenerator = new AtomicInteger(0);
	 
	 static void addUserClient(IoSession session) {
		 int clientId = getClientId();
		 session.setAttribute("clientId", clientId);
		 clients.put(clientId, session);
	 }
	 
	 static IoSession getClient(int clientId){
		 return clients.get(clientId);
	 }
	 
	 static void removeUserClient(IoSession session) {
		 Integer clientIdObj = (Integer)session.getAttribute("clientId");
		 if (clientIdObj != null) {
			clients.remove(clientIdObj);
		 }
	 }
	 
	 static int getIdBySession(IoSession session) {
		 Integer clientIdObj = (Integer)session.getAttribute("clientId");
		 if (clientIdObj != null) {
			 return clientIdObj;
		 }
		 
		 return 0;
	 }
	 
	 static IoSession getUser(int destinationId) {
		 return users.get(destinationId);
	 }
	 
	 static void addUser(int destinationId, IoSession session) {
		 users.put(destinationId, session);
	 }
	 
	 static void bindUser2Client(int destinationId, int clientId) {
		 IoSession session = getClient(clientId);
		 users.put(destinationId, session);
	 }
	 
	 static IoSession removeUser(int destinationId) {
		 return users.remove(destinationId);
	 }
	 
	 /**
	  * @param code 用户的命令
	  * @return
	  */
	 static IoSession getBackend(int code) {
		 return backendServers.get(code);
	 }
	 
	 @SuppressWarnings("unchecked")
	static void addBackendServer(int codeprefix, IoSession session) {
		 backendServers.put(codeprefix, session);
		 Object o = session.getAttribute("codes");
		 if (o == null) {
			 session.setAttribute("codes", o = new ConcurrentLinkedQueue<Integer>());
		 }
		 
		 Queue<Integer> l = (Queue<Integer>)o;
		 l.add(codeprefix);
	 }
	 
	 @SuppressWarnings("unchecked")
	static void removeBackendServer(IoSession session) {
		 Queue<Integer> clientIdObj = (Queue<Integer>)session.getAttribute("codes");
		 if (clientIdObj != null) {
			for (Integer code : clientIdObj) {
				backendServers.remove(code);
			}
		 }
	 }
	 
	 private static int getClientId() {
		 int ret = clientIdGenerator.incrementAndGet();
		 if (ret == Integer.MAX_VALUE) {
			clientIdGenerator.set(0);
		 }
		
		 return ret;
	 }
}
