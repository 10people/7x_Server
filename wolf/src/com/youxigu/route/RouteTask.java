package com.youxigu.route;

import com.youxigu.net.Response;
import com.youxigu.net.WolfTask;

/**
 * 能够支持路由的task：
 * 	1 能够查找目标Id；有个特例如果Id 为 BROADCAST，则直接发给服务器，服务器进行广播（除了发送方）
 *  2 能够检测路由源的Node；
 *  
 * 
 * @author wuliangzhu
 *
 */
public class RouteTask implements WolfTask {
	private static final long serialVersionUID = -4579147680141392407L;
	public static final String TARGET_BROADCAST = "broadcast";
	public static final String TARGET_NODE = "node_";
	public static final String TARGET_UNIT = "unit_";
	
	private WolfTask task;
	private WolfTask noNodeOrUnitHandler;
	private String id;
	private String targetType; // 目标类型
	private String source; // 暂时没有用
	
	public RouteTask(WolfTask task, WolfTask error) {
		this.task = task;
		this.noNodeOrUnitHandler = error;
	}
	
	public RouteTask (){
		
	}
	
	String getDestination(){ // 任务发送目标
		if (this.targetType != null)
			return this.targetType + this.id;
		
		return this.id;
	}
	
	String getSource() { // 任务发送者
		return this.source;
	}
	
	public WolfTask execute(Response response) {
		if (task != null)
			return this.task.execute(response);
		
		return null;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	public boolean isBroadcast() {
		return RouteTask.TARGET_BROADCAST.equals(this.targetType);
	}
	
	public boolean isNodeTarget() {
		return RouteTask.TARGET_NODE.equals(this.targetType);
	}
	
	public boolean isUnitTarget() {
		return RouteTask.TARGET_UNIT.equals(this.targetType);
	}

	public WolfTask getNoNodeOrUnitHandler() {
		return noNodeOrUnitHandler;
	}

	public void setNoNodeOrUnitHandler(WolfTask noNodeOrUnitHandler) {
		this.noNodeOrUnitHandler = noNodeOrUnitHandler;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getId() {
		return id;
	}
}
