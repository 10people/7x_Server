package pct;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.mina.core.session.IoSession;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.template.PveTemp;
import com.manu.dynasty.template.ZhuXian;
import com.manu.dynasty.util.DataLoader;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.bag.BagGrid;
import com.qx.bag.EquipGrid;
import com.qx.task.TaskData;
import com.qx.test.main.GameClient;
import com.qx.test.main.Main;

import qxmobile.protobuf.BagOperProtos.EquipAddReq;
import qxmobile.protobuf.ChongLouPve.ChongLouBattleResult;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.Explore.ExploreReq;
import qxmobile.protobuf.FuWen.FuwenEquipAll;
import qxmobile.protobuf.FuWen.QueryFuwen;
import qxmobile.protobuf.GameTask.GetTaskReward;
import qxmobile.protobuf.GameTask.TaskInfo;
import qxmobile.protobuf.GameTask.TaskList;
import qxmobile.protobuf.GameTask.TaskProgress;
import qxmobile.protobuf.JewelProtos.EquipOperationReq;
import qxmobile.protobuf.JiNengPeiYang.UpgradeJiNengReq;
import qxmobile.protobuf.JunZhuProto.TalentUpLevelReq;
import qxmobile.protobuf.LieFuProto.LieFuActionReq;
import qxmobile.protobuf.MibaoProtos.MibaoActivate;
import qxmobile.protobuf.PveLevel.GetPassZhangJieAwardReq;
import qxmobile.protobuf.PveLevel.GetPveStarAward;
import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Shop.BuyGoodReq;
import qxmobile.protobuf.Shop.ShopReq;
import qxmobile.protobuf.Shop.WubeiFangBuy;
import qxmobile.protobuf.UserEquipProtos.EquipJinJie;
import qxmobile.protobuf.UserEquipProtos.EquipStrengthReq;
import qxmobile.protobuf.ZhanDou.BattleYouXiaResultReq;
import qxmobile.protobuf.ZhanDou.PvpZhanDouInitReq;
import qxmobile.protobuf.ZhanDou.YouXiaZhanDouInitReq;

