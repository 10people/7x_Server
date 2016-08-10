package com.qx.test.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.manu.dynasty.util.ProtobufUtils;
import com.manu.network.PD;
import com.manu.network.ProtoBuffDecoder;
import com.manu.network.TXCodecFactory;
import com.manu.network.msg.ProtobufMsg;

import pct.TestDailyTask;
import pct.TestTask;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;


/**
 *java -jar -Djava.ext.dirs=libs -jar st.jar k3 >/dev/null
 *tcpkill -i lo -9 dst port 8587 and src port  34059 &

 *sar -n DEV 1 100 
 */
public class Main {
	public static AtomicInteger watchCnt = new AtomicInteger(0);;
	public static AtomicInteger totalLaunch = new AtomicInteger(0);;
	public static AtomicInteger conFailCnt = new AtomicInteger(0);;
	public static AtomicInteger regOkCnt = new AtomicInteger(0);
	public static AtomicInteger loginOkCnt = new AtomicInteger(0);
	public static AtomicInteger rndNameCnt = new AtomicInteger(0);
	public static AtomicInteger createRoleOkCnt = new AtomicInteger(0);
	public static AtomicInteger enterSceneCnt = new AtomicInteger(0);
	public static long startTime;
	public static IoConnector net;
	public static String hostname;
	public static boolean exitWhenFinish = false;
	static String head;
	static  InetSocketAddress addr;
	static int port;
	static String forceName;
	static int sceneid = 1 ; 	//客户端默认指定服务器主城副本ID，大于零时无效
	public static void main(String[] args) throws Exception{
		net = setup();
		/*内网*/
		System.out.println("请选择服务器：0内网；1kjh；");
		int i = System.in.read();
		switch(i){
		case '0':
			GameClient.routerIP = "192.168.3.80:8090";hostname = "192.168.3.80";port=8586;TestTask.which=0;
			break;
		case '1':
			//康建虎 
			GameClient.routerIP = "192.168.3.80:8090";hostname = "192.168.0.83";port=8586;TestTask.which=0;
			break;
		}
		System.in.read();System.in.read();//读回车
		//江源
//		GameClient.routerIP = "192.168.3.80:8090";hostname = "192.168.1.96";port=8586;TestTask.which=3;
		/*体验*/
//		GameClient.routerIP = "203.195.230.100:9090";hostname = "203.195.204.128";port=8587;
		/*测试*/
//		GameClient.routerIP = "203.195.230.100:9091";hostname = "203.195.230.100";port=8587;
		/*万旭宏*/
//		GameClient.routerIP = "192.168.3.80:8090";hostname = "192.168.1.100";port=8586;TestTask.which=2;
		int cnt = 1;
//		forceName = "719est358501";
		head = "82A";
		head +=new Random().nextInt(99999);
		addr = new InetSocketAddress(hostname, port);//外网测试服8587
		startTime = System.currentTimeMillis();
//		if(args != null && args.length==1){
//			cnt = Integer.parseInt(args[0]);
//			System.out.println("次数设定:"+cnt);
//			makeClient(cnt, head, addr);
//		}
		new Thread(()->readCmd()).start();
		do{
			long cur = System.currentTimeMillis();
			System.out.println("时间:"+(cur - startTime)+
					" 连接:"+net.getManagedSessionCount()+
					" 失败连接:"+conFailCnt+
					" 已注册:"+regOkCnt+
					" 已登录:"+loginOkCnt+
					" 取名字:"+rndNameCnt+
					" 已创建角色:"+createRoleOkCnt+
					" 已进入场景:"+enterSceneCnt
					);
			Thread.sleep(1000*1);
			break;//------------------------------------------------------
		}while(net.isActive());
	}

