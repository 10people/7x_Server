package com.qx.test.main;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;

import pct.S_GetTaskRwardResult;
import pct.TestAllianceInfo;
import pct.TestBase;
import pct.TestBuyTili;
import pct.TestChengJiuPage;
import pct.TestDailyTask;
import pct.TestEnterPVE;
import pct.TestExplore;
import pct.TestGuanQiaInfo;
import pct.TestJiangHunInfo;
import pct.TestJzInfo;
import pct.TestLieFu;
import pct.TestLookMemmbers;
import pct.TestQiRiAward;
import pct.TestQiandao;
import pct.TestSaoDang;
import pct.TestTask;
import qxmobile.protobuf.Activity.ActivityAchievementResp;
import qxmobile.protobuf.Activity.ActivityGrowthFundRewardResp;
import qxmobile.protobuf.Activity.GrowLevel;
import qxmobile.protobuf.AllianceFightProtos.PlayerReviveRequest;
import qxmobile.protobuf.BagOperProtos.BagChangeInfo;
import qxmobile.protobuf.BagOperProtos.BagInfo;
import qxmobile.protobuf.BagOperProtos.BagItem;
import qxmobile.protobuf.BagOperProtos.EquipInfo;
import qxmobile.protobuf.BattleProg.InProgress;
import qxmobile.protobuf.BattleProg.InitProc;
import qxmobile.protobuf.Chat.ChatPct;
import qxmobile.protobuf.Chat.ChatPct.Channel;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.Explore.ExploreResp;
import qxmobile.protobuf.GameTask.TaskInfo;
import qxmobile.protobuf.GameTask.TaskProgress;
import qxmobile.protobuf.JewelProtos.EquipOperationReq;
import qxmobile.protobuf.MibaoProtos.MibaoInfo;
import qxmobile.protobuf.MibaoProtos.MibaoInfoResp;
import qxmobile.protobuf.MibaoProtos.MibaoLevelupReq;
import qxmobile.protobuf.MoBaiProto.MoBaiInfo;
import qxmobile.protobuf.MoBaiProto.MoBaiReq;
import qxmobile.protobuf.PlayerData.PlayerState;
import qxmobile.protobuf.PlayerData.State;
import qxmobile.protobuf.PveLevel.GuanQiaInfoRequest;
import qxmobile.protobuf.PveLevel.PvePageReq;
import qxmobile.protobuf.PvpProto.ConfirmExecuteReq;
import qxmobile.protobuf.Ranking.RankingReq;
import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Scene.EnterSceneConfirm;
import qxmobile.protobuf.Scene.ExitScene;
import qxmobile.protobuf.Scene.SpriteMove;
import qxmobile.protobuf.WuJiangProtos.HeroInfoReq;
import qxmobile.protobuf.XianShi.XinShouXSActivity;
import qxmobile.protobuf.ZhanDou.PveZhanDouInitReq;
import qxmobile.protobuf.ZhangHao.CreateRoleRequest;
import qxmobile.protobuf.ZhangHao.CreateRoleResponse;
import qxmobile.protobuf.ZhangHao.LoginReq;
import qxmobile.protobuf.ZhangHao.LoginRet;
import qxmobile.protobuf.ZhangHao.RegRet;
import qxmobile.protobuf.ZhangHao.RoleNameRequest;
import qxmobile.protobuf.ZhangHao.RoleNameResponse;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.template.BaseItem;
import com.manu.dynasty.util.ProtobufUtils;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.activity.XianShiConstont;
import com.qx.bag.Bag;
import com.qx.bag.BagGrid;
import com.qx.bag.EquipGrid;
import com.qx.pvp.PVPConstant;
import com.qx.test.message.MessageDispatcher;

/**
 * 在oper方法里写进入游戏后的操作
 * @author 康建虎
 *
 */
