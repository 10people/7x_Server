package com.qx.test.main;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;

import pct.TestBase;
import qxmobile.protobuf.BagOperProtos.EquipAddReq;
import qxmobile.protobuf.BattleProg.InProgress;
import qxmobile.protobuf.BattleProg.InitProc;
import qxmobile.protobuf.Chat.ChatPct;
import qxmobile.protobuf.Chat.ChatPct.Channel;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.Explore.ExploreResp;
import qxmobile.protobuf.GameTask.TaskInfo;
import qxmobile.protobuf.GameTask.TaskProgress;
import qxmobile.protobuf.MoBaiProto.MoBaiInfo;
import qxmobile.protobuf.MoBaiProto.MoBaiReq;
import qxmobile.protobuf.PlayerData.PlayerState;
import qxmobile.protobuf.PlayerData.State;
import qxmobile.protobuf.PveLevel.GuanQiaInfoRequest;
import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Scene.EnterSceneConfirm;
import qxmobile.protobuf.Scene.ExitScene;
import qxmobile.protobuf.Scene.SpriteMove;
import qxmobile.protobuf.UpActionProto.UpAction_C_getData;
import qxmobile.protobuf.WuJiangProtos.HeroInfoReq;
import qxmobile.protobuf.ZhanDou.PveZhanDouInitReq;
import qxmobile.protobuf.ZhangHao.CreateRoleRequest;
import qxmobile.protobuf.ZhangHao.CreateRoleResponse;
import qxmobile.protobuf.ZhangHao.LoginReq;
import qxmobile.protobuf.ZhangHao.LoginRet;
import qxmobile.protobuf.ZhangHao.RegReq;
import qxmobile.protobuf.ZhangHao.RegRet;
import qxmobile.protobuf.ZhangHao.RoleNameRequest;
import qxmobile.protobuf.ZhangHao.RoleNameResponse;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.util.ProtobufUtils;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.test.message.MessageDispatcher;

/**
 * 在oper方法里写进入游戏后的操作
 * @author 康建虎
 *
 */
public class GameClient {
	public String accountName;
	public IoSession session;
	public boolean log = true;
	public int uid;
	public static GameClient useWhenSingle;
	
	public GameClient(String accountName) {
		useWhenSingle = this;
		this.accountName = accountName;
//		this.accountName = "t1";
	}

	public void launch(final IoConnector connector, InetSocketAddress addr) {
		ConnectFuture future = connector.connect(addr);
		future.addListener(new IoFutureListener<ConnectFuture>() {

			@Override
			public void operationComplete(ConnectFuture future) {
				if(future.isConnected()==false){
					Main.conFailCnt.incrementAndGet();
					System.out.println("连接失败");
					return;
				}
				session = future.getSession();
				session.setAttribute("router",new MessageDispatcher(GameClient.this));
				session.write("tgw_l7_forward\r\nHost:app12345.qzoneapp.com:80\r\n\r\n");
				regOrLogin();
//				login(session);
			}
		});
	}

