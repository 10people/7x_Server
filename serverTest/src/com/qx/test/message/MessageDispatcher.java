package com.qx.test.message;

import org.apache.mina.core.session.IoSession;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;

import com.google.protobuf.MessageLite.Builder;
import com.manu.dynasty.util.BaseException;
import com.manu.network.PD;
import com.qx.hero.WuJiang;
import com.qx.test.main.GameClient;
import com.qx.test.main.WuJiangTest;


public class MessageDispatcher {
	private GameClient client;
	
	public MessageDispatcher(GameClient c) {
		super();
		this.client = c;
	}

	public  void msgDispatcher(int id, Builder builder, IoSession session) {
		try {
			switch (id) {
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
			case PD.Enter_Scene_Confirm:
				client.enterSceneRet(builder);
				break;
			case PD.Spirite_Move:
			case PD.Enter_Scene:
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
				WuJiangTest.readJunZhuInfo(session, builder);
				break;
			default:
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