public class GameClient {
	public String accountName;
	public long lastMoveTime;
	public int masterUid;
	public IoSession session;
	public int lasdPveId;
	public boolean log = true;
	public int uid;
	public static GameClient useWhenSingle;
	public Map<Integer, TestBase> handlerMap = new HashMap<Integer, TestBase>();
	public void listen(int id, TestBase tb){
		handlerMap.put(id, tb);
	}
	
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
	public static String routerIP;
	private void regOrLogin() {
//		if(log)System.out.println("尝试注册:"+accountName);
//		RegReq.Builder regReq = RegReq.newBuilder();
//		regReq.setName(accountName);
//		session.write(regReq.build());
		HttpClient hc = new HttpClient();
		GetMethod gm = new GetMethod("http://"+routerIP+"/qxrouter/accountReg.jsp?name="+accountName+"&pwd=1");
//		GetMethod gm = new GetMethod("http://203.195.230.100:9091/qxrouter/accountReg.jsp?name="+accountName+"&pwd=1");
		try{
			hc.executeMethod(gm);
			String responseMess = gm.getResponseBodyAsString().trim();
			System.out.println(responseMess);
			if(responseMess.contains("用户名已被注册")){
				Main.autoDone(this);
				session.close(true);
				return;
			}
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
			ChoseScene(Main.sceneid);
			reqJzInfo();
			enterScene();
			break;
		case 2:
		case 2016:
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
		req.setPosX(-96f+next(5));
		req.setPosY(21f);
		req.setPosZ(14.933374f+next(5));
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
		ThreadLocalRandom r = ThreadLocalRandom.current();
		double x=-16.230766+r.nextInt(10); 
		double y=84.86189; 
		double z=-25.49981+r.nextInt(10); 
		req.setPosX((float) x);
//		req.setPosX(-7.998339f);
		req.setPosY((float) y);
		req.setPosZ((float) z);
//		req.setPosZ(-18.933374f);
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
		req.setRoleId(next(4)+1);
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
			ChoseScene(Main.sceneid);
			reqJzInfo();
			enterScene();
			reqPve();
//			enterShiLian();
//			enterYBScene();
//			enterLMZ();
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
//			shilianret();
//			yaBiao();
		}
	}
	public void shilianret() {
		//报告状态 押镖相关
		PlayerState.Builder b = PlayerState.newBuilder();
		b.setSState(State.State_LEAGUEOFCITY);
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
		double x=-16.230766+r.nextInt(10); 
		double y=84.86189; 
		double z=-25.49981+r.nextInt(10); 
		move.setPosX((float) x);
		move.setPosY((float) y);
		move.setPosZ((float) z);
//		session.write(move.build());
		session.write(move.build());
		//
//			session.write(PD.C_YABIAO_INFO_REQ);
//			session.write(PD.C_YABIAO_MENU_REQ);//请求押镖界面
//			session.write(PD.C_YABIAO_REQ);//开始押镖
	}
	public void yaBiao() {
		//报告状态 押镖相关
		PlayerState.Builder b = PlayerState.newBuilder();
		b.setSState(State.State_YABIAO);
		//
		session.write(b.build());		
//		move();
		//
//			session.write(PD.C_YABIAO_INFO_REQ);
//			session.write(PD.C_YABIAO_MENU_REQ);//请求押镖界面
//			session.write(PD.C_YABIAO_REQ);//开始押镖
	}

