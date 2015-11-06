package com.qx.test.main;

import java.net.InetSocketAddress;
import java.util.Random;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import qxmobile.protobuf.BattlePveResult.BattlePveResultReq;
import qxmobile.protobuf.GameTask.TaskInfo;
import qxmobile.protobuf.GameTask.TaskProgress;
import qxmobile.protobuf.PlayerData.PlayerState;
import qxmobile.protobuf.PlayerData.State;
import qxmobile.protobuf.PveLevel.GuanQiaInfoRequest;
import qxmobile.protobuf.Scene.EnterScene;
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
import com.manu.network.PD;
import com.manu.network.ProtoBuffDecoder;
import com.manu.network.TXCodecFactory;
import com.qx.test.message.MessageDispatcher;

public class GameClient {
	private String accountName;
	private IoSession session;
	public boolean log = true;
	
	public GameClient(String accountName) {
		this.accountName = accountName;
	}

	public void launch(final IoConnector connector, InetSocketAddress addr) {
		ConnectFuture future = connector.connect(addr);
		future.addListener(new IoFutureListener<ConnectFuture>() {

			@Override
			public void operationComplete(ConnectFuture future) {
				if(future.isConnected()==false){
					Main.conFailCnt.incrementAndGet();
					return;
				}
				session = future.getSession();
				session.setAttribute("router",new MessageDispatcher(GameClient.this));
				session.write("tgw_l7_forward\r\nHost:app12345.qzoneapp.com:80\r\n\r\n");
				regOrLogin();
			}
		});
	}

	private void regOrLogin() {
		if(log)System.out.println("尝试注册:"+accountName);
		RegReq.Builder regReq = RegReq.newBuilder();
		regReq.setName(accountName);
		session.write(regReq.build());
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
		case 1:
			enterScene();
			break;
		case 2:
//			createRole();
			randomNameFromServer();
			break;
		case 3:
			if(log)System.out.println("登录失败");
			break;
		default:
			if(log)System.out.println("未知的登录返回码"+code);
			break;
		}
	}

	public void enterScene() {
		//
		EnterScene.Builder req = EnterScene.newBuilder();
		req.setUid(1);
		req.setSenderName(accountName);
		session.write(req.build());
		if(log)System.out.println("发起进入场景。");
		//报告状态
		PlayerState.Builder b = PlayerState.newBuilder();
		b.setSState(State.State_LEAGUEOFCITY);
		session.write(b.build());
		//
		oper();
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
		req.setGuoJiaId(new Random().nextInt(7)+1);
		session.write(req.build());
	}
	Random rnd = new Random();
	public void randNameRet(Builder builder) {
		RoleNameResponse.Builder ret = (qxmobile.protobuf.ZhangHao.RoleNameResponse.Builder) builder;
		String name = ret.getRoleName();
		if(log)System.out.println("获得随机角色名:"+name);
		Main.rndNameCnt.incrementAndGet();
		createRole(name+rnd.nextInt(9999));
	}

	public void createRoleRet(Builder builder) {
		CreateRoleResponse.Builder ret = (qxmobile.protobuf.ZhangHao.CreateRoleResponse.Builder) builder;
		if(log)System.out.println("创建角色结果:"+ret.getIsSucceed()+":"+ret.getMsg());
		if(ret.getIsSucceed()){
			enterScene();
			Main.createRoleOkCnt.incrementAndGet();
		}else{
			createRole("fail"+accountName);
		}
	}

	public void enterSceneRet(Builder builder) {
		if(log)System.out.println("进入场景OK");
		Main.enterSceneCnt.incrementAndGet();
		int cnt = Main.watchCnt.decrementAndGet();
		if(cnt == 0){
			Main.finish();
		}
	}
	

	/**
	 * 测试协议
	 */
	private void oper() {
//		wuJiangInfo();//请求武将图鉴
//		guanQianInfo();
//		wuJiangKeJi();
		taskListReq();
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
		BattlePveResultReq.Builder rz = BattlePveResultReq.newBuilder();
		rz.setResult(1);
		rz.setUid(0);
		rz.setBigId(0);
		rz.setSmaId(0);
		session.write(rz.build());
	}

	private void wuJiangInfo() {
		HeroInfoReq.Builder req = HeroInfoReq.newBuilder();
		req.setUid(000);
		session.write(req.build());
	}
}
