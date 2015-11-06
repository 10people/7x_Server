package com.youxigu.net.routing;

import java.util.LinkedList;
import java.util.List;

import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youxigu.boot.Config;
import com.youxigu.boot.WolfConfig;
import com.youxigu.net.IInitListener;
import com.youxigu.net.SocketContext;
import com.youxigu.net.WolfClient;


/**
 * 对接收到的数据根据配置进行消息转发
 *
 * 再server之间转发，完全根据opcode，这个opcode前几位决定了应该在哪个服务器处理。
 * destinationId，再数据发给玩家的时候有效，前提是 opcode前几位标示了是发送给玩家
 *	
 * 建立连接的时候连接并不对用户和socket进行管理，在认证成功之后，返回的命令被拦截处理，加入用户和socket的对应关系
 * 
 * 对所有的连接都添加一个id，用于标示这个socket，数据包中附带是谁发给他的。
 * 
 * 连接管理器用这个处理器
 * 
 * @author wuliangzhu
 *
 */
public class RoutingHandler extends IoHandlerAdapter implements IInitListener {
	private static Logger logger = LoggerFactory.getLogger(RoutingHandler.class);
	
	private List<WolfClient> backends = new LinkedList<WolfClient>();
	public RoutingHandler() {

	}
	
	/**
	 * 进行消息初始化
	 */
	public void init(SocketContext context) {
		// 进行backendServer连接
		List<String> backendServers = Config.getList("backend");
		WolfConfig wolfConfig = WolfConfig.create("./conf/client.properties");
		for (String backend : backendServers) {
			String[] args = backend.split(":"); // ip port code
			
			WolfClient client = WolfClient.create(args[0], Integer.parseInt(args[1].trim()), wolfConfig);
			client.start();
			
			Routing.addBackendServer(Integer.parseInt(args[2]), client.getSession());
			
			backends.add(client);
		}
	}
	
	public void shutdown(){
		for (WolfClient client : backends) {
			client.stop();
		}
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		// super.exceptionCaught(session, cause);
		logger.info("exception:", cause.toString());
	}

	/**
	 * 因为这个只处理用户请求，所以所有的命令都是根据code 找到backendServer进行发送
	 */
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		DataPacket packet = DataPacket.class.cast(message);
		int code = packet.getServerId();
		int toId = packet.getToId();
		int dest = packet.getDestinationId();
		
		packet.setFromId(Routing.getIdBySession(session)); // 设置数据发送地址
		
		if (code == Routing.ROUTING_SERVER_ID) { // 如果是发送给连接管理器，则code需要设置为-1
			Routing.bindUser2Client(dest, toId);
			return;
		}
		
		// 先判断toId，来确定命令的地址，如果toId无效，再根据flag来找到服务器
		IoSession client = null;
		if (toId > 0) {
			client = Routing.getClient(toId);
			if (client == null) {
				logger.error("toId对应的服务器不存在：toId {}", toId);
			}else {
				client.write(message);
			}			
		} else if (code > 0){		
			IoSession backend = Routing.getBackend(code); // 根据flag 找到服务器
			if (backend != null) { // 设置发送地址
				backend.write(message);
			}else {
				logger.error("Flag对应的服务器不存在：flag {}", code);
			}
		}else if (dest > 0) {
			client = Routing.getUser(dest);
			if (client != null) {
				client.write(message);
			}else {
				logger.error("dest对应的用户不存在：dest {}", dest);
			}
		}else {
			logger.error("无效的信息发送，请指定flag or toId or destination");
		}
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		super.sessionClosed(session);
		
		// 进行连接释放
		Routing.removeUserClient(session);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
		
		// 记录连接
		Routing.addUserClient(session);
	}


}
