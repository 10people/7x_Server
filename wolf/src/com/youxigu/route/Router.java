package com.youxigu.route;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youxigu.net.WolfTask;

/**
 * 根据目的地址进行目标路由
 * 1 如果是广播，则路由给注册的所有Node；
 * 2 如果标识有Unit类型目标：则查找正确的Node，进行发送；
 * 3 如果标识有Node类型的目标，则发送到指定的Node；
 * 
 * 
 * PS: 1 nodeList 和 unitList 的内存泄漏问题：若玩家已经下线，这里没有清除咋办；
 *     2 将来加入unit 和 node 同步检查 以及周期性 刷新的机制
 * @author wuliangzhu
 *
 */
public class Router {
	private static Logger logger = LoggerFactory.getLogger(Router.class);
	
	private static Map<String, IoSession> nodeList = new ConcurrentHashMap<String, IoSession>();
	private static Map<String, String> unitList = new ConcurrentHashMap<String, String>();
	
	// 利用空间换时间
	private static ConcurrentMap<String, Queue<String>> node2Unit = new ConcurrentHashMap<String, Queue<String>>();
	
	private static Router router;
	
	private Router() {}
	
	public static Router getInstance() {
		if (router == null)
			router = new Router();
		
		return router;
	}
	
	public String findNodeByUnit(String unitId) {
		return Router.unitList.get(unitId);
	}
	
	public IoSession findNodeById(String nodeId) {
		return Router.nodeList.get(nodeId);
	}
	
	public void sendMessage2Node(String nodeId, WolfTask message) {
		IoSession session = this.findNodeById(nodeId);
		if (session != null)
			session.write(message);
	}
	
	/**
	 * 进行任务广播
	 * 
	 * @param task
	 */
	public void broadcastTask (WolfTask task) {
		Iterator<IoSession> ir = Router.nodeList.values().iterator();
		while (ir.hasNext()) {
			ir.next().write(task);
		}
	}
	
	/**
	 * Node 连接 到 route
	 * 
	 * @param nodeId
	 * @param session
	 */
	public void registerNode(String nodeId, IoSession session) {
		if (logger.isDebugEnabled()) {
			logger.debug("{} registerNode!!", nodeId);
		}
		
		Router.nodeList.put(nodeId, session);
	}
	
	/**
	 * Node 断开 route
	 * @param nodeId
	 */
	public void unRegisterNode(String nodeId) {
		if (logger.isDebugEnabled()) {
			logger.debug("{} unRegisterNode!!", nodeId);
		}
		
		Router.nodeList.remove(nodeId);
	}
	
	public void registerUnit(String unitId, String nodeId) {
		if (logger.isDebugEnabled()) {
			logger.debug("{} registerUnit!!", unitId);
		}
		
		Router.unitList.put(unitId, nodeId);
		
		Queue<String> unitList = getUnitListByNode(nodeId);
		
		unitList.add(unitId);
	}

	private Queue<String> getUnitListByNode(String nodeId) {
		Queue<String> unitList = Router.node2Unit.get(nodeId);
		if (unitList == null) {
			unitList = new ConcurrentLinkedQueue<String>();
			Queue<String> oldUnitList = Router.node2Unit.putIfAbsent(nodeId, unitList);
			if (oldUnitList != null) {
				unitList = oldUnitList;
			}
		}
		
		return unitList;
	}
	
	public void unRegisterUnit(String unitId) {
		if (logger.isDebugEnabled()) {
			logger.debug("{} unRegister!!", unitId);
		}
		
		String nodeId = Router.unitList.remove(unitId);
		this.getUnitListByNode(nodeId).remove(unitId);
	}
	
	/**
	 * 清空单元列表，并清空节点下面的单元
	 * @param nodeId
	 */
	public void flushUnitByNode(String nodeId) {
		Queue<String> unitList = this.getUnitListByNode(nodeId);
		for (String unitId : unitList) {
			Router.unitList.remove(unitId);
		}
		
		unitList.clear();
	}
}