	public void move() {
		//move 调整在押镖场景中的位置
//			x = 211;
//			z = 135;//强制在一个地方
		ThreadLocalRandom r = ThreadLocalRandom.current();
		SpriteMove.Builder move = SpriteMove.newBuilder();
		move.setDir(r.nextInt(360));
		move.setUid(uid);
//			move.setPosX(184.29079f+);
//		int diff = r.nextInt(8) - 4;
//		int diffZ = r.nextInt(8) - 4;
////		System.out.println("diff is "+diff);
//		move.setPosX(x+diff/8f+(uid%10) * (r.nextInt(1) == 0 ? 1 : -1)  );
//		move.setPosY(4);
//		move.setPosZ(z+diffZ/5f+ (uid%10) * (r.nextInt(1) == 0 ? 1 : -1) );
		Integer dir = (Integer) session.getAttribute("dirX", 1);
		if(dir == 1){
			x+=0.5f;
			if(x>5)session.setAttribute("dirX", -1);
		}else{
			x -=0.5f;
			if(x<-3)session.setAttribute("dirX", 1);
		}
		if( ((int)x) % 20 == 0 ){dir = -dir; session.setAttribute("dirX", dir);}
		//
		dir = (Integer) session.getAttribute("dirX", null);
		if(dir == null || ((int)z) % 20 == 0){dir = -dir; session.setAttribute("dirZ", dir);}
		if(dir == 1){
			z+=0.5f;
			if(z>-5)session.setAttribute("dirZ", -1);
		}else{
			z -=0.5f;
			if(z<-19)session.setAttribute("dirZ", 1);
		}
		move.setPosX(x);
		move.setPosY(4);
		move.setPosZ(z);
		
		session.write(move.build());
//		session.write(move.build());
	}
	

	/**
	 * 测试协议
	 */
	private void oper() {
//		reqPve();
//		reqAlliance();
//		reqRank();
		reqTask();
//		reqAlliance();
//		reqAllianceMembers();
//		wuJiangInfo();//请求武将图鉴
//		guanQianInfo();
//		wuJiangKeJi();
//		taskListReq();
//		session.write(PD.C_InitProc);
//		session.write(PD.C_GET_JINENG_PEIYANG_QUALITY_REQ);
//		联盟抽奖信息();
//		session.write(PD.C_CLOSE_TAN_BAO_UI);
//		enterYBScene();
//		enterShiLian();
//		useItem();
//		聊天广播();
//		getMoBaiInfo();
//		getMoBaiAward();
//		reqQiandao();
//		reqAllianceMembers();
		reqQiRi(); 
//		reqDailyTask();
//		reqAutoSaoDang();
	}
	public TestTask testTask;
	public void reqTask(){
		if(testTask == null){
			session.setAttribute("CL", this);
			testTask = new TestTask();
			ProtobufUtils.prototypeMap.put((int)PD.NEW_MIBAO_INFO, MibaoInfoResp.getDefaultInstance());
			listen(PD.S_TaskList, testTask);	
			listen(PD.S_GetTaskRwardResult, new S_GetTaskRwardResult());	
			listen(PD.ZHANDOU_INIT_RESP, new TestEnterPVE());	
			listen(PD.PVE_GuanQia_Info, new TestGuanQiaInfo());	
			listen(PD.S_MIBAO_INFO_RESP, new TestJiangHunInfo());
			listen(PD.EXPLORE_RESP, new TestExplore());
			listen(PD.S_ZHANDOU_INIT_ERROR, new TestBuyTili());
			listen(PD.LieFu_Action_Info_Resp, new TestLieFu());
			listen(PD.S_DAILY_TASK_FINISH_INFORM, new TestDailyTask());
			listen(PD.S_PVE_SAO_DANG, new TestSaoDang());
			listen(PD.IMMEDIATELY_JOIN_RESP, new TestLookMemmbers());
			listen(PD.LOOK_MEMBERS_RESP, new TestLookMemmbers());
		}
		testTask.req(this);
	}
	public void reqJzInfo(){
		TestJzInfo t = new TestJzInfo();
		listen(PD.JunZhuInfoRet, t);
		System.out.println("TestJzInfo注册"+TestJzInfo.cnt++);
		session.write(PD.JunZhuInfoReq);
//		t.req(this);
	}
	long prePveTime;
	public void reqPve() {
		PvePageReq.Builder req = PvePageReq.newBuilder();
		req.setSSection(1);
		ProtobufMsg msg = new ProtobufMsg(PD.PVE_PAGE_REQ, req);
		session.write(msg);
		prePveTime = System.currentTimeMillis();
	}
	