	private void regOrLogin() {
//		if(log)System.out.println("尝试注册:"+accountName);
//		RegReq.Builder regReq = RegReq.newBuilder();
//		regReq.setName(accountName);
//		session.write(regReq.build());
		HttpClient hc = new HttpClient();
		GetMethod gm = new GetMethod("http://192.168.3.80:8090/qxrouter/accountReg.jsp?name="+accountName+"&pwd=1");
		try{
			hc.executeMethod(gm);
			String responseMess = gm.getResponseBodyAsString().trim();
			System.out.println(responseMess);
			login(session);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void regResult(IoSession session, Builder builder){
		RegRet regRet = (RegRet) builder.build();
		int accId = regRet.getUid();
		String name = regRet.getName();
//			regAccount(session, name);
//			long end = System.currentTimeMillis();
////			if(log)System.out.println(accountName + " 进行1000次请求，总用时间：" + (end - Main.startTime.get()));
////			Main.time = Main.time + (end - clientHandler.getStart());
//			Main.threadCount.incrementAndGet();
//			if(Main.threadCount.intValue() == 100){
//				Main.endTime = System.currentTimeMillis();
//				System.err.println("所有线程执行完毕，所用时间：" + (Main.endTime - Main.startTime.get()));
//			}
//			System.err.println("多线程总用时间：" + Main.time);
		if(accId < 0){
			if(log)System.out.println(name + "：注册失败");
			return;
		}
		if(log)System.out.println(name + "注册成功，id：" + accId);
		Main.regOkCnt.incrementAndGet();
		//
		login(session);
	}

	public void login(IoSession session) {
		LoginReq.Builder login = LoginReq.newBuilder();
		login.setName(accountName);
		session.write(login.build());
	}

	public void loginRet(IoSession session2, int id, Builder builder) {
		LoginRet.Builder ret = (qxmobile.protobuf.ZhangHao.LoginRet.Builder) builder;
		int code = ret.getCode();
		if(log)System.out.println("登录结果:"+code+":"+ret.getMsg());
		//1，登录成功并创建过角色。2，登录成功并未创建过角色。3,登录失败
		Main.loginOkCnt.incrementAndGet();
		switch(code){
		case 100:
		case 1:
			enterScene();
			break;
		case 2:
//			createRole();
//			randomNameFromServer();
			createRole(getRandomString(6));
			break;
		case 3:
			if(log)System.out.println("登录失败");
			break;
		default:
			if(log)System.out.println("未知的登录返回码"+code);
			break;
		}
	}
	//随机生产名字
		public static String getRandomString(int length) { 
		    String base = "abcdefghijklmnopqrstuvwxyz0123456789俞伯牙席潮海丁克曾管正学管虎管谟业管仲陈伟霆王世充李渊杨坚郭树清李鸿忠王穗明刘铁男李登辉彭长健邓鸿王中军景百孚赵永亮陆兆禧严介和郁亮茅于轼王小波冯唐";   
		    StringBuffer sb = new StringBuffer();   
		    for (int i = 0; i < length; i++) {   
		        int number = next(base.length());   
		        sb.append(base.charAt(number));   
		    }   
		    return sb.toString();   
		 }

	public void enterScene() {
		//
		EnterScene.Builder req = EnterScene.newBuilder();
		req.setUid(1);
		req.setSenderName(accountName);
		req.setJzId(0);
		req.setPosX(-7.998339f);
		req.setPosY(4.7422743f);
		req.setPosZ(-18.933374f);
		session.write(req.build());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(PD.OPEN_ShiLian_FuBen), ErrorMessage.getDefaultInstance());
		if(log)System.out.println("发起进入主城。");
		//报告状态
		PlayerState.Builder b = PlayerState.newBuilder();
		b.setSState(State.State_LEAGUEOFCITY);
		session.write(b.build());
		//
	}
	public void enterShiLian() {
		//
		EnterScene.Builder req = EnterScene.newBuilder();
		req.setUid(1);
		req.setSenderName(accountName);
		req.setJzId(0);
		req.setPosX(-7.998339f);
		req.setPosY(4.7422743f);
		req.setPosZ(-18.933374f);
//		session.write(req.build());
		ProtobufUtils.prototypeMap.put(Integer.valueOf(PD.OPEN_ShiLian_FuBen), ErrorMessage.getDefaultInstance());
		session.write(new ProtobufMsg(PD.Enter_TBBXScene, req));
		if(log)System.out.println("发起进入十连副本。");
		//报告状态
				PlayerState.Builder b = PlayerState.newBuilder();
				b.setSState(State.State_LEAGUEOFCITY);
				session.write(b.build());
				//
	}

	public void randomNameFromServer() {
		RoleNameRequest.Builder req = RoleNameRequest.newBuilder();
		req.setRoleId(1);
		session.write(req.build());
		if(log)System.out.println("请求随机角色名称");
	}

	public void createRole(String name) {
		CreateRoleRequest.Builder req =  CreateRoleRequest.newBuilder();
		req.setRoleName(name);
		req.setRoleId(1);
		req.setGuoJiaId(next(7)+1);
		session.write(req.build());
	}
	static Random rnd = new Random();
	static int next(int v){
		synchronized (rnd) {
			return rnd.nextInt(v);
		}
	}
	public long jzId;
	public void randNameRet(Builder builder) {
		RoleNameResponse.Builder ret = (qxmobile.protobuf.ZhangHao.RoleNameResponse.Builder) builder;
		String name = ret.getRoleName();
		if(log)System.out.println("获得随机角色名:"+name);
		Main.rndNameCnt.incrementAndGet();
		createRole(name+next(9999));
	}

	public void createRoleRet(Builder builder) {
		CreateRoleResponse.Builder ret = (qxmobile.protobuf.ZhangHao.CreateRoleResponse.Builder) builder;
		if(log)System.out.println("创建角色结果:"+ret.getIsSucceed()+":"+ret.getMsg());
		if(ret.getIsSucceed()){
			enterScene();
			Main.createRoleOkCnt.incrementAndGet();
		}else{
			createRole(getRandomString(6));
		}
	}
	float x,y,z; 
	public void enterSceneRet(Builder builder) {
		if(log)System.out.println("进入场景OK");
		Main.enterSceneCnt.incrementAndGet();
		EnterSceneConfirm.Builder ret = (EnterSceneConfirm.Builder)builder;
		x = ret.getPosX();
		y = 3.405495f;//ret.getPosY();
		z = ret.getPosZ();
		if(uid <= 0){
			uid = ret.getUid();
			int cnt = Main.watchCnt.decrementAndGet();
			if(cnt == 0){
				Main.finish();
			}
			//
			oper();
		}else{
			//
//			yaBiao();
		}
	}

	public void yaBiao() {
		//报告状态 押镖相关
		PlayerState.Builder b = PlayerState.newBuilder();
		b.setSState(State.State_YABIAO);
		//
		session.write(b.build());		
		//move 调整在押镖场景中的位置
//			x = 211;
//			z = 135;//强制在一个地方
		SpriteMove.Builder move = SpriteMove.newBuilder();
		move.setDir(0);
		move.setUid(uid);
//			move.setPosX(184.29079f+);
		ThreadLocalRandom r = ThreadLocalRandom.current();
		int diff = r.nextInt(10) - 5;
		int diffZ = r.nextInt(10) - 5;
		System.out.println("diff is "+diff);
		move.setPosX(x+diff);
		move.setPosY(4);
		move.setPosZ(z+diffZ);
		session.write(move.build());
		session.write(move.build());
		//
//			session.write(PD.C_YABIAO_INFO_REQ);
//			session.write(PD.C_YABIAO_MENU_REQ);//请求押镖界面
//			session.write(PD.C_YABIAO_REQ);//开始押镖
	}
	

	/**
	 * 测试协议
	 */
	private void oper() {
//		wuJiangInfo();//请求武将图鉴
//		guanQianInfo();
//		wuJiangKeJi();
//		taskListReq();
//		session.write(PD.C_InitProc);
//		session.write(PD.C_GET_JINENG_PEIYANG_QUALITY_REQ);
//		联盟抽奖信息();
//		session.write(PD.C_CLOSE_TAN_BAO_UI);
//		enterYBScene();
//		useItem();
//		聊天广播();
//		getMoBaiInfo();
//		getMoBaiAward();
	}

	public void enterYBScene() {
		//先退出主城
		exitMainCity();
//		if(11>1)return;
		//
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.Enter_YBScene;
		EnterScene.Builder req = EnterScene.newBuilder();
		req.setUid(uid);
		req.setSenderName(accountName);
		req.setJzId(jzId);
		req.setPosX(x);
		req.setPosY(y);
		req.setPosZ(z);
		msg.builder = req;
		session.write(msg);
		if(log)System.out.println("发起进入场景。");
	}

	public void exitMainCity() {
		ExitScene.Builder req = ExitScene.newBuilder();
		req.setUid(uid);
		
		session.write(req.build());
	}

	public void 联盟抽奖信息() {
		ProtobufUtils.prototypeMap.put(Integer.valueOf(PD.S_LM_CHOU_JIANG_INFO), ExploreResp.getDefaultInstance());
		MessageDispatcher.listen(PD.S_LM_CHOU_JIANG_INFO, new TestBase(){
			@Override
			public void handle(int id, IoSession session, Builder builder) {
				ExploreResp.Builder c = (ExploreResp.Builder)builder;
				System.out.println("剩余次数:"+c.getInfo().getRemainFreeCount());
			}
		});
		session.write(PD.C_LM_CHOU_JIANG_INFO);
	}

	public void 聊天广播() {
		MessageDispatcher.listen(PD.S_Send_Chat, new TestBase(){
			@Override
			public void handle(int id, IoSession session, Builder builder) {
				ChatPct.Builder c = (ChatPct.Builder)builder;
				System.out.println(c.getSenderName()+" say: "+ c.getContent());
			}
		});
		ProtobufUtils.prototypeMap.put(Integer.valueOf(PD.S_Send_Chat), ChatPct.getDefaultInstance());
		ChatPct.Builder cm = ChatPct.newBuilder();
		cm.setContent("来自玩家发起的广播");
		cm.setSenderId(0);
		cm.setSenderName("王尼玛");
		cm.setChannel(Channel.Broadcast);
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.C_Send_Chat;
		msg.builder = cm;
		session.write(msg);
	}

	private void useItem() {
		EquipAddReq.Builder req = EquipAddReq.newBuilder();
		req.setGridIndex(12);
		session.write(req);
	}

	private void getMoBaiInfo() {
		MessageDispatcher.listen(PD.S_MoBai_Info, new TestBase(){
			@Override
			public void handle(int id, IoSession session, Builder builder) {
				MoBaiInfo.Builder mb = (qxmobile.protobuf.MoBaiProto.MoBaiInfo.Builder) builder;
				System.out.println(mb.build().toString());
			}
		});
		session.write(PD.C_MoBai_Info);
	}

	public void getMoBaiAward() {
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.C_GET_MOBAI_AWARD;
		MoBaiReq.Builder mb = MoBaiReq.newBuilder();
		mb.setCmd(2);
		msg.builder = mb;
		session.write(msg);
	}

	private void taskListReq() {
		session.write(PD.C_TaskReq);
		//finish task
		TaskInfo.Builder b = TaskInfo.newBuilder();
		b.setId(100001);
		b.setProgress(1);
		TaskProgress.Builder r = TaskProgress.newBuilder();
		r.setTask(b);
		session.write(r.build());
	}

	private void wuJiangKeJi() {
		session.write(PD.WUJIANG_TECHINFO_REQ);
	}

	private void guanQianInfo() {
		GuanQiaInfoRequest.Builder req = GuanQiaInfoRequest.newBuilder();
		req.setGuanQiaId(100203);
		session.write(req.build());
		//
		PveZhanDouInitReq.Builder zd = PveZhanDouInitReq.newBuilder();
		zd.setChapterId(100103);
		session.write(zd.build());
		//
	}

	private void wuJiangInfo() {
		HeroInfoReq.Builder req = HeroInfoReq.newBuilder();
		req.setUid(000);
		session.write(req.build());
	}

	public void antiCheat(Builder builder) {
		InitProc.Builder ret = (qxmobile.protobuf.BattleProg.InitProc.Builder) builder	;		
//		required int32 a = 1;//下一次请求密语使用的协议号。
//		required string b = 2;  //服务器加密后的数据，下一次请求密语时发给服务器。
//		required int32 c = 3;//收到次协议后多少毫秒，再次给服务器发送密语请求
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = ret.getA();
		InProgress.Builder r = InProgress.newBuilder();
		r.setA(ret.getB());
		r.setB("#");
		msg.builder = r;
		System.out.println("密语"+ret.getB()+" 协议号:"+msg.id);
		try {
			Thread.sleep(ret.getC()+1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(msg.id%4==0){
			msg = new ProtobufMsg();
			msg.id = PD.C_zlgdlc;
			InProgress.Builder r2 = InProgress.newBuilder();
			r2.setA(ret.getB());
			r2.setB("#");
			msg.builder = r2;
			session.write(msg);//终止
			System.out.println("终止密语过程");
		}else{
			session.write(msg);			
		}
	}
}
