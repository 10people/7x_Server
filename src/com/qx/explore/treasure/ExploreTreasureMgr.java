package com.qx.explore.treasure;

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
import com.manu.network.PD;
import com.manu.network.SessionAttKey;
import com.manu.network.TXSocketMgr;
import com.manu.network.msg.ProtobufMsg;
import com.qx.account.AccountManager;
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
	public static int staySeconds = 60;//可以捡宝箱的秒数。
	public static int maxPlayerCnt = 20;
	public static int playerCntLimit = maxPlayerCnt+10;
	public static int pickCntLimit = 10;
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
		IoSession session = AccountManager.sessionMap.get(jzId);
		if(session == null){
			return;
		}
		Object o = session.removeAttribute("FreeShiLian");
		if(o != null){
			////免费十连不触发十连副本
			return;
		}
		JunZhu jz = HibernateUtil.find(JunZhu.class, jzId);
		BaoXiangBean bean = initTreasure(jz);
		int preSize = queue.size();
		queue.add(bean);
		log.info("{}:{},trigger",jz.id,jz.name);
		//[ffffff]恭喜[-][dbba8f]*玩家名字七个字*[-][ffffff]十连探宝，[-][dbba8f]5[-][ffffff]分钟后将开启“[-][e5e205]宝藏副本[-][ffffff]”福利，请大家做好准备！[-]
		List<AnnounceTemp> confList = TempletService.listAll(AnnounceTemp.class.getSimpleName());
		if(confList != null){
			Optional<AnnounceTemp> conf = confList.stream().filter(t->t.type==20).findFirst();
			if(preSize > 0 || aliveBaoXiangCnt.get()>0){
				//[ffffff]恭喜[-[dbba8f]*玩家名字七个字*[-][ffffff]十连探宝，增加了“[-][e5e205]宝藏副本[-][ffffff]”内的宝箱波数，快去抢宝箱吧！[-]
				conf = confList.stream().filter(t->t.type==21).findFirst();
			}
			if(conf.isPresent()){
				AnnounceTemp t = conf.get();
				String c = t.announcement.replace("*玩家名字七个字*", jz.name);
				BroadcastMgr.inst.send(c);
			}
		}
		syncInfo(queue.peek());
	}
	public ProtobufMsg buildOpenInfo(BaoXiangBean bean) {
		ProtobufMsg msg = new ProtobufMsg(PD.OPEN_ShiLian_FuBen);
		ErrorMessage.Builder b = ErrorMessage.newBuilder();
		b.setErrorCode(aliveBaoXiangCnt.get());//有多少宝箱存活，客户端能算出来。
		b.setCmd(queue.size());//还有多少
		long s = System.currentTimeMillis();
		long t = 10;
		if(bean != null){
			t = bean.openTime.getTime() - s; 
		}
		t/=1000;
		t = Math.max(t, delay.get());
		if(aliveBaoXiangCnt.get()>0){
			t = 0;
		}
		/*
		 */
		//拼接参数
		StringBuffer sb = new StringBuffer();
		sb.append("nextBXSec:");		sb.append(t);		sb.append("#");
		int st = scene.players.size()>playerCntLimit ? 100 : 0;
		//100满了，不让进；0可进
		sb.append("state:"); sb.append(st);sb.append("#");
		sb.append("staySec:"); sb.append(staySeconds+t);
		b.setErrorDesc(sb.toString());
//		b.setErrorDesc(""+t);
		msg.builder = b;
		return msg;
	}
	@Override
	protected void doReg() {
		EventMgr.regist(ED.tanbao_tenTimes, this);//探宝10连抽
	}
	public void baoXiangPicked(){
		do{
			int left = aliveBaoXiangCnt.decrementAndGet();
			if(left > 0){
				break;//仍然有宝箱存活着
			}
			
			if(delay.get()>0){
				break;
			}
			if(queue.size()>0){
				//10秒后刷下一波
				delay.set(10);
				break;
			}
		}while(false);
		//没有后续宝箱了，通知结束
		syncInfo(null);
	}
	public void dayReset(){
		do{
			BaoXiangBean b = queue.poll();
			if(b == null){
				break;
			}
			b.amount = -1000;
			HibernateUtil.update(b);
			log.info("day reset remove {}", b.id);
		}while(true);
		delay.set(0);
		syncInfo(null);
	}
	public void checkQueue() {
		{//////////这个是动态的
			playerCntLimit = maxPlayerCnt + aliveBaoXiangCnt.get();
		}
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
		do{
			if(delay.get()>0){
				break;
			}
			
			if(System.currentTimeMillis()<bean.openTime.getTime()){//时间尚未达到
				break;
			}
			
			bean.amount-=1;
			HibernateUtil.update(bean);
			
			genBaoXiang(bean);
			if(bean.amount<=0){
				queue.poll();//移除掉
				log.info("此宝箱结束 {} of {}",bean.id,bean.jzId);
			}
		}while(false);
		
		syncInfo(bean);
	}
	public void syncInfo(BaoXiangBean bean) {
		ProtobufMsg msg = buildOpenInfo(bean);
		TXSocketMgr.inst.acceptor.broadcast(msg);
		lastInfo = msg;
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
			enter.setSenderName(bean.jzName);
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

	/**
	 * 发送已经拾取宝箱的信息
	 * @param session
	 */
	public void sendPickedInfo(IoSession session) {
		Long jzId = (Long) session.getAttribute(SessionAttKey.junZhuId);
		if(jzId == null){
			return;
		}
		Date now = new Date();
		BXRecord bean = HibernateUtil.find(BXRecord.class, jzId);
		if(bean == null){
			bean = new BXRecord();
			bean.jzId = jzId;
			bean.resetTime = now;
			HibernateUtil.insert(bean);
		}else{//检查重置
			checkReset(bean, now);
		}
		ErrorMessage.Builder em = ErrorMessage.newBuilder();
		em.setCmd(pickCntLimit - bean.bxCnt);//可拾取多少个箱子
		em.setErrorCode(bean.yuanBao);//已拾取多少元宝
		session.write(new ProtobufMsg(PD.BAO_XIANG_PICKED_INFO, em));
	}
	
	public void checkReset(BXRecord bean, Date now) {
		//每日4点刷新，连个时间都前移4小时，然后判断是不是同一天
		int v1 = getDayOfYear(bean.resetTime);
		int v2 = getDayOfYear(now);
		if(v1 == v2){
			return;
		}
		log.info("重置 {} 拾取宝箱信息 cnt {}, 元宝 {}",bean.jzId, bean.bxCnt, bean.yuanBao);
		bean.resetTime = now;
		bean.bxCnt = 0;
		bean.yuanBao = 0;
		HibernateUtil.update(bean);
	}
	
	public int getDayOfYear(Date d){
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d);
		c1.add(Calendar.HOUR_OF_DAY, -4);
		int v1 = c1.get(Calendar.DAY_OF_YEAR);
		return v1;
	}
	public void palyerCntChange() {
		syncInfo(queue.peek());
	}
}
