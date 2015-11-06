package com.youxigu.route;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youxigu.net.IWolfService;
import com.youxigu.net.Response;

public class RouteService implements IWolfService {
	private static Logger logger = LoggerFactory.getLogger(RouteService.class);

	/**
	 * 判断Message 的类型是否是 RouteTask 如果是 则进行处理
	 * 1 判断destination是否是广播若是 进行广播-----发送给所有注册过的Node，当前Node发送方除外
	 * 2 判断destination是否是NodeId 如果是，找到正确的Node，发送
	 * 3 如果是unitId，查找对应的nodeId
	 */
	public boolean handleMessage(Response response, Object message) {
		if (message instanceof RouteTask) {
			Router router = Router.getInstance();
			RouteTask task = RouteTask.class.cast(message);
			String destId = task.getDestination();
			
			if (task.isBroadcast()) {
				router.broadcastTask(task);
			}else if (task.isNodeTarget()) {
				IoSession session = router.findNodeById(destId);
				if (session == null) {
					if (logger.isDebugEnabled()){
						logger.debug("你要寻找的节点不存在:{}", destId);
					}
					response.write(task.getNoNodeOrUnitHandler());
				}else {
					if (logger.isDebugEnabled()){
						logger.debug("找到节点{}，进行Task分配", destId);
					}
					router.sendMessage2Node(destId, task);
				}
			}else if (task.isUnitTarget()) {
				String nodeId = router.findNodeByUnit(destId);
				if (nodeId == null) {
					if (logger.isDebugEnabled()){
						logger.debug("没有找到单元{}所在的节点", destId);
					}
					response.write(task.getNoNodeOrUnitHandler());
				}else {
					router.sendMessage2Node(nodeId, task);
					if (logger.isDebugEnabled())
						logger.debug("找到单元所在服务器进行处理:" + nodeId + " " + destId);
				}
			
			} else {
				if (logger.isDebugEnabled())
					logger.debug("没有目标节点就在router执行:" + destId);
				task.execute(response);
			}
			
			return true;
		}
		return false;
	}

}
