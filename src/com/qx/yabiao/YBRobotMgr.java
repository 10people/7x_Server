package com.qx.yabiao;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.boot.GameServer;
import com.manu.dynasty.template.CartPath;
import com.manu.dynasty.template.YunbiaoTemp;
import com.manu.network.BigSwitch;
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.qx.world.Scene;
/**
 * 
 * @author yuquanshui 
 *
 */
public class YBRobotMgr implements Runnable{
	public static Logger log = LoggerFactory.getLogger(YBRobotMgr.class);
	public static int INTERVAL = 100;
	public static boolean isShowLog = false;
	public static YBRobotMgr inst;
	
	/**
	 * yabiaoRobotMap 押镖机器人线程管理YaBiaoRobot的Map
	 */
	public ConcurrentHashMap<Long, Object> yabiaoRobotMap;
	public static HashMap<Integer, HashMap<Integer, RoadNode>> road;//双层map第一层存路线第二层节点
	public static HashMap<Integer,Double> distanceMap;
	public YBRobotMgr(){
		inst = this;
		initData();
		new Thread(this, "ybrobotMgr").start();
	}
	public void initData(){
		yabiaoRobotMap= new  ConcurrentHashMap<Long, Object>();
		road= new  HashMap<Integer, HashMap<Integer, RoadNode>>();
		List<CartPath> list = TempletService.listAll(CartPath.class.getSimpleName());
		double distance4All=0;
		int	pathId=0;
		int baimaTotalTime=YunbiaoTemp.cartTime*1000;//TODO 普通车走到终点的时间60秒 (假设，需根据配置改)
		HashMap<Integer,Double> distMap=new HashMap<Integer, Double>();
		HashMap<Integer, RoadNode> roadPath=null;
		int lsize=list.size();
		for (int i= 0; i < lsize; i++) {
			CartPath c = list.get(i);
			if(pathId==0){
				pathId=c.id;
			}
			if(i+1==lsize){
				distMap.put(pathId,distance4All);
				log.info("路线--{}的路程为--{}。。。",pathId,distance4All);
				log.info("所有路线路程计算完成");
				break;
			}
			CartPath cNext = list.get(i+1);
			if(c.id!=cNext.id){
				if(distance4All>0){
					distMap.put(pathId,distance4All);
					log.info("路线--{}的路程为--{}",pathId,distance4All);
				}
				pathId=cNext.id;
				distance4All=0;
				continue;
			}
			//计算路程
			double distance=Math.sqrt((cNext.x-c.x)*(cNext.x-c.x)+(cNext.z-c.z)*(cNext.z-c.z));
			distance4All+=distance;
		}
		YBRobotMgr.distanceMap=distMap;
		pathId=0;
		for (int i= 0; i < lsize; i++) {
			CartPath c = list.get(i);
			if((pathId>0&&pathId!=c.id)||(i+1==lsize)){
				road.put(pathId, roadPath);
			}
			//计算路程
			if(pathId!=c.id){
				pathId=c.id;
				roadPath=new HashMap<Integer, RoadNode>();
			}
			RoadNode node=new RoadNode();
			node.pathId=c.id;
			node.nodeId=c.position;
			node.posX=c.x;
			node.posZ=c.z;
			CartPath cNext =null;
			if(i+1<lsize){
				//计算去下一点所需时间
				double distance=0;
				cNext = list.get(i+1);
				if(c.id==cNext.id){
					//计算路程
					distance=Math.sqrt((cNext.x-c.x)*(cNext.x-c.x)+(cNext.z-c.z)*(cNext.z-c.z));
					//TODO 初始化节点 算出每个节点的时间 结合 cartTemp对象算出来totaltime 还有速度
					Double distance4all=distMap.get(node.pathId);
					if(distance4all==null){
						log.info("distance4all==null");
					}
					node.totalTime=baimaTotalTime*(distance/distance4all);
				}
			}
			roadPath.put(node.nodeId,node);
		}
		log.info("押镖机器人路线数据加载完成");
//		HashMap<Integer, RoadNode>	roadPath1=road.get(1);
//		Iterator iter = roadPath1.entrySet().iterator();
//		while (iter.hasNext()) {
//				Map.Entry entry = (Map.Entry) iter.next();
//				Object key = entry.getKey();
//				RoadNode val = (RoadNode) entry.getValue();
//				System.out.println(key+"----"+val.posX+"=="+val.posZ);
//		}
//		HashMap<Integer, RoadNode>	roadPath2=road.get(2);
//		Iterator iter2 = roadPath2.entrySet().iterator();
//		while (iter2.hasNext()) {
//			Map.Entry entry = (Map.Entry) iter2.next();
//			Object key = entry.getKey();
//			RoadNode val = (RoadNode) entry.getValue();
//			System.out.println(key+"----"+val.posX+"=="+val.posZ);
//		}
//		HashMap<Integer, RoadNode>	roadPath3=road.get(3);
//		Iterator iter3 = roadPath3.entrySet().iterator();
//		while (iter3.hasNext()) {
//			Map.Entry entry = (Map.Entry) iter3.next();
//			Object key = entry.getKey();
//			RoadNode val = (RoadNode) entry.getValue();
//			System.out.println(key+"----"+val.posX+"=="+val.posZ);
//		}
//		HashMap<Integer, RoadNode>	roadPath4=road.get(4);
//		Iterator iter4 = roadPath4.entrySet().iterator();
//		while (iter4.hasNext()) {
//			Map.Entry entry = (Map.Entry) iter4.next();
//			Object key = entry.getKey();
//			RoadNode val = (RoadNode) entry.getValue();
//			System.out.println(key+"----"+val.posX+"=="+val.posZ);
//		}
	}

