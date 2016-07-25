package com.manu.dynasty.template;

import org.apache.mina.core.session.IoSession;

import qxmobile.protobuf.Scene.SpriteMove;

public class RobotInitData {
	public SpriteMove.Builder move;
	public IoSession session;
	public boolean directionB = false;
	public float posX;
	public float posY;
	public float posZ;
	
	public float max;
	public float min;
	
	public byte direction;
	
	public String name;
}