public class TestTask extends TestBase{
	public static int which = 0;
	String[] path = {
			"F:/workspace/Design Doc/data/ZhuXian.xml",//kjh 0
			"E:/workinyxg/Design Doc/data/ZhuXian.xml",//lzw 1
			"E:/workspace/Design Doc/data/ZhuXian.xml",//wxh 2
			"F:/Design Doc/data/ZhuXian.xml",//jy 3
	};
	public HashMap<Integer,Integer> tryIds = new HashMap<>();//已尝试过的任务id，避免重复尝试。
	public static Map<Integer, ZhuXian> map;
	public static Map<Integer, PveTemp> pveMap = new HashMap<>();
	public TestTask() {
		if(map != null){
			return;
		}
		DataLoader dl = new DataLoader("com.manu.dynasty.template.", "");
		try{
			InputStream in = new FileInputStream(path[which]);
			List<ZhuXian> taskConf = null;
			taskConf = (List<ZhuXian>) dl.loadFromStream(in);
			in.close();
			map = taskConf.stream().collect(Collectors.toMap(ZhuXian::getId, t->t));
			//
			in = new FileInputStream(path[which].replace("ZhuXian", "PveTemp"));
			List<PveTemp> ptl = (List<PveTemp>) dl.loadFromStream(in);
			in.close();
			pveMap = ptl.stream().collect(Collectors.toMap(t->t.id, t->t));
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	@Override
	public void req(GameClient cl) {
		super.req(cl);
		cl.session.write(PD.C_TaskReq);
	}
	
	@Override
	public void handle(int id, IoSession session, Builder builder, GameClient cl) {
		//super.handle(id, session, builder, cl);
		TaskList.Builder ret = (TaskList.Builder)builder;
		System.out.println(session.getId()+"收到任务信息:"+ret.getListCount());
		if(ret.getListCount()==0)return;
		ret.getListList().stream()
			.forEach(t->{
				ZhuXian conf = map.get(t.getId());
				if(conf == null){
					System.out.println("任务没有找到:"+t.getId());
					System.exit(0);
				}
				if(conf.type==0){//主线
					tryZhuXian(conf, t,cl);
				}
			});
	}
	public void tryZhuXian(ZhuXian conf, TaskInfo t, GameClient cl) {
		if(conf.id == 100404){
			conf.id+=0;
		}
		Integer preProg = tryIds.get(conf.id);
		if(preProg != null && preProg.intValue() == t.getProgress()){
			return;
		}
		tryIds.put(conf.id, t.getProgress());
		System.out.println(conf.title+"--- 进度  -- "+t.getProgress()+"--- TYPE  -- "+conf.doneType);
		if(conf.title.contains("升至25级")){
			System.out.println("到此为止。");
			Main.autoDone(cl);
			return;
		}
		if(t.getProgress() == -1){
			GetTaskReward.Builder request = GetTaskReward.newBuilder();
			request.setTaskId(t.getId());
			if(t.getId() == 100360){
				cl.session.removeAttribute("STARUP");
			}
			if(t.getId() == 200065){
				cl.session.removeAttribute("LIEFU");
			}
			cl.session.write(new ProtobufMsg(PD.C_GetTaskReward, request));
			System.out.println("尝试领取奖励");
			cl.session.removeAttribute("TIMES");
			if(conf.doneType == TaskData.finish_yunbiao_x){
				cl.enterScene();//运镖结束回主城，防止运镖场景太多人。
			}
			return;
		}
		switch(conf.doneType){
		case TaskData.PVE_GUANQIA:
			tryGuanQia(cl,conf , false);
			break;
		case TaskData.DIALOGUE:
			clientUpdate(cl,conf);
			break;
		case TaskData.one_quality_ok:
			tryWearEquip(cl,conf);
			break;
		case TaskData.ONE_QIANG_HAU:
			qiangHua(cl,conf);
			break;
		case TaskData.GET_PVE_AWARD:
			lingQuStarAward(cl,100103,1013);
			break;
		case TaskData.get_miBao_x_pinZhi_y:
//			jiHuoMiBao(cl,conf);
			cl.session.write(PD.NEW_MIBAO_INFO);
			System.out.println("获取秘宝信息");
			break;
		case TaskData.get_item:
			tryGetItem(cl,conf);
			break;
		case TaskData.mibao_shengji_x:
			cl.session.write(PD.C_MIBAO_INFO_REQ);
			break;
		case TaskData.get_pass_PVE_zhang_award:
			getZhangJieJL(cl,1);
			break;
		case TaskData.FINISH_BAIZHAN_N:
			reqPvpMainInfo(cl);
			enterBaiZhan(cl);
			cl.reqAlliance();
			break;
		case TaskData.pay_weiWang:
			getShopInf(cl);
			shopBuy(cl);
			break;
		case TaskData.MIBAO_HECHENG:
			activeMB(cl);
			break;
		case TaskData.get_achieve:
			getChJiuPage(cl);
			break;
		case TaskData.jinJie_jueSe_jiNeng:
			askForJiNeng(cl, 1201);
			break;
		case TaskData.jueSe_x_jiNeng_n:
			askForJiNeng(cl, 1201);
			askForJiNeng(cl, 1301);
			break;
		case TaskData.get_miShu_pinZhi_y:
			activeMiShu(cl);
			break;
		case TaskData.go_youxia:
			cl.session.write(PD.C_YOUXIA_INFO_REQ);
			youXiaPass(cl, 300401);
			break;
		case TaskData.battle_shiLian_II:
			youXiaPass(cl, conf);
			break;
		case TaskData.tianfu_level_up:
			upTianFu(cl);
			break;
		case TaskData.done_qianChongLou:
			doneChongLou(cl);
			cl.reqAlliance();
			break;
		case TaskData.use_baoShi_x:
			XQBaoShi(cl);
			break;
		case TaskData.done_wuBeiChouJiang:
			wuBeiChou(cl);
			break;
		case TaskData.finish_yunbiao_x:
			doneYaBiao(cl);
			break;
		case TaskData.FINISH_CHUANQI:
			tryGuanQia(cl, conf, true);
			break;
		case TaskData.mibao_shengStar:
			cl.session.setAttribute("STARUP", 1);
			jiangHunStarUp(cl);
			break;
		case TaskData.junzhu_level_up:
			int guanQiaId = cl.lasdPveId == 0 ? 100307 : cl.lasdPveId;
			tryGuanQia(cl, guanQiaId, false);
			break;
		case TaskData.done_lieFu_x:
			doneLieFu(cl);
			break;
		case TaskData.wear_fushi:
			useFuWen(cl);
			break;
		case TaskData.get_mbSuiPian_x_y:
			tryGuanQia(cl, 100207, false);
			break;
		}
		
	}
	public void useFuWen(GameClient cl ){
		FuwenEquipAll.Builder req = FuwenEquipAll.newBuilder();
		req.setTab(1);
		cl.session.write(req.build());
	}
	public void doneLieFu( GameClient cl ){
		LieFuActionReq.Builder req = LieFuActionReq.newBuilder();
		req.setType(1);
		ProtobufMsg msg = new ProtobufMsg(PD.LieFu_Action_req , req);
		cl.session.write(msg);
		
		QueryFuwen.Builder initFuWen = QueryFuwen.newBuilder();
		initFuWen.setTab(1);
		cl.session.write(initFuWen.build());
	}
	public void doneYaBiao( GameClient cl ){
		EnterScene.Builder req = EnterScene.newBuilder();
		req.setUid(cl.uid);
		req.setJzId(cl.jzId);
		req.setSenderName(cl.accountName);
		req.setPosX(-190);
		req.setPosZ((float)-26.47);
		ProtobufMsg msg = new ProtobufMsg(PD.Enter_YBScene, req);
		cl.session.write(msg);
		cl.session.write(PD.C_YABIAO_INFO_REQ);
		cl.session.write(PD.C_YABIAO_MENU_REQ);
		cl.session.write(PD.C_YABIAO_REQ);
		System.out.println("发送押镖请求");
	}
	public void wuBeiChou( GameClient cl ){
		WubeiFangBuy.Builder request = WubeiFangBuy.newBuilder();
		request.setType(1);
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.C_WUBEIFANG_BUY ;
		msg.builder = request;
		cl.session.write(msg);
		System.out.println("发送武备抽奖");
	}
	public void XQBaoShi( GameClient cl ){
		System.out.println("尝试完成宝石镶嵌任务");
		List<EquipGrid> equips = (List<EquipGrid>)cl.session.getAttribute("EQUIP");
			if( equips != null ){
				EquipGrid dao = equips.get(3);
				if(dao != null ){
					EquipOperationReq.Builder req = EquipOperationReq.newBuilder();
					req.setType(2);
					req.setEqulpId(dao.dbId);
					cl.session.write(req.build());
				}
			}else{
				System.out.println("没有装备信息，发送装备信息请求");
				cl.session.write(PD.C_EquipInfo);
			}
	}
	public void jiangHunStarUp(GameClient cl){
		ExploreReq.Builder req = ExploreReq.newBuilder();
		req.setType(4);
		send(cl,PD.EXPLORE_REQ,req);
		tryIds.clear();
		req(cl);
		System.out.println("请求元宝十连探宝");
	}
	public void doneChongLou(GameClient cl ){
		ChongLouBattleResult.Builder req = ChongLouBattleResult.newBuilder();
		req.setLayer(1);
		req.setResult(true);
		ProtobufMsg msg = new ProtobufMsg();
		msg.id = PD.CHONG_LOU_BATTLE_REPORT;
		msg.builder = req ;
		cl.session.write(msg);
		System.out.println("发送通关重楼");
	}
	public void upTianFu(GameClient cl){
		TalentUpLevelReq.Builder req = TalentUpLevelReq.newBuilder();
		req.setPointId(101);
		cl.session.write(req.build());
		System.out.println("发送升级天赋");
	}
	
	public void youXiaPass(GameClient cl , int youXiaId){
		BattleYouXiaResultReq.Builder request = BattleYouXiaResultReq.newBuilder();
		request.setId(youXiaId);
		request.setResult(1);
		request.setScore(80);
		cl.session.write(request.build());
		System.out.println("发送游侠通关"+youXiaId);
	}
	public void youXiaPass(GameClient cl , ZhuXian conf){
		String youXiaStr = "300"+conf.doneCond+"01";
		int youXiaId = Integer.parseInt(youXiaStr);
		BattleYouXiaResultReq.Builder request = BattleYouXiaResultReq.newBuilder();
		request.setId(youXiaId);
		request.setResult(1);
		request.setScore(80);
		cl.session.write(request.build());
		System.out.println("发送游侠通关"+youXiaId);
	}
	public void activeMiShu(GameClient cl){
		cl.session.write(PD.NEW_MIBAO_INFO);//请求这个，服务器需要打标记。
		cl.session.write(PD.NEW_MISHU_JIHUO);
//		cl.session.write(PD.NEW_MIBAO_INFO);
		System.out.println("发送秘术激活信息");
	}
	public void askForJiNeng(GameClient cl , int jiNengId){
		UpgradeJiNengReq.Builder request = UpgradeJiNengReq.newBuilder();
		request.setSkillID(jiNengId);
		cl.session.write(request.build());
		System.out.println("发送技能学习"+jiNengId);
	}
	public void getChJiuPage(GameClient cl) {
		cl.session.write(PD.C_ACTIVITY_ACHIEVEMENT_INFO_REQ);
		tryIds.clear();
		req(cl);
		System.out.println("请求成就列表");
	}
	public void activeMB(GameClient cl) {
		MibaoActivate.Builder req = MibaoActivate.newBuilder();
		req.setTempId(102);
		send(cl,PD.C_MIBAO_ACTIVATE_REQ,req);
		System.out.println("请求合成秘宝");
	}
	public void shopBuy(GameClient cl) {
		BuyGoodReq.Builder req = BuyGoodReq.newBuilder();
		req.setGoodId(1001);
		req.setType(4);
		send(cl,PD.HY_BUY_GOOD_REQ,req);
		System.out.println("请求购买");
	}
	public void getShopInf(GameClient cl) {
		ShopReq.Builder req = ShopReq.newBuilder();
		req.setType(40);
		send(cl,PD.HY_SHOP_REQ,req);
		System.out.println("请求商铺信息");
	}
	public void reqPvpMainInfo(GameClient cl) {
		cl.session.write(PD.BAIZHAN_INFO_REQ);
		System.out.println("请求百战信息");
	}
	public void enterBaiZhan(GameClient cl) {
		PvpZhanDouInitReq.Builder req = PvpZhanDouInitReq.newBuilder();
		req.setUserId(-2411);
		send(cl,PD.ZHANDOU_INIT_PVP_REQ,req);		
		System.out.println("请求进入百战");
	}
	public void getZhangJieJL(GameClient cl,int i) {
		GetPassZhangJieAwardReq.Builder req = GetPassZhangJieAwardReq.newBuilder();
		req.setZhangJieId(i);
		send(cl,PD.get_passZhangJie_award_req,req);		
		System.out.println("尝试领取章节奖励:"+i);
	}
	public void tryGetItem(GameClient cl , ZhuXian conf) {
		if( "301011".equals(conf.doneCond)){
			ExploreReq.Builder req = ExploreReq.newBuilder();
			req.setType(3);
			send(cl,PD.EXPLORE_REQ,req);
			System.out.println("请求元宝单抽，探宝。");
		}else if("920118".equals(conf.doneCond)){
			cl.session.setAttribute("LIEFU", 200065);
			cl.session.write(PD.LieFu_Action_Info_Req);
		}
		
	}
	public void tryGetItem(GameClient cl ) {
		ExploreReq.Builder req = ExploreReq.newBuilder();
		req.setType(3);
		send(cl,PD.EXPLORE_REQ,req);
		System.out.println("请求元宝单抽，探宝。");
	}
	public void jiHuoMiBao(GameClient cl, long l) {
		if(l==0){
			cl.session.write(PD.S_SEND_MIBAO_INFO);
			return;
		}
		ErrorMessage.Builder req = (ErrorMessage.newBuilder());
//		String str = req.getErrorDesc();
//		long dbId = Long.parseLong(str);
		req.setCmd(0);
		req.setErrorCode(0);
		req.setErrorDesc(""+l);
		send(cl,PD.NEW_MIBAO_JIHUO,req);
//		cl.session.write(PD.NEW_MIBAO_INFO);
		cl.session.setAttribute("LastMBID", l);
		System.out.println("尝试激活秘宝"+l);
	}
	public void lingQuStarAward(GameClient cl, int id, int star) {
		GetPveStarAward.Builder b = GetPveStarAward.newBuilder();
		b.setGuanQiaId(id);
//		b.setGuanQiaId(100103);
		b.setIsChuanQi(false);
//		b.setSStarNum(1013);
		b.setSStarNum(star);
		send(cl,PD.PVE_STAR_REWARD_GET , b);
	}
	public void qiangHua(GameClient cl, ZhuXian conf) {
		//EquipStrengthReq.Builder req = EquipStrengthReq.newBuilder();
		//send(cl,,req);
		cl.session.write(PD.C_EQUIP_UPALLGRADE);
		System.out.println("请求强化。");
	}
	public void tryWearEquip(GameClient cl, ZhuXian conf) {
		if("13:1".equals(conf.doneCond)){
			EquipAddReq.Builder req = EquipAddReq.newBuilder();
			req.setBagDBId(cl.equipDbId);
			send(cl, PD.C_EquipAdd, req);
			System.out.println("尝试穿装备:"+cl.equipDbId);
		}else{
			
			List<EquipGrid> equips = (List<EquipGrid>)cl.session.getAttribute("EQUIP");
			EquipJinJie.Builder req = EquipJinJie.newBuilder();
			if(equips != null ){
				EquipGrid eg = equips.get(4);
				if(eg != null){
					req.setEquipId(eg.dbId);
					List<BagGrid> bag = (List<BagGrid>)cl.session.getAttribute("BAG");
					if( bag != null){
						int cnt = 0;
						for( BagGrid bg: bag){
							if(bg.itemId == 101002){
								req.addCailiaoList(bg.dbId);
								cnt++;
							}
						}
						ProtobufMsg msg = new ProtobufMsg();
						msg.id = PD.C_EQUIP_JINJIE ;
						msg.builder = req ;
						if(cnt>0){
							cl.session.write(msg);
							System.out.println("尝试进阶");
						}else{
							tryIds.clear();
							cl.session.write(PD.C_BagInfo);
						}
					}else{
						cl.session.write(PD.C_BagInfo);
					}
				}else{
					cl.session.write(PD.C_EquipInfo);
				}
			}else{
				cl.session.write(PD.C_EquipInfo);
			}
		}
	}
	public void tryGuanQia(GameClient cl, ZhuXian conf , boolean isChuanQi) {
		Object o = cl.session.getAttribute("OVER");
		if(o != null ){
			System.out.println("=====================================");
			System.out.println("无法继续自动任务：体力耗尽");
			System.out.println("=====================================");
			Main.autoDone(cl);
			return;
		}
		int gid = Integer.parseInt(conf.doneCond);
		new TestGuanQiaInfo().sendReq(cl, gid , isChuanQi);
	}
	
	public void tryGuanQia(GameClient cl, int guanQiaId , boolean isChuanQi) {
		Object o = cl.session.getAttribute("OVER");
		if(o != null ){
			System.out.println("=====================================");
			System.out.println("无法继续自动任务：体力耗尽");
			System.out.println("=====================================");
			return;
		}
		new TestGuanQiaInfo().sendReq(cl, guanQiaId , isChuanQi);
	}
	public void clientUpdate(GameClient cl, ZhuXian conf) {
		TaskProgress.Builder req = TaskProgress.newBuilder();
		TaskInfo.Builder tb = TaskInfo.newBuilder();
		tb.setId(conf.id);
		tb.setProgress(1);
		req.setTask(tb);
		cl.session.write(new ProtobufMsg(PD.C_TaskProgress, req));
		System.out.println("尝试对话完成任务");
	}
}
