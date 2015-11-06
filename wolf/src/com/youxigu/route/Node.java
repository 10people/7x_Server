package com.youxigu.route;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youxigu.net.Response;
import com.youxigu.net.WolfClient;
import com.youxigu.net.WolfTask;

/**
 * 一个处理task的单元
 * 他是通过WolfClient来完成的
 * 
 * 1 所有的执行上下问都需要在node上进行注册：connect 没有connect的unit是不能监听到task执行的
 * 2 可以判断unit是否注册过
 * 3 执行task：但不保证在本地执行
 * 
 * @author wuliangzhu
 *
 */
public class Node {
	private static Logger logger = LoggerFactory.getLogger(Node.class);
	
	private static WolfClient client;
	private static Node node;
	private static boolean connected; // 是否已经连接
	private Map<String, Object> units = new ConcurrentHashMap<String, Object>();
	private static Object UNIT_FLAG = new Object();
	
	private Node() {}
	
	public static Node create() {
		if (Node.node != null)
			return Node.node;
		
		Node.node = new Node();
		Node.client = WolfClient.create();

		return Node.node;
	}
	
	public void start() {
		Node.client.start();
		connected = true;
	}
	
	public void stop() {
		Node.client.stop();
		connected = false;
	}
	
	public static Node getInstance() {
		return Node.node;
	}
	
	/**
	 * 进行节点注册
	 */
	public void connectRoute(String nodeName) {
		ConnectRouteTask task = new ConnectRouteTask(true, nodeName);
		Node.client.asynSendTask(task);
	}
	
	public void disconnectRoute(String nodeName) {
		ConnectRouteTask task = new ConnectRouteTask(false, nodeName);
		Node.client.asynSendTask(task);
	}
	
	/**
	 * 进行独立单元注册
	 */
	public void registerUnit(String unitId) {
		this.units.put(RouteTask.TARGET_UNIT + unitId, Node.UNIT_FLAG);
		
		RegisterUnitTask task = new RegisterUnitTask(unitId, true);
		Node.client.asynSendTask(task);
	}
	
	public void unRegisterUnit(String unitId) {
		this.units.remove(RouteTask.TARGET_UNIT + unitId);
		
		RegisterUnitTask task = new RegisterUnitTask(unitId, false);
		Node.client.asynSendTask(task);
	}
	
	public boolean checkInNode(String unitId) {
		return this.units.get(RouteTask.TARGET_UNIT + unitId) != null;
	}
	
	/**
	 * 进行任务发送:
	 *  1 检查本地是否可以
	 * 
	 * @param task
	 */
	public void executeTask (RouteTask task) {
		String id = task.getId();
		if (task.isUnitTarget() && this.checkInNode(id)) {
			logger.debug("不需要路由，直接处理");
			task.execute(null);
			return;
		}
		logger.debug("需要路由，进行转接处理");
		Node.client.asynSendTask(task);
	}
	
	/**
	 * 可以直接进行消息发送
	 * 
	 * @param task
	 */
	public void sendMessage (Object task) {
		Node.client.asynSendTask(task);
	}
	
	/**
	 * 进行节点注册的Task
	 * @author wuliangzhu
	 *
	 */
	private static class ConnectRouteTask implements WolfTask {
		private static final long serialVersionUID = 8168169767220066073L;
		
		private boolean isConnect; // true connect or disconnect
		private String nodeName;
		public ConnectRouteTask (boolean isConnect, String nodeName) {
			this.isConnect = isConnect;
			this.nodeName = RouteTask.TARGET_NODE + nodeName;
		}
		public WolfTask execute(Response response) {
			IoSession session = response.getSession();
			String nodeId = this.nodeName;
			session.setAttachment(nodeId);
			
			Router router = Router.getInstance();
			if (this.isConnect)
				router.registerNode(nodeId, session);
			else 
				router.unRegisterNode(nodeId);
			
			router.flushUnitByNode(nodeId);
			
			return null;
		}
		
	}
	
	/**
	 * 进行单元注册的Task
	 * 
	 * @author wuliangzhu
	 *
	 */
	private static class RegisterUnitTask implements WolfTask {
		private static final long serialVersionUID = 2018922242976897529L;
		private String unitId;
		private boolean isRegister;
		public RegisterUnitTask (String unitId, boolean isRegister) {
			this.unitId = unitId;
			this.isRegister = isRegister;
		}
		
		public WolfTask execute(Response response) {
			IoSession session = response.getSession();
			if (session.getAttachment() == null) {
				logger.error("非法操作：节点没有登记前，不能进行单元注册");
				return null;
			}
			
			String nodeId = session.getAttachment().toString();
			Router router = Router.getInstance();
			
			if (this.isRegister)
				router.registerUnit(RouteTask.TARGET_UNIT + this.unitId, nodeId);
			else
				router.unRegisterUnit(RouteTask.TARGET_UNIT + this.unitId);
			
			return null;
		}
		
	}

	public boolean isConnected() {
		return connected;
	}
}
