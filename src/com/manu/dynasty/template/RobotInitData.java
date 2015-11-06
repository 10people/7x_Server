package com.manu.dynasty.template;

import org.apache.mina.core.session.IoSession;

import qxmobile.protobuf.Scene.SpriteMove;

public class RobotInitData {
	public SpriteMove.Builder move;
	public IoSession session;
	public boolean directionB = false;
	private float posX;
	private float posY;
	private float posZ;
	
	private float max;
	private float min;
	
	private byte direction;
	
	private String name;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public byte getDirection() {
		return direction;
	}
	public void setDirection(byte directtion) {
		this.direction = directtion;
	}
	public float getPosX() {
		return posX;
	}
	public void setPosX(float posX) {
		this.posX = posX;
	}
	public float getPosY() {
		return posY;
	}
	public void setPosY(float posY) {
		this.posY = posY;
	}
	public float getPosZ() {
		return posZ;
	}
	public void setPosZ(float posZ) {
		this.posZ = posZ;
	}
	public float getMax() {
		return max;
	}
	public void setMax(float max) {
		this.max = max;
	}
	public float getMin() {
		return min;
	}
	public void setMin(float min) {
		this.min = min;
	}
}