	@Override
	public void run() {
		while (!GameServer.shutdown) {
			try {
				Thread.sleep(INTERVAL);
				robotsMove();
			} catch (Exception e) {
				log.error("押镖机器人线程执行押镖移动出错 : {}", e);
			}
		}
		log.info("退出押镖机器人线程");
	}
	//移动所有镖车
	public void robotsMove() {
		if(BigSwitch.inst.ybMgr.yabiaoScenes.size()==0){
			return;
		}
		Iterator<?> it= BigSwitch.inst.ybMgr.yabiaoScenes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Integer scId =(Integer) entry.getKey();
//			Scene ybsc =(Scene) entry.getValue();
			Iterator<?> itrobot=yabiaoRobotMap.entrySet().iterator();
			while (itrobot.hasNext()) {
				Map.Entry entryRobot = (Map.Entry) itrobot.next();
				YaBiaoRobot ybr = (YaBiaoRobot) entryRobot.getValue();
				if(ybr==null){
					log.error("押镖 场景机器人出错,(YaBiaoRobot) entryRobot.getValue()为空");
					continue;
				}
				move(ybr,scId,itrobot);
				//镖车不会进入战斗了

			}
		}
	}
	//移动实际为扣除已走的时间（路程已定，速度恒定，则可按照时间来计算坐标）
	public void move(YaBiaoRobot ybr,int scId,Iterator<?> itrobot) {
		long now = System.currentTimeMillis();
		long usedtime=now-ybr.startTime;
		if(ybr.protectTime>0&&(ybr.protectTime-usedtime>0)){
			ybr.protectTime-=usedtime;
		}else{
			ybr.protectTime=0;
		}
		long upSpeed2UsedTime=(long) (now-ybr.startTime2upSpeed);
		if(ybr.speed>1){
			if(upSpeed2UsedTime>=ybr.upSpeedTime){
				//加速时间用完
				ybr.speed=1;
				ybr.startTime2upSpeed=now;
				ybr.upSpeedTime=0;
			}else{
				ybr.startTime2upSpeed=now;
				ybr.upSpeedTime-=upSpeed2UsedTime;
			}
		}else{
			ybr.upSpeedTime=0;
		}
		long  usedtime4short=now-ybr.startTime4short;
		if(ybr.speed>1){
			usedtime*=ybr.speed;
			upSpeed2UsedTime*=ybr.speed;
			usedtime4short*=ybr.speed;
		}
		ybr.usedtime4short+=usedtime4short;
		ybr.usedTime+=usedtime;
		ybr.startTime=now;
		if(ybr.usedtime4short>=ybr.totaltime4short){
			if(isShowLog){
				int baifenbi=(int)((ybr.usedTime/(double)ybr.totalTime)*100);
				log.info("机器人镖车,移动目标坐标为 {}--{}-{},进度{}%,移动节点id--{},从--{}-{}走向--{}--{},镖车君主ID--《{}》,已用时间为{}-{},",
						ybr.move.getPosX(),ybr.move.getPosY(),ybr.move.getPosZ(),
						baifenbi,ybr.nodeId, ybr.startPosX,ybr.startPosZ,
						ybr.nextPosX,ybr.nextPosZ,ybr.jzId,
						ybr.usedTime,ybr.totalTime);
			}
			HashMap<Integer, RoadNode> roadPath=road.get(ybr.pathId);
			if(roadPath==null){
				log.error("机器人镖车移动--{}出错,未找到pathId=={}的路线配置",ybr.jzId,ybr.pathId);
				return;
			}
			//跳到下一节点
			ybr.nodeId+=1;
			if(ybr.nodeId>=roadPath.size()){
				log.info("机器人镖车到达最后一个节点{}--{}-{}",ybr.move.getPosX(),ybr.move.getPosY(),ybr.move.getPosZ());
				ybr.posX=ybr.nextPosX;
				ybr.posZ=ybr.nextPosZ;
				ybr.move.setPosX((float) ybr.posX);
				ybr.move.setPosZ((float) ybr.posZ);
				// 移除押镖人的相关信息
				YaBiaoHuoDongMgr.inst.settleYaBiaoSuccess(ybr.jzId,scId);
				//移除马车
				itrobot.remove();
				return;
			}
			RoadNode rd=roadPath.get(ybr.nodeId);
			if(rd==null){
				log.error("押镖机器人移动错误,节点rd==null");
				return;
			}
			ybr.usedtime4short=ybr.usedtime4short-ybr.totaltime4short;
			//重置一小段路起点终点
			ybr.startPosX=ybr.nextPosX;
			ybr.startPosZ=ybr.nextPosZ;
			RoadNode rd4next=roadPath.get(ybr.nodeId+1);
			if(rd4next==null){
				log.error("押镖机器人移动错误,节点rd4next==null");
				return;
			}
			ybr.nextPosX=rd4next.posX;
			ybr.nextPosZ=rd4next.posZ;
			double totaltime4Nextshort=rd.totalTime;// TODO 从路线对象里面取出来
			ybr.totaltime4short=totaltime4Nextshort;
			ybr.startTime4short=now;
		}

		if(ybr.totaltime4short==0){
			log.error("押镖机器人移动错误,ybr.totaltime4short==0,~~~~~~~~~~~~~~~~~~~~~~~~");
			return;
		}else{
			//计算当前坐标
			ybr.posX=(float) (ybr.startPosX+(ybr.nextPosX-ybr.startPosX)*(ybr.usedtime4short/ybr.totaltime4short));
			ybr.posZ=(float) (ybr.startPosZ+(ybr.nextPosZ-ybr.startPosZ)*(ybr.usedtime4short/ybr.totaltime4short));
		}
		ybr.move.setPosX(ybr.posX);
		ybr.move.setPosZ(ybr.posZ);
//		float dir=0; 2015年12月4日不需要后台算方向 需要的话可以根据斜率转换
//		if((ybr.nextPosX-ybr.startPosX)!=0){
//			dir=(ybr.nextPosZ-ybr.startPosZ)/ (ybr.nextPosX-ybr.startPosX);
//		}
//		ybr.move.setDir(dir);
		ybr.startTime4short=System.currentTimeMillis();
		Scene sc = (Scene) ybr.session.getAttribute(SessionAttKey.Scene);
		if(sc==null){
			log.info("未找到镖车=={}所在的场景",ybr.jzId);
			return;
		}
		sc.exec(PD.Spirite_Move, ybr.session, ybr.move);
		YaBiaoHuoDongMgr.inst.broadBiaoCheInfo(sc, ybr);
	}

}
