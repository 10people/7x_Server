package com.qx.explore.treasure;

import java.sql.ClientInfoStatus;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.manu.dynasty.base.TempletService;
import com.manu.dynasty.template.AnnounceTemp;
import com.manu.dynasty.template.ShiLianFuBen;
import com.manu.dynasty.util.DateUtils;
import com.manu.network.PD;
import com.manu.network.TXSocketMgr;
import com.manu.network.msg.ProtobufMsg;
import com.qx.event.ED;
import com.qx.event.Event;
import com.qx.event.EventMgr;
import com.qx.event.EventProc;
import com.qx.junzhu.JunZhu;
import com.qx.persistent.HibernateUtil;
import com.qx.robot.RobotSession;
import com.qx.util.TableIDCreator;
import com.qx.world.BroadcastMgr;

import qxmobile.protobuf.ErrorMessageProtos.ErrorMessage;
import qxmobile.protobuf.PlayerData.PlayerState;
import qxmobile.protobuf.Scene.EnterScene;

public class ExploreTreasureMgr extends EventProc {
	public static float posZ = 0;
	public static ProtobufMsg lastInfo;
	public Logger log = LoggerFactory.getLogger(ExploreTreasureMgr.class.getSimpleName()); 
	
	public static ExploreTreasureMgr inst = null;
	public AtomicInteger aliveBaoXiangCnt = new AtomicInteger(0);
	public AtomicInteger delay = new AtomicInteger(0);//每次刷宝箱延迟10秒
	
	public BaoXiangScene scene;
	public boolean debug = false;
	
	public ConcurrentLinkedQueue<BaoXiangBean> queue = new ConcurrentLinkedQueue<>();
	
	public ExploreTreasureMgr() {
		inst = this;
		buildQueue();
	}
	public void buildQueue() {
		List<BaoXiangBean> list = HibernateUtil.list(BaoXiangBean.class," where amount>0 order by openTime");
		queue.addAll(list);
	}
	public void makeScene(){
		scene = new BaoXiangScene("BaoXiang");
		scene.startMissionThread();
	}
	
	
	public BaoXiangBean initTreasure(JunZhu jz) {
		BaoXiangBean treasure = null; 
			treasure = new BaoXiangBean();
			treasure.id = TableIDCreator.getTableID(BaoXiangBean.class, 1);
			treasure.jzId = jz.id;
			treasure.jzName = jz.name;
			treasure.chouJiangTime = new Date();
			treasure.openTime = makeOpenTime();
			treasure.amount = 1;
			treasure.total = 1;
			HibernateUtil.insert(treasure);
		return treasure;
	}

	public Date makeOpenTime() {
		Calendar c = Calendar.getInstance();
		if(debug){
			c.add(Calendar.SECOND, 5);
		}else{
			c.add(Calendar.MINUTE, 5);
		}
		return c.getTime();
	}
	@Override
	public void proc(Event param) {
		switch(param.id){
		case ED.tanbao_tenTimes:
			//探宝10次
			trigger(param);
			break;
		}
	}