	private static void readCmd() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		do{
			System.out.println("请输入数量:");
			String line = null;
			try {
				line = br.readLine();
			} catch (IOException e) {
			}
			try{
			parseCmd(line);
			}catch(Exception e){
				e.printStackTrace();
			}
		}while(true);
	}

	public static void parseCmd(String line) {
		if(line == null){
		}else if(line.matches("\\d+")){
			makeClient(Integer.parseInt(line), head, addr);
		}else if(line.startsWith("run")){
			String pre = forceName;
			forceName = line.substring(3);
			makeClient(1, head, addr);
			forceName = pre;
		}else if(line.startsWith("bx")){
			ErrorMessage.Builder m = ErrorMessage.newBuilder();
			m.setCmd(0);
			m.setErrorCode(Integer.parseInt(line.substring(2)));
			IoSession session = net.getManagedSessions().values().iterator().next();
			session.write(new ProtobufMsg(PD.C_GET_BAO_XIANG, m));
			System.out.printf("尝试开开十连宝箱  %d\n",m.getErrorCode());
		}else if(line.startsWith("show")){
			System.out.println("总连接数:"+net.getManagedSessionCount());
		}else if(line.startsWith("kill")){
			int cnt = Integer.parseInt(line.substring(4));
			Iterator<IoSession> it = net.getManagedSessions().values().iterator();
			while(it.hasNext() && cnt > 0){
				cnt --;
				it.next().close(false);
			}
		}else if(line.equals("lm")){
			GameClient.useWhenSingle.reqAlliance();
		}else if(line.equals("mc")){
			GameClient.useWhenSingle.enterScene();
		}else if(line.equals("sl")){
			GameClient.useWhenSingle.enterShiLian();
		}else if(line.startsWith("chose")){
			ChoseSceneID(line);//输入内容实例：chose-1  代表客户端之后登录玩家默认进入-1号主城副本
		}else if(line.startsWith("jump")){
			GameClient.useWhenSingle.JumpN(line);//输入内容实例：jump-2  代表控制最后一名登录玩家跳转进入-2号主城副本
		}else if(line.startsWith("getall")){
			GameClient.useWhenSingle.AskForAllScene();//输入内容实例：jump-2  代表控制最后一名登录玩家跳转进入-2号主城副本
		}else if(line.startsWith("getjunxian")){
			GameClient.useWhenSingle.ReqBaiZhanMain();
			GameClient.useWhenSingle.ReqForEnemyList();
		}else if(line.startsWith("login")){
			loginByAccount();//尝试登陆玩家账号
		}else if(line.startsWith("baoshi")){
			GameClient.useWhenSingle.AskForBaoShi();//尝试登陆玩家账号
		}else if(line.equals("lmz")){
			GameClient.useWhenSingle.enterLMZ();
		}else if(line.equals("fh")){
			GameClient.useWhenSingle.lmzFuHuo();
		}else if(line.equals("move")){
			GameClient.useWhenSingle.move();
		}else if(line.equals("rank")){
			GameClient.useWhenSingle.reqRank();
		}else if(line.equalsIgnoreCase("jzInfo")){
			GameClient.useWhenSingle.reqJzInfo();
		}else if(line.equals("tanbao")){
			GameClient.useWhenSingle.testTask.tryGetItem(GameClient.useWhenSingle);
		}else if(line.equals("task")){
			GameClient.useWhenSingle.reqTask();
			GameClient.useWhenSingle.testTask.tryIds.clear();
			GameClient.useWhenSingle.lasdPveId = 0;
		}else if(line.equals("accinfo")){
			System.out.println(GameClient.useWhenSingle.accountName);
		}else if(line.startsWith("slow")){
			int cnt = Integer.parseInt(line.substring(4));
			for(int i=0;i<cnt;i++){
				makeClient(1, head, addr);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else if(line.equals("pve")){
			GameClient.useWhenSingle.reqPve();
		}else if(line.equalsIgnoreCase("autoNext")){
			autoNext = ! autoNext;
			System.out.println("自动开启下个机器人:"+autoNext);
		}else if(line.equalsIgnoreCase("dailytask")){
//			GameClient.useWhenSingle.reqDailyTask();
		}
		else{
			System.out.println("输入的不是数字");
		}
	}

	public static void makeClient(int cnt, String head, final InetSocketAddress addr) {
		watchCnt.addAndGet(cnt);
		for(int i = 0; i < cnt; i++) {
			String accName = head+watchCnt.incrementAndGet();
			if(forceName != null){
				accName = forceName;
			}
			final GameClient c = new GameClient(accName);
			c.launch(net, addr);
			if(forceName != null){
				break;
			}
		}
	}
	
	public static void loginByAccount(){
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("请输入账号:");
		String line = null;
		try {
			line = br.readLine();
		} catch (IOException e) {
		}
		final Phaser p = new Phaser(1);
		final GameClient c = new GameClient(line);
		Runnable r = new Runnable() {
			public void run() {
				p.arriveAndAwaitAdvance();
				c.launch(net, addr);;
			}
		};
		p.register();
		new Thread(r,c.accountName).start();
		p.arrive();
	}
	
	public static void finish(){
		long end = System.currentTimeMillis();
		System.out.println("共使用时间 "+(end-startTime));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(exitWhenFinish)
			net.dispose();
	}

	public static IoConnector setup() {
		PD.init();
		ProtobufUtils.prototypeMap.put((int)PD.RED_NOTICE, ErrorMessage.getDefaultInstance());
		final IoConnector connector = new NioSocketConnector(10);
		connector.setConnectTimeoutMillis(5000);
		final ProtoBuffDecoder protoBuffDecoder = new ProtoBuffDecoder();
		ProtocolCodecFactory codecFactory = new TXCodecFactory(){
			@Override
			public ProtocolDecoder getDecoder(IoSession s) throws Exception {
				return protoBuffDecoder;
			}
		};
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(codecFactory));
		ClientHandler clientHandler = new ClientHandler();
		connector.setHandler(clientHandler);
		connector.getSessionConfig().setBothIdleTime(5);
		return connector;
	}
	
	//修改客户端指定服务器副本ID，为负数时生效，玩家登录时直接进入ID对应主城副本，无论是否有未满副本
	public static void ChoseSceneID(String string ){
		String idstr = string.substring("chose".length());//截取字符串形式的场景ID
		Integer id = Integer.parseInt(idstr) ;//转化为int对象
		if(id != null ){
			sceneid = id ;//不为空则赋值
		}else{
			System.out.println("场景ID输入错误，正确格式为：chose+id 中间无空格");//为空输出调试信息
		}
	}

	public static boolean autoNext = false;
	public static void autoDone(GameClient cl) {
		if(autoNext==false){
			return;
		}
		cl.session.close(false);
		makeClient(1, head, addr);
	}
}