	TestAllianceInfo testAllianceInfo;
	public void reqAlliance() {
		if(testAllianceInfo == null) {
			testAllianceInfo = new TestAllianceInfo();
			listen(PD.ALLIANCE_NON_RESP, testAllianceInfo);
			listen(PD.ALLIANCE_HAVE_RESP, testAllianceInfo);
		}
		testAllianceInfo.req(this);
	}
	
	long lookMemberStartTime = 0;
	public void reqAllianceMembers() {
		lookMemberStartTime = System.currentTimeMillis();
		session.write(PD.LOOK_MEMBERS);
		listen(PD.LOOK_MEMBERS_RESP, new TestLookMemmbers());
	}
	
	public void procReqMembersResp() {
		long cur = System.currentTimeMillis();
		long diff = cur - lookMemberStartTime;
		//if(diff > 100) {
			System.out.println("------- 请求联盟所有成员信息时间 :"+diff+" 毫秒");
		//}
	}
	
	long preRankTime;
	//7://重楼排行榜   4:// 过关榜 			3:// 百战榜			2:// 联盟榜
	int preRankIdx = 0;
	int[] rankType = {2,3,4,7};
	public void reqRank(){
		preRankTime = System.currentTimeMillis();
		RankingReq.Builder request = RankingReq.newBuilder();
		request.setGuojiaId(0);
		request.setName("");
		request.setPageNo(1);
		request.setRankType(rankType[preRankIdx]);
		session.write(new ProtobufMsg(PD.RANKING_REP, request));
	}
	public void rcvRank(){
		long cur = System.currentTimeMillis();
		long diff = cur - preRankTime;
		if(diff>100)
			System.out.println("----#-----请求排行耗时"+diff+",类型:"+rankType[preRankIdx]);
		preRankIdx+=1;
		preRankIdx = preRankIdx%rankType.length;
//		reqRank();
	}
	public void pveRet(Builder builder) {
		long cur = System.currentTimeMillis();
		long diff = cur - prePveTime;
		if(diff>100)
			System.out.println("------- pve diff :"+diff);
//		reqPve();
	}

	public void enterLMZ() {
		session.write(PD.C_ENTER_LMZ);
		//报告状态
		PlayerState.Builder b = PlayerState.newBuilder();
		b.setSState(State.State_LEAGUEOFCITY);
		session.write(b.build());
		//
		//lmzFuHuo();
	}
	public void lmzFuHuo() {
		PlayerReviveRequest.Builder req = PlayerReviveRequest.newBuilder();
		req.setType(40);
		ProtobufMsg msg = new ProtobufMsg(PD.LMZ_FuHuo, req);
		session.write(msg);
	}

	public void enterYBScene() {
		//先退出主城
//		exitMainCity();
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
		yaBiao();
	}

	public void exitMainCity() {
		ExitScene.Builder req = ExitScene.newBuilder();
		req.setUid(uid);
		
		session.write(req.build());
	}

	public void 联盟抽奖信息() {
		ProtobufUtils.prototypeMap.put(Integer.valueOf(PD.S_LM_CHOU_JIANG_INFO), ExploreResp.getDefaultInstance());
		listen(PD.S_LM_CHOU_JIANG_INFO, new TestBase(){
			@Override
			public void handle(int id, IoSession session, Builder builder,GameClient cl) {
				ExploreResp.Builder c = (ExploreResp.Builder)builder;
				System.out.println("剩余次数:"+c.getInfo().getRemainFreeCount());
			}
		});
		session.write(PD.C_LM_CHOU_JIANG_INFO);
	}

