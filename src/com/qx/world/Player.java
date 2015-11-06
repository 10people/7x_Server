package com.qx.world;

import org.apache.mina.core.session.IoSession;

import qxmobile.protobuf.PlayerData;
import qxmobile.protobuf.PlayerData.State;

public class Player extends Sprite{
	public long jzId;
	public IoSession session;
	public int userId;
	public int roleId;//模型Id
	public State pState;
	public String chengHaoId;
	public String lmName;
	public int vip;
	public int zhiWu;
	public Player(){
		pState = PlayerData.State.State_LOADINGSCENE;
	}

	
}
