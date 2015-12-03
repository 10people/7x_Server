package com.qx.world;

import org.apache.mina.core.session.IoSession;

import qxmobile.protobuf.PlayerData;
import qxmobile.protobuf.PlayerData.State;

public class Player extends Sprite{
	public long jzId;
	public int allianceId;
	public IoSession session;
	public int userId;
	public int roleId;//模型Id
	public State pState;
	public String chengHaoId;
	public String lmName;
	public int vip;
	public int zhiWu;
	public int safeArea;//安全区编号1-4 0和负数表示不在安全区
	public Player(){
		pState = PlayerData.State.State_LOADINGSCENE;
	}

	
}
