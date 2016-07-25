package com.qx.robot;

import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qxmobile.protobuf.Scene.EnterScene;
import qxmobile.protobuf.Scene.SpriteMove;
import qxmobile.protobuf.Scene.SpriteMove.Builder;

import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.template.RobotInitData;
import com.manu.network.PD;
import com.qx.world.Scene;
/**
 * 机器人针对目前只能跑直线的机器人。
 * 如果将来换成记录路线的方式，再做优化。
 * @author hudali
 *
 */
public class RobotProtoType implements Runnable{
	public static RobotProtoType inst;
	public static Logger log = LoggerFactory.getLogger(RobotInitData.class);
	
	public Scene scene;
	public static int INTERVAL = 180;
	
	public float speed = 0.007f;
	public List<Object> initData;
	
	public RobotProtoType(List<Object> initData, Scene scene){
		inst = this;
		this.scene = scene;
		this.initData = initData;
//		enterScene();
	}
	
	public void enterScene() {
		for(Object o : initData){
			RobotInitData d = (RobotInitData) o;
			d.session = new RobotSession();
			d.move = SpriteMove.newBuilder();
			
			d.move.setPosX(d.posX);
			d.move.setPosY(d.posY);
			d.move.setPosZ(d.posZ);
			
			EnterScene.Builder enter = EnterScene.newBuilder();
			enter.setUid(0);
			enter.setSenderName(d.name);
			
			enter.setPosX(d.posX);
			enter.setPosY(d.posY);
			enter.setPosZ(d.posZ);
			
			this.scene.exec(PD.Enter_Scene, d.session, enter);
		}
	}

	@Override
	public void run() {
		
	}
	public void run0() {
		while (GameServer.shutdown==false) {
			try {
				Thread.sleep(INTERVAL);
				for(Object o : initData){
					RobotInitData d = (RobotInitData) o;
					move(d);
				}
			} catch (Exception e) {
				log.error("机器人线程执行Z移动出错 : {}", e);
			}
		}
		log.info("退出机器人线程");
	}
	public void move(RobotInitData d) {
		speed = 0.0075f;
		float max = d.max;
		float min = d.min;
		scene.exec(PD.Spirite_Move, d.session, d.move);
		switch (d.direction) {
			case DIRECT_X:
				moveX(d,max, min);
				break;
			case DIRECT_Y:
				moveY(d.move,max, min);
				break;
			case DIRECT_Z:
				moveZ(d,max, min);
				break;
			default:
				log.error("unkown direction code {}", d.direction);
				break;
		}
	}
	
	
	
	public void moveZ(RobotInitData d, float max, float min) {
		float moveRatio = -1;
				if (d.directionB) {
					if (d.move.getPosZ() <= max) {
						moveRatio = 1;
					}else {
						d.directionB = false;
						moveRatio = -1;
					}
				}else{
					if (d.move.getPosZ() >= min) {
						moveRatio = -1;
					}else{
						moveRatio = 1;
						d.directionB = true;
					}
				}
				d.move.setPosZ(d.move.getPosZ() + speed * INTERVAL * moveRatio);
				
	}

	public void moveY(Builder move, float max, float min) {
		log.error("暂时不会有Y移动");
	}

	public void moveX(RobotInitData d, float max, float min) {
		boolean direction = d.directionB;
		Builder move = d.move;
				int moveRatio;
				if (direction) {
					if (move.getPosX() <= max) {
						move.setPosX(move.getPosX() + speed * INTERVAL);
						moveRatio = 1;
					}else {
						direction = false;
						move.setPosX(move.getPosX() - speed * INTERVAL);
						moveRatio = -1;
					}
				}else{
					if (move.getPosX() >= min) {
						moveRatio = -1;
						move.setPosX(move.getPosX() - speed * INTERVAL);
					}else{
						moveRatio = 1;
						direction = true;
						move.setPosX(move.getPosX() + speed * INTERVAL);
					}
				}
				d.directionB = direction;
				//move.setPosX(move.getPosX() + speed * timeSpan * moveRatio);
				
				
	}

	public static final byte DIRECT_X = 0;
	public static final byte DIRECT_Y = 1;
	public static final byte DIRECT_Z = 2;
}
