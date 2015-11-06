package com.qx.yabiao;

import org.apache.mina.core.session.IoSession;

public class YBRobot{
//	public SpriteMove.Builder move;
	public IoSession session;
	public boolean isBattle=false;//默认未进入战斗
	public long jzId;
	public String name;
	public String lmName;
	public int protectCD;
	public long endBattleTime;
	public long startTime;
	public long totalTime;//到达终点的理想时间
	public long usedTime;//到达下一点的理想时间
	/*进入战斗的时间*/
	public long battleStart;
	public int pathId; ;//路线
	public int horseType; ;//马车品质
//	public int birthPlace;//出生地
//	public int endPlace; ;//终点
//	public float startPosX;
//	public float startPosY;
//	public float startPosZ;	
//	public int direction;//下一个节点
//	public int lastPosition;//上一个节点
//	public float posX;
//	public float posY;
//	public float posZ;
//	public float endPosX;
//	public float endPosY;
//	public float endPosZ;	
}