	public void 聊天广播() {
		listen(PD.S_Send_Chat, new TestBase(){
			@Override
			public void handle(int id, IoSession session, Builder builder,GameClient cl) {
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
//		EquipAddReq.Builder req = EquipAddReq.newBuilder();
//		req.setGridIndex(12);
//		session.write(req);
	}

	private void getMoBaiInfo() {
		listen(PD.S_MoBai_Info, new TestBase(){
			@Override
			public void handle(int id, IoSession session, Builder builder,GameClient cl) {
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
	
	
	
	
	
	//跳转至指定ID的主城副本
	public void JumpN(String string){
		String idstr = string.substring("jump".length());//截取字符串形式的场景ID
		Integer id = Integer.parseInt(idstr) ;//转化为int 对象
		if(id != null ){
			if(id<= 0){
				//场景ID不为空则发送协议到服务器，指定玩家再次进入主城时的副本ID
				ErrorMessage.Builder send = ErrorMessage.newBuilder() ;
				send.setErrorCode(id);
				ProtobufMsg msg = new ProtobufMsg();
				msg.builder = send ;
				msg.id =PD.C_CHOOSE_SCENE ;
				session.write(msg);
			}else{
				//场景ID大于零，不符合主城副本ID设计
				System.out.println("跳转场景必须为负数，格式为：jump+id 中间无空格");
				return ;	
			}
		}else{
			//id为空，输入非法
			System.out.println("跳转场景必须为负数，格式为：jump+id 中间无空格");
			return ;	
		}
		enterScene(); //调用进入主城方法发送进入主城协议
	}
	
	//选择进入ID为secneid的主城副本，如果senceid大于0，不符合主城副本ID设计
	public void ChoseScene(int sceneid){
		if(sceneid <= 0){
			ErrorMessage.Builder send = ErrorMessage.newBuilder() ;
			send.setErrorCode(sceneid);
			ProtobufMsg msg = new ProtobufMsg();
			msg.builder = send ;
			msg.id =PD.C_CHOOSE_SCENE ;
			session.write(msg);
		}
	}
	
	public void ReqBaiZhanMain(){
		session.write(PD.BAIZHAN_INFO_REQ);
	}
	
	public void ReqForEnemyList(){
		
		ConfirmExecuteReq.Builder req = ConfirmExecuteReq.newBuilder();
		req.setType(PVPConstant.get_junxian_enemys);
		req.setJunxianid(101);
		session.write(req.build());
	}
	
	public void AskForAllScene(){
		session.write(PD.C_SCENE_GETALL);
	}
	
	public void AskForBaoShi(){
		EquipOperationReq.Builder req = EquipOperationReq.newBuilder();
		req.setType(1) ;
		req.setEqulpId(290400903);
		session.write(req.build());
	}
	
//	public void chengjiuList(){
//		TestChengJiuPage t = new TestChengJiuPage();
//		listen(PD.S_ACTIVITY_ACHIEVEMENT_INFO_RESP, t);
//		t.req(this);
//	}
	
	public long equipDbId;
	public void readBag(Builder builder) {
		BagChangeInfo.Builder ret = (BagChangeInfo.Builder)builder;
		BagItem item = ret.getItems(0);
		if(item.getItemType() == BaseItem.TYPE_EQUIP){
			equipDbId = item.getDbId();
			System.out.println("收到装备:"+equipDbId);
		}
	}

	public void readMBV2(Builder builder) {
		Long pre = (Long) session.getAttribute("LastMBID", -1L);
		MibaoInfoResp.Builder ret = (MibaoInfoResp.Builder)builder;
		Optional<MibaoInfo> op = ret.getMiBaoListList().stream().filter(t->
			t.getNeedSuipianNum()<=t.getSuiPianNum() && t.getStar()==0
			&& t.getDbId() != pre && t.getSuiPianNum()>0
		).findAny();
		if(op.isPresent()){
			MibaoInfo t = op.get();
			if(t.getDbId()<=0){
				//还没有保存到数据库，所以dbid没有
				session.write(PD.NEW_MIBAO_INFO);
				return;
			}
			testTask.jiHuoMiBao(this, op.get().getDbId());
			if(op.get().getMiBaoId()%10==9){
				testTask.activeMiShu(this);
			}
		}
	}

	public void readMB(Builder builder) {
		if(testTask == null)return;
		MibaoInfoResp.Builder ret = (MibaoInfoResp.Builder)builder;
		ret.getMiBaoListList().stream().forEach(m->{
			if(ret.getLevelPoint()>0 && m.getLevel()>0){
				MibaoLevelupReq.Builder req = MibaoLevelupReq.newBuilder();
				req.setMibaoId(m.getMiBaoId());
				testTask.send(this, PD.C_MIBAO_LEVELUP_REQ, req);
			}
		});
	}

	public void readChengJiuList(Builder builder) {
		ActivityAchievementResp.Builder ret = (ActivityAchievementResp.Builder)builder;
		GrowLevel lv = ret.getLeveListList().stream().findFirst().get();
		//////////
		ActivityGrowthFundRewardResp.Builder req = ActivityGrowthFundRewardResp.newBuilder();
		req.setLevel(lv.getId());
//		1519001
		this.session.write(new ProtobufMsg(PD.C_ACTIVITY_ACHIEVEMENT_GET_REQ, req));
		System.out.println("请求领取成就奖励:"+lv.getId());
	}
	
	public void saveBag(Builder builder){
		List<BagGrid> bag = new LinkedList<BagGrid>();
		BagInfo.Builder resp = (BagInfo.Builder)builder ;
		List<BagItem.Builder> bagList = resp.getItemsBuilderList();
		for( BagItem.Builder info : bagList){
			BagGrid bg = new BagGrid();
			bg.dbId = info.getDbId();
			bg.instId = info.getInstId();
			bg.cnt = info.getCnt();
			bg.itemId = info.getItemId();
			bg.type = info.getItemType();
			bag.add(bg);
		}
		this.session.setAttribute("BAG" , bag);
		session.write(PD.C_TaskReq);
	}
	
	
	public void saveEquip(Builder builder){
		List<EquipGrid> equips = new LinkedList<EquipGrid>();
		EquipInfo.Builder resp = (EquipInfo.Builder)builder ;
		List<BagItem.Builder> bagList = resp.getItemsBuilderList();
		for( BagItem.Builder info : bagList){
			EquipGrid bg = new EquipGrid();
			bg.dbId = info.getDbId();
			bg.instId = info.getInstId();
			bg.itemId = info.getItemId();
			equips.add(bg);
		}
		session.setAttribute("EQUIP" , equips);
		session.write(PD.C_TaskReq);
	}
	
	public void reqQiandao(){
		listen(PD.S_GET_QIANDAO_RESP, new TestQiandao());
		session.write(PD.C_GET_QIANDAO_REQ);
	}
	
	public void reqDailyTask(){
		session.write(PD.C_DAILY_TASK_LIST_REQ);
		listen(PD.S_DAILY_TASK_LIST_RESP, new TestDailyTask());
		listen(PD.S_DAILY_TASK_GET_REWARD_RESP, new TestDailyTask());
		listen(PD.S_DAILY_TASK_FINISH_INFORM, new TestDailyTask());
	}
	public void reqQiRi(){
		listen(PD.S_XINSHOU_XIANSHI_INFO_RESP, new TestQiRiAward());
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.C_XINSHOU_XIANSHI_INFO_REQ;
		XinShouXSActivity.Builder req = XinShouXSActivity.newBuilder();
		req.setTypeId(XianShiConstont.QIRIQIANDAO_TYPE);
		msg.builder = req;
		session.write(msg);
		reqQiandao();
	}
	
	public void reqAutoSaoDang(){
     	listen(PD.PVE_PAGE_RET, new TestSaoDang());
     	listen(PD.S_PVE_SAO_DANG, new TestSaoDang());
		new TestSaoDang().reqGuanQiaList(session);
		listen(PD.JunZhuInfoRet, new TestSaoDang());
	}
	
}