	public void trigger(Event event) {
		Object[]	obs = (Object[])event.param;
		long jzId = (Long)obs[0];	
		JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
		BaoXiangBean bean = initTreasure(jz);
		int preSize = queue.size();
		queue.add(bean);
		log.info("{}:{},trigger",jz.id,jz.name);
		//[ffffff]恭喜[-][dbba8f]*玩家名字七个字*[-][ffffff]十连探宝，[-][dbba8f]5[-][ffffff]分钟后将开启“[-][e5e205]宝藏副本[-][ffffff]”福利，请大家做好准备！[-]
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		if(confList != null){
			int confId = 20;
			if(preSize > 0){
				//[ffffff]恭喜[-[dbba8f]*玩家名字七个字*[-][ffffff]十连探宝，增加了“[-][e5e205]宝藏副本[-][ffffff]”内的宝箱波数，快去抢宝箱吧！[-]
				confId = 21;
			}
			Optional<AnnounceTemp> conf = confList.stream().filter(t->t.type==20).findFirst();
			if(conf.isPresent()){
				AnnounceTemp t = conf.get();
				String c = t.announcement.replace("*玩家名字七个字*", jz.name);
				BroadcastMgr.inst.send(c);
			}
		}
	}
	public ProtobufMsg buildOpenInfo(BaoXiangBean bean) {
		ProtobufMsg msg = new ProtobufMsg(PD.OPEN_ShiLian_FuBen);
		ErrorMessage.Builder b = ErrorMessage.newBuilder();
		b.setErrorCode(bean.amount);
		b.setCmd(bean.total);
		long s = System.currentTimeMillis();
		long t = bean.openTime.getTime() - s;
		t/=1000;
		t = Math.max(t, delay.get());
		if(aliveBaoXiangCnt.get()>0){
			t = 0;
		}
		b.setErrorDesc(""+t);
		msg.builder = b;
		return msg;
	}
	@Override
	protected void doReg() {
		EventMgr.regist(ED.tanbao_tenTimes, this);//探宝10连抽
	}
	public void baoXiangPicked(){
		int left = aliveBaoXiangCnt.decrementAndGet();
		if(left > 0){
			return;//仍然有宝箱存活着
		}
		if(delay.get()>0){
			return;
		}
		if(queue.size()>0){
			//10秒后刷下一波
			delay.set(10);
			return;
		}
		//没有后续宝箱了，通知结束
		ErrorMessage.Builder b = ErrorMessage.newBuilder();
		b.setErrorCode(0);
		b.setCmd(0);
		ProtobufMsg msg = new ProtobufMsg(PD.OPEN_ShiLian_FuBen, b);
		TXSocketMgr.inst.acceptor.broadcast(msg);
		lastInfo = msg;
	}
	public void checkQueue() {
		//如果有，就每分钟产生1波
		BaoXiangBean bean = queue.peek();
		if(bean == null){
			return;
		}
		if(aliveBaoXiangCnt.get()>0){
			return;
		}
		if(delay.get()>0){
			delay.decrementAndGet();
		}
		if(TXSocketMgr.inst == null || TXSocketMgr.inst.acceptor == null){
			return;
		}
		ProtobufMsg msg = buildOpenInfo(bean);
		TXSocketMgr.inst.acceptor.broadcast(msg);
		lastInfo = msg;
		
		if(delay.get()>0){
			return;
		}
		
		if(System.currentTimeMillis()<bean.openTime.getTime()){//时间尚未达到
			return;
		}
		
		bean.amount--;
		HibernateUtil.update(bean);
		
		
		genBaoXiang(bean);
		if(bean.amount<=0){
			queue.poll();//移除掉
			log.info("此宝箱结束 {} of {}",bean.id,bean.jzId);
		}
	}
	public void genBaoXiang(BaoXiangBean bean) {
		log.info("开始产生 {}, {}", bean.id,bean.amount+1);
		int alive = aliveBaoXiangCnt.get();
		if(alive<0){
			aliveBaoXiangCnt.compareAndSet(alive, 0);
		}
		//
		List<ShiLianFuBen> list = TempletService.listAll(ShiLianFuBen.class.getSimpleName());
		if(list == null){
			log.error("没有配置");
			return;
		}
		int[] yuanBaoArr = list.stream().mapToInt(t->t.yuanbaoNum).toArray();
		Collections.shuffle(list);//打乱顺序，以达到随机位置的目的。
		int len = yuanBaoArr.length;
		for(int i=0;i<len;i++){
			int yuanBao = yuanBaoArr[i];
			ShiLianFuBen conf = list.get(i);
			EnterScene.Builder enter = EnterScene.newBuilder();
			enter.setUid(0);
			enter.setSenderName(bean.jzName+"的十连宝箱");
			enter.setPosX(conf.zuobiaoX);
			enter.setPosY(conf.zuobiaoY);
			enter.setPosZ(posZ);
			RobotSession session = new RobotSession();
			session.setAttribute("ForceRoleId", 600);
			session.setAttribute("YuanBaoCnt", yuanBao);
			scene.exec(PD.Enter_Scene, session, enter);
			//
			PlayerState.Builder pb = PlayerState.newBuilder();
			pb.setSState(qxmobile.protobuf.PlayerData.State.State_LEAGUEOFCITY);
			scene.exec(PD.PLAYER_STATE_REPORT, session, pb);
			aliveBaoXiangCnt.incrementAndGet();
			log.info("产生 {}",enter.getSenderName());
		}
		//
	}
	public void playerEnter(IoSession session) {
		BaoXiangBean bx = ExploreTreasureMgr.inst.queue.peek();
		if(bx != null || aliveBaoXiangCnt.get()>0){
			session.write(lastInfo);
		}		
	}
	
}
