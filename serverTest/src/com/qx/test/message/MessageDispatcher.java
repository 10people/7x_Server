package com.qx.test.message;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.session.IoSession;

import pct.TestBase;
import pct.TestJiNengPeiYang;
import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.JunZhuProto.JunZhuInfoRet;
import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Scene.ExitScene;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.util.BaseException;
import com.manu.network.PD;
import com.manu.network.msg.ProtobufMsg;
import com.qx.hero.WuJiang;
import com.qx.test.main.GameClient;
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
			case PD.CREATE_ROLE_RESPONSE:
				client.createRoleRet(builder);
				break;
			case PD.S_InitProc:
				client.antiCheat(builder);
				break;
			case PD.Enter_Scene_Confirm:
				client.enterSceneRet(builder);
				break;
			case PD.Spirite_Move:
				System.out.println("move");
				break;
			case PD.Enter_Scene:{
				EnterScene.Builder enterSc = (EnterScene.Builder )builder;
				System.out.printf("A收到进入场景通知 - %s uid %d\n",
						enterSc.getSenderName(),enterSc.getUid());
			}
				break;
			case PD.Exit_Scene:{
				ExitScene.Builder b = (ExitScene.Builder)builder;
				System.out.printf("退出%s\n", b.getUid());
			}
				break;
			case PD.HERO_INFO:
				WuJiangTest.readList(session,builder);
				break;
			case PD.WUJIANG_TECHINFO_RESP:
				WuJiangTest.readKeJi(session,builder);
				break;
			case PD.PVE_GuanQia_Info:
				WuJiangTest.readGuanQiaInfo(session, builder);
				break;
			case PD.JunZhuInfoRet:
				JunZhuInfoRet.Builder jzInfo = WuJiangTest.readJunZhuInfo(session, builder);
				client.jzId = jzInfo.getId();
				break;
			case PD.S_GET_JINENG_PEIYANG_QUALITY_RESP:
				TestJiNengPeiYang.readList(session,builder);
				break;
			case PD.S_BIAOCHE_STATE:
				//TODO
				break;
			case PD.Enter_YBScene:{
				EnterScene.Builder enterSc = (EnterScene.Builder )builder;
				System.out.printf("B收到进入场景通知 - %s uid %d\n",
						enterSc.getSenderName(),enterSc.getUid());
				break;
			}
			case PD.C_GET_BAO_XIANG	:{
				ErrorMessage.Builder info = (ErrorMessage.Builder)builder	;
				System.out.printf("拾取宝箱成功，获得%d元宝\n",info.getErrorCode());
				break;
			}
			case PD.OPEN_ShiLian_FuBen:
			{
				ErrorMessage.Builder info = (ErrorMessage.Builder)builder	;
				System.out.printf("十连副本信息:%d/%d,%sS\n",
						info.getErrorCode(),info.getCmd(), info.getErrorDesc());
			}
				break;
			default:
				TestBase tb = handlerMap.get(id);
				if(tb != null){
					tb.handle(id, session, builder);
					break;
				}
				if(client.log)System.out.println("未处理的消息:"+id+"->"+(builder == null ? "" : 
					builder.getDefaultInstanceForType().getClass().getSimpleName())
					);
//				System.out.println(builder.toString());
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
