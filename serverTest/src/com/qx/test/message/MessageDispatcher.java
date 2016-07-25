package com.qx.test.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;

import pct.TestBase;
import pct.TestJiNengPeiYang;
import qxmobile.protobuf.ErrorMessageProtos.DataList;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.JewelProtos.EquipOperationResp;
import qxmobile.protobuf.JewelProtos.JewelInfo;
import qxmobile.protobuf.JewelProtos.JewelList;
import qxmobile.protobuf.JunZhuProto.JunZhuInfoRet;
import qxmobile.protobuf.LieFuProto.LieFuActionResp;
import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Scene.EnterSceneCache;
import qxmobile.protobuf.Scene.ExitScene;
import qxmobile.protobuf.Scene.SpriteMove;
import qxmobile.protobuf.VIP.RechargeReq;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.util.BaseException;
import com.manu.network.PD;
import com.manu.network.ParsePD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.hero.WuJiang;
import com.qx.test.main.GameClient;
import com.qx.test.main.Main;
import com.qx.test.main.WuJiangTest;


public class MessageDispatcher {
	private GameClient client;
	
	public MessageDispatcher(GameClient c) {
		super();
		this.client = c;
	}
	public static Map<Integer, TestBase> handlerMap = new HashMap<Integer, TestBase>();
	public static void listen(int id, TestBase tb){
		handlerMap.put(id, tb);
	}
	public  void msgDispatcher(int id, Builder builder, IoSession session) {
		try {
			switch (id) {
			case PD.S_ERROR:
				ErrorMessage.Builder em = (qxmobile.protobuf.ErrorMessageProtos.ErrorMessage.Builder) builder;
				System.out.println("服务器返回错误信息:"+em.getCmd()+","+em.getErrorDesc());
				if("今天购买次数达到最大，不能再次购买体力".equals(em.getErrorDesc())){
					System.out.println("尝试获取君主信息");
					JunZhuInfoRet.Builder jzInfo = (JunZhuInfoRet.Builder)session.getAttachment();
					if( jzInfo != null ){
						System.out.println("获取到君主信息，君主VIP等级为"+jzInfo.getVipLv());
					}
					int vip = jzInfo == null ?0 : jzInfo.getVipLv();
					if(vip < 7 ){
						RechargeReq.Builder czreq = RechargeReq.newBuilder();
						czreq.setType(9);
						czreq.setAmount(648);
						ProtobufMsg msg = new ProtobufMsg(PD.C_RECHARGE_REQ, czreq);
						session.write(czreq.build());
					}else{
						session.setAttribute("OVER", true);
					}
				}
				break;
			case PD.ACC_REG_RET:
				client.regResult(session, builder);
				break;
			case PD.ACC_LOGIN_RET:
				client.loginRet(session,id,builder);
				break;
			case PD.ROLE_NAME_RESPONSE:
				client.randNameRet(builder);
				break;
			case PD.RANKING_RESP:
				client.rcvRank();
				break;
			case PD.CREATE_ROLE_RESPONSE:
				client.createRoleRet(builder);
				break;
			case PD.S_InitProc:
				client.antiCheat(builder);
				break;
			case PD.Enter_Scene_Confirm:
				client.enterSceneRet(builder);
				break;
			case PD.Spirite_Move:{
				fakeMove(client,builder);
			}
//				System.out.println("move");
				break;
			case PD.Enter_Scene:{
//				EnterScene.Builder enterSc = (EnterScene.Builder )builder;
//				System.out.printf("A收到进入场景通知 - %s uid %d\n",
//						enterSc.getSenderName(),enterSc.getUid());
			}
				break;
			case PD.Exit_YBScene:
			case PD.Exit_Scene:{
				ExitScene.Builder b = (ExitScene.Builder)builder;
				//System.out.printf("退出%s\n", b.getUid());
				if(client.masterUid == b.getUid()){
					client.masterUid = 0;
				}
			}
				break;
			case PD.HERO_INFO:
				WuJiangTest.readList(session,builder);
				break;
			case PD.WUJIANG_TECHINFO_RESP:
				WuJiangTest.readKeJi(session,builder);
				break;
//			case PD.PVE_GuanQia_Info:
//				WuJiangTest.readGuanQiaInfo(session, builder);
//				break;
			case PD.S_GET_JINENG_PEIYANG_QUALITY_RESP:
				TestJiNengPeiYang.readList(session,builder);
				break;
			case PD.S_BIAOCHE_STATE:
				//TODO
				break;
			case PD.Enter_YBScene:{
				if(builder instanceof EnterSceneCache.Builder ){
					EnterSceneCache.Builder ss = (EnterSceneCache.Builder )builder;
					EnterScene cc = EnterScene.parseFrom(ss.getBody().toByteArray());
					if(cc.getRoleId()<5){
						client.masterUid = cc.getUid();
					}
				}
//				System.out.printf("B收到进入场景通知 - %s uid %d\n",
//						ss.getSenderName(),ss.getUid());
				break;
			}
			case PD.C_GET_BAO_XIANG	:{
				ErrorMessage.Builder info = (ErrorMessage.Builder)builder	;
				System.out.printf("拾取宝箱成功，获得%d元宝\n",info.getErrorCode());
				break;
			}
			case PD.OPEN_ShiLian_FuBen:
			{
//				ErrorMessage.Builder info = (ErrorMessage.Builder)builder	;
//				System.out.printf("十连副本信息:%d/%d,%sS\n",
//						info.getErrorCode(),info.getCmd(), info.getErrorDesc());
			}
				break;
			case PD.PVE_PAGE_RET:
				client.pveRet(builder);
				break;
			case PD.S_HEAD_INFO:
				System.out.println("S_HEAD_INFO");
				break;
			case PD.S_Send_Chat:
				System.out.println("S_Send_Chat");
				break;
			case PD.S_Broadcast:
				System.out.println("S_Broadcast");
				break;
			case PD.S_CHOOSE_SCENE:
			{
				ErrorMessage.Builder ret = (ErrorMessage.Builder)builder ;
				System.out.println("已指定进入副本id:"+ret.getErrorCode());
				break;
			}
			case PD.S_SCENE_GETALL:
			{
				System.out.print("场景ID列表为：");
				DataList.Builder ret = (DataList.Builder)builder ;
				List<ErrorMessage.Builder> list = ret.getDataBuilderList();
				
				if(list != null){
					for(ErrorMessage.Builder value : list){
						System.out.print(value.getErrorCode()+", 人数为："+ value.getCmd() +"。");
					}
				}
				System.out.println("");
			}				
				break;
			case PD.S_EQUIP_BAOSHI_RESP:
			{
				EquipOperationResp.Builder resp = (EquipOperationResp.Builder) builder;
				JewelList jb = resp.getJewelList();
				System.out.print(jb.getEqulpId()+"    ");
				System.out.println(jb.getJewelNum()+"    ");
				List <JewelInfo> jl = jb.getListList();
				for( JewelInfo j : jl ){
					System.out.print(j.getItemId()+"    ");
					System.out.print(j.getEqulpId()+"    ");
					System.out.print(j.getPossionId()+"    ");
					System.out.println("");
				}
			}
				break;
			case PD.S_BAG_CHANGE_INFO:
				client.readBag(builder);
				break;
			case PD.S_SEND_MIBAO_INFO:
				client.readMBV2(builder);
				break;
//			case PD.S_MIBAO_INFO_RESP:
//				client.readMB(builder);
//				break;
			case PD.S_ACTIVITY_ACHIEVEMENT_INFO_RESP:
				client.readChengJiuList(builder);
				break;
			case PD.S_BagInfo:
				System.out.println("收到背包信息");
				client.saveBag(builder);
				break;
			case PD.S_EquipInfo:
				System.out.println("收到装备信息");
				client.saveEquip(builder);
				break;
			case PD.LieFu_Action_req:
				LieFuActionResp.Builder resp = (LieFuActionResp.Builder)builder;
				if(resp.getResult() == 1){
					client.session.write(PD.C_BUY_TongBi);
				}
				client.session.write(PD.C_TaskReq);
				break;
			default:
				TestBase tb = handlerMap.get(id);
				if(tb != null){
					tb.handle(id, session, builder,client);
					break;
				}
				if(client.log)System.out.println("未处理的消息:"+id+"->"+ParsePD.getName(id) +"-->"+ (builder == null ? "" : 
					builder.getDefaultInstanceForType().getClass().getSimpleName())
					);
				if(builder != null)System.out.println(builder.toString());
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void fakeMove(GameClient c, Builder builder) {
		if(11>1){
//			c.move();
//			return;
		}
		SpriteMove.Builder move = (SpriteMove.Builder)builder;
		int moverUid = move.getUid();
		if(c.masterUid < moverUid){
			c.masterUid = moverUid;
		}
//		c.masterUid = 5401;
		//跟着固定的主人走。
		if(c.masterUid != move.getUid()){
//			return;
		}
		long m = System.currentTimeMillis();
		if(m - c.lastMoveTime<200){
			return;
		}else{
			c.lastMoveTime = m;
//			c.move();
			return;
		}
//		move.setPosX(move.getPosX()-(c.uid-c.masterUid)/80f);
//		c.lastMoveTime = m;
//		c.session.write(builder.build());
//		c.session.write(builder.build());
		
		//System.out.println(c.accountName+" fake move:"+move.getPosX()+" -->"+c.masterUid);
	}

}
