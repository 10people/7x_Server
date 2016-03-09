package com.qx.yabiao;

import org.apache.mina.core.session.IoSession;

import qxmobile.protobuf.Scene.SpriteMove;

public class YaBiaoRobot{
	public SpriteMove.Builder move;
	public IoSession session;
	public boolean isBattle=false;//默认未进入战斗
	public long jzId;
	public int jzLevel;
	public String name;
	public int protectTime;//镖车保护时间
	public long endBattleTime;
	public long startTime;
	public long totalTime;//到达终点的理想时间
	public long usedTime;//到达下一点的理想时间
	/*进入战斗的时间*/
	public int pathId;//路线
	public int nodeId;//路线节点Id
	public int horseType; ;//马车品质
	public float startPosX;
	public float startPosZ;	
	public long startTime4short;
	public double  usedtime4short;//一小段路所用时间
	public double  totaltime4short;//一小段路总共所需时间
	public float nextPosX;
	public float nextPosZ;	
	public float posX;
	public float posZ;	
	public double speed;//速度：普通速度的几倍表示
	public long startTime2upSpeed;//开始加速的时间
	public int upSpeedTime;	//加速时长
	public int  hp;
	public int maxHp;
	public int  hudun;
	public int  hudunMax;
	public int worth;
	public int bcNPCId;//镖车NPC配置Id
	public int bcNPCNo;//镖车NPC编号 2016年1月20日需求变更用的那20辆镖车编号
	public int zhanli;
	public int guojiaId;
	public YBCartAttr4Fight cartAttr4Fight;
}

